package android.icu.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class ClassLoaderUtil {
    private static volatile ClassLoader BOOTSTRAP_CLASSLOADER;

    private static class BootstrapClassLoader extends ClassLoader {
        BootstrapClassLoader() {
            super(Object.class.getClassLoader());
        }
    }

    private static ClassLoader getBootstrapClassLoader() {
        if (BOOTSTRAP_CLASSLOADER == null) {
            synchronized (ClassLoaderUtil.class) {
                if (BOOTSTRAP_CLASSLOADER == null) {
                    ClassLoader cl;
                    if (System.getSecurityManager() != null) {
                        cl = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                            public BootstrapClassLoader run() {
                                return new BootstrapClassLoader();
                            }
                        });
                    } else {
                        cl = new BootstrapClassLoader();
                    }
                    BOOTSTRAP_CLASSLOADER = cl;
                }
            }
        }
        return BOOTSTRAP_CLASSLOADER;
    }

    public static ClassLoader getClassLoader(Class<?> cls) {
        ClassLoader cl = cls.getClassLoader();
        if (cl == null) {
            return getClassLoader();
        }
        return cl;
    }

    public static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            return cl;
        }
        cl = ClassLoader.getSystemClassLoader();
        if (cl == null) {
            return getBootstrapClassLoader();
        }
        return cl;
    }
}
