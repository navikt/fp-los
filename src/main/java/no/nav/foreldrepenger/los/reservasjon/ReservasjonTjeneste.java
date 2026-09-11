package no.nav.foreldrepenger.los.reservasjon;

import static no.nav.foreldrepenger.los.reservasjon.ReservasjonTidspunktUtil.forlengTilNesteUkedag;
import static no.nav.foreldrepenger.los.reservasjon.ReservasjonTidspunktUtil.justerTilNesteUkedag;
import static no.nav.foreldrepenger.los.reservasjon.ReservasjonTidspunktUtil.tomNesteUkedag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingTilstand;
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
        var forlengetTil = forlengTilNesteUkedag(reservasjon.getReservertTil());
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
        var sisteReserverteMetadata = reservasjonRepository.hentSisteReserverteMetadata(BrukerIdent.brukerIdent(), kunAktive);
        var oppgaveIder = sisteReserverteMetadata.stream().map(SisteReserverteMetadata::oppgaveId).toList();
        var oppgaveListe = oppgaveRepository.hentOppgaverReadOnly(oppgaveIder);
        var behandlingTilstandMap = oppgaveListe.stream()
            .map(Oppgave::getBehandling)
            .collect(Collectors.toSet())
            .stream()
            .collect(Collectors.toMap(Behandling::getId, Behandling::getBehandlingTilstand));
        var oppgaveMap = oppgaveListe.stream().collect(Collectors.toMap(Oppgave::getId, Function.identity()));
        return sisteReserverteMetadata.stream().map(mr -> {
            var oppgave = oppgaveMap.get(mr.oppgaveId());
            var status = mapStatus(oppgave, behandlingTilstandMap);
            return new OppgaveBehandlingStatusWrapper(oppgave, status);
        }).toList();

    }

    private static OppgaveBehandlingStatus mapStatus(Oppgave oppgave, Map<UUID, BehandlingTilstand> behandlingTilstandSet) {
        var behandlingTilstand = behandlingTilstandSet.getOrDefault(oppgave.getBehandling().getId(), BehandlingTilstand.INGEN);
        return switch (behandlingTilstand) {
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

    public static Reservasjon standardReservasjon(Oppgave oppgave, String saksbehandler) {
        return reservasjon(oppgave, saksbehandler, tomNesteUkedag(), null);
    }

    public static Reservasjon returFraBeslutterReservasjon(Oppgave oppgave, String saksbehandler) {
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

    private Reservasjon hentReservasjonEllerFeil(Long oppgaveId) {
        return oppgaveRepository.hentReservasjon(oppgaveId)
            .orElseThrow(() -> new IllegalStateException("Fant ikke reservasjon tilknyttet oppgaveId " + oppgaveId));
    }

}
