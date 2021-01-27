package ohos.javax.xml.validation;

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
import ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory;
import ohos.javax.xml.XMLConstants;

/* access modifiers changed from: package-private */
public class SchemaFactoryFinder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String DEFAULT_PACKAGE = "com.sun.org.apache.xerces.internal";
    private static final Class<SchemaFactory> SERVICE_CLASS = SchemaFactory.class;
    private static final Properties cacheProps = new Properties();
    private static boolean debug = false;
    private static volatile boolean firstTime = true;
    private static final SecuritySupport ss = new SecuritySupport();
    private final ClassLoader classLoader;

    static {
        boolean z = true;
        try {
            if (ss.getSystemProperty("jaxp.debug") == null) {
                z = false;
            }
            debug = z;
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

    public SchemaFactoryFinder(ClassLoader classLoader2) {
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

    public SchemaFactory newFactory(String str) {
        if (str != null) {
            SchemaFactory _newFactory = _newFactory(str);
            if (_newFactory != null) {
                debugPrintln("factory '" + _newFactory.getClass().getName() + "' was found for " + str);
            } else {
                debugPrintln("unable to find a factory for " + str);
            }
            return _newFactory;
        }
        throw new NullPointerException();
    }

    private SchemaFactory _newFactory(String str) {
        SchemaFactory createInstance;
        String str2 = SERVICE_CLASS.getName() + ":" + str;
        try {
            debugPrintln("Looking up system property '" + str2 + "'");
            String systemProperty = ss.getSystemProperty(str2);
            if (systemProperty != null) {
                debugPrintln("The value is '" + systemProperty + "'");
                SchemaFactory createInstance2 = createInstance(systemProperty);
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
        SchemaFactory findServiceProvider = findServiceProvider(str);
        if (findServiceProvider != null) {
            return findServiceProvider;
        }
        if (str.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
            debugPrintln("attempting to use the platform default XML Schema validator");
            return new XMLSchemaFactory();
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
    public SchemaFactory createInstance(String str) {
        debugPrintln("createInstance(" + str + ")");
        Class<?> createClass = createClass(str);
        if (createClass == null) {
            debugPrintln("failed to getClass(" + str + ")");
            return null;
        }
        debugPrintln("loaded " + str + " from " + which(createClass));
        try {
            if (SchemaFactory.class.isAssignableFrom(createClass)) {
                return (SchemaFactory) createClass.newInstance();
            }
            throw new ClassCastException(createClass.getName() + " cannot be cast to " + SchemaFactory.class);
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
    private boolean isSchemaLanguageSupportedBy(final SchemaFactory schemaFactory, final String str, AccessControlContext accessControlContext) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            /* class ohos.javax.xml.validation.SchemaFactoryFinder.AnonymousClass1 */

            @Override // java.security.PrivilegedAction
            public Boolean run() {
                return Boolean.valueOf(schemaFactory.isSchemaLanguageSupported(str));
            }
        }, accessControlContext)).booleanValue();
    }

    private SchemaFactory findServiceProvider(final String str) {
        final AccessControlContext context = AccessController.getContext();
        try {
            return (SchemaFactory) AccessController.doPrivileged(new PrivilegedAction<SchemaFactory>() {
                /* class ohos.javax.xml.validation.SchemaFactoryFinder.AnonymousClass2 */

                @Override // java.security.PrivilegedAction
                public SchemaFactory run() {
                    Iterator it = ServiceLoader.load(SchemaFactoryFinder.SERVICE_CLASS).iterator();
                    while (it.hasNext()) {
                        SchemaFactory schemaFactory = (SchemaFactory) it.next();
                        if (SchemaFactoryFinder.this.isSchemaLanguageSupportedBy(schemaFactory, str, context)) {
                            return schemaFactory;
                        }
                    }
                    return null;
                }
            });
        } catch (ServiceConfigurationError e) {
            throw new SchemaFactoryConfigurationError("Provider for " + SERVICE_CLASS + " cannot be created", e);
        }
    }

    private static String which(Class<?> cls) {
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
