package no.nav.foreldrepenger.los.organisasjon;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity(name = "GruppeTilknytningRelasjon")
@IdClass(GruppeTilknytningNøkkel.class)
@Table(name = "GRUPPE_TILKNYTNING")
public class GruppeTilknytningRelasjon implements Serializable {

    @Id
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SAKSBEHANDLER_ID", nullable = false, updatable = false)
    private Saksbehandler saksbehandler;

    @Id
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GRUPPE_ID", nullable = false, updatable = false)
    private SaksbehandlerGruppe gruppe;


    protected GruppeTilknytningRelasjon() {
        // hibernate
    }

    public GruppeTilknytningRelasjon(GruppeTilknytningNøkkel nøkkel) {
        this.saksbehandler = nøkkel.saksbehandler();
        this.gruppe = nøkkel.gruppe();
    }


    public SaksbehandlerGruppe getGruppe() {
        return gruppe;
    }

    public Saksbehandler getSaksbehandler() {
        return saksbehandler;
    }

}
