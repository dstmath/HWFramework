package com.android.org.conscrypt;

import com.android.org.conscrypt.EvpMdRef.MD5;
import com.android.org.conscrypt.EvpMdRef.SHA1;
import com.android.org.conscrypt.EvpMdRef.SHA224;
import com.android.org.conscrypt.EvpMdRef.SHA256;
import com.android.org.conscrypt.EvpMdRef.SHA384;
import com.android.org.conscrypt.EvpMdRef.SHA512;
import java.security.Provider;

public final class OpenSSLProvider extends Provider {
    private static final String PREFIX = (OpenSSLProvider.class.getPackage().getName() + ".");
    public static final String PROVIDER_NAME = "AndroidOpenSSL";
    private static final String STANDARD_EC_PRIVATE_KEY_INTERFACE_CLASS_NAME = "java.security.interfaces.ECPrivateKey";
    private static final String STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME = "java.security.interfaces.RSAPrivateKey";
    private static final String STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME = "java.security.interfaces.RSAPublicKey";
    private static final long serialVersionUID = 2996752495318905136L;

    public OpenSSLProvider() {
        this(PROVIDER_NAME);
    }

    public OpenSSLProvider(String providerName) {
        super(providerName, 1.0d, "Android's OpenSSL-backed security provider");
        Platform.setup();
        String classOpenSSLContextImpl = PREFIX + "OpenSSLContextImpl";
        String tls12SSLContext = classOpenSSLContextImpl + "$TLSv12";
        put("SSLContext.SSL", tls12SSLContext);
        put("SSLContext.TLS", tls12SSLContext);
        put("SSLContext.TLSv1", classOpenSSLContextImpl + "$TLSv1");
        put("SSLContext.TLSv1.1", classOpenSSLContextImpl + "$TLSv11");
        put("SSLContext.TLSv1.2", tls12SSLContext);
        put("SSLContext.Default", PREFIX + "DefaultSSLContextImpl");
        put("MessageDigest.SHA-1", PREFIX + "OpenSSLMessageDigestJDK$SHA1");
        put("Alg.Alias.MessageDigest.SHA1", SHA1.JCA_NAME);
        put("Alg.Alias.MessageDigest.SHA", SHA1.JCA_NAME);
        put("Alg.Alias.MessageDigest.1.3.14.3.2.26", SHA1.JCA_NAME);
        put("MessageDigest.SHA-224", PREFIX + "OpenSSLMessageDigestJDK$SHA224");
        put("Alg.Alias.MessageDigest.SHA224", SHA224.JCA_NAME);
        put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.4", SHA224.JCA_NAME);
        put("MessageDigest.SHA-256", PREFIX + "OpenSSLMessageDigestJDK$SHA256");
        put("Alg.Alias.MessageDigest.SHA256", SHA256.JCA_NAME);
        put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.1", SHA256.JCA_NAME);
        put("MessageDigest.SHA-384", PREFIX + "OpenSSLMessageDigestJDK$SHA384");
        put("Alg.Alias.MessageDigest.SHA384", SHA384.JCA_NAME);
        put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.2", SHA384.JCA_NAME);
        put("MessageDigest.SHA-512", PREFIX + "OpenSSLMessageDigestJDK$SHA512");
        put("Alg.Alias.MessageDigest.SHA512", SHA512.JCA_NAME);
        put("Alg.Alias.MessageDigest.2.16.840.1.101.3.4.2.3", SHA512.JCA_NAME);
        put("MessageDigest.MD5", PREFIX + "OpenSSLMessageDigestJDK$MD5");
        put("Alg.Alias.MessageDigest.1.2.840.113549.2.5", MD5.JCA_NAME);
        put("KeyPairGenerator.RSA", PREFIX + "OpenSSLRSAKeyPairGenerator");
        put("Alg.Alias.KeyPairGenerator.1.2.840.113549.1.1.1", "RSA");
        put("Alg.Alias.KeyPairGenerator.1.2.840.113549.1.1.7", "RSA");
        put("Alg.Alias.KeyPairGenerator.2.5.8.1.1", "RSA");
        put("KeyPairGenerator.EC", PREFIX + "OpenSSLECKeyPairGenerator");
        put("Alg.Alias.KeyPairGenerator.1.2.840.10045.2.1", "EC");
        put("Alg.Alias.KeyPairGenerator.1.3.133.16.840.63.0.2", "EC");
        put("KeyFactory.RSA", PREFIX + "OpenSSLRSAKeyFactory");
        put("Alg.Alias.KeyFactory.1.2.840.113549.1.1.1", "RSA");
        put("Alg.Alias.KeyFactory.1.2.840.113549.1.1.7", "RSA");
        put("Alg.Alias.KeyFactory.2.5.8.1.1", "RSA");
        put("KeyFactory.EC", PREFIX + "OpenSSLECKeyFactory");
        put("Alg.Alias.KeyFactory.1.2.840.10045.2.1", "EC");
        put("Alg.Alias.KeyFactory.1.3.133.16.840.63.0.2", "EC");
        putECDHKeyAgreementImplClass("OpenSSLECDHKeyAgreement");
        putSignatureImplClass("MD5WithRSA", "OpenSSLSignature$MD5RSA");
        put("Alg.Alias.Signature.MD5WithRSAEncryption", "MD5WithRSA");
        put("Alg.Alias.Signature.MD5/RSA", "MD5WithRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.4", "MD5WithRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.4", "MD5WithRSA");
        put("Alg.Alias.Signature.1.2.840.113549.2.5with1.2.840.113549.1.1.1", "MD5WithRSA");
        putSignatureImplClass("SHA1WithRSA", "OpenSSLSignature$SHA1RSA");
        put("Alg.Alias.Signature.SHA1WithRSAEncryption", "SHA1WithRSA");
        put("Alg.Alias.Signature.SHA1/RSA", "SHA1WithRSA");
        put("Alg.Alias.Signature.SHA-1/RSA", "SHA1WithRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.5", "SHA1WithRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.5", "SHA1WithRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.113549.1.1.1", "SHA1WithRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.26with1.2.840.113549.1.1.5", "SHA1WithRSA");
        put("Alg.Alias.Signature.1.3.14.3.2.29", "SHA1WithRSA");
        put("Alg.Alias.Signature.OID.1.3.14.3.2.29", "SHA1WithRSA");
        putSignatureImplClass("SHA224WithRSA", "OpenSSLSignature$SHA224RSA");
        put("Alg.Alias.Signature.SHA224WithRSAEncryption", "SHA224WithRSA");
        put("Alg.Alias.Signature.SHA224/RSA", "SHA224WithRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.14", "SHA224WithRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.14", "SHA224WithRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.113549.1.1.1", "SHA224WithRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.4with1.2.840.113549.1.1.14", "SHA224WithRSA");
        putSignatureImplClass("SHA256WithRSA", "OpenSSLSignature$SHA256RSA");
        put("Alg.Alias.Signature.SHA256WithRSAEncryption", "SHA256WithRSA");
        put("Alg.Alias.Signature.SHA256/RSA", "SHA256WithRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.11", "SHA256WithRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.11", "SHA256WithRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.1", "SHA256WithRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.1with1.2.840.113549.1.1.11", "SHA256WithRSA");
        putSignatureImplClass("SHA384WithRSA", "OpenSSLSignature$SHA384RSA");
        put("Alg.Alias.Signature.SHA384WithRSAEncryption", "SHA384WithRSA");
        put("Alg.Alias.Signature.SHA384/RSA", "SHA384WithRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.12", "SHA384WithRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.12", "SHA384WithRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.2with1.2.840.113549.1.1.1", "SHA384WithRSA");
        putSignatureImplClass("SHA512WithRSA", "OpenSSLSignature$SHA512RSA");
        put("Alg.Alias.Signature.SHA512WithRSAEncryption", "SHA512WithRSA");
        put("Alg.Alias.Signature.SHA512/RSA", "SHA512WithRSA");
        put("Alg.Alias.Signature.1.2.840.113549.1.1.13", "SHA512WithRSA");
        put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.13", "SHA512WithRSA");
        put("Alg.Alias.Signature.2.16.840.1.101.3.4.2.3with1.2.840.113549.1.1.1", "SHA512WithRSA");
        putRAWRSASignatureImplClass("OpenSSLSignatureRawRSA");
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
        put("SecureRandom.SHA1PRNG", PREFIX + "OpenSSLRandom");
        put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
        putRSACipherImplClass("RSA/ECB/NoPadding", "OpenSSLCipherRSA$Raw");
        put("Alg.Alias.Cipher.RSA/None/NoPadding", "RSA/ECB/NoPadding");
        putRSACipherImplClass("RSA/ECB/PKCS1Padding", "OpenSSLCipherRSA$PKCS1");
        put("Alg.Alias.Cipher.RSA/None/PKCS1Padding", "RSA/ECB/PKCS1Padding");
        putRSACipherImplClass("RSA/ECB/OAEPPadding", "OpenSSLCipherRSA$OAEP$SHA1");
        put("Alg.Alias.Cipher.RSA/None/OAEPPadding", "RSA/ECB/OAEPPadding");
        putRSACipherImplClass("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", "OpenSSLCipherRSA$OAEP$SHA1");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-1AndMGF1Padding", "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        putRSACipherImplClass("RSA/ECB/OAEPWithSHA-224AndMGF1Padding", "OpenSSLCipherRSA$OAEP$SHA224");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-224AndMGF1Padding", "RSA/ECB/OAEPWithSHA-224AndMGF1Padding");
        putRSACipherImplClass("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "OpenSSLCipherRSA$OAEP$SHA256");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-256AndMGF1Padding", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        putRSACipherImplClass("RSA/ECB/OAEPWithSHA-384AndMGF1Padding", "OpenSSLCipherRSA$OAEP$SHA384");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-384AndMGF1Padding", "RSA/ECB/OAEPWithSHA-384AndMGF1Padding");
        putRSACipherImplClass("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", "OpenSSLCipherRSA$OAEP$SHA512");
        put("Alg.Alias.Cipher.RSA/None/OAEPWithSHA-512AndMGF1Padding", "RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
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
        put("Alg.Alias.Cipher.PBEWithHmacSHA1AndAES_128", "AES_128/CBC/PKCS5PADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA224AndAES_128", "AES_128/CBC/PKCS5PADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA256AndAES_128", "AES_128/CBC/PKCS5PADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA384AndAES_128", "AES_128/CBC/PKCS5PADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA512AndAES_128", "AES_128/CBC/PKCS5PADDING");
        putSymmetricCipherImplClass("AES_256/ECB/NoPadding", "OpenSSLCipher$EVP_CIPHER$AES_256$ECB$NoPadding");
        putSymmetricCipherImplClass("AES_256/ECB/PKCS5Padding", "OpenSSLCipher$EVP_CIPHER$AES_256$ECB$PKCS5Padding");
        put("Alg.Alias.Cipher.AES_256/ECB/PKCS7Padding", "AES_256/ECB/PKCS5Padding");
        putSymmetricCipherImplClass("AES_256/CBC/NoPadding", "OpenSSLCipher$EVP_CIPHER$AES_256$CBC$NoPadding");
        putSymmetricCipherImplClass("AES_256/CBC/PKCS5Padding", "OpenSSLCipher$EVP_CIPHER$AES_256$CBC$PKCS5Padding");
        put("Alg.Alias.Cipher.AES_256/CBC/PKCS7Padding", "AES_256/CBC/PKCS5Padding");
        put("Alg.Alias.Cipher.PBEWithHmacSHA1AndAES_256", "AES_256/CBC/PKCS5PADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA224AndAES_256", "AES_256/CBC/PKCS5PADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA256AndAES_256", "AES_256/CBC/PKCS5PADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA384AndAES_256", "AES_256/CBC/PKCS5PADDING");
        put("Alg.Alias.Cipher.PBEWithHmacSHA512AndAES_256", "AES_256/CBC/PKCS5PADDING");
        putSymmetricCipherImplClass("DESEDE/CBC/NoPadding", "OpenSSLCipher$EVP_CIPHER$DESEDE$CBC$NoPadding");
        putSymmetricCipherImplClass("DESEDE/CBC/PKCS5Padding", "OpenSSLCipher$EVP_CIPHER$DESEDE$CBC$PKCS5Padding");
        put("Alg.Alias.Cipher.DESEDE/CBC/PKCS7Padding", "DESEDE/CBC/PKCS5Padding");
        putSymmetricCipherImplClass("ARC4", "OpenSSLCipher$EVP_CIPHER$ARC4");
        put("Alg.Alias.Cipher.ARCFOUR", "ARC4");
        put("Alg.Alias.Cipher.RC4", "ARC4");
        put("Alg.Alias.Cipher.1.2.840.113549.3.4", "ARC4");
        put("Alg.Alias.Cipher.OID.1.2.840.113549.3.4", "ARC4");
        putSymmetricCipherImplClass("AES/GCM/NoPadding", "OpenSSLCipher$EVP_AEAD$AES$GCM");
        put("Alg.Alias.Cipher.GCM", "AES/GCM/NoPadding");
        put("Alg.Alias.Cipher.2.16.840.1.101.3.4.1.6", "AES/GCM/NoPadding");
        put("Alg.Alias.Cipher.2.16.840.1.101.3.4.1.26", "AES/GCM/NoPadding");
        put("Alg.Alias.Cipher.2.16.840.1.101.3.4.1.46", "AES/GCM/NoPadding");
        putSymmetricCipherImplClass("AES_128/GCM/NoPadding", "OpenSSLCipher$EVP_AEAD$AES$GCM$AES_128");
        putSymmetricCipherImplClass("AES_256/GCM/NoPadding", "OpenSSLCipher$EVP_AEAD$AES$GCM$AES_256");
        putMacImplClass("HmacMD5", "OpenSSLMac$HmacMD5");
        put("Alg.Alias.Mac.1.3.6.1.5.5.8.1.1", "HmacMD5");
        put("Alg.Alias.Mac.HMAC-MD5", "HmacMD5");
        put("Alg.Alias.Mac.HMAC/MD5", "HmacMD5");
        putMacImplClass("HmacSHA1", "OpenSSLMac$HmacSHA1");
        put("Alg.Alias.Mac.1.2.840.113549.2.7", "HmacSHA1");
        put("Alg.Alias.Mac.1.3.6.1.5.5.8.1.2", "HmacSHA1");
        put("Alg.Alias.Mac.HMAC-SHA1", "HmacSHA1");
        put("Alg.Alias.Mac.HMAC/SHA1", "HmacSHA1");
        putMacImplClass("HmacSHA224", "OpenSSLMac$HmacSHA224");
        put("Alg.Alias.Mac.1.2.840.113549.2.8", "HmacSHA224");
        put("Alg.Alias.Mac.HMAC-SHA224", "HmacSHA224");
        put("Alg.Alias.Mac.HMAC/SHA224", "HmacSHA224");
        put("Alg.Alias.Mac.PBEWITHHMACSHA224", "HmacSHA224");
        putMacImplClass("HmacSHA256", "OpenSSLMac$HmacSHA256");
        put("Alg.Alias.Mac.1.2.840.113549.2.9", "HmacSHA256");
        put("Alg.Alias.Mac.2.16.840.1.101.3.4.2.1", "HmacSHA256");
        put("Alg.Alias.Mac.HMAC-SHA256", "HmacSHA256");
        put("Alg.Alias.Mac.HMAC/SHA256", "HmacSHA256");
        put("Alg.Alias.Mac.PBEWITHHMACSHA256", "HmacSHA256");
        putMacImplClass("HmacSHA384", "OpenSSLMac$HmacSHA384");
        put("Alg.Alias.Mac.1.2.840.113549.2.10", "HmacSHA384");
        put("Alg.Alias.Mac.HMAC-SHA384", "HmacSHA384");
        put("Alg.Alias.Mac.HMAC/SHA384", "HmacSHA384");
        put("Alg.Alias.Mac.PBEWITHHMACSHA384", "HmacSHA384");
        putMacImplClass("HmacSHA512", "OpenSSLMac$HmacSHA512");
        put("Alg.Alias.Mac.1.2.840.113549.2.11", "HmacSHA512");
        put("Alg.Alias.Mac.HMAC-SHA512", "HmacSHA512");
        put("Alg.Alias.Mac.HMAC/SHA512", "HmacSHA512");
        put("Alg.Alias.Mac.PBEWITHHMACSHA512", "HmacSHA512");
        put("CertificateFactory.X509", PREFIX + "OpenSSLX509CertificateFactory");
        put("Alg.Alias.CertificateFactory.X.509", "X509");
    }

    private void putMacImplClass(String algorithm, String className) {
        putImplClassWithKeyConstraints("Mac." + algorithm, PREFIX + className, PREFIX + "OpenSSLKeyHolder", "RAW");
    }

    private void putSymmetricCipherImplClass(String transformation, String className) {
        putImplClassWithKeyConstraints("Cipher." + transformation, PREFIX + className, null, "RAW");
    }

    private void putRSACipherImplClass(String transformation, String className) {
        putImplClassWithKeyConstraints("Cipher." + transformation, PREFIX + className, PREFIX + "OpenSSLRSAPrivateKey" + "|" + STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + PREFIX + "OpenSSLRSAPublicKey" + "|" + STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME, null);
    }

    private void putSignatureImplClass(String algorithm, String className) {
        putImplClassWithKeyConstraints("Signature." + algorithm, PREFIX + className, PREFIX + "OpenSSLKeyHolder" + "|" + STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + STANDARD_EC_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME, "PKCS#8|X.509");
    }

    private void putRAWRSASignatureImplClass(String className) {
        putImplClassWithKeyConstraints("Signature.NONEwithRSA", PREFIX + className, PREFIX + "OpenSSLRSAPrivateKey" + "|" + STANDARD_RSA_PRIVATE_KEY_INTERFACE_CLASS_NAME + "|" + PREFIX + "OpenSSLRSAPublicKey" + "|" + STANDARD_RSA_PUBLIC_KEY_INTERFACE_CLASS_NAME, null);
    }

    private void putECDHKeyAgreementImplClass(String className) {
        putImplClassWithKeyConstraints("KeyAgreement.ECDH", PREFIX + className, PREFIX + "OpenSSLKeyHolder" + "|" + STANDARD_EC_PRIVATE_KEY_INTERFACE_CLASS_NAME, "PKCS#8");
    }

    private void putImplClassWithKeyConstraints(String typeAndAlgName, String fullyQualifiedClassName, String supportedKeyClasses, String supportedKeyFormats) {
        put(typeAndAlgName, fullyQualifiedClassName);
        if (supportedKeyClasses != null) {
            put(typeAndAlgName + " SupportedKeyClasses", supportedKeyClasses);
        }
        if (supportedKeyFormats != null) {
            put(typeAndAlgName + " SupportedKeyFormats", supportedKeyFormats);
        }
    }
}
