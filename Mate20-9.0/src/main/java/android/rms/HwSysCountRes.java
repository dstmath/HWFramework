package android.rms;

import android.os.Bundle;
import android.os.SystemClock;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceCountControl;
import android.rms.utils.Utils;
import android.util.Log;

public class HwSysCountRes extends HwSysResImpl {
    private static final String TAG = "RMS.HwSysCountRes";
    protected long mPreReportTime;
    protected ResourceCountControl mResourceCountControl;
    private String mTag;

    protected HwSysCountRes(int resourceType, String tag, int[] whiteListTypes) {
        super(resourceType, tag, whiteListTypes);
        this.mTag = TAG;
        this.mResourceCountControl = new ResourceCountControl();
        this.mPreReportTime = 0;
        this.mTag = tag;
    }

    protected HwSysCountRes(int resourceType, String tag) {
        this(resourceType, tag, new int[]{0});
    }

    /* access modifiers changed from: protected */
    public boolean isResourceCountOverload(int callingUid, String pkg, int typeID, int count) {
        long id = super.getResourceId(callingUid, pkg, typeID);
        ResourceConfig config = this.mResourceConfig[typeID];
        int softThreshold = config.getResouceWarningThreshold();
        int hardThreshold = config.getResouceUrgentThreshold();
        int normalThreshold = config.getResouceNormalThreshold();
        int timeInterval = config.getLoopInterval();
        int totalTimeInterval = config.getTotalLoopInterval();
        if (this.mResourceCountControl.checkCountOverload(id, softThreshold, hardThreshold, normalThreshold, count, this.mResourceType)) {
            if (Utils.DEBUG || Utils.HWFLOW) {
                String str = this.mTag;
                Log.i(str, "HwSysCountRes is threshold Overload  id=" + id + " CurrentCount =" + count + " softThreshold=" + softThreshold + " hardThreshold=" + hardThreshold);
            } else {
                int i = count;
            }
            int hardThreshold2 = hardThreshold;
            int i2 = softThreshold;
            long id2 = id;
            ResourceConfig config2 = config;
            if (this.mResourceCountControl.isReportTime(id, timeInterval, this.mPreReportTime, totalTimeInterval)) {
                int overloadNum = this.mResourceCountControl.getOverloadNumber(id2);
                int totalCount = this.mResourceCountControl.getTotalCount(id2);
                Bundle mBundle = createBundleForResource(id2, typeID, config2, this.mResourceCountControl, pkg);
                this.mResourceManger.recordResourceOverloadStatus(callingUid, pkg, this.mResourceType, overloadNum, 0, totalCount, mBundle);
                this.mPreReportTime = SystemClock.uptimeMillis();
            }
            if (this.mResourceCountControl.getTotalCount(id2) > hardThreshold2) {
                return true;
            }
        } else {
            int i3 = count;
            int i4 = hardThreshold;
            int i5 = softThreshold;
            long j = id;
            ResourceConfig resourceConfig = config;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isResourceCountOverload(int callingUid, String pkg, int typeID) {
        return isResourceCountOverload(callingUid, pkg, typeID, -1);
    }

    /* access modifiers changed from: protected */
    public int getCount(int callingUid, String pkg, int typeID) {
        return this.mResourceCountControl.getCount(super.getResourceId(callingUid, pkg, typeID));
    }

    /* access modifiers changed from: protected */
    public Bundle createBundleForResource(long id, int typeID, ResourceConfig config, ResourceCountControl mResourceCountControl2, String pkg) {
        return null;
    }
}
