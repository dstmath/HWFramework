package com.android.server.rms.iaware.memory.utils;

import android.os.SystemClock;
import com.android.server.net.HwNetworkStatsService;
import java.util.ArrayList;
import java.util.Arrays;

class RecordTable {
    static final int DATE_DAY = 2;
    static final int DATE_MONTH = 1;
    static final int DATE_YEAR = 0;
    static final int MAX_RECORD_COUNT = 6;
    static final int MAX_RECORD_TIMESTAMP = 60;
    static final int RECORD_ANR = 5;
    static final int RECORD_CRASH = 4;
    static final int RECORD_DIED = 3;
    static final int RECORD_KILL = 2;
    static final int RECORD_RESTART = 1;
    static final int RECORD_START = 0;
    long[] mDayRecordCounts;
    long[] mFirstTimes;
    long[] mMaxRecordCounts;
    ArrayTimeStamps[] mRecordTimeStamps;
    private long mResetScaleTimestamp;
    private int mScaleTimes;
    long[] mTotalRecordCounts;

    static final class ArrayTimeStamps {
        ArrayList<Long> mTimeStamps;

        ArrayTimeStamps() {
            this.mTimeStamps = new ArrayList();
        }
    }

    RecordTable() {
        this.mScaleTimes = RECORD_RESTART;
        this.mResetScaleTimestamp = SystemClock.elapsedRealtime();
        this.mTotalRecordCounts = new long[MAX_RECORD_COUNT];
        Arrays.fill(this.mTotalRecordCounts, 0);
        this.mMaxRecordCounts = new long[MAX_RECORD_COUNT];
        Arrays.fill(this.mMaxRecordCounts, 0);
        this.mDayRecordCounts = new long[MAX_RECORD_COUNT];
        Arrays.fill(this.mDayRecordCounts, 0);
        this.mFirstTimes = new long[MAX_RECORD_COUNT];
        Arrays.fill(this.mFirstTimes, 0);
        this.mRecordTimeStamps = new ArrayTimeStamps[MAX_RECORD_COUNT];
        Arrays.fill(this.mRecordTimeStamps, null);
        for (int i = DATE_YEAR; i < this.mRecordTimeStamps.length; i += RECORD_RESTART) {
            this.mRecordTimeStamps[i] = new ArrayTimeStamps();
        }
    }

    void addTimestamp(int record, long timeStamp) {
        if (record >= 0 && record <= RECORD_ANR) {
            ArrayTimeStamps list = getList(record);
            if (list.mTimeStamps.size() >= MAX_RECORD_TIMESTAMP) {
                list.mTimeStamps.remove(DATE_YEAR);
            }
            list.mTimeStamps.add(Long.valueOf(timeStamp));
            long[] jArr = this.mTotalRecordCounts;
            jArr[record] = jArr[record] + 1;
            jArr = this.mDayRecordCounts;
            jArr[record] = jArr[record] + 1;
            this.mFirstTimes[record] = this.mTotalRecordCounts[record] == 1 ? timeStamp : 0;
            if (record == RECORD_RESTART) {
                addTimestamp(DATE_YEAR, timeStamp);
            }
        }
    }

    void cleanTimestamp(long nowTime, long interval) {
        for (int i = DATE_YEAR; i < MAX_RECORD_COUNT; i += RECORD_RESTART) {
            cleanTimestamp(i, nowTime, interval);
        }
    }

    private void cleanTimestamp(int record, long nowTime, long interval) {
        ArrayTimeStamps list = getList(record);
        int index = list.mTimeStamps.size() - 1;
        while (index >= 0 && interval < nowTime - ((Long) list.mTimeStamps.get(index)).longValue()) {
            list.mTimeStamps.remove(index);
            index--;
        }
    }

    void calcMax() {
        for (int i = DATE_YEAR; i < MAX_RECORD_COUNT; i += RECORD_RESTART) {
            if (this.mDayRecordCounts[i] > this.mMaxRecordCounts[i]) {
                this.mMaxRecordCounts[i] = this.mDayRecordCounts[i];
                this.mDayRecordCounts[i] = 0;
            }
        }
    }

    private ArrayTimeStamps getList(int record) {
        ArrayTimeStamps list = this.mRecordTimeStamps[record];
        if (list != null) {
            return list;
        }
        list = new ArrayTimeStamps();
        this.mRecordTimeStamps[record] = list;
        return list;
    }

    private boolean isInSameHalfHour() {
        if (SystemClock.elapsedRealtime() - this.mResetScaleTimestamp < HwNetworkStatsService.UPLOAD_INTERVAL) {
            return true;
        }
        return false;
    }

    public void updateScaleTimes() {
        if (!isInSameHalfHour()) {
            this.mResetScaleTimestamp = SystemClock.elapsedRealtime();
            this.mScaleTimes = RECORD_RESTART;
        } else if (this.mScaleTimes <= RECORD_CRASH) {
            this.mScaleTimes *= RECORD_KILL;
        }
    }

    public int getScaleTimes() {
        return this.mScaleTimes;
    }
}
