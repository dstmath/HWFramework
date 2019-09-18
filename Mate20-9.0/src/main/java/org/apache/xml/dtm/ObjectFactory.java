package org.apache.xml.dtm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import org.apache.xalan.templates.Constants;

class ObjectFactory {
    private static final boolean DEBUG = false;
    private static final String DEFAULT_PROPERTIES_FILENAME = "xalan.properties";
    private static final String SERVICES_PATH = "META-INF/services/";
    private static long fLastModified = -1;
    private static Properties fXalanProperties = null;

    static class ConfigurationError extends Error {
        static final long serialVersionUID = 5122054096615067992L;
        private Exception exception;

        ConfigurationError(String msg, Exception x) {
            super(msg);
            this.exception = x;
        }

        /* access modifiers changed from: package-private */
        public Exception getException() {
            return this.exception;
        }
    }

    ObjectFactory() {
    }

    static Object createObject(String factoryId, String fallbackClassName) throws ConfigurationError {
        return createObject(factoryId, null, fallbackClassName);
    }

    static Object createObject(String factoryId, String propertiesFilename, String fallbackClassName) throws ConfigurationError {
        Class factoryClass = lookUpFactoryClass(factoryId, propertiesFilename, fallbackClassName);
        if (factoryClass != null) {
            try {
                Object instance = factoryClass.newInstance();
                debugPrintln("created new instance of factory " + factoryId);
                return instance;
            } catch (Exception x) {
                throw new ConfigurationError("Provider for factory " + factoryId + " could not be instantiated: " + x, x);
            }
        } else {
            throw new ConfigurationError("Provider for " + factoryId + " cannot be found", null);
        }
    }

    static Class lookUpFactoryClass(String factoryId) throws ConfigurationError {
        return lookUpFactoryClass(factoryId, null, null);
    }

