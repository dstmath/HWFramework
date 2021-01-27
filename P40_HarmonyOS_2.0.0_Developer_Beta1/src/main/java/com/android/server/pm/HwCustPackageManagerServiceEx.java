package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import huawei.cust.HwCustUtils;
import java.io.File;

public class HwCustPackageManagerServiceEx {
    private HwCustPackageManagerService managerService = ((HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[0]));

    public HwCustPackageManagerService getHwCustPackageManagerService() {
        return this.managerService;
    }

    public void setHwCustPackageManagerService(HwCustPackageManagerService managerService2) {
        this.managerService = managerService2;
    }

    public boolean isHwCustHiddenInfoPackage(PackageParserEx.PackageEx pkgInfo) {
        HwCustPackageManagerService hwCustPackageManagerService = this.managerService;
        if (hwCustPackageManagerService != null) {
            return hwCustPackageManagerService.isHwCustHiddenInfoPackage((PackageParser.Package) pkgInfo.getPackage());
        }
        return false;
    }

    public File customizeUninstallApk(File file) {
        HwCustPackageManagerService hwCustPackageManagerService = this.managerService;
        if (hwCustPackageManagerService != null) {
            return hwCustPackageManagerService.customizeUninstallApk(file);
        }
        return file;
    }

    public String getCustDefaultLauncher(Context context, String pkg) {
        HwCustPackageManagerService hwCustPackageManagerService = this.managerService;
        if (hwCustPackageManagerService != null) {
            return hwCustPackageManagerService.getCustDefaultLauncher(context, pkg);
        }
        return "";
    }

    public boolean isMccMncMatch() {
        return this.managerService.isMccMncMatch();
    }

    public boolean isUnAppInstallAllowed(String originPath, Context context) {
        return this.managerService.isUnAppInstallAllowed(originPath, context);
    }
}
