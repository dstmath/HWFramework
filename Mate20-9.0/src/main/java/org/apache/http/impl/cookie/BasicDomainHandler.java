package org.apache.http.impl.cookie;

import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;

@Deprecated
public class BasicDomainHandler implements CookieAttributeHandler {
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

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin != null) {
            String host = origin.getHost();
            String domain = cookie.getDomain();
            if (domain == null) {
                throw new MalformedCookieException("Cookie domain may not be null");
            } else if (host.contains(".")) {
                if (!host.endsWith(domain)) {
                    if (domain.startsWith(".")) {
                        domain = domain.substring(1, domain.length());
                    }
                    if (!host.equals(domain)) {
                        throw new MalformedCookieException("Illegal domain attribute \"" + domain + "\". Domain of origin: \"" + host + "\"");
                    }
                }
            } else if (!host.equals(domain)) {
                throw new MalformedCookieException("Illegal domain attribute \"" + domain + "\". Domain of origin: \"" + host + "\"");
            }
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin != null) {
            String host = origin.getHost();
            String domain = cookie.getDomain();
            boolean z = false;
            if (domain == null) {
                return false;
            }
            if (host.equals(domain)) {
                return true;
            }
            if (!domain.startsWith(".")) {
                domain = '.' + domain;
            }
            if (host.endsWith(domain) || host.equals(domain.substring(1))) {
                z = true;
            }
            return z;
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }
}
