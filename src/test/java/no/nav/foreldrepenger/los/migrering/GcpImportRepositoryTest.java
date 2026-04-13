package no.nav.foreldrepenger.los.migrering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import no.nav.foreldrepenger.los.migrering.dto.AvdelingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.AvdelingSaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.StatEnhetYtelseBehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.GruppeTilknytningDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OrgDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerGruppeDataDto;

import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;

import no.nav.foreldrepenger.los.statistikk.StatistikkEnhetYtelseBehandling;
import no.nav.foreldrepenger.los.statistikk.StatistikkEnhetYtelseBehandlingNøkkel;

import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerRelasjon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.DBTestUtil;
import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;
import no.nav.foreldrepenger.los.migrering.dto.StatOppgaveFilterDataDto;
import no.nav.foreldrepenger.los.migrering.gcp.GcpImportRepository;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingEgenskap;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.organisasjon.GruppeTilknytningRelasjon;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;
import no.nav.foreldrepenger.los.statistikk.kø.InnslagType;
import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilter;
import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilterNøkkel;

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
        repo.lagre(bulkData);

        em.flush();

        em.clear();
        assertThat(DBTestUtil.hentAlle(em, Avdeling.class)).isNotEmpty();
        assertThat(DBTestUtil.hentAlle(em, Saksbehandler.class)).isNotEmpty();
        assertThat(DBTestUtil.hentAlle(em, SaksbehandlerGruppe.class)).isNotEmpty();
        assertThat(DBTestUtil.hentAlle(em, OppgaveFiltrering.class)).isNotEmpty();
        assertThat(em.createQuery("from FiltreringSaksbehandlerRelasjon", FiltreringSaksbehandlerRelasjon.class).getResultList()).isNotEmpty();
    }

    @Test
    void lagre_organisasjon_shouldBeIdempotent() {
        var now = LocalDateTime.now();
        var orgData = new OrgDataDto(
            List.of(new AvdelingDataDto("4806", "NAV Drammen", false, true, "VL", now.minusDays(5), "VL", now)),
            List.of(new SaksbehandlerDataDto("Z999999", "Saksbehandler Z999999", "4806", "VL", now.minusDays(5), "VL", now)),
            List.of(new AvdelingSaksbehandlerDataDto("4806", "Z999999")),
            List.of(new SaksbehandlerGruppeDataDto(5_000_003L, "Testgruppe", "4806", "VL", now.minusDays(5), "VL", now)),
            List.of(new GruppeTilknytningDataDto("Z999999", 5_000_003L))
        );

        var bulkData = BulkDataWrapper.organisasjonOgKøOppset(orgData, List.of());

        repo.lagre(bulkData); // first run
        em.flush();
        em.clear();

        assertThat(DBTestUtil.hentAlle(em, Avdeling.class)).hasSize(1);
        assertThat(DBTestUtil.hentAlle(em, Saksbehandler.class)).hasSize(1);
        assertThat(DBTestUtil.hentAlle(em, SaksbehandlerGruppe.class)).hasSize(1);
        assertThat(em.createQuery("from AvdelingSaksbehandlerRelasjon", AvdelingSaksbehandlerRelasjon.class).getResultList()).hasSize(1);
        assertThat(em.createQuery("from GruppeTilknytningRelasjon", GruppeTilknytningRelasjon.class).getResultList()).hasSize(1);

        repo.lagre(bulkData); // second run
        em.flush();
        em.clear();

        assertThat(DBTestUtil.hentAlle(em, Avdeling.class)).hasSize(1);
        assertThat(DBTestUtil.hentAlle(em, Saksbehandler.class)).hasSize(1);
        assertThat(DBTestUtil.hentAlle(em, SaksbehandlerGruppe.class)).hasSize(1);
        assertThat(em.createQuery("from AvdelingSaksbehandlerRelasjon", AvdelingSaksbehandlerRelasjon.class).getResultList()).hasSize(1);
        assertThat(em.createQuery("from GruppeTilknytningRelasjon", GruppeTilknytningRelasjon.class).getResultList()).hasSize(1);
    }

    @Test
    void lagre_organisasjon_shouldPersistAndDeduplicateRelations() {
        var now = LocalDateTime.now();
        var orgData = new OrgDataDto(
            List.of(new AvdelingDataDto("4806", "NAV Drammen", false, true, "VL", now.minusDays(5), "VL", now)),
            List.of(new SaksbehandlerDataDto("Z999999", "Saksbehandler Z999999", "4806", "VL", now.minusDays(5), "VL", now)),
            List.of(new AvdelingSaksbehandlerDataDto("4806", "Z999999")),
            List.of(new SaksbehandlerGruppeDataDto(5_000_003L, "Testgruppe", "4806", "VL", now.minusDays(5), "VL", now)),
            List.of(new GruppeTilknytningDataDto("Z999999", 5_000_003L))
        );
        var bulkData = BulkDataWrapper.organisasjonOgKøOppset(orgData, List.of());

        repo.lagre(bulkData);
        em.flush();
        em.clear();

        var avdelingSaksbehandlerRelasjoner = em.createQuery("from AvdelingSaksbehandlerRelasjon", AvdelingSaksbehandlerRelasjon.class).getResultList();
        assertThat(avdelingSaksbehandlerRelasjoner)
            .extracting(r -> r.getSaksbehandler().getSaksbehandlerIdent(), r -> r.getAvdeling().getAvdelingEnhet())
            .containsExactly(tuple("Z999999", "4806"));

        var gruppeTilknytninger = em.createQuery("from GruppeTilknytningRelasjon", GruppeTilknytningRelasjon.class).getResultList();
        assertThat(gruppeTilknytninger)
            .extracting(r -> r.getSaksbehandler().getSaksbehandlerIdent(), r -> r.getGruppe().getId())
            .containsExactly(tuple("Z999999", 5_000_003L));

        repo.lagre(bulkData);
        em.flush();
        em.clear();

        assertThat(em.createQuery("from AvdelingSaksbehandlerRelasjon", AvdelingSaksbehandlerRelasjon.class).getResultList())
            .hasSize(1)
            .extracting(r -> r.getSaksbehandler().getSaksbehandlerIdent(), r -> r.getAvdeling().getAvdelingEnhet())
            .containsExactly(tuple("Z999999", "4806"));
        assertThat(em.createQuery("from GruppeTilknytningRelasjon", GruppeTilknytningRelasjon.class).getResultList())
            .hasSize(1)
            .extracting(r -> r.getSaksbehandler().getSaksbehandlerIdent(), r -> r.getGruppe().getId())
            .containsExactly(tuple("Z999999", 5_000_003L));
    }

    @Test
    void lagre_behandlinger_shouldPersistWithEgenskaper() {
        var bulkData = TestMigreringData.lagBehandlinger();
        repo.lagre(bulkData);
        em.flush();
        em.clear();

        assertThat(DBTestUtil.hentAlle(em, Behandling.class)).hasSize(2);

        var egenskaper = em.createQuery("FROM BehandlingEgenskap", BehandlingEgenskap.class).getResultList();
        assertThat(egenskaper).hasSize(2); // One per behandling
    }

    @Test
    void lagre_behandlinger_shouldBeIdempotent() {
        var bulkData = TestMigreringData.lagBehandlinger();
        repo.lagre(bulkData);
        em.flush();
        em.clear();

        repo.lagre(bulkData); // re-run
        em.flush();
        em.clear();
        assertThat(DBTestUtil.hentAlle(em, Behandling.class)).hasSize(2);
    }

    @Test
    void lagre_aktiveOppgaver_shouldPersistOppgaverOgReservasjoner() {
        var bulkData = TestMigreringData.lagBehandlinger(TestMigreringData.lagAktiveOppgaver());
        repo.lagre(bulkData);
        em.flush();

        em.clear();
        assertThat(DBTestUtil.hentAlle(em, Oppgave.class)).hasSize(2);
        assertThat(DBTestUtil.hentAlle(em, Reservasjon.class)).hasSize(1);
    }

    @Test
    void lagre_aktiveOppgaver_shouldBeIdempotent() {
        var bulkData = TestMigreringData.lagBehandlinger(TestMigreringData.lagAktiveOppgaver());
        repo.lagre(bulkData);
        em.flush();
        em.clear();

        repo.lagre(bulkData); // re-run
        em.flush();
        em.clear();
        assertThat(DBTestUtil.hentAlle(em, Oppgave.class)).hasSize(2);
        assertThat(DBTestUtil.hentAlle(em, Reservasjon.class)).hasSize(1);
    }

    @Test
    void lagre_saksbehandlerGruppe_shouldBePersisted() {
        var bulkData = TestMigreringData.lagOrganisasjonOgKøer();
        repo.lagre(bulkData);

        em.clear();
        var grupper = DBTestUtil.hentAlle(em, SaksbehandlerGruppe.class);
        assertThat(grupper).isNotEmpty();
        assertThat(grupper.getFirst().getGruppeNavn()).isEqualTo("Testgruppe");
    }

    @Test
    void lagre_organisasjon_shouldNotMutateExistingEntities_butPersistNewIds() {
        var existingAvdeling = new Avdeling("4806", "Eksisterende avdeling", false);
        existingAvdeling.setErAktiv(true);
        em.persist(existingAvdeling);

        var existingSaksbehandler = new Saksbehandler("Z999999", "Eksisterende saksbehandler", "4806");
        em.persist(existingSaksbehandler);

        var existingGruppe = new SaksbehandlerGruppe("Eksisterende gruppe", existingAvdeling);
        existingGruppe.setId(5_000_003L);
        em.persist(existingGruppe);

        em.flush();
        em.clear();

        var now = LocalDateTime.now();
        var orgData = new OrgDataDto(
            List.of(
                new AvdelingDataDto("4806", "Skal ikke overskrive", true, false, "VL", now.minusDays(5), "VL", now),
                new AvdelingDataDto("4812", "Ny avdeling", false, true, "VL", now.minusDays(5), "VL", now)
            ),
            List.of(
                new SaksbehandlerDataDto("Z999999", "Skal ikke overskrive", "4812", "VL", now.minusDays(5), "VL", now),
                new SaksbehandlerDataDto("Z123456", "Ny saksbehandler", "4812", "VL", now.minusDays(5), "VL", now)
            ),
            List.of(
                new AvdelingSaksbehandlerDataDto("4806", "Z999999"),
                new AvdelingSaksbehandlerDataDto("4812", "Z123456")
            ),
            List.of(
                new SaksbehandlerGruppeDataDto(5_000_003L, "Skal ikke overskrive", "4806", "VL", now.minusDays(5), "VL", now),
                new SaksbehandlerGruppeDataDto(5_000_004L, "Ny gruppe", "4812", "VL", now.minusDays(5), "VL", now)
            ),
            List.of(
                new GruppeTilknytningDataDto("Z999999", 5_000_003L),
                new GruppeTilknytningDataDto("Z123456", 5_000_004L)
            )
        );

        repo.lagre(BulkDataWrapper.organisasjonOgKøOppset(orgData, List.of()));
        em.flush();
        em.clear();

        var avdeling = em.find(Avdeling.class, "4806");
        assertThat(avdeling.getNavn()).isEqualTo("Eksisterende avdeling");
        assertThat(avdeling.getKreverKode6()).isFalse();
        assertThat(avdeling.getErAktiv()).isTrue();

        var saksbehandler = em.find(Saksbehandler.class, "Z999999");
        assertThat(saksbehandler.getNavn()).isEqualTo("Eksisterende saksbehandler");
        assertThat(saksbehandler.getAnsattVedEnhet()).isEqualTo("4806");

        var gruppe = em.find(SaksbehandlerGruppe.class, 5_000_003L);
        assertThat(gruppe.getGruppeNavn()).isEqualTo("Eksisterende gruppe");

        var nyAvdeling = em.find(Avdeling.class, "4812");
        assertThat(nyAvdeling).isNotNull();
        assertThat(nyAvdeling.getNavn()).isEqualTo("Ny avdeling");

        var nySaksbehandler = em.find(Saksbehandler.class, "Z123456");
        assertThat(nySaksbehandler).isNotNull();
        assertThat(nySaksbehandler.getNavn()).isEqualTo("Ny saksbehandler");

        var nyGruppe = em.find(SaksbehandlerGruppe.class, 5_000_004L);
        assertThat(nyGruppe).isNotNull();
        assertThat(nyGruppe.getGruppeNavn()).isEqualTo("Ny gruppe");
    }

    @Test
    void lagre_organisasjon_shouldIgnoreNullOrgData() {
        assertThatCode(() -> repo.lagre(BulkDataWrapper.organisasjonOgKøOppset(null, List.of()))).doesNotThrowAnyException();
        em.flush();
        em.clear();

        assertThat(DBTestUtil.hentAlle(em, Avdeling.class)).isEmpty();
        assertThat(DBTestUtil.hentAlle(em, Saksbehandler.class)).isEmpty();
        assertThat(DBTestUtil.hentAlle(em, SaksbehandlerGruppe.class)).isEmpty();
        assertThat(em.createQuery("from AvdelingSaksbehandlerRelasjon", AvdelingSaksbehandlerRelasjon.class).getResultList()).isEmpty();
        assertThat(em.createQuery("from GruppeTilknytningRelasjon", GruppeTilknytningRelasjon.class).getResultList()).isEmpty();
    }

    @Test
    void lagre_organisasjon_shouldAllowNullGruppeTilknytninger() {
        var now = LocalDateTime.now();
        var orgData = new OrgDataDto(
            List.of(new AvdelingDataDto("4806", "NAV Drammen", false, true, "VL", now.minusDays(5), "VL", now)),
            List.of(new SaksbehandlerDataDto("Z999999", "Saksbehandler Z999999", "4806", "VL", now.minusDays(5), "VL", now)),
            List.of(new AvdelingSaksbehandlerDataDto("4806", "Z999999")),
            List.of(new SaksbehandlerGruppeDataDto(5_000_003L, "Testgruppe", "4806", "VL", now.minusDays(5), "VL", now)),
            null
        );

        assertThatCode(() -> repo.lagre(BulkDataWrapper.organisasjonOgKøOppset(orgData, List.of()))).doesNotThrowAnyException();
        em.flush();
        em.clear();

        assertThat(DBTestUtil.hentAlle(em, Avdeling.class)).hasSize(1);
        assertThat(DBTestUtil.hentAlle(em, Saksbehandler.class)).hasSize(1);
        assertThat(DBTestUtil.hentAlle(em, SaksbehandlerGruppe.class)).hasSize(1);
        assertThat(em.createQuery("from AvdelingSaksbehandlerRelasjon", AvdelingSaksbehandlerRelasjon.class).getResultList()).hasSize(1);
        assertThat(em.createQuery("from GruppeTilknytningRelasjon", GruppeTilknytningRelasjon.class).getResultList()).isEmpty();
    }

    @Test
    void lagre_stats_oppgavefilter() {
        var statOppgaveFilter1 = new StatOppgaveFilterDataDto(1L,
            1735689600000L,
            LocalDate.of(2026, 1, 1),
            1, 2, 3, 4, 5,
            InnslagType.REGELMESSIG);

        var statOppgaveFilter2 = new StatOppgaveFilterDataDto(
            2L,
            1235689600000L,
            LocalDate.of(2026, 2, 2),
            2, 2, 2, 2, 2,
            InnslagType.REGELMESSIG
        );
        var bulkData = BulkDataWrapper.statistikkOppgaveFilter(List.of(statOppgaveFilter2, statOppgaveFilter1));

        repo.lagre(bulkData);
        em.flush();
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
        assertThatCode(() -> repo.lagre(bulkData)).doesNotThrowAnyException(); // sender samme på nytt
    }

    @Test
    void lagre_stats_enhetytelsebehandling() {
        var dto = new StatEnhetYtelseBehandlingDataDto("4867",
            1735689600000L, FagsakYtelseType.FORELDREPENGER, BehandlingType.FØRSTEGANGSSØKNAD,
            LocalDate.of(2026, 1, 1),
            1, 1, 1);

        repo.lagre(BulkDataWrapper.statistikkEnhetYtelseBehandling(List.of(dto)));
        em.flush();
        em.clear();

        var nøkkel = new StatistikkEnhetYtelseBehandlingNøkkel("4867", 1735689600000L, FagsakYtelseType.FORELDREPENGER, BehandlingType.FØRSTEGANGSSØKNAD);
        var stat = em.find(StatistikkEnhetYtelseBehandling.class, nøkkel);
        assertThat(stat).isNotNull();
        assertThat(stat).matches(s -> s.getBehandlendeEnhet().equals("4867")
            && s.getTidsstempel() == 1735689600000L
            && s.getFagsakYtelseType() == FagsakYtelseType.FORELDREPENGER
            && s.getBehandlingType() == BehandlingType.FØRSTEGANGSSØKNAD
            && s.getStatistikkDato().equals(LocalDate.of(2026, 1, 1))
            && s.getAntallAktive() == 1
            && s.getAntallOpprettet() == 1
            && s.getAntallAvsluttet() == 1);

        em.clear();

        assertThatCode(() -> repo.lagre(BulkDataWrapper.statistikkEnhetYtelseBehandling(List.of(dto)))).doesNotThrowAnyException(); // sender samme på nytt
    }


}

