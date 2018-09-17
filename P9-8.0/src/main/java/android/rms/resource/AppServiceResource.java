package android.rms.resource;

import android.rms.HwSysSpeedRes;
import android.rms.utils.Utils;
import android.util.Log;

public final class AppServiceResource extends HwSysSpeedRes {
    private static final String TAG = "RMS.AppServiceResource";
    private static AppServiceResource mAppServiceResource;

    private AppServiceResource() {
        super(17, TAG);
    }

    /* JADX WARNING: Missing block: B:19:0x0036, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized AppServiceResource getInstance() {
        synchronized (AppServiceResource.class) {
            if (mAppServiceResource == null) {
                mAppServiceResource = new AppServiceResource();
            }
            if (mAppServiceResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
                AppServiceResource appServiceResource = mAppServiceResource;
                return appServiceResource;
            } else if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
        }
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        int strategy = 1;
        if (this.mResourceConfig != null) {
            int typeID = super.getTypeId(callingUid, pkg, processTpye);
            if (isResourceSpeedOverload(callingUid, pkg, typeID) && (isInWhiteList(pkg, 0) ^ 1) != 0) {
                strategy = getSpeedOverloadStrategy(typeID);
                if (Utils.DEBUG) {
                    Log.d(TAG, "getOverloadStrategy = " + strategy);
                }
            }
        }
        return strategy;
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceFlowControl.removeResourceSpeedRecord(super.getResourceId(callingUid, pkg, processTpye));
        this.mResourceManger.clearResourceStatus(callingUid, 17);
    }
}
