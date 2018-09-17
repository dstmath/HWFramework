package org.apache.xml.serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

class SecuritySupport {
    private static final Object securitySupport;

    SecuritySupport() {
    }

    static {
        try {
            Class c = Class.forName("java.security.AccessController");
            SecuritySupport ss = new SecuritySupport12();
            if (ss == null) {
                ss = new SecuritySupport();
            }
            securitySupport = ss;
        } catch (Exception e) {
            securitySupport = new SecuritySupport();
        } catch (Throwable th) {
            securitySupport = new SecuritySupport();
        }
    }

    static SecuritySupport getInstance() {
        return (SecuritySupport) securitySupport;
    }

    ClassLoader getContextClassLoader() {
        return null;
    }

    ClassLoader getSystemClassLoader() {
        return null;
    }

    ClassLoader getParentClassLoader(ClassLoader cl) {
        return null;
    }

    String getSystemProperty(String propName) {
        return System.getProperty(propName);
    }

    FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    InputStream getResourceAsStream(ClassLoader cl, String name) {
        if (cl == null) {
            return ClassLoader.getSystemResourceAsStream(name);
        }
        return cl.getResourceAsStream(name);
    }

    boolean getFileExists(File f) {
        return f.exists();
    }

    long getLastModified(File f) {
        return f.lastModified();
    }
}
