package no.nav.foreldrepenger.los.tjenester.avdelingsleder.saksliste.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.tjenester.avdelingsleder.saksliste.FplosAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public record SakslisteOgAvdelingDto(@NotNull @Digits(integer = 18, fraction = 0) Long sakslisteId,
                                     @NotNull @Pattern(regexp = InputValideringRegex.FRITEKST) String avdelingEnhet) implements AbacDto {

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(FplosAbacAttributtType.OPPGAVESTYRING_ENHET, avdelingEnhet());
    }
}
