package no.nav.foreldrepenger.los.migrering.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * DTO for migrating Avdeling entities
 */
public record AvdelingDataDto(
    @NotNull @Size(max = 500) @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}") String avdelingEnhet,
    @NotNull @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String navn,
    @NotNull boolean kreverKode6,
    @NotNull boolean aktiv,
    @NotNull @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    @NotNull LocalDateTime opprettetTidspunkt,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt
) {}
