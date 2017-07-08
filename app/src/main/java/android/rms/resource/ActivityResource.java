package android.rms.resource;

import android.os.SystemClock;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.config.ResourceConfig;
import android.util.Log;
import java.util.ArrayList;

public final class ActivityResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "ActivityResource";
    private static ActivityResource mActivityResource;
    private static ResourceConfig[] mResourceConfig;
    private static HwSysResManager mResourceManger;
    private long mPreReportTime;
    private ArrayList<String> mWhiteList;

    public ActivityResource() {
        this.mPreReportTime = 0;
        this.mWhiteList = null;
        this.mWhiteList = super.getResWhiteList(36, 0);
    }

    public static synchronized ActivityResource getInstance() {
        ActivityResource activityResource;
        synchronized (ActivityResource.class) {
            if (mActivityResource == null && getConfig(36)) {
                mActivityResource = new ActivityResource();
            }
            activityResource = mActivityResource;
        }
        return activityResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int typeID = isWhiteList(pkg) ? 2 : super.getTypeId(callingUid, pkg, processTpye);
        if (mResourceConfig == null || !isResourceCountOverload(callingUid, pkg, typeID, count)) {
            return 1;
        }
        return mResourceConfig[typeID].getResourceStrategy();
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
        int softThreshold = config.getResourceThreshold();
        int timeInterval = config.getLoopInterval();
        long currentTime = SystemClock.uptimeMillis();
        int hardThreshold = config.getResourceMaxPeroid();
        if (count <= softThreshold) {
            return DEBUG;
        }
        if (Log.HWINFO) {
            Log.i(TAG, "Activity is Overload  id=" + id + " OverloadNumber=" + count + " softThreshold=" + softThreshold + " hardThreshold=" + hardThreshold);
        }
        if (isReportTime(callingUid, typeID, currentTime, timeInterval) || count >= hardThreshold) {
            mResourceManger.recordResourceOverloadStatus(callingUid, pkg, 36, 0, hardThreshold, count);
            this.mPreReportTime = currentTime;
        }
        return true;
    }

    private boolean isReportTime(int callingUid, int typeID, long currentTime, int timeInterval) {
        if (typeID == 3 || currentTime - this.mPreReportTime <= ((long) timeInterval)) {
            return DEBUG;
        }
        return true;
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
