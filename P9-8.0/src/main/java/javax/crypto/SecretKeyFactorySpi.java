package javax.crypto;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public abstract class SecretKeyFactorySpi {
    protected abstract SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException;

    protected abstract KeySpec engineGetKeySpec(SecretKey secretKey, Class<?> cls) throws InvalidKeySpecException;

    protected abstract SecretKey engineTranslateKey(SecretKey secretKey) throws InvalidKeyException;
}
