package no.nav.foreldrepenger.los.migrering;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import no.nav.foreldrepenger.los.migrering.dto.StatOppgaveFilterDataDto;

import no.nav.foreldrepenger.los.migrering.fss.FssExportMapper;
import no.nav.foreldrepenger.los.statistikk.kø.InnslagType;
import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilter;

import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilterNøkkel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.DBTestUtil;
import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;
import no.nav.foreldrepenger.los.migrering.gcp.GcpImportRepository;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingEgenskap;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;

@ExtendWith(JpaExtension.class)
class GcpImportRepositoryTest {

    private GcpImportRepository repo;
    private EntityManager em;

    @BeforeEach
    void setup(EntityManager entityManager) {
        this.em = entityManager;
        this.repo = new GcpImportRepository(entityManager);
    }

    @Test
    void lagre_organisasjonOgKøer_shouldPersistAllEntities() {
        var bulkData = TestMigreringData.lagOrganisasjonOgKøer();
        var kvittering = repo.lagre(bulkData);

        assertThat(kvittering.kjørtUtenFeil()).isTrue();
        assertThat(kvittering.orgData()).isPositive();
        assertThat(kvittering.oppgaveKøer()).isPositive();

        em.clear();
        assertThat(DBTestUtil.hentAlle(em, Avdeling.class)).isNotEmpty();
        assertThat(DBTestUtil.hentAlle(em, Saksbehandler.class)).isNotEmpty();
        assertThat(DBTestUtil.hentAlle(em, SaksbehandlerGruppe.class)).isNotEmpty();
        assertThat(DBTestUtil.hentAlle(em, OppgaveFiltrering.class)).isNotEmpty();
    }

    @Test
    void lagre_organisasjon_shouldBeIdempotent() {
        var bulkData = TestMigreringData.lagOrganisasjonOgKøer();
        repo.lagre(bulkData); // First run
        em.clear();

        var kvittering = repo.lagre(bulkData); // Second run — should merge, not duplicate

        assertThat(kvittering.kjørtUtenFeil()).isTrue();

        // Verify no duplicates
        em.clear();
        var avdelinger = DBTestUtil.hentAlle(em, Avdeling.class).stream()
            .filter(a -> "4806".equals(a.getAvdelingEnhet()))
            .toList();
        assertThat(avdelinger).hasSize(1);
    }

    @Test
    void lagre_behandlinger_shouldPersistWithEgenskaper() {
        var bulkData = TestMigreringData.lagBehandlinger();
        var kvittering = repo.lagre(bulkData);

        assertThat(kvittering.kjørtUtenFeil()).isTrue();
        assertThat(kvittering.behandlinger()).isEqualTo(2);

        em.clear();
        assertThat(DBTestUtil.hentAlle(em, Behandling.class)).hasSize(2);

        var egenskaper = em.createQuery("FROM BehandlingEgenskap", BehandlingEgenskap.class).getResultList();
        assertThat(egenskaper).hasSize(2); // One per behandling
    }

    @Test
    void lagre_behandlinger_shouldBeIdempotent() {
        var bulkData = TestMigreringData.lagBehandlinger();
        repo.lagre(bulkData);
        em.clear();

        var kvittering = repo.lagre(bulkData); // re-run

        assertThat(kvittering.kjørtUtenFeil()).isTrue();

        em.clear();
        assertThat(DBTestUtil.hentAlle(em, Behandling.class)).hasSize(2);
    }

    @Test
    void lagre_aktiveOppgaver_shouldPersistOppgaverOgReservasjoner() {
        var bulkData = TestMigreringData.lagBehandlinger(TestMigreringData.lagAktiveOppgaver());
        var kvittering = repo.lagre(bulkData);

        assertThat(kvittering.kjørtUtenFeil()).isTrue();
        assertThat(kvittering.oppgaver()).isEqualTo(2);
        assertThat(kvittering.reservasjoner()).isEqualTo(1);

        em.clear();
        assertThat(DBTestUtil.hentAlle(em, Oppgave.class)).hasSize(2);
        assertThat(DBTestUtil.hentAlle(em, Reservasjon.class)).hasSize(1);
    }

