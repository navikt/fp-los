package no.nav.foreldrepenger.los.oppgave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import no.nav.foreldrepenger.los.oppgavekø.KøSortering;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OppgavespørringTest {

    @ParameterizedTest
    @MethodSource("behandlingTilstandScenarier")
    void aktuelleBehandlingTilstander(
        List<AndreKriterierType> inkluder,
        List<AndreKriterierType> ekskluder,
        List<BehandlingTilstand> forventet
    ) {
        assertThat(spørring(inkluder, ekskluder).aktuelleBehandlingTilstander())
            .containsExactlyInAnyOrderElementsOf(forventet);
    }

    static Stream<Arguments> behandlingTilstandScenarier() {
        return Stream.of(
            arguments(
                List.of(),
                List.of(),
                List.of(BehandlingTilstand.AKSJONSPUNKT, BehandlingTilstand.BESLUTTER, BehandlingTilstand.PAPIRSØKNAD)
            ),
            arguments(
                List.of(AndreKriterierType.TERMINBEKREFTELSE),
                List.of(AndreKriterierType.TERMINBEKREFTELSE),
                List.of(BehandlingTilstand.AKSJONSPUNKT, BehandlingTilstand.BESLUTTER, BehandlingTilstand.PAPIRSØKNAD)
            ),
            arguments(
                List.of(AndreKriterierType.TIL_BESLUTTER),
                List.of(),
                List.of(BehandlingTilstand.BESLUTTER)
            ),
            arguments(
                List.of(AndreKriterierType.PAPIRSØKNAD),
                List.of(),
                List.of(BehandlingTilstand.PAPIRSØKNAD)
            ),
            arguments(
                List.of(),
                List.of(AndreKriterierType.TIL_BESLUTTER),
                List.of(BehandlingTilstand.AKSJONSPUNKT, BehandlingTilstand.PAPIRSØKNAD)
            )
        );
    }

    private static Oppgavespørring spørring(List<AndreKriterierType> inkluder, List<AndreKriterierType> ekskluder) {
        return new Oppgavespørring("4867", KøSortering.BEHANDLINGSFRIST, List.of(), List.of(), inkluder, ekskluder,
            Periodefilter.FAST_PERIODE, null, null, null, null, Filtreringstype.LEDIGE, null, null);
    }
}

