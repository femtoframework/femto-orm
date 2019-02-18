package org.femtoframework.orm;

import org.femtoframework.implement.ImplementUtil;
import org.femtoframework.orm.dialect.RdbmsDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class RepositoryUtil {

    private static RepositoryModule module = null;

    public static RepositoryModule getModule() {
        if (module == null) {
            module = ImplementUtil.getInstance(RepositoryModule.class);
        }
        return module;
    }

    /**
     * Returns RdbmsDialect by provider
     *
     * @param provider "mysql" "oracle" or "postgres" etc.
     * @return RdbmsDialect
     */
    public static RdbmsDialect getDialect(String provider) {
        return ImplementUtil.getInstance(provider.toLowerCase(), RdbmsDialect.class);
    }


    private static Logger logger = LoggerFactory.getLogger(RepositoryUtil.class);

    /**
     * Retrieve dialect by dataSource
     *
     * @param dataSource DataSource
     * @return RdbmsDialect
     */
    public static RdbmsDialect getDialect(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            String provider = null;
            if (dataSource instanceof NamedDataSource) {
                provider = ((NamedDataSource)dataSource).getProvider();
            }
            if (provider == null) {
                DatabaseMetaData metaData = conn.getMetaData();
                provider = metaData.getDatabaseProductName();
            }
            return getDialect(provider);
        }
        catch(SQLException sqle) {
            logger.error("Retrieve database information error", sqle);
            throw new IllegalStateException("Retrieve database information error", sqle);
        }
    }
}
