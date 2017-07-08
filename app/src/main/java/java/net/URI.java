package java.net;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;
import sun.misc.FloatConsts;
import sun.nio.cs.ThreadLocalCoders;

public final class URI implements Comparable<URI>, Serializable {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final long H_ALPHA = 0;
    private static final long H_ALPHANUM = 0;
    private static final long H_DASH = 0;
    private static final long H_DIGIT = 0;
    private static final long H_DOT = 0;
    private static final long H_ESCAPED = 0;
    private static final long H_HEX = 0;
    private static final long H_LEFT_BRACKET = 0;
    private static final long H_LOWALPHA = 0;
    private static final long H_MARK = 0;
    private static final long H_PATH = 0;
    private static final long H_PCHAR = 0;
    private static final long H_REG_NAME = 0;
    private static final long H_RESERVED = 0;
    private static final long H_SCHEME = 0;
    private static final long H_SERVER = 0;
    private static final long H_SERVER_PERCENT = 0;
    private static final long H_UNDERSCORE = 0;
    private static final long H_UNRESERVED = 0;
    private static final long H_UPALPHA = 0;
    private static final long H_URIC = 0;
    private static final long H_URIC_NO_SLASH = 0;
    private static final long H_USERINFO = 0;
    private static final long L_ALPHA = 0;
    private static final long L_ALPHANUM = 0;
    private static final long L_DASH = 0;
    private static final long L_DIGIT = 0;
    private static final long L_DOT = 0;
    private static final long L_ESCAPED = 1;
    private static final long L_HEX = 0;
    private static final long L_LEFT_BRACKET = 0;
    private static final long L_LOWALPHA = 0;
    private static final long L_MARK = 0;
    private static final long L_PATH = 0;
    private static final long L_PCHAR = 0;
    private static final long L_REG_NAME = 0;
    private static final long L_RESERVED = 0;
    private static final long L_SCHEME = 0;
    private static final long L_SERVER = 0;
    private static final long L_SERVER_PERCENT = 0;
    private static final long L_UNDERSCORE = 0;
    private static final long L_UNRESERVED = 0;
    private static final long L_UPALPHA = 0;
    private static final long L_URIC = 0;
    private static final long L_URIC_NO_SLASH = 0;
    private static final long L_USERINFO = 0;
    private static final char[] hexDigits = null;
    static final long serialVersionUID = -6052424284110960213L;
    private transient String authority;
    private volatile transient String decodedAuthority;
    private volatile transient String decodedFragment;
    private volatile transient String decodedPath;
    private volatile transient String decodedQuery;
    private volatile transient String decodedSchemeSpecificPart;
    private volatile transient String decodedUserInfo;
    private transient String fragment;
    private volatile transient int hash;
    private transient String host;
    private transient String path;
    private transient int port;
    private transient String query;
    private transient String scheme;
    private volatile transient String schemeSpecificPart;
    private volatile String string;
    private transient String userInfo;

    private class Parser {
        private String input;
        private int ipv6byteCount;
        private boolean requireServerAuthority;

        Parser(String s) {
            this.requireServerAuthority = URI.-assertionsDisabled;
            this.ipv6byteCount = 0;
            this.input = s;
            URI.this.string = s;
        }

        private void fail(String reason) throws URISyntaxException {
            throw new URISyntaxException(this.input, reason);
        }

        private void fail(String reason, int p) throws URISyntaxException {
            throw new URISyntaxException(this.input, reason, p);
        }

        private void failExpecting(String expected, int p) throws URISyntaxException {
            fail("Expected " + expected, p);
        }

        private void failExpecting(String expected, String prior, int p) throws URISyntaxException {
            fail("Expected " + expected + " following " + prior, p);
        }

        private String substring(int start, int end) {
            return this.input.substring(start, end);
        }

        private char charAt(int p) {
            return this.input.charAt(p);
        }

        private boolean at(int start, int end, char c) {
            return (start >= end || charAt(start) != c) ? URI.-assertionsDisabled : true;
        }

        private boolean at(int start, int end, String s) {
            boolean z = URI.-assertionsDisabled;
            int p = start;
            int sn = s.length();
            if (sn > end - start) {
                return URI.-assertionsDisabled;
            }
            int i = 0;
            int p2 = p;
            while (i < sn) {
                p = p2 + 1;
                if (charAt(p2) != s.charAt(i)) {
                    break;
                }
                i++;
                p2 = p;
            }
            p = p2;
            if (i == sn) {
                z = true;
            }
            return z;
        }

        private int scan(int start, int end, char c) {
            if (start >= end || charAt(start) != c) {
                return start;
            }
            return start + 1;
        }

        private int scan(int start, int end, String err, String stop) {
            int p = start;
            while (p < end) {
                int c = charAt(p);
                if (err.indexOf(c) >= 0) {
                    return -1;
                }
                if (stop.indexOf(c) >= 0) {
                    break;
                }
                p++;
            }
            return p;
        }

        private int scanEscape(int start, int n, char first) throws URISyntaxException {
            int p = start;
            char c = first;
            if (first == '%') {
                if (start + 3 <= n && URI.match(charAt(start + 1), URI.L_HEX, URI.H_HEX) && URI.match(charAt(start + 2), URI.L_HEX, URI.H_HEX)) {
                    return start + 3;
                }
                fail("Malformed escape pair", start);
            } else if (!(first <= '\u0080' || Character.isSpaceChar(first) || Character.isISOControl(first))) {
                return start + 1;
            }
            return start;
        }

        private int scan(int start, int n, long lowMask, long highMask) throws URISyntaxException {
            int p = start;
            while (p < n) {
                char c = charAt(p);
                if (!URI.match(c, lowMask, highMask)) {
                    if ((URI.L_ESCAPED & lowMask) == URI.L_USERINFO) {
                        break;
                    }
                    int q = scanEscape(p, n, c);
                    if (q <= p) {
                        break;
                    }
                    p = q;
                } else {
                    p++;
                }
            }
            return p;
        }

        private void checkChars(int start, int end, long lowMask, long highMask, String what) throws URISyntaxException {
            int p = scan(start, end, lowMask, highMask);
            if (p < end) {
                fail("Illegal character in " + what, p);
            }
        }

