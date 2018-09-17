package java.sql;

public class SQLFeatureNotSupportedException extends SQLNonTransientException {
    private static final long serialVersionUID = -1026510870282316051L;

    public SQLFeatureNotSupportedException(String reason) {
        super(reason);
    }

    public SQLFeatureNotSupportedException(String reason, String SQLState) {
        super(reason, SQLState);
    }

    public SQLFeatureNotSupportedException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }

    public SQLFeatureNotSupportedException(Throwable cause) {
        super(cause);
    }

    public SQLFeatureNotSupportedException(String reason, Throwable cause) {
        super(reason, cause);
    }

    public SQLFeatureNotSupportedException(String reason, String SQLState, Throwable cause) {
        super(reason, SQLState, cause);
    }

    public SQLFeatureNotSupportedException(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason, SQLState, vendorCode, cause);
    }
}
