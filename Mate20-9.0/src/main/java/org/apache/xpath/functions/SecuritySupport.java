package org.apache.xpath.functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

class SecuritySupport {
    private static final Object securitySupport;

    SecuritySupport() {
    }

    static {
        SecuritySupport ss = null;
        try {
            Class<?> cls = Class.forName("java.security.AccessController");
            ss = new SecuritySupport12();
        } catch (Exception e) {
            if (0 == 0) {
                ss = new SecuritySupport();
            }
        } catch (Throwable th) {
            if (0 == 0) {
                ss = new SecuritySupport();
            }
            securitySupport = ss;
            throw th;
        }
        securitySupport = ss;
    }

    static SecuritySupport getInstance() {
        return (SecuritySupport) securitySupport;
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getContextClassLoader() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getSystemClassLoader() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getParentClassLoader(ClassLoader cl) {
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getSystemProperty(String propName) {
        return System.getProperty(propName);
    }

    /* access modifiers changed from: package-private */
    public FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    /* access modifiers changed from: package-private */
    public InputStream getResourceAsStream(ClassLoader cl, String name) {
        if (cl == null) {
            return ClassLoader.getSystemResourceAsStream(name);
        }
        return cl.getResourceAsStream(name);
    }

    /* access modifiers changed from: package-private */
    public boolean getFileExists(File f) {
        return f.exists();
    }

    /* access modifiers changed from: package-private */
    public long getLastModified(File f) {
        return f.lastModified();
    }
}
