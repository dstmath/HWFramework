package javax.xml.validation;

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
import javax.xml.XMLConstants;
import libcore.io.IoUtils;

final class SchemaFactoryFinder {
    private static final int DEFAULT_LINE_LENGTH = 80;
    private static final Class SERVICE_CLASS = SchemaFactory.class;
    private static final String SERVICE_ID = ("META-INF/services/" + SERVICE_CLASS.getName());
    private static final String W3C_XML_SCHEMA10_NS_URI = "http://www.w3.org/XML/XMLSchema/v1.0";
    private static final String W3C_XML_SCHEMA11_NS_URI = "http://www.w3.org/XML/XMLSchema/v1.1";
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
                if (SchemaFactoryFinder.debug) {
                    SchemaFactoryFinder.debugPrintln("Read properties file " + f);
                }
                try {
                    inputStream = new FileInputStream(f);
                    cacheProps.load(inputStream);
                    inputStream.close();
                    return;
                } catch (Exception ex) {
                    if (SchemaFactoryFinder.debug) {
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

    public SchemaFactoryFinder(ClassLoader loader) {
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

    public SchemaFactory newFactory(String schemaLanguage) {
        if (schemaLanguage != null) {
            SchemaFactory f = _newFactory(schemaLanguage);
            if (debug) {
                if (f != null) {
                    debugPrintln("factory '" + f.getClass().getName() + "' was found for " + schemaLanguage);
                } else {
                    debugPrintln("unable to find a factory for " + schemaLanguage);
                }
            }
            return f;
        }
        throw new NullPointerException("schemaLanguage == null");
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x009e A[Catch:{ Exception -> 0x00c1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00b9 A[Catch:{ Exception -> 0x00c1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x015a  */
    private SchemaFactory _newFactory(String schemaLanguage) {
        String factoryClassName;
        String propertyName = SERVICE_CLASS.getName() + ":" + schemaLanguage;
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
                        SchemaFactory sf = createInstance(factoryClassName);
                        if (sf != null) {
                            return sf;
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
                        SchemaFactory sf2 = loadFromServicesFile(schemaLanguage, resource.toExternalForm(), resource.openStream());
                        if (sf2 != null) {
                            return sf2;
                        }
                    } catch (IOException e) {
                        if (debug) {
                            debugPrintln("failed to read " + resource);
                            e.printStackTrace();
                        }
                    }
                }
                if (!schemaLanguage.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI) || schemaLanguage.equals(W3C_XML_SCHEMA10_NS_URI)) {
                    if (debug) {
                        debugPrintln("attempting to use the platform default XML Schema 1.0 validator");
                    }
                    return createInstance("org.apache.xerces.jaxp.validation.XMLSchemaFactory");
                } else if (schemaLanguage.equals(W3C_XML_SCHEMA11_NS_URI)) {
                    if (debug) {
                        debugPrintln("attempting to use the platform default XML Schema 1.1 validator");
                    }
                    return createInstance("org.apache.xerces.jaxp.validation.XMLSchema11Factory");
                } else {
                    if (debug) {
                        debugPrintln("all things were tried, but none was found. bailing out.");
                    }
                    return null;
                }
            } else {
                if (debug) {
                    debugPrintln("The value is '" + r + "'");
                }
                SchemaFactory sf3 = createInstance(r);
                if (sf3 != null) {
                    return sf3;
                }
                factoryClassName = CacheHolder.cacheProps.getProperty(propertyName);
                if (debug) {
                }
                if (factoryClassName != null) {
                }
                while (r1.hasNext()) {
                }
                if (!schemaLanguage.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                }
                if (debug) {
                }
                return createInstance("org.apache.xerces.jaxp.validation.XMLSchemaFactory");
            }
        } catch (VirtualMachineError vme) {
            throw vme;
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            if (debug) {
                debugPrintln("failed to look up system property '" + propertyName + "'");
                t.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public SchemaFactory createInstance(String className) {
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
            if (newInstance instanceof SchemaFactory) {
                return (SchemaFactory) newInstance;
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
            debugPrintln("failed to instantiate " + className);
            if (debug) {
                t.printStackTrace();
            }
        }
    }

    private Iterable<URL> createServiceFileIterator() {
        if (this.classLoader == null) {
            return Collections.singleton(SchemaFactoryFinder.class.getClassLoader().getResource(SERVICE_ID));
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

    private SchemaFactory loadFromServicesFile(String schemaLanguage, String resourceName, InputStream in) {
        BufferedReader rd;
        if (debug) {
            debugPrintln("Reading " + resourceName);
        }
        try {
            rd = new BufferedReader(new InputStreamReader(in, "UTF-8"), 80);
        } catch (UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(in), 80);
        }
        SchemaFactory resultFactory = null;
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
                        SchemaFactory foundFactory = createInstance(factoryClassName2);
                        if (foundFactory.isSchemaLanguageSupported(schemaLanguage)) {
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
