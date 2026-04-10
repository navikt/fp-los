package no.nav.foreldrepenger.los.oppgave;

import static no.nav.foreldrepenger.los.DBTestUtil.avdelingDrammen;
import static no.nav.foreldrepenger.los.oppgavekø.KøSortering.BEHANDLINGSFRIST;
import static no.nav.foreldrepenger.los.oppgavekø.KøSortering.FEILUTBETALINGSTART;
import static no.nav.foreldrepenger.los.organisasjon.Avdeling.AVDELING_DRAMMEN_ENHET;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.DBTestUtil;
import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.foreldrepenger.los.domene.typer.BehandlingId;
import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.domene.typer.aktør.AktørId;
import no.nav.foreldrepenger.los.felles.util.BrukerIdent;
import no.nav.foreldrepenger.los.oppgavekø.KøSortering;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;

@ExtendWith(JpaExtension.class)
class OppgaveRepositoryTest {

    private static final BehandlingId behandlingId1 = new BehandlingId(UUID.nameUUIDFromBytes("uuid_1".getBytes()));
    private static final BehandlingId behandlingId2 = new BehandlingId(UUID.nameUUIDFromBytes("uuid_2".getBytes()));
    private static final BehandlingId behandlingId3 = new BehandlingId(UUID.nameUUIDFromBytes("uuid_3".getBytes()));
    private static final BehandlingId behandlingId4 = new BehandlingId(UUID.nameUUIDFromBytes("uuid_4".getBytes()));
    private static final BehandlingId behandlingId5 = new BehandlingId(UUID.nameUUIDFromBytes("uuid_5".getBytes()));

    private EntityManager entityManager;
    private OppgaveRepository oppgaveRepository;
    private OppgaveKøRepository oppgaveKøRepository;


    @BeforeEach
    void setup(EntityManager entityManager) {
        this.entityManager = entityManager;
        oppgaveRepository = new OppgaveRepository(entityManager);
        oppgaveKøRepository = new OppgaveKøRepository(entityManager);
    }

    @Test
    void testHentingAvOppgaver() {
        lagStandardSettMedOppgaver();
        var alleOppgaverSpørring = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(),
            new ArrayList<>(), new ArrayList<>(), Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.ALLE, null, null);

