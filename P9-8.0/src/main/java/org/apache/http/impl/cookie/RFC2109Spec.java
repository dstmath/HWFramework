package org.apache.http.impl.cookie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookiePathComparator;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class RFC2109Spec extends CookieSpecBase {
    private static final String[] DATE_PATTERNS = new String[]{"EEE, dd MMM yyyy HH:mm:ss zzz", DateUtils.PATTERN_RFC1036, DateUtils.PATTERN_ASCTIME};
    private static final CookiePathComparator PATH_COMPARATOR = new CookiePathComparator();
    private final String[] datepatterns;
    private final boolean oneHeader;

    public RFC2109Spec(String[] datepatterns, boolean oneHeader) {
        if (datepatterns != null) {
            this.datepatterns = (String[]) datepatterns.clone();
        } else {
            this.datepatterns = DATE_PATTERNS;
        }
        this.oneHeader = oneHeader;
        registerAttribHandler(ClientCookie.VERSION_ATTR, new RFC2109VersionHandler());
        registerAttribHandler(ClientCookie.PATH_ATTR, new BasicPathHandler());
        registerAttribHandler(ClientCookie.DOMAIN_ATTR, new RFC2109DomainHandler());
        registerAttribHandler(ClientCookie.MAX_AGE_ATTR, new BasicMaxAgeHandler());
        registerAttribHandler(ClientCookie.SECURE_ATTR, new BasicSecureHandler());
        registerAttribHandler(ClientCookie.COMMENT_ATTR, new BasicCommentHandler());
        registerAttribHandler("expires", new BasicExpiresHandler(this.datepatterns));
    }

    public RFC2109Spec() {
        this(null, false);
    }

    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        if (header == null) {
            throw new IllegalArgumentException("Header may not be null");
        } else if (origin != null) {
            return parse(header.getElements(), origin);
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        String name = cookie.getName();
        if (name.indexOf(32) != -1) {
            throw new MalformedCookieException("Cookie name may not contain blanks");
        } else if (name.startsWith("$")) {
            throw new MalformedCookieException("Cookie name may not start with $");
        } else {
            super.validate(cookie, origin);
        }
    }

    public List<Header> formatCookies(List<Cookie> cookies) {
        if (cookies == null) {
            throw new IllegalArgumentException("List of cookies may not be null");
        } else if (cookies.isEmpty()) {
            throw new IllegalArgumentException("List of cookies may not be empty");
        } else {
            if (cookies.size() > 1) {
                List<Cookie> cookies2 = new ArrayList(cookies);
                Collections.sort(cookies2, PATH_COMPARATOR);
                cookies = cookies2;
            }
            if (this.oneHeader) {
                return doFormatOneHeader(cookies);
            }
            return doFormatManyHeaders(cookies);
        }
    }

    private List<Header> doFormatOneHeader(List<Cookie> cookies) {
        Cookie cookie;
        int version = Integer.MAX_VALUE;
        for (Cookie cookie2 : cookies) {
            if (cookie2.getVersion() < version) {
                version = cookie2.getVersion();
            }
        }
        CharArrayBuffer buffer = new CharArrayBuffer(cookies.size() * 40);
        buffer.append(SM.COOKIE);
        buffer.append(": ");
        buffer.append("$Version=");
        buffer.append(Integer.toString(version));
        for (Cookie cooky : cookies) {
            buffer.append("; ");
            cookie2 = cooky;
            formatCookieAsVer(buffer, cooky, version);
        }
        List<Header> headers = new ArrayList(1);
        headers.add(new BufferedHeader(buffer));
        return headers;
    }

    private List<Header> doFormatManyHeaders(List<Cookie> cookies) {
        List<Header> headers = new ArrayList(cookies.size());
        for (Cookie cookie : cookies) {
            int version = cookie.getVersion();
            CharArrayBuffer buffer = new CharArrayBuffer(40);
            buffer.append("Cookie: ");
            buffer.append("$Version=");
            buffer.append(Integer.toString(version));
            buffer.append("; ");
            formatCookieAsVer(buffer, cookie, version);
            headers.add(new BufferedHeader(buffer));
        }
        return headers;
    }

    protected void formatParamAsVer(CharArrayBuffer buffer, String name, String value, int version) {
        buffer.append(name);
        buffer.append("=");
        if (value == null) {
            return;
        }
        if (version > 0) {
            buffer.append('\"');
            buffer.append(value);
            buffer.append('\"');
            return;
        }
        buffer.append(value);
    }

    protected void formatCookieAsVer(CharArrayBuffer buffer, Cookie cookie, int version) {
        formatParamAsVer(buffer, cookie.getName(), cookie.getValue(), version);
        if (cookie.getPath() != null && (cookie instanceof ClientCookie) && ((ClientCookie) cookie).containsAttribute(ClientCookie.PATH_ATTR)) {
            buffer.append("; ");
            formatParamAsVer(buffer, "$Path", cookie.getPath(), version);
        }
        if (cookie.getDomain() != null && (cookie instanceof ClientCookie) && ((ClientCookie) cookie).containsAttribute(ClientCookie.DOMAIN_ATTR)) {
            buffer.append("; ");
            formatParamAsVer(buffer, "$Domain", cookie.getDomain(), version);
        }
    }

    public int getVersion() {
        return 1;
    }

    public Header getVersionHeader() {
        return null;
    }
}
