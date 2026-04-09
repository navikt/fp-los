package no.nav.foreldrepenger.los.migrering.gcp;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.los.migrering.dto.BulkDataWrapper;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

import java.util.function.Function;

@Path("/gcp-migrering")
@ApplicationScoped
@Transactional
public class GcpImportRestTjeneste {


    private GcpImportRepository gcpImportRepository;

    @Inject
    public GcpImportRestTjeneste(GcpImportRepository gcpImportRepository) {
        this.gcpImportRepository = gcpImportRepository;
    }

    GcpImportRestTjeneste() {
        // For CDI
    }


    @POST
    @Path("/lagre-bulk")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Lagrer bulk migreringsdata", tags = "migrering")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public Response lagreBulkData(@TilpassetAbacAttributt(supplierClass = GcpMigreringAbacDataSupplier.class) @NotNull @Valid BulkDataWrapper bulkData) {
        var resultat = gcpImportRepository.lagre(bulkData);
        return Response.ok(resultat).build();
    }


    public static class GcpMigreringAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }
}
