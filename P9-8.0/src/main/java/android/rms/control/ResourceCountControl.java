package android.rms.control;

import android.os.SystemClock;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;

public class ResourceCountControl {
    private static final String TAG = "RMS.ResourceCountControl";
    private final HashMap<Long, RecordReourceCount> mResourceCountMap = new HashMap();

    static final class RecordReourceCount {
        private int mCount;
        private int mOverLoadNum;
        private long mReportTimeMillis;
        private int mTotalCount;
        private boolean mWaterFlag = true;

        RecordReourceCount(int totalCount, int count, int overLoadNum, long timeMills) {
            this.mTotalCount = totalCount;
            this.mCount = count;
            this.mOverLoadNum = overLoadNum;
            this.mReportTimeMillis = timeMills;
        }
    }

    /* JADX WARNING: Missing block: B:20:0x008f, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:33:0x00a8, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkCountOverload(long id, int threshold, int hardThreshold, int waterThreshold, int total, int resourceType) {
        if (Utils.DEBUG) {
            Log.d(TAG, "checkCountOverload:threshold=" + threshold + " / id=" + id);
        }
        RecordReourceCount record = getResourceCountRecord(id, true);
        synchronized (record) {
            if (-1 == total) {
                record.mCount = record.mCount + 1;
            } else {
                record.mCount = total;
            }
            if (record.mTotalCount < record.mCount) {
                record.mTotalCount = record.mCount;
            }
            if (!record.mWaterFlag || record.mCount <= threshold) {
                if (record.mCount <= hardThreshold) {
                    if (record.mCount < waterThreshold) {
                        record.mWaterFlag = true;
                    }
                }
            }
            record.mWaterFlag = false;
            if (Utils.DEBUG) {
                Log.d(TAG, "ResourceCountOverload: count = " + record.mCount + ", totalCount = " + record.mTotalCount);
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0020, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:15:0x0048, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getOverloadNumber(long id) {
        synchronized (this.mResourceCountMap) {
            RecordReourceCount record = (RecordReourceCount) this.mResourceCountMap.get(Long.valueOf(id));
            if (record != null) {
                int overNumber = record.mOverLoadNum;
                record.mOverLoadNum = 0;
                if (Utils.DEBUG) {
                    Log.d(TAG, "getOverloadNumber: overNumber =" + overNumber);
                }
            } else if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadNumber: don't have this record");
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x004b, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getTotalCount(long id) {
        synchronized (this.mResourceCountMap) {
            RecordReourceCount record = (RecordReourceCount) this.mResourceCountMap.get(Long.valueOf(id));
            if (record == null) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getTotalCount: don't have this record");
                }
                return 0;
            }
            int totalCount = record.mTotalCount;
            record.mTotalCount = record.mCount;
            if (Utils.DEBUG) {
                Log.d(TAG, "getTotalCount: totalCount =" + totalCount);
            }
        }
    }

    private RecordReourceCount getResourceCountRecord(long id, boolean isCreate) {
        RecordReourceCount record;
        synchronized (this.mResourceCountMap) {
            record = (RecordReourceCount) this.mResourceCountMap.get(Long.valueOf(id));
            if (record == null && isCreate) {
                record = createResourceCountRecordLocked(id);
            }
        }
        return record;
    }

    private RecordReourceCount createResourceCountRecordLocked(long id) {
        RecordReourceCount record = new RecordReourceCount(0, 0, 0, SystemClock.uptimeMillis());
        this.mResourceCountMap.put(Long.valueOf(id), record);
        return record;
    }

    public void removeResourceCountRecord(long id) {
        synchronized (this.mResourceCountMap) {
            if (((RecordReourceCount) this.mResourceCountMap.get(Long.valueOf(id))) != null) {
                this.mResourceCountMap.remove(Long.valueOf(id));
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "removeResourceCountRecord id/" + id);
            }
        }
    }

    public void reduceCurrentCount(long id) {
        RecordReourceCount record = getResourceCountRecord(id, false);
        if (record == null) {
            if (Utils.DEBUG) {
                Log.d(TAG, "updateCurrentCount: error record");
            }
            return;
        }
        synchronized (record) {
            if (record.mCount > 0) {
                record.mCount = record.mCount - 1;
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "reduceCurrentCount  count/" + record.mCount + ", id=" + id + ", mTotalCount=" + record.mTotalCount);
        }
    }

    public boolean isReportTime(long id, int timeInterval, long preReportTime, int totalTimeInterval) {
        RecordReourceCount record = getResourceCountRecord(id, false);
        if (record == null) {
            return false;
        }
        long currentTime = SystemClock.uptimeMillis();
        record.mOverLoadNum = record.mOverLoadNum + 1;
        if (Utils.DEBUG) {
            Log.d(TAG, "ResourceFlowControl.isReportTime:  id:" + id + " timeInterval:" + timeInterval + " preReportTime:" + preReportTime + " totalTimeInterval:" + totalTimeInterval + " currentTime:" + currentTime);
        }
        if (currentTime - record.mReportTimeMillis < ((long) timeInterval) || currentTime - preReportTime < ((long) totalTimeInterval)) {
            return false;
        }
        record.mReportTimeMillis = currentTime;
        return true;
    }
}
