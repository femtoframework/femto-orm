package org.femtoframework.orm.dialect;

public class OracleDialect implements RdbmsDialect {
    /**
     * Get the default driver class
     */
    @Override
    public String getDriver() {
        return "oracle.jdbc.driver.OracleDriver";
    }

    /**
     * Return the data source class name
     *
     * @return DataSource class
     */
    @Override
    public String getDataSourceClass() {
        return "oracle.jdbc.pool.OracleDataSource\n";
    }

    /**
     * Some databases don't need to specify the maximum limit, since the ResultSet is lazy loading
     */
    public boolean useMaxForLimit() {
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
        boolean isForUpdate = false;
        if (querySelect.toUpperCase().endsWith(" FOR UPDATE")) {
            querySelect = querySelect.substring(0, querySelect.length() - 11);
            isForUpdate = true;
        }

        StringBuilder pagingSelect = new StringBuilder(querySelect.length() + 100);
        if (hasOffset) {
            pagingSelect.append("SELECT * FROM ( SELECT ROW_.*, ROWNUM ROWNUM_ FROM ( ");
        }
        else {
            pagingSelect.append("SELECT * FROM ( ");
        }
        pagingSelect.append(querySelect);
        if (hasOffset) {
            pagingSelect.append(" ) ROW_ WHERE ROWNUM <= ?) WHERE ROWNUM_ > ?");
        }
        else {
            pagingSelect.append(" ) WHERE ROWNUM <= ?");
        }

        if (isForUpdate) {
            pagingSelect.append(" FOR UPDATE");
        }

        return pagingSelect.toString();
    }

    /**
     * Whether the database supports Sequence
     *
     * @return Mysql doesn't support
     */
    public boolean supportsSequence() {
        return true;
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
    public String getSequenceNextVal(String name) {
        return name + ".NEXTVAL";
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
    public String getSelectSequenceNextVal(String name) {
        return "SELECT " + getSelectSequenceNextVal(name) + " FROM DUAL";
    }

    /**
     * Name of the object
     *
     * @return Name of the object
     */
    @Override
    public String getName() {
        return "oracle";
    }

    /**
     * Test Query
     *
     * @return Test Query
     */
    public String getTestQuery() {
        return "SELECT 1 FROM DUAL";
    }
}
