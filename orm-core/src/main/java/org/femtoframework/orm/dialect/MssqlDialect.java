package org.femtoframework.orm.dialect;

/**
 * SQL Server 2012
 */
public class MssqlDialect implements RdbmsDialect {
    /**
     * Get the default driver class
     */
    @Override
    public String getDriver() {
        return "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    }

    /**
     * Return the data source class name
     *
     * @return DataSource class
     */
    @Override
    public String getDataSourceClass() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDataSource";
    }

    public boolean supportsLimit()
    {
        return true;
    }

    /**
     * Does the <tt>LIMIT</tt> clause take a "maximum" row number instead
     * of a total number of returned rows?
     */
    public boolean useMaxForLimit()
    {
        return true;
    }

    /**
     * SELECT email FROM emailTable
     * WHERE user_id=3
     * ORDER BY Id
     * OFFSET 10 ROWS
     * FETCH NEXT 10 ROWS ONLY;
     * OFFSET: number of skipped rows
     * NEXT: required number of next rows
     */
    public String getLimitString(String sql, boolean hasOffset)
    {
        return sql + (hasOffset ? " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY" : " FETCH NEXT ? ROWS ONLY");
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
        return "NEXTVAL ('" + name + "')";
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
        return "SELECT " + getSequenceNextVal(name);
    }

    /**
     * Name of the object
     *
     * @return Name of the object
     */
    @Override
    public String getName() {
        return "mssql";
    }
}
