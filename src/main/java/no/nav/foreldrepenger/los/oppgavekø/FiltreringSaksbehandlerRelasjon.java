package no.nav.foreldrepenger.los.oppgavekø;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;

@Entity(name = "FiltreringSaksbehandlerRelasjon")
@Table(name = "FILTRERING_SAKSBEHANDLER")
public class FiltreringSaksbehandlerRelasjon implements Serializable {

    @Embeddable
    public record FiltreringSaksbehandlerNøkkel(
        @Column(name = "SAKSBEHANDLER_ID", nullable = false, updatable = false) String saksbehandlerIdent,
        @Column(name = "OPPGAVE_FILTRERING_ID", nullable = false, updatable = false) Long oppgaveFiltreringId)
        implements Serializable {

        public FiltreringSaksbehandlerNøkkel(Saksbehandler saksbehandler, OppgaveFiltrering oppgaveFiltrering) {
            this(saksbehandler.getSaksbehandlerIdent(), oppgaveFiltrering.getId());
        }
    }

    @EmbeddedId
    private FiltreringSaksbehandlerNøkkel nøkkel;

    @MapsId("saksbehandlerIdent")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SAKSBEHANDLER_ID", nullable = false, updatable = false)
    private Saksbehandler saksbehandler;

    @MapsId("oppgaveFiltreringId")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPPGAVE_FILTRERING_ID", nullable = false, updatable = false)
    private OppgaveFiltrering oppgaveFiltrering;


    protected FiltreringSaksbehandlerRelasjon() {
        // hibernate
    }

    public FiltreringSaksbehandlerRelasjon(Saksbehandler saksbehandler, OppgaveFiltrering oppgaveFiltrering) {
        this.saksbehandler = saksbehandler;
        this.oppgaveFiltrering = oppgaveFiltrering;
        this.nøkkel = new FiltreringSaksbehandlerNøkkel(saksbehandler, oppgaveFiltrering);
    }

    public Saksbehandler getSaksbehandler() {
        return saksbehandler;
    }

    public OppgaveFiltrering getOppgaveFiltrering() {
        return oppgaveFiltrering;
    }

}
