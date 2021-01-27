package org.apache.http.impl.cookie;

import java.util.Date;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;

@Deprecated
public class BasicMaxAgeHandler extends AbstractCookieAttributeHandler {
    @Override // org.apache.http.cookie.CookieAttributeHandler
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (value != null) {
            try {
                int age = Integer.parseInt(value);
                if (age >= 0) {
                    cookie.setExpiryDate(new Date(System.currentTimeMillis() + (((long) age) * 1000)));
                    return;
                }
                throw new MalformedCookieException("Negative max-age attribute: " + value);
            } catch (NumberFormatException e) {
                throw new MalformedCookieException("Invalid max-age attribute: " + value);
            }
        } else {
            throw new MalformedCookieException("Missing value for max-age attribute");
        }
    }
}
