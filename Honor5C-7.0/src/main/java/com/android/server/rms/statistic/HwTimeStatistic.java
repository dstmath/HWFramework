package com.android.server.rms.statistic;

import android.os.SystemClock;
import android.util.Log;
import com.android.server.rms.utils.Utils;

public final class HwTimeStatistic {
    private static final long ONE_WEEK_INTERVAL = 10080000;
    private static final String TAG = "HwTimeCount";
    private static final long WRITE_FILE_INTERVAL = 1800000;
    private long mAccumulateTime;
    private long mCurTime;
    private int mCurWeek;
    private long mLastSaveTime;
    private long mSaveInterval;
    private long mStatisticInterval;

    public void init(long saveInterval, long statisticInterval, long accumulateTime) {
        if (saveInterval <= 0) {
            saveInterval = WRITE_FILE_INTERVAL;
        }
        this.mSaveInterval = saveInterval;
        if (statisticInterval <= 0) {
            statisticInterval = ONE_WEEK_INTERVAL;
        }
        this.mStatisticInterval = statisticInterval;
        if (accumulateTime <= 0) {
            accumulateTime = 0;
        }
        this.mAccumulateTime = accumulateTime;
        this.mCurTime = SystemClock.uptimeMillis();
        this.mLastSaveTime = this.mCurTime;
        this.mCurWeek = ((int) (this.mAccumulateTime / this.mStatisticInterval)) + 1;
        if (Utils.DEBUG) {
            Log.d(TAG, "[res count] HwTimeStatistic init mAccumulateTime (ms):" + this.mAccumulateTime + " mSaveInterval (ms):" + this.mSaveInterval + " mStatisticInterval (ms):" + this.mStatisticInterval + " mCurWeek:" + this.mCurWeek);
        }
    }

    public long accumulateTime() {
        long now = SystemClock.uptimeMillis();
        this.mAccumulateTime += now - this.mCurTime;
        this.mCurTime = now;
        return this.mAccumulateTime;
    }

    public boolean isTimeToSave(long time) {
        if (time - this.mLastSaveTime > this.mSaveInterval) {
            return true;
        }
        return false;
    }

    public long getCurrentTime() {
        return this.mCurTime;
    }

    public long updateSaveTime(long time) {
        if (time <= 0) {
            time = 0;
        }
        this.mLastSaveTime = time;
        return this.mLastSaveTime;
    }

    public long getAccumulateTime() {
        return this.mAccumulateTime;
    }

    public int getCurrentWeek() {
        return this.mCurWeek;
    }

    public boolean isNewWeek(long accumultateTime) {
        if (((int) (accumultateTime / this.mStatisticInterval)) + 1 > this.mCurWeek) {
            return true;
        }
        return false;
    }

    public boolean isExceedHalfWeek(long accumultateTime) {
        return accumultateTime % this.mStatisticInterval > (this.mStatisticInterval >> 1);
    }

    public int updateWeek(long accumultateTime) {
        this.mCurWeek = ((int) (accumultateTime / this.mStatisticInterval)) + 1;
        if (this.mCurWeek < 1) {
            this.mCurWeek = 1;
        }
        return this.mCurWeek;
    }
}
