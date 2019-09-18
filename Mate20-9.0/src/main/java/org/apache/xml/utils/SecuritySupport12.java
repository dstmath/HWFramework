package org.apache.xml.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

class SecuritySupport12 extends SecuritySupport {
    SecuritySupport12() {
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    return Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e) {
                    return null;
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getSystemClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    return ClassLoader.getSystemClassLoader();
                } catch (SecurityException e) {
                    return null;
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getParentClassLoader(final ClassLoader cl) {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader parent = null;
                try {
                    parent = cl.getParent();
                } catch (SecurityException e) {
                }
                if (parent == cl) {
                    return null;
                }
                return parent;
            }
        });
    }

    /* access modifiers changed from: package-private */
    public String getSystemProperty(final String propName) {
        return (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty(propName);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public FileInputStream getFileInputStream(final File file) throws FileNotFoundException {
        try {
            return (FileInputStream) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws FileNotFoundException {
                    return new FileInputStream(file);
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((FileNotFoundException) e.getException());
        }
    }

    /* access modifiers changed from: package-private */
    public InputStream getResourceAsStream(final ClassLoader cl, final String name) {
        return (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                if (cl == null) {
                    return ClassLoader.getSystemResourceAsStream(name);
                }
                return cl.getResourceAsStream(name);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public boolean getFileExists(final File f) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new Boolean(f.exists());
            }
        })).booleanValue();
    }

    /* access modifiers changed from: package-private */
    public long getLastModified(final File f) {
        return ((Long) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new Long(f.lastModified());
            }
        })).longValue();
    }
}
