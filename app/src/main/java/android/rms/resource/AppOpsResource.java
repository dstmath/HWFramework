package android.rms.resource;

import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceCountControl;
import android.util.Log;

public final class AppOpsResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "AppOpsResource";
    private static AppOpsResource mAppOpsResource;
    private static ResourceConfig mResourceConfig;
    private static HwSysResManager mResourceManger;
    private int mOverloadNum;
    private ResourceCountControl mResourceCountControl;

    public AppOpsResource() {
        this.mResourceCountControl = new ResourceCountControl();
    }

    public static synchronized AppOpsResource getInstance() {
        AppOpsResource appOpsResource;
        synchronized (AppOpsResource.class) {
            if (mAppOpsResource == null && getConfig(14)) {
                mAppOpsResource = new AppOpsResource();
            }
            appOpsResource = mAppOpsResource;
        }
        return appOpsResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        int strategy = 1;
        if (mResourceConfig != null && isResourceCountOverload(callingUid, pkg, processTpye)) {
            strategy = mResourceConfig.getResourceStrategy();
        }
        if (Log.HWINFO) {
            return strategy;
        }
        return 1;
    }

    public void release(int callingUid, String pkg, int processTpye) {
        this.mResourceCountControl.reduceCurrentCount(super.getResourceId(callingUid, pkg, processTpye));
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceCountControl.removeResourceCountRecord(super.getResourceId(callingUid, pkg, processTpye));
    }

    private static boolean getConfig(int resourceType) {
        mResourceManger = HwSysResManager.getInstance();
        ResourceConfig[] config = mResourceManger.getResourceConfig(resourceType);
        if (config == null) {
            return DEBUG;
        }
        mResourceConfig = config[0];
        if (mResourceConfig != null) {
        }
        return true;
    }

    private boolean isResourceCountOverload(int callingUid, String pkg, int processTpye) {
        long id = super.getResourceId(callingUid, pkg, processTpye);
        int threshold = mResourceConfig.getResourceThreshold();
        int timeInterval = mResourceConfig.getLoopInterval();
        if (!this.mResourceCountControl.checkCountOverload(id, threshold)) {
            return DEBUG;
        }
        this.mOverloadNum = this.mResourceCountControl.getOverloadNumber(id);
        if (Log.HWINFO) {
            Log.i(TAG, "CallBack is Overload  id=" + id + " OverloadNumber=" + this.mOverloadNum + " threshold=" + threshold);
        }
        if (this.mResourceCountControl.isReportTime(id, timeInterval) && processTpye != 3) {
            mResourceManger.recordResourceOverloadStatus(callingUid, pkg, 14, 0, 0, this.mOverloadNum);
        }
        return true;
    }
}
