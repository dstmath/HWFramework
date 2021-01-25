package com.android.server.biometrics;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.biometrics.BiometricServiceBase;
import java.util.ArrayList;

public abstract class RemovalClient extends ClientMonitor {
    private final int mBiometricId;
    private final BiometricUtils mBiometricUtils;

    public RemovalClient(Context context, Constants constants, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int biometricId, int groupId, int userId, boolean restricted, String owner, BiometricUtils utils) {
        super(context, constants, daemon, halDeviceId, token, listener, userId, groupId, restricted, owner, 0);
        this.mBiometricId = biometricId;
        this.mBiometricUtils = utils;
    }

    public int getBiometricId() {
        return this.mBiometricId;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public void notifyUserActivity() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.LoggableMonitor
    public int statsAction() {
        return 4;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int start() {
        try {
            int result = getDaemonWrapper().remove(getGroupId(), this.mBiometricId);
            if (result != 0) {
                String logTag = getLogTag();
                Slog.w(logTag, "startRemove with id = " + this.mBiometricId + " failed, result=" + result);
                this.mMetricsLogger.histogram(this.mConstants.tagRemoveStartError(), result);
                onError(getHalDeviceId(), 1, 0);
                return result;
            }
        } catch (RemoteException e) {
            Slog.e(getLogTag(), "startRemove failed", e);
        }
        return 0;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int stop(boolean initiatedByClient) {
        if (this.mAlreadyCancelled) {
            Slog.w(getLogTag(), "stopRemove: already cancelled!");
            return 0;
        }
        try {
            int result = getDaemonWrapper().cancel();
            if (result != 0) {
                String logTag = getLogTag();
                Slog.w(logTag, "stopRemoval failed, result=" + result);
                return result;
            }
            String logTag2 = getLogTag();
            Slog.w(logTag2, "client " + getOwnerString() + " is no longer removing");
            this.mAlreadyCancelled = true;
            return 0;
        } catch (RemoteException e) {
            Slog.e(getLogTag(), "stopRemoval failed", e);
            return 3;
        }
    }

    private boolean sendRemoved(BiometricAuthenticator.Identifier identifier, int remaining) {
        try {
            if (getListener() != null) {
                getListener().onRemoved(identifier, remaining);
            }
        } catch (RemoteException e) {
            Slog.w(getLogTag(), "Failed to notify Removed:", e);
        }
        return remaining == 0;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onRemoved(BiometricAuthenticator.Identifier identifier, int remaining) {
        if (identifier.getBiometricId() != 0) {
            this.mBiometricUtils.removeBiometricForUser(getContext(), getTargetUserId(), identifier.getBiometricId());
        }
        return sendRemoved(identifier, remaining);
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onEnrollResult(BiometricAuthenticator.Identifier identifier, int rem) {
        Slog.w(getLogTag(), "onEnrollResult() called for remove!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean authenticated, ArrayList<Byte> arrayList) {
        Slog.w(getLogTag(), "onAuthenticated() called for remove!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onEnumerationResult(BiometricAuthenticator.Identifier identifier, int remaining) {
        Slog.w(getLogTag(), "onEnumerationResult() called for remove!");
        return true;
    }
}
