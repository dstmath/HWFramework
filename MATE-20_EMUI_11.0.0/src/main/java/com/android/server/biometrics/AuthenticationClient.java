package com.android.server.biometrics;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.security.KeyStore;
import android.util.Slog;
import com.android.server.biometrics.BiometricServiceBase;
import java.util.ArrayList;

public abstract class AuthenticationClient extends ClientMonitor {
    private static final String COAUTH_SERVICE_PKG = "com.huawei.coauthservice";
    private static final String KEYGUARD_PACKAGE = "com.android.systemui";
    public static final int LOCKOUT_NONE = 0;
    public static final int LOCKOUT_PERMANENT = 2;
    public static final int LOCKOUT_TIMED = 1;
    protected int mFlags;
    private long mOpId;
    protected String mPackageName;
    private final boolean mRequireConfirmation;
    private boolean mStarted;

    public abstract int handleFailedAttempt();

    public abstract void onStart();

    public abstract void onStop();

    public abstract boolean shouldFrameworkHandleLockout();

    public abstract boolean wasUserDetected();

    public void resetFailedAttempts() {
    }

    public AuthenticationClient(Context context, Constants constants, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int targetUserId, int groupId, long opId, boolean restricted, String owner, int cookie, boolean requireConfirmation) {
        super(context, constants, daemon, halDeviceId, token, listener, targetUserId, groupId, restricted, owner, cookie);
        this.mOpId = opId;
        this.mRequireConfirmation = requireConfirmation;
    }

