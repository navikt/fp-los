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

@Entity(name = "AvdelingSaksbehandlerRelasjon")
@Table(name = "AVDELING_SAKSBEHANDLER")
public class AvdelingSaksbehandlerRelasjon implements Serializable {

    @Embeddable
    public record AvdelingSaksbehandlerNøkkel(
        @Column(name = "SAKSBEHANDLER_ID", nullable = false, updatable = false) String saksbehandlerIdent,
        @Column(name = "AVDELING_ID", nullable = false, updatable = false) String avdelingEnhet)
        implements Serializable {

        public AvdelingSaksbehandlerNøkkel(Saksbehandler saksbehandler, Avdeling avdeling) {
            this(saksbehandler.getSaksbehandlerIdent(), avdeling.getAvdelingEnhet());
        }
    }

    @EmbeddedId
    private AvdelingSaksbehandlerNøkkel nøkkel;

    @MapsId("saksbehandlerIdent")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SAKSBEHANDLER_ID", nullable = false, updatable = false)
    private Saksbehandler saksbehandler;

    @MapsId("avdelingEnhet")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AVDELING_ID", nullable = false, updatable = false)
    private Avdeling avdeling;


    protected AvdelingSaksbehandlerRelasjon() {
        // hibernate
    }

    public AvdelingSaksbehandlerRelasjon(Saksbehandler saksbehandler, Avdeling avdeling) {
        this.saksbehandler = saksbehandler;
        this.avdeling = avdeling;
        this.nøkkel = new AvdelingSaksbehandlerNøkkel(saksbehandler, avdeling);
    }

    public Avdeling getAvdeling() {
        return avdeling;
    }

    public Saksbehandler getSaksbehandler() {
        return saksbehandler;
    }

}
