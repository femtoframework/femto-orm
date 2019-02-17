package org.femtoframework.orm.dialect;

/**
 * Postgres
 */
public class PostgresDialect implements RdbmsDialect {
    /**
     * Get the default driver class
     */
    @Override
    public String getDriver() {
        return "org.postgresql.Driver";
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
        return querySelect + (hasOffset ? " limit ? offset ?" : " limit ?");
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
        return "select " + getSelectSequenceNextVal(name);
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
        return "nextval ('" + name + "')";
    }

    /**
     * Name of the object
     *
     * @return Name of the object
     */
    @Override
    public String getName() {
        return "postgres";
    }
}
