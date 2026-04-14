package no.nav.foreldrepenger.los.migrering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
        assertThat(avdeling.getOpprettetAv()).isEqualTo("VL");
        assertThat(avdeling.getOpprettetTidspunkt()).isEqualTo(NOW.minusDays(10));
        assertThat(avdeling.getEndretAv()).isEqualTo("SB");
        assertThat(avdeling.getEndretTidspunkt()).isEqualTo(NOW);
    }

    @Test
    void mapSaksbehandler_shouldPreserveAllFields() {
        var dto = new SaksbehandlerDataDto("Z999999", "Test Testesen", "4806",
            "VL", NOW.minusDays(5), "VL", NOW);

        var sb = GcpImportMapper.mapSaksbehandler(dto);

        assertThat(sb.getSaksbehandlerIdent()).isEqualTo("Z999999");
        assertThat(sb.getNavn()).isEqualTo("Test Testesen");
        assertThat(sb.getAnsattVedEnhet()).isEqualTo("4806");
        assertThat(sb.getOpprettetAv()).isEqualTo("VL");
        assertThat(sb.getOpprettetTidspunkt()).isEqualTo(NOW.minusDays(5));
        assertThat(sb.getEndretAv()).isEqualTo("VL");
        assertThat(sb.getEndretTidspunkt()).isEqualTo(NOW);
    }

    @Test
    void mapSaksbehandlerGruppe_shouldPreserveAllFields() {
        var avdeling = new Avdeling("4806", "NAV Drammen", false);
        var dto = new SaksbehandlerGruppeDataDto(55L, "Gruppe A", "4806",
            "VL", NOW.minusDays(3), "VL", NOW);

        var gruppe = GcpImportMapper.mapSaksbehandlerGruppe(dto, avdeling);

        assertThat(gruppe.getId()).isEqualTo(55L);
        assertThat(gruppe.getGruppeNavn()).isEqualTo("Gruppe A");
        assertThat(gruppe.getAvdeling()).isSameAs(avdeling);
        assertThat(gruppe.getOpprettetAv()).isEqualTo("VL");
        assertThat(gruppe.getOpprettetTidspunkt()).isEqualTo(NOW.minusDays(3));
        assertThat(gruppe.getEndretAv()).isEqualTo("VL");
        assertThat(gruppe.getEndretTidspunkt()).isEqualTo(NOW);
    }

    @Test
    void mapSaksbehandlerGruppe_withNullAvdeling_shouldThrowException() {
        var dto = new SaksbehandlerGruppeDataDto(55L, "Gruppe B", "4806",
            "VL", NOW, "VL", NOW);

        assertThatThrownBy(() -> GcpImportMapper.mapSaksbehandlerGruppe(dto, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void mapBehandling_shouldMapEverything() {
        var id = UUID.randomUUID();
        var ventefrist = NOW.plusDays(7);
        var opprettet = NOW.minusDays(2);
        var behandlingsfrist = LocalDate.now().plusDays(30);
        var førsteStønadsdag = LocalDate.now().plusMonths(3);
        var feilutbetalingStart = LocalDate.now().minusDays(10);
        var opprettetTidspunkt = NOW.minusDays(5);
        var dto = new BehandlingDataDto(
            id,
            new SaksnummerDto("654321"),
            new AktørId("1234567890123"),
            Fagsystem.FPTILBAKE,
            FagsakYtelseType.FORELDREPENGER,
            BehandlingType.FØRSTEGANGSSØKNAD,
            BehandlingTilstand.OPPRETTET,
            "5080",
            ventefrist,
            opprettet,
            null,
            behandlingsfrist,
            førsteStønadsdag,
            new BigDecimal("10000"),
            feilutbetalingStart,
            "4806",
            "VL", opprettetTidspunkt,
            "VL", NOW,
            Set.of(AndreKriterierType.PAPIRSØKNAD)
        );

        var behandling = GcpImportMapper.mapBehandling(dto);

        assertThat(behandling.getId()).isEqualTo(id);
        assertThat(behandling.getSaksnummer().getVerdi()).isEqualTo("654321");
        assertThat(behandling.getAktørId().getId()).isEqualTo("1234567890123");
        assertThat(behandling.getKildeSystem()).isEqualTo(Fagsystem.FPTILBAKE);
        assertThat(behandling.getFagsakYtelseType()).isEqualTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(behandling.getBehandlingType()).isEqualTo(BehandlingType.FØRSTEGANGSSØKNAD);
        assertThat(behandling.getBehandlingTilstand()).isEqualTo(BehandlingTilstand.OPPRETTET);
        assertThat(behandling.getAktiveAksjonspunkt()).isEqualTo("5080");
        assertThat(behandling.getVentefrist()).isEqualTo(ventefrist);
        assertThat(behandling.getOpprettet()).isEqualTo(opprettet);
        assertThat(behandling.getAvsluttet()).isNull();
        assertThat(behandling.getBehandlingsfrist()).isEqualTo(behandlingsfrist);
        assertThat(behandling.getFørsteStønadsdag()).isEqualTo(førsteStønadsdag);
        assertThat(behandling.getBehandlendeEnhet()).isEqualTo("4806");
        assertThat(behandling.getFeilutbetalingBelop()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(behandling.getFeilutbetalingStart()).isEqualTo(feilutbetalingStart);
        assertThat(behandling.getOpprettetAv()).isEqualTo("VL");
        assertThat(behandling.getOpprettetTidspunkt()).isEqualTo(opprettetTidspunkt);
        assertThat(behandling.getEndretAv()).isEqualTo("VL");
        assertThat(behandling.getEndretTidspunkt()).isEqualTo(NOW);
        assertThat(behandling.getKriterier()).containsExactly(AndreKriterierType.PAPIRSØKNAD);
    }

    @Test
    void mapOppgave_shouldPreserveAllFields() {
        var behandlingId = UUID.randomUUID();
        var oppgaveAvsluttet = NOW.minusHours(3);
        var dto = new OppgaveDataDto(
            42L,
            behandlingId,
            "4806",
            true,
            oppgaveAvsluttet,
            "VL",
            NOW.minusDays(2),
            "SB",
            NOW.minusHours(1),
            null,
            List.of(
                new OppgaveEgenskapDataDto(AndreKriterierType.PAPIRSØKNAD, null),
                new OppgaveEgenskapDataDto(AndreKriterierType.TIL_BESLUTTER, "z999999")
            )
        );

        var behandling = Behandling.builder(Optional.empty())
            .dummyBehandling("4806", BehandlingTilstand.OPPRETTET)
            .medId(behandlingId)
            .build();
        var oppgave = GcpImportMapper.mapOppgave(dto, behandling);

        assertThat(oppgave.getId()).isEqualTo(42L);
        assertThat(oppgave.getBehandlingId().toUUID()).isEqualTo(behandlingId);
        assertThat(oppgave.getBehandlendeEnhet()).isEqualTo("4806");
        assertThat(oppgave.getAktiv()).isTrue();
        assertThat(oppgave.getOppgaveAvsluttet()).isEqualTo(oppgaveAvsluttet);
        assertThat(oppgave.getOpprettetAv()).isEqualTo("VL");
        assertThat(oppgave.getOpprettetTidspunkt()).isEqualTo(NOW.minusDays(2));
        assertThat(oppgave.getEndretAv()).isEqualTo("SB");
        assertThat(oppgave.getEndretTidspunkt()).isEqualTo(NOW.minusHours(1));
        assertThat(oppgave.getOppgaveEgenskaper())
            .extracting("andreKriterierType", "sisteSaksbehandlerForTotrinn")
            .containsExactlyInAnyOrder(
                tuple(AndreKriterierType.PAPIRSØKNAD, null),
                tuple(AndreKriterierType.TIL_BESLUTTER, "Z999999")
            );
    }

    @Test
    void mapOppgave_withNullEgenskaper_shouldReturnEmptySet() {
        var behandlingId = UUID.randomUUID();
        var dto = new OppgaveDataDto(
            43L, behandlingId,
            "4806",
            true, null,
            "VL", NOW, "VL", NOW,
            null, null
        );

        var behandling = Behandling.builder(Optional.empty()).dummyBehandling("4806", BehandlingTilstand.OPPRETTET).medId(behandlingId).build();
        var oppgave = GcpImportMapper.mapOppgave(dto, behandling);

        assertThat(oppgave.getId()).isEqualTo(43L);
        assertThat(oppgave.getBehandlingId().toUUID()).isEqualTo(behandlingId);
        assertThat(oppgave.getBehandlendeEnhet()).isEqualTo("4806");
        assertThat(oppgave.getAktiv()).isTrue();
        assertThat(oppgave.getOppgaveAvsluttet()).isNull();
        assertThat(oppgave.getOpprettetAv()).isEqualTo("VL");
        assertThat(oppgave.getOpprettetTidspunkt()).isEqualTo(NOW);
        assertThat(oppgave.getEndretAv()).isEqualTo("VL");
        assertThat(oppgave.getEndretTidspunkt()).isEqualTo(NOW);
        assertThat(oppgave.getOppgaveEgenskaper()).isEmpty();
    }

    @Test
    void mapReservasjon_shouldSetAllFields() {
        var dto = new ReservasjonDataDto(
            NOW.plusDays(1), "z999999",
            "z888888", NOW.minusHours(2), "Flyttet pga fravær",
            "VL", NOW.minusDays(1),
            "Z888888", NOW.minusHours(2)
        );

        var behandling = Behandling.builder(Optional.empty()).dummyBehandling("4806", BehandlingTilstand.OPPRETTET).build();
        var oppgave = new Oppgave(behandling, "4806");

        var reservasjon = new Reservasjon(oppgave, "Z999999");
        GcpImportMapper.mapReservasjon(dto, reservasjon, oppgave);

        assertThat(reservasjon.getOppgave()).isSameAs(oppgave);
        assertThat(reservasjon.getReservertTil()).isEqualTo(NOW.plusDays(1));
        assertThat(reservasjon.getReservertAv()).isEqualTo("Z999999");
        assertThat(reservasjon.getFlyttetAv()).isEqualTo("Z888888");
        assertThat(reservasjon.getFlyttetTidspunkt()).isEqualTo(NOW.minusHours(2));
        assertThat(reservasjon.getBegrunnelse()).isEqualTo("Flyttet pga fravær");
        assertThat(reservasjon.getOpprettetAv()).isEqualTo("VL");
        assertThat(reservasjon.getOpprettetTidspunkt()).isEqualTo(NOW.minusDays(1));
        assertThat(reservasjon.getEndretAv()).isEqualTo("Z888888");
        assertThat(reservasjon.getEndretTidspunkt()).isEqualTo(NOW.minusHours(2));
    }

    @Test
    void mapOppgaveEgenskap_tilBeslutter_shouldMapSisteSaksbehandler() {
        var dto = new OppgaveEgenskapDataDto(AndreKriterierType.TIL_BESLUTTER, "Z999999");

        var behandling = Behandling.builder(Optional.empty()).dummyBehandling("4806", BehandlingTilstand.OPPRETTET).build();
        var oppgave = new Oppgave(behandling, "4806");
        oppgave.leggTilOppgaveEgenskap(dto.andreKriterierType(), dto.sisteSaksbehandlerForTotrinn());
        var egenskap = oppgave.getOppgaveEgenskaper().stream().findFirst().orElseGet(Assertions::fail);

        assertThat(egenskap.andreKriterierType()).isEqualTo(AndreKriterierType.TIL_BESLUTTER);
        assertThat(egenskap.sisteSaksbehandlerForTotrinn()).isEqualTo("Z999999");
    }


    @Test
    void mapOppgaveFiltrering_withAndreKriterier_shouldSeparateInkluderEkskluder() {
        var avdeling = new Avdeling("4806", "NAV Drammen", false);
        var fomDato = LocalDate.now().minusDays(10);
        var tomDato = LocalDate.now().plusDays(10);
        var dto = new OppgaveFiltreringDataDto(
            100L, "Testkø", "Beskrivelse",
            KøSortering.BEHANDLINGSFRIST, "4806",
            fomDato, tomDato,
            3L, 9L,
            Periodefilter.FAST_PERIODE,
            "VL", NOW, "VL", NOW,
            List.of(BehandlingType.FØRSTEGANGSSØKNAD),
            List.of(FagsakYtelseType.FORELDREPENGER),
            List.of(
                new AndreKriterierDataDto(AndreKriterierType.PAPIRSØKNAD, true),
                new AndreKriterierDataDto(AndreKriterierType.TIL_BESLUTTER, false)
            ), Set.of());

        var filtrering = GcpImportMapper.mapOppgaveFiltrering(dto, avdeling);

        assertThat(filtrering.getId()).isEqualTo(100L);
        assertThat(filtrering.getNavn()).isEqualTo("Testkø");
        assertThat(filtrering.getBeskrivelse()).isEqualTo("Beskrivelse");
        assertThat(filtrering.getSortering()).isEqualTo(KøSortering.BEHANDLINGSFRIST);
        assertThat(filtrering.getAvdeling().getAvdelingEnhet()).isEqualTo("4806");
        assertThat(filtrering.getFomDato()).isEqualTo(fomDato);
        assertThat(filtrering.getTomDato()).isEqualTo(tomDato);
        assertThat(filtrering.getFra()).isEqualTo(3L);
        assertThat(filtrering.getTil()).isEqualTo(9L);
        assertThat(filtrering.getPeriodefilter()).isEqualTo(Periodefilter.FAST_PERIODE);
        assertThat(filtrering.getOpprettetAv()).isEqualTo("VL");
        assertThat(filtrering.getOpprettetTidspunkt()).isEqualTo(NOW);
        assertThat(filtrering.getEndretAv()).isEqualTo("VL");
        assertThat(filtrering.getEndretTidspunkt()).isEqualTo(NOW);
        assertThat(filtrering.getBehandlingTyper()).containsExactly(BehandlingType.FØRSTEGANGSSØKNAD);
        assertThat(filtrering.getFagsakYtelseTyper()).containsExactly(FagsakYtelseType.FORELDREPENGER);
        assertThat(filtrering.getFiltreringAndreKriterierTyper())
            .extracting("andreKriterierType", "inkluder")
            .containsExactlyInAnyOrder(
                tuple(AndreKriterierType.PAPIRSØKNAD, true),
                tuple(AndreKriterierType.TIL_BESLUTTER, false)
            );
    }
}

