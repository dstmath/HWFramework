package ohos.com.sun.org.apache.xerces.internal.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.global.icu.text.Bidi;

public class URI implements Serializable {
    private static final int ASCII_ALPHA_CHARACTERS = 16;
    private static final int ASCII_DIGIT_CHARACTERS = 32;
    private static final int ASCII_HEX_CHARACTERS = 64;
    private static boolean DEBUG = false;
    private static final int MARK_CHARACTERS = 2;
    private static final int MASK_ALPHA_NUMERIC = 48;
    private static final int MASK_PATH_CHARACTER = 178;
    private static final int MASK_SCHEME_CHARACTER = 52;
    private static final int MASK_UNRESERVED_MASK = 50;
    private static final int MASK_URI_CHARACTER = 51;
    private static final int MASK_USERINFO_CHARACTER = 58;
    private static final int PATH_CHARACTERS = 128;
    private static final int RESERVED_CHARACTERS = 1;
    private static final int SCHEME_CHARACTERS = 4;
    private static final int USERINFO_CHARACTERS = 8;
    private static final byte[] fgLookupTable = new byte[128];
    static final long serialVersionUID = 1601921774685357214L;
    private String m_fragment;
    private String m_host;
    private String m_path;
    private int m_port;
    private String m_queryString;
    private String m_regAuthority;
    private String m_scheme;
    private String m_userinfo;

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static class MalformedURIException extends IOException {
        static final long serialVersionUID = -6695054834342951930L;

        public MalformedURIException() {
        }

        public MalformedURIException(String str) {
            super(str);
        }
    }

    static {
        for (int i = 48; i <= 57; i++) {
            byte[] bArr = fgLookupTable;
            bArr[i] = (byte) (bArr[i] | 96);
        }
        for (int i2 = 65; i2 <= 70; i2++) {
            byte[] bArr2 = fgLookupTable;
            bArr2[i2] = (byte) (bArr2[i2] | 80);
            int i3 = i2 + 32;
            bArr2[i3] = (byte) (bArr2[i3] | 80);
        }
        for (int i4 = 71; i4 <= 90; i4++) {
            byte[] bArr3 = fgLookupTable;
            bArr3[i4] = (byte) (bArr3[i4] | 16);
            int i5 = i4 + 32;
            bArr3[i5] = (byte) (bArr3[i5] | 16);
        }
        byte[] bArr4 = fgLookupTable;
        bArr4[59] = (byte) (bArr4[59] | 1);
        bArr4[47] = (byte) (bArr4[47] | 1);
        bArr4[63] = (byte) (bArr4[63] | 1);
        bArr4[58] = (byte) (bArr4[58] | 1);
        bArr4[64] = (byte) (bArr4[64] | 1);
        bArr4[38] = (byte) (bArr4[38] | 1);
        bArr4[61] = (byte) (bArr4[61] | 1);
        bArr4[43] = (byte) (bArr4[43] | 1);
        bArr4[36] = (byte) (bArr4[36] | 1);
        bArr4[44] = (byte) (bArr4[44] | 1);
        bArr4[91] = (byte) (bArr4[91] | 1);
        bArr4[93] = (byte) (bArr4[93] | 1);
        bArr4[45] = (byte) (bArr4[45] | 2);
        bArr4[95] = (byte) (bArr4[95] | 2);
        bArr4[46] = (byte) (bArr4[46] | 2);
        bArr4[33] = (byte) (bArr4[33] | 2);
        bArr4[126] = (byte) (bArr4[126] | 2);
        bArr4[42] = (byte) (bArr4[42] | 2);
        bArr4[39] = (byte) (bArr4[39] | 2);
        bArr4[40] = (byte) (bArr4[40] | 2);
        bArr4[41] = (byte) (bArr4[41] | 2);
        bArr4[43] = (byte) (bArr4[43] | 4);
        bArr4[45] = (byte) (bArr4[45] | 4);
        bArr4[46] = (byte) (bArr4[46] | 4);
        bArr4[59] = (byte) (bArr4[59] | 8);
        bArr4[58] = (byte) (bArr4[58] | 8);
        bArr4[38] = (byte) (bArr4[38] | 8);
        bArr4[61] = (byte) (bArr4[61] | 8);
        bArr4[43] = (byte) (bArr4[43] | 8);
        bArr4[36] = (byte) (bArr4[36] | 8);
        bArr4[44] = (byte) (bArr4[44] | 8);
        bArr4[59] = (byte) (bArr4[59] | Bidi.LEVEL_OVERRIDE);
        bArr4[47] = (byte) (bArr4[47] | Bidi.LEVEL_OVERRIDE);
        bArr4[58] = (byte) (bArr4[58] | Bidi.LEVEL_OVERRIDE);
        bArr4[64] = (byte) (bArr4[64] | Bidi.LEVEL_OVERRIDE);
        bArr4[38] = (byte) (bArr4[38] | Bidi.LEVEL_OVERRIDE);
        bArr4[61] = (byte) (bArr4[61] | Bidi.LEVEL_OVERRIDE);
        bArr4[43] = (byte) (bArr4[43] | Bidi.LEVEL_OVERRIDE);
        bArr4[36] = (byte) (bArr4[36] | Bidi.LEVEL_OVERRIDE);
        bArr4[44] = (byte) (128 | bArr4[44]);
    }

