package com.huawei.security.keystore;

import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwpartsecurity.BuildConfig;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwExportResult;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keystore.HwKeyProperties;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

public class HwUniversalKeyStoreProvider extends Provider {
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
    private static final String KEYSTORE_PRIVATE_KEY_CLASS_NAME = "com.huawei.security.keystore.HwUniversalKeyStorePrivateKey";
    private static final String KEYSTORE_PUBLIC_KEY_CLASS_NAME = "com.huawei.security.keystore.HwUniversalKeyStorePublicKey";
    private static final String KEYSTORE_SECRET_KEY_CLASS_NAME = "com.huawei.security.keystore.HwUniversalKeyStoreSecretKey";
    private static final String PACKAGE_NAME = "com.huawei.security.keystore";
    private static final String PROVIDER_NAME = "HwUniversalKeyStoreProvider";
    private static final long serialVersionUID = 1;

    protected HwUniversalKeyStoreProvider(String providerName, double version, String providerInfo) {
        super(providerName, version, providerInfo);
    }

    public HwUniversalKeyStoreProvider() {
        this(PROVIDER_NAME, 1.0d, "Huawei KeyStore security provider");
        put("KeyStore.HwKeystore", "com.huawei.security.keystore.HwUniversalKeyStoreSpi");
        put("KeyPairGenerator.EC", "com.huawei.security.keystore.HwUniversalKeyStoreKeyPairGeneratorSpi$EC");
        put("KeyPairGenerator.RSA", "com.huawei.security.keystore.HwUniversalKeyStoreKeyPairGeneratorSpi$RSA");
        if (IS_CHINA_AREA) {
            put("KeyPairGenerator.SM2", "com.huawei.security.keystore.HwGmKeyStoreKeyPairGeneratorSpi$SM2");
            put("KeyGenerator.SM4", "com.huawei.security.keystore.HwUniversalKeyStoreKeyGeneratorSpi$SM4");
            put("KeyGenerator.HmacSM3", "com.huawei.security.keystore.HwUniversalKeyStoreKeyGeneratorSpi$HmacSM3");
        }
        putSignatureImpl("SHA256withRSA", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA256WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA256WithRSAEncryption", "SHA256withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.11", "SHA256withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.1", "SHA256withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.11", "SHA256withRSA");
        putSignatureImpl("SHA384withRSA", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA384WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA384WithRSAEncryption", "SHA384withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.12", "SHA384withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.2with1.2.840.113549.1.1.1", "SHA384withRSA");
        putSignatureImpl("SHA512withRSA", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA512WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA512WithRSAEncryption", "SHA512withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.13", "SHA512withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.3with1.2.840.113549.1.1.1", "SHA512withRSA");
        putSignatureImpl("SHA256withRSA/PSS", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA256WithPSSPadding");
        putSignatureImpl("SHA384withRSA/PSS", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA384WithPSSPadding");
        putSignatureImpl("SHA512withRSA/PSS", "com.huawei.security.keystore.HwUniversalKeyStoreRSASignatureSpi$SHA512WithPSSPadding");
        putSignatureImpl("SHA256withECDSA", "com.huawei.security.keystore.HwUniversalKeyStoreECSignatureSpi$SHA256");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.2", "SHA256withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.10045.2.1", "SHA256withECDSA");
        putSignatureImpl("SHA384withECDSA", "com.huawei.security.keystore.HwUniversalKeyStoreECSignatureSpi$SHA384");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.3", "SHA384withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.2with1.2.840.10045.2.1", "SHA384withECDSA");
        putSignatureImpl("SHA512withECDSA", "com.huawei.security.keystore.HwUniversalKeyStoreECSignatureSpi$SHA512");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.4", "SHA512withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.3with1.2.840.10045.2.1", "SHA512withECDSA");
        if (IS_CHINA_AREA) {
            putSignatureImpl("SM3withSM2DSA", "com.huawei.security.keystore.HwGmKeyStoreSM2SignatureSpi$SM3");
            put("Alg.Alias.Signature.1.2.156.197.1.501", "SM3withSM2DSA");
            put("Alg.Alias.Signature.1.2.156.197.1.401.1with1.2.156.197.1.301.1", "SM3withSM2DSA");
        }
        putAsymmetricCipherImpl("RSA/ECB/PKCS1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$PKCS1Padding");
        put("Alg.Alias.Cipher.RSA/None/PKCS1Padding", "RSA/ECB/PKCS1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$OAEPWithSHA256AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-256AndMGF1Padding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-384AndMGF1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$OAEPWithSHA384AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-384AndMGF1Padding", "RSA/ECB/OAEPWithSHA-384AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$OAEPWithSHA512AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-512AndMGF1Padding", "RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
        if (IS_CHINA_AREA) {
            putAsymmetricCipherImpl("SM2/NONE/NoPadding", "com.huawei.security.keystore.HwGmKeyStoreSM2CipherSpi$NoPadding");
            put("Alg.Alias.Cipher.SM2/NONE/NoPadding", "SM2/NONE/NoPadding");
            putSymmetricCipherImpl("SM4/CBC/NoPadding", "com.huawei.security.keystore.HwGmKeyStoreSM4CipherSpi$CBC$NoPadding");
            putSymmetricCipherImpl("SM4/CBC/PKCS7Padding", "com.huawei.security.keystore.HwGmKeyStoreSM4CipherSpi$CBC$PKCS7Padding");
            putSymmetricCipherImpl("SM4/CTR/NoPadding", "com.huawei.security.keystore.HwGmKeyStoreSM4CipherSpi$CTR$NoPadding");
            putMacImpl(HwKeyProperties.KEY_ALGORITHM_HMAC_SM3, "com.huawei.security.keystore.HwUniversalKeyStoreHmacSpi$HmacSM3");
            put("Alg.Alias.Mac.1.2.156.197.1.401.2", HwKeyProperties.KEY_ALGORITHM_HMAC_SM3);
            put("Alg.Alias.Mac.HMAC-SM3", HwKeyProperties.KEY_ALGORITHM_HMAC_SM3);
            put("Alg.Alias.Mac.HMAC/SM3", HwKeyProperties.KEY_ALGORITHM_HMAC_SM3);
            putMessageDigestImpl(HwKeyProperties.DIGEST_SM3, "com.huawei.security.keystore.HwUniversalKeyStoreMessageDigestSpi$SM3");
            put("Alg.Alias.MessageDigest.1.2.156.197.1.401.1", HwKeyProperties.DIGEST_SM3);
        }
    }

    public static void install() {
        Security.addProvider(new HwUniversalKeyStoreProvider());
    }

    @NonNull
    public static HwUniversalKeyStorePrivateKey loadHwKeyStorePrivateKeyFromKeystore(@NonNull HwKeystoreManager keyStore, @NonNull String privateKeyAlias, int uid) throws UnrecoverableKeyException {
        return (HwUniversalKeyStorePrivateKey) loadHwKeyStoreKeyPairFromKeystore(keyStore, privateKeyAlias, uid).getPrivate();
    }

    @NonNull
    public static HwUniversalKeyStoreSecretKey loadHwKeyStoreSecretKeyFromKeystore(@NonNull HwKeystoreManager keyStore, @NonNull String secretKeyAlias, int uid) throws UnrecoverableKeyException {
        int keymasterDigest;
        HwKeyCharacteristics keyCharacteristics = new HwKeyCharacteristics();
        int errorCode = keyStore.getKeyCharacteristics(secretKeyAlias, null, null, uid, keyCharacteristics);
        if (errorCode == 1) {
            Integer keymasterAlgorithm = keyCharacteristics.getEnum(HwKeymasterDefs.KM_TAG_ALGORITHM);
            if (keymasterAlgorithm != null) {
                List<Integer> keymasterDigests = keyCharacteristics.getEnums(HwKeymasterDefs.KM_TAG_DIGEST);
                if (!keymasterDigests.isEmpty()) {
                    keymasterDigest = keymasterDigests.get(0).intValue();
                } else {
                    keymasterDigest = -1;
                }
                try {
                    return new HwUniversalKeyStoreSecretKey(secretKeyAlias, uid, HwKeyProperties.KeyAlgorithm.fromKeymasterSecretKeyAlgorithm(keymasterAlgorithm.intValue(), keymasterDigest));
                } catch (IllegalArgumentException e) {
                    UnrecoverableKeyException newEx = new UnrecoverableKeyException("Unsupported secret key type");
                    newEx.initCause(e);
                    throw newEx;
                }
            } else {
                throw new UnrecoverableKeyException("Key algorithm unknown");
            }
        } else {
            UnrecoverableKeyException unrecoverableKeyException = new UnrecoverableKeyException("Failed to obtain information about secret key");
            unrecoverableKeyException.initCause(HwKeystoreManager.getKeyStoreException(errorCode));
            throw unrecoverableKeyException;
        }
    }

    @NonNull
    public static KeyPair loadHwKeyStoreKeyPairFromKeystore(@NonNull HwKeystoreManager keyStore, @NonNull String privateKeyAlias, int uid) throws UnrecoverableKeyException {
        HwUniversalKeyStorePublicKey publicKey = loadHwKeyStorePublicKeyFromKeystore(keyStore, privateKeyAlias, uid);
        Log.i(PROVIDER_NAME, "load public key succeed!");
        HwUniversalKeyStorePrivateKey privateKey = getHwKeyStorePrivateKey(publicKey);
        Log.i(PROVIDER_NAME, "load private key succeed!");
        return new KeyPair(publicKey, privateKey);
    }

    @NonNull
    public static HwUniversalKeyStorePublicKey getHwKeyStorePublicKey(@NonNull String alias, int uid, @NonNull String keyAlgorithm, @NonNull byte[] x509EncodedForm) {
        if (HwKeyProperties.KEY_ALGORITHM_SM2.equalsIgnoreCase(keyAlgorithm)) {
            return new HwUniversalKeyStoreSM2PublicKey(alias, uid, x509EncodedForm, null, null);
        }
        try {
            PublicKey publicKey = KeyFactory.getInstance(keyAlgorithm).generatePublic(new X509EncodedKeySpec(x509EncodedForm));
            if (HwKeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(keyAlgorithm)) {
                return new HwUniversalKeyStoreRSAPublicKey(alias, uid, (RSAPublicKey) publicKey);
            }
            if (HwKeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(keyAlgorithm)) {
                return new HwUniversalKeyStoreECPublicKey(alias, uid, (ECPublicKey) publicKey);
            }
            throw new ProviderException("Unsupported Huawei Keystore public key algorithm: " + keyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException("Failed to obtain " + keyAlgorithm + " KeyFactory" + e.getMessage());
        } catch (InvalidKeySpecException e2) {
            throw new ProviderException("Invalid X.509 encoding of public key:" + e2.getMessage() + ",alg:" + keyAlgorithm);
        }
    }

    @NonNull
    public static HwUniversalKeyStorePrivateKey getHwKeyStorePrivateKey(@NonNull HwUniversalKeyStorePublicKey publicKey) {
        String keyAlgorithm = publicKey.getAlgorithm();
        if (HwKeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(keyAlgorithm)) {
            return new HwUniversalKeyStoreRSAPrivateKey(publicKey.getAlias(), publicKey.getUid(), ((RSAKey) publicKey).getModulus());
        }
        if (HwKeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(keyAlgorithm)) {
            return new HwUniversalKeyStoreECPrivateKey(publicKey.getAlias(), publicKey.getUid(), ((ECKey) publicKey).getParams());
        }
        if (HwKeyProperties.KEY_ALGORITHM_SM2.equalsIgnoreCase(keyAlgorithm)) {
            return new HwUniversalKeyStoreSM2PrivateKey(publicKey.getAlias(), publicKey.getUid(), ((ECKey) publicKey).getParams());
        }
        throw new ProviderException("Unsupported Huawei Keystore public key algorithm: " + keyAlgorithm);
    }

    @NonNull
    public static HwUniversalKeyStorePublicKey loadHwKeyStorePublicKeyFromKeystore(@NonNull HwKeystoreManager keyStore, @NonNull String privateKeyAlias, int uid) throws UnrecoverableKeyException {
        HwKeyCharacteristics keyCharacteristics = new HwKeyCharacteristics();
        int errorCode = keyStore.getKeyCharacteristics(privateKeyAlias, null, null, uid, keyCharacteristics);
        if (errorCode == 1) {
            HwExportResult exportResult = keyStore.exportKey(privateKeyAlias, 0, null, null, uid);
            if (exportResult.resultCode == 1) {
                byte[] x509EncodedPublicKey = exportResult.exportData;
                Integer keymasterAlgorithm = keyCharacteristics.getEnum(HwKeymasterDefs.KM_TAG_ALGORITHM);
                if (keymasterAlgorithm != null) {
                    boolean keymasterIsFromGm = keyCharacteristics.getBoolean(HwKeymasterDefs.KM_TAG_IS_FROM_GM);
                    if (keymasterIsFromGm) {
                        Log.i(PROVIDER_NAME, "is from gm!");
                    }
                    try {
                        String jcaKeyAlgorithm = HwKeyProperties.KeyAlgorithm.fromKeymasterAsymmetricKeyAlgorithm(keymasterAlgorithm.intValue());
                        if (keymasterIsFromGm) {
                            jcaKeyAlgorithm = HwKeyProperties.KEY_ALGORITHM_SM2;
                        }
                        return getHwKeyStorePublicKey(privateKeyAlias, uid, jcaKeyAlgorithm, x509EncodedPublicKey);
                    } catch (IllegalArgumentException e) {
                        UnrecoverableKeyException unrecoverableKeyException = new UnrecoverableKeyException("Failed to load private key");
                        unrecoverableKeyException.initCause(HwKeystoreManager.getKeyStoreException(errorCode));
                        throw unrecoverableKeyException;
                    }
                } else {
                    throw new UnrecoverableKeyException("Key algorithm unknown");
                }
            } else {
                UnrecoverableKeyException unrecoverableKeyException2 = new UnrecoverableKeyException("Failed to obtain X.509 form of public key");
                unrecoverableKeyException2.initCause(HwKeystoreManager.getKeyStoreException(errorCode));
                throw unrecoverableKeyException2;
            }
        } else {
            UnrecoverableKeyException unrecoverableKeyException3 = new UnrecoverableKeyException("Failed to obtain information about private key");
            unrecoverableKeyException3.initCause(HwKeystoreManager.getKeyStoreException(errorCode));
            throw unrecoverableKeyException3;
        }
    }

    public static String[] getSupportedEcdsaSignatureDigests() {
        return new String[]{HwKeyProperties.DIGEST_SHA256, HwKeyProperties.DIGEST_SHA384, HwKeyProperties.DIGEST_SHA512};
    }

    public static String[] getSupportedRsaSignatureWithPkcs1PaddingDigests() {
        return new String[]{HwKeyProperties.DIGEST_SHA256, HwKeyProperties.DIGEST_SHA384, HwKeyProperties.DIGEST_SHA512};
    }

    private void putSignatureImpl(String algorithm, String implClass) {
        put("Signature." + algorithm, implClass);
        put("Signature." + algorithm + " SupportedKeyClasses", "com.huawei.security.keystore.HwUniversalKeyStorePrivateKey|com.huawei.security.keystore.HwUniversalKeyStorePublicKey");
    }

    private void putAsymmetricCipherImpl(String transformation, String implClass) {
        put("Cipher." + transformation, implClass);
        put("Cipher." + transformation + " SupportedKeyClasses", "com.huawei.security.keystore.HwUniversalKeyStorePrivateKey|com.huawei.security.keystore.HwUniversalKeyStorePublicKey");
    }

    private void putSymmetricCipherImpl(String transformation, String implClass) {
        put("Cipher." + transformation, implClass);
        put("Cipher." + transformation + " SupportedKeyClasses", KEYSTORE_SECRET_KEY_CLASS_NAME);
    }

    private void putMacImpl(String algorithm, String implClass) {
        put("Mac." + algorithm, implClass);
        put("Mac." + algorithm + " SupportedKeyClasses", KEYSTORE_SECRET_KEY_CLASS_NAME);
    }

    private void putMessageDigestImpl(String algorithm, String implClass) {
        put("MessageDigest." + algorithm, implClass);
    }
}
