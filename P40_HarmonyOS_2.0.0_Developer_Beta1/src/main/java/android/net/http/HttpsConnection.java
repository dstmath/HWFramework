package android.net.http;

import android.content.Context;
import android.util.Log;
import com.android.org.conscrypt.FileClientSessionCache;
import com.android.org.conscrypt.SSLClientSessionCache;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.Locale;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpsConnection extends Connection {
    private static SSLSocketFactory mSslSocketFactory = null;
    private boolean mAborted = false;
    private HttpHost mProxyHost;
    private Object mSuspendLock = new Object();
    private boolean mSuspended = false;

    static {
        initializeEngine(null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0059, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005f, code lost:
        throw new java.lang.RuntimeException(r0);
     */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0059 A[ExcHandler: KeyManagementException | NoSuchAlgorithmException | NoSuchProviderException (r0v2 'e' java.security.GeneralSecurityException A[CUSTOM_DECLARE]), Splitter:B:13:0x0051] */
    public static void initializeEngine(File sessionDir) {
        SSLClientSessionCache cache = null;
        if (sessionDir != null) {
            try {
                Log.d("HttpsConnection", "Caching SSL sessions in " + sessionDir + ".");
                cache = FileClientSessionCache.usingDirectory(sessionDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        SSLContext sslContext = SSLContext.getInstance("TLS", "AndroidOpenSSL");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            /* class android.net.http.HttpsConnection.AnonymousClass1 */

            @Override // javax.net.ssl.X509TrustManager
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override // javax.net.ssl.X509TrustManager
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override // javax.net.ssl.X509TrustManager
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }}, null);
        sslContext.getClientSessionContext().setPersistentCache(cache);
        synchronized (HttpsConnection.class) {
            mSslSocketFactory = sslContext.getSocketFactory();
        }
    }

    private static synchronized SSLSocketFactory getSocketFactory() {
        SSLSocketFactory sSLSocketFactory;
        synchronized (HttpsConnection.class) {
            sSLSocketFactory = mSslSocketFactory;
        }
        return sSLSocketFactory;
    }

    HttpsConnection(Context context, HttpHost host, HttpHost proxy, RequestFeeder requestFeeder) {
        super(context, host, requestFeeder);
        this.mProxyHost = proxy;
    }

    /* access modifiers changed from: package-private */
    public void setCertificate(SslCertificate certificate) {
        this.mCertificate = certificate;
    }

    /* access modifiers changed from: package-private */
    @Override // android.net.http.Connection
    public AndroidHttpClientConnection openConnection(Request req) throws IOException {
        StatusLine statusLine;
        int statusCode;
        SSLSocket sslSock = null;
        HttpHost httpHost = this.mProxyHost;
        if (httpHost != null) {
            AndroidHttpClientConnection proxyConnection = null;
            try {
                Socket proxySock = new Socket(httpHost.getHostName(), this.mProxyHost.getPort());
                proxySock.setSoTimeout(60000);
                proxyConnection = new AndroidHttpClientConnection();
                HttpParams params = new BasicHttpParams();
                HttpConnectionParams.setSocketBufferSize(params, 8192);
                proxyConnection.bind(proxySock, params);
                Headers headers = new Headers();
                try {
                    BasicHttpRequest proxyReq = new BasicHttpRequest("CONNECT", this.mHost.toHostString());
                    Header[] allHeaders = req.mHttpRequest.getAllHeaders();
                    for (Header h : allHeaders) {
                        String headerName = h.getName().toLowerCase(Locale.ROOT);
                        if (headerName.startsWith("proxy") || headerName.equals("keep-alive") || headerName.equals("host")) {
                            proxyReq.addHeader(h);
                        }
                    }
                    proxyConnection.sendRequestHeader(proxyReq);
                    proxyConnection.flush();
                    do {
                        statusLine = proxyConnection.parseResponseHeader(headers);
                        statusCode = statusLine.getStatusCode();
                    } while (statusCode < 200);
                    if (statusCode == 200) {
                        try {
                            sslSock = (SSLSocket) getSocketFactory().createSocket(proxySock, this.mHost.getHostName(), this.mHost.getPort(), true);
                        } catch (IOException e) {
                            if (0 != 0) {
                                sslSock.close();
                            }
                            String errorMessage = e.getMessage();
                            if (errorMessage == null) {
                                errorMessage = "failed to create an SSL socket";
                            }
                            throw new IOException(errorMessage);
                        }
                    } else {
                        ProtocolVersion version = statusLine.getProtocolVersion();
                        req.mEventHandler.status(version.getMajor(), version.getMinor(), statusCode, statusLine.getReasonPhrase());
                        req.mEventHandler.headers(headers);
                        req.mEventHandler.endData();
                        proxyConnection.close();
                        return null;
                    }
                } catch (ParseException e2) {
                    String errorMessage2 = e2.getMessage();
                    if (errorMessage2 == null) {
                        errorMessage2 = "failed to send a CONNECT request";
                    }
                    throw new IOException(errorMessage2);
                } catch (HttpException e3) {
                    String errorMessage3 = e3.getMessage();
                    if (errorMessage3 == null) {
                        errorMessage3 = "failed to send a CONNECT request";
                    }
                    throw new IOException(errorMessage3);
                } catch (IOException e4) {
                    String errorMessage4 = e4.getMessage();
                    if (errorMessage4 == null) {
                        errorMessage4 = "failed to send a CONNECT request";
                    }
                    throw new IOException(errorMessage4);
                }
            } catch (IOException e5) {
                if (proxyConnection != null) {
                    proxyConnection.close();
                }
                String errorMessage5 = e5.getMessage();
                if (errorMessage5 == null) {
                    errorMessage5 = "failed to establish a connection to the proxy";
                }
                throw new IOException(errorMessage5);
            }
        } else {
            try {
                sslSock = (SSLSocket) getSocketFactory().createSocket(this.mHost.getHostName(), this.mHost.getPort());
                sslSock.setSoTimeout(60000);
            } catch (IOException e6) {
                if (sslSock != null) {
                    sslSock.close();
                }
                String errorMessage6 = e6.getMessage();
                if (errorMessage6 == null) {
                    errorMessage6 = "failed to create an SSL socket";
                }
                throw new IOException(errorMessage6);
            }
        }
        SslError error = CertificateChainValidator.getInstance().doHandshakeAndValidateServerCertificates(this, sslSock, this.mHost.getHostName());
        if (error != null) {
            synchronized (this.mSuspendLock) {
                this.mSuspended = true;
            }
            if (req.getEventHandler().handleSslErrorRequest(error)) {
                synchronized (this.mSuspendLock) {
                    if (this.mSuspended) {
                        try {
                            this.mSuspendLock.wait(600000);
                            if (this.mSuspended) {
                                this.mSuspended = false;
                                this.mAborted = true;
                            }
                        } catch (InterruptedException e7) {
                        }
                    }
                    if (this.mAborted) {
                        sslSock.close();
                        throw new SSLConnectionClosedByUserException("connection closed by the user");
                    }
                }
            } else {
                throw new IOException("failed to handle " + error);
            }
        }
        AndroidHttpClientConnection conn = new AndroidHttpClientConnection();
        BasicHttpParams params2 = new BasicHttpParams();
        params2.setIntParameter("http.socket.buffer-size", 8192);
        conn.bind(sslSock, params2);
        return conn;
    }

    /* access modifiers changed from: package-private */
    @Override // android.net.http.Connection
    public void closeConnection() {
        if (this.mSuspended) {
            restartConnection(false);
        }
        try {
            if (this.mHttpClientConnection != null && this.mHttpClientConnection.isOpen()) {
                this.mHttpClientConnection.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public void restartConnection(boolean proceed) {
        synchronized (this.mSuspendLock) {
            if (this.mSuspended) {
                boolean z = false;
                this.mSuspended = false;
                if (!proceed) {
                    z = true;
                }
                this.mAborted = z;
                this.mSuspendLock.notify();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.net.http.Connection
    public String getScheme() {
        return "https";
    }
}
