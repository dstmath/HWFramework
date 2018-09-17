package android.rms.resource;

import android.os.RemoteException;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.IUpdateWhiteListCallback;
import android.rms.IUpdateWhiteListCallback.Stub;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceFlowControl;
import android.util.Log;
import java.util.ArrayList;

public final class NotificationResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "NotificationResource";
    private static NotificationResource mNotificationResource;
    private int mOverloadNumber;
    private int mOverloadPeroid;
    private ResourceConfig[] mResourceConfig;
    private ResourceFlowControl mResourceFlowControl;
    private HwSysResManager mResourceManger;
    private IUpdateWhiteListCallback mUpdateWhiteListCallback;
    private ArrayList<String> mWhiteList;

    public NotificationResource() {
        this.mResourceFlowControl = new ResourceFlowControl();
        this.mWhiteList = null;
        this.mUpdateWhiteListCallback = new Stub() {
            public void update() throws RemoteException {
                NotificationResource Resource = NotificationResource.self();
                if (Resource == null) {
                    Log.e(NotificationResource.TAG, "Notification Resource update get the instance is null");
                    return;
                }
                ArrayList<String> tempWhiteList = Resource.getResWhiteList(10, 0);
                if (tempWhiteList.size() != 0) {
                    NotificationResource.this.mWhiteList.clear();
                    NotificationResource.this.mWhiteList = tempWhiteList;
                } else {
                    Log.e(NotificationResource.TAG, "Notification Resource update nameList failed!!!");
                }
            }
        };
        if (!registerResourceCallback(this.mUpdateWhiteListCallback)) {
            Log.e(TAG, "Notification Resource register callback failed");
        }
        getConfig(10);
        this.mWhiteList = super.getResWhiteList(10, 0);
    }

    public static synchronized NotificationResource self() {
        NotificationResource notificationResource;
        synchronized (NotificationResource.class) {
            notificationResource = mNotificationResource;
        }
        return notificationResource;
    }

    public static synchronized NotificationResource getInstance() {
        NotificationResource notificationResource;
        synchronized (NotificationResource.class) {
            if (mNotificationResource == null) {
                mNotificationResource = new NotificationResource();
            }
            notificationResource = mNotificationResource;
        }
        return notificationResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        int typeID = super.getTypeId(callingUid, pkg, processTpye);
        if (this.mResourceConfig == null || isWhiteList(pkg) || !isResourceSpeedOverload(callingUid, pkg, typeID)) {
            return 1;
        }
        return getSpeedOverloadStrategy(typeID);
    }

    public void release(int callingUid, String pkg, int processTpye) {
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceFlowControl.removeResourceSpeedRecord(super.getResourceId(callingUid, pkg, processTpye));
        this.mResourceManger.clearResourceStatus(callingUid, 10);
    }

    private void getConfig(int resourceType) {
        this.mResourceManger = HwSysResManager.getInstance();
        this.mResourceConfig = this.mResourceManger.getResourceConfig(resourceType);
        if (this.mResourceConfig == null) {
        }
    }

    private int getSpeedOverloadStrategy(int typeID) {
        int strategy = this.mResourceConfig[typeID].getResourceStrategy();
        if (this.mOverloadPeroid >= this.mResourceConfig[typeID].getResourceMaxPeroid()) {
            return strategy;
        }
        return 1;
    }

    private boolean isResourceSpeedOverload(int callingUid, String pkg, int typeID) {
        long id = super.getResourceId(callingUid, pkg, typeID);
        ResourceConfig config = this.mResourceConfig[typeID];
        int threshold = config.getResourceThreshold();
        int loopInterval = config.getLoopInterval();
        if (!this.mResourceFlowControl.checkSpeedOverload(id, threshold, loopInterval)) {
            return DEBUG;
        }
        this.mOverloadNumber = this.mResourceFlowControl.getOverloadNumber(id);
        this.mOverloadPeroid = this.mResourceFlowControl.getOverloadPeroid(id);
        if (Log.HWINFO) {
            Log.i(TAG, "isResourceSpeedOverload id=" + id + " threshold=" + threshold + " OverloadNum=" + this.mOverloadNumber + " OverloadPeroid=" + this.mOverloadPeroid);
        }
        if (this.mResourceFlowControl.isReportTime(id, loopInterval * 60) && typeID != 3) {
            this.mResourceManger.recordResourceOverloadStatus(callingUid, pkg, 10, this.mOverloadNumber, this.mOverloadPeroid, 0);
        }
        return true;
    }

    private boolean isWhiteList(String pkg) {
        if (pkg == null) {
            return DEBUG;
        }
        for (String proc : this.mWhiteList) {
            if (pkg.contains(proc)) {
                return true;
            }
        }
        return DEBUG;
    }
}
