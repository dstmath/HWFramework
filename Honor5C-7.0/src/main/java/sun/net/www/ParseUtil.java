package sun.net.www;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.BitSet;
import java.util.regex.Pattern;
import sun.misc.FloatConsts;
import sun.nio.cs.ThreadLocalCoders;
import sun.util.logging.PlatformLogger;

public class ParseUtil {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final long H_ALPHA = 0;
    private static final long H_ALPHANUM = 0;
    private static final long H_DASH = 0;
    private static final long H_DIGIT = 0;
    private static final long H_ESCAPED = 0;
    private static final long H_HEX = 0;
    private static final long H_LOWALPHA = 0;
    private static final long H_MARK = 0;
    private static final long H_PATH = 0;
    private static final long H_PCHAR = 0;
    private static final long H_REG_NAME = 0;
    private static final long H_RESERVED = 0;
    private static final long H_SERVER = 0;
    private static final long H_UNRESERVED = 0;
    private static final long H_UPALPHA = 0;
    private static final long H_URIC = 0;
    private static final long H_USERINFO = 0;
    private static final long L_ALPHA = 0;
    private static final long L_ALPHANUM = 0;
    private static final long L_DASH = 0;
    private static final long L_DIGIT = 0;
    private static final long L_ESCAPED = 1;
    private static final long L_HEX = 0;
    private static final long L_LOWALPHA = 0;
    private static final long L_MARK = 0;
    private static final long L_PATH = 0;
    private static final long L_PCHAR = 0;
    private static final long L_REG_NAME = 0;
    private static final long L_RESERVED = 0;
    private static final long L_SERVER = 0;
    private static final long L_UNRESERVED = 0;
    private static final long L_UPALPHA = 0;
    private static final long L_URIC = 0;
    private static final long L_USERINFO = 0;
    static BitSet encodedInPath;
    private static final char[] hexDigits = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.ParseUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.ParseUtil.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.ParseUtil.<clinit>():void");
    }

    public static String encodePath(String path) {
        return encodePath(path, true);
    }

    public static String encodePath(String path, boolean flag) {
        char[] retCC = new char[((path.length() * 2) + 16)];
        char[] pathCC = path.toCharArray();
        int n = path.length();
        int i = 0;
        int retLen = 0;
        while (i < n) {
            int retLen2;
            char c = pathCC[i];
            if ((!flag && c == '/') || (flag && c == File.separatorChar)) {
                retLen2 = retLen + 1;
                retCC[retLen] = '/';
            } else if (c <= '\u007f') {
                if ((c >= 'a' && c <= 'z') || ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                    retLen2 = retLen + 1;
                    retCC[retLen] = c;
                } else if (encodedInPath.get(c)) {
                    retLen2 = escape(retCC, c, retLen);
                } else {
                    retLen2 = retLen + 1;
                    retCC[retLen] = c;
                }
            } else if (c > '\u07ff') {
                retLen2 = escape(retCC, (char) (((c >> 0) & 63) | Pattern.CANON_EQ), escape(retCC, (char) (((c >> 6) & 63) | Pattern.CANON_EQ), escape(retCC, (char) (((c >> 12) & 15) | 224), retLen)));
            } else {
                retLen2 = escape(retCC, (char) (((c >> 0) & 63) | Pattern.CANON_EQ), escape(retCC, (char) (((c >> 6) & 31) | 192), retLen));
            }
            if (retLen2 + 9 > retCC.length) {
                int newLen = (retCC.length * 2) + 16;
                if (newLen < 0) {
                    newLen = PlatformLogger.OFF;
                }
                char[] buf = new char[newLen];
                System.arraycopy(retCC, 0, buf, 0, retLen2);
                retCC = buf;
            }
            i++;
            retLen = retLen2;
        }
        return new String(retCC, 0, retLen);
    }

    private static int escape(char[] cc, char c, int index) {
        int i = index + 1;
        cc[index] = '%';
        index = i + 1;
        cc[i] = Character.forDigit((c >> 4) & 15, 16);
        i = index + 1;
        cc[index] = Character.forDigit(c & 15, 16);
        return i;
    }

    private static byte unescape(String s, int i) {
        return (byte) Integer.parseInt(s.substring(i + 1, i + 3), 16);
    }

    public static String decode(String s) {
        int n = s.length();
        if (n == 0 || s.indexOf(37) < 0) {
            return s;
        }
        StringBuilder sb = new StringBuilder(n);
        ByteBuffer bb = ByteBuffer.allocate(n);
        CharBuffer cb = CharBuffer.allocate(n);
        CharsetDecoder dec = ThreadLocalCoders.decoderFor("UTF-8").onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        char c = s.charAt(0);
        int i = 0;
        while (i < n) {
            if (!-assertionsDisabled) {
                if (!(c == s.charAt(i))) {
                    throw new AssertionError();
                }
            }
            if (c != '%') {
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
                    try {
                        bb.put(unescape(s, i));
                        i += 3;
                        if (i >= n) {
                            break;
                        }
                        c = s.charAt(i);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException();
                    }
                } while (c == '%');
                bb.flip();
                cb.clear();
                dec.reset();
                if (dec.decode(bb, cb, true).isError()) {
                    throw new IllegalArgumentException("Error decoding percent encoded characters");
                } else if (dec.flush(cb).isError()) {
                    throw new IllegalArgumentException("Error decoding percent encoded characters");
                } else {
                    sb.append(cb.flip().toString());
                }
            }
        }
        return sb.toString();
    }

    public String canonizeString(String file) {
        int lim = file.length();
        while (true) {
            int i = file.indexOf("/../");
            if (i < 0) {
                break;
            }
            lim = file.lastIndexOf(47, i - 1);
            if (lim >= 0) {
                file = file.substring(0, lim) + file.substring(i + 3);
            } else {
                file = file.substring(i + 3);
            }
        }
        while (true) {
            i = file.indexOf("/./");
            if (i < 0) {
                break;
            }
            file = file.substring(0, i) + file.substring(i + 2);
        }
        while (file.endsWith("/..")) {
            i = file.indexOf("/..");
            lim = file.lastIndexOf(47, i - 1);
            if (lim >= 0) {
                file = file.substring(0, lim + 1);
            } else {
                file = file.substring(0, i);
            }
        }
        if (file.endsWith("/.")) {
            return file.substring(0, file.length() - 1);
        }
        return file;
    }

    public static URL fileToEncodedURL(File file) throws MalformedURLException {
        String path = encodePath(file.getAbsolutePath());
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/") && file.isDirectory()) {
            path = path + "/";
        }
        return new URL("file", "", path);
    }

    public static URI toURI(URL url) {
        String protocol = url.getProtocol();
        String auth = url.getAuthority();
        String path = url.getPath();
        String query = url.getQuery();
        String ref = url.getRef();
        if (!(path == null || path.startsWith("/"))) {
            path = "/" + path;
        }
        if (auth != null && auth.endsWith(":-1")) {
            auth = auth.substring(0, auth.length() - 3);
        }
        try {
            return createURI(protocol, auth, path, query, ref);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static URI createURI(String scheme, String authority, String path, String query, String fragment) throws URISyntaxException {
        String s = toString(scheme, null, authority, null, null, -1, path, query, fragment);
        checkPath(s, scheme, path);
        return new URI(s);
    }

    private static String toString(String scheme, String opaquePart, String authority, String userInfo, String host, int port, String path, String query, String fragment) {
        StringBuffer sb = new StringBuffer();
        if (scheme != null) {
            sb.append(scheme);
            sb.append(':');
        }
        appendSchemeSpecificPart(sb, opaquePart, authority, userInfo, host, port, path, query);
        appendFragment(sb, fragment);
        return sb.toString();
    }

    private static void appendSchemeSpecificPart(StringBuffer sb, String opaquePart, String authority, String userInfo, String host, int port, String path, String query) {
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

    private static void appendAuthority(StringBuffer sb, String authority, String userInfo, String host, int port) {
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
                if (end != -1 && authority.indexOf(":") != -1) {
                    String dontquote;
                    String doquote;
                    if (end == authority.length()) {
                        dontquote = authority;
                        doquote = "";
                    } else {
                        dontquote = authority.substring(0, end + 1);
                        doquote = authority.substring(end + 1);
                    }
                    sb.append(dontquote);
                    sb.append(quote(doquote, L_REG_NAME | L_SERVER, H_REG_NAME | H_SERVER));
                    return;
                }
                return;
            }
            sb.append(quote(authority, L_REG_NAME | L_SERVER, H_REG_NAME | H_SERVER));
        }
    }

    private static void appendFragment(StringBuffer sb, String fragment) {
        if (fragment != null) {
            sb.append('#');
            sb.append(quote(fragment, L_URIC, H_URIC));
        }
    }

    private static String quote(String s, long lowMask, long highMask) {
        int n = s.length();
        StringBuffer sb = null;
        boolean allowNonASCII = (L_ESCAPED & lowMask) != L_DIGIT ? true : -assertionsDisabled;
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c < '\u0080') {
                if (!match(c, lowMask, highMask) && !isEscaped(s, i)) {
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
            i++;
        }
        return sb == null ? s : sb.toString();
    }

    private static boolean isEscaped(String s, int pos) {
        boolean z = -assertionsDisabled;
        if (s == null || s.length() <= pos + 2) {
            return -assertionsDisabled;
        }
        if (s.charAt(pos) == '%' && match(s.charAt(pos + 1), L_HEX, H_HEX)) {
            z = match(s.charAt(pos + 2), L_HEX, H_HEX);
        }
        return z;
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

    private static void appendEscape(StringBuffer sb, byte b) {
        sb.append('%');
        sb.append(hexDigits[(b >> 4) & 15]);
        sb.append(hexDigits[(b >> 0) & 15]);
    }

    private static boolean match(char c, long lowMask, long highMask) {
        boolean z = true;
        if (c < '@') {
            if (((L_ESCAPED << c) & lowMask) == L_DIGIT) {
                z = -assertionsDisabled;
            }
            return z;
        } else if (c >= '\u0080') {
            return -assertionsDisabled;
        } else {
            if (((L_ESCAPED << (c - 64)) & highMask) == L_DIGIT) {
                z = -assertionsDisabled;
            }
            return z;
        }
    }

    private static void checkPath(String s, String scheme, String path) throws URISyntaxException {
        if (scheme != null && path != null && path.length() > 0 && path.charAt(0) != '/') {
            throw new URISyntaxException(s, "Relative path in absolute URI");
        }
    }

    private static long lowMask(char first, char last) {
        long m = L_DIGIT;
        for (int i = Math.max(Math.min((int) first, 63), 0); i <= Math.max(Math.min((int) last, 63), 0); i++) {
            m |= L_ESCAPED << i;
        }
        return m;
    }

    private static long lowMask(String chars) {
        int n = chars.length();
        long m = L_DIGIT;
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);
            if (c < '@') {
                m |= L_ESCAPED << c;
            }
        }
        return m;
    }

    private static long highMask(char first, char last) {
        long m = L_DIGIT;
        for (int i = Math.max(Math.min((int) first, (int) FloatConsts.MAX_EXPONENT), 64) - 64; i <= Math.max(Math.min((int) last, (int) FloatConsts.MAX_EXPONENT), 64) - 64; i++) {
            m |= L_ESCAPED << i;
        }
        return m;
    }

    private static long highMask(String chars) {
        int n = chars.length();
        long m = L_DIGIT;
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);
            if (c >= '@' && c < '\u0080') {
                m |= L_ESCAPED << (c - 64);
            }
        }
        return m;
    }
}
