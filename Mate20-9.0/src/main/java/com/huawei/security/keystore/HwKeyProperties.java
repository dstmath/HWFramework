package com.huawei.security.keystore;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keystore.ArrayUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Locale;

public abstract class HwKeyProperties {
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
    public static final int PURPOSE_SOTER_ATTEST_KEY = 16;
    public static final int PURPOSE_VERIFY = 8;
    public static final String SIGNATURE_PADDING_RSA_PKCS1 = "PKCS1";
    public static final String SIGNATURE_PADDING_RSA_PSS = "PSS";

    public static abstract class BlockMode {
        private BlockMode() {
        }

        public static int toKeymaster(@NonNull String blockMode) {
            if (HwKeyProperties.BLOCK_MODE_ECB.equalsIgnoreCase(blockMode)) {
                return 1;
            }
            if (HwKeyProperties.BLOCK_MODE_CBC.equalsIgnoreCase(blockMode)) {
                return 2;
            }
            if (HwKeyProperties.BLOCK_MODE_CTR.equalsIgnoreCase(blockMode)) {
                return 3;
            }
            if (HwKeyProperties.BLOCK_MODE_GCM.equalsIgnoreCase(blockMode)) {
                return 32;
            }
            throw new IllegalArgumentException("Unsupported block mode: " + blockMode);
        }

        @NonNull
        public static String fromKeymaster(int blockMode) {
            if (blockMode == 32) {
                return HwKeyProperties.BLOCK_MODE_GCM;
            }
            switch (blockMode) {
                case 1:
                    return HwKeyProperties.BLOCK_MODE_ECB;
                case 2:
                    return HwKeyProperties.BLOCK_MODE_CBC;
                case 3:
                    return HwKeyProperties.BLOCK_MODE_CTR;
                default:
                    throw new IllegalArgumentException("Unsupported block mode: " + blockMode);
            }
        }

        @NonNull
        public static String[] allFromKeymaster(@NonNull Collection<Integer> blockModes) {
            if (blockModes == null || blockModes.isEmpty()) {
                return ArrayUtils.EmptyArray.STRING;
            }
            String[] result = new String[blockModes.size()];
            int offset = 0;
            for (Integer intValue : blockModes) {
                result[offset] = fromKeymaster(intValue.intValue());
                offset++;
            }
            return result;
        }

