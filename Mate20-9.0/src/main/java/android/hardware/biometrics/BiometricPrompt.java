package android.hardware.biometrics;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.biometrics.IBiometricPromptReceiver;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.TextUtils;
import java.security.Signature;
import java.util.concurrent.Executor;
import javax.crypto.Cipher;
import javax.crypto.Mac;

public class BiometricPrompt implements BiometricAuthenticator, BiometricConstants {
    public static final int DISMISSED_REASON_NEGATIVE = 2;
    public static final int DISMISSED_REASON_POSITIVE = 1;
    public static final int DISMISSED_REASON_USER_CANCEL = 3;
    public static final int HIDE_DIALOG_DELAY = 2000;
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_NEGATIVE_TEXT = "negative_text";
    public static final String KEY_POSITIVE_TEXT = "positive_text";
    public static final String KEY_SUBTITLE = "subtitle";
    public static final String KEY_TITLE = "title";
    private Bundle mBundle;
    IBiometricPromptReceiver mDialogReceiver;
    private FingerprintManager mFingerprintManager;
    /* access modifiers changed from: private */
    public ButtonInfo mNegativeButtonInfo;
    private PackageManager mPackageManager;
    /* access modifiers changed from: private */
    public ButtonInfo mPositiveButtonInfo;

    public static abstract class AuthenticationCallback extends BiometricAuthenticator.AuthenticationCallback {
        public void onAuthenticationError(int errorCode, CharSequence errString) {
        }

        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        }

        public void onAuthenticationSucceeded(AuthenticationResult result) {
        }

        public void onAuthenticationFailed() {
        }

        public void onAuthenticationAcquired(int acquireInfo) {
        }

