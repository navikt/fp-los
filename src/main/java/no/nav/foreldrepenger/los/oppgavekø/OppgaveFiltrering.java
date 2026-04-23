package no.nav.foreldrepenger.los.oppgavekø;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.los.felles.BaseEntitet;
import no.nav.foreldrepenger.los.oppgave.AndreKriterierType;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.oppgave.Periodefilter;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;


@Entity(name = "OppgaveFiltrering")
@Table(name = "OPPGAVE_FILTRERING")
public class OppgaveFiltrering extends BaseEntitet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GLOBAL_PK")
    private Long id;

    @NotNull
    @Column(name = "navn", nullable = false)
    private String navn;

    @Column(name = "BESKRIVELSE")
    private String beskrivelse;

    @NotNull
    @Column(name = "sortering", nullable = false)
    @Enumerated(EnumType.STRING)
    private KøSortering sortering;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "FILTRERING_BEHANDLING_TYPE",
        joinColumns = @JoinColumn(name = "OPPGAVE_FILTRERING_ID")
    )
    @Column(name = "BEHANDLING_TYPE")
    @Enumerated(EnumType.STRING)
    @Fetch(FetchMode.SUBSELECT)
    private Set<BehandlingType> filtreringBehandlingTyper = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "FILTRERING_YTELSE_TYPE",
        joinColumns = @JoinColumn(name = "OPPGAVE_FILTRERING_ID")
    )
    @Column(name = "FAGSAK_YTELSE_TYPE")
    @Enumerated(EnumType.STRING)
    @Fetch(FetchMode.SUBSELECT)
    private Set<FagsakYtelseType> filtreringYtelseTyper = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "FILTRERING_ANDRE_KRITERIER",
        joinColumns = @JoinColumn(name = "OPPGAVE_FILTRERING_ID")
    )
    @Fetch(FetchMode.SUBSELECT)
    private Set<FiltreringAndreKriterierType> andreKriterierTyper = new HashSet<>();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "AVDELING_ID", nullable = false, updatable = false)
    private Avdeling avdeling;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "PERIODEFILTER_TYPE", nullable = false)
    private Periodefilter periodefilter = Periodefilter.FAST_PERIODE;

    @Column(name = "FOM_DATO")
    private LocalDate fomDato;

    @Column(name = "TOM_DATO")
    private LocalDate tomDato;

    @Column(name = "FOM_DAGER")
    private Long fra;

    @Column(name = "TOM_DAGER")
    private Long til;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    protected OppgaveFiltrering() {
        // Hibernate
    }

    public OppgaveFiltrering(String navn, KøSortering sortering, Avdeling avdeling) {
        Objects.requireNonNull(navn, "navn");
        Objects.requireNonNull(sortering, "sortering");
        Objects.requireNonNull(avdeling, "avdeling");
        this.navn = navn;
        this.sortering = sortering;
        this.avdeling = avdeling;
    }

    public Long getId() {
        return id;
    }

    public String getNavn() {
        return navn;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public KøSortering getSortering() {
        return sortering;
    }

    public List<BehandlingType> getBehandlingTyper() {
        return filtreringBehandlingTyper.stream().toList();
    }

    public List<FagsakYtelseType> getFagsakYtelseTyper() {
        return filtreringYtelseTyper.stream().toList();
    }

    public List<FiltreringAndreKriterierType> getFiltreringAndreKriterierTyper() {
        return andreKriterierTyper.stream().toList();
    }

    public Avdeling getAvdeling() {
        return avdeling;
    }

    public Periodefilter getPeriodefilter() {
        return periodefilter;
    }

    public LocalDate getFomDato() {
        return fomDato;
    }

    public LocalDate getTomDato() {
        return tomDato;
    }

    public Long getFra() {
        return fra;
    }

    public Long getTil() {
        return til;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public void setAvdeling(Avdeling avdeling) {
        this.avdeling = avdeling;
    }

    public void setSortering(KøSortering sortering) {
        this.sortering = sortering;
    }

    public void setFiltreringBehandlingTyper(Set<BehandlingType> behandlingTyper) {
        this.filtreringBehandlingTyper.clear();
        this.filtreringBehandlingTyper.addAll(behandlingTyper);
    }

    public void setFiltreringYtelseTyper(Set<FagsakYtelseType> fagsakYtelseTyper) {
        this.filtreringYtelseTyper.clear();
        this.filtreringYtelseTyper.addAll(fagsakYtelseTyper);
    }

    public void setAndreKriterierTyper(Set<AndreKriterierType> inkluder, Set<AndreKriterierType> ekskluder) {
        this.andreKriterierTyper.clear();
        inkluder.stream()
            .map(type -> new FiltreringAndreKriterierType(type, true))
            .forEach(a -> this.andreKriterierTyper.add(a));
        ekskluder.stream()
            .map(type -> new FiltreringAndreKriterierType(type, false))
            .forEach(a -> this.andreKriterierTyper.add(a));
    }

    public void setPeriodefilter(Periodefilter periodefilter) {
        this.periodefilter = periodefilter;
    }

    public void setFomDato(LocalDate fomDato) {
        this.fomDato = fomDato;
    }

    public void setTomDato(LocalDate tomDato) {
        this.tomDato = tomDato;
    }

    public void setFra(Long fra) {
        this.fra = fra;
    }

    public void setTil(Long til) {
        this.til = til;
    }

}
