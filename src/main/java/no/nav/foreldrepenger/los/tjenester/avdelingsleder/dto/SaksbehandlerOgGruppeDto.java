package no.nav.foreldrepenger.los.tjenester.avdelingsleder.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.tjenester.avdelingsleder.saksliste.FplosAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public record SaksbehandlerOgGruppeDto(@NotNull @Size(max = 100) @Pattern(regexp = InputValideringRegex.FRITEKST) String brukerIdent,
                                       @NotNull @Pattern(regexp = InputValideringRegex.FRITEKST) String avdelingEnhet,
                                       @Min(1) @Max(Integer.MAX_VALUE) long gruppeId) implements AbacDto {

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(FplosAbacAttributtType.OPPGAVESTYRING_ENHET, avdelingEnhet());
    }
}
