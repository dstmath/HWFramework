package com.huawei.security.keymaster;

import com.huawei.security.keystore.ArrayUtils;
import com.huawei.security.keystore.HwKeyGenParameterSpec;
import com.huawei.security.keystore.HwKeyProperties;
import java.security.ProviderException;

public abstract class HwKeymasterUtils {
    private static final int AES_GCM_MIN_SUPPORTED_TAG_LENGTH_BITS = 96;
    public static final long INVALID_SECURE_USER_ID = 0;

    private HwKeymasterUtils() {
    }

    public static int getDigestOutputSizeBits(int keymasterDigest) {
        switch (keymasterDigest) {
            case 0:
                return -1;
            case 1:
                return 128;
            case 2:
                return 160;
            case 3:
                return 224;
            case 4:
            case 7:
                return 256;
            case 5:
                return 384;
            case 6:
                return 512;
            default:
                throw new IllegalArgumentException("Unknown digest: " + keymasterDigest);
        }
    }

    public static void addUserAuthArgs(HwKeymasterArguments args, HwKeyGenParameterSpec spec, long boundToSpecificSecureUserId) {
        if (!spec.isUserAuthenticationRequired()) {
            args.addBoolean(HwKeymasterDefs.KM_TAG_NO_AUTH_REQUIRED);
        } else if (spec.getUserAuthenticationValidityDurationSeconds() == -1) {
            long sid = 0;
            if (boundToSpecificSecureUserId != 0) {
                sid = boundToSpecificSecureUserId;
            }
            args.addUnsignedLong(HwKeymasterDefs.KM_TAG_USER_SECURE_ID, HwKeymasterArguments.toUint64(sid));
            args.addEnum(HwKeymasterDefs.KM_TAG_USER_AUTH_TYPE, 2);
            if (spec.isUserAuthenticationValidWhileOnBody()) {
                throw new ProviderException("Key validity extension while device is on-body is not supported for keys requiring fingerprint authentication");
            }
        } else {
            long sid2 = 0;
            if (boundToSpecificSecureUserId != 0) {
                sid2 = boundToSpecificSecureUserId;
            }
            args.addUnsignedLong(HwKeymasterDefs.KM_TAG_USER_SECURE_ID, HwKeymasterArguments.toUint64(sid2));
            args.addEnum(HwKeymasterDefs.KM_TAG_USER_AUTH_TYPE, 2);
            args.addUnsignedInt(HwKeymasterDefs.KM_TAG_AUTH_TIMEOUT, (long) spec.getUserAuthenticationValidityDurationSeconds());
            if (spec.isUserAuthenticationValidWhileOnBody()) {
                args.addBoolean(HwKeymasterDefs.KM_TAG_ALLOW_WHILE_ON_BODY);
            }
        }
    }

    public static boolean isKeymasterBlockModeValidCompatibleWithSymmetricCrypto(int keymasterBlockMode) {
        if (keymasterBlockMode == 1) {
            return false;
        }
        if (keymasterBlockMode == 2 || keymasterBlockMode == 3 || keymasterBlockMode == 32) {
            return true;
        }
        throw new IllegalArgumentException("Unsupported block mode: " + keymasterBlockMode);
    }

    public static void addMinMacLengthAuthorizationIfNecessary(HwKeymasterArguments args, int keymasterAlgorithm, int[] keymasterBlockModes, int[] keymasterDigests) {
        if (keymasterAlgorithm != 32) {
            if (keymasterAlgorithm == 128) {
                if (keymasterDigests.length == 1) {
                    int keymasterDigest = keymasterDigests[0];
                    int digestOutputSizeBits = getDigestOutputSizeBits(keymasterDigest);
                    if (digestOutputSizeBits != -1) {
                        args.addUnsignedInt(HwKeymasterDefs.KM_TAG_MIN_MAC_LENGTH, (long) digestOutputSizeBits);
                        return;
                    }
                    throw new ProviderException("HMAC key authorized for unsupported digest: " + HwKeyProperties.Digest.fromKeymaster(keymasterDigest));
                }
                throw new ProviderException("Unsupported number of authorized digests for HMAC key: " + keymasterDigests.length + ". Exactly one digest must be authorized");
            }
        } else if (ArrayUtils.contains(keymasterBlockModes, 32)) {
            args.addUnsignedInt(HwKeymasterDefs.KM_TAG_MIN_MAC_LENGTH, 96);
        }
    }
}
