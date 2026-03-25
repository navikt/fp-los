package no.nav.foreldrepenger.los.migrering.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.vedtak.util.InputValideringRegex;

import java.time.LocalDateTime;

/**
 * DTO for migrating Avdeling entities
 */
public record AvdelingDataDto(
    @Min(0) @Max(10_000_000) Long id,  // Primary key
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String avdelingEnhet,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String navn,
    boolean kreverKode6,
    boolean aktiv,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    LocalDateTime opprettetTidspunkt,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt
) {}
