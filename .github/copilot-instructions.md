# fp-los

LOS = Ledelse Oppgave Styring: Oppgave- and queue-management application for case workers in the foreldrepenger area.

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context

| Topic | Details                                                                                       |
|---|-----------------------------------------------------------------------------------------------|
| Role | Entry point for case workers to find work items based on queue definitions and assignments    |
| Consumers | `fp-frontend` (work items, reservations), `fp-avdelingsleder` (org and queue definition)      |
| Tech stack        | Standard fp Java backend using `fp-prosesstask`                                               |
| Main integrations | `fp-sak`, `fptilbake`, `fp-tilgang`, PDL.                                                     |
| Data              | PostgreSQL; small and static organization + filter model; dynamic set of oppgaver; statistics |

- `OppgaveFiltrering` queues are not materialized - they hold properties used as query filters against `Oppgave` and `Behandling`.
- The `Oppgave` life-cycle is connected to behandling and aksjonspunkt (employee decision point) in `fp-sak` or `fptilbake`. An open aksjonspunkt implies an aktiv oppgave.

## Entry points

- Kafka consumer `BehandlingHendelseConsumer` processing behandling events from `fp-sak` and `fptilbake`: Calls back to the source to get current `Behandling`status and updates local DB.
- REST endpoints for frontend apps from `ApiConfig` method `getAllClasses`

## Verification

- For backend changes with user-visible or integration impact, verify via `navikt/fp-autotest`
- Suite: `fplos`.
- Preferred path: use the `run-integration-tests` skill
