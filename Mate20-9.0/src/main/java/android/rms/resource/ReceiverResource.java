package android.rms.resource;

import android.common.HwFrameworkFactory;
import android.rms.HwSysCountRes;
import android.rms.utils.Utils;
import android.util.Log;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;

public final class ReceiverResource extends HwSysCountRes {
    private static final String TAG = "RMS.ReceiverResource";
    private static ReceiverResource mReceiverResource;

    private ReceiverResource() {
        super(12, TAG);
    }

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
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "RMS not ready!");
            }
            return null;
        }
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int strategy = 1;
        int typeID = super.getTypeId(callingUid, pkg, processTpye);
        if (Log.HWINFO) {
            IZrHung appRcv = HwFrameworkFactory.getZrHung("appeye_receiver");
            if (appRcv != null) {
                ZrHungData arg = new ZrHungData();
                arg.putString("packageName", pkg);
                arg.putInt("count", count);
                appRcv.sendEvent(arg);
            }
        }
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

    /* access modifiers changed from: protected */
    public boolean needUpdateWhiteList() {
        return false;
    }
}
