package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreECPrivateKey;
import android.security.keystore.AndroidKeyStoreECPublicKey;
import android.security.keystore.AndroidKeyStoreKey;
import android.security.keystore.AndroidKeyStorePrivateKey;
import android.security.keystore.AndroidKeyStorePublicKey;
import android.security.keystore.AndroidKeyStoreRSAPrivateKey;
import android.security.keystore.AndroidKeyStoreRSAPublicKey;
import android.security.keystore.AndroidKeyStoreSecretKey;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProtection;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.HashMap;
import javax.crypto.SecretKey;
import ohos.security.keystore.KeyGenAlgorithmParaSpec;
import ohos.security.keystore.KeyStoreKeySpec;
import ohos.security.keystore.KeyStoreProtectionParameter;

/* access modifiers changed from: package-private */
public class TransferUtils {
    private static final int CAPACITY = 6;
    private static final int ERROR_INDEX = -1;
    private static final String FIELD_NAME_ALIAS = "mAlias";
    private static final String FIELD_NAME_UID = "mUid";

    private TransferUtils() {
    }

    static Key toAndroidKey(Key key) {
        if (key instanceof PrivateKey) {
            return toAndroidPrivateKey((PrivateKey) key);
        }
        if (key instanceof PublicKey) {
            return toAndroidPublicKey((PublicKey) key);
        }
        if (key instanceof SecretKey) {
            return toAndroidSecretKey((SecretKey) key);
        }
        return key instanceof HarmonyKeyStoreKey ? ((HarmonyKeyStoreKey) key).toAndroidKey() : key;
    }

    static Key toAndroidSecretKey(SecretKey secretKey) {
        return secretKey instanceof HarmonyKeyStoreSecretKey ? ((HarmonyKeyStoreSecretKey) secretKey).toAndroidKey() : secretKey;
    }

    static Key toAndroidPrivateKey(PrivateKey privateKey) {
        return privateKey instanceof HarmonyKeyStorePrivateKey ? ((HarmonyKeyStorePrivateKey) privateKey).toAndroidPrivateKey() : privateKey;
    }

    static PublicKey toAndroidPublicKey(PublicKey publicKey) {
        return publicKey instanceof HarmonyKeyStorePublicKey ? ((HarmonyKeyStorePublicKey) publicKey).toAndroidPublicKey() : publicKey;
    }

    static PrivateKey toHarmonyPrivateKey(PrivateKey privateKey) {
        if (privateKey instanceof AndroidKeyStoreECPrivateKey) {
            AndroidKeyStoreECPrivateKey androidKeyStoreECPrivateKey = (AndroidKeyStoreECPrivateKey) privateKey;
            String str = (String) ReflectUtil.getField(androidKeyStoreECPrivateKey, FIELD_NAME_ALIAS, String.class);
            Integer num = (Integer) ReflectUtil.getField(androidKeyStoreECPrivateKey, FIELD_NAME_UID, Integer.class);
            if (num != null) {
                return new HarmonyKeyStoreECPrivateKey(str, num.intValue(), androidKeyStoreECPrivateKey.getParams());
            }
        }
        if (privateKey instanceof AndroidKeyStoreRSAPrivateKey) {
            AndroidKeyStoreRSAPrivateKey androidKeyStoreRSAPrivateKey = (AndroidKeyStoreRSAPrivateKey) privateKey;
            String str2 = (String) ReflectUtil.getField(androidKeyStoreRSAPrivateKey, FIELD_NAME_ALIAS, String.class);
            Integer num2 = (Integer) ReflectUtil.getField(androidKeyStoreRSAPrivateKey, FIELD_NAME_UID, Integer.class);
            if (num2 != null) {
                return new HarmonyKeyStoreRSAPrivateKey(str2, num2.intValue(), androidKeyStoreRSAPrivateKey.getModulus());
            }
        }
        if (!(privateKey instanceof AndroidKeyStorePrivateKey)) {
            return privateKey;
        }
        AndroidKeyStorePrivateKey androidKeyStorePrivateKey = (AndroidKeyStorePrivateKey) privateKey;
        String str3 = (String) ReflectUtil.getField(androidKeyStorePrivateKey, FIELD_NAME_ALIAS, String.class);
        Integer num3 = (Integer) ReflectUtil.getField(androidKeyStorePrivateKey, FIELD_NAME_UID, Integer.class);
        return num3 != null ? new HarmonyKeyStorePrivateKey(str3, num3.intValue(), androidKeyStorePrivateKey.getAlgorithm()) : privateKey;
    }

