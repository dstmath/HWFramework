package android.rms.resource;

import android.rms.HwSysResImpl;
import android.rms.iaware.AwareLog;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;
import java.util.Map.Entry;

public final class OrderedBroadcastObserveResource extends HwSysResImpl {
    private static final int BIGDATA_SCALE = 25;
    private static final int CONFIG_NUM = 0;
    private static final long ONE_DAY_MM = 86400000;
    private static final String TAG = "RMS.OrderedBrObserveRes";
    private static volatile OrderedBroadcastObserveResource mOrderedBroadcastObserveResource;
    private long mNextTimeForUpdate = (System.currentTimeMillis() + 86400000);
    private final HashMap<String, Integer> mResourceRecordMap = new HashMap();

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
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "RMS not ready!");
        }
        return null;
    }

    public int acquire(int callingUid, String target, int processTpye) {
        if (this.mResourceConfig != null) {
            synchronized (this.mResourceRecordMap) {
                int resourceMaxLength = this.mResourceConfig[0].getResourceThreshold();
                Integer times = (Integer) this.mResourceRecordMap.get(target);
                if (times != null) {
                    times = Integer.valueOf(times.intValue() + 1);
                } else {
                    times = Integer.valueOf(1);
                }
                this.mResourceRecordMap.put(target, times);
                long now = System.currentTimeMillis();
                if (now > this.mNextTimeForUpdate || resourceMaxLength < this.mResourceRecordMap.size()) {
                    this.mNextTimeForUpdate = 86400000 + now;
                    triggertoLogIntoJank();
                }
            }
        } else {
            AwareLog.e(TAG, "can't read config file");
        }
        return 1;
    }

    private void triggertoLogIntoJank() {
        for (Entry<String, Integer> entry : this.mResourceRecordMap.entrySet()) {
            this.mResourceManger.recordResourceOverloadStatus(0, (String) entry.getKey(), 31, ((Integer) entry.getValue()).intValue() * 25, 0, 0, null);
        }
        this.mResourceRecordMap.clear();
    }

    protected boolean needUpdateWhiteList() {
        return false;
    }
}
