package ohos.security.keystore.provider;

import android.security.keystore.KeyInfo;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactorySpi;
import ohos.security.keystore.KeyStoreKeySpec;

public class HarmonyKeyStoreSecretKeyFactorySpi extends SecretKeyFactorySpi {
    private Object androidKeyFactorySpi = ReflectUtil.getInstance("android.security.keystore.AndroidKeyStoreSecretKeyFactorySpi");

    /* access modifiers changed from: protected */
    @Override // javax.crypto.SecretKeyFactorySpi
    public KeySpec engineGetKeySpec(SecretKey secretKey, Class<?> cls) throws InvalidKeySpecException {
        Key androidSecretKey = TransferUtils.toAndroidSecretKey(secretKey);
        if (KeyStoreKeySpec.class.equals(cls)) {
            cls = KeyInfo.class;
        }
        if (androidSecretKey instanceof SecretKey) {
            secretKey = (SecretKey) androidSecretKey;
        }
        InvokeResult invoke = ReflectUtil.invoke(this.androidKeyFactorySpi, new Class[]{SecretKey.class, Class.class}, new Object[]{secretKey, cls}, KeySpec.class);
        Throwable throwable = invoke.getThrowable();
        if (!(throwable instanceof InvalidKeySpecException)) {
            return TransferUtils.convertKeySpec((KeySpec) invoke.getResult());
        }
        throw ((InvalidKeySpecException) throwable);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.SecretKeyFactorySpi
    public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
        throw new InvalidKeySpecException("KeyGenerator should be used to generate secret key");
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.SecretKeyFactorySpi
    public SecretKey engineTranslateKey(SecretKey secretKey) throws InvalidKeyException {
        Key androidSecretKey = TransferUtils.toAndroidSecretKey(secretKey);
        if (!(androidSecretKey instanceof SecretKey)) {
            return secretKey;
        }
        InvokeResult invoke = ReflectUtil.invoke(this.androidKeyFactorySpi, new Class[]{SecretKey.class}, new Object[]{(SecretKey) androidSecretKey}, SecretKey.class);
        Throwable throwable = invoke.getThrowable();
        if (!(throwable instanceof InvalidKeyException)) {
            return TransferUtils.toHarmonySecretKey((SecretKey) invoke.getResult());
        }
        throw ((InvalidKeyException) throwable);
    }
}
