package com.huawei.android.app;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import com.huawei.annotation.HwSystemApi;
import com.huawei.securitycenter.HwPermissionManagerAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppOpsManagerEx {
    public static final int GET_OP_CAMERA = 0;
    public static final int GET_OP_RECORD_AUDIO = 1;
    public static final int MODE_ALLOWED = 1;
    public static final int MODE_IGNORED = 2;
    public static final int MODE_REMIND = 0;
    public static final int MODE_UNKNOWN = 3;
    @HwSystemApi
    public static final String OPSTR_REQUEST_INSTALL_PACKAGES = "android:request_install_packages";
    @HwSystemApi
    public static final String OPSTR_WRITE_SMS = "android:write_sms";
    public static final int OP_ADD_VOICEMAIL = 52;
    public static final int OP_GET_USAGE_STATS = 43;
    public static final int OP_READ_CALL_LOG = 6;
    public static final int OP_READ_CONTACTS = 4;
    @HwSystemApi
    public static final int OP_READ_PHONE_STATE = 51;
    public static final int OP_REQUEST_INSTALL_PACKAGES = 66;
    public static final int OP_SYSTEM_ALERT_WINDOW = 24;
    public static final int OP_WRITE_CALL_LOG = 7;
    public static final int OP_WRITE_CONTACTS = 5;
    public static final int RESULT_BAD_ARGUMENTS = 2;
    public static final int RESULT_NO_SERVICE = 1;
    public static final int RESULT_OK = 0;
    public static final int RESULT_SYSTEM_FIXED = 3;
    public static final int TYPE_ACCESS_CALENDAR = 2048;
    public static final int TYPE_CALL_PHONE = 64;
    public static final int TYPE_CAMERA = 1024;
    public static final int TYPE_DELETE_CALLLOG = 262144;
    public static final int TYPE_DELETE_CONTACTS = 131072;
    public static final int TYPE_LOCATION = 8;
    public static final int TYPE_MICROPHONE = 128;
    public static final int TYPE_NET = Integer.MIN_VALUE;
    public static final int TYPE_OPEN_BLUETOOTH = 8388608;
    public static final int TYPE_OPEN_MOBILENETWORK = 4194304;
    public static final int TYPE_OPEN_WIFI = 2097152;
    public static final int TYPE_PHONE_CODE = 16;
    public static final int TYPE_READ_CALLLOG = 2;
    public static final int TYPE_READ_CONTACTS = 1;
    public static final int TYPE_READ_MSG = 4;
    public static final int TYPE_SEND_MMS = 8192;
    public static final int TYPE_SEND_SMS = 32;
    public static final int TYPE_WRITE_CALLLOG = 32768;
    public static final int TYPE_WRITE_CONTACTS = 16384;

    public static int setMode(int code, String packageName, int mode) throws RemoteException {
        return HwPermissionManagerAdapter.setMode(code, packageName, mode);
    }

    public static void setMode(AppOpsManager aom, int code, int uid, String packageName, int mode) {
        aom.setMode(code, uid, packageName, mode);
    }

    @HwSystemApi
    public static void setMode(AppOpsManager aom, String op, int uid, String packageName, int mode) {
        aom.setMode(op, uid, packageName, mode);
    }

    public static int getMode(int code, String packageName) throws RemoteException {
        return HwPermissionManagerAdapter.getMode(code, packageName);
    }

    public static boolean systemFixed(String pkgName) throws RemoteException {
        return HwPermissionManagerAdapter.systemFixed(pkgName);
    }

    public static int checkOp(AppOpsManager aom, int op, int uid, String packageName) {
        return aom.checkOp(op, uid, packageName);
    }

    public static void setUidMode(AppOpsManager aom, String appOp, int uid, int mode) {
        aom.setUidMode(appOp, uid, mode);
    }

    public static Map<String, Integer> getPackagesForOp(AppOpsManager aom, int op, int userId) {
        Map<String, Integer> pkgNameOpsMap = new HashMap<>();
        List<AppOpsManager.PackageOps> packageOpsList = aom.getPackagesForOps(new int[]{op});
        if (packageOpsList != null) {
            for (AppOpsManager.PackageOps packageOps : packageOpsList) {
                if (userId == UserHandle.getUserId(packageOps.getUid())) {
                    int opsMode = AppOpsManager.opToDefaultMode(op);
                    String packageName = packageOps.getPackageName();
                    List<AppOpsManager.OpEntry> appOps = packageOps.getOps();
                    if (appOps != null && !appOps.isEmpty() && appOps.get(0).getOp() == op) {
                        opsMode = appOps.get(0).getMode();
                    }
                    pkgNameOpsMap.put(packageName, Integer.valueOf(opsMode));
                }
            }
        }
        return pkgNameOpsMap;
    }

    public static final int getOp(int type) {
        if (type == 0) {
            return 26;
        }
        if (type != 1) {
            return -1;
        }
        return 27;
    }

    public static Map<String, Bundle> getPermissionUseHistory(Context context, AppOpsManager aom, ArrayList<String> permNameList, long duration) {
        return HwPermissionManagerAdapter.getPermissionUseHistory(context, aom, permNameList, duration);
    }
}
