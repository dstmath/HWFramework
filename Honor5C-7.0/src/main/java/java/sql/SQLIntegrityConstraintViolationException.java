package java.sql;

public class SQLIntegrityConstraintViolationException extends SQLNonTransientException {
    private static final long serialVersionUID = 8033405298774849169L;

    public SQLIntegrityConstraintViolationException(String reason) {
        super(reason);
    }

    public SQLIntegrityConstraintViolationException(String reason, String SQLState) {
        super(reason, SQLState);
    }

    public SQLIntegrityConstraintViolationException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }

    public SQLIntegrityConstraintViolationException(Throwable cause) {
        super(cause);
    }

    public SQLIntegrityConstraintViolationException(String reason, Throwable cause) {
        super(reason, cause);
    }

    public SQLIntegrityConstraintViolationException(String reason, String SQLState, Throwable cause) {
        super(reason, SQLState, cause);
    }

    public SQLIntegrityConstraintViolationException(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason, SQLState, vendorCode, cause);
    }
}
