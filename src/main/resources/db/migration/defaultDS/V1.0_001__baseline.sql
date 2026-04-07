CREATE SEQUENCE SEQ_GLOBAL_PK
    INCREMENT BY 50
    MINVALUE 5000000
    START WITH 5000000
    NO CYCLE
    CACHE 50;

-- -------------------------------------------------------------------------------------
-- 1. Organisasjon: Avdeling, saksbehandler, grupper og relasjoner mellom disse
-- -------------------------------------------------------------------------------------
CREATE TABLE avdeling (
	avdeling_enhet varchar(10) NOT NULL CONSTRAINT pk_avdeling PRIMARY KEY ,
	navn varchar(255) NOT NULL,
	versjon bigint NOT NULL DEFAULT 0,
	opprettet_av varchar(20) NOT NULL DEFAULT 'VL',
	opprettet_tid TIMESTAMP(3) NOT NULL DEFAULT statement_timestamp(),
	endret_av varchar(20),
	endret_tid TIMESTAMP(3),
	krever_kode_6 varchar(1) NOT NULL DEFAULT 'N',
	aktiv varchar(1) NOT NULL DEFAULT 'J'
) ;
COMMENT ON TABLE avdeling IS 'Tabell som inneholder avdelingene som kan utføre oppgaver';
COMMENT ON COLUMN avdeling.avdeling_enhet IS 'Identifikasjon på enhet som har oppgaver';
COMMENT ON COLUMN avdeling.navn IS 'Navn på enhet som har oppgaver';
COMMENT ON COLUMN avdeling.aktiv IS 'Styrer om avdeling er aktiv';
COMMENT ON COLUMN avdeling.krever_kode_6 IS 'Kreves kode 6 for å kunne velge denne avdelingen';

CREATE TABLE saksbehandler (
	saksbehandler_ident varchar(20)  NOT NULL CONSTRAINT pk_saksbehandler PRIMARY KEY ,
	versjon bigint NOT NULL DEFAULT 0,
	opprettet_av varchar(20) NOT NULL DEFAULT 'VL',
	opprettet_tid TIMESTAMP(3) NOT NULL DEFAULT statement_timestamp(),
	endret_av varchar(20),
	endret_tid TIMESTAMP(3),
	navn varchar(250),
	ansatt_enhet varchar(20)
) ;
COMMENT ON TABLE saksbehandler IS 'Tabell som inneholder saksbehandleren som kan løse oppgaver';
COMMENT ON COLUMN saksbehandler.ansatt_enhet IS 'Enhet der saksbehandler er ansatt';
COMMENT ON COLUMN saksbehandler.navn IS 'Saksbehandlers navn';
COMMENT ON COLUMN saksbehandler.saksbehandler_ident IS 'Saksbehandlerident';

CREATE TABLE saksbehandler_gruppe (
	id bigint NOT NULL CONSTRAINT pk_saksbehandler_gruppe PRIMARY KEY,
	gruppe_navn varchar(255) NOT NULL,
	avdeling_id varchar(10) NOT NULL CONSTRAINT fk_saksbehandler_gruppe_avdeling_01 references avdeling(avdeling_enhet),
	versjon bigint NOT NULL DEFAULT 0,
	opprettet_av varchar(20) NOT NULL DEFAULT 'VL',
	opprettet_tid TIMESTAMP(3) NOT NULL DEFAULT statement_timestamp(),
	endret_av varchar(20),
	endret_tid TIMESTAMP(3)
) ;
COMMENT ON TABLE saksbehandler_gruppe IS 'Tabell som holder informasjon om grupper';
COMMENT ON COLUMN saksbehandler_gruppe.avdeling_id IS 'Referanse til avdeling som gruppen tilhører';
COMMENT ON COLUMN saksbehandler_gruppe.endret_av IS 'Endret av';
COMMENT ON COLUMN saksbehandler_gruppe.endret_tid IS 'Timestamp endring';
COMMENT ON COLUMN saksbehandler_gruppe.gruppe_navn IS 'Gruppenavn';
COMMENT ON COLUMN saksbehandler_gruppe.id IS 'Gruppe ID';
COMMENT ON COLUMN saksbehandler_gruppe.opprettet_av IS 'Bruker som opprettet raden';
COMMENT ON COLUMN saksbehandler_gruppe.opprettet_tid IS 'Tidspunkt for opprettelse';
COMMENT ON COLUMN saksbehandler_gruppe.versjon IS 'angir versjon for optimistisk låsing hvor aktuelt';

