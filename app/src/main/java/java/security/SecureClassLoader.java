package java.security;

import java.nio.ByteBuffer;
import java.util.HashMap;
import sun.security.util.Debug;

public class SecureClassLoader extends ClassLoader {
    private static final Debug debug = null;
    private final boolean initialized;
    private final HashMap<CodeSource, ProtectionDomain> pdcache;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.SecureClassLoader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.SecureClassLoader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.security.SecureClassLoader.<clinit>():void");
    }

    protected SecureClassLoader(ClassLoader parent) {
        super(parent);
        this.pdcache = new HashMap(11);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.initialized = true;
    }

    protected SecureClassLoader() {
        this.pdcache = new HashMap(11);
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
                return pd;
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
