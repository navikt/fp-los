package no.nav.foreldrepenger.los.tjenester.felles.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class SakslisteIdDto implements AbacDto {

    @JsonProperty("sakslisteId")
    @NotNull
    @Digits(integer = 18, fraction = 0)
    private final Long sakslisteId;

    public SakslisteIdDto() {
        sakslisteId = null; // NOSONAR
    }

    public SakslisteIdDto(Long sakslisteId) {
        Objects.requireNonNull(sakslisteId, "sakslisteId");
        this.sakslisteId = sakslisteId;
    }

    public SakslisteIdDto(String sakslisteId) {
        this.sakslisteId = Long.valueOf(sakslisteId);
    }

    @JsonIgnore
    public Long getVerdi() {
        return sakslisteId;
    }

    @Override
    public String toString() {
        return "SakslisteIdDto{" + "sakslisteId='" + sakslisteId + '\'' + '}';
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof SakslisteIdDto that && sakslisteId.equals(that.sakslisteId);
    }

    @Override
    public int hashCode() {
        return sakslisteId.hashCode();
    }
}
