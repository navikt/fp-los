package no.nav.foreldrepenger.los.migrering.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * DTO for migrating SaksbehandlerGruppe entities
 */
public record SaksbehandlerGruppeDataDto(
    @NotNull @Min(0) @Max(10_000_000) Long id,  // Primary key
    @NotNull @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String gruppeNavn,
    @NotNull @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}") String avdelingId,
    @NotNull @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    @NotNull LocalDateTime opprettetTidspunkt,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt
) {}
