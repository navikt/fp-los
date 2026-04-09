package no.nav.foreldrepenger.los.migrering.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.aktør.AktørId;
import no.nav.foreldrepenger.los.felles.util.validering.ValidKodeverk;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.BehandlingTilstand;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.tjenester.saksbehandler.oppgave.dto.SaksnummerDto;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * DTO for migrating Behandling entities with preserved PKs and relationships
 */
@Valid
public record BehandlingDataDto(
    UUID id,  // Primary key
    @NotNull @Valid SaksnummerDto saksnummer,
    @Valid AktørId aktørId,
    @NotNull @Valid Fagsystem kildeSystem,
    @NotNull @ValidKodeverk FagsakYtelseType fagsakYtelseType,
    @NotNull @ValidKodeverk BehandlingType behandlingType,
    @NotNull @ValidKodeverk BehandlingTilstand behandlingTilstand,
    @Size(max = 200) @Pattern(regexp = InputValideringRegex.FRITEKST) String aktiveAksjonspunkt,
    LocalDateTime ventefrist,
    @NotNull LocalDateTime opprettet,
    LocalDateTime avsluttet,
    LocalDate behandlingsfrist,
    LocalDate førsteStønadsdag,
    @Min(0) @Max(1_000_000_000) BigDecimal feilutbetalingBelop,
    LocalDate feilutbetalingStart,
    @NotNull @Size(max = 10) @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}") String behandlendeEnhet,
    @Size(max = 20) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    LocalDateTime opprettetTidspunkt,
    @Size(max = 20) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt,
    @NotNull Set<@ValidKodeverk AndreKriterierType> egenskaper
) {
}

