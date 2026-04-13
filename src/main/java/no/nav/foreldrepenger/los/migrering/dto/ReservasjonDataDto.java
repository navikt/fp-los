package no.nav.foreldrepenger.los.migrering.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * DTO for migrating Reservasjon entities
 */
public record ReservasjonDataDto(
    LocalDateTime reservertTil,
    @NotNull @Size(max = 20) @Pattern(regexp = Saksbehandler.VALID_SAKSBEHANDLER_IDENT, message = "Ugyldig ident ${validatedValue}") String reservertAv,
    @Size(max = 100) @Pattern(regexp = InputValideringRegex.FRITEKST) String flyttetAv,
    LocalDateTime flyttetTidspunkt,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String begrunnelse,
    @NotNull @Size(max = 20) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    @NotNull LocalDateTime opprettetTidspunkt,
    @Size(max = 20) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt
) {
}
