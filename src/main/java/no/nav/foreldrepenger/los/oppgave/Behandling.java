package no.nav.foreldrepenger.los.oppgave;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.domene.typer.Fagsystem;
import no.nav.foreldrepenger.los.domene.typer.Saksnummer;
import no.nav.foreldrepenger.los.domene.typer.aktør.AktørId;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;

@Entity(name = "Behandling")
@Table(name = "BEHANDLING")
public class Behandling extends BaseEntitet {

    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    private UUID id;

    @Embedded
    @NotNull
    private Saksnummer saksnummer;

    @Embedded
    @NotNull
    private AktørId aktørId;

    @NotNull
    @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}")
    @Column(name = "BEHANDLENDE_ENHET", nullable = false)
    private String behandlendeEnhet;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "KILDESYSTEM", nullable = false)
    private Fagsystem kildeSystem;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "FAGSAK_YTELSE_TYPE", nullable = false)
    private FagsakYtelseType fagsakYtelseType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "BEHANDLING_TYPE", nullable = false)
    private BehandlingType behandlingType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "BEHANDLING_TILSTAND", nullable = false)
    private BehandlingTilstand behandlingTilstand;

    @Column(name = "AKTIVE_AKSJONSPUNKT")
    private String aktiveAksjonspunkt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "BEHANDLING_EGENSKAP",
        joinColumns = @JoinColumn(name = "BEHANDLING_ID")
    )
    @Column(name = "ANDRE_KRITERIER_TYPE")
    @Enumerated(EnumType.STRING)
    private Set<AndreKriterierType> behandlingEgenskaper = new HashSet<>();

    @Column(name = "VENTEFRIST")
    private LocalDateTime ventefrist;

    @Column(name = "OPPRETTET")
    private LocalDateTime opprettet;

    @Column(name = "AVSLUTTET")
    private LocalDateTime avsluttet;

    @Column(name = "BEHANDLINGSFRIST")
    private LocalDate behandlingsfrist;

    @Column(name = "FORSTE_STONADSDAG")
    private LocalDate førsteStønadsdag;

    @Column(name = "FEILUTBETALING_BELOP")
    private BigDecimal feilutbetalingBelop;

    @Column(name = "FEILUTBETALING_START")
    private LocalDate feilutbetalingStart;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    protected Behandling() {
        // Hibernate
    }

    public Behandling(UUID id, Saksnummer saksnummer, AktørId aktørId, String behandlendeEnhet,
                      Fagsystem kildeSystem, FagsakYtelseType fagsakYtelseType,
                      BehandlingType behandlingType, BehandlingTilstand behandlingTilstand) {
        this.id = Objects.requireNonNull(id, "id");
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
        this.aktørId = Objects.requireNonNull(aktørId, "aktørId");
        this.behandlendeEnhet = Objects.requireNonNull(behandlendeEnhet, "behandlendeEnhet");
        this.kildeSystem = Objects.requireNonNull(kildeSystem, "kildeSystem");
        this.fagsakYtelseType = Objects.requireNonNull(fagsakYtelseType, "fagsakYtelseType");
        this.behandlingType = Objects.requireNonNull(behandlingType, "behandlingType");
        this.behandlingTilstand = Objects.requireNonNull(behandlingTilstand, "behandlingTilstand");
    }

    public UUID getId() {
        return id;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public String getBehandlendeEnhet() {
        return behandlendeEnhet;
    }

    public Fagsystem getKildeSystem() {
        return kildeSystem;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public BehandlingTilstand getBehandlingTilstand() {
        return behandlingTilstand;
    }

    public String getAktiveAksjonspunkt() {
        return aktiveAksjonspunkt;
    }

    public Set<AndreKriterierType> getKriterier() {
        return new HashSet<>(behandlingEgenskaper);
    }

    public LocalDateTime getVentefrist() {
        return ventefrist;
    }

    public LocalDateTime getOpprettet() {
        return opprettet;
    }

    public LocalDateTime getAvsluttet() {
        return avsluttet;
    }

    public LocalDate getBehandlingsfrist() {
        return behandlingsfrist;
    }

    public LocalDate getFørsteStønadsdag() {
        return førsteStønadsdag;
    }

    public BigDecimal getFeilutbetalingBelop() {
        return feilutbetalingBelop;
    }

    public LocalDate getFeilutbetalingStart() {
        return feilutbetalingStart;
    }


    public static Builder builder(Optional<Behandling> behandling) {
        return behandling.map(Builder::oppdater).orElseGet(Builder::ny);
    }

    // Setters added for migration purposes - can be removed after migration
    public void setId(UUID id) {
        this.id = id;
    }

    public void setSaksnummer(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    public void setBehandlendeEnhet(String behandlendeEnhet) {
        this.behandlendeEnhet = behandlendeEnhet;
    }

    public void setFagsystem(Fagsystem kildeSystem) {
        this.kildeSystem = kildeSystem;
    }

    public void setFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
        this.fagsakYtelseType = fagsakYtelseType;
    }

    public void setBehandlingType(BehandlingType behandlingType) {
        this.behandlingType = behandlingType;
    }

    public void setBehandlingTilstand(BehandlingTilstand behandlingTilstand) {
        this.behandlingTilstand = behandlingTilstand;
    }

    public void setAktiveAksjonspunkt(String aktiveAksjonspunkt) {
        this.aktiveAksjonspunkt = aktiveAksjonspunkt;
    }

    public void setKriterier(Set<AndreKriterierType> kriterier) {
        this.behandlingEgenskaper.clear();
        this.behandlingEgenskaper.addAll(kriterier);
    }

    public void leggTilKriterie(AndreKriterierType kriterie) {
        Objects.requireNonNull(kriterie, "kriterie");
        if (!this.behandlingEgenskaper.contains(kriterie)) {
            behandlingEgenskaper.add(kriterie);
        }
    }

    public void setVentefrist(LocalDateTime ventefrist) {
        this.ventefrist = ventefrist;
    }

    public void setOpprettet(LocalDateTime opprettet) {
        this.opprettet = opprettet;
    }

    public void setAvsluttet(LocalDateTime avsluttet) {
        this.avsluttet = avsluttet;
    }

    public void setBehandlingsfrist(LocalDate behandlingsfrist) {
        this.behandlingsfrist = behandlingsfrist;
    }

    public void setFørsteStønadsdag(LocalDate førsteStønadsdag) {
        this.førsteStønadsdag = førsteStønadsdag;
    }

    public void setFeilutbetalingBelop(BigDecimal feilutbetalingBelop) {
        this.feilutbetalingBelop = feilutbetalingBelop;
    }

    public void setFeilutbetalingStart(LocalDate feilutbetalingStart) {
        this.feilutbetalingStart = feilutbetalingStart;
    }

    @Override
    public String toString() {
        return "Behandling{" + "id=" + id.toString() + ", saksnummer=" + saksnummer + ", kildeSystem=" + kildeSystem + '}';
    }

    public static class Builder {
        private final Behandling behandlingKladd;
        private final boolean erOppdatering;

        private Builder(Behandling behandling, boolean erOppdatering) {
            this.behandlingKladd = behandling;
            this.erOppdatering = erOppdatering;
        }

        private static Builder oppdater(Behandling behandling) {
            return new Builder(behandling, true);
        }

        private static Builder ny() {
            return new Builder(new Behandling(), false);
        }

        public Builder medId(UUID id) {
            behandlingKladd.id = id;
            return this;
        }

        public Builder medSaksnummer(Saksnummer saksnummer) {
            behandlingKladd.saksnummer = saksnummer;
            return this;
        }

        public Builder medAktørId(AktørId aktørId) {
            behandlingKladd.aktørId = aktørId;
            return this;
        }

        public Builder medBehandlendeEnhet(String behandlendeEnhet) {
            behandlingKladd.behandlendeEnhet = behandlendeEnhet;
            return this;
        }

        public Builder medKildeSystem(Fagsystem fagsystem) {
            behandlingKladd.kildeSystem = fagsystem;
            return this;
        }

        public Builder medFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
            behandlingKladd.fagsakYtelseType = fagsakYtelseType;
            return this;
        }

        public Builder medBehandlingType(BehandlingType behandlingType) {
            behandlingKladd.behandlingType = behandlingType;
            return this;
        }

        public Builder medBehandlingTilstand(BehandlingTilstand behandlingTilstand) {
            behandlingKladd.behandlingTilstand = behandlingTilstand;
            return this;
        }

        public Builder medAktiveAksjonspunkt(String aktiveAksjonspunkt) {
            behandlingKladd.aktiveAksjonspunkt = aktiveAksjonspunkt;
            return this;
        }

        public Builder medVentefrist(LocalDateTime ventefrist) {
            behandlingKladd.ventefrist = ventefrist;
            return this;
        }

        public Builder medOpprettet(LocalDateTime behandlingOpprettet) {
            behandlingKladd.opprettet = behandlingOpprettet;
            return this;
        }

        public Builder medAvsluttet(LocalDateTime behandlingAvsluttet) {
            behandlingKladd.avsluttet = behandlingAvsluttet;
            return this;
        }

        public Builder medBehandlingsfrist(LocalDate behandlingsfrist) {
            behandlingKladd.behandlingsfrist = behandlingsfrist;
            return this;
        }

        public Builder medFørsteStønadsdag(LocalDate førsteStønadsdag) {
            behandlingKladd.førsteStønadsdag = førsteStønadsdag;
            return this;
        }

        public Builder medFeilutbetalingBelop(BigDecimal feilutbetalingBelop) {
            behandlingKladd.feilutbetalingBelop = feilutbetalingBelop;
            return this;
        }

        public Builder medFeilutbetalingStart(LocalDate feilutbetalingStart) {
            behandlingKladd.feilutbetalingStart = feilutbetalingStart;
            return this;
        }

        public Builder medKriterier(Set<AndreKriterierType> kriterier) {
            if (behandlingKladd.behandlingEgenskaper.size() != kriterier.size() || !behandlingKladd.behandlingEgenskaper.containsAll(kriterier)) {  // avoids delete+reinsert if identical
                behandlingKladd.behandlingEgenskaper.clear();
                behandlingKladd.behandlingEgenskaper.addAll(kriterier);
            }
            return this;
        }

        public Builder dummyBehandling(String enhet, BehandlingTilstand tilstand) {
            behandlingKladd.id = UUID.nameUUIDFromBytes("331133L".getBytes());
            behandlingKladd.saksnummer = new Saksnummer("3478293");
            behandlingKladd.aktørId = AktørId.dummy();
            behandlingKladd.fagsakYtelseType = FagsakYtelseType.FORELDREPENGER;
            behandlingKladd.behandlingType = BehandlingType.FØRSTEGANGSSØKNAD;
            behandlingKladd.kildeSystem = Fagsystem.FPSAK;
            behandlingKladd.behandlendeEnhet = enhet;
            behandlingKladd.behandlingsfrist = LocalDate.now();
            behandlingKladd.behandlingTilstand = tilstand;
            behandlingKladd.opprettet = LocalDateTime.now();
            behandlingKladd.førsteStønadsdag = LocalDate.now().plusMonths(1);
            return this;
        }

        public Behandling build() {
            Objects.requireNonNull(behandlingKladd.id, "id");
            Objects.requireNonNull(behandlingKladd.aktørId, "aktørId");
            Objects.requireNonNull(behandlingKladd.saksnummer, "saksnummer");
            Objects.requireNonNull(behandlingKladd.fagsakYtelseType, "fagsakYtelseType");
            Objects.requireNonNull(behandlingKladd.behandlingType, "behandlingType");
            Objects.requireNonNull(behandlingKladd.behandlendeEnhet, "behandlendeEnhet");
            Objects.requireNonNull(behandlingKladd.kildeSystem, "kildeSystem");
            Objects.requireNonNull(behandlingKladd.behandlingTilstand, "behandlingTilstand");
            Objects.requireNonNull(behandlingKladd.opprettet, "opprettet");
            if (!Fagsystem.FPTILBAKE.equals(behandlingKladd.kildeSystem) && (behandlingKladd.feilutbetalingStart != null
                || behandlingKladd.feilutbetalingBelop != null)) {
                throw new IllegalArgumentException("Utviklerfeil: Angitt tilbakebetalingsinformasjon i FPSAK-oppgave");
            }
            return behandlingKladd;
        }

        public boolean erOppdatering() {
            return erOppdatering;
        }
    }
}
