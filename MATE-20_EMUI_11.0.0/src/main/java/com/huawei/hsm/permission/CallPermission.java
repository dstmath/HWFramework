package com.huawei.hsm.permission;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;

public class CallPermission {
    private static final String[] BEGIN_NUMS = {"*72", "*90", "*92", "*68", "**62*", "*62*", "%2362%23", "**61*", "*61*", "%2361%23", "**67*", "*67*", "%2367%23", "**21*", "*21*", "%2321%23", "*720", "*920", "*680", "*74", "*730", "%23%2362%23", "%23%2361%23", "%23%2367%23", "%23%2321%23", "*900", "%23%23002%23"};
    private static final String TAG = "CallPermission";
    private Context mContext;

    public CallPermission(Context context) {
        this.mContext = context;
    }

    private int getCallType(String tel) {
        for (String head : BEGIN_NUMS) {
            if (tel.startsWith(head)) {
                return StubController.PERMISSION_CALL_FORWARD;
            }
        }
        return 64;
    }

    public boolean isCallBlocked(Intent intent, int uid, int pid) {
        int permissionType = 0;
        String tel = null;
        String uriStr = intent.getDataString();
        if (uriStr != null) {
            tel = uriStr.substring(uriStr.indexOf(58) + 1);
            permissionType = getCallType(tel);
        }
        if (uriStr == null) {
            Log.e(TAG, "error! uriStr is null!!!");
            return false;
        }
        int selectionResult = StubController.holdForGetPermissionSelection(permissionType, uid, pid, tel);
        if (selectionResult == 0) {
            Log.e(TAG, "CallPermission holdForGetPermissionSelection error");
            return false;
        } else if (2 == selectionResult) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean blockStartActivity(Context context, Intent intent) {
        if ("android.intent.action.CALL".equals(intent.getAction())) {
            return new CallPermission(context).isCallBlocked(intent, Binder.getCallingUid(), Binder.getCallingPid());
        }
        if (!intentToDial(intent) || !StubController.checkPreBlock(Binder.getCallingUid(), 64)) {
            return false;
        }
        return true;
    }

    private static boolean intentToDial(Intent intent) {
        String action = intent.getAction();
        if ("android.intent.action.DIAL".equals(action)) {
            return true;
        }
        if (!"android.intent.action.VIEW".equals(action)) {
            return false;
        }
        String scheme = intent.getData() != null ? intent.getData().getScheme() : null;
        if (scheme == null || !scheme.equalsIgnoreCase("tel")) {
            return false;
        }
        return true;
    }
}
