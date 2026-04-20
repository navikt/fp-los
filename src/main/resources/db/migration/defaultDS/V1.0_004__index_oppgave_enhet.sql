-- Indeks for oppgavekø-spørringer som filtrerer på behandlende_enhet uten aktiv-filter.
-- Eksisterende idx_oppgave_enhet_aktiv(aktiv, behandlende_enhet) har feil kolonnerekkefølge
-- for disse spørringene.
CREATE INDEX idx_oppgave_enhet ON oppgave (behandlende_enhet);
