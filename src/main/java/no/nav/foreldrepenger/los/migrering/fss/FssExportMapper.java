package no.nav.foreldrepenger.los.migrering.fss;

import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.los.migrering.dto.AndreKriterierDataDto;
import no.nav.foreldrepenger.los.migrering.dto.AvdelingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.AvdelingSaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.FiltreringSaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveEgenskapDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveFiltreringDataDto;
import no.nav.foreldrepenger.los.migrering.dto.ReservasjonDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerGruppeDataDto;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgave.OppgaveEgenskap;
import no.nav.foreldrepenger.los.oppgavekø.FiltreringSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.AvdelingSaksbehandlerRelasjon;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.organisasjon.SaksbehandlerGruppe;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;
import no.nav.foreldrepenger.los.tjenester.saksbehandler.oppgave.dto.SaksnummerDto;

/**
 * Stateless mapper for eksport fra FSS-entiteter til migrerins-DTOer.
 * Ingen avhengigheter til EntityManager eller andre tjenester — kan testes isolert.
 */
public final class FssExportMapper {

    private FssExportMapper() {
        // Utility class
    }

    public static BehandlingDataDto mapToBehandlingDataDto(Behandling behandling) {
        return new BehandlingDataDto(
                behandling.getId(),
                new SaksnummerDto(behandling.getSaksnummer().getVerdi()),
                behandling.getAktørId(),
                behandling.getKildeSystem(),
                behandling.getFagsakYtelseType(),
                behandling.getBehandlingType(),
                behandling.getBehandlingTilstand(),
                behandling.getAktiveAksjonspunkt(),
                behandling.getVentefrist(),
                behandling.getOpprettet(),
                behandling.getAvsluttet(),
                behandling.getBehandlingsfrist(),
                behandling.getFørsteStønadsdag(),
                behandling.getFeilutbetalingBelop(),
                behandling.getFeilutbetalingStart(),
                behandling.getBehandlendeEnhet(),
                behandling.getOpprettetAv(),
                behandling.getOpprettetTidspunkt(),
                behandling.getEndretAv(),
                behandling.getEndretTidspunkt(),
                behandling.getKriterier()
        );
    }

    public static OppgaveDataDto mapToOppgaveDataDto(Oppgave oppgave) {
        return new OppgaveDataDto(
                oppgave.getId(),
                oppgave.getSaksnummer() == null ? null : new SaksnummerDto(oppgave.getSaksnummer().getVerdi()),
                oppgave.getAktørId(),
                oppgave.getBehandlingId(),
                oppgave.getBehandlingType(),
                oppgave.getFagsakYtelseType(),
                oppgave.getBehandlendeEnhet(),
                oppgave.getBehandlingsfrist(),
                oppgave.getBehandlingOpprettet(),
                oppgave.getFørsteStønadsdag(),
                oppgave.getAktiv(),
                oppgave.getSystem(),
                oppgave.getOppgaveAvsluttet(),
                oppgave.getFeilutbetalingBelop(),
                oppgave.getFeilutbetalingStart(),
                oppgave.getOpprettetAv(),
                oppgave.getOpprettetTidspunkt(),
                oppgave.getEndretAv(),
                oppgave.getEndretTidspunkt(),
                mapToReservasjonDataDto(oppgave.getReservasjon()),
                mapToOppgaveEgenskapDataDtoList(oppgave.getOppgaveEgenskaper())
        );
    }

    public static List<OppgaveEgenskapDataDto> mapToOppgaveEgenskapDataDtoList(Set<OppgaveEgenskap> egenskaper) {
        if (egenskaper == null || egenskaper.isEmpty()) {
            return List.of();
        }
        return egenskaper.stream()
                .map(FssExportMapper::mapToOppgaveEgenskapDataDto)
                .toList();
    }

    public static OppgaveEgenskapDataDto mapToOppgaveEgenskapDataDto(OppgaveEgenskap egenskap) {
        return new OppgaveEgenskapDataDto(
                egenskap.getAndreKriterierType(),
                egenskap.getSisteSaksbehandlerForTotrinn()
        );
    }

