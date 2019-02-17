package org.femtoframework.orm.dialect;

public class Db2Dialect implements RdbmsDialect {
    /**
     * Get the default driver class
     */
    @Override
    public String getDriver() {
        return "COM.ibm.db2.jdbc.net.DB2Driver";
    }

    public boolean useMaxForLimit()
    {
        return true;
    }

    /**
     * Render the <tt>rownumber() over ( .... ) as rownumber_,</tt>
     * bit, that goes in the select list
     */
    private StringBuilder appendRowNumber(StringBuilder sb, String sql)
    {
        sb.append("rownumber() over(");
        int orderByIndex = sql.toLowerCase().indexOf("order by");

        if (orderByIndex > 0 && !hasDistinct(sql)) {
            sb.append(sql.substring(orderByIndex));
        }
        sb.append(") as rownumber_,");
        return sb;
    }

    private static boolean hasDistinct(String sql)
    {
        return sql.toLowerCase().contains("select distinct");
    }

    /**
     * Add LIMIT on query
     *
     * @param sql Original SELECT query
     * @param hasOffset   whether the offset start with 0
     * @return new SQL
     */
    @Override
    public String getLimitString(String sql, boolean hasOffset) {

        int startOfSelect = sql.toLowerCase().indexOf("select");
        StringBuilder pagingSelect = new StringBuilder(sql.length() + 100)
                .append(sql, 0, startOfSelect)    // add the comment
                .append("select * from ( select ");             // nest the main query in an outer select
        appendRowNumber(pagingSelect, sql);                 // add the rownnumber bit into the outer query select list

        if (hasDistinct(sql)) {
            pagingSelect.append(" row_.* from ( ")            // add another (inner) nested select
                    .append(sql.substring(startOfSelect)) // add the main query
                    .append(" ) as row_");                     // close off the inner nested select
        }
        else {
            pagingSelect.append(sql.substring(startOfSelect + 6)); // add the main query
        }

        pagingSelect.append(" ) as temp_ where rownumber_ ");

        //add the restriction to the outer select
        if (hasOffset) {
            pagingSelect.append("between ?+1 and ?");
        }
        else {
            pagingSelect.append("<= ?");
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
        return "values nextval for " + name;
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
        return "select " + getSequenceNextVal(name);
    }

    /**
     * Name of the object
     *
     * @return Name of the object
     */
    @Override
    public String getName() {
        return "db2";
    }
}
