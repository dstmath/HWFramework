package org.apache.http.impl.cookie;

@Deprecated
public class DateParseException extends Exception {
    private static final long serialVersionUID = 4417696455000643370L;

    public DateParseException() {
    }

    public DateParseException(String message) {
        super(message);
    }
}
