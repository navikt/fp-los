package no.nav.foreldrepenger.los.organisasjon;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.migrering.gcp.SequenceOrAssigned;
import no.nav.foreldrepenger.los.migrering.gcp.SequenceOrAssignedMarker;

@Entity(name = "saksbehandlerGruppe")
@Table(name = "SAKSBEHANDLER_GRUPPE")
public class SaksbehandlerGruppe extends BaseEntitet implements SequenceOrAssignedMarker<Long> {

    @Id
    @NotNull
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GLOBAL_PK")
    // bruker en custom IdGenerator for å kunne sette PK ved migrering
    @SequenceOrAssigned(sequence = "SEQ_GLOBAL_PK")
    private Long id;

    @NotNull
    @Column(name = "GRUPPE_NAVN", nullable = false)
    private String gruppeNavn;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AVDELING_ID", nullable = false, updatable = false)
    private Avdeling avdeling;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    protected SaksbehandlerGruppe() {
        // Hibernate
    }

    public SaksbehandlerGruppe(String gruppeNavn, Avdeling avdeling) {
        Objects.requireNonNull(avdeling, "avdeling");
        Objects.requireNonNull(gruppeNavn, "gruppeNavn");
        this.gruppeNavn = gruppeNavn;
        this.avdeling = avdeling;
    }

    public Long getId() {
        return id;
    }

    // TODO: Fjerne etter migrering
    public void setId(Long id) {
        this.id = id;
    }

    public Avdeling getAvdeling() {
        return avdeling;
    }

    public void setGruppeNavn(String gruppeNavn) {
        this.gruppeNavn = gruppeNavn;
    }

    public String getGruppeNavn() {
        return gruppeNavn;
    }

    public void setAvdeling(Avdeling avdeling) {
        this.avdeling = avdeling;
    }
}
