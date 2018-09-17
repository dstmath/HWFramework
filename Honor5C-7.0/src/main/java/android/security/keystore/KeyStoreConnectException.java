package android.security.keystore;

import java.security.ProviderException;

public class KeyStoreConnectException extends ProviderException {
    public KeyStoreConnectException() {
        super("Failed to communicate with keystore service");
    }
}
