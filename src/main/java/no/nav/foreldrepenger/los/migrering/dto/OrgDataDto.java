package no.nav.foreldrepenger.los.migrering.dto;

import java.util.List;

import jakarta.validation.Valid;

/**
 * DTO for migrating organizational data (Avdeling, Saksbehandler, etc.) with preserved PKs
 */
public record OrgDataDto(
    List<@Valid AvdelingDataDto> avdelinger,
    List<@Valid SaksbehandlerDataDto> saksbehandlere,
    List<@Valid AvdelingSaksbehandlerDataDto> avdelingSaksbehandlere,
    List<@Valid SaksbehandlerGruppeDataDto> saksbehandlerGrupper,
    List<@Valid GruppeTilknytningDataDto> gruppeTilknytninger
) {
}
