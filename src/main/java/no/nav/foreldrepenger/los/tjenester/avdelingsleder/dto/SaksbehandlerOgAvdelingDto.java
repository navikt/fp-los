package no.nav.foreldrepenger.los.tjenester.avdelingsleder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.tjenester.avdelingsleder.saksliste.FplosAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class SaksbehandlerOgAvdelingDto implements AbacDto {

    @NotNull
    @Size(max = 100)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String brukerIdent;

    @NotNull
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String avdelingEnhet;

    public SaksbehandlerOgAvdelingDto() {
    }

    public SaksbehandlerOgAvdelingDto(String brukerIdent, String avdelingEnhet) {
        this.brukerIdent = brukerIdent;
        this.avdelingEnhet = avdelingEnhet;
    }

    public String getBrukerIdent() {
        return brukerIdent;
    }

    public String getAvdelingEnhet() {
        return avdelingEnhet;
    }

    @Override
    public String toString() {
        return "SaksbehandlerOgAvdelingDto{" + "brukerIdent=" + brukerIdent + ", avdelingEnhet=" + avdelingEnhet + '}';
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(FplosAbacAttributtType.OPPGAVESTYRING_ENHET, avdelingEnhet);

    }
}
