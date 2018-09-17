package com.android.server.mtm.iaware.appmng.appstart.comm;

import android.content.pm.ApplicationInfo;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class AppStartupUtil {
    private static Set<String> CTS_FILTER_PKGS = new HashSet();
    private static final Pattern CTS_PATTERN = Pattern.compile(".*android.*cts.*");
    private static Set<String> CTS_SPECIAL_PKGS = new HashSet();
    private static final String TAG = "AppStartupUtil";

    static {
        CTS_SPECIAL_PKGS.add("android.tests.devicesetup");
        CTS_SPECIAL_PKGS.add("android.voicesettings");
        CTS_SPECIAL_PKGS.add("android.voiceinteraction");
        CTS_SPECIAL_PKGS.add("android.externalservice.service");
        CTS_SPECIAL_PKGS.add("com.android.app2");
        CTS_SPECIAL_PKGS.add("com.google.android.gts.managedprovisioning");
        CTS_SPECIAL_PKGS.add("com.afwsamples.testdpc");
        CTS_SPECIAL_PKGS.add("com.google.android.apps.enterprise.dmagent");
        CTS_SPECIAL_PKGS.add("com.example.android.basicmanagedprofile");
        CTS_FILTER_PKGS.add("com.android.contacts");
        CTS_FILTER_PKGS.add("com.google.android.syncadapters.contacts");
    }

    public static boolean isSystemUnRemovablePkg(ApplicationInfo applicationInfo) {
        if ((applicationInfo.flags & 1) == 0 || (applicationInfo.hwFlags & 100663296) != 0) {
            return false;
        }
        return true;
    }

    public static boolean isAppStopped(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & HighBitsCompModeID.MODE_EYE_PROTECT) != 0;
    }

    public static boolean isCtsPackage(String pkgName) {
        if ((!CTS_PATTERN.matcher(pkgName).matches() || (CTS_FILTER_PKGS.contains(pkgName) ^ 1) == 0) && !CTS_SPECIAL_PKGS.contains(pkgName)) {
            return false;
        }
        return true;
    }
}
