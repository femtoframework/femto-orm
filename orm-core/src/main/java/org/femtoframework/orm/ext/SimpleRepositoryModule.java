package org.femtoframework.orm.ext;

import org.femtoframework.orm.NamedDataSource;
import org.femtoframework.orm.RepositoryFactory;
import org.femtoframework.orm.RepositoryModule;
import org.femtoframework.pattern.ext.BaseFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleRepositoryModule extends BaseFactory<DataSource> implements RepositoryModule {

    private Map<DataSource, RepositoryFactory> factoryMap = new ConcurrentHashMap<>();

    /**
     * Add DataSource
     *
     * @param dataSource DataSource
     */
    @Override
    public void addDatasource(NamedDataSource dataSource) {
        add(dataSource);
    }

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

    /**
     * Return default DataSource, the logic is this,
     * 1. If there is no DataSource, return null
     * 2. If there is only one DataSource, return the only one.
     * 3. If there is multiple, select first NamedDataSource who has default=true
     * 4. No default DataSource specified, use the first one in spec
     *
     * @return The default DataSource
     */
    @Override
    public DataSource getDefaultDataSource() {
        Collection<DataSource> dataSources = getObjects();
        if (dataSources.isEmpty()) {
            return null;
        }
        else if (dataSources.size() == 1) {
            return dataSources.iterator().next();
        }
        for(DataSource dataSource : dataSources) {
            if (dataSource instanceof NamedDataSource) {
                if (((NamedDataSource)dataSource).isDefault()) {
                    return dataSource;
                }
            }
        }
        Set<String> names = getNames();
        return get(names.iterator().next());
    }
}
