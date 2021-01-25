package ohos.javax.xml.datatype;

import java.io.File;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

class FactoryFinder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String DEFAULT_PACKAGE = "com.sun.org.apache.xerces.internal";
    private static final Properties cacheProps = new Properties();
    private static boolean debug = false;
    private static volatile boolean firstTime = true;
    private static final SecuritySupport ss = new SecuritySupport();

    FactoryFinder() {
    }

    static {
        boolean z = true;
        try {
            String systemProperty = ss.getSystemProperty("jaxp.debug");
            if (systemProperty == null || "false".equals(systemProperty)) {
                z = false;
            }
            debug = z;
        } catch (SecurityException unused) {
            debug = false;
        }
    }

    private static void dPrint(String str) {
        if (debug) {
            PrintStream printStream = System.err;
            printStream.println("JAXP: " + str);
        }
    }

    private static Class<?> getProviderClass(String str, ClassLoader classLoader, boolean z, boolean z2) throws ClassNotFoundException {
        if (classLoader != null) {
            return Class.forName(str, false, classLoader);
        }
        if (z2) {
            try {
                return Class.forName(str, false, FactoryFinder.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                if (z) {
                    return Class.forName(str, false, FactoryFinder.class.getClassLoader());
                }
                throw e;
            }
        } else {
            ClassLoader contextClassLoader = ss.getContextClassLoader();
            if (contextClassLoader != null) {
                return Class.forName(str, false, contextClassLoader);
            }
            throw new ClassNotFoundException();
        }
    }

    static <T> T newInstance(Class<T> cls, String str, ClassLoader classLoader, boolean z) throws DatatypeConfigurationException {
        return (T) newInstance(cls, str, classLoader, z, false);
    }

    static <T> T newInstance(Class<T> cls, String str, ClassLoader classLoader, boolean z, boolean z2) throws DatatypeConfigurationException {
        if (!(System.getSecurityManager() == null || str == null || !str.startsWith(DEFAULT_PACKAGE))) {
            classLoader = null;
            z2 = true;
        }
        try {
            Class<?> providerClass = getProviderClass(str, classLoader, z, z2);
            if (cls.isAssignableFrom(providerClass)) {
                Object newInstance = providerClass.newInstance();
                if (debug) {
                    dPrint("created new instance of " + providerClass + " using ClassLoader: " + classLoader);
                }
                return cls.cast(newInstance);
            }
            throw new ClassCastException(str + " cannot be cast to " + cls.getName());
        } catch (ClassNotFoundException e) {
            throw new DatatypeConfigurationException("Provider " + str + " not found", e);
        } catch (Exception e2) {
            throw new DatatypeConfigurationException("Provider " + str + " could not be instantiated: " + e2, e2);
        }
    }

    static <T> T find(Class<T> cls, String str) throws DatatypeConfigurationException {
        String name = cls.getName();
        dPrint("find factoryId =" + name);
        try {
            String systemProperty = ss.getSystemProperty(name);
            if (systemProperty != null) {
                dPrint("found system property, value=" + systemProperty);
                return (T) newInstance(cls, systemProperty, null, true);
            }
        } catch (SecurityException e) {
            if (debug) {
                e.printStackTrace();
            }
        }
        try {
            if (firstTime) {
                synchronized (cacheProps) {
                    if (firstTime) {
                        File file = new File(ss.getSystemProperty("java.home") + File.separator + "lib" + File.separator + "jaxp.properties");
                        firstTime = false;
                        if (ss.doesFileExist(file)) {
                            dPrint("Read properties file " + file);
                            cacheProps.load(ss.getFileInputStream(file));
                        }
                    }
                }
            }
            String property = cacheProps.getProperty(name);
            if (property != null) {
                dPrint("found in $java.home/jaxp.properties, value=" + property);
                return (T) newInstance(cls, property, null, true);
            }
        } catch (Exception e2) {
            if (debug) {
                e2.printStackTrace();
            }
        }
        T t = (T) findServiceProvider(cls);
        if (t != null) {
            return t;
        }
        if (str != null) {
            dPrint("loaded from fallback value: " + str);
            return (T) newInstance(cls, str, null, true);
        }
        throw new DatatypeConfigurationException("Provider for " + name + " cannot be found");
    }

    private static <T> T findServiceProvider(final Class<T> cls) throws DatatypeConfigurationException {
        try {
            return (T) AccessController.doPrivileged(new PrivilegedAction<T>() {
                /* class ohos.javax.xml.datatype.FactoryFinder.AnonymousClass1 */

                @Override // java.security.PrivilegedAction
                public T run() {
                    Iterator it = ServiceLoader.load(cls).iterator();
                    if (it.hasNext()) {
                        return (T) it.next();
                    }
                    return null;
                }
            });
        } catch (ServiceConfigurationError e) {
            throw new DatatypeConfigurationException("Provider for " + cls + " cannot be found", e);
        }
    }
}
