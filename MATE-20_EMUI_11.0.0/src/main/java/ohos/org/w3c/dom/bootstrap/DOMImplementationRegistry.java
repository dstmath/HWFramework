package ohos.org.w3c.dom.bootstrap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.DOMImplementationList;
import ohos.org.w3c.dom.DOMImplementationSource;

public final class DOMImplementationRegistry {
    private static final int DEFAULT_LINE_LENGTH = 80;
    private static final String DEFAULT_PACKAGE = "com.sun.org.apache.xerces.internal.dom";
    private static final String FALLBACK_CLASS = "ohos.com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl";
    public static final String PROPERTY = "ohos.org.w3c.dom.DOMImplementationSourceList";
    private Vector sources;

    private DOMImplementationRegistry(Vector vector) {
        this.sources = vector;
    }

    public static DOMImplementationRegistry newInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
        Class<?> cls;
        Vector vector = new Vector();
        ClassLoader classLoader = getClassLoader();
        String systemProperty = getSystemProperty(PROPERTY);
        if (systemProperty == null) {
            systemProperty = getServiceValue(classLoader);
        }
        if (systemProperty == null) {
            systemProperty = FALLBACK_CLASS;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(systemProperty);
        while (stringTokenizer.hasMoreTokens()) {
            String nextToken = stringTokenizer.nextToken();
            boolean z = false;
            if (!(System.getSecurityManager() == null || nextToken == null || !nextToken.startsWith(DEFAULT_PACKAGE))) {
                z = true;
            }
            if (classLoader == null || z) {
                cls = Class.forName(nextToken);
            } else {
                cls = classLoader.loadClass(nextToken);
            }
            vector.addElement((DOMImplementationSource) cls.newInstance());
        }
        return new DOMImplementationRegistry(vector);
    }

    public DOMImplementation getDOMImplementation(String str) {
        int size = this.sources.size();
        for (int i = 0; i < size; i++) {
            DOMImplementation dOMImplementation = ((DOMImplementationSource) this.sources.elementAt(i)).getDOMImplementation(str);
            if (dOMImplementation != null) {
                return dOMImplementation;
            }
        }
        return null;
    }

    public DOMImplementationList getDOMImplementationList(String str) {
        final Vector vector = new Vector();
        int size = this.sources.size();
        for (int i = 0; i < size; i++) {
            DOMImplementationList dOMImplementationList = ((DOMImplementationSource) this.sources.elementAt(i)).getDOMImplementationList(str);
            for (int i2 = 0; i2 < dOMImplementationList.getLength(); i2++) {
                vector.addElement(dOMImplementationList.item(i2));
            }
        }
        return new DOMImplementationList() {
            /* class ohos.org.w3c.dom.bootstrap.DOMImplementationRegistry.AnonymousClass1 */

            @Override // ohos.org.w3c.dom.DOMImplementationList
            public DOMImplementation item(int i) {
                if (i >= 0 && i < vector.size()) {
                    try {
                        return (DOMImplementation) vector.elementAt(i);
                    } catch (ArrayIndexOutOfBoundsException unused) {
                    }
                }
                return null;
            }

            @Override // ohos.org.w3c.dom.DOMImplementationList
            public int getLength() {
                return vector.size();
            }
        };
    }

    public void addSource(DOMImplementationSource dOMImplementationSource) {
        if (dOMImplementationSource == null) {
            throw new NullPointerException();
        } else if (!this.sources.contains(dOMImplementationSource)) {
            this.sources.addElement(dOMImplementationSource);
        }
    }

    private static ClassLoader getClassLoader() {
        try {
            ClassLoader contextClassLoader = getContextClassLoader();
            if (contextClassLoader != null) {
                return contextClassLoader;
            }
            return DOMImplementationRegistry.class.getClassLoader();
        } catch (Exception unused) {
            return DOMImplementationRegistry.class.getClassLoader();
        }
    }

    private static String getServiceValue(ClassLoader classLoader) {
        BufferedReader bufferedReader;
        try {
            InputStream resourceAsStream = getResourceAsStream(classLoader, "META-INF/services/ohos.org.w3c.dom.DOMImplementationSourceList");
            if (resourceAsStream != null) {
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, ConstantValue.JPEG_FILE_NAME_ENCODE_CHARSET), DEFAULT_LINE_LENGTH);
                } catch (UnsupportedEncodingException unused) {
                    bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream), DEFAULT_LINE_LENGTH);
                }
                String readLine = bufferedReader.readLine();
                bufferedReader.close();
                if (readLine != null && readLine.length() > 0) {
                    return readLine;
                }
            }
        } catch (Exception unused2) {
        }
        return null;
    }

    private static boolean isJRE11() {
        try {
            Class.forName("java.security.AccessController");
            return false;
        } catch (Exception unused) {
            return true;
        }
    }

    private static ClassLoader getContextClassLoader() {
        if (isJRE11()) {
            return null;
        }
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.org.w3c.dom.bootstrap.DOMImplementationRegistry.AnonymousClass2 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                try {
                    return Thread.currentThread().getContextClassLoader();
                } catch (SecurityException unused) {
                    return null;
                }
            }
        });
    }

    private static String getSystemProperty(final String str) {
        if (isJRE11()) {
            return System.getProperty(str);
        }
        return (String) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.org.w3c.dom.bootstrap.DOMImplementationRegistry.AnonymousClass3 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return System.getProperty(str);
            }
        });
    }

    private static InputStream getResourceAsStream(final ClassLoader classLoader, final String str) {
        if (!isJRE11()) {
            return (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
                /* class ohos.org.w3c.dom.bootstrap.DOMImplementationRegistry.AnonymousClass4 */

                @Override // java.security.PrivilegedAction
                public Object run() {
                    ClassLoader classLoader = classLoader;
                    if (classLoader == null) {
                        return ClassLoader.getSystemResourceAsStream(str);
                    }
                    return classLoader.getResourceAsStream(str);
                }
            });
        }
        if (classLoader == null) {
            return ClassLoader.getSystemResourceAsStream(str);
        }
        return classLoader.getResourceAsStream(str);
    }
}
