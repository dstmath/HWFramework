package ohos.security.keystore.provider;

import java.security.ProviderException;

public class KeyStoreConnectException extends ProviderException {
    private static final long serialVersionUID = 4792915827894478442L;

    public KeyStoreConnectException() {
        super("Failed to communicate with keystore service");
    }
}
