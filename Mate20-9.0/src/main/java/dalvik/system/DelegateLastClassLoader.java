package dalvik.system;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import sun.misc.CompoundEnumeration;

public final class DelegateLastClassLoader extends PathClassLoader {
    public DelegateLastClassLoader(String dexPath, ClassLoader parent) {
        super(dexPath, parent);
    }

    public DelegateLastClassLoader(String dexPath, String librarySearchPath, ClassLoader parent) {
        super(dexPath, librarySearchPath, parent);
    }

    /* access modifiers changed from: protected */
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> cl = findLoadedClass(name);
        if (cl != null) {
            return cl;
        }
        try {
            return Object.class.getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            try {
                return findClass(name);
            } catch (ClassNotFoundException ex) {
                try {
                    return getParent().loadClass(name);
                } catch (ClassNotFoundException e2) {
                    throw ex;
                }
            }
        }
    }

    public URL getResource(String name) {
        URL resource = Object.class.getClassLoader().getResource(name);
        if (resource != null) {
            return resource;
        }
        URL resource2 = findResource(name);
        if (resource2 != null) {
            return resource2;
        }
        ClassLoader cl = getParent();
        return cl == null ? null : cl.getResource(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL>[] resources = new Enumeration[3];
        resources[0] = Object.class.getClassLoader().getResources(name);
        resources[1] = findResources(name);
        resources[2] = getParent() == null ? null : getParent().getResources(name);
        return new CompoundEnumeration(resources);
    }
}
