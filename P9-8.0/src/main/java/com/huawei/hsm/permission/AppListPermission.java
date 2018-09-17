package com.huawei.hsm.permission;

import android.os.Binder;
import android.util.Log;

public class AppListPermission {
    private static final String TAG = "AppListPermission";
    private int mPermissionType = StubController.PERMISSION_GET_PACKAGE_LIST;
    private int mPid = Binder.getCallingPid();
    private int mUid = Binder.getCallingUid();

    public boolean allowOp() {
        if (this.mPermissionType == 0 || !StubController.checkPrecondition(this.mUid) || !isGlobalSwitchOn(this.mUid, this.mPid)) {
            return true;
        }
        switch (StubController.holdForGetPermissionSelection(this.mPermissionType, this.mUid, this.mPid, null)) {
            case 0:
                Log.e(TAG, "AppListPermission holdForGetPermissionSelection error");
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