        private void checkChar(int p, long lowMask, long highMask, String what) throws URISyntaxException {
            checkChars(p, p + 1, lowMask, highMask, what);
        }

        void parse(boolean rsa) throws URISyntaxException {
            int ssp;
            int p;
            this.requireServerAuthority = rsa;
            int n = this.input.length();
            int p2 = scan(0, n, "/?#", ":");
            if (p2 < 0 || !at(p2, n, ':')) {
                ssp = 0;
                p = parseHierarchical(0, n);
            } else {
                if (p2 == 0) {
                    failExpecting("scheme name", 0);
                }
                checkChar(0, URI.L_USERINFO, URI.H_ALPHA, "scheme name");
                checkChars(1, p2, URI.L_SCHEME, URI.H_SCHEME, "scheme name");
                URI.this.scheme = substring(0, p2);
                p = p2 + 1;
                ssp = p;
                if (at(p, n, '/')) {
                    p = parseHierarchical(p, n);
                } else {
                    int q = scan(p, n, "", "#");
                    if (q <= p) {
                        failExpecting("scheme-specific part", p);
                    }
                    checkChars(p, q, URI.L_URIC, URI.H_URIC, "opaque part");
                    p = q;
                }
            }
            URI.this.schemeSpecificPart = substring(ssp, p);
            if (at(p, n, '#')) {
                checkChars(p + 1, n, URI.L_URIC, URI.H_URIC, "fragment");
                URI.this.fragment = substring(p + 1, n);
                p = n;
            }
            if (p < n) {
                fail("end of URI", p);
            }
        }

        private int parseHierarchical(int start, int n) throws URISyntaxException {
            int q;
            int p = start;
            if (at(start, n, '/') && at(start + 1, n, '/')) {
                p = start + 2;
                q = scan(p, n, "", "/?#");
                if (q > p) {
                    p = parseAuthority(p, q);
                } else if (q >= n) {
                    failExpecting("authority", p);
                }
            }
            q = scan(p, n, "", "?#");
            checkChars(p, q, URI.L_PATH, URI.H_PATH, "path");
            URI.this.path = substring(p, q);
            p = q;
            if (!at(q, n, '?')) {
                return p;
            }
            p = q + 1;
            q = scan(p, n, "", "#");
            checkChars(p, q, URI.L_URIC, URI.H_URIC, "query");
            URI.this.query = substring(p, q);
            return q;
        }

        private int parseAuthority(int start, int n) throws URISyntaxException {
            boolean serverChars;
            int p = start;
            int q = start;
            URISyntaxException ex = null;
            if (scan(start, n, "", "]") > start) {
                serverChars = scan(start, n, URI.L_SERVER_PERCENT, URI.H_SERVER_PERCENT) == n ? true : URI.-assertionsDisabled;
            } else {
                serverChars = scan(start, n, URI.L_SERVER, URI.H_SERVER) == n ? true : URI.-assertionsDisabled;
            }
            boolean regChars = scan(start, n, URI.L_REG_NAME, URI.H_REG_NAME) == n ? true : URI.-assertionsDisabled;
            if (!regChars || serverChars) {
                if (serverChars) {
                    try {
                        q = parseServer(start, n);
                        if (q < n) {
                            failExpecting("end of authority", q);
                        }
                        URI.this.authority = substring(start, n);
                    } catch (URISyntaxException x) {
                        URI.this.userInfo = null;
                        URI.this.host = null;
                        URI.this.port = -1;
                        if (this.requireServerAuthority) {
                            throw x;
                        }
                        ex = x;
                        q = start;
                    }
                }
                if (q < n) {
                    if (regChars) {
                        URI.this.authority = substring(start, n);
                    } else if (ex != null) {
                        throw ex;
                    } else {
                        fail("Illegal character in authority", q);
                    }
                }
                return n;
            }
            URI.this.authority = substring(start, n);
            return n;
        }

        private int parseServer(int start, int n) throws URISyntaxException {
            int p = start;
            int q = scan(start, n, "/?#", "@");
            if (q >= start && at(q, n, '@')) {
                checkChars(start, q, URI.L_USERINFO, URI.H_USERINFO, "user info");
                URI.this.userInfo = substring(start, q);
                p = q + 1;
            }
            if (at(p, n, '[')) {
                int p2 = p + 1;
                q = scan(p2, n, "/?#", "]");
                if (q <= p2 || !at(q, n, ']')) {
                    failExpecting("closing bracket for IPv6 address", q);
                    p = p2;
                } else {
                    int r = scan(p2, q, "", "%");
                    if (r > p2) {
                        parseIPv6Reference(p2, r);
                        if (r + 1 == q) {
                            fail("scope id expected");
                        }
                        checkChars(r + 1, q, URI.L_ALPHANUM, URI.H_ALPHANUM, "scope id");
                    } else {
                        parseIPv6Reference(p2, q);
                    }
                    URI.this.host = substring(p2 - 1, q + 1);
                    p = q + 1;
                }
            } else {
                q = parseIPv4Address(p, n);
                if (q <= p) {
                    q = parseHostname(p, n);
                }
                p = q;
            }
            if (at(p, n, ':')) {
                p++;
                q = scan(p, n, "", "/");
                if (q > p) {
                    checkChars(p, q, URI.L_DIGIT, URI.L_USERINFO, "port number");
                    try {
                        URI.this.port = Integer.parseInt(substring(p, q));
                    } catch (NumberFormatException e) {
                        fail("Malformed port number", p);
                    }
                    p = q;
                }
            }
            if (p < n) {
                failExpecting("port number", p);
            }
            return p;
        }

        private int scanByte(int start, int n) throws URISyntaxException {
            int p = start;
            int q = scan(start, n, URI.L_DIGIT, (long) URI.L_USERINFO);
            if (q > start && Integer.parseInt(substring(start, q)) > 255) {
                return start;
            }
            return q;
        }

