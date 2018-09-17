package sun.net.www.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.CacheRequest;
import java.net.CookieHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.regex.Pattern;
import sun.net.NetworkClient;
import sun.net.ProgressSource;
import sun.net.www.HeaderParser;
import sun.net.www.MessageHeader;
import sun.net.www.MeteredStream;
import sun.net.www.ParseUtil;
import sun.net.www.URLConnection;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.net.www.protocol.http.HttpURLConnection.TunnelState;
import sun.util.logging.PlatformLogger;

public class HttpClient extends NetworkClient {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int HTTP_CONTINUE = 100;
    static final int httpPortNumber = 80;
    protected static KeepAliveCache kac;
    private static boolean keepAliveProp;
    private static final PlatformLogger logger = null;
    private static boolean retryPostProp;
    private CacheRequest cacheRequest;
    protected boolean cachedHttpClient;
    private HttpCapture capture;
    boolean failedOnce;
    protected String host;
    private boolean ignoreContinue;
    protected boolean inCache;
    int keepAliveConnections;
    int keepAliveTimeout;
    volatile boolean keepingAlive;
    protected int port;
    PosterOutputStream poster;
    protected boolean proxyDisabled;
    MessageHeader requests;
    public boolean reuse;
    boolean streaming;
    protected URL url;
    public boolean usingProxy;

    /* renamed from: sun.net.www.http.HttpClient.1 */
    class AnonymousClass1 implements PrivilegedExceptionAction<Void> {
        final /* synthetic */ InetSocketAddress val$server;

        AnonymousClass1(InetSocketAddress val$server) {
            this.val$server = val$server;
        }

        public Void run() throws IOException {
            HttpClient.this.openServer(this.val$server.getHostString(), this.val$server.getPort());
            return null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.http.HttpClient.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.http.HttpClient.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.http.HttpClient.<clinit>():void");
    }

    protected int getDefaultPort() {
        return httpPortNumber;
    }

    private static int getDefaultPort(String proto) {
        if ("http".equalsIgnoreCase(proto)) {
            return httpPortNumber;
        }
        if ("https".equalsIgnoreCase(proto)) {
            return 443;
        }
        return -1;
    }

    private static void logFinest(String msg) {
        if (logger.isLoggable((int) PlatformLogger.FINEST)) {
            logger.finest(msg);
        }
    }

    @Deprecated
    public static synchronized void resetProperties() {
        synchronized (HttpClient.class) {
        }
    }

    int getKeepAliveTimeout() {
        return this.keepAliveTimeout;
    }

    public boolean getHttpKeepAliveSet() {
        return keepAliveProp;
    }

    protected HttpClient() {
        this.cachedHttpClient = -assertionsDisabled;
        this.poster = null;
        this.failedOnce = -assertionsDisabled;
        this.ignoreContinue = true;
        this.usingProxy = -assertionsDisabled;
        this.keepingAlive = -assertionsDisabled;
        this.keepAliveConnections = -1;
        this.keepAliveTimeout = 0;
        this.cacheRequest = null;
        this.reuse = -assertionsDisabled;
        this.capture = null;
    }

    private HttpClient(URL url) throws IOException {
        this(url, (String) null, -1, -assertionsDisabled);
    }

    protected HttpClient(URL url, boolean proxyDisabled) throws IOException {
        this(url, null, -1, proxyDisabled);
    }

    public HttpClient(URL url, String proxyHost, int proxyPort) throws IOException {
        this(url, proxyHost, proxyPort, -assertionsDisabled);
    }

    protected HttpClient(URL url, Proxy p, int to) throws IOException {
        this.cachedHttpClient = -assertionsDisabled;
        this.poster = null;
        this.failedOnce = -assertionsDisabled;
        this.ignoreContinue = true;
        this.usingProxy = -assertionsDisabled;
        this.keepingAlive = -assertionsDisabled;
        this.keepAliveConnections = -1;
        this.keepAliveTimeout = 0;
        this.cacheRequest = null;
        this.reuse = -assertionsDisabled;
        this.capture = null;
        if (p == null) {
            p = Proxy.NO_PROXY;
        }
        this.proxy = p;
        this.host = url.getHost();
        this.url = url;
        this.port = url.getPort();
        if (this.port == -1) {
            this.port = getDefaultPort();
        }
        setConnectTimeout(to);
        this.capture = HttpCapture.getCapture(url);
        openServer();
    }