    public URI() {
        this.m_scheme = null;
        this.m_userinfo = null;
        this.m_host = null;
        this.m_port = -1;
        this.m_regAuthority = null;
        this.m_path = null;
        this.m_queryString = null;
        this.m_fragment = null;
    }

    public URI(URI uri) {
        this.m_scheme = null;
        this.m_userinfo = null;
        this.m_host = null;
        this.m_port = -1;
        this.m_regAuthority = null;
        this.m_path = null;
        this.m_queryString = null;
        this.m_fragment = null;
        initialize(uri);
    }

    public URI(String str) throws MalformedURIException {
        this((URI) null, str);
    }

    public URI(String str, boolean z) throws MalformedURIException {
        this(null, str, z);
    }

    public URI(URI uri, String str) throws MalformedURIException {
        this.m_scheme = null;
        this.m_userinfo = null;
        this.m_host = null;
        this.m_port = -1;
        this.m_regAuthority = null;
        this.m_path = null;
        this.m_queryString = null;
        this.m_fragment = null;
        initialize(uri, str);
    }

    public URI(URI uri, String str, boolean z) throws MalformedURIException {
        this.m_scheme = null;
        this.m_userinfo = null;
        this.m_host = null;
        this.m_port = -1;
        this.m_regAuthority = null;
        this.m_path = null;
        this.m_queryString = null;
        this.m_fragment = null;
        initialize(uri, str, z);
    }

    public URI(String str, String str2) throws MalformedURIException {
        this.m_scheme = null;
        this.m_userinfo = null;
        this.m_host = null;
        this.m_port = -1;
        this.m_regAuthority = null;
        this.m_path = null;
        this.m_queryString = null;
        this.m_fragment = null;
        if (str == null || str.trim().length() == 0) {
            throw new MalformedURIException("Cannot construct URI with null/empty scheme!");
        } else if (str2 == null || str2.trim().length() == 0) {
            throw new MalformedURIException("Cannot construct URI with null/empty scheme-specific part!");
        } else {
            setScheme(str);
            setPath(str2);
        }
    }

    public URI(String str, String str2, String str3, String str4, String str5) throws MalformedURIException {
        this(str, null, str2, -1, str3, str4, str5);
    }

    public URI(String str, String str2, String str3, int i, String str4, String str5, String str6) throws MalformedURIException {
        this.m_scheme = null;
        this.m_userinfo = null;
        this.m_host = null;
        this.m_port = -1;
        this.m_regAuthority = null;
        this.m_path = null;
        this.m_queryString = null;
        this.m_fragment = null;
        if (str == null || str.trim().length() == 0) {
            throw new MalformedURIException("Scheme is required!");
        }
        if (str3 == null) {
            if (str2 != null) {
                throw new MalformedURIException("Userinfo may not be specified if host is not specified!");
            } else if (i != -1) {
                throw new MalformedURIException("Port may not be specified if host is not specified!");
            }
        }
        if (str4 != null) {
            if (str4.indexOf(63) != -1 && str5 != null) {
                throw new MalformedURIException("Query string cannot be specified in path and query string!");
            } else if (!(str4.indexOf(35) == -1 || str6 == null)) {
                throw new MalformedURIException("Fragment cannot be specified in both the path and fragment!");
            }
        }
        setScheme(str);
        setHost(str3);
        setPort(i);
        setUserinfo(str2);
        setPath(str4);
        setQueryString(str5);
        setFragment(str6);
    }

    private void initialize(URI uri) {
        this.m_scheme = uri.getScheme();
        this.m_userinfo = uri.getUserinfo();
        this.m_host = uri.getHost();
        this.m_port = uri.getPort();
        this.m_regAuthority = uri.getRegBasedAuthority();
        this.m_path = uri.getPath();
        this.m_queryString = uri.getQueryString();
        this.m_fragment = uri.getFragment();
    }

