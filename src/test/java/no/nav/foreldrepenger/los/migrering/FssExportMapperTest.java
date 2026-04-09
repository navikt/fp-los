package no.nav.foreldrepenger.los.migrering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.migrering.fss.FssExportMapper;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingTilstand;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;

class FssExportMapperTest {

    private static final String ENHET = "4806";
    private static final Behandling behandling = new Behandling();

    static {
        behandling.setId(UUID.randomUUID());
        behandling.setBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD);
        behandling.setFagsakYtelseType(FagsakYtelseType.FORELDREPENGER);
        behandling.setSaksnummer(new Saksnummer("123456"));
        behandling.setFagsystem(Fagsystem.FPSAK);
    }

    @Test
    void mapToBehandlingDataDto_shouldMapAllFieldsAndEgenskaper() {
        var behandling = Behandling.builder(Optional.empty())
            .dummyBehandling(ENHET, BehandlingTilstand.OPPRETTET)
            .medKildeSystem(Fagsystem.FPSAK)
            .medKriterier(Set.of(AndreKriterierType.PAPIRSØKNAD, AndreKriterierType.UTBETALING_TIL_BRUKER))
            .build();

        var dto = FssExportMapper.mapToBehandlingDataDto(behandling);

        assertThat(dto.id()).isEqualTo(behandling.getId());
        assertThat(dto.saksnummer().saksnummer()).isEqualTo(behandling.getSaksnummer().getVerdi());
        assertThat(dto.kildeSystem()).isEqualTo(Fagsystem.FPSAK);
        assertThat(dto.behandlingType()).isEqualTo(BehandlingType.FØRSTEGANGSSØKNAD);
        assertThat(dto.behandlingTilstand()).isEqualTo(BehandlingTilstand.OPPRETTET);
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
            .dummyOppgave(ENHET, behandling)
            .build();

        var dto = FssExportMapper.mapToOppgaveDataDto(oppgave);

        assertThat(dto.behandlingId()).isEqualTo(oppgave.getBehandlingId().toUUID());
        assertThat(dto.behandlendeEnhet()).isEqualTo(ENHET);
        assertThat(dto.aktiv()).isTrue();
        assertThat(dto.oppgaveAvsluttet()).isNull();
    }

    @Test
    void mapToOppgaveDataDto_withEgenskaper_shouldIncludeThem() {

        var oppgave = Oppgave.builder()
            .dummyOppgave(ENHET, behandling)
            .medKriterier(Set.of(AndreKriterierType.PAPIRSØKNAD), null)
            .build();

        var dto = FssExportMapper.mapToOppgaveDataDto(oppgave);

        assertThat(dto.oppgaveEgenskaper()).hasSize(1);
        assertThat(dto.oppgaveEgenskaper().getFirst().andreKriterierType()).isEqualTo(AndreKriterierType.PAPIRSØKNAD);
    }

    @Test
    void mapToOppgaveDataDto_withoutReservasjon_shouldReturnNullReservasjon() {
        var oppgave = Oppgave.builder().dummyOppgave(ENHET, behandling).build();
        var dto = FssExportMapper.mapToOppgaveDataDto(oppgave);
        assertThat(dto.reservasjonDataDto()).isNull();
    }

    @Test
    void mapToAvdelingDataDto_shouldMapAllFields() {
        var avdeling = new Avdeling("4806", "NAV Drammen", false);

        var dto = FssExportMapper.mapToAvdelingDataDto(avdeling);

        assertThat(dto.avdelingEnhet()).isEqualTo("4806");
        assertThat(dto.navn()).isEqualTo("NAV Drammen");
        assertThat(dto.kreverKode6()).isFalse();
        assertThat(dto.aktiv()).isTrue();
    }

    @Test
    void mapToSaksbehandlerDataDto_shouldMapAllFields() {
        var sb = new Saksbehandler("Z999999", "Test Testesen", "4806");

        var dto = FssExportMapper.mapToSaksbehandlerDataDto(sb);

        assertThat(dto.saksbehandlerIdent()).isEqualTo("Z999999");
        assertThat(dto.navn()).isEqualTo("Test Testesen");
        assertThat(dto.ansattVedEnhet()).isEqualTo("4806");
    }

    @Test
    void mapToSaksbehandlerGruppeDataDto_shouldMapAllFields() {
        var gruppe = new SaksbehandlerGruppe("Testgruppe");
        var avdeling = new Avdeling("4806", "NAV Drammen", false);
        gruppe.setAvdeling(avdeling);

        var dto = FssExportMapper.mapToSaksbehandlerGruppeDataDto(gruppe);

        assertThat(dto.gruppeNavn()).isEqualTo("Testgruppe");
        assertThat(dto.avdelingId()).isEqualTo("4806");
    }
}

