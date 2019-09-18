package com.huawei.android.app;

import android.app.AppOpsManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import com.huawei.hsm.permission.StubController;
import com.huawei.permission.IHoldService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppOpsManagerEx {
    private static final String EXTRA_CODE = "opCode";
    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_MONITOR = "shouldMonitor";
    private static final String EXTRA_PACKAGE = "packageName";
    private static final String EXTRA_RESULT = "result";
    public static final int GET_OP_CAMERA = 0;
    public static final int GET_OP_RECORD_AUDIO = 1;
    public static final int MODE_ALLOWED = 1;
    public static final int MODE_IGNORED = 2;
    public static final int MODE_REMIND = 0;
    public static final int MODE_UNKNOWN = 3;
    private static final int MONITOR = 1;
    public static final int OP_ADD_VOICEMAIL = 52;
    public static final int OP_GET_USAGE_STATS = 43;
    public static final int OP_READ_CALL_LOG = 6;
    public static final int OP_READ_CONTACTS = 4;
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
        IHoldService service = StubController.getHoldService();
        if (service != null) {
            Bundle params = new Bundle();
            params.putInt(EXTRA_CODE, code);
            params.putString(EXTRA_PACKAGE, packageName);
            params.putInt(EXTRA_MODE, mode);
            Bundle result = service.callHsmService("setMode", params);
            if (result != null) {
                return result.getInt(EXTRA_RESULT, 0);
            }
            throw new RemoteException("Exception in service.");
        }
        throw new RemoteException("Can't find service.");
    }

    public static int getMode(int code, String packageName) throws RemoteException {
        IHoldService service = StubController.getHoldService();
        if (service != null) {
            Bundle params = new Bundle();
            params.putInt(EXTRA_CODE, code);
            params.putString(EXTRA_PACKAGE, packageName);
            Bundle result = service.callHsmService("getMode", params);
            if (result != null) {
                return result.getInt(EXTRA_RESULT, 3);
            }
            throw new RemoteException("Exception in service.");
        }
        throw new RemoteException("Can't find service.");
    }

    public static boolean systemFixed(String pkgName) throws RemoteException {
        IHoldService service = StubController.getHoldService();
        if (service != null) {
            Bundle params = new Bundle();
            params.putString(EXTRA_PACKAGE, pkgName);
            Bundle result = service.callHsmService("checkShoudMonitor", params);
            if (result != null) {
                return result.getInt(EXTRA_MONITOR) != 1;
            }
            throw new RemoteException("Exception in service.");
        }
        throw new RemoteException("Can't find service.");
    }

    public static int checkOp(AppOpsManager aom, int op, int uid, String packageName) {
        return aom.checkOp(op, uid, packageName);
    }

    public static void setMode(AppOpsManager aom, int code, int uid, String packageName, int mode) {
        aom.setMode(code, uid, packageName, mode);
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
                    List<AppOpsManager.OpEntry> appOpsList = packageOps.getOps();
                    if (appOpsList != null && !appOpsList.isEmpty() && appOpsList.get(0).getOp() == op) {
                        opsMode = appOpsList.get(0).getMode();
                    }
                    pkgNameOpsMap.put(packageName, Integer.valueOf(opsMode));
                }
            }
        }
        return pkgNameOpsMap;
    }

    public static final int getOp(int type) {
        switch (type) {
            case 0:
                return 26;
            case 1:
                return 27;
            default:
                return -1;
        }
    }
}
