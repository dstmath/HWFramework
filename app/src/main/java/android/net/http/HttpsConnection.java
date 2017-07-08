package android.net.http;

import android.content.Context;
import android.util.Log;
import com.android.org.conscrypt.FileClientSessionCache;
import com.android.org.conscrypt.OpenSSLContextImpl;
import com.android.org.conscrypt.SSLClientSessionCache;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.cert.X509Certificate;
import java.util.Locale;
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
    private static SSLSocketFactory mSslSocketFactory;
    private boolean mAborted;
    private HttpHost mProxyHost;
    private Object mSuspendLock;
    private boolean mSuspended;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.http.HttpsConnection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.http.HttpsConnection.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.http.HttpsConnection.<clinit>():void");
    }

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public static void initializeEngine(File sessionDir) {
        SSLClientSessionCache cache = null;
        if (sessionDir != null) {
            try {
                Log.d("HttpsConnection", "Caching SSL sessions in " + sessionDir + ".");
                cache = FileClientSessionCache.usingDirectory(sessionDir);
            } catch (KeyManagementException e) {
                throw new RuntimeException(e);
            } catch (IOException e2) {
                throw new RuntimeException(e2);
            }
        }
        OpenSSLContextImpl sslContext = OpenSSLContextImpl.getPreferred();
        sslContext.engineInit(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }}, null);
        sslContext.engineGetClientSessionContext().setPersistentCache(cache);
        synchronized (HttpsConnection.class) {
            mSslSocketFactory = sslContext.engineGetSocketFactory();
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
        this.mSuspendLock = new Object();
        this.mSuspended = false;
        this.mAborted = false;
        this.mProxyHost = proxy;
    }

    void setCertificate(SslCertificate certificate) {
        this.mCertificate = certificate;
    }

    AndroidHttpClientConnection openConnection(Request req) throws IOException {
        Socket sslSock;
        String errorMessage;
        IOException e;
        Socket socket;
        SSLSocket sslSock2 = null;
        if (this.mProxyHost != null) {
            AndroidHttpClientConnection androidHttpClientConnection = null;
            try {
                Socket socket2 = new Socket(this.mProxyHost.getHostName(), this.mProxyHost.getPort());
                try {
                    socket2.setSoTimeout(60000);
                    AndroidHttpClientConnection proxyConnection = new AndroidHttpClientConnection();
                    try {
                        HttpParams params = new BasicHttpParams();
                        HttpConnectionParams.setSocketBufferSize(params, 8192);
                        proxyConnection.bind(socket2, params);
                        Headers headers = new Headers();
                        try {
                            int statusCode;
                            StatusLine statusLine;
                            BasicHttpRequest basicHttpRequest = new BasicHttpRequest("CONNECT", this.mHost.toHostString());
                            for (Header h : req.mHttpRequest.getAllHeaders()) {
                                String headerName = h.getName().toLowerCase(Locale.ROOT);
                                if (!headerName.startsWith("proxy")) {
                                    if (!headerName.equals("keep-alive")) {
                                        if (!headerName.equals("host")) {
                                        }
                                    }
                                }
                                basicHttpRequest.addHeader(h);
                            }
                            proxyConnection.sendRequestHeader(basicHttpRequest);
                            proxyConnection.flush();
                            do {
                                statusLine = proxyConnection.parseResponseHeader(headers);
                                statusCode = statusLine.getStatusCode();
                            } while (statusCode < 200);
                            if (statusCode == 200) {
                                try {
                                    sslSock = (SSLSocket) getSocketFactory().createSocket(socket2, this.mHost.getHostName(), this.mHost.getPort(), true);
                                } catch (IOException e2) {
                                    errorMessage = e2.getMessage();
                                    if (errorMessage == null) {
                                        errorMessage = "failed to create an SSL socket";
                                    }
                                    throw new IOException(errorMessage);
                                }
                            }
                            ProtocolVersion version = statusLine.getProtocolVersion();
                            req.mEventHandler.status(version.getMajor(), version.getMinor(), statusCode, statusLine.getReasonPhrase());
                            req.mEventHandler.headers(headers);
                            req.mEventHandler.endData();
                            proxyConnection.close();
                            return null;
                        } catch (ParseException e3) {
                            errorMessage = e3.getMessage();
                            if (errorMessage == null) {
                                errorMessage = "failed to send a CONNECT request";
                            }
                            throw new IOException(errorMessage);
                        } catch (HttpException e4) {
                            errorMessage = e4.getMessage();
                            if (errorMessage == null) {
                                errorMessage = "failed to send a CONNECT request";
                            }
                            throw new IOException(errorMessage);
                        } catch (IOException e22) {
                            errorMessage = e22.getMessage();
                            if (errorMessage == null) {
                                errorMessage = "failed to send a CONNECT request";
                            }
                            throw new IOException(errorMessage);
                        }
                    } catch (IOException e5) {
                        e22 = e5;
                        socket = socket2;
                        androidHttpClientConnection = proxyConnection;
                        if (androidHttpClientConnection != null) {
                            androidHttpClientConnection.close();
                        }
                        errorMessage = e22.getMessage();
                        if (errorMessage == null) {
                            errorMessage = "failed to establish a connection to the proxy";
                        }
                        throw new IOException(errorMessage);
                    }
                } catch (IOException e6) {
                    e22 = e6;
                    socket = socket2;
                    if (androidHttpClientConnection != null) {
                        androidHttpClientConnection.close();
                    }
                    errorMessage = e22.getMessage();
                    if (errorMessage == null) {
                        errorMessage = "failed to establish a connection to the proxy";
                    }
                    throw new IOException(errorMessage);
                }
            } catch (IOException e7) {
                e22 = e7;
                if (androidHttpClientConnection != null) {
                    androidHttpClientConnection.close();
                }
                errorMessage = e22.getMessage();
                if (errorMessage == null) {
                    errorMessage = "failed to establish a connection to the proxy";
                }
                throw new IOException(errorMessage);
            }
        }
        try {
            sslSock2 = (SSLSocket) getSocketFactory().createSocket(this.mHost.getHostName(), this.mHost.getPort());
            sslSock2.setSoTimeout(60000);
        } catch (IOException e222) {
            if (sslSock2 != null) {
                sslSock2.close();
            }
            errorMessage = e222.getMessage();
            if (errorMessage == null) {
                errorMessage = "failed to create an SSL socket";
            }
            throw new IOException(errorMessage);
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
                        } catch (InterruptedException e8) {
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

    void closeConnection() {
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

    void restartConnection(boolean proceed) {
        boolean z = false;
        synchronized (this.mSuspendLock) {
            if (this.mSuspended) {
                this.mSuspended = false;
                if (!proceed) {
                    z = true;
                }
                this.mAborted = z;
                this.mSuspendLock.notify();
            }
        }
    }

    String getScheme() {
        return "https";
    }
}
