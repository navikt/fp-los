package no.nav.foreldrepenger.los.migrering.gcp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import no.nav.foreldrepenger.los.migrering.dto.BehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveFiltreringDataDto;
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
import no.nav.foreldrepenger.los.statistikk.StatistikkEnhetYtelseBehandlingNøkkel;
import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilter;
import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilterNøkkel;

/**
 * Lagrer data fra FSS-instans i GCP-instans. Bevarer PK fra FSS der relevant
 */
@ApplicationScoped
@Transactional
public class GcpImportRepository {

    private static final Logger LOG = LoggerFactory.getLogger(GcpImportRepository.class);

    private EntityManager entityManager;

    @Inject
    public GcpImportRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    GcpImportRepository() {
        // For CDI
    }

    public void lagre(BulkDataWrapper bulkData) {
        lagreOrganisasjonsData(bulkData.organisasjonData());
        lagreOppgaveFiltreringer(bulkData.oppgaveFiltrering());

        lagreBehandlinger(bulkData.behandlinger());
        lagreOppgaverReservasjoner(bulkData.aktiveOppgaver());
        lagreOppgaverReservasjoner(bulkData.inaktiveOppgaver());

        lagreStatEnhetYtelseBehandling(bulkData.statistikkEnhetYtelseBehandling());
        lagreStatOppgaveFilter(bulkData.statistikkOppgaveFilter());
    }

    private void lagreOppgaverReservasjoner(List<OppgaveDataDto> oppgaver) {
        if (oppgaver.isEmpty()) {
            return;
        }

        var fssOppgaveIds = oppgaver.stream().map(OppgaveDataDto::id).collect(Collectors.toSet());
        var gcpOppgaveId = entityManager.createQuery("select o.id from Oppgave o where o.id in :ids", Long.class)
            .setParameter("ids", fssOppgaveIds)
            .getResultStream()
            .collect(Collectors.toSet());
        var oppgaverLagret = new HashMap<Long, Oppgave>();

        for (var dto : oppgaver) {
            if (gcpOppgaveId.contains(dto.id())) {
                // antar ferdiglagret
                continue;
            }
            var behRef = entityManager.getReference(Behandling.class, dto.behandlingId());
            var oppgave = GcpImportMapper.mapOppgave(dto, behRef);
            entityManager.persist(oppgave);
            oppgaverLagret.put(oppgave.getId(), oppgave);
        }

        entityManager.flush();

        for (var dto : oppgaver) {
            var resDto = dto.reservasjonDataDto();
            if (resDto == null || !oppgaverLagret.containsKey(dto.id())) {
                continue;
            }
            var oppgave = oppgaverLagret.get(dto.id());
            var reservasjon = new Reservasjon(oppgave, resDto.reservertAv());
            GcpImportMapper.mapReservasjon(resDto, reservasjon, oppgave);
            entityManager.persist(reservasjon);
        }
    }

