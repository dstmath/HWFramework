package com.android.server.pm;

import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.hardware.biometrics.fingerprint.V2_1.RequestStatus;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.FileUtils;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.util.ArraySet;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.FastPrintWriter;
import com.android.server.EventLogTags;
import com.android.server.pm.dex.DexManager;
import com.android.server.pm.dex.PackageDexUsage;
import dalvik.system.VMRuntime;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

public class PackageManagerServiceUtils {
    private static final long SEVEN_DAYS_IN_MILLISECONDS = 604800000;

    private static ArraySet<String> getPackageNamesForIntent(Intent intent, int userId) {
        List<ResolveInfo> ris = null;
        try {
            ris = AppGlobals.getPackageManager().queryIntentReceivers(intent, null, 0, userId).getList();
        } catch (RemoteException e) {
        }
        ArraySet<String> pkgNames = new ArraySet<>();
        if (ris != null) {
            for (ResolveInfo ri : ris) {
                pkgNames.add(ri.activityInfo.packageName);
            }
        }
        return pkgNames;
    }

    public static void sortPackagesByUsageDate(List<PackageParser.Package> pkgs, PackageManagerService packageManagerService) {
        if (packageManagerService.isHistoricalPackageUsageAvailable()) {
            Collections.sort(pkgs, $$Lambda$PackageManagerServiceUtils$ePZ6rsJ05hJ2glmOqcq1_jX6J8w.INSTANCE);
        }
    }

    private static void applyPackageFilter(Predicate<PackageParser.Package> filter, Collection<PackageParser.Package> result, Collection<PackageParser.Package> packages, List<PackageParser.Package> sortTemp, PackageManagerService packageManagerService) {
        for (PackageParser.Package pkg : packages) {
            if (filter.test(pkg)) {
                sortTemp.add(pkg);
            }
        }
        sortPackagesByUsageDate(sortTemp, packageManagerService);
        packages.removeAll(sortTemp);
        for (PackageParser.Package pkg2 : sortTemp) {
            result.add(pkg2);
            Collection<PackageParser.Package> deps = packageManagerService.findSharedNonSystemLibraries(pkg2);
            if (!deps.isEmpty()) {
                deps.removeAll(result);
                result.addAll(deps);
                packages.removeAll(deps);
            }
        }
        sortTemp.clear();
    }

