package org.apache.http.cookie;

import org.apache.http.ProtocolException;

@Deprecated
public class MalformedCookieException extends ProtocolException {
    private static final long serialVersionUID = -6695462944287282185L;

    public MalformedCookieException(String message) {
        super(message);
    }

    public MalformedCookieException(String message, Throwable cause) {
        super(message, cause);
    }
}
