package no.nav.foreldrepenger.los.migrering;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.los.migrering.dto.BehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;
import no.nav.foreldrepenger.los.migrering.dto.KøOppsettDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OrgDataDto;
import no.nav.foreldrepenger.los.migrering.dto.ReservasjonDataDto;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerNøkkel;
import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerNøkkel;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;

/**
 * Lagrer data fra FSS-instans i GCP-instans. Bevarer PK fra FSS der relevant for idempotent sync.
 */
@ApplicationScoped
@Transactional
public class GcpImportRepository {

    private static final Logger LOG = LoggerFactory.getLogger(GcpImportRepository.class);
    private static final int BATCH_SIZE = 200;

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

            var fssOppgaveIds = batch.stream()
                .map(OppgaveDataDto::id)
                .collect(Collectors.toSet());

            var gcpOppgaveIds = new HashSet<>(entityManager.createQuery("select o.id from Oppgave o where o.id in :ids", Long.class)
                .setParameter("ids", fssOppgaveIds)
                .getResultList());

            for (var dto : batch) {
                var behandling = entityManager.find(Behandling.class, dto.behandlingId().toUUID());
                var oppgave = GcpImportMapper.mapOppgave(dto, behandling);

                if (gcpOppgaveIds.contains(dto.id())) {
                    entityManager.merge(oppgave);
                } else {
                    entityManager.persist(oppgave);
                }

                oppgaveCount++;
            }

            var fssReservasjonIds = batch.stream()
                .map(OppgaveDataDto::reservasjonDataDto)
                .filter(Objects::nonNull)
                .map(ReservasjonDataDto::oppgaveId)
                .collect(Collectors.toSet());

            var gcpReservasjonerMap = entityManager.createQuery("from Reservasjon b where b.oppgave.id in :ids", Reservasjon.class)
                .setParameter("ids", fssReservasjonIds)
                .getResultStream()
                .collect(Collectors.toMap(r -> r.getOppgave().getId(), r -> r));

            for (var dto : batch) {
                var resDto = dto.reservasjonDataDto();
                if (resDto == null) {
                    continue;
                }

                var oppgave = entityManager.find(Oppgave.class, dto.id());
                var reservasjon = gcpReservasjonerMap.getOrDefault(resDto.oppgaveId(), new Reservasjon());
                GcpImportMapper.mapReservasjon(oppgave, resDto, reservasjon);

                if (gcpReservasjonerMap.containsKey(resDto.oppgaveId())) {
                    entityManager.merge(reservasjon);
                } else {
                    entityManager.persist(reservasjon);
                }

                reservasjonCount++;
            }

            entityManager.flush();
            entityManager.clear();
        }

        storedCounts.oppgaver(oppgaveCount).reservasjoner(reservasjonCount);
    }

    private int lagreOrganisasjonsData(List<OrgDataDto> orgDataList) {
        if (orgDataList.isEmpty()) return 0;

        var orgData = orgDataList.get(0);
        var count = 0;

        for (var dto : orgData.avdelinger()) {
            var existing = entityManager.find(Avdeling.class, dto.avdelingEnhet());
            if (existing == null) {
                var avdeling = GcpImportMapper.mapAvdeling(dto);
                entityManager.merge(avdeling);
                count++;
            }
        }

        for (var dto : orgData.saksbehandlere()) {
            var existing = entityManager.find(Saksbehandler.class, dto.saksbehandlerIdent());
            if (existing == null) {
                var saksbehandler = GcpImportMapper.mapSaksbehandler(dto);
                entityManager.merge(saksbehandler);
                count++;
            }
        }

        for (var dto : orgData.avdelingSaksbehandlere()) {
            var avdeling = entityManager.getReference(Avdeling.class, dto.avdelingId());
            var saksbehandler = entityManager.getReference(Saksbehandler.class, dto.saksbehandlerId());
            var key = new AvdelingSaksbehandlerNøkkel(saksbehandler, avdeling);

            var existing = entityManager.find(AvdelingSaksbehandlerRelasjon.class, key);
            if (existing == null) {
                var relationship = new AvdelingSaksbehandlerRelasjon(key);
                entityManager.persist(relationship);
                count++;
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
        var antallKøer = 0;

        for (var dto : køOppsettDto.oppgaveFiltrering()) {
            var existing = entityManager.find(OppgaveFiltrering.class, dto.id());
            if (existing == null) {
                var avdeling = dto.avdelingId() != null ? entityManager.getReference(Avdeling.class, dto.avdelingId()) : null;
                var filtrering = GcpImportMapper.mapOppgaveFiltrering(dto, avdeling);
                entityManager.merge(filtrering);
                antallKøer++;
            }
        }

        for (var sbDto : køOppsettDto.saksbehandlerKøer()) {
            var saksbehandler = entityManager.find(Saksbehandler.class, sbDto.saksbehandlerId());
            var filtrering = entityManager.find(OppgaveFiltrering.class, sbDto.oppgaveFiltreringId());
            if (saksbehandler != null && filtrering != null) {
                var nøkkel = new FiltreringSaksbehandlerNøkkel(saksbehandler, filtrering);
                var existing = entityManager.find(FiltreringSaksbehandlerRelasjon.class, nøkkel);
                if (existing == null) {
                    entityManager.persist(new FiltreringSaksbehandlerRelasjon(nøkkel));
                    antallKøer++;
                }
            }
        }

        return antallKøer;
    }

}
