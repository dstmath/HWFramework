package javax.xml.xpath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
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
    /* access modifiers changed from: private */
    public static boolean debug;
    private final ClassLoader classLoader;

    private static class CacheHolder {
        /* access modifiers changed from: private */
        public static Properties cacheProps = new Properties();

        private CacheHolder() {
        }

        static {
            FileInputStream inputStream;
            String javah = System.getProperty("java.home");
            File f = new File(javah + File.separator + "lib" + File.separator + "jaxp.properties");
            if (f.exists()) {
                if (XPathFactoryFinder.debug) {
                    XPathFactoryFinder.debugPrintln("Read properties file " + f);
                }
                try {
                    inputStream = new FileInputStream(f);
                    cacheProps.load(inputStream);
                    inputStream.close();
                    return;
                } catch (Exception ex) {
                    if (XPathFactoryFinder.debug) {
                        ex.printStackTrace();
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    r4.addSuppressed(th);
                }
            } else {
                return;
            }
            throw th;
        }
    }

    static {
        boolean z = false;
        debug = false;
        String val = System.getProperty("jaxp.debug");
        if (val != null && !"false".equals(val)) {
            z = true;
        }
        debug = z;
    }

    /* access modifiers changed from: private */
    public static void debugPrintln(String msg) {
        if (debug) {
            PrintStream printStream = System.err;
            printStream.println("JAXP: " + msg);
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
        if (uri != null) {
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
        throw new NullPointerException("uri == null");
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0081 A[Catch:{ Exception -> 0x00a4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x009c A[Catch:{ Exception -> 0x00a4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ba  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x010d  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x011d  */
    private XPathFactory _newFactory(String uri) {
        String factoryClassName;
        String propertyName = SERVICE_CLASS.getName() + ":" + uri;
        try {
            if (debug) {
                debugPrintln("Looking up system property '" + propertyName + "'");
            }
            String r = System.getProperty(propertyName);
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
                        XPathFactory xpf = createInstance(factoryClassName);
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
                        XPathFactory xpf2 = loadFromServicesFile(uri, resource.toExternalForm(), resource.openStream());
                        if (xpf2 != null) {
                            return xpf2;
                        }
                    } catch (IOException e) {
                        if (debug) {
                            debugPrintln("failed to read " + resource);
                            e.printStackTrace();
                        }
                    }
                }
                if (!uri.equals("http://java.sun.com/jaxp/xpath/dom")) {
                    if (debug) {
                        debugPrintln("attempting to use the platform default W3C DOM XPath lib");
                    }
                    return createInstance("org.apache.xpath.jaxp.XPathFactoryImpl");
                }
                if (debug) {
                    debugPrintln("all things were tried, but none was found. bailing out.");
                }
                return null;
            }
            if (debug) {
                debugPrintln("The value is '" + r + "'");
            }
            XPathFactory xpf3 = createInstance(r);
            if (xpf3 != null) {
                return xpf3;
            }
            factoryClassName = CacheHolder.cacheProps.getProperty(propertyName);
            if (debug) {
            }
            if (factoryClassName != null) {
            }
            while (r1.hasNext()) {
            }
            if (!uri.equals("http://java.sun.com/jaxp/xpath/dom")) {
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public XPathFactory createInstance(String className) {
        Class clazz;
        try {
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
            Object newInstance = clazz.newInstance();
            if (newInstance instanceof XPathFactory) {
                return (XPathFactory) newInstance;
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
        BufferedReader rd;
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
                String factoryClassName2 = factoryClassName.trim();
                if (factoryClassName2.length() != 0) {
                    try {
                        XPathFactory foundFactory = createInstance(factoryClassName2);
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
        IoUtils.closeQuietly((AutoCloseable) rd);
        return resultFactory;
    }

    private Iterable<URL> createServiceFileIterator() {
        if (this.classLoader == null) {
            return Collections.singleton(XPathFactoryFinder.class.getClassLoader().getResource(SERVICE_ID));
        }
        try {
            Enumeration<URL> e = this.classLoader.getResources(SERVICE_ID);
            if (debug && !e.hasMoreElements()) {
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
