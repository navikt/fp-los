package no.nav.foreldrepenger.los.migrering.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * DTO for migrating Oppgave entities with preserved PKs
 */
public record OppgaveDataDto(
    @NotNull @Min(1) @Max(100_000_000) Long id,
    @NotNull UUID behandlingId,
    @NotNull @Size(max = 10) @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}") String behandlendeEnhet,
    boolean aktiv,
    LocalDateTime oppgaveAvsluttet,
    @NotNull @Size(max = 20) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    @NotNull LocalDateTime opprettetTidspunkt,
    @Size(max = 20) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt,
    @Valid ReservasjonDataDto reservasjonDataDto,
    List<@Valid OppgaveEgenskapDataDto> oppgaveEgenskaper
) {
}
