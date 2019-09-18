package com.android.server.pm;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;

public class HwParentControlUtils {
    private static final String TAG = "HwParentControlUtils";
    private static volatile HwParentControlUtils mInstance;
    private IHwPackageManagerServiceExInner mHwPmsExInner;
    private Context mPmsContext = this.mHwPmsExInner.getContextInner();
    private IHwPackageManagerInner mPmsInner = this.mHwPmsExInner.getIPmsInner();

    private HwParentControlUtils(IHwPackageManagerServiceExInner pmsEx) {
        this.mHwPmsExInner = pmsEx;
    }

    public static HwParentControlUtils getInstance(IHwPackageManagerServiceExInner pmsEx) {
        if (mInstance == null) {
            synchronized (HwParentControlUtils.class) {
                if (mInstance == null) {
                    mInstance = new HwParentControlUtils(pmsEx);
                }
            }
        }
        return mInstance;
    }

    public boolean isAppInstallAllowed(String installer, String appName) {
        if (!isParentControlEnabled() || isInstallerValidForParentControl(installer) || this.mPmsInner.getPackageInfoInner(appName, 0, 0) != null) {
            return true;
        }
        return false;
    }

    private boolean isParentControlEnabled() {
        if (Settings.Secure.getInt(this.mPmsContext.getContentResolver(), "childmode_status", 0) == 0 || this.mPmsInner.getPackageInfoInner("com.huawei.parentcontrol", 0, 0) == null || !isChinaArea()) {
            return false;
        }
        return true;
    }

    private boolean isChinaArea() {
        return SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    }

    private boolean isInstallerValidForParentControl(String installer) {
        String whiteInstallerPackages = Settings.Secure.getString(this.mPmsContext.getContentResolver(), "childmode_installer_whitelist");
        if (!(whiteInstallerPackages == null || "".equals(whiteInstallerPackages.trim()) || installer == null)) {
            for (String pkg : whiteInstallerPackages.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                if (installer.equals(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }
}
