package no.nav.foreldrepenger.los.oppgave;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;

@Embeddable
public record OppgaveEgenskap(
    @Enumerated(EnumType.STRING)
    @Column(name = "ANDRE_KRITERIER_TYPE", nullable = false)
    AndreKriterierType andreKriterierType,

    // brukes i query for å ekskludere egne oppgaver i beslutterkøer
    @Pattern(regexp = Saksbehandler.VALID_SAKSBEHANDLER_IDENT, message = "Ugyldig ident ${validatedValue}")
    @Column(name = "siste_saksbehandler_totrinn")
    String sisteSaksbehandlerForTotrinn
) implements Serializable {

    public OppgaveEgenskap {
        if (sisteSaksbehandlerForTotrinn != null) {
            sisteSaksbehandlerForTotrinn = sisteSaksbehandlerForTotrinn.trim().toUpperCase();
        }
    }
}
