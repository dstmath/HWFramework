package android.os;

public class TransactionTooLargeException extends RemoteException {
    public TransactionTooLargeException() {
    }

    public TransactionTooLargeException(String msg) {
        super(msg);
    }
}
