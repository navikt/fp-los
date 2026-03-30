package no.nav.foreldrepenger.los.tjenester.admin;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.los.domene.typer.BehandlingId;
import no.nav.foreldrepenger.los.hendelse.behandlinghendelse.BehandlingHendelseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.hendelser.behandling.Kildesystem;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class SynkroniseringHendelseTaskOppretterTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(SynkroniseringHendelseTaskOppretterTjeneste.class);

    private ProsessTaskTjeneste prosessTaskTjeneste;

    @Inject
    public SynkroniseringHendelseTaskOppretterTjeneste(ProsessTaskTjeneste prosessTaskTjeneste) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    SynkroniseringHendelseTaskOppretterTjeneste() {
        // for CDI proxy
    }

    public int opprettOppgaveEgenskapOppdatererTasks(List<KildeBehandlingId> behandlinger) {
        if (behandlinger.size() > 1000) {
            throw new IllegalArgumentException("Støtter ikke så mange behandlinger, send under 1000");
        }
        if (behandlinger.isEmpty()) {
            LOG.info("Ingen behandlinger å opprette tasks for.");
            return 0;
        }

        final var callId = (MDCOperations.getCallId() == null ? MDCOperations.generateCallId() : MDCOperations.getCallId()) + "_";

        LOG.info("Oppretter tasker for synkronisering av {} hendelser", behandlinger.size());
        var gruppe = new ProsessTaskGruppe();
        var gruppeNavn = BehandlingHendelseTask.class.getSimpleName() + System.currentTimeMillis();
        for (var behandling : behandlinger) {
            opprettSynkroniseringTask(gruppe, behandling, callId, gruppeNavn);
        }
        prosessTaskTjeneste.lagre(gruppe);
        return behandlinger.size();
    }

    public record KildeBehandlingId(Kildesystem kildesystem, BehandlingId behandlingId) {
    }

    private void opprettSynkroniseringTask(ProsessTaskGruppe gruppe, KildeBehandlingId kildeBehandlingId, String callId, String gruppeNavn) {
        var prosessTaskData = ProsessTaskData.forProsessTask(BehandlingHendelseTask.class);
        prosessTaskData.setGruppe(gruppeNavn);
        prosessTaskData.setCallId(callId + kildeBehandlingId.behandlingId.toString());
        prosessTaskData.setPrioritet(4);
        prosessTaskData.setBehandlingUUid(kildeBehandlingId.behandlingId.getValue());
        prosessTaskData.setProperty(BehandlingHendelseTask.KILDE, kildeBehandlingId.kildesystem.name());
        gruppe.addNesteSekvensiell(prosessTaskData);
    }
}
