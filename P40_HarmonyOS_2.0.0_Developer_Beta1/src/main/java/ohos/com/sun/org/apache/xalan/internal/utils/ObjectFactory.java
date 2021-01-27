package ohos.com.sun.org.apache.xalan.internal.utils;

public class ObjectFactory {
    private static final boolean DEBUG = false;
    private static final String JAXP_INTERNAL = "com.sun.org.apache";
    private static final String STAX_INTERNAL = "com.sun.xml.internal";

    private static void debugPrintln(String str) {
    }

    public static ClassLoader findClassLoader() {
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

    static Object newInstance(String str, ClassLoader classLoader, boolean z) throws ConfigurationError {
        try {
            return findProviderClass(str, classLoader, z).newInstance();
        } catch (ClassNotFoundException e) {
            throw new ConfigurationError("Provider " + str + " not found", e);
        } catch (Exception e2) {
            throw new ConfigurationError("Provider " + str + " could not be instantiated: " + e2, e2);
        }
    }

    public static Class<?> findProviderClass(String str, boolean z) throws ClassNotFoundException, ConfigurationError {
        return findProviderClass(str, findClassLoader(), z);
    }

    private static Class<?> findProviderClass(String str, ClassLoader classLoader, boolean z) throws ClassNotFoundException, ConfigurationError {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            try {
                if (!str.startsWith(JAXP_INTERNAL)) {
                    if (!str.startsWith(STAX_INTERNAL)) {
                        int lastIndexOf = str.lastIndexOf(".");
                        securityManager.checkPackageAccess(lastIndexOf != -1 ? str.substring(0, lastIndexOf) : str);
                    }
                }
                classLoader = null;
            } catch (SecurityException e) {
                throw e;
            }
        }
        if (classLoader == null) {
            return Class.forName(str, false, ObjectFactory.class.getClassLoader());
        }
        try {
            return classLoader.loadClass(str);
        } catch (ClassNotFoundException e2) {
            if (z) {
                ClassLoader classLoader2 = ObjectFactory.class.getClassLoader();
                if (classLoader2 == null) {
                    return Class.forName(str);
                }
                if (classLoader != classLoader2) {
                    return classLoader2.loadClass(str);
                }
                throw e2;
            }
            throw e2;
        }
    }
}
