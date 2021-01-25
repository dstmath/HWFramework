package ohos.javax.xml.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

class SecuritySupport {
    SecuritySupport() {
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getContextClassLoader() throws SecurityException {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.javax.xml.stream.SecuritySupport.AnonymousClass1 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                return contextClassLoader == null ? ClassLoader.getSystemClassLoader() : contextClassLoader;
            }
        });
    }

    /* access modifiers changed from: package-private */
    public String getSystemProperty(final String str) {
        return (String) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.javax.xml.stream.SecuritySupport.AnonymousClass2 */

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
                /* class ohos.javax.xml.stream.SecuritySupport.AnonymousClass3 */

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
            /* class ohos.javax.xml.stream.SecuritySupport.AnonymousClass4 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                ClassLoader classLoader = classLoader;
                if (classLoader == null) {
                    return Object.class.getResourceAsStream(str);
                }
                return classLoader.getResourceAsStream(str);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public boolean doesFileExist(final File file) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction() {
            /* class ohos.javax.xml.stream.SecuritySupport.AnonymousClass5 */

            @Override // java.security.PrivilegedAction
            public Object run() {
                return new Boolean(file.exists());
            }
        })).booleanValue();
    }
}
