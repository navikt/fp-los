package no.nav.foreldrepenger.los.oppgave;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
    @NaturalId
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

    public Behandling() {
        // Hibernate
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
        return new Builder(behandling.orElseGet(Behandling::new));
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
        return "Oppgave{" + "id=" + id.toString() + ", saksnummer=" + saksnummer + ", kildeSystem=" + kildeSystem + '}';
    }

    public static class Builder {
        private final Behandling behandlingKladd;

        private Builder(Behandling behandling) {
            behandlingKladd = behandling;
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

        public Builder dummyBehandling(String enhet, BehandlingTilstand tilstand) {
            behandlingKladd.id = UUID.nameUUIDFromBytes("331133L".getBytes());
            behandlingKladd.saksnummer = new Saksnummer("3478293");
            behandlingKladd.aktørId = AktørId.dummy();
            behandlingKladd.fagsakYtelseType = FagsakYtelseType.FORELDREPENGER;
            behandlingKladd.behandlingType = BehandlingType.FØRSTEGANGSSØKNAD;
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
    }
}
