package org.femtoframework.orm.ext;

import org.femtoframework.bean.InitializableMBean;
import org.femtoframework.bean.annotation.Ignore;
import org.femtoframework.bean.annotation.Property;
import org.femtoframework.bean.info.BeanInfo;
import org.femtoframework.bean.info.BeanInfoUtil;
import org.femtoframework.bean.info.PropertyInfo;
import org.femtoframework.lang.reflect.Reflection;
import org.femtoframework.orm.*;
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

    private PropertyInfo idPropertyInfo = null;

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
                    for(int i = 0; i < parameters.length; i ++) {
                        pstmt.setObject(i+1, parameters[i]);
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
                    for(int i = 0; i < parameters.length; i ++) {
                        pstmt.setObject(i+1, parameters[i]);
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

    private String idSeqSql = null;

    protected void setId(Connection conn, E entity) throws RepositoryException {
        if (dialect.supportsSequence()) {
            if (idPropertyInfo != null && idPropertyInfo.isWritable()) {
                if (idSeqSql == null) {
                    idSeqSql = dialect.getSelectSequenceNextVal(tableName + "_id_seq");
                }

                try (PreparedStatement pstmt = conn.prepareStatement(idSeqSql)) {
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        idPropertyInfo.invokeSetter(entity, id);
                    }
                    rs.close();
                } catch (SQLException sqle) {
                    String msg = "Execute sql:" + toString(idSeqSql, null) + " error";
                    logger.error(msg, sqle);
                    throw new RepositoryException(msg, sqle);
                }
            }
        }
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

    private String updateSQL = null;

    protected String getUpdateSQL() {
        if (updateSQL == null) {
            StringBuilder sb = new StringBuilder(128);
            sb.append("UPDATE ").append(tableName).append(" SET ");
            Collection<PropertyInfo> propertyInfos = beanInfo.getProperties();
            boolean first = true;
            for(PropertyInfo propertyInfo: propertyInfos) {
                if (propertyInfo.isReadable()) {
                    String name = propertyInfo.getName();
                    if (!"id".equalsIgnoreCase(name)) {
                        String columnName = NamingConvention.format(name);
                        if (first) {
                            first = false;
                            sb.append(columnName).append("=?");
                        }
                        else {
                            sb.append(',').append(columnName).append("=?");
                        }
                    }
                }
            }
            sb.append(" WHERE id = ?");
            updateSQL = sb.toString();
        }
        return updateSQL;
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
            setId(conn, entity);
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

                    setId(conn, e);

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
        String updateSQL = getUpdateSQL();

        try (Connection conn = getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                fillForUpdate(pstmt, entity);
                return pstmt.executeUpdate() >= 1;
            }
        }
        catch(SQLException sqle) {
            String msg = "Execute sql:" + toString(updateSQL, null) + " error";
            logger.error(msg, sqle);
            throw new RepositoryException(msg, sqle);
        }
    }


    protected PropertyInfo fillForUpdate(PreparedStatement pstmt, E entity) throws SQLException {
        int i = 1;

        for(PropertyInfo propertyInfo: beanInfo.getProperties()) {
            if (propertyInfo.isReadable()) {
                String name = propertyInfo.getName();
                if (!"id".equalsIgnoreCase(name)) {
                    pstmt.setObject(i++, propertyInfo.invokeGetter(entity));
                }
                else {
                    idPropertyInfo = propertyInfo;
                }
            }
        }
//        if (idPropertyInfo == null) {
//            throw new IllegalStateException("No 'id'?");
//        }
//        else {
        int id = idPropertyInfo.invokeGetter(entity);
        if (id == 0) {
            throw new IllegalStateException("The id is zero");
        }
        pstmt.setObject(i, id);
//        }
        return idPropertyInfo;
    }

    /**
     * Update entities with specific options such as {batch_size->100} to avoid cache
     *
     * @param entities  Entity entity
     * @param options Options
     * @return Statuses of update
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public boolean[] update(List<E> entities, Parameters options) throws RepositoryException {
        String updateSQL = getUpdateSQL();

        try (Connection conn = getConnection()) {
            boolean[] result = new boolean[entities.size()];
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                for(E entity: entities) {
                    fillForUpdate(pstmt, entity);
                    pstmt.addBatch();
                }
                int[] batchResult = pstmt.executeBatch();
                int i = 0;
                for (int r : batchResult) {
                    result[i] = r > 0;
                }
                return result;
            }
        }
        catch(SQLException sqle) {
            String msg = "Execute sql:" + toString(updateSQL, null) + " error";
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
    public int save(E entity, Parameters options) throws RepositoryException {
        int id = idPropertyInfo.invokeGetter(entity);
        if (id == 0) {
            //Create
            return create(entity, options) ? 1 : -1;
        }
        else {
            //Update
            return update(entity, options) ? 0 : -1;
        }
    }

    /**
     * Update entities with specific options such as {batch_size->100} to avoid cache
     *
     * @param entities  Entities
     * @param options Options
     * @return Statuses of update
     * @throws RepositoryException SQL Exception or downstream exceptions
     */
    @Override
    public boolean[] save(List<E> entities, Parameters options) throws RepositoryException {
        List<E> toCreate = new ArrayList<>(entities.size());
        List<E> toUpdate = new ArrayList<>(entities.size());
        boolean[] result = new boolean[entities.size()];
        int i = 0;
        for(E entity: entities) {
            int id = idPropertyInfo.invokeGetter(entity);
            if (id == 0) {
                toCreate.add(entity);
                result[i++] = true;
            }
            else {
                toUpdate.add(entity);
                result[i++] = false;
            }
        }

        boolean[] created = create(toCreate, options);
        boolean[] updated = update(toUpdate, options);
        int j = 0, k = 0;
        for(i = 0; i < result.length; i ++) {
            if (result[i]) { //Create
                result[i++] = created[j++];
            }
            else {
                result[i++] = updated[k++];
            }
        }
        return result;
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
                    for (int i = 0; i < parameters.length; i++) {
                        pstmt.setObject(i+1, parameters[i]);
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


    @Override
    public void init() {
        InitializableMBean.super.init();
    }

    /**
     * Initiliaze internally
     */
    @Override
    public void _doInit() {
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
        this.idPropertyInfo = beanInfo.getProperty("id");
        if (idPropertyInfo == null) {
            throw new IllegalStateException("No 'id' in the entity");
        }
        this.dialect = RepositoryUtil.getDialect(dataSource);
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
