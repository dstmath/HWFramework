package android.content.rollback;

import android.annotation.SystemApi;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.ParceledListSlice;
import android.content.pm.VersionedPackage;
import android.os.RemoteException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

@SystemApi
public final class RollbackManager {
    public static final String EXTRA_STATUS = "android.content.rollback.extra.STATUS";
    public static final String EXTRA_STATUS_MESSAGE = "android.content.rollback.extra.STATUS_MESSAGE";
    public static final String PROPERTY_ROLLBACK_LIFETIME_MILLIS = "rollback_lifetime_in_millis";
    public static final int STATUS_FAILURE = 1;
    public static final int STATUS_FAILURE_INSTALL = 3;
    public static final int STATUS_FAILURE_ROLLBACK_UNAVAILABLE = 2;
    public static final int STATUS_SUCCESS = 0;
    private final IRollbackManager mBinder;
    private final String mCallerPackageName;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
    }

    public RollbackManager(Context context, IRollbackManager binder) {
        this.mCallerPackageName = context.getPackageName();
        this.mBinder = binder;
    }

    public List<RollbackInfo> getAvailableRollbacks() {
        try {
            return this.mBinder.getAvailableRollbacks().getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<RollbackInfo> getRecentlyCommittedRollbacks() {
        try {
            return this.mBinder.getRecentlyExecutedRollbacks().getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void commitRollback(int rollbackId, List<VersionedPackage> causePackages, IntentSender statusReceiver) {
        try {
            this.mBinder.commitRollback(rollbackId, new ParceledListSlice(causePackages), this.mCallerPackageName, statusReceiver);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reloadPersistedData() {
        try {
            this.mBinder.reloadPersistedData();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void expireRollbackForPackage(String packageName) {
        try {
            this.mBinder.expireRollbackForPackage(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
