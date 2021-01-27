package android.rms.resource;

import android.rms.HwSysSpeedRes;
import android.rms.utils.Utils;
import android.util.Log;

public final class BroadcastResource extends HwSysSpeedRes {
    private static final String TAG = "RMS.BroadcastResource";
    private static final int[] WHITE_LIST_TYPES = {1, 2};
    private static BroadcastResource broadcastResource;

    private BroadcastResource() {
        super(11, TAG, WHITE_LIST_TYPES);
    }

    public static synchronized BroadcastResource getInstance() {
        synchronized (BroadcastResource.class) {
            if (broadcastResource == null) {
                broadcastResource = new BroadcastResource();
            }
            if (broadcastResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
                return broadcastResource;
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
            return null;
        }
    }

    @Override // android.rms.HwSysResImpl
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

    @Override // android.rms.HwSysResImpl
    public int queryPkgPolicy(int type, int value, String key) {
        if (type != 1) {
            if (type != 2) {
                return 0;
            }
            if (super.isInWhiteList(key, 2)) {
                return 1;
            }
            return 4;
        } else if (super.isInWhiteList(key, 1)) {
            return 1;
        } else {
            return 4;
        }
    }

    @Override // android.rms.HwSysResImpl
    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceFlowControl.removeResourceSpeedRecord(super.getResourceId(callingUid, pkg, processTpye));
        this.mResourceManger.clearResourceStatus(callingUid, 11);
    }
}
