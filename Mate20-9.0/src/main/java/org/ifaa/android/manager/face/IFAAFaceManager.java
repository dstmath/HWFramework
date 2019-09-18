package org.ifaa.android.manager.face;

public abstract class IFAAFaceManager {

    public static abstract class AuthenticatorCallback {
        public void onAuthenticationError(int errorCode) {
        }

        public void onAuthenticationStatus(int status) {
        }

        public void onAuthenticationSucceeded() {
        }

        public void onAuthenticationFailed(int errCode) {
        }
    }

    public abstract void authenticate(int i, int i2, AuthenticatorCallback authenticatorCallback);

    public abstract int cancel(int i);

    public abstract int getVersion();
}
