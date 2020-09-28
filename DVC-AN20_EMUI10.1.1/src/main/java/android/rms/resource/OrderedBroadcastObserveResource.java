package android.rms.resource;

import android.rms.HwSysResImpl;
import android.rms.iaware.DataContract;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public final class OrderedBroadcastObserveResource extends HwSysResImpl {
    private static final int BIGDATA_SCALE = 25;
    private static final int CONFIG_NUM = 0;
    private static final long ONE_DAY_MM = 86400000;
    private static final String TAG = "RMS.OrderedBrObserveRes";
    private static volatile OrderedBroadcastObserveResource mOrderedBroadcastObserveResource;
    private long mNextTimeForUpdate = (System.currentTimeMillis() + 86400000);
    private final HashMap<String, Integer> mResourceRecordMap = new HashMap<>();

    private OrderedBroadcastObserveResource() {
        super(31, TAG, null);
    }

    public static OrderedBroadcastObserveResource getInstance() {
        if (mOrderedBroadcastObserveResource == null) {
            mOrderedBroadcastObserveResource = new OrderedBroadcastObserveResource();
        }
        if (mOrderedBroadcastObserveResource.getConfig()) {
            if (Utils.DEBUG) {
                Log.d(TAG, "getInstance create new resource");
            }
            return mOrderedBroadcastObserveResource;
        } else if (!Utils.DEBUG) {
            return null;
        } else {
            Log.d(TAG, "RMS not ready!");
            return null;
        }
    }

    @Override // android.rms.HwSysResImpl
    public int acquire(int callingUid, String target, int processTpye) {
        Integer times;
        if (this.mResourceConfig != null) {
            synchronized (this.mResourceRecordMap) {
                int resourceMaxLength = this.mResourceConfig[0].getResourceThreshold();
                Integer times2 = this.mResourceRecordMap.get(target);
                if (times2 != null) {
                    times = Integer.valueOf(times2.intValue() + 1);
                } else {
                    times = 1;
                }
                this.mResourceRecordMap.put(target, times);
                long now = System.currentTimeMillis();
                if (now > this.mNextTimeForUpdate || resourceMaxLength < this.mResourceRecordMap.size()) {
                    this.mNextTimeForUpdate = 86400000 + now;
                    triggertoLogIntoJank();
                }
            }
        } else {
            Log.e(TAG, "can't read config file");
        }
        return 1;
    }

    private void triggertoLogIntoJank() {
        for (Map.Entry<String, Integer> entry : this.mResourceRecordMap.entrySet()) {
            Map<String, Object> overloadStatus = new HashMap<>(7);
            overloadStatus.put(DataContract.BaseProperty.UID, 0);
            overloadStatus.put("pkg", (String) entry.getKey());
            overloadStatus.put("resourceType", 31);
            overloadStatus.put("overloadNum", Integer.valueOf(entry.getValue().intValue() * 25));
            overloadStatus.put("speedOverLoadPeriod", 0);
            overloadStatus.put("totalNum", 0);
            overloadStatus.put("bundleArgs", null);
            this.mResourceManger.recordResourceOverloadStatus(overloadStatus);
        }
        this.mResourceRecordMap.clear();
    }

    /* access modifiers changed from: protected */
    @Override // android.rms.HwSysResImpl
    public boolean needUpdateWhiteList() {
        return false;
    }
}
