package android.hardware.biometrics;

import android.os.CancellationSignal;
import android.os.Parcelable;
import java.util.concurrent.Executor;

public interface BiometricAuthenticator {

    public static abstract class AuthenticationCallback {
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
    }

    public static class AuthenticationResult {
        private CryptoObject mCryptoObject;
        private BiometricIdentifier mIdentifier;
        private int mUserId;

        public AuthenticationResult() {
        }

        public AuthenticationResult(CryptoObject crypto, BiometricIdentifier identifier, int userId) {
            this.mCryptoObject = crypto;
            this.mIdentifier = identifier;
            this.mUserId = userId;
        }

        public CryptoObject getCryptoObject() {
            return this.mCryptoObject;
        }

        public BiometricIdentifier getId() {
            return this.mIdentifier;
        }

        public int getUserId() {
            return this.mUserId;
        }
    }

    public static abstract class BiometricIdentifier implements Parcelable {
    }

    void authenticate(CryptoObject cryptoObject, CancellationSignal cancellationSignal, Executor executor, AuthenticationCallback authenticationCallback);

    void authenticate(CancellationSignal cancellationSignal, Executor executor, AuthenticationCallback authenticationCallback);
}
