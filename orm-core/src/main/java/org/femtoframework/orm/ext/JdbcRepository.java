package org.femtoframework.orm.ext;

import org.femtoframework.bean.InitializableMBean;
import org.femtoframework.bean.annotation.Ignore;
import org.femtoframework.bean.info.BeanInfo;
import org.femtoframework.bean.info.BeanInfoUtil;
import org.femtoframework.bean.info.PropertyInfo;
import org.femtoframework.implement.ImplementUtil;
import org.femtoframework.lang.reflect.Reflection;
import org.femtoframework.orm.Limit;
import org.femtoframework.orm.Repository;
import org.femtoframework.orm.RepositoryException;
import org.femtoframework.orm.SortBy;
import org.femtoframework.orm.dialect.RdbmsDialect;
import org.femtoframework.parameters.Parameters;
import org.femtoframework.text.NamingConvention;
import org.femtoframework.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JdbcRepository<E> implements Repository<E>, InitializableMBean {

    private static Logger logger = LoggerFactory.getLogger(JdbcRepository.class);

    private DataSource dataSource;

    private String entityType;

    @Ignore
    private Class<E> entityClass;

    private BeanInfo beanInfo;

    private String tableName;

    protected Connection getConnection() throws RepositoryException {
        try {
            return dataSource.getConnection();
        }
        catch(SQLException sqle) {
            throw new RepositoryException("Get a new connection error", sqle);
        }
    }

    /**
     * List entities by given conditions
     *
     * @param columns    Specify the columns to list, if first column is "*", means select all columns
     * @param limit      Limit the result set
     * @param sortBy     SortBy specific column
     * @param query      Query part after "WHERE" in SQL, query should use "id = ? AND name = ?" syntax
     * @param parameters Parameters in sequences
     * @return entities, zero size list if there is no entity
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public List<E> listBy(String[] columns, Limit limit, SortBy sortBy, String query, Object... parameters) throws RepositoryException {
        StringBuilder sb = new StringBuilder(128);
        sb.append("SELECT ");
        if (columns == null || columns.length == 0) {
            sb.append(LIST_ALL_COLUMNS[0]);
        }
        else {
            if (columns.length == 1 && "*".equals(columns[0])) {
                sb.append(LIST_ALL_COLUMNS[0]);
            }
            else {
                boolean first = true;
                for(String column: columns) {
                    if (first) {
                        first = false;
                        sb.append(column);
                    }
                    else {
                        sb.append(',').append(column);
                    }
                }
            }
        }
        sb.append(" FROM ").append(tableName);

        if (StringUtil.isInvalid(query)) {
            if (!(parameters == null || parameters.length == 0)) {
                throw new IllegalArgumentException("There is parameter, but not query condition");
            }
        }
        else {
            sb.append(" WHERE ").append(query);
        }

        int off = limit == null ? 0 : limit.getOffset();
        int lmt = limit == null || dialect.useMaxForLimit() ? -1 : limit.getLimit();
        String newSql = sb.toString();
        if (dialect.supportsLimit()) {
            newSql = dialect.getLimitString(newSql, off, lmt);
        }

        try (Connection conn = getConnection()) {
            ResultSet rs = null;
            try (PreparedStatement pstmt = conn.prepareStatement(newSql)) {
                if (parameters != null && parameters.length > 0) {
                    for(int i = 1; i <= parameters.length; i ++) {
                        pstmt.setObject(i, parameters);
                    }
                }

                if (off > 0) {
                    pstmt.setInt(parameters.length+1, off); //offset
                }
                if (lmt > 0) {
                    pstmt.setInt(parameters.length+1, lmt);
                }

                int end = lmt > 0 ? lmt : limit != null ? limit.getLimit() : Integer.MAX_VALUE; //end
                rs = pstmt.executeQuery();

                List<E> list = new ArrayList<E>(lmt > 0 ? lmt : 10);
                int i = 0;
                while (rs.next() && i < end) {
                    list.add(newEntity(rs));
                    i ++;
                }
                rs.close();
                return list;
            } catch (Exception e) {
                if (rs != null) {
                    rs.close();
                }
                String msg = "Creating entity " + toString(newSql, parameters) + " error";
                logger.error(msg, e);
                throw new RepositoryException(msg, e);
            }
        }
        catch(SQLException sqle) {
            String msg = "Execute sql:" + toString(newSql, parameters) + " error";
            logger.error(msg, sqle);
            throw new RepositoryException(msg, sqle);
        }
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
        String sql = "SELECT * FROM " + tableName + " WHERE " + query ;
        try (Connection conn = getConnection()) {
            ResultSet rs = null;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (parameters != null && parameters.length > 0) {
                    for(int i = 1; i <= parameters.length; i ++) {
                        pstmt.setObject(i, parameters);
                    }
                }
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    E entity = newEntity(rs);
                    rs.close();
                    return entity;
                }
                else {
                    rs.close();
                    return null;
                }
            } catch (Exception e) {
                if (rs != null) {
                    rs.close();
                }
                String msg = "Creating entity " + toString(sql, parameters) + " error";
                logger.error(msg, e);
                throw new RepositoryException(msg, e);
            }
        }
        catch(SQLException sqle) {
            String msg = "Execute sql:" + toString(sql, parameters) + " error";
            logger.error(msg, sqle);
            throw new RepositoryException(msg, sqle);
        }
    }

    protected E newEntity(ResultSet rs) throws Exception {
        E entity = entityClass.newInstance();
        Collection<PropertyInfo> propertyInfos = beanInfo.getProperties();
        for(PropertyInfo info: propertyInfos) {
            if (info.isWritable()) {
                info.invokeSetter(entity, rs.getObject(NamingConvention.format(info.getName())));
            }
        }
        return entity;
    }


    private String insertSQL = null;


    protected String getInsertSQL() {
        if (insertSQL == null) {
            StringBuilder sb = new StringBuilder(128);
            sb.append("INSERT INTO " + tableName + " (");
            Collection<PropertyInfo> propertyInfos = beanInfo.getProperties();
            boolean first = true;
            int count = 0;
            for(PropertyInfo propertyInfo: propertyInfos) {
                if (propertyInfo.isReadable()) {
                    String columnName = NamingConvention.format(propertyInfo.getName());
                    if (first) {
                        first = false;
                        sb.append(columnName);
                    }
                    else {
                        sb.append(',').append(columnName);
                    }
                    count ++;
                }
            }
            sb.append(") VALUES (?");
            for(int i = 1; i < count; i ++) {
                sb.append(",?");
            }
            sb.append(')');
            insertSQL = sb.toString();
        }
        return insertSQL;
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
        String sql = getInsertSQL();
        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int i = 1;
                for(PropertyInfo propertyInfo: beanInfo.getProperties()) {
                    if (propertyInfo.isReadable()) {
                        pstmt.setObject(i ++, propertyInfo.invokeGetter(entity));
                    }
                }
                return pstmt.executeUpdate() >= 1;
            }
        }
        catch(SQLException sqle) {
            String msg = "Execute sql:" + toString(sql, null) + " error";
            logger.error(msg, sqle);
            throw new RepositoryException(msg, sqle);
        }
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
        String sql = getInsertSQL();
        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for(E e: entity) {
                    int i = 1;
                    for (PropertyInfo propertyInfo : beanInfo.getProperties()) {
                        if (propertyInfo.isReadable()) {
                            pstmt.setObject(i++, propertyInfo.invokeGetter(e));
                        }
                    }
                    pstmt.addBatch();
                }
                int[] batch = pstmt.executeBatch();
                boolean[] result = new boolean[batch.length];
                for(int i = 0; i < batch.length; i ++) {
                    result[i] = batch[i] > 0;
                }
                return result;
            }
        }
        catch(SQLException sqle) {
            String msg = "Execute sql:" + toString(sql, null) + " error";
            logger.error(msg, sqle);
            throw new RepositoryException(msg, sqle);
        }
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
     * @param ids Entity Id
     * @return Deleted or not
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public boolean[] deleteByIds(long... ids) throws RepositoryException {
        String sql = "DELETE FROM " + tableName + " WHERE id=?" ;
        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (long id : ids) {
                    pstmt.setLong(1, id);
                    pstmt.addBatch();
                }
                int[] result = pstmt.executeBatch();
                boolean[] array = new boolean[result.length];
                int i = 0;
                for (int r : result) {
                    array[i] = r > 0;
                }
                return array;
            }
        }
        catch(SQLException sqle) {
            String msg = "Execute sql:" + sql + " error";
            logger.error(msg, sqle);
            throw new RepositoryException(msg, sqle);
        }
    }

    /**
     * Convert sql and parameters information to string
     *
     * @param sql SQL
     * @param parameters Parameters
     * @return String
     */
    protected String toString(String sql, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return sql;
        }
        else if (parameters.length == 1) {
            return sql + " " + String.valueOf(parameters[0]);
        }
        else {
            return sql + " [" + StringUtil.toString(parameters, ',') + "]";
        }
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
        if (StringUtil.isInvalid(query)) {
            throw new IllegalArgumentException("No any condition in the query:" + query);
        }
        String sql = "DELETE FROM " + tableName + " WHERE " + query;
        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (parameters != null && parameters.length > 0) {
                    for (int i = 1; i <= parameters.length; i++) {
                        pstmt.setObject(i, parameters);
                    }
                }
                return pstmt.executeUpdate() >= 1;
            }
        }
        catch(SQLException sqle) {
            String msg = "Execute sql:" + toString(sql, parameters) + " error";
            logger.error(msg, sqle);
            throw new RepositoryException(msg, sqle);
        }
    }

    private boolean initialized = false;

    /**
     * Return whether it is initialized
     *
     * @return whether it is initialized
     */
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Initialized setter for internal
     *
     * @param initialized BeanPhase
     */
    @Override
    public void _doSetInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    private RdbmsDialect dialect;

