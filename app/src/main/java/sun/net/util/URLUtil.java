package sun.net.util;

import java.net.URL;

public class URLUtil {
    public static String urlNoFragString(URL url) {
        StringBuilder strForm = new StringBuilder();
        String protocol = url.getProtocol();
        if (protocol != null) {
            strForm.append(protocol.toLowerCase());
            strForm.append("://");
        }
        String host = url.getHost();
        if (host != null) {
            strForm.append(host.toLowerCase());
            int port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }
            if (port != -1) {
                strForm.append(":").append(port);
            }
        }
        String file = url.getFile();
        if (file != null) {
            strForm.append(file);
        }
        return strForm.toString();
    }
}
