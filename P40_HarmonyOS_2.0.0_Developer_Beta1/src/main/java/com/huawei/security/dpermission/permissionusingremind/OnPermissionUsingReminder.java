package com.huawei.security.dpermission.permissionusingremind;

import android.os.Bundle;

public interface OnPermissionUsingReminder {
    void onPermissionStartUsing(Bundle bundle);

    void onPermissionStopUsing(Bundle bundle);
}
