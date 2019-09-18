package java.security;

import java.nio.ByteBuffer;
import java.util.HashMap;
import sun.security.util.Debug;

public class SecureClassLoader extends ClassLoader {
    private static final Debug debug = Debug.getInstance("scl");
    private final boolean initialized;
    private final HashMap<CodeSource, ProtectionDomain> pdcache = new HashMap<>(11);

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

    /* access modifiers changed from: protected */
    public final Class<?> defineClass(String name, byte[] b, int off, int len, CodeSource cs) {
        return defineClass(name, b, off, len, getProtectionDomain(cs));
    }

    /* access modifiers changed from: protected */
    public final Class<?> defineClass(String name, ByteBuffer b, CodeSource cs) {
        return defineClass(name, b, getProtectionDomain(cs));
    }

    /* access modifiers changed from: protected */
    public PermissionCollection getPermissions(CodeSource codesource) {
        check();
        return new Permissions();
    }

    private ProtectionDomain getProtectionDomain(CodeSource cs) {
        ProtectionDomain pd;
        if (cs == null) {
            return null;
        }
        synchronized (this.pdcache) {
            pd = this.pdcache.get(cs);
            if (pd == null) {
                pd = new ProtectionDomain(cs, getPermissions(cs), this, null);
                this.pdcache.put(cs, pd);
                if (debug != null) {
                    Debug debug2 = debug;
                    debug2.println(" getPermissions " + pd);
                    debug.println("");
                }
            }
        }
        return pd;
    }

    private void check() {
        if (!this.initialized) {
            throw new SecurityException("ClassLoader object not initialized");
        }
    }
}
