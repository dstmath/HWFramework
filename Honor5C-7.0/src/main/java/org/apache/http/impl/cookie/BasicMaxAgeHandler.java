package org.apache.http.impl.cookie;

import java.util.Date;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;

@Deprecated
public class BasicMaxAgeHandler extends AbstractCookieAttributeHandler {
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        } else if (value == null) {
            throw new MalformedCookieException("Missing value for max-age attribute");
        } else {
            try {
                int age = Integer.parseInt(value);
                if (age < 0) {
                    throw new MalformedCookieException("Negative max-age attribute: " + value);
                }
                cookie.setExpiryDate(new Date(System.currentTimeMillis() + (((long) age) * 1000)));
            } catch (NumberFormatException e) {
                throw new MalformedCookieException("Invalid max-age attribute: " + value);
            }
        }
    }
}
