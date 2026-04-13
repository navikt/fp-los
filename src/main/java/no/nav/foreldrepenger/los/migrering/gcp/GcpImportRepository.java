package no.nav.foreldrepenger.los.migrering.gcp;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveFiltreringDataDto;
import no.nav.foreldrepenger.los.statistikk.StatistikkEnhetYtelseBehandlingNøkkel;

import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilterNøkkel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.migrering.dto.BehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;
import no.nav.foreldrepenger.los.migrering.dto.GcpImportKvittering;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OrgDataDto;
import no.nav.foreldrepenger.los.migrering.dto.StatEnhetYtelseBehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.StatOppgaveFilterDataDto;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerNøkkel;
import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerNøkkel;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.organisasjon.GruppeTilknytningNøkkel;
import no.nav.foreldrepenger.los.organisasjon.GruppeTilknytningRelasjon;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;
import no.nav.foreldrepenger.los.statistikk.StatistikkEnhetYtelseBehandling;
import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilter;

/**
 * Lagrer data fra FSS-instans i GCP-instans. Bevarer PK fra FSS der relevant for idempotent sync.
 */
@ApplicationScoped
@Transactional
public class GcpImportRepository {

    private static final Logger LOG = LoggerFactory.getLogger(GcpImportRepository.class);
    private static final int BATCH_SIZE = 300;

    private EntityManager entityManager;

    @Inject
    public GcpImportRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    GcpImportRepository() {
        // For CDI
    }

    public GcpImportKvittering lagre(BulkDataWrapper bulkData) {
        if (Environment.current().isFss()) {
            throw new RuntimeException("MIGRERING (GCP): forsøkt lagring i FSS!");
        }
        LOG.info("MIGRERING (GCP): starter lagring");
        var importKvittering = new GcpImportKvittering.Builder();

        importKvittering.orgData(lagreOrganisasjonsData(bulkData.organisasjonData()));

        lagreOppgaveKøer(bulkData.oppgaveFiltrering(), importKvittering);

        importKvittering.behandlinger(lagreBehandlinger(bulkData.behandlinger()));

        lagreOppgaverReservasjoner(importKvittering, bulkData.aktiveOppgaver());
        lagreOppgaverReservasjoner(importKvittering, bulkData.inaktiveOppgaver());

        importKvittering.statistikkEnhetYtelseBehandling(lagreStatEnhetYtelseBehandling(bulkData.statistikkEnhetYtelseBehandling()));
        importKvittering.statistikkOppgaveFilter(lagreStatOppgaveFilter(bulkData.statistikkOppgaveFilter()));

        var kvittering = importKvittering.build();
        LOG.info(
            "MIGRERING (GCP): lagret {} enheter, {} køer, {} behandlinger, {} oppgaver, {} reservasjoner, {} statistikkOppgaveFilter, {} statistikkEnhetYtelseBehandling",
            kvittering.orgData(), kvittering.oppgaveKøer(), kvittering.behandlinger(), kvittering.oppgaver(), kvittering.reservasjoner(),
            kvittering.statistikkOppgaveFilter(), kvittering.statistikkEnhetYtelseBehandling());
        return kvittering;
    }

