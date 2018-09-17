package sun.net.www.protocol.http;

import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import sun.misc.BASE64Encoder;
import sun.net.www.HeaderParser;

class BasicAuthentication extends AuthenticationInfo {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final long serialVersionUID = 100;
    String auth;

    private class BasicBASE64Encoder extends BASE64Encoder {
        private BasicBASE64Encoder() {
        }

        protected int bytesPerLine() {
            return 10000;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.protocol.http.BasicAuthentication.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.protocol.http.BasicAuthentication.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.protocol.http.BasicAuthentication.<clinit>():void");
    }

    public BasicAuthentication(boolean isProxy, String host, int port, String realm, PasswordAuthentication pw) {
        super(isProxy ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, AuthScheme.BASIC, host, port, realm);
        byte[] nameBytes = null;
        try {
            nameBytes = (pw.getUserName() + ":").getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        char[] passwd = pw.getPassword();
        byte[] passwdBytes = new byte[passwd.length];
        for (int i = 0; i < passwd.length; i++) {
            passwdBytes[i] = (byte) passwd[i];
        }
        byte[] concat = new byte[(nameBytes.length + passwdBytes.length)];
        System.arraycopy(nameBytes, 0, concat, 0, nameBytes.length);
        System.arraycopy(passwdBytes, 0, concat, nameBytes.length, passwdBytes.length);
        this.auth = "Basic " + new BasicBASE64Encoder().encode(concat);
        this.pw = pw;
    }

    public BasicAuthentication(boolean isProxy, String host, int port, String realm, String auth) {
        super(isProxy ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, AuthScheme.BASIC, host, port, realm);
        this.auth = "Basic " + auth;
    }

    public BasicAuthentication(boolean isProxy, URL url, String realm, PasswordAuthentication pw) {
        super(isProxy ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, AuthScheme.BASIC, url, realm);
        byte[] nameBytes = null;
        try {
            nameBytes = (pw.getUserName() + ":").getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        char[] passwd = pw.getPassword();
        byte[] passwdBytes = new byte[passwd.length];
        for (int i = 0; i < passwd.length; i++) {
            passwdBytes[i] = (byte) passwd[i];
        }
        byte[] concat = new byte[(nameBytes.length + passwdBytes.length)];
        System.arraycopy(nameBytes, 0, concat, 0, nameBytes.length);
        System.arraycopy(passwdBytes, 0, concat, nameBytes.length, passwdBytes.length);
        this.auth = "Basic " + new BasicBASE64Encoder().encode(concat);
        this.pw = pw;
    }

    public BasicAuthentication(boolean isProxy, URL url, String realm, String auth) {
        super(isProxy ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, AuthScheme.BASIC, url, realm);
        this.auth = "Basic " + auth;
    }

    public boolean supportsPreemptiveAuthorization() {
        return true;
    }

    public boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
        conn.setAuthenticationProperty(getHeaderName(), getHeaderValue(null, null));
        return true;
    }

    public String getHeaderValue(URL url, String method) {
        return this.auth;
    }

    public boolean isAuthorizationStale(String header) {
        return -assertionsDisabled;
    }

    static String getRootPath(String npath, String opath) {
        int index = 0;
        try {
            npath = new URI(npath).normalize().getPath();
            opath = new URI(opath).normalize().getPath();
        } catch (URISyntaxException e) {
        }
        while (index < opath.length()) {
            int toindex = opath.indexOf(47, index + 1);
            if (toindex == -1 || !opath.regionMatches(0, npath, 0, toindex + 1)) {
                return opath.substring(0, index + 1);
            }
            index = toindex;
        }
        return npath;
    }
}
