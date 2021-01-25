package com.android.server.pm;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwPreAppManager {
    private static final String APK_INSTALLFILE = "xml/APKInstallListEMUI5Release.txt";
    private static boolean DEBUG = PackageManagerService.DEBUG_INSTALL;
    private static final String DELAPK_INSTALLFILE = "xml/DelAPKInstallListEMUI5Release.txt";
    private static final int DEL_MULTI_INSTALL_MAP_SIZE = 3;
    private static final String FLAG_APK_END = ".apk";
    private static final String FLAG_APK_NOSYS = "nosys";
    private static final String FLAG_APK_PRIV = "priv";
    private static final String FLAG_APK_SYS = "sys";
    private static final String FLAG_STSTEM_PVI_APK = "/system/priv-app/";
    private static final boolean IS_AUTO_INSTALL_ENABLE = SystemProperties.getBoolean("hw_mc.pms.recovery_preset_app", false);
    private static final Object LOCK = new Object();
    private static final int MULTI_INSTALL_MAP_SIZE = 2;
    private static final int NO_PARSE_FLAG = -1;
    private static final int SCAN_AS_PRIVILEGED = 262144;
    private static final int SCAN_AS_SYSTEM = 131072;
    private static final String TAG = "HwPreAppManager";
    static Map<String, HashSet<String>> sDefaultSystemList = null;
    private static HashMap<String, HashSet<String>> sDelMultiInstallMap = null;
    private static volatile HwPreAppManager sInstance;
    private static HashMap<String, HashSet<String>> sMultiInstallMap = null;
    private static List<String> sRemoveablePreInstallApks = new ArrayList();
    private IHwPackageManagerServiceExInner mHwPmsExInner;

    private HwPreAppManager(IHwPackageManagerServiceExInner pmsEx) {
        this.mHwPmsExInner = pmsEx;
    }

    public static HwPreAppManager getInstance(IHwPackageManagerServiceExInner pmsEx) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new HwPreAppManager(pmsEx);
                }
            }
        }
        return sInstance;
    }

    public boolean isDelappInData(PackageSetting ps) {
        if (ps == null || ps.codePath == null) {
            return false;
        }
        return isDelappInData(ps.codePath.toString());
    }

    private boolean isDelappInData(String scanFileString) {
        HashMap<String, HashSet<String>> hashMap;
        HashMap<String, HashSet<String>> cotaDelInstallMap = this.mHwPmsExInner.getIPmsInner().getHwPMSCotaDelInstallMap();
        if (!(scanFileString == null || (hashMap = sDelMultiInstallMap) == null || hashMap.isEmpty())) {
            for (Map.Entry<String, HashSet<String>> entry : sDelMultiInstallMap.entrySet()) {
                HashSet<String> hashSet = entry.getValue();
                if (!(hashSet == null || hashSet.isEmpty() || !hashSet.contains(scanFileString))) {
                    return true;
                }
            }
        }
        if (scanFileString == null || cotaDelInstallMap == null || cotaDelInstallMap.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, HashSet<String>> entry2 : cotaDelInstallMap.entrySet()) {
            HashSet<String> hashSet2 = entry2.getValue();
            if (!(hashSet2 == null || hashSet2.isEmpty() || !hashSet2.contains(scanFileString))) {
                return true;
            }
        }
        return false;
    }

    public void addUpdatedRemoveableAppFlag(String scanFileString, String packageName) {
        if (scanFileString != null && packageName != null) {
            if (this.mHwPmsExInner.containDelPath(scanFileString) || isDelappInData(scanFileString) || isPreRemovableApp(scanFileString)) {
                ArrayMap<String, PackageParser.Package> packagesLock = this.mHwPmsExInner.getIPmsInner().getPackagesLock();
                synchronized (packagesLock) {
                    sRemoveablePreInstallApks.add(packageName);
                    PackageParser.Package packageInfo = packagesLock.get(packageName);
                    if (packageInfo != null) {
                        if (packageInfo.applicationInfo != null) {
                            packageInfo.applicationInfo.hwFlags &= -33554433;
                            packageInfo.applicationInfo.hwFlags |= 67108864;
                            packagesLock.put(packageInfo.applicationInfo.packageName, packageInfo);
                        }
                    }
                }
            }
        }
    }

    public boolean needAddUpdatedRemoveableAppFlag(String packageName) {
        if (packageName == null || !sRemoveablePreInstallApks.contains(packageName)) {
            return false;
        }
        sRemoveablePreInstallApks.remove(packageName);
        return true;
    }

    private boolean isContain(HashSet<String> appSet, String path) {
        if (appSet == null || appSet.isEmpty() || !appSet.contains(path)) {
            return false;
        }
        return true;
    }

    private boolean isNonSystemPartition(String path, HashMap<String, HashSet<String>> map) {
        return isContain(map.get(FLAG_APK_PRIV), path) || isContain(map.get(FLAG_APK_SYS), path) || isContain(map.get(FLAG_APK_NOSYS), path);
    }

    public boolean isAppNonSystemPartitionDir(String path) {
        if (path == null) {
            return false;
        }
        HashMap<String, HashSet<String>> hashMap = sMultiInstallMap;
        if (hashMap != null && sDelMultiInstallMap != null && (isNonSystemPartition(path, hashMap) || isNonSystemPartition(path, sDelMultiInstallMap))) {
            return true;
        }
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        HashMap<String, HashSet<String>> cotaInstallMap = pmsInner.getHwPMSCotaInstallMap();
        HashMap<String, HashSet<String>> cotaDelInstallMap = pmsInner.getHwPMSCotaDelInstallMap();
        if ((cotaInstallMap == null || cotaDelInstallMap == null || (!isNonSystemPartition(path, cotaInstallMap) && !isNonSystemPartition(path, cotaDelInstallMap))) && !HotInstall.getInstance().isNonSystemPartition(path)) {
            return false;
        }
        return true;
    }

    public boolean isPrivAppNonSystemPartitionDir(File path) {
        HashMap<String, HashSet<String>> hashMap;
        if (!(path == null || (hashMap = sMultiInstallMap) == null || sDelMultiInstallMap == null)) {
            HashSet<String> privAppHashSet = hashMap.get(FLAG_APK_PRIV);
            if (privAppHashSet != null && !privAppHashSet.isEmpty() && privAppHashSet.contains(path.getPath())) {
                return true;
            }
            HashSet<String> privAppHashSet2 = sDelMultiInstallMap.get(FLAG_APK_PRIV);
            if (privAppHashSet2 != null && !privAppHashSet2.isEmpty() && privAppHashSet2.contains(path.getPath())) {
                return true;
            }
        }
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        HashMap<String, HashSet<String>> cotaInstallMap = pmsInner.getHwPMSCotaInstallMap();
        HashMap<String, HashSet<String>> cotaDelInstallMap = pmsInner.getHwPMSCotaDelInstallMap();
        if (path == null || cotaInstallMap == null || cotaDelInstallMap == null) {
            return false;
        }
        HashSet<String> privAppHashSet3 = cotaInstallMap.get(FLAG_APK_PRIV);
        if (privAppHashSet3 != null && !privAppHashSet3.isEmpty() && privAppHashSet3.contains(path.getPath())) {
            return true;
        }
        HashSet<String> privAppHashSet4 = cotaDelInstallMap.get(FLAG_APK_PRIV);
        if (privAppHashSet4 == null || privAppHashSet4.isEmpty() || !privAppHashSet4.contains(path.getPath())) {
            return false;
        }
        return true;
    }

    private List<File> addApkConfigFile(List<File> apkConfigList, String configFilePath) {
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            return apkConfigList;
        }
        List<File> resultList = apkConfigList;
        if (resultList == null) {
            resultList = new ArrayList<>();
        }
        resultList.add(configFile);
        return resultList;
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    private void addNonSystemPartitionApkToHashMap() {
        if (sMultiInstallMap == null) {
            sMultiInstallMap = new HashMap<>();
            sMultiInstallMap.put(FLAG_APK_SYS, new HashSet<>());
            sMultiInstallMap.put(FLAG_APK_PRIV, new HashSet<>());
            List<File> allApkList = addApkConfigFile(getApkInstallFileCfgList(APK_INSTALLFILE), HotInstall.AUTO_INSTALL_APK_CONFIG);
            sysDllApkInstall(allApkList);
            if (allApkList != null && allApkList.size() > 0) {
                Iterator<File> it = allApkList.iterator();
                while (it.hasNext()) {
                    Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "get all apk cfg list --> " + it.next().getPath());
                }
                getMultiApkInstallList(allApkList, sMultiInstallMap);
            }
        }
        if (sDelMultiInstallMap == null) {
            sDelMultiInstallMap = new HashMap<>();
            sDelMultiInstallMap.put(FLAG_APK_SYS, new HashSet<>());
            sDelMultiInstallMap.put(FLAG_APK_PRIV, new HashSet<>());
            sDelMultiInstallMap.put(FLAG_APK_NOSYS, new HashSet<>());
            List<File> allDelApkList = addApkConfigFile(getApkInstallFileCfgList(DELAPK_INSTALLFILE), HotInstall.AUTO_INSTALL_DEL_APK_CONFIG);
            if (allDelApkList != null && allDelApkList.size() > 0) {
                Iterator<File> it2 = allDelApkList.iterator();
                while (it2.hasNext()) {
                    Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "get all del apk cfg list --> " + it2.next().getPath());
                }
                getMultiApkInstallList(allDelApkList, sDelMultiInstallMap);
                HwPackageManagerServiceUtils.setDelMultiInstallMap(sDelMultiInstallMap);
            }
        }
    }

    private void sysDllApkInstall(List<File> allApkList) {
        List<File> sysDllApkFiles;
        if (!(!HotInstall.IS_APK_INSTALL_FOREVER || allApkList == null || !"sysdll".equals(SystemProperties.get("persist.sys.sysdll", "")) || (sysDllApkFiles = getApkInstallFileCfgList(HotInstall.SYSDLL_PATH)) == null || sysDllApkFiles.size() == 0)) {
            for (File file : sysDllApkFiles) {
                allApkList.add(file);
            }
        }
    }

    public void scanNonSystemPartitionDir(int scanMode) {
        if (!sMultiInstallMap.isEmpty()) {
            this.mHwPmsExInner.installAPKforInstallList(sMultiInstallMap.get(FLAG_APK_SYS), 16, scanMode | 131072, 0, 0);
            this.mHwPmsExInner.installAPKforInstallList(sMultiInstallMap.get(FLAG_APK_PRIV), 16, scanMode | 131072 | 262144, 0, 0);
        }
        HashMap<String, HashSet<String>> hashMap = sDelMultiInstallMap;
        if (hashMap != null && !hashMap.isEmpty()) {
            this.mHwPmsExInner.installAPKforInstallList(sDelMultiInstallMap.get(FLAG_APK_SYS), 16, scanMode | 131072, 0, 33554432);
            this.mHwPmsExInner.installAPKforInstallList(sDelMultiInstallMap.get(FLAG_APK_PRIV), 16, scanMode | 131072 | 262144, 0, 33554432);
        }
    }

    public void scanNoSysAppInNonSystemPartitionDir(int scanMode) {
        HashMap<String, HashSet<String>> hashMap = sDelMultiInstallMap;
        if (hashMap != null && !hashMap.isEmpty()) {
            this.mHwPmsExInner.installAPKforInstallList(sDelMultiInstallMap.get(FLAG_APK_NOSYS), 0, scanMode, 0, 33554432);
        }
    }

    public boolean checkUninstalledSystemApp(PackageParser.Package pkg, PackageManagerService.InstallArgs args, PackageManagerService.PackageInstalledInfo res) throws PackageManagerException {
        boolean z;
        if (pkg == null || pkg.packageName == null || args == null) {
            z = false;
        } else if (res == null) {
            z = false;
        } else {
            String packageName = pkg.packageName;
            if (!"com.google.android.syncadapters.contacts".equals(pkg.packageName) && (pkg.mAppMetaData == null || !pkg.mAppMetaData.getBoolean("android.huawei.MARKETED_SYSTEM_APP", false))) {
                return false;
            }
            String codePath = getCodePath(packageName);
            if (TextUtils.isEmpty(codePath)) {
                Slog.i(TAG, packageName + " not a uninstalled system app");
                return false;
            }
            int parseFlags = getParseFlags(codePath);
            if (parseFlags == -1) {
                Slog.i(TAG, "unkown the parse flag of " + codePath);
                return false;
            }
            Slog.i(TAG, "check uninstalled package:" + codePath + ", parseFlags = " + Integer.toHexString(parseFlags));
            PackageParser.Package uninstalledPkg = getUninstalledPkg(codePath, parseFlags);
            if (!validateUninstalledPkg(uninstalledPkg, pkg, codePath)) {
                return false;
            }
            pkg.applicationInfo.hwFlags |= 536870912;
            if (pkg.mPersistentApp) {
                Slog.i(TAG, packageName + " is a persistent system app!");
                ApplicationInfo applicationInfo = pkg.applicationInfo;
                applicationInfo.flags = applicationInfo.flags | 8;
            }
            IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
            pmsInner.assertProvidersNotDefined(pkg);
            if (!this.mHwPmsExInner.scanInstallApk(packageName, codePath, args.user.getIdentifier())) {
                Slog.w(TAG, "restore the uninstalled apk failed");
                return false;
            }
            long uninstalledVersion = uninstalledPkg.getLongVersionCode();
            long pkgVersion = pkg.getLongVersionCode();
            Slog.i(TAG, "uninstalled package versioncode = " + uninstalledVersion + ", installing versionCode = " + pkgVersion);
            if (uninstalledVersion < pkgVersion) {
                return true;
            }
            throwExceptionOfUninstalledApk(pmsInner, packageName, res, args);
            return false;
        }
        Slog.i(TAG, "checkUninstalledSystemApp illegal call");
        return z;
    }

    private String getCodePath(String packageName) {
        String codePath = this.mHwPmsExInner.getUninstalledMap().get(packageName);
        if (codePath != null || !IS_AUTO_INSTALL_ENABLE) {
            return codePath;
        }
        return HwForbidUninstallManager.getInstance(this.mHwPmsExInner).getScanInstallApkCodePath(packageName);
    }

    private void throwExceptionOfUninstalledApk(IHwPackageManagerInner pmsInner, String packageName, PackageManagerService.PackageInstalledInfo res, PackageManagerService.InstallArgs args) throws PackageManagerException {
        ArrayMap<String, PackageParser.Package> packagesLock = pmsInner.getPackagesLock();
        synchronized (packagesLock) {
            PackageParser.Package newPackage = packagesLock.get(packageName);
            if (newPackage != null) {
                res.origUsers = new int[]{args.user.getIdentifier()};
                PackageSetting ps = (PackageSetting) pmsInner.getSettings().mPackages.get(packageName);
                if (ps != null) {
                    res.newUsers = ps.queryInstalledUsers(PackageManagerService.sUserManager.getUserIds(), true);
                    pmsInner.updateSettingsLIInner(newPackage, args.installerPackageName, (int[]) null, res, args.user, args.installReason);
                }
            } else {
                Slog.e(TAG, "can not found scanned package:" + packageName);
                res.setError(-25, "Update package's version code is older than uninstalled one");
            }
            throw new PackageManagerException("Package " + packageName + " only restored to uninstalled apk");
        }
    }

    private int getParseFlags(String codePath) {
        HashMap<String, HashSet<String>> cotaDelInstallMap = this.mHwPmsExInner.getIPmsInner().getHwPMSCotaDelInstallMap();
        if (sDefaultSystemList.get(FLAG_APK_SYS).contains(codePath) || sDefaultSystemList.get(FLAG_APK_PRIV).contains(codePath)) {
            return 16;
        }
        HashMap<String, HashSet<String>> hashMap = sDelMultiInstallMap;
        if (hashMap != null && (hashMap.get(FLAG_APK_SYS).contains(codePath) || sDelMultiInstallMap.get(FLAG_APK_PRIV).contains(codePath))) {
            return 16;
        }
        if (cotaDelInstallMap != null && (cotaDelInstallMap.get(FLAG_APK_SYS).contains(codePath) || cotaDelInstallMap.get(FLAG_APK_PRIV).contains(codePath))) {
            return 16;
        }
        HashMap<String, HashSet<String>> hashMap2 = sDelMultiInstallMap;
        if ((hashMap2 != null && hashMap2.get(FLAG_APK_NOSYS).contains(codePath)) || (cotaDelInstallMap != null && cotaDelInstallMap.get(FLAG_APK_NOSYS).contains(codePath))) {
            return 0;
        }
        if (codePath.startsWith("/system/delapp/")) {
            return 16;
        }
        return -1;
    }

    private PackageParser.Package getUninstalledPkg(String codePath, int parseFlags) {
        try {
            PackageParser.Package uninstalledPkg = new PackageParser().parsePackage(new File(codePath), parseFlags, true, 0);
            PackageParser.collectCertificates(uninstalledPkg, true);
            return uninstalledPkg;
        } catch (PackageParser.PackageParserException e) {
            Slog.e(TAG, "collectCertificates throw " + e);
            return null;
        }
    }

    private boolean validateUninstalledPkg(PackageParser.Package uninstalledPkg, PackageParser.Package originalPkg, String codePath) {
        if (uninstalledPkg == null || uninstalledPkg.mSigningDetails.signatures == null) {
            Slog.e(TAG, "parsed uninstalled package's signature failed!");
            return false;
        } else if (PackageManagerServiceUtils.compareSignatures(originalPkg.mSigningDetails.signatures, uninstalledPkg.mSigningDetails.signatures) == 0) {
            return true;
        } else {
            Slog.w(TAG, "Warning:" + originalPkg.packageName + " has same package name with system app:" + codePath + ", but has different signatures!");
            return false;
        }
    }

    public void readPreInstallApkList() {
        sDefaultSystemList = new HashMap();
        if (sMultiInstallMap == null || sDelMultiInstallMap == null) {
            sDefaultSystemList.put(FLAG_APK_SYS, new HashSet<>());
            sDefaultSystemList.put(FLAG_APK_PRIV, new HashSet<>());
            addNonSystemPartitionApkToHashMap();
        }
    }

    public boolean isPreRemovableApp(String codePath) {
        String path;
        if (codePath == null) {
            return false;
        }
        if (codePath.endsWith(FLAG_APK_END)) {
            path = HwPackageManagerServiceUtils.getCustPackagePath(codePath);
        } else {
            path = codePath;
        }
        if (path == null) {
            return false;
        }
        if (path.startsWith("/data/cust/delapp/") || path.startsWith("/system/delapp/")) {
            return true;
        }
        boolean isDelMultiInstall = false;
        HashMap<String, HashSet<String>> hashMap = sDelMultiInstallMap;
        if (hashMap != null) {
            isDelMultiInstall = (hashMap.get(FLAG_APK_PRIV) != null && sDelMultiInstallMap.get(FLAG_APK_PRIV).contains(path)) || (sDelMultiInstallMap.get(FLAG_APK_SYS) != null && sDelMultiInstallMap.get(FLAG_APK_SYS).contains(path)) || (sDelMultiInstallMap.get(FLAG_APK_NOSYS) != null && sDelMultiInstallMap.get(FLAG_APK_NOSYS).contains(path));
        }
        HashMap<String, HashSet<String>> cotaDelInstallMap = this.mHwPmsExInner.getIPmsInner().getHwPMSCotaDelInstallMap();
        if (cotaDelInstallMap != null) {
            isDelMultiInstall = isDelMultiInstall || (cotaDelInstallMap.get(FLAG_APK_PRIV) != null && cotaDelInstallMap.get(FLAG_APK_PRIV).contains(path)) || (cotaDelInstallMap.get(FLAG_APK_SYS) != null && cotaDelInstallMap.get(FLAG_APK_SYS).contains(path)) || (cotaDelInstallMap.get(FLAG_APK_NOSYS) != null && cotaDelInstallMap.get(FLAG_APK_NOSYS).contains(path));
        }
        Map<String, HashSet<String>> map = sDefaultSystemList;
        if (map == null) {
            Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "isPreRemovableApp-> sDefaultSystemList is null;");
            return isDelMultiInstall;
        }
        boolean isDefaultSystem = (map.get(FLAG_APK_PRIV) != null && sDefaultSystemList.get(FLAG_APK_PRIV).contains(path)) || (sDefaultSystemList.get(FLAG_APK_SYS) != null && sDefaultSystemList.get(FLAG_APK_SYS).contains(path));
        if (isDelMultiInstall || isDefaultSystem) {
            return true;
        }
        return false;
    }

    public boolean isPrivilegedPreApp(File scanFile) {
        String path;
        if (scanFile == null) {
            return false;
        }
        String codePath = scanFile.getAbsolutePath();
        if (codePath.endsWith(FLAG_APK_END)) {
            path = getCustPackagePath(codePath);
        } else {
            path = codePath;
        }
        if (path == null) {
            return false;
        }
        if (path.startsWith(FLAG_STSTEM_PVI_APK)) {
            return true;
        }
        HashMap<String, HashSet<String>> hashMap = sDelMultiInstallMap;
        boolean isNormalDelMultiApp = hashMap != null && hashMap.get(FLAG_APK_PRIV).contains(path);
        HashMap<String, HashSet<String>> hashMap2 = sMultiInstallMap;
        boolean isNormalMultiApp = hashMap2 != null && hashMap2.get(FLAG_APK_PRIV).contains(path);
        if (isNormalDelMultiApp || isNormalMultiApp) {
            return true;
        }
        return false;
    }

    public boolean isPrivilegedPreApp(String codePath) {
        String path;
        if (codePath == null) {
            return false;
        }
        if (codePath.endsWith(FLAG_APK_END)) {
            path = HwPackageManagerServiceUtils.getCustPackagePath(codePath);
        } else {
            path = codePath;
        }
        if (path == null) {
            return false;
        }
        if (path.startsWith(FLAG_STSTEM_PVI_APK)) {
            return true;
        }
        HashMap<String, HashSet<String>> hashMap = sDelMultiInstallMap;
        boolean isNormalDelMultiApp = hashMap != null && hashMap.get(FLAG_APK_PRIV).contains(path);
        HashMap<String, HashSet<String>> hashMap2 = sMultiInstallMap;
        boolean isNormalMultiApp = hashMap2 != null && hashMap2.get(FLAG_APK_PRIV).contains(path);
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        HashMap<String, HashSet<String>> cotaDelInstallMap = pmsInner.getHwPMSCotaDelInstallMap();
        boolean isCotaDelMultiApp = cotaDelInstallMap != null && cotaDelInstallMap.get(FLAG_APK_PRIV).contains(path);
        boolean isCotaMultiApp = pmsInner.getHwPMSCotaInstallMap() != null && ((HashSet) pmsInner.getHwPMSCotaInstallMap().get(FLAG_APK_PRIV)).contains(path);
        if (isNormalDelMultiApp || isNormalMultiApp || isCotaDelMultiApp || isCotaMultiApp) {
            return true;
        }
        return false;
    }

    public void getMultiApkInstallList(List<File> apkFileList, HashMap<String, HashSet<String>> multiInstallMap) {
        if (!(multiInstallMap == null || apkFileList == null || apkFileList.size() <= 0)) {
            for (File apkFile : apkFileList) {
                getApkInstallList(apkFile, multiInstallMap);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0170, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0175, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0176, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0179, code lost:
        throw r4;
     */
    public void getApkInstallList(File scanApk, HashMap<String, HashSet<String>> multiInstallMap) {
        if (scanApk != null && multiInstallMap != null && scanApk.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scanApk), "UTF-8"));
                IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
                while (true) {
                    String line = reader.readLine();
                    if (line != null) {
                        String[] strSplits = line.trim().split(",");
                        if (strSplits.length != 0) {
                            String packagePath = getCustPackagePath(strSplits[0]);
                            if (DEBUG) {
                                Log.i(TAG, "read cota xml originpackagePath = " + packagePath);
                            }
                            String packagePath2 = HotInstall.getInstance().replaceCotaPath(scanApk.getCanonicalPath(), packagePath);
                            if (pmsInner.getCotaFlagInner()) {
                                Log.i(TAG, "read cota xml getApkInstallList. packagePath = " + packagePath2);
                                ArrayList<String> ignorePackages = HotInstall.getInstance().getDataApkShouldNotUpdateByCota();
                                if (ignorePackages != null && ignorePackages.contains(packagePath2)) {
                                    Log.i(TAG, packagePath2 + " has installed in /data/app, cota ignore it.");
                                }
                            }
                            if (packagePath2 != null) {
                                if (packagePath2.startsWith("/system/app/")) {
                                    Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "pre removable system app, packagePath: " + packagePath2);
                                    sDefaultSystemList.get(FLAG_APK_SYS).add(packagePath2.trim());
                                } else if (packagePath2.startsWith(FLAG_STSTEM_PVI_APK)) {
                                    Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "pre removable system priv app, packagePath: " + packagePath2);
                                    sDefaultSystemList.get(FLAG_APK_PRIV).add(packagePath2.trim());
                                } else if (HwPackageManagerUtils.isPackageFilename(strSplits[0].trim())) {
                                    if (strSplits.length == 2 && isCheckedKey(strSplits[1].trim(), multiInstallMap.size())) {
                                        multiInstallMap.get(strSplits[1].trim()).add(packagePath2.trim());
                                    } else if (strSplits.length == 1) {
                                        multiInstallMap.get(FLAG_APK_SYS).add(packagePath2.trim());
                                    } else {
                                        Slog.e(TAG, "Config error for packagePath:" + packagePath2);
                                    }
                                }
                            }
                        }
                    } else {
                        reader.close();
                        return;
                    }
                }
            } catch (FileNotFoundException e) {
                Log.w(TAG, "load scanApk error.");
            } catch (IOException e2) {
                Log.e(TAG, "PackageManagerService.getApkInstallList error for IO.");
            }
        }
    }

    private boolean isCheckedKey(String key, int mapSize) {
        if (mapSize == 2) {
            return FLAG_APK_SYS.equals(key) || FLAG_APK_PRIV.equals(key);
        }
        if (mapSize == 3) {
            return FLAG_APK_SYS.equals(key) || FLAG_APK_PRIV.equals(key) || FLAG_APK_NOSYS.equals(key);
        }
        return false;
    }

    private List<File> getApkInstallFileCfgList(String apkCfgFile) {
        String[] policyDirs = null;
        List<File> allApkInstalls = new ArrayList<>();
        try {
            policyDirs = HwCfgFilePolicy.getCfgPolicyDir(0);
        } catch (NoClassDefFoundError e) {
            Slog.w(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (policyDirs == null) {
            return new ArrayList(0);
        }
        for (int i = policyDirs.length - 1; i >= 0; i--) {
            try {
                String subPolicyDir = policyDirs[i];
                Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "getApkInstallFileCfgList from custpolicy " + subPolicyDir);
                String canonicalPath = new File(subPolicyDir).getCanonicalPath();
                Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "getApkInstallFileCfgList canonicalPath:" + canonicalPath);
                File rawFileAddToList = adjustmccmncList(canonicalPath, apkCfgFile);
                if (rawFileAddToList != null) {
                    Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "getApkInstallFileCfgList add File :" + rawFileAddToList.getPath());
                    allApkInstalls.add(rawFileAddToList);
                }
                File rawNewFileAddToList = adjustmccmncList(new File("data/hw_init/" + canonicalPath).getCanonicalPath(), apkCfgFile);
                if (rawNewFileAddToList != null) {
                    Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "getApkInstallFileCfgList add data File :" + rawNewFileAddToList.getPath());
                    allApkInstalls.add(rawNewFileAddToList);
                }
            } catch (IOException e2) {
                Slog.e(TAG, "Unable to obtain canonical paths");
            }
        }
        if (allApkInstalls.size() == 0) {
            Log.w(TAG, "No config file found for:" + apkCfgFile);
        }
        return allApkInstalls;
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
                        Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "adjustRetFile mccmnc :" + adjustRetFile.getPath());
                    } else {
                        if (new File(canonicalPath + "/" + apkFile).exists()) {
                            adjustRetFile = new File(canonicalPath + "/" + apkFile);
                            StringBuilder sb = new StringBuilder();
                            sb.append("adjustRetFile :");
                            sb.append(adjustRetFile.getPath());
                            Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, sb.toString());
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
        Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, sb2.toString());
        return adjustRetFile2;
    }

    private String joinCustomizeFile(String fileName) {
        String mccmnc = SystemProperties.get("persist.sys.mccmnc", "");
        if (fileName == null) {
            return fileName;
        }
        String[] splitArray = fileName.split("\\.");
        if (splitArray.length != 2) {
            return fileName;
        }
        return splitArray[0] + "_" + mccmnc + "." + splitArray[1];
    }

    public boolean isSystemPreApp(File scanFile) {
        String path;
        if (scanFile == null) {
            return false;
        }
        String codePath = scanFile.getAbsolutePath();
        if (codePath.endsWith(FLAG_APK_END)) {
            path = getCustPackagePath(codePath);
        } else {
            path = codePath;
        }
        if (path == null) {
            return false;
        }
        HashMap<String, HashSet<String>> hashMap = sDelMultiInstallMap;
        boolean isNormalDelMultiApp = hashMap != null && hashMap.get(FLAG_APK_SYS).contains(path);
        HashMap<String, HashSet<String>> hashMap2 = sMultiInstallMap;
        boolean isNormalMultiApp = hashMap2 != null && hashMap2.get(FLAG_APK_SYS).contains(path);
        if (isNormalDelMultiApp || isNormalMultiApp) {
            return true;
        }
        return false;
    }

    private String getCustPackagePath(String readLine) {
        return HwPackageManagerServiceUtils.getCustPackagePath(readLine);
    }
}
