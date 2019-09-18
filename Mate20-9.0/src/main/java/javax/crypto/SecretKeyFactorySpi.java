package javax.crypto;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public abstract class SecretKeyFactorySpi {
    /* access modifiers changed from: protected */
    public abstract SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException;

    /* access modifiers changed from: protected */
    public abstract KeySpec engineGetKeySpec(SecretKey secretKey, Class<?> cls) throws InvalidKeySpecException;

    /* access modifiers changed from: protected */
    public abstract SecretKey engineTranslateKey(SecretKey secretKey) throws InvalidKeyException;
}
