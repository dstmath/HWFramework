package java.net;

import java.io.IOException;

public class HttpRetryException extends IOException {
    private static final long serialVersionUID = -9186022286469111381L;
    private String location;
    private int responseCode;

    public HttpRetryException(String detail, int code) {
        super(detail);
        this.responseCode = code;
    }

    public HttpRetryException(String detail, int code, String location) {
        super(detail);
        this.responseCode = code;
        this.location = location;
    }

    public int responseCode() {
        return this.responseCode;
    }

    public String getReason() {
        return super.getMessage();
    }

    public String getLocation() {
        return this.location;
    }
}