CREATE TABLE avdeling_saksbehandler (
	saksbehandler_id varchar(20) NOT NULL CONSTRAINT fk_avdeling_saksbehandler_1 REFERENCES saksbehandler(saksbehandler_ident),
	avdeling_id varchar(10) NOT NULL CONSTRAINT fk_avdeling_saksbehandler_2 REFERENCES avdeling(avdeling_enhet),
	CONSTRAINT pk_avdeling_saksbehandler PRIMARY KEY (saksbehandler_id, avdeling_id)
) ;
COMMENT ON TABLE avdeling_saksbehandler IS 'Tabell som er koblingen mellom avdeling og saksbehandler';
COMMENT ON COLUMN avdeling_saksbehandler.avdeling_id IS 'FK til avdeling';
COMMENT ON COLUMN avdeling_saksbehandler.saksbehandler_id IS 'FK til saksbehandler';

CREATE TABLE gruppe_tilknytning (
	saksbehandler_id varchar(20) NOT NULL CONSTRAINT fk_gruppe_tilknytning_saksbehandler_01 REFERENCES saksbehandler(saksbehandler_ident),
	gruppe_id bigint NOT NULL CONSTRAINT fk_gruppe_tilknytning_saksbehandler_gruppe_01 REFERENCES saksbehandler_gruppe(id),
	CONSTRAINT pk_gruppe_tilknytning PRIMARY KEY (saksbehandler_id, gruppe_id)
) ;
COMMENT ON TABLE gruppe_tilknytning IS 'Jointabell som holder koblinger mellom saksbehandlere og gruppe';
COMMENT ON COLUMN gruppe_tilknytning.gruppe_id IS 'Gruppe ID';
COMMENT ON COLUMN gruppe_tilknytning.saksbehandler_id IS 'Saksbehandler ID';


-- -------------------------------------------------------------------------------------
-- 2. OppgaveFilter: Filter m/kriterier og relasjon til saksbehandler
-- -------------------------------------------------------------------------------------
CREATE TABLE oppgave_filtrering (
	id bigint NOT NULL CONSTRAINT pk_oppgave_filtrering PRIMARY KEY,
	navn varchar(100) NOT NULL,
	sortering varchar(100) NOT NULL,
	avdeling_id varchar(10) NOT NULL CONSTRAINT fk_liste_1 REFERENCES avdeling(avdeling_enhet),
	versjon bigint NOT NULL DEFAULT 0,
	opprettet_av varchar(20) NOT NULL DEFAULT 'VL',
	opprettet_tid TIMESTAMP(3) NOT NULL DEFAULT statement_timestamp(),
	endret_av varchar(20),
	endret_tid TIMESTAMP(3),
	fom_dato timestamp(0),
	tom_dato timestamp(0),
	fom_dager bigint,
	tom_dager bigint,
	periodefilter_type varchar(40) NOT NULL DEFAULT 'FAST_PERIODE',
	beskrivelse varchar(1024)
) ;
COMMENT ON TABLE oppgave_filtrering IS 'Tabell som skal inneholde informasjon om lister som brukes til filtrering av oppgaver';
COMMENT ON COLUMN oppgave_filtrering.avdeling_id IS 'Avdelingsenhet oppgavefiltreringa er koblet til';
COMMENT ON COLUMN oppgave_filtrering.beskrivelse IS 'Beskrivelse av oppgavefiltrering';
COMMENT ON COLUMN oppgave_filtrering.fom_dager IS 'Datointervall fra og med dag';
COMMENT ON COLUMN oppgave_filtrering.fom_dato IS 'Datointervall fra og med dato';
COMMENT ON COLUMN oppgave_filtrering.id IS 'PK';
COMMENT ON COLUMN oppgave_filtrering.navn IS 'Navn på listen';
COMMENT ON COLUMN oppgave_filtrering.periodefilter_type IS 'Type periodefilter som skal brukes for oppgavefiltrering';
COMMENT ON COLUMN oppgave_filtrering.sortering IS 'Hva skal listen sorteres på';
COMMENT ON COLUMN oppgave_filtrering.tom_dager IS 'Datointervall til og med dag';
COMMENT ON COLUMN oppgave_filtrering.tom_dato IS 'Datointervall til og med dato';

