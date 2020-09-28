package com.huawei.security.keystore;

import android.security.keystore.UserNotAuthenticatedException;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keystore.ArrayUtils;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;

public class HwUniversalKeyStoreCryptoOperationUtils {
    private static volatile SecureRandom sRng;

    private HwUniversalKeyStoreCryptoOperationUtils() {
    }

    static InvalidKeyException getInvalidKeyExceptionForInit(HwKeystoreManager keyStore, HwUniversalKeyStoreKey key, int beginOpResultCode) {
        if (beginOpResultCode == 1) {
            return null;
        }
        InvalidKeyException exception = keyStore.getInvalidKeyException(key.getAlias(), key.getUid(), beginOpResultCode);
        if (beginOpResultCode == 15 && (exception instanceof UserNotAuthenticatedException)) {
            return null;
        }
        return exception;
    }

    public static GeneralSecurityException getExceptionForCipherInit(HwKeystoreManager keyStore, HwUniversalKeyStoreKey key, int beginOpResultCode) {
        if (beginOpResultCode == 1) {
            return null;
        }
        if (beginOpResultCode == -55) {
            return new InvalidAlgorithmParameterException("Caller-provided IV not permitted");
        }
        if (beginOpResultCode == -52) {
            return new InvalidAlgorithmParameterException("Invalid IV");
        }
        getInvalidKeyExceptionForInit(keyStore, key, beginOpResultCode);
        return getInvalidKeyExceptionForInit(keyStore, key, beginOpResultCode);
    }

    static byte[] getRandomBytesToMixIntoKeystoreRng(SecureRandom rng, int sizeBytes) {
        if (sizeBytes <= 0) {
            return ArrayUtils.EmptyArray.BYTE;
        }
        if (rng == null) {
            rng = getRng();
        }
        byte[] result = new byte[sizeBytes];
        rng.nextBytes(result);
        return result;
    }

    private static SecureRandom getRng() {
        if (sRng == null) {
            sRng = new SecureRandom();
        }
        return sRng;
    }
}
