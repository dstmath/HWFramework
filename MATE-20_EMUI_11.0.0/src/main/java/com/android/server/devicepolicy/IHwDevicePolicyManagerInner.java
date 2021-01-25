package com.android.server.devicepolicy;

import android.content.ComponentName;
import com.android.server.devicepolicy.HwDevicePolicyManagerInnerEx;

public interface IHwDevicePolicyManagerInner {
    void enforceFullCrossUsersPermissionInner(int i);

    HwDevicePolicyManagerInnerEx.ActiveAdminEx getActiveAdminForCallerLockedInner(ComponentName componentName, int i) throws SecurityException;

    HwDevicePolicyManagerInnerEx.ActiveAdminEx getActiveAdminUncheckedLockedInner(ComponentName componentName, int i);

    Object getLockObjectInner();

    HwDevicePolicyManagerInnerEx.DevicePolicyDataEx getUserDataInner(int i);

    void saveSettingsLockedInner(int i);
}
