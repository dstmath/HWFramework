package android.security.keystore.recovery;

import android.annotation.SystemApi;
import java.security.GeneralSecurityException;

@SystemApi
public class DecryptionFailedException extends GeneralSecurityException {
    public DecryptionFailedException(String msg) {
        super(msg);
    }
}
