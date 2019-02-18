package org.femtoframework.orm.hikari;

import com.zaxxer.hikari.HikariDataSource;
import org.femtoframework.bean.info.BeanInfo;
import org.femtoframework.bean.info.BeanInfoUtil;
import org.junit.Test;

public class HikariDataSourceTest {

    @Test
    public void testDataSource() {
        BeanInfo beanInfo = BeanInfoUtil.getBeanInfo(HikariDataSource.class);
        System.out.println(beanInfo);
    }
}
