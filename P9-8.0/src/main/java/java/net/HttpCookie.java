package java.net;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import libcore.net.http.HttpDate;

public final class HttpCookie implements Cloneable {
    static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final long MAX_AGE_UNSPECIFIED = -1;
    private static final Set<String> RESERVED_NAMES = new HashSet();
    private static final String SET_COOKIE = "set-cookie:";
    private static final String SET_COOKIE2 = "set-cookie2:";
    static final Map<String, CookieAttributeAssignor> assignors = new HashMap();
    private static final String tspecials = ",;= \t";
    private String comment;
    private String commentURL;
    private String domain;
    public final String header;
    private boolean httpOnly;
    private long maxAge;
    private final String name;
    private String path;
    private String portlist;
    private boolean secure;
    private boolean toDiscard;
    private String value;
    private int version;
    private final long whenCreated;

    interface CookieAttributeAssignor {
        void assign(HttpCookie httpCookie, String str, String str2);
    }

    static {
        RESERVED_NAMES.add("comment");
        RESERVED_NAMES.add("commenturl");
        RESERVED_NAMES.add("discard");
        RESERVED_NAMES.add("domain");
        RESERVED_NAMES.add("expires");
        RESERVED_NAMES.add("httponly");
        RESERVED_NAMES.add("max-age");
        RESERVED_NAMES.add("path");
        RESERVED_NAMES.add("port");
        RESERVED_NAMES.add("secure");
        RESERVED_NAMES.add("version");
        assignors.put("comment", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getComment() == null) {
                    cookie.setComment(attrValue);
                }
            }
        });
        assignors.put("commenturl", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getCommentURL() == null) {
                    cookie.setCommentURL(attrValue);
                }
            }
        });
        assignors.put("discard", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                cookie.setDiscard(true);
            }
        });
        assignors.put("domain", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getDomain() == null) {
                    cookie.setDomain(attrValue);
                }
            }
        });
        assignors.put("max-age", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                try {
                    long maxage = Long.parseLong(attrValue);
                    if (cookie.getMaxAge() == -1) {
                        cookie.setMaxAge(maxage);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Illegal cookie max-age attribute");
                }
            }
        });
        assignors.put("path", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getPath() == null) {
                    cookie.setPath(attrValue);
                }
            }
        });
        assignors.put("port", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getPortlist() == null) {
                    if (attrValue == null) {
                        attrValue = "";
                    }
                    cookie.setPortlist(attrValue);
                }
            }
        });
        assignors.put("secure", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                cookie.setSecure(true);
            }
        });
        assignors.put("httponly", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                cookie.setHttpOnly(true);
            }
        });
        assignors.put("version", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                try {
                    cookie.setVersion(Integer.parseInt(attrValue));
                } catch (NumberFormatException e) {
                }
            }
        });
        assignors.put("expires", new CookieAttributeAssignor() {
            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getMaxAge() == -1) {
                    Date date = HttpDate.parse(attrValue);
                    long maxAgeInSeconds = 0;
                    if (date != null) {
                        maxAgeInSeconds = (date.getTime() - cookie.whenCreated) / 1000;
                        if (maxAgeInSeconds == -1) {
                            maxAgeInSeconds = 0;
                        }
                    }
                    cookie.setMaxAge(maxAgeInSeconds);
                }
            }
        });
    }

    public HttpCookie(String name, String value) {
        this(name, value, null);
    }

    private HttpCookie(String name, String value, String header) {
        this.maxAge = -1;
        this.version = 1;
        name = name.trim();
        if (name.length() == 0 || (isToken(name) ^ 1) != 0 || name.charAt(0) == '$') {
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
        return this.maxAge != -1 && (System.currentTimeMillis() - this.whenCreated) / 1000 > this.maxAge;
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
        if (host.indexOf(46) == -1 && (isLocalDomain || domain.equalsIgnoreCase(host + ".local"))) {
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
            if (c < ' ' || c >= 127 || tspecials.indexOf(c) != -1) {
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
        return getName() + "=" + getValue();
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

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
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
