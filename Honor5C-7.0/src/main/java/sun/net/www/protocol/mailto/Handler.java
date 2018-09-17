package sun.net.www.protocol.mailto;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {
    public synchronized URLConnection openConnection(URL u) {
        return new MailToURLConnection(u);
    }

    public void parseURL(URL u, String spec, int start, int limit) {
        String protocol = u.getProtocol();
        String host = "";
        int port = u.getPort();
        String file = "";
        if (start < limit) {
            file = spec.substring(start, limit);
        }
        boolean nogood = false;
        if (file == null || file.equals("")) {
            nogood = true;
        } else {
            boolean allwhites = true;
            for (int i = 0; i < file.length(); i++) {
                if (!Character.isWhitespace(file.charAt(i))) {
                    allwhites = false;
                }
            }
            if (allwhites) {
                nogood = true;
            }
        }
        if (nogood) {
            throw new RuntimeException("No email address");
        }
        setURL(u, protocol, host, port, file, null);
    }
}
