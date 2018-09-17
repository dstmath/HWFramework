package javax.xml.xpath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import libcore.io.IoUtils;

final class XPathFactoryFinder {
    private static final int DEFAULT_LINE_LENGTH = 80;
    private static final Class SERVICE_CLASS = XPathFactory.class;
    private static final String SERVICE_ID = ("META-INF/services/" + SERVICE_CLASS.getName());
    private static boolean debug;
    private final ClassLoader classLoader;

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
                if (XPathFactoryFinder.debug) {
                    XPathFactoryFinder.debugPrintln("Read properties file " + f);
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
                                if (XPathFactoryFinder.debug) {
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

    static {
        boolean z = false;
        debug = false;
        String val = System.getProperty("jaxp.debug");
        if (val != null) {
            z = "false".equals(val) ^ 1;
        }
        debug = z;
    }

    private static void debugPrintln(String msg) {
        if (debug) {
            System.err.println("JAXP: " + msg);
        }
    }

    public XPathFactoryFinder(ClassLoader loader) {
        this.classLoader = loader;
        if (debug) {
            debugDisplayClassLoader();
        }
    }

    private void debugDisplayClassLoader() {
        if (this.classLoader == Thread.currentThread().getContextClassLoader()) {
            debugPrintln("using thread context class loader (" + this.classLoader + ") for search");
        } else if (this.classLoader == ClassLoader.getSystemClassLoader()) {
            debugPrintln("using system class loader (" + this.classLoader + ") for search");
        } else {
            debugPrintln("using class loader (" + this.classLoader + ") for search");
        }
    }

    public XPathFactory newFactory(String uri) {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        }
        XPathFactory f = _newFactory(uri);
        if (debug) {
            if (f != null) {
                debugPrintln("factory '" + f.getClass().getName() + "' was found for " + uri);
            } else {
                debugPrintln("unable to find a factory for " + uri);
            }
        }
        return f;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x008c A:{Catch:{ Exception -> 0x00b8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00ac A:{Catch:{ Exception -> 0x00b8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ce  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0139  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0127  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private XPathFactory _newFactory(String uri) {
        String propertyName = SERVICE_CLASS.getName() + ":" + uri;
        try {
            if (debug) {
                debugPrintln("Looking up system property '" + propertyName + "'");
            }
            String r = System.getProperty(propertyName);
            String factoryClassName;
            XPathFactory xpf;
            if (r == null || r.length() <= 0) {
                if (debug) {
                    debugPrintln("The property is undefined.");
                }
                try {
                    factoryClassName = CacheHolder.cacheProps.getProperty(propertyName);
                    if (debug) {
                        debugPrintln("found " + factoryClassName + " in $java.home/jaxp.properties");
                    }
                    if (factoryClassName != null) {
                        xpf = createInstance(factoryClassName);
                        if (xpf != null) {
                            return xpf;
                        }
                    }
                } catch (Exception ex) {
                    if (debug) {
                        ex.printStackTrace();
                    }
                }
                for (URL resource : createServiceFileIterator()) {
                    if (debug) {
                        debugPrintln("looking into " + resource);
                    }
                    try {
                        xpf = loadFromServicesFile(uri, resource.toExternalForm(), resource.openStream());
                        if (xpf != null) {
                            return xpf;
                        }
                    } catch (IOException e) {
                        if (debug) {
                            debugPrintln("failed to read " + resource);
                            e.printStackTrace();
                        }
                    }
                }
                if (uri.equals("http://java.sun.com/jaxp/xpath/dom")) {
                    if (debug) {
                        debugPrintln("all things were tried, but none was found. bailing out.");
                    }
                    return null;
                }
                if (debug) {
                    debugPrintln("attempting to use the platform default W3C DOM XPath lib");
                }
                return createInstance("org.apache.xpath.jaxp.XPathFactoryImpl");
            }
            if (debug) {
                debugPrintln("The value is '" + r + "'");
            }
            xpf = createInstance(r);
            if (xpf != null) {
                return xpf;
            }
            factoryClassName = CacheHolder.cacheProps.getProperty(propertyName);
            if (debug) {
            }
            if (factoryClassName != null) {
            }
            for (URL resource2 : createServiceFileIterator()) {
            }
            if (uri.equals("http://java.sun.com/jaxp/xpath/dom")) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    XPathFactory createInstance(String className) {
        try {
            Class clazz;
            if (debug) {
                debugPrintln("instantiating " + className);
            }
            if (this.classLoader != null) {
                clazz = this.classLoader.loadClass(className);
            } else {
                clazz = Class.forName(className);
            }
            if (debug) {
                debugPrintln("loaded it from " + which(clazz));
            }
            Object o = clazz.newInstance();
            if (o instanceof XPathFactory) {
                return (XPathFactory) o;
            }
            if (debug) {
                debugPrintln(className + " is not assignable to " + SERVICE_CLASS.getName());
            }
            return null;
        } catch (VirtualMachineError vme) {
            throw vme;
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            if (debug) {
                debugPrintln("failed to instantiate " + className);
                t.printStackTrace();
            }
        }
    }

    private XPathFactory loadFromServicesFile(String uri, String resourceName, InputStream in) {
        AutoCloseable rd;
        if (debug) {
            debugPrintln("Reading " + resourceName);
        }
        try {
            rd = new BufferedReader(new InputStreamReader(in, "UTF-8"), 80);
        } catch (UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(in), 80);
        }
        XPathFactory resultFactory = null;
        while (true) {
            try {
                String factoryClassName = rd.readLine();
                if (factoryClassName == null) {
                    break;
                }
                int hashIndex = factoryClassName.indexOf(35);
                if (hashIndex != -1) {
                    factoryClassName = factoryClassName.substring(0, hashIndex);
                }
                factoryClassName = factoryClassName.trim();
                if (factoryClassName.length() != 0) {
                    try {
                        XPathFactory foundFactory = createInstance(factoryClassName);
                        if (foundFactory.isObjectModelSupported(uri)) {
                            resultFactory = foundFactory;
                            break;
                        }
                    } catch (Exception e2) {
                    }
                }
            } catch (IOException e3) {
            }
        }
        IoUtils.closeQuietly(rd);
        return resultFactory;
    }

    private Iterable<URL> createServiceFileIterator() {
        if (this.classLoader == null) {
            return Collections.singleton(XPathFactoryFinder.class.getClassLoader().getResource(SERVICE_ID));
        }
        try {
            Enumeration<URL> e = this.classLoader.getResources(SERVICE_ID);
            if (debug && (e.hasMoreElements() ^ 1) != 0) {
                debugPrintln("no " + SERVICE_ID + " file was found");
            }
            return Collections.list(e);
        } catch (IOException e2) {
            if (debug) {
                debugPrintln("failed to enumerate resources " + SERVICE_ID);
                e2.printStackTrace();
            }
            return Collections.emptySet();
        }
    }

    private static String which(Class clazz) {
        return which(clazz.getName(), clazz.getClassLoader());
    }

    private static String which(String classname, ClassLoader loader) {
        String classnameAsResource = classname.replace('.', '/') + ".class";
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        URL it = loader.getResource(classnameAsResource);
        if (it != null) {
            return it.toString();
        }
        return null;
    }
}
