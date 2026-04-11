package no.nav.foreldrepenger.los.migrering;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.aktør.AktørId;
import no.nav.foreldrepenger.los.migrering.dto.AndreKriterierDataDto;
import no.nav.foreldrepenger.los.migrering.dto.AvdelingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.AvdelingSaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;
import no.nav.foreldrepenger.los.migrering.dto.GruppeTilknytningDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveEgenskapDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveFiltreringDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OrgDataDto;
import no.nav.foreldrepenger.los.migrering.dto.ReservasjonDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerGruppeDataDto;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.BehandlingTilstand;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.oppgave.Periodefilter;
import no.nav.foreldrepenger.los.oppgavekø.KøSortering;
import no.nav.foreldrepenger.los.tjenester.saksbehandler.oppgave.dto.SaksnummerDto;

/**
 * Test data builder for migrering DTOs.
 * Builds realistic BulkDataWrapper instances usable by all migration tests.
 */
public final class TestMigreringData {

    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final String ENHET_DRAMMEN = "4806";

    private TestMigreringData() {
    }

    public static AvdelingDataDto lagAvdelingDataDto(String enhet, String navn) {
        return new AvdelingDataDto(enhet, navn, false, true,
            "VL", NOW.minusDays(30), "VL", NOW);
    }

    public static SaksbehandlerDataDto lagSaksbehandlerDataDto(String ident) {
        return new SaksbehandlerDataDto(ident, "Saksbehandler " + ident, ENHET_DRAMMEN,
            "VL", NOW.minusDays(30), "VL", NOW);
    }

    public static SaksbehandlerGruppeDataDto lagSaksbehandlerGruppeDataDto(Long id, String navn, String enhetsnummer) {
        return new SaksbehandlerGruppeDataDto(id, navn, enhetsnummer,
            "VL", NOW.minusDays(10), "VL", NOW);
    }

    public static BehandlingDataDto lagBehandlingDataDto(UUID id) {
        return new BehandlingDataDto(
            id,
            new SaksnummerDto("123456"),
            new AktørId("9999999999999"),
            Fagsystem.FPSAK,
            FagsakYtelseType.FORELDREPENGER,
            BehandlingType.FØRSTEGANGSSØKNAD,
            BehandlingTilstand.OPPRETTET,
            null,
            null,
            NOW.minusDays(5),
            null,
            LocalDate.now().plusDays(30),
            LocalDate.now().plusMonths(3),
            null,
            null,
            ENHET_DRAMMEN,
            "VL", NOW.minusDays(5),
            "VL", NOW,
            Set.of(AndreKriterierType.PAPIRSØKNAD)
        );
    }

    public static OppgaveDataDto lagOppgaveDataDto(Long id) {
        return lagOppgaveDataDto(id, true, null);
    }

    public static OppgaveDataDto lagOppgaveDataDto(Long id, boolean aktiv, ReservasjonDataDto reservasjon) {
        var behandlingId = UUID.nameUUIDFromBytes(("oppgave-" + id).getBytes());
        return new OppgaveDataDto(
            id,
            behandlingId,
            ENHET_DRAMMEN,
            aktiv,
            aktiv ? null : NOW,
            "VL", NOW.minusDays(5),
            "VL", NOW,
            reservasjon,
            List.of(new OppgaveEgenskapDataDto(AndreKriterierType.PAPIRSØKNAD, null))
        );
    }

    public static ReservasjonDataDto lagReservasjonDataDto() {
        return new ReservasjonDataDto(
            NOW.plusDays(1),
            "Z999999",
            null, null, null,
            "VL", NOW,
            "VL", NOW
        );
    }

    public static OppgaveFiltreringDataDto lagOppgaveFiltreringDataDto(Long id, String enhetsnummer, Set<String> saksbehandlerIdenter) {
        return new OppgaveFiltreringDataDto(
            id, "Testkø " + id, "En testkø",
            KøSortering.BEHANDLINGSFRIST,
            enhetsnummer,
            LocalDate.now().minusDays(30),
            LocalDate.now().plusDays(30),
            null,
            null,
            Periodefilter.FAST_PERIODE,
            "VL", NOW.minusDays(10),
            "VL", NOW,
            List.of(BehandlingType.FØRSTEGANGSSØKNAD),
            List.of(FagsakYtelseType.FORELDREPENGER),
            List.of(new AndreKriterierDataDto(AndreKriterierType.PAPIRSØKNAD, true)), saksbehandlerIdenter);
    }

    public static BulkDataWrapper lagOrganisasjonOgKøer() {
        var avdeling = lagAvdelingDataDto(ENHET_DRAMMEN, "NAV Drammen");
        var saksbehandler = lagSaksbehandlerDataDto("Z999999");
        var gruppe = lagSaksbehandlerGruppeDataDto(5_000_003L, "Testgruppe", ENHET_DRAMMEN);
        var avdelingSb = new AvdelingSaksbehandlerDataDto(ENHET_DRAMMEN, "Z999999");

        var orgData = new OrgDataDto(
            List.of(avdeling),
            List.of(saksbehandler),
            List.of(avdelingSb),
            List.of(gruppe),
            List.of(new GruppeTilknytningDataDto("Z999999", 5_000_003L))
        );

        var kø = lagOppgaveFiltreringDataDto(5_000_010L, ENHET_DRAMMEN, Set.of("Z999999"));

        return BulkDataWrapper.organisasjonOgKøOppset(orgData, List.of(kø));
    }

    public static BulkDataWrapper lagAktiveOppgaver() {
        var oppgave1 = lagOppgaveDataDto(5_000_100L);
        var oppgave2 = lagOppgaveDataDto(5_000_101L, true,
            lagReservasjonDataDto());
        return BulkDataWrapper.aktiveOppgaver(List.of(oppgave1, oppgave2));
    }

    public static BulkDataWrapper lagBehandlinger() {
        var b1 = lagBehandlingDataDto(UUID.nameUUIDFromBytes("behandling-1".getBytes()));
        var b2 = lagBehandlingDataDto(UUID.nameUUIDFromBytes("behandling-2".getBytes()));
        return BulkDataWrapper.behandlinger(List.of(b1, b2));
    }

    public static BulkDataWrapper lagBehandlinger(BulkDataWrapper bulkData) {
        var behandlinger = bulkData.aktiveOppgaver()
            .stream()
            .map(o -> lagBehandlingDataDto(o.behandlingId()))
            .toList();
        return BulkDataWrapper.leggTilBehandlinger(bulkData, behandlinger);
    }

}

