package sun.net.www.protocol.ftp;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {
    /* access modifiers changed from: protected */
    public int getDefaultPort() {
        return 21;
    }

    /* access modifiers changed from: protected */
    public boolean equals(URL u1, URL u2) {
        String userInfo1 = u1.getUserInfo();
        String userInfo2 = u2.getUserInfo();
        return super.equals(u1, u2) && (userInfo1 != null ? userInfo1.equals(userInfo2) : userInfo2 == null);
    }

    /* access modifiers changed from: protected */
    public URLConnection openConnection(URL u) throws IOException {
        return openConnection(u, null);
    }

    /* access modifiers changed from: protected */
    public URLConnection openConnection(URL u, Proxy p) throws IOException {
        return new FtpURLConnection(u, p);
    }
}
