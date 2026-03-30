package no.nav.foreldrepenger.los.oppgave;


import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@IdClass(BehandlingEgenskap.BehandlingEgenskapIdType.class)
@Table(name = "BEHANDLING_EGENSKAP")
public class BehandlingEgenskap implements Serializable {

    @Embeddable
    public static record BehandlingEgenskapIdType(Behandling behandling, AndreKriterierType andreKriterierType) implements Serializable { }

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "BEHANDLING_ID", nullable = false)
    private Behandling behandling;

    @Id
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ANDRE_KRITERIER_TYPE", nullable = false)
    private AndreKriterierType andreKriterierType;

    protected BehandlingEgenskap() {
    }

    public BehandlingEgenskap(Behandling behandling, AndreKriterierType andreKriterierType) {
        this.behandling = behandling;
        this.andreKriterierType = andreKriterierType;
    }

    public BehandlingEgenskap(BehandlingEgenskap.BehandlingEgenskapIdType egenskap) {
        this(egenskap.behandling(), egenskap.andreKriterierType());
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public AndreKriterierType getAndreKriterierType() {
        return andreKriterierType;
    }
}
