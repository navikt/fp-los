--------------------------------------------------------
-- DDL for Prosesstask
-- Viktig å merke seg her at alt av DDL relatert til prosesstask-biten er ikke eid av dette prosjektet,
-- DDL eies av no.nav.vedtak.felles.prosesstask.
--------------------------------------------------------

CREATE TABLE PROSESS_TASK
(
    ID                        NUMERIC       NOT NULL
        CONSTRAINT PK_PROSESS_TASK
            PRIMARY KEY,
    TASK_TYPE                 VARCHAR(50)   NOT NULL ,
    PRIORITET                 NUMERIC(3, 0) NOT NULL DEFAULT 0 ,
    STATUS                    VARCHAR(20)   NOT NULL DEFAULT 'KLAR' ,
        constraint CHK_PROSESS_TASK_STATUS
            check (status in ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'VETO', 'FERDIG', 'KJOERT')),
    TASK_PARAMETERE           VARCHAR(4000),
    TASK_PAYLOAD              TEXT,
    TASK_GRUPPE               VARCHAR(250),
    TASK_SEKVENS              VARCHAR(100)  NOT NULL DEFAULT '1' ,
    NESTE_KJOERING_ETTER      TIMESTAMP(0)  DEFAULT current_timestamp,
    FEILEDE_FORSOEK           NUMERIC(5, 0) DEFAULT 0,
    SISTE_KJOERING_TS         TIMESTAMP(6),
    SISTE_KJOERING_FEIL_KODE  VARCHAR(50),
    SISTE_KJOERING_FEIL_TEKST TEXT,
    SISTE_KJOERING_SERVER     VARCHAR(50),
    OPPRETTET_AV              VARCHAR(20)   NOT NULL DEFAULT 'VL',
    OPPRETTET_TID             TIMESTAMP(6)  NOT NULL DEFAULT current_timestamp ,
    BLOKKERT_AV               NUMERIC,
    VERSJON                   NUMERIC       NOT NULL DEFAULT 0 ,
    SISTE_KJOERING_SLUTT_TS   TIMESTAMP(6),
    SISTE_KJOERING_PLUKK_TS   TIMESTAMP(6)
);


COMMENT ON COLUMN PROSESS_TASK.ID IS 'Primary Key';
COMMENT ON COLUMN PROSESS_TASK.TASK_TYPE IS 'navn på task. Brukes til å matche riktig implementasjon';
COMMENT ON COLUMN PROSESS_TASK.PRIORITET IS 'prioritet på task.  Høyere tall har høyere prioritet';
COMMENT ON COLUMN PROSESS_TASK.STATUS IS 'status på task: KLAR, NYTT_FORSOEK, FEILET, VENTER_SVAR, FERDIG';
COMMENT ON COLUMN PROSESS_TASK.TASK_PARAMETERE IS 'parametere angitt for en task';
COMMENT ON COLUMN PROSESS_TASK.TASK_PAYLOAD IS 'inputdata for en task';
COMMENT ON COLUMN PROSESS_TASK.TASK_GRUPPE IS 'angir en unik id som grupperer flere ';
COMMENT ON COLUMN PROSESS_TASK.TASK_SEKVENS IS 'angir rekkefølge på task innenfor en gruppe ';
COMMENT ON COLUMN PROSESS_TASK.NESTE_KJOERING_ETTER IS 'tasken skal ikke kjøeres før tidspunkt er passert';
COMMENT ON COLUMN PROSESS_TASK.FEILEDE_FORSOEK IS 'antall feilede forsøk';
COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_TS IS 'siste gang tasken ble forsøkt kjørt';
COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_FEIL_KODE IS 'siste feilkode tasken fikk';
COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_FEIL_TEKST IS 'siste feil tasken fikk';
COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_SERVER IS 'navn på node som sist kjørte en task (server@pid)';
COMMENT ON COLUMN PROSESS_TASK.VERSJON IS 'angir versjon for optimistisk låsing';
COMMENT ON COLUMN PROSESS_TASK.BLOKKERT_AV IS 'Id til ProsessTask som blokkerer kjøring av denne (når status=VETO)';
COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_TS IS 'siste gang tasken ble forsøkt kjørt (før kjøring)';
COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_SLUTT_TS IS 'tidsstempel siste gang tasken ble kjørt (etter kjøring)';
COMMENT ON TABLE PROSESS_TASK IS 'Inneholder tasks som skal kjøres i bakgrunnen';

CREATE INDEX IDX_PROSESS_TASK_1 ON PROSESS_TASK (STATUS);
CREATE INDEX IDX_PROSESS_TASK_2 ON PROSESS_TASK (TASK_TYPE);
CREATE INDEX IDX_PROSESS_TASK_3 ON PROSESS_TASK (NESTE_KJOERING_ETTER);
CREATE INDEX IDX_PROSESS_TASK_5 ON PROSESS_TASK (TASK_GRUPPE);
CREATE INDEX IDX_PROSESS_TASK_6 ON PROSESS_TASK (BLOKKERT_AV);

--------------------------------------------------------
--  Sequences
--------------------------------------------------------
CREATE SEQUENCE SEQ_PROSESS_TASK MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NO CYCLE;
CREATE SEQUENCE SEQ_PROSESS_TASK_GRUPPE MINVALUE 10000000 START WITH 10000000 INCREMENT BY 1000000 NO CYCLE;
