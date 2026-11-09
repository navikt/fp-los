-- Indekser for oppgavekø-spørringer som filtrerer på behandling_tilstand og sorterer på et behandlingsfelt.
-- Uten tilstand først leser planleggeren hele sorteringsindeksen og forkaster behandlinger i filter.
CREATE INDEX idx_behandling_tilstand_opprettet ON behandling (behandling_tilstand, opprettet);
CREATE INDEX idx_behandling_tilstand_frist ON behandling (behandling_tilstand, behandlingsfrist);
CREATE INDEX idx_behandling_tilstand_stonadsdag ON behandling (behandling_tilstand, forste_stonadsdag);
CREATE INDEX idx_behandling_tilstand_feilut_start ON behandling (behandling_tilstand, feilutbetaling_start);
CREATE INDEX idx_behandling_tilstand_feilut_belop ON behandling (behandling_tilstand, feilutbetaling_belop);