CREATE TABLE filtrering_andre_kriterier (
	oppgave_filtrering_id bigint NOT NULL CONSTRAINT fk_oppgave_filtrering_1 REFERENCES oppgave_filtrering(id),
	andre_kriterier_type varchar(100) NOT NULL,
	inkluder varchar(1) NOT NULL DEFAULT 'J',
    CONSTRAINT pk_filtrering_andre_kriterier PRIMARY KEY (oppgave_filtrering_id, andre_kriterier_type)
) ;
COMMENT ON TABLE filtrering_andre_kriterier IS 'Tabell inneholder filtreringer for andre kriterier';
COMMENT ON COLUMN filtrering_andre_kriterier.andre_kriterier_type IS 'Kode for de andre kriteriene oppgavene skal filtreres på';
COMMENT ON COLUMN filtrering_andre_kriterier.inkluder IS 'Verdi som sier om det skal filtreres inn eller filtreres vekk';
COMMENT ON COLUMN filtrering_andre_kriterier.oppgave_filtrering_id IS 'Oppgavefiltrering som filtreringen hører til';

CREATE TABLE filtrering_behandling_type (
	oppgave_filtrering_id bigint NOT NULL CONSTRAINT fk_oppgave_filtrering_1 REFERENCES oppgave_filtrering(id),
	behandling_type varchar(100) NOT NULL,
	CONSTRAINT pk_filtrering_behandling_type PRIMARY KEY (oppgave_filtrering_id, behandling_type)
) ;
COMMENT ON TABLE filtrering_behandling_type IS 'Tabell inneholder filtreringer på behandlingType';
COMMENT ON COLUMN filtrering_behandling_type.behandling_type IS 'Koden behandlingstypen skal filtreres på';
COMMENT ON COLUMN filtrering_behandling_type.oppgave_filtrering_id IS 'Listen filtreringen hører til';

CREATE TABLE filtrering_ytelse_type (
	oppgave_filtrering_id bigint NOT NULL CONSTRAINT fk_oppgave_filtrering_1 REFERENCES oppgave_filtrering(id),
	fagsak_ytelse_type varchar(100) NOT NULL,
	CONSTRAINT pk_filtrering_ytelse_type PRIMARY KEY (oppgave_filtrering_id, fagsak_ytelse_type)
) ;
COMMENT ON TABLE filtrering_ytelse_type IS 'Tabell inneholder filtreringer på behandlingType';
COMMENT ON COLUMN filtrering_ytelse_type.fagsak_ytelse_type IS 'Koden ytelsetypen skal filtreres på';
COMMENT ON COLUMN filtrering_ytelse_type.oppgave_filtrering_id IS 'Oppgavefiltrering som filtreringen hører til';

CREATE TABLE filtrering_saksbehandler (
	saksbehandler_id varchar(20) NOT NULL CONSTRAINT fk_filtrering_saksbehandler_1 REFERENCES saksbehandler(saksbehandler_ident),
	oppgave_filtrering_id bigint NOT NULL CONSTRAINT fk_filtrering_saksbehandler_2 REFERENCES oppgave_filtrering(id),
	CONSTRAINT pk_filtrering_saksbehandler PRIMARY KEY (saksbehandler_id, oppgave_filtrering_id)
) ;
COMMENT ON TABLE filtrering_saksbehandler IS 'Tabell som er koblingen mellom avdeling og saksbehandler';
COMMENT ON COLUMN filtrering_saksbehandler.oppgave_filtrering_id IS 'FK til oppgave filtrering';
COMMENT ON COLUMN filtrering_saksbehandler.saksbehandler_id IS 'FK til saksbehandler';


