package no.nav.foreldrepenger.los.persontjeneste;

import no.nav.vedtak.felles.integrasjon.person.AbstractPersonKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "pdl.base.url",
    endpointDefault = "pdl.base.url=https://pdl-api.prod-fss-pub.nais.io/graphql",
    scopesProperty = "pdl.scopes", scopesDefault = "api://prod-fss.pdl.pdl-api/.default")
public class PdlKlient extends AbstractPersonKlient {

    public PdlKlient() {
        super();
    }
}
