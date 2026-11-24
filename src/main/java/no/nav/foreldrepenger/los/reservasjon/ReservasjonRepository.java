package no.nav.foreldrepenger.los.reservasjon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.oppgave.Oppgave;

@ApplicationScoped
public class ReservasjonRepository {
    private EntityManager entityManager;

    public ReservasjonRepository() {
    }

    @Inject
    public ReservasjonRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<SisteReserverteMetadata> hentSisteReserverteMetadata(String uid, boolean kunAktive) {
        /*
        * sb_res: behandlinger saksbehandler har reservert, med aktuell_tid = siste gang saksbehandler tok i behandlingen (brukes i sortering).
        *   Svakhet: 1-1 relasjon mellom oppgave og reservasjon. Dersom saksbehandler ikke har skapt aktivitet (i.e løst AP) som gir ferske
        *   oppgaver vil ev. nye reservasjoner overskrive.
        * siste_oppgave (lateral): siste oppgave på behandlingen, uavhengig av "eier". Avgjør om behandlingen regnes som åpen (aktiv oppgave).
        * per_sak: kun siste rørte behandling per saksnummer, etter filtrering på aktiv.
        */
        var kunAktiveValue = kunAktive ? List.of(Boolean.TRUE) : List.of(Boolean.TRUE, Boolean.FALSE);
        var query = entityManager.createNativeQuery("""
            with sb_res as (
                select o.behandling_id, max(coalesce(r.endret_tid, r.opprettet_tid)) as aktuell_tid
                from reservasjon r
                inner join oppgave o on o.id = r.oppgave_id
                where r.reservert_av = :uid
                and r.opprettet_tid > :fom
                group by o.behandling_id
            ),
            per_sak as (
                select siste_oppgave.id as oppgave_id, sb_res.aktuell_tid,
                row_number() over (partition by b.saksnummer order by sb_res.aktuell_tid desc) as rn
                from sb_res
                inner join behandling b on b.id = sb_res.behandling_id
                cross join lateral (
                    select o.id, o.aktiv
                    from oppgave o
                    where o.behandling_id = sb_res.behandling_id
                    order by o.opprettet_tid desc, o.id desc
                    limit 1
                ) siste_oppgave
                where siste_oppgave.aktiv in ( :kunAktiveValue )
            )
            select oppgave_id, aktuell_tid
            from per_sak
            where rn = 1
            order by aktuell_tid desc
            """)
            .setParameter("uid", uid)
            .setParameter("fom", LocalDate.now().minusWeeks(3).atStartOfDay())
            .setParameter("kunAktiveValue", kunAktiveValue)
            .setMaxResults(15);

        @SuppressWarnings("unchecked")
        Stream<Object[]> result = query.getResultStream();

        return result.map(row -> {
            var oppgaveId = ((Number) row[0]).longValue();
            var aktuellTid = ((LocalDateTime) row[1]);
            return new SisteReserverteMetadata(oppgaveId, aktuellTid);
        }).toList();
    }

    public List<Oppgave> hentSaksbehandlersReserverteAktiveOppgaver(String uid) {
        return entityManager.createQuery("""
            select o from Oppgave o
            join fetch o.behandling b
            where o.aktiv = true
            and exists (
                select 1 from Reservasjon r
                where r.oppgave = o
                and r.reservertTil > :nå
                and r.reservertAv = :uid
            )
            """, Oppgave.class).setParameter("nå", LocalDateTime.now()).setParameter("uid", uid.toUpperCase()).getResultList();
    }

    public List<Reservasjon> hentAlleReservasjonerForAvdeling(String avdelingEnhet) {
        return entityManager.createQuery("""
            select r from Reservasjon r
            inner join Oppgave o on r.oppgave = o
            where r.reservertTil > :nå and o.behandlendeEnhet = :behandlendeEnhet
            order by r.reservertAv
            """, Reservasjon.class).setParameter("nå", LocalDateTime.now()).setParameter("behandlendeEnhet", avdelingEnhet).getResultList();
    }

    public Optional<Reservasjon> hentAktivReservasjon(Long oppgaveId) {
        // første forsøk på å forberede en-til-mange relasjon mellom oppgave og reservasjon.
        // "not exists"-clause unødvendig da implisitt kun én reservertTil > :now ?
        return entityManager.createQuery("""
            select r
            from Reservasjon r
            where r.oppgave.id = :id
            and r.reservertTil > :now
            and not exists (
                select 1
                from Reservasjon r2
                where r2.oppgave.id = r.oppgave.id
                and r2.opprettetTidspunkt > r.opprettetTidspunkt
            )
            """, Reservasjon.class).setParameter("id", oppgaveId).setParameter("now", LocalDateTime.now()).getResultStream().findFirst();
    }

    public <U extends BaseEntitet> void lagre(U entity) {
        entityManager.persist(entity);
        entityManager.flush();
    }

    public <U extends BaseEntitet> void refresh(U entity) {
        entityManager.refresh(entity);
    }
}
