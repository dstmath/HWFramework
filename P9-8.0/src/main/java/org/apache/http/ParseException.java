package org.apache.http;

@Deprecated
public class ParseException extends RuntimeException {
    private static final long serialVersionUID = -7288819855864183578L;

    public ParseException(String message) {
        super(message);
    }
}
