package org.femtoframework.orm.dialect;

import org.femtoframework.implement.ImplementUtil;

import static org.junit.Assert.*;

public class MysqlDialectTest {

    @org.junit.Test
    public void getLimitString() {
        RdbmsDialect rdbmsDialect = ImplementUtil.getInstance("mysql", RdbmsDialect.class);
        assertNotNull(rdbmsDialect);
    }
}