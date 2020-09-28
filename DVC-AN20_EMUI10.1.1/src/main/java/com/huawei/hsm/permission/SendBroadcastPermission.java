package com.huawei.hsm.permission;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;

public class SendBroadcastPermission {
    private static final String INSTALL_SHORTCUT_PERMISSION = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static final String TAG = "SendBroadcastPermission";
    private static final String UNINSTALL_SHORTCUT_PERMISSION = "com.android.launcher.action.UNINSTALL_SHORTCUT";
    private int mPermissionType;
    private int mPid;
    private int mUid;

    public SendBroadcastPermission() {
        this.mPermissionType = 0;
    }

    public SendBroadcastPermission(Context context) {
        this.mPermissionType = 0;
        this.mUid = Binder.getCallingUid();
        this.mPid = Binder.getCallingPid();
    }

    private void getPermissionType(Intent intent) {
        String action = intent.getAction();
        if (INSTALL_SHORTCUT_PERMISSION.equals(action) || UNINSTALL_SHORTCUT_PERMISSION.equals(action)) {
            this.mPermissionType = 16777216;
        }
    }

    public boolean allowSendBroadcast(Intent intent) {
        getPermissionType(intent);
        int i = this.mPermissionType;
        if (i == 0) {
            return true;
        }
        int selectionResult = StubController.holdForGetPermissionSelection(i, this.mUid, this.mPid, null);
        if (selectionResult == 0) {
            Log.e(TAG, "SendBroadcastPermission holdForGetPermissionSelection error");
            return false;
        } else if (selectionResult != 2) {
            return true;
        } else {
            return false;
        }
    }

    private void getPermissionType(String action) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
            this.mPermissionType = StubController.PERMISSION_RECEIVE_SMS;
        }
    }

    public void insertSendBroadcastRecord(String pkgName, String action, int uid) {
        getPermissionType(action);
        if (this.mPermissionType == 0) {
            Log.i(TAG, "insertSendBroadcastRecord mPermissionType = " + this.mPermissionType);
        } else if (StubController.checkPreconditionPermissionEnabled()) {
            Log.i(TAG, "insertSendBroadcastRecord, do not need to check permission.");
        }
    }

    private boolean isGlobalSwitchOn(Intent intent, int uid, int pid) {
        return true;
    }
}
