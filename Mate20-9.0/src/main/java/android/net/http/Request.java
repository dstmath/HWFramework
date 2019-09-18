package android.net.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.RequestContent;

class Request {
    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    private static final String CONTENT_LENGTH_HEADER = "content-length";
    private static final String HOST_HEADER = "Host";
    private static RequestContent requestContentProcessor = new RequestContent();
    private int mBodyLength;
    private InputStream mBodyProvider;
    volatile boolean mCancelled = false;
    private final Object mClientResource = new Object();
    private Connection mConnection;
    EventHandler mEventHandler;
    int mFailCount = 0;
    HttpHost mHost;
    BasicHttpRequest mHttpRequest;
    private boolean mLoadingPaused = false;
    String mPath;
    HttpHost mProxyHost;
    private int mReceivedBytes = 0;

    Request(String method, HttpHost host, HttpHost proxyHost, String path, InputStream bodyProvider, int bodyLength, EventHandler eventHandler, Map<String, String> headers) {
        this.mEventHandler = eventHandler;
        this.mHost = host;
        this.mProxyHost = proxyHost;
        this.mPath = path;
        this.mBodyProvider = bodyProvider;
        this.mBodyLength = bodyLength;
        if (bodyProvider != null || HttpPost.METHOD_NAME.equalsIgnoreCase(method)) {
            this.mHttpRequest = new BasicHttpEntityEnclosingRequest(method, getUri());
            if (bodyProvider != null) {
                setBodyProvider(bodyProvider, bodyLength);
            }
        } else {
            this.mHttpRequest = new BasicHttpRequest(method, getUri());
        }
        addHeader("Host", getHostPort());
        addHeader(ACCEPT_ENCODING_HEADER, "gzip");
        addHeaders(headers);
    }

    /* access modifiers changed from: package-private */
    public synchronized void setLoadingPaused(boolean pause) {
        this.mLoadingPaused = pause;
        if (!this.mLoadingPaused) {
            notify();
        }
    }

    /* access modifiers changed from: package-private */
    public void setConnection(Connection connection) {
        this.mConnection = connection;
    }

    /* access modifiers changed from: package-private */
    public EventHandler getEventHandler() {
        return this.mEventHandler;
    }

    /* access modifiers changed from: package-private */
    public void addHeader(String name, String value) {
        if (name == null) {
            HttpLog.e("Null http header name");
            throw new NullPointerException("Null http header name");
        } else if (value == null || value.length() == 0) {
            String damage = "Null or empty value for header \"" + name + "\"";
            HttpLog.e(damage);
            throw new RuntimeException(damage);
        } else {
            this.mHttpRequest.addHeader(name, value);
        }
    }

