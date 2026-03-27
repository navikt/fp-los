package no.nav.foreldrepenger.los.migrering.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.felles.util.validering.ValidKodeverk;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * DTO for migrating OppgaveEgenskap entities.
 */
public record OppgaveEgenskapDataDto(
    @ValidKodeverk AndreKriterierType andreKriterierType,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String sisteSaksbehandlerForTotrinn
) {
}


