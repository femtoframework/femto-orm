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
        StringBuilder sb = new StringBuilder(querySelect.length() + 20);
        sb.append(querySelect);
        sb.append(hasOffset ? " LIMIT ?, ?" : " LIMIT ?");
        return sb.toString();
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
