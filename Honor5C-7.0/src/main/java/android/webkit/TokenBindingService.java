package android.webkit;

import android.net.Uri;
import java.security.KeyPair;

public abstract class TokenBindingService {
    public static final String KEY_ALGORITHM_ECDSAP256 = "ECDSAP256";
    public static final String KEY_ALGORITHM_RSA2048_PKCS_1_5 = "RSA2048_PKCS_1.5";
    public static final String KEY_ALGORITHM_RSA2048_PSS = "RSA2048PSS";

    public static abstract class TokenBindingKey {
        public abstract String getAlgorithm();

        public abstract KeyPair getKeyPair();
    }

    public abstract void deleteAllKeys(ValueCallback<Boolean> valueCallback);

    public abstract void deleteKey(Uri uri, ValueCallback<Boolean> valueCallback);

    public abstract void enableTokenBinding();

    public abstract void getKey(Uri uri, String[] strArr, ValueCallback<TokenBindingKey> valueCallback);

    public static TokenBindingService getInstance() {
        return WebViewFactory.getProvider().getTokenBindingService();
    }
}
