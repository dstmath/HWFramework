package com.android.server.pm;

import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.PackageParser.Package;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.system.ErrnoException;
import android.util.ArraySet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import libcore.io.Libcore;

public class PackageManagerServiceUtils {
    private static final long SEVEN_DAYS_IN_MILLISECONDS = 604800000;

    final /* synthetic */ class -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl0 implements Comparator {
        public int compare(Object arg0, Object arg1) {
            return Long.compare(((Package) arg0).getLatestForegroundPackageUseTimeInMills(), ((Package) arg1).getLatestForegroundPackageUseTimeInMills());
        }
    }

    private static ArraySet<String> getPackageNamesForIntent(Intent intent, int userId) {
        Iterable ris = null;
        try {
            ris = AppGlobals.getPackageManager().queryIntentReceivers(intent, null, 0, userId).getList();
        } catch (RemoteException e) {
        }
        ArraySet<String> pkgNames = new ArraySet();
        if (r4 != null) {
            for (ResolveInfo ri : r4) {
                pkgNames.add(ri.activityInfo.packageName);
            }
        }
        return pkgNames;
    }

    private static void filterRecentlyUsedApps(Collection<Package> pkgs, long estimatedPreviousSystemUseTime, long dexOptLRUThresholdInMills) {
        int total = pkgs.size();
        int skipped = 0;
        Iterator<Package> i = pkgs.iterator();
        while (i.hasNext()) {
            if (((Package) i.next()).getLatestForegroundPackageUseTimeInMills() < estimatedPreviousSystemUseTime - dexOptLRUThresholdInMills) {
                i.remove();
                skipped++;
            }
        }
    }

    public static List<Package> getPackagesForDexopt(Collection<Package> packages, PackageManagerService packageManagerService) {
        ArrayList<Package> remainingPkgs = new ArrayList(packages);
        LinkedList<Package> result = new LinkedList();
        for (Package pkg : remainingPkgs) {
            if (pkg.coreApp) {
                result.add(pkg);
            }
        }
        remainingPkgs.removeAll(result);
        packageManagerService.filterShellApps(remainingPkgs, result);
        ArraySet<String> pkgNames = getPackageNamesForIntent(new Intent("android.intent.action.PRE_BOOT_COMPLETED"), 0);
        for (Package pkg2 : remainingPkgs) {
            if (pkgNames.contains(pkg2.packageName)) {
                result.add(pkg2);
            }
        }
        remainingPkgs.removeAll(result);
        for (Package pkg22 : remainingPkgs) {
            if (PackageDexOptimizer.isUsedByOtherApps(pkg22)) {
                result.add(pkg22);
            }
        }
        remainingPkgs.removeAll(result);
        if (!remainingPkgs.isEmpty() && packageManagerService.isHistoricalPackageUsageAvailable()) {
            long estimatedPreviousSystemUseTime = ((Package) Collections.max(remainingPkgs, new -java_util_List_getPackagesForDexopt_java_util_Collection_packages_com_android_server_pm_PackageManagerService_packageManagerService_LambdaImpl0())).getLatestForegroundPackageUseTimeInMills();
            if (estimatedPreviousSystemUseTime != 0) {
                filterRecentlyUsedApps(remainingPkgs, estimatedPreviousSystemUseTime, SEVEN_DAYS_IN_MILLISECONDS);
            }
        }
        result.addAll(remainingPkgs);
        Set<Package> dependencies = new HashSet();
        for (Package p : result) {
            dependencies.addAll(packageManagerService.findSharedNonSystemLibraries(p));
        }
        if (!dependencies.isEmpty()) {
            dependencies.removeAll(result);
        }
        result.addAll(dependencies);
        return result;
    }

    public static String realpath(File path) throws IOException {
        try {
            return Libcore.os.realpath(path.getAbsolutePath());
        } catch (ErrnoException ee) {
            throw ee.rethrowAsIOException();
        }
    }
}
