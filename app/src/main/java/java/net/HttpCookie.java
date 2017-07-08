package java.net;

import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

public final class HttpCookie implements Cloneable {
    private static final String[] COOKIE_DATE_FORMATS = null;
    static final TimeZone GMT = null;
    private static final long MAX_AGE_UNSPECIFIED = -1;
    private static final Set<String> RESERVED_NAMES = null;
    private static final String SET_COOKIE = "set-cookie:";
    private static final String SET_COOKIE2 = "set-cookie2:";
    static Map<String, CookieAttributeAssignor> assignors = null;
    private static final String tspecials = ",;= \t";
    private String comment;
    private String commentURL;
    private String domain;
    public final String header;
    private boolean httpOnly;
    private long maxAge;
    private String name;
    private String path;
    private String portlist;
    private boolean secure;
    private boolean toDiscard;
    private String value;
    private int version;
    private long whenCreated;

    interface CookieAttributeAssignor {
        void assign(HttpCookie httpCookie, String str, String str2);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.HttpCookie.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.HttpCookie.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.net.HttpCookie.<clinit>():void");
    }

    public HttpCookie(String name, String value) {
        this(name, value, null);
    }

    private HttpCookie(String name, String value, String header) {
        this.maxAge = MAX_AGE_UNSPECIFIED;
        this.version = 1;
        this.whenCreated = 0;
        name = name.trim();
        if (name.length() == 0 || !isToken(name) || name.charAt(0) == '$') {
            throw new IllegalArgumentException("Illegal cookie name");
        }
        this.name = name;
        this.value = value;
        this.toDiscard = false;
        this.secure = false;
        this.whenCreated = System.currentTimeMillis();
        this.portlist = null;
        this.header = header;
    }

    public static List<HttpCookie> parse(String header) {
        return parse(header, false);
    }