    protected static Proxy newHttpProxy(String proxyHost, int proxyPort, String proto) {
        if (proxyHost == null || proto == null) {
            return Proxy.NO_PROXY;
        }
        int pport;
        if (proxyPort < 0) {
            pport = getDefaultPort(proto);
        } else {
            pport = proxyPort;
        }
        return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(proxyHost, pport));
    }

    private HttpClient(URL url, String proxyHost, int proxyPort, boolean proxyDisabled) throws IOException {
        Proxy proxy;
        if (proxyDisabled) {
            proxy = Proxy.NO_PROXY;
        } else {
            proxy = newHttpProxy(proxyHost, proxyPort, "http");
        }
        this(url, proxy, -1);
    }

    public HttpClient(URL url, String proxyHost, int proxyPort, boolean proxyDisabled, int to) throws IOException {
        Proxy proxy;
        if (proxyDisabled) {
            proxy = Proxy.NO_PROXY;
        } else {
            proxy = newHttpProxy(proxyHost, proxyPort, "http");
        }
        this(url, proxy, to);
    }

    public static HttpClient New(URL url) throws IOException {
        return New(url, Proxy.NO_PROXY, -1, true, null);
    }

    public static HttpClient New(URL url, boolean useCache) throws IOException {
        return New(url, Proxy.NO_PROXY, -1, useCache, null);
    }

    public static HttpClient New(URL url, Proxy p, int to, boolean useCache, HttpURLConnection httpuc) throws IOException {
        if (p == null) {
            p = Proxy.NO_PROXY;
        }
        HttpClient httpClient = null;
        if (useCache) {
            httpClient = kac.get(url, null);
            if (!(httpClient == null || httpuc == null || !httpuc.streaming() || httpuc.getRequestMethod() != "POST" || httpClient.available())) {
                httpClient.inCache = -assertionsDisabled;
                httpClient.closeServer();
                httpClient = null;
            }
            if (httpClient != null) {
                if ((httpClient.proxy == null || !httpClient.proxy.equals(p)) && !(httpClient.proxy == null && p == null)) {
                    synchronized (httpClient) {
                        httpClient.inCache = -assertionsDisabled;
                        httpClient.closeServer();
                    }
                    httpClient = null;
                } else {
                    synchronized (httpClient) {
                        httpClient.cachedHttpClient = true;
                        if (-assertionsDisabled || httpClient.inCache) {
                            httpClient.inCache = -assertionsDisabled;
                            if (httpuc != null && httpClient.needsTunneling()) {
                                httpuc.setTunnelState(TunnelState.TUNNELING);
                            }
                            logFinest("KeepAlive stream retrieved from the cache, " + httpClient);
                        } else {
                            throw new AssertionError();
                        }
                    }
                }
            }
        }
        if (httpClient == null) {
            return new HttpClient(url, p, to);
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (httpClient.proxy == Proxy.NO_PROXY || httpClient.proxy == null) {
                security.checkConnect(InetAddress.getByName(url.getHost()).getHostAddress(), url.getPort());
            } else {
                security.checkConnect(url.getHost(), url.getPort());
            }
        }
        httpClient.url = url;
        return httpClient;
    }

    public static HttpClient New(URL url, Proxy p, int to, HttpURLConnection httpuc) throws IOException {
        return New(url, p, to, true, httpuc);
    }

    public static HttpClient New(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        return New(url, newHttpProxy(proxyHost, proxyPort, "http"), -1, useCache, null);
    }

    public static HttpClient New(URL url, String proxyHost, int proxyPort, boolean useCache, int to, HttpURLConnection httpuc) throws IOException {
        return New(url, newHttpProxy(proxyHost, proxyPort, "http"), to, useCache, httpuc);
    }

