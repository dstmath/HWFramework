package ohos.javax.xml.stream;

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
    private static final String DEFAULT_PACKAGE = "com.sun.xml.internal.";
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

    private static Class getProviderClass(String str, ClassLoader classLoader, boolean z, boolean z2) throws ClassNotFoundException {
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

    static <T> T newInstance(Class<T> cls, String str, ClassLoader classLoader, boolean z) throws FactoryConfigurationError {
        return (T) newInstance(cls, str, classLoader, z, false);
    }

    static <T> T newInstance(Class<T> cls, String str, ClassLoader classLoader, boolean z, boolean z2) throws FactoryConfigurationError {
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
            throw new FactoryConfigurationError("Provider " + str + " not found", e);
        } catch (Exception e2) {
            throw new FactoryConfigurationError("Provider " + str + " could not be instantiated: " + e2, e2);
        }
    }

    static <T> T find(Class<T> cls, String str) throws FactoryConfigurationError {
        return (T) find(cls, cls.getName(), null, str);
    }

    static <T> T find(Class<T> cls, String str, ClassLoader classLoader, String str2) throws FactoryConfigurationError {
        String str3;
        T t;
        String str4;
        dPrint("find factoryId =" + str);
        try {
            if (cls.getName().equals(str)) {
                str3 = ss.getSystemProperty(str);
            } else {
                str3 = System.getProperty(str);
            }
            if (str3 != null) {
                dPrint("found system property, value=" + str3);
                return (T) newInstance(cls, str3, classLoader, true);
            }
            try {
                if (firstTime) {
                    synchronized (cacheProps) {
                        if (firstTime) {
                            str4 = ss.getSystemProperty("java.home") + File.separator + "lib" + File.separator + "stax.properties";
                            File file = new File(str4);
                            firstTime = false;
                            if (ss.doesFileExist(file)) {
                                dPrint("Read properties file " + file);
                                cacheProps.load(ss.getFileInputStream(file));
                            } else {
                                str4 = ss.getSystemProperty("java.home") + File.separator + "lib" + File.separator + "jaxp.properties";
                                File file2 = new File(str4);
                                if (ss.doesFileExist(file2)) {
                                    dPrint("Read properties file " + file2);
                                    cacheProps.load(ss.getFileInputStream(file2));
                                }
                            }
                        } else {
                            str4 = null;
                        }
                    }
                } else {
                    str4 = null;
                }
                String property = cacheProps.getProperty(str);
                if (property != null) {
                    dPrint("found in " + str4 + " value=" + property);
                    return (T) newInstance(cls, property, classLoader, true);
                }
            } catch (Exception e) {
                if (debug) {
                    e.printStackTrace();
                }
            }
            if (cls.getName().equals(str) && (t = (T) findServiceProvider(cls, classLoader)) != null) {
                return t;
            }
            if (str2 != null) {
                dPrint("loaded from fallback value: " + str2);
                return (T) newInstance(cls, str2, classLoader, true);
            }
            throw new FactoryConfigurationError("Provider for " + str + " cannot be found", (Exception) null);
        } catch (SecurityException e2) {
            throw new FactoryConfigurationError("Failed to read factoryId '" + str + "'", e2);
        }
    }

    private static <T> T findServiceProvider(final Class<T> cls, final ClassLoader classLoader) {
        try {
            return (T) AccessController.doPrivileged(new PrivilegedAction<T>() {
                /* class ohos.javax.xml.stream.FactoryFinder.AnonymousClass1 */

                @Override // java.security.PrivilegedAction
                public T run() {
                    ServiceLoader serviceLoader;
                    ClassLoader classLoader = classLoader;
                    if (classLoader == null) {
                        serviceLoader = ServiceLoader.load(cls);
                    } else {
                        serviceLoader = ServiceLoader.load(cls, classLoader);
                    }
                    Iterator it = serviceLoader.iterator();
                    if (it.hasNext()) {
                        return (T) it.next();
                    }
                    return null;
                }
            });
        } catch (ServiceConfigurationError e) {
            RuntimeException runtimeException = new RuntimeException("Provider for " + cls + " cannot be created", e);
            throw new FactoryConfigurationError(runtimeException, runtimeException.getMessage());
        }
    }
}
