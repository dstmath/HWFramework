package android.rms.control;

import android.os.SystemClock;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;

public class ResourceCountControl {
    private static final String TAG = "RMS.ResourceCountControl";
    private final HashMap<Long, RecordReourceCount> mResourceCountMap = new HashMap<>(16);

    /* access modifiers changed from: package-private */
    public static final class RecordReourceCount {
        private int mCount;
        private boolean mIsWaterFlag = true;
        private int mOverLoadNum;
        private long mReportTimeMillis;
        private int mTotalCount;

        static /* synthetic */ int access$008(RecordReourceCount x0) {
            int i = x0.mCount;
            x0.mCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$010(RecordReourceCount x0) {
            int i = x0.mCount;
            x0.mCount = i - 1;
            return i;
        }

        static /* synthetic */ int access$308(RecordReourceCount x0) {
            int i = x0.mOverLoadNum;
            x0.mOverLoadNum = i + 1;
            return i;
        }

        RecordReourceCount(int totalCount, int count, int overLoadNum, long timeMills) {
            this.mTotalCount = totalCount;
            this.mCount = count;
            this.mOverLoadNum = overLoadNum;
            this.mReportTimeMillis = timeMills;
        }
    }

    public boolean checkCountOverload(long id, int threshold, int hardThreshold, int waterThreshold, int total) {
        if (Utils.DEBUG) {
            Log.d(TAG, "checkCountOverload:threshold=" + threshold + " / id=" + id);
        }
        RecordReourceCount record = getResourceCountRecord(id, true);
        synchronized (record) {
            if (total == -1) {
                RecordReourceCount.access$008(record);
            } else {
                record.mCount = total;
            }
            if (record.mTotalCount < record.mCount) {
                record.mTotalCount = record.mCount;
            }
            if ((!record.mIsWaterFlag || record.mCount <= threshold) && record.mCount <= hardThreshold) {
                if (record.mCount < waterThreshold) {
                    record.mIsWaterFlag = true;
                }
                return false;
            }
            record.mIsWaterFlag = false;
            if (Utils.DEBUG) {
                Log.d(TAG, "ResourceCountOverload: count = " + record.mCount + ", totalCount = " + record.mTotalCount);
            }
            return true;
        }
    }

    public int getOverloadNumber(long id) {
        synchronized (this.mResourceCountMap) {
            RecordReourceCount record = this.mResourceCountMap.get(Long.valueOf(id));
            if (record == null) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getOverloadNumber: don't have this record");
                }
                return 0;
            }
            int overNumber = record.mOverLoadNum;
            record.mOverLoadNum = 0;
            if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadNumber: overNumber =" + overNumber);
            }
            return overNumber;
        }
    }

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
            record.mTotalCount = record.mCount;
            if (Utils.DEBUG) {
                Log.d(TAG, "getTotalCount: totalCount =" + totalCount);
            }
            return totalCount;
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
            return record.mCount;
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
        if (record != null) {
            synchronized (record) {
                if (record.mCount > 0) {
                    RecordReourceCount.access$010(record);
                }
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "reduceCurrentCount  count/" + record.mCount + ", id=" + id + ", mTotalCount=" + record.mTotalCount);
            }
        } else if (Utils.DEBUG) {
            Log.d(TAG, "updateCurrentCount: error record");
        }
    }

    public boolean isReportTime(long id, int timeInterval, long preReportTime, int totalTimeInterval) {
        RecordReourceCount record = getResourceCountRecord(id, false);
        if (record == null) {
            return false;
        }
        long currentTime = SystemClock.uptimeMillis();
        synchronized (record) {
            RecordReourceCount.access$308(record);
        }
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
