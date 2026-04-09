package no.nav.foreldrepenger.los.migrering.gcp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;
import no.nav.foreldrepenger.los.migrering.dto.GcpImportKvittering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.los.migrering.dto.BehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.KøOppsettDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OrgDataDto;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerNøkkel;
import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerNøkkel;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;

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

        try {
            importKvittering.orgData(lagreOrganisasjonsData(bulkData.organisasjonData()));
            importKvittering.oppgaveKøer(lagreOppgaveKøer(bulkData.køOppsettDto()));
            importKvittering.behandlinger(lagreBehandlinger(bulkData.behandlinger()));
            lagreOppgaverReservasjoner(importKvittering, bulkData.aktiveOppgaver());
            lagreOppgaverReservasjoner(importKvittering, bulkData.inaktiveOppgaver());
            // TODO: lagreKøStatistikk()
            importKvittering.kjørtUtenFeil(true);
        } catch (Exception e) {
            LOG.error("MIGRERING (GCP): feilet", e);
            importKvittering.kjørtUtenFeil(false);
        }

        var kvittering = importKvittering.build();
        LOG.info("MIGRERING (GCP): kjørtUtenFeil {}, lagret {} enheter, {} køer, {} behandlinger, {} oppgaver, {} reservasjoner", kvittering.kjørtUtenFeil(),
            kvittering.orgData(), kvittering.oppgaveKøer(), kvittering.behandlinger(), kvittering.oppgaver(), kvittering.reservasjoner());
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
                var behandlingId = dto.behandlingId().toUUID();
                var behRef = entityManager.getReference(Behandling.class, behandlingId);
                var oppgave = gcpOppgaveMap.get(dto.id());
                GcpImportMapper.mapOppgave(dto, behRef, oppgave);
                oppgaveCount++;
            }

            entityManager.flush();

            for (var dto : batch) {
                if (gcpOppgaveMap.containsKey(dto.id())) {
                    continue;
                }
                var behRef = entityManager.getReference(Behandling.class, dto.behandlingId().toUUID());
                var oppgave = new Oppgave();
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

                var reservasjon = gcpReservasjonerMap.get(resDto.oppgaveId());
                var oppgave = gcpOppgaveMap.get(resDto.oppgaveId());
                var nyReservasjon = reservasjon == null;
                if (nyReservasjon) {
                   reservasjon = new Reservasjon();
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
                var gruppe = new SaksbehandlerGruppe(dto.gruppeNavn());
                if (avdeling != null) {
                    gruppe.setAvdeling(avdeling);
                }
                GcpImportMapper.setBaseEntitetFields(gruppe, dto.opprettetAv(), dto.opprettetTidspunkt(), dto.endretAv(), dto.endretTidspunkt());
                entityManager.persist(gruppe);
            }
            count++;
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
                var behandling = gcpBehandlingMap.getOrDefault(dto.id(), new Behandling());
                GcpImportMapper.mapBehandling(dto, behandling);

                if (gcpBehandlingMap.containsKey(dto.id())) {
                    entityManager.merge(behandling);
                } else {
                    entityManager.persist(behandling);
                }

                count++;
            }

            entityManager.flush();
            entityManager.clear();
        }
        return count;
    }


    private int lagreOppgaveKøer(KøOppsettDto køOppsettDto) {
        if (køOppsettDto == null) return 0;

        var antallKøer = 0;
        var filtreringByImportId = new HashMap<Long, OppgaveFiltrering>();

        for (var dto : køOppsettDto.oppgaveFiltrering()) {
            var avdeling = dto.avdelingId() != null ? entityManager.getReference(Avdeling.class, dto.avdelingId()) : null;
            var filtrering = entityManager.find(OppgaveFiltrering.class, dto.id());
            var erNyFiltrering = filtrering == null;

            if (erNyFiltrering) {
                filtrering = new OppgaveFiltrering();
            }

            filtrering.setNavn(dto.navn());
            filtrering.setBeskrivelse(dto.beskrivelse());
            if (dto.køSortering() != null) {
                filtrering.setSortering(dto.køSortering());
            }
            if (avdeling != null) {
                filtrering.setAvdeling(avdeling);
            }
            filtrering.setFomDato(dto.fomDato());
            filtrering.setTomDato(dto.tomDato());
            if (dto.periodeFilter() != null) {
                filtrering.setPeriodefilter(dto.periodeFilter());
            }

            filtrering.setFiltreringBehandlingTyper(new HashSet<>(dto.behandlingTyper()));
            filtrering.setFiltreringYtelseTyper(new HashSet<>(dto.fagsakYtelseTyper()));
            var inkluder = dto.andreKriterier().stream()
                .filter(no.nav.foreldrepenger.los.migrering.dto.AndreKriterierDataDto::inkluder)
                .map(no.nav.foreldrepenger.los.migrering.dto.AndreKriterierDataDto::andreKriterierType)
                .collect(java.util.stream.Collectors.toSet());
            var ekskluder = dto.andreKriterier().stream()
                .filter(ak -> !ak.inkluder())
                .map(no.nav.foreldrepenger.los.migrering.dto.AndreKriterierDataDto::andreKriterierType)
                .collect(java.util.stream.Collectors.toSet());
            filtrering.setAndreKriterierTyper(inkluder, ekskluder);

            GcpImportMapper.setBaseEntitetFields(filtrering, dto.opprettetAv(), dto.opprettetTidspunkt(), dto.endretAv(), dto.endretTidspunkt());
            if (erNyFiltrering) {
                entityManager.persist(filtrering);
            }
            filtreringByImportId.put(dto.id(), filtrering);
            antallKøer++;
        }

        entityManager.flush();

        for (var sbDto : køOppsettDto.saksbehandlerKøer()) {
            var saksbehandler = entityManager.find(Saksbehandler.class, sbDto.saksbehandlerId());
            var filtrering = filtreringByImportId.get(sbDto.oppgaveFiltreringId());
            if (filtrering == null) {
                filtrering = entityManager.find(OppgaveFiltrering.class, sbDto.oppgaveFiltreringId());
            }
            if (saksbehandler != null && filtrering != null) {
                var nøkkel = new FiltreringSaksbehandlerNøkkel(saksbehandler, filtrering);
                var existing = entityManager.find(FiltreringSaksbehandlerRelasjon.class, nøkkel);
                if (existing == null) {
                    entityManager.persist(new FiltreringSaksbehandlerRelasjon(nøkkel));
                }
                antallKøer++;
            }
        }

        return antallKøer;
    }

}
