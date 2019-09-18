package com.android.server.mtm.iaware.appmng.appstart.comm;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.pm.ApplicationInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class AppStartupUtil {
    private static final Pattern CTS_PATTERN = Pattern.compile(".*android.*cts.*");
    private static final String TAG = "AppStartupUtil";
    private static Set<String> mCtsFilterPkgs = new HashSet();
    private static Set<String> mCtsSpecPkgs = new HashSet();
    private static AtomicBoolean mIsCtsInitialized = new AtomicBoolean(false);

    public static void initCtsPkgList() {
        if (!mIsCtsInitialized.get()) {
            mIsCtsInitialized.set(true);
            ArrayList<String> pkgList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "ctspkglist");
            if (pkgList != null) {
                Set<String> ctsSpecPkgs = new HashSet<>();
                ctsSpecPkgs.addAll(pkgList);
                mCtsSpecPkgs = ctsSpecPkgs;
            }
            ArrayList<String> pkgList2 = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "ctsfilterpkglist");
            if (pkgList2 != null) {
                Set<String> ctsSpecPkgs2 = new HashSet<>();
                ctsSpecPkgs2.addAll(pkgList2);
                mCtsFilterPkgs = ctsSpecPkgs2;
            }
        }
    }

    public static boolean isSystemUnRemovablePkg(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & 1) != 0 && (applicationInfo.hwFlags & 100663296) == 0;
    }

    public static boolean isAppStopped(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & HighBitsCompModeID.MODE_EYE_PROTECT) != 0;
    }

    public static boolean isCtsPackage(String pkgName) {
        if (pkgName == null || ((!CTS_PATTERN.matcher(pkgName).matches() || mCtsFilterPkgs.contains(pkgName)) && ((!pkgName.startsWith("com.google.android") || !pkgName.contains(".gts")) && !mCtsSpecPkgs.contains(pkgName)))) {
            return false;
        }
        return true;
    }

    public static String getDumpCtsPackages() {
        StringBuilder cacheStr = new StringBuilder();
        Set<String> ctsSpecPkgs = mCtsSpecPkgs;
        Set<String> ctsFilterPkgs = mCtsFilterPkgs;
        cacheStr.append("ctsSpecPkgs:\n");
        Iterator<String> it = ctsSpecPkgs.iterator();
        while (it.hasNext()) {
            cacheStr.append("  " + it.next() + "\n");
        }
        cacheStr.append("ctsFilterPkgs:\n");
        Iterator<String> it2 = ctsFilterPkgs.iterator();
        while (it2.hasNext()) {
            cacheStr.append("  " + it2.next() + "\n");
        }
        return cacheStr.toString();
    }
}
