package org.femtoframework.orm;

import javax.sql.DataSource;

public interface RepositoryModule {

    /**
     * Return RepositoryFactory by DataSource
     *
     * @param dataSource DataSource
     * @return Repository
     */
    RepositoryFactory getRepositoryFactory(DataSource dataSource);
}
