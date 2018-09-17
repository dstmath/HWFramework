package sun.net.www.protocol.https;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.security.Permission;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;

public class HttpsURLConnectionImpl extends HttpsURLConnection {
    protected DelegateHttpsURLConnection delegate;

    HttpsURLConnectionImpl(URL u, Handler handler) throws IOException {
        this(u, null, handler);
    }

    HttpsURLConnectionImpl(URL u, Proxy p, Handler handler) throws IOException {
        super(u);
        this.delegate = new DelegateHttpsURLConnection(this.url, p, handler, this);
    }

    protected HttpsURLConnectionImpl(URL u) throws IOException {
        super(u);
    }

    protected void setNewClient(URL url) throws IOException {
        this.delegate.setNewClient(url, false);
    }

    protected void setNewClient(URL url, boolean useCache) throws IOException {
        this.delegate.setNewClient(url, useCache);
    }

    protected void setProxiedClient(URL url, String proxyHost, int proxyPort) throws IOException {
        this.delegate.setProxiedClient(url, proxyHost, proxyPort);
    }

    protected void setProxiedClient(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        this.delegate.setProxiedClient(url, proxyHost, proxyPort, useCache);
    }

    public void connect() throws IOException {
        this.delegate.connect();
    }

    protected boolean isConnected() {
        return this.delegate.isConnected();
    }

    protected void setConnected(boolean conn) {
        this.delegate.setConnected(conn);
    }

    public String getCipherSuite() {
        return this.delegate.getCipherSuite();
    }

    public Certificate[] getLocalCertificates() {
        return this.delegate.getLocalCertificates();
    }

    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        return this.delegate.getServerCertificates();
    }

    public X509Certificate[] getServerCertificateChain() {
        try {
            return this.delegate.getServerCertificateChain();
        } catch (SSLPeerUnverifiedException e) {
            return null;
        }
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return this.delegate.getPeerPrincipal();
    }

    public Principal getLocalPrincipal() {
        return this.delegate.getLocalPrincipal();
    }

    public synchronized OutputStream getOutputStream() throws IOException {
        return this.delegate.getOutputStream();
    }

    public synchronized InputStream getInputStream() throws IOException {
        return this.delegate.getInputStream();
    }

    public InputStream getErrorStream() {
        return this.delegate.getErrorStream();
    }

    public void disconnect() {
        this.delegate.disconnect();
    }

    public boolean usingProxy() {
        return this.delegate.usingProxy();
    }

    public Map<String, List<String>> getHeaderFields() {
        return this.delegate.getHeaderFields();
    }

    public String getHeaderField(String name) {
        return this.delegate.getHeaderField(name);
    }

    public String getHeaderField(int n) {
        return this.delegate.getHeaderField(n);
    }

    public String getHeaderFieldKey(int n) {
        return this.delegate.getHeaderFieldKey(n);
    }

    public void setRequestProperty(String key, String value) {
        this.delegate.setRequestProperty(key, value);
    }

    public void addRequestProperty(String key, String value) {
        this.delegate.addRequestProperty(key, value);
    }

    public int getResponseCode() throws IOException {
        return this.delegate.getResponseCode();
    }

    public String getRequestProperty(String key) {
        return this.delegate.getRequestProperty(key);
    }

    public Map<String, List<String>> getRequestProperties() {
        return this.delegate.getRequestProperties();
    }

    public void setInstanceFollowRedirects(boolean shouldFollow) {
        this.delegate.setInstanceFollowRedirects(shouldFollow);
    }

    public boolean getInstanceFollowRedirects() {
        return this.delegate.getInstanceFollowRedirects();
    }

    public void setRequestMethod(String method) throws ProtocolException {
        this.delegate.setRequestMethod(method);
    }

    public String getRequestMethod() {
        return this.delegate.getRequestMethod();
    }

    public String getResponseMessage() throws IOException {
        return this.delegate.getResponseMessage();
    }

    public long getHeaderFieldDate(String name, long Default) {
        return this.delegate.getHeaderFieldDate(name, Default);
    }

    public Permission getPermission() throws IOException {
        return this.delegate.getPermission();
    }

    public URL getURL() {
        return this.delegate.getURL();
    }

    public int getContentLength() {
        return this.delegate.getContentLength();
    }

    public long getContentLengthLong() {
        return this.delegate.getContentLengthLong();
    }

    public String getContentType() {
        return this.delegate.getContentType();
    }

    public String getContentEncoding() {
        return this.delegate.getContentEncoding();
    }

    public long getExpiration() {
        return this.delegate.getExpiration();
    }

    public long getDate() {
        return this.delegate.getDate();
    }

    public long getLastModified() {
        return this.delegate.getLastModified();
    }

    public int getHeaderFieldInt(String name, int Default) {
        return this.delegate.getHeaderFieldInt(name, Default);
    }

    public long getHeaderFieldLong(String name, long Default) {
        return this.delegate.getHeaderFieldLong(name, Default);
    }

    public Object getContent() throws IOException {
        return this.delegate.getContent();
    }

    public Object getContent(Class[] classes) throws IOException {
        return this.delegate.getContent(classes);
    }

    public String toString() {
        return this.delegate.toString();
    }

    public void setDoInput(boolean doinput) {
        this.delegate.setDoInput(doinput);
    }

    public boolean getDoInput() {
        return this.delegate.getDoInput();
    }

    public void setDoOutput(boolean dooutput) {
        this.delegate.setDoOutput(dooutput);
    }

    public boolean getDoOutput() {
        return this.delegate.getDoOutput();
    }

    public void setAllowUserInteraction(boolean allowuserinteraction) {
        this.delegate.setAllowUserInteraction(allowuserinteraction);
    }

    public boolean getAllowUserInteraction() {
        return this.delegate.getAllowUserInteraction();
    }

    public void setUseCaches(boolean usecaches) {
        this.delegate.setUseCaches(usecaches);
    }

    public boolean getUseCaches() {
        return this.delegate.getUseCaches();
    }

    public void setIfModifiedSince(long ifmodifiedsince) {
        this.delegate.setIfModifiedSince(ifmodifiedsince);
    }

    public long getIfModifiedSince() {
        return this.delegate.getIfModifiedSince();
    }

    public boolean getDefaultUseCaches() {
        return this.delegate.getDefaultUseCaches();
    }

    public void setDefaultUseCaches(boolean defaultusecaches) {
        this.delegate.setDefaultUseCaches(defaultusecaches);
    }

    protected void finalize() throws Throwable {
        this.delegate.dispose();
    }

    public boolean equals(Object obj) {
        return this.delegate.equals(obj);
    }

    public int hashCode() {
        return this.delegate.hashCode();
    }

    public void setConnectTimeout(int timeout) {
        this.delegate.setConnectTimeout(timeout);
    }

    public int getConnectTimeout() {
        return this.delegate.getConnectTimeout();
    }

    public void setReadTimeout(int timeout) {
        this.delegate.setReadTimeout(timeout);
    }

    public int getReadTimeout() {
        return this.delegate.getReadTimeout();
    }

    public void setFixedLengthStreamingMode(int contentLength) {
        this.delegate.setFixedLengthStreamingMode(contentLength);
    }

    public void setFixedLengthStreamingMode(long contentLength) {
        this.delegate.setFixedLengthStreamingMode(contentLength);
    }

    public void setChunkedStreamingMode(int chunklen) {
        this.delegate.setChunkedStreamingMode(chunklen);
    }
}
