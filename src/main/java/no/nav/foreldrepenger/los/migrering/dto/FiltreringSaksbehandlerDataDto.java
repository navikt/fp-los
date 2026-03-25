package no.nav.foreldrepenger.los.migrering.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO for migrating FiltreringSaksbehandlerRelasjon - only nøkkel values.
 */
public record FiltreringSaksbehandlerDataDto(
    @Min(0) @Max(10_000_000) Long saksbehandlerId,
    @Min(0) @Max(10_000_000) Long oppgaveFiltreringId
) {}