    public void finished() {
        if (!this.reuse) {
            this.keepAliveConnections--;
            this.poster = null;
            if (this.keepAliveConnections <= 0 || !isKeepingAlive() || this.serverOutput.checkError()) {
                closeServer();
            } else {
                putInKeepAliveCache();
            }
        }
    }

    protected synchronized boolean available() {
        boolean available;
        available = true;
        try {
            int old = this.serverSocket.getSoTimeout();
            this.serverSocket.setSoTimeout(1);
            if (new BufferedInputStream(this.serverSocket.getInputStream()).read() == -1) {
                logFinest("HttpClient.available(): read returned -1: not available");
                available = -assertionsDisabled;
            }
            if (old != -1) {
                try {
                    this.serverSocket.setSoTimeout(old);
                } catch (IOException e) {
                    logFinest("HttpClient.available(): SocketException: not available");
                    available = -assertionsDisabled;
                }
            }
        } catch (SocketTimeoutException e2) {
            logFinest("HttpClient.available(): SocketTimeout: its available");
            if (-1 != -1) {
                this.serverSocket.setSoTimeout(-1);
            }
        } catch (Throwable th) {
            if (-1 != -1) {
                this.serverSocket.setSoTimeout(-1);
            }
        }
        return available;
    }

    protected synchronized void putInKeepAliveCache() {
        if (!this.inCache) {
            this.inCache = true;
            kac.put(this.url, null, this);
        } else if (!-assertionsDisabled) {
            throw new AssertionError((Object) "Duplicate put to keep alive cache");
        }
    }

    protected synchronized boolean isInKeepAliveCache() {
        return this.inCache;
    }

    public void closeIdleConnection() {
        HttpClient http = kac.get(this.url, null);
        if (http != null) {
            http.closeServer();
        }
    }

