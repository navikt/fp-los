package no.nav.foreldrepenger.los.migrering.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO for migrating AvdelingSaksbehandler relationship entities (composite key)
 */
public record AvdelingSaksbehandlerDataDto(
    @Min(0) @Max(10_000_000) Long avdelingId,
    @Min(0) @Max(10_000_000) Long saksbehandlerId
) {}
