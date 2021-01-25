package jcifs.https;

public class Handler extends jcifs.http.Handler {
    public static final int DEFAULT_HTTPS_PORT = 443;

    /* access modifiers changed from: protected */
    @Override // jcifs.http.Handler, java.net.URLStreamHandler
    public int getDefaultPort() {
        return DEFAULT_HTTPS_PORT;
    }
}
