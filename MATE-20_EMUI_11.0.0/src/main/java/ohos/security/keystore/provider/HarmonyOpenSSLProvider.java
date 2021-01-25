package ohos.security.keystore.provider;

import java.security.Provider;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.common.PropertyKey;
import ohos.security.keystore.KeyStoreConstants;

public final class HarmonyOpenSSLProvider extends Provider {
    private static final HiLogLabel LABEL = KeyStoreLogger.getLabel(TAG);
    private static final String PREFIX = "com.android.org.conscrypt.";
    private static final String TAG = "HarmonyOpenSSLProvider";
    private static final long serialVersionUID = -8858225336184595697L;

    public HarmonyOpenSSLProvider() {
        this("HarmonyOpenSSL");
    }

    public HarmonyOpenSSLProvider(String str) {
        this(str, false);
    }

    private HarmonyOpenSSLProvider(String str, boolean z) {
        super(str, 1.0d, "Harmony's OpenSSL-backed security provider");
        addSslContextImpl();
        if (z) {
            put("TrustManagerFactory.PKIX", "com.android.org.conscrypt.TrustManagerFactoryImpl");
            put("Alg.Alias.TrustManagerFactory.X509", "PKIX");
        }
        addAlgorithmParametersImpl();
        addMessageDigestsImpl();
        addKeyGeneratorsImpl();
        addKeyPairGeneratorsImpl();
        addKeyFactoryImpl();
        addKeyAgreementImpl();
        addSignaturesImpl();
        addSecureRandomImpl();
        addCipherImpl();
        addMacImpl();
        addCertificateImpl();
    }

    private void addCertificateImpl() {
        put("CertificateFactory.X509", "com.android.org.conscrypt.OpenSSLX509CertificateFactory");
        put("Alg.Alias.CertificateFactory.X.509", "X509");
    }

