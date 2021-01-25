package ohos.security.keystore.provider;

import android.security.KeyStoreException;
import android.security.keystore.KeyExpiredException;
import android.security.keystore.KeyNotYetValidException;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyStoreConnectException;
import android.security.keystore.UserNotAuthenticatedException;
import java.security.InvalidKeyException;

public abstract class ExceptionAdapter {
    public static Throwable reThrowException(Throwable th) {
        if (th instanceof UserNotAuthenticatedException) {
            return new InvalidKeyException("User not authenticated");
        }
        if (th instanceof KeyNotYetValidException) {
            return new InvalidKeyException("Key not yet valid");
        }
        if (th instanceof KeyPermanentlyInvalidatedException) {
            return new InvalidKeyException("Key permanently invalidated");
        }
        if (th instanceof KeyExpiredException) {
            return new InvalidKeyException("Key expired");
        }
        if (th instanceof InvalidKeyException) {
            return new InvalidKeyException(th.getMessage());
        }
        if (th instanceof KeyStoreException) {
            return new KeyStoreException(((KeyStoreException) th).getErrorCode(), th.getMessage());
        }
        return th instanceof KeyStoreConnectException ? new KeyStoreConnectException() : th;
    }
}
