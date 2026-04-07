CREATE INDEX idx_avdeling_saksbehandler_sbh_id ON avdeling_saksbehandler (saksbehandler_id);
CREATE INDEX idx_avdeling_saksbehandler_avdeling_id ON avdeling_saksbehandler (avdeling_id);

CREATE INDEX idx_behandling_tilstand ON behandling (behandling_tilstand);
CREATE INDEX idx_behandling_ventefrist ON behandling (ventefrist);
CREATE INDEX idx_behandling_avsluttet ON behandling (avsluttet);
CREATE INDEX idx_behandling_ytelse_type ON behandling (fagsak_ytelse_type);
CREATE INDEX idx_behandling_type ON behandling (behandling_type);
CREATE INDEX idx_behandling_enhet ON behandling (behandlende_enhet);
CREATE INDEX idx_behandling_frist ON behandling (behandlingsfrist);
CREATE INDEX idx_behandling_opprettet ON behandling (opprettet);
CREATE INDEX idx_behandling_stonadsdag ON behandling (forste_stonadsdag);
CREATE INDEX idx_behandling_feilutbet_belop ON behandling (feilutbetaling_belop);
CREATE INDEX idx_behandling_feilutbet_start ON behandling (feilutbetaling_start);
CREATE INDEX idx_behandling_saksnummer ON behandling (saksnummer);

CREATE INDEX idx_behandling_egenskap_beh_id ON behandling_egenskap (behandling_id);
CREATE INDEX idx_behandling_egenskap_kriterie_type ON behandling_egenskap (andre_kriterier_type);

CREATE INDEX idx_oppgave_egenskap_beh_id ON oppgave_egenskap (oppgave_id);
CREATE INDEX idx_oppgave_egenskap_kriterie_type ON oppgave_egenskap (andre_kriterier_type);

CREATE INDEX idx_filtrering_andre_krit_kriterie_type ON filtrering_andre_kriterier (andre_kriterier_type);
CREATE INDEX idx_filtr_beh_type_type ON filtrering_behandling_type (behandling_type);
CREATE INDEX idx_filtrering_saksbehandler_sbh_id ON filtrering_saksbehandler (saksbehandler_id);
CREATE INDEX idx_filtrering_saksbehandler_filtrering_id ON filtrering_saksbehandler (oppgave_filtrering_id);
CREATE INDEX idx_filtr_ytelse_type_type ON filtrering_ytelse_type (fagsak_ytelse_type);
CREATE INDEX idx_gruppe_tilknytning_saksbehandler_id ON gruppe_tilknytning (saksbehandler_id);
CREATE INDEX idx_gruppe_tilknytning_gruppe_id ON gruppe_tilknytning (gruppe_id);
CREATE INDEX idx_oppgave_enhet_aktiv ON oppgave (aktiv, behandlende_enhet);
CREATE INDEX idx_oppgave_behandling_id ON oppgave (behandling_id);
CREATE INDEX idx_oppgave_filtrering_avdeling_id ON oppgave_filtrering (avdeling_id);
CREATE INDEX idx_saksbehandler_gruppe_avdeling_id ON saksbehandler_gruppe (avdeling_id);


-- =====================================================================================
-- V2: Indeksoptimalisering for oppgave-spørringer
--
-- Adresserer manglende indekser og suboptimal kolonnerekkefølge identifisert i
-- OppgaveKøRepository, ReservasjonRepository og SlettUtdaterteTask.
-- =====================================================================================


-- -------------------------------------------------------------------------------------
-- 2. oppgave: Manglende indekser for kolonner brukt i filter og ORDER BY
-- -------------------------------------------------------------------------------------

-- opprettet_tid: brukes i opprettetEtterFilter og ORDER BY (KøSortering.OPPGAVE_OPPRETTET)
CREATE INDEX idx_oppgave_opprettet_tid ON oppgave (opprettet_tid);

-- oppgave_avsluttet: brukes i avsluttetEtterFilter
CREATE INDEX idx_oppgave_avsluttet ON oppgave (oppgave_avsluttet);

-- -------------------------------------------------------------------------------------
-- 4. reservasjon: Indeks på reservert_av for spørringer i ReservasjonRepository
--
--    hentSisteReserverteMetadata: WHERE r.reservert_av = :uid
--    hentSaksbehandlersReserverteAktiveOppgaver: WHERE upper(r.reservertAv) = upper(:uid)
--    hentAlleReservasjonerForAvdeling: ORDER BY r.reservertAv
-- -------------------------------------------------------------------------------------
CREATE INDEX idx_reservasjon_reservert_av ON reservasjon (reservert_av);


-- -------------------------------------------------------------------------------------
-- 5. reservasjon: Indeks på reservert_til for tidsbaserte oppslag
--
--    Brukes i OppgaveKøRepository.reserverteSubquery: r.reservertTil > :nå
--    Brukes i SlettUtdaterteTask: WHERE reservertTil < :foer
--    Brukes i hentAlleReservasjonerForAvdeling: WHERE r.reservertTil > :nå
-- -------------------------------------------------------------------------------------
CREATE INDEX idx_reservasjon_reservert_til ON reservasjon (reservert_til);


-- -------------------------------------------------------------------------------------
-- 6. oppgave: Partiell funksjonsindeks for SlettUtdaterteTask opprydding
--
--    WHERE aktiv = false AND coalesce(endretTidspunkt, opprettetTidspunkt) < :foer
--    Partiell indeks begrenser seg til inaktive rader (aktiv = 'N') for å holde
--    indeksstørrelsen liten.
-- -------------------------------------------------------------------------------------
CREATE INDEX idx_oppgave_inaktiv_sist_endret ON oppgave (COALESCE(endret_tid, opprettet_tid))
    WHERE aktiv = 'N';
