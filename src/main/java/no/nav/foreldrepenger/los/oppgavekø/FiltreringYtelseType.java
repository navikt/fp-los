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
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;

@Entity(name = "FiltreringYtelseType")
@IdClass(FiltreringYtelseType.FiltreringYtelseTypeIdType.class)
@Table(name = "FILTRERING_YTELSE_TYPE")
public class FiltreringYtelseType implements Serializable {

    @Embeddable
    public record FiltreringYtelseTypeIdType(OppgaveFiltrering oppgaveFiltrering, FagsakYtelseType fagsakYtelseType) implements Serializable { }

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "OPPGAVE_FILTRERING_ID", nullable = false)
    private OppgaveFiltrering oppgaveFiltrering;

    @Id
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "FAGSAK_YTELSE_TYPE", nullable = false)
    private FagsakYtelseType fagsakYtelseType;

    protected FiltreringYtelseType() {
        // Hibernate
    }

    public FiltreringYtelseType(OppgaveFiltrering oppgaveFiltrering, FagsakYtelseType fagsakYtelseTypeKode) {
        this.oppgaveFiltrering = oppgaveFiltrering;
        this.fagsakYtelseType = fagsakYtelseTypeKode;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiltreringYtelseType other)) return false;
        return this.fagsakYtelseType == other.fagsakYtelseType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fagsakYtelseType);
    }

}
