package ohos.security.keystore;

public abstract class KeyStoreConstants {
    public static final int CRYPTO_PARAMETER_BLOCK_MODE = 2;
    public static final int CRYPTO_PARAMETER_DIGEST = 0;
    public static final int CRYPTO_PARAMETER_ENCRYPT_PADDING = 1;
    public static final int CRYPTO_PARAMETER_SIGNATURE_PADDING = 3;
    public static final String DIGEST_ALGORITHM_MD5 = "MD5";
    public static final String DIGEST_ALGORITHM_NONE = "NONE";
    public static final String DIGEST_ALGORITHM_SHA1 = "SHA-1";
    public static final String DIGEST_ALGORITHM_SHA224 = "SHA-224";
    public static final String DIGEST_ALGORITHM_SHA256 = "SHA-256";
    public static final String DIGEST_ALGORITHM_SHA384 = "SHA-384";
    public static final String DIGEST_ALGORITHM_SHA512 = "SHA-512";
    public static final int FLAG_KEY_ACCESSIBLE_NEEDED_USER_AUTH = 1;
    public static final int FLAG_KEY_ACCESSIBLE_NEED_DEVICE_UNLOCK = 4;
    public static final int FLAG_KEY_ACCESSIBLE_NEED_RANDOMIZED_ENCRYPTION = 8;
    public static final String NONE_ENCRYPTION_PADDING = "NoPadding";
    public static final String OPTIMAL_ASYMMETRIC_ENCRYPTION_PADDING = "OAEPPadding";
    public static final String PKCS1_ENCRYPTION_PADDING = "PKCS1Padding";
    public static final String PKCS1_SIGNATURE_PADDING = "PKCS1";
    public static final String PKCS7_ENCRYPTION_PADDING = "PKCS7Padding";
    public static final String PROBABILISTIC_SIGNATURE_PADDING = "PSS";
    public static final int PURPOSE_CAN_DECRYPT = 2;
    public static final int PURPOSE_CAN_ENCRYPT = 1;
    public static final int PURPOSE_CAN_SIGN = 4;
    public static final int PURPOSE_CAN_VERIFY = 8;
    public static final int PURPOSE_CAN_WRAP = 32;
    public static final String SEC_BLOCK_MODE_CBC = "CBC";
    public static final String SEC_BLOCK_MODE_CTR = "CTR";
    public static final String SEC_BLOCK_MODE_ECB = "ECB";
    public static final String SEC_BLOCK_MODE_GCM = "GCM";
    public static final String SEC_KEY_ALGORITHM_AES = "AES";
    public static final String SEC_KEY_ALGORITHM_EC = "EC";
    public static final String SEC_KEY_ALGORITHM_HMAC_SHA1 = "HmacSHA1";
    public static final String SEC_KEY_ALGORITHM_HMAC_SHA224 = "HmacSHA224";
    public static final String SEC_KEY_ALGORITHM_HMAC_SHA256 = "HmacSHA256";
    public static final String SEC_KEY_ALGORITHM_HMAC_SHA384 = "HmacSHA384";
    public static final String SEC_KEY_ALGORITHM_HMAC_SHA512 = "HmacSHA512";
    public static final String SEC_KEY_ALGORITHM_RSA = "RSA";
    public static final int SEC_KEY_SOURCE_GENERATED = 1;
    public static final int SEC_KEY_SOURCE_IMPORTED = 2;
    public static final int SEC_KEY_SOURCE_UNKNOWN = 4;

    private KeyStoreConstants() {
    }
}
