package no.nav.foreldrepenger.los.domene.typer.aktør;

import java.util.Objects;

public record Person(AktørId aktørId, Fødselsnummer fødselsnummer, String navn) {

    public Person {
        Objects.requireNonNull(navn);
        Objects.requireNonNull(fødselsnummer);
        Objects.requireNonNull(aktørId);
    }

    public Person(Person person) {
        this(person.aktørId(), person.fødselsnummer(), person.navn());
    }

}
