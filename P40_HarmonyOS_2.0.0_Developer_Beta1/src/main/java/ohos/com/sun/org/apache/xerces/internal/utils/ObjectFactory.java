package ohos.com.sun.org.apache.xerces.internal.utils;

import java.io.PrintStream;

public final class ObjectFactory {
    private static final boolean DEBUG = isDebugEnabled();
    private static final String JAXP_INTERNAL = "com.sun.org.apache";
    private static final String STAX_INTERNAL = "com.sun.xml.internal";

    private static boolean isDebugEnabled() {
        try {
            String systemProperty = SecuritySupport.getSystemProperty("xerces.debug");
            if (systemProperty == null || "false".equals(systemProperty)) {
                return false;
            }
            return true;
        } catch (SecurityException unused) {
            return false;
        }
    }

    private static void debugPrintln(String str) {
        if (DEBUG) {
            PrintStream printStream = System.err;
            printStream.println("XERCES: " + str);
        }
    }

    public static ClassLoader findClassLoader() throws ConfigurationError {
        if (System.getSecurityManager() != null) {
            return null;
        }
        ClassLoader contextClassLoader = SecuritySupport.getContextClassLoader();
        ClassLoader systemClassLoader = SecuritySupport.getSystemClassLoader();
        for (ClassLoader classLoader = systemClassLoader; contextClassLoader != classLoader; classLoader = SecuritySupport.getParentClassLoader(classLoader)) {
            if (classLoader == null) {
                return contextClassLoader;
            }
        }
        ClassLoader classLoader2 = ObjectFactory.class.getClassLoader();
        for (ClassLoader classLoader3 = systemClassLoader; classLoader2 != classLoader3; classLoader3 = SecuritySupport.getParentClassLoader(classLoader3)) {
            if (classLoader3 == null) {
                return classLoader2;
            }
        }
        return systemClassLoader;
    }

    public static Object newInstance(String str, boolean z) throws ConfigurationError {
        if (System.getSecurityManager() != null) {
            return newInstance(str, null, z);
        }
        return newInstance(str, findClassLoader(), z);
    }

    public static Object newInstance(String str, ClassLoader classLoader, boolean z) throws ConfigurationError {
        try {
            Class findProviderClass = findProviderClass(str, classLoader, z);
            Object newInstance = findProviderClass.newInstance();
            if (DEBUG) {
                debugPrintln("created new instance of " + findProviderClass + " using ClassLoader: " + classLoader);
            }
            return newInstance;
        } catch (ClassNotFoundException e) {
            throw new ConfigurationError("Provider " + str + " not found", e);
        } catch (Exception e2) {
            throw new ConfigurationError("Provider " + str + " could not be instantiated: " + e2, e2);
        }
    }

    public static Class findProviderClass(String str, boolean z) throws ClassNotFoundException, ConfigurationError {
        return findProviderClass(str, findClassLoader(), z);
    }

    public static Class findProviderClass(String str, ClassLoader classLoader, boolean z) throws ClassNotFoundException, ConfigurationError {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            if (str.startsWith(JAXP_INTERNAL) || str.startsWith(STAX_INTERNAL)) {
                classLoader = null;
            } else {
                int lastIndexOf = str.lastIndexOf(".");
                securityManager.checkPackageAccess(lastIndexOf != -1 ? str.substring(0, lastIndexOf) : str);
            }
        }
        if (classLoader == null) {
            return Class.forName(str, false, ObjectFactory.class.getClassLoader());
        }
        try {
            return classLoader.loadClass(str);
        } catch (ClassNotFoundException e) {
            if (z) {
                ClassLoader classLoader2 = ObjectFactory.class.getClassLoader();
                if (classLoader2 == null) {
                    return Class.forName(str);
                }
                if (classLoader != classLoader2) {
                    return classLoader2.loadClass(str);
                }
                throw e;
            }
            throw e;
        }
    }
}
