package no.nav.foreldrepenger.los.migrering.fss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.los.migrering.fss.FssGcpMigrasjonTask.MigreringSteg;

class MigreringStegTest {

    @Test
    void neste_shouldProgressThroughAllSteps() {
        assertThat(MigreringSteg.DEL1_ORGANISASJON_OG_KØ.neste()).isEqualTo(MigreringSteg.DEL2_AKTIVE_OPPGAVER);
        assertThat(MigreringSteg.DEL2_AKTIVE_OPPGAVER.neste()).isEqualTo(MigreringSteg.DEL3_INAKTIVE_OPPGAVER);
        assertThat(MigreringSteg.DEL3_INAKTIVE_OPPGAVER.neste()).isEqualTo(MigreringSteg.DEL4_BEHANDLINGER);
        assertThat(MigreringSteg.DEL4_BEHANDLINGER.neste()).isEqualTo(MigreringSteg.DEL5_STATISTIKK);
        assertThat(MigreringSteg.DEL5_STATISTIKK.neste()).isEqualTo(MigreringSteg.DEL6_FERDIG);
    }

    @Test
    void erFerdig_del1_alwaysTrue() {
        assertThat(MigreringSteg.DEL1_ORGANISASJON_OG_KØ.erFerdig(0, 1000)).isTrue();
        assertThat(MigreringSteg.DEL1_ORGANISASJON_OG_KØ.erFerdig(1, 1000)).isTrue();
    }

    @Test
    void erFerdig_batchSteps_whenFullBatch_notFinished() {
        assertThat(MigreringSteg.DEL2_AKTIVE_OPPGAVER.erFerdig(1000, 1000)).isFalse();
        assertThat(MigreringSteg.DEL3_INAKTIVE_OPPGAVER.erFerdig(1000, 1000)).isFalse();
        assertThat(MigreringSteg.DEL4_BEHANDLINGER.erFerdig(1000, 1000)).isFalse();
    }

    @Test
    void erFerdig_batchSteps_whenPartialBatch_finished() {
        assertThat(MigreringSteg.DEL2_AKTIVE_OPPGAVER.erFerdig(500, 1000)).isTrue();
        assertThat(MigreringSteg.DEL3_INAKTIVE_OPPGAVER.erFerdig(999, 1000)).isTrue();
        assertThat(MigreringSteg.DEL4_BEHANDLINGER.erFerdig(0, 1000)).isTrue();
    }

    @Test
    void erFerdig_del6_shouldThrow() {
        assertThatThrownBy(() -> MigreringSteg.DEL6_FERDIG.erFerdig(0, 1000))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hent_del6_shouldThrow() {
        assertThatThrownBy(() -> MigreringSteg.DEL6_FERDIG.hent(null, 0, 1000))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hentetAntall_del5_shouldReturnZero() {
        var bulkData = no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper.behandlinger(java.util.List.of());
        assertThat(MigreringSteg.DEL6_FERDIG.hentetAntall(bulkData)).isZero();
    }
}

