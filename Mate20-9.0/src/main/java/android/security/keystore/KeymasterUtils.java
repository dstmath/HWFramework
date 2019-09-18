package android.security.keystore;

import android.hardware.fingerprint.FingerprintManager;
import android.security.GateKeeper;
import android.security.KeyStore;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties;
import android.util.JlogConstants;
import com.android.internal.util.ArrayUtils;
import java.security.ProviderException;

public abstract class KeymasterUtils {
    private KeymasterUtils() {
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
                return 256;
            case 5:
                return JlogConstants.JLID_ACTIVITY_START_RECORD_TIME;
            case 6:
                return 512;
            default:
                throw new IllegalArgumentException("Unknown digest: " + keymasterDigest);
        }
    }

    public static boolean isKeymasterBlockModeIndCpaCompatibleWithSymmetricCrypto(int keymasterBlockMode) {
        if (keymasterBlockMode != 32) {
            switch (keymasterBlockMode) {
                case 1:
                    return false;
                case 2:
                case 3:
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported block mode: " + keymasterBlockMode);
            }
        }
        return true;
    }

    public static boolean isKeymasterPaddingSchemeIndCpaCompatibleWithAsymmetricCrypto(int keymasterPadding) {
        if (keymasterPadding != 4) {
            switch (keymasterPadding) {
                case 1:
                    return false;
                case 2:
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported asymmetric encryption padding scheme: " + keymasterPadding);
            }
        }
        return true;
    }

    public static void addUserAuthArgs(KeymasterArguments args, UserAuthArgs spec) {
        long sid;
        long sid2;
        if (spec.isUserConfirmationRequired()) {
            args.addBoolean(KeymasterDefs.KM_TAG_TRUSTED_CONFIRMATION_REQUIRED);
        }
        if (spec.isUserPresenceRequired()) {
            args.addBoolean(KeymasterDefs.KM_TAG_TRUSTED_USER_PRESENCE_REQUIRED);
        }
        if (spec.isUnlockedDeviceRequired()) {
            args.addBoolean(KeymasterDefs.KM_TAG_UNLOCKED_DEVICE_REQUIRED);
        }
        if (!spec.isUserAuthenticationRequired()) {
            args.addBoolean(KeymasterDefs.KM_TAG_NO_AUTH_REQUIRED);
            return;
        }
        if (spec.getUserAuthenticationValidityDurationSeconds() == -1) {
            FingerprintManager fingerprintManager = (FingerprintManager) KeyStore.getApplicationContext().getSystemService(FingerprintManager.class);
            long fingerprintOnlySid = fingerprintManager != null ? fingerprintManager.getAuthenticatorId() : 0;
            if (fingerprintOnlySid != 0) {
                if (spec.getBoundToSpecificSecureUserId() != 0) {
                    sid2 = spec.getBoundToSpecificSecureUserId();
                } else if (spec.isInvalidatedByBiometricEnrollment()) {
                    sid2 = fingerprintOnlySid;
                } else {
                    sid2 = getRootSid();
                }
                args.addUnsignedLong(KeymasterDefs.KM_TAG_USER_SECURE_ID, KeymasterArguments.toUint64(sid2));
                args.addEnum(KeymasterDefs.KM_TAG_USER_AUTH_TYPE, 2);
                if (spec.isUserAuthenticationValidWhileOnBody()) {
                    throw new ProviderException("Key validity extension while device is on-body is not supported for keys requiring fingerprint authentication");
                }
            } else {
                throw new IllegalStateException("At least one fingerprint must be enrolled to create keys requiring user authentication for every use");
            }
        } else {
            if (spec.getBoundToSpecificSecureUserId() != 0) {
                sid = spec.getBoundToSpecificSecureUserId();
            } else {
                sid = getRootSid();
            }
            args.addUnsignedLong(KeymasterDefs.KM_TAG_USER_SECURE_ID, KeymasterArguments.toUint64(sid));
            args.addEnum(KeymasterDefs.KM_TAG_USER_AUTH_TYPE, 3);
            args.addUnsignedInt(KeymasterDefs.KM_TAG_AUTH_TIMEOUT, (long) spec.getUserAuthenticationValidityDurationSeconds());
            if (spec.isUserAuthenticationValidWhileOnBody()) {
                args.addBoolean(KeymasterDefs.KM_TAG_ALLOW_WHILE_ON_BODY);
            }
        }
    }

    public static void addMinMacLengthAuthorizationIfNecessary(KeymasterArguments args, int keymasterAlgorithm, int[] keymasterBlockModes, int[] keymasterDigests) {
        if (keymasterAlgorithm != 32) {
            if (keymasterAlgorithm == 128) {
                if (keymasterDigests.length == 1) {
                    int keymasterDigest = keymasterDigests[0];
                    int digestOutputSizeBits = getDigestOutputSizeBits(keymasterDigest);
                    if (digestOutputSizeBits != -1) {
                        args.addUnsignedInt(KeymasterDefs.KM_TAG_MIN_MAC_LENGTH, (long) digestOutputSizeBits);
                        return;
                    }
                    throw new ProviderException("HMAC key authorized for unsupported digest: " + KeyProperties.Digest.fromKeymaster(keymasterDigest));
                }
                throw new ProviderException("Unsupported number of authorized digests for HMAC key: " + keymasterDigests.length + ". Exactly one digest must be authorized");
            }
        } else if (ArrayUtils.contains(keymasterBlockModes, 32)) {
            args.addUnsignedInt(KeymasterDefs.KM_TAG_MIN_MAC_LENGTH, 96);
        }
    }

    private static long getRootSid() {
        long rootSid = GateKeeper.getSecureUserId();
        if (rootSid != 0) {
            return rootSid;
        }
        throw new IllegalStateException("Secure lock screen must be enabled to create keys requiring user authentication");
    }
}