    static PublicKey toHarmonyPublicKey(PublicKey publicKey) {
        if (publicKey instanceof AndroidKeyStoreECPublicKey) {
            AndroidKeyStoreECPublicKey androidKeyStoreECPublicKey = (AndroidKeyStoreECPublicKey) publicKey;
            String str = (String) ReflectUtil.getField(androidKeyStoreECPublicKey, FIELD_NAME_ALIAS, String.class);
            Integer num = (Integer) ReflectUtil.getField(androidKeyStoreECPublicKey, FIELD_NAME_UID, Integer.class);
            if (num != null) {
                return new HarmonyKeyStoreECPublicKey(str, num.intValue(), androidKeyStoreECPublicKey);
            }
        }
        if (publicKey instanceof AndroidKeyStoreRSAPublicKey) {
            AndroidKeyStoreRSAPublicKey androidKeyStoreRSAPublicKey = (AndroidKeyStoreRSAPublicKey) publicKey;
            String str2 = (String) ReflectUtil.getField(androidKeyStoreRSAPublicKey, FIELD_NAME_ALIAS, String.class);
            Integer num2 = (Integer) ReflectUtil.getField(androidKeyStoreRSAPublicKey, FIELD_NAME_UID, Integer.class);
            if (num2 != null) {
                return new HarmonyKeyStoreRSAPublicKey(str2, num2.intValue(), androidKeyStoreRSAPublicKey);
            }
        }
        if (!(publicKey instanceof AndroidKeyStorePublicKey)) {
            return publicKey;
        }
        AndroidKeyStorePublicKey androidKeyStorePublicKey = (AndroidKeyStorePublicKey) publicKey;
        String str3 = (String) ReflectUtil.getField(androidKeyStorePublicKey, FIELD_NAME_ALIAS, String.class);
        Integer num3 = (Integer) ReflectUtil.getField(androidKeyStorePublicKey, FIELD_NAME_UID, Integer.class);
        return num3 != null ? new HarmonyKeyStorePublicKey(str3, num3.intValue(), androidKeyStorePublicKey.getAlgorithm(), androidKeyStorePublicKey.getEncoded()) : publicKey;
    }

    static SecretKey toHarmonySecretKey(SecretKey secretKey) {
        if (!(secretKey instanceof AndroidKeyStoreSecretKey)) {
            return secretKey;
        }
        AndroidKeyStoreSecretKey androidKeyStoreSecretKey = (AndroidKeyStoreSecretKey) secretKey;
        String str = (String) ReflectUtil.getField(androidKeyStoreSecretKey, FIELD_NAME_ALIAS, String.class);
        Integer num = (Integer) ReflectUtil.getField(androidKeyStoreSecretKey, FIELD_NAME_UID, Integer.class);
        return num != null ? new HarmonyKeyStoreSecretKey(str, num.intValue(), androidKeyStoreSecretKey.getAlgorithm()) : secretKey;
    }

    static Key toHarmonyKey(Key key) {
        if (key instanceof PrivateKey) {
            return toHarmonyPrivateKey((PrivateKey) key);
        }
        if (key instanceof PublicKey) {
            return toHarmonyPublicKey((PublicKey) key);
        }
        if (key instanceof SecretKey) {
            return toHarmonySecretKey((SecretKey) key);
        }
        if (!(key instanceof AndroidKeyStoreKey)) {
            return key;
        }
        AndroidKeyStoreKey androidKeyStoreKey = (AndroidKeyStoreKey) key;
        String str = (String) ReflectUtil.getField(androidKeyStoreKey, FIELD_NAME_ALIAS, String.class);
        Integer num = (Integer) ReflectUtil.getField(androidKeyStoreKey, FIELD_NAME_UID, Integer.class);
        return num != null ? new HarmonyKeyStoreKey(str, num.intValue(), androidKeyStoreKey.getAlgorithm()) : key;
    }

