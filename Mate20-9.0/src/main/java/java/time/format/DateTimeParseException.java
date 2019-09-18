package java.time.format;

import java.time.DateTimeException;

public class DateTimeParseException extends DateTimeException {
    private static final long serialVersionUID = 4304633501674722597L;
    private final int errorIndex;
    private final String parsedString;

    public DateTimeParseException(String message, CharSequence parsedData, int errorIndex2) {
        super(message);
        this.parsedString = parsedData.toString();
        this.errorIndex = errorIndex2;
    }

    public DateTimeParseException(String message, CharSequence parsedData, int errorIndex2, Throwable cause) {
        super(message, cause);
        this.parsedString = parsedData.toString();
        this.errorIndex = errorIndex2;
    }

    public String getParsedString() {
        return this.parsedString;
    }

    public int getErrorIndex() {
        return this.errorIndex;
    }
}
