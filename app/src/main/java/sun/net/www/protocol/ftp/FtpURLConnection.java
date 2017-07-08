package sun.net.www.protocol.ftp;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketPermission;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;
import libcore.net.NetworkSecurityPolicy;
import sun.net.ProgressMonitor;
import sun.net.ProgressSource;
import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpLoginException;
import sun.net.ftp.FtpProtocolException;
import sun.net.www.MessageHeader;
import sun.net.www.MeteredStream;
import sun.net.www.ParseUtil;
import sun.net.www.URLConnection;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.security.action.GetPropertyAction;
import sun.security.util.SecurityConstants;

public class FtpURLConnection extends URLConnection {
    static final int ASCII = 1;
    static final int BIN = 2;
    static final int DIR = 3;
    static final int NONE = 0;
    private int connectTimeout;
    String filename;
    FtpClient ftp;
    String fullpath;
    String host;
    HttpURLConnection http;
    private Proxy instProxy;
    InputStream is;
    OutputStream os;
    String password;
    String pathname;
    Permission permission;
    int port;
    private int readTimeout;
    int type;
    String user;

    protected class FtpInputStream extends FilterInputStream {
        FtpClient ftp;

        FtpInputStream(FtpClient cl, InputStream fd) {
            super(new BufferedInputStream(fd));
            this.ftp = cl;
        }

        public void close() throws IOException {
            super.close();
            if (this.ftp != null) {
                this.ftp.close();
            }
        }
    }

    protected class FtpOutputStream extends FilterOutputStream {
        FtpClient ftp;

        FtpOutputStream(FtpClient cl, OutputStream fd) {
            super(fd);
            this.ftp = cl;
        }

        public void close() throws IOException {
            super.close();
            if (this.ftp != null) {
                this.ftp.close();
            }
        }
    }

    public FtpURLConnection(URL url) throws IOException {
        this(url, null);
    }

    FtpURLConnection(URL url, Proxy p) throws IOException {
        super(url);
        this.http = null;
        this.is = null;
        this.os = null;
        this.ftp = null;
        this.type = 0;
        this.connectTimeout = -1;
        this.readTimeout = -1;
        this.instProxy = p;
        this.host = url.getHost();
        this.port = url.getPort();
        String userInfo = url.getUserInfo();
        if (!NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted()) {
            throw new IOException("Cleartext traffic not permitted: " + url.getProtocol() + "://" + this.host + (url.getPort() >= 0 ? ":" + url.getPort() : ""));
        } else if (userInfo != null) {
            int delimiter = userInfo.indexOf(58);
            if (delimiter == -1) {
                this.user = ParseUtil.decode(userInfo);
                this.password = null;
                return;
            }
            int delimiter2 = delimiter + ASCII;
            this.user = ParseUtil.decode(userInfo.substring(0, delimiter));
            this.password = ParseUtil.decode(userInfo.substring(delimiter2));
        }
    }

