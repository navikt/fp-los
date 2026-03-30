package no.nav.foreldrepenger.los.migrering.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;

/**
 * DTO for migrating FiltreringSaksbehandlerRelasjon - only nøkkel values.
 */
public record FiltreringSaksbehandlerDataDto(
    @NotNull @Pattern(regexp = Saksbehandler.VALID_SAKSBEHANDLER_IDENT, message = "Ugyldig ident ${validatedValue}") String saksbehandlerId,
    @NotNull @Min(0) @Max(10_000_000) Long oppgaveFiltreringId
) {}

