package ohos.com.sun.org.apache.xml.internal.serializer.utils;

import java.io.IOException;
import java.util.Objects;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.impl.locale.LanguageTag;

/* access modifiers changed from: package-private */
public final class URI {
    private static boolean DEBUG = false;
    private static final String MARK_CHARACTERS = "-_.!~*'() ";
    private static final String RESERVED_CHARACTERS = ";/?:@&=+$,";
    private static final String SCHEME_CHARACTERS = "+-.";
    private static final String USERINFO_CHARACTERS = ";:&=+$,";
    private String m_fragment;
    private String m_host;
    private String m_path;
    private int m_port;
    private String m_queryString;
    private String m_scheme;
    private String m_userinfo;

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static class MalformedURIException extends IOException {
        public MalformedURIException() {
        }

        public MalformedURIException(String str) {
            super(str);
        }
    }

    public URI() {
        this.m_scheme = null;
        this.m_userinfo = null;
        this.m_host = null;
        this.m_port = -1;
        this.m_path = null;
        this.m_queryString = null;
        this.m_fragment = null;
    }

    public URI(URI uri) {
        this.m_scheme = null;
        this.m_userinfo = null;
        this.m_host = null;
        this.m_port = -1;
        this.m_path = null;
        this.m_queryString = null;
        this.m_fragment = null;
        initialize(uri);
    }

    public URI(String str) throws MalformedURIException {
        this((URI) null, str);
    }

    public URI(URI uri, String str) throws MalformedURIException {
        this.m_scheme = null;
        this.m_userinfo = null;
        this.m_host = null;
        this.m_port = -1;
        this.m_path = null;
        this.m_queryString = null;
        this.m_fragment = null;
        initialize(uri, str);
    }

