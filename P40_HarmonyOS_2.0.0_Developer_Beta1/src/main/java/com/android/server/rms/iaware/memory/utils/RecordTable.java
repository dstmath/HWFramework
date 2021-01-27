package com.android.server.rms.iaware.memory.utils;

import android.os.SystemClock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/* access modifiers changed from: package-private */
public class RecordTable {
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
    private static volatile int sMaxScaleTimes = 8;
    long[] mDayRecordCounts;
    long[] mFirstTimes;
    long[] mMaxRecordCounts;
    ArrayTimeStamps[] mRecordTimeStamps;
    private long mResetScaleTimestamp = SystemClock.elapsedRealtime();
    private int mScaleTimes = 1;
    long[] mTotalRecordCounts = new long[6];

    static void saveScaleTimes(int scaleTimes) {
        sMaxScaleTimes = scaleTimes;
    }

    /* access modifiers changed from: package-private */
    public static final class ArrayTimeStamps {
        ArrayList<Long> mTimeStamps = new ArrayList<>();

        ArrayTimeStamps() {
        }
    }

    RecordTable() {
        Arrays.fill(this.mTotalRecordCounts, 0L);
        this.mMaxRecordCounts = new long[6];
        Arrays.fill(this.mMaxRecordCounts, 0L);
        this.mDayRecordCounts = new long[6];
        Arrays.fill(this.mDayRecordCounts, 0L);
        this.mFirstTimes = new long[6];
        Arrays.fill(this.mFirstTimes, 0L);
        this.mRecordTimeStamps = new ArrayTimeStamps[6];
        Arrays.fill(this.mRecordTimeStamps, (Object) null);
        int i = 0;
        while (true) {
            ArrayTimeStamps[] arrayTimeStampsArr = this.mRecordTimeStamps;
            if (i < arrayTimeStampsArr.length) {
                arrayTimeStampsArr[i] = new ArrayTimeStamps();
                i++;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addTimeStamp(int record, long timeStamp) {
        if (record >= 0 && record <= 5) {
            ArrayTimeStamps list = getList(record);
            if (list.mTimeStamps.size() >= 60) {
                list.mTimeStamps.remove(0);
            }
            list.mTimeStamps.add(Long.valueOf(timeStamp));
            long[] jArr = this.mTotalRecordCounts;
            jArr[record] = jArr[record] + 1;
            long[] jArr2 = this.mDayRecordCounts;
            jArr2[record] = jArr2[record] + 1;
            if (jArr[record] == 1) {
                this.mFirstTimes[record] = timeStamp;
            }
            if (record == 1) {
                addTimeStamp(0, timeStamp);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cleanTimeStamp(long nowTime, long interval) {
        for (int i = 0; i < 6; i++) {
            cleanTimeStamp(i, nowTime, interval);
        }
    }

    private void cleanTimeStamp(int record, long nowTime, long interval) {
        ArrayTimeStamps list = getList(record);
        if (list.mTimeStamps.size() >= 1) {
            Iterator<Long> listIt = list.mTimeStamps.iterator();
            while (listIt.hasNext()) {
                Long oldTime = listIt.next();
                if (oldTime != null && interval < nowTime - oldTime.longValue()) {
                    listIt.remove();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void calcMax() {
        for (int i = 0; i < 6; i++) {
            long[] jArr = this.mDayRecordCounts;
            long j = jArr[i];
            long[] jArr2 = this.mMaxRecordCounts;
            if (j > jArr2[i]) {
                jArr2[i] = jArr[i];
            }
            this.mDayRecordCounts[i] = 0;
        }
    }

    private ArrayTimeStamps getList(int record) {
        ArrayTimeStamps list = this.mRecordTimeStamps[record];
        if (list != null) {
            return list;
        }
        ArrayTimeStamps list2 = new ArrayTimeStamps();
        this.mRecordTimeStamps[record] = list2;
        return list2;
    }

    private boolean isInSameHalfHour() {
        if (SystemClock.elapsedRealtime() - this.mResetScaleTimestamp < 1800000) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateScaleTimes() {
        if (!isInSameHalfHour()) {
            this.mResetScaleTimestamp = SystemClock.elapsedRealtime();
            this.mScaleTimes = 1;
        } else if (this.mScaleTimes < sMaxScaleTimes) {
            this.mScaleTimes++;
        }
    }

    /* access modifiers changed from: package-private */
    public int getScaleTimes() {
        return this.mScaleTimes;
    }
}
