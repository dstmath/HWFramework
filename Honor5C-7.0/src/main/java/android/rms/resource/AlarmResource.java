package android.rms.resource;

import android.os.SystemClock;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.config.ResourceConfig;
import android.util.Log;

public final class AlarmResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "AlarmResource";
    private static AlarmResource mAlarmResource;
    private static ResourceConfig[] mResourceConfig;
    private static HwSysResManager mResourceManger;
    private long mPreReportTime;
    private int mPreReportUid;

    public AlarmResource() {
        this.mPreReportUid = 0;
        this.mPreReportTime = 0;
    }

    public static synchronized AlarmResource getInstance() {
        AlarmResource alarmResource;
        synchronized (AlarmResource.class) {
            if (mAlarmResource == null && getConfig(13)) {
                mAlarmResource = new AlarmResource();
            }
            alarmResource = mAlarmResource;
        }
        return alarmResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int strategy = 1;
        int typeID = super.getTypeId(callingUid, pkg, processTpye);
        if (mResourceConfig != null && isResourceCountOverload(callingUid, pkg, typeID, count)) {
            strategy = mResourceConfig[typeID].getResourceStrategy();
            if (typeID == 2 && Log.HWINFO) {
                Log.i(TAG, Log.getStackTraceString(new Throwable()));
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
        int timeInterval = config.getLoopInterval();
        long currentTime = SystemClock.uptimeMillis();
        if (count <= threshold) {
            return DEBUG;
        }
        if (Log.HWINFO) {
            Log.i(TAG, "Alarm is Overload  id=" + id + " OverloadNumber=" + count + " threshold=" + threshold);
        }
        if (isReportTime(callingUid, typeID, currentTime, timeInterval)) {
            mResourceManger.recordResourceOverloadStatus(callingUid, pkg, 13, 0, 0, count);
            this.mPreReportUid = callingUid;
            this.mPreReportTime = currentTime;
        }
        return true;
    }

    public boolean isReportTime(int callingUid, int typeID, long currentTime, int timeInterval) {
        if (typeID == 3 || this.mPreReportUid == callingUid || currentTime - this.mPreReportTime <= ((long) timeInterval)) {
            return DEBUG;
        }
        return true;
    }
}
