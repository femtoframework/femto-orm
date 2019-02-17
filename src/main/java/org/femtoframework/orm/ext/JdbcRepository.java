package org.femtoframework.orm.ext;

import lombok.Getter;
import lombok.Setter;
import org.femtoframework.coin.info.BeanInfo;
import org.femtoframework.coin.info.BeanInfoFactory;
import org.femtoframework.coin.spi.BeanInfoFactoryAware;
import org.femtoframework.orm.Repository;
import org.femtoframework.orm.RepositoryException;
import org.femtoframework.parameters.Parameters;
import org.femtoframework.text.NamingConvention;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class JdbcRepository<E> implements Repository<E>, BeanInfoFactoryAware {

    private BeanInfoFactory beanInfoFactory;

    @Setter
    private DataSource dataSource;

    private Class<E> entityType;

    private BeanInfo beanInfo;

    @Getter
    private String tableName;

    public JdbcRepository(Class<E> entityType) {

//        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
//        metaData.getDatabaseProductName()
        this.entityType = entityType;
        this.tableName = NamingConvention.format(entityType);
        this.beanInfo = beanInfoFactory.getBeanInfo(entityType, true);
    }

    protected E toEntity(ResultSet rs, String[] columns) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        return null;
    }

    protected Connection getConnection() throws RepositoryException {
        try {
            return dataSource.getConnection();
        }
        catch(SQLException sqle) {
            throw new RepositoryException("Get a new connection error", sqle);
        }
    }

    /**
     * List all entities
     *
     * @param columns Specify the columns to list, if first column is "*", means select all columns
     * @return all entities, zero size list if there is no entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public List<E> listAll(String[] columns) throws RepositoryException {
        return null;
    }

    /**
     * List entities by given conditions
     *
     * @param columns    Specify the columns to list, if first column is "*", means select all columns
     * @param query      Query part after "WHERE" in SQL, query should use "id = ? AND name = ?" syntax
     * @param parameters Parameters in sequences
     * @return entities, zero size list if there is no entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public List<E> listBy(String[] columns, String query, Object... parameters) throws RepositoryException {
        return null;
    }

    /**
     * List entities by given conditions
     *
     * @param columns    Specify the columns to list, if first column is "*", means select all columns
     * @param query      Query part after "WHERE" in SQL, query should use "id = {foo_id} AND name = {foo_name}" syntax
     * @param parameters Parameters should have {foo_id->123,foo_name->'Sheldon'}
     * @return entities, zero size list if there is no entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public List<E> listBy(String[] columns, String query, Parameters parameters) throws RepositoryException {
        return null;
    }

    /**
     * Retrieve entity by given conditions
     *
     * @param query      Query part after "WHERE" in SQL, query should use "id = ? AND name = ?" syntax
     * @param parameters Parameters in sequences
     * @return Entity, null if there is no such entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public <C> E getBy(String query, Object... parameters) throws RepositoryException {
        return null;
    }

    /**
     * Retrieve entity by given conditions
     *
     * @param query      Query part after "WHERE" in SQL, query should use "id = {foo_id} AND name = {foo_name}" syntax
     * @param parameters Parameters should have {foo_id->123,foo_name->'Sheldon'}
     * @return Entity, null if there is no such entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public E getBy(String query, Parameters parameters) throws RepositoryException {
        return null;
    }

    /**
     * Create entity with specific options such as {force->true} to avoid cache
     *
     * @param entity  Entity entity
     * @param options Options
     * @return Created or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public boolean create(E entity, Parameters options) throws RepositoryException {
        return false;
    }

    /**
     * Create entities with specific options such as {batch_size->100, ignore_error->true} to avoid cache
     *
     * @param entity  Entity entity
     * @param options Options
     * @return Statuses of creation
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public boolean[] create(List<E> entity, Parameters options) throws RepositoryException {
        return new boolean[0];
    }

    /**
     * Update entity with specific options such as {force->true} to avoid cache
     *
     * @param entity  Entity entity
     * @param options Options
     * @return Updated or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public boolean update(E entity, Parameters options) throws RepositoryException {
        return false;
    }

    /**
     * Update entities with specific options such as {batch_size->100} to avoid cache
     *
     * @param entity  Entity entity
     * @param options Options
     * @return Statuses of update
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public boolean[] update(List<E> entity, Parameters options) throws RepositoryException {
        return new boolean[0];
    }

    /**
     * Update entity with specific options such as {force->true} to avoid cache
     *
     * @param entity  Entity entity
     * @param options Options
     * @return Updated or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public int save(E entity, Parameters options) throws RepositoryException {
        return 0;
    }

    /**
     * Update entities with specific options such as {batch_size->100} to avoid cache
     *
     * @param entity  Entity entity
     * @param options Options
     * @return Statuses of update
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public boolean[] save(List<E> entity, Parameters options) throws RepositoryException {
        return new boolean[0];
    }

    /**
     * Delete entity by ids
     *
     * @param id Entity Id
     * @return Deleted or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public boolean[] deleteByIds(long... id) throws RepositoryException {
        return new boolean[0];
    }

    /**
     * Retrieve entity by given conditions
     *
     * @param query      Query part after "WHERE" in SQL, query should use "id = ? AND name = ?" syntax
     * @param parameters Parameters in sequences
     * @return Entity, null if there is no such entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public <C> boolean deleteBy(String query, Object... parameters) throws RepositoryException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM " + tableName + " WHERE " + query);

        }
        catch(SQLException sqle) {

        }
        return false;
    }

    /**
     * Retrieve entity by given conditions
     *
     * @param query      Query part after "WHERE" in SQL, query should use "id = {foo_id} AND name = {foo_name}" syntax
     * @param parameters Parameters should have {foo_id->123,foo_name->'Sheldon'}
     * @return Entity, null if there is no such entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public boolean deleteBy(String query, Parameters parameters) throws RepositoryException {
        return false;
    }

    /**
     * Set BeanInfoFactory
     *
     * @param factory BeanInfoFactory
     */
    @Override
    public void setBeanInfoFactory(BeanInfoFactory factory) {
        this.beanInfoFactory = factory;
    }
}
