package com.huawei.wallet.sdk.common.log;

import android.content.Context;
import java.util.Map;

public class LogC {
    public static final String LOG_HWSDK_TAG = "hwSDK";

    public static void d(String tag, String msg, boolean isNeedProguard) {
        LogUtil.d(tag, msg, isNeedProguard);
    }

    public static void init(Context context) {
        if (context != null) {
            String pkgName = context.getPackageName();
            d("logC.init: " + pkgName, false);
        }
    }

    public static void i(String tag, String msg, boolean isNeedProguard) {
        LogUtil.i(tag, msg, isNeedProguard);
    }

    public static void d(String tag, String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.d(tag, msg, e, isNeedProguard);
    }

    public static void w(String tag, String msg, boolean isNeedProguard) {
        LogUtil.e(tag, msg, null, isNeedProguard);
    }

    public static void i(String tag, String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.i(tag, msg, e, isNeedProguard);
    }

    public static void e(String tag, String msg, boolean isNeedProguard) {
        LogUtil.e(tag, msg, null, isNeedProguard);
    }

    public static void w(String tag, String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.e(tag, msg, e, isNeedProguard);
    }

    public static void e(String tag, String message, Throwable e, int errorCode, Map<String, String> map, boolean uploadLog, boolean isNeedProguard) {
        if (message != null) {
            LogUtil.e(tag, message, e, isNeedProguard);
        }
    }

    public static void e(String tag, String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.e(tag, msg, e, isNeedProguard);
    }

    public static void v(String tag, String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.d(tag, msg, e, isNeedProguard);
    }

    public static void v(String tag, String msg, boolean isNeedProguard) {
        LogUtil.d(tag, msg, isNeedProguard);
    }

    public static void d(String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.d(LOG_HWSDK_TAG, msg, e, isNeedProguard);
    }

    public static void d(String msg, boolean isNeedProguard) {
        LogUtil.d(LOG_HWSDK_TAG, msg, isNeedProguard);
    }

    public static void i(String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.i(LOG_HWSDK_TAG, msg, e, isNeedProguard);
    }

    public static void i(String msg, boolean isNeedProguard) {
        LogUtil.i(LOG_HWSDK_TAG, msg, isNeedProguard);
    }

    public static void w(String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.e(LOG_HWSDK_TAG, msg, e, isNeedProguard);
    }

    public static void w(String msg, boolean isNeedProguard) {
        LogUtil.e(LOG_HWSDK_TAG, msg, null, isNeedProguard);
    }

    public static void e(String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.e(LOG_HWSDK_TAG, msg, e, isNeedProguard);
    }

    public static void e(String msg, boolean isNeedProguard) {
        LogUtil.e(LOG_HWSDK_TAG, msg, null, isNeedProguard);
    }

    public static void v(String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.d(LOG_HWSDK_TAG, msg, e, isNeedProguard);
    }

    public static void v(String msg, boolean isNeedProguard) {
        LogUtil.d(LOG_HWSDK_TAG, msg, isNeedProguard);
    }
}
