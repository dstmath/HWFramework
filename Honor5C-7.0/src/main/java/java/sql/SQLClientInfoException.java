package java.sql;

import java.util.Map;

public class SQLClientInfoException extends SQLException {
    private static final long serialVersionUID = -4319604256824655880L;
    private Map<String, ClientInfoStatus> failedProperties;

    public SQLClientInfoException() {
        this.failedProperties = null;
    }

    public SQLClientInfoException(Map<String, ClientInfoStatus> failedProperties) {
        this.failedProperties = failedProperties;
    }

    public SQLClientInfoException(Map<String, ClientInfoStatus> failedProperties, Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.toString();
        }
        super(str);
        initCause(cause);
        this.failedProperties = failedProperties;
    }

    public SQLClientInfoException(String reason, Map<String, ClientInfoStatus> failedProperties) {
        super(reason);
        this.failedProperties = failedProperties;
    }

    public SQLClientInfoException(String reason, Map<String, ClientInfoStatus> failedProperties, Throwable cause) {
        super(reason);
        initCause(cause);
        this.failedProperties = failedProperties;
    }

    public SQLClientInfoException(String reason, String SQLState, Map<String, ClientInfoStatus> failedProperties) {
        super(reason, SQLState);
        this.failedProperties = failedProperties;
    }

    public SQLClientInfoException(String reason, String SQLState, Map<String, ClientInfoStatus> failedProperties, Throwable cause) {
        super(reason, SQLState);
        initCause(cause);
        this.failedProperties = failedProperties;
    }

    public SQLClientInfoException(String reason, String SQLState, int vendorCode, Map<String, ClientInfoStatus> failedProperties) {
        super(reason, SQLState, vendorCode);
        this.failedProperties = failedProperties;
    }

    public SQLClientInfoException(String reason, String SQLState, int vendorCode, Map<String, ClientInfoStatus> failedProperties, Throwable cause) {
        super(reason, SQLState, vendorCode);
        initCause(cause);
        this.failedProperties = failedProperties;
    }

    public Map<String, ClientInfoStatus> getFailedProperties() {
        return this.failedProperties;
    }
}
