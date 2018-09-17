package android.rms.resource;

import android.os.Bundle;
import android.rms.HwSysCountRes;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceCountControl;
import android.rms.utils.Utils;
import android.util.Log;

public final class AlarmResource extends HwSysCountRes {
    private static final String TAG = "RMS.AlarmResource";
    private static AlarmResource mAlarmResource;

    private AlarmResource() {
        super(13, TAG);
    }

    /* JADX WARNING: Missing block: B:19:0x0036, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized AlarmResource getInstance() {
        synchronized (AlarmResource.class) {
            if (mAlarmResource == null) {
                mAlarmResource = new AlarmResource();
            }
            if (mAlarmResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
                AlarmResource alarmResource = mAlarmResource;
                return alarmResource;
            } else if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
        }
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int strategy = 1;
        int typeID = super.getTypeId(callingUid, pkg, processTpye);
        if (!(this.mResourceConfig == null || !isResourceCountOverload(callingUid, pkg, typeID, count) || (isInWhiteList(pkg, 0) ^ 1) == 0)) {
            strategy = this.mResourceConfig[typeID].getResourceStrategy();
            if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadStrategy CountOverload = " + strategy);
            }
            if (typeID == 2 && (Utils.DEBUG || Utils.HWFLOW)) {
                Log.i(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        return strategy;
    }

    protected Bundle createBundleForResource(long id, int typeID, ResourceConfig config, ResourceCountControl mResourceCountControl, String pkg) {
        if (config == null || mResourceCountControl == null) {
            return null;
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "createBundleForResource Call In AlarmResource");
        }
        Bundle bundle = new Bundle();
        bundle.putInt(Utils.BUNDLE_HARD_THRESHOLD, config.getResouceUrgentThreshold());
        bundle.putInt(Utils.BUNDLE_CURRENT_COUNT, mResourceCountControl.getTotalCount(id));
        bundle.putBoolean(Utils.BUNDLE_IS_IN_WHITELIST, isInWhiteList(pkg, 0));
        return bundle;
    }
}
