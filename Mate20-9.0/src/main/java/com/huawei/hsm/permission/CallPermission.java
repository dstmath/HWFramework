package com.huawei.hsm.permission;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;
import com.huawei.hsm.permission.monitor.PermRecordHandler;

public class CallPermission {
    private static final String TAG = "CallPermission";
    private Context mContext;

    public CallPermission(Context context) {
        this.mContext = context;
    }

    private void recordPermissionUsed(int uid, int pid, int permissionType) {
        PermRecordHandler mPermRecHandler = PermRecordHandler.getHandleInstance();
        if (mPermRecHandler != null) {
            mPermRecHandler.accessPermission(uid, pid, permissionType, null);
        }
    }

    private int getCallType(String tel) {
        if (tel.startsWith("*72") || tel.startsWith("*90") || tel.startsWith("*92") || tel.startsWith("*68") || tel.startsWith("**62*") || tel.startsWith("**61*") || tel.startsWith("**67*") || tel.startsWith("**21*") || tel.startsWith("*720") || tel.startsWith("*900") || tel.startsWith("*920") || tel.startsWith("*680") || tel.startsWith("%23%2362%23") || tel.startsWith("%23%2361%23") || tel.startsWith("%23%2367%23") || tel.startsWith("%23%2321%23") || tel.startsWith("%23%23002%23")) {
            return StubController.PERMISSION_CALL_FORWARD;
        }
        return 64;
    }

    public boolean isCallBlocked(Intent intent, int uid, int pid) {
        int permissionType = 0;
        String tel = null;
        String uriStr = intent.getDataString();
        boolean z = true;
        if (uriStr != null) {
            tel = uriStr.substring(uriStr.indexOf(58) + 1);
            permissionType = getCallType(tel);
        }
        if (StubController.checkPreBlock(uid, 64)) {
            return true;
        }
        if (!StubController.checkPrecondition(uid)) {
            recordPermissionUsed(uid, pid, permissionType);
            return false;
        } else if (!StubController.isGlobalSwitchOn(this.mContext, 64)) {
            return false;
        } else {
            if (uriStr == null) {
                Log.e(TAG, "error! uriStr is null!!!");
                return false;
            }
            int selectionResult = StubController.holdForGetPermissionSelection(permissionType, uid, pid, tel);
            if (selectionResult == 0) {
                Log.e(TAG, "CallPermission holdForGetPermissionSelection error");
                return false;
            }
            if (2 != selectionResult) {
                z = false;
            }
            return z;
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
        if ("android.intent.action.VIEW".equals(action)) {
            String scheme = intent.getData() != null ? intent.getData().getScheme() : null;
            if (scheme != null && scheme.equalsIgnoreCase("tel")) {
                return true;
            }
        }
        return false;
    }
}
