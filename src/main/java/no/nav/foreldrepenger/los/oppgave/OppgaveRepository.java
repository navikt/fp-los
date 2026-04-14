package no.nav.foreldrepenger.los.oppgave;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.jpa.HibernateHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.domene.typer.BehandlingId;
import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;

@ApplicationScoped
public class OppgaveRepository {

    private static final Logger LOG = LoggerFactory.getLogger(OppgaveRepository.class);

    public static final String BEHANDLING_ID_FELT_SQL = "behandlingId";

    private EntityManager entityManager;

    @Inject
    public OppgaveRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    OppgaveRepository() {
    }

    public List<Oppgave> hentAktiveOppgaverForSaksnummer(Collection<Saksnummer> saksnummerListe) {
        return entityManager.createQuery(
            "from Oppgave o where o.behandling.saksnummer in :saksnummerListe and o.aktiv order by o.behandling.saksnummer desc",
            Oppgave.class).setParameter("saksnummerListe", saksnummerListe).getResultList();
    }

    public Optional<Reservasjon> hentReservasjon(Long oppgaveId) {
        return Optional.ofNullable(entityManager.find(Reservasjon.class, oppgaveId));
    }

    public List<OppgaveFiltrering> hentAlleOppgaveFiltreReadOnly() {
        var listeTypedQuery = entityManager.createQuery("from OppgaveFiltrering", OppgaveFiltrering.class);
        listeTypedQuery.setHint(HibernateHints.HINT_READ_ONLY, "true");
        return listeTypedQuery.getResultList();
    }

    public List<OppgaveFiltrering> hentAlleOppgaveFilterSettTilknyttetEnhet(String avdelingEnhet) {
        var listeTypedQuery = entityManager.createQuery("from OppgaveFiltrering l where l.avdeling.avdelingEnhet = :avdelingEnhet order by l.navn",
            OppgaveFiltrering.class).setParameter("avdelingEnhet", avdelingEnhet);//$NON-NLS-1$
        return listeTypedQuery.getResultList();
    }

    public Optional<OppgaveFiltrering> hentOppgaveFilterSett(Long listeId) {
        return Optional.ofNullable(entityManager.find(OppgaveFiltrering.class, listeId));
    }

    public Long lagreFiltrering(OppgaveFiltrering oppgaveFiltrering) {
        lagre(oppgaveFiltrering);
        return oppgaveFiltrering.getId();
    }

    public void slettListe(OppgaveFiltrering oppgaveFiltrering) {
        entityManager.remove(oppgaveFiltrering);
    }

    public boolean sjekkOmOppgaverFortsattErTilgjengelige(List<Long> oppgaveIder) {
        var fortsattTilgjengelige = entityManager.createQuery("""
            select count(o.id) from Oppgave o
            where not exists (
                select 1
                from Reservasjon r
                where r.oppgave = o
                and r.reservertTil > :nå
            )
            and o.id in ( :oppgaveId )
            and o.aktiv
            """, Long.class).setParameter("nå", LocalDateTime.now()).setParameter("oppgaveId", oppgaveIder).getSingleResult();
        return oppgaveIder.size() == fortsattTilgjengelige.intValue();
    }

    public Oppgave hentOppgave(Long oppgaveId) {
        return entityManager.createQuery("FROM Oppgave o where o.id = :id", Oppgave.class).setParameter("id", oppgaveId).getSingleResult();
    }

    public List<Oppgave> hentOppgaverReadOnly(List<Long> oppgaveIder) {
        if (oppgaveIder == null || oppgaveIder.isEmpty()) {
            return List.of();
        }
        return entityManager.createQuery("FROM Oppgave o where o.id IN (:oppgaveIder)", Oppgave.class)
            .setParameter("oppgaveIder", oppgaveIder)
            .setHint(HibernateHints.HINT_READ_ONLY, "true")
            .getResultList();
    }

    public List<Oppgave> hentOppgaver(BehandlingId behandlingId) {
        return entityManager.createQuery("FROM Oppgave o where o.behandling.id = :behandlingId", Oppgave.class)
            .setParameter(BEHANDLING_ID_FELT_SQL, behandlingId.toUUID())
            .getResultList();
    }

