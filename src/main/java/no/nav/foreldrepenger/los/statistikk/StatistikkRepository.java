package no.nav.foreldrepenger.los.statistikk;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.statistikk.kø.InnslagType;
import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilter;

@ApplicationScoped
public class StatistikkRepository {

    private EntityManager entityManager;

    @Inject
    public StatistikkRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    StatistikkRepository() {
        //CDI
    }

    public void lagreStatistikkEnhetYtelseBehandling(Collection<StatistikkEnhetYtelseBehandling> statistikk) {
        statistikk.forEach(innslag -> entityManager.persist(innslag));
        entityManager.flush();
    }

    public void lagreStatistikkOppgaveFilter(StatistikkOppgaveFilter statistikk) {
        entityManager.persist(statistikk);
        if (InnslagType.REGELMESSIG.equals(statistikk.getInnslagType())) {
            fjernSnapshotStatistikkOppgaveFilterTidligereEnn(statistikk.getOppgaveFilterId());
        }
    }

    public List<OppgaveEnhetYtelseBehandling> hentÅpneOppgaverPerEnhetYtelseBehandling() {
        return entityManager.createQuery("""
            Select new no.nav.foreldrepenger.los.statistikk.OppgaveEnhetYtelseBehandling(
                o.behandlendeEnhet, b.fagsakYtelseType, b.behandlingType, Count(o.id))
            FROM Oppgave o JOIN Behandling b ON o.behandling = b
            WHERE o.aktiv = true
            GROUP BY o.behandlendeEnhet, b.fagsakYtelseType, b.behandlingType
            """, OppgaveEnhetYtelseBehandling.class).getResultList();
    }

    public List<OppgaveEnhetYtelseBehandling> hentOpprettetOppgaverPerEnhetYtelseBehandling() {
        return entityManager.createQuery("""
            Select new no.nav.foreldrepenger.los.statistikk.OppgaveEnhetYtelseBehandling(
                o.behandlendeEnhet, b.fagsakYtelseType, b.behandlingType, Count(o.id))
            FROM Oppgave o JOIN Behandling b ON o.behandling = b
            WHERE o.opprettetTidspunkt > :opprettet
            GROUP BY o.behandlendeEnhet, b.fagsakYtelseType, b.behandlingType
            """, OppgaveEnhetYtelseBehandling.class)
            .setParameter("opprettet", LocalDateTime.now().minusHours(24))
            .getResultList();
    }

    public List<OppgaveEnhetYtelseBehandling> hentAvsluttetOppgaverPerEnhetYtelseBehandling() {
        return entityManager.createQuery("""
            Select new no.nav.foreldrepenger.los.statistikk.OppgaveEnhetYtelseBehandling(
                o.behandlendeEnhet, b.fagsakYtelseType, b.behandlingType, Count(o.id))
            FROM Oppgave o JOIN Behandling b ON o.behandling = b
            WHERE o.aktiv = false and o.oppgaveAvsluttet > :endret
            GROUP BY o.behandlendeEnhet, b.fagsakYtelseType, b.behandlingType
            """, OppgaveEnhetYtelseBehandling.class)
            .setParameter("endret", LocalDateTime.now().minusHours(24))
            .getResultList();
    }

    public List<StatistikkEnhetYtelseBehandling> hentStatistikkForEnhetFomDato(String enhet, LocalDate fom) {
        var startpunkt = fom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return entityManager.createQuery("""
            SELECT s FROM StatistikkEnhetYtelseBehandling s
            WHERE s.nøkkel.behandlendeEnhet = :enhet AND s.nøkkel.tidsstempel >= :tidsstempel
            ORDER BY s.nøkkel.tidsstempel, s.nøkkel.fagsakYtelseType, s.nøkkel.behandlingType
            """, StatistikkEnhetYtelseBehandling.class)
            .setParameter("enhet", enhet)
            .setParameter("tidsstempel", startpunkt)
            .getResultList();
    }

    public List<StatistikkEnhetYtelseBehandling> hentStatistikkFomDato(LocalDate fom) {
        var startpunkt = fom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return entityManager.createQuery("""
            SELECT s FROM StatistikkEnhetYtelseBehandling s
            WHERE s.nøkkel.tidsstempel >= :tidsstempel
            ORDER BY s.nøkkel.tidsstempel, s.nøkkel.fagsakYtelseType, s.nøkkel.behandlingType
            """, StatistikkEnhetYtelseBehandling.class)
            .setParameter("tidsstempel", startpunkt)
            .getResultList();
    }

    public Optional<StatistikkOppgaveFilter> hentSisteStatistikkOppgaveFilter(Long oppgaveFilterId, Set<InnslagType> inkluderTyper) {
        return entityManager.createQuery("""
            SELECT s FROM StatistikkOppgaveFilter s
            where s.nøkkel.oppgaveFilterId = :oppgaveFilterId AND s.innslagType IN :innslagstyper
            ORDER BY s.nøkkel.tidsstempel DESC
            """, StatistikkOppgaveFilter.class)
            .setParameter("oppgaveFilterId", oppgaveFilterId)
            .setParameter("innslagstyper", inkluderTyper)
            .setMaxResults(1)
            .getResultStream()
            .findFirst();
    }

    public List<StatistikkOppgaveFilter> hentStatistikkOppgaveFilterFraFom(Long oppgaveFilterId, LocalDate fom) {
        var startpunkt = fom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return entityManager.createQuery("""
            SELECT s FROM StatistikkOppgaveFilter s
            WHERE s.nøkkel.oppgaveFilterId = :oppgaveFilterId AND s.nøkkel.tidsstempel >= :tidsstempel and s.innslagType = :innslagtype
            ORDER BY s.nøkkel.tidsstempel
            """, StatistikkOppgaveFilter.class)
            .setParameter("tidsstempel", startpunkt)
            .setParameter("oppgaveFilterId", oppgaveFilterId)
            .setParameter("innslagtype", InnslagType.REGELMESSIG)
            .getResultList();
    }

    private void fjernSnapshotStatistikkOppgaveFilterTidligereEnn(Long oppgaveFilterId) {
        entityManager.createQuery("""
            DELETE FROM StatistikkOppgaveFilter s
            WHERE s.nøkkel.oppgaveFilterId = :oppgaveFilterId AND s.innslagType = :innslagtype
            """)
            .setParameter("oppgaveFilterId", oppgaveFilterId)
            .setParameter("innslagtype", InnslagType.SNAPSHOT)
            .executeUpdate();
    }

    public Map<Long, StatistikkOppgaveFilter> hentSisteStatistikkForAlleOppgaveFiltre() {
        var alleStatistikk = entityManager.createQuery("""
            SELECT s FROM StatistikkOppgaveFilter s
            WHERE s.nøkkel.tidsstempel = (
                SELECT MAX(s2.nøkkel.tidsstempel)
                FROM StatistikkOppgaveFilter s2
                WHERE s2.nøkkel.oppgaveFilterId = s.nøkkel.oppgaveFilterId
            )
            """, StatistikkOppgaveFilter.class)
            .getResultList();

        return alleStatistikk.stream()
            .collect(java.util.stream.Collectors.toMap(
                StatistikkOppgaveFilter::getOppgaveFilterId,
                s -> s
            ));
    }
}
