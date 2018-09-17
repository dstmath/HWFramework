package org.apache.xpath.functions;

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

    ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader cl = null;
                try {
                    return Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e) {
                    return cl;
                }
            }
        });
    }

    ClassLoader getSystemClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader cl = null;
                try {
                    return ClassLoader.getSystemClassLoader();
                } catch (SecurityException e) {
                    return cl;
                }
            }
        });
    }

    ClassLoader getParentClassLoader(final ClassLoader cl) {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader parent = null;
                try {
                    parent = cl.getParent();
                } catch (SecurityException e) {
                }
                return parent == cl ? null : parent;
            }
        });
    }

    String getSystemProperty(final String propName) {
        return (String) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return System.getProperty(propName);
            }
        });
    }

    FileInputStream getFileInputStream(final File file) throws FileNotFoundException {
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

    InputStream getResourceAsStream(final ClassLoader cl, final String name) {
        return (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                if (cl == null) {
                    return ClassLoader.getSystemResourceAsStream(name);
                }
                return cl.getResourceAsStream(name);
            }
        });
    }

    boolean getFileExists(final File f) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new Boolean(f.exists());
            }
        })).booleanValue();
    }

    long getLastModified(final File f) {
        return ((Long) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new Long(f.lastModified());
            }
        })).longValue();
    }
}
