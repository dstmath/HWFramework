package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import java.util.ArrayList;

public class HotInstallEx {
    public static final String AUTO_INSTALL_APK_CONFIG = "/data/system/auto_install/APKInstallListEMUI5Release.txt";
    public static final String AUTO_INSTALL_DEL_APK_CONFIG = "/data/system/auto_install/DelAPKInstallListEMUI5Release.txt";
    public static final boolean IS_APK_INSTALL_FOREVER = HotInstall.IS_APK_INSTALL_FOREVER;
    public static final String SYSDLL_PATH = "xml/APKInstallListEMUI5Release_732999.txt";

    public static boolean isNonSystemPartition(String path) {
        return HotInstall.getInstance().isNonSystemPartition(path);
    }

    public static String replaceCotaPath(String configFilepath, String packagePath) {
        return HotInstall.getInstance().replaceCotaPath(configFilepath, packagePath);
    }

    public static ArrayList<String> getDataApkShouldNotUpdateByCota() {
        return HotInstall.getInstance().getDataApkShouldNotUpdateByCota();
    }

    public static void setPackageManagerInner(PackageManagerServiceEx pms, Context context) {
        HotInstall.getInstance().setPackageManagerInner(pms.getPackageManagerSerivce(), context);
    }

    public static void recordAutoInstallPkg(PackageParserEx.PackageEx pkg) {
        HotInstall.getInstance();
        HotInstall.recordAutoInstallPkg((PackageParser.Package) pkg.getPackage());
    }
}
