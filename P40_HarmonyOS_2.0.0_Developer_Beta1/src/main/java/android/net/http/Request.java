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

public class Request {
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

    public Request(String method, HttpHost host, HttpHost proxyHost, String path, InputStream bodyProvider, int bodyLength, EventHandler eventHandler, Map<String, String> headers) {
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
            BasicHttpRequest basicHttpRequest = this.mHttpRequest;
            if (basicHttpRequest instanceof HttpEntityEnclosingRequest) {
                httpClientConnection.sendRequestEntity((HttpEntityEnclosingRequest) basicHttpRequest);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0159, code lost:
        if (r13 == null) goto L_0x0163;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x015b, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0137, code lost:
        if (r13 != null) goto L_0x015b;
     */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a0 A[SYNTHETIC, Splitter:B:31:0x00a0] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0132  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x014c A[Catch:{ EOFException -> 0x0145, IOException -> 0x0129, all -> 0x0123, all -> 0x0152 }] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0155  */
    public void readResponse(AndroidHttpClientConnection httpClientConnection) throws IOException, ParseException {
        StatusLine statusLine;
        int statusCode;
        HttpEntity entity;
        EOFException e;
        IOException e2;
        int len;
        Throwable th;
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
                androidHttpClientConnection = httpClientConnection;
            }
            ProtocolVersion v = statusLine.getProtocolVersion();
            this.mEventHandler.status(v.getMajor(), v.getMinor(), statusCode, statusLine.getReasonPhrase());
            this.mEventHandler.headers(header);
            boolean hasBody = canResponseHaveBody(this.mHttpRequest, statusCode);
            if (hasBody) {
                entity = androidHttpClientConnection.receiveResponseEntity(header);
            } else {
                entity = null;
            }
            boolean supportPartialContent = "bytes".equalsIgnoreCase(header.getAcceptRanges());
            if (entity != null) {
                InputStream is = entity.getContent();
                Header contentEncoding = entity.getContentEncoding();
                InputStream nis = null;
                byte[] buf = null;
                int count = 0;
                if (contentEncoding != null) {
                    try {
                        if (contentEncoding.getValue().equals("gzip")) {
                            nis = new GZIPInputStream(is);
                            buf = this.mConnection.getBuf();
                            int lowWater = buf.length / 2;
                            int count2 = 0;
                            len = 0;
                            while (len != -1) {
                                try {
                                    synchronized (this) {
                                        while (this.mLoadingPaused) {
                                            try {
                                                try {
                                                    wait();
                                                } catch (InterruptedException e3) {
                                                    StringBuilder sb = new StringBuilder();
                                                    sb.append("Interrupted exception whilst network thread paused at WebCore's request. ");
                                                    sb.append(e3.getMessage());
                                                    HttpLog.e(sb.toString());
                                                    v = v;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    throw th;
                                                }
                                            } catch (Throwable th3) {
                                                th = th3;
                                                throw th;
                                            }
                                        }
                                    }
                                    try {
                                        len = nis.read(buf, count2, buf.length - count2);
                                        if (len != -1) {
                                            count2 += len;
                                            if (supportPartialContent) {
                                                this.mReceivedBytes += len;
                                            }
                                        }
                                        if (len != -1) {
                                            if (count2 < lowWater) {
                                                hasBody = hasBody;
                                                v = v;
                                            }
                                        }
                                        this.mEventHandler.data(buf, count2);
                                        count2 = 0;
                                        hasBody = hasBody;
                                        v = v;
                                    } catch (EOFException e4) {
                                        count = count2;
                                        if (count > 0) {
                                        }
                                    } catch (IOException e5) {
                                        e2 = e5;
                                        count = count2;
                                        if (statusCode != 200) {
                                        }
                                        this.mEventHandler.data(buf, count);
                                        throw e2;
                                    } catch (Throwable th4) {
                                        e = th4;
                                        if (nis != null) {
                                        }
                                        throw e;
                                    }
                                } catch (EOFException e6) {
                                    count = count2;
                                    if (count > 0) {
                                    }
                                } catch (IOException e7) {
                                    e2 = e7;
                                    count = count2;
                                    if (statusCode != 200) {
                                    }
                                    this.mEventHandler.data(buf, count);
                                    throw e2;
                                } catch (Throwable th5) {
                                    e = th5;
                                    if (nis != null) {
                                    }
                                    throw e;
                                }
                            }
                            if (nis != null) {
                                nis.close();
                            }
                        }
                    } catch (EOFException e8) {
                        if (count > 0) {
                        }
                    } catch (IOException e9) {
                        e2 = e9;
                        if (statusCode != 200) {
                        }
                        this.mEventHandler.data(buf, count);
                        throw e2;
                    } catch (Throwable th6) {
                        e = th6;
                        if (nis != null) {
                        }
                        throw e;
                    }
                }
                nis = is;
                try {
                    buf = this.mConnection.getBuf();
                    int lowWater2 = buf.length / 2;
                    int count22 = 0;
                    len = 0;
                    while (len != -1) {
                    }
                    if (nis != null) {
                    }
                } catch (EOFException e10) {
                    if (count > 0) {
                        this.mEventHandler.data(buf, count);
                    }
                } catch (IOException e11) {
                    e2 = e11;
                    if (statusCode != 200 || statusCode == 206) {
                        if (supportPartialContent && count > 0) {
                            this.mEventHandler.data(buf, count);
                        }
                        throw e2;
                    }
                } catch (Throwable th7) {
                    e = th7;
                    if (nis != null) {
                    }
                    throw e;
                }
            }
            this.mConnection.setCanPersist(entity, statusLine.getProtocolVersion(), header.getConnectionType());
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
        InputStream inputStream = this.mBodyProvider;
        if (inputStream != null) {
            try {
                inputStream.reset();
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
        if (!HttpHead.METHOD_NAME.equalsIgnoreCase(request.getRequestLine().getMethod()) && status >= 200 && status != 204 && status != 304) {
            return true;
        }
        return false;
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
