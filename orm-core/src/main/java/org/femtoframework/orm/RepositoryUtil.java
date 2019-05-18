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


    /**
     * Return RepositoryFactory by DataSource
     *
     * @param dataSource DataSource
     * @return Repository
     */
    public static RepositoryFactory getRepositoryFactory(DataSource dataSource) {
        return getModule().getRepositoryFactory(dataSource);
    }

    /**
     * Return default DataSource, the logic is this,
     *
     * 1. If there is no DataSource, return null
     * 2. If there is only one DataSource, return the only one.
     * 3. If there is multiple, select first NamedDataSource who has default=true
     * 4. No default DataSource specified, use the first one in spec
     *
     * @return The default DataSource
     */
    public static DataSource getDefaultDataSource() {
        return getModule().getDefaultDataSource();
    }

    /**
     * 设置自动Commit
     *
     * @param conn
     * @param isAutoCommit 是否自动Commit
     * @return 原先是否自动Commit
     */
    public static boolean setAutoCommit(Connection conn, boolean isAutoCommit)
    {
        boolean orig = true;
        try {
            if (conn != null) {
                orig = conn.getAutoCommit();
                conn.setAutoCommit(isAutoCommit);
                return orig;
            }
        }
        catch (Exception e) {
        }
        return orig;
    }

    /**
     * 回滚
     *
     * @param conn
     */
    public static void rollback(Connection conn)
    {
        try {
            if (conn != null) {
                conn.rollback();
            }
        }
        catch (SQLException e) {
        }
    }
}
