package ohos.javax.xml.xpath;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import ohos.com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl;

/* access modifiers changed from: package-private */
public class XPathFactoryFinder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String DEFAULT_PACKAGE = "com.sun.org.apache.xpath.internal";
    private static final Class<XPathFactory> SERVICE_CLASS = XPathFactory.class;
    private static final Properties cacheProps = new Properties();
    private static boolean debug;
    private static volatile boolean firstTime = true;
    private static final SecuritySupport ss = new SecuritySupport();
    private final ClassLoader classLoader;

    static {
        debug = false;
        try {
            debug = ss.getSystemProperty("jaxp.debug") != null;
        } catch (Exception unused) {
            debug = false;
        }
    }

    private static void debugPrintln(String str) {
        if (debug) {
            PrintStream printStream = System.err;
            printStream.println("JAXP: " + str);
        }
    }

    public XPathFactoryFinder(ClassLoader classLoader2) {
        this.classLoader = classLoader2;
        if (debug) {
            debugDisplayClassLoader();
        }
    }

    private void debugDisplayClassLoader() {
        try {
            if (this.classLoader == ss.getContextClassLoader()) {
                debugPrintln("using thread context class loader (" + this.classLoader + ") for search");
                return;
            }
        } catch (Throwable unused) {
        }
        if (this.classLoader == ClassLoader.getSystemClassLoader()) {
            debugPrintln("using system class loader (" + this.classLoader + ") for search");
            return;
        }
        debugPrintln("using class loader (" + this.classLoader + ") for search");
    }

    public XPathFactory newFactory(String str) throws XPathFactoryConfigurationException {
        if (str != null) {
            XPathFactory _newFactory = _newFactory(str);
            if (_newFactory != null) {
                debugPrintln("factory '" + _newFactory.getClass().getName() + "' was found for " + str);
            } else {
                debugPrintln("unable to find a factory for " + str);
            }
            return _newFactory;
        }
        throw new NullPointerException();
    }

    private XPathFactory _newFactory(String str) throws XPathFactoryConfigurationException {
        XPathFactory createInstance;
        String str2 = SERVICE_CLASS.getName() + ":" + str;
        try {
            debugPrintln("Looking up system property '" + str2 + "'");
            String systemProperty = ss.getSystemProperty(str2);
            if (systemProperty != null) {
                debugPrintln("The value is '" + systemProperty + "'");
                XPathFactory createInstance2 = createInstance(systemProperty);
                if (createInstance2 != null) {
                    return createInstance2;
                }
            } else {
                debugPrintln("The property is undefined.");
            }
        } catch (Throwable th) {
            if (debug) {
                debugPrintln("failed to look up system property '" + str2 + "'");
                th.printStackTrace();
            }
        }
        String str3 = ss.getSystemProperty("java.home") + File.separator + "lib" + File.separator + "jaxp.properties";
        try {
            if (firstTime) {
                synchronized (cacheProps) {
                    if (firstTime) {
                        File file = new File(str3);
                        firstTime = false;
                        if (ss.doesFileExist(file)) {
                            debugPrintln("Read properties file " + file);
                            cacheProps.load(ss.getFileInputStream(file));
                        }
                    }
                }
            }
            String property = cacheProps.getProperty(str2);
            debugPrintln("found " + property + " in $java.home/jaxp.properties");
            if (!(property == null || (createInstance = createInstance(property)) == null)) {
                return createInstance;
            }
        } catch (Exception e) {
            if (debug) {
                e.printStackTrace();
            }
        }
        XPathFactory findServiceProvider = findServiceProvider(str);
        if (findServiceProvider != null) {
            return findServiceProvider;
        }
        if (str.equals("http://java.sun.com/jaxp/xpath/dom")) {
            debugPrintln("attempting to use the platform default W3C DOM XPath lib");
            return new XPathFactoryImpl();
        }
        debugPrintln("all things were tried, but none was found. bailing out.");
        return null;
    }

    private Class<?> createClass(String str) {
        boolean z = (System.getSecurityManager() == null || str == null || !str.startsWith(DEFAULT_PACKAGE)) ? false : true;
        try {
            if (this.classLoader == null || z) {
                return Class.forName(str);
            }
            return Class.forName(str, false, this.classLoader);
        } catch (Throwable th) {
            if (!debug) {
                return null;
            }
            th.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public XPathFactory createInstance(String str) throws XPathFactoryConfigurationException {
        debugPrintln("createInstance(" + str + ")");
        Class<?> createClass = createClass(str);
        if (createClass == null) {
            debugPrintln("failed to getClass(" + str + ")");
            return null;
        }
        debugPrintln("loaded " + str + " from " + which(createClass));
        try {
            return (XPathFactory) createClass.newInstance();
        } catch (ClassCastException e) {
            debugPrintln("could not instantiate " + createClass.getName());
            if (debug) {
                e.printStackTrace();
            }
            return null;
        } catch (IllegalAccessException e2) {
            debugPrintln("could not instantiate " + createClass.getName());
            if (debug) {
                e2.printStackTrace();
            }
            return null;
        } catch (InstantiationException e3) {
            debugPrintln("could not instantiate " + createClass.getName());
            if (debug) {
                e3.printStackTrace();
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isObjectModelSupportedBy(final XPathFactory xPathFactory, final String str, AccessControlContext accessControlContext) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            /* class ohos.javax.xml.xpath.XPathFactoryFinder.AnonymousClass1 */

            @Override // java.security.PrivilegedAction
            public Boolean run() {
                return Boolean.valueOf(xPathFactory.isObjectModelSupported(str));
            }
        }, accessControlContext)).booleanValue();
    }

    private XPathFactory findServiceProvider(final String str) throws XPathFactoryConfigurationException {
        final AccessControlContext context = AccessController.getContext();
        try {
            return (XPathFactory) AccessController.doPrivileged(new PrivilegedAction<XPathFactory>() {
                /* class ohos.javax.xml.xpath.XPathFactoryFinder.AnonymousClass2 */

                @Override // java.security.PrivilegedAction
                public XPathFactory run() {
                    Iterator it = ServiceLoader.load(XPathFactoryFinder.SERVICE_CLASS).iterator();
                    while (it.hasNext()) {
                        XPathFactory xPathFactory = (XPathFactory) it.next();
                        if (XPathFactoryFinder.this.isObjectModelSupportedBy(xPathFactory, str, context)) {
                            return xPathFactory;
                        }
                    }
                    return null;
                }
            });
        } catch (ServiceConfigurationError e) {
            throw new XPathFactoryConfigurationException(e);
        }
    }

    private static String which(Class cls) {
        return which(cls.getName(), cls.getClassLoader());
    }

    private static String which(String str, ClassLoader classLoader2) {
        String str2 = str.replace('.', '/') + ".class";
        if (classLoader2 == null) {
            classLoader2 = ClassLoader.getSystemClassLoader();
        }
        URL resourceAsURL = ss.getResourceAsURL(classLoader2, str2);
        if (resourceAsURL != null) {
            return resourceAsURL.toString();
        }
        return null;
    }
}
