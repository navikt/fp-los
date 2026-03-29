package no.nav.foreldrepenger.los.migrering.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;

/**
 * DTO for migrating AvdelingSaksbehandler relationship entities (composite key)
 */
public record AvdelingSaksbehandlerDataDto(
    @NotNull @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}") String avdelingId,
    @NotNull @Pattern(regexp = Saksbehandler.VALID_SAKSBEHANDLER_IDENT, message = "Ugyldig ident ${validatedValue}") String saksbehandlerId
    ) {}
