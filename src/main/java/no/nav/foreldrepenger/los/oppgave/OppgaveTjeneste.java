package no.nav.foreldrepenger.los.oppgave;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.los.domene.typer.BehandlingId;
import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.reservasjon.ReservasjonTjeneste;

@ApplicationScoped
public class OppgaveTjeneste {

    private ReservasjonTjeneste reservasjonTjeneste;
    private OppgaveRepository oppgaveRepository;

    @Inject
    public OppgaveTjeneste(OppgaveRepository oppgaveRepository, ReservasjonTjeneste reservasjonTjeneste) {
        this.oppgaveRepository = oppgaveRepository;
        this.reservasjonTjeneste = reservasjonTjeneste;
    }

    OppgaveTjeneste() {
    }

    public List<Oppgave> hentAktiveOppgaverForSaksnummer(Collection<Saksnummer> saksnummerListe) {
        return oppgaveRepository.hentAktiveOppgaverForSaksnummer(saksnummerListe);
    }

    public boolean erAlleOppgaverFortsattTilgjengelig(List<Long> oppgaveIder) {
        return oppgaveRepository.sjekkOmOppgaverFortsattErTilgjengelige(oppgaveIder);
    }

    public Oppgave hentOppgave(Long oppgaveId) {
        return oppgaveRepository.hentOppgave(oppgaveId);
    }

    public Oppgave hentSkrivelåstOppgave(Long oppgaveId) {
        return oppgaveRepository.hentSkrivelåstOppgave(oppgaveId);
    }

    public void adminAvsluttMultiOppgaveAvsluttTilknyttetReservasjon(BehandlingId behandlingId) {
        var oppgaver = oppgaveRepository.hentOppgaver(behandlingId);
        var antallAktive = oppgaver.stream().filter(Oppgave::getAktiv).count();
        if (antallAktive <= 1) {
            throw new IllegalStateException(
                String.format("Forventet mer enn én aktiv oppgave for behandlingId %s, fant %s", behandlingId, antallAktive));
        }
        oppgaver.stream().filter(Oppgave::getAktiv).min(Comparator.comparing(Oppgave::getOpprettetTidspunkt)).ifPresent(oppgave -> {
            oppgaveRepository.hentReservasjon(oppgave.getId()).ifPresent(reservasjonTjeneste::slettReservasjon);
            oppgave.avsluttOppgave();
            oppgaveRepository.lagre(oppgave);
        });
    }

}