    private void addMacImpl() {
        putMacImplClass("HmacMD5", "OpenSSLMac$HmacMD5");
        put("Alg.Alias.Mac.1.3.6.1.5.5.8.1.1", "HmacMD5");
        put("Alg.Alias.Mac.HMAC-MD5", "HmacMD5");
        put("Alg.Alias.Mac.HMAC/MD5", "HmacMD5");
        putMacImplClass(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1, "OpenSSLMac$HmacSHA1");
        put("Alg.Alias.Mac.1.2.840.113549.2.7", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        put("Alg.Alias.Mac.1.3.6.1.5.5.8.1.2", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        put("Alg.Alias.Mac.HMAC-SHA1", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        put("Alg.Alias.Mac.HMAC/SHA1", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        putMacImplClass(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224, "OpenSSLMac$HmacSHA224");
        put("Alg.Alias.Mac.1.2.840.113549.2.8", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        put("Alg.Alias.Mac.HMAC-SHA224", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        put("Alg.Alias.Mac.HMAC/SHA224", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        put("Alg.Alias.Mac.PBEWITHHMACSHA224", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        putMacImplClass(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256, "OpenSSLMac$HmacSHA256");
        put("Alg.Alias.Mac.1.2.840.113549.2.9", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.Mac.2.16.840.1.101.3.4.2.1", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.Mac.HMAC-SHA256", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.Mac.HMAC/SHA256", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.Mac.PBEWITHHMACSHA256", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        putMacImplClass(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384, "OpenSSLMac$HmacSHA384");
        put("Alg.Alias.Mac.1.2.840.113549.2.10", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        put("Alg.Alias.Mac.HMAC-SHA384", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        put("Alg.Alias.Mac.HMAC/SHA384", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        put("Alg.Alias.Mac.PBEWITHHMACSHA384", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        putMacImplClass(KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512, "OpenSSLMac$HmacSHA512");
        put("Alg.Alias.Mac.1.2.840.113549.2.11", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
        put("Alg.Alias.Mac.HMAC-SHA512", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
        put("Alg.Alias.Mac.HMAC/SHA512", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
        put("Alg.Alias.Mac.PBEWITHHMACSHA512", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
    }

    private void addCipherImpl() {
        putRsaCipherImplClass("RSA/ECB/NoPadding", "OpenSSLCipherRSA$Raw");
        put("Alg.Alias.Cipher.RSA/None/NoPadding", "RSA/ECB/NoPadding");
        putRsaCipherImplClass("RSA/ECB/PKCS1Padding", "OpenSSLCipherRSA$PKCS1");
        put("Alg.Alias.Cipher.RSA/None/PKCS1Padding", "RSA/ECB/PKCS1Padding");
        putRsaCipherImplClass("RSA/ECB/OAEPPadding", "OpenSSLCipherRSA$OAEP$SHA1");
        put("Alg.Alias.Cipher.RSA/None/OAEPPadding", "RSA/ECB/OAEPPadding");
        putRsaCipherImplClass("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", "OpenSSLCipherRSA$OAEP$SHA1");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-1AndMGF1Padding", "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        putRsaCipherImplClass("RSA/ECB/OAEPWithSHA-224AndMGF1Padding", "OpenSSLCipherRSA$OAEP$SHA224");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-224AndMGF1Padding", "RSA/ECB/OAEPWithSHA-224AndMGF1Padding");
        putRsaCipherImplClass("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "OpenSSLCipherRSA$OAEP$SHA256");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-256AndMGF1Padding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        putRsaCipherImplClass("RSA/ECB/OAEPWithSHA-384AndMGF1Padding", "OpenSSLCipherRSA$OAEP$SHA384");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-384AndMGF1Padding", "RSA/ECB/OAEPWithSHA-384AndMGF1Padding");
        putRsaCipherImplClass("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", "OpenSSLCipherRSA$OAEP$SHA512");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-512AndMGF1Padding", "RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
        addSymmetricCipherImpl();
    }

    private void addSymmetricCipherImpl() {
        addAesCipherImpl();
        putSymmetricCipherImplClass("DESEDE/CBC/NoPadding", "OpenSSLCipher$EVP_CIPHER$DESEDE$CBC$NoPadding");
        putSymmetricCipherImplClass("DESEDE/CBC/PKCS5Padding", "OpenSSLCipher$EVP_CIPHER$DESEDE$CBC$PKCS5Padding");
        put("Alg.Alias.Cipher.DESEDE/CBC/PKCS7Padding", "DESEDE/CBC/PKCS5Padding");
        putSymmetricCipherImplClass("ARC4", "OpenSSLCipher$EVP_CIPHER$ARC4");
        put("Alg.Alias.Cipher.ARCFOUR", "ARC4");
        put("Alg.Alias.Cipher.RC4", "ARC4");
        put("Alg.Alias.Cipher.1.2.840.113549.3.4", "ARC4");
        put("Alg.Alias.Cipher.OID.1.2.840.113549.3.4", "ARC4");
        putSymmetricCipherImplClass("ChaCha20", "OpenSSLCipherChaCha20");
        putSymmetricCipherImplClass("ChaCha20/Poly1305/NoPadding", "OpenSSLCipher$EVP_AEAD$ChaCha20");
        put("Alg.Alias.Cipher.ChaCha20-Poly1305", "ChaCha20/Poly1305/NoPadding");
    }

    private void addAesCipherImpl() {
        putSymmetricCipherImplClass("AES/ECB/NoPadding", "OpenSSLCipher$EVP_CIPHER$AES$ECB$NoPadding");
        putSymmetricCipherImplClass("AES/ECB/PKCS5Padding", "OpenSSLCipher$EVP_CIPHER$AES$ECB$PKCS5Padding");
        put("Alg.Alias.Cipher.AES/ECB/PKCS7Padding", "AES/ECB/PKCS5Padding");
        putSymmetricCipherImplClass("AES/CBC/NoPadding", "OpenSSLCipher$EVP_CIPHER$AES$CBC$NoPadding");
        putSymmetricCipherImplClass("AES/CBC/PKCS5Padding", "OpenSSLCipher$EVP_CIPHER$AES$CBC$PKCS5Padding");
        put("Alg.Alias.Cipher.AES/CBC/PKCS7Padding", "AES/CBC/PKCS5Padding");
        putSymmetricCipherImplClass("AES/CTR/NoPadding", "OpenSSLCipher$EVP_CIPHER$AES$CTR");
        putSymmetricCipherImplClass("AES_128/ECB/NoPadding", "OpenSSLCipher$EVP_CIPHER$AES_128$ECB$NoPadding");
        putSymmetricCipherImplClass("AES_128/ECB/PKCS5Padding", "OpenSSLCipher$EVP_CIPHER$AES_128$ECB$PKCS5Padding");
        put("Alg.Alias.Cipher.AES_128/ECB/PKCS7Padding", "AES_128/ECB/PKCS5Padding");
        putSymmetricCipherImplClass("AES_128/CBC/NoPadding", "OpenSSLCipher$EVP_CIPHER$AES_128$CBC$NoPadding");
        putSymmetricCipherImplClass("AES_128/CBC/PKCS5Padding", "OpenSSLCipher$EVP_CIPHER$AES_128$CBC$PKCS5Padding");
        put("Alg.Alias.Cipher.AES_128/CBC/PKCS7Padding", "AES_128/CBC/PKCS5Padding");
        put("Alg.Alias.Cipher.PBEWithHmacSHA1AndAES_128", "AES_128/CBC/NOPADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA224AndAES_128", "AES_128/CBC/NOPADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA256AndAES_128", "AES_128/CBC/NOPADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA384AndAES_128", "AES_128/CBC/NOPADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA512AndAES_128", "AES_128/CBC/NOPADDING");
        putSymmetricCipherImplClass("AES_256/ECB/NoPadding", "OpenSSLCipher$EVP_CIPHER$AES_256$ECB$NoPadding");
        putSymmetricCipherImplClass("AES_256/ECB/PKCS5Padding", "OpenSSLCipher$EVP_CIPHER$AES_256$ECB$PKCS5Padding");
        put("Alg.Alias.Cipher.AES_256/ECB/PKCS7Padding", "AES_256/ECB/PKCS5Padding");
        putSymmetricCipherImplClass("AES_256/CBC/NoPadding", "OpenSSLCipher$EVP_CIPHER$AES_256$CBC$NoPadding");
        putSymmetricCipherImplClass("AES_256/CBC/PKCS5Padding", "OpenSSLCipher$EVP_CIPHER$AES_256$CBC$PKCS5Padding");
        put("Alg.Alias.Cipher.AES_256/CBC/PKCS7Padding", "AES_256/CBC/PKCS5Padding");
        put("Alg.Alias.Cipher.PBEWithHmacSHA1AndAES_256", "AES_256/CBC/NOPADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA224AndAES_256", "AES_256/CBC/NOPADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA256AndAES_256", "AES_256/CBC/NOPADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA384AndAES_256", "AES_256/CBC/NOPADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA512AndAES_256", "AES_256/CBC/NOPADDING");
        putSymmetricCipherImplClass("AES/GCM/NoPadding", "OpenSSLCipher$EVP_AEAD$AES$GCM");
        put("Alg.Alias.Cipher.GCM", "AES/GCM/NoPadding");
        put("Alg.Alias.Cipher.2.16.840.1.101.3.4.1.6", "AES/GCM/NoPadding");
        put("Alg.Alias.Cipher.2.16.840.1.101.3.4.1.26", "AES/GCM/NoPadding");
        put("Alg.Alias.Cipher.2.16.840.1.101.3.4.1.46", "AES/GCM/NoPadding");
        putSymmetricCipherImplClass("AES_128/GCM/NoPadding", "OpenSSLCipher$EVP_AEAD$AES$GCM$AES_128");
        putSymmetricCipherImplClass("AES_256/GCM/NoPadding", "OpenSSLCipher$EVP_AEAD$AES$GCM$AES_256");
    }

    private void addSecureRandomImpl() {
        put("SecureRandom.SHA1PRNG", "com.android.org.conscrypt.OpenSSLRandom");
        put("SecureRandom.SHA1PRNG ImplementedIn", PropertyKey.Exif.SOFTWARE);
    }

    private void addSignaturesImpl() {
        addSignaturesMd5Impl();
        addShaWithRsaImpl();
        addSignaturesWithEcdsaImpl();
        putSignatureImplClass("SHA1withRSA/PSS", "OpenSSLSignature$SHA1RSAPSS");
        put("Alg.Alias.Signature.SHA1withRSAandMGF1", "SHA1withRSA/PSS");
        putSignatureImplClass("SHA224withRSA/PSS", "OpenSSLSignature$SHA224RSAPSS");
        put("Alg.Alias.Signature.SHA224withRSAandMGF1", "SHA224withRSA/PSS");
        putSignatureImplClass("SHA256withRSA/PSS", "OpenSSLSignature$SHA256RSAPSS");
        put("Alg.Alias.Signature.SHA256withRSAandMGF1", "SHA256withRSA/PSS");
        putSignatureImplClass("SHA384withRSA/PSS", "OpenSSLSignature$SHA384RSAPSS");
        put("Alg.Alias.Signature.SHA384withRSAandMGF1", "SHA384withRSA/PSS");
        putSignatureImplClass("SHA512withRSA/PSS", "OpenSSLSignature$SHA512RSAPSS");
        put("Alg.Alias.Signature.SHA512withRSAandMGF1", "SHA512withRSA/PSS");
    }

    private void addShaWithRsaImpl() {
        putSignatureImplClass("SHA1withRSA", "OpenSSLSignature$SHA1RSA");
        put("Alg.Alias.Signature.SHA1withRSAEncryption", "SHA1withRSA");
        put("Alg.Alias.Signature.SHA1/RSA", "SHA1withRSA");
        put("Alg.Alias.Signature.SHA-1/RSA", "SHA1withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.5", "SHA1withRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.5", "SHA1withRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.113549.1.1.1", "SHA1withRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.113549.1.1.5", "SHA1withRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.29", "SHA1withRSA");
        put("Alg.Alias.Signature.OID.1.3.14.3.2.29", "SHA1withRSA");
        putSignatureImplClass("SHA224withRSA", "OpenSSLSignature$SHA224RSA");
        put("Alg.Alias.Signature.SHA224withRSAEncryption", "SHA224withRSA");
        put("Alg.Alias.Signature.SHA224/RSA", "SHA224withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.14", "SHA224withRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.14", "SHA224withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.113549.1.1.1", "SHA224withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.113549.1.1.14", "SHA224withRSA");
        putSignatureImplClass("SHA256withRSA", "OpenSSLSignature$SHA256RSA");
        put("Alg.Alias.Signature.SHA256withRSAEncryption", "SHA256withRSA");
        put("Alg.Alias.Signature.SHA256/RSA", "SHA256withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.11", "SHA256withRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.11", "SHA256withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.1", "SHA256withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.11", "SHA256withRSA");
        putSignatureImplClass("SHA384withRSA", "OpenSSLSignature$SHA384RSA");
        put("Alg.Alias.Signature.SHA384withRSAEncryption", "SHA384withRSA");
        put("Alg.Alias.Signature.SHA384/RSA", "SHA384withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.12", "SHA384withRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.12", "SHA384withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.2with1.2.840.113549.1.1.1", "SHA384withRSA");
        putSignatureImplClass("SHA512withRSA", "OpenSSLSignature$SHA512RSA");
        put("Alg.Alias.Signature.SHA512withRSAEncryption", "SHA512withRSA");
        put("Alg.Alias.Signature.SHA512/RSA", "SHA512withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.13", "SHA512withRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.13", "SHA512withRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.3with1.2.840.113549.1.1.1", "SHA512withRSA");
        putRawRsaSignatureImplClass("OpenSSLSignatureRawRSA");
    }

    private void addSignaturesMd5Impl() {
        putSignatureImplClass("MD5withRSA", "OpenSSLSignature$MD5RSA");
        put("Alg.Alias.Signature.MD5withRSAEncryption", "MD5withRSA");
        put("Alg.Alias.Signature.MD5/RSA", "MD5withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.4", "MD5withRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.4", "MD5withRSA");
        put("Alg.Alias.Signature.1.2.840.113549.2.5with1.2.840.113549.1.1.1", "MD5withRSA");
    }

    private void addSignaturesWithEcdsaImpl() {
        putSignatureImplClass("NONEwithECDSA", "OpenSSLSignatureRawECDSA");
        putSignatureImplClass("SHA1withECDSA", "OpenSSLSignature$SHA1ECDSA");
        put("Alg.Alias.Signature.ECDSA", "SHA1withECDSA");
        put("Alg.Alias.Signature.ECDSAwithSHA1", "SHA1withECDSA");
        put("Alg.Alias.Signature.1.2.840.10045.4.1", "SHA1withECDSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.10045.2.1", "SHA1withECDSA");
        putSignatureImplClass("SHA224withECDSA", "OpenSSLSignature$SHA224ECDSA");
        put("Alg.Alias.Signature.SHA224/ECDSA", "SHA224withECDSA");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.1", "SHA224withECDSA");
        put("Alg.Alias.Signature.OID.1.2.840.10045.4.3.1", "SHA224withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.10045.2.1", "SHA224withECDSA");
        putSignatureImplClass("SHA256withECDSA", "OpenSSLSignature$SHA256ECDSA");
        put("Alg.Alias.Signature.SHA256/ECDSA", "SHA256withECDSA");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.2", "SHA256withECDSA");
        put("Alg.Alias.Signature.OID.1.2.840.10045.4.3.2", "SHA256withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.10045.2.1", "SHA256withECDSA");
        putSignatureImplClass("SHA384withECDSA", "OpenSSLSignature$SHA384ECDSA");
        put("Alg.Alias.Signature.SHA384/ECDSA", "SHA384withECDSA");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.3", "SHA384withECDSA");
        put("Alg.Alias.Signature.OID.1.2.840.10045.4.3.3", "SHA384withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.2with1.2.840.10045.2.1", "SHA384withECDSA");
        putSignatureImplClass("SHA512withECDSA", "OpenSSLSignature$SHA512ECDSA");
        put("Alg.Alias.Signature.SHA512/ECDSA", "SHA512withECDSA");
        put("Alg.Alias.Signature.1.2.840.10045.4.3.4", "SHA512withECDSA");
        put("Alg.Alias.Signature.OID.1.2.840.10045.4.3.4", "SHA512withECDSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.3with1.2.840.10045.2.1", "SHA512withECDSA");
    }

    private void addKeyAgreementImpl() {
        putEcdhKeyAgreementImplClass("OpenSSLECDHKeyAgreement");
    }

    private void addKeyFactoryImpl() {
        put("KeyFactory.RSA", "com.android.org.conscrypt.OpenSSLRSAKeyFactory");
        put("Alg.Alias.KeyFactory.1.2.840.113549.1.1.1", KeyStoreConstants.SEC_KEY_ALGORITHM_RSA);
        put("Alg.Alias.KeyFactory.1.2.840.113549.1.1.7", KeyStoreConstants.SEC_KEY_ALGORITHM_RSA);
        put("Alg.Alias.KeyFactory.2.5.8.1.1", KeyStoreConstants.SEC_KEY_ALGORITHM_RSA);
        put("KeyFactory.EC", "com.android.org.conscrypt.OpenSSLECKeyFactory");
        put("Alg.Alias.KeyFactory.1.2.840.10045.2.1", KeyStoreConstants.SEC_KEY_ALGORITHM_EC);
        put("Alg.Alias.KeyFactory.1.3.133.16.840.63.0.2", KeyStoreConstants.SEC_KEY_ALGORITHM_EC);
        put("SecretKeyFactory.DESEDE", "com.android.org.conscrypt.DESEDESecretKeyFactory");
        put("Alg.Alias.SecretKeyFactory.TDEA", "DESEDE");
    }

    private void addKeyPairGeneratorsImpl() {
        put("KeyPairGenerator.RSA", "com.android.org.conscrypt.OpenSSLRSAKeyPairGenerator");
        put("Alg.Alias.KeyPairGenerator.1.2.840.113549.1.1.1", KeyStoreConstants.SEC_KEY_ALGORITHM_RSA);
        put("Alg.Alias.KeyPairGenerator.1.2.840.113549.1.1.7", KeyStoreConstants.SEC_KEY_ALGORITHM_RSA);
        put("Alg.Alias.KeyPairGenerator.2.5.8.1.1", KeyStoreConstants.SEC_KEY_ALGORITHM_RSA);
        put("KeyPairGenerator.EC", "com.android.org.conscrypt.OpenSSLECKeyPairGenerator");
        put("Alg.Alias.KeyPairGenerator.1.2.840.10045.2.1", KeyStoreConstants.SEC_KEY_ALGORITHM_EC);
        put("Alg.Alias.KeyPairGenerator.1.3.133.16.840.63.0.2", KeyStoreConstants.SEC_KEY_ALGORITHM_EC);
    }

    private void addKeyGeneratorsImpl() {
        put("KeyGenerator.ARC4", "com.android.org.conscrypt.KeyGeneratorImpl$ARC4");
        put("Alg.Alias.KeyGenerator.RC4", "ARC4");
        put("Alg.Alias.KeyGenerator.1.2.840.113549.3.4", "ARC4");
        put("KeyGenerator.AES", "com.android.org.conscrypt.KeyGeneratorImpl$AES");
        put("KeyGenerator.ChaCha20", "com.android.org.conscrypt.KeyGeneratorImpl$ChaCha20");
        put("KeyGenerator.DESEDE", "com.android.org.conscrypt.KeyGeneratorImpl$DESEDE");
        put("Alg.Alias.KeyGenerator.TDEA", "DESEDE");
        put("KeyGenerator.HmacMD5", "com.android.org.conscrypt.KeyGeneratorImpl$HmacMD5");
        put("Alg.Alias.KeyGenerator.1.3.6.1.5.5.8.1.1", "HmacMD5");
        put("Alg.Alias.KeyGenerator.HMAC-MD5", "HmacMD5");
        put("Alg.Alias.KeyGenerator.HMAC/MD5", "HmacMD5");
        put("KeyGenerator.HmacSHA1", "com.android.org.conscrypt.KeyGeneratorImpl$HmacSHA1");
        put("Alg.Alias.KeyGenerator.1.2.840.113549.2.7", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        put("Alg.Alias.KeyGenerator.1.3.6.1.5.5.8.1.2", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        put("Alg.Alias.KeyGenerator.HMAC-SHA1", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        put("Alg.Alias.KeyGenerator.HMAC/SHA1", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA1);
        put("KeyGenerator.HmacSHA224", "com.android.org.conscrypt.KeyGeneratorImpl$HmacSHA224");
        put("Alg.Alias.KeyGenerator.1.2.840.113549.2.8", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        put("Alg.Alias.KeyGenerator.HMAC-SHA224", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        put("Alg.Alias.KeyGenerator.HMAC/SHA224", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA224);
        put("KeyGenerator.HmacSHA256", "com.android.org.conscrypt.KeyGeneratorImpl$HmacSHA256");
        put("Alg.Alias.KeyGenerator.1.2.840.113549.2.9", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.KeyGenerator.2.16.840.1.101.3.4.2.1", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.KeyGenerator.HMAC-SHA256", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        put("Alg.Alias.KeyGenerator.HMAC/SHA256", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA256);
        put("KeyGenerator.HmacSHA384", "com.android.org.conscrypt.KeyGeneratorImpl$HmacSHA384");
        put("Alg.Alias.KeyGenerator.1.2.840.113549.2.10", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        put("Alg.Alias.KeyGenerator.HMAC-SHA384", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        put("Alg.Alias.KeyGenerator.HMAC/SHA384", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA384);
        put("KeyGenerator.HmacSHA512", "com.android.org.conscrypt.KeyGeneratorImpl$HmacSHA512");
        put("Alg.Alias.KeyGenerator.1.2.840.113549.2.11", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
        put("Alg.Alias.KeyGenerator.HMAC-SHA512", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
        put("Alg.Alias.KeyGenerator.HMAC/SHA512", KeyStoreConstants.SEC_KEY_ALGORITHM_HMAC_SHA512);
    }

    private void addMessageDigestsImpl() {
        put("MessageDigest.SHA-1", "com.android.org.conscrypt.OpenSSLMessageDigestJDK$SHA1");
        put("Alg.Alias.MessageDigest.SHA1", KeyStoreConstants.DIGEST_ALGORITHM_SHA1);
        put("Alg.Alias.MessageDigest.SHA", KeyStoreConstants.DIGEST_ALGORITHM_SHA1);
        put("Alg.Alias.MessageDigest.1.3.14.3.2.26", KeyStoreConstants.DIGEST_ALGORITHM_SHA1);
        put("MessageDigest.SHA-224", "com.android.org.conscrypt.OpenSSLMessageDigestJDK$SHA224");
        put("Alg.Alias.MessageDigest.SHA224", KeyStoreConstants.DIGEST_ALGORITHM_SHA224);
        put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.4", KeyStoreConstants.DIGEST_ALGORITHM_SHA224);
        put("MessageDigest.SHA-256", "com.android.org.conscrypt.OpenSSLMessageDigestJDK$SHA256");
        put("Alg.Alias.MessageDigest.SHA256", KeyStoreConstants.DIGEST_ALGORITHM_SHA256);
        put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.1", KeyStoreConstants.DIGEST_ALGORITHM_SHA256);
        put("MessageDigest.SHA-384", "com.android.org.conscrypt.OpenSSLMessageDigestJDK$SHA384");
        put("Alg.Alias.MessageDigest.SHA384", KeyStoreConstants.DIGEST_ALGORITHM_SHA384);
        put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.2", KeyStoreConstants.DIGEST_ALGORITHM_SHA384);
        put("MessageDigest.SHA-512", "com.android.org.conscrypt.OpenSSLMessageDigestJDK$SHA512");
        put("Alg.Alias.MessageDigest.SHA512", KeyStoreConstants.DIGEST_ALGORITHM_SHA512);
        put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.3", KeyStoreConstants.DIGEST_ALGORITHM_SHA512);
        put("MessageDigest.MD5", "com.android.org.conscrypt.OpenSSLMessageDigestJDK$MD5");
        put("Alg.Alias.MessageDigest.1.2.840.113549.2.5", KeyStoreConstants.DIGEST_ALGORITHM_MD5);
    }

    private void addAlgorithmParametersImpl() {
        put("AlgorithmParameters.AES", "com.android.org.conscrypt.IvParameters$AES");
        put("Alg.Alias.AlgorithmParameters.2.16.840.1.101.3.4.1.2", KeyStoreConstants.SEC_KEY_ALGORITHM_AES);
        put("Alg.Alias.AlgorithmParameters.2.16.840.1.101.3.4.1.22", KeyStoreConstants.SEC_KEY_ALGORITHM_AES);
        put("Alg.Alias.AlgorithmParameters.2.16.840.1.101.3.4.1.42", KeyStoreConstants.SEC_KEY_ALGORITHM_AES);
        put("AlgorithmParameters.ChaCha20", "com.android.org.conscrypt.IvParameters$ChaCha20");
        put("AlgorithmParameters.DESEDE", "com.android.org.conscrypt.IvParameters$DESEDE");
        put("Alg.Alias.AlgorithmParameters.TDEA", "DESEDE");
        put("Alg.Alias.AlgorithmParameters.1.2.840.113549.3.7", "DESEDE");
        put("AlgorithmParameters.GCM", "com.android.org.conscrypt.GCMParameters");
        put("Alg.Alias.AlgorithmParameters.2.16.840.1.101.3.4.1.6", KeyStoreConstants.SEC_BLOCK_MODE_GCM);
        put("Alg.Alias.AlgorithmParameters.2.16.840.1.101.3.4.1.26", KeyStoreConstants.SEC_BLOCK_MODE_GCM);
        put("Alg.Alias.AlgorithmParameters.2.16.840.1.101.3.4.1.46", KeyStoreConstants.SEC_BLOCK_MODE_GCM);
        put("AlgorithmParameters.OAEP", "com.android.org.conscrypt.OAEPParameters");
        put("AlgorithmParameters.PSS", "com.android.org.conscrypt.PSSParameters");
        put("AlgorithmParameters.EC", "com.android.org.conscrypt.ECParameters");
    }

    private void addSslContextImpl() {
        put("SSLContext.SSL", "com.android.org.conscrypt.OpenSSLContextImpl$TLSv13");
        put("SSLContext.TLS", "com.android.org.conscrypt.OpenSSLContextImpl$TLSv13");
        put("SSLContext.TLSv1", "com.android.org.conscrypt.OpenSSLContextImpl$TLSv1");
        put("SSLContext.TLSv1.1", "com.android.org.conscrypt.OpenSSLContextImpl$TLSv11");
        put("SSLContext.TLSv1.2", "com.android.org.conscrypt.OpenSSLContextImpl$TLSv12");
        put("SSLContext.TLSv1.3", "com.android.org.conscrypt.OpenSSLContextImpl$TLSv13");
        put("SSLContext.Default", "com.android.org.conscrypt.DefaultSSLContextImpl");
    }

    private void putMacImplClass(String str, String str2) {
        putImplClassWithKeyConstraints("Mac." + str, PREFIX + str2, "com.android.org.conscrypt.OpenSSLKeyHolder", "RAW");
    }

    private void putSymmetricCipherImplClass(String str, String str2) {
        putImplClassWithKeyConstraints("Cipher." + str, PREFIX + str2, null, "RAW");
    }

    private void putRsaCipherImplClass(String str, String str2) {
        putImplClassWithKeyConstraints("Cipher." + str, PREFIX + str2, "com.android.org.conscrypt.OpenSSLRSAPrivateKey|java.security.interfaces.ECPrivateKey|com.android.org.conscrypt.OpenSSLRSAPublicKey|java.security.interfaces.RSAPublicKey", null);
    }

    private void putSignatureImplClass(String str, String str2) {
        putImplClassWithKeyConstraints("Signature." + str, PREFIX + str2, "com.android.org.conscrypt.OpenSSLKeyHolder|java.security.interfaces.RSAPrivateKey|java.security.interfaces.ECPrivateKey|java.security.interfaces.RSAPublicKey", "PKCS#8|X.509");
    }

    private void putRawRsaSignatureImplClass(String str) {
        putImplClassWithKeyConstraints("Signature.NONEwithRSA", PREFIX + str, "com.android.org.conscrypt.OpenSSLRSAPrivateKey|java.security.interfaces.RSAPrivateKey|com.android.org.conscrypt.OpenSSLRSAPublicKey|java.security.interfaces.RSAPublicKey", null);
    }

    private void putEcdhKeyAgreementImplClass(String str) {
        putImplClassWithKeyConstraints("KeyAgreement.ECDH", PREFIX + str, "com.android.org.conscrypt.OpenSSLKeyHolder|java.security.interfaces.ECPrivateKey", "PKCS#8");
    }

    private void putImplClassWithKeyConstraints(String str, String str2, String str3, String str4) {
        put(str, str2);
        if (str3 != null) {
            put(str + " SupportedKeyClasses", str3);
        }
        if (str4 != null) {
            put(str + " SupportedKeyFormats", str4);
        }
    }
}
