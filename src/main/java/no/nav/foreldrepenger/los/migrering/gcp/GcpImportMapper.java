package no.nav.foreldrepenger.los.migrering.gcp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.migrering.dto.AndreKriterierDataDto;
import no.nav.foreldrepenger.los.migrering.dto.AvdelingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveEgenskapDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveFiltreringDataDto;
import no.nav.foreldrepenger.los.migrering.dto.ReservasjonDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerGruppeDataDto;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;

/**
 * Utility-klasse for mapping av DTOer til entiteter ved GCP-import.
 */
public final class GcpImportMapper {

    private GcpImportMapper() {
        // Utility class
    }

    public static Avdeling mapAvdeling(AvdelingDataDto dto) {
        var avdeling = new Avdeling(dto.avdelingEnhet(), dto.navn(), dto.kreverKode6());
        avdeling.setErAktiv(dto.aktiv());
        setBaseEntitetFields(avdeling, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());
        return avdeling;
    }

    public static Saksbehandler mapSaksbehandler(SaksbehandlerDataDto dto) {
        var saksbehandler = new Saksbehandler(dto.saksbehandlerIdent(), dto.navn(), dto.ansattVedEnhet());
        setBaseEntitetFields(saksbehandler, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());
        return saksbehandler;
    }

    public static SaksbehandlerGruppe mapSaksbehandlerGruppe(SaksbehandlerGruppeDataDto dto, Avdeling avdeling) {
        var gruppe = new SaksbehandlerGruppe(dto.gruppeNavn(), avdeling);
        gruppe.setId(dto.id());
        setBaseEntitetFields(gruppe, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());
        return gruppe;
    }

    public static Behandling mapBehandling(BehandlingDataDto dto) {
        var behandling = Behandling.builder(Optional.empty())
            .medId(dto.id())
            .medSaksnummer(new Saksnummer(dto.saksnummer().saksnummer()))
            .medAktørId(dto.aktørId())
            .medBehandlendeEnhet(dto.behandlendeEnhet())
            .medKildeSystem(dto.kildeSystem())
            .medFagsakYtelseType(dto.fagsakYtelseType())
            .medBehandlingType(dto.behandlingType())
            .medBehandlingTilstand(dto.behandlingTilstand())
            .medAktiveAksjonspunkt(dto.aktiveAksjonspunkt())
            .medVentefrist(dto.ventefrist())
            .medOpprettet(dto.opprettet())
            .medAvsluttet(dto.avsluttet())
            .medBehandlingsfrist(dto.behandlingsfrist())
            .medFørsteStønadsdag(dto.førsteStønadsdag())
            .medFeilutbetalingBelop(dto.feilutbetalingBelop())
            .medFeilutbetalingStart(dto.feilutbetalingStart())
            .medKriterier(dto.egenskaper())
            .build();

        setBaseEntitetFields(behandling, dto.opprettetAv(), dto.opprettetTidspunkt(),
            dto.endretAv(), dto.endretTidspunkt());
        return behandling;
    }

    public static Oppgave mapOppgave(OppgaveDataDto dto, Behandling behandlingRef) {
        var oppgave = new Oppgave(behandlingRef, dto.behandlendeEnhet());
        oppgave.setId(dto.id());
        oppgave.setBehandlendeEnhet(dto.behandlendeEnhet());
        oppgave.setAktiv(dto.aktiv());
        oppgave.setOppgaveAvsluttet(dto.oppgaveAvsluttet());
        setBaseEntitetFields(oppgave, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());

        Set<AndreKriterierType> oppgaveEgenskapTyper = dto.oppgaveEgenskaper() == null
            ? Set.of()
            : dto.oppgaveEgenskaper().stream().map(OppgaveEgenskapDataDto::andreKriterierType).collect(Collectors.toSet());
        oppgave.beholdKunOppgaveEgenskaper(oppgaveEgenskapTyper);
        if (dto.oppgaveEgenskaper() != null) {
            for (var egenskapDto : dto.oppgaveEgenskaper()) {
                oppgave.leggTilOppgaveEgenskap(egenskapDto.andreKriterierType(), egenskapDto.sisteSaksbehandlerForTotrinn());
            }
        }
        return oppgave;
    }

    public static void mapReservasjon(ReservasjonDataDto dto, Reservasjon reservasjon, Oppgave oppgaveRef) {
        reservasjon.setOppgave(oppgaveRef);
        reservasjon.setReservertTil(dto.reservertTil());
        reservasjon.setReservertAv(dto.reservertAv());
        reservasjon.setFlyttetAv(dto.flyttetAv());
        reservasjon.setFlyttetTidspunkt(dto.flyttetTidspunkt());
        reservasjon.setBegrunnelse(dto.begrunnelse());
        setBaseEntitetFields(reservasjon, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());
    }

    public static OppgaveFiltrering mapOppgaveFiltrering(OppgaveFiltreringDataDto dto, Avdeling avdeling) {
        var filtrering = new OppgaveFiltrering(dto.navn(), dto.køSortering(), avdeling);
        filtrering.setId(dto.id());
        filtrering.setBeskrivelse(dto.beskrivelse());
        filtrering.setFomDato(dto.fomDato());
        filtrering.setTomDato(dto.tomDato());
        filtrering.setFra(dto.fomDager());
        filtrering.setTil(dto.tomDager());
        filtrering.setPeriodefilter(dto.periodeFilter());
        filtrering.setFiltreringBehandlingTyper(new HashSet<>(dto.behandlingTyper()));
        filtrering.setFiltreringYtelseTyper(new HashSet<>(dto.fagsakYtelseTyper()));

        var inkluderKriterier = dto.andreKriterier().stream()
                .filter(AndreKriterierDataDto::inkluder)
                .map(AndreKriterierDataDto::andreKriterierType)
                .collect(Collectors.toSet());

        var ekskluderKriterier = dto.andreKriterier().stream()
                .filter(ak -> !ak.inkluder())
                .map(AndreKriterierDataDto::andreKriterierType)
                .collect(Collectors.toSet());

        filtrering.setAndreKriterierTyper(inkluderKriterier, ekskluderKriterier);

        setBaseEntitetFields(filtrering, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());
        return filtrering;
    }

    public static void setBaseEntitetFields(BaseEntitet entitet, String opprettetAv, LocalDateTime opprettetTid,
                                     String endretAv, LocalDateTime endretTid) {
        entitet.setOpprettetAv(opprettetAv);
        entitet.setOpprettetTidspunkt(opprettetTid);
        if (endretAv != null) {
            entitet.setEndretAv(endretAv);
        }
        if (endretTid != null) {
            entitet.setEndretTidspunkt(endretTid);
        }
        entitet.setSkipAutoAudit(true);
    }

}

