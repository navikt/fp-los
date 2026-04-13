package no.nav.foreldrepenger.los.migrering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.domene.typer.aktør.AktørId;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveEgenskapDataDto;
import no.nav.foreldrepenger.los.migrering.fss.FssExportMapper;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingTilstand;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgave.Periodefilter;
import no.nav.foreldrepenger.los.oppgavekø.KøSortering;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerNøkkel;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.GruppeTilknytningNøkkel;
import no.nav.foreldrepenger.los.organisasjon.GruppeTilknytningRelasjon;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;
import no.nav.foreldrepenger.los.statistikk.StatistikkEnhetYtelseBehandling;
import no.nav.foreldrepenger.los.statistikk.kø.InnslagType;
import no.nav.foreldrepenger.los.statistikk.kø.StatistikkOppgaveFilter;

class FssExportMapperTest {

    private static final String ENHET = "4806";
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final Behandling BEHANDLING = Behandling.builder(Optional.empty())
        .dummyBehandling(ENHET, BehandlingTilstand.OPPRETTET)
        .medKildeSystem(Fagsystem.FPSAK)
        .build();

    @Test
    void mapToBehandlingDataDto_shouldMapAllFieldsAndEgenskaper() {
        var id = UUID.randomUUID();
        var behandlingsfrist = LocalDate.now().plusDays(7);
        var førsteStønadsdag = LocalDate.now().plusMonths(1);
        var feilutbetalingStart = LocalDate.now().minusDays(30);
        var behandling = Behandling.builder(Optional.empty())
            .medId(id)
            .medSaksnummer(new Saksnummer("987654"))
            .medAktørId(new AktørId("1234567891011"))
            .medKildeSystem(Fagsystem.FPTILBAKE)
            .medFagsakYtelseType(FagsakYtelseType.FORELDREPENGER)
            .medBehandlingType(BehandlingType.KLAGE)
            .medBehandlingTilstand(BehandlingTilstand.VENT_MANUELL)
            .medAktiveAksjonspunkt("5004")
            .medVentefrist(NOW.plusDays(2))
            .medOpprettet(NOW.minusDays(10))
            .medAvsluttet(NOW.minusDays(1))
            .medBehandlingsfrist(behandlingsfrist)
            .medFørsteStønadsdag(førsteStønadsdag)
            .medFeilutbetalingBelop(new BigDecimal("1000"))
            .medFeilutbetalingStart(feilutbetalingStart)
            .medBehandlendeEnhet("4812")
            .medKriterier(Set.of(AndreKriterierType.PAPIRSØKNAD, AndreKriterierType.UTBETALING_TIL_BRUKER))
            .build();
        behandling.setOpprettetAv("VL");
        behandling.setOpprettetTidspunkt(NOW.minusDays(9));
        behandling.setEndretAv("SB");
        behandling.setEndretTidspunkt(NOW.minusDays(1));

        var dto = FssExportMapper.mapToBehandlingDataDto(behandling);

        assertThat(dto.id()).isEqualTo(behandling.getId());
        assertThat(dto.saksnummer().saksnummer()).isEqualTo(behandling.getSaksnummer().getVerdi());
        assertThat(dto.aktørId()).isEqualTo(behandling.getAktørId());
        assertThat(dto.kildeSystem()).isEqualTo(Fagsystem.FPTILBAKE);
        assertThat(dto.fagsakYtelseType()).isEqualTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(dto.behandlingType()).isEqualTo(BehandlingType.KLAGE);
        assertThat(dto.behandlingTilstand()).isEqualTo(BehandlingTilstand.VENT_MANUELL);
        assertThat(dto.aktiveAksjonspunkt()).isEqualTo("5004");
        assertThat(dto.ventefrist()).isEqualTo(NOW.plusDays(2));
        assertThat(dto.opprettet()).isEqualTo(NOW.minusDays(10));
        assertThat(dto.avsluttet()).isEqualTo(NOW.minusDays(1));
        assertThat(dto.behandlingsfrist()).isEqualTo(behandlingsfrist);
        assertThat(dto.førsteStønadsdag()).isEqualTo(førsteStønadsdag);
        assertThat(dto.feilutbetalingBelop()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(dto.feilutbetalingStart()).isEqualTo(feilutbetalingStart);
        assertThat(dto.behandlendeEnhet()).isEqualTo("4812");
        assertThat(dto.opprettetAv()).isEqualTo("VL");
        assertThat(dto.opprettetTidspunkt()).isEqualTo(NOW.minusDays(9));
        assertThat(dto.endretAv()).isEqualTo("SB");
        assertThat(dto.endretTidspunkt()).isEqualTo(NOW.minusDays(1));
        assertThat(dto.egenskaper()).containsExactlyInAnyOrder(
            AndreKriterierType.PAPIRSØKNAD, AndreKriterierType.UTBETALING_TIL_BRUKER);
    }

    @Test
    void mapToBehandlingDataDto_withEmptyEgenskaper_shouldReturnEmptySet() {
        var behandling = Behandling.builder(Optional.empty())
            .dummyBehandling(ENHET, BehandlingTilstand.OPPRETTET)
            .medKildeSystem(Fagsystem.FPSAK)
            .build();

        var dto = FssExportMapper.mapToBehandlingDataDto(behandling);

        assertThat(dto.egenskaper()).isEmpty();
    }

    @Test
    void mapToOppgaveDataDto_shouldMapAllFields() {
        var oppgave = Oppgave.builder()
            .dummyOppgave(ENHET, BEHANDLING)
            .build();
        oppgave.setId(42L);
        oppgave.setOpprettetAv("VL");
        oppgave.setOpprettetTidspunkt(NOW.minusDays(2));
        oppgave.setEndretAv("SB");
        oppgave.setEndretTidspunkt(NOW.minusDays(1));

        var dto = FssExportMapper.mapToOppgaveDataDto(oppgave);

        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.behandlingId()).isEqualTo(oppgave.getBehandlingId().toUUID());
        assertThat(dto.behandlendeEnhet()).isEqualTo(ENHET);
        assertThat(dto.aktiv()).isTrue();
        assertThat(dto.oppgaveAvsluttet()).isNull();
        assertThat(dto.opprettetAv()).isEqualTo("VL");
        assertThat(dto.opprettetTidspunkt()).isEqualTo(NOW.minusDays(2));
        assertThat(dto.endretAv()).isEqualTo("SB");
        assertThat(dto.endretTidspunkt()).isEqualTo(NOW.minusDays(1));
        assertThat(dto.reservasjonDataDto()).isNull();
        assertThat(dto.oppgaveEgenskaper()).isEmpty();
    }

