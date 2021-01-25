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
    @Override // org.apache.http.cookie.CookieAttributeHandler
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        int version;
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (value != null) {
            try {
                version = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                version = -1;
            }
            if (version >= 0) {
                cookie.setVersion(version);
                return;
            }
            throw new MalformedCookieException("Invalid cookie version.");
        } else {
            throw new MalformedCookieException("Missing value for version attribute");
        }
    }

    @Override // org.apache.http.cookie.CookieAttributeHandler
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if ((cookie instanceof SetCookie2) && (cookie instanceof ClientCookie) && !((ClientCookie) cookie).containsAttribute(ClientCookie.VERSION_ATTR)) {
            throw new MalformedCookieException("Violates RFC 2965. Version attribute is required.");
        }
    }

    @Override // org.apache.http.cookie.CookieAttributeHandler
    public boolean match(Cookie cookie, CookieOrigin origin) {
        return true;
    }
}