    public static List<PackageParser.Package> getPackagesForDexopt(Collection<PackageParser.Package> packages, PackageManagerService packageManagerService) {
        Predicate<PackageParser.Package> remainingPredicate;
        Predicate<PackageParser.Package> remainingPredicate2;
        ArrayList<PackageParser.Package> remainingPkgs = new ArrayList<>(packages);
        LinkedList<PackageParser.Package> result = new LinkedList<>();
        ArrayList<PackageParser.Package> sortTemp = new ArrayList<>(remainingPkgs.size());
        applyPackageFilter($$Lambda$PackageManagerServiceUtils$QMVUHbRIK26QMZL5iM27MchX7U.INSTANCE, result, remainingPkgs, sortTemp, packageManagerService);
        packageManagerService.getHwPMSEx().filterShellApps(remainingPkgs, result);
        applyPackageFilter(new Predicate(getPackageNamesForIntent(new Intent("android.intent.action.PRE_BOOT_COMPLETED"), 0)) {
            private final /* synthetic */ ArraySet f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return this.f$0.contains(((PackageParser.Package) obj).packageName);
            }
        }, result, remainingPkgs, sortTemp, packageManagerService);
        applyPackageFilter(new Predicate() {
            public final boolean test(Object obj) {
                return DexManager.this.getPackageUseInfoOrDefault(((PackageParser.Package) obj).packageName).isAnyCodePathUsedByOtherApps();
            }
        }, result, remainingPkgs, sortTemp, packageManagerService);
        if (remainingPkgs.isEmpty() || !packageManagerService.isHistoricalPackageUsageAvailable()) {
            remainingPredicate = $$Lambda$PackageManagerServiceUtils$hVRkjdaFuAMTY9J9JQ7JyWMYCHA.INSTANCE;
        } else {
            long estimatedPreviousSystemUseTime = ((PackageParser.Package) Collections.max(remainingPkgs, $$Lambda$PackageManagerServiceUtils$whx96xO50U3fax1NRe1upTcx9jc.INSTANCE)).getLatestForegroundPackageUseTimeInMills();
            if (estimatedPreviousSystemUseTime != 0) {
                remainingPredicate2 = new Predicate(estimatedPreviousSystemUseTime - 604800000) {
                    private final /* synthetic */ long f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final boolean test(Object obj) {
                        return PackageManagerServiceUtils.lambda$getPackagesForDexopt$5(this.f$0, (PackageParser.Package) obj);
                    }
                };
            } else {
                remainingPredicate2 = $$Lambda$PackageManagerServiceUtils$Fz3elZ0VmMMv9wl_G3AN15dUU8.INSTANCE;
            }
            remainingPredicate = remainingPredicate2;
            sortPackagesByUsageDate(remainingPkgs, packageManagerService);
        }
        applyPackageFilter(remainingPredicate, result, remainingPkgs, sortTemp, packageManagerService);
        return result;
    }

    static /* synthetic */ boolean lambda$getPackagesForDexopt$5(long cutoffTime, PackageParser.Package pkg) {
        return pkg.getLatestForegroundPackageUseTimeInMills() >= cutoffTime;
    }

    static /* synthetic */ boolean lambda$getPackagesForDexopt$6(PackageParser.Package pkg) {
        return true;
    }

    static /* synthetic */ boolean lambda$getPackagesForDexopt$7(PackageParser.Package pkg) {
        return true;
    }

    public static boolean isUnusedSinceTimeInMillis(long firstInstallTime, long currentTimeInMillis, long thresholdTimeinMillis, PackageDexUsage.PackageUseInfo packageUseInfo, long latestPackageUseTimeInMillis, long latestForegroundPackageUseTimeInMillis) {
        boolean z = false;
        if (currentTimeInMillis - firstInstallTime < thresholdTimeinMillis) {
            return false;
        }
        if (currentTimeInMillis - latestForegroundPackageUseTimeInMillis < thresholdTimeinMillis) {
            return false;
        }
        if (!(currentTimeInMillis - latestPackageUseTimeInMillis < thresholdTimeinMillis && packageUseInfo.isAnyCodePathUsedByOtherApps())) {
            z = true;
        }
        return z;
    }

    public static String realpath(File path) throws IOException {
        try {
            return Os.realpath(path.getAbsolutePath());
        } catch (ErrnoException ee) {
            throw ee.rethrowAsIOException();
        }
    }

    public static String packagesToString(Collection<PackageParser.Package> c) {
        StringBuilder sb = new StringBuilder();
        for (PackageParser.Package pkg : c) {
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

    public static long getLastModifiedTime(PackageParser.Package pkg) {
        File srcFile = new File(pkg.codePath);
        if (!srcFile.isDirectory()) {
            return srcFile.lastModified();
        }
        long maxModifiedTime = new File(pkg.baseCodePath).lastModified();
        if (pkg.splitCodePaths != null) {
            for (int i = pkg.splitCodePaths.length - 1; i >= 0; i--) {
                maxModifiedTime = Math.max(maxModifiedTime, new File(pkg.splitCodePaths[i]).lastModified());
            }
        }
        return maxModifiedTime;
    }

    private static File getSettingsProblemFile() {
        return new File(new File(Environment.getDataDirectory(), "system"), "uiderrors.txt");
    }

    public static void dumpCriticalInfo(ProtoOutputStream proto) {
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(getSettingsProblemFile()));
            while (true) {
                String readLine = in.readLine();
                String line = readLine;
                if (readLine == null) {
                    $closeResource(null, in);
                    return;
                } else if (!line.contains("ignored: updated version")) {
                    proto.write(2237677961223L, line);
                }
            }
        } catch (IOException e) {
        } catch (Throwable th) {
            $closeResource(r1, in);
            throw th;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public static void dumpCriticalInfo(PrintWriter pw, String msg) {
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(getSettingsProblemFile()));
            while (true) {
                String readLine = in.readLine();
                String line = readLine;
                if (readLine == null) {
                    $closeResource(null, in);
                    return;
                } else if (!line.contains("ignored: updated version")) {
                    if (msg != null) {
                        pw.print(msg);
                    }
                    pw.println(line);
                }
            }
        } catch (IOException e) {
        } catch (Throwable th) {
            $closeResource(r1, in);
            throw th;
        }
    }

    public static void logCriticalInfo(int priority, String msg) {
        Slog.println(priority, "PackageManager", msg);
        EventLogTags.writePmCriticalInfo(msg);
        try {
            File fname = getSettingsProblemFile();
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(fname, true));
            String dateString = new SimpleDateFormat().format(new Date(System.currentTimeMillis()));
            pw.println(dateString + ": " + msg);
            pw.close();
            FileUtils.setPermissions(fname.toString(), 508, -1, -1);
        } catch (IOException e) {
        }
    }

    public static void enforceShellRestriction(String restriction, int callingUid, int userHandle) {
        if (callingUid != 2000) {
            return;
        }
        if (userHandle >= 0 && PackageManagerService.sUserManager.hasUserRestriction(restriction, userHandle)) {
            throw new SecurityException("Shell does not have permission to access user " + userHandle);
        } else if (userHandle < 0) {
            Slog.e("PackageManager", "Unable to check shell permission for user " + userHandle + "\n\t" + Debug.getCallers(3));
        }
    }

    public static String deriveAbiOverride(String abiOverride, PackageSetting settings) {
        if ("-".equals(abiOverride)) {
            return null;
        }
        if (abiOverride != null) {
            return abiOverride;
        }
        if (settings != null) {
            return settings.cpuAbiOverrideString;
        }
        return null;
    }

    public static int compareSignatures(Signature[] s1, Signature[] s2) {
        int i = 1;
        if (s1 == null) {
            if (s2 != null) {
                i = -1;
            }
            return i;
        } else if (s2 == null) {
            return -2;
        } else {
            if (s1.length != s2.length) {
                return -3;
            }
            int i2 = 0;
            if (s1.length == 1) {
                if (!s1[0].equals(s2[0])) {
                    i2 = -3;
                }
                return i2;
            }
            ArraySet<Signature> set1 = new ArraySet<>();
            for (Signature sig : s1) {
                set1.add(sig);
            }
            ArraySet<Signature> set2 = new ArraySet<>();
            for (Signature sig2 : s2) {
                set2.add(sig2);
            }
            if (set1.equals(set2)) {
                return 0;
            }
            return -3;
        }
    }

    private static boolean matchSignaturesCompat(String packageName, PackageSignatures packageSignatures, PackageParser.SigningDetails parsedSignatures) {
        ArraySet<Signature> existingSet = new ArraySet<>();
        for (Signature sig : packageSignatures.mSigningDetails.signatures) {
            existingSet.add(sig);
        }
        ArraySet<Signature> scannedCompatSet = new ArraySet<>();
        for (Signature sig2 : parsedSignatures.signatures) {
            try {
                for (Signature chainSig : sig2.getChainSignatures()) {
                    scannedCompatSet.add(chainSig);
                }
            } catch (CertificateEncodingException e) {
                scannedCompatSet.add(sig2);
            }
        }
        if (scannedCompatSet.equals(existingSet)) {
            packageSignatures.mSigningDetails = parsedSignatures;
            return true;
        }
        if (parsedSignatures.hasPastSigningCertificates()) {
            logCriticalInfo(4, "Existing package " + packageName + " has flattened signing certificate chain. Unable to install newer version with rotated signing certificate.");
        }
        return false;
    }

    private static boolean matchSignaturesRecover(String packageName, PackageParser.SigningDetails existingSignatures, PackageParser.SigningDetails parsedSignatures, @PackageParser.SigningDetails.CertCapabilities int flags) {
        String msg = null;
        try {
            if (parsedSignatures.checkCapabilityRecover(existingSignatures, flags)) {
                logCriticalInfo(4, "Recovered effectively matching certificates for " + packageName);
                return true;
            }
        } catch (CertificateException e) {
            msg = e.getMessage();
        }
        logCriticalInfo(4, "Failed to recover certificates for " + packageName + ": " + msg);
        return false;
    }

    private static boolean matchSignatureInSystem(PackageSetting pkgSetting, PackageSetting disabledPkgSetting) {
        try {
            PackageParser.collectCertificates(disabledPkgSetting.pkg, true);
            if (!pkgSetting.signatures.mSigningDetails.checkCapability(disabledPkgSetting.signatures.mSigningDetails, 1)) {
                if (!disabledPkgSetting.signatures.mSigningDetails.checkCapability(pkgSetting.signatures.mSigningDetails, 8)) {
                    logCriticalInfo(6, "Updated system app mismatches cert on /system: " + pkgSetting.name);
                    return false;
                }
            }
            return true;
        } catch (PackageParser.PackageParserException e) {
            logCriticalInfo(6, "Failed to collect cert for " + pkgSetting.name + ": " + e.getMessage());
            return false;
        }
    }

    static boolean isApkVerityEnabled() {
        return SystemProperties.getInt("ro.apk_verity.mode", 0) != 0;
    }

    static boolean isApkVerificationForced(PackageSetting disabledPs) {
        return disabledPs != null && disabledPs.isPrivileged() && isApkVerityEnabled();
    }

    public static boolean verifySignatures(PackageSetting pkgSetting, PackageSetting disabledPkgSetting, PackageParser.SigningDetails parsedSignatures, boolean compareCompat, boolean compareRecover) throws PackageManagerException {
        String packageName = pkgSetting.name;
        boolean compatMatch = false;
        boolean z = false;
        if (pkgSetting.signatures.mSigningDetails.signatures != null) {
            boolean match = parsedSignatures.checkCapability(pkgSetting.signatures.mSigningDetails, 1) || pkgSetting.signatures.mSigningDetails.checkCapability(parsedSignatures, 8);
            if (!match && compareCompat) {
                match = matchSignaturesCompat(packageName, pkgSetting.signatures, parsedSignatures);
                compatMatch = match;
            }
            if (!match && compareRecover) {
                match = matchSignaturesRecover(packageName, pkgSetting.signatures.mSigningDetails, parsedSignatures, 1) || matchSignaturesRecover(packageName, parsedSignatures, pkgSetting.signatures.mSigningDetails, 8);
            }
            if (!match && isApkVerificationForced(disabledPkgSetting)) {
                match = matchSignatureInSystem(pkgSetting, disabledPkgSetting);
            }
            if (!match) {
                throw new PackageManagerException(-7, "Package " + packageName + " signatures do not match previously installed version; ignoring!");
            }
        }
        if (!(pkgSetting.sharedUser == null || pkgSetting.sharedUser.signatures.mSigningDetails == PackageParser.SigningDetails.UNKNOWN)) {
            boolean match2 = parsedSignatures.checkCapability(pkgSetting.sharedUser.signatures.mSigningDetails, 2) || pkgSetting.sharedUser.signatures.mSigningDetails.checkCapability(parsedSignatures, 2);
            if (!match2 && compareCompat) {
                match2 = matchSignaturesCompat(packageName, pkgSetting.sharedUser.signatures, parsedSignatures);
            }
            if (!match2 && compareRecover) {
                if (matchSignaturesRecover(packageName, pkgSetting.sharedUser.signatures.mSigningDetails, parsedSignatures, 2) || matchSignaturesRecover(packageName, parsedSignatures, pkgSetting.sharedUser.signatures.mSigningDetails, 2)) {
                    z = true;
                }
                match2 = z;
                compatMatch |= match2;
            }
            if (!match2) {
                throw new PackageManagerException(-8, "Package " + packageName + " has no signatures that match those in shared user " + pkgSetting.sharedUser.name + "; ignoring!");
            }
        }
        return compatMatch;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0051, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0052, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0056, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0057, code lost:
        r5 = r4;
        r4 = r3;
        r3 = r5;
     */
    public static int decompressFile(File srcFile, File dstFile) throws ErrnoException {
        InputStream fileIn;
        Throwable th;
        Throwable th2;
        if (PackageManagerService.DEBUG_COMPRESSION) {
            Slog.i("PackageManager", "Decompress file; src: " + srcFile.getAbsolutePath() + ", dst: " + dstFile.getAbsolutePath());
        }
        try {
            fileIn = new GZIPInputStream(new FileInputStream(srcFile));
            OutputStream fileOut = new FileOutputStream(dstFile, false);
            FileUtils.copy(fileIn, fileOut);
            Os.chmod(dstFile.getAbsolutePath(), 420);
            $closeResource(null, fileOut);
            $closeResource(null, fileIn);
            return 1;
            $closeResource(th, fileOut);
            throw th2;
        } catch (IOException e) {
            logCriticalInfo(6, "Failed to decompress file; src: " + srcFile.getAbsolutePath() + ", dst: " + dstFile.getAbsolutePath());
            return RequestStatus.SYS_ETIMEDOUT;
        } catch (Throwable th3) {
            $closeResource(r1, fileIn);
            throw th3;
        }
    }

    public static File[] getCompressedFiles(String codePath) {
        File stubCodePath = new File(codePath);
        String stubName = stubCodePath.getName();
        int idx = stubName.lastIndexOf(PackageManagerService.STUB_SUFFIX);
        if (idx < 0 || stubName.length() != PackageManagerService.STUB_SUFFIX.length() + idx) {
            return null;
        }
        File stubParentDir = stubCodePath.getParentFile();
        if (stubParentDir == null) {
            Slog.e("PackageManager", "Unable to determine stub parent dir for codePath: " + codePath);
            return null;
        }
        File[] files = new File(stubParentDir, stubName.substring(0, idx)).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(PackageManagerService.COMPRESSED_EXTENSION);
            }
        });
        if (PackageManagerService.DEBUG_COMPRESSION && files != null && files.length > 0) {
            Slog.i("PackageManager", "getCompressedFiles[" + codePath + "]: " + Arrays.toString(files));
        }
        return files;
    }

    public static boolean compressedFileExists(String codePath) {
        File[] compressedFiles = getCompressedFiles(codePath);
        return compressedFiles != null && compressedFiles.length > 0;
    }
}
