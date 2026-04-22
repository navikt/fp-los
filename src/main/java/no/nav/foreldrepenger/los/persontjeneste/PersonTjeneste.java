package no.nav.foreldrepenger.los.persontjeneste;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.los.domene.typer.aktør.AktørId;
import no.nav.foreldrepenger.los.domene.typer.aktør.Fødselsnummer;
import no.nav.foreldrepenger.los.domene.typer.aktør.Person;
import no.nav.foreldrepenger.los.oppgave.Oppgave;
import no.nav.pdl.Folkeregisteridentifikator;
import no.nav.pdl.FolkeregisteridentifikatorResponseProjection;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.HentPersonBolkQueryRequest;
import no.nav.pdl.HentPersonBolkResultResponseProjection;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.person.FalskIdentitet;
import no.nav.vedtak.felles.integrasjon.person.PersonMappers;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class PersonTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(PersonTjeneste.class);

    private static final int DEFAULT_CACHE_SIZE = 10000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS);

    private final LRUCache<AktørId, Person> cacheAktørIdTilPerson = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);

    private final PdlSystemKlient pdlSystem;

    public PersonTjeneste() {
        this.pdlSystem = new PdlSystemKlient();
    }

    public Map<Long, Person> hentPersoner(List<Oppgave> oppgaver) {
        var resultat = new HashMap<Long, Person>();
        var mangler = new ArrayList<Oppgave>();
        for (var oppgave : oppgaver) {
            var cachedPerson = cacheAktørIdTilPerson.get(oppgave.getAktørId());
            if (cachedPerson == null) {
                mangler.add(oppgave);
            } else {
                cacheAktørIdTilPerson.put(oppgave.getAktørId(), cachedPerson);
                resultat.put(oppgave.getId(), cachedPerson);
            }
        }
        if (mangler.isEmpty()) {
            return resultat;
        }
        var hentes = mangler.stream().map(Oppgave::getAktørId).map(AktørId::getId).collect(Collectors.toSet());
        var funnet = hentPersonerFraPdl(hentes.stream().toList());
        funnet.forEach(p -> cacheAktørIdTilPerson.put(p.aktørId(), new Person(p)));
        for (var oppgave : mangler) {
            var cachedPerson = cacheAktørIdTilPerson.get(oppgave.getAktørId());
            if (cachedPerson != null) {
                resultat.put(oppgave.getId(), cachedPerson);
            }
        }
        return resultat;
    }

    private List<Person> hentPersonerFraPdl(List<String> aktørId) {
        try {
            var bolkrequest = new HentPersonBolkQueryRequest();
            bolkrequest.setIdenter(aktørId);
            var projeksjonBolk = new HentPersonBolkResultResponseProjection().ident()
                .person(new PersonResponseProjection().navn(new NavnResponseProjection().fornavn().mellomnavn().etternavn())
                    .folkeregisteridentifikator(new FolkeregisteridentifikatorResponseProjection().identifikasjonsnummer().status().type()));
            return pdlSystem.hentPersonBolk(bolkrequest, projeksjonBolk).stream()
                .filter(r -> r.getPerson() != null)
                .map(this::mapPerson)
                .flatMap(Optional::stream)
                .toList();
        } catch (Exception e) {
            LOG.warn("Feil ved henting av personer fra PDL", e);
            return List.of();
        }
    }

    private Optional<Person> mapPerson(no.nav.pdl.HentPersonBolkResult resultat) {
        var aktørId = new AktørId(resultat.getIdent());
        return fnr(resultat.getPerson().getFolkeregisteridentifikator(), resultat.getIdent())
            .map(fnr -> {
                if (harIdentifikator(resultat.getPerson().getFolkeregisteridentifikator())) {
                    return new Person(aktørId, fnr, PersonMappers.mapNavn(resultat.getPerson()).orElse("Ukjent Navn"));
                } else  {
                    // Falsk Identitet har navn i objekt. Utgått Identitet har Navn i Person
                    var falskIdentitetNavn = hentNavnForFalskIdentitet(resultat.getIdent())
                        .or(() -> PersonMappers.mapNavn(resultat.getPerson()));
                    return new Person(aktørId, fnr, falskIdentitetNavn.orElse("Ukjent Navn"));
                }
            });
    }

    private static boolean harIdentifikator(List<Folkeregisteridentifikator> folkeregisteridentifikator) {
        return folkeregisteridentifikator.stream()
            .anyMatch(i -> i.getStatus().equals("I_BRUK"));
    }

    private Optional<Fødselsnummer> fnr(List<Folkeregisteridentifikator> folkeregisteridentifikator, String aktørId) {
        return folkeregisteridentifikator.stream()
            .filter(i -> i.getStatus().equals("I_BRUK"))
            .map(Folkeregisteridentifikator::getIdentifikasjonsnummer)
            .map(Fødselsnummer::new)
            .findFirst()
            .or(() -> hentFødselsnummerForAktørId(aktørId));
    }

    private Optional<Fødselsnummer> hentFødselsnummerForAktørId(String aktørId) {
        try {
            var request = new HentIdenterQueryRequest();
            request.setIdent(aktørId);
            request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT, IdentGruppe.NPID));
            request.setHistorikk(Boolean.FALSE);
            var projection = new IdentlisteResponseProjection().identer(new IdentInformasjonResponseProjection().ident());

            return pdlSystem.hentIdenter(request, projection).getIdenter().stream().findFirst()
                .map(IdentInformasjon::getIdent).map(Fødselsnummer::new);
        } catch (Exception e) {
            LOG.warn("Feil ved henting av identer fra PDL", e);
            return Optional.empty();
        }
    }

    private Optional<String> hentNavnForFalskIdentitet(String aktørId) {
        try {
            return FalskIdentitet.finnFalskIdentitet(aktørId, pdlSystem).map(FalskIdentitet.Informasjon::navn);
        } catch (Exception e) {
            LOG.warn("Feil ved henting av falsk identitet fra PDL", e);
            return Optional.empty();
        }
    }


}
