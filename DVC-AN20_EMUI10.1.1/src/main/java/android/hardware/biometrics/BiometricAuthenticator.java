package android.hardware.biometrics;

import android.os.CancellationSignal;
import android.os.Parcelable;
import java.util.concurrent.Executor;

public interface BiometricAuthenticator {
    public static final int TYPE_FACE = 4;
    public static final int TYPE_FINGERPRINT = 1;
    public static final int TYPE_IRIS = 2;
    public static final int TYPE_NONE = 0;

    public static abstract class Identifier implements Parcelable {
        private int mBiometricId;
        private long mDeviceId;
        private CharSequence mName;

        public Identifier() {
        }

        public Identifier(CharSequence name, int biometricId, long deviceId) {
            this.mName = name;
            this.mBiometricId = biometricId;
            this.mDeviceId = deviceId;
        }

        public CharSequence getName() {
            return this.mName;
        }

        public int getBiometricId() {
            return this.mBiometricId;
        }

        public long getDeviceId() {
            return this.mDeviceId;
        }

        public void setName(CharSequence name) {
            this.mName = name;
        }

        public void setDeviceId(long deviceId) {
            this.mDeviceId = deviceId;
        }
    }

    public static class AuthenticationResult {
        private CryptoObject mCryptoObject;
        private Identifier mIdentifier;
        private int mUserId;

        public AuthenticationResult() {
        }

        public AuthenticationResult(CryptoObject crypto, Identifier identifier, int userId) {
            this.mCryptoObject = crypto;
            this.mIdentifier = identifier;
            this.mUserId = userId;
        }

        public CryptoObject getCryptoObject() {
            return this.mCryptoObject;
        }

        public Identifier getId() {
            return this.mIdentifier;
        }

        public int getUserId() {
            return this.mUserId;
        }
    }

    public static abstract class AuthenticationCallback {
        public void onAuthenticationError(int errorCode, CharSequence errString) {
        }

        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        }

        public void onAuthenticationFailed() {
        }

        public void onAuthenticationAcquired(int acquireInfo) {
        }
    }

    default boolean isHardwareDetected() {
        throw new UnsupportedOperationException("Stub!");
    }

    default boolean hasEnrolledTemplates() {
        throw new UnsupportedOperationException("Stub!");
    }

    default boolean hasEnrolledTemplates(int userId) {
        throw new UnsupportedOperationException("Stub!");
    }

    default void setActiveUser(int userId) {
        throw new UnsupportedOperationException("Stub!");
    }

    default void authenticate(CryptoObject crypto, CancellationSignal cancel, Executor executor, AuthenticationCallback callback) {
        throw new UnsupportedOperationException("Stub!");
    }

    default void authenticate(CancellationSignal cancel, Executor executor, AuthenticationCallback callback) {
        throw new UnsupportedOperationException("Stub!");
    }
}
