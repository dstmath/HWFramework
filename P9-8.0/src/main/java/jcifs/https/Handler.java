package jcifs.https;

public class Handler extends jcifs.http.Handler {
    public static final int DEFAULT_HTTPS_PORT = 443;

    protected int getDefaultPort() {
        return DEFAULT_HTTPS_PORT;
    }
}
