package org.femtoframework.orm.ext;

import org.femtoframework.orm.Repository;
import org.femtoframework.orm.RepositoryFactory;
import org.femtoframework.parameters.Parameters;
import org.femtoframework.pattern.ext.BaseFactory;
import org.femtoframework.text.NamingConvention;

import javax.sql.DataSource;

public class SimpleRepositoryFactory extends BaseFactory<Repository> implements RepositoryFactory {

    private DataSource dataSource;

    public SimpleRepositoryFactory() {
    }

    /**
     * Return Repository by domainClass
     *
     * @param domainClass Domain Class
     * @return Repository
     */
    @Override
    public <E> Repository<E> getRepository(Class<E> domainClass) {
        String tableName = NamingConvention.format(domainClass.getSimpleName());
        Repository<E> repository = get(tableName);
        if (repository == null) {
            synchronized (this) {
                repository = get(tableName);
                if (repository == null) {
                    JdbcRepository<E> jdbcRepository = new JdbcRepository<>();
                    jdbcRepository.setDataSource(dataSource);
                    jdbcRepository.setEntityClass(domainClass);
                    jdbcRepository.init();
                    add(jdbcRepository);
                    repository = jdbcRepository;
                }
            }
        }
        return repository;
    }

    /**
     * Type Safe Repository
     * <p>
     * You don't have to define a POJO, you can use
     *
     * @param entityName Table Name(Or entityName)
     * @return Type Safe Repository
     */
    @Override
    public Repository<Parameters> getTypeSafeRepository(String entityName) {
        return null;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
