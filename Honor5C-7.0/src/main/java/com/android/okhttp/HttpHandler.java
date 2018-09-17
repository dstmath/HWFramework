package com.android.okhttp;

import com.android.okhttp.internal.URLFilter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ResponseCache;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;
import java.util.concurrent.TimeUnit;
import libcore.net.NetworkSecurityPolicy;

public class HttpHandler extends URLStreamHandler {
    private static final CleartextURLFilter CLEARTEXT_FILTER = null;
    private static final List<ConnectionSpec> CLEARTEXT_ONLY = null;
    private final ConfigAwareConnectionPool configAwareConnectionPool;

    private static final class CleartextURLFilter implements URLFilter {
        private CleartextURLFilter() {
        }

        public void checkURLPermitted(URL url) throws IOException {
            String host = url.getHost();
            if (!NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(host)) {
                throw new IOException("Cleartext HTTP traffic to " + host + " not permitted");
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.HttpHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.HttpHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.HttpHandler.<clinit>():void");
    }

    public HttpHandler() {
        this.configAwareConnectionPool = ConfigAwareConnectionPool.getInstance();
    }

    protected URLConnection openConnection(URL url) throws IOException {
        return newOkUrlFactory(null).open(url);
    }

    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        if (url != null && proxy != null) {
            return newOkUrlFactory(proxy).open(url);
        }
        throw new IllegalArgumentException("url == null || proxy == null");
    }

    protected int getDefaultPort() {
        return 80;
    }

    protected OkUrlFactory newOkUrlFactory(Proxy proxy) {
        OkUrlFactory okUrlFactory = createHttpOkUrlFactory(proxy);
        okUrlFactory.client().setConnectionPool(this.configAwareConnectionPool.get());
        return okUrlFactory;
    }

    public static OkUrlFactory createHttpOkUrlFactory(Proxy proxy) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(0, TimeUnit.MILLISECONDS);
        client.setReadTimeout(0, TimeUnit.MILLISECONDS);
        client.setWriteTimeout(0, TimeUnit.MILLISECONDS);
        client.setFollowRedirects(HttpURLConnection.getFollowRedirects());
        client.setFollowSslRedirects(false);
        client.setConnectionSpecs(CLEARTEXT_ONLY);
        if (proxy != null) {
            client.setProxy(proxy);
        }
        OkUrlFactory okUrlFactory = new OkUrlFactory(client);
        okUrlFactory.setUrlFilter(CLEARTEXT_FILTER);
        ResponseCache responseCache = ResponseCache.getDefault();
        if (responseCache != null) {
            AndroidInternal.setResponseCache(okUrlFactory, responseCache);
        }
        return okUrlFactory;
    }
}
