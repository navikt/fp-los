package no.nav.foreldrepenger.los.oppgave;

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
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;

@Entity(name = "OppgaveEgenskap")
@IdClass(OppgaveEgenskap.OppgaveEgenskapIdType.class)
@Table(name = "OPPGAVE_EGENSKAP")
public class OppgaveEgenskap implements Serializable {

    @Embeddable
    public record OppgaveEgenskapIdType(Oppgave oppgave, AndreKriterierType andreKriterierType) implements Serializable { }

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "OPPGAVE_ID", nullable = false)
    private Oppgave oppgave;

    @Id
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ANDRE_KRITERIER_TYPE", nullable = false)
    private AndreKriterierType andreKriterierType;

    // feltet brukes i query for å ekskludere egne oppgaver i beslutterkøer
    @Pattern(regexp = Saksbehandler.VALID_SAKSBEHANDLER_IDENT, message = "Ugyldig ident ${validatedValue}")
    @Column(name = "SISTE_SAKSBEHANDLER_FOR_TOTR")
    private String sisteSaksbehandlerForTotrinn;

    protected OppgaveEgenskap() {
        // Hibernate
    }

    protected OppgaveEgenskap(Oppgave oppgave, AndreKriterierType andreKriterierType) {
        Objects.requireNonNull(oppgave);
        Objects.requireNonNull(andreKriterierType);
        if (andreKriterierType.erTilBeslutter()) {
            throw new IllegalArgumentException("Feil constructor for OppgaveEgenskap " + andreKriterierType.name());
        }
        this.oppgave = oppgave;
        this.andreKriterierType = andreKriterierType;
    }

    protected OppgaveEgenskap(Oppgave oppgave, AndreKriterierType andreKriterierType, String sisteSaksbehandlerForTotrinn) {
        Objects.requireNonNull(oppgave);
        Objects.requireNonNull(andreKriterierType);
        if (!andreKriterierType.erTilBeslutter()) {
            throw new IllegalArgumentException("Feil constructor for OppgaveEgenskap  " + andreKriterierType.name());
        }
        Objects.requireNonNull(sisteSaksbehandlerForTotrinn);
        this.oppgave = oppgave;
        this.andreKriterierType = andreKriterierType;
        this.sisteSaksbehandlerForTotrinn = sisteSaksbehandlerForTotrinn.trim().toUpperCase();
    }

    void setOppgave(Oppgave oppgave) {
        this.oppgave = oppgave;
    }

    public Oppgave getOppgave() {
        return oppgave;
    }

    public AndreKriterierType getAndreKriterierType() {
        return andreKriterierType;
    }

    public String getSisteSaksbehandlerForTotrinn() {
        return sisteSaksbehandlerForTotrinn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OppgaveEgenskap that)) return false;
        return andreKriterierType == that.andreKriterierType && Objects.equals(sisteSaksbehandlerForTotrinn, that.sisteSaksbehandlerForTotrinn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(andreKriterierType, sisteSaksbehandlerForTotrinn);
    }
}
