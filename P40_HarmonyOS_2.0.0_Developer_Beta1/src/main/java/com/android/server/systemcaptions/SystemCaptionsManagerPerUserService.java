package com.android.server.systemcaptions;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.infra.AbstractPerUserSystemService;

final class SystemCaptionsManagerPerUserService extends AbstractPerUserSystemService<SystemCaptionsManagerPerUserService, SystemCaptionsManagerService> {
    private static final String TAG = SystemCaptionsManagerPerUserService.class.getSimpleName();
    @GuardedBy({"mLock"})
    private RemoteSystemCaptionsManagerService mRemoteService;

    SystemCaptionsManagerPerUserService(SystemCaptionsManagerService master, Object lock, boolean disabled, int userId) {
        super(master, lock, userId);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractPerUserSystemService
    public ServiceInfo newServiceInfoLocked(ComponentName serviceComponent) throws PackageManager.NameNotFoundException {
        try {
            return AppGlobals.getPackageManager().getServiceInfo(serviceComponent, 128, this.mUserId);
        } catch (RemoteException e) {
            throw new PackageManager.NameNotFoundException("Could not get service for " + serviceComponent);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void initializeLocked() {
        if (((SystemCaptionsManagerService) this.mMaster).verbose) {
            Slog.v(TAG, "initialize()");
        }
        if (getRemoteServiceLocked() == null && ((SystemCaptionsManagerService) this.mMaster).verbose) {
            Slog.v(TAG, "initialize(): Failed to init remote server");
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void destroyLocked() {
        if (((SystemCaptionsManagerService) this.mMaster).verbose) {
            Slog.v(TAG, "destroyLocked()");
        }
        RemoteSystemCaptionsManagerService remoteSystemCaptionsManagerService = this.mRemoteService;
        if (remoteSystemCaptionsManagerService != null) {
            remoteSystemCaptionsManagerService.destroy();
            this.mRemoteService = null;
        }
    }

    @GuardedBy({"mLock"})
    private RemoteSystemCaptionsManagerService getRemoteServiceLocked() {
        if (this.mRemoteService == null) {
            String serviceName = getComponentNameLocked();
            if (serviceName != null) {
                this.mRemoteService = new RemoteSystemCaptionsManagerService(getContext(), ComponentName.unflattenFromString(serviceName), this.mUserId, ((SystemCaptionsManagerService) this.mMaster).verbose);
                if (((SystemCaptionsManagerService) this.mMaster).verbose) {
                    String str = TAG;
                    Slog.v(str, "getRemoteServiceLocked(): initialize for user " + this.mUserId);
                }
                this.mRemoteService.initialize();
            } else if (!((SystemCaptionsManagerService) this.mMaster).verbose) {
                return null;
            } else {
                Slog.v(TAG, "getRemoteServiceLocked(): Not set");
                return null;
            }
        }
        return this.mRemoteService;
    }
}
