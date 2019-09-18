package android.rms.resource;

import android.rms.HwSysCountRes;
import android.rms.utils.Utils;
import android.util.Log;

public final class AppOpsResource extends HwSysCountRes {
    private static final String TAG = "RMS.AppOpsResource";
    private static AppOpsResource mAppOpsResource;

    private AppOpsResource() {
        super(14, TAG);
    }

    public static synchronized AppOpsResource getInstance() {
        synchronized (AppOpsResource.class) {
            if (mAppOpsResource == null) {
                mAppOpsResource = new AppOpsResource();
            }
            if (mAppOpsResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
                AppOpsResource appOpsResource = mAppOpsResource;
                return appOpsResource;
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
            return null;
        }
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int strategy = 1;
        if (this.mResourceConfig != null && isResourceCountOverload(callingUid, pkg, processTpye, count) && !isInWhiteList(pkg, 0)) {
            strategy = this.mResourceConfig[processTpye].getResourceStrategy();
            if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadStrategy CountOverload = " + strategy);
            }
        }
        if (Utils.HWFLOW) {
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
}
