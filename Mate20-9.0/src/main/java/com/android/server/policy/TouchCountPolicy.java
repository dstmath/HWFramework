package com.android.server.policy;

import android.util.SparseIntArray;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class TouchCountPolicy {
    private static final int DAY_COUNT = 7;
    private static final int HOUR_COUNT = 24;
    private static final int MAX_COUNT_NEW_POLICY = 255;
    private static final int MIN_COUNT_NEW_POLICY = 5;
    private static final int TOTAL_TOUCH_INFO_COUNT = 168;
    private static final SparseIntArray sDayIndexMap = new SparseIntArray();
    private int mLastIndex = 0;
    private int mTouchCount = 5;
    private int[] mTouchInfo = new int[TOTAL_TOUCH_INFO_COUNT];

    static {
        sDayIndexMap.append(2, 0);
        sDayIndexMap.append(3, 1);
        sDayIndexMap.append(4, 2);
        sDayIndexMap.append(5, 3);
        sDayIndexMap.append(6, 4);
        sDayIndexMap.append(7, 5);
        sDayIndexMap.append(1, 6);
    }

    public void updateTouchCountInfo() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(System.currentTimeMillis());
        int index = (sDayIndexMap.get(calendar.get(7), 0) * 24) + calendar.get(11);
        if (index < TOTAL_TOUCH_INFO_COUNT) {
            if (this.mLastIndex != index) {
                this.mTouchCount = 6;
                this.mLastIndex = index;
            } else {
                this.mTouchCount++;
                if (this.mTouchCount > 255) {
                    this.mTouchCount = 255;
                }
            }
            this.mTouchInfo[index] = this.mTouchCount;
        }
    }

    public int[] getTouchCountInfo() {
        int[] copy = Arrays.copyOf(this.mTouchInfo, this.mTouchInfo.length);
        Arrays.fill(this.mTouchInfo, 0);
        this.mTouchCount = 5;
        return copy;
    }

    public int[] getDefaultTouchCountInfo() {
        int[] defaultInfo = new int[TOTAL_TOUCH_INFO_COUNT];
        Arrays.fill(defaultInfo, -1);
        return defaultInfo;
    }
}
