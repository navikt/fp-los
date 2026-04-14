package no.nav.foreldrepenger.los.hendelse.behandlinghendelse;

import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class MottattHendelseRepository {

    private EntityManager entityManager;

    MottattHendelseRepository() {
        // CDI
    }

    @Inject
    public MottattHendelseRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public boolean hendelseErNy(String hendelseUid) {
        return entityManager.find(MottattHendelse.class, hendelseUid) == null;
    }

    public void registrerMottattHendelse(String hendelseUid) {
        var hendelse = new MottattHendelse(hendelseUid);
        entityManager.persist(hendelse);
        entityManager.flush();
    }

    public void slettMånedsGamle() {
        entityManager.createQuery("DELETE FROM MottattHendelse WHERE mottattTidspunkt < :foer")
            .setParameter("foer", LocalDateTime.now().minusWeeks(4))
            .executeUpdate();
        entityManager.flush();
    }

}
