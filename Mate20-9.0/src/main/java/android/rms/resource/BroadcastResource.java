package android.rms.resource;

import android.rms.HwSysSpeedRes;
import android.rms.utils.Utils;
import android.util.Log;

public final class BroadcastResource extends HwSysSpeedRes {
    private static final String TAG = "RMS.BroadcastResource";
    private static BroadcastResource mBroadcastResource;
    private static final int[] mWhiteListTypes = {1, 2};

    private BroadcastResource() {
        super(11, TAG, mWhiteListTypes);
    }

    public static synchronized BroadcastResource getInstance() {
        synchronized (BroadcastResource.class) {
            if (mBroadcastResource == null) {
                mBroadcastResource = new BroadcastResource();
            }
            if (mBroadcastResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
                BroadcastResource broadcastResource = mBroadcastResource;
                return broadcastResource;
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
            return null;
        }
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        int strategy = 1;
        if (this.mResourceConfig != null) {
            int typeID = super.getTypeId(callingUid, pkg, processTpye);
            if (isResourceSpeedOverload(callingUid, pkg, typeID)) {
                strategy = getSpeedOverloadStrategy(typeID);
                if (Utils.DEBUG) {
                    Log.d(TAG, "getOverloadStrategy = " + strategy);
                }
            }
        }
        return strategy;
    }

    public int queryPkgPolicy(int type, int value, String key) {
        switch (type) {
            case 1:
                if (super.isInWhiteList(key, 1)) {
                    return 1;
                }
                return 4;
            case 2:
                if (super.isInWhiteList(key, 2)) {
                    return 1;
                }
                return 4;
            default:
                return 0;
        }
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceFlowControl.removeResourceSpeedRecord(super.getResourceId(callingUid, pkg, processTpye));
        this.mResourceManger.clearResourceStatus(callingUid, 11);
    }
}
