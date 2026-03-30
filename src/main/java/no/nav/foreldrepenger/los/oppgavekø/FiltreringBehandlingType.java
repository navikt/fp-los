package no.nav.foreldrepenger.los.oppgavekø;

import java.io.Serializable;
import java.util.Objects;

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
import no.nav.foreldrepenger.los.oppgave.BehandlingType;

@Entity(name = "FiltreringBehandlingType")
@IdClass(FiltreringBehandlingType.FiltreringBehandlingTypeIdType.class)
@Table(name = "FILTRERING_BEHANDLING_TYPE")
public class FiltreringBehandlingType implements Serializable {

    @Embeddable
    public record FiltreringBehandlingTypeIdType(OppgaveFiltrering oppgaveFiltrering, BehandlingType behandlingType) implements Serializable { }

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "OPPGAVE_FILTRERING_ID", nullable = false)
    private OppgaveFiltrering oppgaveFiltrering;

    @Id
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "BEHANDLING_TYPE", nullable = false)
    private BehandlingType behandlingType;


    protected FiltreringBehandlingType() {
        // Hibernate
    }

    public FiltreringBehandlingType(OppgaveFiltrering oppgaveFiltrering, BehandlingType behandlingType) {
        this.oppgaveFiltrering = oppgaveFiltrering;
        this.behandlingType = behandlingType;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public OppgaveFiltrering getOppgaveFiltrering() {
        return oppgaveFiltrering;
    }

    public void setOppgaveFiltrering(OppgaveFiltrering oppgaveFiltrering) {
        this.oppgaveFiltrering = oppgaveFiltrering;
    }

    public void setBehandlingType(BehandlingType behandlingType) {
        this.behandlingType = behandlingType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiltreringBehandlingType other)) return false;
        return this.behandlingType == other.behandlingType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(behandlingType);
    }

}
