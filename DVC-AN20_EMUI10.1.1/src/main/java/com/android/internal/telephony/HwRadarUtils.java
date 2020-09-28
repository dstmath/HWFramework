package com.android.internal.telephony;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.util.LogException;

public class HwRadarUtils {
    private static final String APP_VERSION = "1.0";
    private static final String CAT_CSP = "csp";
    private static final String CAT_MMS = "mms";
    private static final String CHR_ACTION = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
    private static final String CHR_RECEIVE_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String DEFAULT_PACKAGE = "com.android.mms";
    public static final int ERROR_BASE_MMS = 1300;
    public static final int ERR_SMS_RECEIVE = 1312;
    public static final int ERR_SMS_SEND = 1311;
    public static final int ERR_SMS_SEND_BACKGROUND = 1317;
    private static final int INVALID_SUBID = -1;
    private static final int MODULE_ID = 5000;
    private static final int RADAR_BUG_TYPE_FUNCTION_ERROR = 100;
    public static final int RADAR_LEVEL_A = 65;
    public static final int RADAR_LEVEL_B = 66;
    public static final int RADAR_LEVEL_C = 67;
    public static final int RADAR_LEVEL_D = 68;
    private static final String TAG = "HwRadarUtils";
    private static LogException mLogException = HwFrameworkFactory.getLogException();
    private static final String sAppInfo;

    static {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Package: ");
        sb.append(DEFAULT_PACKAGE);
        sb.append("\n");
        sb.append("APK version: ");
        sb.append(APP_VERSION);
        sb.append("\n");
        sAppInfo = sb.toString();
    }

    private HwRadarUtils() {
    }

    public static void report(int errorType, String content) {
        report(null, errorType, content, 0);
    }

    public static void report(int errorType, String content, int subId) {
        report(null, errorType, content, subId);
    }

    public static void report(Context context, int errorType, String content) {
        report(context, errorType, content, SubscriptionManager.getDefaultSmsSubscriptionId());
    }

    public static void report(Context context, int errorType, String content, int subId) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(sAppInfo);
        sb.append("Bug type: ");
        sb.append(String.valueOf(transalateErrorToBugType(errorType)));
        sb.append("\n");
        sb.append("Scene def: ");
        sb.append(String.valueOf(errorType));
        sb.append("\n");
        String header = sb.toString();
        if (isNeedToTriggerAppRadar(errorType)) {
            reportApplicationRadarLog(header, content);
        }
        if (isNeedToTriggerCHR(errorType)) {
            reportChr(context, errorType, content, subId);
        }
    }

    private static final int transalateErrorToBugType(int error) {
        return 100;
    }

    private static boolean isNeedToTriggerAppRadar(int errorType) {
        Rlog.d(TAG, "isNeedToTriggerAppRadar for error:" + errorType);
        if (errorType == 1311) {
            Rlog.d(TAG, "not need to trigger applicaton radar log");
            return false;
        } else if (errorType == 1312 || errorType == 1317) {
            Rlog.d(TAG, "need to trigger applicaton radar log");
            return true;
        } else {
            Rlog.d(TAG, "Not need to trigger APP Radar");
            return false;
        }
    }

    private static boolean isNeedToTriggerCHR(int errorType) {
        Rlog.d(TAG, "isNeedToTriggerCHR for error:" + errorType);
        if (errorType == 1312 || errorType == 1317) {
            Rlog.d(TAG, "need To TriggerCHR");
            return true;
        }
        Rlog.d(TAG, "not need to trigger CHR");
        return false;
    }

    public static void reportApplicationRadarLog(String header, String msg) {
        reportApplicationRadarLog(CAT_MMS, 65, header, msg);
    }

    public static void reportApplicationRadarLog(String category, int level, String header, String msg) {
        if (mLogException != null) {
            try {
                Rlog.w(TAG, "radar report in FW:" + msg);
                mLogException.msg(category, level, header, msg);
            } catch (Throwable th) {
                Rlog.e(TAG, "call radar interface has exception");
            }
        } else {
            Rlog.e(TAG, "Radar interface is not support");
        }
    }

    private static void reportChr(Context context, int errorType, String content, int subId) {
        if (context == null || subId == -1) {
            Rlog.e(TAG, "para is error, not to trigger reportChr");
            return;
        }
        Intent intent = new Intent("com.huawei.android.chr.action.ACTION_REPORT_CHR");
        intent.putExtra("module_id", MODULE_ID);
        intent.putExtra("event_id", errorType);
        intent.putExtra("sub", subId);
        intent.putExtra("app_data", content);
        context.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
        Rlog.d(TAG, "reportChr in FW done");
    }
}
