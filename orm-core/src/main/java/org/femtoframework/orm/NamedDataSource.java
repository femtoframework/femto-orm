package org.femtoframework.orm;

import org.femtoframework.bean.NamedBean;

import javax.sql.DataSource;

/**
 * Named DataSource
 */
public interface NamedDataSource extends DataSource, NamedBean {

    /**
     * DataSource name to differentiate different DataSource
     *
     * @return Name
     */
    String getName();

    /**
     * Database product name in lower case
     *
     * @return Database product name in lower case
     */
    String getProvider();

    /**
     * Whether the database is default,
     * the default will be injected to the bean whose doesn't specific particular DataSource
     *
     * @return IsDefault
     */
    boolean isDefault();
}
