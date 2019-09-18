package com.android.server.pm;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import com.android.server.pm.PackageManagerService;
import com.huawei.cust.HwCustUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class HwPreAppManager {
    private static final String APK_INSTALLFILE = "xml/APKInstallListEMUI5Release.txt";
    private static final String DELAPK_INSTALLFILE = "xml/DelAPKInstallListEMUI5Release.txt";
    private static final int DEL_MULTI_INSTALL_MAP_SIZE = 3;
    private static final String FLAG_APK_NOSYS = "nosys";
    private static final String FLAG_APK_PRIV = "priv";
    private static final String FLAG_APK_SYS = "sys";
    private static final int MULTI_INSTALL_MAP_SIZE = 2;
    private static final int SCAN_AS_PRIVILEGED = 262144;
    private static final int SCAN_AS_SYSTEM = 131072;
    private static final String TAG = "HwPreAppManager";
    static Map<String, HashSet<String>> mDefaultSystemList = null;
    private static HashMap<String, HashSet<String>> mDelMultiInstallMap = null;
    private static volatile HwPreAppManager mInstance;
    private static HashMap<String, HashSet<String>> mMultiInstallMap = null;
    private static ArrayList<String> mRemoveablePreInstallApks = new ArrayList<>();
    private IHwPackageManagerServiceExInner mHwPmsExInner;

    private HwPreAppManager(IHwPackageManagerServiceExInner pmsEx) {
        this.mHwPmsExInner = pmsEx;
    }

    public static HwPreAppManager getInstance(IHwPackageManagerServiceExInner pmsEx) {
        if (mInstance == null) {
            synchronized (HwPreAppManager.class) {
                if (mInstance == null) {
                    mInstance = new HwPreAppManager(pmsEx);
                }
            }
        }
        return mInstance;
    }

    public boolean isDelappInData(PackageSetting ps) {
        if (ps == null || ps.codePath == null) {
            return false;
        }
        return isDelappInData(ps.codePath.toString());
    }

    private boolean isDelappInData(String scanFileString) {
        HashMap<String, HashSet<String>> cotaDelInstallMap = this.mHwPmsExInner.getIPmsInner().getHwPMSCotaDelInstallMap();
        if (!(scanFileString == null || mDelMultiInstallMap == null || mDelMultiInstallMap.isEmpty())) {
            for (Map.Entry<String, HashSet<String>> entry : mDelMultiInstallMap.entrySet()) {
                HashSet<String> hashSet = entry.getValue();
                if (hashSet != null && !hashSet.isEmpty() && hashSet.contains(scanFileString)) {
                    return true;
                }
            }
        }
        if (!(scanFileString == null || cotaDelInstallMap == null || cotaDelInstallMap.isEmpty())) {
            for (Map.Entry<String, HashSet<String>> entry2 : cotaDelInstallMap.entrySet()) {
                HashSet<String> hashSet2 = entry2.getValue();
                if (hashSet2 != null && !hashSet2.isEmpty() && hashSet2.contains(scanFileString)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addUpdatedRemoveableAppFlag(String scanFileString, String packageName) {
        if (this.mHwPmsExInner.containDelPath(scanFileString) || isDelappInData(scanFileString) || isPreRemovableApp(scanFileString)) {
            ArrayMap<String, PackageParser.Package> packagesLock = this.mHwPmsExInner.getIPmsInner().getPackagesLock();
            synchronized (packagesLock) {
                mRemoveablePreInstallApks.add(packageName);
                PackageParser.Package p = packagesLock.get(packageName);
                if (!(p == null || p.applicationInfo == null)) {
                    p.applicationInfo.hwFlags &= -33554433;
                    p.applicationInfo.hwFlags |= 67108864;
                    packagesLock.put(p.applicationInfo.packageName, p);
                }
            }
        }
    }

    public boolean needAddUpdatedRemoveableAppFlag(String packageName) {
        if (!mRemoveablePreInstallApks.contains(packageName)) {
            return false;
        }
        mRemoveablePreInstallApks.remove(packageName);
        return true;
    }

    public boolean isPrivAppNonSystemPartitionDir(File path) {
        if (!(path == null || mMultiInstallMap == null || mDelMultiInstallMap == null)) {
            HashSet<String> privAppHashSet = mMultiInstallMap.get(FLAG_APK_PRIV);
            if (privAppHashSet != null && !privAppHashSet.isEmpty() && privAppHashSet.contains(path.getPath())) {
                return true;
            }
            HashSet<String> privAppHashSet2 = mDelMultiInstallMap.get(FLAG_APK_PRIV);
            if (privAppHashSet2 != null && !privAppHashSet2.isEmpty() && privAppHashSet2.contains(path.getPath())) {
                return true;
            }
        }
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        HashMap<String, HashSet<String>> cotaInstallMap = pmsInner.getHwPMSCotaInstallMap();
        HashMap<String, HashSet<String>> cotaDelInstallMap = pmsInner.getHwPMSCotaDelInstallMap();
        if (!(path == null || cotaInstallMap == null || cotaDelInstallMap == null)) {
            HashSet<String> privAppHashSet3 = cotaInstallMap.get(FLAG_APK_PRIV);
            if (privAppHashSet3 != null && !privAppHashSet3.isEmpty() && privAppHashSet3.contains(path.getPath())) {
                return true;
            }
            HashSet<String> privAppHashSet4 = cotaDelInstallMap.get(FLAG_APK_PRIV);
            if (privAppHashSet4 != null && !privAppHashSet4.isEmpty() && privAppHashSet4.contains(path.getPath())) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    private void addNonSystemPartitionApkToHashMap() {
        if (mMultiInstallMap == null) {
            mMultiInstallMap = new HashMap<>();
            ArrayList<File> allAPKList = getApkInstallFileCfgList(APK_INSTALLFILE);
            if (HwPackageManagerService.APK_INSTALL_FOREVER && "sysdll".equals(SystemProperties.get("persist.sys.sysdll", ""))) {
                ArrayList<File> sysdllAPKList = getApkInstallFileCfgList(HwPackageManagerService.SYSDLL_PATH);
                if (!(sysdllAPKList == null || allAPKList == null)) {
                    int size = sysdllAPKList.size();
                    for (int i = 0; i < size; i++) {
                        allAPKList.add(sysdllAPKList.get(i));
                    }
                }
            }
            if (allAPKList != null) {
                int size2 = allAPKList.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    Flog.i(205, "get all apk cfg list -->" + i2 + " --" + allAPKList.get(i2).getPath());
                }
                HashSet<String> sysInstallSet = new HashSet<>();
                HashSet<String> privInstallSet = new HashSet<>();
                mMultiInstallMap.put(FLAG_APK_SYS, sysInstallSet);
                mMultiInstallMap.put(FLAG_APK_PRIV, privInstallSet);
                getMultiAPKInstallList(allAPKList, mMultiInstallMap);
            }
        }
        if (mDelMultiInstallMap == null) {
            mDelMultiInstallMap = new HashMap<>();
            ArrayList<File> allDelAPKList = getApkInstallFileCfgList(DELAPK_INSTALLFILE);
            if (allDelAPKList != null) {
                int size3 = allDelAPKList.size();
                for (int i3 = 0; i3 < size3; i3++) {
                    Flog.i(205, "get all del apk cfg list -->" + i3 + " --" + allDelAPKList.get(i3).getPath());
                }
                HashSet<String> sysInstallSet2 = new HashSet<>();
                HashSet<String> privInstallSet2 = new HashSet<>();
                HashSet<String> noSysInstallSet = new HashSet<>();
                mDelMultiInstallMap.put(FLAG_APK_SYS, sysInstallSet2);
                mDelMultiInstallMap.put(FLAG_APK_PRIV, privInstallSet2);
                mDelMultiInstallMap.put(FLAG_APK_NOSYS, noSysInstallSet);
                getMultiAPKInstallList(allDelAPKList, mDelMultiInstallMap);
                HwPackageManagerServiceUtils.setDelMultiInstallMap(mDelMultiInstallMap);
            }
        }
    }

    public void scanNonSystemPartitionDir(int scanMode) {
        if (!mMultiInstallMap.isEmpty()) {
            this.mHwPmsExInner.installAPKforInstallList(mMultiInstallMap.get(FLAG_APK_SYS), 16, scanMode | 131072, 0, 0);
            this.mHwPmsExInner.installAPKforInstallList(mMultiInstallMap.get(FLAG_APK_PRIV), 16, scanMode | 131072 | 262144, 0, 0);
        }
        if (!mDelMultiInstallMap.isEmpty()) {
            this.mHwPmsExInner.installAPKforInstallList(mDelMultiInstallMap.get(FLAG_APK_SYS), 16, scanMode | 131072, 0, 33554432);
            this.mHwPmsExInner.installAPKforInstallList(mDelMultiInstallMap.get(FLAG_APK_PRIV), 16, scanMode | 131072 | 262144, 0, 33554432);
            this.mHwPmsExInner.installAPKforInstallList(mDelMultiInstallMap.get(FLAG_APK_NOSYS), 0, scanMode, 0, 33554432);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:86:0x02be, code lost:
        throw new com.android.server.pm.PackageManagerException("Package " + r13 + " only restored to uninstalled apk");
     */
    public boolean checkUninstalledSystemApp(PackageParser.Package pkg, PackageManagerService.InstallArgs args, PackageManagerService.PackageInstalledInfo res) throws PackageManagerException {
        boolean z;
        PackageParser.Package packageR = pkg;
        PackageManagerService.InstallArgs installArgs = args;
        PackageManagerService.PackageInstalledInfo packageInstalledInfo = res;
        if (!"com.google.android.syncadapters.contacts".equals(packageR.packageName) && (packageR.mAppMetaData == null || !packageR.mAppMetaData.getBoolean("android.huawei.MARKETED_SYSTEM_APP", false))) {
            return false;
        }
        String packageName = packageR.packageName;
        String codePath = this.mHwPmsExInner.getUninstalledMap().get(packageName);
        if (codePath == null) {
            Slog.i(TAG, packageName + " not a uninstalled system app");
            return false;
        }
        int parseFlags = 0;
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        HashMap<String, HashSet<String>> cotaDelInstallMap = pmsInner.getHwPMSCotaDelInstallMap();
        if (mDelMultiInstallMap.get(FLAG_APK_SYS).contains(codePath) || mDefaultSystemList.get(FLAG_APK_SYS).contains(codePath) || (cotaDelInstallMap != null && cotaDelInstallMap.get(FLAG_APK_SYS).contains(codePath))) {
            parseFlags = 16;
        } else if (mDelMultiInstallMap.get(FLAG_APK_PRIV).contains(codePath) || mDefaultSystemList.get(FLAG_APK_PRIV).contains(codePath) || (cotaDelInstallMap != null && cotaDelInstallMap.get(FLAG_APK_PRIV).contains(codePath))) {
            parseFlags = 16;
        } else if (mDelMultiInstallMap.get(FLAG_APK_NOSYS).contains(codePath) || (cotaDelInstallMap != null && cotaDelInstallMap.get(FLAG_APK_NOSYS).contains(codePath))) {
            Slog.d(TAG, "checkUninstalledSystemApp");
        } else {
            Slog.i(TAG, "unkown the parse flag of " + codePath);
            return false;
        }
        int parseFlags2 = parseFlags;
        File scanFile = new File(codePath);
        Slog.i(TAG, "check uninstalled package:" + codePath + ",parseFlags=" + Integer.toHexString(parseFlags2));
        try {
            PackageParser.Package uninstalledPkg = new PackageParser().parsePackage(scanFile, parseFlags2, true, 0);
            try {
                PackageParser.collectCertificates(uninstalledPkg, true);
                if (uninstalledPkg == null) {
                    File file = scanFile;
                    int i = parseFlags2;
                    HashMap<String, HashSet<String>> hashMap = cotaDelInstallMap;
                    IHwPackageManagerInner iHwPackageManagerInner = pmsInner;
                    z = false;
                } else if (uninstalledPkg.mSigningDetails.signatures == null) {
                    PackageParser.Package packageR2 = uninstalledPkg;
                    File file2 = scanFile;
                    int i2 = parseFlags2;
                    HashMap<String, HashSet<String>> hashMap2 = cotaDelInstallMap;
                    IHwPackageManagerInner iHwPackageManagerInner2 = pmsInner;
                    z = false;
                } else if (PackageManagerServiceUtils.compareSignatures(packageR.mSigningDetails.signatures, uninstalledPkg.mSigningDetails.signatures) != 0) {
                    Slog.w(TAG, "Warnning:" + packageName + " has same package name with system app:" + codePath + ", but has different signatures!");
                    return false;
                } else {
                    packageR.applicationInfo.hwFlags |= 536870912;
                    if (packageR.mPersistentApp) {
                        Slog.i(TAG, packageName + " is a persistent system app!");
                        ApplicationInfo applicationInfo = packageR.applicationInfo;
                        applicationInfo.flags = applicationInfo.flags | 8;
                    }
                    if (this.mHwPmsExInner.scanInstallApk(packageName, codePath, installArgs.user.getIdentifier())) {
                        long uninstalledVersion = uninstalledPkg.getLongVersionCode();
                        File file3 = scanFile;
                        int i3 = parseFlags2;
                        long pkgVersion = pkg.getLongVersionCode();
                        Slog.i(TAG, "uninstalled package versioncode=" + uninstalledVersion + ", installing versionCode=" + pkgVersion);
                        boolean upgrade = uninstalledVersion < pkgVersion;
                        if (upgrade) {
                            return true;
                        }
                        ArrayMap<String, PackageParser.Package> packagesLock = pmsInner.getPackagesLock();
                        synchronized (packagesLock) {
                            try {
                                PackageParser.Package newPackage = packagesLock.get(packageName);
                                if (newPackage != null) {
                                    boolean z2 = upgrade;
                                    long uninstalledVersion2 = uninstalledVersion;
                                    try {
                                        packageInstalledInfo.origUsers = new int[]{installArgs.user.getIdentifier()};
                                        PackageSetting ps = (PackageSetting) pmsInner.getSettings().mPackages.get(packageName);
                                        if (ps != null) {
                                            packageInstalledInfo.newUsers = ps.queryInstalledUsers(PackageManagerService.sUserManager.getUserIds(), true);
                                            long j = pkgVersion;
                                            try {
                                                long j2 = uninstalledVersion2;
                                                PackageParser.Package packageR3 = uninstalledPkg;
                                                HashMap<String, HashSet<String>> hashMap3 = cotaDelInstallMap;
                                                IHwPackageManagerInner iHwPackageManagerInner3 = pmsInner;
                                                pmsInner.updateSettingsLIInner(newPackage, installArgs.installerPackageName, null, packageInstalledInfo, installArgs.user, installArgs.installReason);
                                            } catch (Throwable th) {
                                                th = th;
                                                throw th;
                                            }
                                        } else {
                                            HashMap<String, HashSet<String>> hashMap4 = cotaDelInstallMap;
                                            long j3 = uninstalledVersion2;
                                            PackageParser.Package packageR4 = uninstalledPkg;
                                            IHwPackageManagerInner iHwPackageManagerInner4 = pmsInner;
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        long j4 = pkgVersion;
                                        HashMap<String, HashSet<String>> hashMap5 = cotaDelInstallMap;
                                        long j5 = uninstalledVersion2;
                                        PackageParser.Package packageR5 = uninstalledPkg;
                                        IHwPackageManagerInner iHwPackageManagerInner5 = pmsInner;
                                        throw th;
                                    }
                                } else {
                                    long j6 = uninstalledVersion;
                                    PackageParser.Package packageR6 = uninstalledPkg;
                                    long j7 = pkgVersion;
                                    HashMap<String, HashSet<String>> hashMap6 = cotaDelInstallMap;
                                    IHwPackageManagerInner iHwPackageManagerInner6 = pmsInner;
                                    Slog.e(TAG, "can not found scanned package:" + packageName);
                                    packageInstalledInfo.setError(-25, "Update package's version code is older than uninstalled one");
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                boolean z3 = upgrade;
                                long j8 = uninstalledVersion;
                                PackageParser.Package packageR7 = uninstalledPkg;
                                long j9 = pkgVersion;
                                HashMap<String, HashSet<String>> hashMap7 = cotaDelInstallMap;
                                IHwPackageManagerInner iHwPackageManagerInner7 = pmsInner;
                                throw th;
                            }
                        }
                    } else {
                        File file4 = scanFile;
                        int i4 = parseFlags2;
                        HashMap<String, HashSet<String>> hashMap8 = cotaDelInstallMap;
                        IHwPackageManagerInner iHwPackageManagerInner8 = pmsInner;
                        Slog.w(TAG, "restore the uninstalled apk failed");
                        return false;
                    }
                }
                Slog.e(TAG, "parsed uninstalled package's signature failed!");
                return z;
            } catch (PackageParser.PackageParserException e) {
                e = e;
                File file5 = scanFile;
                int i5 = parseFlags2;
                HashMap<String, HashSet<String>> hashMap9 = cotaDelInstallMap;
                IHwPackageManagerInner iHwPackageManagerInner9 = pmsInner;
                PackageParser.Package packageR8 = uninstalledPkg;
            }
        } catch (PackageParser.PackageParserException e2) {
            e = e2;
            File file6 = scanFile;
            int i6 = parseFlags2;
            HashMap<String, HashSet<String>> hashMap10 = cotaDelInstallMap;
            IHwPackageManagerInner iHwPackageManagerInner10 = pmsInner;
            Slog.e(TAG, "collectCertificates throw " + e);
            return false;
        }
    }

    public void readPreInstallApkList() {
        mDefaultSystemList = new HashMap();
        if (mMultiInstallMap == null || mDelMultiInstallMap == null) {
            HashSet<String> sysInstallSet = new HashSet<>();
            HashSet<String> privInstallSet = new HashSet<>();
            mDefaultSystemList.put(FLAG_APK_SYS, sysInstallSet);
            mDefaultSystemList.put(FLAG_APK_PRIV, privInstallSet);
            addNonSystemPartitionApkToHashMap();
        }
    }

    public boolean isPreRemovableApp(String codePath) {
        String path;
        HashMap<String, HashSet<String>> cotaDelInstallMap = this.mHwPmsExInner.getIPmsInner().getHwPMSCotaDelInstallMap();
        if (codePath.endsWith(".apk")) {
            path = HwPackageManagerServiceUtils.getCustPackagePath(codePath);
        } else {
            path = codePath;
        }
        boolean z = false;
        if (path == null) {
            return false;
        }
        if (path.startsWith("/data/cust/delapp/") || path.startsWith("/system/delapp/")) {
            return true;
        }
        boolean res2 = mDelMultiInstallMap.get(FLAG_APK_PRIV).contains(path) || mDelMultiInstallMap.get(FLAG_APK_SYS).contains(path) || mDelMultiInstallMap.get(FLAG_APK_NOSYS).contains(path);
        if (cotaDelInstallMap != null) {
            res2 = res2 || cotaDelInstallMap.get(FLAG_APK_PRIV).contains(path) || cotaDelInstallMap.get(FLAG_APK_SYS).contains(path) || cotaDelInstallMap.get(FLAG_APK_NOSYS).contains(path);
        }
        if (mDefaultSystemList == null) {
            Flog.i(205, "isPreRemovableApp-> mDefaultSystemList is null;");
            return res2;
        }
        boolean res3 = mDefaultSystemList.get(FLAG_APK_PRIV).contains(path) || mDefaultSystemList.get(FLAG_APK_SYS).contains(path);
        if (res2 || res3) {
            z = true;
        }
        return z;
    }

    public boolean isPrivilegedPreApp(File scanFile) {
        String path;
        boolean z = false;
        if (scanFile == null) {
            return false;
        }
        String codePath = scanFile.getAbsolutePath();
        if (codePath.endsWith(".apk")) {
            path = getCustPackagePath(codePath);
        } else {
            path = codePath;
        }
        if (path == null) {
            return false;
        }
        if (path.startsWith("/system/priv-app/")) {
            return true;
        }
        boolean normalDelMultiApp = mDelMultiInstallMap != null && mDelMultiInstallMap.get(FLAG_APK_PRIV).contains(path);
        boolean normalMultiApp = mMultiInstallMap != null && mMultiInstallMap.get(FLAG_APK_PRIV).contains(path);
        if (normalDelMultiApp || normalMultiApp) {
            z = true;
        }
        return z;
    }

    public boolean isPrivilegedPreApp(String codePath) {
        String path;
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        HashMap<String, HashSet<String>> cotaDelInstallMap = pmsInner.getHwPMSCotaDelInstallMap();
        if (codePath.endsWith(".apk")) {
            path = HwPackageManagerServiceUtils.getCustPackagePath(codePath);
        } else {
            path = codePath;
        }
        boolean z = false;
        if (path == null) {
            return false;
        }
        if (path.startsWith("/system/priv-app/")) {
            return true;
        }
        boolean normalDelMultiApp = mDelMultiInstallMap != null && mDelMultiInstallMap.get(FLAG_APK_PRIV).contains(path);
        boolean normalMultiApp = mMultiInstallMap != null && mMultiInstallMap.get(FLAG_APK_PRIV).contains(path);
        boolean cotaDelMultiApp = cotaDelInstallMap != null && cotaDelInstallMap.get(FLAG_APK_PRIV).contains(path);
        boolean cotaMultiApp = pmsInner.getHwPMSCotaInstallMap() != null && ((HashSet) pmsInner.getHwPMSCotaInstallMap().get(FLAG_APK_PRIV)).contains(path);
        if (normalDelMultiApp || normalMultiApp || cotaDelMultiApp || cotaMultiApp) {
            z = true;
        }
        return z;
    }

    public void getMultiAPKInstallList(List<File> lists, HashMap<String, HashSet<String>> multiInstallMap) {
        if (multiInstallMap != null && lists.size() > 0) {
            for (File list : lists) {
                getAPKInstallList(list, multiInstallMap);
            }
        }
    }

    public void getAPKInstallList(File scanApk, HashMap<String, HashSet<String>> multiInstallMap) {
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(scanApk), "UTF-8"));
            IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
            while (true) {
                String readLine = reader2.readLine();
                String line = readLine;
                if (readLine != null) {
                    String[] strSplit = line.trim().split(",");
                    String packagePath = getCustPackagePath(strSplit[0]);
                    if (pmsInner.getCotaFlagInner()) {
                        Log.i(TAG, "read cota xml getAPKInstallList packagePath = " + packagePath);
                        ArrayList<String> ignoreList = ((HwPackageManagerServiceEx) this.mHwPmsExInner).getDataApkShouldNotUpdateByCota();
                        if (ignoreList != null && ignoreList.contains(packagePath)) {
                            Log.i(TAG, packagePath + " has installed in /data/app, cota ignore it.");
                        }
                    }
                    if (packagePath != null && packagePath.startsWith("/system/app/")) {
                        Flog.i(205, "pre removable system app, packagePath: " + packagePath);
                        mDefaultSystemList.get(FLAG_APK_SYS).add(packagePath.trim());
                    } else if (packagePath != null && packagePath.startsWith("/system/priv-app/")) {
                        Flog.i(205, "pre removable system priv app, packagePath: " + packagePath);
                        mDefaultSystemList.get(FLAG_APK_PRIV).add(packagePath.trim());
                    } else if (packagePath != null && HwPackageManagerUtils.isPackageFilename(strSplit[0].trim())) {
                        if (2 == strSplit.length && isCheckedKey(strSplit[1].trim(), multiInstallMap.size())) {
                            multiInstallMap.get(strSplit[1].trim()).add(packagePath.trim());
                        } else if (1 == strSplit.length) {
                            multiInstallMap.get(FLAG_APK_SYS).add(packagePath.trim());
                        } else {
                            Slog.e(TAG, "Config error for packagePath:" + packagePath);
                        }
                    }
                } else {
                    try {
                        reader2.close();
                        return;
                    } catch (Exception e) {
                        Log.e(TAG, "PackageManagerService.getAPKInstallList error for closing IO");
                        return;
                    }
                }
            }
        } catch (FileNotFoundException e2) {
            Log.w(TAG, "FileNotFound No such file or directory :" + scanApk.getPath());
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e3) {
            Log.e(TAG, "PackageManagerService.getAPKInstallList error for IO");
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e4) {
                    Log.e(TAG, "PackageManagerService.getAPKInstallList error for closing IO");
                }
            }
            throw th;
        }
    }

    private boolean isCheckedKey(String key, int mapSize) {
        boolean z = true;
        if (mapSize == 2) {
            if (!FLAG_APK_SYS.equals(key) && !FLAG_APK_PRIV.equals(key)) {
                z = false;
            }
            return z;
        } else if (mapSize != 3) {
            return false;
        } else {
            if (!FLAG_APK_SYS.equals(key) && !FLAG_APK_PRIV.equals(key) && !FLAG_APK_NOSYS.equals(key)) {
                z = false;
            }
            return z;
        }
    }

    private ArrayList<File> getApkInstallFileCfgList(String apkCfgFile) {
        String canonicalPath;
        String[] policyDir = null;
        ArrayList<File> allApkInstallList = new ArrayList<>();
        try {
            policyDir = HwCfgFilePolicy.getCfgPolicyDir(0);
            for (int i = 0; i < policyDir.length; i++) {
                Flog.i(205, "getApkInstallFileCfgList from custpolicy i=" + i + "| " + policyDir[i]);
            }
        } catch (NoClassDefFoundError e) {
            Slog.w(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (policyDir == null) {
            return null;
        }
        int i2 = policyDir.length - 1;
        while (i2 >= 0) {
            try {
                Flog.i(205, "getApkInstallFileCfgList canonicalPath:" + canonicalPath);
                File rawFileAddToList = adjustmccmncList(canonicalPath, apkCfgFile);
                if (rawFileAddToList != null) {
                    Flog.i(205, "getApkInstallFileCfgList add File :" + rawFileAddToList.getPath());
                    allApkInstallList.add(rawFileAddToList);
                }
                File rawNewFileAddToList = adjustmccmncList(new File("data/hw_init/" + canonicalPath).getCanonicalPath(), apkCfgFile);
                if (rawNewFileAddToList != null) {
                    Flog.i(205, "getApkInstallFileCfgList add data File :" + rawNewFileAddToList.getPath());
                    allApkInstallList.add(rawNewFileAddToList);
                }
            } catch (IOException e2) {
                Slog.e(TAG, "Unable to obtain canonical paths");
            }
            i2--;
        }
        if (allApkInstallList.size() == 0) {
            Log.w(TAG, "No config file found for:" + apkCfgFile);
        }
        return allApkInstallList;
    }

    private File adjustmccmncList(String canonicalPath, String apkFile) {
        HwCustPackageManagerService hwCustPackageManagerService = (HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[0]);
        File adjustRetFile = null;
        if (hwCustPackageManagerService != null) {
            try {
                if (hwCustPackageManagerService.isMccMncMatch()) {
                    File mccmncFile = new File(canonicalPath + "/" + joinCustomizeFile(apkFile));
                    if (mccmncFile.exists()) {
                        adjustRetFile = new File(mccmncFile.getCanonicalPath());
                        Flog.i(205, "adjustRetFile mccmnc :" + adjustRetFile.getPath());
                    } else {
                        if (new File(canonicalPath + "/" + apkFile).exists()) {
                            adjustRetFile = new File(canonicalPath + "/" + apkFile);
                            StringBuilder sb = new StringBuilder();
                            sb.append("adjustRetFile :");
                            sb.append(adjustRetFile.getPath());
                            Flog.i(205, sb.toString());
                        }
                    }
                    return adjustRetFile;
                }
            } catch (IOException e) {
                Slog.e(TAG, "Unable to obtain canonical paths");
                return null;
            }
        }
        if (!new File(canonicalPath + "/" + apkFile).exists()) {
            return null;
        }
        File adjustRetFile2 = new File(canonicalPath + "/" + apkFile);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("adjustRetFile :");
        sb2.append(adjustRetFile2.getPath());
        Flog.i(205, sb2.toString());
        return adjustRetFile2;
    }

    private String joinCustomizeFile(String fileName) {
        String joinFileName = fileName;
        String mccmnc = SystemProperties.get("persist.sys.mccmnc", "");
        if (fileName == null) {
            return joinFileName;
        }
        String[] splitArray = fileName.split("\\.");
        if (splitArray.length != 2) {
            return joinFileName;
        }
        return splitArray[0] + "_" + mccmnc + "." + splitArray[1];
    }

    public boolean isSystemPreApp(File scanFile) {
        String path;
        boolean z = false;
        if (scanFile == null) {
            return false;
        }
        String codePath = scanFile.getAbsolutePath();
        if (codePath.endsWith(".apk")) {
            path = getCustPackagePath(codePath);
        } else {
            path = codePath;
        }
        if (path == null) {
            return false;
        }
        boolean normalDelMultiApp = mDelMultiInstallMap != null && mDelMultiInstallMap.get(FLAG_APK_SYS).contains(path);
        boolean normalMultiApp = mMultiInstallMap != null && mMultiInstallMap.get(FLAG_APK_SYS).contains(path);
        if (normalDelMultiApp || normalMultiApp) {
            z = true;
        }
        return z;
    }

    private String getCustPackagePath(String readLine) {
        return HwPackageManagerServiceUtils.getCustPackagePath(readLine);
    }
}