    private void initialize(URI uri, String str, boolean z) throws MalformedURIException {
        int i = 0;
        int length = str != null ? str.length() : 0;
        if (uri == null && length == 0) {
            if (z) {
                this.m_path = "";
                return;
            }
            throw new MalformedURIException("Cannot initialize URI with empty parameters.");
        } else if (length == 0) {
            initialize(uri);
        } else {
            int indexOf = str.indexOf(58);
            if (indexOf != -1) {
                int i2 = indexOf - 1;
                int lastIndexOf = str.lastIndexOf(47, i2);
                int lastIndexOf2 = str.lastIndexOf(63, i2);
                int lastIndexOf3 = str.lastIndexOf(35, i2);
                if (indexOf != 0 && lastIndexOf == -1 && lastIndexOf2 == -1 && lastIndexOf3 == -1) {
                    initializeScheme(str);
                    int length2 = this.m_scheme.length() + 1;
                    if (indexOf == length - 1 || str.charAt(indexOf + 1) == '#') {
                        throw new MalformedURIException("Scheme specific part cannot be empty.");
                    }
                    i = length2;
                } else if (indexOf == 0 || (uri == null && lastIndexOf3 != 0 && !z)) {
                    throw new MalformedURIException("No scheme found in URI.");
                }
            } else if (uri == null && str.indexOf(35) != 0 && !z) {
                throw new MalformedURIException("No scheme found in URI.");
            }
            int i3 = i + 1;
            if (i3 < length && str.charAt(i) == '/' && str.charAt(i3) == '/') {
                int i4 = i + 2;
                int i5 = i4;
                while (i5 < length) {
                    char charAt = str.charAt(i5);
                    if (charAt == '/' || charAt == '?' || charAt == '#') {
                        break;
                    }
                    i5++;
                }
                if (i5 <= i4) {
                    this.m_host = "";
                } else if (!initializeAuthority(str.substring(i4, i5))) {
                    i = i4 - 2;
                }
                i = i5;
            }
            initializePath(str, i);
            if (uri != null) {
                absolutize(uri);
            }
        }
    }