        public void onAuthenticationSucceeded(BiometricAuthenticator.AuthenticationResult result) {
            onAuthenticationSucceeded(new AuthenticationResult((CryptoObject) result.getCryptoObject(), result.getId(), result.getUserId()));
        }
    }

    public static class AuthenticationResult extends BiometricAuthenticator.AuthenticationResult {
        public AuthenticationResult(CryptoObject crypto, BiometricAuthenticator.BiometricIdentifier identifier, int userId) {
            super(crypto, identifier, userId);
        }

        public CryptoObject getCryptoObject() {
            return (CryptoObject) super.getCryptoObject();
        }
    }

    public static class Builder {
        private final Bundle mBundle = new Bundle();
        private Context mContext;
        private ButtonInfo mNegativeButtonInfo;
        private ButtonInfo mPositiveButtonInfo;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setTitle(CharSequence title) {
            this.mBundle.putCharSequence("title", title);
            return this;
        }

        public Builder setSubtitle(CharSequence subtitle) {
            this.mBundle.putCharSequence(BiometricPrompt.KEY_SUBTITLE, subtitle);
            return this;
        }

        public Builder setDescription(CharSequence description) {
            this.mBundle.putCharSequence("description", description);
            return this;
        }

        public Builder setPositiveButton(CharSequence text, Executor executor, DialogInterface.OnClickListener listener) {
            if (TextUtils.isEmpty(text)) {
                throw new IllegalArgumentException("Text must be set and non-empty");
            } else if (executor == null) {
                throw new IllegalArgumentException("Executor must not be null");
            } else if (listener != null) {
                this.mBundle.putCharSequence(BiometricPrompt.KEY_POSITIVE_TEXT, text);
                this.mPositiveButtonInfo = new ButtonInfo(executor, listener);
                return this;
            } else {
                throw new IllegalArgumentException("Listener must not be null");
            }
        }

        public Builder setNegativeButton(CharSequence text, Executor executor, DialogInterface.OnClickListener listener) {
            if (TextUtils.isEmpty(text)) {
                throw new IllegalArgumentException("Text must be set and non-empty");
            } else if (executor == null) {
                throw new IllegalArgumentException("Executor must not be null");
            } else if (listener != null) {
                this.mBundle.putCharSequence(BiometricPrompt.KEY_NEGATIVE_TEXT, text);
                this.mNegativeButtonInfo = new ButtonInfo(executor, listener);
                return this;
            } else {
                throw new IllegalArgumentException("Listener must not be null");
            }
        }

        public BiometricPrompt build() {
            CharSequence title = this.mBundle.getCharSequence("title");
            CharSequence negative = this.mBundle.getCharSequence(BiometricPrompt.KEY_NEGATIVE_TEXT);
            if (TextUtils.isEmpty(title)) {
                throw new IllegalArgumentException("Title must be set and non-empty");
            } else if (!TextUtils.isEmpty(negative)) {
                BiometricPrompt biometricPrompt = new BiometricPrompt(this.mContext, this.mBundle, this.mPositiveButtonInfo, this.mNegativeButtonInfo);
                return biometricPrompt;
            } else {
                throw new IllegalArgumentException("Negative text must be set and non-empty");
            }
        }
    }

    private static class ButtonInfo {
        Executor executor;
        DialogInterface.OnClickListener listener;

        ButtonInfo(Executor ex, DialogInterface.OnClickListener l) {
            this.executor = ex;
            this.listener = l;
        }
    }

    public static final class CryptoObject extends CryptoObject {
        public CryptoObject(Signature signature) {
            super(signature);
        }

        public CryptoObject(Cipher cipher) {
            super(cipher);
        }

        public CryptoObject(Mac mac) {
            super(mac);
        }

        public Signature getSignature() {
            return super.getSignature();
        }

        public Cipher getCipher() {
            return super.getCipher();
        }

        public Mac getMac() {
            return super.getMac();
        }
    }

    private BiometricPrompt(Context context, Bundle bundle, ButtonInfo positiveButtonInfo, ButtonInfo negativeButtonInfo) {
        this.mDialogReceiver = new IBiometricPromptReceiver.Stub() {
            public void onDialogDismissed(int reason) {
                if (reason == 1) {
                    BiometricPrompt.this.mPositiveButtonInfo.executor.execute(new Runnable() {
                        public final void run() {
                            BiometricPrompt.this.mPositiveButtonInfo.listener.onClick(null, -1);
                        }
                    });
                } else if (reason == 2) {
                    BiometricPrompt.this.mNegativeButtonInfo.executor.execute(new Runnable() {
                        public final void run() {
                            BiometricPrompt.this.mNegativeButtonInfo.listener.onClick(null, -2);
                        }
                    });
                }
            }
        };
        this.mBundle = bundle;
        this.mPositiveButtonInfo = positiveButtonInfo;
        this.mNegativeButtonInfo = negativeButtonInfo;
        this.mFingerprintManager = (FingerprintManager) context.getSystemService(FingerprintManager.class);
        this.mPackageManager = context.getPackageManager();
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, Executor executor, BiometricAuthenticator.AuthenticationCallback callback) {
        if (callback instanceof AuthenticationCallback) {
            authenticate(crypto, cancel, executor, (BiometricAuthenticator.AuthenticationCallback) (AuthenticationCallback) callback);
            return;
        }
        throw new IllegalArgumentException("Callback cannot be casted");
    }

    public void authenticate(CancellationSignal cancel, Executor executor, BiometricAuthenticator.AuthenticationCallback callback) {
        if (callback instanceof AuthenticationCallback) {
            authenticate(cancel, executor, (AuthenticationCallback) callback);
            return;
        }
        throw new IllegalArgumentException("Callback cannot be casted");
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, Executor executor, AuthenticationCallback callback) {
        if (!handlePreAuthenticationErrors(callback, executor)) {
            this.mFingerprintManager.authenticate((CryptoObject) crypto, cancel, this.mBundle, executor, this.mDialogReceiver, (BiometricAuthenticator.AuthenticationCallback) callback);
        }
    }

    public void authenticate(CancellationSignal cancel, Executor executor, AuthenticationCallback callback) {
        if (!handlePreAuthenticationErrors(callback, executor)) {
            this.mFingerprintManager.authenticate(cancel, this.mBundle, executor, this.mDialogReceiver, (BiometricAuthenticator.AuthenticationCallback) callback);
        }
    }

    private boolean handlePreAuthenticationErrors(AuthenticationCallback callback, Executor executor) {
        if (!this.mPackageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            sendError(12, callback, executor);
            return true;
        } else if (!this.mFingerprintManager.isHardwareDetected()) {
            sendError(1, callback, executor);
            return true;
        } else if (this.mFingerprintManager.hasEnrolledFingerprints()) {
            return false;
        } else {
            sendError(11, callback, executor);
            return true;
        }
    }

    private void sendError(int error, AuthenticationCallback callback, Executor executor) {
        executor.execute(new Runnable(callback, error) {
            private final /* synthetic */ BiometricPrompt.AuthenticationCallback f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                this.f$1.onAuthenticationError(this.f$2, BiometricPrompt.this.mFingerprintManager.getErrorString(this.f$2, 0));
            }
        });
    }
}
