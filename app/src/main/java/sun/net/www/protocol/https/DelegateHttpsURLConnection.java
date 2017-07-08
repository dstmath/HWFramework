package sun.net.www.protocol.https;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import sun.net.www.protocol.http.Handler;

public class DelegateHttpsURLConnection extends AbstractDelegateHttpsURLConnection {
    public HttpsURLConnection httpsURLConnection;

    DelegateHttpsURLConnection(URL url, Handler handler, HttpsURLConnection httpsURLConnection) throws IOException {
        this(url, null, handler, httpsURLConnection);
    }

    DelegateHttpsURLConnection(URL url, Proxy p, Handler handler, HttpsURLConnection httpsURLConnection) throws IOException {
        super(url, p, handler);
        this.httpsURLConnection = httpsURLConnection;
    }

    protected SSLSocketFactory getSSLSocketFactory() {
        return this.httpsURLConnection.getSSLSocketFactory();
    }

    protected HostnameVerifier getHostnameVerifier() {
        return this.httpsURLConnection.getHostnameVerifier();
    }

    protected void dispose() throws Throwable {
        super.finalize();
    }
}
