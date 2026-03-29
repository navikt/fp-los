package no.nav.foreldrepenger.los.migrering.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.domene.typer.BehandlingId;
import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.aktør.AktørId;
import no.nav.foreldrepenger.los.felles.util.validering.ValidKodeverk;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.tjenester.saksbehandler.oppgave.dto.SaksnummerDto;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * DTO for migrating Oppgave entities with preserved PKs
 */
public record OppgaveDataDto(
    @Min(1) @Max(100_000_000) Long id,  // Primary key
    @NotNull @Valid SaksnummerDto saksnummer,
    @Valid AktørId aktørId,
    @Valid BehandlingId behandlingId,
    @ValidKodeverk BehandlingType behandlingType,
    @ValidKodeverk FagsakYtelseType fagsakYtelseType,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String behandlendeEnhet,
    LocalDate behandlingsfrist,
    LocalDateTime behandlingOpprettet,
    LocalDate førsteStønadsdag,
    boolean aktiv,
    @Valid Fagsystem system,
    LocalDateTime oppgaveAvsluttet,
    @Min(0) @Max(1_000_000_000) BigDecimal feilutbetalingBelop,
    LocalDate feilutbetalingStart,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String opprettetAv,
    LocalDateTime opprettetTidspunkt,
    @Size(max = 500) @Pattern(regexp = InputValideringRegex.FRITEKST) String endretAv,
    LocalDateTime endretTidspunkt,
    @Valid ReservasjonDataDto reservasjonDataDto,
    List<@Valid OppgaveEgenskapDataDto> oppgaveEgenskaper
) {
}
