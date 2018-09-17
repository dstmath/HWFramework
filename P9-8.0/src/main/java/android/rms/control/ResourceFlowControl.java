package android.rms.control;

import android.os.SystemClock;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.HashMap;

public class ResourceFlowControl {
    private static final String TAG = "RMS.ResourceFlowControl";
    private long mCurrentTime = 0;
    private final HashMap<Long, RecordReourceSpeed> mResourceSpeedMap = new HashMap();

    static final class RecordReourceSpeed {
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

    public boolean checkSpeedOverload(long id, int threshold, int loopInterval) {
        boolean flag = false;
        RecordReourceSpeed record = getResourceSpeedRecord(id, loopInterval);
        synchronized (record) {
            long currentTime = SystemClock.uptimeMillis();
            if (Utils.DEBUG) {
                Log.d(TAG, "checkSpeedOverload: /countinperoid=" + record.mCountInPeroid + " /overloadnum =" + record.mOverLoadNum + " /mTimeMillis =" + record.mTimeMillis + " /currentTime =" + currentTime);
            }
            if (currentTime - record.mTimeMillis > ((long) loopInterval) * 2) {
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
        record.mOverLoadNum = record.mOverLoadNum + 1;
        if ((this.mCurrentTime - record.mReportTimeMillis < ((long) loopInterval) && record.mReportTimeMillis != 0) || this.mCurrentTime - preReportTime < ((long) totalTimeInterval)) {
            return false;
        }
        record.mReportTimeMillis = this.mCurrentTime;
        return true;
    }

    /* JADX WARNING: Missing block: B:9:0x0020, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:17:0x004c, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getOverloadNumber(long id) {
        synchronized (this.mResourceSpeedMap) {
            RecordReourceSpeed record = (RecordReourceSpeed) this.mResourceSpeedMap.get(Long.valueOf(id));
            if (record != null) {
                int overNumber = record.mOverLoadNum;
                record.mOverLoadNum = 0;
                if (Utils.DEBUG || Log.HWINFO) {
                    Log.d(TAG, "getOverloadNumber: overNumber =" + overNumber);
                }
            } else if (Utils.DEBUG) {
                Log.d(TAG, "getOverloadNumber: don't have this record");
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0044, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getCountInPeroid(long id) {
        synchronized (this.mResourceSpeedMap) {
            RecordReourceSpeed record = (RecordReourceSpeed) this.mResourceSpeedMap.get(Long.valueOf(id));
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

    /* JADX WARNING: Missing block: B:16:0x0044, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getOverloadPeroid(long id) {
        synchronized (this.mResourceSpeedMap) {
            RecordReourceSpeed record = (RecordReourceSpeed) this.mResourceSpeedMap.get(Long.valueOf(id));
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
            record = (RecordReourceSpeed) this.mResourceSpeedMap.get(Long.valueOf(id));
            if (record == null) {
                record = createResourceSpeedRecordLocked(id, loopInterval);
            }
        }
        return record;
    }

    private RecordReourceSpeed createResourceSpeedRecordLocked(long id, int loopInterval) {
        RecordReourceSpeed record = new RecordReourceSpeed(0, 0, 0, 0, ((long) loopInterval) * (SystemClock.uptimeMillis() / ((long) loopInterval)), 0);
        this.mResourceSpeedMap.put(Long.valueOf(id), record);
        return record;
    }

    public void removeResourceSpeedRecord(long id) {
        synchronized (this.mResourceSpeedMap) {
            if (((RecordReourceSpeed) this.mResourceSpeedMap.get(Long.valueOf(id))) != null) {
                this.mResourceSpeedMap.remove(Long.valueOf(id));
                if (Utils.DEBUG) {
                    Log.d(TAG, "removeResourceSpeedRecord id/" + id);
                }
            }
        }
    }
}
