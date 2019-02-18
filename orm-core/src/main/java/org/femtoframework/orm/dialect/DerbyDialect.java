package org.femtoframework.orm.dialect;

/**
 * Derby
 */
public class DerbyDialect extends Db2Dialect {

    @Override
    public String getDriver() {
        return "org.apache.derby.jdbc.EmbeddedDriver";
    }

    public String getName() {
        return "derby";
    }

    /**
     * Return the data source class name
     *
     * @return DataSource class
     */
    @Override
    public String getDataSourceClass() {
        return "org.apache.derby.jdbc.ClientDataSource";
    }
}
