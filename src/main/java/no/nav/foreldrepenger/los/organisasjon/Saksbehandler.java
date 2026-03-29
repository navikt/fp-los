package no.nav.foreldrepenger.los.organisasjon;

import java.util.Objects;
import java.util.Optional;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.felles.BaseEntitet;

@Entity(name = "saksbehandler")
@Table(name = "SAKSBEHANDLER")
public class Saksbehandler extends BaseEntitet {

    public static final String VALID_SAKSBEHANDLER_IDENT = "^[A-Z]{1}\\d{6}$";

    @Id
    @NaturalId
    @NotNull
    @Pattern(regexp = VALID_SAKSBEHANDLER_IDENT, message = "Ugyldig ident ${validatedValue}")
    @Column(name = "SAKSBEHANDLER_IDENT", nullable = false, unique = true)
    private String saksbehandlerIdent;

    @Column(name = "NAVN")
    private String navn;

    @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}")
    @Column(name = "ANSATT_ENHET")
    private String ansattVedEnhet;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    protected Saksbehandler() {
        // Hibernate
    }

    public Saksbehandler(String saksbehandlerIdent, String navn, String ansattVedEnhet) {
        Objects.requireNonNull(saksbehandlerIdent, "saksbehandlerIdent");
        this.saksbehandlerIdent = saksbehandlerIdent.toUpperCase();
        this.navn = navn;
        this.ansattVedEnhet = ansattVedEnhet;
    }

    public String getSaksbehandlerIdent() {
        return saksbehandlerIdent;
    }

    public String getNavn() {
        return navn;
    }

    public String getNavnEllerUkjent() {
        return Optional.ofNullable(navn).orElse("Ukjent saksbehandler");
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getAnsattVedEnhet() {
        return ansattVedEnhet;
    }

    public String getAnsattVedEnhetEllerUkjent() {
        return Optional.ofNullable(ansattVedEnhet).orElse("9999");
    }


    public void setAnsattVedEnhet(String ansattVedEnhet) {
        this.ansattVedEnhet = ansattVedEnhet;
    }

    // Setter added for migration purposes - can be removed after migration
    public void setSaksbehandlerIdent(String saksbehandlerIdent) {
        Objects.requireNonNull(saksbehandlerIdent, "saksbehandlerIdent");
        this.saksbehandlerIdent = saksbehandlerIdent.toUpperCase();
    }
}