    /* access modifiers changed from: package-private */
    public void addHeaders(Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendRequest(AndroidHttpClientConnection httpClientConnection) throws HttpException, IOException {
        if (!this.mCancelled) {
            requestContentProcessor.process(this.mHttpRequest, this.mConnection.getHttpContext());
            httpClientConnection.sendRequestHeader(this.mHttpRequest);
            if (this.mHttpRequest instanceof HttpEntityEnclosingRequest) {
                httpClientConnection.sendRequestEntity((HttpEntityEnclosingRequest) this.mHttpRequest);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x014c, code lost:
        if (r13 != null) goto L_0x014e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x014e, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        r15 = r13.read(r14, r2, r14.length - r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d7, code lost:
        if (r15 == -1) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00d9, code lost:
        r2 = r2 + r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00da, code lost:
        if (r10 == false) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00dc, code lost:
        r1.mReceivedBytes += r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00e2, code lost:
        if (r15 == -1) goto L_0x00ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00e4, code lost:
        if (r2 < r7) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ec, code lost:
        r1.mEventHandler.data(r14, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00f1, code lost:
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00f8, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00f9, code lost:
        r15 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00fb, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00fc, code lost:
        r15 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x00ff, code lost:
        r15 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0114, code lost:
        if (r13 == null) goto L_0x0156;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x012b, code lost:
        if (r13 != null) goto L_0x014e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x0148  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0112 A[EDGE_INSN: B:111:0x0112->B:80:0x0112 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x009d A[SYNTHETIC, Splitter:B:30:0x009d] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0126  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x013f A[Catch:{ EOFException -> 0x0138, IOException -> 0x011d, all -> 0x0117, all -> 0x0145 }] */
    public void readResponse(AndroidHttpClientConnection httpClientConnection) throws IOException, ParseException {
        StatusLine statusLine;
        int statusCode;
        InputStream nis;
        int len;
        ProtocolVersion v;
        AndroidHttpClientConnection androidHttpClientConnection = httpClientConnection;
        if (!this.mCancelled) {
            httpClientConnection.flush();
            Headers header = new Headers();
            while (true) {
                statusLine = androidHttpClientConnection.parseResponseHeader(header);
                statusCode = statusLine.getStatusCode();
                if (statusCode >= 200) {
                    break;
                }
                StatusLine statusLine2 = statusLine;
            }
            ProtocolVersion v2 = statusLine.getProtocolVersion();
            this.mEventHandler.status(v2.getMajor(), v2.getMinor(), statusCode, statusLine.getReasonPhrase());
            this.mEventHandler.headers(header);
            HttpEntity entity = null;
            boolean hasBody = canResponseHaveBody(this.mHttpRequest, statusCode);
            if (hasBody) {
                entity = androidHttpClientConnection.receiveResponseEntity(header);
            }
            HttpEntity entity2 = entity;
            boolean supportPartialContent = "bytes".equalsIgnoreCase(header.getAcceptRanges());
            if (entity2 != null) {
                InputStream is = entity2.getContent();
                Header contentEncoding = entity2.getContentEncoding();
                InputStream nis2 = null;
                byte[] buf = null;
                int count = 0;
                if (contentEncoding != null) {
                    try {
                        if (contentEncoding.getValue().equals("gzip")) {
                            nis = new GZIPInputStream(is);
                            nis2 = nis;
                            buf = this.mConnection.getBuf();
                            int lowWater = buf.length / 2;
                            int count2 = 0;
                            len = 0;
                            while (true) {
                                boolean hasBody2 = hasBody;
                                if (len != -1) {
                                    break;
                                }
                                try {
                                    synchronized (this) {
                                        while (this.mLoadingPaused) {
                                            try {
                                                wait();
                                            } catch (InterruptedException e) {
                                                InterruptedException interruptedException = e;
                                                StringBuilder sb = new StringBuilder();
                                                ProtocolVersion v3 = v2;
                                                sb.append("Interrupted exception whilst network thread paused at WebCore's request. ");
                                                sb.append(e.getMessage());
                                                HttpLog.e(sb.toString());
                                                v2 = v3;
                                            } catch (Throwable th) {
                                                th = th;
                                                throw th;
                                            }
                                        }
                                        v = v2;
                                    }
                                } catch (EOFException e2) {
                                    ProtocolVersion protocolVersion = v2;
                                    count = count2;
                                    if (count > 0) {
                                        this.mEventHandler.data(buf, count);
                                    }
                                } catch (IOException e3) {
                                    e = e3;
                                    ProtocolVersion protocolVersion2 = v2;
                                    count = count2;
                                    if (statusCode != 200 || statusCode == 206) {
                                        if (supportPartialContent && count > 0) {
                                            this.mEventHandler.data(buf, count);
                                        }
                                        throw e;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    ProtocolVersion protocolVersion3 = v2;
                                    int i = count2;
                                    if (nis2 != null) {
                                        nis2.close();
                                    }
                                    throw th;
                                }
                                hasBody = hasBody2;
                                v2 = v;
                            }
                        }
                    } catch (EOFException e4) {
                        boolean z = hasBody;
                        ProtocolVersion protocolVersion4 = v2;
                        if (count > 0) {
                        }
                    } catch (IOException e5) {
                        e = e5;
                        boolean z2 = hasBody;
                        ProtocolVersion protocolVersion5 = v2;
                        if (statusCode != 200) {
                        }
                        this.mEventHandler.data(buf, count);
                        throw e;
                    } catch (Throwable th3) {
                        th = th3;
                        boolean z3 = hasBody;
                        ProtocolVersion protocolVersion6 = v2;
                        if (nis2 != null) {
                        }
                        throw th;
                    }
                }
                nis = is;
                nis2 = nis;
                try {
                    buf = this.mConnection.getBuf();
                    int lowWater2 = buf.length / 2;
                    int count22 = 0;
                    len = 0;
                    while (true) {
                        boolean hasBody22 = hasBody;
                        if (len != -1) {
                        }
                        hasBody = hasBody22;
                        v2 = v;
                    }
                } catch (EOFException e6) {
                    boolean z4 = hasBody;
                    ProtocolVersion protocolVersion7 = v2;
                    if (count > 0) {
                    }
                } catch (IOException e7) {
                    e = e7;
                    boolean z5 = hasBody;
                    ProtocolVersion protocolVersion8 = v2;
                    if (statusCode != 200) {
                    }
                    this.mEventHandler.data(buf, count);
                    throw e;
                } catch (Throwable th4) {
                    th = th4;
                    if (nis2 != null) {
                    }
                    throw th;
                }
            } else {
                ProtocolVersion protocolVersion9 = v2;
            }
            this.mConnection.setCanPersist(entity2, statusLine.getProtocolVersion(), header.getConnectionType());
            this.mEventHandler.endData();
            complete();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void cancel() {
        this.mLoadingPaused = false;
        notify();
        this.mCancelled = true;
        if (this.mConnection != null) {
            this.mConnection.cancel();
        }
    }

    /* access modifiers changed from: package-private */
    public String getHostPort() {
        String myScheme = this.mHost.getSchemeName();
        int myPort = this.mHost.getPort();
        if ((myPort == 80 || !myScheme.equals(HttpHost.DEFAULT_SCHEME_NAME)) && (myPort == 443 || !myScheme.equals("https"))) {
            return this.mHost.getHostName();
        }
        return this.mHost.toHostString();
    }

    /* access modifiers changed from: package-private */
    public String getUri() {
        if (this.mProxyHost == null || this.mHost.getSchemeName().equals("https")) {
            return this.mPath;
        }
        return this.mHost.getSchemeName() + "://" + getHostPort() + this.mPath;
    }

    public String toString() {
        return this.mPath;
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        this.mHttpRequest.removeHeaders("content-length");
        if (this.mBodyProvider != null) {
            try {
                this.mBodyProvider.reset();
            } catch (IOException e) {
            }
            setBodyProvider(this.mBodyProvider, this.mBodyLength);
        }
        if (this.mReceivedBytes > 0) {
            this.mFailCount = 0;
            HttpLog.v("*** Request.reset() to range:" + this.mReceivedBytes);
            BasicHttpRequest basicHttpRequest = this.mHttpRequest;
            basicHttpRequest.setHeader("Range", "bytes=" + this.mReceivedBytes + "-");
        }
    }

    /* access modifiers changed from: package-private */
    public void waitUntilComplete() {
        synchronized (this.mClientResource) {
            try {
                this.mClientResource.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void complete() {
        synchronized (this.mClientResource) {
            this.mClientResource.notifyAll();
        }
    }

    private static boolean canResponseHaveBody(HttpRequest request, int status) {
        boolean z = false;
        if (HttpHead.METHOD_NAME.equalsIgnoreCase(request.getRequestLine().getMethod())) {
            return false;
        }
        if (!(status < 200 || status == 204 || status == 304)) {
            z = true;
        }
        return z;
    }

    private void setBodyProvider(InputStream bodyProvider, int bodyLength) {
        if (bodyProvider.markSupported()) {
            bodyProvider.mark(Integer.MAX_VALUE);
            ((BasicHttpEntityEnclosingRequest) this.mHttpRequest).setEntity(new InputStreamEntity(bodyProvider, (long) bodyLength));
            return;
        }
        throw new IllegalArgumentException("bodyProvider must support mark()");
    }

    public void handleSslErrorResponse(boolean proceed) {
        HttpsConnection connection = (HttpsConnection) this.mConnection;
        if (connection != null) {
            connection.restartConnection(proceed);
        }
    }

    /* access modifiers changed from: package-private */
    public void error(int errorId, String errorMessage) {
        this.mEventHandler.error(errorId, errorMessage);
    }
}
