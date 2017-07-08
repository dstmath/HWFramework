package android.rms.resource;

import android.os.SystemClock;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.config.ResourceConfig;
import android.util.Log;
import java.util.ArrayList;

public final class ReceiverResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "ReceiverResource";
    private static ReceiverResource mReceiverResource;
    private static ResourceConfig[] mResourceConfig;
    private static HwSysResManager mResourceManger;
    private int mOverloadNum;
    private long mPreReportTime;
    private ArrayList<String> mWhiteList;

    public ReceiverResource() {
        this.mOverloadNum = 0;
        this.mPreReportTime = 0;
        this.mWhiteList = null;
        this.mWhiteList = super.getResWhiteList(12, 0);
    }

    public static synchronized ReceiverResource getInstance() {
        ReceiverResource receiverResource;
        synchronized (ReceiverResource.class) {
            if (mReceiverResource == null && getConfig(12)) {
                mReceiverResource = new ReceiverResource();
            }
            receiverResource = mReceiverResource;
        }
        return receiverResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int strategy = 1;
        int typeID = super.getTypeId(callingUid, pkg, processTpye);
        if (mResourceConfig != null && isResourceCountOverload(callingUid, pkg, typeID, count)) {
            strategy = mResourceConfig[typeID].getResourceStrategy();
            if (typeID == 2 || isWhiteList(pkg)) {
                if (Log.HWINFO) {
                    Log.i(TAG, Log.getStackTraceString(new Throwable()));
                }
                return 1;
            }
        }
        return strategy;
    }

    private static boolean getConfig(int resourceType) {
        mResourceManger = HwSysResManager.getInstance();
        mResourceConfig = mResourceManger.getResourceConfig(resourceType);
        if (mResourceConfig != null) {
            return true;
        }
        return DEBUG;
    }

    private boolean isResourceCountOverload(int callingUid, String pkg, int typeID, int count) {
        long id = super.getResourceId(callingUid, pkg, typeID);
        ResourceConfig config = mResourceConfig[typeID];
        int threshold = config.getResourceThreshold();
        int reportInterval = config.getLoopInterval();
        long currentTime = SystemClock.uptimeMillis();
        if (count <= threshold) {
            return DEBUG;
        }
        this.mOverloadNum++;
        if (Log.HWINFO) {
            Log.i(TAG, "Receiver is Overload  id=" + id + " OverloadNumber=" + count + " OverloadNum=" + this.mOverloadNum);
        }
        if (isReportTime(callingUid, typeID, currentTime, reportInterval)) {
            mResourceManger.recordResourceOverloadStatus(callingUid, pkg, 12, 0, 0, this.mOverloadNum);
            this.mPreReportTime = currentTime;
        }
        return true;
    }

    private boolean isReportTime(int callingUid, int typeID, long currentTime, int timeInterval) {
        return currentTime - this.mPreReportTime > ((long) timeInterval) ? true : DEBUG;
    }

    private boolean isWhiteList(String pkg) {
        if (pkg == null) {
            return DEBUG;
        }
        for (String proc : this.mWhiteList) {
            if (pkg.contains(proc)) {
                return true;
            }
        }
        return DEBUG;
    }
}
