package org.apache.http.impl.cookie;

import java.util.Locale;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;

@Deprecated
public class RFC2965DomainAttributeHandler implements CookieAttributeHandler {
    public void parse(SetCookie cookie, String domain) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (domain == null) {
            throw new MalformedCookieException("Missing value for domain attribute");
        } else if (domain.trim().length() == 0) {
            throw new MalformedCookieException("Blank value for domain attribute");
        } else {
            domain = domain.toLowerCase(Locale.ENGLISH);
            if (!domain.startsWith(".")) {
                domain = '.' + domain;
            }
            cookie.setDomain(domain);
        }
    }

    public boolean domainMatch(String host, String domain) {
        if (host.equals(domain)) {
            return true;
        }
        return domain.startsWith(".") ? host.endsWith(domain) : false;
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
            String host = origin.getHost().toLowerCase(Locale.ENGLISH);
            if (cookie.getDomain() == null) {
                throw new MalformedCookieException("Invalid cookie state: domain not specified");
            }
            String cookieDomain = cookie.getDomain().toLowerCase(Locale.ENGLISH);
            if ((cookie instanceof ClientCookie) && ((ClientCookie) cookie).containsAttribute(ClientCookie.DOMAIN_ATTR)) {
                if (cookieDomain.startsWith(".")) {
                    int dotIndex = cookieDomain.indexOf(46, 1);
                    if ((dotIndex < 0 || dotIndex == cookieDomain.length() - 1) && (cookieDomain.equals(".local") ^ 1) != 0) {
                        throw new MalformedCookieException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: the value contains no embedded dots " + "and the value is not .local");
                    } else if (!domainMatch(host, cookieDomain)) {
                        throw new MalformedCookieException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: effective host name does not " + "domain-match domain attribute.");
                    } else if (host.substring(0, host.length() - cookieDomain.length()).indexOf(46) != -1) {
                        throw new MalformedCookieException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: " + "effective host minus domain may not contain any dots");
                    } else {
                        return;
                    }
                }
                throw new MalformedCookieException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2109: domain must start with a dot");
            } else if (!cookie.getDomain().equals(host)) {
                throw new MalformedCookieException("Illegal domain attribute: \"" + cookie.getDomain() + "\"." + "Domain of origin: \"" + host + "\"");
            }
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        boolean z = false;
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        } else {
            String host = origin.getHost().toLowerCase(Locale.ENGLISH);
            String cookieDomain = cookie.getDomain();
            if (!domainMatch(host, cookieDomain)) {
                return false;
            }
            if (host.substring(0, host.length() - cookieDomain.length()).indexOf(46) == -1) {
                z = true;
            }
            return z;
        }
    }
}
