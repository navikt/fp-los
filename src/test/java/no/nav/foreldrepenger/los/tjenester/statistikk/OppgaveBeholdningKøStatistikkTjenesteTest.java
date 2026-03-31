package no.nav.foreldrepenger.los.tjenester.statistikk;

import static no.nav.foreldrepenger.los.organisasjon.Avdeling.AVDELING_DRAMMEN_ENHET;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.DBTestUtil;
import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingTilstand;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgave.OppgaveRepository;
import no.nav.foreldrepenger.los.tjenester.avdelingsleder.nøkkeltall.NøkkeltallRepository;

@ExtendWith(JpaExtension.class)
@ExtendWith(MockitoExtension.class)
class OppgaveBeholdningKøStatistikkTjenesteTest {

    private NøkkeltallRepository nøkkeltallRepository;
    private OppgaveRepository oppgaveRepository;
    private EntityManager entityManager;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        this.nøkkeltallRepository = new NøkkeltallRepository(entityManager);
        this.entityManager = entityManager;
        this.oppgaveRepository = new OppgaveRepository(entityManager);
        DBTestUtil.avdelingDrammen(entityManager);
    }

    private void leggInnEttSettMedOppgaver() {
        var fgbid = UUID.randomUUID();
        var fgb2id = UUID.randomUUID();
        var klageid = UUID.randomUUID();
        var innsynid = UUID.randomUUID();
        var beslid = UUID.randomUUID();
        var besl2id = UUID.randomUUID();
        var lukketid = UUID.randomUUID();
        oppgaveRepository.lagreBehandling(
            Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT).medId(fgbid));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT).medId(fgb2id));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT).medId(klageid).medBehandlingType(BehandlingType.KLAGE));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT).medId(innsynid).medBehandlingType(BehandlingType.INNSYN));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.BESLUTTER).medId(beslid));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.BESLUTTER).medId(besl2id));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.VENT_MANUELL).medId(lukketid));

        var førstegangOppgave = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(fgbid)).build();
        var førstegangOppgave2 = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(fgb2id)).build();
        var klageOppgave = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(klageid)).build();
        var innsynOppgave = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(innsynid)).build();
        var beslutterOppgave = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(beslid))
            .medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER), "z999999")
            .build();
        var beslutterOppgave2 = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(besl2id))
            .medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER), "z999999")
            .build();
        var lukketOppgave = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(lukketid)).medAktiv(false).build();
        entityManager.persist(førstegangOppgave);
        entityManager.persist(førstegangOppgave2);
        entityManager.persist(klageOppgave);
        entityManager.persist(innsynOppgave);
        entityManager.persist(beslutterOppgave);
        entityManager.persist(beslutterOppgave2);
        entityManager.persist(lukketOppgave);
        entityManager.flush();
    }

    @Test
    void hentAlleOppgaverForAvdelingTest() {
        leggInnEttSettMedOppgaver();
        var resultater = nøkkeltallRepository.hentAlleOppgaverForAvdeling(AVDELING_DRAMMEN_ENHET);

        assertThat(resultater).hasSize(4);
        assertThat(resultater.get(0).fagsakYtelseType()).isEqualTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(resultater.get(0).behandlingType()).isEqualTo(BehandlingType.FØRSTEGANGSSØKNAD);
        assertThat(resultater.get(0).tilBehandling()).isFalse();
        assertThat(resultater.get(0).antall()).isEqualTo(2L);

        assertThat(resultater.get(1).fagsakYtelseType()).isEqualTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(resultater.get(1).behandlingType()).isEqualTo(BehandlingType.FØRSTEGANGSSØKNAD);
        assertThat(resultater.get(1).tilBehandling()).isTrue();
        assertThat(resultater.get(1).antall()).isEqualTo(2L);

        assertThat(resultater.get(2).antall()).isEqualTo(1L);
        assertThat(resultater.get(3).antall()).isEqualTo(1L);
    }

    @Test
    void hentOppgaverPerFørsteStønadsdagMåned() {
        leggInnEttSettMedOppgaver();
        var idag = LocalDate.now();
        var idagPlusMnd = idag.plusMonths(1);
        var resultater = nøkkeltallRepository.hentOppgaverPerFørsteStønadsdagMåned(AVDELING_DRAMMEN_ENHET);
        assertThat(resultater).hasSize(1);
        assertThat(resultater.get(0).førsteStønadsdag()).isEqualTo(idagPlusMnd.withDayOfMonth(1));
        assertThat(resultater.get(0).antall()).isEqualTo(4L);
    }

}
