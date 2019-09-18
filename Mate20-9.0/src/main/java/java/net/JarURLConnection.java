package java.net;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.net.www.ParseUtil;

public abstract class JarURLConnection extends URLConnection {
    private String entryName;
    private URL jarFileURL;
    protected URLConnection jarFileURLConnection;

    public abstract JarFile getJarFile() throws IOException;

    protected JarURLConnection(URL url) throws MalformedURLException {
        super(url);
        parseSpecs(url);
    }

    private void parseSpecs(URL url) throws MalformedURLException {
        String spec = url.getFile();
        int separator = spec.indexOf("!/");
        if (separator != -1) {
            this.jarFileURL = new URL(spec.substring(0, separator));
            this.entryName = null;
            int separator2 = separator + 1 + 1;
            if (separator2 != spec.length()) {
                this.entryName = spec.substring(separator2, spec.length());
                this.entryName = ParseUtil.decode(this.entryName);
                return;
            }
            return;
        }
        throw new MalformedURLException("no !/ found in url spec:" + spec);
    }

    public URL getJarFileURL() {
        return this.jarFileURL;
    }

    public String getEntryName() {
        return this.entryName;
    }

    public Manifest getManifest() throws IOException {
        return getJarFile().getManifest();
    }

    public JarEntry getJarEntry() throws IOException {
        return getJarFile().getJarEntry(this.entryName);
    }

    public Attributes getAttributes() throws IOException {
        JarEntry e = getJarEntry();
        if (e != null) {
            return e.getAttributes();
        }
        return null;
    }

    public Attributes getMainAttributes() throws IOException {
        Manifest man = getManifest();
        if (man != null) {
            return man.getMainAttributes();
        }
        return null;
    }

    public Certificate[] getCertificates() throws IOException {
        JarEntry e = getJarEntry();
        if (e != null) {
            return e.getCertificates();
        }
        return null;
    }
}
