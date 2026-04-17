# FP-LOS
===============

[![Bygg og deploy](https://github.com/navikt/fp-los/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/navikt/fp-los/actions/workflows/build.yml)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-los&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=navikt_fp-los)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-los&metric=coverage)](https://sonarcloud.io/summary/new_code?id=navikt_fp-los)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-los&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fp-los)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-los&metric=bugs)](https://sonarcloud.io/dashboard?id=navikt_fp-los)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-los&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=navikt_fp-los)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-los&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=navikt_fp-los)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-los&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=navikt_fp-los)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-los&metric=sqale_index)](https://sonarcloud.io/dashboard?id=navikt_fp-los)

FP-LOS håndterer oppgave- og ledelsesstyring på foreldrepengeområdet. Fp-sak og fp-tilbake produserer hendelser ved endringer i tilstand i behandlingsprosessen. FP-LOS lytter til hendelsene for å dekke behovet for oppgavestyring og statistikk. 

Oppgavestyrere definerer kriterier som ligger til grunn for køer som fordeler oppgaver etter prioritet til saksbehandlere. 

https://confluence.adeo.no/display/TVF/FP-LOS

## Kjøring lokalt

`no.nav.foreldrepenger.los.JettyDevServer` started i Intellij. Lokalt så går den mot Virtuell Tjenesteplattform (VTP).

### Sikkerhet
Det er mulig å kalle tjenesten med bruk av følgende tokens
- Azure CC
- Azure OBO med følgende rettigheter:
    - fpsak-saksbehandler
    - fpsak-veileder
    - fpsak-oppgavestyrer
    - fpsak-drift
