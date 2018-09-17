package sun.net.www.protocol.jar;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarURLConnection extends java.net.JarURLConnection {
    private static final boolean debug = false;
    private static final JarFileFactory factory = JarFileFactory.getInstance();
    private String contentType;
    private String entryName = getEntryName();
    private JarEntry jarEntry;
    private JarFile jarFile;
    private URL jarFileURL = getJarFileURL();
    private URLConnection jarFileURLConnection = this.jarFileURL.openConnection();
    private Permission permission;

    class JarURLInputStream extends FilterInputStream {
        JarURLInputStream(InputStream src) {
            super(src);
        }

        public void close() throws IOException {
            try {
                super.close();
            } finally {
                if (!JarURLConnection.this.getUseCaches()) {
                    JarURLConnection.this.jarFile.close();
                }
            }
        }
    }

    public JarURLConnection(URL url, Handler handler) throws MalformedURLException, IOException {
        super(url);
    }

    public JarFile getJarFile() throws IOException {
        connect();
        return this.jarFile;
    }

    public JarEntry getJarEntry() throws IOException {
        connect();
        return this.jarEntry;
    }

    public Permission getPermission() throws IOException {
        return this.jarFileURLConnection.getPermission();
    }

    public void connect() throws IOException {
        if (!this.connected) {
            this.jarFile = factory.get(getJarFileURL(), getUseCaches());
            if (getUseCaches()) {
                this.jarFileURLConnection = factory.getConnection(this.jarFile);
            }
            if (this.entryName != null) {
                this.jarEntry = (JarEntry) this.jarFile.getEntry(this.entryName);
                if (this.jarEntry == null) {
                    try {
                        if (!getUseCaches()) {
                            this.jarFile.close();
                        }
                    } catch (Exception e) {
                    }
                    throw new FileNotFoundException("JAR entry " + this.entryName + " not found in " + this.jarFile.getName());
                }
            }
            this.connected = true;
        }
    }

    public InputStream getInputStream() throws IOException {
        connect();
        if (this.entryName == null) {
            throw new IOException("no entry name specified");
        } else if (this.jarEntry != null) {
            return new JarURLInputStream(this.jarFile.getInputStream(this.jarEntry));
        } else {
            throw new FileNotFoundException("JAR entry " + this.entryName + " not found in " + this.jarFile.getName());
        }
    }

    public int getContentLength() {
        long result = getContentLengthLong();
        if (result > 2147483647L) {
            return -1;
        }
        return (int) result;
    }

    public long getContentLengthLong() {
        try {
            connect();
            if (this.jarEntry == null) {
                return this.jarFileURLConnection.getContentLengthLong();
            }
            return getJarEntry().getSize();
        } catch (IOException e) {
            return -1;
        }
    }

    public Object getContent() throws IOException {
        connect();
        if (this.entryName == null) {
            return this.jarFile;
        }
        return super.getContent();
    }

    public String getContentType() {
        if (this.contentType == null) {
            if (this.entryName == null) {
                this.contentType = "x-java/jar";
            } else {
                try {
                    connect();
                    InputStream in = this.jarFile.getInputStream(this.jarEntry);
                    this.contentType = URLConnection.guessContentTypeFromStream(new BufferedInputStream(in));
                    in.close();
                } catch (IOException e) {
                }
            }
            if (this.contentType == null) {
                this.contentType = URLConnection.guessContentTypeFromName(this.entryName);
            }
            if (this.contentType == null) {
                this.contentType = "content/unknown";
            }
        }
        return this.contentType;
    }

    public String getHeaderField(String name) {
        return this.jarFileURLConnection.getHeaderField(name);
    }

    public void setRequestProperty(String key, String value) {
        this.jarFileURLConnection.setRequestProperty(key, value);
    }

    public String getRequestProperty(String key) {
        return this.jarFileURLConnection.getRequestProperty(key);
    }

    public void addRequestProperty(String key, String value) {
        this.jarFileURLConnection.addRequestProperty(key, value);
    }

    public Map<String, List<String>> getRequestProperties() {
        return this.jarFileURLConnection.getRequestProperties();
    }

    public void setAllowUserInteraction(boolean allowuserinteraction) {
        this.jarFileURLConnection.setAllowUserInteraction(allowuserinteraction);
    }

    public boolean getAllowUserInteraction() {
        return this.jarFileURLConnection.getAllowUserInteraction();
    }

    public void setUseCaches(boolean usecaches) {
        this.jarFileURLConnection.setUseCaches(usecaches);
    }

    public boolean getUseCaches() {
        return this.jarFileURLConnection.getUseCaches();
    }

    public void setIfModifiedSince(long ifmodifiedsince) {
        this.jarFileURLConnection.setIfModifiedSince(ifmodifiedsince);
    }

    public void setDefaultUseCaches(boolean defaultusecaches) {
        this.jarFileURLConnection.setDefaultUseCaches(defaultusecaches);
    }

    public boolean getDefaultUseCaches() {
        return this.jarFileURLConnection.getDefaultUseCaches();
    }
}
