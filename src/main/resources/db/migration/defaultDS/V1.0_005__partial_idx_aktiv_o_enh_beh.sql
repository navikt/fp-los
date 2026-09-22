CREATE INDEX IF NOT EXISTS idx_aktiv_oppgave_enhet
    ON oppgave (behandlende_enhet, behandling_id)
    WHERE aktiv = true;
