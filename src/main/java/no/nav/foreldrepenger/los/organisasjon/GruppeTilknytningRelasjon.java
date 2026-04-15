package no.nav.foreldrepenger.los.organisasjon;

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

@Entity(name = "GruppeTilknytningRelasjon")
@Table(name = "GRUPPE_TILKNYTNING")
public class GruppeTilknytningRelasjon implements Serializable {

    @Embeddable
    public record GruppeTilknytningNøkkel(
        @Column(name = "SAKSBEHANDLER_ID", nullable = false, updatable = false) String saksbehandlerIdent,
        @Column(name = "GRUPPE_ID", nullable = false, updatable = false) Long gruppeId)
        implements Serializable {

        public GruppeTilknytningNøkkel(Saksbehandler saksbehandler, SaksbehandlerGruppe gruppe) {
            this(saksbehandler.getSaksbehandlerIdent(), gruppe.getId());
        }
    }

    @EmbeddedId
    private GruppeTilknytningNøkkel nøkkel;

    @MapsId("saksbehandlerIdent")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SAKSBEHANDLER_ID", nullable = false, updatable = false)
    private Saksbehandler saksbehandler;

    @MapsId("gruppeId")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GRUPPE_ID", nullable = false, updatable = false)
    private SaksbehandlerGruppe gruppe;


    protected GruppeTilknytningRelasjon() {
        // hibernate
    }

    public GruppeTilknytningRelasjon(Saksbehandler saksbehandler, SaksbehandlerGruppe gruppe) {
        this.saksbehandler = saksbehandler;
        this.gruppe = gruppe;
        this.nøkkel = new GruppeTilknytningNøkkel(saksbehandler, gruppe);
    }

    public SaksbehandlerGruppe getGruppe() {
        return gruppe;
    }

    public Saksbehandler getSaksbehandler() {
        return saksbehandler;
    }

}