    private void initialize(URI uri, String str) throws MalformedURIException {
        int i = 0;
        int length = str != null ? str.length() : 0;
        if (uri == null && length == 0) {
            throw new MalformedURIException("Cannot initialize URI with empty parameters.");
        } else if (length == 0) {
            initialize(uri);
        } else {
            int indexOf = str.indexOf(58);
            if (indexOf != -1) {
                int i2 = indexOf - 1;
                int lastIndexOf = str.lastIndexOf(47, i2);
                int lastIndexOf2 = str.lastIndexOf(63, i2);
                int lastIndexOf3 = str.lastIndexOf(35, i2);
                if (indexOf != 0 && lastIndexOf == -1 && lastIndexOf2 == -1 && lastIndexOf3 == -1) {
                    initializeScheme(str);
                    i = this.m_scheme.length() + 1;
                    if (indexOf == length - 1 || str.charAt(indexOf + 1) == '#') {
                        throw new MalformedURIException("Scheme specific part cannot be empty.");
                    }
                } else if (indexOf == 0 || (uri == null && lastIndexOf3 != 0)) {
                    throw new MalformedURIException("No scheme found in URI.");
                }
            } else if (uri == null && str.indexOf(35) != 0) {
                throw new MalformedURIException("No scheme found in URI.");
            }
            int i3 = i + 1;
            if (i3 < length && str.charAt(i) == '/' && str.charAt(i3) == '/') {
                int i4 = i + 2;
                int i5 = i4;
                while (i5 < length) {
                    char charAt = str.charAt(i5);
                    if (charAt == '/' || charAt == '?' || charAt == '#') {
                        break;
                    }
                    i5++;
                }
                if (i5 > i4) {
                    if (!initializeAuthority(str.substring(i4, i5))) {
                        i = i4 - 2;
                    }
                } else if (i5 < length) {
                    this.m_host = "";
                } else {
                    throw new MalformedURIException("Expected authority.");
                }
                i = i5;
            }
            initializePath(str, i);
            if (uri != null) {
                absolutize(uri);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00b0, code lost:
        if (r8.m_path.length() > 0) goto L_0x00b4;
     */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00c2 A[LOOP:0: B:35:0x00ba->B:37:0x00c2, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00ed  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00d3 A[EDGE_INSN: B:56:0x00d3->B:38:0x00d3 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x011c A[EDGE_INSN: B:58:0x011c->B:50:0x011c ?: BREAK  , SYNTHETIC] */
    public void absolutize(URI uri) {
        String concat;
        int indexOf;
        int i;
        int indexOf2;
        int lastIndexOf;
        if (this.m_path.length() == 0 && this.m_scheme == null && this.m_host == null && this.m_regAuthority == null) {
            this.m_scheme = uri.getScheme();
            this.m_userinfo = uri.getUserinfo();
            this.m_host = uri.getHost();
            this.m_port = uri.getPort();
            this.m_regAuthority = uri.getRegBasedAuthority();
            this.m_path = uri.getPath();
            if (this.m_queryString == null) {
                this.m_queryString = uri.getQueryString();
                if (this.m_fragment == null) {
                    this.m_fragment = uri.getFragment();
                }
            }
        } else if (this.m_scheme == null) {
            this.m_scheme = uri.getScheme();
            if (this.m_host == null && this.m_regAuthority == null) {
                this.m_userinfo = uri.getUserinfo();
                this.m_host = uri.getHost();
                this.m_port = uri.getPort();
                this.m_regAuthority = uri.getRegBasedAuthority();
                int length = this.m_path.length();
                String str = PsuedoNames.PSEUDONAME_ROOT;
                if (length <= 0 || !this.m_path.startsWith(str)) {
                    String path = uri.getPath();
                    if (path != null && path.length() > 0) {
                        int lastIndexOf2 = path.lastIndexOf(47);
                        if (lastIndexOf2 != -1) {
                            str = path.substring(0, lastIndexOf2 + 1);
                            concat = str.concat(this.m_path);
                            while (true) {
                                indexOf = concat.indexOf("/./");
                                if (indexOf != -1) {
                                    break;
                                }
                                concat = concat.substring(0, indexOf + 1).concat(concat.substring(indexOf + 3));
                            }
                            if (concat.endsWith("/.")) {
                                concat = concat.substring(0, concat.length() - 1);
                            }
                            i = 1;
                            while (true) {
                                indexOf2 = concat.indexOf("/../", i);
                                if (indexOf2 > 0) {
                                    break;
                                }
                                String substring = concat.substring(0, concat.indexOf("/../"));
                                int lastIndexOf3 = substring.lastIndexOf(47);
                                if (lastIndexOf3 == -1 || substring.substring(lastIndexOf3).equals(Constants.ATTRVAL_PARENT)) {
                                    i = indexOf2 + 4;
                                } else {
                                    concat = concat.substring(0, lastIndexOf3 + 1).concat(concat.substring(indexOf2 + 4));
                                    i = lastIndexOf3;
                                }
                            }
                            if (concat.endsWith("/..") && (lastIndexOf = concat.substring(0, concat.length() - 3).lastIndexOf(47)) != -1) {
                                concat = concat.substring(0, lastIndexOf + 1);
                            }
                            this.m_path = concat;
                        }
                    }
                    str = "";
                    concat = str.concat(this.m_path);
                    while (true) {
                        indexOf = concat.indexOf("/./");
                        if (indexOf != -1) {
                        }
                        concat = concat.substring(0, indexOf + 1).concat(concat.substring(indexOf + 3));
                    }
                    if (concat.endsWith("/.")) {
                    }
                    i = 1;
                    while (true) {
                        indexOf2 = concat.indexOf("/../", i);
                        if (indexOf2 > 0) {
                        }
                    }
                    concat = concat.substring(0, lastIndexOf + 1);
                    this.m_path = concat;
                }
            }
        }
    }

    private void initializeScheme(String str) throws MalformedURIException {
        int length = str.length();
        int i = 0;
        while (i < length) {
            char charAt = str.charAt(i);
            if (charAt == ':' || charAt == '/' || charAt == '?' || charAt == '#') {
                break;
            }
            i++;
        }
        String substring = str.substring(0, i);
        if (substring.length() != 0) {
            setScheme(substring);
            return;
        }
        throw new MalformedURIException("No scheme found in URI.");
    }

    private boolean initializeAuthority(String str) {
        int i;
        String str2;
        boolean z;
        int i2;
        int i3;
        boolean z2;
        int length = str.length();
        int i4 = -1;
        if (str.indexOf(64, 0) != -1) {
            int i5 = 0;
            while (i5 < length && str.charAt(i5) != '@') {
                i5++;
            }
            str2 = str.substring(0, i5);
            i = i5 + 1;
        } else {
            str2 = null;
            i = 0;
        }
        if (i >= length) {
            z = false;
            i2 = i;
        } else if (str.charAt(i) == '[') {
            int indexOf = str.indexOf(93, i);
            if (indexOf == -1) {
                indexOf = length;
            }
            int i6 = indexOf + 1;
            if (i6 >= length || str.charAt(i6) != ':') {
                i3 = length;
                z2 = false;
            } else {
                i3 = i6;
                z2 = true;
            }
            z = z2;
            i2 = i3;
        } else {
            i2 = str.lastIndexOf(58, length);
            if (i2 <= i) {
                i2 = length;
            }
            z = i2 != length;
        }
        String substring = str.substring(i, i2);
        if (substring.length() > 0 && z) {
            int i7 = i2 + 1;
            int i8 = i7;
            while (i8 < length) {
                i8++;
            }
            String substring2 = str.substring(i7, i8);
            if (substring2.length() > 0) {
                try {
                    int parseInt = Integer.parseInt(substring2);
                    if (parseInt == -1) {
                        parseInt--;
                    }
                    i4 = parseInt;
                } catch (NumberFormatException unused) {
                    i4 = -2;
                }
            }
        }
        if (isValidServerBasedAuthority(substring, i4, str2)) {
            this.m_host = substring;
            this.m_port = i4;
            this.m_userinfo = str2;
            return true;
        } else if (!isValidRegistryBasedAuthority(str)) {
            return false;
        } else {
            this.m_regAuthority = str;
            return true;
        }
    }

    private boolean isValidServerBasedAuthority(String str, int i, String str2) {
        if (!isWellFormedAddress(str) || i < -1 || i > 65535) {
            return false;
        }
        if (str2 != null) {
            int length = str2.length();
            int i2 = 0;
            while (i2 < length) {
                char charAt = str2.charAt(i2);
                if (charAt == '%') {
                    int i3 = i2 + 2;
                    if (i3 >= length || !isHex(str2.charAt(i2 + 1)) || !isHex(str2.charAt(i3))) {
                        return false;
                    }
                    i2 = i3;
                } else if (!isUserinfoCharacter(charAt)) {
                    return false;
                }
                i2++;
            }
        }
        return true;
    }

    private boolean isValidRegistryBasedAuthority(String str) {
        int length = str.length();
        int i = 0;
        while (i < length) {
            char charAt = str.charAt(i);
            if (charAt == '%') {
                int i2 = i + 2;
                if (i2 >= length || !isHex(str.charAt(i + 1)) || !isHex(str.charAt(i2))) {
                    return false;
                }
                i = i2;
            } else if (!isPathCharacter(charAt)) {
                return false;
            }
            i++;
        }
        return true;
    }

    private void initializePath(String str, int i) throws MalformedURIException {
        char c;
        int i2;
        int i3;
        if (str != null) {
            int length = str.length();
            if (i < length) {
                if (getScheme() != null && str.charAt(i) != '/') {
                    c = 0;
                    i2 = i;
                    while (i2 < length) {
                        c = str.charAt(i2);
                        if (c == '?' || c == '#') {
                            break;
                        }
                        if (c == '%') {
                            int i4 = i2 + 2;
                            if (i4 >= length || !isHex(str.charAt(i2 + 1)) || !isHex(str.charAt(i4))) {
                                throw new MalformedURIException("Opaque part contains invalid escape sequence!");
                            }
                            i2 = i4;
                        } else if (!isURICharacter(c)) {
                            throw new MalformedURIException("Opaque part contains invalid character: " + c);
                        }
                        i2++;
                    }
                } else {
                    c = 0;
                    i2 = i;
                    while (true) {
                        if (i2 >= length) {
                            break;
                        }
                        c = str.charAt(i2);
                        if (c == '%') {
                            int i5 = i2 + 2;
                            if (i5 >= length || !isHex(str.charAt(i2 + 1)) || !isHex(str.charAt(i5))) {
                                break;
                            }
                            i2 = i5;
                        } else if (!isPathCharacter(c)) {
                            if (c != '?' && c != '#') {
                                throw new MalformedURIException("Path contains invalid character: " + c);
                            }
                        }
                        i2++;
                    }
                    throw new MalformedURIException("Path contains invalid escape sequence!");
                }
            } else {
                c = 0;
                i2 = i;
            }
            this.m_path = str.substring(i, i2);
            if (c == '?') {
                int i6 = i2 + 1;
                i3 = i6;
                while (i3 < length) {
                    c = str.charAt(i3);
                    if (c == '#') {
                        break;
                    }
                    if (c == '%') {
                        int i7 = i3 + 2;
                        if (i7 >= length || !isHex(str.charAt(i3 + 1)) || !isHex(str.charAt(i7))) {
                            throw new MalformedURIException("Query string contains invalid escape sequence!");
                        }
                        i3 = i7;
                    } else if (!isURICharacter(c)) {
                        throw new MalformedURIException("Query string contains invalid character: " + c);
                    }
                    i3++;
                }
                this.m_queryString = str.substring(i6, i3);
            } else {
                i3 = i2;
            }
            if (c == '#') {
                int i8 = i3 + 1;
                int i9 = i8;
                while (i9 < length) {
                    char charAt = str.charAt(i9);
                    if (charAt == '%') {
                        int i10 = i9 + 2;
                        if (i10 >= length || !isHex(str.charAt(i9 + 1)) || !isHex(str.charAt(i10))) {
                            throw new MalformedURIException("Fragment contains invalid escape sequence!");
                        }
                        i9 = i10;
                    } else if (!isURICharacter(charAt)) {
                        throw new MalformedURIException("Fragment contains invalid character: " + charAt);
                    }
                    i9++;
                }
                this.m_fragment = str.substring(i8, i9);
                return;
            }
            return;
        }
        throw new MalformedURIException("Cannot initialize path from null string!");
    }

    public String getScheme() {
        return this.m_scheme;
    }

    public String getSchemeSpecificPart() {
        StringBuilder sb = new StringBuilder();
        if (!(this.m_host == null && this.m_regAuthority == null)) {
            sb.append("//");
            if (this.m_host != null) {
                String str = this.m_userinfo;
                if (str != null) {
                    sb.append(str);
                    sb.append('@');
                }
                sb.append(this.m_host);
                if (this.m_port != -1) {
                    sb.append(':');
                    sb.append(this.m_port);
                }
            } else {
                sb.append(this.m_regAuthority);
            }
        }
        String str2 = this.m_path;
        if (str2 != null) {
            sb.append(str2);
        }
        if (this.m_queryString != null) {
            sb.append('?');
            sb.append(this.m_queryString);
        }
        if (this.m_fragment != null) {
            sb.append('#');
            sb.append(this.m_fragment);
        }
        return sb.toString();
    }

    public String getUserinfo() {
        return this.m_userinfo;
    }

    public String getHost() {
        return this.m_host;
    }

    public int getPort() {
        return this.m_port;
    }

    public String getRegBasedAuthority() {
        return this.m_regAuthority;
    }

    public String getAuthority() {
        StringBuilder sb = new StringBuilder();
        if (!(this.m_host == null && this.m_regAuthority == null)) {
            sb.append("//");
            if (this.m_host != null) {
                String str = this.m_userinfo;
                if (str != null) {
                    sb.append(str);
                    sb.append('@');
                }
                sb.append(this.m_host);
                if (this.m_port != -1) {
                    sb.append(':');
                    sb.append(this.m_port);
                }
            } else {
                sb.append(this.m_regAuthority);
            }
        }
        return sb.toString();
    }

    public String getPath(boolean z, boolean z2) {
        StringBuilder sb = new StringBuilder(this.m_path);
        if (z && this.m_queryString != null) {
            sb.append('?');
            sb.append(this.m_queryString);
        }
        if (z2 && this.m_fragment != null) {
            sb.append('#');
            sb.append(this.m_fragment);
        }
        return sb.toString();
    }

    public String getPath() {
        return this.m_path;
    }

    public String getQueryString() {
        return this.m_queryString;
    }

    public String getFragment() {
        return this.m_fragment;
    }

    public void setScheme(String str) throws MalformedURIException {
        if (str == null) {
            throw new MalformedURIException("Cannot set scheme from null string!");
        } else if (isConformantSchemeName(str)) {
            this.m_scheme = str.toLowerCase();
        } else {
            throw new MalformedURIException("The scheme is not conformant.");
        }
    }

    public void setUserinfo(String str) throws MalformedURIException {
        if (str == null) {
            this.m_userinfo = null;
        } else if (this.m_host != null) {
            int length = str.length();
            for (int i = 0; i < length; i++) {
                char charAt = str.charAt(i);
                if (charAt == '%') {
                    int i2 = i + 2;
                    if (i2 >= length || !isHex(str.charAt(i + 1)) || !isHex(str.charAt(i2))) {
                        throw new MalformedURIException("Userinfo contains invalid escape sequence!");
                    }
                } else if (!isUserinfoCharacter(charAt)) {
                    throw new MalformedURIException("Userinfo contains invalid character:" + charAt);
                }
            }
            this.m_userinfo = str;
        } else {
            throw new MalformedURIException("Userinfo cannot be set when host is null!");
        }
    }

    public void setHost(String str) throws MalformedURIException {
        if (str == null || str.length() == 0) {
            if (str != null) {
                this.m_regAuthority = null;
            }
            this.m_host = str;
            this.m_userinfo = null;
            this.m_port = -1;
        } else if (isWellFormedAddress(str)) {
            this.m_host = str;
            this.m_regAuthority = null;
        } else {
            throw new MalformedURIException("Host is not a well formed address!");
        }
    }

    public void setPort(int i) throws MalformedURIException {
        if (i < 0 || i > 65535) {
            if (i != -1) {
                throw new MalformedURIException("Invalid port number!");
            }
        } else if (this.m_host == null) {
            throw new MalformedURIException("Port cannot be set when host is null!");
        }
        this.m_port = i;
    }

    public void setRegBasedAuthority(String str) throws MalformedURIException {
        if (str == null) {
            this.m_regAuthority = null;
        } else if (str.length() < 1 || !isValidRegistryBasedAuthority(str) || str.indexOf(47) != -1) {
            throw new MalformedURIException("Registry based authority is not well formed.");
        } else {
            this.m_regAuthority = str;
            this.m_host = null;
            this.m_userinfo = null;
            this.m_port = -1;
        }
    }

    public void setPath(String str) throws MalformedURIException {
        if (str == null) {
            this.m_path = null;
            this.m_queryString = null;
            this.m_fragment = null;
            return;
        }
        initializePath(str, 0);
    }

    public void appendPath(String str) throws MalformedURIException {
        if (str != null && str.trim().length() != 0) {
            if (isURIString(str)) {
                String str2 = this.m_path;
                if (str2 == null || str2.trim().length() == 0) {
                    if (str.startsWith(PsuedoNames.PSEUDONAME_ROOT)) {
                        this.m_path = str;
                        return;
                    }
                    this.m_path = PsuedoNames.PSEUDONAME_ROOT + str;
                } else if (this.m_path.endsWith(PsuedoNames.PSEUDONAME_ROOT)) {
                    if (str.startsWith(PsuedoNames.PSEUDONAME_ROOT)) {
                        this.m_path = this.m_path.concat(str.substring(1));
                    } else {
                        this.m_path = this.m_path.concat(str);
                    }
                } else if (str.startsWith(PsuedoNames.PSEUDONAME_ROOT)) {
                    this.m_path = this.m_path.concat(str);
                } else {
                    String str3 = this.m_path;
                    this.m_path = str3.concat(PsuedoNames.PSEUDONAME_ROOT + str);
                }
            } else {
                throw new MalformedURIException("Path contains invalid character!");
            }
        }
    }

    public void setQueryString(String str) throws MalformedURIException {
        if (str == null) {
            this.m_queryString = null;
        } else if (!isGenericURI()) {
            throw new MalformedURIException("Query string can only be set for a generic URI!");
        } else if (getPath() == null) {
            throw new MalformedURIException("Query string cannot be set when path is null!");
        } else if (isURIString(str)) {
            this.m_queryString = str;
        } else {
            throw new MalformedURIException("Query string contains invalid character!");
        }
    }

    public void setFragment(String str) throws MalformedURIException {
        if (str == null) {
            this.m_fragment = null;
        } else if (!isGenericURI()) {
            throw new MalformedURIException("Fragment can only be set for a generic URI!");
        } else if (getPath() == null) {
            throw new MalformedURIException("Fragment cannot be set when path is null!");
        } else if (isURIString(str)) {
            this.m_fragment = str;
        } else {
            throw new MalformedURIException("Fragment contains invalid character!");
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8;
        String str9;
        String str10;
        String str11;
        if (!(obj instanceof URI)) {
            return false;
        }
        URI uri = (URI) obj;
        if ((this.m_scheme != null || uri.m_scheme != null) && ((str10 = this.m_scheme) == null || (str11 = uri.m_scheme) == null || !str10.equals(str11))) {
            return false;
        }
        if ((this.m_userinfo != null || uri.m_userinfo != null) && ((str8 = this.m_userinfo) == null || (str9 = uri.m_userinfo) == null || !str8.equals(str9))) {
            return false;
        }
        if (((this.m_host != null || uri.m_host != null) && ((str6 = this.m_host) == null || (str7 = uri.m_host) == null || !str6.equals(str7))) || this.m_port != uri.m_port) {
            return false;
        }
        if ((this.m_path != null || uri.m_path != null) && ((str4 = this.m_path) == null || (str5 = uri.m_path) == null || !str4.equals(str5))) {
            return false;
        }
        if ((this.m_queryString != null || uri.m_queryString != null) && ((str2 = this.m_queryString) == null || (str3 = uri.m_queryString) == null || !str2.equals(str3))) {
            return false;
        }
        if (this.m_fragment == null && uri.m_fragment == null) {
            return true;
        }
        String str12 = this.m_fragment;
        return (str12 == null || (str = uri.m_fragment) == null || !str12.equals(str)) ? false : true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return ((((((((((((235 + Objects.hashCode(this.m_scheme)) * 47) + Objects.hashCode(this.m_userinfo)) * 47) + Objects.hashCode(this.m_host)) * 47) + this.m_port) * 47) + Objects.hashCode(this.m_path)) * 47) + Objects.hashCode(this.m_queryString)) * 47) + Objects.hashCode(this.m_fragment);
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String str = this.m_scheme;
        if (str != null) {
            sb.append(str);
            sb.append(':');
        }
        sb.append(getSchemeSpecificPart());
        return sb.toString();
    }

    public boolean isGenericURI() {
        return this.m_host != null;
    }

    public boolean isAbsoluteURI() {
        return this.m_scheme != null;
    }

    public static boolean isConformantSchemeName(String str) {
        if (str == null || str.trim().length() == 0 || !isAlpha(str.charAt(0))) {
            return false;
        }
        int length = str.length();
        for (int i = 1; i < length; i++) {
            if (!isSchemeCharacter(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWellFormedAddress(String str) {
        int length;
        if (str == null || (length = str.length()) == 0) {
            return false;
        }
        if (str.startsWith("[")) {
            return isWellFormedIPv6Reference(str);
        }
        if (str.startsWith(".") || str.startsWith(LanguageTag.SEP) || str.endsWith(LanguageTag.SEP)) {
            return false;
        }
        int lastIndexOf = str.lastIndexOf(46);
        if (str.endsWith(".")) {
            lastIndexOf = str.substring(0, lastIndexOf).lastIndexOf(46);
        }
        int i = lastIndexOf + 1;
        if (i < length && isDigit(str.charAt(i))) {
            return isWellFormedIPv4Address(str);
        }
        if (length > 255) {
            return false;
        }
        int i2 = 0;
        for (int i3 = 0; i3 < length; i3++) {
            char charAt = str.charAt(i3);
            if (charAt == '.') {
                if (!isAlphanum(str.charAt(i3 - 1))) {
                    return false;
                }
                int i4 = i3 + 1;
                if (i4 < length && !isAlphanum(str.charAt(i4))) {
                    return false;
                }
                i2 = 0;
            } else if (!(isAlphanum(charAt) || charAt == '-') || (i2 = i2 + 1) > 63) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0030, code lost:
        return false;
     */
    public static boolean isWellFormedIPv4Address(String str) {
        int i;
        int length = str.length();
        int i2 = 0;
        int i3 = 0;
        for (int i4 = 0; i4 < length; i4++) {
            char charAt = str.charAt(i4);
            if (charAt == '.') {
                if ((i4 > 0 && !isDigit(str.charAt(i4 - 1))) || (((i = i4 + 1) < length && !isDigit(str.charAt(i))) || (i2 = i2 + 1) > 3)) {
                    return false;
                }
                i3 = 0;
            } else if (!isDigit(charAt) || (i3 = i3 + 1) > 3) {
                return false;
            } else {
                if (i3 == 3) {
                    char charAt2 = str.charAt(i4 - 2);
                    char charAt3 = str.charAt(i4 - 1);
                    if (charAt2 >= '2' && (charAt2 != '2' || (charAt3 >= '5' && (charAt3 != '5' || charAt > '5')))) {
                        return false;
                    }
                } else {
                    continue;
                }
            }
        }
        return i2 == 3;
    }

    public static boolean isWellFormedIPv6Reference(String str) {
        int[] iArr;
        int scanHexSequence;
        int length = str.length();
        int i = length - 1;
        if (length <= 2 || str.charAt(0) != '[' || str.charAt(i) != ']' || (scanHexSequence = scanHexSequence(str, 1, i, (iArr = new int[1]))) == -1) {
            return false;
        }
        if (scanHexSequence != i) {
            int i2 = scanHexSequence + 1;
            if (i2 < i && str.charAt(scanHexSequence) == ':') {
                if (str.charAt(i2) == ':') {
                    int i3 = iArr[0] + 1;
                    iArr[0] = i3;
                    if (i3 > 8) {
                        return false;
                    }
                    int i4 = scanHexSequence + 2;
                    if (i4 == i) {
                        return true;
                    }
                    int i5 = iArr[0];
                    int scanHexSequence2 = scanHexSequence(str, i4, i, iArr);
                    if (scanHexSequence2 == i) {
                        return true;
                    }
                    if (scanHexSequence2 != -1) {
                        if (iArr[0] > i5) {
                            scanHexSequence2++;
                        }
                        if (isWellFormedIPv4Address(str.substring(scanHexSequence2, i))) {
                            return true;
                        }
                    }
                    return false;
                } else if (iArr[0] != 6 || !isWellFormedIPv4Address(str.substring(i2, i))) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        } else if (iArr[0] == 8) {
            return true;
        } else {
            return false;
        }
    }

    private static int scanHexSequence(String str, int i, int i2, int[] iArr) {
        int i3;
        int i4 = 0;
        for (int i5 = i; i5 < i2; i5++) {
            char charAt = str.charAt(i5);
            if (charAt == ':') {
                if (i4 > 0) {
                    int i6 = iArr[0] + 1;
                    iArr[0] = i6;
                    if (i6 > 8) {
                        return -1;
                    }
                }
                if (i4 == 0 || ((i3 = i5 + 1) < i2 && str.charAt(i3) == ':')) {
                    return i5;
                }
                i4 = 0;
            } else if (isHex(charAt)) {
                i4++;
                if (i4 > 4) {
                    return -1;
                }
            } else if (charAt != '.' || i4 >= 4 || i4 <= 0 || iArr[0] > 6) {
                return -1;
            } else {
                int i7 = (i5 - i4) - 1;
                return i7 >= i ? i7 : i7 + 1;
            }
        }
        if (i4 > 0) {
            int i8 = iArr[0] + 1;
            iArr[0] = i8;
            if (i8 <= 8) {
                return i2;
            }
        }
        return -1;
    }

    private static boolean isHex(char c) {
        return c <= 'f' && (fgLookupTable[c] & 64) != 0;
    }

    private static boolean isAlphanum(char c) {
        return c <= 'z' && (fgLookupTable[c] & 48) != 0;
    }

    private static boolean isReservedCharacter(char c) {
        return c <= ']' && (fgLookupTable[c] & 1) != 0;
    }

    private static boolean isUnreservedCharacter(char c) {
        return c <= '~' && (fgLookupTable[c] & 50) != 0;
    }

    private static boolean isURICharacter(char c) {
        return c <= '~' && (fgLookupTable[c] & 51) != 0;
    }

    private static boolean isSchemeCharacter(char c) {
        return c <= 'z' && (fgLookupTable[c] & 52) != 0;
    }

    private static boolean isUserinfoCharacter(char c) {
        return c <= 'z' && (fgLookupTable[c] & 58) != 0;
    }

    private static boolean isPathCharacter(char c) {
        return c <= '~' && (fgLookupTable[c] & 178) != 0;
    }

    private static boolean isURIString(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        int i = 0;
        while (i < length) {
            char charAt = str.charAt(i);
            if (charAt == '%') {
                int i2 = i + 2;
                if (i2 >= length || !isHex(str.charAt(i + 1)) || !isHex(str.charAt(i2))) {
                    return false;
                }
                i = i2;
            } else if (!isURICharacter(charAt)) {
                return false;
            }
            i++;
        }
        return true;
    }
}
