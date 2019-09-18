package com.huawei.security.keymaster;

import java.security.ProviderException;

public abstract class HwKeymasterUtils {
    public static final long INVALID_SECURE_USER_ID = 0;

    private HwKeymasterUtils() {
    }

    public static int getDigestOutputSizeBits(int keymasterDigest) {
        switch (keymasterDigest) {
            case 0:
                return -1;
            case 1:
                return HwKeymasterDefs.KM_ALGORITHM_HMAC;
            case 2:
                return 160;
            case 3:
                return 224;
            case 4:
                return 256;
            case 5:
                return 384;
            case 6:
                return 512;
            default:
                throw new IllegalArgumentException("Unknown digest: " + keymasterDigest);
        }
    }

    public static void addUserAuthArgs(HwKeymasterArguments args, boolean userAuthenticationRequired, int userAuthenticationValidityDurationSeconds, boolean userAuthenticationValidWhileOnBody, boolean invalidatedByBiometricEnrollment, long boundToSpecificSecureUserId) {
        if (!userAuthenticationRequired) {
            args.addBoolean(HwKeymasterDefs.KM_TAG_NO_AUTH_REQUIRED);
            return;
        }
        if (userAuthenticationValidityDurationSeconds == -1) {
            long sid = 0;
            if (boundToSpecificSecureUserId != 0) {
                sid = boundToSpecificSecureUserId;
            }
            args.addUnsignedLong(HwKeymasterDefs.KM_TAG_USER_SECURE_ID, HwKeymasterArguments.toUint64(sid));
            args.addEnum(HwKeymasterDefs.KM_TAG_USER_AUTH_TYPE, 2);
            if (userAuthenticationValidWhileOnBody) {
                throw new ProviderException("Key validity extension while device is on-body is not supported for keys requiring fingerprint authentication");
            }
        } else {
            long sid2 = 0;
            if (boundToSpecificSecureUserId != 0) {
                sid2 = boundToSpecificSecureUserId;
            }
            args.addUnsignedLong(HwKeymasterDefs.KM_TAG_USER_SECURE_ID, HwKeymasterArguments.toUint64(sid2));
            args.addEnum(HwKeymasterDefs.KM_TAG_USER_AUTH_TYPE, 2);
            args.addUnsignedInt(HwKeymasterDefs.KM_TAG_AUTH_TIMEOUT, (long) userAuthenticationValidityDurationSeconds);
            if (userAuthenticationValidWhileOnBody) {
                args.addBoolean(HwKeymasterDefs.KM_TAG_ALLOW_WHILE_ON_BODY);
            }
        }
    }
}
