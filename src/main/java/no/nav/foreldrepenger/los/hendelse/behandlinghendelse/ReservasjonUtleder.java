package no.nav.foreldrepenger.los.hendelse.behandlinghendelse;

import java.time.LocalDateTime;
import java.util.Optional;

import no.nav.foreldrepenger.los.felles.util.BrukerIdent;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;
import no.nav.foreldrepenger.los.reservasjon.ReservasjonKonstanter;
import no.nav.foreldrepenger.los.reservasjon.ReservasjonTidspunktUtil;

import static no.nav.foreldrepenger.los.reservasjon.ReservasjonTidspunktUtil.tomNesteUkedag;

class ReservasjonUtleder {

    private ReservasjonUtleder() {
        //CDI
    }

    static Optional<Reservasjon> utledReservasjon(Oppgave nyOppgave, Optional<Oppgave> eksisterendeOppgaveOpt,
                                                  boolean reservasjonskandidat, OppgaveGrunnlag oppgaveGrunnlag) {
        return utledReservasjon(nyOppgave, eksisterendeOppgaveOpt, Optional.empty(), reservasjonskandidat, oppgaveGrunnlag);
    }

    static Optional<Reservasjon> utledReservasjon(Oppgave nyOppgave, Optional<Oppgave> eksisterendeOppgaveOpt,
                                                  Optional<Reservasjon> eksisterendeReservasjonOpt,
                                                  boolean reservasjonskandidat, OppgaveGrunnlag oppgaveGrunnlag) {
        if (eksisterendeOppgaveOpt.isPresent()) {
            var eksisterendeOppgave = eksisterendeOppgaveOpt.get();
            if (harEndretEnhet(oppgaveGrunnlag, eksisterendeOppgave)) {
                return Optional.empty();
            }
            if (erReturFraBeslutter(nyOppgave, eksisterendeOppgave)) {
                return Optional.of(returFraBeslutterReservasjon(nyOppgave, oppgaveGrunnlag.ansvarligSaksbehandlerIdent()));
            }
            if (eksisterendeReservasjonOpt.filter(Reservasjon::erAktiv).isPresent()) {
                if (eksisterendeOppgave.harKriterie(AndreKriterierType.PAPIRSØKNAD) && !nyOppgave.harKriterie(AndreKriterierType.PAPIRSØKNAD)) {
                    return Optional.empty();
                }
                if (nyOppgave.harKriterie(AndreKriterierType.TIL_BESLUTTER) && !eksisterendeOppgave.harKriterie(AndreKriterierType.TIL_BESLUTTER)) {
                    return Optional.empty();
                }
                return Optional.of(videreførReservasjonTilNyOppgave(eksisterendeReservasjonOpt.orElseThrow(), nyOppgave));
            }
            return Optional.empty();
        }
        if (oppgaveGrunnlag.ansvarligSaksbehandlerIdent() != null && reservasjonskandidat) {
            return Optional.of(standardReservasjon(nyOppgave, oppgaveGrunnlag.ansvarligSaksbehandlerIdent()));
        }
        return Optional.empty();
    }

    public static boolean erReservasjonskandidat(OppgaveGrunnlag oppgaveGrunnlag, Optional<Behandling> lagretBehandling) {
        return erPåVent(lagretBehandling) || erNyManuellRevurdering(oppgaveGrunnlag, lagretBehandling);
    }

    private static boolean erPåVent(Optional<Behandling> lagretBehandling) {
        return lagretBehandling.stream().anyMatch(behandling -> behandling.getBehandlingTilstand().erPåVent());
    }

    private static boolean erNyManuellRevurdering(OppgaveGrunnlag oppgaveGrunnlag, Optional<Behandling> lagretBehandling) {
        return lagretBehandling.isEmpty() && erManuellRevurdering(oppgaveGrunnlag);
    }

    private static boolean erManuellRevurdering(OppgaveGrunnlag oppgaveGrunnlag) {
        if (BehandlingType.TILBAKEBETALING_REVURDERING == oppgaveGrunnlag.behandlingstype()) {
            //Fptilbake behandlinger har ikke behandlingsårsaker, eneste måten å opprette en revurdering på er manuell saksbehandling
            return true;
        }
        return oppgaveGrunnlag.behandlingstype() == BehandlingType.REVURDERING && oppgaveGrunnlag.behandlingsårsaker()
            .contains(OppgaveGrunnlag.Behandlingsårsak.MANUELL);
    }

    private static boolean erReturFraBeslutter(Oppgave nyOppgave, Oppgave eksisterendeOppgave) {
        return eksisterendeOppgave.harKriterie(AndreKriterierType.TIL_BESLUTTER) && nyOppgave.harKriterie(AndreKriterierType.RETURNERT_FRA_BESLUTTER);
    }

    private static boolean harEndretEnhet(OppgaveGrunnlag oppgaveGrunnlag, Oppgave eksisterendeOppgave) {
        return !eksisterendeOppgave.getBehandlendeEnhet().equals(oppgaveGrunnlag.behandlendeEnhetId());
    }

    private static Reservasjon videreførReservasjonTilNyOppgave(Reservasjon eksisterendeReservasjon, Oppgave nyOppgave) {
        var reservasjon = new Reservasjon(nyOppgave, eksisterendeReservasjon.getReservertAv());
        reservasjon.setReservertTil(eksisterendeReservasjon.getReservertTil());
        reservasjon.setFlyttetAv(eksisterendeReservasjon.getFlyttetAv());
        reservasjon.setBegrunnelse(eksisterendeReservasjon.getBegrunnelse());
        reservasjon.setFlyttetTidspunkt(eksisterendeReservasjon.getFlyttetTidspunkt());
        return reservasjon;
    }

    private static Reservasjon standardReservasjon(Oppgave oppgave, String saksbehandler) {
        return reservasjon(oppgave, saksbehandler, tomNesteUkedag(), null);
    }

    private static Reservasjon returFraBeslutterReservasjon(Oppgave oppgave, String saksbehandler) {
        return reservasjon(oppgave, saksbehandler, ReservasjonTidspunktUtil.tomSjuDagerFremJustertTilNesteUkedag(), ReservasjonKonstanter.RETUR_FRA_BESLUTTER);
    }

    private static Reservasjon reservasjon(Oppgave oppgave, String saksbehandler, LocalDateTime reservertTil, String begrunnelse) {
        var reservasjon = new Reservasjon(oppgave, saksbehandler);
        reservasjon.setReservertTil(reservertTil);
        reservasjon.setFlyttetAv(BrukerIdent.brukerIdentEllerDefault());
        reservasjon.setFlyttetTidspunkt(LocalDateTime.now());
        reservasjon.setBegrunnelse(begrunnelse);
        return reservasjon;
    }
}
