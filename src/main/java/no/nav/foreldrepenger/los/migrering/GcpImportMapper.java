package no.nav.foreldrepenger.los.migrering;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.migrering.dto.AndreKriterierDataDto;
import no.nav.foreldrepenger.los.migrering.dto.AvdelingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.BehandlingDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveDataDto;
import no.nav.foreldrepenger.los.migrering.dto.OppgaveFiltreringDataDto;
import no.nav.foreldrepenger.los.migrering.dto.ReservasjonDataDto;
import no.nav.foreldrepenger.los.migrering.dto.SaksbehandlerDataDto;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveFiltrering;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;

/**
 * Utility-klasse for mapping av DTOer til entiteter ved GCP-import.
 */
public final class GcpImportMapper {

    private GcpImportMapper() {
        // Utility class
    }

    static Avdeling mapAvdeling(AvdelingDataDto dto) {
        var avdeling = new Avdeling(dto.avdelingEnhet(), dto.navn(), dto.kreverKode6());
        avdeling.setErAktiv(dto.aktiv());
        setBaseEntitetFields(avdeling, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());
        return avdeling;
    }

    static Saksbehandler mapSaksbehandler(SaksbehandlerDataDto dto) {
        var saksbehandler = new Saksbehandler(dto.saksbehandlerIdent(), dto.navn(), dto.ansattVedEnhet());
        setBaseEntitetFields(saksbehandler, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());
        return saksbehandler;
    }

    static void mapBehandling(BehandlingDataDto dto, Behandling behandling) {
        behandling.setId(dto.id()); // Primary key

        if (dto.saksnummer() != null) {
            behandling.setSaksnummer(new Saksnummer(dto.saksnummer().saksnummer()));
        }
        if (dto.aktørId() != null) {
            behandling.setAktørId(dto.aktørId());
        }
        if (dto.kildeSystem() != null) {
            behandling.setFagsystem(dto.kildeSystem());
        }
        if (dto.fagsakYtelseType() != null) {
            behandling.setFagsakYtelseType(dto.fagsakYtelseType());
        }
        if (dto.behandlingType() != null) {
            behandling.setBehandlingType(dto.behandlingType());
        }
        if (dto.behandlingTilstand() != null) {
            behandling.setBehandlingTilstand(dto.behandlingTilstand());
        }

        behandling.setAktiveAksjonspunkt(dto.aktiveAksjonspunkt());
        behandling.setVentefrist(dto.ventefrist());
        behandling.setOpprettet(dto.opprettet());
        behandling.setAvsluttet(dto.avsluttet());
        behandling.setBehandlingsfrist(dto.behandlingsfrist());
        behandling.setFørsteStønadsdag(dto.førsteStønadsdag());
        behandling.setFeilutbetalingBelop(dto.feilutbetalingBelop());
        behandling.setFeilutbetalingStart(dto.feilutbetalingStart());
        behandling.setBehandlendeEnhet(dto.behandlendeEnhet());
        behandling.setKriterier(dto.egenskaper());

        setBaseEntitetFields(behandling, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());
    }

    static Oppgave mapOppgave(OppgaveDataDto dto, Behandling behandling) {
        var oppgave = new Oppgave();
        oppgave.setId(dto.id());
        oppgave.setSaksnummer(new Saksnummer(dto.saksnummer().saksnummer()));
        oppgave.setAktørId(dto.aktørId());
        oppgave.setBehandling(behandling);
        oppgave.setBehandlingType(dto.behandlingType());
        oppgave.setFagsakYtelseType(dto.fagsakYtelseType());
        oppgave.setBehandlendeEnhet(dto.behandlendeEnhet());
        oppgave.setBehandlingsfrist(dto.behandlingsfrist());
        oppgave.setBehandlingOpprettet(dto.behandlingOpprettet());
        oppgave.setFørsteStønadsdag(dto.førsteStønadsdag());
        oppgave.setAktiv(dto.aktiv());
        oppgave.setOppgaveAvsluttet(dto.oppgaveAvsluttet());
        oppgave.setFeilutbetalingBelop(dto.feilutbetalingBelop());
        oppgave.setFeilutbetalingStart(dto.feilutbetalingStart());
        setBaseEntitetFields(oppgave, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());

        oppgave.clearOppgaveEgenskaper();
        if (dto.oppgaveEgenskaper() != null) {
            for (var egenskapDto : dto.oppgaveEgenskaper()) {
                oppgave.leggTilOppgaveEgenskap(egenskapDto.andreKriterierType(), egenskapDto.sisteSaksbehandlerForTotrinn());
            }
        }
        oppgave.setSkipAutoAudit(true);
        return oppgave;
    }

    static void mapReservasjon(Oppgave oppgave, ReservasjonDataDto dto, Reservasjon reservasjon) {
        reservasjon.setOppgave(oppgave);
        reservasjon.setReservertTil(dto.reservertTil());
        reservasjon.setReservertAv(dto.reservertAv());
        reservasjon.setFlyttetAv(dto.flyttetAv());
        reservasjon.setFlyttetTidspunkt(dto.flyttetTidspunkt());
        reservasjon.setBegrunnelse(dto.begrunnelse());
        setBaseEntitetFields(reservasjon, dto.opprettetAv(), dto.opprettetTidspunkt(),
                            dto.endretAv(), dto.endretTidspunkt());
        reservasjon.setSkipAutoAudit(true);
    }

    static OppgaveFiltrering mapOppgaveFiltrering(OppgaveFiltreringDataDto dto, Avdeling avdeling) {
        var filtrering = new OppgaveFiltrering();
        setIdUsingReflection(filtrering, dto.id());
        filtrering.setNavn(dto.navn());
        filtrering.setBeskrivelse(dto.beskrivelse());

        if (dto.køSortering() != null) {
            filtrering.setSortering(dto.køSortering());
        }
        if (avdeling != null) {
            filtrering.setAvdeling(avdeling);
        }

        // Skjermet field not available in OppgaveFiltrering entity TODO: fiks denne
        filtrering.setFomDato(dto.fomDato());
        filtrering.setTomDato(dto.tomDato());

        if (dto.periodeFilter() != null) {
            filtrering.setPeriodefilter(dto.periodeFilter());
        }

        // Handle embedded collections
        filtrering.setFiltreringBehandlingTyper(new HashSet<>(dto.behandlingTyper()));
        filtrering.setFiltreringYtelseTyper(new HashSet<>(dto.fagsakYtelseTyper()));

        // Handle andre kriterier with inkluder/ekskluder separation
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

    // Utility methods for setting private fields using reflection
    static void setIdUsingReflection(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id field", e);
        }
    }

    static void setBaseEntitetFields(BaseEntitet entity, String opprettetAv, LocalDateTime opprettetTid,
                                     String endretAv, LocalDateTime endretTid) {
        if (opprettetAv != null) {
            entity.setOpprettetAv(opprettetAv);
        }
        if (opprettetTid != null) {
            entity.setOpprettetTidspunkt(opprettetTid);
        }
        if (endretAv != null) {
            entity.setEndretAv(endretAv);
        }
        if (endretTid != null) {
            entity.setEndretTidspunkt(endretTid);
        }
    }

}

