package no.nav.foreldrepenger.los.reservasjon;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.foreldrepenger.los.organisasjon.Saksbehandler;

@Entity(name = "Reservasjon")
@Table(name = "RESERVASJON")
public class Reservasjon extends BaseEntitet {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GLOBAL_PK")
    private Long id;

    @NotNull
    @OneToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "oppgave_id", updatable = false, insertable = false, unique = true)
    private Oppgave oppgave;

    // legger til rå kolonne for gcp-migrering
    @NotNull
    @Column(name = "oppgave_id", nullable = false)
    private Long oppgaveId;

    @Column(name = "RESERVERT_TIL")
    private LocalDateTime reservertTil;

    @NotNull
    @Pattern(regexp = Saksbehandler.VALID_SAKSBEHANDLER_IDENT, message = "Ugyldig ident ${validatedValue}")
    @Column(name = "RESERVERT_AV", nullable = false)
    private String reservertAv;

    @Column(name = "FLYTTET_AV")
    private String flyttetAv;

    @Column(name = "FLYTTET_TIDSPUNKT")
    private LocalDateTime flyttetTidspunkt;

    @Column(name = "BEGRUNNELSE")
    private String begrunnelse;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public Reservasjon() {
        // Hibernate
    }

    public Reservasjon(Oppgave oppgave) {
        this.oppgave = oppgave;
        this.oppgaveId = oppgave.getId();
    }

    public Long getId() {
        return id;
    }

    public Oppgave getOppgave() {
        return oppgave;
    }

    public LocalDateTime getReservertTil() {
        return reservertTil;
    }

    public String getReservertAv() {
        return reservertAv;
    }

    public String getFlyttetAv() {
        return flyttetAv;
    }

    public LocalDateTime getFlyttetTidspunkt() {
        return flyttetTidspunkt;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public boolean erAktiv() {
        return reservertTil != null && reservertTil.isAfter(LocalDateTime.now());
    }

    public void setReservertTil(LocalDateTime reservertTil) {
        this.reservertTil = reservertTil;
    }

    public void setOppgave(Oppgave oppgave) {
        this.oppgave = oppgave;
        this.oppgaveId = oppgave != null ? oppgave.getId() : null;
    }

    public void setReservertAv(String reservertAv) {
        this.reservertAv = reservertAv != null ? reservertAv.toUpperCase() : null;
    }

    public void setFlyttetAv(String flyttetAv) {
        this.flyttetAv = flyttetAv != null ? flyttetAv.toUpperCase() : null;
    }

    public void setFlyttetTidspunkt(LocalDateTime flyttetTidspunkt) {
        this.flyttetTidspunkt = flyttetTidspunkt;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOppgaveId(Long oppgaveId) {
        this.oppgaveId = oppgaveId;
    }
}
