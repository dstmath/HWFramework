package javax.xml.datatype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;

final class FactoryFinder {
    private static final String CLASS_NAME = "javax.xml.datatype.FactoryFinder";
    private static final int DEFAULT_LINE_LENGTH = 80;
    private static boolean debug;

    private static class CacheHolder {
        private static Properties cacheProps;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.xml.datatype.FactoryFinder.CacheHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.xml.datatype.FactoryFinder.CacheHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.FactoryFinder.CacheHolder.<clinit>():void");
        }

        private CacheHolder() {
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.xml.datatype.FactoryFinder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.xml.datatype.FactoryFinder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.FactoryFinder.<clinit>():void");
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
        AutoCloseable rd;
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
        if (debug) {
            debugPrintln("found jar resource=" + serviceId + " using ClassLoader: " + cl);
        }
        try {
            rd = new BufferedReader(new InputStreamReader(is, "UTF-8"), DEFAULT_LINE_LENGTH);
        } catch (UnsupportedEncodingException e) {
            rd = new BufferedReader(new InputStreamReader(is), DEFAULT_LINE_LENGTH);
        }
        String str = null;
        try {
            str = rd.readLine();
            if (str == null || XmlPullParser.NO_NAMESPACE.equals(str)) {
                return null;
            }
            if (debug) {
                debugPrintln("found in resource, value=" + str);
            }
            return newInstance(str, cl);
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
