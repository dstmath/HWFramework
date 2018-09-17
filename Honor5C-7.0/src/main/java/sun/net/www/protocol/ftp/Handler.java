package sun.net.www.protocol.ftp;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {
    protected int getDefaultPort() {
        return 21;
    }

    protected boolean equals(URL u1, URL u2) {
        String userInfo1 = u1.getUserInfo();
        String userInfo2 = u2.getUserInfo();
        if (!super.equals(u1, u2)) {
            return false;
        }
        if (userInfo1 != null) {
            return userInfo1.equals(userInfo2);
        }
        if (userInfo2 == null) {
            return true;
        }
        return false;
    }

    protected URLConnection openConnection(URL u) throws IOException {
        return openConnection(u, null);
    }

    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        return new FtpURLConnection(u, p);
    }
}
