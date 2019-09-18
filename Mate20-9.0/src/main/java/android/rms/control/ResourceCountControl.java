package android.rms.control;

import android.os.SystemClock;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;

public class ResourceCountControl {
    private static final String TAG = "RMS.ResourceCountControl";
    private final HashMap<Long, RecordReourceCount> mResourceCountMap = new HashMap<>();

    static final class RecordReourceCount {
        /* access modifiers changed from: private */
        public int mCount;
        /* access modifiers changed from: private */
        public int mOverLoadNum;
        /* access modifiers changed from: private */
        public long mReportTimeMillis;
        /* access modifiers changed from: private */
        public int mTotalCount;
        /* access modifiers changed from: private */
        public boolean mWaterFlag = true;

        static /* synthetic */ int access$010(RecordReourceCount x0) {
            int i = x0.mCount;
            x0.mCount = i - 1;
            return i;
        }

        RecordReourceCount(int totalCount, int count, int overLoadNum, long timeMills) {
            this.mTotalCount = totalCount;
            this.mCount = count;
            this.mOverLoadNum = overLoadNum;
            this.mReportTimeMillis = timeMills;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0086, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0091, code lost:
        return false;
     */
    public boolean checkCountOverload(long id, int threshold, int hardThreshold, int waterThreshold, int total, int resourceType) {
        if (Utils.DEBUG) {
            Log.d(TAG, "checkCountOverload:threshold=" + threshold + " / id=" + id);
        }
        RecordReourceCount record = getResourceCountRecord(id, true);
        synchronized (record) {
            if (-1 == total) {
                try {
                    int unused = record.mCount = record.mCount + 1;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                int unused2 = record.mCount = total;
            }
            if (record.mTotalCount < record.mCount) {
                int unused3 = record.mTotalCount = record.mCount;
            }
            if ((record.mWaterFlag && record.mCount > threshold) || record.mCount > hardThreshold) {
                boolean unused4 = record.mWaterFlag = false;
                if (Utils.DEBUG) {
                    Log.d(TAG, "ResourceCountOverload: count = " + record.mCount + ", totalCount = " + record.mTotalCount);
                }
            } else if (record.mCount < waterThreshold) {
                boolean unused5 = record.mWaterFlag = true;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0041, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001e, code lost:
        return 0;
     */
    public int getOverloadNumber(long id) {
        synchronized (this.mResourceCountMap) {
            RecordReourceCount record = this.mResourceCountMap.get(Long.valueOf(id));
            if (record != null) {
                int overNumber = record.mOverLoadNum;
                int unused = record.mOverLoadNum = 0;
                if (Utils.DEBUG) {
                    Log.d(TAG, "getOverloadNumber: overNumber =" + overNumber);
                }
            } else if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadNumber: don't have this record");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0045, code lost:
        return r2;
     */
    public int getTotalCount(long id) {
        synchronized (this.mResourceCountMap) {
            RecordReourceCount record = this.mResourceCountMap.get(Long.valueOf(id));
            if (record == null) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getTotalCount: don't have this record");
                }
                return 0;
            }
            int totalCount = record.mTotalCount;
            int unused = record.mTotalCount = record.mCount;
            if (Utils.DEBUG) {
                Log.d(TAG, "getTotalCount: totalCount =" + totalCount);
            }
        }
    }

    public int getCount(long id) {
        synchronized (this.mResourceCountMap) {
            RecordReourceCount record = this.mResourceCountMap.get(Long.valueOf(id));
            if (record == null) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getTotalCount: don't have this record");
                }
                return 0;
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "getCount: mCount =" + record.mCount);
            }
            int access$000 = record.mCount;
            return access$000;
        }
    }

    private RecordReourceCount getResourceCountRecord(long id, boolean isCreate) {
        RecordReourceCount record;
        synchronized (this.mResourceCountMap) {
            record = this.mResourceCountMap.get(Long.valueOf(id));
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
            if (this.mResourceCountMap.get(Long.valueOf(id)) != null) {
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
                RecordReourceCount.access$010(record);
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
        int unused = record.mOverLoadNum = record.mOverLoadNum + 1;
        if (Utils.DEBUG) {
            Log.d(TAG, "ResourceFlowControl.isReportTime:  id:" + id + " timeInterval:" + timeInterval + " preReportTime:" + preReportTime + " totalTimeInterval:" + totalTimeInterval + " currentTime:" + currentTime);
        }
        if (currentTime - record.mReportTimeMillis < ((long) timeInterval) || currentTime - preReportTime < ((long) totalTimeInterval)) {
            return false;
        }
        long unused2 = record.mReportTimeMillis = currentTime;
        return true;
    }
}