    @Override // com.android.server.biometrics.ClientMonitor, android.os.IBinder.DeathRecipient
    public void binderDied() {
        super.binderDied();
        stop(false);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.LoggableMonitor
    public int statsAction() {
        return 2;
    }

    public boolean isBiometricPrompt() {
        return getCookie() != 0;
    }

    public boolean getRequireConfirmation() {
        return this.mRequireConfirmation;
    }

    public boolean inLockoutMode() {
        return false;
    }

    public void handleHwFailedAttempt(int flags, String packagesName) {
    }

    /* access modifiers changed from: protected */
    public boolean isScreenOn(Context context) {
        if (context == null) {
            Slog.w(getLogTag(), "input context is null");
            return false;
        }
        PowerManager power = (PowerManager) context.getSystemService("power");
        if (power != null) {
            return power.isScreenOn();
        }
        Slog.w(getLogTag(), "PowerManager is null");
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.LoggableMonitor
    public boolean isCryptoOperation() {
        return this.mOpId != 0;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onError(long deviceId, int error, int vendorCode) {
        if (!shouldFrameworkHandleLockout() && (error == 3 ? wasUserDetected() || isBiometricPrompt() : error == 7 || error == 9) && this.mStarted) {
            vibrateError();
        }
        return super.onError(deviceId, error, vendorCode);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v5, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r2v6, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r2v7, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r2v8, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r2v9, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r2v10, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r2v11, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean authenticated, ArrayList<Byte> token) {
        RemoteException e;
        boolean result;
        int errorCode;
        byte[] byteToken;
        RemoteException e2;
        super.logOnAuthenticated(getContext(), authenticated, this.mRequireConfirmation, getTargetUserId(), isBiometricPrompt());
        BiometricServiceBase.ServiceListener listener = getListener();
        this.mMetricsLogger.action(this.mConstants.actionBiometricAuth(), authenticated);
        try {
            Slog.v(getLogTag(), "onAuthenticated(" + authenticated + "), ID:" + identifier.getBiometricId() + ", Owner: " + getOwnerString() + ", isBP: " + isBiometricPrompt() + ", listener: " + listener + ", requireConfirmation: " + this.mRequireConfirmation + ", user: " + getTargetUserId());
            int i = 0;
            if (authenticated) {
                try {
                    this.mAlreadyDone = true;
                    result = 1;
                    result = 1;
                    result = 1;
                    result = 1;
                    if (shouldFrameworkHandleLockout()) {
                        resetFailedAttempts();
                    }
                    onStop();
                    byteToken = new byte[token.size()];
                    while (i < token.size()) {
                        try {
                            byteToken[i] = token.get(i).byteValue();
                            i++;
                        } catch (RemoteException e3) {
                            e = e3;
                            Slog.e(getLogTag(), "Remote exception", e);
                            return true;
                        }
                    }
                } catch (RemoteException e4) {
                    e = e4;
                    Slog.e(getLogTag(), "Remote exception", e);
                    return true;
                }
                try {
                    if (isBiometricPrompt() && listener != null) {
                        listener.onAuthenticationSucceededInternal(this.mRequireConfirmation, byteToken);
                    } else if (isBiometricPrompt() || listener == null) {
                        Slog.w(getLogTag(), "Client not listening");
                        result = 1;
                    } else {
                        KeyStore.getInstance().addAuthToken(byteToken);
                        try {
                            if (!getIsRestricted()) {
                                try {
                                    listener.onAuthenticationSucceeded(getHalDeviceId(), identifier, getTargetUserId());
                                } catch (RemoteException e5) {
                                    e2 = e5;
                                    Slog.e(getLogTag(), "Remote exception", e2);
                                    notifyUserActivity();
                                    mAcquiredInfo = -1;
                                    return result;
                                }
                            } else {
                                listener.onAuthenticationSucceeded(getHalDeviceId(), null, getTargetUserId());
                            }
                        } catch (RemoteException e6) {
                            e2 = e6;
                            Slog.e(getLogTag(), "Remote exception", e2);
                            notifyUserActivity();
                            mAcquiredInfo = -1;
                            return result;
                        }
                    }
                } catch (RemoteException e7) {
                    e = e7;
                    Slog.e(getLogTag(), "Remote exception", e);
                    return true;
                }
            } else {
                if (listener != null && !isKeyguardOrCoauthservice(this.mPackageName)) {
                    vibrateError();
                }
                int lockoutMode = 0;
                if (isScreenOn(getContext()) && !inLockoutMode() && (lockoutMode = handleFailedAttempt()) != 0 && shouldFrameworkHandleLockout()) {
                    Slog.w(getLogTag(), "Forcing lockout (driver code should do this!), mode(" + lockoutMode + ")");
                    stop(false);
                    if (lockoutMode == 1) {
                        errorCode = 7;
                    } else {
                        errorCode = 9;
                    }
                    if (listener != null) {
                        listener.onError(getHalDeviceId(), errorCode, 0, getCookie());
                    }
                }
                if (lockoutMode == 0 && listener != null) {
                    if (isBiometricPrompt()) {
                        listener.onAuthenticationFailedInternal(getCookie(), getRequireConfirmation());
                    } else {
                        listener.onAuthenticationFailed(getHalDeviceId());
                    }
                }
                if (lockoutMode != 0) {
                    i = 1;
                }
                result = i | 0;
            }
            notifyUserActivity();
            mAcquiredInfo = -1;
            return result;
        } catch (RemoteException e8) {
            e = e8;
            Slog.e(getLogTag(), "Remote exception", e);
            return true;
        }
    }

    private boolean isKeyguardOrCoauthservice(String clientPackage) {
        return isKeyguard(clientPackage) || COAUTH_SERVICE_PKG.equals(clientPackage);
    }

    private boolean isKeyguard(String clientPackage) {
        return KEYGUARD_PACKAGE.equals(clientPackage);
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int start() {
        this.mStarted = true;
        onStart();
        try {
            int result = getDaemonWrapper().authenticate(this.mOpId, getGroupId());
            if (result != 0) {
                String logTag = getLogTag();
                Slog.w(logTag, "startAuthentication failed, result=" + result);
                this.mMetricsLogger.histogram(this.mConstants.tagAuthStartError(), result);
                onError(getHalDeviceId(), 1, 0);
                return result;
            }
            String logTag2 = getLogTag();
            Slog.w(logTag2, "client " + getOwnerString() + " is authenticating...");
            return 0;
        } catch (RemoteException e) {
            Slog.e(getLogTag(), "startAuthentication failed", e);
            return 3;
        }
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int stop(boolean initiatedByClient) {
        if (this.mAlreadyCancelled) {
            Slog.w(getLogTag(), "stopAuthentication: already cancelled!");
            return 0;
        }
        this.mStarted = false;
        onStop();
        try {
            int result = getDaemonWrapper().cancel();
            if (result != 0) {
                String logTag = getLogTag();
                Slog.w(logTag, "stopAuthentication failed, result=" + result);
                return result;
            }
            String logTag2 = getLogTag();
            Slog.w(logTag2, "client " + getOwnerString() + " is no longer authenticating");
            this.mAlreadyCancelled = true;
            return 0;
        } catch (RemoteException e) {
            Slog.e(getLogTag(), "stopAuthentication failed", e);
            return 3;
        }
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onEnrollResult(BiometricAuthenticator.Identifier identifier, int remaining) {
        Slog.w(getLogTag(), "onEnrollResult() called for authenticate!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onRemoved(BiometricAuthenticator.Identifier identifier, int remaining) {
        Slog.w(getLogTag(), "onRemoved() called for authenticate!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onEnumerationResult(BiometricAuthenticator.Identifier identifier, int remaining) {
        Slog.w(getLogTag(), "onEnumerationResult() called for authenticate!");
        return true;
    }
}
