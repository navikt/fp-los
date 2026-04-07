package no.nav.foreldrepenger.los.statistikk;

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
import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingTilstand;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgave.OppgaveRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(JpaExtension.class)
@ExtendWith(MockitoExtension.class)
class StatistikkEnhetYtelseBehandlingTest {

    private static final String ANNEN_ENHET = "5555";

    private EntityManager entityManager;
    private SnapshotEnhetYtelseBehandlingTask snapshotTask;
    private StatistikkRepository statistikkRepository;
    private OppgaveRepository oppgaveRepository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        this.statistikkRepository = new StatistikkRepository(entityManager);
        this.snapshotTask = new SnapshotEnhetYtelseBehandlingTask(statistikkRepository);
        this.entityManager = entityManager;
        this.oppgaveRepository = new OppgaveRepository(entityManager);
    }

    private void avsluttOppgave(Oppgave oppgave) {
        oppgave.avsluttOppgave();
        entityManager.merge(oppgave);
        entityManager.flush();
    }

    @Test
    void taSnapshotHentResultat() {
        var fgbid = UUID.randomUUID();
        var fgb2id = UUID.randomUUID();
        var klageid = UUID.randomUUID();
        var innsynid = UUID.randomUUID();
        var annenavdid = UUID.randomUUID();
        var beslid = UUID.randomUUID();
        var besl2id = UUID.randomUUID();
        var lukketid = UUID.randomUUID();
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT).medId(fgbid));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT).medId(fgb2id));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT).medId(klageid).medBehandlingType(BehandlingType.KLAGE));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AKSJONSPUNKT).medId(innsynid).medBehandlingType(BehandlingType.INNSYN));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(ANNEN_ENHET, BehandlingTilstand.AKSJONSPUNKT).medId(annenavdid));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.BESLUTTER).medId(beslid));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.BESLUTTER).medId(besl2id));
        oppgaveRepository.lagreBehandling(Behandling.builder(Optional.empty()).dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.VENT_MANUELL).medId(lukketid));

        var førstegangOppgave = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(fgbid)).build();
        var førstegangOppgave2 = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(fgb2id)).build();
        var klageOppgave = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(klageid)).build();
        var innsynOppgave = Oppgave.builder().dummyOppgave(AVDELING_DRAMMEN_ENHET, oppgaveRepository.hentBehandling(innsynid)).build();
        var annenAvdeling = Oppgave.builder().dummyOppgave(ANNEN_ENHET, oppgaveRepository.hentBehandling(annenavdid)).build();
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
        entityManager.persist(annenAvdeling);
        entityManager.persist(beslutterOppgave);
        entityManager.persist(beslutterOppgave2);
        entityManager.persist(lukketOppgave);
        entityManager.flush();

        snapshotTask.doTask(ProsessTaskData.forProsessTask(SnapshotEnhetYtelseBehandlingTask.class));

        var resultater0 = statistikkRepository.hentStatistikkFomDato(LocalDate.now().minusWeeks(1));
        assertThat(resultater0).hasSize(4);
        assertThat(resultater0.stream().filter(r -> AVDELING_DRAMMEN_ENHET.equals(r.getBehandlendeEnhet())).count()).isEqualTo(3);
        assertThat(resultater0.stream().map(StatistikkEnhetYtelseBehandling::getAntallAvsluttet).reduce(0, Integer::sum)).isZero();

        var resultater = statistikkRepository.hentStatistikkForEnhetFomDato(AVDELING_DRAMMEN_ENHET, LocalDate.now().minusWeeks(1));
        assertThat(resultater).hasSize(3);
        assertThat(resultater.stream().filter(r -> AVDELING_DRAMMEN_ENHET.equals(r.getBehandlendeEnhet())).count()).isEqualTo(3);
        assertThat(resultater.stream().map(StatistikkEnhetYtelseBehandling::getAntallAvsluttet).reduce(0, Integer::sum)).isZero();

        avsluttOppgave(beslutterOppgave);
        snapshotTask.doTask(ProsessTaskData.forProsessTask(SnapshotEnhetYtelseBehandlingTask.class));
        resultater = statistikkRepository.hentStatistikkForEnhetFomDato(AVDELING_DRAMMEN_ENHET, LocalDate.now().minusWeeks(1));
        assertThat(resultater.stream().map(StatistikkEnhetYtelseBehandling::getAntallAvsluttet).reduce(0, Integer::sum)).isEqualTo(1);
    }

}
