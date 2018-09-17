package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import java.util.Arrays;

public abstract class EnrollClient extends ClientMonitor {
    private static final int ENROLLMENT_TIMEOUT_MS = 60000;
    private static final long MS_PER_SEC = 1000;
    private byte[] mCryptoToken;

    public EnrollClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int userId, int groupId, byte[] cryptoToken, boolean restricted, String owner) {
        super(context, halDeviceId, token, receiver, userId, groupId, restricted, owner);
        this.mCryptoToken = Arrays.copyOf(cryptoToken, cryptoToken.length);
    }

    public boolean onEnrollResult(int fingerId, int groupId, int remaining) {
        if (groupId != getGroupId()) {
            Slog.w("FingerprintService", "groupId != getGroupId(), groupId: " + groupId + " getGroupId():" + getGroupId());
        }
        if (remaining == 0) {
            FingerprintUtils.getInstance().addFingerprintForUser(getContext(), fingerId, getTargetUserId());
        }
        return sendEnrollResult(fingerId, groupId, remaining);
    }

    private boolean sendEnrollResult(int fpId, int groupId, int remaining) {
        IFingerprintServiceReceiver receiver = getReceiver();
        if (receiver == null) {
            return true;
        }
        notifyUserActivity();
        mAcquiredInfo = -1;
        FingerprintUtils.vibrateFingerprintSuccess(getContext());
        MetricsLogger.action(getContext(), 251);
        try {
            receiver.onEnrollResult(getHalDeviceId(), fpId, groupId, remaining);
            return remaining == 0;
        } catch (RemoteException e) {
            Slog.w("FingerprintService", "Failed to notify EnrollResult:", e);
            return true;
        }
    }

    public int start() {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w("FingerprintService", "enroll: no fingeprintd!");
            return 3;
        }
        try {
            int result = daemon.enroll(this.mCryptoToken, getRealUserIdForHal(getGroupId()), 60);
            if (result != 0) {
                Slog.w("FingerprintService", "startEnroll failed, result=" + result);
                onError(1);
                return result;
            }
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "startEnroll failed", e);
        }
        return 0;
    }

    public int stop(boolean initiatedByClient) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w("FingerprintService", "stopEnrollment: no fingeprintd!");
            return 3;
        }
        try {
            int result = daemon.cancelEnrollment();
            if (result != 0) {
                Slog.w("FingerprintService", "startEnrollCancel failed, result = " + result);
                return result;
            }
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "stopEnrollment failed", e);
        }
        if (initiatedByClient) {
            onError(5);
        }
        return 0;
    }

    public boolean onRemoved(int fingerId, int groupId) {
        Slog.w("FingerprintService", "onRemoved() called for enroll!");
        return true;
    }

    public boolean onEnumerationResult(int fingerId, int groupId) {
        Slog.w("FingerprintService", "onEnumerationResult() called for enroll!");
        return true;
    }

    public boolean onAuthenticated(int fingerId, int groupId) {
        Slog.w("FingerprintService", "onAuthenticated() called for enroll!");
        return true;
    }
}
