package sun.security.jca;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import java.util.jar.Pack200.Unpacker;
import sun.security.util.Debug;
import sun.security.util.PropertyExpander;

final class ProviderConfig {
    private static final Class[] CL_STRING = null;
    private static final int MAX_LOAD_TRIES = 30;
    private static final String P11_SOL_ARG = "${java.home}/lib/security/sunpkcs11-solaris.cfg";
    private static final String P11_SOL_NAME = "sun.security.pkcs11.SunPKCS11";
    private static final Debug debug = null;
    private final String argument;
    private final String className;
    private boolean isLoading;
    private volatile Provider provider;
    private int tries;

    /* renamed from: sun.security.jca.ProviderConfig.3 */
    static class AnonymousClass3 implements PrivilegedAction<String> {
        final /* synthetic */ String val$value;

        AnonymousClass3(String val$value) {
            this.val$value = val$value;
        }

        public String run() {
            try {
                return PropertyExpander.expand(this.val$value);
            } catch (Throwable e) {
                throw new ProviderException(e);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.jca.ProviderConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.jca.ProviderConfig.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.jca.ProviderConfig.<clinit>():void");
    }

    ProviderConfig(String className, String argument) {
        if (className.equals(P11_SOL_NAME) && argument.equals(P11_SOL_ARG)) {
            checkSunPKCS11Solaris();
        }
        this.className = className;
        this.argument = expand(argument);
    }

    ProviderConfig(String className) {
        this(className, "");
    }

    ProviderConfig(Provider provider) {
        this.className = provider.getClass().getName();
        this.argument = "";
        this.provider = provider;
    }

    private void checkSunPKCS11Solaris() {
        if (((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                if (!new File("/usr/lib/libpkcs11.so").exists()) {
                    return Boolean.FALSE;
                }
                if (Unpacker.FALSE.equalsIgnoreCase(System.getProperty("sun.security.pkcs11.enable-solaris"))) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        })) == Boolean.FALSE) {
            this.tries = MAX_LOAD_TRIES;
        }
    }

    private boolean hasArgument() {
        return this.argument.length() != 0;
    }

    private boolean shouldLoad() {
        return this.tries < MAX_LOAD_TRIES;
    }

    private void disableLoad() {
        this.tries = MAX_LOAD_TRIES;
    }

    boolean isLoaded() {
        return this.provider != null;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProviderConfig)) {
            return false;
        }
        ProviderConfig other = (ProviderConfig) obj;
        if (this.className.equals(other.className)) {
            z = this.argument.equals(other.argument);
        }
        return z;
    }

    public int hashCode() {
        return this.className.hashCode() + this.argument.hashCode();
    }

    public String toString() {
        if (hasArgument()) {
            return this.className + "('" + this.argument + "')";
        }
        return this.className;
    }

    synchronized Provider getProvider() {
        Provider p = this.provider;
        if (p != null) {
            return p;
        }
        if (!shouldLoad()) {
            return null;
        }
        if (this.isLoading) {
            if (debug != null) {
                debug.println("Recursion loading provider: " + this);
                new Exception("Call trace").printStackTrace();
            }
            return null;
        }
        try {
            this.isLoading = true;
            this.tries++;
            p = doLoadProvider();
            this.isLoading = false;
            this.provider = p;
            return p;
        } catch (Throwable th) {
            this.isLoading = false;
        }
    }

    private Provider doLoadProvider() {
        return (Provider) AccessController.doPrivileged(new PrivilegedAction<Provider>() {
            public Provider run() {
                Throwable t;
                if (ProviderConfig.debug != null) {
                    ProviderConfig.debug.println("Loading provider: " + ProviderConfig.this);
                }
                try {
                    return ProviderConfig.this.initProvider(ProviderConfig.this.className, Object.class.getClassLoader());
                } catch (Exception e) {
                    try {
                        return ProviderConfig.this.initProvider(ProviderConfig.this.className, ClassLoader.getSystemClassLoader());
                    } catch (Throwable e2) {
                        if (e2 instanceof InvocationTargetException) {
                            t = ((InvocationTargetException) e2).getCause();
                        } else {
                            t = e2;
                        }
                        if (ProviderConfig.debug != null) {
                            ProviderConfig.debug.println("Error loading provider " + ProviderConfig.this);
                            t.printStackTrace();
                        }
                        if (t instanceof ProviderException) {
                            throw ((ProviderException) t);
                        }
                        if (t instanceof UnsupportedOperationException) {
                            ProviderConfig.this.disableLoad();
                        }
                        return null;
                    }
                }
            }
        });
    }

    private Provider initProvider(String className, ClassLoader cl) throws Exception {
        Class<?> provClass;
        Object obj;
        if (cl != null) {
            provClass = cl.loadClass(className);
        } else {
            provClass = Class.forName(className);
        }
        if (hasArgument()) {
            obj = provClass.getConstructor(CL_STRING).newInstance(this.argument);
        } else {
            obj = provClass.newInstance();
        }
        if (obj instanceof Provider) {
            if (debug != null) {
                debug.println("Loaded provider " + obj);
            }
            return (Provider) obj;
        }
        if (debug != null) {
            debug.println(className + " is not a provider");
        }
        disableLoad();
        return null;
    }

    private static String expand(String value) {
        if (value.contains("${")) {
            return (String) AccessController.doPrivileged(new AnonymousClass3(value));
        }
        return value;
    }
}
