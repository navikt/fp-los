package no.nav.foreldrepenger.los.statistikk;


import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;

@Entity
@IdClass(EnhetYtelseBehandlingType.class)
@Table(name = "STAT_ENHET_YTELSE_BEHANDLING")
public class StatistikkEnhetYtelseBehandling implements Serializable {

    @Id
    @NotNull
    @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}")
    @Column(name = "BEHANDLENDE_ENHET", updatable = false, nullable = false)
    private String behandlendeEnhet;

    @Id
    @NotNull
    @Column(name = "TIDSSTEMPEL", updatable = false, nullable = false)
    private Long tidsstempel;

    @Id
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "FAGSAK_YTELSE_TYPE", updatable = false, nullable = false)
    private FagsakYtelseType fagsakYtelseType;

    @Id
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "BEHANDLING_TYPE", updatable = false, nullable = false)
    private BehandlingType behandlingType;

    @NotNull
    @Column(name = "STAT_DATO", updatable = false, nullable = false)
    private LocalDate statistikkDato;

    @NotNull
    @Column(name = "ANTALL_AKTIVE", updatable = false, nullable = false)
    private Integer antallAktive;

    @NotNull
    @Column(name = "ANTALL_OPPRETTET", updatable = false, nullable = false)
    private Integer antallOpprettet;

    @NotNull
    @Column(name = "ANTALL_AVSLUTTET", updatable = false, nullable = false)
    private Integer antallAvsluttet;

    public StatistikkEnhetYtelseBehandling() {
        // for hibernate
    }

    public StatistikkEnhetYtelseBehandling(String behandlendeEnhet,
                                           Long tidsstempel,
                                           FagsakYtelseType fagsakYtelseType,
                                           BehandlingType behandlingType,
                                           LocalDate statistikkDato,
                                           Integer antallAktive,
                                           Integer antallOpprettet,
                                           Integer antallAvsluttet) {
        this.behandlendeEnhet = behandlendeEnhet;
        this.tidsstempel = tidsstempel;
        this.fagsakYtelseType = fagsakYtelseType;
        this.behandlingType = behandlingType;
        this.statistikkDato = statistikkDato;
        this.antallAktive = antallAktive;
        this.antallOpprettet = antallOpprettet;
        this.antallAvsluttet = antallAvsluttet;
    }

    public String getBehandlendeEnhet() {
        return behandlendeEnhet;
    }

    public Long getTidsstempel() {
        return tidsstempel;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public LocalDate getStatistikkDato() {
        return statistikkDato;
    }

    public Integer getAntallAktive() {
        return antallAktive;
    }

    public Integer getAntallOpprettet() {
        return antallOpprettet;
    }

    public Integer getAntallAvsluttet() {
        return antallAvsluttet;
    }

    @Override
    public String toString() {
        return "StatistikkEnhetYtelseBehandling{" + "behandlendeEnhet='" + behandlendeEnhet + '\'' + ", tidsstempel=" + tidsstempel
            + ", fagsakYtelseType=" + fagsakYtelseType + ", behandlingType=" + behandlingType + ", statistikkDato=" + statistikkDato
            + ", antallAktive=" + antallAktive + ", antallOpprettet=" + antallOpprettet + ", antallAvsluttet=" + antallAvsluttet + '}';
    }
}
