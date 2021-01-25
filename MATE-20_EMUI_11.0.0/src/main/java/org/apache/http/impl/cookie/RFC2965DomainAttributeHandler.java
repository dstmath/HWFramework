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
    @Override // org.apache.http.cookie.CookieAttributeHandler
    public void parse(SetCookie cookie, String domain) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (domain == null) {
            throw new MalformedCookieException("Missing value for domain attribute");
        } else if (domain.trim().length() != 0) {
            String domain2 = domain.toLowerCase(Locale.ENGLISH);
            if (!domain2.startsWith(".")) {
                domain2 = '.' + domain2;
            }
            cookie.setDomain(domain2);
        } else {
            throw new MalformedCookieException("Blank value for domain attribute");
        }
    }

    public boolean domainMatch(String host, String domain) {
        return host.equals(domain) || (domain.startsWith(".") && host.endsWith(domain));
    }

    @Override // org.apache.http.cookie.CookieAttributeHandler
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin != null) {
            String host = origin.getHost().toLowerCase(Locale.ENGLISH);
            if (cookie.getDomain() != null) {
                String cookieDomain = cookie.getDomain().toLowerCase(Locale.ENGLISH);
                if (!(cookie instanceof ClientCookie) || !((ClientCookie) cookie).containsAttribute(ClientCookie.DOMAIN_ATTR)) {
                    if (!cookie.getDomain().equals(host)) {
                        throw new MalformedCookieException("Illegal domain attribute: \"" + cookie.getDomain() + "\".Domain of origin: \"" + host + "\"");
                    }
                } else if (cookieDomain.startsWith(".")) {
                    int dotIndex = cookieDomain.indexOf(46, 1);
                    if ((dotIndex < 0 || dotIndex == cookieDomain.length() - 1) && !cookieDomain.equals(".local")) {
                        throw new MalformedCookieException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: the value contains no embedded dots and the value is not .local");
                    } else if (!domainMatch(host, cookieDomain)) {
                        throw new MalformedCookieException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: effective host name does not domain-match domain attribute.");
                    } else if (host.substring(0, host.length() - cookieDomain.length()).indexOf(46) != -1) {
                        throw new MalformedCookieException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2965: effective host minus domain may not contain any dots");
                    }
                } else {
                    throw new MalformedCookieException("Domain attribute \"" + cookie.getDomain() + "\" violates RFC 2109: domain must start with a dot");
                }
            } else {
                throw new MalformedCookieException("Invalid cookie state: domain not specified");
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
            String host = origin.getHost().toLowerCase(Locale.ENGLISH);
            String cookieDomain = cookie.getDomain();
            if (domainMatch(host, cookieDomain) && host.substring(0, host.length() - cookieDomain.length()).indexOf(46) == -1) {
                return true;
            }
            return false;
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }
}
