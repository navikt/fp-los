package no.nav.foreldrepenger.los.struktur;

import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.vedtak.felles.testutilities.db.AbstractPostgresDbStrukturTest;

/**
 * Tester at alle migreringer følger standarder for navn og god praksis.
 */
@ExtendWith(JpaExtension.class)
class SjekkDbStrukturTest extends AbstractPostgresDbStrukturTest {}