    public static List<HttpCookie> parse(String header, boolean retainHeader) {
        int version = guessCookieVersion(header);
        if (startsWithIgnoreCase(header, SET_COOKIE2)) {
            header = header.substring(SET_COOKIE2.length());
        } else if (startsWithIgnoreCase(header, SET_COOKIE)) {
            header = header.substring(SET_COOKIE.length());
        }
        List<HttpCookie> cookies = new ArrayList();
        HttpCookie cookie;
        if (version == 0) {
            cookie = parseInternal(header, retainHeader);
            cookie.setVersion(0);
            cookies.add(cookie);
        } else {
            for (String cookieStr : splitMultiCookies(header)) {
                cookie = parseInternal(cookieStr, retainHeader);
                cookie.setVersion(1);
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    public boolean hasExpired() {
        if (this.maxAge == 0) {
            return true;
        }
        return this.maxAge != MAX_AGE_UNSPECIFIED && (System.currentTimeMillis() - this.whenCreated) / 1000 > this.maxAge;
    }

    public void setComment(String purpose) {
        this.comment = purpose;
    }

    public String getComment() {
        return this.comment;
    }

    public void setCommentURL(String purpose) {
        this.commentURL = purpose;
    }

    public String getCommentURL() {
        return this.commentURL;
    }

    public void setDiscard(boolean discard) {
        this.toDiscard = discard;
    }

    public boolean getDiscard() {
        return this.toDiscard;
    }

    public void setPortlist(String ports) {
        this.portlist = ports;
    }

    public String getPortlist() {
        return this.portlist;
    }

    public void setDomain(String pattern) {
        if (pattern != null) {
            this.domain = pattern.toLowerCase();
        } else {
            this.domain = pattern;
        }
    }

    public String getDomain() {
        return this.domain;
    }

    public void setMaxAge(long expiry) {
        this.maxAge = expiry;
    }

    public long getMaxAge() {
        return this.maxAge;
    }

    public void setPath(String uri) {
        this.path = uri;
    }

    public String getPath() {
        return this.path;
    }

    public void setSecure(boolean flag) {
        this.secure = flag;
    }

    public boolean getSecure() {
        return this.secure;
    }

    public String getName() {
        return this.name;
    }

    public void setValue(String newValue) {
        this.value = newValue;
    }

    public String getValue() {
        return this.value;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int v) {
        if (v == 0 || v == 1) {
            this.version = v;
            return;
        }
        throw new IllegalArgumentException("cookie version should be 0 or 1");
    }

    public boolean isHttpOnly() {
        return this.httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public static boolean domainMatches(String domain, String host) {
        boolean z = false;
        if (domain == null || host == null) {
            return false;
        }
        boolean isLocalDomain = ".local".equalsIgnoreCase(domain);
        int embeddedDotInDomain = domain.indexOf(46);
        if (embeddedDotInDomain == 0) {
            embeddedDotInDomain = domain.indexOf(46, 1);
        }
        if (!isLocalDomain && (embeddedDotInDomain == -1 || embeddedDotInDomain == domain.length() - 1)) {
            return false;
        }
        if (host.indexOf(46) == -1 && isLocalDomain) {
            return true;
        }
        int lengthDiff = host.length() - domain.length();
        if (lengthDiff == 0) {
            return host.equalsIgnoreCase(domain);
        }
        if (lengthDiff > 0) {
            String H = host.substring(0, lengthDiff);
            if (!host.substring(lengthDiff).equalsIgnoreCase(domain)) {
                isLocalDomain = false;
            } else if (domain.startsWith(".") && isFullyQualifiedDomainName(domain, 1)) {
                isLocalDomain = true;
            }
            return isLocalDomain;
        } else if (lengthDiff != -1) {
            return false;
        } else {
            if (domain.charAt(0) == '.') {
                z = host.equalsIgnoreCase(domain.substring(1));
            }
            return z;
        }
    }

    private static boolean isFullyQualifiedDomainName(String s, int firstCharacter) {
        int dotPosition = s.indexOf(46, firstCharacter + 1);
        if (dotPosition == -1 || dotPosition >= s.length() - 1) {
            return false;
        }
        return true;
    }

    public String toString() {
        if (getVersion() > 0) {
            return toRFC2965HeaderString();
        }
        return toNetscapeHeaderString();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof HttpCookie)) {
            return false;
        }
        HttpCookie other = (HttpCookie) obj;
        if (equalsIgnoreCase(getName(), other.getName()) && equalsIgnoreCase(getDomain(), other.getDomain())) {
            z = Objects.equals(getPath(), other.getPath());
        }
        return z;
    }

    public int hashCode() {
        return (this.name.toLowerCase().hashCode() + (this.domain != null ? this.domain.toLowerCase().hashCode() : 0)) + (this.path != null ? this.path.hashCode() : 0);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static boolean isToken(String value) {
        if (RESERVED_NAMES.contains(value.toLowerCase(Locale.US))) {
            return false;
        }
        int len = value.length();
        for (int i = 0; i < len; i++) {
            int c = value.charAt(i);
            if (c < ' ' || c >= '\u007f' || tspecials.indexOf(c) != -1) {
                return false;
            }
        }
        return true;
    }

    private static HttpCookie parseInternal(String header, boolean retainHeader) {
        StringTokenizer tokenizer = new StringTokenizer(header, ";");
        try {
            String namevaluePair = tokenizer.nextToken();
            int index = namevaluePair.indexOf(61);
            if (index != -1) {
                HttpCookie cookie;
                String name = namevaluePair.substring(0, index).trim();
                String value = namevaluePair.substring(index + 1).trim();
                if (retainHeader) {
                    cookie = new HttpCookie(name, stripOffSurroundingQuote(value), header);
                } else {
                    cookie = new HttpCookie(name, stripOffSurroundingQuote(value));
                }
                while (tokenizer.hasMoreTokens()) {
                    namevaluePair = tokenizer.nextToken();
                    index = namevaluePair.indexOf(61);
                    if (index != -1) {
                        name = namevaluePair.substring(0, index).trim();
                        value = namevaluePair.substring(index + 1).trim();
                    } else {
                        name = namevaluePair.trim();
                        value = null;
                    }
                    assignAttribute(cookie, name, value);
                }
                return cookie;
            }
            throw new IllegalArgumentException("Invalid cookie name-value pair");
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Empty cookie header string");
        }
    }

    private static void assignAttribute(HttpCookie cookie, String attrName, String attrValue) {
        attrValue = stripOffSurroundingQuote(attrValue);
        CookieAttributeAssignor assignor = (CookieAttributeAssignor) assignors.get(attrName.toLowerCase());
        if (assignor != null) {
            assignor.assign(cookie, attrName, attrValue);
        }
    }

    private String header() {
        return this.header;
    }

    private String toNetscapeHeaderString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("=").append(getValue());
        return sb.toString();
    }

    private String toRFC2965HeaderString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("=\"").append(getValue()).append('\"');
        if (getPath() != null) {
            sb.append(";$Path=\"").append(getPath()).append('\"');
        }
        if (getDomain() != null) {
            sb.append(";$Domain=\"").append(getDomain()).append('\"');
        }
        if (getPortlist() != null) {
            sb.append(";$Port=\"").append(getPortlist()).append('\"');
        }
        return sb.toString();
    }

    private long expiryDate2DeltaSeconds(String dateString) {
        Calendar cal = new GregorianCalendar(GMT);
        int i = 0;
        while (i < COOKIE_DATE_FORMATS.length) {
            SimpleDateFormat df = new SimpleDateFormat(COOKIE_DATE_FORMATS[i], Locale.US);
            cal.set(1970, 0, 1, 0, 0, 0);
            df.setTimeZone(GMT);
            df.setLenient(false);
            df.set2DigitYearStart(cal.getTime());
            try {
                cal.setTime(df.parse(dateString));
                if (!COOKIE_DATE_FORMATS[i].contains("yyyy")) {
                    int year = cal.get(1) % 100;
                    if (year < 70) {
                        year += Types.JAVA_OBJECT;
                    } else {
                        year += 1900;
                    }
                    cal.set(1, year);
                }
                return (cal.getTimeInMillis() - this.whenCreated) / 1000;
            } catch (Exception e) {
                i++;
            }
        }
        return 0;
    }

    private static int guessCookieVersion(String header) {
        header = header.toLowerCase();
        if (header.indexOf("expires=") != -1) {
            return 0;
        }
        if (header.indexOf("version=") != -1) {
            return 1;
        }
        if (header.indexOf("max-age") != -1) {
            return 1;
        }
        if (startsWithIgnoreCase(header, SET_COOKIE2)) {
            return 1;
        }
        return 0;
    }

    private static String stripOffSurroundingQuote(String str) {
        if (str != null && str.length() > 2 && str.charAt(0) == '\"' && str.charAt(str.length() - 1) == '\"') {
            return str.substring(1, str.length() - 1);
        }
        if (str == null || str.length() <= 2 || str.charAt(0) != '\'' || str.charAt(str.length() - 1) != '\'') {
            return str;
        }
        return str.substring(1, str.length() - 1);
    }

    private static boolean equalsIgnoreCase(String s, String t) {
        if (s == t) {
            return true;
        }
        if (s == null || t == null) {
            return false;
        }
        return s.equalsIgnoreCase(t);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean startsWithIgnoreCase(String s, String start) {
        if (s == null || start == null || s.length() < start.length() || !start.equalsIgnoreCase(s.substring(0, start.length()))) {
            return false;
        }
        return true;
    }

    private static List<String> splitMultiCookies(String header) {
        List<String> cookies = new ArrayList();
        int quoteCount = 0;
        int q = 0;
        for (int p = 0; p < header.length(); p++) {
            char c = header.charAt(p);
            if (c == '\"') {
                quoteCount++;
            }
            if (c == ',' && quoteCount % 2 == 0) {
                cookies.add(header.substring(q, p));
                q = p + 1;
            }
        }
        cookies.add(header.substring(q));
        return cookies;
    }
}
