package android.security.keystore;

import java.util.Collection;
import java.util.Locale;
import libcore.util.EmptyArray;

public abstract class KeyProperties {
    public static final String BLOCK_MODE_CBC = "CBC";
    public static final String BLOCK_MODE_CTR = "CTR";
    public static final String BLOCK_MODE_ECB = "ECB";
    public static final String BLOCK_MODE_GCM = "GCM";
    public static final String DIGEST_MD5 = "MD5";
    public static final String DIGEST_NONE = "NONE";
    public static final String DIGEST_SHA1 = "SHA-1";
    public static final String DIGEST_SHA224 = "SHA-224";
    public static final String DIGEST_SHA256 = "SHA-256";
    public static final String DIGEST_SHA384 = "SHA-384";
    public static final String DIGEST_SHA512 = "SHA-512";
    public static final String ENCRYPTION_PADDING_NONE = "NoPadding";
    public static final String ENCRYPTION_PADDING_PKCS7 = "PKCS7Padding";
    public static final String ENCRYPTION_PADDING_RSA_OAEP = "OAEPPadding";
    public static final String ENCRYPTION_PADDING_RSA_PKCS1 = "PKCS1Padding";
    public static final String KEY_ALGORITHM_AES = "AES";
    public static final String KEY_ALGORITHM_EC = "EC";
    public static final String KEY_ALGORITHM_HMAC_SHA1 = "HmacSHA1";
    public static final String KEY_ALGORITHM_HMAC_SHA224 = "HmacSHA224";
    public static final String KEY_ALGORITHM_HMAC_SHA256 = "HmacSHA256";
    public static final String KEY_ALGORITHM_HMAC_SHA384 = "HmacSHA384";
    public static final String KEY_ALGORITHM_HMAC_SHA512 = "HmacSHA512";
    public static final String KEY_ALGORITHM_RSA = "RSA";
    public static final int ORIGIN_GENERATED = 1;
    public static final int ORIGIN_IMPORTED = 2;
    public static final int ORIGIN_UNKNOWN = 4;
    public static final int PURPOSE_DECRYPT = 2;
    public static final int PURPOSE_ENCRYPT = 1;
    public static final int PURPOSE_SIGN = 4;
    public static final int PURPOSE_VERIFY = 8;
    public static final String SIGNATURE_PADDING_RSA_PKCS1 = "PKCS1";
    public static final String SIGNATURE_PADDING_RSA_PSS = "PSS";

    public static abstract class BlockMode {
        private BlockMode() {
        }

        public static int toKeymaster(String blockMode) {
            if (KeyProperties.BLOCK_MODE_ECB.equalsIgnoreCase(blockMode)) {
                return 1;
            }
            if (KeyProperties.BLOCK_MODE_CBC.equalsIgnoreCase(blockMode)) {
                return 2;
            }
            if (KeyProperties.BLOCK_MODE_CTR.equalsIgnoreCase(blockMode)) {
                return 3;
            }
            if (KeyProperties.BLOCK_MODE_GCM.equalsIgnoreCase(blockMode)) {
                return 32;
            }
            throw new IllegalArgumentException("Unsupported block mode: " + blockMode);
        }

        public static String fromKeymaster(int blockMode) {
            switch (blockMode) {
                case 1:
                    return KeyProperties.BLOCK_MODE_ECB;
                case 2:
                    return KeyProperties.BLOCK_MODE_CBC;
                case 3:
                    return KeyProperties.BLOCK_MODE_CTR;
                case 32:
                    return KeyProperties.BLOCK_MODE_GCM;
                default:
                    throw new IllegalArgumentException("Unsupported block mode: " + blockMode);
            }
        }

        public static String[] allFromKeymaster(Collection<Integer> blockModes) {
            if (blockModes == null || blockModes.isEmpty()) {
                return EmptyArray.STRING;
            }
            String[] result = new String[blockModes.size()];
            int offset = 0;
            for (Integer intValue : blockModes) {
                result[offset] = fromKeymaster(intValue.intValue());
                offset++;
            }
            return result;
        }

        public static int[] allToKeymaster(String[] blockModes) {
            if (blockModes == null || blockModes.length == 0) {
                return EmptyArray.INT;
            }
            int[] result = new int[blockModes.length];
            for (int i = 0; i < blockModes.length; i++) {
                result[i] = toKeymaster(blockModes[i]);
            }
            return result;
        }
    }

    public static abstract class Digest {
        private Digest() {
        }

        public static int toKeymaster(String digest) {
            String toUpperCase = digest.toUpperCase(Locale.US);
            if (toUpperCase.equals(KeyProperties.DIGEST_SHA1)) {
                return 2;
            }
            if (toUpperCase.equals(KeyProperties.DIGEST_SHA224)) {
                return 3;
            }
            if (toUpperCase.equals(KeyProperties.DIGEST_SHA256)) {
                return 4;
            }
            if (toUpperCase.equals(KeyProperties.DIGEST_SHA384)) {
                return 5;
            }
            if (toUpperCase.equals(KeyProperties.DIGEST_SHA512)) {
                return 6;
            }
            if (toUpperCase.equals(KeyProperties.DIGEST_NONE)) {
                return 0;
            }
            if (toUpperCase.equals(KeyProperties.DIGEST_MD5)) {
                return 1;
            }
            throw new IllegalArgumentException("Unsupported digest algorithm: " + digest);
        }

