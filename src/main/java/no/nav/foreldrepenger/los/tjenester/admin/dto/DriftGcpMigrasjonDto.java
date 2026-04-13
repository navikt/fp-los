package no.nav.foreldrepenger.los.tjenester.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.los.migrering.fss.FssGcpMigrasjonTask;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public record DriftGcpMigrasjonDto(@JsonProperty("steg")
                                   @Valid
                                   FssGcpMigrasjonTask.MigreringSteg steg) implements AbacDto {
    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}

