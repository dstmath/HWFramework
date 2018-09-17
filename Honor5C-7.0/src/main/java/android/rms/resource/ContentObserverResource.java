package android.rms.resource;

import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceCountControl;
import android.util.Log;

public final class ContentObserverResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "ContentOberverResource";
    private static ContentObserverResource mContentObserverResource;
    private static ResourceConfig[] mResourceConfig;
    private static HwSysResManager mResourceManager;
    private ResourceCountControl mResourceCountControl;

    public ContentObserverResource() {
        this.mResourceCountControl = new ResourceCountControl();
    }

    public static synchronized ContentObserverResource getInstance() {
        ContentObserverResource contentObserverResource;
        synchronized (ContentObserverResource.class) {
            if (mContentObserverResource == null && getConfig(35)) {
                mContentObserverResource = new ContentObserverResource();
            }
            contentObserverResource = mContentObserverResource;
        }
        return contentObserverResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        int strategy = 1;
        if (pkg == null) {
            Log.d(TAG, "pkg is null!");
            return 1;
        } else if (!needCountObserverNumber(callingUid, pkg)) {
            return 1;
        } else {
            int typeID = super.getTypeId(callingUid, pkg, processTpye);
            if (mResourceConfig == null || typeID >= mResourceConfig.length) {
                Log.e(TAG, "contentObserverconfig is null!!");
            } else if (isResourceCountOverload(callingUid, pkg, typeID) && mResourceConfig[typeID] != null) {
                strategy = mResourceConfig[typeID].getResourceStrategy();
            }
            return strategy;
        }
    }

    public void release(int callingUid, String pkg, int processTpye) {
        if (pkg == null) {
            Log.d(TAG, "pkg is null!");
        } else if (needCountObserverNumber(callingUid, pkg)) {
            long id = super.getResourceId(callingUid, pkg, super.getTypeId(callingUid, pkg, processTpye));
            if (this.mResourceCountControl != null) {
                this.mResourceCountControl.reduceCurrentCount(id);
            }
        }
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        if (pkg == null) {
            Log.d(TAG, "pkg is null!");
        } else if (needCountObserverNumber(callingUid, pkg)) {
            long id = super.getResourceId(callingUid, pkg, super.getTypeId(callingUid, pkg, processTpye));
            if (this.mResourceCountControl != null) {
                this.mResourceCountControl.removeResourceCountRecord(id);
                Log.d(TAG, "clear ObserverResource in contentService");
            }
        }
    }

    private static boolean getConfig(int resourceType) {
        mResourceManager = HwSysResManager.getInstance();
        if (mResourceManager != null) {
            mResourceConfig = mResourceManager.getResourceConfig(resourceType);
            if (mResourceConfig != null) {
                return true;
            }
        }
        Log.w(TAG, "get content observer config failed!");
        return DEBUG;
    }

    private boolean isResourceCountOverload(int callingUid, String pkg, int typeId) {
        if (pkg == null) {
            Log.d(TAG, "pkg is null!");
            return DEBUG;
        }
        long id = super.getResourceId(callingUid, pkg, typeId);
        if (mResourceConfig != null && typeId < mResourceConfig.length) {
            ResourceConfig config = mResourceConfig[typeId];
            if (!(config == null || this.mResourceCountControl == null)) {
                int softThreshold = config.getResourceThreshold();
                int timeInterval = config.getLoopInterval();
                int hardThreshold = config.getResourceMaxPeroid();
                if (this.mResourceCountControl.checkCountOverload(id, softThreshold)) {
                    int totalCount = this.mResourceCountControl.getTotalCount(id);
                    int overLoadNum = totalCount - softThreshold;
                    if (Log.HWINFO) {
                        Log.i(TAG, "CallBack is Overload  id=" + id + ", overLoadNum=" + overLoadNum + " totalCount=" + totalCount + " uploadthreshold=" + softThreshold + ", killThreshold=" + hardThreshold);
                    }
                    if (mResourceManager != null && (this.mResourceCountControl.isReportTime(id, timeInterval) || overLoadNum > hardThreshold)) {
                        mResourceManager.recordResourceOverloadStatus(callingUid, pkg, 35, 0, hardThreshold, overLoadNum);
                    }
                    return true;
                }
            }
        }
        return DEBUG;
    }

    private boolean needCountObserverNumber(int callingUid, String pkg) {
        if (pkg == null) {
            Log.w(TAG, "packageName is null!");
            return DEBUG;
        } else if (callingUid == 1000 || pkg.endsWith(":1000")) {
            return DEBUG;
        } else {
            return true;
        }
    }
}
