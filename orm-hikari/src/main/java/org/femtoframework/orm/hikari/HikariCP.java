package org.femtoframework.orm.hikari;

import com.zaxxer.hikari.HikariDataSource;
import org.femtoframework.bean.BeanPhase;
import org.femtoframework.bean.LifecycleMBean;
import org.femtoframework.bean.Nameable;
import org.femtoframework.bean.annotation.Property;
import org.femtoframework.io.IOUtil;
import org.femtoframework.orm.NamedDataSource;
import org.femtoframework.orm.RepositoryUtil;
import org.femtoframework.orm.dialect.RdbmsDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hikari Connection Pool
 */
public class HikariCP extends HikariDataSource implements LifecycleMBean, NamedDataSource, Nameable {

    private String provider;

    @Property("default")
    private boolean isDefault = false;

    private BeanPhase beanPhase = BeanPhase.DISABLED;


    public HikariCP() {

    }

    /**
     * Implement method of getPhase
     *
     * @return BeanPhase
     */
    @Override
    public BeanPhase _doGetPhase() {
        return beanPhase;
    }

    /**
     * Phase setter for internal
     *
     * @param phase BeanPhase
     */
    @Override
    public void _doSetPhase(BeanPhase phase) {
        this.beanPhase = phase;
    }

    /**
     * Initiliaze internally
     */
    public void _doInit() {

        RepositoryUtil.getModule().addDatasource(this);

        if (getProvider() != null) { //AutoConfig
            RdbmsDialect dialect = RepositoryUtil.getDialect(getProvider());
            if (dialect != null) {
                if (getDriverClassName() == null) {
                    setDriverClassName(dialect.getDriver());
                }

                if (getConnectionTestQuery() == null) {
                    setConnectionTestQuery(dialect.getTestQuery());
                }
            }
        }
        super.validate();
    }

    private static Logger logger = LoggerFactory.getLogger(HikariCP.class);


    /**
     * Start internally
     */
    public void _doStart() {
        Connection connection = null;
        try {
            connection = getConnection();
        }
        catch (SQLException e) {
            logger.error("Starting HikariCP error", e);
        }
        finally {
            IOUtil.close(connection);
        }
    }

    public void _doDestroy() {
        RepositoryUtil.getModule().delete(getName());
        this.close();
    }

    /**
     * DataSource name to differentiate different DataSource
     *
     * @return Name
     */
    @Override
    public String getName() {
        return getPoolName();
    }

    /**
     * Database product name in lower case
     *
     * @return Database product name in lower case
     */
    @Override
    public String getProvider() {
        if (provider == null) {
            String jdbcUrl = getJdbcUrl();
            if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:")) {
                int index = jdbcUrl.indexOf(':', 5);
                if (index > 5) {
                    provider = jdbcUrl.substring(5, index);
                }
            }
        }
        return provider;
    }

    /**
     * Whether the database is default,
     * the default will be injected to the bean whose doesn't specific particular DataSource
     *
     * @return IsDefault
     */
    @Override
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setName(String name) {
        if (getBeanPhase().isBeforeOrCurrent(BeanPhase.STARTING)) {
            setPoolName(name);
        }
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setJdbcUrl(String url) {
        if (getBeanPhase().isBeforeOrCurrent(BeanPhase.STARTING)) {
            super.setJdbcUrl(url);
        }
    }


    public void setConnectionInitSql(String sql) {
        if (getBeanPhase().isBeforeOrCurrent(BeanPhase.STARTING)) {
            super.setConnectionInitSql(sql);
        }
    }

    public void setConnectionTestQuery(String sql) {
        if (getBeanPhase().isBeforeOrCurrent(BeanPhase.STARTING)) {
            super.setConnectionTestQuery(sql);
        }
    }

    public void setDataSourceClassName(String className) {
        if (getBeanPhase().isBeforeOrCurrent(BeanPhase.STARTING)) {
            super.setDataSourceClassName(className);
        }
    }

    public void setDriverClassName(String className) {
        if (getBeanPhase().isBeforeOrCurrent(BeanPhase.STARTING)) {
            super.setDriverClassName(className);
        }
    }
}
