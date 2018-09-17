package java.lang;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/* compiled from: ClassLoader */
class BootClassLoader extends ClassLoader {
    private static BootClassLoader instance;

    @FindBugsSuppressWarnings({"DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED"})
    public static synchronized BootClassLoader getInstance() {
        BootClassLoader bootClassLoader;
        synchronized (BootClassLoader.class) {
            if (instance == null) {
                instance = new BootClassLoader();
            }
            bootClassLoader = instance;
        }
        return bootClassLoader;
    }

    public BootClassLoader() {
        super(null);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.classForName(name, false, null);
    }

    protected URL findResource(String name) {
        return VMClassLoader.getResource(name);
    }

    protected Enumeration<URL> findResources(String resName) throws IOException {
        return Collections.enumeration(VMClassLoader.getResources(resName));
    }

    protected Package getPackage(String name) {
        if (name == null || (name.isEmpty() ^ 1) == 0) {
            return null;
        }
        Package pack;
        synchronized (this) {
            pack = super.getPackage(name);
            if (pack == null) {
                pack = definePackage(name, "Unknown", "0.0", "Unknown", "Unknown", "0.0", "Unknown", null);
            }
        }
        return pack;
    }

    public URL getResource(String resName) {
        return findResource(resName);
    }

    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(className);
        if (clazz == null) {
            return findClass(className);
        }
        return clazz;
    }

    public Enumeration<URL> getResources(String resName) throws IOException {
        return findResources(resName);
    }
}