    @Test
    void mapToOppgaveDataDto_withEgenskaper_shouldIncludeThem() {

        var oppgave = Oppgave.builder()
            .dummyOppgave(ENHET, BEHANDLING)
            .medKriterier(Set.of(AndreKriterierType.PAPIRSØKNAD, AndreKriterierType.TIL_BESLUTTER), "z123456")
            .build();

        var dto = FssExportMapper.mapToOppgaveDataDto(oppgave);

        assertThat(dto.oppgaveEgenskaper())
            .extracting(OppgaveEgenskapDataDto::andreKriterierType, OppgaveEgenskapDataDto::sisteSaksbehandlerForTotrinn)
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple(AndreKriterierType.PAPIRSØKNAD, null),
                org.assertj.core.groups.Tuple.tuple(AndreKriterierType.TIL_BESLUTTER, "Z123456")
            );
    }

    @Test
    void mapToOppgaveDataDto_withoutReservasjon_shouldReturnNullReservasjon() {
        var oppgave = Oppgave.builder().dummyOppgave(ENHET, BEHANDLING).build();
        var dto = FssExportMapper.mapToOppgaveDataDto(oppgave);
        assertThat(dto.reservasjonDataDto()).isNull();
    }

    @Test
    void mapToReservasjonDataDto_shouldMapAllFields() {
        var oppgave = Oppgave.builder().dummyOppgave(ENHET, BEHANDLING).build();
        var reservasjon = new Reservasjon(oppgave, "z123456");
        reservasjon.setReservertTil(NOW.plusDays(1));
        reservasjon.setFlyttetAv("z654321");
        reservasjon.setFlyttetTidspunkt(NOW.minusHours(2));
        reservasjon.setBegrunnelse("Flyttet til annen kø");
        reservasjon.setOpprettetAv("VL");
        reservasjon.setOpprettetTidspunkt(NOW.minusDays(3));
        reservasjon.setEndretAv("SB");
        reservasjon.setEndretTidspunkt(NOW.minusDays(1));

        var dto = FssExportMapper.mapToReservasjonDataDto(reservasjon);

        assertThat(dto.reservertTil()).isEqualTo(NOW.plusDays(1));
        assertThat(dto.reservertAv()).isEqualTo("Z123456");
        assertThat(dto.flyttetAv()).isEqualTo("Z654321");
        assertThat(dto.flyttetTidspunkt()).isEqualTo(NOW.minusHours(2));
        assertThat(dto.begrunnelse()).isEqualTo("Flyttet til annen kø");
        assertThat(dto.opprettetAv()).isEqualTo("VL");
        assertThat(dto.opprettetTidspunkt()).isEqualTo(NOW.minusDays(3));
        assertThat(dto.endretAv()).isEqualTo("SB");
        assertThat(dto.endretTidspunkt()).isEqualTo(NOW.minusDays(1));
    }

    @Test
    void mapToAvdelingDataDto_shouldMapAllFields() {
        var avdeling = new Avdeling("4806", "NAV Drammen", false);
        avdeling.setOpprettetAv("VL");
        avdeling.setOpprettetTidspunkt(NOW.minusDays(4));
        avdeling.setEndretAv("SB");
        avdeling.setEndretTidspunkt(NOW.minusDays(1));

        var dto = FssExportMapper.mapToAvdelingDataDto(avdeling);

        assertThat(dto.avdelingEnhet()).isEqualTo("4806");
        assertThat(dto.navn()).isEqualTo("NAV Drammen");
        assertThat(dto.kreverKode6()).isFalse();
        assertThat(dto.aktiv()).isTrue();
        assertThat(dto.opprettetAv()).isEqualTo("VL");
        assertThat(dto.opprettetTidspunkt()).isEqualTo(NOW.minusDays(4));
        assertThat(dto.endretAv()).isEqualTo("SB");
        assertThat(dto.endretTidspunkt()).isEqualTo(NOW.minusDays(1));
    }

    @Test
    void mapToSaksbehandlerDataDto_shouldMapAllFields() {
        var sb = new Saksbehandler("Z999999", "Test Testesen", "4806");
        sb.setOpprettetAv("VL");
        sb.setOpprettetTidspunkt(NOW.minusDays(8));
        sb.setEndretAv("SB");
        sb.setEndretTidspunkt(NOW.minusDays(2));

        var dto = FssExportMapper.mapToSaksbehandlerDataDto(sb);

        assertThat(dto.saksbehandlerIdent()).isEqualTo("Z999999");
        assertThat(dto.navn()).isEqualTo("Test Testesen");
        assertThat(dto.ansattVedEnhet()).isEqualTo("4806");
        assertThat(dto.opprettetAv()).isEqualTo("VL");
        assertThat(dto.opprettetTidspunkt()).isEqualTo(NOW.minusDays(8));
        assertThat(dto.endretAv()).isEqualTo("SB");
        assertThat(dto.endretTidspunkt()).isEqualTo(NOW.minusDays(2));
    }

    @Test
    void mapToSaksbehandlerGruppeDataDto_shouldMapAllFields() {
        var gruppe = new SaksbehandlerGruppe("Testgruppe", new Avdeling("4806", "NAV Drammen", false));
        gruppe.setId(55L);
        gruppe.setOpprettetAv("VL");
        gruppe.setOpprettetTidspunkt(NOW.minusDays(7));
        gruppe.setEndretAv("SB");
        gruppe.setEndretTidspunkt(NOW.minusDays(1));

        var dto = FssExportMapper.mapToSaksbehandlerGruppeDataDto(gruppe);

        assertThat(dto.id()).isEqualTo(55L);
        assertThat(dto.gruppeNavn()).isEqualTo("Testgruppe");
        assertThat(dto.avdelingId()).isEqualTo("4806");
        assertThat(dto.opprettetAv()).isEqualTo("VL");
        assertThat(dto.opprettetTidspunkt()).isEqualTo(NOW.minusDays(7));
        assertThat(dto.endretAv()).isEqualTo("SB");
        assertThat(dto.endretTidspunkt()).isEqualTo(NOW.minusDays(1));
    }

    @Test
    void mapToAvdelingSaksbehandlerDataDto_shouldMapAllFields() {
        var avdeling = new Avdeling("4806", "NAV Drammen", false);
        var saksbehandler = new Saksbehandler("Z123456", "Test Testesen", "4806");
        var relasjon = new AvdelingSaksbehandlerRelasjon(new AvdelingSaksbehandlerNøkkel(saksbehandler, avdeling));

        var dto = FssExportMapper.mapToAvdelingSaksbehandlerDataDto(relasjon);

        assertThat(dto.avdelingId()).isEqualTo("4806");
        assertThat(dto.saksbehandlerId()).isEqualTo("Z123456");
    }

    @Test
    void mapToOppgaveFiltreringDataDto_shouldMapAllFields() {
        var fomDato = LocalDate.now().minusDays(10);
        var tomDato = LocalDate.now().plusDays(10);
        var avdeling = new Avdeling("4806", "NAV Drammen", false);
        var filtrering = new OppgaveFiltrering("Kø A", KøSortering.OPPRETT_BEHANDLING, avdeling);
        filtrering.setId(100L);
        filtrering.setBeskrivelse("Beskrivelse");
        filtrering.setFomDato(fomDato);
        filtrering.setTomDato(tomDato);
        filtrering.setFra(2L);
        filtrering.setTil(8L);
        filtrering.setPeriodefilter(Periodefilter.RELATIV_PERIODE_DAGER);
        filtrering.setFiltreringBehandlingTyper(Set.of(BehandlingType.KLAGE));
        filtrering.setFiltreringYtelseTyper(Set.of(FagsakYtelseType.SVANGERSKAPSPENGER));
        filtrering.setAndreKriterierTyper(Set.of(AndreKriterierType.PAPIRSØKNAD), Set.of(AndreKriterierType.TIL_BESLUTTER));
        filtrering.setOpprettetAv("VL");
        filtrering.setOpprettetTidspunkt(NOW.minusDays(5));
        filtrering.setEndretAv("SB");
        filtrering.setEndretTidspunkt(NOW.minusDays(1));

        var dto = FssExportMapper.mapToOppgaveFiltreringDataDto(filtrering, Map.of(100L, Set.of("Z111111", "Z222222")));

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.navn()).isEqualTo("Kø A");
        assertThat(dto.beskrivelse()).isEqualTo("Beskrivelse");
        assertThat(dto.køSortering()).isEqualTo(KøSortering.OPPRETT_BEHANDLING);
        assertThat(dto.avdelingId()).isEqualTo("4806");
        assertThat(dto.fomDato()).isEqualTo(fomDato);
        assertThat(dto.tomDato()).isEqualTo(tomDato);
        assertThat(dto.fomDager()).isEqualTo(2L);
        assertThat(dto.tomDager()).isEqualTo(8L);
        assertThat(dto.periodeFilter()).isEqualTo(Periodefilter.RELATIV_PERIODE_DAGER);
        assertThat(dto.opprettetAv()).isEqualTo("VL");
        assertThat(dto.opprettetTidspunkt()).isEqualTo(NOW.minusDays(5));
        assertThat(dto.endretAv()).isEqualTo("SB");
        assertThat(dto.endretTidspunkt()).isEqualTo(NOW.minusDays(1));
        assertThat(dto.behandlingTyper()).containsExactly(BehandlingType.KLAGE);
        assertThat(dto.fagsakYtelseTyper()).containsExactly(FagsakYtelseType.SVANGERSKAPSPENGER);
        assertThat(dto.andreKriterier())
            .extracting("andreKriterierType", "inkluder")
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple(AndreKriterierType.PAPIRSØKNAD, true),
                org.assertj.core.groups.Tuple.tuple(AndreKriterierType.TIL_BESLUTTER, false)
            );
        assertThat(dto.saksbehandlerIdenter()).containsExactlyInAnyOrder("Z111111", "Z222222");
    }

    @Test
    void mapToGruppeTilknytningDataDto_shouldMapAllFields() {
        var saksbehandler = new Saksbehandler("Z123456", "Test Testesen", "4806");
        var gruppe = new SaksbehandlerGruppe("Gruppe A", new Avdeling("4806", "NAV Drammen", false));
        gruppe.setId(77L);
        var relasjon = new GruppeTilknytningRelasjon(new GruppeTilknytningNøkkel(saksbehandler, gruppe));

        var dto = FssExportMapper.mapToGruppeTilknytningDataDto(relasjon);

        assertThat(dto.saksbehandlerId()).isEqualTo("Z123456");
        assertThat(dto.gruppeId()).isEqualTo(77L);
    }

    @Test
    void mapToStatEnhetYtelseBehandlingDataDto_shouldMapAllFields() {
        var statistikkDato = LocalDate.now();
        var stat = new StatistikkEnhetYtelseBehandling(
            "4806",
            123L,
            FagsakYtelseType.FORELDREPENGER,
            BehandlingType.FØRSTEGANGSSØKNAD,
            statistikkDato,
            5,
            2,
            1
        );

        var dto = FssExportMapper.mapToStatEnhetYtelseBehandlingDataDto(stat);

        assertThat(dto.behandlendeEnhet()).isEqualTo("4806");
        assertThat(dto.tidsstempel()).isEqualTo(123L);
        assertThat(dto.fagsakYtelseType()).isEqualTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(dto.behandlingType()).isEqualTo(BehandlingType.FØRSTEGANGSSØKNAD);
        assertThat(dto.statistikkDato()).isEqualTo(statistikkDato);
        assertThat(dto.antallAktive()).isEqualTo(5);
        assertThat(dto.antallOpprettet()).isEqualTo(2);
        assertThat(dto.antallAvsluttet()).isEqualTo(1);
    }

    @Test
    void mapToStatOppgaveFilterDataDto_shouldMapAllFields() {
        var statistikkDato = LocalDate.now();
        var stat = new StatistikkOppgaveFilter(
            100L,
            456L,
            statistikkDato,
            8,
            3,
            1,
            2,
            4,
            InnslagType.REGELMESSIG
        );

        var dto = FssExportMapper.mapToStatOppgaveFilterDataDto(stat);

        assertThat(dto.oppgaveFilterId()).isEqualTo(100L);
        assertThat(dto.tidsstempel()).isEqualTo(456L);
        assertThat(dto.statistikkDato()).isEqualTo(statistikkDato);
        assertThat(dto.antallAktive()).isEqualTo(8);
        assertThat(dto.antallTilgjengelige()).isEqualTo(3);
        assertThat(dto.antallVentende()).isEqualTo(1);
        assertThat(dto.antallOpprettet()).isEqualTo(2);
        assertThat(dto.antallAvsluttet()).isEqualTo(4);
        assertThat(dto.innslagType()).isEqualTo(InnslagType.REGELMESSIG);
    }
}

