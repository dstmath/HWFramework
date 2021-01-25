package com.android.server.policy;

import android.util.SparseIntArray;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class TouchCountPolicy {
    private static final int DAY_COUNT = 7;
    private static final SparseIntArray DAY_INDEX_MAPS = new SparseIntArray();
    private static final int DEFAULT_INFO = -1;
    private static final int FRIDAY = 4;
    private static final int HOUR_COUNT = 24;
    private static final int MAX_COUNT_NEW_POLICY = 255;
    private static final int MIN_COUNT_NEW_POLICY = 5;
    private static final int MONDAY = 0;
    private static final int SATURDAY = 5;
    private static final int SUNDAY = 6;
    private static final int THURSDAY = 3;
    private static final int TOTAL_TOUCH_INFO_COUNT = 168;
    private static final int TUESDAY = 1;
    private static final int WEDNESDAY = 2;
    private int mLastIndex = 0;
    private int mTouchCount = 5;
    private int[] mTouchInfos = new int[TOTAL_TOUCH_INFO_COUNT];

    static {
        DAY_INDEX_MAPS.append(2, 0);
        DAY_INDEX_MAPS.append(3, 1);
        DAY_INDEX_MAPS.append(4, 2);
        DAY_INDEX_MAPS.append(5, 3);
        DAY_INDEX_MAPS.append(6, 4);
        DAY_INDEX_MAPS.append(7, 5);
        DAY_INDEX_MAPS.append(1, 6);
    }

    public void updateTouchCountInfo() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(System.currentTimeMillis());
        int index = (DAY_INDEX_MAPS.get(calendar.get(7), 0) * 24) + calendar.get(11);
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
            this.mTouchInfos[index] = this.mTouchCount;
        }
    }

    public int[] getTouchCountInfo() {
        int[] iArr = this.mTouchInfos;
        int[] touchInfos = Arrays.copyOf(iArr, iArr.length);
        Arrays.fill(this.mTouchInfos, 0);
        this.mTouchCount = 5;
        return touchInfos;
    }

    public int[] getDefaultTouchCountInfo() {
        int[] defaultInfos = new int[TOTAL_TOUCH_INFO_COUNT];
        Arrays.fill(defaultInfos, -1);
        return defaultInfos;
    }
}