    static Class lookUpFactoryClass(String factoryId, String propertiesFilename, String fallbackClassName) throws ConfigurationError {
        String factoryClassName = lookUpFactoryClassName(factoryId, propertiesFilename, fallbackClassName);
        ClassLoader cl = findClassLoader();
        if (factoryClassName == null) {
            factoryClassName = fallbackClassName;
        }
        try {
            Class providerClass = findProviderClass(factoryClassName, cl, true);
            debugPrintln("created new instance of " + providerClass + " using ClassLoader: " + cl);
            return providerClass;
        } catch (ClassNotFoundException x) {
            throw new ConfigurationError("Provider " + factoryClassName + " not found", x);
        } catch (Exception x2) {
            throw new ConfigurationError("Provider " + factoryClassName + " could not be instantiated: " + x2, x2);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a8, code lost:
        if (r12 != null) goto L_0x00aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r12.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b9, code lost:
        if (r12 == null) goto L_0x00bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ef, code lost:
        if (r5 != null) goto L_0x00f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0103, code lost:
        if (0 == 0) goto L_0x0106;
     */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00c2  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0108  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0125  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:52:0x00d4=Splitter:B:52:0x00d4, B:41:0x00bd=Splitter:B:41:0x00bd} */
    static String lookUpFactoryClassName(String factoryId, String propertiesFilename, String fallbackClassName) {
        String str = factoryId;
        String propertiesFilename2 = propertiesFilename;
        SecuritySupport ss = SecuritySupport.getInstance();
        try {
            String systemProp = ss.getSystemProperty(str);
            if (systemProp != null) {
                debugPrintln("found system property, value=" + systemProp);
                return systemProp;
            }
        } catch (SecurityException e) {
        }
        String factoryClassName = null;
        FileInputStream fis = null;
        if (propertiesFilename2 == null) {
            File propertiesFile = null;
            boolean propertiesFileExists = false;
            try {
                String javah = ss.getSystemProperty("java.home");
                propertiesFilename2 = javah + File.separator + "lib" + File.separator + DEFAULT_PROPERTIES_FILENAME;
                propertiesFile = new File(propertiesFilename2);
                propertiesFileExists = ss.getFileExists(propertiesFile);
            } catch (SecurityException e2) {
                fLastModified = -1;
                fXalanProperties = null;
            }
            String propertiesFilename3 = propertiesFilename2;
            synchronized (ObjectFactory.class) {
                boolean loadProperties = false;
                FileInputStream fis2 = null;
                try {
                    if (fLastModified >= 0) {
                        if (propertiesFileExists) {
                            long j = fLastModified;
                            long lastModified = ss.getLastModified(propertiesFile);
                            fLastModified = lastModified;
                            if (j < lastModified) {
                                loadProperties = true;
                            }
                        }
                        if (!propertiesFileExists) {
                            fLastModified = -1;
                            fXalanProperties = null;
                        }
                    } else if (propertiesFileExists) {
                        loadProperties = true;
                        fLastModified = ss.getLastModified(propertiesFile);
                    }
                    if (loadProperties) {
                        fXalanProperties = new Properties();
                        fis2 = ss.getFileInputStream(propertiesFile);
                        fXalanProperties.load(fis2);
                    }
                } catch (Exception e3) {
                    try {
                        fXalanProperties = null;
                        fLastModified = -1;
                    } catch (Throwable th) {
                        Throwable th2 = th;
                        if (fis2 != null) {
                            try {
                                fis2.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th2;
                    }
                }
            }
            if (fXalanProperties != null) {
                factoryClassName = fXalanProperties.getProperty(str);
            }
            propertiesFilename2 = propertiesFilename3;
        } else {
            try {
                fis = ss.getFileInputStream(new File(propertiesFilename2));
                Properties props = new Properties();
                props.load(fis);
                factoryClassName = props.getProperty(str);
            } catch (Exception e5) {
            } catch (Throwable th3) {
                FileInputStream fis3 = null;
                Throwable th4 = th3;
                if (fis3 != null) {
                    try {
                        fis3.close();
                    } catch (IOException e6) {
                    }
                }
                throw th4;
            }
        }
        if (factoryClassName != null) {
            return findJarServiceProviderName(factoryId);
        }
        debugPrintln("found in " + propertiesFilename2 + ", value=" + factoryClassName);
        return factoryClassName;
        if (fXalanProperties != null) {
        }
        propertiesFilename2 = propertiesFilename3;
        if (factoryClassName != null) {
        }
    }

    private static void debugPrintln(String msg) {
    }

    static ClassLoader findClassLoader() throws ConfigurationError {
        SecuritySupport ss = SecuritySupport.getInstance();
        ClassLoader context = ss.getContextClassLoader();
        ClassLoader system = ss.getSystemClassLoader();
        for (ClassLoader chain = system; context != chain; chain = ss.getParentClassLoader(chain)) {
            if (chain == null) {
                return context;
            }
        }
        ClassLoader current = ObjectFactory.class.getClassLoader();
        for (ClassLoader chain2 = system; current != chain2; chain2 = ss.getParentClassLoader(chain2)) {
            if (chain2 == null) {
                return current;
            }
        }
        return system;
    }

    static Object newInstance(String className, ClassLoader cl, boolean doFallback) throws ConfigurationError {
        try {
            Class providerClass = findProviderClass(className, cl, doFallback);
            Object instance = providerClass.newInstance();
            debugPrintln("created new instance of " + providerClass + " using ClassLoader: " + cl);
            return instance;
        } catch (ClassNotFoundException x) {
            throw new ConfigurationError("Provider " + className + " not found", x);
        } catch (Exception x2) {
            throw new ConfigurationError("Provider " + className + " could not be instantiated: " + x2, x2);
        }
    }

    static Class findProviderClass(String className, ClassLoader cl, boolean doFallback) throws ClassNotFoundException, ConfigurationError {
        Class providerClass;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            try {
                int lastDot = className.lastIndexOf(Constants.ATTRVAL_THIS);
                String packageName = className;
                if (lastDot != -1) {
                    packageName = className.substring(0, lastDot);
                }
                security.checkPackageAccess(packageName);
            } catch (SecurityException e) {
                throw e;
            }
        }
        if (cl == null) {
            return Class.forName(className);
        }
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException x) {
            if (doFallback) {
                ClassLoader current = ObjectFactory.class.getClassLoader();
                if (current == null) {
                    providerClass = Class.forName(className);
                } else if (cl != current) {
                    providerClass = current.loadClass(className);
                } else {
                    throw x;
                }
                return providerClass;
            }
            throw x;
        }
    }

    private static String findJarServiceProviderName(String factoryId) {
        BufferedReader rd;
        SecuritySupport ss = SecuritySupport.getInstance();
        String serviceId = SERVICES_PATH + factoryId;
        ClassLoader cl = findClassLoader();
        InputStream is = ss.getResourceAsStream(cl, serviceId);
        if (is == null) {
            ClassLoader current = ObjectFactory.class.getClassLoader();
            if (cl != current) {
                cl = current;
                is = ss.getResourceAsStream(cl, serviceId);
            }
        }
        if (is == null) {
            return null;
        }
        debugPrintln("found jar resource=" + serviceId + " using ClassLoader: " + cl);
        try {
            rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(is));
        }
        try {
            String factoryClassName = rd.readLine();
            try {
                rd.close();
            } catch (IOException e2) {
            }
            if (factoryClassName == null || "".equals(factoryClassName)) {
                return null;
            }
            debugPrintln("found in resource, value=" + factoryClassName);
            return factoryClassName;
        } catch (IOException e3) {
            try {
                rd.close();
            } catch (IOException e4) {
            }
            return null;
        } catch (Throwable th) {
            try {
                rd.close();
            } catch (IOException e5) {
            }
            throw th;
        }
    }
}
