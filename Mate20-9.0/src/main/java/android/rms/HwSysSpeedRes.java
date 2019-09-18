package android.rms;

import android.os.Bundle;
import android.os.SystemClock;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceFlowControl;
import android.rms.utils.Utils;
import android.util.Log;

public class HwSysSpeedRes extends HwSysResImpl {
    private static final String TAG = "RMS.HwSysSpeedRes";
    private static int[] mWhiteListTypes = {0};
    protected int mOverloadNumber;
    protected int mOverloadPeriod;
    protected long mPreReportTime;
    protected ResourceFlowControl mResourceFlowControl;
    private String mTag;

    protected HwSysSpeedRes(int resourceType, String tag) {
        this(resourceType, tag, mWhiteListTypes);
    }

    protected HwSysSpeedRes(int resourceType, String tag, int[] mWhiteListTypes2) {
        super(resourceType, tag, mWhiteListTypes2);
        this.mTag = TAG;
        this.mPreReportTime = 0;
        this.mResourceFlowControl = new ResourceFlowControl();
        this.mTag = tag;
    }

    /* access modifiers changed from: protected */
    public int getSpeedOverloadStrategy(int typeID) {
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

    /* access modifiers changed from: protected */
    public boolean isResourceSpeedOverload(int callingUid, String pkg, int typeID) {
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
                    int i = loopInterval;
                    ResourceConfig resourceConfig = config;
                    Bundle bundle = createBundleForResource(id, typeID, config, this.mResourceFlowControl);
                    this.mOverloadNumber = this.mResourceFlowControl.getOverloadNumber(id);
                    int countInPeriod = this.mResourceFlowControl.getCountInPeroid(id);
                    this.mResourceManger.recordResourceOverloadStatus(callingUid, pkg, this.mResourceType, this.mOverloadNumber, this.mOverloadPeriod, countInPeriod, bundle);
                    this.mPreReportTime = SystemClock.uptimeMillis();
                    String str = this.mTag;
                    Log.i(str, "HwSysSpeedRes is threshold Overload  id=" + id + " pkg=" + pkg + " threshold=" + threshold + " OverloadPeriod=" + this.mOverloadPeriod + " maxPeriod=" + maxPeriod);
                } else {
                    int i2 = maxPeriod;
                    int i3 = loopInterval;
                    int i4 = threshold;
                    ResourceConfig resourceConfig2 = config;
                    String str2 = pkg;
                }
                return true;
            }
        }
        int i5 = threshold;
        ResourceConfig resourceConfig3 = config;
        String str3 = pkg;
        return false;
    }

    /* access modifiers changed from: protected */
    public Bundle createBundleForResource(long id, int typeID, ResourceConfig config, ResourceFlowControl resourceFlowControl) {
        return null;
    }
}
