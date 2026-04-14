package no.nav.foreldrepenger.los.reservasjon;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
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
    private Long id; // Idiomatisk JPA. Matches the PK type of Oppgave. Populeres av MapsId under

    @MapsId
    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "oppgave_id")
    private Oppgave oppgave;

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

    protected Reservasjon() {
        // Hibernate
    }

    public Reservasjon(Oppgave oppgave) {
        Objects.requireNonNull(oppgave, "oppgave ctor");
        this.oppgave = oppgave;
    }

    public Reservasjon(Oppgave oppgave, String reservertAv) {
        Objects.requireNonNull(oppgave, "oppgave ctor-reservertAv");
        Objects.requireNonNull(reservertAv, "reservertAv");
        this.oppgave = oppgave;
        this.reservertAv = reservertAv.toUpperCase();
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
        Objects.requireNonNull(oppgave, "oppgave");
        this.oppgave = oppgave;
    }

    public void setReservertAv(String reservertAv) {
        Objects.requireNonNull(reservertAv, "reservertAv");
        this.reservertAv = reservertAv.toUpperCase();
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
}
