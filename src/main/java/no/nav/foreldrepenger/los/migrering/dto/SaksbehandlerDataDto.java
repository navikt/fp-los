package no.nav.foreldrepenger.los.migrering.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.vedtak.util.InputValideringRegex;

import java.time.LocalDateTime;

/**
 * DTO for migrating Saksbehandler entities
 */
public record SaksbehandlerDataDto(
    @Min(0) @Max(10_000_000) Long id,  // Primary key
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String saksbehandlerIdent,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String navn,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String ansattVedEnhet,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    LocalDateTime opprettetTidspunkt,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt
) {}
