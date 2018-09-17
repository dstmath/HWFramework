package sun.net.www.protocol.https;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import sun.net.www.http.HttpClient;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.net.www.protocol.http.HttpURLConnection.TunnelState;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.SSLSocketImpl;
import sun.security.util.HostnameChecker;
import sun.util.logging.PlatformLogger;

final class HttpsClient extends HttpClient implements HandshakeCompletedListener {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final String defaultHVCanonicalName = "javax.net.ssl.DefaultHostnameVerifier";
    private static final int httpsPortNumber = 443;
    private HostnameVerifier hv;
    private SSLSession session;
    private SSLSocketFactory sslSocketFactory;

    /* renamed from: sun.net.www.protocol.https.HttpsClient.1 */
    static class AnonymousClass1 implements PrivilegedExceptionAction<InetSocketAddress> {
        final /* synthetic */ String val$phost;
        final /* synthetic */ int val$pport;

        AnonymousClass1(String val$phost, int val$pport) {
            this.val$phost = val$phost;
            this.val$pport = val$pport;
        }

        public InetSocketAddress run() {
            return new InetSocketAddress(this.val$phost, this.val$pport);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.protocol.https.HttpsClient.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.protocol.https.HttpsClient.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.protocol.https.HttpsClient.<clinit>():void");
    }

    protected int getDefaultPort() {
        return httpsPortNumber;
    }

    private String[] getCipherSuites() {
        String cipherString = (String) AccessController.doPrivileged(new GetPropertyAction("https.cipherSuites"));
        if (cipherString == null || "".equals(cipherString)) {
            return null;
        }
        Vector<String> v = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(cipherString, ",");
        while (tokenizer.hasMoreTokens()) {
            v.addElement(tokenizer.nextToken());
        }
        String[] ciphers = new String[v.size()];
        for (int i = 0; i < ciphers.length; i++) {
            ciphers[i] = (String) v.elementAt(i);
        }
        return ciphers;
    }

    private String[] getProtocols() {
        String protocolString = (String) AccessController.doPrivileged(new GetPropertyAction("https.protocols"));
        if (protocolString == null || "".equals(protocolString)) {
            return null;
        }
        Vector<String> v = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(protocolString, ",");
        while (tokenizer.hasMoreTokens()) {
            v.addElement(tokenizer.nextToken());
        }
        String[] protocols = new String[v.size()];
        for (int i = 0; i < protocols.length; i++) {
            protocols[i] = (String) v.elementAt(i);
        }
        return protocols;
    }

    private String getUserAgent() {
        String userAgent = (String) AccessController.doPrivileged(new GetPropertyAction("https.agent"));
        if (userAgent == null || userAgent.length() == 0) {
            return "JSSE";
        }
        return userAgent;
    }

    private static Proxy newHttpProxy(String proxyHost, int proxyPort) {
        SocketAddress saddr = null;
        String phost = proxyHost;
        try {
            saddr = (InetSocketAddress) AccessController.doPrivileged(new AnonymousClass1(proxyHost, proxyPort < 0 ? httpsPortNumber : proxyPort));
        } catch (PrivilegedActionException e) {
        }
        return new Proxy(Type.HTTP, saddr);
    }

    private HttpsClient(SSLSocketFactory sf, URL url) throws IOException {
        this(sf, url, (String) null, -1);
    }

    HttpsClient(SSLSocketFactory sf, URL url, String proxyHost, int proxyPort) throws IOException {
        this(sf, url, proxyHost, proxyPort, -1);
    }

    HttpsClient(SSLSocketFactory sf, URL url, String proxyHost, int proxyPort, int connectTimeout) throws IOException {
        Proxy proxy = null;
        if (proxyHost != null) {
            proxy = newHttpProxy(proxyHost, proxyPort);
        }
        this(sf, url, proxy, connectTimeout);
    }

