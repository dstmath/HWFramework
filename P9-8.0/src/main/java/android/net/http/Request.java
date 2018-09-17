package android.net.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
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
        if (bodyProvider != null || (HttpPost.METHOD_NAME.equalsIgnoreCase(method) ^ 1) == 0) {
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

    synchronized void setLoadingPaused(boolean pause) {
        this.mLoadingPaused = pause;
        if (!this.mLoadingPaused) {
            notify();
        }
    }

    void setConnection(Connection connection) {
        this.mConnection = connection;
    }

    EventHandler getEventHandler() {
        return this.mEventHandler;
    }

    void addHeader(String name, String value) {
        String damage;
        if (name == null) {
            damage = "Null http header name";
            HttpLog.e(damage);
            throw new NullPointerException(damage);
        } else if (value == null || value.length() == 0) {
            damage = "Null or empty value for header \"" + name + "\"";
            HttpLog.e(damage);
            throw new RuntimeException(damage);
        } else {
            this.mHttpRequest.addHeader(name, value);
        }
    }

    void addHeaders(Map<String, String> headers) {
        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                addHeader((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    void sendRequest(AndroidHttpClientConnection httpClientConnection) throws HttpException, IOException {
        if (!this.mCancelled) {
            requestContentProcessor.process(this.mHttpRequest, this.mConnection.getHttpContext());
            httpClientConnection.sendRequestHeader(this.mHttpRequest);
            if (this.mHttpRequest instanceof HttpEntityEnclosingRequest) {
                httpClientConnection.sendRequestEntity((HttpEntityEnclosingRequest) this.mHttpRequest);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x00b4 A:{Catch:{ EOFException -> 0x00e1, IOException -> 0x015e }} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x015a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readResponse(AndroidHttpClientConnection httpClientConnection) throws IOException, ParseException {
        if (!this.mCancelled) {
            int statusCode;
            StatusLine statusLine;
            httpClientConnection.flush();
            Headers header = new Headers();
            do {
                statusLine = httpClientConnection.parseResponseHeader(header);
                statusCode = statusLine.getStatusCode();
            } while (statusCode < 200);
            ProtocolVersion v = statusLine.getProtocolVersion();
            this.mEventHandler.status(v.getMajor(), v.getMinor(), statusCode, statusLine.getReasonPhrase());
            this.mEventHandler.headers(header);
            HttpEntity entity = null;
            if (canResponseHaveBody(this.mHttpRequest, statusCode)) {
                entity = httpClientConnection.receiveResponseEntity(header);
            }
            boolean supportPartialContent = "bytes".equalsIgnoreCase(header.getAcceptRanges());
            if (entity != null) {
                int len;
                int lowWater;
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
                            len = 0;
                            lowWater = buf.length / 2;
                            while (len != -1) {
                                synchronized (this) {
                                    while (this.mLoadingPaused) {
                                        try {
                                            wait();
                                        } catch (InterruptedException e) {
                                            HttpLog.e("Interrupted exception whilst network thread paused at WebCore's request. " + e.getMessage());
                                        }
                                    }
                                }
                                len = nis.read(buf, count, buf.length - count);
                                if (len != -1) {
                                    count += len;
                                    if (supportPartialContent) {
                                        this.mReceivedBytes += len;
                                    }
                                }
                                if (len == -1 || count >= lowWater) {
                                    this.mEventHandler.data(buf, count);
                                    count = 0;
                                }
                            }
                            if (nis != null) {
                                nis.close();
                            }
                        }
                    } catch (EOFException e2) {
                        if (count > 0) {
                            try {
                                this.mEventHandler.data(buf, count);
                            } catch (Throwable th) {
                                if (nis != null) {
                                    nis.close();
                                }
                            }
                        }
                        if (nis != null) {
                            nis.close();
                        }
                    } catch (IOException e3) {
                        if (statusCode == 200 || statusCode == 206) {
                            if (supportPartialContent && count > 0) {
                                this.mEventHandler.data(buf, count);
                            }
                            throw e3;
                        } else if (nis != null) {
                            nis.close();
                        }
                    }
                }
                nis = is;
                buf = this.mConnection.getBuf();
                len = 0;
                lowWater = buf.length / 2;
                while (len != -1) {
                }
                if (nis != null) {
                }
            }
            this.mConnection.setCanPersist(entity, statusLine.getProtocolVersion(), header.getConnectionType());
            this.mEventHandler.endData();
            complete();
        }
    }

    synchronized void cancel() {
        this.mLoadingPaused = false;
        notify();
        this.mCancelled = true;
        if (this.mConnection != null) {
            this.mConnection.cancel();
        }
    }

    String getHostPort() {
        String myScheme = this.mHost.getSchemeName();
        int myPort = this.mHost.getPort();
        if ((myPort == 80 || !myScheme.equals(HttpHost.DEFAULT_SCHEME_NAME)) && (myPort == 443 || !myScheme.equals("https"))) {
            return this.mHost.getHostName();
        }
        return this.mHost.toHostString();
    }

    String getUri() {
        if (this.mProxyHost == null || this.mHost.getSchemeName().equals("https")) {
            return this.mPath;
        }
        return this.mHost.getSchemeName() + "://" + getHostPort() + this.mPath;
    }

    public String toString() {
        return this.mPath;
    }

    void reset() {
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
            this.mHttpRequest.setHeader("Range", "bytes=" + this.mReceivedBytes + "-");
        }
    }

    void waitUntilComplete() {
        synchronized (this.mClientResource) {
            try {
                this.mClientResource.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    void complete() {
        synchronized (this.mClientResource) {
            this.mClientResource.notifyAll();
        }
    }

    private static boolean canResponseHaveBody(HttpRequest request, int status) {
        boolean z = false;
        if (HttpHead.METHOD_NAME.equalsIgnoreCase(request.getRequestLine().getMethod())) {
            return false;
        }
        if (!(status < HttpStatus.SC_OK || status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_NOT_MODIFIED)) {
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
        HttpsConnection connection = this.mConnection;
        if (connection != null) {
            connection.restartConnection(proceed);
        }
    }

    void error(int errorId, String errorMessage) {
        this.mEventHandler.error(errorId, errorMessage);
    }
}
