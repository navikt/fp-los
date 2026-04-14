package no.nav.foreldrepenger.los.oppgavekø;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;

@Embeddable
public record FiltreringAndreKriterierType(
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ANDRE_KRITERIER_TYPE", nullable = false)
    AndreKriterierType andreKriterierType,

    @NotNull
    @Column(name = "INKLUDER", nullable = false)
    boolean inkluder
) implements Serializable {
}
