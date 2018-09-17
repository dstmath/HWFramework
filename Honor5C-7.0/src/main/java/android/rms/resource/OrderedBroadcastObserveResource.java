package android.rms.resource;

import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.config.ResourceConfig;
import android.rms.iaware.AwareLog;
import java.util.HashMap;
import java.util.Map.Entry;

public final class OrderedBroadcastObserveResource extends HwSysResImpl {
    private static final int BIGDATA_SCALE = 25;
    private static final int CONFIG_NUM = 0;
    private static final long ONE_DAY_MM = 86400000;
    private static final String TAG = "OrderedBrObserveRes";
    private static volatile OrderedBroadcastObserveResource mOrderedBroadcastObserveResource;
    private static ResourceConfig mResourceConfig;
    private static HwSysResManager mResourceManger;
    private long mNextTimeForUpdate;
    private final HashMap<String, Integer> mResourceRecordMap;

    private OrderedBroadcastObserveResource() {
        this.mResourceRecordMap = new HashMap();
        this.mNextTimeForUpdate = System.currentTimeMillis() + ONE_DAY_MM;
    }

    public static OrderedBroadcastObserveResource getInstance() {
        if (mOrderedBroadcastObserveResource == null && getConfig(37)) {
            synchronized (OrderedBroadcastObserveResource.class) {
                if (mOrderedBroadcastObserveResource == null) {
                    mOrderedBroadcastObserveResource = new OrderedBroadcastObserveResource();
                }
            }
        }
        return mOrderedBroadcastObserveResource;
    }

    public int acquire(int callingUid, String target, int processTpye) {
        if (mResourceConfig != null) {
            synchronized (this.mResourceRecordMap) {
                int resourceMaxLength = mResourceConfig.getResourceThreshold();
                Integer times = (Integer) this.mResourceRecordMap.get(target);
                if (times != null) {
                    times = Integer.valueOf(times.intValue() + 1);
                } else {
                    times = Integer.valueOf(1);
                }
                this.mResourceRecordMap.put(target, times);
                long now = System.currentTimeMillis();
                if (now > this.mNextTimeForUpdate || resourceMaxLength < this.mResourceRecordMap.size()) {
                    this.mNextTimeForUpdate = ONE_DAY_MM + now;
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
            mResourceManger.recordResourceOverloadStatus(CONFIG_NUM, (String) entry.getKey(), 37, ((Integer) entry.getValue()).intValue() * BIGDATA_SCALE, CONFIG_NUM, CONFIG_NUM);
        }
        this.mResourceRecordMap.clear();
    }

    private static boolean getConfig(int resourceType) {
        mResourceManger = HwSysResManager.getInstance();
        ResourceConfig[] config = mResourceManger.getResourceConfig(resourceType);
        if (config != null) {
            mResourceConfig = config[CONFIG_NUM];
            return true;
        }
        AwareLog.e(TAG, "can't read config");
        return false;
    }
}
