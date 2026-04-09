package no.nav.foreldrepenger.los.migrering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.aktør.AktørId;
import no.nav.foreldrepenger.los.migrering.dto.AndreKriterierDataDto;
import no.nav.foreldrepenger.los.migrering.dto.AvdelingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveEgenskapDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveFiltreringDataDto;
import no.nav.foreldrepenger.los.migrering.dto.ReservasjonDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerGruppeDataDto;
import no.nav.foreldrepenger.los.migrering.gcp.GcpImportMapper;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingTilstand;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgave.Periodefilter;
import no.nav.foreldrepenger.los.oppgavekø.KøSortering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;
import no.nav.foreldrepenger.los.tjenester.saksbehandler.oppgave.dto.SaksnummerDto;

@ExtendWith(JpaExtension.class)
class GcpImportMapperTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    void mapAvdeling_shouldPreserveAllFields() {
        var dto = new AvdelingDataDto("4806", "NAV Drammen", true, true,
            "VL", NOW.minusDays(10), "SB", NOW);

        var avdeling = GcpImportMapper.mapAvdeling(dto);

        assertThat(avdeling.getAvdelingEnhet()).isEqualTo("4806");
        assertThat(avdeling.getNavn()).isEqualTo("NAV Drammen");
        assertThat(avdeling.getKreverKode6()).isTrue();
        assertThat(avdeling.getErAktiv()).isTrue();
    }

    @Test
    void mapSaksbehandler_shouldPreserveAllFields() {
        var dto = new SaksbehandlerDataDto("Z999999", "Test Testesen", "4806",
            "VL", NOW.minusDays(5), "VL", NOW);

        var sb = GcpImportMapper.mapSaksbehandler(dto);

        assertThat(sb.getSaksbehandlerIdent()).isEqualTo("Z999999");
        assertThat(sb.getNavn()).isEqualTo("Test Testesen");
    }

    @Test
    void mapSaksbehandlerGruppe_shouldPreserveAllFields() {
        var avdeling = new Avdeling("4806", "NAV Drammen", false);
        var dto = new SaksbehandlerGruppeDataDto(55L, "Gruppe A", "4806",
            "VL", NOW.minusDays(3), "VL", NOW);

        var gruppe = GcpImportMapper.mapSaksbehandlerGruppe(dto, avdeling);

        assertThat(gruppe.getGruppeNavn()).isEqualTo("Gruppe A");
        assertThat(gruppe.getAvdeling()).isSameAs(avdeling);
    }

    @Test
    void mapSaksbehandlerGruppe_withNullAvdeling_shouldHandleGracefully() {
        var dto = new SaksbehandlerGruppeDataDto(55L, "Gruppe B", "4806",
            "VL", NOW, "VL", NOW);

        var gruppe = GcpImportMapper.mapSaksbehandlerGruppe(dto, null);

        assertThat(gruppe.getAvdeling()).isNull();
    }

    @Test
    void mapBehandling_shouldMapEverything() {
        var id = UUID.randomUUID();
        var dto = new BehandlingDataDto(
            id,
            new SaksnummerDto("654321"),
            new AktørId("1234567890123"),
            Fagsystem.FPSAK,
            FagsakYtelseType.FORELDREPENGER,
            BehandlingType.FØRSTEGANGSSØKNAD,
            BehandlingTilstand.OPPRETTET,
            "5080",
            NOW.plusDays(7),
            NOW.minusDays(2),
            null,
            LocalDate.now().plusDays(30),
            LocalDate.now().plusMonths(3),
            new BigDecimal("10000"),
            LocalDate.now().minusDays(10),
            "4806",
            "VL", NOW.minusDays(5),
            "VL", NOW,
            Set.of(AndreKriterierType.PAPIRSØKNAD)
        );

        var behandling = new no.nav.foreldrepenger.los.oppgave.Behandling();
        GcpImportMapper.mapBehandling(dto, behandling);

        assertThat(behandling.getId()).isEqualTo(id);
        assertThat(behandling.getSaksnummer().getVerdi()).isEqualTo("654321");
        assertThat(behandling.getAktørId().getId()).isEqualTo("1234567890123");
        assertThat(behandling.getKildeSystem()).isEqualTo(Fagsystem.FPSAK);
        assertThat(behandling.getFagsakYtelseType()).isEqualTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(behandling.getBehandlingType()).isEqualTo(BehandlingType.FØRSTEGANGSSØKNAD);
        assertThat(behandling.getBehandlingTilstand()).isEqualTo(BehandlingTilstand.OPPRETTET);
        assertThat(behandling.getAktiveAksjonspunkt()).isEqualTo("5080");
        assertThat(behandling.getBehandlendeEnhet()).isEqualTo("4806");
        assertThat(behandling.getFeilutbetalingBelop()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(behandling.getKriterier()).containsExactly(AndreKriterierType.PAPIRSØKNAD);
    }

    @Test
    void mapOppgave_shouldPreserveAllFields() {
        var dto = TestMigreringData.lagOppgaveDataDto(42L);

        var oppgave = new Oppgave();
        GcpImportMapper.mapOppgave(dto, new Behandling(), oppgave);

        assertThat(oppgave.getId()).isEqualTo(42L);
        //assertThat(oppgave.getSaksnummer().getVerdi()).isEqualTo("123456");
        assertThat(oppgave.getAktiv()).isTrue();
        //assertThat(oppgave.getFagsakYtelseType()).isEqualTo(FagsakYtelseType.FORELDREPENGER);
        //assertThat(oppgave.getBehandlingType()).isEqualTo(BehandlingType.FØRSTEGANGSSØKNAD);
        assertThat(oppgave.getOppgaveEgenskaper()).hasSize(1);
    }

    @Test
    void mapOppgave_withNullEgenskaper_shouldReturnEmptySet() {
        var dto = new OppgaveDataDto(
            43L, UUID.randomUUID(),
            "4806",
            true, null,
            "VL", NOW, "VL", NOW,
            null, null
        );

        var oppgave = new Oppgave();
        GcpImportMapper.mapOppgave(dto, new Behandling(), oppgave);
        assertThat(oppgave.getOppgaveEgenskaper()).isEmpty();
    }

    @Test
    void mapReservasjon_shouldSetAllFields() {
        var dto = new ReservasjonDataDto(
            NOW.plusDays(1), "Z999999",
            "Z888888", NOW.minusHours(2), "Flyttet pga fravær",
            "VL", NOW.minusDays(1),
            "Z888888", NOW.minusHours(2)
        );

        var oppgave = new Oppgave();

        var reservasjon = new Reservasjon();
        GcpImportMapper.mapReservasjon(dto, reservasjon, oppgave);

        assertThat(reservasjon.getReservertAv()).isEqualTo("Z999999");
        assertThat(reservasjon.getFlyttetAv()).isEqualTo("Z888888");
        assertThat(reservasjon.getBegrunnelse()).isEqualTo("Flyttet pga fravær");
    }

    @Test
    void mapOppgaveEgenskap_tilBeslutter_shouldMapSisteSaksbehandler() {
        var dto = new OppgaveEgenskapDataDto(AndreKriterierType.TIL_BESLUTTER, "Z999999");

        var oppgave = new Oppgave();
        oppgave.leggTilOppgaveEgenskap(dto.andreKriterierType(), dto.sisteSaksbehandlerForTotrinn());
        var egenskap = oppgave.getOppgaveEgenskaper().stream().findFirst().orElseGet(Assertions::fail);

        assertThat(egenskap.getAndreKriterierType()).isEqualTo(AndreKriterierType.TIL_BESLUTTER);
        assertThat(egenskap.getSisteSaksbehandlerForTotrinn()).isEqualTo("Z999999");
    }


    @Test
    void mapOppgaveFiltrering_withAndreKriterier_shouldSeparateInkluderEkskluder() {
        var avdeling = new Avdeling("4806", "NAV Drammen", false);
        var dto = new OppgaveFiltreringDataDto(
            100L, "Testkø", "Beskrivelse",
            KøSortering.BEHANDLINGSFRIST, "4806",
            LocalDate.now().minusDays(10), LocalDate.now().plusDays(10),
            null, null,
            Periodefilter.FAST_PERIODE,
            "VL", NOW, "VL", NOW,
            List.of(BehandlingType.FØRSTEGANGSSØKNAD),
            List.of(FagsakYtelseType.FORELDREPENGER),
            List.of(
                new AndreKriterierDataDto(AndreKriterierType.PAPIRSØKNAD, true),
                new AndreKriterierDataDto(AndreKriterierType.TIL_BESLUTTER, false)
            )
        );

        var filtrering = GcpImportMapper.mapOppgaveFiltrering(dto, avdeling);

        assertThat(filtrering.getId()).isEqualTo(100L);
        assertThat(filtrering.getNavn()).isEqualTo("Testkø");
        assertThat(filtrering.getSortering()).isEqualTo(KøSortering.BEHANDLINGSFRIST);
        assertThat(filtrering.getAvdeling().getAvdelingEnhet()).isEqualTo("4806");
        assertThat(filtrering.getBehandlingTyper()).containsExactly(BehandlingType.FØRSTEGANGSSØKNAD);
        assertThat(filtrering.getFagsakYtelseTyper()).containsExactly(FagsakYtelseType.FORELDREPENGER);
        assertThat(filtrering.getFiltreringAndreKriterierTyper()).hasSize(2);
    }
}

