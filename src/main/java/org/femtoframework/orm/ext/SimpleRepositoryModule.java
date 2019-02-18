package org.femtoframework.orm.ext;

import org.femtoframework.orm.RepositoryFactory;
import org.femtoframework.orm.RepositoryModule;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleRepositoryModule implements RepositoryModule {

    private Map<DataSource, RepositoryFactory> factoryMap = new ConcurrentHashMap<>();

    /**
     * Return RepositoryFactory by DataSource
     *
     * @param dataSource DataSource
     * @return Repository
     */
    @Override
    public RepositoryFactory getRepositoryFactory(DataSource dataSource) {
        RepositoryFactory factory = factoryMap.get(dataSource);
        if (factory == null) {
            SimpleRepositoryFactory simpleRepositoryFactory = new SimpleRepositoryFactory();
            simpleRepositoryFactory.setDataSource(dataSource);
            factoryMap.put(dataSource, simpleRepositoryFactory);
            factory = simpleRepositoryFactory;
        }
        return factory;
    }
}
