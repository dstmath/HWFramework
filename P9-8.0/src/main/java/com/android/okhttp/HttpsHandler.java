package com.android.okhttp;

import com.android.okhttp.ConnectionSpec.Builder;
import java.net.Proxy;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

public final class HttpsHandler extends HttpHandler {
    private static final List<Protocol> HTTP_1_1_ONLY = Collections.singletonList(Protocol.HTTP_1_1);
    private static final ConnectionSpec TLS_CONNECTION_SPEC = new Builder(true).allEnabledCipherSuites().allEnabledTlsVersions().supportsTlsExtensions(true).build();
    private final ConfigAwareConnectionPool configAwareConnectionPool = ConfigAwareConnectionPool.getInstance();

    protected int getDefaultPort() {
        return 443;
    }

    protected OkUrlFactory newOkUrlFactory(Proxy proxy) {
        OkUrlFactory okUrlFactory = createHttpsOkUrlFactory(proxy);
        okUrlFactory.client().setConnectionPool(this.configAwareConnectionPool.get());
        return okUrlFactory;
    }

    public static OkUrlFactory createHttpsOkUrlFactory(Proxy proxy) {
        OkUrlFactory okUrlFactory = HttpHandler.createHttpOkUrlFactory(proxy);
        okUrlFactory.setUrlFilter(null);
        OkHttpClient okHttpClient = okUrlFactory.client();
        okHttpClient.setProtocols(HTTP_1_1_ONLY);
        okHttpClient.setConnectionSpecs(Collections.singletonList(TLS_CONNECTION_SPEC));
        okHttpClient.setCertificatePinner(CertificatePinner.DEFAULT);
        okUrlFactory.client().setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
        okHttpClient.setSslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
        return okUrlFactory;
    }
}