    @Test
    void lagre_aktiveOppgaver_shouldBeIdempotent() {
        var bulkData = TestMigreringData.lagBehandlinger(TestMigreringData.lagAktiveOppgaver());
        repo.lagre(bulkData);
        em.clear();

        var kvittering = repo.lagre(bulkData); // re-run
        assertThat(kvittering.kjørtUtenFeil()).isTrue();

        em.clear();
        assertThat(DBTestUtil.hentAlle(em, Oppgave.class)).hasSize(2);
        assertThat(DBTestUtil.hentAlle(em, Reservasjon.class)).hasSize(1);
    }

    @Test
    void lagre_emptyBulkData_shouldReturnSuccessfulKvittering() {
        var bulkData = BulkDataWrapper.behandlinger(List.of());
        var kvittering = repo.lagre(bulkData);

        assertThat(kvittering.kjørtUtenFeil()).isTrue();
        assertThat(kvittering.behandlinger()).isZero();
        assertThat(kvittering.oppgaver()).isZero();
    }

    @Test
    void lagre_saksbehandlerGruppe_shouldBePersisted() {
        var bulkData = TestMigreringData.lagOrganisasjonOgKøer();
        var kvittering = repo.lagre(bulkData);

        assertThat(kvittering.kjørtUtenFeil()).isTrue();

        em.clear();
        var grupper = DBTestUtil.hentAlle(em, SaksbehandlerGruppe.class);
        assertThat(grupper).isNotEmpty();
        assertThat(grupper.getFirst().getGruppeNavn()).isEqualTo("Testgruppe");
    }

    @Test
    void lagre_stats_oppgavefilter() {
        var dtoEksisterendeEntitet = new StatOppgaveFilterDataDto(1L,
            1735689600000L,
            LocalDate.of(2026, 1, 1),
            1, 2, 3, 4, 5,
            InnslagType.REGELMESSIG);
        var førsteKvittering1Ny = repo.lagre(BulkDataWrapper.statistikkOppgaveFilter(List.of(dtoEksisterendeEntitet)));
        em.clear();

        assertThat(førsteKvittering1Ny.kjørtUtenFeil()).isTrue();
        assertThat(førsteKvittering1Ny.statistikkOppgaveFilter()).isEqualTo(1);

        var dtoNy = new StatOppgaveFilterDataDto(
            2L,
            1235689600000L,
            LocalDate.of(2026, 2, 2),
            2, 2, 2, 2, 2,
            InnslagType.REGELMESSIG
        );
        var begge = BulkDataWrapper.statistikkOppgaveFilter(List.of(dtoNy, dtoEksisterendeEntitet));
        assertThat(begge.statistikkOppgaveFilter()).hasSize(2);

        var andreKvittering1Ny1Gammel = repo.lagre(begge);
        assertThat(andreKvittering1Ny1Gammel.kjørtUtenFeil()).isTrue();
        assertThat(andreKvittering1Ny1Gammel.statistikkOppgaveFilter()).isEqualTo(1);

        em.clear();

        var nøkkel = new StatistikkOppgaveFilterNøkkel(2L, 1235689600000L);
        var stat = em.find(StatistikkOppgaveFilter.class, nøkkel);

        assertThat(stat).isNotNull();
        assertThat(stat.getOppgaveFilterId()).isEqualTo(2L);
        assertThat(stat.getTidsstempel()).isEqualTo(1235689600000L);
        assertThat(stat.getStatistikkDato()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(stat.getAntallAktive()).isEqualTo(2);
        assertThat(stat.getAntallTilgjengelige()).isEqualTo(2);
        assertThat(stat.getAntallVentende()).isEqualTo(2);
        assertThat(stat.getAntallOpprettet()).isEqualTo(2);
        assertThat(stat.getAntallAvsluttet()).isEqualTo(2);
        assertThat(stat.getInnslagType()).isEqualTo(InnslagType.REGELMESSIG);

        em.clear();
        var tredjeKvittering2Gamle = repo.lagre(begge);
        assertThat(tredjeKvittering2Gamle.kjørtUtenFeil()).isTrue();
        assertThat(tredjeKvittering2Gamle.statistikkOppgaveFilter()).isZero();
    }

}

