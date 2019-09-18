package android.net.lowpan;

import android.os.ServiceSpecificException;
import android.util.AndroidException;

public class LowpanException extends AndroidException {
    public LowpanException() {
    }

    public LowpanException(String message) {
        super(message);
    }

    public LowpanException(String message, Throwable cause) {
        super(message, cause);
    }

    public LowpanException(Exception cause) {
        super(cause);
    }

    static LowpanException rethrowFromServiceSpecificException(ServiceSpecificException e) throws LowpanException {
        switch (e.errorCode) {
            case 2:
                throw new LowpanRuntimeException(e.getMessage() != null ? e.getMessage() : "Invalid argument", e);
            case 3:
                throw new InterfaceDisabledException((Exception) e);
            case 4:
                throw new WrongStateException((Exception) e);
            case 7:
                throw new LowpanRuntimeException(e.getMessage() != null ? e.getMessage() : "NCP problem", e);
            case 10:
                throw new OperationCanceledException((Exception) e);
            case 11:
                throw new LowpanException(e.getMessage() != null ? e.getMessage() : "Feature not supported", e);
            case 12:
                throw new JoinFailedException((Exception) e);
            case 13:
                throw new JoinFailedAtScanException((Exception) e);
            case 14:
                throw new JoinFailedAtAuthException((Exception) e);
            case 15:
                throw new NetworkAlreadyExistsException((Exception) e);
            default:
                throw new LowpanRuntimeException((Exception) e);
        }
    }
}
