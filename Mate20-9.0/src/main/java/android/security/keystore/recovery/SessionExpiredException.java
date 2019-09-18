package android.security.keystore.recovery;

import android.annotation.SystemApi;
import java.security.GeneralSecurityException;

@SystemApi
public class SessionExpiredException extends GeneralSecurityException {
    public SessionExpiredException(String msg) {
        super(msg);
    }
}
