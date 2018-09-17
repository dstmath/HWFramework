package android.net;

import android.content.IntentFilter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebAddress {
    static final int MATCH_GROUP_AUTHORITY = 2;
    static final int MATCH_GROUP_HOST = 3;
    static final int MATCH_GROUP_PATH = 5;
    static final int MATCH_GROUP_PORT = 4;
    static final int MATCH_GROUP_SCHEME = 1;
    static Pattern sAddressPattern;
    private String mAuthInfo;
    private String mHost;
    private String mPath;
    private int mPort;
    private String mScheme;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.WebAddress.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.WebAddress.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.WebAddress.<clinit>():void");
    }

    public WebAddress(String address) throws ParseException {
        if (address == null) {
            throw new NullPointerException();
        }
        this.mScheme = ProxyInfo.LOCAL_EXCL_LIST;
        this.mHost = ProxyInfo.LOCAL_EXCL_LIST;
        this.mPort = -1;
        this.mPath = "/";
        this.mAuthInfo = ProxyInfo.LOCAL_EXCL_LIST;
        Matcher m = sAddressPattern.matcher(address);
        if (m.matches()) {
            String t = m.group(MATCH_GROUP_SCHEME);
            if (t != null) {
                this.mScheme = t.toLowerCase(Locale.ROOT);
            }
            t = m.group(MATCH_GROUP_AUTHORITY);
            if (t != null) {
                this.mAuthInfo = t;
            }
            t = m.group(MATCH_GROUP_HOST);
            if (t != null) {
                this.mHost = t;
            }
            t = m.group(MATCH_GROUP_PORT);
            if (t != null && t.length() > 0) {
                try {
                    this.mPort = Integer.parseInt(t);
                } catch (NumberFormatException e) {
                    throw new ParseException("Bad port");
                }
            }
            t = m.group(MATCH_GROUP_PATH);
            if (t != null && t.length() > 0) {
                if (t.charAt(0) == '/') {
                    this.mPath = t;
                } else {
                    this.mPath = "/" + t;
                }
            }
            if (this.mPort == 443 && this.mScheme.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
                this.mScheme = IntentFilter.SCHEME_HTTPS;
            } else if (this.mPort == -1) {
                if (this.mScheme.equals(IntentFilter.SCHEME_HTTPS)) {
                    this.mPort = 443;
                } else {
                    this.mPort = 80;
                }
            }
            if (this.mScheme.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
                this.mScheme = IntentFilter.SCHEME_HTTP;
                return;
            }
            return;
        }
        throw new ParseException("Bad address");
    }

    public String toString() {
        String port = ProxyInfo.LOCAL_EXCL_LIST;
        if ((this.mPort != 443 && this.mScheme.equals(IntentFilter.SCHEME_HTTPS)) || (this.mPort != 80 && this.mScheme.equals(IntentFilter.SCHEME_HTTP))) {
            port = ":" + Integer.toString(this.mPort);
        }
        String authInfo = ProxyInfo.LOCAL_EXCL_LIST;
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
