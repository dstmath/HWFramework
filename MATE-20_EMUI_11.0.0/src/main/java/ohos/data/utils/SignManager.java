package ohos.data.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.security.keystore.KeyGenAlgorithmParaSpec;

public class SignManager {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "KeyStoreUtils");
    private static final String SIGN_ALGORITHM = "HmacSHA256";

    public static byte[] signEncryptKey(Context context, byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return bArr;
        }
        byte[] generateSign = generateSign(generateSecretKey(generateSecretKeyAlias(context)));
        if (generateSign == null || generateSign.length == 0) {
            HiLog.error(LABEL, "Sign encrypt key error.", new Object[0]);
            throw new IllegalStateException("Sign encrypt key error.");
        }
        byte[] copyOf = Arrays.copyOf(bArr, bArr.length + generateSign.length);
        System.arraycopy(generateSign, 0, copyOf, bArr.length, generateSign.length);
        Arrays.fill(generateSign, (byte) 0);
        return copyOf;
    }

    private static String generateSecretKeyAlias(Context context) {
        if (context != null) {
            String bundleName = context.getBundleName();
            if (bundleName == null || bundleName.length() == 0) {
                throw new IllegalStateException("Get package name error.");
            }
            return "database_sign_" + bundleName + "_" + SIGN_ALGORITHM;
        }
        throw new IllegalStateException("Context cannot be empty in the encrypt database.");
    }

    private static SecretKey generateSecretKey(String str) {
        try {
            KeyStore instance = KeyStore.getInstance("HarmonyKeyStore");
            instance.load(null);
            if (instance.containsAlias(str)) {
                return (SecretKey) instance.getKey(str, null);
            }
            KeyGenerator instance2 = KeyGenerator.getInstance(SIGN_ALGORITHM, "HarmonyKeyStore");
            instance2.init((AlgorithmParameterSpec) new KeyGenAlgorithmParaSpec.Builder(str).setSecKeyUsagePurposes(4).createKeyGenAlgorithmParaSpec());
            return instance2.generateKey();
        } catch (IOException | InvalidAlgorithmParameterException | KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | UnrecoverableKeyException | CertificateException e) {
            HiLog.error(LABEL, "Generate secret key error. Throw %{public}s exception : %{public}s", new Object[]{e.getClass().getName(), e.getMessage()});
            throw new IllegalStateException("Generate secret key error. Throw " + e.getClass().getName() + "exception : " + e.getMessage());
        }
    }

    private static byte[] generateSign(SecretKey secretKey) {
        try {
            Mac instance = Mac.getInstance(SIGN_ALGORITHM, "HarmonyKeyStoreBCWorkaround");
            instance.init(secretKey);
            return instance.doFinal("database_sign".getBytes(StandardCharsets.UTF_8));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException e) {
            HiLog.error(LABEL, "Generate sign error. Throw %{public}s exception : %{public}s", new Object[]{e.getClass().getName(), e.getMessage()});
            throw new IllegalStateException("Generate sign error. Throw " + e.getClass().getName() + "exception : " + e.getMessage());
        }
    }
}
