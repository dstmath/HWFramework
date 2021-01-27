package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

public final class SecuritySupport {
    private static final SecuritySupport securitySupport = new SecuritySupport();

    public static SecuritySupport getInstance() {
        return securitySupport;
    }

    static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport.AnonymousClass1 */

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
            /* class ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport.AnonymousClass2 */

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
            /* class ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport.AnonymousClass3 */

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
            /* class ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport.AnonymousClass4 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return System.getProperty(str);
            }
        });
    }

    static FileInputStream getFileInputStream(final File file) throws FileNotFoundException {
        try {
            return (FileInputStream) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                /* class ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport.AnonymousClass5 */

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
        return getResourceAsStream(findClassLoader(), str);
    }

    public static InputStream getResourceAsStream(final ClassLoader classLoader, final String str) {
        return (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport.AnonymousClass6 */

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

    public static ListResourceBundle getResourceBundle(String str) {
        return getResourceBundle(str, Locale.getDefault());
    }

    public static ListResourceBundle getResourceBundle(final String str, final Locale locale) {
        return (ListResourceBundle) AccessController.doPrivileged(new PrivilegedAction<ListResourceBundle>() {
            /* class ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport.AnonymousClass7 */

            @Override // java.security.PrivilegedAction
            public ListResourceBundle run() {
                try {
                    return (ListResourceBundle) ResourceBundle.getBundle(str, locale);
                } catch (MissingResourceException unused) {
                    try {
                        return (ListResourceBundle) ResourceBundle.getBundle(str, new Locale("en", "US"));
                    } catch (MissingResourceException unused2) {
                        throw new MissingResourceException("Could not load any resource bundle by " + str, str, "");
                    }
                }
            }
        });
    }

    public static String[] getFileList(final File file, final FilenameFilter filenameFilter) {
        return (String[]) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport.AnonymousClass8 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return file.list(filenameFilter);
            }
        });
    }

    public static boolean getFileExists(final File file) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport.AnonymousClass9 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return file.exists() ? Boolean.TRUE : Boolean.FALSE;
            }
        })).booleanValue();
    }

    static long getLastModified(final File file) {
        return ((Long) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport.AnonymousClass10 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return new Long(file.lastModified());
            }
        })).longValue();
    }

    public static ClassLoader findClassLoader() {
        if (System.getSecurityManager() != null) {
            return null;
        }
        return SecuritySupport.class.getClassLoader();
    }

    private SecuritySupport() {
    }
}
