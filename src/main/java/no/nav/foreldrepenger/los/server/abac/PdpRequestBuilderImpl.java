package no.nav.foreldrepenger.los.server.abac;

import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.los.oppgave.OppgaveTjeneste;
import no.nav.foreldrepenger.los.tjenester.avdelingsleder.saksliste.FplosAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;

@Dependent
@Alternative
@Priority(2)
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    private final OppgaveTjeneste oppgaveTjeneste;

    @Inject
    public PdpRequestBuilderImpl(OppgaveTjeneste oppgaveTjeneste) {
        this.oppgaveTjeneste = oppgaveTjeneste;
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {

        Set<Long> oppgaveIdList = dataAttributter.getVerdier(FplosAbacAttributtType.OPPGAVE_ID);
        var oppgave = oppgaveIdList.stream().findFirst().map(oppgaveTjeneste::hentOppgave);

        var builder = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES);
        oppgave.ifPresent(o -> builder
            .medSaksnummer(o.getSaksnummer().getVerdi())
            .medLoggSaksnummer(o.getSaksnummer().getVerdi())
            .medLoggBehandling(o.getBehandling().getId()));
        return builder.build();
    }
}
