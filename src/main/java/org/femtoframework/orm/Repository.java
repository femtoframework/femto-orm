package org.femtoframework.orm;

import org.femtoframework.parameters.Parameters;
import org.femtoframework.util.CollectionUtil;

import java.util.List;

/**
 * CRUD Repository
 *
 * Naming conversion:
 *
 * ---------------------------
 * Entity Class |  Table Name
 * ---------------------------
 * Device       |  device
 * DeviceType   |  device_type
 * ---------------------------
 *
 * ---------------------------
 * Property     |  Column Name
 * ---------------------------
 * id           |  id
 * deviceId     |  device_id
 * userId       |  user_id
 * ---------------------------
 *
 * In query string, should follow column name's rule, for examples "device_id=? and user_id=?".
 * In the query parameters, we can convert it to column name's rule.
 * 
 * @param <E>
 */
public interface Repository<E> {

    String[] LIST_ALL_COLUMNS = { "*" };

    /**
     * List all entities
     *
     * @return all entities, zero size list if there is no entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default List<E> listAll() throws RepositoryException {
        return listAll(LIST_ALL_COLUMNS);
    }

    /**
     * List entities by given conditions
     *
     * @param query Query part after "WHERE" in SQL, query should use "id = ? AND name = ?" syntax
     * @param parameters Parameters in sequences
     * @return entities, zero size list if there is no entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default List<E> listBy(String query, Object... parameters) throws RepositoryException {
        return listBy(LIST_ALL_COLUMNS, query, parameters);
    }

    /**
     * List entities by given conditions
     *
     * @param query Query part after "WHERE" in SQL, query should use "id = {foo_id} AND name = {foo_name}" syntax
     * @param parameters Parameters should have {foo_id->123,foo_name->'Sheldon'}
     * @return entities, zero size list if there is no entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default List<E> listBy(String query, Parameters parameters) throws RepositoryException {
        return listBy(LIST_ALL_COLUMNS, query, parameters);
    }

    /**
     * List all entities
     *
     * @param columns Specify the columns to list, if first column is "*", means select all columns
     * @return all entities, zero size list if there is no entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    List<E> listAll(String[] columns) throws RepositoryException;

    /**
     * List entities by given conditions
     *
     * @param columns Specify the columns to list, if first column is "*", means select all columns
     * @param query Query part after "WHERE" in SQL, query should use "id = ? AND name = ?" syntax
     * @param parameters Parameters in sequences
     * @return entities, zero size list if there is no entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    List<E> listBy(String[] columns, String query, Object... parameters) throws RepositoryException;

    /**
     * List entities by given conditions
     *
     * @param columns Specify the columns to list, if first column is "*", means select all columns
     * @param query Query part after "WHERE" in SQL, query should use "id = {foo_id} AND name = {foo_name}" syntax
     * @param parameters Parameters should have {foo_id->123,foo_name->'Sheldon'}
     * @return entities, zero size list if there is no entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    List<E> listBy(String[] columns, String query, Parameters parameters) throws RepositoryException;

    /**
     * Retrieve entity by id
     *
     * @param id Entity Id
     * @return Entity, null if there is no such entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default E getById(long id) throws RepositoryException {
        return getByColumn("id", id);
    }

    /**
     * Retrieve entity by column
     *
     * @param columnName ColumnName
     * @param columnValue Column Value
     * @return Entity, null if there is no such entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default <C> E getByColumn(String columnName, C columnValue) throws RepositoryException {
        return getBy(columnName + "=?", columnValue);
    }


    /**
     * Retrieve entity by given conditions
     *
     * @param query Query part after "WHERE" in SQL, query should use "id = ? AND name = ?" syntax
     * @param parameters Parameters in sequences
     * @return Entity, null if there is no such entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    <C> E getBy(String query, Object... parameters) throws RepositoryException;

    /**
     * Retrieve entity by given conditions
     *
     * @param query Query part after "WHERE" in SQL, query should use "id = {foo_id} AND name = {foo_name}" syntax
     * @param parameters Parameters should have {foo_id->123,foo_name->'Sheldon'}
     * @return Entity, null if there is no such entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    E getBy(String query, Parameters parameters) throws RepositoryException;

    //=========CREATE==========
    /**
     * Create entity with no option
     *
     * @param entity Entity
     * @return Created or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default boolean create(E entity) throws RepositoryException {
        return create(entity, CollectionUtil.emptyParameters());
    }

    /**
     * Create entity with specific options such as {force->true} to avoid cache
     *
     * @param entity Entity entity
     * @param options Options
     * @return Created or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    boolean create(E entity, Parameters options) throws RepositoryException;

    /**
     * Create entities with no option
     *
     * @param entities Entities
     * @return Statuses of creation
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default boolean[] create(List<E> entities) throws RepositoryException {
        return create(entities, CollectionUtil.emptyParameters());
    }

    /**
     * Create entities with specific options such as {batch_size->100, ignore_error->true} to avoid cache
     *
     * @param entity Entity entity
     * @param options Options
     * @return Statuses of creation
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    boolean[] create(List<E> entity, Parameters options) throws RepositoryException;


    //=========UPDATE==========
    /**
     * Update entity with no option
     *
     * @param entity Entity
     * @return Updated or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default boolean update(E entity) throws RepositoryException {
        return update(entity, CollectionUtil.emptyParameters());
    }

    /**
     * Update entity with specific options such as {force->true} to avoid cache
     *
     * @param entity Entity entity
     * @param options Options
     * @return Updated or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    boolean update(E entity, Parameters options) throws RepositoryException;

    /**
     * Update entities with no option
     *
     * @param entities Entities
     * @return Statuses of update
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default boolean[] update(List<E> entities) throws RepositoryException {
        return update(entities, CollectionUtil.emptyParameters());
    }

    /**
     * Update entities with specific options such as {batch_size->100} to avoid cache
     *
     * @param entity Entity entity
     * @param options Options
     * @return Statuses of update
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    boolean[] update(List<E> entity, Parameters options) throws RepositoryException;


    //=========SAVE==========
    // Save means update or create
    /**
     * Save entity with no option
     *
     * @param entity Entity
     * @return 1: Created 0: Updated -1: Failed
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default int save(E entity) throws RepositoryException {
        return save(entity, CollectionUtil.emptyParameters());
    }

    /**
     * Update entity with specific options such as {force->true} to avoid cache
     *
     * @param entity Entity entity
     * @param options Options
     * @return Updated or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    int save(E entity, Parameters options) throws RepositoryException;

    /**
     * Update entities with no option
     *
     * @param entities Entities
     * @return Statuses of update
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default boolean[] save(List<E> entities) throws RepositoryException {
        return save(entities, CollectionUtil.emptyParameters());
    }

    /**
     * Update entities with specific options such as {batch_size->100} to avoid cache
     *
     * @param entity Entity entity
     * @param options Options
     * @return Statuses of update
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    boolean[] save(List<E> entity, Parameters options) throws RepositoryException;

    //==========DELETE========
    /**
     * Delete entity by id
     *
     * @param id Entity Id
     * @return Deleted or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default boolean deleteById(long id) throws RepositoryException {
        return deleteByColumn("id", id);
    }

    /**
     * Delete entity by ids
     *
     * @param id Entity Id
     * @return Deleted or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    boolean[] deleteByIds(long... id) throws RepositoryException;

    /**
     * Delete entity by column
     *
     * @param columnName ColumnName
     * @param columnValue Column Value
     * @return Deleted or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    default <C> boolean deleteByColumn(String columnName, C columnValue) throws RepositoryException {
        return deleteBy(columnName + "=?", columnValue);
    }


    /**
     * Retrieve entity by given conditions
     *
     * @param query Query part after "WHERE" in SQL, query should use "id = ? AND name = ?" syntax
     * @param parameters Parameters in sequences
     * @return Entity, null if there is no such entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    <C> boolean deleteBy(String query, Object... parameters) throws RepositoryException;

    /**
     * Retrieve entity by given conditions
     *
     * @param query Query part after "WHERE" in SQL, query should use "id = {foo_id} AND name = {foo_name}" syntax
     * @param parameters Parameters should have {foo_id->123,foo_name->'Sheldon'}
     * @return Entity, null if there is no such entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    boolean deleteBy(String query, Parameters parameters) throws RepositoryException;
}
