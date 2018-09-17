package com.android.server.pm;

import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.PackageParser.Package;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.RemoteException;
import android.system.ErrnoException;
import android.util.ArraySet;
import com.android.server.pm.-$Lambda$LlDgbnHlShdoOCTPTWIe496B9MM.AnonymousClass5;
import com.android.server.pm.-$Lambda$LlDgbnHlShdoOCTPTWIe496B9MM.AnonymousClass6;
import com.android.server.pm.-$Lambda$LlDgbnHlShdoOCTPTWIe496B9MM.AnonymousClass7;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import libcore.io.Libcore;

public class PackageManagerServiceUtils {
    private static final long SEVEN_DAYS_IN_MILLISECONDS = 604800000;

    private static ArraySet<String> getPackageNamesForIntent(Intent intent, int userId) {
        Iterable ris = null;
        try {
            ris = AppGlobals.getPackageManager().queryIntentReceivers(intent, null, 0, userId).getList();
        } catch (RemoteException e) {
        }
        ArraySet<String> pkgNames = new ArraySet();
        if (ris != null) {
            for (ResolveInfo ri : ris) {
                pkgNames.add(ri.activityInfo.packageName);
            }
        }
        return pkgNames;
    }

    public static void sortPackagesByUsageDate(List<Package> pkgs, PackageManagerService packageManagerService) {
        if (packageManagerService.isHistoricalPackageUsageAvailable()) {
            Collections.sort(pkgs, new Comparator() {
                public final int compare(Object obj, Object obj2) {
                    return $m$0(obj, obj2);
                }
            });
        }
    }

    private static void applyPackageFilter(Predicate<Package> filter, Collection<Package> result, Collection<Package> packages, List<Package> sortTemp, PackageManagerService packageManagerService) {
        for (Package pkg : packages) {
            if (filter.test(pkg)) {
                sortTemp.add(pkg);
            }
        }
        sortPackagesByUsageDate(sortTemp, packageManagerService);
        packages.removeAll(sortTemp);
        for (Package pkg2 : sortTemp) {
            result.add(pkg2);
            Collection<Package> deps = packageManagerService.findSharedNonSystemLibraries(pkg2);
            if (!deps.isEmpty()) {
                deps.removeAll(result);
                result.addAll(deps);
                packages.removeAll(deps);
            }
        }
        sortTemp.clear();
    }

    public static List<Package> getPackagesForDexopt(Collection<Package> packages, PackageManagerService packageManagerService) {
        Predicate<Package> remainingPredicate;
        ArrayList<Package> remainingPkgs = new ArrayList(packages);
        LinkedList<Package> result = new LinkedList();
        ArrayList<Package> sortTemp = new ArrayList(remainingPkgs.size());
        applyPackageFilter(new Predicate() {
            public final boolean test(Object obj) {
                return $m$0(obj);
            }
        }, result, remainingPkgs, sortTemp, packageManagerService);
        packageManagerService.filterShellApps(remainingPkgs, result);
        applyPackageFilter(new AnonymousClass5(getPackageNamesForIntent(new Intent("android.intent.action.PRE_BOOT_COMPLETED"), 0)), result, remainingPkgs, sortTemp, packageManagerService);
        applyPackageFilter(new AnonymousClass6(packageManagerService), result, remainingPkgs, sortTemp, packageManagerService);
        if (remainingPkgs.isEmpty() || !packageManagerService.isHistoricalPackageUsageAvailable()) {
            remainingPredicate = new Predicate() {
                public final boolean test(Object obj) {
                    return $m$0(obj);
                }
            };
        } else {
            long estimatedPreviousSystemUseTime = ((Package) Collections.max(remainingPkgs, new -$Lambda$LlDgbnHlShdoOCTPTWIe496B9MM())).getLatestForegroundPackageUseTimeInMills();
            if (estimatedPreviousSystemUseTime != 0) {
                remainingPredicate = new AnonymousClass7(estimatedPreviousSystemUseTime - 604800000);
            } else {
                remainingPredicate = new Predicate() {
                    public final boolean test(Object obj) {
                        return $m$0(obj);
                    }
                };
            }
            sortPackagesByUsageDate(remainingPkgs, packageManagerService);
        }
        applyPackageFilter(remainingPredicate, result, remainingPkgs, sortTemp, packageManagerService);
        return result;
    }

    static /* synthetic */ boolean lambda$-com_android_server_pm_PackageManagerServiceUtils_7053(long cutoffTime, Package pkg) {
        return pkg.getLatestForegroundPackageUseTimeInMills() >= cutoffTime;
    }

    public static String realpath(File path) throws IOException {
        try {
            return Libcore.os.realpath(path.getAbsolutePath());
        } catch (ErrnoException ee) {
            throw ee.rethrowAsIOException();
        }
    }

    public static String packagesToString(Collection<Package> c) {
        StringBuilder sb = new StringBuilder();
        for (Package pkg : c) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(pkg.packageName);
        }
        return sb.toString();
    }

    public static boolean checkISA(String isa) {
        for (String abi : Build.SUPPORTED_ABIS) {
            if (VMRuntime.getInstructionSet(abi).equals(isa)) {
                return true;
            }
        }
        return false;
    }
}
