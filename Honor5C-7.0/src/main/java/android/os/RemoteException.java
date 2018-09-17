package android.os;

import android.util.AndroidException;

public class RemoteException extends AndroidException {
    public RemoteException(String message) {
        super(message);
    }

    public RuntimeException rethrowAsRuntimeException() {
        throw new RuntimeException(this);
    }

    public RuntimeException rethrowFromSystemServer() {
        if (this instanceof DeadObjectException) {
            throw new RuntimeException(new DeadSystemException());
        }
        throw new RuntimeException(this);
    }
}
