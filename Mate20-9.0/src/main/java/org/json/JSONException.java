package org.json;

public class JSONException extends Exception {
    public JSONException(String s) {
        super(s);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONException(Throwable cause) {
        super(cause);
    }
}
