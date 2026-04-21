package no.nav.foreldrepenger.los.tjenester.saksbehandler.saksliste;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.los.oppgavekø.OppgaveKøTjeneste;
import no.nav.foreldrepenger.los.statistikk.StatistikkRepository;
import no.nav.foreldrepenger.los.tjenester.avdelingsleder.nøkkeltall.NøkkeltallRestTjeneste;
import no.nav.foreldrepenger.los.tjenester.felles.dto.SaksbehandlerDtoTjeneste;
import no.nav.foreldrepenger.los.tjenester.felles.dto.SakslisteDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path("/saksbehandler/saksliste")
@ApplicationScoped
@Transactional
public class SaksbehandlerSakslisteRestTjeneste {

    private OppgaveKøTjeneste oppgaveKøTjeneste;
    private StatistikkRepository statistikkRepository;

    @Inject
    public SaksbehandlerSakslisteRestTjeneste(OppgaveKøTjeneste oppgaveKøTjeneste, StatistikkRepository statistikkRepository) {
        this.oppgaveKøTjeneste = oppgaveKøTjeneste;
        this.statistikkRepository = statistikkRepository;
    }

    public SaksbehandlerSakslisteRestTjeneste() {
        // For Rest-CDI
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter sakslister", tags = "Saksliste")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    public List<SakslisteDto> hentSakslister() {
        var filtre = oppgaveKøTjeneste.hentOppgaveFiltreringerForPåloggetBruker();
        var filterIds = filtre.stream().map(of -> of.getId()).collect(Collectors.toSet());
        var statistikkMap = statistikkRepository.hentSisteStatistikkForOppgaveFiltre(filterIds);
        var saksbehandlerMap = oppgaveKøTjeneste.hentSaksbehandlereForOppgaveFiltreringer(filtre);
        return filtre.stream().map(of -> new SakslisteDto(of,
            saksbehandlerMap.getOrDefault(of.getId(), Collections.emptyList()).stream().map(SaksbehandlerDtoTjeneste::saksbehandlerDto).toList(),
            Optional.ofNullable(statistikkMap.get(of.getId())).map(NøkkeltallRestTjeneste::tilDto).orElse(null))).toList();
    }
}
