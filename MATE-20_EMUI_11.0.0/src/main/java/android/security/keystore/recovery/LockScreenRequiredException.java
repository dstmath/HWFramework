package android.security.keystore.recovery;

import android.annotation.SystemApi;
import java.security.GeneralSecurityException;

@SystemApi
public class LockScreenRequiredException extends GeneralSecurityException {
    public LockScreenRequiredException(String msg) {
        super(msg);
    }
}
