package sun.nio.fs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;

class UnixUriUtils {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long H_ALPHA = (H_LOWALPHA | H_UPALPHA);
    private static final long H_ALPHANUM = (H_ALPHA | 0);
    private static final long H_DIGIT = 0;
    private static final long H_LOWALPHA = highMask('a', 'z');
    private static final long H_MARK = highMask("-_.!~*'()");
    private static final long H_PATH = (H_PCHAR | highMask(";/"));
    private static final long H_PCHAR = (H_UNRESERVED | highMask(":@&=+$,"));
    private static final long H_UNRESERVED = (H_ALPHANUM | H_MARK);
    private static final long H_UPALPHA = highMask('A', 'Z');
    private static final long L_ALPHA = 0;
    private static final long L_ALPHANUM = (L_DIGIT | 0);
    private static final long L_DIGIT = lowMask('0', '9');
    private static final long L_LOWALPHA = 0;
    private static final long L_MARK = lowMask("-_.!~*'()");
    private static final long L_PATH = (L_PCHAR | lowMask(";/"));
    private static final long L_PCHAR = (L_UNRESERVED | lowMask(":@&=+$,"));
    private static final long L_UNRESERVED = (L_ALPHANUM | L_MARK);
    private static final long L_UPALPHA = 0;
    private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private UnixUriUtils() {
    }

    static Path fromUri(UnixFileSystem fs, URI uri) {
        byte b;
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("URI is not absolute");
        } else if (!uri.isOpaque()) {
            String scheme = uri.getScheme();
            if (scheme == null || !scheme.equalsIgnoreCase("file")) {
                throw new IllegalArgumentException("URI scheme is not \"file\"");
            } else if (uri.getAuthority() != null) {
                throw new IllegalArgumentException("URI has an authority component");
            } else if (uri.getFragment() != null) {
                throw new IllegalArgumentException("URI has a fragment component");
            } else if (uri.getQuery() != null) {
                throw new IllegalArgumentException("URI has a query component");
            } else if (!uri.toString().startsWith("file:///")) {
                return new File(uri).toPath();
            } else {
                String p = uri.getRawPath();
                int len = p.length();
                if (len != 0) {
                    if (p.endsWith("/") && len > 1) {
                        len--;
                    }
                    byte[] result = new byte[len];
                    int rlen = 0;
                    int pos = 0;
                    while (pos < len) {
                        int pos2 = pos + 1;
                        char c = p.charAt(pos);
                        if (c == '%') {
                            int pos3 = pos2 + 1;
                            int pos4 = pos3 + 1;
                            b = (byte) ((decode(p.charAt(pos2)) << 4) | decode(p.charAt(pos3)));
                            if (b != 0) {
                                pos2 = pos4;
                            } else {
                                throw new IllegalArgumentException("Nul character not allowed");
                            }
                        } else {
                            b = (byte) c;
                        }
                        result[rlen] = b;
                        pos = pos2;
                        rlen++;
                    }
                    if (rlen != result.length) {
                        result = Arrays.copyOf(result, rlen);
                    }
                    return new UnixPath(fs, result);
                }
                throw new IllegalArgumentException("URI path component is empty");
            }
        } else {
            throw new IllegalArgumentException("URI is not hierarchical");
        }
    }

    static URI toUri(UnixPath up) {
        byte[] path = up.toAbsolutePath().asByteArray();
        StringBuilder sb = new StringBuilder("file:///");
        for (int i = 1; i < path.length; i++) {
            char c = (char) (path[i] & Character.DIRECTIONALITY_UNDEFINED);
            if (match(c, L_PATH, H_PATH)) {
                sb.append(c);
            } else {
                sb.append('%');
                sb.append(hexDigits[(c >> 4) & 15]);
                sb.append(hexDigits[c & 15]);
            }
        }
        if (sb.charAt(sb.length() - 1) != '/') {
            try {
                if (UnixFileAttributes.get(up, true).isDirectory()) {
                    sb.append('/');
                }
            } catch (UnixException e) {
            }
        }
        try {
            return new URI(sb.toString());
        } catch (URISyntaxException x) {
            throw new AssertionError((Object) x);
        }
    }

    private static long lowMask(String chars) {
        int n = chars.length();
        long m = 0;
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);
            if (c < '@') {
                m |= 1 << c;
            }
        }
        return m;
    }

    private static long highMask(String chars) {
        int n = chars.length();
        long m = 0;
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);
            if (c >= '@' && c < 128) {
                m |= 1 << (c - '@');
            }
        }
        return m;
    }

    private static long lowMask(char first, char last) {
        long m = 0;
        for (int i = Math.max(Math.min((int) first, 63), 0); i <= Math.max(Math.min((int) last, 63), 0); i++) {
            m |= 1 << i;
        }
        return m;
    }

    private static long highMask(char first, char last) {
        int l = Math.max(Math.min((int) last, 127), 64) - 64;
        long m = 0;
        for (int i = Math.max(Math.min((int) first, 127), 64) - 64; i <= l; i++) {
            m |= 1 << i;
        }
        return m;
    }

    private static boolean match(char c, long lowMask, long highMask) {
        boolean z = true;
        if (c < '@') {
            if (((1 << c) & lowMask) == 0) {
                z = false;
            }
            return z;
        } else if (c >= 128) {
            return false;
        } else {
            if (((1 << (c - '@')) & highMask) == 0) {
                z = false;
            }
            return z;
        }
    }

    private static int decode(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 'a') + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 'A') + 10;
        }
        throw new AssertionError();
    }
}
