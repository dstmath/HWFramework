package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;

public abstract class AuthenticationClient extends ClientMonitor {
    private static final int SPECIAL_USER_ID = -101;
    private static final String mKeyguardPackage = "com.android.systemui";
    protected int mFlags;
    private long mOpId;
    private String opPackageName;

    public abstract boolean handleFailedAttempt();

    public abstract boolean inLockoutMode();

    public abstract void resetFailedAttempts();

    public AuthenticationClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int targetUserId, int groupId, long opId, boolean restricted, String owner) {
        this(context, halDeviceId, token, receiver, targetUserId, groupId, opId, restricted, owner, 0);
    }

    public AuthenticationClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int targetUserId, int groupId, long opId, boolean restricted, String owner, int flags) {
        super(context, halDeviceId, token, receiver, targetUserId, groupId, restricted, owner);
        this.mOpId = opId;
        this.opPackageName = owner;
        this.mFlags = flags;
    }

    public void handleHwFailedAttempt(int flags, String packagesName) {
    }

    private boolean isScreenOn(Context context) {
        if (context == null) {
            Slog.w("FingerprintService", "input context is null");
            return false;
        }
        PowerManager power = (PowerManager) context.getSystemService("power");
        if (power != null) {
            return power.isScreenOn();
        }
        Slog.w("FingerprintService", "PowerManager is null");
        return false;
    }

    public boolean onAuthenticated(int fingerId, int groupId) {
        boolean result = false;
        boolean authenticated = fingerId != 0;
        IFingerprintServiceReceiver receiver = getReceiver();
        if (receiver != null) {
            try {
                MetricsLogger.action(getContext(), 252, authenticated);
                if (authenticated) {
                    Fingerprint fp;
                    Slog.v("FingerprintService", "onAuthenticated(owner=" + getOwnerString() + ", id=" + fingerId + ", gp=" + groupId + ")");
                    if (getIsRestricted()) {
                        fp = null;
                    } else {
                        fp = new Fingerprint("", groupId, fingerId, getHalDeviceId());
                    }
                    receiver.onAuthenticationSucceeded(getHalDeviceId(), fp, getTargetUserId());
                } else {
                    if (isScreenOn(getContext())) {
                        handleHwFailedAttempt(this.mFlags, this.opPackageName);
                    }
                    receiver.onAuthenticationFailed(getHalDeviceId());
                }
                notifyUserActivity();
                mAcquiredInfo = -1;
            } catch (RemoteException e) {
                Slog.w("FingerprintService", "Failed to notify Authenticated:", e);
                result = true;
            }
        } else {
            result = true;
        }
        if (authenticated) {
            result |= true;
            resetFailedAttempts();
            return result;
        }
        if (!(receiver == null || isKeyguard(this.opPackageName))) {
            FingerprintUtils.vibrateFingerprintErrorHw(getContext());
        }
        if (!isScreenOn(getContext()) || inLockoutMode()) {
            return result;
        }
        boolean inLockoutMode = handleFailedAttempt();
        if (inLockoutMode) {
            try {
                Slog.w("FingerprintService", "Forcing lockout (fp driver code should do this!)");
                receiver.onError(getHalDeviceId(), 7);
                stop(false);
            } catch (RemoteException e2) {
                Slog.w("FingerprintService", "Failed to notify lockout:", e2);
            }
        }
        return result | inLockoutMode;
    }

    public int start() {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w("FingerprintService", "start authentication: no fingeprintd!");
            return 3;
        }
        try {
            int newGroupId = getGroupId();
            if (newGroupId != SPECIAL_USER_ID) {
                newGroupId = getRealUserIdForHal(getGroupId());
            }
            int result = daemon.authenticate(this.mOpId, newGroupId);
            if (result != 0) {
                Slog.w("FingerprintService", "startAuthentication failed, result=" + result);
                onError(1);
                return result;
            }
            Slog.w("FingerprintService", "client " + getOwnerString() + " is authenticating...");
            return 0;
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "startAuthentication failed", e);
            return 3;
        }
    }

    private boolean isKeyguard(String clientPackage) {
        return mKeyguardPackage.equals(clientPackage);
    }

    public int stop(boolean initiatedByClient) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w("FingerprintService", "stopAuthentication: no fingeprintd!");
            return 3;
        }
        try {
            int result = daemon.cancelAuthentication();
            if (result != 0) {
                Slog.w("FingerprintService", "stopAuthentication failed, result=" + result);
                return result;
            }
            Slog.w("FingerprintService", "client " + getOwnerString() + " is no longer authenticating");
            return 0;
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "stopAuthentication failed", e);
            return 3;
        }
    }

    public boolean onEnrollResult(int fingerId, int groupId, int rem) {
        Slog.w("FingerprintService", "onEnrollResult() called for authenticate!");
        return true;
    }

    public boolean onRemoved(int fingerId, int groupId) {
        Slog.w("FingerprintService", "onRemoved() called for authenticate!");
        return true;
    }

    public boolean onEnumerationResult(int fingerId, int groupId) {
        Slog.w("FingerprintService", "onEnumerationResult() called for authenticate!");
        return true;
    }
}
