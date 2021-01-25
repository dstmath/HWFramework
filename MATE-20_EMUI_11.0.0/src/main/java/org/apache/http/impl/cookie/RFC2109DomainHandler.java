package org.apache.http.impl.cookie;

import java.util.Locale;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;

@Deprecated
public class RFC2109DomainHandler implements CookieAttributeHandler {
    @Override // org.apache.http.cookie.CookieAttributeHandler
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (value == null) {
            throw new MalformedCookieException("Missing value for domain attribute");
        } else if (value.trim().length() != 0) {
            cookie.setDomain(value);
        } else {
            throw new MalformedCookieException("Blank value for domain attribute");
        }
    }

    @Override // org.apache.http.cookie.CookieAttributeHandler
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin != null) {
            String host = origin.getHost();
            String domain = cookie.getDomain();
            if (domain == null) {
                throw new MalformedCookieException("Cookie domain may not be null");
            } else if (domain.equals(host)) {
            } else {
                if (domain.indexOf(46) == -1) {
                    throw new MalformedCookieException("Domain attribute \"" + domain + "\" does not match the host \"" + host + "\"");
                } else if (domain.startsWith(".")) {
                    int dotIndex = domain.indexOf(46, 1);
                    if (dotIndex < 0 || dotIndex == domain.length() - 1) {
                        throw new MalformedCookieException("Domain attribute \"" + domain + "\" violates RFC 2109: domain must contain an embedded dot");
                    }
                    String host2 = host.toLowerCase(Locale.ENGLISH);
                    if (!host2.endsWith(domain)) {
                        throw new MalformedCookieException("Illegal domain attribute \"" + domain + "\". Domain of origin: \"" + host2 + "\"");
                    } else if (host2.substring(0, host2.length() - domain.length()).indexOf(46) != -1) {
                        throw new MalformedCookieException("Domain attribute \"" + domain + "\" violates RFC 2109: host minus domain may not contain any dots");
                    }
                } else {
                    throw new MalformedCookieException("Domain attribute \"" + domain + "\" violates RFC 2109: domain must start with a dot");
                }
            }
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }

    @Override // org.apache.http.cookie.CookieAttributeHandler
    public boolean match(Cookie cookie, CookieOrigin origin) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin != null) {
            String host = origin.getHost();
            String domain = cookie.getDomain();
            if (domain == null) {
                return false;
            }
            if (host.equals(domain) || (domain.startsWith(".") && host.endsWith(domain))) {
                return true;
            }
            return false;
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }
}
