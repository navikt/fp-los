package no.nav.foreldrepenger.los.felles.util;

import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.vedtak.sikkerhet.kontekst.Kontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

public class BrukerIdent {

    private static final Environment ENV = Environment.current();
    private static final String TEST_IDENT = "Z999999";

    private BrukerIdent() {
    }

    public static String brukerIdent() {
        return Optional.ofNullable(KontekstHolder.getKontekst())
            .map(Kontekst::getUid)
            .map(String::toUpperCase)
            .or(() -> ENV.isLocal() ? Optional.of(TEST_IDENT) : Optional.empty())
            .orElse(null);
    }

    public static String brukerIdentEllerDefault() {
        return Optional.ofNullable(brukerIdent())
            .orElse(BaseEntitet.BRUKERNAVN_NÅR_SIKKERHETSKONTEKST_IKKE_FINNES);
    }

}
