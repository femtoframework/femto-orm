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
     * @param tableName If it is null, will use name conversion from DomainClass
     * @param domainClass Domain Class
     * @param <E> Entity
     * @return Repository
     */
    public <E> Repository<E> getRepository(String tableName, Class<E> domainClass) {
        tableName = tableName == null ? NamingConvention.format(domainClass.getSimpleName()) : tableName;
        Repository<E> repository = get(tableName);
        if (repository == null) {
            synchronized (this) {
                repository = get(tableName);
                if (repository == null) {
                    JdbcRepository<E> jdbcRepository = new JdbcRepository<>();
                    jdbcRepository.setTableName(tableName);
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