    private void lagreOppgaverReservasjoner(GcpImportKvittering.Builder storedCounts, List<OppgaveDataDto> oppgaver) {
        if (oppgaver.isEmpty()) {
            storedCounts.oppgaver(0).reservasjoner(0);
            return;
        }

        int oppgaveCount = 0;
        int reservasjonCount = 0;

        for (int start = 0; start < oppgaver.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, oppgaver.size());
            List<OppgaveDataDto> batch = oppgaver.subList(start, end);

            var fssOppgaveIds = batch.stream().map(OppgaveDataDto::id).collect(Collectors.toSet());
            var gcpOppgaveMap = entityManager.createQuery("from Oppgave o where o.id in :ids", Oppgave.class)
                .setParameter("ids", fssOppgaveIds)
                .getResultStream()
                .collect(Collectors.toMap(Oppgave::getId, o -> o));

            for (var dto : batch) {
                if (!gcpOppgaveMap.containsKey(dto.id())) {
                    // vi merger + flusher de eksisterende først for effektiv batch
                    continue;
                }
                var behRef = entityManager.getReference(Behandling.class, dto.behandlingId());
                var oppgave = gcpOppgaveMap.get(dto.id());
                GcpImportMapper.mapOppgave(dto, behRef, oppgave);
                oppgaveCount++;
            }

            entityManager.flush();

            for (var dto : batch) {
                if (gcpOppgaveMap.containsKey(dto.id())) {
                    continue;
                }
                var behRef = entityManager.getReference(Behandling.class, dto.behandlingId());
                var oppgave = new Oppgave(behRef, dto.behandlendeEnhet());
                GcpImportMapper.mapOppgave(dto, behRef, oppgave);
                entityManager.merge(oppgave);
                oppgaveCount++;
                gcpOppgaveMap.put(oppgave.getId(), oppgave);
            }

            entityManager.flush();

            var gcpReservasjonerMap = entityManager.createQuery("from Reservasjon r where r.oppgave.id in :ids", Reservasjon.class)
                .setParameter("ids", fssOppgaveIds)
                .getResultStream()
                .collect(Collectors.toMap(r -> r.getOppgave().getId(), r -> r));

            for (var dto : batch) {
                var resDto = dto.reservasjonDataDto();
                if (resDto == null) {
                    continue;
                }

                var reservasjon = gcpReservasjonerMap.get(dto.id());
                var oppgave = gcpOppgaveMap.get(dto.id());
                var nyReservasjon = reservasjon == null;
                if (nyReservasjon) {
                   reservasjon = new Reservasjon(oppgave, resDto.reservertAv());
                }
                GcpImportMapper.mapReservasjon(resDto, reservasjon, oppgave);

                if (nyReservasjon) {
                    entityManager.persist(reservasjon);
                }

                reservasjonCount++;
            }

            entityManager.flush();
            entityManager.clear();
        }

