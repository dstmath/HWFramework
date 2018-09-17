package org.apache.http.impl.cookie;

import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.cookie.SetCookie2;

@Deprecated
public class RFC2965VersionAttributeHandler implements CookieAttributeHandler {
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (value == null) {
            throw new MalformedCookieException("Missing value for version attribute");
        } else {
            int version;
            try {
                version = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                version = -1;
            }
            if (version < 0) {
                throw new MalformedCookieException("Invalid cookie version.");
            }
            cookie.setVersion(version);
        }
    }

    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if ((cookie instanceof SetCookie2) && (cookie instanceof ClientCookie) && (((ClientCookie) cookie).containsAttribute(ClientCookie.VERSION_ATTR) ^ 1) != 0) {
            throw new MalformedCookieException("Violates RFC 2965. Version attribute is required.");
        }
    }

    public boolean match(Cookie cookie, CookieOrigin origin) {
        return true;
    }
}
