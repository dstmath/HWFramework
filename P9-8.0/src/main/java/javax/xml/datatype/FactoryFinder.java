package javax.xml.datatype;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;
import libcore.io.IoUtils;

final class FactoryFinder {
    private static final String CLASS_NAME = "javax.xml.datatype.FactoryFinder";
    private static final int DEFAULT_LINE_LENGTH = 80;
    private static boolean debug;

    private static class CacheHolder {
        private static Properties cacheProps = new Properties();

        private CacheHolder() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:28:0x0087 A:{SYNTHETIC, Splitter: B:28:0x0087} */
        /* JADX WARNING: Removed duplicated region for block: B:39:0x009a A:{Catch:{ Exception -> 0x008d }} */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x008c A:{SYNTHETIC, Splitter: B:31:0x008c} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        static {
            Exception ex;
            Throwable th;
            Throwable th2 = null;
            File f = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "jaxp.properties");
            if (f.exists()) {
                if (FactoryFinder.debug) {
                    FactoryFinder.debugPrintln("Read properties file " + f);
                }
                FileInputStream inputStream = null;
                try {
                    FileInputStream inputStream2 = new FileInputStream(f);
                    try {
                        cacheProps.load(inputStream2);
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        }
                        if (th2 != null) {
                            try {
                                throw th2;
                            } catch (Exception e) {
                                ex = e;
                                inputStream = inputStream2;
                            }
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        inputStream = inputStream2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th5) {
                                if (th2 == null) {
                                    th2 = th5;
                                } else if (th2 != th5) {
                                    th2.addSuppressed(th5);
                                }
                            }
                        }
                        if (th2 == null) {
                            try {
                                throw th2;
                            } catch (Exception e2) {
                                ex = e2;
                                if (FactoryFinder.debug) {
                                    ex.printStackTrace();
                                    return;
                                }
                                return;
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    if (inputStream != null) {
                    }
                    if (th2 == null) {
                    }
                }
            }
        }
    }

    static class ConfigurationError extends Error {
        private static final long serialVersionUID = -3644413026244211347L;
        private Exception exception;

        ConfigurationError(String msg, Exception x) {
            super(msg);
            this.exception = x;
        }

        Exception getException() {
            return this.exception;
        }
    }

    static {
        boolean z = false;
        debug = false;
        String val = System.getProperty("jaxp.debug");
        if (val != null) {
            z = "false".equals(val) ^ 1;
        }
        debug = z;
    }

    private FactoryFinder() {
    }

    private static void debugPrintln(String msg) {
        if (debug) {
            System.err.println("javax.xml.datatype.FactoryFinder:" + msg);
        }
    }

    private static ClassLoader findClassLoader() throws ConfigurationError {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (debug) {
            debugPrintln("Using context class loader: " + classLoader);
        }
        if (classLoader == null) {
            classLoader = FactoryFinder.class.getClassLoader();
            if (debug) {
                debugPrintln("Using the class loader of FactoryFinder: " + classLoader);
            }
        }
        return classLoader;
    }

    static Object newInstance(String className, ClassLoader classLoader) throws ConfigurationError {
        Class spiClass;
        if (classLoader == null) {
            try {
                spiClass = Class.forName(className);
            } catch (ClassNotFoundException x) {
                throw new ConfigurationError("Provider " + className + " not found", x);
            } catch (Exception x2) {
                throw new ConfigurationError("Provider " + className + " could not be instantiated: " + x2, x2);
            }
        }
        spiClass = classLoader.loadClass(className);
        if (debug) {
            debugPrintln("Loaded " + className + " from " + which(spiClass));
        }
        return spiClass.newInstance();
    }

    static Object find(String factoryId, String fallbackClassName) throws ConfigurationError {
        ClassLoader classLoader = findClassLoader();
        String systemProp = System.getProperty(factoryId);
        if (systemProp == null || systemProp.length() <= 0) {
            try {
                String factoryClassName = CacheHolder.cacheProps.getProperty(factoryId);
                if (debug) {
                    debugPrintln("found " + factoryClassName + " in $java.home/jaxp.properties");
                }
                if (factoryClassName != null) {
                    return newInstance(factoryClassName, classLoader);
                }
            } catch (Exception ex) {
                if (debug) {
                    ex.printStackTrace();
                }
            }
            Object provider = findJarServiceProvider(factoryId);
            if (provider != null) {
                return provider;
            }
            if (fallbackClassName == null) {
                throw new ConfigurationError("Provider for " + factoryId + " cannot be found", null);
            }
            if (debug) {
                debugPrintln("loaded from fallback value: " + fallbackClassName);
            }
            return newInstance(fallbackClassName, classLoader);
        }
        if (debug) {
            debugPrintln("found " + systemProp + " in the system property " + factoryId);
        }
        return newInstance(systemProp, classLoader);
    }

    private static Object findJarServiceProvider(String factoryId) throws ConfigurationError {
        String serviceId = "META-INF/services/" + factoryId;
        InputStream is = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            is = cl.getResourceAsStream(serviceId);
        }
        if (is == null) {
            cl = FactoryFinder.class.getClassLoader();
            is = cl.getResourceAsStream(serviceId);
        }
        if (is == null) {
            return null;
        }
        AutoCloseable rd;
        if (debug) {
            debugPrintln("found jar resource=" + serviceId + " using ClassLoader: " + cl);
        }
        try {
            rd = new BufferedReader(new InputStreamReader(is, "UTF-8"), 80);
        } catch (UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(is), 80);
        }
        String factoryClassName = null;
        try {
            factoryClassName = rd.readLine();
            if (factoryClassName == null || ("".equals(factoryClassName) ^ 1) == 0) {
                return null;
            }
            if (debug) {
                debugPrintln("found in resource, value=" + factoryClassName);
            }
            return newInstance(factoryClassName, cl);
        } catch (IOException e2) {
            return null;
        } finally {
            IoUtils.closeQuietly(rd);
        }
    }

    private static String which(Class clazz) {
        try {
            URL it;
            String classnameAsResource = clazz.getName().replace('.', '/') + ".class";
            ClassLoader loader = clazz.getClassLoader();
            if (loader != null) {
                it = loader.getResource(classnameAsResource);
            } else {
                it = ClassLoader.getSystemResource(classnameAsResource);
            }
            if (it != null) {
                return it.toString();
            }
        } catch (VirtualMachineError vme) {
            throw vme;
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            if (debug) {
                t.printStackTrace();
            }
        }
        return "unknown location";
    }
}
