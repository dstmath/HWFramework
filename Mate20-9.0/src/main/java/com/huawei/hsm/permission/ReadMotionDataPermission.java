package com.huawei.hsm.permission;

import android.os.Binder;
import android.util.Log;
import com.huawei.hsm.permission.monitor.PermRecordHandler;

public class ReadMotionDataPermission {
    private static final String TAG = "ReadMotionDataPermission";
    private int mPermissionType = StubController.RMD_PERMISSION_CODE;
    private int mPid = Binder.getCallingPid();
    private int mUid = Binder.getCallingUid();

    private void recordPermissionUsed() {
        PermRecordHandler mPermRecHandler = PermRecordHandler.getHandleInstance();
        if (mPermRecHandler != null) {
            mPermRecHandler.accessPermission(this.mUid, this.mPid, this.mPermissionType, null);
        }
    }

    public boolean allowOp() {
        if (!isGlobalSwitchOn(this.mUid, this.mPid) || this.mPermissionType == 0) {
            return true;
        }
        if (!StubController.checkPrecondition(this.mUid)) {
            recordPermissionUsed();
            return true;
        }
        int selectionResult = StubController.holdForGetPermissionSelection(this.mPermissionType, this.mUid, this.mPid, null);
        if (selectionResult == 0) {
            Log.e(TAG, "holdForGetPermissionSelection error");
            return false;
        } else if (selectionResult != 2) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isGlobalSwitchOn(int uid, int pid) {
        return false;
    }
}
