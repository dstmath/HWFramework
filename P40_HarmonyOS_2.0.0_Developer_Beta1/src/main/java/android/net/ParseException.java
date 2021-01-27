package android.net;

public class ParseException extends RuntimeException {
    public String response;

    ParseException(String response2) {
        super(response2);
        this.response = response2;
    }

    ParseException(String response2, Throwable cause) {
        super(response2, cause);
        this.response = response2;
    }
}
