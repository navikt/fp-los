package no.nav.foreldrepenger.los.migrering.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.felles.util.validering.ValidKodeverk;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.oppgave.Periodefilter;
import no.nav.foreldrepenger.los.oppgavekø.KøSortering;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * DTO for migrating OppgaveFiltrering entities with embedded collections
 */
public record OppgaveFiltreringDataDto(
    @Min(0) @Max(10_000_000) Long id,  // Primary key - must be preserved for potential references
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String navn,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String beskrivelse,
    @ValidKodeverk KøSortering køSortering,
    @Min(0) @Max(10_000_000) Long avdelingId,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String skjermet,
    LocalDate fomDato,
    LocalDate tomDato,
    @Valid Periodefilter periodeFilter,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    LocalDateTime opprettetTidspunkt,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt,
    // Embedded collections - no need to preserve PKs of collection items
    List<@ValidKodeverk BehandlingType> behandlingTyper,
    List<@ValidKodeverk FagsakYtelseType> fagsakYtelseTyper,
    List<@Valid AndreKriterierDataDto> andreKriterier
) {}
