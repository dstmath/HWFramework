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
            this.mPermissionType = StubController.PERMISSION_EDIT_SHORTCUT;
        }
    }

    public boolean allowSendBroadcast(Intent intent) {
        getPermissionType(intent);
        if (this.mPermissionType == 0 || !StubController.checkPrecondition(this.mUid) || !isGlobalSwitchOn(intent, this.mUid, this.mPid)) {
            return true;
        }
        switch (StubController.holdForGetPermissionSelection(this.mPermissionType, this.mUid, this.mPid, null)) {
            case 0:
                Log.e(TAG, "SendBroadcastPermission holdForGetPermissionSelection error");
                return false;
            case 2:
                return false;
            default:
                return true;
        }
    }

    private void getPermissionType(String action) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
            this.mPermissionType = 4096;
        }
    }

    public void insertSendBroadcastRecord(String pkgName, String action, int uid) {
        getPermissionType(action);
        if (this.mPermissionType == 0) {
            Log.i(TAG, "insertSendBroadcastRecord mPermissionType = " + this.mPermissionType);
        } else if (StubController.checkPreconditionPermissionEnabled()) {
            Log.i(TAG, "insertSendBroadcastRecord, do not need to check permission.");
        } else {
            StubController.holdForInsertBroadcastRecord(pkgName, this.mPermissionType, uid);
        }
    }

    private boolean isGlobalSwitchOn(Intent intent, int uid, int pid) {
        return true;
    }
}
