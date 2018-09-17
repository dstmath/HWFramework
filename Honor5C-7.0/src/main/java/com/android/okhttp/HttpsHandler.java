package com.android.okhttp;

import java.net.Proxy;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

public final class HttpsHandler extends HttpHandler {
    private static final List<Protocol> HTTP_1_1_ONLY = null;
    private static final List<ConnectionSpec> SECURE_CONNECTION_SPECS = null;
    private static final ConnectionSpec SSL_3_0 = null;
    private static final ConnectionSpec TLS_1_0_AND_BELOW = null;
    private static final ConnectionSpec TLS_1_1_AND_BELOW = null;
    private static final ConnectionSpec TLS_1_2_AND_BELOW = null;
    private final ConfigAwareConnectionPool configAwareConnectionPool;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.HttpsHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.HttpsHandler.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.HttpsHandler.<clinit>():void");
    }

    public HttpsHandler() {
        this.configAwareConnectionPool = ConfigAwareConnectionPool.getInstance();
    }

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
        okHttpClient.setConnectionSpecs(SECURE_CONNECTION_SPECS);
        okUrlFactory.client().setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
        okHttpClient.setSslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
        return okUrlFactory;
    }
}
