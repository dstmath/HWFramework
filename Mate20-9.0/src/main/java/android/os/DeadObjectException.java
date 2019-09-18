package android.os;

public class DeadObjectException extends RemoteException {
    public DeadObjectException() {
    }

    public DeadObjectException(String message) {
        super(message);
    }
}
