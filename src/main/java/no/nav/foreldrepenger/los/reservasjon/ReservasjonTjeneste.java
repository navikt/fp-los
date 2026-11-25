package no.nav.foreldrepenger.los.reservasjon;

import static no.nav.foreldrepenger.los.reservasjon.ReservasjonTidspunktUtil.justerTilNesteUkedag;
import static no.nav.foreldrepenger.los.reservasjon.ReservasjonTidspunktUtil.tomNesteUkedag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import no.nav.foreldrepenger.los.felles.util.BrukerIdent;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgave.OppgaveRepository;
import no.nav.foreldrepenger.los.tjenester.felles.dto.OppgaveBehandlingStatus;


@ApplicationScoped
public class ReservasjonTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ReservasjonTjeneste.class);

    private OppgaveRepository oppgaveRepository;
    private ReservasjonRepository reservasjonRepository;

    @Inject
    public ReservasjonTjeneste(OppgaveRepository oppgaveRepository, ReservasjonRepository reservasjonRepository) {
        this.oppgaveRepository = oppgaveRepository;
        this.reservasjonRepository = reservasjonRepository;
    }

    public ReservasjonTjeneste() {
    }

    public List<Oppgave> hentSaksbehandlersReserverteAktiveOppgaver() {
        return reservasjonRepository.hentSaksbehandlersReserverteAktiveOppgaver(BrukerIdent.brukerIdent());
    }

    public List<Reservasjon> hentReservasjonerForAvdeling(String avdelingEnhet) {
        return reservasjonRepository.hentAlleReservasjonerForAvdeling(avdelingEnhet);
    }

    public Reservasjon reserverOppgave(Oppgave oppgave) {
        LOG.info("Reserverer oppgave {}", oppgave.getId());
        var reservasjon = oppgaveRepository.hentReservasjon(oppgave.getId()).map((r -> {
            r.setFlyttetTidspunkt(null);
            r.setBegrunnelse(null);
            r.setFlyttetAv(null);
            return r;
        })).orElseGet(() -> new Reservasjon(oppgave));
        if (reservasjon.erAktiv()) {
            LOG.info("Fant aktiv reservasjon for oppgave {} reservasjon {}", oppgave.getId(), reservasjon.getReservertAv());
        } else {
            LOG.info("Fant ikke aktiv reservasjon for oppgave {}", oppgave.getId());
            reservasjon.setReservertTil(tomNesteUkedag());
            reservasjon.setReservertAv(BrukerIdent.brukerIdent());
            try {
                oppgaveRepository.lagre(reservasjon);
                oppgaveRepository.refresh(reservasjon.getOppgave());
            } catch (OptimisticLockException e) {
                // Annen saksbehandler har oppdatert reservasjonen – refresher reservasjonen og returnerer den som om reservasjon ellers var vellykket
                // Frontend vil vise modal om at annen saksbehandler holder reservasjonen.
                oppgaveRepository.refresh(reservasjon);
                LOG.info("Reservasjon feilet: annen saksbehandler {} har oppdatert reservasjon", reservasjon.getReservertAv());
            } catch (PersistenceException e) {
                // Sannsynligvis har annen saksbehandler laget ny reservasjon.
                // Ignorerer feil ettersom ReservasjonDto til frontend vil vise modal
                oppgaveRepository.refresh(oppgave);
                LOG.info("Reservasjon feilet", e);
            }
        }
        return reservasjon;
    }

    public Optional<Reservasjon> slettReservasjon(Long oppgaveId) {
        var reservasjon = reservasjonRepository.hentAktivReservasjon(oppgaveId);
        reservasjon.ifPresentOrElse(this::slettReservasjon,
            () -> LOG.info("Forsøker slette reservasjon, men fant ingen for oppgaveId {}", oppgaveId));
        return reservasjon;
    }

    public void slettReservasjon(Reservasjon reservasjon) {
        if (reservasjon != null && reservasjon.erAktiv()) {
            reservasjon.setReservertTil(LocalDateTime.now().minusSeconds(1));
            reservasjonRepository.lagre(reservasjon);
        }
    }

    public Reservasjon flyttReservasjon(Long oppgaveId, String brukernavn, String begrunnelse) {
        var reservasjon = hentReservasjonEllerFeil(oppgaveId);
        var forlengetTil = justerTilNesteUkedag(reservasjon.getReservertTil().plusDays(1));
        reservasjon.setReservertTil(forlengetTil);
        reservasjon.setReservertAv(brukernavn);
        reservasjon.setFlyttetAv(BrukerIdent.brukerIdentEllerDefault());
        reservasjon.setFlyttetTidspunkt(LocalDateTime.now());
        reservasjon.setBegrunnelse(begrunnelse);
        oppgaveRepository.lagre(reservasjon);
        oppgaveRepository.refresh(reservasjon.getOppgave());
        return reservasjon;
    }

    public Reservasjon endreReservasjonsdato(Long oppgaveId, LocalDate reservertTil) {
        var reservasjon = hentReservasjonEllerFeil(oppgaveId);
        reservasjon.setReservertTil(justerTilNesteUkedag(reservertTil.atStartOfDay()));
        reservasjonRepository.lagre(reservasjon);
        return reservasjon;
    }

    public List<OppgaveBehandlingStatusWrapper> hentSaksbehandlersSisteReserverteMedStatus(boolean kunAktive) {
        var sorterteOppgaveIder = reservasjonRepository.hentNyesteOppgaveIderForSisteReserverteBehandlinger(BrukerIdent.brukerIdent(), kunAktive);
        var usorterteOppgaver = oppgaveRepository.hentOppgaverReadOnly(sorterteOppgaveIder);
        var oppgaveMap = usorterteOppgaver.stream().collect(Collectors.toMap(Oppgave::getId, Function.identity()));
        return sorterteOppgaveIder.stream().map(oppgaveId -> {
            var oppgave = oppgaveMap.get(oppgaveId);
            var status = mapStatus(oppgave);
            return new OppgaveBehandlingStatusWrapper(oppgave, status);
        }).toList();

    }

    private static OppgaveBehandlingStatus mapStatus(Oppgave oppgave) {
        return switch (oppgave.getBehandling().getBehandlingTilstand()) {
            case AKSJONSPUNKT -> {
                var erReturnertFraBeslutter = oppgave.getOppgaveEgenskaper()
                    .stream()
                    .anyMatch(egenskap -> AndreKriterierType.RETURNERT_FRA_BESLUTTER.equals(egenskap.andreKriterierType()));
                yield erReturnertFraBeslutter ? OppgaveBehandlingStatus.RETURNERT_FRA_BESLUTTER : OppgaveBehandlingStatus.UNDER_ARBEID;
            }
            case OPPRETTET, INGEN, PAPIRSØKNAD -> OppgaveBehandlingStatus.UNDER_ARBEID;
            case VENT_TIDLIG, VENT_KOMPLETT, VENT_REGISTERDATA, VENT_KLAGEINSTANS, VENT_KØ, VENT_MANUELL, VENT_SØKNAD ->
                OppgaveBehandlingStatus.PÅ_VENT;
            case BESLUTTER -> OppgaveBehandlingStatus.TIL_BESLUTTER;
            case AVSLUTTET -> OppgaveBehandlingStatus.FERDIG;
        };
    }

    private Reservasjon hentReservasjonEllerFeil(Long oppgaveId) {
        return oppgaveRepository.hentReservasjon(oppgaveId)
            .orElseThrow(() -> new IllegalStateException("Fant ikke reservasjon tilknyttet oppgaveId " + oppgaveId));
    }

}
