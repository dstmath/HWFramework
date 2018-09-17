package com.huawei.hsm.permission;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;

public class CallPermission {
    private static final String TAG = "CallPermission";
    private Context mContext;

    public CallPermission(Context context) {
        this.mContext = context;
    }

    public boolean isCallBlocked(Intent intent, int uid, int pid) {
        boolean z = true;
        if (StubController.checkPreBlock(uid, 64)) {
            return true;
        }
        if (!StubController.checkPrecondition(uid) || !StubController.isGlobalSwitchOn(this.mContext, 64)) {
            return false;
        }
        String uriStr = intent.getDataString();
        if (uriStr == null) {
            Log.e(TAG, "error! uriStr is null!!!");
            return false;
        }
        int selectionResult;
        String tel = uriStr.substring(uriStr.indexOf(58) + 1);
        if (tel.startsWith("*72") || tel.startsWith("*90") || tel.startsWith("*92") || tel.startsWith("*68") || tel.startsWith("**62*") || tel.startsWith("**61*") || tel.startsWith("**67*") || tel.startsWith("**21*")) {
            selectionResult = StubController.holdForGetPermissionSelection(StubController.PERMISSION_CALL_FORWARD, uid, pid, tel);
        } else if (tel.startsWith("*720") || tel.startsWith("*900") || tel.startsWith("*920") || tel.startsWith("*680") || tel.startsWith("%23%2362%23") || tel.startsWith("%23%2361%23") || tel.startsWith("%23%2367%23") || tel.startsWith("%23%2321%23") || tel.startsWith("%23%23002%23")) {
            selectionResult = StubController.holdForGetPermissionSelection(StubController.PERMISSION_CALL_FORWARD, uid, pid, tel);
        } else {
            selectionResult = StubController.holdForGetPermissionSelection(64, uid, pid, tel);
        }
        if (selectionResult == 0) {
            Log.e(TAG, "CallPermission holdForGetPermissionSelection error");
            return false;
        }
        if (2 != selectionResult) {
            z = false;
        }
        return z;
    }

    public static boolean blockStartActivity(Context context, Intent intent) {
        if ("android.intent.action.CALL".equals(intent.getAction())) {
            return new CallPermission(context).isCallBlocked(intent, Binder.getCallingUid(), Binder.getCallingPid());
        }
        if (intentToDial(intent) && StubController.checkPreBlock(Binder.getCallingUid(), 64)) {
            return true;
        }
        return false;
    }

    private static boolean intentToDial(Intent intent) {
        String action = intent.getAction();
        if ("android.intent.action.DIAL".equals(action)) {
            return true;
        }
        if ("android.intent.action.VIEW".equals(action)) {
            String scheme;
            if (intent.getData() != null) {
                scheme = intent.getData().getScheme();
            } else {
                scheme = null;
            }
            if (scheme != null && scheme.equalsIgnoreCase("tel")) {
                return true;
            }
        }
        return false;
    }
}