        public static String fromKeymaster(int digest) {
            switch (digest) {
                case 0:
                    return KeyProperties.DIGEST_NONE;
                case 1:
                    return KeyProperties.DIGEST_MD5;
                case 2:
                    return KeyProperties.DIGEST_SHA1;
                case 3:
                    return KeyProperties.DIGEST_SHA224;
                case 4:
                    return KeyProperties.DIGEST_SHA256;
                case 5:
                    return KeyProperties.DIGEST_SHA384;
                case 6:
                    return KeyProperties.DIGEST_SHA512;
                default:
                    throw new IllegalArgumentException("Unsupported digest algorithm: " + digest);
            }
        }

        public static String fromKeymasterToSignatureAlgorithmDigest(int digest) {
            switch (digest) {
                case 0:
                    return KeyProperties.DIGEST_NONE;
                case 1:
                    return KeyProperties.DIGEST_MD5;
                case 2:
                    return "SHA1";
                case 3:
                    return "SHA224";
                case 4:
                    return "SHA256";
                case 5:
                    return "SHA384";
                case 6:
                    return "SHA512";
                default:
                    throw new IllegalArgumentException("Unsupported digest algorithm: " + digest);
            }
        }

        public static String[] allFromKeymaster(Collection<Integer> digests) {
            if (digests.isEmpty()) {
                return EmptyArray.STRING;
            }
            String[] result = new String[digests.size()];
            int offset = 0;
            for (Integer intValue : digests) {
                result[offset] = fromKeymaster(intValue.intValue());
                offset++;
            }
            return result;
        }

        public static int[] allToKeymaster(String[] digests) {
            if (digests == null || digests.length == 0) {
                return EmptyArray.INT;
            }
            int[] result = new int[digests.length];
            int offset = 0;
            for (String digest : digests) {
                result[offset] = toKeymaster(digest);
                offset++;
            }
            return result;
        }
    }

    public static abstract class EncryptionPadding {
        private EncryptionPadding() {
        }

        public static int toKeymaster(String padding) {
            if (KeyProperties.ENCRYPTION_PADDING_NONE.equalsIgnoreCase(padding)) {
                return 1;
            }
            if (KeyProperties.ENCRYPTION_PADDING_PKCS7.equalsIgnoreCase(padding)) {
                return 64;
            }
            if (KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1.equalsIgnoreCase(padding)) {
                return 4;
            }
            if (KeyProperties.ENCRYPTION_PADDING_RSA_OAEP.equalsIgnoreCase(padding)) {
                return 2;
            }
            throw new IllegalArgumentException("Unsupported encryption padding scheme: " + padding);
        }

        public static String fromKeymaster(int padding) {
            switch (padding) {
                case 1:
                    return KeyProperties.ENCRYPTION_PADDING_NONE;
                case 2:
                    return KeyProperties.ENCRYPTION_PADDING_RSA_OAEP;
                case 4:
                    return KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1;
                case 64:
                    return KeyProperties.ENCRYPTION_PADDING_PKCS7;
                default:
                    throw new IllegalArgumentException("Unsupported encryption padding: " + padding);
            }
        }

        public static int[] allToKeymaster(String[] paddings) {
            if (paddings == null || paddings.length == 0) {
                return EmptyArray.INT;
            }
            int[] result = new int[paddings.length];
            for (int i = 0; i < paddings.length; i++) {
                result[i] = toKeymaster(paddings[i]);
            }
            return result;
        }
    }

    public static abstract class KeyAlgorithm {
        private KeyAlgorithm() {
        }

        public static int toKeymasterAsymmetricKeyAlgorithm(String algorithm) {
            if (KeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(algorithm)) {
                return 3;
            }
            if (KeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(algorithm)) {
                return 1;
            }
            throw new IllegalArgumentException("Unsupported key algorithm: " + algorithm);
        }

        public static String fromKeymasterAsymmetricKeyAlgorithm(int keymasterAlgorithm) {
            switch (keymasterAlgorithm) {
                case 1:
                    return KeyProperties.KEY_ALGORITHM_RSA;
                case 3:
                    return KeyProperties.KEY_ALGORITHM_EC;
                default:
                    throw new IllegalArgumentException("Unsupported key algorithm: " + keymasterAlgorithm);
            }
        }

        public static int toKeymasterSecretKeyAlgorithm(String algorithm) {
            if (KeyProperties.KEY_ALGORITHM_AES.equalsIgnoreCase(algorithm)) {
                return 32;
            }
            if (algorithm.toUpperCase(Locale.US).startsWith("HMAC")) {
                return 128;
            }
            throw new IllegalArgumentException("Unsupported secret key algorithm: " + algorithm);
        }

