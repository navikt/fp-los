package no.nav.foreldrepenger.los.migrering.fss;

import java.time.LocalDate;
import java.util.Objects;

import org.hibernate.jpa.HibernateHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;
import no.nav.foreldrepenger.los.migrering.dto.KøOppsettDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OrgDataDto;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingTilstand;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.organisasjon.GruppeTilknytningRelasjon;
import no.nav.foreldrepenger.los.organisasjon.OrganisasjonRepository;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;
import no.nav.foreldrepenger.los.statistikk.StatistikkEnhetYtelseBehandling;
import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilter;

/**
 * Eksport fra FSS-los. Beholder PK.
 */
@ApplicationScoped
@Transactional
public class FssEksportRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FssEksportRepository.class);

    private EntityManager entityManager;
    private OrganisasjonRepository organisasjonRepository;

    @Inject
    public FssEksportRepository(EntityManager entityManager, OrganisasjonRepository organisasjonRepository) {
        this.entityManager = entityManager;
        this.organisasjonRepository = organisasjonRepository;
    }

    FssEksportRepository() {
        // For CDI
    }

    public BulkDataWrapper hentOrganisasjonOgKøer() {
        var orgData = hentOrganisasjonData();
        var oppgaveKøer = hentOppgaveKøData();
        var dto = BulkDataWrapper.organisasjonOgKøOppset(orgData, oppgaveKøer);
        logg(dto);
        return dto;
    }

    public BulkDataWrapper hentAktiveOppgaverOgReservasjoner(int startPosisjon, int batchSize) {
        var oppgaver = entityManager.createQuery("""
                FROM Oppgave
                WHERE aktiv = true
                ORDER BY id ASC
            """, Oppgave.class)
            .setHint(HibernateHints.HINT_READ_ONLY, true)
            .setFirstResult(startPosisjon)
            .setMaxResults(batchSize)
            .getResultStream()
            .map(FssExportMapper::mapToOppgaveDataDto)
            .toList();

        var dto = BulkDataWrapper.aktiveOppgaver(oppgaver);
        logg(dto);
        return dto;
    }

    public BulkDataWrapper hentInaktiveOppgaverOgReservasjoner(int startPosisjon, int batchSize) {
        var oppgaver = entityManager.createQuery("""
                FROM Oppgave o
                JOIN o.reservasjon r
                WHERE o.aktiv = false
                AND coalesce(r.endretTidspunkt, r.opprettetTidspunkt) > :fra
                AND o.behandling.behandlingTilstand != :avsluttet
                ORDER BY r.id ASC
            """, Oppgave.class)
            .setHint(HibernateHints.HINT_READ_ONLY, true)
            .setParameter("fra", LocalDate.now().minusDays(21).atStartOfDay())
            .setParameter("avsluttet", BehandlingTilstand.AVSLUTTET)
            .setFirstResult(startPosisjon)
            .setMaxResults(batchSize)
            .getResultStream()
            .map(FssExportMapper::mapToOppgaveDataDto)
            .toList();
        var dto = BulkDataWrapper.inaktiveOppgaver(oppgaver);
        logg(dto);
        return dto;
    }

    BulkDataWrapper hentBehandlinger(int startPosisjon, int batchSize) {
        var behandlinger = entityManager.createQuery("""
                FROM Behandling
                WHERE behandlingTilstand != :avsluttet
                ORDER BY id ASC
            """, Behandling.class)
            .setHint(HibernateHints.HINT_READ_ONLY, true)
            .setParameter("avsluttet", BehandlingTilstand.AVSLUTTET)
            .setFirstResult(startPosisjon)
            .setMaxResults(batchSize)
            .getResultList();

        var dtos = behandlinger.stream()
            .map(FssExportMapper::mapToBehandlingDataDto)
            .toList();

        var dto = BulkDataWrapper.behandlinger(dtos);
        logg(dto);
        return dto;
    }

    private void logg(BulkDataWrapper dto) {
        var antallReservasjoner = dto.aktiveOppgaver().stream().map(OppgaveDataDto::reservasjonDataDto).filter(Objects::nonNull).count();
        LOG.info("MIGRERING: Ekstrahert {} behandlinger, {} oppgaver, {} reservasjoner",
            dto.behandlinger().size(), dto.aktiveOppgaver().size(), antallReservasjoner);
    }

    private OrgDataDto hentOrganisasjonData() {
        var avdelinger = organisasjonRepository.hentAktiveAvdelinger().stream().map(FssExportMapper::mapToAvdelingDataDto).toList();

        var saksbehandlere = entityManager.createQuery("FROM saksbehandler", Saksbehandler.class)
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .getResultList()
                .stream()
                .map(FssExportMapper::mapToSaksbehandlerDataDto)
                .toList();

        var avdelingSaksbehandlere = entityManager.createQuery("FROM AvdelingSaksbehandlerRelasjon", AvdelingSaksbehandlerRelasjon.class)
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .getResultList()
                .stream()
                .map(FssExportMapper::mapToAvdelingSaksbehandlerDataDto)
                .toList();

        var saksbehandlerGrupper = entityManager.createQuery("FROM saksbehandlerGruppe", SaksbehandlerGruppe.class)
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .getResultList()
                .stream()
                .map(FssExportMapper::mapToSaksbehandlerGruppeDataDto)
                .toList();

        var gruppeTilknytninger = entityManager.createQuery("FROM GruppeTilknytningRelasjon", GruppeTilknytningRelasjon.class)
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .getResultList()
                .stream()
                .map(FssExportMapper::mapToGruppeTilknytningDataDto)
                .toList();

        return new OrgDataDto(avdelinger, saksbehandlere, avdelingSaksbehandlere, saksbehandlerGrupper, gruppeTilknytninger);
    }

    public BulkDataWrapper hentStatistikkEnhetYtelseBehandling(int startPosisjon, int batchSize) {
        var enhetYtelseBehandling = entityManager.createQuery("FROM StatistikkEnhetYtelseBehandling ORDER BY tidsstempel ASC", StatistikkEnhetYtelseBehandling.class)
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .setFirstResult(startPosisjon)
                .setMaxResults(batchSize)
                .getResultStream()
                .map(FssExportMapper::mapToStatEnhetYtelseBehandlingDataDto)
                .toList();

        return BulkDataWrapper.statistikkEnhetYtelseBehandling(enhetYtelseBehandling);
    }

    public BulkDataWrapper hentStatistikkOppgaveFilter(int startPosisjon, int batchSize) {
        var oppgaveFilter = entityManager.createQuery("FROM StatistikkOppgaveFilter WHERE statistikkDato >= :fra ORDER BY tidsstempel ASC", StatistikkOppgaveFilter.class)
            .setHint(HibernateHints.HINT_READ_ONLY, true)
            .setParameter("fra", LocalDate.now().minusWeeks(4))
            .setFirstResult(startPosisjon)
            .setMaxResults(batchSize)
            .getResultStream()
            .map(FssExportMapper::mapToStatOppgaveFilterDataDto)
            .toList();
        return BulkDataWrapper.statistikkOppgaveFilter(oppgaveFilter);
    }

    private KøOppsettDto hentOppgaveKøData() {
        var oppgaveFiltrering = entityManager.createQuery("FROM OppgaveFiltrering", OppgaveFiltrering.class)
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .getResultList()
                .stream()
                .map(FssExportMapper::mapToOppgaveFiltreringDataDto)
                .toList();

        var saksbehandlerKøer = entityManager.createQuery("FROM FiltreringSaksbehandlerRelasjon", FiltreringSaksbehandlerRelasjon.class)
                .setHint(HibernateHints.HINT_READ_ONLY, true)
                .getResultList()
                .stream()
                .map(FssExportMapper::mapToFiltreringSaksbehandlerDataDto)
                .toList();

        return new KøOppsettDto(oppgaveFiltrering, saksbehandlerKøer);
    }

}
