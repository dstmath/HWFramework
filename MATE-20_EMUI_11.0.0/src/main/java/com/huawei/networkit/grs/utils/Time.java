package com.huawei.networkit.grs.utils;

import android.text.TextUtils;
import com.huawei.networkit.grs.common.Logger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Time {
    private static final String TAG = Time.class.getSimpleName();

    public static String getUTCtime() {
        Calendar cal = Calendar.getInstance();
        cal.add(14, -(cal.get(15) + cal.get(16)));
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ROOT).format(new Date(cal.getTimeInMillis()));
    }

    public static boolean isTimeWillExpire(String spValue, long currentUPTime) {
        if (TextUtils.isEmpty(spValue)) {
            Logger.v(TAG, "isSpExpire spValue is null.");
            return true;
        }
        try {
            if (Long.parseLong(spValue) - (System.currentTimeMillis() + currentUPTime) >= 0) {
                Logger.v(TAG, "isSpExpire false.");
                return false;
            }
        } catch (NumberFormatException e) {
            Logger.v(TAG, "isSpExpire spValue NumberFormatException.");
        }
        return true;
    }

    public static boolean isTimeExpire(Long expireTime) {
        if (expireTime == null) {
            Logger.v(TAG, "Method isTimeExpire input param expireTime is null.");
            return true;
        }
        try {
            if (expireTime.longValue() - System.currentTimeMillis() >= 0) {
                Logger.v(TAG, "isSpExpire false.");
                return false;
            }
            Logger.v(TAG, "isSpExpire true.");
            return true;
        } catch (NumberFormatException e) {
            Logger.v(TAG, "isSpExpire spValue NumberFormatException.");
        }
    }

    public static boolean isTimeWillExpire(Long expireTime, long currentUPTime) {
        if (expireTime == null) {
            Logger.v(TAG, "Method isTimeWillExpire input param expireTime is null.");
            return true;
        }
        try {
            if (expireTime.longValue() - (System.currentTimeMillis() + currentUPTime) >= 0) {
                Logger.v(TAG, "isSpExpire false.");
                return false;
            }
        } catch (NumberFormatException e) {
            Logger.v(TAG, "isSpExpire spValue NumberFormatException.");
        }
        return true;
    }
}
