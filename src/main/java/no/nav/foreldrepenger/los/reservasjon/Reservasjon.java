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

import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.oppgave.Oppgave;

@Entity(name = "Reservasjon")
@Table(name = "RESERVASJON")
public class Reservasjon extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GLOBAL_PK")
    private Long id;

    @OneToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "oppgave_id", updatable = false, insertable = false)
    private Oppgave oppgave;

    // legger til rå kolonne for gcp-migrering
    @Column(name = "oppgave_id", nullable = false)
    private Long oppgaveId;

    @Column(name = "RESERVERT_TIL")
    private LocalDateTime reservertTil;

    @Column(name = "RESERVERT_AV")
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
        this.reservertAv = reservertAv;
    }

    public void setFlyttetAv(String flyttetAv) {
        this.flyttetAv = flyttetAv;
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