    static AlgorithmParameterSpec convertParam(AlgorithmParameterSpec algorithmParameterSpec) {
        if (!(algorithmParameterSpec instanceof KeyGenAlgorithmParaSpec)) {
            return algorithmParameterSpec;
        }
        KeyGenAlgorithmParaSpec keyGenAlgorithmParaSpec = (KeyGenAlgorithmParaSpec) algorithmParameterSpec;
        KeyGenParameterSpec.Builder randomizedEncryptionRequired = new KeyGenParameterSpec.Builder(keyGenAlgorithmParaSpec.getSecKeyAlias(), keyGenAlgorithmParaSpec.getSecKeyUsagePurposes()).setBlockModes(keyGenAlgorithmParaSpec.getSecKeyCryptoAttr(2)).setDigests(keyGenAlgorithmParaSpec.getSecKeyCryptoAttr(0)).setEncryptionPaddings(keyGenAlgorithmParaSpec.getSecKeyCryptoAttr(1)).setSignaturePaddings(keyGenAlgorithmParaSpec.getSecKeyCryptoAttr(3)).setUserAuthenticationRequired(keyGenAlgorithmParaSpec.isKeyAccessible(1)).setUserAuthenticationValidityDurationSeconds(keyGenAlgorithmParaSpec.getSecKeyAuthDuration()).setUnlockedDeviceRequired(keyGenAlgorithmParaSpec.isKeyAccessible(4)).setRandomizedEncryptionRequired(keyGenAlgorithmParaSpec.isKeyAccessible(8));
        if (keyGenAlgorithmParaSpec.getSecKeyAlgorithmParameterSpec() != null) {
            randomizedEncryptionRequired.setAlgorithmParameterSpec(keyGenAlgorithmParaSpec.getSecKeyAlgorithmParameterSpec());
        }
        if (keyGenAlgorithmParaSpec.getSecKeySizeInBits() > 0) {
            randomizedEncryptionRequired.setKeySize(keyGenAlgorithmParaSpec.getSecKeySizeInBits());
        }
        return randomizedEncryptionRequired.build();
    }

    static KeyStore.ProtectionParameter convertParam(KeyStore.ProtectionParameter protectionParameter) {
        if (!(protectionParameter instanceof KeyStoreProtectionParameter)) {
            return protectionParameter;
        }
        KeyStoreProtectionParameter keyStoreProtectionParameter = (KeyStoreProtectionParameter) protectionParameter;
        return new KeyProtection.Builder(keyStoreProtectionParameter.getSecKeyUsagePurposes()).setEncryptionPaddings(keyStoreProtectionParameter.getSecKeyCryptoAttrs(1)).setSignaturePaddings(keyStoreProtectionParameter.getSecKeyCryptoAttrs(3)).setDigests(keyStoreProtectionParameter.getSecKeyCryptoAttrs(0)).setBlockModes(keyStoreProtectionParameter.getSecKeyCryptoAttrs(2)).setUserAuthenticationRequired(keyStoreProtectionParameter.isKeyAccessible(1)).setUserAuthenticationValidityDurationSeconds(keyStoreProtectionParameter.getSecKeyAuthDuration()).setUnlockedDeviceRequired(keyStoreProtectionParameter.isKeyAccessible(4)).setRandomizedEncryptionRequired(keyStoreProtectionParameter.isKeyAccessible(8)).build();
    }

    static KeySpec convertKeySpec(KeySpec keySpec) {
        if (!(keySpec instanceof KeyInfo)) {
            return keySpec;
        }
        KeyInfo keyInfo = (KeyInfo) keySpec;
        int i = keyInfo.isUserAuthenticationRequired() ? 1 : 0;
        String[] encryptionPaddings = keyInfo.getEncryptionPaddings();
        String[] signaturePaddings = keyInfo.getSignaturePaddings();
        String[] digests = keyInfo.getDigests();
        String[] blockModes = keyInfo.getBlockModes();
        HashMap hashMap = new HashMap(6);
        hashMap.put(1, encryptionPaddings);
        hashMap.put(3, signaturePaddings);
        hashMap.put(0, digests);
        hashMap.put(2, blockModes);
        String keystoreAlias = keyInfo.getKeystoreAlias();
        int keySize = keyInfo.getKeySize();
        int origin = keyInfo.getOrigin();
        int purposes = keyInfo.getPurposes();
        int userAuthenticationValidityDurationSeconds = keyInfo.getUserAuthenticationValidityDurationSeconds();
        KeyStoreKeySpec keyStoreKeySpec = new KeyStoreKeySpec(keystoreAlias, origin, keySize, purposes, hashMap);
        keyStoreKeySpec.setSecKeyAccessibleAttr(i);
        keyStoreKeySpec.setAuthDuration(userAuthenticationValidityDurationSeconds);
        return keyStoreKeySpec;
    }

    static String getInnerClassName(String str) {
        int indexOf = str.indexOf(36);
        return indexOf != -1 ? str.substring(indexOf) : "";
    }

    public static KeyPair toHarmonyKeyPair(KeyPair keyPair) {
        return new KeyPair(toHarmonyPublicKey(keyPair.getPublic()), toHarmonyPrivateKey(keyPair.getPrivate()));
    }
}
