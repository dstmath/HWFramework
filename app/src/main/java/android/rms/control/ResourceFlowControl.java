package android.rms.control;

import android.os.SystemClock;
import android.util.Log;
import java.util.HashMap;

public class ResourceFlowControl {
    private static final boolean DEBUG = false;
    private static final String TAG = "ResourceFlowControl";
    private final HashMap<Long, RecordReourceSpeed> mResourceSpeedMap;

    final class RecordReourceSpeed {
        private int mContinuousOverLoadNum;
        private int mCountInPeroid;
        private int mOverLoadNum;
        private long mReportTimeMillis;
        private long mTimeMillis;
        private int mTotalCount;

        RecordReourceSpeed(int totalCount, int countInPeroid, int overLoadNum, int mContinuousOverLoadNum, long timeMillis, long reportTimeMillis) {
            this.mTotalCount = totalCount;
            this.mCountInPeroid = countInPeroid;
            this.mOverLoadNum = overLoadNum;
            this.mContinuousOverLoadNum = mContinuousOverLoadNum;
            this.mTimeMillis = timeMillis;
            this.mReportTimeMillis = reportTimeMillis;
        }
    }

    public ResourceFlowControl() {
        this.mResourceSpeedMap = new HashMap();
    }

    public boolean checkSpeedOverload(long id, int threshold, int loopInterval) {
        boolean flag = DEBUG;
        RecordReourceSpeed record = getResourceSpeedRecord(id, loopInterval);
        synchronized (record) {
            long currentTime = SystemClock.uptimeMillis();
            if (currentTime - record.mTimeMillis > ((long) (loopInterval * 2))) {
                record.mTimeMillis = (currentTime / ((long) loopInterval)) * ((long) loopInterval);
                record.mCountInPeroid = 0;
                record.mContinuousOverLoadNum = 0;
            } else if (currentTime - record.mTimeMillis > ((long) loopInterval) && currentTime - record.mTimeMillis <= ((long) (loopInterval * 2))) {
                record.mTimeMillis = (currentTime / ((long) loopInterval)) * ((long) loopInterval);
                if (record.mCountInPeroid > threshold) {
                    record.mContinuousOverLoadNum = record.mContinuousOverLoadNum + 1;
                } else {
                    record.mContinuousOverLoadNum = 0;
                }
                record.mCountInPeroid = 0;
            }
            record.mCountInPeroid = record.mCountInPeroid + 1;
            record.mTotalCount = record.mTotalCount + 1;
            if (record.mCountInPeroid > threshold) {
                record.mOverLoadNum = record.mOverLoadNum + 1;
                flag = true;
            }
        }
        return flag;
    }

    public boolean isReportTime(long id, int loopInterval) {
        RecordReourceSpeed record = getResourceSpeedRecord(id, loopInterval);
        long currentTime = SystemClock.uptimeMillis();
        if (currentTime - record.mReportTimeMillis < ((long) loopInterval)) {
            return DEBUG;
        }
        record.mReportTimeMillis = currentTime;
        return true;
    }

    public int getOverloadNumber(long id) {
        synchronized (this.mResourceSpeedMap) {
            RecordReourceSpeed record = (RecordReourceSpeed) this.mResourceSpeedMap.get(Long.valueOf(id));
            if (record == null) {
                return 0;
            }
            int overNumber = record.mOverLoadNum;
            if (Log.HWINFO) {
                Log.d(TAG, "getOverloadNumber: overNumber =" + overNumber);
            }
            return overNumber;
        }
    }

    public int getOverloadPeroid(long id) {
        synchronized (this.mResourceSpeedMap) {
            RecordReourceSpeed record = (RecordReourceSpeed) this.mResourceSpeedMap.get(Long.valueOf(id));
            if (record == null) {
                return 0;
            }
            int overPeroid = record.mContinuousOverLoadNum;
            return overPeroid;
        }
    }

    private RecordReourceSpeed getResourceSpeedRecord(long id, int loopInterval) {
        RecordReourceSpeed record;
        synchronized (this.mResourceSpeedMap) {
            record = (RecordReourceSpeed) this.mResourceSpeedMap.get(Long.valueOf(id));
            if (record == null) {
                record = createResourceSpeedRecordLocked(id, loopInterval);
            }
        }
        return record;
    }

    private RecordReourceSpeed createResourceSpeedRecordLocked(long id, int loopInterval) {
        long currentTime = SystemClock.uptimeMillis();
        RecordReourceSpeed record = new RecordReourceSpeed(0, 0, 0, 0, ((long) loopInterval) * (currentTime / ((long) loopInterval)), currentTime);
        this.mResourceSpeedMap.put(Long.valueOf(id), record);
        return record;
    }

    public void removeResourceSpeedRecord(long id) {
        synchronized (this.mResourceSpeedMap) {
            if (((RecordReourceSpeed) this.mResourceSpeedMap.get(Long.valueOf(id))) != null) {
                this.mResourceSpeedMap.remove(Long.valueOf(id));
            }
        }
    }
}
