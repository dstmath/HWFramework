package javax.xml.xpath;

import java.io.BufferedReader;
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
    private static final Class SERVICE_CLASS = null;
    private static final String SERVICE_ID = null;
    private static boolean debug;
    private final ClassLoader classLoader;

    private static class CacheHolder {
        private static Properties cacheProps;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.xml.xpath.XPathFactoryFinder.CacheHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.xml.xpath.XPathFactoryFinder.CacheHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: javax.xml.xpath.XPathFactoryFinder.CacheHolder.<clinit>():void");
        }

        private CacheHolder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.xml.xpath.XPathFactoryFinder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.xml.xpath.XPathFactoryFinder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.xpath.XPathFactoryFinder.<clinit>():void");
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
                if (uri.equals(XPathFactory.DEFAULT_OBJECT_MODEL_URI)) {
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
                debugPrintln("found " + factoryClassName + " in $java.home/jaxp.properties");
            }
            if (factoryClassName != null) {
                xpf = createInstance(factoryClassName);
                if (xpf != null) {
                    return xpf;
                }
            }
            for (URL resource2 : createServiceFileIterator()) {
                if (debug) {
                    debugPrintln("looking into " + resource2);
                }
                xpf = loadFromServicesFile(uri, resource2.toExternalForm(), resource2.openStream());
                if (xpf != null) {
                    return xpf;
                }
            }
            if (uri.equals(XPathFactory.DEFAULT_OBJECT_MODEL_URI)) {
                if (debug) {
                    debugPrintln("all things were tried, but none was found. bailing out.");
                }
                return null;
            }
            if (debug) {
                debugPrintln("attempting to use the platform default W3C DOM XPath lib");
            }
            return createInstance("org.apache.xpath.jaxp.XPathFactoryImpl");
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
        XPathFactory foundFactory;
        if (debug) {
            debugPrintln("Reading " + resourceName);
        }
        try {
            rd = new BufferedReader(new InputStreamReader(in, "UTF-8"), DEFAULT_LINE_LENGTH);
        } catch (UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(in), DEFAULT_LINE_LENGTH);
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
                        foundFactory = createInstance(factoryClassName);
                        if (foundFactory.isObjectModelSupported(uri)) {
                            break;
                        }
                    } catch (Exception e2) {
                    }
                }
            } catch (IOException e3) {
            }
        }
        resultFactory = foundFactory;
        IoUtils.closeQuietly(rd);
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