-- -------------------------------------------------------------------------------------
-- 3. Behandling: Behandling m/egenskaper
-- -------------------------------------------------------------------------------------
CREATE TABLE behandling (
	id uuid NOT NULL CONSTRAINT pk_behandling PRIMARY KEY,
	aktor_id varchar(50) NOT NULL,
	saksnummer varchar(19) NOT NULL,
	fagsak_ytelse_type varchar(100) NOT NULL,
	kildesystem varchar(100) NOT NULL,
	behandlende_enhet varchar(10) NOT NULL,
	behandling_type varchar(100) NOT NULL,
	behandling_tilstand varchar(100) NOT NULL,
	aktive_aksjonspunkt varchar(200),
	ventefrist TIMESTAMP(3),
	opprettet TIMESTAMP(3) NOT NULL,
	avsluttet TIMESTAMP(3),
	behandlingsfrist DATE,
	forste_stonadsdag DATE,
	feilutbetaling_belop decimal(19,2),
	feilutbetaling_start DATE,
	versjon bigint NOT NULL DEFAULT 0,
	opprettet_av varchar(20) NOT NULL DEFAULT 'VL',
	opprettet_tid TIMESTAMP(3) NOT NULL DEFAULT statement_timestamp(),
	endret_av varchar(20),
	endret_tid TIMESTAMP(3)
) ;
COMMENT ON TABLE behandling IS 'Tabell som speiler behandling og status fra kildesystem';
COMMENT ON COLUMN behandling.aktive_aksjonspunkt IS 'Åpne aksjonspnkt, sorterte koder';
COMMENT ON COLUMN behandling.aktor_id IS 'AktørId for saken';
COMMENT ON COLUMN behandling.avsluttet IS 'Tidspunktet behandlingen ble avsluttet';
COMMENT ON COLUMN behandling.behandlende_enhet IS 'Hvilken enhet';
COMMENT ON COLUMN behandling.behandling_tilstand IS 'Behandlingstilstand';
COMMENT ON COLUMN behandling.behandling_type IS 'Hva slags behandlingstype behandlingen har';
COMMENT ON COLUMN behandling.behandlingsfrist IS 'Behandlingsfrist';
COMMENT ON COLUMN behandling.fagsak_ytelse_type IS 'Hva slags ytelse type fagsaken har';
COMMENT ON COLUMN behandling.feilutbetaling_belop IS 'Feilutbetalt beløp fra fptilbake';
COMMENT ON COLUMN behandling.feilutbetaling_start IS 'Startdato for feilutbetaling fra fptilbake';
COMMENT ON COLUMN behandling.forste_stonadsdag IS 'Første stønadsdag';
COMMENT ON COLUMN behandling.id IS 'PK - behandling sin uuid';
COMMENT ON COLUMN behandling.kildesystem IS 'Hvilket system oppgaven kommer fra';
COMMENT ON COLUMN behandling.opprettet IS 'Tidspunktet behandlingen ble opprettet';
COMMENT ON COLUMN behandling.saksnummer IS 'Saksnummer for fagsaken';
COMMENT ON COLUMN behandling.ventefrist IS 'Tidligste ventefrist for behandlinger på vent';

CREATE TABLE behandling_egenskap (
	behandling_id uuid NOT NULL CONSTRAINT fk_behandling_egenskap_01 REFERENCES behandling(id),
	andre_kriterier_type varchar(100) NOT NULL,
	CONSTRAINT pk_behandling_egenskap PRIMARY KEY (behandling_id, andre_kriterier_type)
) ;
COMMENT ON TABLE behandling_egenskap IS 'Tabell som inneholder egenskaper for en behandling';
COMMENT ON COLUMN behandling_egenskap.andre_kriterier_type IS 'Kode for de andre kriteriene oppgavene skal filtreres på';
COMMENT ON COLUMN behandling_egenskap.behandling_id IS 'Referanse til behandling';


