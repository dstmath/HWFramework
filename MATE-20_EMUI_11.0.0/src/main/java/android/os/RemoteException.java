package android.os;

import android.annotation.UnsupportedAppUsage;
import android.util.AndroidException;

public class RemoteException extends AndroidException {
    public RemoteException() {
    }

    public RemoteException(String message) {
        super(message);
    }

    public RemoteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RuntimeException rethrowAsRuntimeException() {
        throw new RuntimeException(this);
    }

    @UnsupportedAppUsage
    public RuntimeException rethrowFromSystemServer() {
        if (this instanceof DeadObjectException) {
            throw new RuntimeException(new DeadSystemException());
        }
        throw new RuntimeException(this);
    }
}
