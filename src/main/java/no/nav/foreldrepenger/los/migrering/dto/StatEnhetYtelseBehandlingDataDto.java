package no.nav.foreldrepenger.los.migrering.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.felles.util.validering.ValidKodeverk;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;

/**
 * DTO for migrating StatistikkEnhetYtelseBehandling entities.
 * Maps to table stat_enhet_ytelse_behandling.
 */
public record StatEnhetYtelseBehandlingDataDto(
    @NotNull @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}") String behandlendeEnhet,
    @NotNull @Min(0) @Max(Long.MAX_VALUE) Long tidsstempel,
    @NotNull @ValidKodeverk FagsakYtelseType fagsakYtelseType,
    @NotNull @ValidKodeverk BehandlingType behandlingType,
    @NotNull LocalDate statistikkDato,
    @NotNull @Min(0) @Max(Integer.MAX_VALUE) Integer antallAktive,
    @NotNull @Min(0) @Max(Integer.MAX_VALUE) Integer antallOpprettet,
    @NotNull @Min(0) @Max(Integer.MAX_VALUE) Integer antallAvsluttet
) {}
