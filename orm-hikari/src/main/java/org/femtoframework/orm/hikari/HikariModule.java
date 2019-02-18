package org.femtoframework.orm.hikari;

import com.zaxxer.hikari.HikariDataSource;
import org.femtoframework.bean.BeanPhase;
import org.femtoframework.bean.LifecycleMBean;

/**
 * Hikari Module
 */
public class HikariModule extends HikariDataSource implements LifecycleMBean {

    private BeanPhase phase = BeanPhase.DISABLED;

    /**
     * Implement method of getPhase
     *
     * @return BeanPhase
     */
    @Override
    public BeanPhase _doGetPhase() {
        return phase;
    }

    /**
     * Phase setter for internal
     *
     * @param phase BeanPhase
     */
    @Override
    public void _doSetPhase(BeanPhase phase) {
        this.phase = phase;
    }

    /**
     * Initiliaze internally
     */
    public void _doInitialize() {
        super.validate();
    }

    public void _doDestroy() {
        this.close();
    }
}
