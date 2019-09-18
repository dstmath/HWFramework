package java.time.temporal;

import java.time.DateTimeException;

public class UnsupportedTemporalTypeException extends DateTimeException {
    private static final long serialVersionUID = -6158898438688206006L;

    public UnsupportedTemporalTypeException(String message) {
        super(message);
    }

    public UnsupportedTemporalTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
