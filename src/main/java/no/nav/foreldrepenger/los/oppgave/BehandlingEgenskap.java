package no.nav.foreldrepenger.los.oppgave;


import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@IdClass(BehandlingEgenskapIdType.class)
@Table(name = "BEHANDLING_EGENSKAP")
public class BehandlingEgenskap implements Serializable {

    @Id
    @NotNull
    @Column(name = "BEHANDLING_ID", updatable = false, nullable = false)
    private UUID behandlingId;

    @Id
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ANDRE_KRITERIER_TYPE", updatable = false, nullable = false)
    private AndreKriterierType andreKriterierType;

    protected BehandlingEgenskap() {
    }

    public BehandlingEgenskap(UUID behandlingId, AndreKriterierType andreKriterierType) {
        this.behandlingId = behandlingId;
        this.andreKriterierType = andreKriterierType;
    }

    public BehandlingEgenskap(BehandlingEgenskapIdType egenskap) {
        this(egenskap.behandlingId(), egenskap.andreKriterierType());
    }

    public UUID getBehandlingId() {
        return behandlingId;
    }

    public AndreKriterierType getAndreKriterierType() {
        return andreKriterierType;
    }
}
