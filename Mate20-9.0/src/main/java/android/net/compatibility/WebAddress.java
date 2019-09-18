package android.net.compatibility;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpHost;

public class WebAddress {
    static final int MATCH_GROUP_AUTHORITY = 2;
    static final int MATCH_GROUP_HOST = 3;
    static final int MATCH_GROUP_PATH = 5;
    static final int MATCH_GROUP_PORT = 4;
    static final int MATCH_GROUP_SCHEME = 1;
    static Pattern sAddressPattern = Pattern.compile("(?:(http|https|file)\\:\\/\\/)?(?:([-A-Za-z0-9$_.+!*'(),;?&=]+(?:\\:[-A-Za-z0-9$_.+!*'(),;?&=]+)?)@)?([a-zA-Z0-9 -퟿豈-﷏ﷰ-￯%_-][a-zA-Z0-9 -퟿豈-﷏ﷰ-￯%_\\.-]*|\\[[0-9a-fA-F:\\.]+\\])?(?:\\:([0-9]*))?(\\/?[^#]*)?.*", 2);
    private String mAuthInfo;
    private String mHost;
    private String mPath;
    private int mPort;
    private String mScheme;

    public WebAddress(String address) throws IllegalArgumentException {
        if (address != null) {
            this.mScheme = "";
            this.mHost = "";
            this.mPort = -1;
            this.mPath = "/";
            this.mAuthInfo = "";
            Matcher m = sAddressPattern.matcher(address);
            if (m.matches()) {
                String t = m.group(1);
                if (t != null) {
                    this.mScheme = t.toLowerCase(Locale.ROOT);
                }
                String t2 = m.group(2);
                if (t2 != null) {
                    this.mAuthInfo = t2;
                }
                String t3 = m.group(3);
                if (t3 != null) {
                    this.mHost = t3;
                }
                String t4 = m.group(4);
                if (t4 != null && t4.length() > 0) {
                    try {
                        this.mPort = Integer.parseInt(t4);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Bad port");
                    }
                }
                String t5 = m.group(5);
                if (t5 != null && t5.length() > 0) {
                    if (t5.charAt(0) == '/') {
                        this.mPath = t5;
                    } else {
                        this.mPath = "/" + t5;
                    }
                }
                if (this.mPort == 443 && this.mScheme.equals("")) {
                    this.mScheme = "https";
                } else if (this.mPort == -1) {
                    if (this.mScheme.equals("https")) {
                        this.mPort = 443;
                    } else {
                        this.mPort = 80;
                    }
                }
                if (this.mScheme.equals("")) {
                    this.mScheme = HttpHost.DEFAULT_SCHEME_NAME;
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("Bad address");
        }
        throw new NullPointerException();
    }

    public String toString() {
        String port = "";
        if ((this.mPort != 443 && this.mScheme.equals("https")) || (this.mPort != 80 && this.mScheme.equals(HttpHost.DEFAULT_SCHEME_NAME))) {
            port = ":" + Integer.toString(this.mPort);
        }
        String authInfo = "";
        if (this.mAuthInfo.length() > 0) {
            authInfo = this.mAuthInfo + "@";
        }
        return this.mScheme + "://" + authInfo + this.mHost + port + this.mPath;
    }

    public void setScheme(String scheme) {
        this.mScheme = scheme;
    }

    public String getScheme() {
        return this.mScheme;
    }

    public void setHost(String host) {
        this.mHost = host;
    }

    public String getHost() {
        return this.mHost;
    }

    public void setPort(int port) {
        this.mPort = port;
    }

    public int getPort() {
        return this.mPort;
    }

    public void setPath(String path) {
        this.mPath = path;
    }

    public String getPath() {
        return this.mPath;
    }

    public void setAuthInfo(String authInfo) {
        this.mAuthInfo = authInfo;
    }

    public String getAuthInfo() {
        return this.mAuthInfo;
    }
}