-- -------------------------------------------------------------------------------------
-- 4. Oppgave og reservasjon: Oppgave med egenskaper og reservasjon
-- -------------------------------------------------------------------------------------
CREATE TABLE oppgave (
	id bigint NOT NULL CONSTRAINT pk_oppgave PRIMARY KEY,
    behandling_id uuid NOT NULL CONSTRAINT fk_oppgave_01 REFERENCES behandling(id),
	behandlende_enhet varchar(10) NOT NULL,
	aktiv varchar(1) NOT NULL DEFAULT 'J',
    oppgave_avsluttet TIMESTAMP(3),
	versjon bigint NOT NULL DEFAULT 0,
	opprettet_av varchar(20) NOT NULL DEFAULT 'VL',
	opprettet_tid TIMESTAMP(3) NOT NULL DEFAULT statement_timestamp(),
	endret_av varchar(20),
	endret_tid TIMESTAMP(3)
) ;
COMMENT ON TABLE oppgave IS 'Tabell som skal inneholde informasjon om behandlinger som trenger intervensjon av en saksbehandler';
COMMENT ON COLUMN oppgave.aktiv IS 'Setter oppgaveegenskap til aktiv eller inaktiv etter egenskapen sin tilstand';
COMMENT ON COLUMN oppgave.behandlende_enhet IS 'Enheten som har oppgaven satt til seg';
COMMENT ON COLUMN oppgave.behandling_id IS 'Behandling id for behandling i fagsystem';
COMMENT ON COLUMN oppgave.id IS 'PK';
COMMENT ON COLUMN oppgave.oppgave_avsluttet IS 'Tidspunkt for avslutting av Oppgave';

CREATE TABLE oppgave_egenskap (
	oppgave_id bigint NOT NULL CONSTRAINT fk_oppgave_egenskap_2 REFERENCES oppgave(id),
	andre_kriterier_type varchar(100) NOT NULL,
	siste_saksbehandler_totrinn varchar(20),
    CONSTRAINT pk_oppgave_egenskap PRIMARY KEY (oppgave_id, andre_kriterier_type)
) ;
COMMENT ON TABLE oppgave_egenskap IS 'Tabell inneholder filtreringer for andre kriterier';
COMMENT ON COLUMN oppgave_egenskap.andre_kriterier_type IS 'Kode for de andre kriteriene oppgavene skal filtreres på';
COMMENT ON COLUMN oppgave_egenskap.oppgave_id IS 'Oppgave egenskapen hører til';
COMMENT ON COLUMN oppgave_egenskap.siste_saksbehandler_totrinn IS 'Saksbehandleren som sendte behandlingen til totrinnskontroll';

CREATE TABLE reservasjon (
	oppgave_id bigint NOT NULL
	    CONSTRAINT pk_reservasjon PRIMARY KEY
	    CONSTRAINT fk_reservasjon_oppgave_id REFERENCES oppgave(id),
	reservert_til TIMESTAMP(3),
	reservert_av varchar(20) NOT NULL ,
	versjon bigint NOT NULL DEFAULT 0,
	opprettet_av varchar(20) NOT NULL DEFAULT 'VL',
	opprettet_tid TIMESTAMP(3) NOT NULL DEFAULT statement_timestamp(),
	endret_av varchar(20),
	endret_tid TIMESTAMP(3),
	flyttet_av varchar(100),
	flyttet_tidspunkt TIMESTAMP(3),
	begrunnelse varchar(500)
) ;
COMMENT ON TABLE reservasjon IS 'Tabell som skal inneholde reservasjoner av oppgaver';
COMMENT ON COLUMN reservasjon.begrunnelse IS 'Begrunnelse for opphør/flytting av reservasjon';
COMMENT ON COLUMN reservasjon.flyttet_av IS 'Ident som har flyttet reservasjonen';
COMMENT ON COLUMN reservasjon.flyttet_tidspunkt IS 'Tidspunkt for flytting av reservasjonen';
COMMENT ON COLUMN reservasjon.oppgave_id IS 'FK: relasjon til Oppgave';
COMMENT ON COLUMN reservasjon.reservert_av IS 'Saksbehandler ident til saksbehandler som har reservert oppgaven';
COMMENT ON COLUMN reservasjon.reservert_til IS 'Tidspunkt for når reservasjonen går ut';


