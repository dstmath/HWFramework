package com.huawei.wallet.sdk.business.idcard.walletbase.util;

import android.text.TextUtils;
import com.huawei.wallet.sdk.common.log.LogUtil;

@Deprecated
public final class LogX {
    private static final String LOG_HWNFC_TAG = "HwNFC";
    private static final String TAB_STR = "    ";

    private LogX() {
    }

    private static String getLogMsg(String tag, String msg, Throwable e, boolean isNeedProguard) {
        StringBuilder retStr = new StringBuilder(256);
        if (!TextUtils.isEmpty(tag)) {
            retStr.append(tag);
            retStr.append(TAB_STR);
        }
        if (!TextUtils.isEmpty(msg)) {
            if (isNeedProguard) {
                retStr.append(LogUtil.formatLogWithStar(msg));
            } else {
                retStr.append(msg);
            }
        }
        if (e != null) {
            retStr.append(TAB_STR);
            retStr.append(LogUtil.getStackTraceString(e));
        }
        return retStr.toString();
    }

    public static String toSting() {
        return LogUtil.toSting();
    }

    public static void d(String tag, String message) {
        LogUtil.d(tag, message, false);
    }

    public static void d(String message) {
        LogUtil.d(LOG_HWNFC_TAG, message, false);
    }

    public static void d(String message, boolean isNeedProguard) {
        LogUtil.d(LOG_HWNFC_TAG, message, isNeedProguard);
    }

    public static void i(String tag, String message) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, message, null, false));
        LogUtil.i(tag, message, false);
    }

    public static void i(String message) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, message, null, false));
        LogUtil.i(LOG_HWNFC_TAG, message, false);
    }

    public static void i(String message, boolean isNeedProguard) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, message, null, isNeedProguard));
        LogUtil.i(LOG_HWNFC_TAG, message, isNeedProguard);
    }

    public static void w(String tag, String message) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, message, null, false));
        LogUtil.e(tag, message, null, false);
    }

    public static void w(String message) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, message, null, false));
        LogUtil.e(LOG_HWNFC_TAG, message, null, false);
    }

    public static void w(String message, boolean isNeedProguard) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, message, null, isNeedProguard));
        LogUtil.e(LOG_HWNFC_TAG, message, null, isNeedProguard);
    }

    public static void w(String tag, String msg, Throwable e) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, msg, e, false));
        LogUtil.e(tag, msg, e, false);
    }

    public static void w(String msg, Throwable e) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, msg, e, false));
        LogUtil.e(LOG_HWNFC_TAG, msg, e, false);
    }

    public static void w(String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, msg, e, isNeedProguard));
        LogUtil.e(LOG_HWNFC_TAG, msg, e, isNeedProguard);
    }

    public static void e(String tag, String message) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, message, null, false));
        LogUtil.e(LOG_HWNFC_TAG, message, null, false);
    }

    public static void e(String message) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, message, null, false));
        LogUtil.e(LOG_HWNFC_TAG, message, null, false);
    }

    public static void e(String message, boolean isNeedProguard) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, message, null, isNeedProguard));
        LogUtil.e(LOG_HWNFC_TAG, message, null, isNeedProguard);
    }

    public static void e(String tag, String msg, Throwable e) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, msg, e, false));
        LogUtil.e(LOG_HWNFC_TAG, msg, e, false);
    }

    public static void e(String msg, Throwable e) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, msg, e, false));
        LogUtil.e(LOG_HWNFC_TAG, msg, e, false);
    }

    public static void e(String msg, Throwable e, boolean isNeedProguard) {
        LogUtil.putLog(getLogMsg(LOG_HWNFC_TAG, msg, e, isNeedProguard));
        LogUtil.e(LOG_HWNFC_TAG, msg, e, isNeedProguard);
    }
}
