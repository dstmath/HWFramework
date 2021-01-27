package ohos.security.keystore.provider;

import java.security.Provider;
import ohos.security.keystore.KeyStoreConstants;

class HarmonyKeyStoreBCWorkaroundProvider extends Provider {
    private static final String CIPHER_PREFIX = "Cipher.";
    private static final String KEYSTORE_PRIVATE_KEY_CLASS_NAME = "ohos.security.keystore.provider.HarmonyKeyStorePrivateKey";
    private static final String KEYSTORE_PUBLIC_KEY_CLASS_NAME = "ohos.security.keystore.provider.HarmonyKeyStorePublicKey";
    private static final String KEYSTORE_SECRET_KEY_CLASS_NAME = "ohos.security.keystore.provider.HarmonyKeyStoreSecretKey";
    private static final String PACKAGE_NAME = "ohos.security.keystore.provider";
    private static final String SIGNATURE_SUPPORTED_KEY_CLASS = " SupportedKeyClasses";
    private static final double VERSION = 1.0d;
    private static final long serialVersionUID = -8163013617703977846L;

    HarmonyKeyStoreBCWorkaroundProvider() {
        super("HarmonyKeyStoreBCWorkaround", VERSION, "Harmony KeyStore security provider to work around Bouncy Castle");
        addMacImpl();
        addSymmetricCipherImpl();
        addSignatureImplWithRsa();
        addSignatureImplWithEcdsa();
    }

    private void addSignatureImplWithEcdsa() {
        putSignatureImpl("NONEwithECDSA", "ohos.security.keystore.provider.HarmonyKeyStoreECDSASignatureSpi$NONE");
        putSignatureImpl("SHA1withECDSA", "ohos.security.keystore.provider.HarmonyKeyStoreECDSASignatureSpi$SHA1");
        put("Alg.Alias.Signature.ECDSA", "SHA1withECDSA");
        put("Alg.Alias.Signature.ECDSAwithSHA1", "SHA1withECDSA");
        put("Alg.Alias.Signature.1.2.840.10045.4.1", "SHA1withECDSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.10045.2.1", "SHA1withECDSA");
        putSignatureImpl("SHA224withECDSA", "ohos.security.keystore.provider.HarmonyKeyStoreECDSASignatureSpi$SHA224");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.1", "SHA224withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.10045.2.1", "SHA224withECDSA");
        putSignatureImpl("SHA256withECDSA", "ohos.security.keystore.provider.HarmonyKeyStoreECDSASignatureSpi$SHA256");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.2", "SHA256withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.10045.2.1", "SHA256withECDSA");
        putSignatureImpl("SHA384withECDSA", "ohos.security.keystore.provider.HarmonyKeyStoreECDSASignatureSpi$SHA384");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.3", "SHA384withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.2with1.2.840.10045.2.1", "SHA384withECDSA");
        putSignatureImpl("SHA512withECDSA", "ohos.security.keystore.provider.HarmonyKeyStoreECDSASignatureSpi$SHA512");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.4", "SHA512withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.3with1.2.840.10045.2.1", "SHA512withECDSA");
    }