    HttpsClient(SSLSocketFactory sf, URL url, Proxy proxy, int connectTimeout) throws IOException {
        this.proxy = proxy;
        setSSLSocketFactory(sf);
        this.proxyDisabled = true;
        this.host = url.getHost();
        this.url = url;
        this.port = url.getPort();
        if (this.port == -1) {
            this.port = getDefaultPort();
        }
        setConnectTimeout(connectTimeout);
        openServer();
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, HttpURLConnection httpuc) throws IOException {
        return New(sf, url, hv, true, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, boolean useCache, HttpURLConnection httpuc) throws IOException {
        return New(sf, url, hv, (String) null, -1, useCache, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, String proxyHost, int proxyPort, HttpURLConnection httpuc) throws IOException {
        return New(sf, url, hv, proxyHost, proxyPort, true, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, String proxyHost, int proxyPort, boolean useCache, HttpURLConnection httpuc) throws IOException {
        return New(sf, url, hv, proxyHost, proxyPort, useCache, -1, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, String proxyHost, int proxyPort, boolean useCache, int connectTimeout, HttpURLConnection httpuc) throws IOException {
        Proxy proxy = null;
        if (proxyHost != null) {
            proxy = newHttpProxy(proxyHost, proxyPort);
        }
        return New(sf, url, hv, proxy, useCache, connectTimeout, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, Proxy p, boolean useCache, int connectTimeout, HttpURLConnection httpuc) throws IOException {
        if (p == null) {
            p = Proxy.NO_PROXY;
        }
        HttpClient httpClient = null;
        if (useCache) {
            httpClient = (HttpsClient) kac.get(url, sf);
            if (!(httpClient == null || httpuc == null || !httpuc.streaming() || httpuc.getRequestMethod() != "POST" || httpClient.available())) {
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
                            PlatformLogger logger = HttpURLConnection.getHttpLogger();
                            if (logger.isLoggable((int) PlatformLogger.FINEST)) {
                                logger.finest("KeepAlive stream retrieved from the cache, " + httpClient);
                            }
                        } else {
                            throw new AssertionError();
                        }
                    }
                }
            }
        }
        if (httpClient == null) {
            httpClient = new HttpsClient(sf, url, p, connectTimeout);
        } else {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (httpClient.proxy == Proxy.NO_PROXY || httpClient.proxy == null) {
                    security.checkConnect(InetAddress.getByName(url.getHost()).getHostAddress(), url.getPort());
                } else {
                    security.checkConnect(url.getHost(), url.getPort());
                }
            }
            httpClient.url = url;
        }
        httpClient.setHostnameVerifier(hv);
        return httpClient;
    }

    void setHostnameVerifier(HostnameVerifier hv) {
        this.hv = hv;
    }

    void setSSLSocketFactory(SSLSocketFactory sf) {
        this.sslSocketFactory = sf;
    }

    SSLSocketFactory getSSLSocketFactory() {
        return this.sslSocketFactory;
    }

    protected Socket createSocket() throws IOException {
        try {
            return this.sslSocketFactory.createSocket();
        } catch (SocketException se) {
            Throwable t = se.getCause();
            if (t != null && (t instanceof UnsupportedOperationException)) {
                return super.createSocket();
            }
            throw se;
        }
    }

    public boolean needsTunneling() {
        if (this.proxy == null || this.proxy.type() == Type.DIRECT || this.proxy.type() == Type.SOCKS) {
            return -assertionsDisabled;
        }
        return true;
    }

    public void afterConnect() throws IOException, UnknownHostException {
        if (isCachedConnection()) {
            this.session = ((SSLSocket) this.serverSocket).getSession();
            return;
        }
        SSLSocket s;
        SSLSocketFactory factory = this.sslSocketFactory;
        try {
            if (this.serverSocket instanceof SSLSocket) {
                s = (SSLSocket) this.serverSocket;
                if (s instanceof SSLSocketImpl) {
                    ((SSLSocketImpl) s).setHost(this.host);
                }
            } else {
                s = (SSLSocket) factory.createSocket(this.serverSocket, this.host, this.port, true);
            }
        } catch (IOException ex) {
            try {
                s = (SSLSocket) factory.createSocket(this.host, this.port);
            } catch (IOException e) {
                throw ex;
            }
        }
        String[] protocols = getProtocols();
        String[] ciphers = getCipherSuites();
        if (protocols != null) {
            s.setEnabledProtocols(protocols);
        }
        if (ciphers != null) {
            s.setEnabledCipherSuites(ciphers);
        }
        s.addHandshakeCompletedListener(this);
        boolean needToCheckSpoofing = true;
        String identification = s.getSSLParameters().getEndpointIdentificationAlgorithm();
        if (identification == null || identification.length() == 0) {
            boolean isDefaultHostnameVerifier = -assertionsDisabled;
            if (this.hv != null) {
                String canonicalName = this.hv.getClass().getCanonicalName();
                if (canonicalName != null && canonicalName.equalsIgnoreCase(defaultHVCanonicalName)) {
                    isDefaultHostnameVerifier = true;
                }
            } else {
                isDefaultHostnameVerifier = true;
            }
            if (isDefaultHostnameVerifier) {
                SSLParameters paramaters = s.getSSLParameters();
                paramaters.setEndpointIdentificationAlgorithm("HTTPS");
                s.setSSLParameters(paramaters);
                needToCheckSpoofing = -assertionsDisabled;
            }
        } else if (identification.equalsIgnoreCase("HTTPS")) {
            needToCheckSpoofing = -assertionsDisabled;
        }
        s.startHandshake();
        this.session = s.getSession();
        this.serverSocket = s;
        try {
            this.serverOutput = new PrintStream(new BufferedOutputStream(this.serverSocket.getOutputStream()), -assertionsDisabled, encoding);
            if (needToCheckSpoofing) {
                checkURLSpoofing(this.hv);
            }
        } catch (UnsupportedEncodingException e2) {
            throw new InternalError(encoding + " encoding not found");
        }
    }

    private void checkURLSpoofing(HostnameVerifier hostnameVerifier) throws IOException {
        String host = this.url.getHost();
        if (host != null && host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        String cipher = this.session.getCipherSuite();
        try {
            HostnameChecker checker = HostnameChecker.getInstance((byte) 1);
            if (!cipher.startsWith("TLS_KRB5")) {
                Certificate[] peerCerts = this.session.getPeerCertificates();
                if (peerCerts[0] instanceof X509Certificate) {
                    checker.match(host, peerCerts[0]);
                } else {
                    throw new SSLPeerUnverifiedException("");
                }
            } else if (!HostnameChecker.match(host, getPeerPrincipal())) {
                throw new SSLPeerUnverifiedException("Hostname checker failed for Kerberos");
            }
        } catch (SSLPeerUnverifiedException e) {
            if (cipher == null && cipher.indexOf("_anon_") != -1) {
                return;
            }
            if (hostnameVerifier != null || !hostnameVerifier.verify(host, this.session)) {
                this.serverSocket.close();
                this.session.invalidate();
                throw new IOException("HTTPS hostname wrong:  should be <" + this.url.getHost() + ">");
            }
        } catch (CertificateException e2) {
            if (cipher == null) {
            }
            if (hostnameVerifier != null) {
            }
            this.serverSocket.close();
            this.session.invalidate();
            throw new IOException("HTTPS hostname wrong:  should be <" + this.url.getHost() + ">");
        }
    }

    protected void putInKeepAliveCache() {
        if (!this.inCache) {
            this.inCache = true;
            kac.put(this.url, this.sslSocketFactory, this);
        } else if (!-assertionsDisabled) {
            throw new AssertionError((Object) "Duplicate put to keep alive cache");
        }
    }

    public void closeIdleConnection() {
        HttpClient http = kac.get(this.url, this.sslSocketFactory);
        if (http != null) {
            http.closeServer();
        }
    }

    String getCipherSuite() {
        return this.session.getCipherSuite();
    }

    public Certificate[] getLocalCertificates() {
        return this.session.getLocalCertificates();
    }

    Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        return this.session.getPeerCertificates();
    }

    javax.security.cert.X509Certificate[] getServerCertificateChain() throws SSLPeerUnverifiedException {
        return this.session.getPeerCertificateChain();
    }

    Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        try {
            return this.session.getPeerPrincipal();
        } catch (AbstractMethodError e) {
            return ((X509Certificate) this.session.getPeerCertificates()[0]).getSubjectX500Principal();
        }
    }

    Principal getLocalPrincipal() {
        try {
            return this.session.getLocalPrincipal();
        } catch (AbstractMethodError e) {
            Certificate[] certs = this.session.getLocalCertificates();
            if (certs != null) {
                return ((X509Certificate) certs[0]).getSubjectX500Principal();
            }
            return null;
        }
    }

    public void handshakeCompleted(HandshakeCompletedEvent event) {
        this.session = event.getSession();
    }

    public String getProxyHostUsed() {
        if (needsTunneling()) {
            return super.getProxyHostUsed();
        }
        return null;
    }

    public int getProxyPortUsed() {
        if (this.proxy == null || this.proxy.type() == Type.DIRECT || this.proxy.type() == Type.SOCKS) {
            return -1;
        }
        return ((InetSocketAddress) this.proxy.address()).getPort();
    }
}
