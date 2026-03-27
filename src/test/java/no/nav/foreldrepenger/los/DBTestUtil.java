package no.nav.foreldrepenger.los;

import static no.nav.foreldrepenger.los.organisasjon.Avdeling.AVDELING_DRAMMEN_ENHET;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;

public final class DBTestUtil {

    public static <T extends BaseEntitet> List<T> hentAlle(EntityManager entityManager, Class<T> klasse) {
        var criteria = entityManager.getCriteriaBuilder().createQuery(klasse);
        criteria.select(criteria.from(klasse));
        return entityManager.createQuery(criteria)
            .getResultStream()
            .sorted(Comparator.comparing(BaseEntitet::getOpprettetTidspunkt))
            .collect(Collectors.toList());
    }

    public static <T extends BaseEntitet> T hentUnik(EntityManager entityManager, Class<T> klasse) {
        var entiteter = hentAlle(entityManager, klasse);
        assertThat(entiteter).hasSize(1);
        return entiteter.get(0);
    }

    public static Avdeling avdelingDrammen(EntityManager entityManager) {
        return hentAlle(entityManager, Avdeling.class)
            .stream()
            .filter(a -> a.getAvdelingEnhet().equals(AVDELING_DRAMMEN_ENHET))
            .findAny()
            .orElseGet(() -> lagDrammen(entityManager));
    }

    public static Avdeling lagDrammen(EntityManager entityManager) {
        var avdeling = new Avdeling("4806", "NAV Familie- og pensjonsytelser Drammen", false);
        return lagAvdeling(entityManager, avdeling);
    }

    public static Avdeling lagNasjonal(EntityManager entityManager) {
        var avdeling = new Avdeling("4867", "NAV Familie- og pensjonsytelser foreldrepenger", false);
        return lagAvdeling(entityManager, avdeling);
    }

    public static Avdeling lagAvdeling(EntityManager entityManager, Avdeling avdeling) {
        entityManager.persist(avdeling);
        entityManager.flush();
        return avdeling;
    }


}
