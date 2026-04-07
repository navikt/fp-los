package no.nav.foreldrepenger.los.organisasjon;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.felles.BaseEntitet;

@Entity(name = "avdeling")
@Table(name = "AVDELING")
public class Avdeling extends BaseEntitet {

    public static final String AVDELING_DRAMMEN_ENHET = "4806";

    public static final String VALID_AVDELING_ID = "^\\d{4}$";

    @Id
    @NaturalId
    @NotNull
    @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}")
    @Column(name = "AVDELING_ENHET", unique = true, nullable = false)
    private String avdelingEnhet;

    @NotNull
    @Column(name = "NAVN", nullable = false)
    private String navn;

    @NotNull
    @Column(name = "KREVER_KODE_6", nullable = false)
    private boolean kreverKode6 = Boolean.FALSE;

    @NotNull
    @Column(name = "AKTIV", nullable = false)
    private boolean erAktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    protected Avdeling() {
        // Hibernate
    }

    public Avdeling(String avdelingEnhet, String navn, Boolean kreverKode6) {
        this.avdelingEnhet = avdelingEnhet;
        this.navn = navn;
        this.kreverKode6 = kreverKode6;
    }

    public String getAvdelingEnhet() {
        return avdelingEnhet;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public boolean getKreverKode6() {
        return kreverKode6;
    }

    public boolean getErAktiv() {
        return erAktiv;
    }

    public void setErAktiv(boolean erAktiv) {
        this.erAktiv = erAktiv;
    }

    public void setAvdelingEnhet(String avdelingEnhet) {
        this.avdelingEnhet = avdelingEnhet;
    }

    public void setKreverKode6(boolean kreverKode6) {
        this.kreverKode6 = kreverKode6;
    }

}