        public static String fromKeymasterSecretKeyAlgorithm(int keymasterAlgorithm, int keymasterDigest) {
            switch (keymasterAlgorithm) {
                case 32:
                    return KeyProperties.KEY_ALGORITHM_AES;
                case 128:
                    switch (keymasterDigest) {
                        case 2:
                            return KeyProperties.KEY_ALGORITHM_HMAC_SHA1;
                        case 3:
                            return KeyProperties.KEY_ALGORITHM_HMAC_SHA224;
                        case 4:
                            return KeyProperties.KEY_ALGORITHM_HMAC_SHA256;
                        case 5:
                            return KeyProperties.KEY_ALGORITHM_HMAC_SHA384;
                        case 6:
                            return KeyProperties.KEY_ALGORITHM_HMAC_SHA512;
                        default:
                            throw new IllegalArgumentException("Unsupported HMAC digest: " + Digest.fromKeymaster(keymasterDigest));
                    }
                default:
                    throw new IllegalArgumentException("Unsupported key algorithm: " + keymasterAlgorithm);
            }
        }

        public static int toKeymasterDigest(String algorithm) {
            String algorithmUpper = algorithm.toUpperCase(Locale.US);
            if (!algorithmUpper.startsWith("HMAC")) {
                return -1;
            }
            String digestUpper = algorithmUpper.substring("HMAC".length());
            if (digestUpper.equals("SHA1")) {
                return 2;
            }
            if (digestUpper.equals("SHA224")) {
                return 3;
            }
            if (digestUpper.equals("SHA256")) {
                return 4;
            }
            if (digestUpper.equals("SHA384")) {
                return 5;
            }
            if (digestUpper.equals("SHA512")) {
                return 6;
            }
            throw new IllegalArgumentException("Unsupported HMAC digest: " + digestUpper);
        }
    }

    public static abstract class Origin {
        private Origin() {
        }

        public static int fromKeymaster(int origin) {
            switch (origin) {
                case 0:
                    return 1;
                case 2:
                    return 2;
                case 3:
                    return 4;
                default:
                    throw new IllegalArgumentException("Unknown origin: " + origin);
            }
        }
    }

    public static abstract class Purpose {
        private Purpose() {
        }

        public static int toKeymaster(int purpose) {
            switch (purpose) {
                case 1:
                    return 0;
                case 2:
                    return 1;
                case 4:
                    return 2;
                case 8:
                    return 3;
                default:
                    throw new IllegalArgumentException("Unknown purpose: " + purpose);
            }
        }

        public static int fromKeymaster(int purpose) {
            switch (purpose) {
                case 0:
                    return 1;
                case 1:
                    return 2;
                case 2:
                    return 4;
                case 3:
                    return 8;
                default:
                    throw new IllegalArgumentException("Unknown purpose: " + purpose);
            }
        }

        public static int[] allToKeymaster(int purposes) {
            int[] result = KeyProperties.getSetFlags(purposes);
            for (int i = 0; i < result.length; i++) {
                result[i] = toKeymaster(result[i]);
            }
            return result;
        }

        public static int allFromKeymaster(Collection<Integer> purposes) {
            int result = 0;
            for (Integer intValue : purposes) {
                result |= fromKeymaster(intValue.intValue());
            }
            return result;
        }
    }

    static abstract class SignaturePadding {
        private SignaturePadding() {
        }

        static int toKeymaster(String padding) {
            String toUpperCase = padding.toUpperCase(Locale.US);
            if (toUpperCase.equals(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)) {
                return 5;
            }
            if (toUpperCase.equals(KeyProperties.SIGNATURE_PADDING_RSA_PSS)) {
                return 3;
            }
            throw new IllegalArgumentException("Unsupported signature padding scheme: " + padding);
        }

        static String fromKeymaster(int padding) {
            switch (padding) {
                case 3:
                    return KeyProperties.SIGNATURE_PADDING_RSA_PSS;
                case 5:
                    return KeyProperties.SIGNATURE_PADDING_RSA_PKCS1;
                default:
                    throw new IllegalArgumentException("Unsupported signature padding: " + padding);
            }
        }

        static int[] allToKeymaster(String[] paddings) {
            if (paddings == null || paddings.length == 0) {
                return EmptyArray.INT;
            }
            int[] result = new int[paddings.length];
            for (int i = 0; i < paddings.length; i++) {
                result[i] = toKeymaster(paddings[i]);
            }
            return result;
        }
    }

    private KeyProperties() {
    }

    private static int[] getSetFlags(int flags) {
        if (flags == 0) {
            return EmptyArray.INT;
        }
        int[] result = new int[getSetBitCount(flags)];
        int resultOffset = 0;
        int flag = 1;
        while (flags != 0) {
            if ((flags & 1) != 0) {
                result[resultOffset] = flag;
                resultOffset++;
            }
            flags >>>= 1;
            flag <<= 1;
        }
        return result;
    }

    private static int getSetBitCount(int value) {
        if (value == 0) {
            return 0;
        }
        int result = 0;
        while (value != 0) {
            if ((value & 1) != 0) {
                result++;
            }
            value >>>= 1;
        }
        return result;
    }
}
