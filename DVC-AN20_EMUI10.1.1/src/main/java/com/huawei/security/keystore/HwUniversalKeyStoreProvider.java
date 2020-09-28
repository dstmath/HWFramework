package com.huawei.security.keystore;

import android.support.annotation.NonNull;
import android.util.Log;
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

public class HwUniversalKeyStoreProvider extends Provider {
    private static final String KEYSTORE_PRIVATE_KEY_CLASS_NAME = "com.huawei.security.keystore.HwUniversalKeyStorePrivateKey";
    private static final String KEYSTORE_PUBLIC_KEY_CLASS_NAME = "com.huawei.security.keystore.HwUniversalKeyStorePublicKey";
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
        putAsymmetricCipherImpl("RSA/ECB/PKCS1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$PKCS1Padding");
        put("Alg.Alias.Cipher.RSA/None/PKCS1Padding", "RSA/ECB/PKCS1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$OAEPWithSHA256AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-256AndMGF1Padding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-384AndMGF1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$OAEPWithSHA384AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-384AndMGF1Padding", "RSA/ECB/OAEPWithSHA-384AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", "com.huawei.security.keystore.HwUniversalKeyStoreRSACipherSpi$OAEPWithSHA512AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-512AndMGF1Padding", "RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
    }

    public static void install() {
        Security.addProvider(new HwUniversalKeyStoreProvider());
    }

    @NonNull
    public static HwUniversalKeyStorePrivateKey loadHwKeyStorePrivateKeyFromKeystore(@NonNull HwKeystoreManager keyStore, @NonNull String privateKeyAlias, int uid) throws UnrecoverableKeyException {
        return (HwUniversalKeyStorePrivateKey) loadHwKeyStoreKeyPairFromKeystore(keyStore, privateKeyAlias, uid).getPrivate();
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
            throw new ProviderException("Invalid X.509 encoding of public key" + e2.getMessage());
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
                    try {
                        return getHwKeyStorePublicKey(privateKeyAlias, uid, HwKeyProperties.KeyAlgorithm.fromKeymasterAsymmetricKeyAlgorithm(keymasterAlgorithm.intValue()), x509EncodedPublicKey);
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
}