        public static int[] allToKeymaster(@Nullable String[] blockModes) {
            if (blockModes == null || blockModes.length == 0) {
                return ArrayUtils.EmptyArray.INT;
            }
            int[] result = new int[blockModes.length];
            for (int i = 0; i < blockModes.length; i++) {
                result[i] = toKeymaster(blockModes[i]);
            }
            return result;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface BlockModeEnum {
    }

    public static abstract class Digest {
        private Digest() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x0053  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0055  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0057  */
        public static int toKeymaster(@NonNull String digest) {
            char c;
            String upperCase = digest.toUpperCase(Locale.US);
            int hashCode = upperCase.hashCode();
            if (hashCode == -1523887726) {
                if (upperCase.equals(HwKeyProperties.DIGEST_SHA256)) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == -1523886674) {
                if (upperCase.equals(HwKeyProperties.DIGEST_SHA384)) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == -1523884971 && upperCase.equals(HwKeyProperties.DIGEST_SHA512)) {
                c = 2;
                switch (c) {
                    case 0:
                        return 4;
                    case 1:
                        return 5;
                    case 2:
                        return 6;
                    default:
                        throw new IllegalArgumentException("Unsupported digest algorithm: " + digest);
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        }

        @NonNull
        public static String fromKeymaster(int digest) {
            switch (digest) {
                case 4:
                    return HwKeyProperties.DIGEST_SHA256;
                case 5:
                    return HwKeyProperties.DIGEST_SHA384;
                case 6:
                    return HwKeyProperties.DIGEST_SHA512;
                default:
                    throw new IllegalArgumentException("Unsupported digest algorithm: " + digest);
            }
        }

        @NonNull
        public static String fromKeymasterToSignatureAlgorithmDigest(int digest) {
            switch (digest) {
                case 4:
                    return HwKeyProperties.DIGEST_SHA256;
                case 5:
                    return HwKeyProperties.DIGEST_SHA384;
                case 6:
                    return HwKeyProperties.DIGEST_SHA512;
                default:
                    throw new IllegalArgumentException("Unsupported digest algorithm: " + digest);
            }
        }

        @NonNull
        public static String[] allFromKeymaster(@NonNull Collection<Integer> digests) {
            if (digests.isEmpty()) {
                return ArrayUtils.EmptyArray.STRING;
            }
            String[] result = new String[digests.size()];
            int offset = 0;
            for (Integer intValue : digests) {
                result[offset] = fromKeymaster(intValue.intValue());
                offset++;
            }
            return result;
        }

        @NonNull
        public static int[] allToKeymaster(@Nullable String[] digests) {
            if (digests == null || digests.length == 0) {
                return ArrayUtils.EmptyArray.INT;
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface DigestEnum {
    }

    public static abstract class EncryptionPadding {
        private EncryptionPadding() {
        }

        public static int toKeymaster(@NonNull String padding) {
            if (HwKeyProperties.ENCRYPTION_PADDING_NONE.equalsIgnoreCase(padding)) {
                return 1;
            }
            if (HwKeyProperties.ENCRYPTION_PADDING_PKCS7.equalsIgnoreCase(padding)) {
                return 64;
            }
            if (HwKeyProperties.ENCRYPTION_PADDING_RSA_PKCS1.equalsIgnoreCase(padding)) {
                return 4;
            }
            if (HwKeyProperties.ENCRYPTION_PADDING_RSA_OAEP.equalsIgnoreCase(padding)) {
                return 2;
            }
            throw new IllegalArgumentException("Unsupported encryption padding scheme: " + padding);
        }

        @NonNull
        public static String fromKeymaster(int padding) {
            if (padding == 4) {
                return HwKeyProperties.ENCRYPTION_PADDING_RSA_PKCS1;
            }
            if (padding == 64) {
                return HwKeyProperties.ENCRYPTION_PADDING_PKCS7;
            }
            switch (padding) {
                case 1:
                    return HwKeyProperties.ENCRYPTION_PADDING_NONE;
                case 2:
                    return HwKeyProperties.ENCRYPTION_PADDING_RSA_OAEP;
                default:
                    throw new IllegalArgumentException("Unsupported encryption padding: " + padding);
            }
        }

        @NonNull
        public static int[] allToKeymaster(@Nullable String[] paddings) {
            if (paddings == null || paddings.length == 0) {
                return ArrayUtils.EmptyArray.INT;
            }
            int[] result = new int[paddings.length];
            for (int i = 0; i < paddings.length; i++) {
                result[i] = toKeymaster(paddings[i]);
            }
            return result;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EncryptionPaddingEnum {
    }

    public static abstract class KeyAlgorithm {
        private KeyAlgorithm() {
        }

        public static int toKeymasterAsymmetricKeyAlgorithm(@NonNull String algorithm) {
            if (HwKeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(algorithm)) {
                return 3;
            }
            if (HwKeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(algorithm)) {
                return 1;
            }
            throw new IllegalArgumentException("Unsupported key algorithm: " + algorithm);
        }

        @NonNull
        public static String fromKeymasterAsymmetricKeyAlgorithm(int keymasterAlgorithm) {
            if (keymasterAlgorithm == 1) {
                return HwKeyProperties.KEY_ALGORITHM_RSA;
            }
            if (keymasterAlgorithm == 3) {
                return HwKeyProperties.KEY_ALGORITHM_EC;
            }
            throw new IllegalArgumentException("Unsupported key algorithm: " + keymasterAlgorithm);
        }

        public static int toKeymasterSecretKeyAlgorithm(@NonNull String algorithm) {
            if (HwKeyProperties.KEY_ALGORITHM_AES.equalsIgnoreCase(algorithm)) {
                return 32;
            }
            if (algorithm.toUpperCase(Locale.US).startsWith("HMAC")) {
                return HwKeymasterDefs.KM_ALGORITHM_HMAC;
            }
            throw new IllegalArgumentException("Unsupported secret key algorithm: " + algorithm);
        }

        @NonNull
        public static String fromKeymasterSecretKeyAlgorithm(int keymasterAlgorithm, int keymasterDigest) {
            if (keymasterAlgorithm == 32) {
                return HwKeyProperties.KEY_ALGORITHM_AES;
            }
            if (keymasterAlgorithm == 128) {
                switch (keymasterDigest) {
                    case 2:
                        return HwKeyProperties.KEY_ALGORITHM_HMAC_SHA1;
                    case 3:
                        return HwKeyProperties.KEY_ALGORITHM_HMAC_SHA224;
                    case 4:
                        return HwKeyProperties.KEY_ALGORITHM_HMAC_SHA256;
                    case 5:
                        return HwKeyProperties.KEY_ALGORITHM_HMAC_SHA384;
                    case 6:
                        return HwKeyProperties.KEY_ALGORITHM_HMAC_SHA512;
                    default:
                        throw new IllegalArgumentException("Unsupported HMAC digest: " + Digest.fromKeymaster(keymasterDigest));
                }
            } else {
                throw new IllegalArgumentException("Unsupported key algorithm: " + keymasterAlgorithm);
            }
        }

        public static int toKeymasterDigest(@NonNull String algorithm) {
            String algorithmUpper = algorithm.toUpperCase(Locale.US);
            char c = 65535;
            if (!algorithmUpper.startsWith("HMAC")) {
                return -1;
            }
            String digestUpper = algorithmUpper.substring("HMAC".length());
            switch (digestUpper.hashCode()) {
                case -1850268184:
                    if (digestUpper.equals("SHA224")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1850268089:
                    if (digestUpper.equals("SHA256")) {
                        c = 2;
                        break;
                    }
                    break;
                case -1850267037:
                    if (digestUpper.equals("SHA384")) {
                        c = 3;
                        break;
                    }
                    break;
                case -1850265334:
                    if (digestUpper.equals("SHA512")) {
                        c = 4;
                        break;
                    }
                    break;
                case 2543909:
                    if (digestUpper.equals("SHA1")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    return 2;
                case 1:
                    return 3;
                case 2:
                    return 4;
                case 3:
                    return 5;
                case 4:
                    return 6;
                default:
                    throw new IllegalArgumentException("Unsupported HMAC digest: " + digestUpper);
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface KeyAlgorithmEnum {
    }

    public static abstract class Origin {
        private Origin() {
        }

        public static int fromKeymaster(int origin) {
            if (origin == 0) {
                return 1;
            }
            switch (origin) {
                case 2:
                    return 2;
                case 3:
                    return 4;
                default:
                    throw new IllegalArgumentException("Unknown origin: " + origin);
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface OriginEnum {
    }

    public static abstract class Purpose {
        private Purpose() {
        }

        public static int toKeymaster(int purpose) {
            if (purpose == 4) {
                return 2;
            }
            if (purpose == 8) {
                return 3;
            }
            if (purpose == 16) {
                return HwKeymasterDefs.KM_PURPOSE_SOTER_ATTEST_KEY;
            }
            switch (purpose) {
                case 1:
                    return 0;
                case 2:
                    return 1;
                default:
                    throw new IllegalArgumentException("Unknown purpose: " + purpose);
            }
        }

        public static int fromKeymaster(int purpose) {
            if (purpose == -16711679) {
                return 16;
            }
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

        @NonNull
        public static int[] allToKeymaster(int purposes) {
            int[] result = HwKeyProperties.getSetFlags(purposes);
            for (int i = 0; i < result.length; i++) {
                result[i] = toKeymaster(result[i]);
            }
            return result;
        }

        public static int allFromKeymaster(@NonNull Collection<Integer> purposes) {
            int result = 0;
            for (Integer intValue : purposes) {
                result |= fromKeymaster(intValue.intValue());
            }
            return result;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PurposeEnum {
    }

    public static abstract class SignaturePadding {
        private SignaturePadding() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x0044  */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x0046  */
        static int toKeymaster(@NonNull String padding) {
            char c;
            String upperCase = padding.toUpperCase(Locale.US);
            int hashCode = upperCase.hashCode();
            if (hashCode == 79536) {
                if (upperCase.equals(HwKeyProperties.SIGNATURE_PADDING_RSA_PSS)) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                    }
                }
            } else if (hashCode == 76183014 && upperCase.equals(HwKeyProperties.SIGNATURE_PADDING_RSA_PKCS1)) {
                c = 0;
                switch (c) {
                    case 0:
                        return 5;
                    case 1:
                        return 3;
                    default:
                        throw new IllegalArgumentException("Unsupported signature padding scheme: " + padding);
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
        }

        @NonNull
        static String fromKeymaster(int padding) {
            if (padding == 3) {
                return HwKeyProperties.SIGNATURE_PADDING_RSA_PSS;
            }
            if (padding == 5) {
                return HwKeyProperties.SIGNATURE_PADDING_RSA_PKCS1;
            }
            throw new IllegalArgumentException("Unsupported signature padding: " + padding);
        }

        @NonNull
        public static int[] allToKeymaster(@Nullable String[] paddings) {
            if (paddings == null || paddings.length == 0) {
                return ArrayUtils.EmptyArray.INT;
            }
            int[] result = new int[paddings.length];
            for (int i = 0; i < paddings.length; i++) {
                result[i] = toKeymaster(paddings[i]);
            }
            return result;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SignaturePaddingEnum {
    }

    private HwKeyProperties() {
    }

    /* access modifiers changed from: private */
    public static int[] getSetFlags(int flags) {
        if (flags == 0) {
            return ArrayUtils.EmptyArray.INT;
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
        int result = 0;
        if (value == 0) {
            return 0;
        }
        while (value != 0) {
            if ((value & 1) != 0) {
                result++;
            }
            value >>>= 1;
        }
        return result;
    }
}
