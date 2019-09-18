package java.time.zone;

import java.time.DateTimeException;

public class ZoneRulesException extends DateTimeException {
    private static final long serialVersionUID = -1632418723876261839L;

    public ZoneRulesException(String message) {
        super(message);
    }

    public ZoneRulesException(String message, Throwable cause) {
        super(message, cause);
    }
}
