package org.femtoframework.orm.dialect;

/**
 * EnterpriseDB
 */
public class EnterpriseDBDialect extends PostgresDialect {

    @Override
    public String getDriver() {
        return "com.edb.Driver";
    }

    /**
     * Name of the object
     *
     * @return Name of the object
     */
    @Override
    public String getName() {
        return "enterprisedb";
    }
}
