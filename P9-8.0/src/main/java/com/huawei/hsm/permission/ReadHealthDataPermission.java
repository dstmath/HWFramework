package com.huawei.hsm.permission;

import android.os.Binder;
import android.util.Log;

public class ReadHealthDataPermission {
    private static final String TAG = "ReadHealthDataPermission";
    private int mPermissionType = StubController.RHD_PERMISSION_CODE;
    private int mPid = Binder.getCallingPid();
    private int mUid = Binder.getCallingUid();

    public boolean allowOp() {
        if (this.mPermissionType == 0 || !StubController.checkPrecondition(this.mUid) || !isGlobalSwitchOn(this.mUid, this.mPid)) {
            return true;
        }
        switch (StubController.holdForGetPermissionSelection(this.mPermissionType, this.mUid, this.mPid, null)) {
            case 0:
                Log.e(TAG, "holdForGetPermissionSelection error");
                return false;
            case 2:
                return false;
            default:
                return true;
        }
    }

    private boolean isGlobalSwitchOn(int uid, int pid) {
        return true;
    }
}
