package sun.security.jca;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import sun.security.util.Debug;
import sun.security.util.PropertyExpander;

final class ProviderConfig {
    private static final Class[] CL_STRING = {String.class};
    private static final int MAX_LOAD_TRIES = 30;
    private static final String P11_SOL_ARG = "${java.home}/lib/security/sunpkcs11-solaris.cfg";
    private static final String P11_SOL_NAME = "sun.security.pkcs11.SunPKCS11";
    /* access modifiers changed from: private */
    public static final Debug debug = Debug.getInstance("jca", "ProviderConfig");
    private final String argument;
    /* access modifiers changed from: private */
    public final String className;
    private boolean isLoading;
    private volatile Provider provider;
    private int tries;

    ProviderConfig(String className2, String argument2) {
        if (className2.equals(P11_SOL_NAME) && argument2.equals(P11_SOL_ARG)) {
            checkSunPKCS11Solaris();
        }
        this.className = className2;
        this.argument = expand(argument2);
    }

    ProviderConfig(String className2) {
        this(className2, "");
    }

    ProviderConfig(Provider provider2) {
        this.className = provider2.getClass().getName();
        this.argument = "";
        this.provider = provider2;
    }

    private void checkSunPKCS11Solaris() {
        if (((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                if (!new File("/usr/lib/libpkcs11.so").exists()) {
                    return Boolean.FALSE;
                }
                if ("false".equalsIgnoreCase(System.getProperty("sun.security.pkcs11.enable-solaris"))) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        })) == Boolean.FALSE) {
            this.tries = 30;
        }
    }

    private boolean hasArgument() {
        return this.argument.length() != 0;
    }

    private boolean shouldLoad() {
        return this.tries < 30;
    }

    /* access modifiers changed from: private */
    public void disableLoad() {
        this.tries = 30;
    }

    /* access modifiers changed from: package-private */
    public boolean isLoaded() {
        return this.provider != null;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProviderConfig)) {
            return false;
        }
        ProviderConfig other = (ProviderConfig) obj;
        if (!this.className.equals(other.className) || !this.argument.equals(other.argument)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.className.hashCode() + this.argument.hashCode();
    }

    public String toString() {
        if (!hasArgument()) {
            return this.className;
        }
        return this.className + "('" + this.argument + "')";
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0039, code lost:
        return null;
     */
    public synchronized Provider getProvider() {
        Provider p = this.provider;
        if (p != null) {
            return p;
        }
        if (!shouldLoad()) {
            return null;
        }
        if (!this.isLoading) {
            try {
                this.isLoading = true;
                this.tries++;
                Provider p2 = doLoadProvider();
                this.isLoading = false;
                this.provider = p2;
                return p2;
            } catch (Throwable th) {
                this.isLoading = false;
                throw th;
            }
        } else if (debug != null) {
            debug.println("Recursion loading provider: " + this);
            new Exception("Call trace").printStackTrace();
        }
    }

    private Provider doLoadProvider() {
        return (Provider) AccessController.doPrivileged(new PrivilegedAction<Provider>() {
            public Provider run() {
                Throwable t;
                if (ProviderConfig.debug != null) {
                    Debug access$000 = ProviderConfig.debug;
                    access$000.println("Loading provider: " + ProviderConfig.this);
                }
                try {
                    return ProviderConfig.this.initProvider(ProviderConfig.this.className, Object.class.getClassLoader());
                } catch (Exception e) {
                    try {
                        return ProviderConfig.this.initProvider(ProviderConfig.this.className, ClassLoader.getSystemClassLoader());
                    } catch (Exception e2) {
                        if (e2 instanceof InvocationTargetException) {
                            t = ((InvocationTargetException) e2).getCause();
                        } else {
                            t = e2;
                        }
                        if (ProviderConfig.debug != null) {
                            Debug access$0002 = ProviderConfig.debug;
                            access$0002.println("Error loading provider " + ProviderConfig.this);
                            t.printStackTrace();
                        }
                        if (!(t instanceof ProviderException)) {
                            if (t instanceof UnsupportedOperationException) {
                                ProviderConfig.this.disableLoad();
                            }
                            return null;
                        }
                        throw ((ProviderException) t);
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public Provider initProvider(String className2, ClassLoader cl) throws Exception {
        Class<?> provClass;
        Object obj;
        if (cl != null) {
            provClass = cl.loadClass(className2);
        } else {
            provClass = Class.forName(className2);
        }
        if (!hasArgument()) {
            obj = provClass.newInstance();
        } else {
            obj = provClass.getConstructor(CL_STRING).newInstance(this.argument);
        }
        if (obj instanceof Provider) {
            if (debug != null) {
                Debug debug2 = debug;
                debug2.println("Loaded provider " + obj);
            }
            return (Provider) obj;
        }
        if (debug != null) {
            Debug debug3 = debug;
            debug3.println(className2 + " is not a provider");
        }
        disableLoad();
        return null;
    }

    private static String expand(final String value) {
        if (!value.contains("${")) {
            return value;
        }
        return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                try {
                    return PropertyExpander.expand(String.this);
                } catch (GeneralSecurityException e) {
                    throw new ProviderException((Throwable) e);
                }
            }
        });
    }
}
