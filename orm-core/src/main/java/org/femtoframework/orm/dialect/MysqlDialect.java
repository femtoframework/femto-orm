package org.femtoframework.orm.dialect;

/**
 * Mysql Dialect
 */
public class MysqlDialect implements RdbmsDialect {
    /**
     * Get the default driver class
     */
    @Override
    public String getDriver() {
        return "com.mysql.cj.jdbc.Driver";
    }

    /**
     * Return the data source class name
     *
     * @return DataSource class
     */
    @Override
    public String getDataSourceClass() {
        return "com.mysql.jdbc.jdbc2.optional.MysqlDataSource";
    }

    /**
     * Whether the DB supports "LIMIT" in SELECT SQL
     *
     * @return LIMIT the records
     */
    public boolean supportsLimit() {
        return true;
    }

    /**
     * Add LIMIT on query
     *
     * @param querySelect Original SELECT query
     * @param hasOffset   whether the offset start with 0
     * @return new SQL
     */
    @Override
    public String getLimitString(String querySelect, boolean hasOffset) {
        querySelect = querySelect.trim();
        return querySelect + (hasOffset ? " LIMIT ?, ?" : " LIMIT ?");
    }

    /**
     * Name of the object
     *
     * @return Name of the object
     */
    @Override
    public String getName() {
        return "mysql";
    }
}
