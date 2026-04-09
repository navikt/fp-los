package no.nav.foreldrepenger.los.migrering.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.felles.util.validering.ValidKodeverk;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;

/**
 * DTO for migrating OppgaveEgenskap entities.
 */
public record OppgaveEgenskapDataDto(
    @ValidKodeverk AndreKriterierType andreKriterierType,
    @Size(max = 20) @Pattern(regexp = Saksbehandler.VALID_SAKSBEHANDLER_IDENT, message = "Ugyldig ident ${validatedValue}") String sisteSaksbehandlerForTotrinn
) {
}


