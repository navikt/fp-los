package no.nav.foreldrepenger.los.server;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.micrometer.core.instrument.Metrics;
import no.nav.foreldrepenger.konfig.Environment;

class DataSourceUtil {
    private static final Environment ENV = Environment.current();

    private DataSourceUtil() {
    }

    static DataSource createDataSource(int maxPoolSize, int minIdle) {
        var config = new HikariConfig();
        config.setJdbcUrl(ENV.getRequiredProperty("DB_JDBC_URL"));
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(2));
        config.setMinimumIdle(minIdle);
        config.setMaximumPoolSize(maxPoolSize);
        config.setConnectionTestQuery("select 1");
        config.setDriverClassName("org.postgresql.Driver");
        config.setMetricRegistry(Metrics.globalRegistry);
        config.setAutoCommit(false);

        var dsProperties = new Properties();
        dsProperties.setProperty("reWriteBatchedInserts", "true");
        dsProperties.setProperty("logServerErrorDetail", "false"); // skrur av batch exceptions som lekker statements i åpen logg
        config.setDataSourceProperties(dsProperties);

        return new HikariDataSource(config);
    }

    /* Denne gir lazy loading og feiler ikke ved lokalt kjøring uten vault mount */
    private static String hentEllerBeregnVerdiHvisMangler(String key, String mappeNavn, String filNavn) {
        if (ENV.getProperty(key) == null) {
            System.getProperties().computeIfAbsent(key, _ -> VaultUtil.lesFilVerdi(mappeNavn, filNavn));
        }
        return ENV.getRequiredProperty(key);
    }
}
