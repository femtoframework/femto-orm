package org.femtoframework.orm.hikari;

import com.zaxxer.hikari.HikariDataSource;
import org.femtoframework.bean.info.BeanInfo;
import org.femtoframework.bean.info.BeanInfoUtil;
import org.femtoframework.coin.CoinModule;
import org.femtoframework.coin.CoinUtil;
import org.femtoframework.coin.DefaultComponentFactory;
import org.femtoframework.coin.Namespace;
import org.femtoframework.orm.*;
import org.femtoframework.orm.domain.Device;
import org.femtoframework.util.nutlet.NutletUtil;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HikariDataSourceTest {

    @Test
    public void testDataSource() {
        BeanInfo beanInfo = BeanInfoUtil.getBeanInfo(HikariDataSource.class);
        System.out.println(beanInfo);
    }


    @Test
    public void testCoin() throws Exception {
        File file = NutletUtil.getResourceAsFile("hikari.yaml");
        CoinModule module = CoinUtil.newModule();
        module.getController().create(file);

        Namespace ns = module.getNamespaceFactory().get("orm");
        assertNotNull(ns);

        Object bean = ns.getBeanFactory().get("derby");
        assertNotNull(bean);

        HikariCP cp = (HikariCP)bean;
        assertTrue(cp.isRunning());

        try (Connection conn = cp.getConnection()) {
            Statement statement = conn.createStatement();
            try {
                statement.execute("DROP SEQUENCE device_id_seq RESTRICT");
                statement.execute("DROP TABLE device");
            }
            catch(Exception ex) {
                //Ignore
            }
            statement.execute("CREATE SEQUENCE device_id_seq AS INT START WITH 1");
            statement.execute("CREATE TABLE device (id INT NOT NULL, product_no VARCHAR(15) NOT NULL,\n" +
                    "\tmodel VARCHAR(10) DEFAULT 'AI-600', uuid VARCHAR(15))");

            statement.close();
        }

        RepositoryModule repositoryModule = RepositoryUtil.getModule();
        RepositoryFactory repositoryFactory = repositoryModule.getRepositoryFactory(cp);
        Repository<Device> deviceRepository = repositoryFactory.getRepository(Device.class);

        Device device = new Device();
        device.setModel("AI-600");
        device.setProductNo("600123456789");
        device.setUuid("ABCDEF");
        assertTrue(deviceRepository.create(device));

        assertTrue(device.getId() != 0);
        Device d = deviceRepository.getById(device.getId());
        assertNotNull(d);
        assertEquals(d.getModel(), device.getModel());
        assertEquals(d.getProductNo(), device.getProductNo());
        assertEquals(d.getUuid(), device.getUuid());
    }
}
