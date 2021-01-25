package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

final class SecuritySupport {
    private static final SecuritySupport securitySupport = new SecuritySupport();

    static SecuritySupport getInstance() {
        return securitySupport;
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xml.internal.serialize.SecuritySupport.AnonymousClass1 */

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

    /* access modifiers changed from: package-private */
    public ClassLoader getSystemClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xml.internal.serialize.SecuritySupport.AnonymousClass2 */

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

    /* access modifiers changed from: package-private */
    public ClassLoader getParentClassLoader(final ClassLoader classLoader) {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xml.internal.serialize.SecuritySupport.AnonymousClass3 */

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

    /* access modifiers changed from: package-private */
    public String getSystemProperty(final String str) {
        return (String) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xml.internal.serialize.SecuritySupport.AnonymousClass4 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return System.getProperty(str);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public FileInputStream getFileInputStream(final File file) throws FileNotFoundException {
        try {
            return (FileInputStream) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                /* class ohos.com.sun.org.apache.xml.internal.serialize.SecuritySupport.AnonymousClass5 */

                @Override // java.security.PrivilegedExceptionAction
                public Object run() throws FileNotFoundException {
                    return new FileInputStream(file);
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((FileNotFoundException) e.getException());
        }
    }

    /* access modifiers changed from: package-private */
    public InputStream getResourceAsStream(final ClassLoader classLoader, final String str) {
        return (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xml.internal.serialize.SecuritySupport.AnonymousClass6 */

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

    /* access modifiers changed from: package-private */
    public boolean getFileExists(final File file) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xml.internal.serialize.SecuritySupport.AnonymousClass7 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return new Boolean(file.exists());
            }
        })).booleanValue();
    }

    /* access modifiers changed from: package-private */
    public long getLastModified(final File file) {
        return ((Long) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.com.sun.org.apache.xml.internal.serialize.SecuritySupport.AnonymousClass8 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return new Long(file.lastModified());
            }
        })).longValue();
    }

    private SecuritySupport() {
    }
}
