package android.rms.resource;

import android.os.Bundle;
import android.rms.HwSysCountRes;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceCountControl;
import android.rms.utils.Utils;
import android.util.Log;

public final class ActivityResource extends HwSysCountRes {
    private static final String TAG = "RMS.ActivityResource";
    private static ActivityResource mActivityResource;

    private ActivityResource() {
        super(30, TAG);
    }

    public static synchronized ActivityResource getInstance() {
        synchronized (ActivityResource.class) {
            if (mActivityResource == null) {
                mActivityResource = new ActivityResource();
            }
            if (mActivityResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
                ActivityResource activityResource = mActivityResource;
                return activityResource;
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
            return null;
        }
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int strategy = 1;
        int typeID = isInWhiteList(pkg, 0) ? 2 : super.getTypeId(callingUid, pkg, processTpye);
        if (this.mResourceConfig != null && isResourceCountOverload(callingUid, pkg, typeID, count)) {
            strategy = this.mResourceConfig[typeID].getResourceStrategy();
            if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadStrategy CountOverload = " + strategy);
            }
        }
        return strategy;
    }

    /* access modifiers changed from: protected */
    public Bundle createBundleForResource(long id, int typeID, ResourceConfig config, ResourceCountControl mResourceCountControl, String pkg) {
        if (config == null || mResourceCountControl == null) {
            return null;
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "createBundleForResource Call In ActivityResource");
        }
        Bundle bundle = new Bundle();
        bundle.putInt(Utils.BUNDLE_HARD_THRESHOLD, config.getResouceUrgentThreshold());
        bundle.putInt(Utils.BUNDLE_CURRENT_COUNT, mResourceCountControl.getTotalCount(id));
        bundle.putBoolean(Utils.BUNDLE_IS_IN_WHITELIST, isInWhiteList(pkg, 0));
        return bundle;
    }
}
