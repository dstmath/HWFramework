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
    private Map<String, Attributes> superEntries;
    private Manifest superMan;

    public interface URLJarFileCloseController {
        void close(JarFile jarFile);
    }

    private class URLJarFileEntry extends JarEntry {
        private JarEntry je;

        URLJarFileEntry(JarEntry je) {
            super(je);
            this.je = je;
        }

        public Attributes getAttributes() throws IOException {
            if (URLJarFile.this.isSuperMan()) {
                Map<String, Attributes> e = URLJarFile.this.superEntries;
                if (e != null) {
                    Attributes a = (Attributes) e.get(getName());
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

    static JarFile getJarFile(URL url, URLJarFileCloseController closeController) throws IOException {
        if (isFileURL(url)) {
            return new URLJarFile(url, closeController);
        }
        return retrieve(url, closeController);
    }

    public URLJarFile(File file) throws IOException {
        this(file, null);
    }

    public URLJarFile(File file, URLJarFileCloseController closeController) throws IOException {
        super(file, true, 5);
        this.closeController = null;
        this.closeController = closeController;
    }

    private URLJarFile(URL url, URLJarFileCloseController closeController) throws IOException {
        super(ParseUtil.decode(url.getFile()));
        this.closeController = null;
        this.closeController = closeController;
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

    protected void finalize() throws IOException {
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
                entries.put(key, (Attributes) ((Attributes) this.superEntries.get(key)).clone());
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

    private synchronized boolean isSuperMan() throws IOException {
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

    private static JarFile retrieve(URL url, final URLJarFileCloseController closeController) throws IOException {
        Throwable th;
        Throwable th2 = null;
        InputStream inputStream = null;
        try {
            inputStream = url.openConnection().getInputStream();
            JarFile result = (JarFile) AccessController.doPrivileged(new PrivilegedExceptionAction<JarFile>() {
                public JarFile run() throws IOException {
                    Path tmpFile = Files.createTempFile("jar_cache", null, new FileAttribute[0]);
                    try {
                        Files.copy(inputStream, tmpFile, StandardCopyOption.REPLACE_EXISTING);
                        JarFile jarFile = new URLJarFile(tmpFile.toFile(), closeController);
                        tmpFile.toFile().deleteOnExit();
                        return jarFile;
                    } catch (Throwable thr) {
                        try {
                            Files.delete(tmpFile);
                        } catch (IOException ioe) {
                            thr.addSuppressed(ioe);
                        }
                    }
                }
            });
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (th2 == null) {
                return result;
            }
            try {
                throw th2;
            } catch (PrivilegedActionException pae) {
                throw ((IOException) pae.getException());
            }
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }
}
