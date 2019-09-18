package jcifs.https;

public class Handler extends jcifs.http.Handler {
    public static final int DEFAULT_HTTPS_PORT = 443;

    /* access modifiers changed from: protected */
    public int getDefaultPort() {
        return DEFAULT_HTTPS_PORT;
    }
}
