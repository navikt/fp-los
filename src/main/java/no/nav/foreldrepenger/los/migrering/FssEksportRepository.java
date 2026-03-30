package no.nav.foreldrepenger.los.migrering;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import no.nav.foreldrepenger.los.migrering.dto.AndreKriterierDataDto;
import no.nav.foreldrepenger.los.migrering.dto.AvdelingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.AvdelingSaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;
import no.nav.foreldrepenger.los.migrering.dto.FiltreringSaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.KøOppsettDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveEgenskapDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveFiltreringDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OrgDataDto;
import no.nav.foreldrepenger.los.migrering.dto.ReservasjonDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerGruppeDataDto;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingEgenskap;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgave.OppgaveEgenskap;
import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.organisasjon.OrganisasjonRepository;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;
import no.nav.foreldrepenger.los.tjenester.saksbehandler.oppgave.dto.SaksnummerDto;

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
            .setFirstResult(startPosisjon)
            .setMaxResults(batchSize)
            .getResultStream()
            .map(this::mapToOppgaveDataDto)
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
                and coalesce(r.endretTidspunkt, r.opprettetTidspunkt) > :fra
                ORDER BY r.id ASC
            """, Oppgave.class)
            .setParameter("fra", LocalDate.now().minusDays(21).atStartOfDay())
            .setFirstResult(startPosisjon)
            .setMaxResults(batchSize)
            .getResultStream()
            .map(this::mapToOppgaveDataDto)
            .toList();
        var dto = BulkDataWrapper.inaktiveOppgaver(oppgaver);
        logg(dto);
        return dto;
    }

    BulkDataWrapper hentBehandlinger(int startPosisjon, int batchSize) {
        var behandlinger = entityManager.createQuery("""
                FROM Behandling
                ORDER BY id ASC
            """, Behandling.class)
            .setFirstResult(startPosisjon)
            .setMaxResults(batchSize)
            .getResultStream()
            .map(this::mapToBehandlingDataDto)
            .toList();
        var dto = BulkDataWrapper.behandlinger(behandlinger);
        logg(dto);
        return dto;
    }

    private void logg(BulkDataWrapper dto) {
        var antallReservasjoner = dto.aktiveOppgaver().stream().map(OppgaveDataDto::reservasjonDataDto).filter(Objects::nonNull).count();
        LOG.info("MIGRERING: Ekstrahert {} behandlinger, {} oppgaver, {} reservasjoner",
            dto.behandlinger().size(), dto.aktiveOppgaver().size(), antallReservasjoner);
    }

    private List<OrgDataDto> hentOrganisasjonData() {
        var avdelinger = organisasjonRepository.hentAktiveAvdelinger().stream().map(this::mapToAvdelingDataDto).toList();

        var saksbehandlere = entityManager.createQuery("FROM saksbehandler", Saksbehandler.class)
                .getResultList()
                .stream()
                .map(this::mapToSaksbehandlerDataDto)
                .toList();

        var avdelingSaksbehandlere = entityManager.createQuery("FROM AvdelingSaksbehandlerRelasjon", AvdelingSaksbehandlerRelasjon.class)
                .getResultList()
                .stream()
                .map(this::mapToAvdelingSaksbehandlerDataDto)
                .toList();

        var saksbehandlerGrupper = entityManager.createQuery("FROM saksbehandlerGruppe", SaksbehandlerGruppe.class)
                .getResultList()
                .stream()
                .map(this::mapToSaksbehandlerGruppeDataDto)
                .toList();

        return List.of(new OrgDataDto(avdelinger, saksbehandlere, avdelingSaksbehandlere, saksbehandlerGrupper));
    }

    private KøOppsettDto hentOppgaveKøData() {
        var oppgaveFiltrering = entityManager.createQuery("FROM OppgaveFiltrering", OppgaveFiltrering.class)
                .getResultList()
                .stream()
                .map(this::mapToOppgaveFiltreringDataDto)
                .toList();

        var saksbehandlerKøer = entityManager.createQuery("FROM FiltreringSaksbehandlerRelasjon", FiltreringSaksbehandlerRelasjon.class)
                .getResultList()
                .stream()
                .map(r -> new FiltreringSaksbehandlerDataDto(
                        r.getSaksbehandler().getSaksbehandlerIdent(),
                        r.getOppgaveFiltrering().getId()))
                .toList();

        return new KøOppsettDto(oppgaveFiltrering, saksbehandlerKøer);
    }

    // Mapping methods
    private BehandlingDataDto mapToBehandlingDataDto(Behandling behandling) {
        var egenskaper = entityManager.createQuery("""
                FROM BehandlingEgenskap be WHERE be.behandlingId = :behandlingId
                """, BehandlingEgenskap.class)
                .setParameter("behandlingId", behandling.getId())
                .getResultList()
                .stream()
                .map(BehandlingEgenskap::getAndreKriterierType)
                .toList();

        return new BehandlingDataDto(
                behandling.getId(),
                new SaksnummerDto(behandling.getSaksnummer().getVerdi()),
                behandling.getAktørId(),
                behandling.getKildeSystem(),
                behandling.getFagsakYtelseType(),
                behandling.getBehandlingType(),
                behandling.getBehandlingTilstand(),
                behandling.getAktiveAksjonspunkt(),
                behandling.getVentefrist(),
                behandling.getOpprettet(),
                behandling.getAvsluttet(),
                behandling.getBehandlingsfrist(),
                behandling.getFørsteStønadsdag(),
                behandling.getFeilutbetalingBelop(),
                behandling.getFeilutbetalingStart(),
                behandling.getBehandlendeEnhet(),
                behandling.getOpprettetAv(),
                behandling.getOpprettetTidspunkt(),
                behandling.getEndretAv(),
                behandling.getEndretTidspunkt(),
                egenskaper
        );
    }

    private OppgaveDataDto mapToOppgaveDataDto(Oppgave oppgave) {
        return new OppgaveDataDto(
                oppgave.getId(),
                new SaksnummerDto(oppgave.getSaksnummer().getVerdi()),
                oppgave.getAktørId(),
                oppgave.getBehandlingId(),
                oppgave.getBehandlingType(),
                oppgave.getFagsakYtelseType(),
                oppgave.getBehandlendeEnhet(),
                oppgave.getBehandlingsfrist(),
                oppgave.getBehandlingOpprettet(),
                oppgave.getFørsteStønadsdag(),
                oppgave.getAktiv(),
                oppgave.getSystem(),
                oppgave.getOppgaveAvsluttet(),
                oppgave.getFeilutbetalingBelop(),
                oppgave.getFeilutbetalingStart(),
                oppgave.getOpprettetAv(),
                oppgave.getOpprettetTidspunkt(),
                oppgave.getEndretAv(),
                oppgave.getEndretTidspunkt(),
                mapToReservasjonDataDto(oppgave.getReservasjon()),
                mapToOppgaveEgenskapDataDtoList(oppgave.getOppgaveEgenskaper())
        );
    }

    private List<OppgaveEgenskapDataDto> mapToOppgaveEgenskapDataDtoList(java.util.Set<OppgaveEgenskap> egenskaper) {
        if (egenskaper == null || egenskaper.isEmpty()) {
            return List.of();
        }
        return egenskaper.stream()
                .map(this::mapToOppgaveEgenskapDataDto)
                .toList();
    }

    private OppgaveEgenskapDataDto mapToOppgaveEgenskapDataDto(OppgaveEgenskap egenskap) {
        return new OppgaveEgenskapDataDto(
                egenskap.getAndreKriterierType(),
                egenskap.getSisteSaksbehandlerForTotrinn()
        );
    }

    private ReservasjonDataDto mapToReservasjonDataDto(Reservasjon reservasjon) {
        if (reservasjon == null) {
            return null;
        }
        return new ReservasjonDataDto(
                reservasjon.getId(),
                reservasjon.getOppgave().getId(),
                reservasjon.getReservertTil(),
                reservasjon.getReservertAv(),
                reservasjon.getFlyttetAv(),
                reservasjon.getFlyttetTidspunkt(),
                reservasjon.getBegrunnelse(),
                reservasjon.getOpprettetAv(),
                reservasjon.getOpprettetTidspunkt(),
                reservasjon.getEndretAv(),
                reservasjon.getEndretTidspunkt()
        );
    }

    // Add other mapping methods for organizational data and queue data...
    private AvdelingDataDto mapToAvdelingDataDto(Avdeling avdeling) {
        return new AvdelingDataDto(
                avdeling.getAvdelingEnhet(),
                avdeling.getNavn(),
                avdeling.getKreverKode6(),
                avdeling.getErAktiv(),
                avdeling.getOpprettetAv(),
                avdeling.getOpprettetTidspunkt(),
                avdeling.getEndretAv(),
                avdeling.getEndretTidspunkt()
        );
    }

    private SaksbehandlerDataDto mapToSaksbehandlerDataDto(Saksbehandler saksbehandler) {
        return new SaksbehandlerDataDto(
                saksbehandler.getSaksbehandlerIdent(),
                saksbehandler.getNavn(),
                saksbehandler.getAnsattVedEnhet(),
                saksbehandler.getOpprettetAv(),
                saksbehandler.getOpprettetTidspunkt(),
                saksbehandler.getEndretAv(),
                saksbehandler.getEndretTidspunkt()
        );
    }

    private AvdelingSaksbehandlerDataDto mapToAvdelingSaksbehandlerDataDto(AvdelingSaksbehandlerRelasjon as) {
        return new AvdelingSaksbehandlerDataDto(
                as.getAvdeling().getAvdelingEnhet(),
                as.getSaksbehandler().getSaksbehandlerIdent()
        );
    }

    private SaksbehandlerGruppeDataDto mapToSaksbehandlerGruppeDataDto(SaksbehandlerGruppe sg) {
        return new SaksbehandlerGruppeDataDto(
                sg.getId(),
                sg.getGruppeNavn(),
                sg.getAvdeling().getAvdelingEnhet(),
                sg.getOpprettetAv(),
                sg.getOpprettetTidspunkt(),
                sg.getEndretAv(),
                sg.getEndretTidspunkt()
        );
    }

    private OppgaveFiltreringDataDto mapToOppgaveFiltreringDataDto(OppgaveFiltrering of) {
        // Extract collections as simple lists/DTOs without preserving PKs
        var behandlingTyper = of.getBehandlingTyper();

        var fagsakYtelseTyper = of.getFagsakYtelseTyper();

        var andreKriterier = of.getFiltreringAndreKriterierTyper().stream()
                .map(ak -> new AndreKriterierDataDto(
                        ak.getAndreKriterierType(),
                        ak.isInkluder()
                ))
                .toList();

        return new OppgaveFiltreringDataDto(
                of.getId(),
                of.getNavn(),
                of.getBeskrivelse(),
                of.getSortering(),
                of.getAvdeling().getAvdelingEnhet(),
                of.getFomDato(),
                of.getTomDato(),
                of.getPeriodefilter(),
                of.getOpprettetAv(),
                of.getOpprettetTidspunkt(),
                of.getEndretAv(),
                of.getEndretTidspunkt(),
                behandlingTyper,
                fagsakYtelseTyper,
                andreKriterier
        );
    }

}

