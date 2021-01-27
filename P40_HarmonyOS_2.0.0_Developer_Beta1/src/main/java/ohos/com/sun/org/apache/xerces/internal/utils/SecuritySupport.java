package ohos.com.sun.org.apache.xerces.internal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import ohos.ai.asr.util.AsrConstants;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

public final class SecuritySupport {
    static final Properties cacheProps = new Properties();
    static volatile boolean firstTime = true;
    private static final SecuritySupport securitySupport = new SecuritySupport();

    public static SecuritySupport getInstance() {
        return securitySupport;
    }

    static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport.AnonymousClass1 */

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

    static ClassLoader getSystemClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport.AnonymousClass2 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                try {
                    return ClassLoader.getSystemClassLoader();
                } catch (SecurityException unused) {
                    return null;
                }
            }
        });
    }

    static ClassLoader getParentClassLoader(final ClassLoader classLoader) {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport.AnonymousClass3 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                ClassLoader classLoader;
                try {
                    classLoader = classLoader.getParent();
                } catch (SecurityException unused) {
                    classLoader = null;
                }
                if (classLoader == classLoader) {
                    return null;
                }
                return classLoader;
            }
        });
    }

    public static String getSystemProperty(final String str) {
        return (String) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport.AnonymousClass4 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return System.getProperty(str);
            }
        });
    }

    static FileInputStream getFileInputStream(final File file) throws FileNotFoundException {
        try {
            return (FileInputStream) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                /* class ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport.AnonymousClass5 */

                @Override // java.security.PrivilegedExceptionAction
                public Object run() throws FileNotFoundException {
                    return new FileInputStream(file);
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((FileNotFoundException) e.getException());
        }
    }

    public static InputStream getResourceAsStream(String str) {
        if (System.getSecurityManager() != null) {
            return getResourceAsStream(null, str);
        }
        return getResourceAsStream(ObjectFactory.findClassLoader(), str);
    }

    public static InputStream getResourceAsStream(final ClassLoader classLoader, final String str) {
        return (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport.AnonymousClass6 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                ClassLoader classLoader = classLoader;
                if (classLoader != null) {
                    return classLoader.getResourceAsStream(str);
                }
                return Object.class.getResourceAsStream(PsuedoNames.PSEUDONAME_ROOT + str);
            }
        });
    }

    public static ResourceBundle getResourceBundle(String str) {
        return getResourceBundle(str, Locale.getDefault());
    }

    public static ResourceBundle getResourceBundle(final String str, final Locale locale) {
        return (ResourceBundle) AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>() {
            /* class ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport.AnonymousClass7 */

            @Override // java.security.PrivilegedAction
            public ResourceBundle run() {
                try {
                    return PropertyResourceBundle.getBundle(str, locale);
                } catch (MissingResourceException unused) {
                    try {
                        return PropertyResourceBundle.getBundle(str, new Locale("en", "US"));
                    } catch (MissingResourceException unused2) {
                        throw new MissingResourceException("Could not load any resource bundle by " + str, str, "");
                    }
                }
            }
        });
    }

    static boolean getFileExists(final File file) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport.AnonymousClass8 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return file.exists() ? Boolean.TRUE : Boolean.FALSE;
            }
        })).booleanValue();
    }

    static long getLastModified(final File file) {
        return ((Long) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport.AnonymousClass9 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return new Long(file.lastModified());
            }
        })).longValue();
    }

    public static String sanitizePath(String str) {
        if (str == null) {
            return "";
        }
        int lastIndexOf = str.lastIndexOf(PsuedoNames.PSEUDONAME_ROOT);
        return lastIndexOf > 0 ? str.substring(lastIndexOf + 1, str.length()) : str;
    }

    public static String checkAccess(String str, String str2, String str3) throws IOException {
        String str4;
        if (str == null || (str2 != null && str2.equalsIgnoreCase(str3))) {
            return null;
        }
        if (str.indexOf(":") == -1) {
            str4 = AsrConstants.ASR_SRC_FILE;
        } else {
            URL url = new URL(str);
            str4 = url.getProtocol();
            if (str4.equalsIgnoreCase("jar")) {
                String path = url.getPath();
                str4 = path.substring(0, path.indexOf(":"));
            }
        }
        if (isProtocolAllowed(str4, str2)) {
            return null;
        }
        return str4;
    }

    private static boolean isProtocolAllowed(String str, String str2) {
        if (str2 == null) {
            return false;
        }
        for (String str3 : str2.split(",")) {
            if (str3.trim().equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public static String getJAXPSystemProperty(String str) {
        String systemProperty = getSystemProperty(str);
        return systemProperty == null ? readJAXPProperty(str) : systemProperty;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005c, code lost:
        if (r2 != null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x006d, code lost:
        if (r2 != null) goto L_0x005e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0068 A[SYNTHETIC, Splitter:B:35:0x0068] */
    static String readJAXPProperty(String str) {
        FileInputStream fileInputStream;
        Throwable th;
        String str2 = null;
        InputStream inputStream = null;
        str2 = null;
        try {
            if (firstTime) {
                synchronized (cacheProps) {
                    try {
                        if (firstTime) {
                            File file = new File(getSystemProperty("java.home") + File.separator + "lib" + File.separator + "jaxp.properties");
                            if (getFileExists(file)) {
                                fileInputStream = getFileInputStream(file);
                                cacheProps.load(fileInputStream);
                            } else {
                                fileInputStream = null;
                            }
                            firstTime = false;
                        } else {
                            fileInputStream = null;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            } else {
                fileInputStream = null;
            }
            str2 = cacheProps.getProperty(str);
        } catch (Exception unused) {
            fileInputStream = null;
        } catch (Throwable th3) {
            th = th3;
            if (inputStream != null) {
            }
            throw th;
        }
        return str2;
    }

    private SecuritySupport() {
    }
}
