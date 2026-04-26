package no.nav.foreldrepenger.los.tjenester.felles.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgave.OppgaveRepository;
import no.nav.foreldrepenger.los.organisasjon.OrganisasjonRepository;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;

@ApplicationScoped
public class ReservasjonStatusDtoTjeneste {

    private static final String SYSTEMBRUKER = "SRVFPLOS";
    private OrganisasjonRepository organisasjonRepository;
    private OppgaveRepository oppgaveRepository;

    @Inject
    public ReservasjonStatusDtoTjeneste(OrganisasjonRepository organisasjonRepository, OppgaveRepository oppgaveRepository) {
        this.organisasjonRepository = organisasjonRepository;
        this.oppgaveRepository = oppgaveRepository;
    }

    ReservasjonStatusDtoTjeneste() {
        //CDI
    }

    ReservasjonStatusDto lagStatusFor(Oppgave oppgave) {
        var reservasjon = oppgaveRepository.hentReservasjon(oppgave.getId()).filter(Reservasjon::erAktiv).orElse(null);
        return lagStatusFor(reservasjon);
    }

    Map<Long, ReservasjonStatusDto> lagStatusFor(List<Oppgave> oppgaver) {
        var ids = oppgaver.stream().map(Oppgave::getId).collect(Collectors.toSet());
        var reservasjoner = oppgaveRepository.hentReservasjoner(ids).stream()
            .filter(Reservasjon::erAktiv)
            .collect(Collectors.toMap(r -> r.getOppgave().getId(), Function.identity()));
        // Slett ikke alle oppgaver har en aktiv reservasjon
        return ids.stream()
            .collect(Collectors.toMap(Function.identity(), o -> lagStatusFor(reservasjoner.get(o))));
    }

    ReservasjonStatusDto lagStatusFor(Reservasjon reservasjon) {
        if (reservasjon != null) {
            if (SYSTEMBRUKER.equalsIgnoreCase(reservasjon.getFlyttetAv())) {
                return systembrukerSpesialTilfelle(reservasjon);
            }
            var flyttetAvIdent = reservasjon.getFlyttetAv();
            var flyttetAvNavn = hentNavn(reservasjon.getFlyttetAv());
            var reservertAvNavn = reservasjon.getReservertAv().equalsIgnoreCase(flyttetAvIdent) ? flyttetAvNavn : hentNavn(
                reservasjon.getReservertAv());
            return ReservasjonStatusDto.reservert(reservasjon, reservertAvNavn, flyttetAvNavn);
        }
        return ReservasjonStatusDto.ikkeReservert();
    }

    private String hentNavn(String ident) {
        return Optional.ofNullable(ident)
            .flatMap(organisasjonRepository::hentSaksbehandlerHvisEksisterer)
            .map(Saksbehandler::getNavn).orElse("Ukjent");
    }

    private ReservasjonStatusDto systembrukerSpesialTilfelle(Reservasjon reservasjon) {
        // forskjønne visning av systembrukers navn i frontend
        var flyttetReservasjonDto = new FlyttetReservasjonDto(reservasjon.getFlyttetTidspunkt(), "Fplos", "oppgavesystem",
            reservasjon.getBegrunnelse());
        return ReservasjonStatusDto.reservert(reservasjon, hentNavn(reservasjon.getReservertAv()), flyttetReservasjonDto);
    }
}
