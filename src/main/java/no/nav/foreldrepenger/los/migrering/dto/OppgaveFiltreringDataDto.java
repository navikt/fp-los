package no.nav.foreldrepenger.los.migrering.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.felles.util.validering.ValidKodeverk;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.oppgave.Periodefilter;
import no.nav.foreldrepenger.los.oppgavekø.KøSortering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * DTO for migrating OppgaveFiltrering entities with embedded collections
 */
public record OppgaveFiltreringDataDto(
    @NotNull @Min(0) @Max(10_000_000) Long id,
    @NotNull @Size(max = 100) @Pattern(regexp = InputValideringRegex.FRITEKST) String navn,
    @Size(max = 1024) @Pattern(regexp = InputValideringRegex.FRITEKST) String beskrivelse,
    @NotNull @ValidKodeverk KøSortering køSortering,
    @NotNull @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}") String avdelingId,
    LocalDate fomDato,
    LocalDate tomDato,
    @Min(Long.MIN_VALUE) @Max(Long.MAX_VALUE) Long fomDager,
    @Min(Long.MIN_VALUE) @Max(Long.MAX_VALUE) Long tomDager,
    @NotNull @Valid Periodefilter periodeFilter,
    @NotNull @Size(max = 20) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    @NotNull  LocalDateTime opprettetTidspunkt,
    @Size(max = 20) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt,
    List<@ValidKodeverk BehandlingType> behandlingTyper,
    List<@ValidKodeverk FagsakYtelseType> fagsakYtelseTyper,
    List<@Valid AndreKriterierDataDto> andreKriterier,
    Set<@NotNull @Pattern(regexp = Saksbehandler.VALID_SAKSBEHANDLER_IDENT, message = "Ugyldig ident ${validatedValue}") String> saksbehandlerIdenter) {}
