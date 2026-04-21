package no.nav.foreldrepenger.los.statistikk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.foreldrepenger.los.statistikk.kø.InnslagType;
import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilter;

@ExtendWith(JpaExtension.class)
@ExtendWith(MockitoExtension.class)
class StatistikkRepositoryTest {

    private StatistikkRepository statistikkRepository;
    private EntityManager entityManager;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        this.statistikkRepository = new StatistikkRepository(entityManager);
        this.entityManager = entityManager;
    }

    @Test
    void skal_returner_en_rad_for_hver_id_hvor_tidspunktet_er_størst() {
        // Arrange
        var nå = LocalDateTime.now();
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(1L, toMs(nå.minusHours(1)), nå.toLocalDate(), 1, 0, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(1L, toMs(nå.minusHours(2)), nå.toLocalDate(), 2, 1, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(1L, toMs(nå.minusHours(3)), nå.toLocalDate(), 3, 2, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(1L, toMs(nå.minusHours(4)), nå.toLocalDate(), 4, 3, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(1L, toMs(nå.minusHours(5)), nå.toLocalDate(), 5, 4, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(2L, toMs(nå.minusHours(1)), nå.toLocalDate(), 1, 0, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(2L, toMs(nå.minusHours(2)), nå.toLocalDate(), 2, 1, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(2L, toMs(nå.minusHours(3)), nå.toLocalDate(), 3, 2, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(2L, toMs(nå.minusHours(4)), nå.toLocalDate(), 4, 3, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(2L, toMs(nå.minusHours(5)), nå.toLocalDate(), 5, 4, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(3L, toMs(nå.minusHours(1)), nå.toLocalDate(), 1, 4, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(new StatistikkOppgaveFilter(3L, toMs(nå.minusHours(5)), nå.toLocalDate(), 2, 2, 2, 3, 4, InnslagType.REGELMESSIG));
        entityManager.flush();


        // Act
        var statistikkMap = statistikkRepository.hentSisteStatistikkForOppgaveFiltre(Set.of(1L, 2L, 3L));

        // Assert
        assertThat(statistikkMap).hasSize(3);
        assertThat(statistikkMap.get(1L).getAntallAktive()).isEqualTo(1);
        assertThat(statistikkMap.get(2L).getAntallAktive()).isEqualTo(1);
        assertThat(statistikkMap.get(3L).getAntallAktive()).isEqualTo(1);
        assertThat(statistikkMap.get(3L).getAntallVentende()).isEqualTo(2);
    }

    @Test
    void skal_returnere_statistikk_kun_for_spesifiserte_filter_ider() {
        // Arrange
        var nå = LocalDateTime.now();
        statistikkRepository.lagreStatistikkOppgaveFilter(
            new StatistikkOppgaveFilter(1L, toMs(nå.minusHours(1)), nå.toLocalDate(), 10, 5, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(
            new StatistikkOppgaveFilter(1L, toMs(nå.minusHours(2)), nå.toLocalDate(), 20, 15, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(
            new StatistikkOppgaveFilter(2L, toMs(nå.minusHours(1)), nå.toLocalDate(), 30, 25, 2, 3, 4, InnslagType.REGELMESSIG));
        statistikkRepository.lagreStatistikkOppgaveFilter(
            new StatistikkOppgaveFilter(3L, toMs(nå.minusHours(1)), nå.toLocalDate(), 40, 35, 2, 3, 4, InnslagType.REGELMESSIG));
        entityManager.flush();

        // Act - hent kun for filter 1 og 3
        var statistikkMap = statistikkRepository.hentSisteStatistikkForOppgaveFiltre(Set.of(1L, 3L));

        // Assert
        assertThat(statistikkMap).hasSize(2).containsOnlyKeys(1L, 3L);
        assertThat(statistikkMap.get(1L).getAntallAktive()).isEqualTo(10); // siste for filter 1
        assertThat(statistikkMap.get(3L).getAntallAktive()).isEqualTo(40);
    }

    @Test
    void skal_returnere_tomt_map_for_tom_input() {
        var statistikkMap = statistikkRepository.hentSisteStatistikkForOppgaveFiltre(Set.of());
        assertThat(statistikkMap).isEmpty();
    }

    private static Long toMs(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
