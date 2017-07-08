package org.apache.xml.dtm.ref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

class SecuritySupport12 extends SecuritySupport {

    /* renamed from: org.apache.xml.dtm.ref.SecuritySupport12.3 */
    class AnonymousClass3 implements PrivilegedAction {
        final /* synthetic */ ClassLoader val$cl;

        AnonymousClass3(ClassLoader val$cl) {
            this.val$cl = val$cl;
        }

        public Object run() {
            ClassLoader parent = null;
            try {
                parent = this.val$cl.getParent();
            } catch (SecurityException e) {
            }
            return parent == this.val$cl ? null : parent;
        }
    }

    /* renamed from: org.apache.xml.dtm.ref.SecuritySupport12.4 */
    class AnonymousClass4 implements PrivilegedAction {
        final /* synthetic */ String val$propName;

        AnonymousClass4(String val$propName) {
            this.val$propName = val$propName;
        }

        public Object run() {
            return System.getProperty(this.val$propName);
        }
    }

    /* renamed from: org.apache.xml.dtm.ref.SecuritySupport12.5 */
    class AnonymousClass5 implements PrivilegedExceptionAction {
        final /* synthetic */ File val$file;

        AnonymousClass5(File val$file) {
            this.val$file = val$file;
        }

        public Object run() throws FileNotFoundException {
            return new FileInputStream(this.val$file);
        }
    }

    /* renamed from: org.apache.xml.dtm.ref.SecuritySupport12.6 */
    class AnonymousClass6 implements PrivilegedAction {
        final /* synthetic */ ClassLoader val$cl;
        final /* synthetic */ String val$name;

        AnonymousClass6(ClassLoader val$cl, String val$name) {
            this.val$cl = val$cl;
            this.val$name = val$name;
        }

        public Object run() {
            if (this.val$cl == null) {
                return ClassLoader.getSystemResourceAsStream(this.val$name);
            }
            return this.val$cl.getResourceAsStream(this.val$name);
        }
    }

    /* renamed from: org.apache.xml.dtm.ref.SecuritySupport12.7 */
    class AnonymousClass7 implements PrivilegedAction {
        final /* synthetic */ File val$f;

        AnonymousClass7(File val$f) {
            this.val$f = val$f;
        }

        public Object run() {
            return new Boolean(this.val$f.exists());
        }
    }

    /* renamed from: org.apache.xml.dtm.ref.SecuritySupport12.8 */
    class AnonymousClass8 implements PrivilegedAction {
        final /* synthetic */ File val$f;

        AnonymousClass8(File val$f) {
            this.val$f = val$f;
        }

        public Object run() {
            return new Long(this.val$f.lastModified());
        }
    }

    SecuritySupport12() {
    }

    ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader cl = null;
                try {
                    cl = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e) {
                }
                return cl;
            }
        });
    }

    ClassLoader getSystemClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader cl = null;
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (SecurityException e) {
                }
                return cl;
            }
        });
    }

    ClassLoader getParentClassLoader(ClassLoader cl) {
        return (ClassLoader) AccessController.doPrivileged(new AnonymousClass3(cl));
    }

    String getSystemProperty(String propName) {
        return (String) AccessController.doPrivileged(new AnonymousClass4(propName));
    }

    FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        try {
            return (FileInputStream) AccessController.doPrivileged(new AnonymousClass5(file));
        } catch (PrivilegedActionException e) {
            throw ((FileNotFoundException) e.getException());
        }
    }

    InputStream getResourceAsStream(ClassLoader cl, String name) {
        return (InputStream) AccessController.doPrivileged(new AnonymousClass6(cl, name));
    }

    boolean getFileExists(File f) {
        return ((Boolean) AccessController.doPrivileged(new AnonymousClass7(f))).booleanValue();
    }

    long getLastModified(File f) {
        return ((Long) AccessController.doPrivileged(new AnonymousClass8(f))).longValue();
    }
}