//    /**
//     * Table Metadata
//     */
//    protected void fetchTableMetadata() {
//        try (Connection conn = dataSource.getConnection()) {
//            Statement statement = conn.createStatement();
//            statement.execute("SELECT * FROM " + tableName + " ")
//            String productName = metaData.getDatabaseProductName();
//            dialect = ImplementUtil.getInstance(productName.toLowerCase(), RdbmsDialect.class);
//        }
//        catch(SQLException sqle) {
//            logger.error("Retrieve database information error", sqle);
//            throw new IllegalStateException("Retrieve database information error", sqle);
//        }
//    }

    /**
     * Initiliaze internally
     */
    @Override
    public void _doInitialize() {
        if (dataSource == null) {
            throw new IllegalStateException("No data source was bound to this repository for table:" + tableName);
        }

        if (entityType == null) {
            throw new IllegalStateException("No entity type set");
        }

        //MySQL

        try {
            entityClass = Reflection.getClass(entityType);
        }
        catch(ClassNotFoundException cnfe) {
            throw new IllegalStateException("No such class:" + entityType, cnfe);
        }

        if (tableName == null) {
            this.tableName = NamingConvention.format(entityClass.getSimpleName());
        }
        this.beanInfo = BeanInfoUtil.getBeanInfo(entityClass, true);

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String productName = metaData.getDatabaseProductName();
            dialect = ImplementUtil.getInstance(productName.toLowerCase(), RdbmsDialect.class);
        }
        catch(SQLException sqle) {
            logger.error("Retrieve database information error", sqle);
            throw new IllegalStateException("Retrieve database information error", sqle);
        }
    }

    public RdbmsDialect getDialect() {
        return dialect;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getTableName() {
        return tableName;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    private Map<String, IndexedQuery> queryMap = new ConcurrentHashMap<>();

    /**
     * Supports cache for query condition
     *
     * @param query Condition
     * @return IndexedQuery
     */
    public IndexedQuery toIndexedQuery(String query) {
        if (StringUtil.isInvalid(query)) {
            return null;
        }
        IndexedQuery indexedQuery = queryMap.get(query);
        if (indexedQuery == null) {
            indexedQuery = Repository.super.toIndexedQuery(query);
            queryMap.put(query, indexedQuery);
        }
        return indexedQuery;
    }

    /**
     * Name of the object
     *
     * @return Name of the object
     */
    @Override
    public String getName() {
        return getTableName();
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<E> entityClass) {
        this.entityClass = entityClass;
        setEntityType(entityClass.getName());
    }
}
