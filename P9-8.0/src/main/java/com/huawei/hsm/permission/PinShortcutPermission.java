package com.huawei.hsm.permission;

import android.os.Binder;
import android.util.Log;

public class PinShortcutPermission {
    private static final String TAG = "PinShortcutPermission";
    private int mPermissionType = StubController.PERMISSION_EDIT_SHORTCUT;
    private int mPid = Binder.getCallingPid();
    private int mUid = Binder.getCallingUid();

    public boolean allowOp() {
        if (this.mPermissionType == 0 || !StubController.checkPrecondition(this.mUid)) {
            return true;
        }
        switch (StubController.holdForGetPermissionSelection(this.mPermissionType, this.mUid, this.mPid, null)) {
            case 0:
                Log.e(TAG, "PinShortcutPermission holdForGetPermissionSelection error");
                return false;
            case 2:
                return false;
            default:
                return true;
        }
    }
}
