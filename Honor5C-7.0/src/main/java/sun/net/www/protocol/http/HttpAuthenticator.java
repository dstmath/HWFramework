package sun.net.www.protocol.http;

import java.net.URL;

@Deprecated
public interface HttpAuthenticator {
    String authString(URL url, String str, String str2);

    boolean schemeSupported(String str);
}
