package no.nav.foreldrepenger.los.statistikk.kø;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "STAT_OPPGAVE_FILTER")
public class StatistikkOppgaveFilter implements Serializable {

    @EmbeddedId
    @Valid
    private StatistikkOppgaveFilterNøkkel nøkkel;

    @NotNull
    @Column(name = "STAT_DATO", updatable = false, nullable = false)
    private LocalDate statistikkDato;

    @NotNull
    @Column(name = "ANTALL_AKTIVE", updatable = false, nullable = false)
    private Integer antallAktive;

    @NotNull
    @Column(name = "ANTALL_TILGJENGELIGE", updatable = false, nullable = false)
    private Integer antallTilgjengelige;

    @Column(name = "ANTALL_VENTENDE")
    private Integer antallVentende;

    @Column(name = "ANTALL_OPPRETTET")
    private Integer antallOpprettet;

    @Column(name = "ANTALL_AVSLUTTET")
    private Integer antallAvsluttet;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "INNSLAG_TYPE", updatable = false, nullable = false)
    private InnslagType innslagType;

    public StatistikkOppgaveFilter() {
        // for hibernate
    }

    public StatistikkOppgaveFilter(Long oppgaveFilterId,
                                   Long tidsstempel,
                                   LocalDate statistikkDato,
                                   Integer antallAktive,
                                   Integer antallTilgjengelige,
                                   Integer antallVentende,
                                   Integer antallOpprettet,
                                   Integer antallAvsluttet,
                                   InnslagType innslagType) {
        this.nøkkel = new StatistikkOppgaveFilterNøkkel(oppgaveFilterId, tidsstempel);
        this.statistikkDato = statistikkDato;
        this.antallAktive = antallAktive;
        this.antallTilgjengelige = antallTilgjengelige;
        this.antallVentende = antallVentende;
        this.innslagType = innslagType;
        this.antallOpprettet = antallOpprettet;
        this.antallAvsluttet = antallAvsluttet;
    }

    public Long getTidsstempel() {
        return nøkkel.tidsstempel();
    }

    public Long getOppgaveFilterId() {
        return nøkkel.oppgaveFilterId();
    }

    public LocalDate getStatistikkDato() {
        return statistikkDato;
    }

    public Integer getAntallAktive() {
        return antallAktive;
    }

    public Integer getAntallTilgjengelige() {
        return antallTilgjengelige;
    }

    public  Integer getAntallVentende() {
        return Optional.ofNullable(antallVentende).orElse(0);
    }

    public InnslagType getInnslagType() {
        return innslagType;
    }

    public Integer getAntallOpprettet() {
        return antallOpprettet;
    }

    public Integer getAntallAvsluttet() {
        return Optional.ofNullable(antallAvsluttet).orElse(0);
    }

    @Override
    public String toString() {
        return "StatistikkOppgaveFilter{" + "nøkkel=" + nøkkel + ", statistikkDato=" + statistikkDato + ", antallAktive=" + antallAktive
            + ", antallTilgjengelige=" + antallTilgjengelige + ", antallVentende=" + antallVentende + ", antallOpprettet=" + antallOpprettet
            + ", antallAvsluttet=" + antallAvsluttet + ", innslagType=" + innslagType + '}';
    }
}
