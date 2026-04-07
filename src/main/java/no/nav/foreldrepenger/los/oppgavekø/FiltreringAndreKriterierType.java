package no.nav.foreldrepenger.los.oppgavekø;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;

@Entity(name = "FiltreringAndreKriterier")
@IdClass(FiltreringAndreKriterierType.FiltreringAndreKriterierIdType.class)
@Table(name = "FILTRERING_ANDRE_KRITERIER")
public class FiltreringAndreKriterierType implements Serializable {

    public record FiltreringAndreKriterierIdType(OppgaveFiltrering oppgaveFiltrering, AndreKriterierType andreKriterierType) implements Serializable { }

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "OPPGAVE_FILTRERING_ID", nullable = false)
    private OppgaveFiltrering oppgaveFiltrering;

    @Id
    @NotNull
    @Column(name = "ANDRE_KRITERIER_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private AndreKriterierType andreKriterierType;

    //Verdi som viser om filtreringen skal inkludere eller ekskludere oppgaver med det gitte innslaget.
    @NotNull
    @Column(name = "INKLUDER", nullable = false)
    private boolean inkluder = true;

    protected FiltreringAndreKriterierType() {
        // Hibernate
    }

    public FiltreringAndreKriterierType(OppgaveFiltrering oppgaveFiltrering, AndreKriterierType andreKriterierType, boolean inkluder) {
        this.oppgaveFiltrering = oppgaveFiltrering;
        this.andreKriterierType = andreKriterierType;
        this.inkluder = inkluder;
    }

    public AndreKriterierType getAndreKriterierType() {
        return andreKriterierType;
    }

    public boolean isInkluder() {
        return inkluder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiltreringAndreKriterierType other)) return false;
        return this.andreKriterierType == other.andreKriterierType && this.inkluder == other.inkluder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(andreKriterierType, inkluder);
    }

}