    private void setTimeouts() {
        if (this.ftp != null) {
            if (this.connectTimeout >= 0) {
                this.ftp.setConnectTimeout(this.connectTimeout);
            }
            if (this.readTimeout >= 0) {
                this.ftp.setReadTimeout(this.readTimeout);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void connect() throws IOException {
        if (!this.connected) {
            Proxy proxy = null;
            if (this.instProxy == null) {
                ProxySelector sel = (ProxySelector) AccessController.doPrivileged(new PrivilegedAction<ProxySelector>() {
                    public ProxySelector run() {
                        return ProxySelector.getDefault();
                    }
                });
                if (sel != null) {
                    URI uri = ParseUtil.toURI(this.url);
                    for (Proxy proxy2 : sel.select(uri)) {
                        if (!(proxy2 == null || proxy2 == Proxy.NO_PROXY)) {
                            if (proxy2.type() == Type.SOCKS) {
                                break;
                            } else if (proxy2.type() == Type.HTTP && (proxy2.address() instanceof InetSocketAddress)) {
                                InetSocketAddress paddr = (InetSocketAddress) proxy2.address();
                                try {
                                    this.http = new HttpURLConnection(this.url, proxy2);
                                    this.http.setDoInput(getDoInput());
                                    this.http.setDoOutput(getDoOutput());
                                    if (this.connectTimeout >= 0) {
                                        this.http.setConnectTimeout(this.connectTimeout);
                                    }
                                    if (this.readTimeout >= 0) {
                                        this.http.setReadTimeout(this.readTimeout);
                                    }
                                    this.http.connect();
                                    this.connected = true;
                                    return;
                                } catch (FtpProtocolException e) {
                                    this.ftp.close();
                                    throw new FtpLoginException("Invalid username/password");
                                } catch (UnknownHostException e2) {
                                    throw e2;
                                } catch (Throwable fe) {
                                    throw new IOException(fe);
                                } catch (IOException ioe) {
                                    sel.connectFailed(uri, paddr, ioe);
                                    this.http = null;
                                }
                            } else {
                                sel.connectFailed(uri, proxy2.address(), new IOException("Wrong proxy type"));
                            }
                        }
                    }
                }
            } else {
                proxy2 = this.instProxy;
                if (proxy2.type() == Type.HTTP) {
                    this.http = new HttpURLConnection(this.url, this.instProxy);
                    this.http.setDoInput(getDoInput());
                    this.http.setDoOutput(getDoOutput());
                    if (this.connectTimeout >= 0) {
                        this.http.setConnectTimeout(this.connectTimeout);
                    }
                    if (this.readTimeout >= 0) {
                        this.http.setReadTimeout(this.readTimeout);
                    }
                    this.http.connect();
                    this.connected = true;
                    return;
                }
            }
            if (this.user == null) {
                this.user = "anonymous";
                this.password = (String) AccessController.doPrivileged(new GetPropertyAction("ftp.protocol.user", "Java" + ((String) AccessController.doPrivileged(new GetPropertyAction("java.version"))) + "@"));
            }
            this.ftp = FtpClient.create();
            if (proxy2 != null) {
                this.ftp.setProxy(proxy2);
            }
            setTimeouts();
            if (this.port != -1) {
                this.ftp.connect(new InetSocketAddress(this.host, this.port));
            } else {
                this.ftp.connect(new InetSocketAddress(this.host, FtpClient.defaultPort()));
            }
            this.ftp.login(this.user, this.password.toCharArray());
            this.connected = true;
        }
    }

    private void decodePath(String path) {
        int i = path.indexOf(";type=");
        if (i >= 0) {
            String s1 = path.substring(i + 6, path.length());
            if ("i".equalsIgnoreCase(s1)) {
                this.type = BIN;
            }
            if ("a".equalsIgnoreCase(s1)) {
                this.type = ASCII;
            }
            if ("d".equalsIgnoreCase(s1)) {
                this.type = DIR;
            }
            path = path.substring(0, i);
        }
        if (path != null && path.length() > ASCII && path.charAt(0) == '/') {
            path = path.substring(ASCII);
        }
        if (path == null || path.length() == 0) {
            path = "./";
        }
        if (path.endsWith("/")) {
            this.pathname = path.substring(0, path.length() - 1);
            this.filename = null;
        } else {
            i = path.lastIndexOf(47);
            if (i > 0) {
                this.filename = path.substring(i + ASCII, path.length());
                this.filename = ParseUtil.decode(this.filename);
                this.pathname = path.substring(0, i);
            } else {
                this.filename = ParseUtil.decode(path);
                this.pathname = null;
            }
        }
        if (this.pathname != null) {
            String str;
            StringBuilder append = new StringBuilder().append(this.pathname).append("/");
            if (this.filename != null) {
                str = this.filename;
            } else {
                str = "";
            }
            this.fullpath = append.append(str).toString();
            return;
        }
        this.fullpath = this.filename;
    }

    private void cd(String path) throws FtpProtocolException, IOException {
        if (path != null && !path.isEmpty()) {
            if (path.indexOf(47) == -1) {
                this.ftp.changeDirectory(ParseUtil.decode(path));
                return;
            }
            StringTokenizer token = new StringTokenizer(path, "/");
            while (token.hasMoreTokens()) {
                this.ftp.changeDirectory(ParseUtil.decode(token.nextToken()));
            }
        }
    }

    public InputStream getInputStream() throws IOException {
        if (!this.connected) {
            connect();
        }
        if (this.http != null) {
            return this.http.getInputStream();
        }
        if (this.os != null) {
            throw new IOException("Already opened for output");
        } else if (this.is != null) {
            return this.is;
        } else {
            MessageHeader msgh = new MessageHeader();
            try {
                decodePath(this.url.getPath());
                if (this.filename == null || this.type == DIR) {
                    this.ftp.setAsciiType();
                    cd(this.pathname);
                    if (this.filename == null) {
                        this.is = new FtpInputStream(this.ftp, this.ftp.list(null));
                    } else {
                        this.is = new FtpInputStream(this.ftp, this.ftp.nameList(this.filename));
                    }
                } else {
                    if (this.type == ASCII) {
                        this.ftp.setAsciiType();
                    } else {
                        this.ftp.setBinaryType();
                    }
                    cd(this.pathname);
                    this.is = new FtpInputStream(this.ftp, this.ftp.getFileStream(this.filename));
                }
                try {
                    long l = this.ftp.getLastTransferSize();
                    msgh.add("content-length", Long.toString(l));
                    if (l > 0) {
                        ProgressSource progressSource = null;
                        if (ProgressMonitor.getDefault().shouldMeterInput(this.url, "GET")) {
                            progressSource = new ProgressSource(this.url, "GET", l);
                            progressSource.beginTracking();
                        }
                        this.is = new MeteredStream(this.is, progressSource, l);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (false) {
                    msgh.add("content-type", "text/plain");
                    msgh.add("access-type", "directory");
                } else {
                    msgh.add("access-type", "file");
                    String ftype = java.net.URLConnection.guessContentTypeFromName(this.fullpath);
                    if (ftype == null && this.is.markSupported()) {
                        ftype = java.net.URLConnection.guessContentTypeFromStream(this.is);
                    }
                    if (ftype != null) {
                        msgh.add("content-type", ftype);
                    }
                }
            } catch (FileNotFoundException e2) {
                try {
                    cd(this.fullpath);
                    this.ftp.setAsciiType();
                    this.is = new FtpInputStream(this.ftp, this.ftp.list(null));
                    msgh.add("content-type", "text/plain");
                    msgh.add("access-type", "directory");
                } catch (IOException e3) {
                    throw new FileNotFoundException(this.fullpath);
                } catch (FtpProtocolException e4) {
                    throw new FileNotFoundException(this.fullpath);
                }
            } catch (Throwable ftpe) {
                throw new IOException(ftpe);
            }
            setProperties(msgh);
            return this.is;
        }
    }

    public OutputStream getOutputStream() throws IOException {
        if (!this.connected) {
            connect();
        }
        if (this.http != null) {
            OutputStream out = this.http.getOutputStream();
            this.http.getInputStream();
            return out;
        } else if (this.is != null) {
            throw new IOException("Already opened for input");
        } else if (this.os != null) {
            return this.os;
        } else {
            decodePath(this.url.getPath());
            if (this.filename == null || this.filename.length() == 0) {
                throw new IOException("illegal filename for a PUT");
            }
            try {
                if (this.pathname != null) {
                    cd(this.pathname);
                }
                if (this.type == ASCII) {
                    this.ftp.setAsciiType();
                } else {
                    this.ftp.setBinaryType();
                }
                this.os = new FtpOutputStream(this.ftp, this.ftp.putFileStream(this.filename, false));
                return this.os;
            } catch (Throwable e) {
                throw new IOException(e);
            }
        }
    }

    String guessContentTypeFromFilename(String fname) {
        return java.net.URLConnection.guessContentTypeFromName(fname);
    }

    public Permission getPermission() {
        if (this.permission == null) {
            int urlport = this.url.getPort();
            if (urlport < 0) {
                urlport = FtpClient.defaultPort();
            }
            this.permission = new SocketPermission(this.host + ":" + urlport, SecurityConstants.SOCKET_CONNECT_ACTION);
        }
        return this.permission;
    }

    public void setRequestProperty(String key, String value) {
        super.setRequestProperty(key, value);
        if (!"type".equals(key)) {
            return;
        }
        if ("i".equalsIgnoreCase(value)) {
            this.type = BIN;
        } else if ("a".equalsIgnoreCase(value)) {
            this.type = ASCII;
        } else if ("d".equalsIgnoreCase(value)) {
            this.type = DIR;
        } else {
            throw new IllegalArgumentException("Value of '" + key + "' request property was '" + value + "' when it must be either 'i', 'a' or 'd'");
        }
    }

    public String getRequestProperty(String key) {
        String value = super.getRequestProperty(key);
        if (value != null || !"type".equals(key)) {
            return value;
        }
        if (this.type == ASCII) {
            return "a";
        }
        return this.type == DIR ? "d" : "i";
    }

    public void setConnectTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeouts can't be negative");
        }
        this.connectTimeout = timeout;
    }

    public int getConnectTimeout() {
        return this.connectTimeout < 0 ? 0 : this.connectTimeout;
    }

    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeouts can't be negative");
        }
        this.readTimeout = timeout;
    }

    public int getReadTimeout() {
        return this.readTimeout < 0 ? 0 : this.readTimeout;
    }
}