        var oppgaves = oppgaveKøRepository.hentOppgaver(alleOppgaverSpørring);
        assertThat(oppgaves).hasSize(5);
        assertThat(oppgaveKøRepository.hentAntallOppgaver(alleOppgaverSpørring)).isEqualTo(5);
        assertThat(oppgaves).first().hasFieldOrPropertyWithValue("behandlendeEnhet", AVDELING_DRAMMEN_ENHET);
    }

    @Test
    void testOppgaveSpørringMedEgenskaperfiltrering() {
        var kriterier = Set.of(AndreKriterierType.UTLANDSSAK, AndreKriterierType.UTBETALING_TIL_BRUKER);
        var saksnummer = new Saksnummer(String.valueOf(Math.abs(new Random().nextLong() % 999999999)));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder().medSaksnummer(saksnummer).medKriterier(kriterier));
        var oppgave = Oppgave.builder()
            .dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(behandlingId1.toUUID()))
            .medKriterier(kriterier, "z999999")
            .build();
        entityManager.persist(oppgave);
        entityManager.flush();
        var oppgaveQuery = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, BEHANDLINGSFRIST, List.of(), List.of(), List.of(AndreKriterierType.UTLANDSSAK),
            // inkluderes
            List.of(AndreKriterierType.VURDER_SYKDOM), // ekskluderes
            Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.ALLE, null, null);
        var oppgaver = oppgaveKøRepository.hentOppgaver(oppgaveQuery);
        assertThat(oppgaver).hasSize(1);
        assertThat(oppgaver.getFirst().getSaksnummer()).isEqualTo(saksnummer);
    }


    @Test
    void testEkskluderingOgInkluderingAvOppgaver() {
        lagStandardSettMedOppgaver();
        var oppgaver = oppgaveKøRepository.hentOppgaver(
            new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(),
                List.of(AndreKriterierType.TIL_BESLUTTER, AndreKriterierType.PAPIRSØKNAD), new ArrayList<>(), Periodefilter.FAST_PERIODE, null, null,
                null, null, Filtreringstype.ALLE, null, null));
        assertThat(oppgaver).hasSize(1);

        oppgaver = oppgaveKøRepository.hentOppgaver(
            new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(),
                List.of(AndreKriterierType.TIL_BESLUTTER), new ArrayList<>(), Periodefilter.FAST_PERIODE, null, null, null, null,
                Filtreringstype.ALLE, null, null));
        assertThat(oppgaver).hasSize(2);

        oppgaver = oppgaveKøRepository.hentOppgaver(
            new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                List.of(AndreKriterierType.TIL_BESLUTTER, AndreKriterierType.PAPIRSØKNAD), // ekskluder andreKriterierType
                Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.ALLE, null, null));
        assertThat(oppgaver).hasSize(2);

        oppgaver = oppgaveKøRepository.hentOppgaver(
            new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                List.of(AndreKriterierType.TIL_BESLUTTER),  // ekskluderAndreKriterierType
                Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.ALLE, null, null));
        assertThat(oppgaver).hasSize(3);

        oppgaver = oppgaveKøRepository.hentOppgaver(
            new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(),
                List.of(AndreKriterierType.PAPIRSØKNAD), List.of(AndreKriterierType.TIL_BESLUTTER), Periodefilter.FAST_PERIODE, null, null, null,
                null, Filtreringstype.ALLE, null, null));
        assertThat(oppgaver).hasSize(1);
        var antallOppgaver = oppgaveKøRepository.hentAntallOppgaver(
            new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(),
                List.of(AndreKriterierType.PAPIRSØKNAD), List.of(AndreKriterierType.TIL_BESLUTTER), Periodefilter.FAST_PERIODE, null, null, null,
                null, Filtreringstype.LEDIGE, null, null));
        assertThat(antallOppgaver).isEqualTo(1);

        var antallOppgaverForAvdeling = oppgaveKøRepository.hentAntallOppgaverForAvdeling(AVDELING_DRAMMEN_ENHET);
        assertThat(antallOppgaverForAvdeling).isEqualTo(5);

    }

    @Test
    void testAntallOppgaverForAvdeling() {
        var antallOppgaverForAvdeling = oppgaveKøRepository.hentAntallOppgaverForAvdeling(AVDELING_DRAMMEN_ENHET);
        assertThat(antallOppgaverForAvdeling).isZero();
        lagStandardSettMedOppgaver();
        antallOppgaverForAvdeling = oppgaveKøRepository.hentAntallOppgaverForAvdeling(AVDELING_DRAMMEN_ENHET);
        assertThat(antallOppgaverForAvdeling).isEqualTo(5);
    }

    @Test
    void testFiltreringRelativAvOppgaverIntervall() {
        lagStandardSettMedOppgaver();
        var oppgaves = oppgaveKøRepository.hentOppgaver(
            new Oppgavespørring(AVDELING_DRAMMEN_ENHET, BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                Periodefilter.RELATIV_PERIODE_DAGER, null, null, 1L, 10L, Filtreringstype.ALLE, null, null));
        assertThat(oppgaves).hasSize(2);
    }

    @Test
    void testFiltreringRelativAvOppgaverBareFomDato() {
        lagStandardSettMedOppgaver();
        var oppgaves = oppgaveKøRepository.hentOppgaver(
            new Oppgavespørring(AVDELING_DRAMMEN_ENHET, BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                Periodefilter.RELATIV_PERIODE_DAGER, null, null, 15L, null, Filtreringstype.ALLE, null, null));
        assertThat(oppgaves).hasSize(2);
    }

    @Test
    void testFiltreringRelativAvOppgaverBareTomDato() {
        lagStandardSettMedOppgaver();
        var oppgaves = oppgaveKøRepository.hentOppgaver(
            new Oppgavespørring(AVDELING_DRAMMEN_ENHET, BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                Periodefilter.RELATIV_PERIODE_DAGER, null, null, null, 15L, Filtreringstype.ALLE, null, null));
        assertThat(oppgaves).hasSize(4);
    }

    @Test
    void testFiltreringRelativAvOppgaverIntervallMåneder() {
        lagStandardSettMedOppgaver();
        var oppgaves = oppgaveKøRepository.hentOppgaver(
            new Oppgavespørring(AVDELING_DRAMMEN_ENHET, BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                Periodefilter.RELATIV_PERIODE_MÅNEDER, null, null, -3L, 5L, Filtreringstype.ALLE, null, null));
        assertThat(oppgaves).hasSize(4);
    }

    private void lagStandardSettMedOppgaver() {
        lagStandardSettMedBehandlinger();
        var førsteOppgave = Oppgave.builder()
            .dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(behandlingId1.toUUID()))
            .medKriterier(Set.of(AndreKriterierType.PAPIRSØKNAD, AndreKriterierType.TIL_BESLUTTER), "z999999")
            .build();
        var andreOppgave = Oppgave.builder()
            .dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(behandlingId2.toUUID()))
            .medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER), "z999999")
            .build();
        var tredjeOppgave = Oppgave.builder()
            .dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(behandlingId3.toUUID()))
            .medKriterier(Set.of(AndreKriterierType.PAPIRSØKNAD), null)
            .build();
        var fjerdeOppgave = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(behandlingId4.toUUID())).build();

        var femteOppgave = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(behandlingId5.toUUID())).build();

        entityManager.persist(førsteOppgave);
        entityManager.persist(andreOppgave);
        entityManager.persist(tredjeOppgave);
        entityManager.persist(fjerdeOppgave);
        entityManager.persist(femteOppgave);

        entityManager.flush();
    }

    private void lagStandardSettMedBehandlinger() {
        var førsteBehandling = Behandling.builder(Optional.empty())
            .dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.BESLUTTER)
            .medId(behandlingId1.toUUID())
            .medSaksnummer(new Saksnummer("111"))
            .medOpprettet(LocalDateTime.now().minusDays(10))
            .medBehandlingsfrist(LocalDate.now().plusDays(10))
            .medKriterier(Set.of(AndreKriterierType.PAPIRSØKNAD, AndreKriterierType.TIL_BESLUTTER))
            .build();
        var andreBehandling = Behandling.builder(Optional.empty())
            .dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.BESLUTTER)
            .medId(behandlingId2.toUUID())
            .medSaksnummer(new Saksnummer("222"))
            .medOpprettet(LocalDateTime.now().minusDays(9))
            .medBehandlingsfrist(LocalDate.now().plusDays(5))
            .medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER))
            .build();
        var tredjeBehandling = Behandling.builder(Optional.empty())
            .dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.PAPIRSØKNAD)
            .medId(behandlingId3.toUUID())
            .medSaksnummer(new Saksnummer("333"))
            .medOpprettet(LocalDateTime.now().minusDays(8))
            .medBehandlingsfrist(LocalDate.now().plusDays(15))
            .medKriterier(Set.of(AndreKriterierType.PAPIRSØKNAD))
            .build();
        var fjerdeBehandling = Behandling.builder(Optional.empty())
            .dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT)
            .medId(behandlingId4.toUUID())
            .medSaksnummer(new Saksnummer("444"))
            .medOpprettet(LocalDateTime.now())
            .medBehandlingsfrist(LocalDate.now())
            .build();

        var femteBehandling = Behandling.builder(Optional.empty())
            .dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT)
            .medId(behandlingId5.toUUID())
            .medSaksnummer(new Saksnummer("555"))
            .medOpprettet(LocalDateTime.now())
            .medBehandlingsfrist(LocalDate.now().plusYears(1))
            .build();

        entityManager.persist(førsteBehandling);
        entityManager.persist(andreBehandling);
        entityManager.persist(tredjeBehandling);
        entityManager.persist(fjerdeBehandling);
        entityManager.persist(femteBehandling);

        entityManager.flush();
    }

    @Test
    void testReservering() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder().medOpprettet(LocalDate.now().minusDays(10).atStartOfDay()));
        var oppgave = basicOppgaveBuilder().build();
        entityManager.persist(oppgave);
        entityManager.flush();
        var reservertOppgave = oppgaveRepository.hentReservasjon(oppgave.getId());
        assertThat(reservertOppgave).isNotNull();
    }

    @Test
    void hentAlleLister() {
        var avdeling = avdelingDrammen(entityManager);
        var førsteOppgaveFiltrering = new OppgaveFiltrering("OPPRETTET", KøSortering.OPPRETT_BEHANDLING, avdeling);
        var andreOppgaveFiltrering = new OppgaveFiltrering("BEHANDLINGSFRIST", BEHANDLINGSFRIST, avdeling);

        entityManager.persist(førsteOppgaveFiltrering);
        entityManager.persist(andreOppgaveFiltrering);
        entityManager.flush();

        var lister = oppgaveRepository.hentAlleOppgaveFilterSettTilknyttetEnhet(Avdeling.AVDELING_DRAMMEN_ENHET);

        assertThat(lister).extracting(OppgaveFiltrering::getNavn).contains("OPPRETTET", "BEHANDLINGSFRIST");
        assertThat(lister).extracting(OppgaveFiltrering::getAvdeling).contains(avdeling);
        assertThat(lister).extracting(OppgaveFiltrering::getSortering).contains(BEHANDLINGSFRIST, KøSortering.OPPRETT_BEHANDLING);
    }

    @Test
    void lagreOppgaveHvisForskjelligEnhet() {
        lagBehandling();
        var oppgave = lagOppgave(AVDELING_DRAMMEN_ENHET);
        var avdelingAnnetEnhet = "4000";
        var oppgaveKommerPåNytt = lagOppgave(avdelingAnnetEnhet);
        persistFlush(oppgave);
        assertThat(DBTestUtil.hentAlle(entityManager, Oppgave.class)).hasSize(1);
        persistFlush(oppgaveKommerPåNytt);
        assertThat(DBTestUtil.hentAlle(entityManager, Oppgave.class)).hasSize(2);
    }

    @Test
    void lagreOppgaveHvisAvsluttetFraFør() {
        lagBehandling();
        var oppgave = lagOppgave(AVDELING_DRAMMEN_ENHET);
        persistFlush(oppgave);
        var oppgaveKommerPåNytt = lagOppgave(AVDELING_DRAMMEN_ENHET);
        persistFlush(oppgaveKommerPåNytt);
        assertThat(DBTestUtil.hentAlle(entityManager, Oppgave.class)).hasSize(2);
    }

    @Test
    void filtrerPåOpprettetDatoTomDato() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder().medOpprettet(LocalDateTime.now().minusDays(2)));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId2.toUUID()).medOpprettet(LocalDateTime.now()));
        var aktuellOppgave = basicOppgaveBuilder().build();
        var uaktuellOppgave = basicOppgaveBuilder(behandlingId2.toUUID()).build();
        oppgaveRepository.lagre(uaktuellOppgave);
        oppgaveRepository.lagre(aktuellOppgave);
        var filtrerTomDato = LocalDate.now().minusDays(1);
        var query = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.OPPRETT_BEHANDLING, List.of(BehandlingType.FØRSTEGANGSSØKNAD),
            List.of(FagsakYtelseType.FORELDREPENGER), List.of(), List.of(), Periodefilter.FAST_PERIODE, null, filtrerTomDato, null, null,
            Filtreringstype.ALLE, null, null);
        var oppgaveResultat = oppgaveKøRepository.hentOppgaver(query);
        assertThat(oppgaveResultat).containsExactly(aktuellOppgave);
    }

    @Test
    void filtrerPåFørsteStønadsdag() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId1.toUUID()).medFørsteStønadsdag(LocalDate.now().minusDays(1)));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId2.toUUID()).medFørsteStønadsdag(LocalDate.now()));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId3.toUUID()).medFørsteStønadsdag(LocalDate.now().plusDays(5)));
        var oppgave1 = basicOppgaveBuilder(behandlingId1.toUUID()).build();
        var oppgave2 = basicOppgaveBuilder(behandlingId2.toUUID()).build();
        var oppgave3 = basicOppgaveBuilder(behandlingId3.toUUID()).build();
        oppgaveRepository.lagre(oppgave1);
        oppgaveRepository.lagre(oppgave2);
        oppgaveRepository.lagre(oppgave3);
        Assertions.assertThat(filterOppgaver(oppgave1.getFørsteStønadsdag(), oppgave3.getFørsteStønadsdag()))
            .containsExactlyInAnyOrder(oppgave2, oppgave1, oppgave3);
        Assertions.assertThat(filterOppgaver(oppgave1.getFørsteStønadsdag(), oppgave1.getFørsteStønadsdag())).containsExactly(oppgave1);
        Assertions.assertThat(filterOppgaver(oppgave1.getFørsteStønadsdag().minusDays(10), oppgave1.getFørsteStønadsdag().minusDays(1))).isEmpty();
    }

    @Test
    void filtrerSorterFeilutbetaltBeløp() {
        oppgaveRepository.lagreBehandling(tilbakekrevingBehandlingBuilder(behandlingId1.toUUID()).medOpprettet(LocalDateTime.now().minusDays(2L))
            .medFeilutbetalingBelop(BigDecimal.valueOf(100L)));
        var oppgave1 = tilbakekrevingOppgaveBuilder(behandlingId1.toUUID()).medBehandling(oppgaveRepository.hentBehandling(behandlingId1.toUUID()))
            .build();
        oppgaveRepository.lagreBehandling(tilbakekrevingBehandlingBuilder(behandlingId2.toUUID()).medOpprettet(LocalDateTime.now().minusDays(2L))
            .medFeilutbetalingBelop(BigDecimal.valueOf(200L)));
        var oppgave2 = tilbakekrevingOppgaveBuilder(behandlingId2.toUUID()).medBehandling(oppgaveRepository.hentBehandling(behandlingId2.toUUID()))
            .build();
        oppgaveRepository.lagre(oppgave1);
        oppgaveRepository.lagre(oppgave2);

        var queryFiltrertPåBeløpsstørrelse = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BELØP, List.of(), List.of(), List.of(),
            List.of(), Periodefilter.FAST_PERIODE, null, null, 50L, 150L, Filtreringstype.ALLE, null, null);
        var oppgaver = oppgaveKøRepository.hentOppgaver(queryFiltrertPåBeløpsstørrelse);
        assertThat(oppgaver).containsExactly(oppgave1);

        var querySortertPåBeløpsstørrelseDesc = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BELØP, List.of(), List.of(), List.of(),
            List.of(), Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.ALLE, null, null);
        var oppgaverSortert = oppgaveKøRepository.hentOppgaver(querySortertPåBeløpsstørrelseDesc);
        assertThat(oppgaverSortert).containsExactly(oppgave2, oppgave1);
    }

    @Test
    void nullSistVedFeilutbetalingStartSomSorteringsKriterium() {
        // dersom oppdrag ikke leverer grunnlag innen frist, gir fptilbake opp og
        // lager hendelse som fører til oppgave. Formålet er at saksbehandler skal avklare
        // status på grunnlaget. Funksjonelt kan det variere om filtrene som brukes i enhetene
        // fanger opp disse (fom/tom på feltet vil ekskludere bla).
        oppgaveRepository.lagreBehandling(tilbakekrevingBehandlingBuilder(behandlingId1.toUUID()).medOpprettet(LocalDateTime.now().minusDays(2L))
            .medFeilutbetalingBelop(BigDecimal.valueOf(0L))
            .medFeilutbetalingStart(null));
        var oppgaveUtenStartDato = tilbakekrevingOppgaveBuilder(behandlingId1.toUUID()).medBehandling(
            oppgaveRepository.hentBehandling(behandlingId1.toUUID())).build();
        oppgaveRepository.lagreBehandling(tilbakekrevingBehandlingBuilder(behandlingId2.toUUID()).medOpprettet(LocalDateTime.now().minusDays(1L))
            .medFeilutbetalingBelop(BigDecimal.valueOf(10L))
            .medFeilutbetalingStart(LocalDate.now()));
        var oppgaveMedStartDato = tilbakekrevingOppgaveBuilder(behandlingId2.toUUID()).medBehandling(
            oppgaveRepository.hentBehandling(behandlingId2.toUUID())).build();
        oppgaveRepository.lagre(oppgaveUtenStartDato);
        oppgaveRepository.lagre(oppgaveMedStartDato);

        var query = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, FEILUTBETALINGSTART, List.of(), List.of(), List.of(), List.of(),
            Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.ALLE, null, null);
        var oppgaver = oppgaveKøRepository.hentOppgaver(query);
        assertThat(oppgaver).containsExactly(oppgaveMedStartDato, oppgaveUtenStartDato);
    }

    @Test
    void skalKunneSorterePåFørsteStønadsdagSynkende() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId1.toUUID()).medFørsteStønadsdag(LocalDate.now().minusDays(1)));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId2.toUUID()).medFørsteStønadsdag(LocalDate.now()));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId3.toUUID()).medFørsteStønadsdag(LocalDate.now().plusDays(5)));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId4.toUUID()).medFørsteStønadsdag(null));
        var oppgave1 = basicOppgaveBuilder(behandlingId1.toUUID()).build();
        var oppgave2 = basicOppgaveBuilder(behandlingId2.toUUID()).build();
        var oppgave3 = basicOppgaveBuilder(behandlingId3.toUUID()).build();
        var oppgave4 = basicOppgaveBuilder(behandlingId4.toUUID()).build(); // verifiserer nulls last
        oppgaveRepository.lagre(oppgave1);
        oppgaveRepository.lagre(oppgave2);
        oppgaveRepository.lagre(oppgave3);
        oppgaveRepository.lagre(oppgave4);

        var query = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.FØRSTE_STØNADSDAG_SYNKENDE, List.of(), List.of(), List.of(), List.of(),
            Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.ALLE, null, null);
        var oppgaver = oppgaveKøRepository.hentOppgaver(query);
        Assertions.assertThat(oppgaver).containsExactly(oppgave3, oppgave2, oppgave1, oppgave4);

        var queryAvgrenset = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.FØRSTE_STØNADSDAG_SYNKENDE, List.of(), List.of(), List.of(),
            List.of(), Periodefilter.FAST_PERIODE, oppgave2.getFørsteStønadsdag(), oppgave3.getFørsteStønadsdag(), null, null, Filtreringstype.ALLE,
            null, null);
        var oppgaverAvgrenset = oppgaveKøRepository.hentOppgaver(queryAvgrenset);
        Assertions.assertThat(oppgaverAvgrenset).containsExactly(oppgave3, oppgave2);
    }

    @Test
    void fårTomtSvarFraOppgaveFiltrering() {
        var filtrering = oppgaveRepository.hentOppgaveFilterSett(0L);
        assertThat(filtrering).isEmpty();
    }

    @Test
    void avdelingslederTellerMedEgneReservasjoner() {
        var saksnummer = new Saksnummer(String.valueOf(Math.abs(new Random().nextLong() % 999999999)));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder().medSaksnummer(saksnummer).medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER)));
        var oppgave = Oppgave.builder()
            .dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(behandlingId1.toUUID()))
            .medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER), BrukerIdent.brukerIdent())
            .build();
        entityManager.persist(oppgave);
        entityManager.flush();

        // saksbehandlere bør ikke få opp et antall som ikke stemmer med det de ser i køen (egne vedtak til beslutter filtreres bort fra beslutterkø)
        var beslutterKøIkkeAvdelingsleder = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BEHANDLINGSFRIST, List.of(), List.of(),
            List.of(AndreKriterierType.TIL_BESLUTTER), List.of(), Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.LEDIGE, null,
            null);
        var oppgaver = oppgaveKøRepository.hentAntallOppgaver(beslutterKøIkkeAvdelingsleder);
        assertThat(oppgaver).isZero();

        // avdelingsleder skal se antallet i avdelingslederkontekst, også eventuelle egne foreslåtte vedtak der avdelingsleder også er saksbehandler
        var beslutterKøAvdelingsleder = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BEHANDLINGSFRIST, List.of(), List.of(),
            List.of(AndreKriterierType.TIL_BESLUTTER), List.of(), Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.ALLE, null,
            null);
        var oppgaveAntallAdelingsleder = oppgaveKøRepository.hentAntallOppgaver(beslutterKøAvdelingsleder);
        assertThat(oppgaveAntallAdelingsleder).isEqualTo(1);
    }

    @Test
    void lagre_behandling_finn_den() {
        var behandling = Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT).build();
        oppgaveRepository.lagre(behandling);
        var hentet = oppgaveRepository.finnBehandling(behandling.getId());
        assertThat(hentet).isPresent();
        assertThat(hentet.get().getId()).isEqualTo(behandling.getId());
        assertThat(hentet.get().getBehandlingTilstand()).isEqualTo(BehandlingTilstand.AKSJONSPUNKT);
        var oppdatert = Behandling.builder(hentet).medBehandlingTilstand(BehandlingTilstand.VENT_MANUELL).build();
        oppgaveRepository.lagre(oppdatert);
        hentet = oppgaveRepository.finnBehandling(behandling.getId());
        assertThat(hentet).isPresent();
        assertThat(hentet.get().getBehandlingTilstand()).isEqualTo(BehandlingTilstand.VENT_MANUELL);
    }

    @Test
    void skalKunneSorterePåOppgaveOpprettetTidStigende() {

        var now = LocalDateTime.now();
        var t1 = now.minusHours(3);
        var t2 = now.minusHours(2);
        var t3 = now.minusHours(1);

        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId1.toUUID()).medBehandlingsfrist(t1.toLocalDate())
            .medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER)));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId2.toUUID()).medBehandlingsfrist(t2.toLocalDate())
            .medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER)));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId3.toUUID()).medBehandlingsfrist(t3.toLocalDate())
            .medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER)));


        // setter omvendt tid på behandlingsfrist for å sørge for motsatt sortering med default sortering
        var beslutterOppgaveEldste = basicOppgaveBuilder(behandlingId3.toUUID()).medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER), "z999999")
            .build();
        var beslutterOppgaveMellomste = basicOppgaveBuilder(behandlingId2.toUUID()).medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER), "z999999")
            .build();
        var beslutterOppgaveNyeste = basicOppgaveBuilder(behandlingId1.toUUID()).medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER), "z999999")
            .build();

        entityManager.persist(beslutterOppgaveEldste);
        entityManager.persist(beslutterOppgaveMellomste);
        entityManager.persist(beslutterOppgaveNyeste);
        entityManager.flush();

        // for enkel, pålitelig test må vi patche oppgave.opprettet_tid via nativequery
        setOpprettetTid(beslutterOppgaveEldste, t1);
        setOpprettetTid(beslutterOppgaveMellomste, t2);
        setOpprettetTid(beslutterOppgaveNyeste, t3);
        entityManager.flush();

        var oppgaveOpprettetSorteringQuery = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.OPPGAVE_OPPRETTET, List.of(), List.of(),
            List.of(AndreKriterierType.TIL_BESLUTTER), List.of(), Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.ALLE, null,
            null);
        var oppgaver = oppgaveKøRepository.hentOppgaver(oppgaveOpprettetSorteringQuery);
        assertThat(oppgaver).hasSize(3);
        assertThat(oppgaver).extracting(Oppgave::getId)
            .containsExactly(beslutterOppgaveEldste.getId(), beslutterOppgaveMellomste.getId(), beslutterOppgaveNyeste.getId());

        // sjekker at default sortering gir motsatt rekkefølge
        var defaultSorteringQuery = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.BEHANDLINGSFRIST, new ArrayList<>(), new ArrayList<>(),
            new ArrayList<>(), new ArrayList<>(), Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.ALLE, null, null);
        var defaultSorteringOppgaver = oppgaveKøRepository.hentOppgaver(defaultSorteringQuery);
        assertThat(defaultSorteringOppgaver).hasSize(3);
        assertThat(defaultSorteringOppgaver).extracting(Oppgave::getId)
            .containsExactlyInAnyOrder(beslutterOppgaveNyeste.getId(), beslutterOppgaveMellomste.getId(), beslutterOppgaveEldste.getId());
    }

    private void setOpprettetTid(Oppgave oppgave, LocalDateTime ts) {
        entityManager.createNativeQuery("update OPPGAVE set OPPRETTET_TID = :ts where id = :id")
            .setParameter("ts", ts)
            .setParameter("id", oppgave.getId())
            .executeUpdate();
    }

    private List<Oppgave> filterOppgaver(LocalDate filtrerFomDato, LocalDate filtrerTomDato) {
        var query = new Oppgavespørring(AVDELING_DRAMMEN_ENHET, KøSortering.FØRSTE_STØNADSDAG, List.of(), List.of(), List.of(), List.of(),
            Periodefilter.FAST_PERIODE, filtrerFomDato, filtrerTomDato, null, null, Filtreringstype.ALLE, null, null);
        return oppgaveKøRepository.hentOppgaver(query);
    }

    private Oppgave.Builder basicOppgaveBuilder() {
        return basicOppgaveBuilder(behandlingId1.toUUID());
    }

    private Oppgave.Builder basicOppgaveBuilder(UUID behandlingId) {
        return Oppgave.builder()
            .medBehandling(oppgaveRepository.hentBehandling(behandlingId))
            .medAktiv(true)
            .medBehandlendeEnhet(AVDELING_DRAMMEN_ENHET);
    }

    private Behandling.Builder basicBehandlingBuilder() {
        return basicBehandlingBuilder(behandlingId1.toUUID());
    }

    private Behandling.Builder basicBehandlingBuilder(UUID behandlingId) {
        return Behandling.builder(Optional.empty())
            .dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT)
            .medId(behandlingId)
            .medSaksnummer(new Saksnummer("1337"))
            .medAktørId(AktørId.dummy())
            .medBehandlingsfrist(LocalDate.now())
            .medBehandlendeEnhet(AVDELING_DRAMMEN_ENHET)
            .medOpprettet(LocalDateTime.now());
    }

    private void lagBehandling() {
        var førsteBehandling = Behandling.builder(Optional.empty())
            .dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT)
            .medId(behandlingId1.toUUID())
            .medSaksnummer(new Saksnummer("1337"))
            .medOpprettet(LocalDateTime.now())
            .medBehandlingsfrist(LocalDate.now());
        oppgaveRepository.lagreBehandling(førsteBehandling);
    }

    private Oppgave lagOppgave(String behandlendeEnhet) {
        return Oppgave.builder()
            .medBehandling(oppgaveRepository.hentBehandling(behandlingId1.toUUID()))
            .medBehandlendeEnhet(behandlendeEnhet)
            .medAktiv(true)
            .build();
    }

    private Oppgave.Builder tilbakekrevingOppgaveBuilder(UUID behandlingId) {
        return Oppgave.builder()
            .medAktiv(true)
            .medBehandling(oppgaveRepository.hentBehandling(behandlingId))
            .medBehandlendeEnhet(AVDELING_DRAMMEN_ENHET);
    }

    private Behandling.Builder tilbakekrevingBehandlingBuilder(UUID behandlingId) {
        return Behandling.builder(Optional.empty())
            .dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT)
            .medKildeSystem(Fagsystem.FPTILBAKE)
            .medBehandlingType(BehandlingType.TILBAKEBETALING)
            .medId(behandlingId);
    }

    private void persistFlush(Oppgave oppgave) {
        entityManager.persist(oppgave);
        entityManager.flush();
    }

    @Test
    void hentAktiveOppgaverForSaksnummer_returnerKunAktive() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId1.toUUID()).medSaksnummer(new Saksnummer("100")));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId2.toUUID()).medSaksnummer(new Saksnummer("200")));
        var aktivOppgave = basicOppgaveBuilder(behandlingId1.toUUID()).medAktiv(true).build();
        var inaktivOppgave = basicOppgaveBuilder(behandlingId2.toUUID()).medAktiv(false).build();
        entityManager.persist(aktivOppgave);
        entityManager.persist(inaktivOppgave);
        entityManager.flush();

        var result = oppgaveRepository.hentAktiveOppgaverForSaksnummer(List.of(new Saksnummer("100"), new Saksnummer("200")));

        assertThat(result).containsExactly(aktivOppgave);
    }

    @Test
    void hentAktiveOppgaverForSaksnummer_returnerTomListeNårIngenMatch() {
        var result = oppgaveRepository.hentAktiveOppgaverForSaksnummer(List.of(new Saksnummer("999")));
        assertThat(result).isEmpty();
    }

    @Test
    void hentAlleOppgaveFiltreReadOnly_returnerAlle() {
        var avdeling = avdelingDrammen(entityManager);
        var filtrering1 = lagFiltrering("Kø A", avdeling);
        var filtrering2 = lagFiltrering("Kø B", avdeling);
        entityManager.persist(filtrering1);
        entityManager.persist(filtrering2);
        entityManager.flush();

        var result = oppgaveRepository.hentAlleOppgaveFiltreReadOnly();

        assertThat(result).extracting(OppgaveFiltrering::getNavn).contains("Kø A", "Kø B");
    }

    @Test
    void lagreFiltrering_returnerIdOgKanHentes() {
        var avdeling = avdelingDrammen(entityManager);
        var filtrering = lagFiltrering("TestKø", avdeling);

        var id = oppgaveRepository.lagreFiltrering(filtrering);

        assertThat(id).isNotNull();
        var hentet = oppgaveRepository.hentOppgaveFilterSett(id);
        assertThat(hentet).isPresent();
        assertThat(hentet.get().getNavn()).isEqualTo("TestKø");
    }

    @Test
    void slettListe_sletterFiltreringen() {
        var avdeling = avdelingDrammen(entityManager);
        var filtrering = lagFiltrering("SlettMeg", avdeling);
        entityManager.persist(filtrering);
        entityManager.flush();

        oppgaveRepository.slettListe(filtrering);
        entityManager.flush();

        assertThat(oppgaveRepository.hentOppgaveFilterSett(filtrering.getId())).isEmpty();
    }

    @Test
    void sjekkOmOppgaverFortsattErTilgjengelige_ledigeOppgaverErTilgjengelige() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder());
        var oppgave = basicOppgaveBuilder().build();
        entityManager.persist(oppgave);
        entityManager.flush();

        assertThat(oppgaveRepository.sjekkOmOppgaverFortsattErTilgjengelige(List.of(oppgave.getId()))).isTrue();
    }

    @Test
    void sjekkOmOppgaverFortsattErTilgjengelige_reservertOppgaveErIkkeTilgjengelig() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder());
        var oppgave = basicOppgaveBuilder().build();
        entityManager.persist(oppgave);
        entityManager.flush();

        var reservasjon = new Reservasjon(oppgave, "Z999999");
        reservasjon.setReservertTil(LocalDateTime.now().plusHours(2));
        entityManager.persist(reservasjon);
        entityManager.flush();

        assertThat(oppgaveRepository.sjekkOmOppgaverFortsattErTilgjengelige(List.of(oppgave.getId()))).isFalse();
    }

    @Test
    void sjekkOmOppgaverFortsattErTilgjengelige_inaktivOppgaveErIkkeTilgjengelig() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder());
        var oppgave = basicOppgaveBuilder().medAktiv(false).build();
        entityManager.persist(oppgave);
        entityManager.flush();

        assertThat(oppgaveRepository.sjekkOmOppgaverFortsattErTilgjengelige(List.of(oppgave.getId()))).isFalse();
    }

    @Test
    void hentOppgave_returnerOppgaveMedGittId() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder());
        var oppgave = basicOppgaveBuilder().build();
        entityManager.persist(oppgave);
        entityManager.flush();

        var hentet = oppgaveRepository.hentOppgave(oppgave.getId());

        assertThat(hentet).isEqualTo(oppgave);
    }

    @Test
    void hentOppgaverReadOnly_returnerOppgaverForGitteIder() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId1.toUUID()));
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder(behandlingId2.toUUID()));
        var oppgave1 = basicOppgaveBuilder(behandlingId1.toUUID()).build();
        var oppgave2 = basicOppgaveBuilder(behandlingId2.toUUID()).build();
        entityManager.persist(oppgave1);
        entityManager.persist(oppgave2);
        entityManager.flush();

        var result = oppgaveRepository.hentOppgaverReadOnly(List.of(oppgave1.getId(), oppgave2.getId()));

        assertThat(result).containsExactlyInAnyOrder(oppgave1, oppgave2);
    }

    @Test
    void hentOppgaverReadOnly_tomListeGirTomtResultat() {
        assertThat(oppgaveRepository.hentOppgaverReadOnly(List.of())).isEmpty();
        assertThat(oppgaveRepository.hentOppgaverReadOnly(null)).isEmpty();
    }

    @Test
    void hentOppgaver_returnerAlleOppgaverForBehandlingId() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder());
        var oppgave1 = basicOppgaveBuilder().medAktiv(true).build();
        var oppgave2 = basicOppgaveBuilder().medAktiv(false).build();
        entityManager.persist(oppgave1);
        entityManager.persist(oppgave2);
        entityManager.flush();

        var result = oppgaveRepository.hentOppgaver(behandlingId1);

        assertThat(result).containsExactlyInAnyOrder(oppgave1, oppgave2);
    }

    @Test
    void hentAktivOppgave_returnerAktivOppgave() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder());
        var inaktiv = basicOppgaveBuilder().medAktiv(false).build();
        var aktiv = basicOppgaveBuilder().medAktiv(true).build();
        entityManager.persist(inaktiv);
        entityManager.persist(aktiv);
        entityManager.flush();

        var result = oppgaveRepository.hentAktivOppgave(behandlingId1);

        assertThat(result).isPresent().contains(aktiv);
    }

    @Test
    void hentAktivOppgave_ingenAktivReturnererEmpty() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder());
        var inaktiv = basicOppgaveBuilder().medAktiv(false).build();
        entityManager.persist(inaktiv);
        entityManager.flush();

        assertThat(oppgaveRepository.hentAktivOppgave(behandlingId1)).isEmpty();
    }

    @Test
    void opprettOppgave_persisterOgReturnererOppgave() {
        oppgaveRepository.lagreBehandling(basicBehandlingBuilder());
        var oppgave = basicOppgaveBuilder().build();

        var opprettet = oppgaveRepository.opprettOppgave(oppgave);
        entityManager.flush();

        assertThat(opprettet.getId()).isNotNull();
        assertThat(DBTestUtil.hentAlle(entityManager, Oppgave.class)).contains(opprettet);
    }

    @Test
    void tilknyttSaksbehandlerOppgaveFiltrering_leggerTilKnytning() {
        var avdeling = avdelingDrammen(entityManager);
        var saksbehandler = lagOgPersisterSaksbehandler("Z123456");
        var filtrering = lagFiltrering("TestKø", avdeling);
        entityManager.persist(filtrering);
        entityManager.flush();

        oppgaveRepository.tilknyttSaksbehandlerOppgaveFiltrering(saksbehandler, filtrering);
        entityManager.flush();

        assertThat(oppgaveRepository.saksbehandlereForOppgaveFiltrering(filtrering)).containsExactly(saksbehandler);
    }

    @Test
    void tilknyttSaksbehandlerOppgaveFiltrering_erIdempotent() {
        var avdeling = avdelingDrammen(entityManager);
        var saksbehandler = lagOgPersisterSaksbehandler("Z123456");
        var filtrering = lagFiltrering("TestKø", avdeling);
        entityManager.persist(filtrering);
        entityManager.flush();

        oppgaveRepository.tilknyttSaksbehandlerOppgaveFiltrering(saksbehandler, filtrering);
        oppgaveRepository.tilknyttSaksbehandlerOppgaveFiltrering(saksbehandler, filtrering);
        entityManager.flush();

        assertThat(oppgaveRepository.saksbehandlereForOppgaveFiltrering(filtrering)).containsExactly(saksbehandler);
    }

    @Test
    void fraknyttSaksbehandlerOppgaveFiltrering_fjernKnytning() {
        var avdeling = avdelingDrammen(entityManager);
        var saksbehandler = lagOgPersisterSaksbehandler("Z123456");
        var filtrering = lagFiltrering("TestKø", avdeling);
        entityManager.persist(filtrering);
        entityManager.flush();
        oppgaveRepository.tilknyttSaksbehandlerOppgaveFiltrering(saksbehandler, filtrering);
        entityManager.flush();

        oppgaveRepository.fraknyttSaksbehandlerOppgaveFiltrering(saksbehandler, filtrering);
        entityManager.flush();

        assertThat(oppgaveRepository.saksbehandlereForOppgaveFiltrering(filtrering)).isEmpty();
    }

    @Test
    void fraknyttAlleSaksbehandlereFraOppgaveFiltrering_fjernAlleKnytninger() {
        var avdeling = avdelingDrammen(entityManager);
        var sb1 = lagOgPersisterSaksbehandler("Z111111");
        var sb2 = lagOgPersisterSaksbehandler("Z222222");
        var filtrering = lagFiltrering("TestKø", avdeling);
        entityManager.persist(filtrering);
        entityManager.flush();
        oppgaveRepository.tilknyttSaksbehandlerOppgaveFiltrering(sb1, filtrering);
        oppgaveRepository.tilknyttSaksbehandlerOppgaveFiltrering(sb2, filtrering);
        entityManager.flush();

        oppgaveRepository.fraknyttAlleSaksbehandlereFraOppgaveFiltrering(filtrering);
        entityManager.flush();

        assertThat(oppgaveRepository.saksbehandlereForOppgaveFiltrering(filtrering)).isEmpty();
    }

    @Test
    void oppgaveFiltreringerForSaksbehandler_returnerTilknyttedeFiltreringer() {
        var avdeling = avdelingDrammen(entityManager);
        var saksbehandler = lagOgPersisterSaksbehandler("Z123456");
        var filtrering1 = lagFiltrering("Kø 1", avdeling);
        var filtrering2 = lagFiltrering("Kø 2", avdeling);
        entityManager.persist(filtrering1);
        entityManager.persist(filtrering2);
        entityManager.flush();
        oppgaveRepository.tilknyttSaksbehandlerOppgaveFiltrering(saksbehandler, filtrering1);
        oppgaveRepository.tilknyttSaksbehandlerOppgaveFiltrering(saksbehandler, filtrering2);
        entityManager.flush();

        var result = oppgaveRepository.oppgaveFiltreringerForSaksbehandler(saksbehandler);

        assertThat(result).containsExactlyInAnyOrder(filtrering1, filtrering2);
    }

    @Test
    void saksbehandlereForOppgaveFiltrering_returnerTilknyttedeSaksbehandlere() {
        var avdeling = avdelingDrammen(entityManager);
        var sb1 = lagOgPersisterSaksbehandler("Z111111");
        var sb2 = lagOgPersisterSaksbehandler("Z222222");
        var filtrering = lagFiltrering("TestKø", avdeling);
        entityManager.persist(filtrering);
        entityManager.flush();
        oppgaveRepository.tilknyttSaksbehandlerOppgaveFiltrering(sb1, filtrering);
        oppgaveRepository.tilknyttSaksbehandlerOppgaveFiltrering(sb2, filtrering);
        entityManager.flush();

        var result = oppgaveRepository.saksbehandlereForOppgaveFiltrering(filtrering);

        assertThat(result).containsExactlyInAnyOrder(sb1, sb2);
    }

    private OppgaveFiltrering lagFiltrering(String navn, Avdeling avdeling) {
        return new OppgaveFiltrering(navn, KøSortering.BEHANDLINGSFRIST, avdeling);
    }

    private Saksbehandler lagOgPersisterSaksbehandler(String ident) {
        var saksbehandler = new Saksbehandler(ident, "Test Testesen", AVDELING_DRAMMEN_ENHET);
        entityManager.persist(saksbehandler);
        entityManager.flush();
        return saksbehandler;
    }

}
