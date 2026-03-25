package no.nav.foreldrepenger.los.migrering.dto;
import jakarta.validation.Valid;

import java.util.List;
/**
 * DTO for migrating OppgaveKø (queue) data with collections embedded
 */
public record KøOppsettDto(
    List<@Valid OppgaveFiltreringDataDto> oppgaveFiltrering,
    List<@Valid FiltreringSaksbehandlerDataDto> saksbehandlerKøer
) {
}
