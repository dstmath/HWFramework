package java.sql;

import java.util.Map;

public class SQLClientInfoException extends SQLException {
    private static final long serialVersionUID = -4319604256824655880L;
    private Map<String, ClientInfoStatus> failedProperties;

    public SQLClientInfoException() {
        this.failedProperties = null;
    }

    public SQLClientInfoException(Map<String, ClientInfoStatus> failedProperties2) {
        this.failedProperties = failedProperties2;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SQLClientInfoException(Map<String, ClientInfoStatus> failedProperties2, Throwable cause) {
        super(cause != null ? cause.toString() : null);
        initCause(cause);
        this.failedProperties = failedProperties2;
    }

    public SQLClientInfoException(String reason, Map<String, ClientInfoStatus> failedProperties2) {
        super(reason);
        this.failedProperties = failedProperties2;
    }

    public SQLClientInfoException(String reason, Map<String, ClientInfoStatus> failedProperties2, Throwable cause) {
        super(reason);
        initCause(cause);
        this.failedProperties = failedProperties2;
    }

    public SQLClientInfoException(String reason, String SQLState, Map<String, ClientInfoStatus> failedProperties2) {
        super(reason, SQLState);
        this.failedProperties = failedProperties2;
    }

    public SQLClientInfoException(String reason, String SQLState, Map<String, ClientInfoStatus> failedProperties2, Throwable cause) {
        super(reason, SQLState);
        initCause(cause);
        this.failedProperties = failedProperties2;
    }

    public SQLClientInfoException(String reason, String SQLState, int vendorCode, Map<String, ClientInfoStatus> failedProperties2) {
        super(reason, SQLState, vendorCode);
        this.failedProperties = failedProperties2;
    }

    public SQLClientInfoException(String reason, String SQLState, int vendorCode, Map<String, ClientInfoStatus> failedProperties2, Throwable cause) {
        super(reason, SQLState, vendorCode);
        initCause(cause);
        this.failedProperties = failedProperties2;
    }

    public Map<String, ClientInfoStatus> getFailedProperties() {
        return this.failedProperties;
    }
}
