package com.huawei.hsm.permission;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;
import com.huawei.hsm.permission.monitor.PermRecordHandler;

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

    private void recordPermissionUsed() {
        PermRecordHandler mPermRecHandler = PermRecordHandler.getHandleInstance();
        if (mPermRecHandler != null) {
            mPermRecHandler.accessPermission(this.mUid, this.mPid, this.mPermissionType, null);
        }
    }

    public boolean allowSendBroadcast(Intent intent) {
        getPermissionType(intent);
        if (this.mPermissionType == 0) {
            return true;
        }
        if (!StubController.checkPrecondition(this.mUid)) {
            recordPermissionUsed();
            return true;
        } else if (!isGlobalSwitchOn(intent, this.mUid, this.mPid)) {
            return true;
        } else {
            int selectionResult = StubController.holdForGetPermissionSelection(this.mPermissionType, this.mUid, this.mPid, null);
            if (selectionResult == 0) {
                Log.e(TAG, "SendBroadcastPermission holdForGetPermissionSelection error");
                return false;
            } else if (selectionResult != 2) {
                return true;
            } else {
                return false;
            }
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
            recordPermissionUsed();
        } else {
            StubController.holdForInsertBroadcastRecord(pkgName, this.mPermissionType, uid);
        }
    }

    private boolean isGlobalSwitchOn(Intent intent, int uid, int pid) {
        return true;
    }
}
