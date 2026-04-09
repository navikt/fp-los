package no.nav.foreldrepenger.los.migrering.dto;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.los.felles.util.validering.ValidKodeverk;
import no.nav.foreldrepenger.los.statistikk.kø.InnslagType;

/**
 * DTO for migrating StatistikkOppgaveFilter entities.
 * Maps to table stat_oppgave_filter.
 */
public record StatOppgaveFilterDataDto(
    @NotNull @Min(0) @Max(Long.MAX_VALUE) Long oppgaveFilterId,
    @NotNull @Min(0) @Max(Long.MAX_VALUE) Long tidsstempel,
    @NotNull LocalDate statistikkDato,
    @NotNull @Min(0) @Max(Integer.MAX_VALUE) Integer antallAktive,
    @NotNull @Min(0) @Max(Integer.MAX_VALUE) Integer antallTilgjengelige,
    @Min(0) @Max(Integer.MAX_VALUE) Integer antallVentende,
    @Min(0) @Max(Integer.MAX_VALUE) Integer antallOpprettet,
    @Min(0) @Max(Integer.MAX_VALUE) Integer antallAvsluttet,
    @NotNull @Valid InnslagType innslagType
) {}
