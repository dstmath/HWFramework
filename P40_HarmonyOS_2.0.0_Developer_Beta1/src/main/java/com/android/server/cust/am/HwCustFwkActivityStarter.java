package com.android.server.cust.am;

import android.content.ComponentName;
import android.content.Intent;
import android.hdm.HwDeviceManager;
import android.util.Slog;
import com.android.server.cust.utils.HwCustPkgNameConstant;
import java.util.Arrays;
import java.util.List;

public class HwCustFwkActivityStarter {
    private static final String TAG = "HwCustFwkActivityStarter";
    private static final List<String> TASK_LOCK_ACTIVITY_WHITE_LIST = Arrays.asList(HwCustPkgNameConstant.HW_DESKLOCK_FULL_ACTIVITYNAME, HwCustPkgNameConstant.HW_DESKLOCK_FULL_ACTIVITYNAME_BEFORE);
    private static final List<String> TASK_LOCK_PKG_WHITE_LIST = Arrays.asList("com.huawei.android.launcher", "com.huawei.systemmanager", HwCustPkgNameConstant.HW_PERMISSION_CONTROLLER_PACKAGE, HwCustPkgNameConstant.HW_SYSTEMUI_PACKAGE, HwCustPkgNameConstant.HW_HWOUC_PACKAGE, HwCustPkgNameConstant.HW_STARTUP_GUIDE, HwCustPkgNameConstant.HW_UPGRADE_GUIDE, HwCustPkgNameConstant.PACKAGE_INSTALLER);
    private static volatile boolean isDisableTaskLock = true;

    public int getPreventStartStatus(Intent intent) {
        if (intent != null && isAppDisabledByTaskLock(intent)) {
            return 102;
        }
        return 0;
    }

    private boolean isAppDisabledByTaskLock(Intent intent) {
        ComponentName componentName;
        List<String> taskLockAppList = HwDeviceManager.getList(55);
        if (taskLockAppList == null || taskLockAppList.isEmpty() || (componentName = intent.getComponent()) == null) {
            return false;
        }
        String pkgName = componentName.getPackageName();
        if (taskLockAppList.contains(pkgName)) {
            isDisableTaskLock = false;
            return false;
        } else if (isDisableTaskLock || TASK_LOCK_PKG_WHITE_LIST.contains(pkgName)) {
            return false;
        } else {
            String componentString = componentName.flattenToShortString();
            if (TASK_LOCK_ACTIVITY_WHITE_LIST.contains(componentString)) {
                return false;
            }
            Slog.i(TAG, "In mdm task-lock-mode, can not start. ComponentString: " + componentString);
            return true;
        }
    }
}
