package android.security.keystore;

import android.hardware.fingerprint.FingerprintManager;
import android.hardware.usb.UsbConstants;
import android.provider.DocumentsContract.Document;
import android.renderscript.Mesh.TriangleMeshBuilder;
import android.security.GateKeeper;
import android.security.KeyStore;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties.Digest;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import com.android.internal.util.ArrayUtils;
import java.security.ProviderException;

public abstract class KeymasterUtils {
    private KeymasterUtils() {
    }

    public static int getDigestOutputSizeBits(int keymasterDigest) {
        switch (keymasterDigest) {
            case TextToSpeech.SUCCESS /*0*/:
                return -1;
            case AudioState.ROUTE_EARPIECE /*1*/:
                return KeymasterDefs.KM_ALGORITHM_HMAC;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                return Const.CODE_G3_RANGE_START;
            case Engine.DEFAULT_STREAM /*3*/:
                return UsbConstants.USB_CLASS_WIRELESS_CONTROLLER;
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                return TriangleMeshBuilder.TEXTURE_0;
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                return 384;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                return Document.FLAG_VIRTUAL_DOCUMENT;
            default:
                throw new IllegalArgumentException("Unknown digest: " + keymasterDigest);
        }
    }

    public static boolean isKeymasterBlockModeIndCpaCompatibleWithSymmetricCrypto(int keymasterBlockMode) {
        switch (keymasterBlockMode) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                return false;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case Engine.DEFAULT_STREAM /*3*/:
            case KeymasterDefs.KM_MODE_GCM /*32*/:
                return true;
            default:
                throw new IllegalArgumentException("Unsupported block mode: " + keymasterBlockMode);
        }
    }

    public static boolean isKeymasterPaddingSchemeIndCpaCompatibleWithAsymmetricCrypto(int keymasterPadding) {
        switch (keymasterPadding) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                return false;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                return true;
            default:
                throw new IllegalArgumentException("Unsupported asymmetric encryption padding scheme: " + keymasterPadding);
        }
    }

    public static void addUserAuthArgs(KeymasterArguments args, boolean userAuthenticationRequired, int userAuthenticationValidityDurationSeconds, boolean userAuthenticationValidWhileOnBody, boolean invalidatedByBiometricEnrollment) {
        if (userAuthenticationRequired) {
            if (userAuthenticationValidityDurationSeconds == -1) {
                FingerprintManager fingerprintManager = (FingerprintManager) KeyStore.getApplicationContext().getSystemService(FingerprintManager.class);
                long fingerprintOnlySid = fingerprintManager != null ? fingerprintManager.getAuthenticatorId() : 0;
                if (fingerprintOnlySid == 0) {
                    throw new IllegalStateException("At least one fingerprint must be enrolled to create keys requiring user authentication for every use");
                }
                long sid;
                if (invalidatedByBiometricEnrollment) {
                    sid = fingerprintOnlySid;
                } else {
                    sid = getRootSid();
                }
                args.addUnsignedLong(KeymasterDefs.KM_TAG_USER_SECURE_ID, KeymasterArguments.toUint64(sid));
                args.addEnum(KeymasterDefs.KM_TAG_USER_AUTH_TYPE, 2);
                if (userAuthenticationValidWhileOnBody) {
                    throw new ProviderException("Key validity extension while device is on-body is not supported for keys requiring fingerprint authentication");
                }
            }
            args.addUnsignedLong(KeymasterDefs.KM_TAG_USER_SECURE_ID, KeymasterArguments.toUint64(getRootSid()));
            args.addEnum(KeymasterDefs.KM_TAG_USER_AUTH_TYPE, 3);
            args.addUnsignedInt(KeymasterDefs.KM_TAG_AUTH_TIMEOUT, (long) userAuthenticationValidityDurationSeconds);
            if (userAuthenticationValidWhileOnBody) {
                args.addBoolean(KeymasterDefs.KM_TAG_ALLOW_WHILE_ON_BODY);
            }
            return;
        }
        args.addBoolean(KeymasterDefs.KM_TAG_NO_AUTH_REQUIRED);
    }

    public static void addMinMacLengthAuthorizationIfNecessary(KeymasterArguments args, int keymasterAlgorithm, int[] keymasterBlockModes, int[] keymasterDigests) {
        switch (keymasterAlgorithm) {
            case KeymasterDefs.KM_MODE_GCM /*32*/:
                if (ArrayUtils.contains(keymasterBlockModes, 32)) {
                    args.addUnsignedInt(KeymasterDefs.KM_TAG_MIN_MAC_LENGTH, 96);
                }
            case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                if (keymasterDigests.length != 1) {
                    throw new ProviderException("Unsupported number of authorized digests for HMAC key: " + keymasterDigests.length + ". Exactly one digest must be authorized");
                }
                int keymasterDigest = keymasterDigests[0];
                int digestOutputSizeBits = getDigestOutputSizeBits(keymasterDigest);
                if (digestOutputSizeBits == -1) {
                    throw new ProviderException("HMAC key authorized for unsupported digest: " + Digest.fromKeymaster(keymasterDigest));
                }
                args.addUnsignedInt(KeymasterDefs.KM_TAG_MIN_MAC_LENGTH, (long) digestOutputSizeBits);
            default:
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
