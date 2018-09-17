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

    protected boolean isResourceCountOverload(int callingUid, String pkg, int typeID, int count) {
        long id = super.getResourceId(callingUid, pkg, typeID);
        ResourceConfig config = this.mResourceConfig[typeID];
        int softThreshold = config.getResouceWarningThreshold();
        int hardThreshold = config.getResouceUrgentThreshold();
        int normalThreshold = config.getResouceNormalThreshold();
        int timeInterval = config.getLoopInterval();
        int totalTimeInterval = config.getTotalLoopInterval();
        if (this.mResourceCountControl.checkCountOverload(id, softThreshold, hardThreshold, normalThreshold, count, this.mResourceType)) {
            if (Utils.DEBUG || Utils.HWFLOW) {
                Log.i(this.mTag, "HwSysCountRes is threshold Overload  id=" + id + " CurrentCount =" + count + " softThreshold=" + softThreshold + " hardThreshold=" + hardThreshold);
            }
            if (this.mResourceCountControl.isReportTime(id, timeInterval, this.mPreReportTime, totalTimeInterval)) {
                int i = callingUid;
                String str = pkg;
                this.mResourceManger.recordResourceOverloadStatus(i, str, this.mResourceType, this.mResourceCountControl.getOverloadNumber(id), 0, this.mResourceCountControl.getTotalCount(id), createBundleForResource(id, typeID, config, this.mResourceCountControl, pkg));
                this.mPreReportTime = SystemClock.uptimeMillis();
            }
            if (this.mResourceCountControl.getTotalCount(id) > hardThreshold) {
                return true;
            }
        }
        return false;
    }

    protected boolean isResourceCountOverload(int callingUid, String pkg, int typeID) {
        return isResourceCountOverload(callingUid, pkg, typeID, -1);
    }

    protected Bundle createBundleForResource(long id, int typeID, ResourceConfig config, ResourceCountControl mResourceCountControl, String pkg) {
        return null;
    }
}
