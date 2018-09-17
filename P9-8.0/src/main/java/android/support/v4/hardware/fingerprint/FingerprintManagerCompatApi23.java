package android.support.v4.hardware.fingerprint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.Mac;

@RequiresApi(23)
@RestrictTo({Scope.LIBRARY_GROUP})
public final class FingerprintManagerCompatApi23 {

    public static abstract class AuthenticationCallback {
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
        }

        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        }

        public void onAuthenticationSucceeded(AuthenticationResultInternal result) {
        }

        public void onAuthenticationFailed() {
        }
    }

    public static final class AuthenticationResultInternal {
        private CryptoObject mCryptoObject;

        public AuthenticationResultInternal(CryptoObject crypto) {
            this.mCryptoObject = crypto;
        }

        public CryptoObject getCryptoObject() {
            return this.mCryptoObject;
        }
    }

    public static class CryptoObject {
        private final Cipher mCipher;
        private final Mac mMac;
        private final Signature mSignature;

        public CryptoObject(Signature signature) {
            this.mSignature = signature;
            this.mCipher = null;
            this.mMac = null;
        }

        public CryptoObject(Cipher cipher) {
            this.mCipher = cipher;
            this.mSignature = null;
            this.mMac = null;
        }

        public CryptoObject(Mac mac) {
            this.mMac = mac;
            this.mCipher = null;
            this.mSignature = null;
        }

        public Signature getSignature() {
            return this.mSignature;
        }

        public Cipher getCipher() {
            return this.mCipher;
        }

        public Mac getMac() {
            return this.mMac;
        }
    }

    private static FingerprintManager getFingerprintManagerOrNull(Context context) {
        if (context.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
            return (FingerprintManager) context.getSystemService(FingerprintManager.class);
        }
        return null;
    }

    @SuppressLint({"MissingPermission"})
    static boolean hasEnrolledFingerprints(Context context) {
        FingerprintManager fp = getFingerprintManagerOrNull(context);
        return fp != null ? fp.hasEnrolledFingerprints() : false;
    }

    @SuppressLint({"MissingPermission"})
    static boolean isHardwareDetected(Context context) {
        FingerprintManager fp = getFingerprintManagerOrNull(context);
        return fp != null ? fp.isHardwareDetected() : false;
    }

    @SuppressLint({"MissingPermission"})
    static void authenticate(Context context, CryptoObject crypto, int flags, Object cancel, AuthenticationCallback callback, Handler handler) {
        FingerprintManager fp = getFingerprintManagerOrNull(context);
        if (fp != null) {
            fp.authenticate(wrapCryptoObject(crypto), (CancellationSignal) cancel, flags, wrapCallback(callback), handler);
        }
    }

    private static android.hardware.fingerprint.FingerprintManager.CryptoObject wrapCryptoObject(CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        }
        if (cryptoObject.getCipher() != null) {
            return new android.hardware.fingerprint.FingerprintManager.CryptoObject(cryptoObject.getCipher());
        }
        if (cryptoObject.getSignature() != null) {
            return new android.hardware.fingerprint.FingerprintManager.CryptoObject(cryptoObject.getSignature());
        }
        if (cryptoObject.getMac() != null) {
            return new android.hardware.fingerprint.FingerprintManager.CryptoObject(cryptoObject.getMac());
        }
        return null;
    }

    private static CryptoObject unwrapCryptoObject(android.hardware.fingerprint.FingerprintManager.CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        }
        if (cryptoObject.getCipher() != null) {
            return new CryptoObject(cryptoObject.getCipher());
        }
        if (cryptoObject.getSignature() != null) {
            return new CryptoObject(cryptoObject.getSignature());
        }
        if (cryptoObject.getMac() != null) {
            return new CryptoObject(cryptoObject.getMac());
        }
        return null;
    }

    private static android.hardware.fingerprint.FingerprintManager.AuthenticationCallback wrapCallback(final AuthenticationCallback callback) {
        return new android.hardware.fingerprint.FingerprintManager.AuthenticationCallback() {
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                callback.onAuthenticationError(errMsgId, errString);
            }

            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                callback.onAuthenticationHelp(helpMsgId, helpString);
            }

            public void onAuthenticationSucceeded(AuthenticationResult result) {
                callback.onAuthenticationSucceeded(new AuthenticationResultInternal(FingerprintManagerCompatApi23.unwrapCryptoObject(result.getCryptoObject())));
            }

            public void onAuthenticationFailed() {
                callback.onAuthenticationFailed();
            }
        };
    }
}
