package com.huawei.android.biometric;

import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.biometrics.IBiometricServiceReceiver;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.biometrics.BiometricServiceBase;
import com.huawei.fingerprint.IAuthenticatorListener;
import vendor.huawei.hardware.biometrics.fingerprint.V2_2.IFidoAuthenticationCallback;

public class BiometricServiceReceiverListenerEx {
    private static final String TAG_LISTENER = "BiometricServiceReceiverListenerEx";
    private IAuthenticatorListener mAuthenticatorListener;
    private IBiometricServiceLockoutResetCallback mBiometricServiceLockoutResetCallback;
    private IBiometricServiceReceiverInternal mBiometricServiceReceiver;
    private IBiometricServiceReceiver mDialogReceiver;
    private IFidoAuthenticationCallback mFidoAuthenticationCallback;
    private IFingerprintServiceReceiver mFingerprintServiceReceiver;
    private BiometricServiceBase.ServiceListener mServiceListener;

    public BiometricServiceBase.ServiceListener getServiceListener() {
        return this.mServiceListener;
    }

    public void setServiceListener(BiometricServiceBase.ServiceListener serviceListener) {
        this.mServiceListener = serviceListener;
    }

    public IFingerprintServiceReceiver getFingerprintServiceReceiver() {
        return this.mFingerprintServiceReceiver;
    }

    public void setFingerprintServiceReceiver(IFingerprintServiceReceiver fingerprintServiceReceiver) {
        this.mFingerprintServiceReceiver = fingerprintServiceReceiver;
    }

    public IBiometricServiceReceiverInternal getBiometricServiceReceiver() {
        return this.mBiometricServiceReceiver;
    }

    public void setBiometricServiceReceiver(IBiometricServiceReceiverInternal biometricServiceReceiver) {
        this.mBiometricServiceReceiver = biometricServiceReceiver;
    }

    public IBiometricServiceReceiver getDialogReceiver() {
        return this.mDialogReceiver;
    }

    public void setmDialogReceiver(IBiometricServiceReceiver dialogReceiver) {
        this.mDialogReceiver = dialogReceiver;
    }

    public IAuthenticatorListener getAuthenticatorListener() {
        return this.mAuthenticatorListener;
    }

    public void setAuthenticatorListener(IAuthenticatorListener authenticatorListener) {
        this.mAuthenticatorListener = authenticatorListener;
    }

    public IFidoAuthenticationCallback getFidoAuthenticationCallback() {
        return this.mFidoAuthenticationCallback;
    }

    public void setFidoAuthenticationCallback(IFidoAuthenticationCallback fidoAuthenticationCallback) {
        this.mFidoAuthenticationCallback = fidoAuthenticationCallback;
    }

    public IBiometricServiceLockoutResetCallback getBiometricServiceLockoutResetCallback() {
        return this.mBiometricServiceLockoutResetCallback;
    }

    public void setBiometricServiceLockoutResetCallback(IBiometricServiceLockoutResetCallback biometricServiceLockoutResetCallback) {
        this.mBiometricServiceLockoutResetCallback = biometricServiceLockoutResetCallback;
    }

    public void onDialogDismissedDialogReceiver(int reason) {
        try {
            if (this.mDialogReceiver != null) {
                this.mDialogReceiver.onDialogDismissed(reason);
            }
        } catch (RemoteException e) {
            Log.e(TAG_LISTENER, "Failed to onDialogDismissedDialogReceiver");
        }
    }

    public void onErrorFingerprintServiceReceiver(long deviceId, int error, int vendorCode) {
        try {
            if (this.mFingerprintServiceReceiver != null) {
                this.mFingerprintServiceReceiver.onError(deviceId, error, vendorCode);
            }
            if (this.mBiometricServiceReceiver != null && deviceId == 0) {
                this.mBiometricServiceReceiver.onError(vendorCode, error, (String) null);
            }
        } catch (RemoteException e) {
            Log.e(TAG_LISTENER, "Failed to onErrorFingerprintServiceReceiver");
        }
    }

    public void onAcquiredFingerprintServiceReceiver(long deviceId, int acquiredInfo, int vendorCode) {
        try {
            if (this.mFingerprintServiceReceiver != null) {
                this.mFingerprintServiceReceiver.onAcquired(deviceId, acquiredInfo, vendorCode);
            }
            if (this.mBiometricServiceReceiver != null && deviceId == 0) {
                this.mBiometricServiceReceiver.onAcquired(acquiredInfo, (String) null);
            }
        } catch (RemoteException e) {
            Log.e(TAG_LISTENER, "Failed to onAcquiredFingerprintServiceReceiver");
        }
    }

    public void onDialogDismissedBiometricServiceReceiver(int reason) {
        try {
            if (this.mBiometricServiceReceiver != null) {
                this.mBiometricServiceReceiver.onDialogDismissed(reason);
            }
        } catch (RemoteException e) {
            Log.e(TAG_LISTENER, "Failed to onDialogDismissedBiometricServiceReceiver");
        }
    }

    public boolean hasDialogReceiver() {
        if (this.mDialogReceiver != null) {
            return true;
        }
        return false;
    }

    public boolean hasFingerprintServiceReceiver() {
        if (this.mFingerprintServiceReceiver != null) {
            return true;
        }
        return false;
    }

    public boolean hasBiometricServiceReceiver() {
        if (this.mBiometricServiceReceiver != null) {
            return true;
        }
        return false;
    }

    public void onUserVerificationResult(int result, byte[] userid, byte[] encapsulatedResult) {
        try {
            if (this.mAuthenticatorListener != null) {
                this.mAuthenticatorListener.onUserVerificationResult(result, userid, encapsulatedResult);
            }
            if (this.mBiometricServiceReceiver != null && result == 0 && userid == null) {
                Log.i(TAG_LISTENER, "onAuthenticationSucceeded");
                this.mBiometricServiceReceiver.onAuthenticationSucceeded(false, encapsulatedResult);
            }
            if (this.mBiometricServiceReceiver != null && result != 0 && userid == null && encapsulatedResult == null) {
                Log.i(TAG_LISTENER, "onAuthenticationFailed");
                this.mBiometricServiceReceiver.onAuthenticationFailed(result, false);
            }
        } catch (RemoteException e) {
            Log.e(TAG_LISTENER, "Failed to onUserVerificationResult");
        }
    }

    public IBinder asBinder() {
        IFingerprintServiceReceiver iFingerprintServiceReceiver = this.mFingerprintServiceReceiver;
        if (iFingerprintServiceReceiver != null) {
            return iFingerprintServiceReceiver.asBinder();
        }
        return null;
    }
}
