package no.nav.foreldrepenger.los.migrering.dto;

import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;

/**
 * DTO for migrating BehandlingEgenskap entities (uses composite key)
 */
public record BehandlingEgenskapDataDto(
    AndreKriterierType andreKriterierType
) {
}