-- -------------------------------------------------------------------------------------
-- 5. Statistikk: Statistikk for behandlinger og oppgavefiltre
-- -------------------------------------------------------------------------------------
CREATE TABLE stat_enhet_ytelse_behandling (
	behandlende_enhet varchar(10) NOT NULL,
	tidsstempel bigint NOT NULL,
	fagsak_ytelse_type varchar(100) NOT NULL,
	behandling_type varchar(100) NOT NULL,
	stat_dato timestamp(0) NOT NULL,
	antall_aktive bigint NOT NULL,
	antall_opprettet bigint NOT NULL,
	antall_avsluttet bigint NOT NULL,
	CONSTRAINT pk_stat_enhet_ytelse_behandling PRIMARY KEY (behandlende_enhet, tidsstempel, fagsak_ytelse_type, behandling_type)
) ;
COMMENT ON TABLE stat_enhet_ytelse_behandling IS 'Daglig statistikk over antall aktive, opprettede og avsluttede oppgaver';
COMMENT ON COLUMN stat_enhet_ytelse_behandling.antall_aktive IS 'Antall aktive oppgaver';
COMMENT ON COLUMN stat_enhet_ytelse_behandling.antall_avsluttet IS 'Antall oppgaver avsluttet siden forrige telling';
COMMENT ON COLUMN stat_enhet_ytelse_behandling.antall_opprettet IS 'Antall oppgaver opprettet siden forrige telling';
COMMENT ON COLUMN stat_enhet_ytelse_behandling.behandlende_enhet IS 'Hvilken enhet';
COMMENT ON COLUMN stat_enhet_ytelse_behandling.behandling_type IS 'Telling for behandlingstype';
COMMENT ON COLUMN stat_enhet_ytelse_behandling.fagsak_ytelse_type IS 'Telling for ytelse';
COMMENT ON COLUMN stat_enhet_ytelse_behandling.stat_dato IS 'Telling for dato';
COMMENT ON COLUMN stat_enhet_ytelse_behandling.tidsstempel IS 'Tidsstempel for når statistikken er tatt';

CREATE TABLE stat_oppgave_filter (
	oppgave_filter_id bigint NOT NULL,
	tidsstempel bigint NOT NULL,
	stat_dato timestamp(0) NOT NULL,
	antall_aktive bigint NOT NULL,
	antall_tilgjengelige bigint NOT NULL,
	innslag_type varchar(20) NOT NULL DEFAULT 'REGELMESSIG',
	antall_ventende bigint,
	antall_opprettet bigint,
	antall_avsluttet bigint,
	CONSTRAINT pk_stat_oppgave_filter PRIMARY KEY (oppgave_filter_id, tidsstempel)
) ;
COMMENT ON TABLE stat_oppgave_filter IS 'Daglig statistikk over antall aktive, opprettede og avsluttede oppgaver i en kø';
COMMENT ON COLUMN stat_oppgave_filter.antall_aktive IS 'Antall aktive oppgaver';
COMMENT ON COLUMN stat_oppgave_filter.antall_avsluttet IS 'Antall avsluttet oppaver siden sist innslag';
COMMENT ON COLUMN stat_oppgave_filter.antall_opprettet IS 'Antall opprettet oppaver siden sist innslag';
COMMENT ON COLUMN stat_oppgave_filter.antall_tilgjengelige IS 'Antall aktive oppgaver som ikke er reservert';
COMMENT ON COLUMN stat_oppgave_filter.antall_ventende IS 'Antall ventende behandlinger';
COMMENT ON COLUMN stat_oppgave_filter.innslag_type IS 'REGELMESSIG for målinger gjort regelmessig hver time, SNAPSHOT for øyeblikksbilder';
COMMENT ON COLUMN stat_oppgave_filter.oppgave_filter_id IS 'Id for oppgavefilteret som køen er basert på';
COMMENT ON COLUMN stat_oppgave_filter.stat_dato IS 'Telling for dato';
COMMENT ON COLUMN stat_oppgave_filter.tidsstempel IS 'Tidsstempel for når statistikken er tatt';


-- -------------------------------------------------------------------------------------
-- 6. Annet: Hendelselogg mm.
-- -------------------------------------------------------------------------------------
CREATE TABLE mottatt_hendelse (
	hendelse_uid varchar(100) NOT NULL CONSTRAINT pk_mottatt_hendelse PRIMARY KEY,
	mottatt_tid TIMESTAMP(3) NOT NULL DEFAULT statement_timestamp()
) ;
COMMENT ON TABLE mottatt_hendelse IS 'Holder unik identifikator for alle mottatte hendelser';
COMMENT ON COLUMN mottatt_hendelse.hendelse_uid IS 'Unik identifikator for hendelse mottatt';
COMMENT ON COLUMN mottatt_hendelse.mottatt_tid IS 'Hendelse mottatt tidspunkt';