    public URI(String str, String str2) throws MalformedURIException {
        this.m_scheme = null;
        this.m_userinfo = null;
        this.m_host = null;
        this.m_port = -1;
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
        this.m_path = null;
        this.m_queryString = null;
        this.m_fragment = null;
        if (str == null || str.trim().length() == 0) {
            throw new MalformedURIException(Utils.messages.createMessage("ER_SCHEME_REQUIRED", null));
        }
        if (str3 == null) {
            if (str2 != null) {
                throw new MalformedURIException(Utils.messages.createMessage("ER_NO_USERINFO_IF_NO_HOST", null));
            } else if (i != -1) {
                throw new MalformedURIException(Utils.messages.createMessage("ER_NO_PORT_IF_NO_HOST", null));
            }
        }
        if (str4 != null) {
            if (str4.indexOf(63) != -1 && str5 != null) {
                throw new MalformedURIException(Utils.messages.createMessage("ER_NO_QUERY_STRING_IN_PATH", null));
            } else if (!(str4.indexOf(35) == -1 || str6 == null)) {
                throw new MalformedURIException(Utils.messages.createMessage("ER_NO_FRAGMENT_STRING_IN_PATH", null));
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
        this.m_path = uri.getPath();
        this.m_queryString = uri.getQueryString();
        this.m_fragment = uri.getFragment();
    }

    private void initialize(URI uri, String str) throws MalformedURIException {
        int i;
        int lastIndexOf;
        int lastIndexOf2;
        if (uri == null && (str == null || str.trim().length() == 0)) {
            throw new MalformedURIException(Utils.messages.createMessage("ER_CANNOT_INIT_URI_EMPTY_PARMS", null));
        } else if (str == null || str.trim().length() == 0) {
            initialize(uri);
        } else {
            String trim = str.trim();
            int length = trim.length();
            int indexOf = trim.indexOf(58);
            if (indexOf >= 0) {
                initializeScheme(trim);
                trim = trim.substring(indexOf + 1);
                length = trim.length();
            } else if (uri == null) {
                throw new MalformedURIException(Utils.messages.createMessage("ER_NO_SCHEME_IN_URI", new Object[]{trim}));
            }
            String str2 = "";
            if (1 >= length || !trim.substring(0).startsWith("//")) {
                i = 0;
            } else {
                i = 2;
                while (i < length) {
                    char charAt = trim.charAt(i);
                    if (charAt == '/' || charAt == '?' || charAt == '#') {
                        break;
                    }
                    i++;
                }
                if (i > 2) {
                    initializeAuthority(trim.substring(2, i));
                } else {
                    this.m_host = str2;
                }
            }
            initializePath(trim.substring(i));
            if (uri == null) {
                return;
            }
            if (this.m_path.length() == 0 && this.m_scheme == null && this.m_host == null) {
                this.m_scheme = uri.getScheme();
                this.m_userinfo = uri.getUserinfo();
                this.m_host = uri.getHost();
                this.m_port = uri.getPort();
                this.m_path = uri.getPath();
                if (this.m_queryString == null) {
                    this.m_queryString = uri.getQueryString();
                    return;
                }
                return;
            }
            if (this.m_scheme == null) {
                this.m_scheme = uri.getScheme();
            }
            if (this.m_host == null) {
                this.m_userinfo = uri.getUserinfo();
                this.m_host = uri.getHost();
                this.m_port = uri.getPort();
                if (this.m_path.length() <= 0 || !this.m_path.startsWith(PsuedoNames.PSEUDONAME_ROOT)) {
                    String path = uri.getPath();
                    if (!(path == null || (lastIndexOf2 = path.lastIndexOf(47)) == -1)) {
                        str2 = path.substring(0, lastIndexOf2 + 1);
                    }
                    String concat = str2.concat(this.m_path);
                    while (true) {
                        int indexOf2 = concat.indexOf("/./");
                        if (indexOf2 == -1) {
                            break;
                        }
                        concat = concat.substring(0, indexOf2 + 1).concat(concat.substring(indexOf2 + 3));
                    }
                    if (concat.endsWith("/.")) {
                        concat = concat.substring(0, concat.length() - 1);
                    }
                    while (true) {
                        int indexOf3 = concat.indexOf("/../");
                        if (indexOf3 <= 0) {
                            break;
                        }
                        String substring = concat.substring(0, concat.indexOf("/../"));
                        int lastIndexOf3 = substring.lastIndexOf(47);
                        if (lastIndexOf3 != -1) {
                            int i2 = lastIndexOf3 + 1;
                            if (!substring.substring(lastIndexOf3).equals(Constants.ATTRVAL_PARENT)) {
                                concat = concat.substring(0, i2).concat(concat.substring(indexOf3 + 4));
                            }
                        }
                    }
                    if (concat.endsWith("/..") && (lastIndexOf = concat.substring(0, concat.length() - 3).lastIndexOf(47)) != -1) {
                        concat = concat.substring(0, lastIndexOf + 1);
                    }
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
        throw new MalformedURIException(Utils.messages.createMessage("ER_NO_SCHEME_INURI", null));
    }

    private void initializeAuthority(String str) throws MalformedURIException {
        char c;
        int i;
        String str2;
        int length = str.length();
        int i2 = -1;
        if (str.indexOf(64, 0) != -1) {
            int i3 = 0;
            c = 0;
            while (i3 < length) {
                c = str.charAt(i3);
                if (c == '@') {
                    break;
                }
                i3++;
            }
            str2 = str.substring(0, i3);
            i = i3 + 1;
        } else {
            str2 = null;
            i = 0;
            c = 0;
        }
        char c2 = c;
        int i4 = i;
        while (i4 < length && (c2 = str.charAt(i4)) != ':') {
            i4++;
        }
        String substring = str.substring(i, i4);
        if (substring.length() > 0 && c2 == ':') {
            int i5 = i4 + 1;
            int i6 = i5;
            while (i6 < length) {
                i6++;
            }
            String substring2 = str.substring(i5, i6);
            if (substring2.length() > 0) {
                for (int i7 = 0; i7 < substring2.length(); i7++) {
                    if (!isDigit(substring2.charAt(i7))) {
                        throw new MalformedURIException(substring2 + " is invalid. Port should only contain digits!");
                    }
                }
                try {
                    i2 = Integer.parseInt(substring2);
                } catch (NumberFormatException unused) {
                }
            }
        }
        setHost(substring);
        setPort(i2);
        setUserinfo(str2);
    }

    private void initializePath(String str) throws MalformedURIException {
        int i;
        if (str != null) {
            int length = str.length();
            int i2 = 0;
            char c = 0;
            while (i2 < length && (c = str.charAt(i2)) != '?' && c != '#') {
                if (c == '%') {
                    int i3 = i2 + 2;
                    if (i3 >= length || !isHex(str.charAt(i2 + 1)) || !isHex(str.charAt(i3))) {
                        throw new MalformedURIException(Utils.messages.createMessage("ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", null));
                    }
                } else if (!isReservedCharacter(c) && !isUnreservedCharacter(c) && '\\' != c) {
                    throw new MalformedURIException(Utils.messages.createMessage("ER_PATH_INVALID_CHAR", new Object[]{String.valueOf(c)}));
                }
                i2++;
            }
            this.m_path = str.substring(0, i2);
            if (c == '?') {
                int i4 = i2 + 1;
                i = i4;
                while (i < length) {
                    c = str.charAt(i);
                    if (c == '#') {
                        break;
                    }
                    if (c == '%') {
                        int i5 = i + 2;
                        if (i5 >= length || !isHex(str.charAt(i + 1)) || !isHex(str.charAt(i5))) {
                            throw new MalformedURIException("Query string contains invalid escape sequence!");
                        }
                    } else if (!isReservedCharacter(c) && !isUnreservedCharacter(c)) {
                        throw new MalformedURIException("Query string contains invalid character:" + c);
                    }
                    i++;
                }
                this.m_queryString = str.substring(i4, i);
            } else {
                i = i2;
            }
            if (c == '#') {
                int i6 = i + 1;
                int i7 = i6;
                while (i7 < length) {
                    char charAt = str.charAt(i7);
                    if (charAt == '%') {
                        int i8 = i7 + 2;
                        if (i8 >= length || !isHex(str.charAt(i7 + 1)) || !isHex(str.charAt(i8))) {
                            throw new MalformedURIException("Fragment contains invalid escape sequence!");
                        }
                    } else if (!isReservedCharacter(charAt) && !isUnreservedCharacter(charAt)) {
                        throw new MalformedURIException("Fragment contains invalid character:" + charAt);
                    }
                    i7++;
                }
                this.m_fragment = str.substring(i6, i7);
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
        if (!(this.m_userinfo == null && this.m_host == null && this.m_port == -1)) {
            sb.append("//");
        }
        String str = this.m_userinfo;
        if (str != null) {
            sb.append(str);
            sb.append('@');
        }
        String str2 = this.m_host;
        if (str2 != null) {
            sb.append(str2);
        }
        if (this.m_port != -1) {
            sb.append(':');
            sb.append(this.m_port);
        }
        String str3 = this.m_path;
        if (str3 != null) {
            sb.append(str3);
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
            throw new MalformedURIException(Utils.messages.createMessage("ER_SCHEME_FROM_NULL_STRING", null));
        } else if (isConformantSchemeName(str)) {
            this.m_scheme = str.toLowerCase();
        } else {
            throw new MalformedURIException(Utils.messages.createMessage("ER_SCHEME_NOT_CONFORMANT", null));
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
                } else if (!isUnreservedCharacter(charAt) && USERINFO_CHARACTERS.indexOf(charAt) == -1) {
                    throw new MalformedURIException("Userinfo contains invalid character:" + charAt);
                }
            }
        } else {
            throw new MalformedURIException("Userinfo cannot be set when host is null!");
        }
        this.m_userinfo = str;
    }

    public void setHost(String str) throws MalformedURIException {
        if (str == null || str.trim().length() == 0) {
            this.m_host = str;
            this.m_userinfo = null;
            this.m_port = -1;
        } else if (!isWellFormedAddress(str)) {
            throw new MalformedURIException(Utils.messages.createMessage("ER_HOST_ADDRESS_NOT_WELLFORMED", null));
        }
        this.m_host = str;
    }

    public void setPort(int i) throws MalformedURIException {
        if (i < 0 || i > 65535) {
            if (i != -1) {
                throw new MalformedURIException(Utils.messages.createMessage("ER_INVALID_PORT", null));
            }
        } else if (this.m_host == null) {
            throw new MalformedURIException(Utils.messages.createMessage("ER_PORT_WHEN_HOST_NULL", null));
        }
        this.m_port = i;
    }

    public void setPath(String str) throws MalformedURIException {
        if (str == null) {
            this.m_path = null;
            this.m_queryString = null;
            this.m_fragment = null;
            return;
        }
        initializePath(str);
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
                throw new MalformedURIException(Utils.messages.createMessage("ER_PATH_INVALID_CHAR", new Object[]{str}));
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
            throw new MalformedURIException(Utils.messages.createMessage("ER_FRAG_FOR_GENERIC_URI", null));
        } else if (getPath() == null) {
            throw new MalformedURIException(Utils.messages.createMessage("ER_FRAG_WHEN_PATH_NULL", null));
        } else if (isURIString(str)) {
            this.m_fragment = str;
        } else {
            throw new MalformedURIException(Utils.messages.createMessage("ER_FRAG_INVALID_CHAR", null));
        }
    }

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

    public int hashCode() {
        return ((((((((((((205 + Objects.hashCode(this.m_scheme)) * 41) + Objects.hashCode(this.m_userinfo)) * 41) + Objects.hashCode(this.m_host)) * 41) + this.m_port) * 41) + Objects.hashCode(this.m_path)) * 41) + Objects.hashCode(this.m_queryString)) * 41) + Objects.hashCode(this.m_fragment);
    }

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

    public static boolean isConformantSchemeName(String str) {
        if (str == null || str.trim().length() == 0 || !isAlpha(str.charAt(0))) {
            return false;
        }
        for (int i = 1; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (!isAlphanum(charAt) && SCHEME_CHARACTERS.indexOf(charAt) == -1) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWellFormedAddress(String str) {
        String trim;
        int length;
        int i;
        if (str == null || (length = (trim = str.trim()).length()) == 0 || length > 255 || trim.startsWith(".") || trim.startsWith(LanguageTag.SEP)) {
            return false;
        }
        int lastIndexOf = trim.lastIndexOf(46);
        if (trim.endsWith(".")) {
            lastIndexOf = trim.substring(0, lastIndexOf).lastIndexOf(46);
        }
        int i2 = lastIndexOf + 1;
        if (i2 >= length || !isDigit(str.charAt(i2))) {
            for (int i3 = 0; i3 < length; i3++) {
                char charAt = trim.charAt(i3);
                if (charAt == '.') {
                    if (!isAlphanum(trim.charAt(i3 - 1))) {
                        return false;
                    }
                    int i4 = i3 + 1;
                    if (i4 < length && !isAlphanum(trim.charAt(i4))) {
                        return false;
                    }
                } else if (!isAlphanum(charAt) && charAt != '-') {
                    return false;
                }
            }
        } else {
            int i5 = 0;
            for (int i6 = 0; i6 < length; i6++) {
                char charAt2 = trim.charAt(i6);
                if (charAt2 == '.') {
                    if (!isDigit(trim.charAt(i6 - 1)) || ((i = i6 + 1) < length && !isDigit(trim.charAt(i)))) {
                        return false;
                    }
                    i5++;
                } else if (!isDigit(charAt2)) {
                    return false;
                }
            }
            if (i5 != 3) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHex(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isAlphanum(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private static boolean isReservedCharacter(char c) {
        return RESERVED_CHARACTERS.indexOf(c) != -1;
    }

    private static boolean isUnreservedCharacter(char c) {
        return isAlphanum(c) || MARK_CHARACTERS.indexOf(c) != -1;
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
            } else if (!isReservedCharacter(charAt) && !isUnreservedCharacter(charAt)) {
                return false;
            }
            i++;
        }
        return true;
    }
}
