package com.huawei.hsm.permission;

import android.os.Binder;
import android.util.Log;
import com.huawei.hsm.permission.monitor.PermRecordHandler;

public class ReadHealthDataPermission {
    private static final String TAG = "ReadHealthDataPermission";
    private int mPermissionType = StubController.RHD_PERMISSION_CODE;
    private int mPid = Binder.getCallingPid();
    private int mUid = Binder.getCallingUid();

    private void recordPermissionUsed() {
        PermRecordHandler mPermRecHandler = PermRecordHandler.getHandleInstance();
        if (mPermRecHandler != null) {
            mPermRecHandler.accessPermission(this.mUid, this.mPid, this.mPermissionType, null);
        }
    }

    public boolean allowOp() {
        if (this.mPermissionType == 0) {
            return true;
        }
        if (!StubController.checkPrecondition(this.mUid)) {
            recordPermissionUsed();
            return true;
        } else if (!isGlobalSwitchOn(this.mUid, this.mPid)) {
            return true;
        } else {
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
    }

    private boolean isGlobalSwitchOn(int uid, int pid) {
        return true;
    }
}
