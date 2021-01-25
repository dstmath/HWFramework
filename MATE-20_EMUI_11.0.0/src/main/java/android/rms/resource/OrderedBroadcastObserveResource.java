package android.rms.resource;

import android.rms.HwSysResImpl;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public final class OrderedBroadcastObserveResource extends HwSysResImpl {
    private static final int BIGDATA_SCALE = 25;
    private static final int CONFIG_NUM = 0;
    private static final long ONE_DAY_MM = 86400000;
    private static final String TAG = "RMS.OrderedBrObserveRes";
    private static volatile OrderedBroadcastObserveResource orderedBroadcastObserveResource;
    private long mNextTimeForUpdate = (System.currentTimeMillis() + ONE_DAY_MM);
    private final HashMap<String, Integer> mResourceRecordMap = new HashMap<>();

    private OrderedBroadcastObserveResource() {
        super(31, TAG, null);
    }

    public static OrderedBroadcastObserveResource getInstance() {
        if (orderedBroadcastObserveResource == null) {
            orderedBroadcastObserveResource = new OrderedBroadcastObserveResource();
        }
        if (orderedBroadcastObserveResource.getConfig()) {
            if (Utils.DEBUG) {
                Log.d(TAG, "getInstance create new resource");
            }
            return orderedBroadcastObserveResource;
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
                    this.mNextTimeForUpdate = ONE_DAY_MM + now;
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
            Map<String, Object> mapParam = new HashMap<>(7);
            mapParam.put("uid", 0);
            mapParam.put("pkg", (String) entry.getKey());
            mapParam.put("resourceType", 31);
            mapParam.put("overloadNum", Integer.valueOf(entry.getValue().intValue() * BIGDATA_SCALE));
            mapParam.put("speedOverLoadPeriod", 0);
            mapParam.put("totalNum", 0);
            mapParam.put("bundleArgs", null);
            this.mResourceManger.recordResourceOverloadStatus(mapParam);
        }
        this.mResourceRecordMap.clear();
    }

    /* access modifiers changed from: protected */
    @Override // android.rms.HwSysResImpl
    public boolean needUpdateWhiteList() {
        return false;
    }
}