    public void openServer(String server, int port) throws IOException {
        this.serverSocket = doConnect(server, port);
        try {
            OutputStream out = this.serverSocket.getOutputStream();
            if (this.capture != null) {
                out = new HttpCaptureOutputStream(out, this.capture);
            }
            this.serverOutput = new PrintStream(new BufferedOutputStream(out), (boolean) -assertionsDisabled, encoding);
            this.serverSocket.setTcpNoDelay(true);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + " encoding not found");
        }
    }

    public boolean needsTunneling() {
        return -assertionsDisabled;
    }

    public synchronized boolean isCachedConnection() {
        return this.cachedHttpClient;
    }

    public void afterConnect() throws IOException, UnknownHostException {
    }

    private synchronized void privilegedOpenServer(InetSocketAddress server) throws IOException {
        try {
            AccessController.doPrivileged(new AnonymousClass1(server));
        } catch (PrivilegedActionException pae) {
            throw ((IOException) pae.getException());
        }
    }

    private void superOpenServer(String proxyHost, int proxyPort) throws IOException, UnknownHostException {
        super.openServer(proxyHost, proxyPort);
    }

    protected synchronized void openServer() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkConnect(this.host, this.port);
        }
        if (!this.keepingAlive) {
            if (this.url.getProtocol().equals("http") || this.url.getProtocol().equals("https")) {
                if (this.proxy == null || this.proxy.type() != Type.HTTP) {
                    openServer(this.host, this.port);
                    this.usingProxy = -assertionsDisabled;
                    return;
                }
                URLConnection.setProxiedHost(this.host);
                privilegedOpenServer((InetSocketAddress) this.proxy.address());
                this.usingProxy = true;
            } else if (this.proxy == null || this.proxy.type() != Type.HTTP) {
                super.openServer(this.host, this.port);
                this.usingProxy = -assertionsDisabled;
            } else {
                URLConnection.setProxiedHost(this.host);
                privilegedOpenServer((InetSocketAddress) this.proxy.address());
                this.usingProxy = true;
            }
        }
    }

    public String getURLFile() throws IOException {
        String fileName = this.url.getFile();
        if (fileName == null || fileName.length() == 0) {
            fileName = "/";
        }
        if (this.usingProxy && !this.proxyDisabled) {
            StringBuffer result = new StringBuffer((int) Pattern.CANON_EQ);
            result.append(this.url.getProtocol());
            result.append(":");
            if (this.url.getAuthority() != null && this.url.getAuthority().length() > 0) {
                result.append("//");
                result.append(this.url.getAuthority());
            }
            if (this.url.getPath() != null) {
                result.append(this.url.getPath());
            }
            if (this.url.getQuery() != null) {
                result.append('?');
                result.append(this.url.getQuery());
            }
            fileName = result.toString();
        }
        if (fileName.indexOf(10) == -1) {
            return fileName;
        }
        throw new MalformedURLException("Illegal character in URL");
    }

    @Deprecated
    public void writeRequests(MessageHeader head) {
        this.requests = head;
        this.requests.print(this.serverOutput);
        this.serverOutput.flush();
    }

    public void writeRequests(MessageHeader head, PosterOutputStream pos) throws IOException {
        this.requests = head;
        this.requests.print(this.serverOutput);
        this.poster = pos;
        if (this.poster != null) {
            this.poster.writeTo(this.serverOutput);
        }
        this.serverOutput.flush();
    }

    public void writeRequests(MessageHeader head, PosterOutputStream pos, boolean streaming) throws IOException {
        this.streaming = streaming;
        writeRequests(head, pos);
    }

    public boolean parseHTTP(MessageHeader responses, ProgressSource pi, HttpURLConnection httpuc) throws IOException {
        try {
            this.serverInput = this.serverSocket.getInputStream();
            if (this.capture != null) {
                this.serverInput = new HttpCaptureInputStream(this.serverInput, this.capture);
            }
            this.serverInput = new BufferedInputStream(this.serverInput);
            return parseHTTPHeader(responses, pi, httpuc);
        } catch (SocketTimeoutException stex) {
            if (this.ignoreContinue) {
                closeServer();
            }
            throw stex;
        } catch (IOException e) {
            closeServer();
            this.cachedHttpClient = -assertionsDisabled;
            if (!(this.failedOnce || this.requests == null)) {
                this.failedOnce = true;
                if (!getRequestMethod().equals("CONNECT") && (!httpuc.getRequestMethod().equals("POST") || (retryPostProp && !this.streaming))) {
                    openServer();
                    if (needsTunneling()) {
                        httpuc.doTunneling();
                    }
                    afterConnect();
                    writeRequests(this.requests, this.poster);
                    return parseHTTP(responses, pi, httpuc);
                }
            }
            throw e;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseHTTPHeader(MessageHeader responses, ProgressSource pi, HttpURLConnection httpuc) throws IOException {
        this.keepAliveConnections = -1;
        this.keepAliveTimeout = 0;
        byte[] b = new byte[8];
        int nread = 0;
        try {
            this.serverInput.mark(10);
            while (nread < 8) {
                int r = this.serverInput.read(b, nread, 8 - nread);
                if (r < 0) {
                    break;
                }
                nread += r;
            }
            String keep = null;
            boolean ret = (b[0] == 72 && b[1] == 84 && b[2] == 84 && b[3] == httpPortNumber && b[4] == 47 && b[5] == 49) ? b[6] == 46 ? true : -assertionsDisabled : -assertionsDisabled;
            this.serverInput.reset();
            if (ret) {
                responses.parseHeader(this.serverInput);
                CookieHandler cookieHandler = httpuc.getCookieHandler();
                if (cookieHandler != null) {
                    URI uri = ParseUtil.toURI(this.url);
                    if (uri != null) {
                        cookieHandler.put(uri, responses.getHeaders());
                    }
                }
                if (this.usingProxy) {
                    keep = responses.findValue("Proxy-Connection");
                }
                if (keep == null) {
                    keep = responses.findValue("Connection");
                }
                if (keep != null) {
                    if (keep.toLowerCase(Locale.US).equals("keep-alive")) {
                        HeaderParser headerParser = new HeaderParser(responses.findValue("Keep-Alive"));
                        if (headerParser != null) {
                            int i;
                            this.keepAliveConnections = headerParser.findInt("max", this.usingProxy ? 50 : 5);
                            String str = "timeout";
                            if (this.usingProxy) {
                                i = 60;
                            } else {
                                i = 5;
                            }
                            this.keepAliveTimeout = headerParser.findInt(str, i);
                        }
                    }
                }
                if (b[7] != 48) {
                    if (keep != null) {
                        this.keepAliveConnections = 1;
                    } else {
                        this.keepAliveConnections = 5;
                    }
                }
            } else if (nread != 8) {
                if (!(this.failedOnce || this.requests == null)) {
                    this.failedOnce = true;
                    if (!getRequestMethod().equals("CONNECT") && (!httpuc.getRequestMethod().equals("POST") || (retryPostProp && !this.streaming))) {
                        closeServer();
                        this.cachedHttpClient = -assertionsDisabled;
                        openServer();
                        if (needsTunneling()) {
                            httpuc.doTunneling();
                        }
                        afterConnect();
                        writeRequests(this.requests, this.poster);
                        return parseHTTP(responses, pi, httpuc);
                    }
                }
                throw new SocketException("Unexpected end of file from server");
            } else {
                responses.set("Content-type", "unknown/unknown");
            }
            int code = -1;
            try {
                String resp = responses.getValue(0);
                int ind = resp.indexOf(32);
                while (resp.charAt(ind) == ' ') {
                    ind++;
                }
                code = Integer.parseInt(resp.substring(ind, ind + 3));
            } catch (Exception e) {
            }
            if (code == HTTP_CONTINUE && this.ignoreContinue) {
                responses.reset();
                return parseHTTPHeader(responses, pi, httpuc);
            }
            long cl = -1;
            String te = responses.findValue("Transfer-Encoding");
            if (te != null) {
                if (te.equalsIgnoreCase("chunked")) {
                    this.serverInput = new ChunkedInputStream(this.serverInput, this, responses);
                    if (this.keepAliveConnections <= 1) {
                        this.keepAliveConnections = 1;
                        this.keepingAlive = -assertionsDisabled;
                    } else {
                        this.keepingAlive = true;
                    }
                    this.failedOnce = -assertionsDisabled;
                    if (cl <= 0) {
                        if (pi != null) {
                            pi.setContentType(responses.findValue("content-type"));
                        }
                        if (isKeepingAlive()) {
                            this.serverInput = new MeteredStream(this.serverInput, pi, cl);
                        } else {
                            logFinest("KeepAlive stream used: " + this.url);
                            this.serverInput = new KeepAliveStream(this.serverInput, pi, cl, this);
                            this.failedOnce = -assertionsDisabled;
                        }
                    } else if (cl != -1) {
                        if (pi != null) {
                            pi.setContentType(responses.findValue("content-type"));
                            this.serverInput = new MeteredStream(this.serverInput, pi, cl);
                        }
                    } else if (pi != null) {
                        pi.finishTracking();
                    }
                    return ret;
                }
            }
            String cls = responses.findValue("content-length");
            if (cls != null) {
                try {
                    cl = Long.parseLong(cls);
                } catch (NumberFormatException e2) {
                    cl = -1;
                }
            }
            String requestLine = this.requests.getKey(0);
            if (requestLine != null) {
            }
            if (code != java.net.HttpURLConnection.HTTP_NOT_MODIFIED) {
                if (code == java.net.HttpURLConnection.HTTP_NO_CONTENT) {
                }
                if (this.keepAliveConnections > 1 || !(cl >= 0 || code == java.net.HttpURLConnection.HTTP_NOT_MODIFIED || code == java.net.HttpURLConnection.HTTP_NO_CONTENT)) {
                    if (this.keepingAlive) {
                        this.keepingAlive = -assertionsDisabled;
                    }
                    if (cl <= 0) {
                        if (pi != null) {
                            pi.setContentType(responses.findValue("content-type"));
                        }
                        if (isKeepingAlive()) {
                            this.serverInput = new MeteredStream(this.serverInput, pi, cl);
                        } else {
                            logFinest("KeepAlive stream used: " + this.url);
                            this.serverInput = new KeepAliveStream(this.serverInput, pi, cl, this);
                            this.failedOnce = -assertionsDisabled;
                        }
                    } else if (cl != -1) {
                        if (pi != null) {
                            pi.finishTracking();
                        }
                    } else if (pi != null) {
                        pi.setContentType(responses.findValue("content-type"));
                        this.serverInput = new MeteredStream(this.serverInput, pi, cl);
                    }
                    return ret;
                }
                this.keepingAlive = true;
                this.failedOnce = -assertionsDisabled;
                if (cl <= 0) {
                    if (pi != null) {
                        pi.setContentType(responses.findValue("content-type"));
                    }
                    if (isKeepingAlive()) {
                        logFinest("KeepAlive stream used: " + this.url);
                        this.serverInput = new KeepAliveStream(this.serverInput, pi, cl, this);
                        this.failedOnce = -assertionsDisabled;
                    } else {
                        this.serverInput = new MeteredStream(this.serverInput, pi, cl);
                    }
                } else if (cl != -1) {
                    if (pi != null) {
                        pi.setContentType(responses.findValue("content-type"));
                        this.serverInput = new MeteredStream(this.serverInput, pi, cl);
                    }
                } else if (pi != null) {
                    pi.finishTracking();
                }
                return ret;
            }
            cl = 0;
            if (this.keepAliveConnections > 1) {
            }
            if (this.keepingAlive) {
                this.keepingAlive = -assertionsDisabled;
            }
            if (cl <= 0) {
                if (pi != null) {
                    pi.setContentType(responses.findValue("content-type"));
                }
                if (isKeepingAlive()) {
                    this.serverInput = new MeteredStream(this.serverInput, pi, cl);
                } else {
                    logFinest("KeepAlive stream used: " + this.url);
                    this.serverInput = new KeepAliveStream(this.serverInput, pi, cl, this);
                    this.failedOnce = -assertionsDisabled;
                }
            } else if (cl != -1) {
                if (pi != null) {
                    pi.finishTracking();
                }
            } else if (pi != null) {
                pi.setContentType(responses.findValue("content-type"));
                this.serverInput = new MeteredStream(this.serverInput, pi, cl);
            }
            return ret;
        } catch (IOException e3) {
            throw e3;
        }
    }

    public synchronized InputStream getInputStream() {
        return this.serverInput;
    }

    public OutputStream getOutputStream() {
        return this.serverOutput;
    }

    public String toString() {
        return getClass().getName() + "(" + this.url + ")";
    }

    public final boolean isKeepingAlive() {
        return getHttpKeepAliveSet() ? this.keepingAlive : -assertionsDisabled;
    }

    public void setCacheRequest(CacheRequest cacheRequest) {
        this.cacheRequest = cacheRequest;
    }

    CacheRequest getCacheRequest() {
        return this.cacheRequest;
    }

    String getRequestMethod() {
        if (this.requests != null) {
            String requestLine = this.requests.getKey(0);
            if (requestLine != null) {
                return requestLine.split("\\s+")[0];
            }
        }
        return "";
    }

    protected void finalize() throws Throwable {
    }

    public void setDoNotRetry(boolean value) {
        this.failedOnce = value;
    }

    public void setIgnoreContinue(boolean value) {
        this.ignoreContinue = value;
    }

    public void closeServer() {
        try {
            this.keepingAlive = -assertionsDisabled;
            this.serverSocket.close();
        } catch (Exception e) {
        }
    }

    public String getProxyHostUsed() {
        if (this.usingProxy) {
            return ((InetSocketAddress) this.proxy.address()).getHostString();
        }
        return null;
    }

    public int getProxyPortUsed() {
        if (this.usingProxy) {
            return ((InetSocketAddress) this.proxy.address()).getPort();
        }
        return -1;
    }
}