        storedCounts.oppgaver(oppgaveCount).reservasjoner(reservasjonCount);
    }

    private int lagreOrganisasjonsData(OrgDataDto orgData) {
        if (orgData == null) return 0;

        var count = 0;

        for (var dto : orgData.avdelinger()) {
            var existing = entityManager.find(Avdeling.class, dto.avdelingEnhet());
            if (existing != null) {
                existing.setNavn(dto.navn());
                existing.setErAktiv(dto.aktiv());
                existing.setAvdelingEnhet(dto.avdelingEnhet());
                existing.setKreverKode6(dto.kreverKode6());
                GcpImportMapper.setBaseEntitetFields(existing, dto.opprettetAv(), dto.opprettetTidspunkt(), dto.endretAv(), dto.endretTidspunkt());
            } else {
                var avdeling = GcpImportMapper.mapAvdeling(dto);
                entityManager.persist(avdeling);
            }
            count++;
        }

        for (var dto : orgData.saksbehandlere()) {
            var existing = entityManager.find(Saksbehandler.class, dto.saksbehandlerIdent());
            if (existing != null) {
                existing.setNavn(dto.navn());
                existing.setAnsattVedEnhet(dto.ansattVedEnhet());
                GcpImportMapper.setBaseEntitetFields(existing, dto.opprettetAv(), dto.opprettetTidspunkt(), dto.endretAv(), dto.endretTidspunkt());
            } else {
                var saksbehandler = GcpImportMapper.mapSaksbehandler(dto);
                entityManager.persist(saksbehandler);
            }
            count++;
        }

        entityManager.flush();

        for (var dto : orgData.avdelingSaksbehandlere()) {
            var avdeling = entityManager.getReference(Avdeling.class, dto.avdelingId());
            var saksbehandler = entityManager.getReference(Saksbehandler.class, dto.saksbehandlerId());
            var key = new AvdelingSaksbehandlerNøkkel(saksbehandler, avdeling);

            var existing = entityManager.find(AvdelingSaksbehandlerRelasjon.class, key);
            if (existing == null) {
                var relationship = new AvdelingSaksbehandlerRelasjon(key);
                entityManager.persist(relationship);
            }
            count++;
        }

        for (var dto : orgData.saksbehandlerGrupper()) {
            var avdeling = dto.avdelingId() != null ? entityManager.getReference(Avdeling.class, dto.avdelingId()) : null;
            var existing = entityManager.find(SaksbehandlerGruppe.class, dto.id());
            if (existing == null && avdeling != null) {
                existing = entityManager.createQuery(
                        "from saksbehandlerGruppe sg where sg.gruppeNavn = :navn and sg.avdeling = :avdeling",
                        SaksbehandlerGruppe.class)
                    .setParameter("navn", dto.gruppeNavn())
                    .setParameter("avdeling", avdeling)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            }

            if (existing != null) {
                existing.setGruppeNavn(dto.gruppeNavn());
                if (avdeling != null) {
                    existing.setAvdeling(avdeling);
                }
                GcpImportMapper.setBaseEntitetFields(existing, dto.opprettetAv(), dto.opprettetTidspunkt(), dto.endretAv(), dto.endretTidspunkt());
            } else {
                var gruppe = new SaksbehandlerGruppe(dto.gruppeNavn(), avdeling);
                GcpImportMapper.setBaseEntitetFields(gruppe, dto.opprettetAv(), dto.opprettetTidspunkt(), dto.endretAv(), dto.endretTidspunkt());
                entityManager.persist(gruppe);
            }
            count++;
        }

        entityManager.flush();

        if (orgData.gruppeTilknytninger() != null) {
            for (var dto : orgData.gruppeTilknytninger()) {
                var saksbehandler = entityManager.find(Saksbehandler.class, dto.saksbehandlerId());
                var gruppe = entityManager.find(SaksbehandlerGruppe.class, dto.gruppeId());
                if (saksbehandler != null && gruppe != null) {
                    var nøkkel = new GruppeTilknytningNøkkel(saksbehandler, gruppe);
                    var existing = entityManager.find(GruppeTilknytningRelasjon.class, nøkkel);
                    if (existing == null) {
                        entityManager.persist(new GruppeTilknytningRelasjon(nøkkel));
                    }
                    count++;
                }
            }
        }

        return count;
    }

    private int lagreBehandlinger(List<BehandlingDataDto> behandlinger) {
        if (behandlinger.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (int start = 0; start < behandlinger.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, behandlinger.size());
            List<BehandlingDataDto> batch = behandlinger.subList(start, end);

            var batchIds = batch.stream().map(BehandlingDataDto::id).collect(Collectors.toSet());

            var gcpBehandlingMap = entityManager.createQuery("from Behandling b where b.id in :ids", Behandling.class)
                .setParameter("ids", batchIds)
                .getResultStream()
                .collect(Collectors.toMap(Behandling::getId, r -> r));

            for (BehandlingDataDto dto : batch) {
                var erNy = !gcpBehandlingMap.containsKey(dto.id());
                var behandling = erNy
                    ? new Behandling(dto.id(), new Saksnummer(dto.saksnummer().saksnummer()), dto.aktørId(),
                        dto.behandlendeEnhet(), dto.kildeSystem(), dto.fagsakYtelseType(),
                        dto.behandlingType(), dto.behandlingTilstand())
                    : gcpBehandlingMap.get(dto.id());
                GcpImportMapper.mapBehandling(dto, behandling);

                if (erNy) {
                    entityManager.persist(behandling);
                } else {
                    entityManager.merge(behandling);
                }

                count++;
            }

            entityManager.flush();
            entityManager.clear();
        }
        return count;
    }


    private void lagreOppgaveKøer(@NotNull List<@Valid OppgaveFiltreringDataDto> oppgaveFiltreringDto, GcpImportKvittering.Builder importKvittering) {
        if (oppgaveFiltreringDto == null) return;

        var fssOppgaveFiltreringId = oppgaveFiltreringDto.stream().map(OppgaveFiltreringDataDto::id).collect(Collectors.toSet());

        var gcpOppgaveFiltrering = entityManager.createQuery("select f.id from OppgaveFiltrering f where f.id in :ids", Long.class)
            .setParameter("ids", fssOppgaveFiltreringId)
            .getResultStream()
            .collect(Collectors.toSet());

        var antallKøer = 0;
        for (var dto : oppgaveFiltreringDto) {
            if (gcpOppgaveFiltrering.contains(dto.id())) {
                continue;
            }
            var avdeling = entityManager.getReference(Avdeling.class, dto.avdelingId());
            entityManager.persist(GcpImportMapper.mapOppgaveFiltrering(dto, avdeling));
            antallKøer++;
        }
        entityManager.flush();

        var gcpSaksbehandlerIdenter = entityManager.createQuery("select sb.saksbehandlerIdent from saksbehandler sb", String.class)
            .getResultList();
        var gcpFiltreringSaskbehandlerNøkkel = entityManager.createQuery("from FiltreringSaksbehandlerRelasjon", FiltreringSaksbehandlerRelasjon.class)
            .getResultStream()
            .map(r -> new FiltreringSaksbehandlerNøkkel(r.getSaksbehandler(), r.getOppgaveFiltrering()))
            .collect(Collectors.toSet());

        var antallFiltreringSaksbehandlerRelasjon = 0;

        for (var ofDto : oppgaveFiltreringDto) {
            for (var saksbehandlerIdent : ofDto.saksbehandlerIdenter()) {

                if (!gcpSaksbehandlerIdenter.contains(saksbehandlerIdent)) {
                    LOG.warn("MIGRERING (GCP): fant ikke saksbehandler {}, hopper over lagring av FiltreringSaksbehandlerRelasjon {}",
                        saksbehandlerIdent, ofDto.id());
                    continue;
                }

                var filtreringRef = entityManager.getReference(OppgaveFiltrering.class, ofDto.id());
                var saksbehandlerRef = entityManager.getReference(Saksbehandler.class, saksbehandlerIdent);
                var nøkkel = new FiltreringSaksbehandlerNøkkel(saksbehandlerRef, filtreringRef);
                if (!gcpFiltreringSaskbehandlerNøkkel.contains(nøkkel)) { // TODO: dette blir vel feil, her sammenlikner jeg nøkkel med ref med nøkkel med fulle entiteter
                    entityManager.persist(new FiltreringSaksbehandlerRelasjon(nøkkel));
                    antallFiltreringSaksbehandlerRelasjon++;
                }
            }
        }
        importKvittering
            .oppgaveKøer(antallKøer)
            .filtreringSaksbehandlerRelasjon(antallFiltreringSaksbehandlerRelasjon);
    }

    private int lagreStatEnhetYtelseBehandling(List<StatEnhetYtelseBehandlingDataDto> enhetYtelseDtos) {
        if (enhetYtelseDtos.isEmpty()) return 0;

        int countEnhetYtelseBehandling = 0;
        var aktuelleNøkler = enhetYtelseDtos.stream()
            .map(dto -> new StatistikkEnhetYtelseBehandlingNøkkel(dto.behandlendeEnhet(), dto.tidsstempel(), dto.fagsakYtelseType(), dto.behandlingType()))
            .collect(Collectors.toCollection(HashSet::new));

        entityManager.createQuery("select s.nøkkel from StatistikkEnhetYtelseBehandling s where s.nøkkel in (:nøkler)",
            StatistikkEnhetYtelseBehandlingNøkkel.class).setParameter("nøkler", aktuelleNøkler).getResultStream().forEach(aktuelleNøkler::remove);

        for (var dto : enhetYtelseDtos) {
            var nøkkel = new StatistikkEnhetYtelseBehandlingNøkkel(dto.behandlendeEnhet(), dto.tidsstempel(), dto.fagsakYtelseType(), dto.behandlingType());
            if (aktuelleNøkler.contains(nøkkel)) {
                var stat = new StatistikkEnhetYtelseBehandling(dto.behandlendeEnhet(), dto.tidsstempel(), dto.fagsakYtelseType(),
                    dto.behandlingType(), dto.statistikkDato(), dto.antallAktive(), dto.antallOpprettet(), dto.antallAvsluttet());
                entityManager.persist(stat);
                countEnhetYtelseBehandling++;
            }
        }

        return countEnhetYtelseBehandling;
    }


    private int lagreStatOppgaveFilter(List<StatOppgaveFilterDataDto> oppgaveFilterDtos) {
        if (oppgaveFilterDtos.isEmpty()) return 0;

        var nøklerForLagring = oppgaveFilterDtos.stream()
            .map(dto -> new StatistikkOppgaveFilterNøkkel(dto.oppgaveFilterId(), dto.tidsstempel()))
            .collect(Collectors.toCollection(HashSet::new));

        entityManager.createQuery("select s.nøkkel from StatistikkOppgaveFilter s where s.nøkkel in (:nøkler)", StatistikkOppgaveFilterNøkkel.class)
            .setParameter("nøkler", nøklerForLagring)
            .getResultStream() // stream av allerede lagrede nøkler
            .forEach(nøklerForLagring::remove);

        int countOppgaveFilter = 0;
        for (var dto : oppgaveFilterDtos) {
            var nøkkel = new StatistikkOppgaveFilterNøkkel(dto.oppgaveFilterId(), dto.tidsstempel());
            if (nøklerForLagring.contains(nøkkel)) {
                var stat = new StatistikkOppgaveFilter(dto.oppgaveFilterId(), dto.tidsstempel(),
                    dto.statistikkDato(), dto.antallAktive(), dto.antallTilgjengelige(),
                    dto.antallVentende(), dto.antallOpprettet(), dto.antallAvsluttet(), dto.innslagType());
                entityManager.persist(stat);
                countOppgaveFilter++;
            }
        }

        entityManager.flush();
        return countOppgaveFilter;
    }

}
