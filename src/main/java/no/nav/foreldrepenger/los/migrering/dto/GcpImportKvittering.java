package no.nav.foreldrepenger.los.migrering.dto;

public record GcpImportKvittering(int behandlinger, int oppgaver, int reservasjoner, int orgData, int oppgaveKøer,
                                  int statistikkOppgaveFilter, int statistikkEnhetYtelseBehandling, int filtreringSaksbehandlerRelasjon) {
    public static class Builder {
        private int behandlinger = 0;
        private int oppgaver = 0;
        private int reservasjoner = 0;
        private int orgData = 0;
        private int oppgaveKøer = 0;
        private int statistikkOppgaveFilter = 0;
        private int statistikkEnhetYtelseBehandling = 0;
        private int filtreringSaksbehandlerRelasjon = 0;


        public Builder behandlinger(int antall) {
            this.behandlinger += antall;
            return this;
        }

        public Builder oppgaver(int antall) {
            this.oppgaver += antall;
            return this;
        }

        public Builder reservasjoner(int antall) {
            this.reservasjoner += antall;
            return this;
        }

        public Builder orgData(int antall) {
            this.orgData += antall;
            return this;
        }

        public Builder oppgaveKøer(int antall) {
            this.oppgaveKøer += antall;
            return this;
        }

        public Builder filtreringSaksbehandlerRelasjon(int antall) {
            this.filtreringSaksbehandlerRelasjon += antall;
            return this;
        }

        public Builder statistikkOppgaveFilter(int antall) {
            this.statistikkOppgaveFilter += antall;
            return this;
        }

        public Builder statistikkEnhetYtelseBehandling(int antall) {
            this.statistikkEnhetYtelseBehandling += antall;
            return this;
        }

        public GcpImportKvittering build() {
            return new GcpImportKvittering(behandlinger, oppgaver, reservasjoner, orgData, oppgaveKøer, statistikkOppgaveFilter,
                statistikkEnhetYtelseBehandling, filtreringSaksbehandlerRelasjon);
        }
    }
}
