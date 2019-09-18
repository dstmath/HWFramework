package java.lang;

import dalvik.system.PathClassLoader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sun.misc.CompoundEnumeration;
import sun.reflect.CallerSensitive;

public abstract class ClassLoader {
    private transient long allocator;
    private transient long classTable;
    private final HashMap<String, Package> packages;
    private final ClassLoader parent;
    public final Map<List<Class<?>>, Class<?>> proxyCache;

    private static class SystemClassLoader {
        public static ClassLoader loader = ClassLoader.createSystemClassLoader();

        private SystemClassLoader() {
        }
    }

    /* access modifiers changed from: private */
    public static ClassLoader createSystemClassLoader() {
        return new PathClassLoader(System.getProperty("java.class.path", "."), System.getProperty("java.library.path", ""), BootClassLoader.getInstance());
    }

    private static Void checkCreateClassLoader() {
        return null;
    }

    private ClassLoader(Void unused, ClassLoader parent2) {
        this.proxyCache = new HashMap();
        this.packages = new HashMap<>();
        this.parent = parent2;
    }

    protected ClassLoader(ClassLoader parent2) {
        this(checkCreateClassLoader(), parent2);
    }

    protected ClassLoader() {
        this(checkCreateClassLoader(), getSystemClassLoader());
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    /* access modifiers changed from: protected */
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        try {
            if (this.parent != null) {
                c = this.parent.loadClass(name, false);
            } else {
                c = findBootstrapClassOrNull(name);
            }
        } catch (ClassNotFoundException e) {
        }
        if (c == null) {
            return findClass(name);
        }
        return c;
    }

    /* access modifiers changed from: protected */
    public Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public final Class<?> defineClass(byte[] b, int off, int len) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    /* access modifiers changed from: protected */
    public final Class<?> defineClass(String name, byte[] b, int off, int len) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    /* access modifiers changed from: protected */
    public final Class<?> defineClass(String name, byte[] b, int off, int len, ProtectionDomain protectionDomain) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    /* access modifiers changed from: protected */
    public final Class<?> defineClass(String name, ByteBuffer b, ProtectionDomain protectionDomain) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    /* access modifiers changed from: protected */
    public final void resolveClass(Class<?> cls) {
    }

    /* access modifiers changed from: protected */
    public final Class<?> findSystemClass(String name) throws ClassNotFoundException {
        return Class.forName(name, false, getSystemClassLoader());
    }

    private Class<?> findBootstrapClassOrNull(String name) {
        return null;
    }

    /* access modifiers changed from: protected */
    public final Class<?> findLoadedClass(String name) {
        ClassLoader loader;
        if (this == BootClassLoader.getInstance()) {
            loader = null;
        } else {
            loader = this;
        }
        return VMClassLoader.findLoadedClass(loader, name);
    }

    /* access modifiers changed from: protected */
    public final void setSigners(Class<?> cls, Object[] signers) {
    }

    public URL getResource(String name) {
        URL url;
        if (this.parent != null) {
            url = this.parent.getResource(name);
        } else {
            url = getBootstrapResource(name);
        }
        if (url == null) {
            return findResource(name);
        }
        return url;
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL>[] tmp = new Enumeration[2];
        if (this.parent != null) {
            tmp[0] = this.parent.getResources(name);
        } else {
            tmp[0] = getBootstrapResources(name);
        }
        tmp[1] = findResources(name);
        return new CompoundEnumeration(tmp);
    }

    /* access modifiers changed from: protected */
    public URL findResource(String name) {
        return null;
    }

    /* access modifiers changed from: protected */
    public Enumeration<URL> findResources(String name) throws IOException {
        return Collections.emptyEnumeration();
    }

    @CallerSensitive
    protected static boolean registerAsParallelCapable() {
        return true;
    }

    public static URL getSystemResource(String name) {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResource(name);
        }
        return system.getResource(name);
    }

    public static Enumeration<URL> getSystemResources(String name) throws IOException {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResources(name);
        }
        return system.getResources(name);
    }

    private static URL getBootstrapResource(String name) {
        return null;
    }

    private static Enumeration<URL> getBootstrapResources(String name) throws IOException {
        return null;
    }

    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        InputStream inputStream = null;
        if (url != null) {
            try {
                inputStream = url.openStream();
            } catch (IOException e) {
                return null;
            }
        }
        return inputStream;
    }

    public static InputStream getSystemResourceAsStream(String name) {
        URL url = getSystemResource(name);
        InputStream inputStream = null;
        if (url != null) {
            try {
                inputStream = url.openStream();
            } catch (IOException e) {
                return null;
            }
        }
        return inputStream;
    }

    @CallerSensitive
    public final ClassLoader getParent() {
        return this.parent;
    }

    @CallerSensitive
    public static ClassLoader getSystemClassLoader() {
        return SystemClassLoader.loader;
    }

    /* access modifiers changed from: protected */
    public Package definePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase) throws IllegalArgumentException {
        Package pkg;
        String str = name;
        synchronized (this.packages) {
            if (this.packages.get(str) == null) {
                Package packageR = new Package(str, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase, this);
                pkg = packageR;
                this.packages.put(str, pkg);
            } else {
                throw new IllegalArgumentException(str);
            }
        }
        return pkg;
    }

    /* access modifiers changed from: protected */
    public Package getPackage(String name) {
        Package pkg;
        synchronized (this.packages) {
            pkg = this.packages.get(name);
        }
        return pkg;
    }

    /* access modifiers changed from: protected */
    public Package[] getPackages() {
        Map<String, Package> map;
        synchronized (this.packages) {
            map = new HashMap<>((Map<? extends String, ? extends Package>) this.packages);
        }
        return (Package[]) map.values().toArray(new Package[map.size()]);
    }

    /* access modifiers changed from: protected */
    public String findLibrary(String libname) {
        return null;
    }

    public void setDefaultAssertionStatus(boolean enabled) {
    }

    public void setPackageAssertionStatus(String packageName, boolean enabled) {
    }

    public void setClassAssertionStatus(String className, boolean enabled) {
    }

    public void clearAssertionStatus() {
    }
}
