package jcifs.smb;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {
    static final URLStreamHandler SMB_HANDLER = new Handler();

    protected int getDefaultPort() {
        return SmbConstants.DEFAULT_PORT;
    }

    public URLConnection openConnection(URL u) throws IOException {
        return new SmbFile(u);
    }

    protected void parseURL(URL u, String spec, int start, int limit) {
        String host = u.getHost();
        if (spec.equals("smb://")) {
            spec = "smb:////";
            limit += 2;
        } else if (!(spec.startsWith("smb://") || host == null || host.length() != 0)) {
            spec = "//" + spec;
            limit += 2;
        }
        super.parseURL(u, spec, start, limit);
        String path = u.getPath();
        String ref = u.getRef();
        if (ref != null) {
            path = path + '#' + ref;
        }
        int port = u.getPort();
        if (port == -1) {
            port = getDefaultPort();
        }
        setURL(u, "smb", u.getHost(), port, u.getAuthority(), u.getUserInfo(), path, u.getQuery(), null);
    }
}
