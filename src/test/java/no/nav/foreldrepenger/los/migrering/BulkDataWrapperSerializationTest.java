package no.nav.foreldrepenger.los.migrering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;

/**
 * Validates the JSON round-trip contract between FSS and GCP.
 * Catches issues like missing @JsonProperty, @JsonCreator, or enum serialization problems.
 */
class BulkDataWrapperSerializationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Test
    void shouldSerializeAndDeserialize_organisasjonOgKøer() throws Exception {
        var original = TestMigreringData.lagOrganisasjonOgKøer();

        var json = MAPPER.writeValueAsString(original);
        var deserialized = MAPPER.readValue(json, BulkDataWrapper.class);

        assertThat(deserialized.organisasjonData().avdelinger()).hasSize(1);
        assertThat(deserialized.organisasjonData().saksbehandlere()).hasSize(1);
        assertThat(deserialized.organisasjonData().saksbehandlerGrupper()).hasSize(1);
        assertThat(deserialized.organisasjonData().avdelingSaksbehandlere()).hasSize(1);
        assertThat(deserialized.køOppsettDto().oppgaveFiltrering()).hasSize(1);
        assertThat(deserialized.køOppsettDto().saksbehandlerKøer()).hasSize(1);

        assertThat(deserialized.behandlinger()).isEmpty();
        assertThat(deserialized.aktiveOppgaver()).isEmpty();
        assertThat(deserialized.inaktiveOppgaver()).isEmpty();
    }

    @Test
    void shouldSerializeAndDeserialize_aktiveOppgaver() throws Exception {
        var original = TestMigreringData.lagAktiveOppgaver();

        var json = MAPPER.writeValueAsString(original);
        var deserialized = MAPPER.readValue(json, BulkDataWrapper.class);

        assertThat(deserialized.aktiveOppgaver()).hasSize(2);
        assertThat(deserialized.aktiveOppgaver().get(0).saksnummer().saksnummer()).isEqualTo("123456");
        assertThat(deserialized.aktiveOppgaver().get(0).oppgaveEgenskaper()).hasSize(1);

        // Second oppgave has a reservasjon
        assertThat(deserialized.aktiveOppgaver().get(1).reservasjonDataDto()).isNotNull();
        assertThat(deserialized.aktiveOppgaver().get(1).reservasjonDataDto().reservertAv()).isEqualTo("Z999999");
    }

    @Test
    void shouldSerializeAndDeserialize_behandlinger() throws Exception {
        var original = TestMigreringData.lagBehandlinger();

        var json = MAPPER.writeValueAsString(original);
        var deserialized = MAPPER.readValue(json, BulkDataWrapper.class);

        assertThat(deserialized.behandlinger()).hasSize(2);
        assertThat(deserialized.behandlinger().getFirst().saksnummer().saksnummer()).isEqualTo("123456");
        assertThat(deserialized.behandlinger().getFirst().egenskaper()).containsExactly(
            no.nav.foreldrepenger.los.oppgave.AndreKriterierType.PAPIRSØKNAD);
    }

    @Test
    void shouldHandleEmptyBulkDataWrapper() throws Exception {
        var original = BulkDataWrapper.behandlinger(List.of());

        var json = MAPPER.writeValueAsString(original);
        var deserialized = MAPPER.readValue(json, BulkDataWrapper.class);

        assertThat(deserialized.behandlinger()).isEmpty();
        assertThat(deserialized.aktiveOppgaver()).isEmpty();
        assertThat(deserialized.inaktiveOppgaver()).isEmpty();
    }
}

