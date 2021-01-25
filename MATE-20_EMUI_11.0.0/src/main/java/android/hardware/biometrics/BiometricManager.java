package android.hardware.biometrics;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Slog;

public class BiometricManager {
    public static final int BIOMETRIC_ERROR_HW_UNAVAILABLE = 1;
    public static final int BIOMETRIC_ERROR_NONE_ENROLLED = 11;
    public static final int BIOMETRIC_ERROR_NO_HARDWARE = 12;
    public static final int BIOMETRIC_SUCCESS = 0;
    private static final String TAG = "BiometricManager";
    private final Context mContext;
    private final boolean mHasHardware;
    private final IBiometricService mService;

    @interface BiometricError {
    }

    public static boolean hasBiometrics(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) || pm.hasSystemFeature(PackageManager.FEATURE_IRIS) || pm.hasSystemFeature(PackageManager.FEATURE_FACE);
    }

    public BiometricManager(Context context, IBiometricService service) {
        this.mContext = context;
        this.mService = service;
        this.mHasHardware = hasBiometrics(context);
    }

    @BiometricError
    public int canAuthenticate() {
        IBiometricService iBiometricService = this.mService;
        if (iBiometricService != null) {
            try {
                return iBiometricService.canAuthenticate(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else if (!this.mHasHardware) {
            return 12;
        } else {
            Slog.w(TAG, "hasEnrolledBiometrics(): Service not connected");
            return 1;
        }
    }

    public void registerEnabledOnKeyguardCallback(IBiometricEnabledOnKeyguardCallback callback) {
        IBiometricService iBiometricService = this.mService;
        if (iBiometricService != null) {
            try {
                iBiometricService.registerEnabledOnKeyguardCallback(callback);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "registerEnabledOnKeyguardCallback(): Service not connected");
        }
    }

    public void setActiveUser(int userId) {
        IBiometricService iBiometricService = this.mService;
        if (iBiometricService != null) {
            try {
                iBiometricService.setActiveUser(userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "setActiveUser(): Service not connected");
        }
    }

    public void resetLockout(byte[] token) {
        IBiometricService iBiometricService = this.mService;
        if (iBiometricService != null) {
            try {
                iBiometricService.resetLockout(token);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "resetLockout(): Service not connected");
        }
    }

    public void onConfirmDeviceCredentialSuccess() {
        IBiometricService iBiometricService = this.mService;
        if (iBiometricService != null) {
            try {
                iBiometricService.onConfirmDeviceCredentialSuccess();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "onConfirmDeviceCredentialSuccess(): Service not connected");
        }
    }

    public void onConfirmDeviceCredentialError(int error, String message) {
        IBiometricService iBiometricService = this.mService;
        if (iBiometricService != null) {
            try {
                iBiometricService.onConfirmDeviceCredentialError(error, message);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "onConfirmDeviceCredentialError(): Service not connected");
        }
    }

    public void registerCancellationCallback(IBiometricConfirmDeviceCredentialCallback callback) {
        IBiometricService iBiometricService = this.mService;
        if (iBiometricService != null) {
            try {
                iBiometricService.registerCancellationCallback(callback);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Slog.w(TAG, "registerCancellationCallback(): Service not connected");
        }
    }
}
