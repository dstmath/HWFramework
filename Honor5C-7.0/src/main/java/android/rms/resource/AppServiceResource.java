package android.rms.resource;

import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceFlowControl;
import android.util.Log;

public final class AppServiceResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "AppServiceResource";
    private static AppServiceResource mAppServiceResource;
    private int mOverloadNumber;
    private int mOverloadPeroid;
    private ResourceConfig[] mResourceConfig;
    private ResourceFlowControl mResourceFlowControl;
    private HwSysResManager mResourceManger;

    public AppServiceResource() {
        this.mResourceFlowControl = new ResourceFlowControl();
        getConfig(18);
    }

    public static synchronized AppServiceResource getInstance() {
        AppServiceResource appServiceResource;
        synchronized (AppServiceResource.class) {
            if (mAppServiceResource == null) {
                mAppServiceResource = new AppServiceResource();
            }
            appServiceResource = mAppServiceResource;
        }
        return appServiceResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        if (this.mResourceConfig == null) {
            return 1;
        }
        int typeID = super.getTypeId(callingUid, pkg, processTpye);
        if (isResourceSpeedOverload(callingUid, pkg, typeID)) {
            return getSpeedOverloadStrategy(typeID);
        }
        return 1;
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceFlowControl.removeResourceSpeedRecord(super.getResourceId(callingUid, pkg, processTpye));
        this.mResourceManger.clearResourceStatus(callingUid, 18);
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
        int maxPeroid = config.getResourceMaxPeroid();
        if (!this.mResourceFlowControl.checkSpeedOverload(id, threshold, loopInterval)) {
            return DEBUG;
        }
        this.mOverloadPeroid = this.mResourceFlowControl.getOverloadPeroid(id);
        if (this.mOverloadPeroid >= maxPeroid) {
            this.mOverloadNumber = this.mResourceFlowControl.getOverloadNumber(id);
            if (Log.HWINFO) {
                Log.i(TAG, "isResourceSpeedOverload id=" + id + " threshold=" + threshold + " OverloadNum=" + this.mOverloadNumber + " OverloadPeroid=" + this.mOverloadPeroid);
            }
            this.mResourceManger.recordResourceOverloadStatus(callingUid, pkg, 18, this.mOverloadNumber, this.mOverloadPeroid, 0);
        }
        return true;
    }
}
