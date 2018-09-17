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
        } else if (value.trim().length() == 0) {
            throw new MalformedCookieException("Blank value for domain attribute");
        } else {
            cookie.setDomain(value);
        }
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
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
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        boolean z = true;
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
            String host = origin.getHost();
            String domain = cookie.getDomain();
            if (domain == null) {
                return false;
            }
            if (host.equals(domain)) {
                return true;
            }
            if (!domain.startsWith(".")) {
                domain = '.' + domain;
            }
            if (!host.endsWith(domain)) {
                z = host.equals(domain.substring(1));
            }
            return z;
        }
    }
}
