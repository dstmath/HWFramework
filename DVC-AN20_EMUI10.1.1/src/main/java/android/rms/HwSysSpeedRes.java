package android.rms;

import android.os.SystemClock;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceFlowControl;
import android.rms.iaware.DataContract;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class HwSysSpeedRes extends HwSysResImpl {
    private static final String TAG = "RMS.HwSysSpeedRes";
    private static int[] whiteListTypes = {0};
    protected int mOverloadNumber;
    protected int mOverloadPeriod;
    protected long mPreReportTime;
    protected ResourceFlowControl mResourceFlowControl;
    private String mTag;

    protected HwSysSpeedRes(int resourceType, String tag) {
        this(resourceType, tag, whiteListTypes);
    }

    protected HwSysSpeedRes(int resourceType, String tag, int[] whiteListTypes2) {
        super(resourceType, tag, whiteListTypes2);
        this.mPreReportTime = 0;
        this.mTag = TAG;
        this.mResourceFlowControl = new ResourceFlowControl();
        this.mTag = tag;
    }

    /* access modifiers changed from: protected */
    public int getSpeedOverloadStrategy(int typeId) {
        int strategy = this.mResourceConfig[typeId].getResourceStrategy();
        int maxPeriod = this.mResourceConfig[typeId].getResourceMaxPeroid();
        if (Utils.DEBUG) {
            Log.d(TAG, "getOverloadStrategy  resource_strategy /" + strategy + " mOverloadPeriod/" + this.mOverloadPeriod + " MaxPeriod/" + maxPeriod);
        }
        if (this.mOverloadPeriod >= maxPeriod) {
            return strategy;
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public boolean isResourceSpeedOverload(int callingUid, String pkg, int typeId) {
        long id = super.getResourceId(callingUid, pkg, typeId);
        ResourceConfig config = this.mResourceConfig[typeId];
        if (config == null) {
            return false;
        }
        int threshold = config.getResourceThreshold();
        int loopInterval = config.getLoopInterval();
        if (this.mResourceFlowControl.checkSpeedOverload(id, threshold, loopInterval)) {
            int maxPeriod = config.getResourceMaxPeroid();
            int totalTimeInterval = config.getTotalLoopInterval();
            this.mOverloadPeriod = this.mResourceFlowControl.getOverloadPeroid(id);
            if (this.mOverloadPeriod >= maxPeriod) {
                if (this.mResourceType == 34) {
                    this.mOverloadNumber = this.mResourceFlowControl.getOverloadNumber(id);
                    String str = this.mTag;
                    Log.i(str, "TELEPHONY OverLoad mOverloadNumber=" + this.mOverloadNumber);
                    return true;
                }
                if (this.mResourceFlowControl.isReportTime(id, loopInterval, this.mPreReportTime, totalTimeInterval) || this.mResourceType == 18) {
                    this.mOverloadNumber = this.mResourceFlowControl.getOverloadNumber(id);
                    int countInPeriod = this.mResourceFlowControl.getCountInPeroid(id);
                    Map<String, Object> overloadStatus = new HashMap<>(7);
                    overloadStatus.put(DataContract.BaseProperty.UID, Integer.valueOf(callingUid));
                    overloadStatus.put("pkg", pkg);
                    overloadStatus.put("resourceType", Integer.valueOf(this.mResourceType));
                    overloadStatus.put("overloadNum", Integer.valueOf(this.mOverloadNumber));
                    overloadStatus.put("speedOverLoadPeriod", Integer.valueOf(maxPeriod));
                    overloadStatus.put("totalNum", Integer.valueOf(countInPeriod));
                    overloadStatus.put("bundleArgs", null);
                    this.mResourceManger.recordResourceOverloadStatus(overloadStatus);
                    this.mPreReportTime = SystemClock.uptimeMillis();
                    String str2 = this.mTag;
                    Log.i(str2, "HwSysSpeedRes is threshold Overload  id=" + id + " pkg=" + pkg + " threshold=" + threshold + " OverloadPeriod=" + this.mOverloadPeriod + " maxPeriod=" + maxPeriod);
                }
                return true;
            }
        }
        return false;
    }
}
