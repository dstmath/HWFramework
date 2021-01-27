package ohos.security.keystore.provider;

abstract class Constants {
    static final String AES_128_CBC_NOPADDING = "AES_128/CBC/NoPadding";
    static final String AES_128_CBC_NOPADDING_UPPER = "AES_128/CBC/NOPADDING";
    static final String AES_128_CBC_PKCS5PADDING = "AES_128/CBC/PKCS5Padding";
    static final String AES_128_ECB_NOPADDING = "AES_128/ECB/NoPadding";
    static final String AES_128_ECB_PKCS5PADDING = "AES_128/ECB/PKCS5Padding";
    static final String AES_128_GCM_NOPADDING = "AES_128/GCM/NoPadding";
    static final String AES_256_CBC_NOPADDING = "AES_256/CBC/NoPadding";
    static final String AES_256_CBC_NOPADDING_UPPER = "AES_256/CBC/NOPADDING";
    static final String AES_256_CBC_PKCS5PADDING = "AES_256/CBC/PKCS5Padding";
    static final String AES_256_ECB_NOPADDING = "AES_256/ECB/NoPadding";
    static final String AES_256_ECB_PKCS5PADDING = "AES_256/ECB/PKCS5Padding";
    static final String AES_256_GCM_NOPADDING = "AES_256/GCM/NoPadding";
    static final String AES_CBC_NOPADDING = "AES/CBC/NoPadding";
    static final String AES_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding";
    static final String AES_CBC_PKCS7PADDING = "AES/CBC/PKCS7Padding";
    static final String AES_CTR_NOPADDING = "AES/CTR/NoPadding";
    static final String AES_ECB_NOPADDING = "AES/ECB/NoPadding";
    static final String AES_ECB_PKCS5PADDING = "AES/ECB/PKCS5Padding";
    static final String AES_ECB_PKCS7PADDING = "AES/ECB/PKCS7Padding";
    static final String AES_GCM_NOPADDING = "AES/GCM/NoPadding";
    static final String ARC4 = "ARC4";
    static final String CHACHA20 = "ChaCha20";
    static final String CHACHA20_POLY1305_NOPADDING = "ChaCha20/Poly1305/NoPadding";
    static final String DESEDE_CBC_NOPADDING = "DESEDE/CBC/NoPadding";
    static final String DESEDE_CBC_PKCS5PADDING = "DESEDE/CBC/PKCS5Padding";
    static final String EC_PARAMETERS = "ECParameters";
    static final String FORMAT_PKCS8 = "PKCS#8";
    static final String FORMAT_RAW = "RAW";
    static final String FORMAT_X_509 = "X.509";
    static final String GCM_PARAMETERS = "GCMParameters";
    static final String MD5_WITH_RSA = "MD5withRSA";
    static final String NONE_WITH_ECDSA = "NONEwithECDSA";
    static final String NONE_WITH_RSA = "NONEwithRSA";
    static final String OAEP_PARAMETERS = "OAEPParameters";
    static final String PSS_PARAMETERS = "PSSParameters";
    static final String RAW_RSA = "OpenSSLSignatureRawRSA";
    static final String RSA_ECB_MGF1PADDING_SHA1 = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
    static final String RSA_ECB_MGF1PADDING_SHA224 = "RSA/ECB/OAEPWithSHA-224AndMGF1Padding";
    static final String RSA_ECB_MGF1PADDING_SHA256 = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    static final String RSA_ECB_MGF1PADDING_SHA384 = "RSA/ECB/OAEPWithSHA-384AndMGF1Padding";
    static final String RSA_ECB_MGF1PADDING_SHA512 = "RSA/ECB/OAEPWithSHA-512AndMGF1Padding";
    static final String RSA_ECB_NOPADDING = "RSA/ECB/NoPadding";
    static final String RSA_ECB_OAEPPADDING = "RSA/ECB/OAEPPadding";
    static final String RSA_ECB_PKCS1PADDING = "RSA/ECB/PKCS1Padding";
    static final String SEC_KEY_ALGORITHM_3DES = "DESEDE";
    static final String SEC_KEY_ALGORITHM_HMAC_MD5 = "HmacMD5";
    static final String SHA1_WITH_ECDSA = "SHA1withECDSA";
    static final String SHA1_WITH_RSA = "SHA1withRSA";
    static final String SHA1_WITH_RSA_PSS = "SHA1withRSA/PSS";
    static final String SHA224_WITH_ECDSA = "SHA224withECDSA";
    static final String SHA224_WITH_RSA = "SHA224withRSA";
    static final String SHA224_WITH_RSA_PSS = "SHA224withRSA/PSS";
    static final String SHA256_WITH_ECDSA = "SHA256withECDSA";
    static final String SHA256_WITH_RSA = "SHA256withRSA";
    static final String SHA256_WITH_RSA_PSS = "SHA256withRSA/PSS";
    static final String SHA384_WITH_ECDSA = "SHA384withECDSA";
    static final String SHA384_WITH_RSA = "SHA384withRSA";
    static final String SHA384_WITH_RSA_PSS = "SHA384withRSA/PSS";
    static final String SHA512_WITH_ECDSA = "SHA512withECDSA";
    static final String SHA512_WITH_RSA = "SHA512withRSA";
    static final String SHA512_WITH_RSA_PSS = "SHA512withRSA/PSS";
    static final String SIGNATURE_VERTICAL_BAR = "|";

    private Constants() {
    }
}
