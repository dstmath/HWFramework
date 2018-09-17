package com.android.server.policy;

import android.util.SparseIntArray;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class TouchCountPolicy {
    private static final int DAY_COUNT = 7;
    private static final int HOUR_COUNT = 24;
    private static final int TOTAL_TOUCH_INFO_COUNT = 168;
    private static final SparseIntArray sDayIndexMap = new SparseIntArray();
    private int mLastIndex = 0;
    private long mTouchCount = 0;
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
                this.mTouchCount = 1;
                this.mLastIndex = index;
            } else {
                this.mTouchCount++;
            }
            this.mTouchInfo[index] = mapTouchCountInfo(this.mTouchCount);
        }
    }

    private int mapTouchCountInfo(long count) {
        if (count == 0) {
            return 0;
        }
        if (count < 10) {
            return 1;
        }
        if (count < 100) {
            return 2;
        }
        return 3;
    }

    public int[] getTouchCountInfo() {
        int[] copy = Arrays.copyOf(this.mTouchInfo, this.mTouchInfo.length);
        Arrays.fill(this.mTouchInfo, 0);
        this.mTouchCount = 0;
        return copy;
    }

    public int[] getDefaultTouchCountInfo() {
        int[] defaultInfo = new int[TOTAL_TOUCH_INFO_COUNT];
        Arrays.fill(defaultInfo, -1);
        return defaultInfo;
    }
}
