package sun.net.www.protocol.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import sun.net.www.ParseUtil;

public class URLJarFile extends JarFile {
    private static int BUF_SIZE = 2048;
    private URLJarFileCloseController closeController;
    private Attributes superAttr;
    /* access modifiers changed from: private */
    public Map<String, Attributes> superEntries;
    private Manifest superMan;

    public interface URLJarFileCloseController {
        void close(JarFile jarFile);
    }

    private class URLJarFileEntry extends JarEntry {
        private JarEntry je;

        URLJarFileEntry(JarEntry je2) {
            super(je2);
            this.je = je2;
        }

        public Attributes getAttributes() throws IOException {
            if (URLJarFile.this.isSuperMan()) {
                Map<String, Attributes> e = URLJarFile.this.superEntries;
                if (e != null) {
                    Attributes a = e.get(getName());
                    if (a != null) {
                        return (Attributes) a.clone();
                    }
                }
            }
            return null;
        }

        public Certificate[] getCertificates() {
            Certificate[] certs = this.je.getCertificates();
            if (certs == null) {
                return null;
            }
            return (Certificate[]) certs.clone();
        }

        public CodeSigner[] getCodeSigners() {
            CodeSigner[] csg = this.je.getCodeSigners();
            if (csg == null) {
                return null;
            }
            return (CodeSigner[]) csg.clone();
        }
    }

    static JarFile getJarFile(URL url) throws IOException {
        return getJarFile(url, null);
    }

    static JarFile getJarFile(URL url, URLJarFileCloseController closeController2) throws IOException {
        if (isFileURL(url)) {
            return new URLJarFile(url, closeController2);
        }
        return retrieve(url, closeController2);
    }

    public URLJarFile(File file) throws IOException {
        this(file, (URLJarFileCloseController) null);
    }

    public URLJarFile(File file, URLJarFileCloseController closeController2) throws IOException {
        super(file, true, 5);
        this.closeController = null;
        this.closeController = closeController2;
    }

    private URLJarFile(URL url, URLJarFileCloseController closeController2) throws IOException {
        super(ParseUtil.decode(url.getFile()));
        this.closeController = null;
        this.closeController = closeController2;
    }

    private static boolean isFileURL(URL url) {
        if (url.getProtocol().equalsIgnoreCase("file")) {
            String host = url.getHost();
            if (host == null || host.equals("") || host.equals("~") || host.equalsIgnoreCase("localhost")) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws IOException {
        close();
    }

    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze == null) {
            return null;
        }
        if (ze instanceof JarEntry) {
            return new URLJarFileEntry((JarEntry) ze);
        }
        throw new InternalError(super.getClass() + " returned unexpected entry type " + ze.getClass());
    }

    public Manifest getManifest() throws IOException {
        if (!isSuperMan()) {
            return null;
        }
        Manifest man = new Manifest();
        man.getMainAttributes().putAll((Map) this.superAttr.clone());
        if (this.superEntries != null) {
            Map<String, Attributes> entries = man.getEntries();
            for (String key : this.superEntries.keySet()) {
                entries.put(key, (Attributes) this.superEntries.get(key).clone());
            }
        }
        return man;
    }

    public void close() throws IOException {
        if (this.closeController != null) {
            this.closeController.close(this);
        }
        super.close();
    }

    /* access modifiers changed from: private */
    public synchronized boolean isSuperMan() throws IOException {
        if (this.superMan == null) {
            this.superMan = super.getManifest();
        }
        if (this.superMan == null) {
            return false;
        }
        this.superAttr = this.superMan.getMainAttributes();
        this.superEntries = this.superMan.getEntries();
        return true;
    }

    private static JarFile retrieve(URL url) throws IOException {
        return retrieve(url, null);
    }

    private static JarFile retrieve(URL url, final URLJarFileCloseController closeController2) throws IOException {
        final InputStream in;
        try {
            in = url.openConnection().getInputStream();
            JarFile result = (JarFile) AccessController.doPrivileged(new PrivilegedExceptionAction<JarFile>() {
                public JarFile run() throws IOException {
                    Path tmpFile = Files.createTempFile("jar_cache", null, new FileAttribute[0]);
                    try {
                        Files.copy(InputStream.this, tmpFile, StandardCopyOption.REPLACE_EXISTING);
                        JarFile jarFile = new URLJarFile(tmpFile.toFile(), closeController2);
                        tmpFile.toFile().deleteOnExit();
                        return jarFile;
                    } catch (Throwable thr) {
                        try {
                            Files.delete(tmpFile);
                        } catch (IOException ioe) {
                            thr.addSuppressed(ioe);
                        }
                        throw thr;
                    }
                }
            });
            if (in != null) {
                in.close();
            }
            return result;
        } catch (PrivilegedActionException pae) {
            throw ((IOException) pae.getException());
        } catch (Throwable th) {
            r0.addSuppressed(th);
        }
        throw th;
    }
}
