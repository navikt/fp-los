package no.nav.foreldrepenger.los.migrering.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * DTO for migrating Reservasjon entities with preserved PKs
 */
public record ReservasjonDataDto(
    @Min(0) @Max(10_000_000) Long oppgaveId,
    LocalDateTime reservertTil,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String reservertAv,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String flyttetAv,
    LocalDateTime flyttetTidspunkt,
    @Size(max = 4000) @Pattern(regexp = InputValideringRegex.FRITEKST) String begrunnelse,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    LocalDateTime opprettetTidspunkt,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt
) {
}
