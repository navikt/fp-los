package no.nav.foreldrepenger.los.oppgave;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import no.nav.foreldrepenger.los.migrering.gcp.SequenceOrAssigned;
import no.nav.foreldrepenger.los.migrering.gcp.SequenceOrAssignedMarker;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;
import no.nav.foreldrepenger.los.reservasjon.Reservasjon;

@Entity(name = "Oppgave")
@Table(name = "OPPGAVE")
public class Oppgave extends BaseEntitet implements SequenceOrAssignedMarker<Long> {

    @Id
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GLOBAL_PK")
    // bruker en custom IdGenerator for å kunne sette PK ved migrering
    @SequenceOrAssigned(sequence = "SEQ_GLOBAL_PK")
    private Long id;

    @NotNull
    @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}")
    @Column(name = "BEHANDLENDE_ENHET", nullable = false)
    private String behandlendeEnhet;

    @BatchSize(size = 100)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "OPPGAVE_EGENSKAP", joinColumns = @JoinColumn(name = "OPPGAVE_ID"))
    private Set<OppgaveEgenskap> oppgaveEgenskaper = new HashSet<>();

    @NotNull
    @Column(name = "AKTIV", nullable = false)
    private boolean aktiv = true;

    @Column(name = "OPPGAVE_AVSLUTTET")
    private LocalDateTime oppgaveAvsluttet;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "BEHANDLING_ID", nullable = false)
    private Behandling behandling;

    @OneToOne(mappedBy = "oppgave")
    private Reservasjon reservasjon;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    protected Oppgave() {
        // Hibernate
    }

    public Oppgave(Behandling behandling, String behandlendeEnhet) {
        Objects.requireNonNull(behandling, "behandling");
        Objects.requireNonNull(behandlendeEnhet, "behandlendeEnhet");
        this.behandling = behandling;
        this.behandlendeEnhet = behandlendeEnhet;
    }

    public void leggTilOppgaveEgenskap(AndreKriterierType andreKriterierType, String ansvarligSaksbehandlerForTotrinn) {
        Objects.requireNonNull(andreKriterierType, "andreKriterierType");
        var eksisterendeEgenskap = oppgaveEgenskaper.stream()
            .filter(oe -> andreKriterierType.equals(oe.andreKriterierType()))
            .findFirst()
            .orElse(null);

        if (andreKriterierType.erTilBeslutter()) {
            Objects.requireNonNull(ansvarligSaksbehandlerForTotrinn, "ansvarligSaksbehandlerForTotrinn");
            if (eksisterendeEgenskap != null) {
                if (Objects.equals(ansvarligSaksbehandlerForTotrinn.trim().toUpperCase(), eksisterendeEgenskap.sisteSaksbehandlerForTotrinn())) {
                    return; // ingen endring
                } else {
                    oppgaveEgenskaper.remove(eksisterendeEgenskap);
                }
            }
            oppgaveEgenskaper.add(new OppgaveEgenskap(andreKriterierType, ansvarligSaksbehandlerForTotrinn));
        } else {
            if (eksisterendeEgenskap == null) {
                oppgaveEgenskaper.add(new OppgaveEgenskap(andreKriterierType, null));
            }
        }
    }

    public void beholdKunOppgaveEgenskaper(Set<AndreKriterierType> oppgaveEgenskapTyper) {
        var typerSomSkalBeholdes = oppgaveEgenskapTyper == null
            || oppgaveEgenskapTyper.isEmpty()
            ? EnumSet.noneOf(AndreKriterierType.class)
            : EnumSet.copyOf(oppgaveEgenskapTyper);
        oppgaveEgenskaper.removeIf(oe -> !typerSomSkalBeholdes.contains(oe.andreKriterierType()));
    }

    public Long getId() {
        return id;
    }

    public Saksnummer getSaksnummer() {
        return getBehandling().getSaksnummer();
    }

    public AktørId getAktørId() {
        return getBehandling().getAktørId();
    }

    public BehandlingType getBehandlingType() {
        return getBehandling().getBehandlingType();
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return getBehandling().getFagsakYtelseType();
    }

    public String getBehandlendeEnhet() {
        return behandlendeEnhet;
    }

    public boolean getAktiv() {
        return aktiv;
    }

    public Fagsystem getSystem() {
        return getBehandling().getKildeSystem();
    }

    public BehandlingId getBehandlingId() {
        return new BehandlingId(behandling.getId());
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public LocalDate getBehandlingsfrist() {
        return getBehandling().getBehandlingsfrist();
    }

    public LocalDateTime getBehandlingOpprettet() {
        return getBehandling().getOpprettet();
    }

    public LocalDate getFørsteStønadsdag() {
        return getBehandling().getFørsteStønadsdag();
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
        return getBehandling().getFeilutbetalingBelop();
    }

    public LocalDate getFeilutbetalingStart() {
        return getBehandling().getFeilutbetalingStart();
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
        return "Oppgave{" + "id=" + id + ", behandling=" + behandling.getId() + ", aktiv=" + aktiv + ", type=" + getBehandlingType() + '}';
    }

    public boolean harKriterie(AndreKriterierType kriterie) {
        return oppgaveEgenskaper.stream().anyMatch(egenskap -> egenskap.andreKriterierType() == kriterie);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    public void setBehandlendeEnhet(String behandlendeEnhet) {
        this.behandlendeEnhet = behandlendeEnhet;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public void setOppgaveAvsluttet(LocalDateTime oppgaveAvsluttet) {
        this.oppgaveAvsluttet = oppgaveAvsluttet;
    }

    public static class Builder {
        private final Oppgave tempOppgave;

        Builder() {
            tempOppgave = new Oppgave();
        }

        public Builder medBehandling(Behandling behandling) {
            tempOppgave.behandling = behandling;
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

        public Builder medKriterier(Set<AndreKriterierType> kriterier, String ansvarligSaksbehandlerForTotrinn) {
            kriterier.forEach(k -> tempOppgave.leggTilOppgaveEgenskap(k, ansvarligSaksbehandlerForTotrinn));
            return this;
        }

        public Builder dummyOppgave(String enhet, Behandling behandling) {
            tempOppgave.behandling = behandling;
            tempOppgave.behandlendeEnhet = enhet;
            return this;
        }

        public Oppgave build() {
            Objects.requireNonNull(tempOppgave.behandling, "behandling");
            return tempOppgave;
        }
    }
}
