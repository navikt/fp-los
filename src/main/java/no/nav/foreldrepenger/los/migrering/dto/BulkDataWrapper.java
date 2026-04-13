package no.nav.foreldrepenger.los.migrering.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for bulk migration of entity data between clusters.
 * Contains all entities needed for complete data transfer.
 */
@Valid
public record BulkDataWrapper(
    @NotNull List<@Valid BehandlingDataDto> behandlinger,
    @NotNull List<@Valid OppgaveDataDto> aktiveOppgaver,
    @NotNull List<@Valid OppgaveDataDto> inaktiveOppgaver,
    @Valid OrgDataDto organisasjonData,
    @Valid KøOppsettDto køOppsettDto,
    @NotNull List<@Valid StatEnhetYtelseBehandlingDataDto> statistikkEnhetYtelseBehandling,
    @NotNull List<@Valid StatOppgaveFilterDataDto> statistikkOppgaveFilter
) {
    public static BulkDataWrapper behandlinger(List<BehandlingDataDto> behandlinger) {
        return new BulkDataWrapper(behandlinger, List.of(), List.of(), null, new KøOppsettDto(List.of(), List.of()), List.of(), List.of());
    }

    public static BulkDataWrapper leggTilBehandlinger(BulkDataWrapper existing, List<BehandlingDataDto> behandlinger) {
        return new BulkDataWrapper(behandlinger, existing.aktiveOppgaver(), existing.inaktiveOppgaver(), existing.organisasjonData(), existing.køOppsettDto(), existing.statistikkEnhetYtelseBehandling(), existing.statistikkOppgaveFilter());
    }

    public static BulkDataWrapper aktiveOppgaver(List<OppgaveDataDto> oppgaver) {
        return new BulkDataWrapper(List.of(), oppgaver, List.of(), null, new KøOppsettDto(List.of(), List.of()), List.of(), List.of());
    }

    public static BulkDataWrapper inaktiveOppgaver(List<OppgaveDataDto> inaktiveOppgaver) {
        return new BulkDataWrapper(List.of(), List.of(), inaktiveOppgaver, null, new KøOppsettDto(List.of(), List.of()), List.of(), List.of());
    }

    public static BulkDataWrapper organisasjonOgKøOppset(OrgDataDto orgData, KøOppsettDto køOppsettDto) {
        return new BulkDataWrapper(List.of(), List.of(), List.of(), orgData, køOppsettDto, List.of(), List.of());
    }

    public static BulkDataWrapper statistikkOppgaveFilter(List<StatOppgaveFilterDataDto> oppgaveFilter) {
        return new BulkDataWrapper(List.of(), List.of(), List.of(), null, new KøOppsettDto(List.of(), List.of()), List.of(), oppgaveFilter);
    }

    public static BulkDataWrapper statistikkEnhetYtelseBehandling(List<StatEnhetYtelseBehandlingDataDto> enhetYtelseBehandling) {
        return new BulkDataWrapper(List.of(), List.of(), List.of(), null, new KøOppsettDto(List.of(), List.of()), enhetYtelseBehandling, List.of());
    }
}
