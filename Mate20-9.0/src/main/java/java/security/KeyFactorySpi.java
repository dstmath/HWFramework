package java.security;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public abstract class KeyFactorySpi {
    /* access modifiers changed from: protected */
    public abstract PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException;

    /* access modifiers changed from: protected */
    public abstract PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException;

    /* access modifiers changed from: protected */
    public abstract <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> cls) throws InvalidKeySpecException;

    /* access modifiers changed from: protected */
    public abstract Key engineTranslateKey(Key key) throws InvalidKeyException;
}