    // Lagrer Avdeling, Saksbehandler, AvdelingSaksbehandlerRelasjon, SaksbehandlerGruppe, GruppeTilknytningRelasjon
    private void lagreOrganisasjonsData(OrgDataDto orgData) {
        if (orgData == null) {
            return;
        }

        for (var dto : orgData.avdelinger()) {
            var existing = entityManager.find(Avdeling.class, dto.avdelingEnhet());
            if (existing != null) {
                continue;
            }
            var avdeling = GcpImportMapper.mapAvdeling(dto);
            entityManager.persist(avdeling);
        }
        entityManager.flush();

        for (var dto : orgData.saksbehandlere()) {
            var existing = entityManager.find(Saksbehandler.class, dto.saksbehandlerIdent());
            if (existing != null) {
                continue;
            }
            var saksbehandler = GcpImportMapper.mapSaksbehandler(dto);
            entityManager.persist(saksbehandler);
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
        }

        for (var dto : orgData.saksbehandlerGrupper()) {
            var avdelingRef = entityManager.getReference(Avdeling.class, dto.avdelingId());
            var existing = entityManager.find(SaksbehandlerGruppe.class, dto.id());
            if (existing != null) {
                continue;
            }
            var gruppe = GcpImportMapper.mapSaksbehandlerGruppe(dto, avdelingRef);
            entityManager.persist(gruppe);
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
                } else {
                    LOG.warn("MIGRERING (GCP): fant ikke saksbehandler {} eller gruppe {}", dto.saksbehandlerId(), dto.gruppeId());
                }
            }
        }
    }

    private void lagreBehandlinger(List<BehandlingDataDto> behandlinger) {
        if (behandlinger.isEmpty()) {
            return;
        }

        var fssBehandlingUuider = behandlinger.stream().map(BehandlingDataDto::id).collect(Collectors.toSet());
        var gcpBehandlingMap = entityManager.createQuery("select b.id from Behandling b where b.id in :ids", UUID.class)
            .setParameter("ids", fssBehandlingUuider)
            .getResultStream()
            .collect(Collectors.toSet());

        for (BehandlingDataDto dto : behandlinger) {
            if (gcpBehandlingMap.contains(dto.id())) {
                continue;
            }
            var behandling = GcpImportMapper.mapBehandling(dto);
            entityManager.persist(behandling);
        }
    }


    private void lagreOppgaveFiltreringer(List<OppgaveFiltreringDataDto> oppgaveFiltreringDto) {
        if (oppgaveFiltreringDto == null) {
            return;
        }

        var fssOppgaveFiltreringId = oppgaveFiltreringDto.stream().map(OppgaveFiltreringDataDto::id).collect(Collectors.toSet());
        var gcpOppgaveFiltreringId = entityManager.createQuery("select f.id from OppgaveFiltrering f where f.id in :ids", Long.class)
            .setParameter("ids", fssOppgaveFiltreringId)
            .getResultStream()
            .collect(Collectors.toSet());

        var avdelinger = entityManager.createQuery("from avdeling", Avdeling.class).getResultStream().collect(Collectors.toMap(Avdeling::getAvdelingEnhet, a -> a));
        var lagredeOF = new HashMap<Long, OppgaveFiltrering>();

        for (var dto : oppgaveFiltreringDto) {
            if (gcpOppgaveFiltreringId.contains(dto.id())) {
                continue;
            }
            var avdeling = avdelinger.get(dto.avdelingId());
            var oppgaveFiltrering = GcpImportMapper.mapOppgaveFiltrering(dto, avdeling);
            entityManager.persist(oppgaveFiltrering);
            lagredeOF.put(oppgaveFiltrering.getId(), oppgaveFiltrering);
        }
        entityManager.flush();

        for (var ofDto : oppgaveFiltreringDto) {
            if (lagredeOF.get(ofDto.id()) == null) {
                continue; // antar relasjoner er lagret fra før
            }
            for (var saksbehandlerIdent : ofDto.saksbehandlerIdenter()) {
                var saksbehandlerRef = entityManager.getReference(Saksbehandler.class, saksbehandlerIdent);
                var oppgaveFiltrering = lagredeOF.get(ofDto.id());
                var nøkkel = new FiltreringSaksbehandlerNøkkel(saksbehandlerRef, oppgaveFiltrering);
                entityManager.persist(new FiltreringSaksbehandlerRelasjon(nøkkel));
            }
        }
    }

    private void lagreStatEnhetYtelseBehandling(List<StatEnhetYtelseBehandlingDataDto> enhetYtelseDtos) {
        if (enhetYtelseDtos.isEmpty()) {
            return;
        }

        var aktuelleNøkler = enhetYtelseDtos.stream()
            .map(dto -> new StatistikkEnhetYtelseBehandlingNøkkel(dto.behandlendeEnhet(), dto.tidsstempel(), dto.fagsakYtelseType(), dto.behandlingType()))
            .collect(Collectors.toCollection(HashSet::new));

        entityManager.createQuery("select s.nøkkel from StatistikkEnhetYtelseBehandling s where s.nøkkel in (:nøkler)",
            StatistikkEnhetYtelseBehandlingNøkkel.class)
            .setParameter("nøkler", aktuelleNøkler)
            .getResultStream()
            .forEach(aktuelleNøkler::remove);

        for (var dto : enhetYtelseDtos) {
            var nøkkel = new StatistikkEnhetYtelseBehandlingNøkkel(dto.behandlendeEnhet(), dto.tidsstempel(), dto.fagsakYtelseType(), dto.behandlingType());
            if (aktuelleNøkler.contains(nøkkel)) {
                var stat = new StatistikkEnhetYtelseBehandling(dto.behandlendeEnhet(), dto.tidsstempel(), dto.fagsakYtelseType(),
                    dto.behandlingType(), dto.statistikkDato(), dto.antallAktive(), dto.antallOpprettet(), dto.antallAvsluttet());
                entityManager.persist(stat);
            }
        }
    }


    private void lagreStatOppgaveFilter(List<StatOppgaveFilterDataDto> oppgaveFilterDtos) {
        if (oppgaveFilterDtos.isEmpty()) {
            return;
        }

        var nøklerForLagring = oppgaveFilterDtos.stream()
            .map(dto -> new StatistikkOppgaveFilterNøkkel(dto.oppgaveFilterId(), dto.tidsstempel()))
            .collect(Collectors.toCollection(HashSet::new));

        entityManager.createQuery("select s.nøkkel from StatistikkOppgaveFilter s where s.nøkkel in (:nøkler)", StatistikkOppgaveFilterNøkkel.class)
            .setParameter("nøkler", nøklerForLagring)
            .getResultStream() // stream av allerede lagrede nøkler
            .forEach(nøklerForLagring::remove);

        for (var dto : oppgaveFilterDtos) {
            var nøkkel = new StatistikkOppgaveFilterNøkkel(dto.oppgaveFilterId(), dto.tidsstempel());
            if (nøklerForLagring.contains(nøkkel)) {
                var stat = new StatistikkOppgaveFilter(dto.oppgaveFilterId(), dto.tidsstempel(),
                    dto.statistikkDato(), dto.antallAktive(), dto.antallTilgjengelige(),
                    dto.antallVentende(), dto.antallOpprettet(), dto.antallAvsluttet(), dto.innslagType());
                entityManager.persist(stat);
            }
        }
    }

}
