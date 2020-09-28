package android.rms;

import android.os.SystemClock;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceCountControl;
import android.rms.iaware.DataContract;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class HwSysCountRes extends HwSysResImpl {
    private static final String TAG = "RMS.HwSysCountRes";
    protected long mPreReportTime;
    protected ResourceCountControl mResourceCountControl;
    private String mTag;

    protected HwSysCountRes(int resourceType, String tag, int[] whiteListTypes) {
        super(resourceType, tag, whiteListTypes);
        this.mResourceCountControl = new ResourceCountControl();
        this.mPreReportTime = 0;
        this.mTag = TAG;
        this.mTag = tag;
    }

    protected HwSysCountRes(int resourceType, String tag) {
        this(resourceType, tag, new int[]{0});
    }

    /* access modifiers changed from: protected */
    public boolean isResourceCountOverload(int callingUid, String pkg, int typeId, int count) {
        long id = super.getResourceId(callingUid, pkg, typeId);
        ResourceConfig config = this.mResourceConfig[typeId];
        int softThreshold = config.getResouceWarningThreshold();
        int hardThreshold = config.getResouceUrgentThreshold();
        int normalThreshold = config.getResouceNormalThreshold();
        int timeInterval = config.getLoopInterval();
        int totalTimeInterval = config.getTotalLoopInterval();
        if (this.mResourceCountControl.checkCountOverload(id, softThreshold, hardThreshold, normalThreshold, count)) {
            if (Utils.DEBUG || Utils.HWFLOW) {
                String str = this.mTag;
                Log.i(str, "HwSysCountRes is threshold Overload  id=" + id + " CurrentCount =" + count + " softThreshold=" + softThreshold + " hardThreshold=" + hardThreshold);
            }
            if (this.mResourceCountControl.isReportTime(id, timeInterval, this.mPreReportTime, totalTimeInterval)) {
                int overloadNum = this.mResourceCountControl.getOverloadNumber(id);
                int totalCount = this.mResourceCountControl.getTotalCount(id);
                Map<String, Object> paramsMap = new HashMap<>(7);
                paramsMap.put(DataContract.BaseProperty.UID, Integer.valueOf(callingUid));
                paramsMap.put("pkg", pkg);
                paramsMap.put("resourceType", Integer.valueOf(this.mResourceType));
                paramsMap.put("overloadNum", Integer.valueOf(overloadNum));
                paramsMap.put("speedOverLoadPeriod", 0);
                paramsMap.put("totalNum", Integer.valueOf(totalCount));
                paramsMap.put("bundleArgs", null);
                this.mResourceManger.recordResourceOverloadStatus(paramsMap);
                this.mPreReportTime = SystemClock.uptimeMillis();
            }
            if (this.mResourceCountControl.getTotalCount(id) > hardThreshold) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isResourceCountOverload(int callingUid, String pkg, int typeId) {
        return isResourceCountOverload(callingUid, pkg, typeId, -1);
    }

    /* access modifiers changed from: protected */
    public int getCount(int callingUid, String pkg, int typeId) {
        return this.mResourceCountControl.getCount(super.getResourceId(callingUid, pkg, typeId));
    }
}
