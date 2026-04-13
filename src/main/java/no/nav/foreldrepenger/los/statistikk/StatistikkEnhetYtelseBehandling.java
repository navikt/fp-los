package no.nav.foreldrepenger.los.statistikk;


import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;

@Entity
@Table(name = "STAT_ENHET_YTELSE_BEHANDLING")
public class StatistikkEnhetYtelseBehandling implements Serializable {

    @EmbeddedId
    @Valid
    private StatistikkEnhetYtelseBehandlingNøkkel nøkkel;

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
        this.nøkkel = new StatistikkEnhetYtelseBehandlingNøkkel(behandlendeEnhet, tidsstempel, fagsakYtelseType, behandlingType);
        this.statistikkDato = statistikkDato;
        this.antallAktive = antallAktive;
        this.antallOpprettet = antallOpprettet;
        this.antallAvsluttet = antallAvsluttet;
    }

    public String getBehandlendeEnhet() {
        return nøkkel.behandlendeEnhet();
    }

    public Long getTidsstempel() {
        return nøkkel.tidsstempel();
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return nøkkel.fagsakYtelseType();
    }

    public BehandlingType getBehandlingType() {
        return nøkkel.behandlingType();
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
        return "StatistikkEnhetYtelseBehandling{" + "enhetYtelseBehandlingId=" + nøkkel + ", statistikkDato=" + statistikkDato
            + ", antallAktive=" + antallAktive + ", antallOpprettet=" + antallOpprettet + ", antallAvsluttet=" + antallAvsluttet + '}';
    }
}
