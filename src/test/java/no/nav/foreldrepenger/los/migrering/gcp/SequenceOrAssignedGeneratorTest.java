package no.nav.foreldrepenger.los.migrering.gcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;

@ExtendWith(JpaExtension.class)
class SequenceOrAssignedGeneratorTest {

    private static final AtomicInteger AVDELING_COUNTER = new AtomicInteger(9000);

    private EntityManager entityManager;

    @BeforeEach
    void setup(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Test
    void persistWithAssignedId_preservesAssignedId() {
        var avdeling = lagAvdeling();
        var expectedId = 1_123_456L;

        var gruppe = new SaksbehandlerGruppe("Manuell ID", avdeling);
        gruppe.setId(expectedId);

        entityManager.persist(gruppe);
        entityManager.flush();
        entityManager.clear();

        var persisted = entityManager.find(SaksbehandlerGruppe.class, expectedId);
        assertThat(persisted).isNotNull();
        assertThat(persisted.getId()).isEqualTo(expectedId);
    }

    @Test
    void persistWithoutAssignedId_usesHibernateSequence() {
        var avdeling = lagAvdeling();
        var explicitId = 3_234_567L;

        var medEksplisittId = new SaksbehandlerGruppe("Eksplisitt ID", avdeling);
        medEksplisittId.setId(explicitId);
        entityManager.persist(medEksplisittId);

        var medGenerertId = new SaksbehandlerGruppe("Generert ID", avdeling);
        entityManager.persist(medGenerertId);

        entityManager.flush();
        var generatedId = medGenerertId.getId();
        entityManager.clear();

        assertThat(generatedId).isNotNull();
        assertThat(generatedId).isNotEqualTo(explicitId);
        assertThat(generatedId).isGreaterThanOrEqualTo(4_999_951); // pga allokering av hibernate blokker kan reell være litt under 5m

        assertThat(entityManager.find(SaksbehandlerGruppe.class, explicitId)).isNotNull();
        assertThat(entityManager.find(SaksbehandlerGruppe.class, generatedId)).isNotNull();
    }

    private Avdeling lagAvdeling() {
        var avdelingEnhet = String.valueOf(AVDELING_COUNTER.incrementAndGet());
        var avdeling = new Avdeling(avdelingEnhet, "Testavdeling " + avdelingEnhet, false);
        entityManager.persist(avdeling);
        return avdeling;
    }
}

