package no.nav.foreldrepenger.los.migrering.dto;

public record GcpImportKvittering(boolean kjørtUtenFeil, int behandlinger, int oppgaver, int reservasjoner, int orgData, int oppgaveKøer, int statistikkOppgaveFilter, int statistikkEnhetYtelseBehandling) {
    public static class Builder {
        private Boolean kjørtUtenFeil;
        private int behandlinger = 0;
        private int oppgaver = 0;
        private int reservasjoner = 0;
        private int orgData = 0;
        private int oppgaveKøer = 0;
        private int statistikkOppgaveFilter = 0;
        private int statistikkEnhetYtelseBehandling = 0;

        public Builder kjørtUtenFeil(boolean kjørtUtenFeil) {
            this.kjørtUtenFeil = kjørtUtenFeil;
            return this;
        }

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

        public Builder statistikkOppgaveFilter(int antall) {
            this.statistikkOppgaveFilter += antall;
            return this;
        }

        public Builder statistikkEnhetYtelseBehandling(int antall) {
            this.statistikkEnhetYtelseBehandling += antall;
            return this;
        }

        public GcpImportKvittering build() {
            if (kjørtUtenFeil == null) {
                throw new IllegalStateException("Må sette kjørtUtenFeil før bygging av GcpImportKvittering");
            }
            return new GcpImportKvittering(kjørtUtenFeil, behandlinger, oppgaver, reservasjoner, orgData, oppgaveKøer, statistikkOppgaveFilter, statistikkEnhetYtelseBehandling);
        }
    }
}
