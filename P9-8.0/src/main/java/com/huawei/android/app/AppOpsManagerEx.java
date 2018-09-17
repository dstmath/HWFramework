package com.huawei.android.app;

import android.app.AppOpsManager;
import android.os.Bundle;
import android.os.RemoteException;
import com.huawei.hsm.permission.StubController;
import com.huawei.permission.IHoldService;

public class AppOpsManagerEx {
    private static final String EXTRA_CODE = "opCode";
    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_MONITOR = "shouldMonitor";
    private static final String EXTRA_PACKAGE = "packageName";
    private static final String EXTRA_RESULT = "result";
    public static final int MODE_ALLOWED = 1;
    public static final int MODE_IGNORED = 2;
    public static final int MODE_REMIND = 0;
    public static final int MODE_UNKNOWN = 3;
    private static final int MONITOR = 1;
    public static final int OP_GET_USAGE_STATS = 43;
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
        if (service == null) {
            throw new RemoteException("Can't find service.");
        }
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

    public static int getMode(int code, String packageName) throws RemoteException {
        IHoldService service = StubController.getHoldService();
        if (service == null) {
            throw new RemoteException("Can't find service.");
        }
        Bundle params = new Bundle();
        params.putInt(EXTRA_CODE, code);
        params.putString(EXTRA_PACKAGE, packageName);
        Bundle result = service.callHsmService("getMode", params);
        if (result != null) {
            return result.getInt(EXTRA_RESULT, 3);
        }
        throw new RemoteException("Exception in service.");
    }

    public static boolean systemFixed(String pkgName) throws RemoteException {
        IHoldService service = StubController.getHoldService();
        if (service == null) {
            throw new RemoteException("Can't find service.");
        }
        Bundle params = new Bundle();
        params.putString(EXTRA_PACKAGE, pkgName);
        Bundle result = service.callHsmService("checkShoudMonitor", params);
        if (result == null) {
            throw new RemoteException("Exception in service.");
        } else if (result.getInt(EXTRA_MONITOR) == 1) {
            return false;
        } else {
            return true;
        }
    }

    public static int checkOp(AppOpsManager aom, int op, int uid, String packageName) {
        return aom.checkOp(op, uid, packageName);
    }
}
