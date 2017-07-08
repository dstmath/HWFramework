package sun.net.www.protocol.http;

import java.io.IOException;
import java.net.Authenticator.RequestorType;
import java.net.URL;
import java.util.HashMap;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.net.www.HeaderParser;

class NegotiateAuthentication extends AuthenticationInfo {
    static HashMap<String, Negotiator> cache = null;
    private static final long serialVersionUID = 100;
    static HashMap<String, Boolean> supported;
    private final HttpCallerInfo hci;
    private Negotiator negotiator;

    class B64Encoder extends BASE64Encoder {
        B64Encoder() {
        }

        protected int bytesPerLine() {
            return 100000;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.protocol.http.NegotiateAuthentication.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.protocol.http.NegotiateAuthentication.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.protocol.http.NegotiateAuthentication.<clinit>():void");
    }

    public NegotiateAuthentication(HttpCallerInfo hci) {
        super(RequestorType.PROXY == hci.authType ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, hci.scheme.equalsIgnoreCase("Negotiate") ? AuthScheme.NEGOTIATE : AuthScheme.KERBEROS, hci.url, "");
        this.negotiator = null;
        this.hci = hci;
    }

    public boolean supportsPreemptiveAuthorization() {
        return false;
    }

    public static synchronized boolean isSupported(HttpCallerInfo hci) {
        synchronized (NegotiateAuthentication.class) {
            if (supported == null) {
                supported = new HashMap();
                cache = new HashMap();
            }
            String hostname = hci.host.toLowerCase();
            if (supported.containsKey(hostname)) {
                boolean booleanValue = ((Boolean) supported.get(hostname)).booleanValue();
                return booleanValue;
            }
            Negotiator neg = Negotiator.getNegotiator(hci);
            if (neg != null) {
                supported.put(hostname, Boolean.valueOf(true));
                cache.put(hostname, neg);
                return true;
            }
            supported.put(hostname, Boolean.valueOf(false));
            return false;
        }
    }

    public String getHeaderValue(URL url, String method) {
        throw new RuntimeException("getHeaderValue not supported");
    }

    public boolean isAuthorizationStale(String header) {
        return false;
    }

    public synchronized boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
        byte[] incoming = null;
        try {
            String[] parts = raw.split("\\s+");
            if (parts.length > 1) {
                incoming = new BASE64Decoder().decodeBuffer(parts[1]);
            }
            conn.setAuthenticationProperty(getHeaderName(), this.hci.scheme + " " + new B64Encoder().encode(incoming == null ? firstToken() : nextToken(incoming)));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private byte[] firstToken() throws IOException {
        this.negotiator = null;
        if (cache != null) {
            synchronized (cache) {
                this.negotiator = (Negotiator) cache.get(getHost());
                if (this.negotiator != null) {
                    cache.remove(getHost());
                }
            }
        }
        if (this.negotiator == null) {
            this.negotiator = Negotiator.getNegotiator(this.hci);
            if (this.negotiator == null) {
                throw new IOException("Cannot initialize Negotiator");
            }
        }
        return this.negotiator.firstToken();
    }

    private byte[] nextToken(byte[] token) throws IOException {
        return this.negotiator.nextToken(token);
    }
}