    private void addSignatureImplWithRsa() {
        putSignatureImpl("NONEwithRSA", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$NONEWithPKCS1Padding");
        putSignatureImpl("MD5withRSA", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$MD5WithPKCS1Padding");
        put("Alg.Alias.Signature.MD5WithRSAEncryption", "MD5withRSA");
        put("Alg.Alias.Signature.MD5/RSA", "MD5withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.4", "MD5withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.2.5with1.2.840.113549.1.1.1", "MD5withRSA");
        putSignatureImpl("SHA1withRSA", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$SHA1WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA1WithRSAEncryption", "SHA1withRSA");
        put("Alg.Alias.Signature.SHA1/RSA", "SHA1withRSA");
        put("Alg.Alias.Signature.SHA-1/RSA", "SHA1withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.5", "SHA1withRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.113549.1.1.1", "SHA1withRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.113549.1.1.5", "SHA1withRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.29", "SHA1withRSA");
        putSignatureImpl("SHA224withRSA", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$SHA224WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA224WithRSAEncryption", "SHA224withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.11", "SHA224withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.113549.1.1.1", "SHA224withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.113549.1.1.11", "SHA224withRSA");
        putSignatureImpl("SHA256withRSA", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$SHA256WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA256WithRSAEncryption", "SHA256withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.11", "SHA256withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.1", "SHA256withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.11", "SHA256withRSA");
        putSignatureImpl("SHA384withRSA", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$SHA384WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA384WithRSAEncryption", "SHA384withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.12", "SHA384withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.2with1.2.840.113549.1.1.1", "SHA384withRSA");
        putSignatureImpl("SHA512withRSA", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$SHA512WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA512WithRSAEncryption", "SHA512withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.13", "SHA512withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.3with1.2.840.113549.1.1.1", "SHA512withRSA");
        putSignatureImpl("SHA1withRSA/PSS", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$SHA1WithPSSPadding");
        putSignatureImpl("SHA224withRSA/PSS", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$SHA224WithPSSPadding");
        putSignatureImpl("SHA256withRSA/PSS", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$SHA256WithPSSPadding");
        putSignatureImpl("SHA384withRSA/PSS", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$SHA384WithPSSPadding");
        putSignatureImpl("SHA512withRSA/PSS", "ohos.security.keystore.provider.HarmonyKeyStoreRSASignatureSpi$SHA512WithPSSPadding");
    }

    private void addSymmetricCipherImpl() {
        putSymmetricCipherImpl("AES/ECB/NoPadding", "ohos.security.keystore.provider.HarmonyKeyStoreUnauthenticatedAESCipherSpi$ECB$NoPadding");
        putSymmetricCipherImpl("AES/ECB/PKCS7Padding", "ohos.security.keystore.provider.HarmonyKeyStoreUnauthenticatedAESCipherSpi$ECB$PKCS7Padding");
        putSymmetricCipherImpl("AES/CBC/NoPadding", "ohos.security.keystore.provider.HarmonyKeyStoreUnauthenticatedAESCipherSpi$CBC$NoPadding");
        putSymmetricCipherImpl("AES/CBC/PKCS7Padding", "ohos.security.keystore.provider.HarmonyKeyStoreUnauthenticatedAESCipherSpi$CBC$PKCS7Padding");
        putSymmetricCipherImpl("AES/CTR/NoPadding", "ohos.security.keystore.provider.HarmonyKeyStoreUnauthenticatedAESCipherSpi$CTR$NoPadding");
        putSymmetricCipherImpl("AES/GCM/NoPadding", "ohos.security.keystore.provider.HarmonyKeyStoreAuthenticatedAESCipherSpi$GCM$NoPadding");
        putAsymmetricCipherImpl("RSA/ECB/NoPadding", "ohos.security.keystore.provider.HarmonyKeyStoreRSACipherSpi$NoPadding");
        put("Alg.Alias.Cipher.RSA/None/NoPadding", "RSA/ECB/NoPadding");
        putAsymmetricCipherImpl("RSA/ECB/PKCS1Padding", "ohos.security.keystore.provider.HarmonyKeyStoreRSACipherSpi$PKCS1Padding");
        put("Alg.Alias.Cipher.RSA/None/PKCS1Padding", "RSA/ECB/PKCS1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPPadding", "ohos.security.keystore.provider.HarmonyKeyStoreRSACipherSpi$OAEPWithSHA1AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPPadding", "RSA/ECB/OAEPPadding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", "ohos.security.keystore.provider.HarmonyKeyStoreRSACipherSpi$OAEPWithSHA1AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-1AndMGF1Padding", "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-224AndMGF1Padding", "ohos.security.keystore.provider.HarmonyKeyStoreRSACipherSpi$OAEPWithSHA224AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-224AndMGF1Padding", "RSA/ECB/OAEPWithSHA-224AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "ohos.security.keystore.provider.HarmonyKeyStoreRSACipherSpi$OAEPWithSHA256AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-256AndMGF1Padding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-384AndMGF1Padding", "ohos.security.keystore.provider.HarmonyKeyStoreRSACipherSpi$OAEPWithSHA384AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-384AndMGF1Padding", "RSA/ECB/OAEPWithSHA-384AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", "ohos.security.keystore.provider.HarmonyKeyStoreRSACipherSpi$OAEPWithSHA512AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-512AndMGF1Padding", "RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
    }

    private void addMacImpl() {
        putMacImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1, "ohos.security.keystore.provider.HarmonyKeyStoreHmacSpi$HmacSHA1");
        put("Alg.Alias.Mac.1.2.840.113549.2.7", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        put("Alg.Alias.Mac.HMAC-SHA1", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        put("Alg.Alias.Mac.HMAC/SHA1", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        putMacImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224, "ohos.security.keystore.provider.HarmonyKeyStoreHmacSpi$HmacSHA224");
        put("Alg.Alias.Mac.1.2.840.113549.2.9", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        put("Alg.Alias.Mac.HMAC-SHA224", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        put("Alg.Alias.Mac.HMAC/SHA224", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        putMacImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256, "ohos.security.keystore.provider.HarmonyKeyStoreHmacSpi$HmacSHA256");
        put("Alg.Alias.Mac.1.2.840.113549.2.9", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.Mac.HMAC-SHA256", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.Mac.HMAC/SHA256", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        putMacImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384, "ohos.security.keystore.provider.HarmonyKeyStoreHmacSpi$HmacSHA384");
        put("Alg.Alias.Mac.1.2.840.113549.2.10", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        put("Alg.Alias.Mac.HMAC-SHA384", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        put("Alg.Alias.Mac.HMAC/SHA384", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        putMacImpl(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512, "ohos.security.keystore.provider.HarmonyKeyStoreHmacSpi$HmacSHA512");
        put("Alg.Alias.Mac.1.2.840.113549.2.11", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
        put("Alg.Alias.Mac.HMAC-SHA512", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
        put("Alg.Alias.Mac.HMAC/SHA512", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
    }

    private void putMacImpl(String str, String str2) {
        put("Mac." + str, str2);
        put("Mac." + str + SIGNATURE_SUPPORTED_KEY_CLASS, KEYSTORE_SECRET_KEY_CLASS_NAME);
    }

    private void putSymmetricCipherImpl(String str, String str2) {
        put(CIPHER_PREFIX + str, str2);
        put(CIPHER_PREFIX + str + SIGNATURE_SUPPORTED_KEY_CLASS, KEYSTORE_SECRET_KEY_CLASS_NAME);
    }

    private void putAsymmetricCipherImpl(String str, String str2) {
        put(CIPHER_PREFIX + str, str2);
        put(CIPHER_PREFIX + str + SIGNATURE_SUPPORTED_KEY_CLASS, "ohos.security.keystore.provider.HarmonyKeyStorePrivateKey|ohos.security.keystore.provider.HarmonyKeyStorePublicKey");
    }

    private void putSignatureImpl(String str, String str2) {
        put("Signature." + str, str2);
        put("Signature." + str + SIGNATURE_SUPPORTED_KEY_CLASS, "ohos.security.keystore.provider.HarmonyKeyStorePrivateKey|ohos.security.keystore.provider.HarmonyKeyStorePublicKey");
    }
}
