package org.apache.http.impl.cookie;

import java.util.List;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;

@Deprecated
public class BestMatchSpec implements CookieSpec {
    private BrowserCompatSpec compat;
    private final String[] datepatterns;
    private NetscapeDraftSpec netscape;
    private final boolean oneHeader;
    private RFC2965Spec strict;

    public BestMatchSpec(String[] datepatterns, boolean oneHeader) {
        this.datepatterns = datepatterns;
        this.oneHeader = oneHeader;
    }

    public BestMatchSpec() {
        this(null, false);
    }

    private RFC2965Spec getStrict() {
        if (this.strict == null) {
            this.strict = new RFC2965Spec(this.datepatterns, this.oneHeader);
        }
        return this.strict;
    }

    private BrowserCompatSpec getCompat() {
        if (this.compat == null) {
            this.compat = new BrowserCompatSpec(this.datepatterns);
        }
        return this.compat;
    }

    private NetscapeDraftSpec getNetscape() {
        if (this.netscape == null) {
            String[] patterns = this.datepatterns;
            if (patterns == null) {
                patterns = BrowserCompatSpec.DATE_PATTERNS;
            }
            this.netscape = new NetscapeDraftSpec(patterns);
        }
        return this.netscape;
    }

    public List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        if (header == null) {
            throw new IllegalArgumentException("Header may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
            HeaderElement[] helems = header.getElements();
            boolean versioned = false;
            boolean netscape = false;
            for (HeaderElement helem : helems) {
                if (helem.getParameterByName(ClientCookie.VERSION_ATTR) != null) {
                    versioned = true;
                }
                if (helem.getParameterByName("expires") != null) {
                    netscape = true;
                }
            }
            if (versioned) {
                return getStrict().parse(helems, origin);
            }
            if (netscape) {
                return getNetscape().parse(header, origin);
            }
            return getCompat().parse(helems, origin);
        }
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else if (cookie.getVersion() > 0) {
            getStrict().validate(cookie, origin);
        } else {
            getCompat().validate(cookie, origin);
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else if (cookie.getVersion() > 0) {
            return getStrict().match(cookie, origin);
        } else {
            return getCompat().match(cookie, origin);
        }
    }

    public List<Header> formatCookies(List<Cookie> cookies) {
        if (cookies == null) {
            throw new IllegalArgumentException("List of cookie may not be null");
        }
        int version = Integer.MAX_VALUE;
        for (Cookie cookie : cookies) {
            if (cookie.getVersion() < version) {
                version = cookie.getVersion();
            }
        }
        if (version > 0) {
            return getStrict().formatCookies(cookies);
        }
        return getCompat().formatCookies(cookies);
    }

    public int getVersion() {
        return getStrict().getVersion();
    }

    public Header getVersionHeader() {
        return getStrict().getVersionHeader();
    }
}