    public Optional<Oppgave> hentAktivOppgave(BehandlingId behandlingId) {
        var oppgaver = entityManager.createQuery("""
            FROM Oppgave o
            where o.behandling.id = :behandlingId
            and o.aktiv
            """, Oppgave.class).setParameter(BEHANDLING_ID_FELT_SQL, behandlingId.toUUID()).getResultList();
        if (oppgaver.size() > 1) {
            LOG.warn("Flere enn én aktive oppgaver for behandlingId {}", behandlingId);
        }
        return oppgaver.stream().max(Comparator.comparing(Oppgave::getOpprettetTidspunkt));
    }

    public Oppgave opprettOppgave(Oppgave oppgave) {
        entityManager.persist(oppgave);
        return oppgave;
    }

    public <U extends BaseEntitet> void lagre(U entitet) {
        entityManager.persist(entitet);
        entityManager.flush();
    }

    public <U extends BaseEntitet> void flette(U entitet) {
        entityManager.merge(entitet);
        entityManager.flush();
    }


    public <U extends BaseEntitet> void refresh(U entitet) {
        entityManager.refresh(entitet);
    }

    public Optional<Behandling> finnBehandling(UUID behandlingId) {
        return Optional.ofNullable(entityManager.find(Behandling.class, behandlingId));
    }

    public Behandling hentBehandling(UUID behandlingId) {
        return finnBehandling(behandlingId).orElseThrow();
    }

    public void lagreBehandling(Behandling.Builder behandlingBuilder) {
        if (behandlingBuilder.erOppdatering()) {
            entityManager.merge(behandlingBuilder.build());
        } else {
            entityManager.persist(behandlingBuilder.build());
        }
    }

    public void tilknyttSaksbehandlerOppgaveFiltrering(Saksbehandler saksbehandler, OppgaveFiltrering oppgaveFiltrering) {
        var nøkkel = new FiltreringSaksbehandlerRelasjon.FiltreringSaksbehandlerNøkkel(saksbehandler, oppgaveFiltrering);
        if (entityManager.find(FiltreringSaksbehandlerRelasjon.class, nøkkel) == null) {
            var knytning = new FiltreringSaksbehandlerRelasjon(saksbehandler, oppgaveFiltrering);
            entityManager.persist(knytning);
        }
    }

    public void fraknyttSaksbehandlerOppgaveFiltrering(Saksbehandler saksbehandler, OppgaveFiltrering oppgaveFiltrering) {
        entityManager.createQuery(
                "DELETE from FiltreringSaksbehandlerRelasjon where saksbehandler = :saksbehandler and oppgaveFiltrering = :oppgaveFiltrering")
            .setParameter("saksbehandler", saksbehandler)
            .setParameter("oppgaveFiltrering", oppgaveFiltrering)
            .executeUpdate();
    }

    public void fraknyttAlleSaksbehandlereFraOppgaveFiltrering(OppgaveFiltrering oppgaveFiltrering) {
        entityManager.createQuery("DELETE from FiltreringSaksbehandlerRelasjon where oppgaveFiltrering = :oppgaveFiltrering")
            .setParameter("oppgaveFiltrering", oppgaveFiltrering)
            .executeUpdate();
    }

    public List<OppgaveFiltrering> oppgaveFiltreringerForSaksbehandler(Saksbehandler saksbehandler) {
        return entityManager.createQuery("select oppgaveFiltrering from FiltreringSaksbehandlerRelasjon where saksbehandler = :saksbehandler",
            OppgaveFiltrering.class).setParameter("saksbehandler", saksbehandler).getResultList();
    }

    public List<Saksbehandler> saksbehandlereForOppgaveFiltrering(OppgaveFiltrering oppgaveFiltrering) {
        return entityManager.createQuery("select saksbehandler from FiltreringSaksbehandlerRelasjon where oppgaveFiltrering = :oppgaveFiltrering",
            Saksbehandler.class).setParameter("oppgaveFiltrering", oppgaveFiltrering).getResultList();
    }

}
