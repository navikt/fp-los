package no.nav.foreldrepenger.los.migrering.dto;

import jakarta.validation.Valid;
import no.nav.foreldrepenger.los.felles.util.validering.ValidKodeverk;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;

/**
 * Simple DTO for andre kriterier without primary key
 */
public record AndreKriterierDataDto(
    @ValidKodeverk AndreKriterierType andreKriterierType,
    boolean inkluder
) {}
