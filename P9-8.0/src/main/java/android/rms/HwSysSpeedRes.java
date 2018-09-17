package android.rms;

import android.os.Bundle;
import android.os.SystemClock;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceFlowControl;
import android.rms.utils.Utils;
import android.util.Log;

public class HwSysSpeedRes extends HwSysResImpl {
    private static final String TAG = "RMS.HwSysSpeedRes";
    private static int[] mWhiteListTypes = new int[]{0};
    protected int mOverloadNumber;
    protected int mOverloadPeriod;
    protected long mPreReportTime;
    protected ResourceFlowControl mResourceFlowControl;
    private String mTag;

    protected HwSysSpeedRes(int resourceType, String tag) {
        this(resourceType, tag, mWhiteListTypes);
    }

    protected HwSysSpeedRes(int resourceType, String tag, int[] mWhiteListTypes) {
        super(resourceType, tag, mWhiteListTypes);
        this.mTag = TAG;
        this.mPreReportTime = 0;
        this.mResourceFlowControl = new ResourceFlowControl();
        this.mTag = tag;
    }

    protected int getSpeedOverloadStrategy(int typeID) {
        int strategy = this.mResourceConfig[typeID].getResourceStrategy();
        int maxPeriod = this.mResourceConfig[typeID].getResourceMaxPeroid();
        if (Utils.DEBUG) {
            Log.d(TAG, "getOverloadStrategy  resource_strategy /" + strategy + " mOverloadPeriod/" + this.mOverloadPeriod + " MaxPeriod/" + maxPeriod);
        }
        if (this.mOverloadPeriod >= maxPeriod) {
            return strategy;
        }
        return 1;
    }

    protected boolean isResourceSpeedOverload(int callingUid, String pkg, int typeID) {
        long id = super.getResourceId(callingUid, pkg, typeID);
        ResourceConfig config = this.mResourceConfig[typeID];
        int threshold = config.getResourceThreshold();
        int loopInterval = config.getLoopInterval();
        if (this.mResourceFlowControl.checkSpeedOverload(id, threshold, loopInterval)) {
            int maxPeriod = config.getResourceMaxPeroid();
            int totalTimeInterval = config.getTotalLoopInterval();
            this.mOverloadPeriod = this.mResourceFlowControl.getOverloadPeroid(id);
            if (this.mOverloadPeriod >= maxPeriod) {
                if (this.mResourceFlowControl.isReportTime(id, loopInterval, this.mPreReportTime, totalTimeInterval) || this.mResourceType == 18) {
                    Bundle bundle = createBundleForResource(id, typeID, config, this.mResourceFlowControl);
                    this.mOverloadNumber = this.mResourceFlowControl.getOverloadNumber(id);
                    int i = callingUid;
                    String str = pkg;
                    this.mResourceManger.recordResourceOverloadStatus(i, str, this.mResourceType, this.mOverloadNumber, this.mOverloadPeriod, this.mResourceFlowControl.getCountInPeroid(id), bundle);
                    this.mPreReportTime = SystemClock.uptimeMillis();
                    Log.i(this.mTag, "HwSysSpeedRes is threshold Overload  id=" + id + " pkg=" + pkg + " threshold=" + threshold + " OverloadPeriod=" + this.mOverloadPeriod + " maxPeriod=" + maxPeriod);
                }
                return true;
            }
        }
        return false;
    }

    protected Bundle createBundleForResource(long id, int typeID, ResourceConfig config, ResourceFlowControl resourceFlowControl) {
        return null;
    }
}
