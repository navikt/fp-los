package no.nav.foreldrepenger.los.tjenester.saksbehandler.oppgave.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.los.tjenester.avdelingsleder.saksliste.FplosAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class OppgaveFlyttingDto implements AbacDto {

    @NotNull
    @Digits(integer = 18, fraction = 0)
    private Long oppgaveId;

    @NotNull
    @Size(max = 100)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String brukerIdent;

    @NotNull
    @Size(max = 500)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    public OppgaveFlyttingDto() {
    }

    public OppgaveFlyttingDto(Long oppgaveId, String brukerIdent, String begrunnelse) {
        this.oppgaveId = oppgaveId;
        this.brukerIdent = brukerIdent;
        this.begrunnelse = begrunnelse;
    }

    public Long getOppgaveId() {
        return oppgaveId;
    }

    public String getBrukerIdent() {
        return brukerIdent;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    @Override
    public String toString() {
        return "OppgaveFlyttingDto{" + "oppgaveId=" + oppgaveId + ", brukerIdent=" + brukerIdent + ", begrunnelse='" + "*****" + '\'' + '}';
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(FplosAbacAttributtType.OPPGAVE_ID, oppgaveId);
    }
}
