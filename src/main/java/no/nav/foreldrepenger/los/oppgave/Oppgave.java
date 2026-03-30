package no.nav.foreldrepenger.los.oppgave;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.domene.typer.BehandlingId;
import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.domene.typer.aktør.AktørId;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Oppgave")
@Table(name = "OPPGAVE")
public class Oppgave extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GLOBAL_PK")
    private Long id;

    @Embedded
    private Saksnummer saksnummer; // Denne er de-facto non-null

    @Embedded
    private AktørId aktørId;

    @NotNull
    @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}")
    @Column(name = "BEHANDLENDE_ENHET", nullable = false)
    private String behandlendeEnhet;

    @Column(name = "BEHANDLINGSFRIST")
    private LocalDate behandlingsfrist;

    @Column(name = "BEHANDLING_OPPRETTET")
    private LocalDateTime behandlingOpprettet;

    @Column(name = "FORSTE_STONADSDAG")
    private LocalDate førsteStønadsdag;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "BEHANDLING_TYPE", nullable = false)
    private BehandlingType behandlingType = BehandlingType.INNSYN;

    @OneToMany(mappedBy = "oppgave", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<OppgaveEgenskap> oppgaveEgenskaper = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "FAGSAK_YTELSE_TYPE", nullable = false)
    private FagsakYtelseType fagsakYtelseType;

    @NotNull
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "AKTIV", nullable = false)
    private boolean aktiv = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "SYSTEM")
    private Fagsystem system;

    @Column(name = "OPPGAVE_AVSLUTTET")
    private LocalDateTime oppgaveAvsluttet;

    @Embedded
    private BehandlingId behandlingId;

    @OneToOne(mappedBy = "oppgave")
    private Reservasjon reservasjon;

    @Column(name = "FEILUTBETALING_BELOP")
    private BigDecimal feilutbetalingBelop;

    @Column(name = "FEILUTBETALING_START")
    private LocalDate feilutbetalingStart;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public Oppgave() {
        // Hibernate
    }

    public void leggTilOppgaveEgenskap(OppgaveEgenskap oppgaveEgenskap) {
        Objects.requireNonNull(oppgaveEgenskap, "oppgaveEgenskap");
        oppgaveEgenskaper.removeIf(oe -> oe.getAndreKriterierType().equals(oppgaveEgenskap.getAndreKriterierType()));
        oppgaveEgenskaper.add(oppgaveEgenskap);
        oppgaveEgenskap.setOppgave(this);
    }

    public void clearOppgaveEgenskaper() {
        oppgaveEgenskaper.clear();
        // legger til denne for migrering
    }

    public Long getId() {
        return id;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public String getBehandlendeEnhet() {
        return behandlendeEnhet;
    }

    public boolean getAktiv() {
        return aktiv;
    }

    public Fagsystem getSystem() {
        return system;
    }

    public BehandlingId getBehandlingId() {
        return behandlingId;
    }

    public LocalDate getBehandlingsfrist() {
        return behandlingsfrist;
    }

    public LocalDateTime getBehandlingOpprettet() {
        return behandlingOpprettet;
    }

    public LocalDate getFørsteStønadsdag() {
        return førsteStønadsdag;
    }

    public LocalDateTime getOppgaveAvsluttet() {
        return oppgaveAvsluttet;
    }

    public Reservasjon getReservasjon() {
        return reservasjon;
    }

    public Set<OppgaveEgenskap> getOppgaveEgenskaper() {
        return Collections.unmodifiableSet(oppgaveEgenskaper);
    }

    public BigDecimal getFeilutbetalingBelop() {
        return feilutbetalingBelop;
    }

    public LocalDate getFeilutbetalingStart() {
        return feilutbetalingStart;
    }

    public void avsluttOppgave() {
        aktiv = false;
        oppgaveAvsluttet = LocalDateTime.now();
    }

    public boolean harAktivReservasjon() {
        return reservasjon != null && reservasjon.erAktiv();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "Oppgave{" + "id=" + id + ", saksnummer=" + saksnummer + ", aktiv=" + aktiv + ", system='" + system + '\'' + '}';
    }

    public boolean harKriterie(AndreKriterierType kriterie) {
        return oppgaveEgenskaper.stream().anyMatch(egenskap -> egenskap.getAndreKriterierType() == kriterie);
    }

    // Setters added for migration purposes - can be removed after migration
    public void setSaksnummer(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBehandlingId(BehandlingId behandlingId) {
        this.behandlingId = behandlingId;
    }

    public void setBehandlingType(BehandlingType behandlingType) {
        this.behandlingType = behandlingType;
    }

    public void setFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
        this.fagsakYtelseType = fagsakYtelseType;
    }

    public void setBehandlendeEnhet(String behandlendeEnhet) {
        this.behandlendeEnhet = behandlendeEnhet;
    }

    public void setBehandlingsfrist(LocalDate behandlingsfrist) {
        this.behandlingsfrist = behandlingsfrist;
    }

    public void setBehandlingOpprettet(LocalDateTime behandlingOpprettet) {
        this.behandlingOpprettet = behandlingOpprettet;
    }

    public void setFørsteStønadsdag(LocalDate førsteStønadsdag) {
        this.førsteStønadsdag = førsteStønadsdag;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public void setSystem(Fagsystem system) {
        this.system = system;
    }

    public void setOppgaveAvsluttet(LocalDateTime oppgaveAvsluttet) {
        this.oppgaveAvsluttet = oppgaveAvsluttet;
    }

    public void setFeilutbetalingBelop(BigDecimal feilutbetalingBelop) {
        this.feilutbetalingBelop = feilutbetalingBelop;
    }

    public void setFeilutbetalingStart(LocalDate feilutbetalingStart) {
        this.feilutbetalingStart = feilutbetalingStart;
    }

    public static class Builder {
        private final Oppgave tempOppgave;

        Builder() {
            tempOppgave = new Oppgave();
        }

        public Builder medBehandlingId(BehandlingId behandlingId) {
            tempOppgave.behandlingId = behandlingId;
            return this;
        }

        public Builder medSaksnummer(Saksnummer saksnummer) {
            tempOppgave.saksnummer = saksnummer;
            return this;
        }

        public Builder medAktørId(AktørId aktørId) {
            tempOppgave.aktørId = aktørId;
            return this;
        }

        public Builder medBehandlendeEnhet(String behandlendeEnhet) {
            tempOppgave.behandlendeEnhet = behandlendeEnhet;
            return this;
        }

        public Builder medAktiv(boolean aktiv) {
            tempOppgave.aktiv = aktiv;
            return this;
        }

        public Builder medBehandlingType(BehandlingType behandlingType) {
            tempOppgave.behandlingType = behandlingType;
            return this;
        }

        public Builder medSystem(Fagsystem fagsystem) {
            tempOppgave.system = fagsystem;
            return this;
        }

        public Builder medBehandlingsfrist(LocalDate behandlingsfrist) {
            tempOppgave.behandlingsfrist = behandlingsfrist;
            return this;
        }

        public Builder medBehandlingOpprettet(LocalDateTime behandlingOpprettet) {
            tempOppgave.behandlingOpprettet = behandlingOpprettet;
            return this;
        }

        public Builder medFørsteStønadsdag(LocalDate førsteStønadsdag) {
            tempOppgave.førsteStønadsdag = førsteStønadsdag;
            return this;
        }

        public Builder medFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
            tempOppgave.fagsakYtelseType = fagsakYtelseType;
            return this;
        }

        public Builder medFeilutbetalingBeløp(BigDecimal feilutbetalingBeløp) {
            tempOppgave.feilutbetalingBelop = feilutbetalingBeløp;
            return this;
        }

        public Builder medFeilutbetalingStart(LocalDate feilutbetalingStart) {
            tempOppgave.feilutbetalingStart = feilutbetalingStart;
            return this;
        }

        public Builder medKriterier(Set<OppgaveEgenskap> egenskaper) {
            egenskaper.forEach(tempOppgave::leggTilOppgaveEgenskap);
            return this;
        }

        public Builder dummyOppgave(String enhet) {
            tempOppgave.behandlingId = new BehandlingId(UUID.nameUUIDFromBytes("331133L".getBytes()));
            tempOppgave.saksnummer = new Saksnummer("3478293");
            tempOppgave.aktørId = AktørId.dummy();
            tempOppgave.fagsakYtelseType = FagsakYtelseType.FORELDREPENGER;
            tempOppgave.behandlingType = BehandlingType.FØRSTEGANGSSØKNAD;
            tempOppgave.behandlendeEnhet = enhet;
            tempOppgave.behandlingsfrist = LocalDate.now();
            tempOppgave.behandlingOpprettet = LocalDateTime.now();
            tempOppgave.førsteStønadsdag = LocalDate.now().plusMonths(1);
            return this;
        }

        public Oppgave build() {
            Objects.requireNonNull(tempOppgave.saksnummer, "saksnummer");
            return tempOppgave;
        }
    }
}
