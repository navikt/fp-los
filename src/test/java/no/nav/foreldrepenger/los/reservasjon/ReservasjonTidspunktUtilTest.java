package no.nav.foreldrepenger.los.reservasjon;


import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static no.nav.foreldrepenger.los.reservasjon.ReservasjonTidspunktUtil.JUSTER_TIL_NESTE_UKEDAG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservasjonTidspunktUtilTest {

    @Test
    void skalGodtaGyldigeDatoer() {
        for (var dagerFraIdag : new long[]{0, 10, 30}) {
            var date = LocalDate.now().plusDays(dagerFraIdag);
            assertDoesNotThrow(() -> ReservasjonTidspunktUtil.validerReservasjonsdato(date));
        }
    }

    @Test
    void skalFeileForUgyldigeDatoer() {
        for (var dagerFraIdag : new long[]{-1, 31}) {
            var date = LocalDate.now().plusDays(dagerFraIdag);
            assertThrows(IllegalArgumentException.class, () -> ReservasjonTidspunktUtil.validerReservasjonsdato(date));
        }
    }

    @Test
    void skalJustereDatoTilUkedagOgSluttenAvDagen() {
        var søndag = LocalDateTime.of(2026, 3, 15, 15, 43);
        var justertTidspunkt = søndag.with(JUSTER_TIL_NESTE_UKEDAG);
        assertThat(justertTidspunkt).isEqualTo(LocalDateTime.of(2026, 3, 16, 23, 59, 59));
    }

    @Test
    void tomNesteUkedagSkalGiTidspunktEnDagFrem() {
        var before = LocalDateTime.now();
        var tidspunkt = ReservasjonTidspunktUtil.tomNesteUkedag();
        var after = LocalDateTime.now();

        assertThat(tidspunkt).isAfterOrEqualTo(before.plusDays(1).with(JUSTER_TIL_NESTE_UKEDAG));
        assertThat(tidspunkt).isBeforeOrEqualTo(after.plusDays(1).with(JUSTER_TIL_NESTE_UKEDAG));
    }

    @Test
    void tomUkedagEnUkeFremSkalGiTidspunktEnUkeFrem() {
        var before = LocalDateTime.now();
        var tidspunkt = ReservasjonTidspunktUtil.tomSjuDagerFremJustertTilNesteUkedag();
        var after = LocalDateTime.now();

        assertThat(tidspunkt).isAfterOrEqualTo(before.plusDays(7).with(JUSTER_TIL_NESTE_UKEDAG));
        assertThat(tidspunkt).isBeforeOrEqualTo(after.plusDays(7).with(JUSTER_TIL_NESTE_UKEDAG));
    }

    @Test
    void justerTilNesteUkedagSkalHoppeOverHelgUtenÅLeggeTilDag() {
        var lørdag = LocalDateTime.of(2026, 3, 14, 8, 0);
        var justert = ReservasjonTidspunktUtil.justerTilNesteUkedag(lørdag);
        assertThat(justert).isEqualTo(LocalDateTime.of(2026, 3, 16, 23, 59, 59));
    }

    @Test
    void justerTilNesteUkedagSkalBeholdeUkedagenNårDetAlleredeErEnUkedag() {
        var tirsdag = LocalDateTime.of(2026, 3, 17, 9, 0);
        var justert = ReservasjonTidspunktUtil.justerTilNesteUkedag(tirsdag);
        assertThat(justert).isEqualTo(LocalDateTime.of(2026, 3, 17, 23, 59, 59));
    }
}
