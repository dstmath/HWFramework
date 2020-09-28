package android.rms.resource;

import android.os.Binder;
import android.rms.HwSysSpeedRes;
import android.rms.utils.Utils;
import android.util.Log;

public final class NotificationResource extends HwSysSpeedRes {
    private static final String TAG = "RMS.NotificationResource";
    private static NotificationResource notificationResource;

    private NotificationResource() {
        super(10, TAG);
    }

    public static synchronized NotificationResource getInstance() {
        synchronized (NotificationResource.class) {
            if (notificationResource == null) {
                notificationResource = new NotificationResource();
            }
            if (notificationResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
                return notificationResource;
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
            return null;
        }
    }

    @Override // android.rms.HwSysResImpl
    public int acquire(int callingUid, String pkg, int processTpye) {
        int strategy = 1;
        int typeId = super.getTypeId(callingUid, pkg, processTpye);
        if (this.mResourceConfig != null) {
            long token = Binder.clearCallingIdentity();
            if (isResourceSpeedOverload(callingUid, pkg, typeId) && !isInWhiteList(pkg, 0)) {
                strategy = getSpeedOverloadStrategy(typeId);
                if (Utils.DEBUG) {
                    Log.d(TAG, "getOverloadStrategy = " + strategy);
                }
            }
            Binder.restoreCallingIdentity(token);
        }
        return strategy;
    }

    @Override // android.rms.HwSysResImpl
    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceFlowControl.removeResourceSpeedRecord(super.getResourceId(callingUid, pkg, processTpye));
        this.mResourceManger.clearResourceStatus(callingUid, 10);
    }
}
