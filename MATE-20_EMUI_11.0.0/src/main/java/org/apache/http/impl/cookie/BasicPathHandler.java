package org.apache.http.impl.cookie;

import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;

@Deprecated
public class BasicPathHandler implements CookieAttributeHandler {
    @Override // org.apache.http.cookie.CookieAttributeHandler
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        if (cookie != null) {
            if (value == null || value.trim().length() == 0) {
                value = "/";
            }
            cookie.setPath(value);
            return;
        }
        throw new IllegalArgumentException("Cookie may not be null");
    }

    @Override // org.apache.http.cookie.CookieAttributeHandler
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        if (!match(cookie, origin)) {
            throw new MalformedCookieException("Illegal path attribute \"" + cookie.getPath() + "\". Path of origin: \"" + origin.getPath() + "\"");
        }
    }

    @Override // org.apache.http.cookie.CookieAttributeHandler
    public boolean match(Cookie cookie, CookieOrigin origin) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (origin != null) {
            String targetpath = origin.getPath();
            String topmostPath = cookie.getPath();
            if (topmostPath == null) {
                topmostPath = "/";
            }
            boolean match = false;
            if (topmostPath.length() > 1 && topmostPath.endsWith("/")) {
                topmostPath = topmostPath.substring(0, topmostPath.length() - 1);
            }
            boolean match2 = targetpath.startsWith(topmostPath);
            if (!match2 || targetpath.length() == topmostPath.length() || topmostPath.endsWith("/")) {
                return match2;
            }
            if (targetpath.charAt(topmostPath.length()) == '/') {
                match = true;
            }
            return match;
        } else {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
    }
}
