package no.nav.foreldrepenger.los.hendelse.behandlinghendelse;

import static no.nav.foreldrepenger.los.hendelse.behandlinghendelse.BehandlingTjeneste.mapAktiveAksjonspunkt;
import static no.nav.foreldrepenger.los.hendelse.behandlinghendelse.OppgaveGrunnlag.AksjonspunktType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.los.beskyttelsesbehov.Beskyttelsesbehov;
import no.nav.foreldrepenger.los.domene.typer.BehandlingId;
import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.hendelse.behandlinghendelse.OppgaveGrunnlag.BehandlingStatus;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgave.OppgaveRepository;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;
import no.nav.foreldrepenger.los.reservasjon.ReservasjonRepository;
import no.nav.vedtak.hendelser.behandling.Aksjonspunktstatus;
import no.nav.vedtak.hendelser.behandling.los.LosBehandlingDto;
import no.nav.vedtak.hendelser.behandling.los.LosFagsakEgenskaperDto;

@ApplicationScoped
public class BehandlingHendelseTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(BehandlingHendelseTjeneste.class);

    private final BehandlingTjeneste behandlingTjeneste;
    private final OppgaveRepository oppgaveRepository;
    private final ReservasjonRepository reservasjonRepository;
    private final Beskyttelsesbehov beskyttelsesbehov;

    @Inject
    BehandlingHendelseTjeneste(BehandlingTjeneste behandlingTjeneste,
                               OppgaveRepository oppgaveRepository,
                               ReservasjonRepository reservasjonRepository,
                               Beskyttelsesbehov beskyttelsesbehov) {
        this.behandlingTjeneste = behandlingTjeneste;
        this.oppgaveRepository = oppgaveRepository;
        this.reservasjonRepository = reservasjonRepository;
        this.beskyttelsesbehov = beskyttelsesbehov;
    }

    public void lagreBehandlingOppdaterOppgaver(LosBehandlingDto dto, LosFagsakEgenskaperDto egenskaperDto, Fagsystem kilde) {
        var beskyttelseKriterier = beskyttelsesbehov.getBeskyttelsesKriterier(new Saksnummer(dto.saksnummer()));

        // Hent eksisterende oppgave og behandling
        var eksisterendeOppgave = oppgaveRepository.hentAktivOppgave(new BehandlingId(dto.behandlingUuid()));
        var eksisterendeBehandling = oppgaveRepository.finnBehandling(dto.behandlingUuid());

        // Bygg grunnlag for videre logikk
        var oppgaveGrunnlag = OppgaveGrunnlagUtleder.lagGrunnlag(dto, egenskaperDto);
        var kriterier = KriterieUtleder.utledKriterier(oppgaveGrunnlag, beskyttelseKriterier);

        var sammeAktiveAksjonspunkt = Objects.equals(eksisterendeBehandling.map(Behandling::getAktiveAksjonspunkt).orElse(null), mapAktiveAksjonspunkt(
            dto));
        var reservasjonskandidat = ReservasjonUtleder.erReservasjonskandidat(oppgaveGrunnlag, eksisterendeBehandling);

        behandlingTjeneste.lagreBehandling(dto, kilde, eksisterendeBehandling, kriterier);

        if (skalEksistereOppgave(oppgaveGrunnlag)) {
            var behandling = behandlingTjeneste.hentBehandling(dto);
            var oppgave = lagOppgave(behandling, oppgaveGrunnlag, kriterier);
            //Prøver å beholde oppgave for å unngå at statistikk teller tilbakehopp som en avluttet oppgave
            if (skalBeholdeEksisterendeOppgave(eksisterendeOppgave, oppgave, sammeAktiveAksjonspunkt)) {
                LOG.info("Eksisterende oppgave {} for behandling {} er lik den utledede oppgaven. Ingen ny oppgave nødvendig.",
                    eksisterendeOppgave.orElseThrow().getId(), oppgave.getBehandling().getId());
            } else {
                LOG.info("Oppretter oppgave for behandling {}", oppgaveGrunnlag.behandlingUuid());
                oppgaveRepository.opprettOppgave(oppgave);
                opprettReservasjon(oppgave, eksisterendeOppgave, reservasjonskandidat, oppgaveGrunnlag);
                eksisterendeOppgave.ifPresent(o -> avsluttOppgave(o, oppgaveGrunnlag.behandlingUuid()));
            }
        } else {
            eksisterendeOppgave.ifPresent(o -> avsluttOppgave(o, oppgaveGrunnlag.behandlingUuid()));
        }
    }

    private static boolean skalBeholdeEksisterendeOppgave(Optional<Oppgave> eksisterendeOppgave,
                                                          Oppgave oppgave,
                                                          boolean likeAktiveAksjonspunkt) {
        var likOppgave = eksisterendeOppgave.isPresent() && erLik(oppgave, eksisterendeOppgave.get());
        return likOppgave && likeAktiveAksjonspunkt;
    }

    private static boolean erLik(Oppgave oppgave1, Oppgave oppgave2) {
        //Ikke komplett equals, men felter som kan endre seg fra en hendelse til en annen
        return Objects.equals(oppgave1.getBehandlendeEnhet(), (oppgave2.getBehandlendeEnhet())) && Objects.equals(oppgave1.getBehandlingsfrist(),
            oppgave2.getBehandlingsfrist()) && Objects.equals(oppgave1.getFørsteStønadsdag(), oppgave2.getFørsteStønadsdag()) && Objects.equals(
            oppgave1.getFeilutbetalingBelop(), oppgave2.getFeilutbetalingBelop()) && Objects.equals(oppgave1.getFeilutbetalingStart(),
            oppgave2.getFeilutbetalingStart()) && oppgave1.getOppgaveEgenskaper().size() == oppgave2.getOppgaveEgenskaper().size()
            && oppgave1.getOppgaveEgenskaper()
            .stream()
            .allMatch(e1 -> oppgave2.getOppgaveEgenskaper().stream().anyMatch(e2 -> e2.getAndreKriterierType().equals(e1.getAndreKriterierType())));
    }

    private void avsluttOppgave(Oppgave o, UUID behandlingId) {
        LOG.info("Avslutter eksisterende oppgave {} for behandling {}", o.getId(), behandlingId);
        o.avsluttOppgave();
        if (o.harAktivReservasjon()) {
            avsluttReservasjon(o.getReservasjon());
        }
    }

    private void avsluttReservasjon(Reservasjon reservasjon) {
        reservasjon.setReservertTil(LocalDateTime.now().minusSeconds(1));
        reservasjonRepository.lagre(reservasjon);
    }

    private void opprettReservasjon(Oppgave oppgave,
                                    Optional<Oppgave> eksisterendeOppgave,
                                    boolean reservasjonskandidat,
                                    OppgaveGrunnlag oppgaveGrunnlag) {
        var reservasjon = ReservasjonUtleder.utledReservasjon(oppgave, eksisterendeOppgave, reservasjonskandidat, oppgaveGrunnlag);
        reservasjon.ifPresent(r -> {
            LOG.info("Opprettet reservasjon for oppgave {}", oppgave.getId());
            reservasjonRepository.lagre(r);
        });
    }

    private Oppgave lagOppgave(Behandling behandling, OppgaveGrunnlag oppgaveGrunnlag, Set<AndreKriterierType> kriterier) {
        LOG.info("Utledet kriterier {} for oppgave til behandling {}", kriterier, oppgaveGrunnlag.behandlingUuid());

        return Oppgave.builder()
            .medBehandlendeEnhet(oppgaveGrunnlag.behandlendeEnhetId())
            .medAktiv(true)
            .medBehandling(behandling)
            .medKriterier(kriterier, oppgaveGrunnlag.ansvarligSaksbehandlerIdent())
            .build();
    }

    private static boolean skalEksistereOppgave(OppgaveGrunnlag oppgaveGrunnlag) {
        var erPåVent = oppgaveGrunnlag.aksjonspunkt()
            .stream()
            .filter(aksjonspunkt -> aksjonspunkt.status() == Aksjonspunktstatus.OPPRETTET)
            .anyMatch(a -> a.type() == AksjonspunktType.PÅ_VENT);
        var underBehandlingStatus = Set.of(BehandlingStatus.OPPRETTET, BehandlingStatus.UTREDES, BehandlingStatus.FATTER_VEDTAK)
            .contains(oppgaveGrunnlag.behandlingStatus());
        var harOpprettetAksjonspunkt = oppgaveGrunnlag.aksjonspunkt().stream().anyMatch(a -> a.status().equals(Aksjonspunktstatus.OPPRETTET));
        return !erPåVent && underBehandlingStatus && harOpprettetAksjonspunkt;
    }

}
