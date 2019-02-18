package org.femtoframework.orm.dialect;

import org.femtoframework.bean.NamedBean;

/**
 * Represents a dialect of SQL implemented by a particular RDBMS.
 */
public interface RdbmsDialect extends NamedBean {

    /**
     * Get the default driver class
     */
    String getDriver();

    /**
     * Return the data source class name
     *
     * @return DataSource class
     */
    String getDataSourceClass();

    /**
     * Whether the DB supports "LIMIT" in SELECT SQL
     *
     * @return LIMIT the records
     */
    default boolean supportsLimit() {
        return false;
    }

    /**
     * Some databases don't need to specify the maximum limit, since the ResultSet is lazy loading
     */
    default boolean useMaxForLimit() {
        return false;
    }

    /**
     * Wrap the SELECT with given offset and limit
     *
     * @param querySelect Original SELECT query
     * @param offset Offset
     * @param limit LIMIT
     * @return NEW SQL
     */
    default String getLimitString(String querySelect, int offset, int limit) {
        return getLimitString(querySelect, offset > 0);
    }

    /**
     * Add LIMIT on query
     *
     * @param querySelect Original SELECT query
     * @param hasOffset whether the offset start with 0
     * @return new SQL
     */
    String getLimitString(String querySelect, boolean hasOffset);


    /**
     * Whether the database supports Sequence
     *
     * @return Mysql doesn't support
     */
    default boolean supportsSequence() {
        return false;
    }

    /**
     * Generate the appropriate select statement to to retrieve the next value
     * of a sequence, if sequences are supported.
     * <p/>
     * This should be a "stand alone" select statement.
     *
     * @param name the name of the sequence
     * @return String The "nextval" select string.
     */
    default String getSequenceNextVal(String name) {
        throw new IllegalStateException("DB:" + getName() + " doesn't support sequence");
    }

    /**
     * Generate the SELECT expression fragment that will retrieve the next
     * value of a sequence, if sequences are supported.
     * <p/>
     * This differs from {@link #getSequenceNextVal(String)} in that this
     * should return an expression usable within another select statement.
     *
     * @param name the name of the sequence
     * @return String
     */
    default String getSelectSequenceNextVal(String name) {
        throw new IllegalStateException("DB:" + getName() + " doesn't support sequence");
    }

    /**
     * Test Query
     *
     * @return Test Query
     */
    default String getTestQuery() {
        return "SELECT 1";
    }
}