    public static ReservasjonDataDto mapToReservasjonDataDto(Reservasjon reservasjon) {
        if (reservasjon == null) {
            return null;
        }
        return new ReservasjonDataDto(
                reservasjon.getOppgave().getId(),
                reservasjon.getReservertTil(),
                reservasjon.getReservertAv(),
                reservasjon.getFlyttetAv(),
                reservasjon.getFlyttetTidspunkt(),
                reservasjon.getBegrunnelse(),
                reservasjon.getOpprettetAv(),
                reservasjon.getOpprettetTidspunkt(),
                reservasjon.getEndretAv(),
                reservasjon.getEndretTidspunkt()
        );
    }

    public static AvdelingDataDto mapToAvdelingDataDto(Avdeling avdeling) {
        return new AvdelingDataDto(
                avdeling.getAvdelingEnhet(),
                avdeling.getNavn(),
                avdeling.getKreverKode6(),
                avdeling.getErAktiv(),
                avdeling.getOpprettetAv(),
                avdeling.getOpprettetTidspunkt(),
                avdeling.getEndretAv(),
                avdeling.getEndretTidspunkt()
        );
    }

    public static SaksbehandlerDataDto mapToSaksbehandlerDataDto(Saksbehandler saksbehandler) {
        return new SaksbehandlerDataDto(
                saksbehandler.getSaksbehandlerIdent(),
                saksbehandler.getNavn(),
                saksbehandler.getAnsattVedEnhet(),
                saksbehandler.getOpprettetAv(),
                saksbehandler.getOpprettetTidspunkt(),
                saksbehandler.getEndretAv(),
                saksbehandler.getEndretTidspunkt()
        );
    }

    public static AvdelingSaksbehandlerDataDto mapToAvdelingSaksbehandlerDataDto(AvdelingSaksbehandlerRelasjon as) {
        return new AvdelingSaksbehandlerDataDto(
                as.getAvdeling().getAvdelingEnhet(),
                as.getSaksbehandler().getSaksbehandlerIdent()
        );
    }

    public static SaksbehandlerGruppeDataDto mapToSaksbehandlerGruppeDataDto(SaksbehandlerGruppe sg) {
        return new SaksbehandlerGruppeDataDto(
                sg.getId(),
                sg.getGruppeNavn(),
                sg.getAvdeling().getAvdelingEnhet(),
                sg.getOpprettetAv(),
                sg.getOpprettetTidspunkt(),
                sg.getEndretAv(),
                sg.getEndretTidspunkt()
        );
    }

    public static FiltreringSaksbehandlerDataDto mapToFiltreringSaksbehandlerDataDto(FiltreringSaksbehandlerRelasjon r) {
        return new FiltreringSaksbehandlerDataDto(
                r.getSaksbehandler().getSaksbehandlerIdent(),
                r.getOppgaveFiltrering().getId()
        );
    }

    public static OppgaveFiltreringDataDto mapToOppgaveFiltreringDataDto(OppgaveFiltrering of) {
        var behandlingTyper = of.getBehandlingTyper();
        var fagsakYtelseTyper = of.getFagsakYtelseTyper();

        var andreKriterier = of.getFiltreringAndreKriterierTyper().stream()
                .map(ak -> new AndreKriterierDataDto(
                        ak.getAndreKriterierType(),
                        ak.isInkluder()
                ))
                .toList();

        return new OppgaveFiltreringDataDto(
                of.getId(),
                of.getNavn(),
                of.getBeskrivelse(),
                of.getSortering(),
                of.getAvdeling().getAvdelingEnhet(),
                of.getFomDato(),
                of.getTomDato(),
                of.getPeriodefilter(),
                of.getOpprettetAv(),
                of.getOpprettetTidspunkt(),
                of.getEndretAv(),
                of.getEndretTidspunkt(),
                behandlingTyper,
                fagsakYtelseTyper,
                andreKriterier
        );
    }
}

