package no.nav.foreldrepenger.los.statistikk;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.los.oppgave.BehandlingType;
import no.nav.foreldrepenger.los.oppgave.FagsakYtelseType;
import no.nav.foreldrepenger.los.organisasjon.Avdeling;

@Embeddable
public record StatistikkEnhetYtelseBehandlingNøkkel(
    @NotNull
    @Pattern(regexp = Avdeling.VALID_AVDELING_ID, message = "Ugyldig enhetsnummer ${validatedValue}")
    @Column(name = "BEHANDLENDE_ENHET", updatable = false, nullable = false)
    String behandlendeEnhet,

    @NotNull
    @Column(name = "TIDSSTEMPEL", updatable = false, nullable = false)
    Long tidsstempel,

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "FAGSAK_YTELSE_TYPE", updatable = false, nullable = false)
    FagsakYtelseType fagsakYtelseType,

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "BEHANDLING_TYPE", updatable = false, nullable = false)
    BehandlingType behandlingType) {
}

