package com.android.server.biometrics;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.biometrics.BiometricServiceBase;
import java.util.ArrayList;

public abstract class EnumerateClient extends ClientMonitor {
    public EnumerateClient(Context context, Constants constants, BiometricServiceBase.DaemonWrapper daemon, long halDeviceId, IBinder token, BiometricServiceBase.ServiceListener listener, int groupId, int userId, boolean restricted, String owner) {
        super(context, constants, daemon, halDeviceId, token, listener, userId, groupId, restricted, owner, 0);
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public void notifyUserActivity() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.LoggableMonitor
    public int statsAction() {
        return 3;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int start() {
        try {
            int result = getDaemonWrapper().enumerate();
            if (result != 0) {
                String logTag = getLogTag();
                Slog.w(logTag, "start enumerate for user " + getTargetUserId() + " failed, result=" + result);
                this.mMetricsLogger.histogram(this.mConstants.tagEnumerateStartError(), result);
                onError(getHalDeviceId(), 1, 0);
                return result;
            }
        } catch (RemoteException e) {
            Slog.e(getLogTag(), "startEnumeration failed", e);
        }
        return 0;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public int stop(boolean initiatedByClient) {
        if (this.mAlreadyCancelled) {
            Slog.w(getLogTag(), "stopEnumerate: already cancelled!");
            return 0;
        }
        try {
            int result = getDaemonWrapper().cancel();
            if (result != 0) {
                String logTag = getLogTag();
                Slog.w(logTag, "stop enumeration failed, result=" + result);
                return result;
            }
            if (initiatedByClient) {
                onError(getHalDeviceId(), 5, 0);
            }
            this.mAlreadyCancelled = true;
            return 0;
        } catch (RemoteException e) {
            Slog.e(getLogTag(), "stopEnumeration failed", e);
            return 3;
        }
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onEnumerationResult(BiometricAuthenticator.Identifier identifier, int remaining) {
        notifyUserActivity();
        mAcquiredInfo = -1;
        try {
            if (getListener() != null) {
                getListener().onEnumerated(identifier, remaining);
            }
        } catch (RemoteException e) {
            Slog.w(getLogTag(), "Failed to notify enumerated:", e);
        }
        return remaining == 0;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean authenticated, ArrayList<Byte> arrayList) {
        Slog.w(getLogTag(), "onAuthenticated() called for enumerate!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onEnrollResult(BiometricAuthenticator.Identifier identifier, int rem) {
        Slog.w(getLogTag(), "onEnrollResult() called for enumerate!");
        return true;
    }

    @Override // com.android.server.biometrics.ClientMonitor
    public boolean onRemoved(BiometricAuthenticator.Identifier identifier, int remaining) {
        Slog.w(getLogTag(), "onRemoved() called for enumerate!");
        return true;
    }
}
