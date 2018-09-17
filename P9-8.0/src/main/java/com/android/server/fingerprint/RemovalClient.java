package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;

public abstract class RemovalClient extends ClientMonitor {
    private int mFingerId;

    public RemovalClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int fingerId, int groupId, int userId, boolean restricted, String owner) {
        super(context, halDeviceId, token, receiver, userId, groupId, restricted, owner);
        this.mFingerId = fingerId;
    }

    public int start() {
        try {
            int result = getFingerprintDaemon().remove(getRealUserIdForHal(getGroupId()), this.mFingerId);
            if (result != 0) {
                Slog.w("FingerprintService", "startRemove with id = " + this.mFingerId + " failed, result=" + result);
                MetricsLogger.histogram(getContext(), "fingerprintd_remove_start_error", result);
                onError(1, 0);
                return result;
            }
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "startRemove failed", e);
        }
        return 0;
    }

    public int stop(boolean initiatedByClient) {
        if (this.mAlreadyCancelled) {
            Slog.w("FingerprintService", "stopRemove: already cancelled!");
            return 0;
        }
        IBiometricsFingerprint daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w("FingerprintService", "stopRemoval: no fingerprint HAL!");
            return 3;
        }
        try {
            int result = daemon.cancel();
            if (result != 0) {
                Slog.w("FingerprintService", "stopRemoval failed, result=" + result);
                return result;
            }
            Slog.w("FingerprintService", "client " + getOwnerString() + " is no longer removing");
            this.mAlreadyCancelled = true;
            return 0;
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "stopRemoval failed", e);
            return 3;
        }
    }

    private boolean sendRemoved(int fingerId, int groupId, int remaining) {
        IFingerprintServiceReceiver receiver = getReceiver();
        if (receiver != null) {
            try {
                receiver.onRemoved(getHalDeviceId(), fingerId, groupId, remaining);
            } catch (RemoteException e) {
                Slog.w("FingerprintService", "Failed to notify Removed:", e);
            }
        }
        if (remaining == 0) {
            return true;
        }
        return false;
    }

    public boolean onRemoved(int fingerId, int groupId, int remaining) {
        if (fingerId != 0) {
            FingerprintUtils.getInstance().removeFingerprintIdForUser(getContext(), fingerId, getTargetUserId());
        }
        return sendRemoved(fingerId, getGroupId(), remaining);
    }

    public boolean onEnrollResult(int fingerId, int groupId, int rem) {
        Slog.w("FingerprintService", "onEnrollResult() called for remove!");
        return true;
    }

    public boolean onAuthenticated(int fingerId, int groupId) {
        Slog.w("FingerprintService", "onAuthenticated() called for remove!");
        return true;
    }

    public boolean onEnumerationResult(int fingerId, int groupId, int remaining) {
        Slog.w("FingerprintService", "onEnumerationResult() called for remove!");
        return true;
    }
}
