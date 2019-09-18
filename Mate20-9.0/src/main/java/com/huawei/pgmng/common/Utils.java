package com.huawei.pgmng.common;

import android.os.SystemProperties;
import android.os.WorkSource;
import android.util.Log;
import com.huawei.pgmng.log.LogPower;

public final class Utils {
    private static final String TAG = "PG Utils";
    private static int mAutoAdjustBrightnessLimitVal = 110;
    private static int mMinBrightnessRatio = SystemProperties.getInt("ro.config.pg_min_ratio", 100);
    private static int mRatioMaxBrightness = 185;
    private static int mRatioMinBrightness = 35;

    public static void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
        Log.i(TAG, "configBrightnessRange ratioMin = " + ratioMin + "  ratioMax = " + ratioMax + "  autoLimit = " + autoLimit);
        mRatioMinBrightness = (mMinBrightnessRatio * ratioMin) / 100;
        mRatioMaxBrightness = ratioMax;
        mAutoAdjustBrightnessLimitVal = autoLimit;
    }

    public static int getRatioBright(int bright, double ratio) {
        if (bright <= mRatioMinBrightness || bright >= mRatioMaxBrightness) {
            return bright;
        }
        int bright2 = (int) (((double) bright) * ratio);
        if (bright2 < mRatioMinBrightness) {
            return mRatioMinBrightness;
        }
        return bright2;
    }

    public static int getAutoAdjustBright(int bright) {
        if (bright < mAutoAdjustBrightnessLimitVal && bright > mRatioMinBrightness) {
            return bright - (((bright - mRatioMinBrightness) * 3) / 10);
        }
        if (bright <= mAutoAdjustBrightnessLimitVal || bright >= mRatioMaxBrightness) {
            return bright;
        }
        return bright - (((mRatioMaxBrightness - bright) * 3) / 10);
    }

    public static int getAnimatedValue(int tarVal, int curVal, int amount) {
        int animatedValue = curVal;
        if (curVal < tarVal) {
            return Math.min(curVal + amount, tarVal);
        }
        if (curVal > tarVal) {
            return Math.max(curVal - amount, tarVal);
        }
        return animatedValue;
    }

    public static void noteWakelock(int flags, String tag, int ownerUid, int ownerPid, WorkSource workSource, int eventTag) {
        if (workSource != null) {
            int N = workSource.size();
            int ownerUid2 = ownerUid;
            for (int i = 0; i < N; i++) {
                ownerUid2 = workSource.get(i);
                if (!(ownerUid2 == 1000 || ownerUid2 == 1001)) {
                    LogPower.push(eventTag, Integer.toString(ownerUid2), Integer.toString(flags), Integer.toString(-2), new String[]{tag});
                }
            }
            int i2 = ownerUid2;
        } else if (ownerUid != 1000 && ownerUid != 1001) {
            LogPower.push(eventTag, Integer.toString(ownerUid), Integer.toString(flags), Integer.toString(ownerPid), new String[]{tag});
        }
    }

    private static void checkWorkSourceThenNote(int flags, String tag, int ownerUid, int ownerPid, WorkSource workSource1, WorkSource workSource2, int eventTag) {
        int iWs1Size = workSource1.size();
        int iWs2Size = workSource2.size();
        int i = ownerUid;
        for (int i2 = 0; i2 < iWs2Size; i2++) {
            int ownerUid2 = workSource2.get(i2);
            int j = 0;
            while (j < iWs1Size && workSource1.get(j) != ownerUid2) {
                j++;
            }
            if (!(j != iWs1Size || ownerUid2 == 1000 || ownerUid2 == 1001)) {
                LogPower.push(eventTag, Integer.toString(ownerUid2), Integer.toString(flags), Integer.toString(-2), new String[]{tag});
            }
        }
    }

    public static void noteWakelock(int flags, String tag, int ownerUid, int ownerPid, WorkSource oldWorkSource, WorkSource newWorkSource) {
        checkWorkSourceThenNote(flags, tag, ownerUid, ownerPid, newWorkSource, oldWorkSource, 161);
        checkWorkSourceThenNote(flags, tag, ownerUid, ownerPid, oldWorkSource, newWorkSource, 160);
    }

    public static void handleTimeOut(String reason, String pkg, String pid) {
        LogPower.push(148, reason, pkg, pid);
    }
}
