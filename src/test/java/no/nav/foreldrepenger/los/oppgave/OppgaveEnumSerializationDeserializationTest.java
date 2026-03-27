package no.nav.foreldrepenger.los.oppgave;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class OppgaveEnumSerializationDeserializationTest {

    @Test
    void fagsakStatus() {
        testRoundtrip(FagsakStatus.LØPENDE);
    }

    @Test
    void andreKriterierType() {
        testRoundtrip(AndreKriterierType.TIL_BESLUTTER);
    }

    @Test
    void behandlingType() {
        testRoundtrip(BehandlingType.FØRSTEGANGSSØKNAD);
    }

    private void testRoundtrip(Object initiell)  {
        var json = DefaultJsonMapper.toJson(initiell);
        var roundtripped = DefaultJsonMapper.fromJson(json, initiell.getClass());
        assertThat(initiell).isEqualTo(roundtripped);
    }


}
