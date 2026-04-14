package no.nav.foreldrepenger.los.tjenester.felles.dto;

import static no.nav.foreldrepenger.los.organisasjon.Avdeling.AVDELING_DRAMMEN_ENHET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.los.JpaExtension;
import no.nav.foreldrepenger.los.domene.typer.aktør.Fødselsnummer;
import no.nav.foreldrepenger.los.domene.typer.aktør.Person;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.Behandling;
import no.nav.foreldrepenger.los.oppgave.BehandlingTilstand;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.oppgave.OppgaveRepository;
import no.nav.foreldrepenger.los.oppgave.OppgaveTjeneste;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveKøTjeneste;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.organisasjon.OrganisasjonRepository;
import no.nav.foreldrepenger.los.persontjeneste.PersonTjeneste;
import no.nav.foreldrepenger.los.reservasjon.ReservasjonRepository;
import no.nav.foreldrepenger.los.reservasjon.ReservasjonTjeneste;
import no.nav.foreldrepenger.los.server.abac.TilgangFilterKlient;
import no.nav.foreldrepenger.los.statistikk.StatistikkRepository;

@ExtendWith(JpaExtension.class)
class OppgaveDtoTjenesteTest {

    private final TilgangFilterKlient tilgangFilterklient = mock(TilgangFilterKlient.class);
    private final PersonTjeneste personTjeneste = mock(PersonTjeneste.class);

    private OppgaveRepository oppgaveRepository;
    private OppgaveDtoTjeneste oppgaveDtoTjeneste;
    private ReservasjonTjeneste reservasjonTjeneste;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        var reservasjonRepository = new ReservasjonRepository(entityManager);
        this.oppgaveRepository = new OppgaveRepository(entityManager);
        var reservasjonStatusDtoTjeneste = new ReservasjonStatusDtoTjeneste(new OrganisasjonRepository(entityManager), oppgaveRepository);
        this.reservasjonTjeneste = new ReservasjonTjeneste(oppgaveRepository, reservasjonRepository);
        OppgaveTjeneste oppgaveTjeneste = new OppgaveTjeneste(oppgaveRepository, reservasjonTjeneste);
        this.oppgaveDtoTjeneste = new OppgaveDtoTjeneste(oppgaveTjeneste, reservasjonTjeneste, personTjeneste, reservasjonStatusDtoTjeneste, mock(
            OppgaveKøTjeneste.class), tilgangFilterklient, new StatistikkRepository(entityManager));
    }

    @Test
    void skalHenteSisteReserverteOppgaverMedStatus() {
        // Testen kjører i bunn relativt komplisert native query for å hente siste reserverte oppgaveId-referanser med et par datafelter brukt i
        // utledning av status i ReservasjonTjeneste. Tilgangskontroll og mapping til DTO skjer i OppgaveDtoTjeneste.
        var behandlingId = UUID.randomUUID();

        when(tilgangFilterklient.tilgangFilterSaker(anyList())).thenAnswer(invocation -> {
            List<Oppgave> oppgaver = invocation.getArgument(0);
            return oppgaver.stream().map(Oppgave::getSaksnummer).collect(Collectors.toSet());
        });

        when(personTjeneste.hentPerson(any(), any(), any()))
            .thenReturn(Optional.of(new Person(new Fødselsnummer("1233456789"), "Navn Navnesen")));

        var bb = Behandling.builder(Optional.empty())
            .dummyBehandling(AVDELING_DRAMMEN_ENHET, BehandlingTilstand.BESLUTTER)
            .medId(behandlingId);
        oppgaveRepository.lagreBehandling(bb);
        var behandling = oppgaveRepository.hentBehandling(behandlingId);
        var oppgave = Oppgave.builder()
            .dummyOppgave(Avdeling.AVDELING_DRAMMEN_ENHET, behandling)
            .medKriterier(Set.of(AndreKriterierType.TIL_BESLUTTER), "IDENT")
            .build();
        oppgaveRepository.lagre(oppgave);
        reservasjonTjeneste.reserverOppgave(oppgave);

        var sisteReserverteEtterReservasjon = oppgaveDtoTjeneste.getSaksbehandlersSisteReserverteOppgaver(false);
        assertThat(sisteReserverteEtterReservasjon)
            .hasSize(1)
            .first().matches(dto -> dto.getOppgaveBehandlingStatus() == OppgaveBehandlingStatus.TIL_BESLUTTER);

        behandling.setBehandlingTilstand(BehandlingTilstand.AVSLUTTET);
        behandling.setAvsluttet(LocalDateTime.now());
        oppgaveRepository.lagre(behandling);
        oppgaveRepository.hentReservasjon(oppgave.getId()).ifPresent(reservasjonTjeneste::slettReservasjon);
        oppgave.avsluttOppgave();
        oppgaveRepository.lagre(oppgave);

        var sisteReserverte = oppgaveDtoTjeneste.getSaksbehandlersSisteReserverteOppgaver(false);
        assertThat(sisteReserverte)
            .hasSize(1)
            .first().matches(dto -> dto.getOppgaveBehandlingStatus() == OppgaveBehandlingStatus.FERDIG);
    }


}
