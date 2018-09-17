package com.huawei.hsm.permission;

import android.os.Binder;
import android.util.Log;
import huawei.com.android.internal.widget.HwFragmentContainer;

public class ReadMotionDataPermission {
    private static final String TAG = "ReadMotionDataPermission";
    private int mPermissionType;
    private int mPid;
    private int mUid;

    public ReadMotionDataPermission() {
        this.mPermissionType = StubController.RMD_PERMISSION_CODE;
        this.mUid = Binder.getCallingUid();
        this.mPid = Binder.getCallingPid();
    }

    public boolean allowOp() {
        if (this.mPermissionType == 0 || !StubController.checkPrecondition(this.mUid) || !isGlobalSwitchOn(this.mUid, this.mPid)) {
            return true;
        }
        switch (StubController.holdForGetPermissionSelection(this.mPermissionType, this.mUid, this.mPid, null)) {
            case HwFragmentContainer.SPLITE_MODE_DEFAULT_SEPARATE /*0*/:
                Log.e(TAG, "holdForGetPermissionSelection error");
                return false;
            case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                return false;
            default:
                return true;
        }
    }

    private boolean isGlobalSwitchOn(int uid, int pid) {
        return true;
    }
}
