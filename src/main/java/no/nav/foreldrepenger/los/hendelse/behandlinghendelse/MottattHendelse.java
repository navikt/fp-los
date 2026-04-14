package no.nav.foreldrepenger.los.hendelse.behandlinghendelse;

import java.io.Serializable;
import java.sql.Types;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;


@Entity(name = "MottattHendelse")
@Table(name = "MOTTATT_HENDELSE")
public class MottattHendelse implements Serializable {

    @Id
    @NotNull
    @JdbcTypeCode(Types.VARCHAR)
    @Column(name = "hendelse_uid")
    private String hendelseUid;

    @NotNull
    @Column(name = "mottatt_tid", nullable = false)
    private LocalDateTime mottattTidspunkt;

    protected MottattHendelse() {
        //for hibernate
    }

    public MottattHendelse(String hendelseUid) {
        this.hendelseUid = hendelseUid;
        this.mottattTidspunkt = LocalDateTime.now();
    }

    public String getHendelseUid() {
        return hendelseUid;
    }
}
