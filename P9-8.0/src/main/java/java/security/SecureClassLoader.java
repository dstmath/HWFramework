package java.security;

import java.nio.ByteBuffer;
import java.util.HashMap;
import sun.security.util.Debug;

public class SecureClassLoader extends ClassLoader {
    private static final Debug debug = Debug.getInstance("scl");
    private final boolean initialized;
    private final HashMap<CodeSource, ProtectionDomain> pdcache = new HashMap(11);

    static {
        ClassLoader.registerAsParallelCapable();
    }

    protected SecureClassLoader(ClassLoader parent) {
        super(parent);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.initialized = true;
    }

    protected SecureClassLoader() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.initialized = true;
    }

    protected final Class<?> defineClass(String name, byte[] b, int off, int len, CodeSource cs) {
        return defineClass(name, b, off, len, getProtectionDomain(cs));
    }

    protected final Class<?> defineClass(String name, ByteBuffer b, CodeSource cs) {
        return defineClass(name, b, getProtectionDomain(cs));
    }

    protected PermissionCollection getPermissions(CodeSource codesource) {
        check();
        return new Permissions();
    }

    /* JADX WARNING: Missing block: B:15:0x004a, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ProtectionDomain getProtectionDomain(CodeSource cs) {
        Throwable th;
        if (cs == null) {
            return null;
        }
        synchronized (this.pdcache) {
            try {
                ProtectionDomain pd = (ProtectionDomain) this.pdcache.get(cs);
                if (pd == null) {
                    ProtectionDomain pd2 = new ProtectionDomain(cs, getPermissions(cs), this, null);
                    try {
                        this.pdcache.put(cs, pd2);
                        if (debug != null) {
                            debug.println(" getPermissions " + pd2);
                            debug.println("");
                            pd = pd2;
                        } else {
                            pd = pd2;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        pd = pd2;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private void check() {
        if (!this.initialized) {
            throw new SecurityException("ClassLoader object not initialized");
        }
    }
}
