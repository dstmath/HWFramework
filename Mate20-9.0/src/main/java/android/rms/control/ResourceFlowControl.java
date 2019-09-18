package android.rms.control;

import android.os.SystemClock;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;

public class ResourceFlowControl {
    private static final String TAG = "RMS.ResourceFlowControl";
    private long mCurrentTime = 0;
    private final HashMap<Long, RecordReourceSpeed> mResourceSpeedMap = new HashMap<>();

    static final class RecordReourceSpeed {
        /* access modifiers changed from: private */
        public int mContinuousOverLoadNum;
        /* access modifiers changed from: private */
        public int mCountInPeroid;
        /* access modifiers changed from: private */
        public int mOverLoadNum;
        /* access modifiers changed from: private */
        public long mReportTimeMillis;
        /* access modifiers changed from: private */
        public long mTimeMillis;
        /* access modifiers changed from: private */
        public int mTotalCount;

        RecordReourceSpeed(int totalCount, int countInPeroid, int overLoadNum, int mContinuousOverLoadNum2, long timeMillis, long reportTimeMillis) {
            this.mTotalCount = totalCount;
            this.mCountInPeroid = countInPeroid;
            this.mOverLoadNum = overLoadNum;
            this.mContinuousOverLoadNum = mContinuousOverLoadNum2;
            this.mTimeMillis = timeMillis;
            this.mReportTimeMillis = reportTimeMillis;
        }
    }

    public boolean checkSpeedOverload(long id, int threshold, int loopInterval) {
        boolean flag = false;
        RecordReourceSpeed record = getResourceSpeedRecord(id, loopInterval);
        synchronized (record) {
            long currentTime = SystemClock.uptimeMillis();
            if (Utils.DEBUG) {
                Log.d(TAG, "checkSpeedOverload: /countinperoid=" + record.mCountInPeroid + " /overloadnum =" + record.mOverLoadNum + " /mTimeMillis =" + record.mTimeMillis + " /currentTime =" + currentTime);
            }
            if (currentTime - record.mTimeMillis > 2 * ((long) loopInterval)) {
                long unused = record.mTimeMillis = (currentTime / ((long) loopInterval)) * ((long) loopInterval);
                int unused2 = record.mCountInPeroid = 0;
                int unused3 = record.mContinuousOverLoadNum = 0;
            } else if (currentTime - record.mTimeMillis > ((long) loopInterval) && currentTime - record.mTimeMillis <= ((long) (2 * loopInterval))) {
                long unused4 = record.mTimeMillis = (currentTime / ((long) loopInterval)) * ((long) loopInterval);
                if (record.mCountInPeroid > threshold) {
                    int unused5 = record.mContinuousOverLoadNum = record.mContinuousOverLoadNum + 1;
                } else {
                    int unused6 = record.mContinuousOverLoadNum = 0;
                }
                int unused7 = record.mCountInPeroid = 0;
            }
            int unused8 = record.mCountInPeroid = record.mCountInPeroid + 1;
            int unused9 = record.mTotalCount = record.mTotalCount + 1;
            if (record.mCountInPeroid > threshold) {
                this.mCurrentTime = currentTime;
                flag = true;
            }
        }
        return flag;
    }

    public boolean isReportTime(long id, int loopInterval, long preReportTime, int totalTimeInterval) {
        RecordReourceSpeed record = getResourceSpeedRecord(id, loopInterval);
        if (Utils.DEBUG) {
            Log.d(TAG, "ResourceFlowControl.isReportTime:  id:" + id + " timeInterval:" + loopInterval + " preReportTime:" + preReportTime + " totalTimeInterval:" + totalTimeInterval + " currentTime:" + this.mCurrentTime + " ReportTimeInThisApp:" + record.mReportTimeMillis);
        }
        int unused = record.mOverLoadNum = record.mOverLoadNum + 1;
        if ((this.mCurrentTime - record.mReportTimeMillis < ((long) loopInterval) && record.mReportTimeMillis != 0) || this.mCurrentTime - preReportTime < ((long) totalTimeInterval)) {
            return false;
        }
        long unused2 = record.mReportTimeMillis = this.mCurrentTime;
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0045, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001e, code lost:
        return 0;
     */
    public int getOverloadNumber(long id) {
        synchronized (this.mResourceSpeedMap) {
            RecordReourceSpeed record = this.mResourceSpeedMap.get(Long.valueOf(id));
            if (record != null) {
                int overNumber = record.mOverLoadNum;
                int unused = record.mOverLoadNum = 0;
                if (Utils.DEBUG || Log.HWINFO) {
                    Log.d(TAG, "getOverloadNumber: overNumber =" + overNumber);
                }
            } else if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadNumber: don't have this record");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003e, code lost:
        return r2;
     */
    public int getCountInPeroid(long id) {
        synchronized (this.mResourceSpeedMap) {
            RecordReourceSpeed record = this.mResourceSpeedMap.get(Long.valueOf(id));
            if (record == null) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getOverloadPeroid: don't have this record");
                }
                return 0;
            }
            int countInPeroid = record.mCountInPeroid;
            if (Utils.DEBUG) {
                Log.d(TAG, "getCountInPeroid: countInPeroid =" + countInPeroid);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003e, code lost:
        return r2;
     */
    public int getOverloadPeroid(long id) {
        synchronized (this.mResourceSpeedMap) {
            RecordReourceSpeed record = this.mResourceSpeedMap.get(Long.valueOf(id));
            if (record == null) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getOverloadPeroid: don't have this record");
                }
                return 0;
            }
            int overPeroid = record.mContinuousOverLoadNum;
            if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadPeroid: overPeroid =" + overPeroid);
            }
        }
    }

    private RecordReourceSpeed getResourceSpeedRecord(long id, int loopInterval) {
        RecordReourceSpeed record;
        synchronized (this.mResourceSpeedMap) {
            record = this.mResourceSpeedMap.get(Long.valueOf(id));
            if (record == null) {
                record = createResourceSpeedRecordLocked(id, loopInterval);
            }
        }
        return record;
    }

    private RecordReourceSpeed createResourceSpeedRecordLocked(long id, int loopInterval) {
        RecordReourceSpeed record = new RecordReourceSpeed(0, 0, 0, 0, (SystemClock.uptimeMillis() / ((long) loopInterval)) * ((long) loopInterval), 0);
        this.mResourceSpeedMap.put(Long.valueOf(id), record);
        return record;
    }

    public void removeResourceSpeedRecord(long id) {
        synchronized (this.mResourceSpeedMap) {
            if (this.mResourceSpeedMap.get(Long.valueOf(id)) != null) {
                this.mResourceSpeedMap.remove(Long.valueOf(id));
                if (Utils.DEBUG) {
                    Log.d(TAG, "removeResourceSpeedRecord id/" + id);
                }
            }
        }
    }
}