        private int scanIPv4Address(int start, int n, boolean strict) throws URISyntaxException {
            int p = start;
            int m = scan(start, n, URI.L_DOT | URI.L_DIGIT, URI.L_USERINFO | URI.H_DOT);
            if (m <= start || (strict && m != n)) {
                return -1;
            }
            int q = scanByte(start, m);
            if (q > start) {
                p = q;
                int q2 = scan(q, m, '.');
                if (q2 <= q) {
                    q = q2;
                } else {
                    p = q2;
                    q = scanByte(q2, m);
                    if (q > q2) {
                        p = q;
                        q2 = scan(q, m, '.');
                        if (q2 <= q) {
                            q = q2;
                        } else {
                            p = q2;
                            q = scanByte(q2, m);
                            if (q > q2) {
                                p = q;
                                q2 = scan(q, m, '.');
                                if (q2 <= q) {
                                    q = q2;
                                } else {
                                    p = q2;
                                    q = scanByte(q2, m);
                                    if (q > q2) {
                                        p = q;
                                        if (q >= m) {
                                            return q;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            fail("Malformed IPv4 address", q);
            return -1;
        }

        private int takeIPv4Address(int start, int n, String expected) throws URISyntaxException {
            int p = scanIPv4Address(start, n, true);
            if (p <= start) {
                failExpecting(expected, start);
            }
            return p;
        }

        private int parseIPv4Address(int start, int n) {
            try {
                int p = scanIPv4Address(start, n, URI.-assertionsDisabled);
                if (p > start && p < n && charAt(p) != ':') {
                    p = -1;
                }
                if (p > start) {
                    URI.this.host = substring(start, p);
                }
                return p;
            } catch (URISyntaxException e) {
                return -1;
            } catch (NumberFormatException e2) {
                return -1;
            }
        }

        private int parseHostname(int start, int n) throws URISyntaxException {
            int p = start;
            int l = -1;
            do {
                int q;
                if (p >= n || charAt(p) != '.') {
                    q = scan(p, n, URI.L_ALPHANUM, URI.H_ALPHANUM);
                    if (q <= p) {
                        break;
                    }
                    l = p;
                    if (q > p) {
                        p = q;
                        q = scan(q, n, (URI.L_ALPHANUM | URI.L_DASH) | URI.L_UNDERSCORE, (URI.H_ALPHANUM | URI.H_DASH) | URI.H_UNDERSCORE);
                        if (q > p) {
                            if (charAt(q - 1) == '-') {
                                fail("Illegal character in hostname", q - 1);
                            }
                            p = q;
                        }
                    }
                    q = scan(p, n, '.');
                    if (q <= p) {
                        break;
                    }
                    p = q;
                    continue;
                } else {
                    System.logE("URI " + substring(start, n) + " has empty labels in " + "the hostname. This is malformed and will not be accepted" + "in future Android releases.");
                    p++;
                    q = p;
                    continue;
                }
            } while (p < n);
            if (p < n && !at(p, n, ':')) {
                fail("Illegal character in hostname", p);
            }
            if (l < 0) {
                failExpecting("hostname", start);
            }
            if (l > start && !URI.match(charAt(l), URI.L_USERINFO, URI.H_ALPHA)) {
                fail("Illegal character in hostname", l);
            }
            URI.this.host = substring(start, p);
            return p;
        }

        private int parseIPv6Reference(int start, int n) throws URISyntaxException {
            int p = start;
            boolean compressedZeros = URI.-assertionsDisabled;
            int q = scanHexSeq(start, n);
            if (q > start) {
                p = q;
                if (at(q, n, "::")) {
                    compressedZeros = true;
                    p = scanHexPost(q + 2, n);
                } else if (at(q, n, ':')) {
                    p = takeIPv4Address(q + 1, n, "IPv4 address");
                    this.ipv6byteCount += 4;
                }
            } else if (at(start, n, "::")) {
                compressedZeros = true;
                p = scanHexPost(start + 2, n);
            }
            if (p < n) {
                fail("Malformed IPv6 address", start);
            }
            if (this.ipv6byteCount > 16) {
                fail("IPv6 address too long", start);
            }
            if (!compressedZeros && this.ipv6byteCount < 16) {
                fail("IPv6 address too short", start);
            }
            if (compressedZeros && this.ipv6byteCount == 16) {
                fail("Malformed IPv6 address", start);
            }
            return p;
        }

        private int scanHexPost(int start, int n) throws URISyntaxException {
            int p = start;
            if (start == n) {
                return start;
            }
            int q = scanHexSeq(start, n);
            if (q > start) {
                p = q;
                if (at(q, n, ':')) {
                    p = takeIPv4Address(q + 1, n, "hex digits or IPv4 address");
                    this.ipv6byteCount += 4;
                }
            } else {
                p = takeIPv4Address(start, n, "hex digits or IPv4 address");
                this.ipv6byteCount += 4;
            }
            return p;
        }

        private int scanHexSeq(int start, int n) throws URISyntaxException {
            int p = start;
            int q = scan(start, n, URI.L_HEX, URI.H_HEX);
            if (q <= start || at(q, n, '.')) {
                return -1;
            }
            if (q > start + 4) {
                fail("IPv6 hexadecimal digit sequence too long", start);
            }
            this.ipv6byteCount += 2;
            p = q;
            while (p < n && at(p, n, ':') && !at(p + 1, n, ':')) {
                p++;
                q = scan(p, n, URI.L_HEX, URI.H_HEX);
                if (q <= p) {
                    failExpecting("digits for an IPv6 address", p);
                }
                if (at(q, n, '.')) {
                    p--;
                    break;
                }
                if (q > p + 4) {
                    fail("IPv6 hexadecimal digit sequence too long", p);
                }
                this.ipv6byteCount += 2;
                p = q;
            }
            return p;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.URI.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.URI.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.URI.<clinit>():void");
    }

    private URI() {
        this.port = -1;
        this.decodedUserInfo = null;
        this.decodedAuthority = null;
        this.decodedPath = null;
        this.decodedQuery = null;
        this.decodedFragment = null;
        this.decodedSchemeSpecificPart = null;
    }

    public URI(String str) throws URISyntaxException {
        this.port = -1;
        this.decodedUserInfo = null;
        this.decodedAuthority = null;
        this.decodedPath = null;
        this.decodedQuery = null;
        this.decodedFragment = null;
        this.decodedSchemeSpecificPart = null;
        new Parser(str).parse(-assertionsDisabled);
    }

    public URI(String scheme, String userInfo, String host, int port, String path, String query, String fragment) throws URISyntaxException {
        this.port = -1;
        this.decodedUserInfo = null;
        this.decodedAuthority = null;
        this.decodedPath = null;
        this.decodedQuery = null;
        this.decodedFragment = null;
        this.decodedSchemeSpecificPart = null;
        String s = toString(scheme, null, null, userInfo, host, port, path, query, fragment);
        checkPath(s, scheme, path);
        new Parser(s).parse(true);
    }

    public URI(String scheme, String authority, String path, String query, String fragment) throws URISyntaxException {
        this.port = -1;
        this.decodedUserInfo = null;
        this.decodedAuthority = null;
        this.decodedPath = null;
        this.decodedQuery = null;
        this.decodedFragment = null;
        this.decodedSchemeSpecificPart = null;
        String s = toString(scheme, null, authority, null, null, -1, path, query, fragment);
        checkPath(s, scheme, path);
        new Parser(s).parse(-assertionsDisabled);
    }

    public URI(String scheme, String host, String path, String fragment) throws URISyntaxException {
        this(scheme, null, host, -1, path, null, fragment);
    }

    public URI(String scheme, String ssp, String fragment) throws URISyntaxException {
        this.port = -1;
        this.decodedUserInfo = null;
        this.decodedAuthority = null;
        this.decodedPath = null;
        this.decodedQuery = null;
        this.decodedFragment = null;
        this.decodedSchemeSpecificPart = null;
        new Parser(toString(scheme, ssp, null, null, null, -1, null, null, fragment)).parse(-assertionsDisabled);
    }

    public static URI create(String str) {
        try {
            return new URI(str);
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x.getMessage(), x);
        }
    }

    public URI parseServerAuthority() throws URISyntaxException {
        if (this.host != null || this.authority == null) {
            return this;
        }
        defineString();
        new Parser(this.string).parse(true);
        return this;
    }

    public URI normalize() {
        return normalize(this);
    }

    public URI resolve(URI uri) {
        return resolve(this, uri);
    }

    public URI resolve(String str) {
        return resolve(create(str));
    }

    public URI relativize(URI uri) {
        return relativize(this, uri);
    }

    public URL toURL() throws MalformedURLException {
        if (isAbsolute()) {
            return new URL(toString());
        }
        throw new IllegalArgumentException("URI is not absolute");
    }

    public String getScheme() {
        return this.scheme;
    }

    public boolean isAbsolute() {
        return this.scheme != null ? true : -assertionsDisabled;
    }

    public boolean isOpaque() {
        return this.path == null ? true : -assertionsDisabled;
    }

    public String getRawSchemeSpecificPart() {
        defineSchemeSpecificPart();
        return this.schemeSpecificPart;
    }

    public String getSchemeSpecificPart() {
        if (this.decodedSchemeSpecificPart == null) {
            this.decodedSchemeSpecificPart = decode(getRawSchemeSpecificPart());
        }
        return this.decodedSchemeSpecificPart;
    }

    public String getRawAuthority() {
        return this.authority;
    }

    public String getAuthority() {
        if (this.decodedAuthority == null) {
            this.decodedAuthority = decode(this.authority);
        }
        return this.decodedAuthority;
    }

    public String getRawUserInfo() {
        return this.userInfo;
    }

    public String getUserInfo() {
        if (this.decodedUserInfo == null && this.userInfo != null) {
            this.decodedUserInfo = decode(this.userInfo);
        }
        return this.decodedUserInfo;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getRawPath() {
        return this.path;
    }

    public String getPath() {
        if (this.decodedPath == null && this.path != null) {
            this.decodedPath = decode(this.path);
        }
        return this.decodedPath;
    }

    public String getRawQuery() {
        return this.query;
    }

    public String getQuery() {
        if (this.decodedQuery == null && this.query != null) {
            this.decodedQuery = decode(this.query);
        }
        return this.decodedQuery;
    }

    public String getRawFragment() {
        return this.fragment;
    }

    public String getFragment() {
        if (this.decodedFragment == null && this.fragment != null) {
            this.decodedFragment = decode(this.fragment);
        }
        return this.decodedFragment;
    }

    public boolean equals(Object ob) {
        if (ob == this) {
            return true;
        }
        if (!(ob instanceof URI)) {
            return -assertionsDisabled;
        }
        URI that = (URI) ob;
        if (isOpaque() != that.isOpaque() || !equalIgnoringCase(this.scheme, that.scheme) || !equal(this.fragment, that.fragment)) {
            return -assertionsDisabled;
        }
        if (isOpaque()) {
            return equal(this.schemeSpecificPart, that.schemeSpecificPart);
        }
        if (!equal(this.path, that.path) || !equal(this.query, that.query)) {
            return -assertionsDisabled;
        }
        if (this.authority == that.authority) {
            return true;
        }
        if (this.host != null) {
            return (equal(this.userInfo, that.userInfo) && equalIgnoringCase(this.host, that.host) && this.port == that.port) ? true : -assertionsDisabled;
        } else {
            if (this.authority != null) {
                if (!equal(this.authority, that.authority)) {
                    return -assertionsDisabled;
                }
            } else if (this.authority != that.authority) {
                return -assertionsDisabled;
            }
        }
    }

    public int hashCode() {
        if (this.hash != 0) {
            return this.hash;
        }
        int h = hash(hashIgnoringCase(0, this.scheme), this.fragment);
        if (isOpaque()) {
            h = hash(h, this.schemeSpecificPart);
        } else {
            h = hash(hash(h, this.path), this.query);
            if (this.host != null) {
                h = hashIgnoringCase(hash(h, this.userInfo), this.host) + (this.port * 1949);
            } else {
                h = hash(h, this.authority);
            }
        }
        this.hash = h;
        return h;
    }

    public int compareTo(URI that) {
        int c = compareIgnoringCase(this.scheme, that.scheme);
        if (c != 0) {
            return c;
        }
        if (isOpaque()) {
            if (!that.isOpaque()) {
                return 1;
            }
            c = compare(this.schemeSpecificPart, that.schemeSpecificPart);
            if (c != 0) {
                return c;
            }
            return compare(this.fragment, that.fragment);
        } else if (that.isOpaque()) {
            return -1;
        } else {
            if (this.host == null || that.host == null) {
                c = compare(this.authority, that.authority);
                if (c != 0) {
                    return c;
                }
            }
            c = compare(this.userInfo, that.userInfo);
            if (c != 0) {
                return c;
            }
            c = compareIgnoringCase(this.host, that.host);
            if (c != 0) {
                return c;
            }
            c = this.port - that.port;
            if (c != 0) {
                return c;
            }
            c = compare(this.path, that.path);
            if (c != 0) {
                return c;
            }
            c = compare(this.query, that.query);
            if (c != 0) {
                return c;
            }
            return compare(this.fragment, that.fragment);
        }
    }

    public String toString() {
        defineString();
        return this.string;
    }

    public String toASCIIString() {
        defineString();
        return encode(this.string);
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        defineString();
        os.defaultWriteObject();
    }

    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
        this.port = -1;
        is.defaultReadObject();
        try {
            new Parser(this.string).parse(-assertionsDisabled);
        } catch (URISyntaxException x) {
            IOException y = new InvalidObjectException("Invalid URI");
            y.initCause(x);
            throw y;
        }
    }

    private static int toLower(char c) {
        if (c < 'A' || c > 'Z') {
            return c;
        }
        return c + 32;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean equal(String s, String t) {
        if (s == t) {
            return true;
        }
        if (s == null || t == null || s.length() != t.length()) {
            return -assertionsDisabled;
        }
        if (s.indexOf(37) < 0) {
            return s.equals(t);
        }
        int n = s.length();
        int i = 0;
        while (i < n) {
            char c = s.charAt(i);
            char d = t.charAt(i);
            if (c != '%') {
                if (c != d) {
                    return -assertionsDisabled;
                }
                i++;
            } else if (d != '%') {
                return -assertionsDisabled;
            } else {
                i++;
                if (toLower(s.charAt(i)) != toLower(t.charAt(i))) {
                    return -assertionsDisabled;
                }
                i++;
                if (toLower(s.charAt(i)) != toLower(t.charAt(i))) {
                    return -assertionsDisabled;
                }
                i++;
            }
        }
        return true;
    }

    private static boolean equalIgnoringCase(String s, String t) {
        if (s == t) {
            return true;
        }
        if (s == null || t == null) {
            return -assertionsDisabled;
        }
        int n = s.length();
        if (t.length() != n) {
            return -assertionsDisabled;
        }
        for (int i = 0; i < n; i++) {
            if (toLower(s.charAt(i)) != toLower(t.charAt(i))) {
                return -assertionsDisabled;
            }
        }
        return true;
    }

    private static int hash(int hash, String s) {
        if (s == null) {
            return hash;
        }
        return (hash * FloatConsts.MAX_EXPONENT) + s.hashCode();
    }

    private static int hashIgnoringCase(int hash, String s) {
        if (s == null) {
            return hash;
        }
        int h = hash;
        for (int i = 0; i < s.length(); i++) {
            h = (h * 31) + toLower(s.charAt(i));
        }
        return h;
    }

    private static int compare(String s, String t) {
        if (s == t) {
            return 0;
        }
        if (s == null) {
            return -1;
        }
        if (t != null) {
            return s.compareTo(t);
        }
        return 1;
    }

    private static int compareIgnoringCase(String s, String t) {
        if (s == t) {
            return 0;
        }
        if (s == null) {
            return -1;
        }
        if (t == null) {
            return 1;
        }
        int sn = s.length();
        int tn = t.length();
        int n = sn < tn ? sn : tn;
        for (int i = 0; i < n; i++) {
            int c = toLower(s.charAt(i)) - toLower(t.charAt(i));
            if (c != 0) {
                return c;
            }
        }
        return sn - tn;
    }

    private static void checkPath(String s, String scheme, String path) throws URISyntaxException {
        if (scheme != null && path != null && path.length() > 0 && path.charAt(0) != '/') {
            throw new URISyntaxException(s, "Relative path in absolute URI");
        }
    }

    private void appendAuthority(StringBuffer sb, String authority, String userInfo, String host, int port) {
        if (host != null) {
            boolean needBrackets;
            sb.append("//");
            if (userInfo != null) {
                sb.append(quote(userInfo, L_USERINFO, H_USERINFO));
                sb.append('@');
            }
            if (host.indexOf(58) < 0 || host.startsWith("[")) {
                needBrackets = -assertionsDisabled;
            } else {
                needBrackets = host.endsWith("]") ? -assertionsDisabled : true;
            }
            if (needBrackets) {
                sb.append('[');
            }
            sb.append(host);
            if (needBrackets) {
                sb.append(']');
            }
            if (port != -1) {
                sb.append(':');
                sb.append(port);
            }
        } else if (authority != null) {
            sb.append("//");
            if (authority.startsWith("[")) {
                int end = authority.indexOf("]");
                String doquote = authority;
                String dontquote = "";
                if (!(end == -1 || authority.indexOf(":") == -1)) {
                    if (end == authority.length()) {
                        dontquote = authority;
                        doquote = "";
                    } else {
                        dontquote = authority.substring(0, end + 1);
                        doquote = authority.substring(end + 1);
                    }
                }
                sb.append(dontquote);
                sb.append(quote(doquote, L_REG_NAME | L_SERVER, H_REG_NAME | H_SERVER));
                return;
            }
            sb.append(quote(authority, L_REG_NAME | L_SERVER, H_REG_NAME | H_SERVER));
        }
    }

    private void appendSchemeSpecificPart(StringBuffer sb, String opaquePart, String authority, String userInfo, String host, int port, String path, String query) {
        if (opaquePart == null) {
            appendAuthority(sb, authority, userInfo, host, port);
            if (path != null) {
                sb.append(quote(path, L_PATH, H_PATH));
            }
            if (query != null) {
                sb.append('?');
                sb.append(quote(query, L_URIC, H_URIC));
            }
        } else if (opaquePart.startsWith("//[")) {
            int end = opaquePart.indexOf("]");
            if (end != -1 && opaquePart.indexOf(":") != -1) {
                String dontquote;
                String doquote;
                if (end == opaquePart.length()) {
                    dontquote = opaquePart;
                    doquote = "";
                } else {
                    dontquote = opaquePart.substring(0, end + 1);
                    doquote = opaquePart.substring(end + 1);
                }
                sb.append(dontquote);
                sb.append(quote(doquote, L_URIC, H_URIC));
            }
        } else {
            sb.append(quote(opaquePart, L_URIC, H_URIC));
        }
    }

    private void appendFragment(StringBuffer sb, String fragment) {
        if (fragment != null) {
            sb.append('#');
            sb.append(quote(fragment, L_URIC, H_URIC));
        }
    }

    private String toString(String scheme, String opaquePart, String authority, String userInfo, String host, int port, String path, String query, String fragment) {
        StringBuffer sb = new StringBuffer();
        if (scheme != null) {
            sb.append(scheme);
            sb.append(':');
        }
        appendSchemeSpecificPart(sb, opaquePart, authority, userInfo, host, port, path, query);
        appendFragment(sb, fragment);
        return sb.toString();
    }

    private void defineSchemeSpecificPart() {
        if (this.schemeSpecificPart == null) {
            StringBuffer sb = new StringBuffer();
            appendSchemeSpecificPart(sb, null, getAuthority(), getUserInfo(), this.host, this.port, getPath(), getQuery());
            if (sb.length() != 0) {
                this.schemeSpecificPart = sb.toString();
            }
        }
    }

    private void defineString() {
        boolean z = -assertionsDisabled;
        if (this.string == null) {
            StringBuffer sb = new StringBuffer();
            if (this.scheme != null) {
                sb.append(this.scheme);
                sb.append(':');
            }
            if (isOpaque()) {
                sb.append(this.schemeSpecificPart);
            } else {
                if (this.host != null) {
                    boolean needBrackets;
                    sb.append("//");
                    if (this.userInfo != null) {
                        sb.append(this.userInfo);
                        sb.append('@');
                    }
                    if (this.host.indexOf(58) < 0 || this.host.startsWith("[")) {
                        needBrackets = -assertionsDisabled;
                    } else {
                        if (!this.host.endsWith("]")) {
                            z = true;
                        }
                        needBrackets = z;
                    }
                    if (needBrackets) {
                        sb.append('[');
                    }
                    sb.append(this.host);
                    if (needBrackets) {
                        sb.append(']');
                    }
                    if (this.port != -1) {
                        sb.append(':');
                        sb.append(this.port);
                    }
                } else if (this.authority != null) {
                    sb.append("//");
                    sb.append(this.authority);
                }
                if (this.path != null) {
                    sb.append(this.path);
                }
                if (this.query != null) {
                    sb.append('?');
                    sb.append(this.query);
                }
            }
            if (this.fragment != null) {
                sb.append('#');
                sb.append(this.fragment);
            }
            this.string = sb.toString();
        }
    }

    private static String resolvePath(String base, String child, boolean absolute) {
        int i = base.lastIndexOf(47);
        int cn = child.length();
        String path = "";
        if (cn != 0) {
            StringBuffer sb = new StringBuffer(base.length() + cn);
            if (i >= 0) {
                sb.append(base.substring(0, i + 1));
            }
            sb.append(child);
            path = sb.toString();
        } else if (i >= 0) {
            path = base.substring(0, i + 1);
        }
        return normalize(path, true);
    }

    private static URI resolve(URI base, URI child) {
        if (child.isOpaque() || base.isOpaque()) {
            return child;
        }
        URI ru;
        if (child.scheme == null && child.authority == null && child.path.equals("") && child.fragment != null && child.query == null) {
            if (base.fragment != null && child.fragment.equals(base.fragment)) {
                return base;
            }
            ru = new URI();
            ru.scheme = base.scheme;
            ru.authority = base.authority;
            ru.userInfo = base.userInfo;
            ru.host = base.host;
            ru.port = base.port;
            ru.path = base.path;
            ru.fragment = child.fragment;
            ru.query = base.query;
            return ru;
        } else if (child.scheme != null) {
            return child;
        } else {
            ru = new URI();
            ru.scheme = base.scheme;
            ru.query = child.query;
            ru.fragment = child.fragment;
            if (child.authority == null) {
                ru.authority = base.authority;
                ru.host = base.host;
                ru.userInfo = base.userInfo;
                ru.port = base.port;
                if (child.path == null || child.path.isEmpty()) {
                    String str;
                    ru.path = base.path;
                    if (child.query != null) {
                        str = child.query;
                    } else {
                        str = base.query;
                    }
                    ru.query = str;
                } else if (child.path.length() <= 0 || child.path.charAt(0) != '/') {
                    ru.path = resolvePath(base.path, child.path, base.isAbsolute());
                } else {
                    ru.path = normalize(child.path, true);
                }
            } else {
                ru.authority = child.authority;
                ru.host = child.host;
                ru.userInfo = child.userInfo;
                ru.host = child.host;
                ru.port = child.port;
                ru.path = child.path;
            }
            return ru;
        }
    }

    private static URI normalize(URI u) {
        if (u.isOpaque() || u.path == null || u.path.length() == 0) {
            return u;
        }
        String np = normalize(u.path);
        if (np == u.path) {
            return u;
        }
        URI v = new URI();
        v.scheme = u.scheme;
        v.fragment = u.fragment;
        v.authority = u.authority;
        v.userInfo = u.userInfo;
        v.host = u.host;
        v.port = u.port;
        v.path = np;
        v.query = u.query;
        return v;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static URI relativize(URI base, URI child) {
        if (child.isOpaque() || base.isOpaque() || !equalIgnoringCase(base.scheme, child.scheme) || !equal(base.authority, child.authority)) {
            return child;
        }
        String bp = normalize(base.path);
        String cp = normalize(child.path);
        if (!bp.equals(cp)) {
            if (bp.indexOf(47) != -1) {
                bp = bp.substring(0, bp.lastIndexOf(47) + 1);
            }
            if (!cp.startsWith(bp)) {
                return child;
            }
        }
        URI v = new URI();
        v.path = cp.substring(bp.length());
        v.query = child.query;
        v.fragment = child.fragment;
        return v;
    }

    private static int needsNormalization(String path) {
        boolean normal = true;
        int ns = 0;
        int end = path.length() - 1;
        int p = 0;
        while (p <= end && path.charAt(p) == '/') {
            p++;
        }
        if (p > 1) {
            normal = -assertionsDisabled;
        }
        while (p <= end) {
            if (path.charAt(p) == '.' && (p == end || path.charAt(p + 1) == '/' || (path.charAt(p + 1) == '.' && (p + 1 == end || path.charAt(p + 2) == '/')))) {
                normal = -assertionsDisabled;
            }
            ns++;
            int p2 = p;
            while (p2 <= end) {
                p = p2 + 1;
                if (path.charAt(p2) != '/') {
                    p2 = p;
                } else {
                    while (p <= end) {
                        if (path.charAt(p) != '/') {
                            break;
                        }
                        normal = -assertionsDisabled;
                        p++;
                    }
                }
            }
            p = p2;
        }
        return normal ? -1 : ns;
    }

    private static void split(char[] r8, int[] r9) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r7 = 47;
        r6 = 0;
        r5 = r8.length;
        r0 = r5 + -1;
        r3 = 0;
        r1 = 0;
    L_0x0008:
        if (r3 > r0) goto L_0x0034;
    L_0x000a:
        r5 = r8[r3];
        if (r5 == r7) goto L_0x0023;
    L_0x000e:
        r2 = r1;
        r4 = r3;
    L_0x0010:
        if (r4 > r0) goto L_0x003d;
    L_0x0012:
        r1 = r2 + 1;
        r3 = r4 + 1;
        r9[r2] = r4;
        r4 = r3;
    L_0x0019:
        if (r4 > r0) goto L_0x0047;
    L_0x001b:
        r3 = r4 + 1;
        r5 = r8[r4];
        if (r5 == r7) goto L_0x0028;
    L_0x0021:
        r4 = r3;
        goto L_0x0019;
    L_0x0023:
        r8[r3] = r6;
        r3 = r3 + 1;
        goto L_0x0008;
    L_0x0028:
        r5 = r3 + -1;
        r8[r5] = r6;
        r4 = r3;
    L_0x002d:
        if (r4 > r0) goto L_0x0033;
    L_0x002f:
        r5 = r8[r4];
        if (r5 == r7) goto L_0x0037;
    L_0x0033:
        r3 = r4;
    L_0x0034:
        r2 = r1;
        r4 = r3;
        goto L_0x0010;
    L_0x0037:
        r3 = r4 + 1;
        r8[r4] = r6;
        r4 = r3;
        goto L_0x002d;
    L_0x003d:
        r5 = r9.length;
        if (r2 == r5) goto L_0x0046;
    L_0x0040:
        r5 = new java.lang.InternalError;
        r5.<init>();
        throw r5;
    L_0x0046:
        return;
    L_0x0047:
        r3 = r4;
        goto L_0x0034;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.URI.split(char[], int[]):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int join(char[] path, int[] segs) {
        int end = path.length - 1;
        int p = 0;
        if (path[0] == '\u0000') {
            p = 1;
            path[0] = '/';
        }
        for (int q : segs) {
            int q2;
            if (q2 != -1) {
                int p2;
                if (p == q2) {
                    p2 = p;
                    while (p2 <= end && path[p2] != '\u0000') {
                        p2++;
                    }
                    if (p2 <= end) {
                        p = p2 + 1;
                        path[p2] = '/';
                    } else {
                        p = p2;
                    }
                } else if (p < q2) {
                    int q3;
                    while (true) {
                        q3 = q2;
                        p2 = p;
                        if (q3 <= end && path[q3] != '\u0000') {
                            p = p2 + 1;
                            q2 = q3 + 1;
                            path[p2] = path[q3];
                        } else if (q3 > end) {
                            p = p2 + 1;
                            path[p2] = '/';
                            q2 = q3;
                        } else {
                            q2 = q3;
                            p = p2;
                        }
                    }
                    if (q3 > end) {
                        q2 = q3;
                        p = p2;
                    } else {
                        p = p2 + 1;
                        path[p2] = '/';
                        q2 = q3;
                    }
                } else {
                    throw new InternalError();
                }
            }
        }
        return p;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void removeDots(char[] path, int[] segs, boolean removeLeading) {
        int ns = segs.length;
        int end = path.length - 1;
        int i = 0;
        while (i < ns) {
            int dots = 0;
            while (true) {
                int p = segs[i];
                if (path[p] == '.') {
                    if (p == end) {
                        break;
                    } else if (path[p + 1] == '\u0000') {
                        break;
                    } else if (path[p + 1] == '.' && (p + 1 == end || path[p + 2] == '\u0000')) {
                        dots = 2;
                    }
                }
                i++;
                if (i >= ns) {
                    break;
                }
            }
            dots = 1;
            if (i <= ns && dots != 0) {
                if (dots == 1) {
                    segs[i] = -1;
                } else {
                    int j = i - 1;
                    while (j >= 0 && segs[j] == -1) {
                        j--;
                    }
                    if (j >= 0) {
                        int q = segs[j];
                        if (path[q] == '.' && path[q + 1] == '.') {
                            if (path[q + 2] != '\u0000') {
                            }
                        }
                        segs[i] = -1;
                        segs[j] = -1;
                    } else if (removeLeading) {
                        segs[i] = -1;
                    }
                }
                i++;
            } else {
                return;
            }
        }
    }

    private static void maybeAddLeadingDot(char[] path, int[] segs) {
        if (path[0] != '\u0000') {
            int ns = segs.length;
            int f = 0;
            while (f < ns && segs[f] < 0) {
                f++;
            }
            if (f < ns && f != 0) {
                int p = segs[f];
                while (p < path.length && path[p] != ':' && path[p] != '\u0000') {
                    p++;
                }
                if (p < path.length && path[p] != '\u0000') {
                    path[0] = '.';
                    path[1] = '\u0000';
                    segs[0] = 0;
                }
            }
        }
    }

    private static String normalize(String ps) {
        return normalize(ps, -assertionsDisabled);
    }

    private static String normalize(String ps, boolean removeLeading) {
        int ns = needsNormalization(ps);
        if (ns < 0) {
            return ps;
        }
        char[] path = ps.toCharArray();
        int[] segs = new int[ns];
        split(path, segs);
        removeDots(path, segs, removeLeading);
        maybeAddLeadingDot(path, segs);
        String s = new String(path, 0, join(path, segs));
        if (s.equals(ps)) {
            return ps;
        }
        return s;
    }

    private static long lowMask(String chars) {
        int n = chars.length();
        long m = L_USERINFO;
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);
            if (c < '@') {
                m |= L_ESCAPED << c;
            }
        }
        return m;
    }

    private static long highMask(String chars) {
        int n = chars.length();
        long m = L_USERINFO;
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);
            if (c >= '@' && c < '\u0080') {
                m |= L_ESCAPED << (c - 64);
            }
        }
        return m;
    }

    private static long lowMask(char first, char last) {
        long m = L_USERINFO;
        for (int i = Math.max(Math.min((int) first, 63), 0); i <= Math.max(Math.min((int) last, 63), 0); i++) {
            m |= L_ESCAPED << i;
        }
        return m;
    }

    private static long highMask(char first, char last) {
        long m = L_USERINFO;
        for (int i = Math.max(Math.min((int) first, (int) FloatConsts.MAX_EXPONENT), 64) - 64; i <= Math.max(Math.min((int) last, (int) FloatConsts.MAX_EXPONENT), 64) - 64; i++) {
            m |= L_ESCAPED << i;
        }
        return m;
    }

    private static boolean match(char c, long lowMask, long highMask) {
        boolean z = true;
        if (c == '\u0000') {
            return -assertionsDisabled;
        }
        if (c < '@') {
            if (((L_ESCAPED << c) & lowMask) == L_USERINFO) {
                z = -assertionsDisabled;
            }
            return z;
        } else if (c >= '\u0080') {
            return -assertionsDisabled;
        } else {
            if (((L_ESCAPED << (c - 64)) & highMask) == L_USERINFO) {
                z = -assertionsDisabled;
            }
            return z;
        }
    }

    private static void appendEscape(StringBuffer sb, byte b) {
        sb.append('%');
        sb.append(hexDigits[(b >> 4) & 15]);
        sb.append(hexDigits[(b >> 0) & 15]);
    }

    private static void appendEncoded(StringBuffer sb, char c) {
        ByteBuffer bb = null;
        try {
            bb = ThreadLocalCoders.encoderFor("UTF-8").encode(CharBuffer.wrap("" + c));
        } catch (CharacterCodingException e) {
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        while (bb.hasRemaining()) {
            int b = bb.get() & 255;
            if (b >= Pattern.CANON_EQ) {
                appendEscape(sb, (byte) b);
            } else {
                sb.append((char) b);
            }
        }
    }

    private static String quote(String s, long lowMask, long highMask) {
        int n = s.length();
        StringBuffer sb = null;
        boolean allowNonASCII = (L_ESCAPED & lowMask) != L_USERINFO ? true : -assertionsDisabled;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '\u0080') {
                if (!match(c, lowMask, highMask)) {
                    if (sb == null) {
                        sb = new StringBuffer();
                        sb.append(s.substring(0, i));
                    }
                    appendEscape(sb, (byte) c);
                } else if (sb != null) {
                    sb.append(c);
                }
            } else if (allowNonASCII && (Character.isSpaceChar(c) || Character.isISOControl(c))) {
                if (sb == null) {
                    sb = new StringBuffer();
                    sb.append(s.substring(0, i));
                }
                appendEncoded(sb, c);
            } else if (sb != null) {
                sb.append(c);
            }
        }
        return sb == null ? s : sb.toString();
    }

    private static String encode(String s) {
        int n = s.length();
        if (n == 0) {
            return s;
        }
        int i = 0;
        while (s.charAt(i) < '\u0080') {
            i++;
            if (i >= n) {
                return s;
            }
        }
        ByteBuffer bb = null;
        try {
            bb = ThreadLocalCoders.encoderFor("UTF-8").encode(CharBuffer.wrap(Normalizer.normalize(s, Form.NFC)));
        } catch (CharacterCodingException e) {
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        StringBuffer sb = new StringBuffer();
        while (bb.hasRemaining()) {
            int b = bb.get() & 255;
            if (b >= Pattern.CANON_EQ) {
                appendEscape(sb, (byte) b);
            } else {
                sb.append((char) b);
            }
        }
        return sb.toString();
    }

    private static int decode(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 97) + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 65) + 10;
        }
        if (-assertionsDisabled) {
            return -1;
        }
        throw new AssertionError();
    }

    private static byte decode(char c1, char c2) {
        return (byte) (((decode(c1) & 15) << 4) | ((decode(c2) & 15) << 0));
    }

    private static String decode(String s) {
        if (s == null) {
            return s;
        }
        int n = s.length();
        if (n == 0 || s.indexOf(37) < 0) {
            return s;
        }
        StringBuffer sb = new StringBuffer(n);
        ByteBuffer bb = ByteBuffer.allocate(n);
        CharBuffer cb = CharBuffer.allocate(n);
        CharsetDecoder dec = ThreadLocalCoders.decoderFor("UTF-8").onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        char c = s.charAt(0);
        boolean betweenBrackets = -assertionsDisabled;
        int i = 0;
        while (i < n) {
            if (!-assertionsDisabled) {
                if (!(c == s.charAt(i))) {
                    throw new AssertionError();
                }
            }
            if (c == '[') {
                betweenBrackets = true;
            } else if (betweenBrackets && c == ']') {
                betweenBrackets = -assertionsDisabled;
            }
            if (c != '%' || betweenBrackets) {
                sb.append(c);
                i++;
                if (i >= n) {
                    break;
                }
                c = s.charAt(i);
            } else {
                bb.clear();
                int ui = i;
                do {
                    if (!-assertionsDisabled) {
                        if (!(n - i >= 2)) {
                            throw new AssertionError();
                        }
                    }
                    i++;
                    char charAt = s.charAt(i);
                    i++;
                    bb.put(decode(charAt, s.charAt(i)));
                    i++;
                    if (i >= n) {
                        break;
                    }
                    c = s.charAt(i);
                } while (c == '%');
                bb.flip();
                cb.clear();
                dec.reset();
                CoderResult cr = dec.decode(bb, cb, true);
                if (-assertionsDisabled || cr.isUnderflow()) {
                    cr = dec.flush(cb);
                    if (-assertionsDisabled || cr.isUnderflow()) {
                        sb.append(cb.flip().toString());
                    } else {
                        throw new AssertionError();
                    }
                }
                throw new AssertionError();
            }
        }
        return sb.toString();
    }
}
