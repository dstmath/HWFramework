package com.huawei.android.biometric;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.fingerprint.FingerprintManager;
import com.android.server.biometrics.AuthenticationClient;
import java.util.ArrayList;
import vendor.huawei.hardware.biometrics.fingerprint.V2_2.IFidoAuthenticationCallback;

public class AuthenticationClientEx extends AuthenticationClient {
    protected BiometricServiceReceiverListenerEx fidoServiceReceiverListenerEx = new BiometricServiceReceiverListenerEx();
    private IFidoAuthenticationCallback mFidoAuthenticationCallback = new IFidoAuthenticationCallback.Stub() {
        /* class com.huawei.android.biometric.AuthenticationClientEx.AnonymousClass1 */

        @Override // vendor.huawei.hardware.biometrics.fingerprint.V2_1.IFidoAuthenticationCallback
        public void onUserVerificationResult(int result, long opId, ArrayList<Byte> userId, ArrayList<Byte> encapsulatedResult) {
            AuthenticationClientEx.this.onUserVerificationResult(result, opId, userId, encapsulatedResult);
        }
    };

    public AuthenticationClientEx(ClientMonitorParameterEx clientMonitorParameterEx) {
        super(clientMonitorParameterEx.getContext(), clientMonitorParameterEx.getConstants().getConstants(), clientMonitorParameterEx.getDaemon().getDaemonWrapper(), clientMonitorParameterEx.getHalDeviceId(), clientMonitorParameterEx.getToken(), clientMonitorParameterEx.getListener().getServiceListener(), clientMonitorParameterEx.getTargetUserId(), clientMonitorParameterEx.getGroupId(), clientMonitorParameterEx.getOpId(), clientMonitorParameterEx.isRestricted(), clientMonitorParameterEx.getOwner(), clientMonitorParameterEx.getCookie(), clientMonitorParameterEx.isRequireConfirmation());
        this.fidoServiceReceiverListenerEx.setFidoAuthenticationCallback(this.mFidoAuthenticationCallback);
        this.fidoServiceReceiverListenerEx.setAuthenticatorListener(clientMonitorParameterEx.getListener().getAuthenticatorListener());
        this.fidoServiceReceiverListenerEx.setFingerprintServiceReceiver(clientMonitorParameterEx.getListener().getFingerprintServiceReceiver());
    }

    public void onUserVerificationResult(int result, long opId, ArrayList<Byte> arrayList, ArrayList<Byte> arrayList2) {
    }

    public boolean onError(long deviceId, int error, int vendorCode) {
        return AuthenticationClientEx.super.onError(deviceId, error, vendorCode);
    }

    public boolean wasUserDetected() {
        return false;
    }

    public boolean onAuthenticatedEx(BiometricAuthenticatorEx identifier, boolean isAuthenticated, ArrayList<Byte> arrayList) {
        return false;
    }

    public boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean isAuthenticated, ArrayList<Byte> token) {
        BiometricAuthenticatorEx biometricAuthenticatorEx = new BiometricAuthenticatorEx();
        biometricAuthenticatorEx.setIdentifier(identifier);
        onAuthenticatedEx(biometricAuthenticatorEx, isAuthenticated, token);
        return AuthenticationClientEx.super.onAuthenticated(identifier, isAuthenticated, token);
    }

    public int getFlags() {
        return this.mFlags;
    }

    public boolean isScreenOn(Context context) {
        return AuthenticationClientEx.super.isScreenOn(context);
    }

    public String getErrorString(int error, int vendorCode) {
        return FingerprintManager.getErrorString(getContext(), error, vendorCode);
    }

    public String getAcquiredString(int acquireInfo, int vendorCode) {
        return FingerprintManager.getAcquiredString(getContext(), acquireInfo, vendorCode);
    }

    public int getBiometricType() {
        return 1;
    }

    public boolean shouldFrameworkHandleLockout() {
        return true;
    }

    /* access modifiers changed from: protected */
    public int statsModality() {
        return 0;
    }

    public int handleFailedAttempt() {
        return 0;
    }

    public void resetFailedAttempts() {
        AuthenticationClientEx.super.resetFailedAttempts();
    }

    public void notifyUserActivity() {
    }

    public void handleHwFailedAttempt(int flags, String packagesName) {
        AuthenticationClientEx.super.handleHwFailedAttempt(0, (String) null);
    }

    public boolean inLockoutMode() {
        return AuthenticationClientEx.super.inLockoutMode();
    }

    public int start() {
        return AuthenticationClientEx.super.start();
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public int stop(boolean isInitiatedByClient) {
        return AuthenticationClientEx.super.stop(isInitiatedByClient);
    }

    public long getHalDeviceIdEx() {
        return AuthenticationClientEx.super.getHalDeviceId();
    }
}
