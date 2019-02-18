package org.femtoframework.orm;

import org.femtoframework.pattern.Factory;

import javax.sql.DataSource;

public interface RepositoryModule extends Factory<DataSource> {

    /**
     * Return RepositoryFactory by DataSource
     *
     * @param dataSource DataSource
     * @return Repository
     */
    RepositoryFactory getRepositoryFactory(DataSource dataSource);

    /**
     * Return default DataSource, the logic is this,
     * 1. If there is no DataSource, return null
     * 2. If there is only one DataSource, return the only one.
     * 3. If there is multiple, select first NamedDataSource who has default=true
     * 4. No default DataSource specified, use the first one in spec
     *
     * @return The default DataSource
     */
    DataSource getDefaultDataSource();

}
