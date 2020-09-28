package android.rms.control;

import android.os.SystemClock;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;

public class ResourceFlowControl {
    private static final String TAG = "RMS.ResourceFlowControl";
    private long mCurrentTime = 0;
    private final HashMap<Long, RecordReourceSpeed> mResourceSpeedMap = new HashMap<>(16);

    /* access modifiers changed from: package-private */
    public static final class RecordReourceSpeed {
        private int mContinuousOverLoadNum;
        private int mCountInPeroid;
        private int mOverLoadNum;
        private long mReportTimeMillis;
        private long mTimeMillis;
        private int mTotalCount;

        static /* synthetic */ int access$008(RecordReourceSpeed x0) {
            int i = x0.mCountInPeroid;
            x0.mCountInPeroid = i + 1;
            return i;
        }

        static /* synthetic */ int access$108(RecordReourceSpeed x0) {
            int i = x0.mOverLoadNum;
            x0.mOverLoadNum = i + 1;
            return i;
        }

        static /* synthetic */ int access$308(RecordReourceSpeed x0) {
            int i = x0.mContinuousOverLoadNum;
            x0.mContinuousOverLoadNum = i + 1;
            return i;
        }

        static /* synthetic */ int access$408(RecordReourceSpeed x0) {
            int i = x0.mTotalCount;
            x0.mTotalCount = i + 1;
            return i;
        }

        RecordReourceSpeed(int totalCount, int countInPeroid, int overLoadNum, int continuousOverLoadNum, long timeMillis, long reportTimeMillis) {
            this.mTotalCount = totalCount;
            this.mCountInPeroid = countInPeroid;
            this.mOverLoadNum = overLoadNum;
            this.mContinuousOverLoadNum = continuousOverLoadNum;
            this.mTimeMillis = timeMillis;
            this.mReportTimeMillis = reportTimeMillis;
        }
    }

    public boolean checkSpeedOverload(long id, int threshold, int loopInterval) {
        boolean isSpeedOverload = false;
        RecordReourceSpeed record = getResourceSpeedRecord(id, loopInterval);
        synchronized (record) {
            long currentTime = SystemClock.uptimeMillis();
            if (Utils.DEBUG) {
                Log.d(TAG, "checkSpeedOverload: /countinperoid=" + record.mCountInPeroid + " /overloadnum =" + record.mOverLoadNum + " /mTimMil =" + record.mTimeMillis + " /curTime =" + currentTime + " /threshold=" + threshold);
            }
            if (currentTime - record.mTimeMillis > ((long) loopInterval) * 2) {
                record.mTimeMillis = (currentTime / ((long) loopInterval)) * ((long) loopInterval);
                record.mCountInPeroid = 0;
                record.mContinuousOverLoadNum = 0;
            } else if (currentTime - record.mTimeMillis > ((long) loopInterval) && currentTime - record.mTimeMillis <= ((long) (loopInterval * 2))) {
                record.mTimeMillis = (currentTime / ((long) loopInterval)) * ((long) loopInterval);
                if (record.mCountInPeroid > threshold) {
                    RecordReourceSpeed.access$308(record);
                } else {
                    record.mContinuousOverLoadNum = 0;
                }
                record.mCountInPeroid = 0;
            }
            RecordReourceSpeed.access$008(record);
            RecordReourceSpeed.access$408(record);
            if (record.mCountInPeroid > threshold) {
                this.mCurrentTime = currentTime;
                isSpeedOverload = true;
            }
        }
        return isSpeedOverload;
    }

    public boolean isReportTime(long id, int loopInterval, long preReportTime, int totalTimeInterval) {
        RecordReourceSpeed record = getResourceSpeedRecord(id, loopInterval);
        if (Utils.DEBUG) {
            Log.d(TAG, "ResourceFlowControl.isReportTime:  id:" + id + " timeInterval:" + loopInterval + " preReportTime:" + preReportTime + " totalTimeInterval:" + totalTimeInterval + " currentTime:" + this.mCurrentTime + " ReportTimeInThisApp:" + record.mReportTimeMillis);
        }
        RecordReourceSpeed.access$108(record);
        if (this.mCurrentTime - record.mReportTimeMillis < ((long) loopInterval) && record.mReportTimeMillis != 0) {
            return false;
        }
        long j = this.mCurrentTime;
        if (j - preReportTime < ((long) totalTimeInterval)) {
            return false;
        }
        record.mReportTimeMillis = j;
        return true;
    }

    public int getOverloadNumber(long id) {
        synchronized (this.mResourceSpeedMap) {
            RecordReourceSpeed record = this.mResourceSpeedMap.get(Long.valueOf(id));
            if (record == null) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "getOverloadNumber: don't have this record");
                }
                return 0;
            }
            int overNumber = record.mOverLoadNum;
            record.mOverLoadNum = 0;
            if (Utils.DEBUG || Log.HWINFO) {
                Log.d(TAG, "getOverloadNumber: overNumber =" + overNumber);
            }
            return overNumber;
        }
    }

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
            return countInPeroid;
        }
    }

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
            return overPeroid;
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
