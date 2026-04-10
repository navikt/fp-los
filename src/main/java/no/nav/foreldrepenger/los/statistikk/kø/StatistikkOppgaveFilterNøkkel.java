package no.nav.foreldrepenger.los.statistikk.kø;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record StatistikkOppgaveFilterNøkkel(@NotNull @Column(name = "OPPGAVE_FILTER_ID", updatable = false, nullable = false) Long oppgaveFilterId,
                                            @NotNull @Column(name = "TIDSSTEMPEL", updatable = false, nullable = false) Long tidsstempel) {
}
