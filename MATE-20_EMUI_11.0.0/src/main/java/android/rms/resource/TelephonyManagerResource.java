package android.rms.resource;

import android.rms.HwSysSpeedRes;
import android.rms.utils.Utils;
import android.util.Log;

public final class TelephonyManagerResource extends HwSysSpeedRes {
    private static final String TAG = "RMS.TelephonyManagerResource";
    private static TelephonyManagerResource telephonyManagerResource;

    private TelephonyManagerResource() {
        super(34, TAG);
    }

    public static synchronized TelephonyManagerResource getInstance() {
        synchronized (TelephonyManagerResource.class) {
            if (telephonyManagerResource == null) {
                telephonyManagerResource = new TelephonyManagerResource();
            }
            if (telephonyManagerResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
                return telephonyManagerResource;
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
            return null;
        }
    }

    @Override // android.rms.HwSysResImpl
    public int acquire(int callingUid, String pkg, int processType) {
        int strategy = 1;
        if (pkg == null) {
            return 1;
        }
        int typeId = super.getTypeId(callingUid, pkg, processType);
        if (Utils.DEBUG) {
            Log.d(TAG, "getOverloadStrategy = 1 callingUid=" + callingUid + " pkg=" + pkg + " processTpye=" + processType + " typeID=" + typeId);
        }
        if (this.mResourceConfig != null && isResourceSpeedOverload(callingUid, pkg, typeId) && !isInWhiteList(pkg, 0)) {
            strategy = getSpeedOverloadStrategy(typeId);
            if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadStrategy = " + strategy);
            }
        }
        return strategy;
    }

    /* access modifiers changed from: protected */
    @Override // android.rms.HwSysResImpl
    public boolean needUpdateWhiteList() {
        return false;
    }
}
