package java.net;

import java.security.AccessControlContext;

/* compiled from: URLClassLoader */
final class FactoryURLClassLoader extends URLClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    FactoryURLClassLoader(URL[] urls, ClassLoader parent, AccessControlContext acc) {
        super(urls, parent, acc);
    }

    FactoryURLClassLoader(URL[] urls, AccessControlContext acc) {
        super(urls, acc);
    }

    public final Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            int i = name.lastIndexOf(46);
            if (i != -1) {
                sm.checkPackageAccess(name.substring(0, i));
            }
        }
        return super.loadClass(name, resolve);
    }
}
