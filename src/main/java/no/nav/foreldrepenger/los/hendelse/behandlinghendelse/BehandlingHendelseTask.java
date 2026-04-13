package no.nav.foreldrepenger.los.hendelse.behandlinghendelse;

import java.util.UUID;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.CommonTaskProperties;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.hendelser.behandling.Kildesystem;
import no.nav.vedtak.hendelser.behandling.los.LosBehandlingDto;
import no.nav.vedtak.hendelser.behandling.los.LosFagsakEgenskaperDto;

@Dependent
@ProsessTask(value = "håndter.behandlinghendelse", firstDelay = 10, thenDelay = 10)
public class BehandlingHendelseTask implements ProsessTaskHandler {

    static final String BEHANDLING_UUID = CommonTaskProperties.BEHANDLING_UUID;
    public static final String KILDE = "kildesystem";

    private final FpsakBehandlingKlient fpsakKlient;
    private final FptilbakeBehandlingKlient fptilbakeKlient;

    private final BehandlingHendelseTjeneste behandlingHendelseTjeneste;

    @Inject
    BehandlingHendelseTask(FpsakBehandlingKlient fpsakKlient,
                           FptilbakeBehandlingKlient fptilbakeKlient,
                           BehandlingHendelseTjeneste behandlingHendelseTjeneste) {
        this.fpsakKlient = fpsakKlient;
        this.fptilbakeKlient = fptilbakeKlient;
        this.behandlingHendelseTjeneste = behandlingHendelseTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var behandlingUuid = UUID.fromString(prosessTaskData.getPropertyValue(BEHANDLING_UUID));
        var kilde = switch (Kildesystem.valueOf(prosessTaskData.getPropertyValue(KILDE))) {
            case FPSAK -> Fagsystem.FPSAK;
            case FPTILBAKE -> Fagsystem.FPTILBAKE;
        };

        // Hent eksterne data
        var dto = hentDto(behandlingUuid, kilde);
        var egenskaperDto = hentFagsakEgenskaper(dto, kilde);
        behandlingHendelseTjeneste.lagreBehandlingOppdaterOppgaver(dto, egenskaperDto, kilde);
    }

    private LosBehandlingDto hentDto(UUID behandlingUuid, Fagsystem kilde) {
        return kilde.equals(Fagsystem.FPSAK) ? fpsakKlient.hentLosBehandlingDto(behandlingUuid) : fptilbakeKlient.hentLosBehandlingDto(
            behandlingUuid);
    }

    private LosFagsakEgenskaperDto hentFagsakEgenskaper(LosBehandlingDto dto, Fagsystem kilde) {
        return kilde.equals(Fagsystem.FPSAK) ? new LosFagsakEgenskaperDto(dto.saksegenskaper()) : fpsakKlient.hentLosFagsakEgenskaperDto(
            new Saksnummer(dto.saksnummer()));
    }

}
