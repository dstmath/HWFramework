package com.huawei.hsm.permission;

import android.os.Binder;
import android.util.Log;

public class PinShortcutPermission {
    private static final String TAG = "PinShortcutPermission";
    private int mPermissionType = 16777216;
    private int mPid = Binder.getCallingPid();
    private int mUid = Binder.getCallingUid();

    public boolean allowOp() {
        int i = this.mPermissionType;
        if (i == 0) {
            return true;
        }
        int selectionResult = StubController.holdForGetPermissionSelection(i, this.mUid, this.mPid, null);
        if (selectionResult == 0) {
            Log.e(TAG, "PinShortcutPermission holdForGetPermissionSelection error");
            return false;
        } else if (selectionResult != 2) {
            return true;
        } else {
            return false;
        }
    }
}
