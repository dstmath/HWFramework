package android.rms.resource;

import android.rms.HwSysCountRes;
import android.rms.utils.Utils;
import android.util.Log;

public final class ReceiverResource extends HwSysCountRes {
    private static final String TAG = "RMS.ReceiverResource";
    private static ReceiverResource mReceiverResource;

    private ReceiverResource() {
        super(12, TAG);
    }

    /* JADX WARNING: Missing block: B:19:0x0036, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized ReceiverResource getInstance() {
        synchronized (ReceiverResource.class) {
            if (mReceiverResource == null) {
                mReceiverResource = new ReceiverResource();
            }
            if (mReceiverResource.getConfig()) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
                ReceiverResource receiverResource = mReceiverResource;
                return receiverResource;
            } else if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
        }
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int strategy = 1;
        int typeID = super.getTypeId(callingUid, pkg, processTpye);
        if (this.mResourceConfig != null && isResourceCountOverload(callingUid, pkg, typeID, count)) {
            strategy = this.mResourceConfig[typeID].getResourceStrategy();
            if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadStrategy CountOverload = " + strategy);
            }
            if (typeID == 2 || isInWhiteList(pkg, 0)) {
                if (Utils.DEBUG || Utils.HWFLOW) {
                    Log.i(TAG, Log.getStackTraceString(new Throwable()));
                }
                return 1;
            }
        }
        return strategy;
    }

    protected boolean needUpdateWhiteList() {
        return false;
    }
}
