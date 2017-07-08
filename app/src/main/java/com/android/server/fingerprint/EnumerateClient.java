package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;

public abstract class EnumerateClient extends ClientMonitor {
    public EnumerateClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int userId, int groupId, boolean restricted, String owner) {
        super(context, halDeviceId, token, receiver, userId, groupId, restricted, owner);
    }

    public int start() {
        try {
            int result = getFingerprintDaemon().enumerate();
            if (result != 0) {
                Slog.w("FingerprintService", "start enumerate for user " + getTargetUserId() + " failed, result=" + result);
                onError(1);
                return result;
            }
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "startRemove failed", e);
        }
        return 0;
    }

    public int stop(boolean initiatedByClient) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w("FingerprintService", "stopAuthentication: no fingeprintd!");
            return 3;
        }
        try {
            int result = daemon.cancelEnumeration();
            if (result != 0) {
                Slog.w("FingerprintService", "stop enumeration failed, result=" + result);
                return result;
            }
            if (initiatedByClient) {
                onError(5);
            }
            return 0;
        } catch (RemoteException e) {
            Slog.e("FingerprintService", "stop enumeration failed", e);
            return 3;
        }
    }

    public boolean onEnumerationResult(int fingerId, int groupId) {
        boolean z = true;
        IFingerprintServiceReceiver receiver = getReceiver();
        if (receiver == null) {
            return true;
        }
        notifyUserActivity();
        mAcquiredInfo = -1;
        try {
            receiver.onRemoved(getHalDeviceId(), fingerId, groupId);
        } catch (RemoteException e) {
            Slog.w("FingerprintService", "Failed to notify enumerated:", e);
        }
        if (fingerId != 0) {
            z = false;
        }
        return z;
    }
}
