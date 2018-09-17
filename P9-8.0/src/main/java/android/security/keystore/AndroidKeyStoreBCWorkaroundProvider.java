package android.security.keystore;

import java.security.Provider;

class AndroidKeyStoreBCWorkaroundProvider extends Provider {
    private static final String KEYSTORE_PRIVATE_KEY_CLASS_NAME = "android.security.keystore.AndroidKeyStorePrivateKey";
    private static final String KEYSTORE_PUBLIC_KEY_CLASS_NAME = "android.security.keystore.AndroidKeyStorePublicKey";
    private static final String KEYSTORE_SECRET_KEY_CLASS_NAME = "android.security.keystore.AndroidKeyStoreSecretKey";
    private static final String PACKAGE_NAME = "android.security.keystore";

    AndroidKeyStoreBCWorkaroundProvider() {
        super("AndroidKeyStoreBCWorkaround", 1.0d, "Android KeyStore security provider to work around Bouncy Castle");
        putMacImpl(KeyProperties.KEY_ALGORITHM_HMAC_SHA1, "android.security.keystore.AndroidKeyStoreHmacSpi$HmacSHA1");
        put("Alg.Alias.Mac.1.2.840.113549.2.7", KeyProperties.KEY_ALGORITHM_HMAC_SHA1);
        put("Alg.Alias.Mac.HMAC-SHA1", KeyProperties.KEY_ALGORITHM_HMAC_SHA1);
        put("Alg.Alias.Mac.HMAC/SHA1", KeyProperties.KEY_ALGORITHM_HMAC_SHA1);
        putMacImpl(KeyProperties.KEY_ALGORITHM_HMAC_SHA224, "android.security.keystore.AndroidKeyStoreHmacSpi$HmacSHA224");
        put("Alg.Alias.Mac.1.2.840.113549.2.9", KeyProperties.KEY_ALGORITHM_HMAC_SHA224);
        put("Alg.Alias.Mac.HMAC-SHA224", KeyProperties.KEY_ALGORITHM_HMAC_SHA224);
        put("Alg.Alias.Mac.HMAC/SHA224", KeyProperties.KEY_ALGORITHM_HMAC_SHA224);
        putMacImpl(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, "android.security.keystore.AndroidKeyStoreHmacSpi$HmacSHA256");
        put("Alg.Alias.Mac.1.2.840.113549.2.9", KeyProperties.KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.Mac.HMAC-SHA256", KeyProperties.KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.Mac.HMAC/SHA256", KeyProperties.KEY_ALGORITHM_HMAC_SHA256);
        putMacImpl(KeyProperties.KEY_ALGORITHM_HMAC_SHA384, "android.security.keystore.AndroidKeyStoreHmacSpi$HmacSHA384");
        put("Alg.Alias.Mac.1.2.840.113549.2.10", KeyProperties.KEY_ALGORITHM_HMAC_SHA384);
        put("Alg.Alias.Mac.HMAC-SHA384", KeyProperties.KEY_ALGORITHM_HMAC_SHA384);
        put("Alg.Alias.Mac.HMAC/SHA384", KeyProperties.KEY_ALGORITHM_HMAC_SHA384);
        putMacImpl(KeyProperties.KEY_ALGORITHM_HMAC_SHA512, "android.security.keystore.AndroidKeyStoreHmacSpi$HmacSHA512");
        put("Alg.Alias.Mac.1.2.840.113549.2.11", KeyProperties.KEY_ALGORITHM_HMAC_SHA512);
        put("Alg.Alias.Mac.HMAC-SHA512", KeyProperties.KEY_ALGORITHM_HMAC_SHA512);
        put("Alg.Alias.Mac.HMAC/SHA512", KeyProperties.KEY_ALGORITHM_HMAC_SHA512);
        putSymmetricCipherImpl("AES/ECB/NoPadding", "android.security.keystore.AndroidKeyStoreUnauthenticatedAESCipherSpi$ECB$NoPadding");
        putSymmetricCipherImpl("AES/ECB/PKCS7Padding", "android.security.keystore.AndroidKeyStoreUnauthenticatedAESCipherSpi$ECB$PKCS7Padding");
        putSymmetricCipherImpl("AES/CBC/NoPadding", "android.security.keystore.AndroidKeyStoreUnauthenticatedAESCipherSpi$CBC$NoPadding");
        putSymmetricCipherImpl("AES/CBC/PKCS7Padding", "android.security.keystore.AndroidKeyStoreUnauthenticatedAESCipherSpi$CBC$PKCS7Padding");
        putSymmetricCipherImpl("AES/CTR/NoPadding", "android.security.keystore.AndroidKeyStoreUnauthenticatedAESCipherSpi$CTR$NoPadding");
        putSymmetricCipherImpl("AES/GCM/NoPadding", "android.security.keystore.AndroidKeyStoreAuthenticatedAESCipherSpi$GCM$NoPadding");
        putAsymmetricCipherImpl("RSA/ECB/NoPadding", "android.security.keystore.AndroidKeyStoreRSACipherSpi$NoPadding");
        put("Alg.Alias.Cipher.RSA/None/NoPadding", "RSA/ECB/NoPadding");
        putAsymmetricCipherImpl("RSA/ECB/PKCS1Padding", "android.security.keystore.AndroidKeyStoreRSACipherSpi$PKCS1Padding");
        put("Alg.Alias.Cipher.RSA/None/PKCS1Padding", "RSA/ECB/PKCS1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPPadding", "android.security.keystore.AndroidKeyStoreRSACipherSpi$OAEPWithSHA1AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPPadding", "RSA/ECB/OAEPPadding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", "android.security.keystore.AndroidKeyStoreRSACipherSpi$OAEPWithSHA1AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-1AndMGF1Padding", "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-224AndMGF1Padding", "android.security.keystore.AndroidKeyStoreRSACipherSpi$OAEPWithSHA224AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-224AndMGF1Padding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "android.security.keystore.AndroidKeyStoreRSACipherSpi$OAEPWithSHA256AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-256AndMGF1Padding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-384AndMGF1Padding", "android.security.keystore.AndroidKeyStoreRSACipherSpi$OAEPWithSHA384AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-384AndMGF1Padding", "RSA/ECB/OAEPWithSHA-384AndMGF1Padding");
        putAsymmetricCipherImpl("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", "android.security.keystore.AndroidKeyStoreRSACipherSpi$OAEPWithSHA512AndMGF1Padding");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-512AndMGF1Padding", "RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
        putSignatureImpl("NONEwithRSA", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$NONEWithPKCS1Padding");
        putSignatureImpl("MD5withRSA", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$MD5WithPKCS1Padding");
        put("Alg.Alias.Signature.MD5WithRSAEncryption", "MD5withRSA");
        put("Alg.Alias.Signature.MD5/RSA", "MD5withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.4", "MD5withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.2.5with1.2.840.113549.1.1.1", "MD5withRSA");
        putSignatureImpl("SHA1withRSA", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$SHA1WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA1WithRSAEncryption", "SHA1withRSA");
        put("Alg.Alias.Signature.SHA1/RSA", "SHA1withRSA");
        put("Alg.Alias.Signature.SHA-1/RSA", "SHA1withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.5", "SHA1withRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.113549.1.1.1", "SHA1withRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.113549.1.1.5", "SHA1withRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.29", "SHA1withRSA");
        putSignatureImpl("SHA224withRSA", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$SHA224WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA224WithRSAEncryption", "SHA224withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.11", "SHA224withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.113549.1.1.1", "SHA224withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.113549.1.1.11", "SHA224withRSA");
        putSignatureImpl("SHA256withRSA", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$SHA256WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA256WithRSAEncryption", "SHA256withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.11", "SHA256withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.1", "SHA256withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.11", "SHA256withRSA");
        putSignatureImpl("SHA384withRSA", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$SHA384WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA384WithRSAEncryption", "SHA384withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.12", "SHA384withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.2with1.2.840.113549.1.1.1", "SHA384withRSA");
        putSignatureImpl("SHA512withRSA", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$SHA512WithPKCS1Padding");
        put("Alg.Alias.Signature.SHA512WithRSAEncryption", "SHA512withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.13", "SHA512withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.3with1.2.840.113549.1.1.1", "SHA512withRSA");
        putSignatureImpl("SHA1withRSA/PSS", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$SHA1WithPSSPadding");
        putSignatureImpl("SHA224withRSA/PSS", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$SHA224WithPSSPadding");
        putSignatureImpl("SHA256withRSA/PSS", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$SHA256WithPSSPadding");
        putSignatureImpl("SHA384withRSA/PSS", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$SHA384WithPSSPadding");
        putSignatureImpl("SHA512withRSA/PSS", "android.security.keystore.AndroidKeyStoreRSASignatureSpi$SHA512WithPSSPadding");
        putSignatureImpl("NONEwithECDSA", "android.security.keystore.AndroidKeyStoreECDSASignatureSpi$NONE");
        putSignatureImpl("SHA1withECDSA", "android.security.keystore.AndroidKeyStoreECDSASignatureSpi$SHA1");
        put("Alg.Alias.Signature.ECDSA", "SHA1withECDSA");
        put("Alg.Alias.Signature.ECDSAwithSHA1", "SHA1withECDSA");
        put("Alg.Alias.Signature.1.2.840.10045.4.1", "SHA1withECDSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.10045.2.1", "SHA1withECDSA");
        putSignatureImpl("SHA224withECDSA", "android.security.keystore.AndroidKeyStoreECDSASignatureSpi$SHA224");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.1", "SHA224withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.10045.2.1", "SHA224withECDSA");
        putSignatureImpl("SHA256withECDSA", "android.security.keystore.AndroidKeyStoreECDSASignatureSpi$SHA256");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.2", "SHA256withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.10045.2.1", "SHA256withECDSA");
        putSignatureImpl("SHA384withECDSA", "android.security.keystore.AndroidKeyStoreECDSASignatureSpi$SHA384");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.3", "SHA384withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.2with1.2.840.10045.2.1", "SHA384withECDSA");
        putSignatureImpl("SHA512withECDSA", "android.security.keystore.AndroidKeyStoreECDSASignatureSpi$SHA512");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.4", "SHA512withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.3with1.2.840.10045.2.1", "SHA512withECDSA");
    }

    private void putMacImpl(String algorithm, String implClass) {
        put("Mac." + algorithm, implClass);
        put("Mac." + algorithm + " SupportedKeyClasses", KEYSTORE_SECRET_KEY_CLASS_NAME);
    }

    private void putSymmetricCipherImpl(String transformation, String implClass) {
        put("Cipher." + transformation, implClass);
        put("Cipher." + transformation + " SupportedKeyClasses", KEYSTORE_SECRET_KEY_CLASS_NAME);
    }

    private void putAsymmetricCipherImpl(String transformation, String implClass) {
        put("Cipher." + transformation, implClass);
        put("Cipher." + transformation + " SupportedKeyClasses", "android.security.keystore.AndroidKeyStorePrivateKey|android.security.keystore.AndroidKeyStorePublicKey");
    }

    private void putSignatureImpl(String algorithm, String implClass) {
        put("Signature." + algorithm, implClass);
        put("Signature." + algorithm + " SupportedKeyClasses", "android.security.keystore.AndroidKeyStorePrivateKey|android.security.keystore.AndroidKeyStorePublicKey");
    }

    public static String[] getSupportedEcdsaSignatureDigests() {
        return new String[]{KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512};
    }

    public static String[] getSupportedRsaSignatureWithPkcs1PaddingDigests() {
        return new String[]{KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_MD5, KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512};
    }
}
