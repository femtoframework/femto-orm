package org.femtoframework.orm;

import org.femtoframework.parameters.Parameters;
import org.femtoframework.pattern.Factory;

/**
 * Repository Factory
 *
 * Could be multiple instances from different data sources
 */
public interface RepositoryFactory extends Factory<Repository> {

    /**
     * Return Repository by domainClass
     *
     * @param domainClass Domain Class
     * @param <E> Entity
     * @return Repository
     */
    <E> Repository<E> getRepository(Class<E> domainClass);

    /**
     * Type Safe Repository
     *
     * You don't have to define a POJO, you can use
     * @param entityName Table Name(Or entityName)
     * @return Type Safe Repository
     */
    Repository<Parameters> getTypeSafeRepository(String entityName);
}
