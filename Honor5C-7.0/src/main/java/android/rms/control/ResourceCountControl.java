package android.rms.control;

import android.os.SystemClock;
import java.util.HashMap;

public class ResourceCountControl {
    private static final boolean DEBUG = false;
    private static final String TAG = "ResourceCountControl";
    private final HashMap<Long, RecordReourceCount> mResourceCountMap;

    final class RecordReourceCount {
        private int mCount;
        private int mOverLoadNum;
        private long mReportTimeMillis;
        private int mTotalCount;

        RecordReourceCount(int totalCount, int count, int overLoadNum, long timeMills) {
            this.mTotalCount = totalCount;
            this.mCount = count;
            this.mOverLoadNum = overLoadNum;
            this.mReportTimeMillis = timeMills;
        }
    }

    public ResourceCountControl() {
        this.mResourceCountMap = new HashMap();
    }

    public boolean checkCountOverload(long id, int threshold) {
        boolean flag = DEBUG;
        RecordReourceCount record = getResourceCountRecord(id, true);
        synchronized (record) {
            record.mTotalCount = record.mTotalCount + 1;
            record.mCount = record.mCount + 1;
            if (record.mCount > threshold) {
                record.mCount = record.mCount - 1;
                record.mOverLoadNum = record.mOverLoadNum + 1;
                flag = true;
            }
        }
        return flag;
    }

    public int getOverloadNumber(long id) {
        synchronized (this.mResourceCountMap) {
            RecordReourceCount record = (RecordReourceCount) this.mResourceCountMap.get(Long.valueOf(id));
            if (record == null) {
                return 0;
            }
            int overNumber = record.mOverLoadNum;
            return overNumber;
        }
    }

    public int getTotalCount(long id) {
        synchronized (this.mResourceCountMap) {
            RecordReourceCount record = (RecordReourceCount) this.mResourceCountMap.get(Long.valueOf(id));
            if (record == null) {
                return 0;
            }
            int totalCount = record.mTotalCount;
            return totalCount;
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
        }
    }

    public void reduceCurrentCount(long id) {
        RecordReourceCount record = getResourceCountRecord(id, DEBUG);
        if (record != null) {
            synchronized (record) {
                if (record.mCount > 0) {
                    record.mCount = record.mCount - 1;
                }
                if (record.mTotalCount > 0) {
                    record.mTotalCount = record.mTotalCount - 1;
                }
            }
        }
    }

    public boolean isReportTime(long id, int timeInterval) {
        RecordReourceCount record = getResourceCountRecord(id, DEBUG);
        if (record == null) {
            return DEBUG;
        }
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - record.mReportTimeMillis < ((long) timeInterval)) {
            return DEBUG;
        }
        record.mReportTimeMillis = currentTime;
        return true;
    }
}
