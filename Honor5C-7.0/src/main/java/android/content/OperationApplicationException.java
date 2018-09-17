package android.content;

public class OperationApplicationException extends Exception {
    private final int mNumSuccessfulYieldPoints;

    public OperationApplicationException() {
        this.mNumSuccessfulYieldPoints = 0;
    }

    public OperationApplicationException(String message) {
        super(message);
        this.mNumSuccessfulYieldPoints = 0;
    }

    public OperationApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.mNumSuccessfulYieldPoints = 0;
    }

    public OperationApplicationException(Throwable cause) {
        super(cause);
        this.mNumSuccessfulYieldPoints = 0;
    }

    public OperationApplicationException(int numSuccessfulYieldPoints) {
        this.mNumSuccessfulYieldPoints = numSuccessfulYieldPoints;
    }

    public OperationApplicationException(String message, int numSuccessfulYieldPoints) {
        super(message);
        this.mNumSuccessfulYieldPoints = numSuccessfulYieldPoints;
    }

    public int getNumSuccessfulYieldPoints() {
        return this.mNumSuccessfulYieldPoints;
    }
}
