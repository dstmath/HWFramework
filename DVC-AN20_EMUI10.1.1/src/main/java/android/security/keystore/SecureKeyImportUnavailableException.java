package android.security.keystore;

import android.security.KeyStoreException;
import java.security.ProviderException;

public class SecureKeyImportUnavailableException extends ProviderException {
    public SecureKeyImportUnavailableException() {
    }

    public SecureKeyImportUnavailableException(String message) {
        super(message, new KeyStoreException(-68, "Secure Key Import not available"));
    }

    public SecureKeyImportUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecureKeyImportUnavailableException(Throwable cause) {
        super(cause);
    }
}
