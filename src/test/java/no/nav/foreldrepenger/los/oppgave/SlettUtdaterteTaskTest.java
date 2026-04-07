package no.nav.foreldrepenger.los.oppgave;

import static no.nav.foreldrepenger.los.organisasjon.Avdeling.AVDELING_DRAMMEN_ENHET;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.foreldrepenger.los.oppgavekø.SlettUtdaterteTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(JpaExtension.class)
class SlettUtdaterteTaskTest {


    private EntityManager entityManager;


    @BeforeEach
    void setup(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Test
    void testSletting() {
        lagStandardSettMedOppgaver();
        assertDoesNotThrow(() -> new SlettUtdaterteTask(entityManager).doTask(ProsessTaskData.forProsessTask(SlettUtdaterteTask.class)));
    }

    private void lagStandardSettMedOppgaver() {
        var behandlingId = UUID.randomUUID();

        var behandling = Behandling.builder(Optional.empty())
            .dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.AVSLUTTET)
            .medId(behandlingId)
            .medOpprettet(LocalDateTime.now().minusDays(10))
            .medBehandlingsfrist(LocalDate.now().plusDays(10))
            .medAvsluttet(LocalDateTime.now().minusMonths(5))
            .medKriterier(Set.of(AndreKriterierType.PAPIRSØKNAD, AndreKriterierType.TIL_BESLUTTER))
            .build();
        entityManager.persist(behandling);

        var oppgave = Oppgave.builder()
            .dummyOppgave(AVDELING_DRAMMEN_ENHET, behandling)
            .medAktiv(false)
            .medKriterier(Set.of(AndreKriterierType.PAPIRSØKNAD, AndreKriterierType.TIL_BESLUTTER), "z999999")
            .build();
        oppgave.setEndretTidspunkt(LocalDateTime.now().minusMonths(5));

        entityManager.persist(oppgave);
        entityManager.flush();
    }

}
