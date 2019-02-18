package org.femtoframework.orm;

import org.femtoframework.implement.ImplementUtil;

public class RepositoryUtil {

    private static RepositoryModule module = null;

    public static RepositoryModule getModule() {
        if (module == null) {
            module = ImplementUtil.getInstance(RepositoryModule.class);
        }
        return module;
    }
}
