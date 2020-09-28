package android.security.keystore.recovery;

import android.annotation.SystemApi;
import java.security.GeneralSecurityException;

@SystemApi
public class InternalRecoveryServiceException extends GeneralSecurityException {
    public InternalRecoveryServiceException(String msg) {
        super(msg);
    }

    public InternalRecoveryServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
