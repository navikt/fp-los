package no.nav.foreldrepenger.los.tjenester.avdelingsleder.saksbehandler.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.tjenester.avdelingsleder.saksliste.FplosAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public record SaksbehandlerGruppeSletteRequestDto(@Min(1) @Max(Integer.MAX_VALUE) long gruppeId,
                                                  @Pattern(regexp = InputValideringRegex.FRITEKST) String avdelingEnhet) implements AbacDto {

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(FplosAbacAttributtType.OPPGAVESTYRING_ENHET, avdelingEnhet());
    }
}
