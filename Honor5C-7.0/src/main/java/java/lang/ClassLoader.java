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
        public static ClassLoader loader;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ClassLoader.SystemClassLoader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ClassLoader.SystemClassLoader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.lang.ClassLoader.SystemClassLoader.<clinit>():void");
        }

        private SystemClassLoader() {
        }
    }

    private static ClassLoader createSystemClassLoader() {
        return new PathClassLoader(System.getProperty("java.class.path", "."), System.getProperty("java.library.path", ""), BootClassLoader.getInstance());
    }

    private static Void checkCreateClassLoader() {
        return null;
    }

    private ClassLoader(Void unused, ClassLoader parent) {
        this.proxyCache = new HashMap();
        this.packages = new HashMap();
        this.parent = parent;
    }

    protected ClassLoader(ClassLoader parent) {
        this(checkCreateClassLoader(), parent);
    }

    protected ClassLoader() {
        this(checkCreateClassLoader(), getSystemClassLoader());
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        long t0 = System.nanoTime();
        try {
            if (this.parent != null) {
                c = this.parent.loadClass(name, false);
            } else {
                c = findBootstrapClassOrNull(name);
            }
        } catch (ClassNotFoundException e) {
        }
        if (c != null) {
            return c;
        }
        long t1 = System.nanoTime();
        return findClass(name);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    @Deprecated
    protected final Class<?> defineClass(byte[] b, int off, int len) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    protected final Class<?> defineClass(String name, byte[] b, int off, int len) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    protected final Class<?> defineClass(String name, byte[] b, int off, int len, ProtectionDomain protectionDomain) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    protected final Class<?> defineClass(String name, ByteBuffer b, ProtectionDomain protectionDomain) throws ClassFormatError {
        throw new UnsupportedOperationException("can't load this type of class file");
    }

    protected final void resolveClass(Class<?> cls) {
    }

    protected final Class<?> findSystemClass(String name) throws ClassNotFoundException {
        return Class.forName(name, false, getSystemClassLoader());
    }

    private Class findBootstrapClassOrNull(String name) {
        return null;
    }

    protected final Class<?> findLoadedClass(String name) {
        ClassLoader classLoader;
        if (this == BootClassLoader.getInstance()) {
            classLoader = null;
        } else {
            classLoader = this;
        }
        return VMClassLoader.findLoadedClass(classLoader, name);
    }

    protected final void setSigners(Class<?> cls, Object[] signers) {
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
        Enumeration[] tmp = new Enumeration[2];
        if (this.parent != null) {
            tmp[0] = this.parent.getResources(name);
        } else {
            tmp[0] = getBootstrapResources(name);
        }
        tmp[1] = findResources(name);
        return new CompoundEnumeration(tmp);
    }

    protected URL findResource(String name) {
        return null;
    }

    protected Enumeration<URL> findResources(String name) throws IOException {
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
        InputStream inputStream = null;
        URL url = getResource(name);
        if (url != null) {
            try {
                inputStream = url.openStream();
            } catch (IOException e) {
                return inputStream;
            }
        }
        return inputStream;
    }

    public static InputStream getSystemResourceAsStream(String name) {
        InputStream inputStream = null;
        URL url = getSystemResource(name);
        if (url != null) {
            try {
                inputStream = url.openStream();
            } catch (IOException e) {
                return inputStream;
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

    protected Package definePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase) throws IllegalArgumentException {
        Package pkg;
        synchronized (this.packages) {
            if (((Package) this.packages.get(name)) != null) {
                throw new IllegalArgumentException(name);
            }
            pkg = new Package(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase, this);
            this.packages.put(name, pkg);
        }
        return pkg;
    }

    protected Package getPackage(String name) {
        Package pkg;
        synchronized (this.packages) {
            pkg = (Package) this.packages.get(name);
        }
        return pkg;
    }

    protected Package[] getPackages() {
        Map<String, Package> map;
        synchronized (this.packages) {
            map = new HashMap(this.packages);
        }
        return (Package[]) map.values().toArray(new Package[map.size()]);
    }

    protected String findLibrary(String libname) {
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
