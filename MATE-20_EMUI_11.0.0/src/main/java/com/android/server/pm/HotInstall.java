package com.android.server.pm;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.HwServiceExFactory;
import com.android.server.SystemConfig;
import com.android.server.cota.CotaInstallImpl;
import com.android.server.cota.CotaService;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HotInstall {
    public static final String AUTO_INSTALL_APK_CONFIG = "/data/system/auto_install/APKInstallListEMUI5Release.txt";
    public static final String AUTO_INSTALL_DEL_APK_CONFIG = "/data/system/auto_install/DelAPKInstallListEMUI5Release.txt";
    private static final String AUTO_INSTALL_PATH = "/data/system/auto_install";
    private static final String CACHE_BASE_DIR = "/data/system/package_cache/";
    private static final String COTA_APK_XML_PATH = "xml/APKInstallListEMUI5Release.txt";
    private static final int COTA_APP_INSTALLING = -1;
    private static final int COTA_APP_INSTALL_FIAL = 0;
    private static final int COTA_APP_INSTALL_ILLEGAL = -3;
    private static final int COTA_APP_INSTALL_INIT = -2;
    private static final int COTA_APP_INSTALL_SUCCESS = 1;
    private static final String COTA_APP_UPDATE_APPWIDGET = "huawei.intent.action.UPDATE_COTA_APP_WIDGET";
    private static final String COTA_APP_UPDATE_APPWIDGET_EXTRA = "huawei.intent.extra.cota_package_list";
    private static final String COTA_DEL_APK_XML_PATH = "xml/DelAPKInstallListEMUI5Release.txt";
    private static final String COTA_PMS = "cota_pms";
    private static final String COTA_UPDATE_FLAG_FAIL = "_2";
    private static final String COTA_UPDATE_FLAG_INIT = "_0";
    private static final String COTA_UPDATE_FLAG_SUCCESS = "_1";
    private static final String CUST_COTALITE_PATH = "/cust/cotalite";
    private static final String CUST_COTA_PATH = "/cust/cota";
    private static boolean DEBUG = PackageManagerService.DEBUG_INSTALL;
    private static final String FLAG_APK_NOSYS = "nosys";
    private static final String FLAG_APK_PRIV = "priv";
    private static final String FLAG_APK_SYS = "sys";
    public static final boolean IS_APK_INSTALL_FOREVER = SystemProperties.getBoolean("ro.config.apkinstallforever", false);
    public static final boolean IS_SUPPORT_HW_COTA = SystemProperties.getBoolean("ro.config.hw_cota", false);
    private static final int SCAN_AS_PRIVILEGED = 262144;
    private static final int SCAN_AS_SYSTEM = 131072;
    private static final int SCAN_BOOTING = 16;
    private static final int SCAN_FIRST_BOOT_OR_UPGRADE = 8192;
    private static final int SCAN_INITIAL = 512;
    public static final String SYSDLL_PATH = "xml/APKInstallListEMUI5Release_732999.txt";
    private static final String TAG = "HotInstall";
    private static final String UTF_8 = "utf-8";
    private static final String VERSION_PATH = "/version";
    private static List<PackageParser.Package> sAutoInstallPkgList = new ArrayList();
    private static HashMap<String, HashSet<String>> sCotaDelInstallMap = null;
    private static HashMap<String, HashSet<String>> sCotaInstallMap = null;
    private static HotInstall sHotInstaller = new HotInstall();
    private static boolean sIsAutoInstall = false;
    private Context mContext;
    private int mCotaApksInstallStatus = -2;
    private CotaInstallImpl.CotaInstallCallBack mCotaInstallCallBack = new CotaInstallImpl.CotaInstallCallBack() {
        /* class com.android.server.pm.HotInstall.AnonymousClass1 */

        @Override // com.android.server.cota.CotaInstallImpl.CotaInstallCallBack
        public void startAutoInstall(String apkInstallConfig, String removableApkInstallConfig, String strMccMnc) {
            HotInstall hotInstall = HotInstall.this;
            hotInstall.realStartAutoInstall(hotInstall.getHwPMSEx(hotInstall.mPms, HotInstall.this.mContext), apkInstallConfig, removableApkInstallConfig, strMccMnc);
        }

        @Override // com.android.server.cota.CotaInstallImpl.CotaInstallCallBack
        public void startInstall() {
            if (HotInstall.IS_SUPPORT_HW_COTA || HotInstall.IS_APK_INSTALL_FOREVER) {
                Log.i(HotInstall.TAG, "realStartCotaInstall()");
                HotInstall hotInstall = HotInstall.this;
                hotInstall.realStartCotaInstall(hotInstall.getHwPMSEx(hotInstall.mPms, HotInstall.this.mContext));
                return;
            }
            Log.i(HotInstall.TAG, "donot support hw_cota or sysdll!");
        }

        @Override // com.android.server.cota.CotaInstallImpl.CotaInstallCallBack
        public int getStatus() {
            if (!HotInstall.IS_SUPPORT_HW_COTA && !HotInstall.IS_APK_INSTALL_FOREVER) {
                return HotInstall.this.getCotaStatus();
            }
            Log.i(HotInstall.TAG, "getStatus()");
            return HotInstall.this.getCotaStatus();
        }
    };
    private String mCotaUpdateFlag = "";
    private HashSet<String> mCurrenPaths = new HashSet<>();
    private PackageManagerService mPms;
    private ArrayList<String> mShouldNotUpdateByCotaDataApks = new ArrayList<>();

    private HotInstall() {
    }

    public static HotInstall getInstance() {
        return sHotInstaller;
    }

    public static void recordAutoInstallPkg(PackageParser.Package pkg) {
        if (sIsAutoInstall) {
            Log.i(TAG, "pkg installed " + pkg.packageName);
            sAutoInstallPkgList.add(pkg);
        }
    }

    public static ArrayList<ArrayList<File>> getCotaApkInstallXmlPath() {
        ArrayList<File> apkInstallList = getCotaXmlFile(COTA_APK_XML_PATH);
        ArrayList<File> apkDelInstallList = getCotaXmlFile(COTA_DEL_APK_XML_PATH);
        ArrayList<ArrayList<File>> result = new ArrayList<>();
        result.add(apkInstallList);
        result.add(apkDelInstallList);
        return result;
    }

    public static ArrayList<File> getSysdllInstallXmlPath() {
        ArrayList<File> files = new ArrayList<>();
        File sysdllFile = null;
        try {
            sysdllFile = HwCfgFilePolicy.getCfgFile(SYSDLL_PATH, 0);
            if (DEBUG) {
                Log.i(TAG, "getSysdllInstallXmlPath sysdllFile: " + sysdllFile);
            }
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "getSysdllInstallXmlPath getCfgFile NoClassDefFoundError");
        }
        if (sysdllFile != null) {
            files.add(sysdllFile);
        }
        return files;
    }

    public void setPackageManagerInner(IHwPackageManagerInner pmsInner, Context context) {
        if (pmsInner instanceof PackageManagerService) {
            this.mPms = (PackageManagerService) pmsInner;
        }
        this.mContext = context;
    }

    public HashMap<String, HashSet<String>> getCotaDelInstallMap() {
        return sCotaDelInstallMap;
    }

    public HashMap<String, HashSet<String>> getCotaInstallMap() {
        return sCotaInstallMap;
    }

    public ArrayList<String> getDataApkShouldNotUpdateByCota() {
        return this.mShouldNotUpdateByCotaDataApks;
    }

    public void setCotaApksInstallStatus(int value) {
        this.mCotaApksInstallStatus = value;
    }

    public String replaceCotaPath(String configFilepath, String packagePath) {
        if (TextUtils.isEmpty(configFilepath) || TextUtils.isEmpty(packagePath)) {
            return "";
        }
        String realPath = packagePath;
        if (configFilepath.startsWith("/cust/cota/")) {
            realPath = packagePath.replaceFirst(VERSION_PATH, CUST_COTA_PATH);
        }
        if (configFilepath.startsWith("/cust/cotalite/")) {
            return packagePath.replaceFirst(VERSION_PATH, CUST_COTALITE_PATH);
        }
        return realPath;
    }

    public void registInstallCallBack() {
        CotaInstallImpl.getInstance().registInstallCallBack(this.mCotaInstallCallBack);
    }

    public boolean isNonSystemPartition(String path) {
        if (this.mCurrenPaths.contains(path)) {
            return true;
        }
        return false;
    }

    private static ArrayList<File> getCotaXmlFile(String xmlType) {
        ArrayList<File> cotaFiles = new ArrayList<>();
        try {
            String[] policyDirs = HwCfgFilePolicy.getCfgPolicyDir(0);
            for (String policyDir : policyDirs) {
                if (policyDir != null && policyDir.startsWith(CUST_COTA_PATH)) {
                    cotaFiles.add(new File(policyDir + "/" + xmlType));
                    Log.i(TAG, "getCotaXmlFile add = " + policyDir + "/" + xmlType);
                }
            }
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "HwCfgFilePolicy getCotaXmlFile NoClassDefFoundError");
        }
        return cotaFiles;
    }

    private static File[] getCotaApkInstallXmlFile(Set<String> allCotaApkPath) {
        int fileSize = allCotaApkPath.size();
        File[] files = new File[fileSize];
        int index = 0;
        for (String installPath : allCotaApkPath) {
            File file = new File(installPath);
            if (index < fileSize) {
                files[index] = file;
                index++;
            }
        }
        return files;
    }

    private static void deletePackageCache(String apkName) {
        File[] listOfFiles;
        File[] allPackageNameFiles;
        File cacheBaseDir = new File(CACHE_BASE_DIR);
        if (cacheBaseDir.exists() && (listOfFiles = cacheBaseDir.listFiles()) != null && listOfFiles.length > 0) {
            File cacheDir = listOfFiles[0];
            if (!(cacheDir == null || (allPackageNameFiles = cacheDir.listFiles()) == null || allPackageNameFiles.length <= 0)) {
                for (File allPackageNameFile : allPackageNameFiles) {
                    if (allPackageNameFile.getName().startsWith(apkName + AwarenessInnerConstants.DASH_KEY)) {
                        Log.i(TAG, "deletePackageCache " + allPackageNameFile);
                        if (!allPackageNameFile.delete()) {
                            Log.d(TAG, "Failed to delete file!");
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IHwPackageManagerServiceEx getHwPMSEx(IHwPackageManagerInner pmsInner, Context context) {
        return HwServiceExFactory.getHwPackageManagerServiceEx(pmsInner, context);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void realStartAutoInstall(IHwPackageManagerServiceEx packageServiceEx, String apkInstallConfig, String removableApkInstallConfig, String strMccMnc) {
        if (packageServiceEx == null || (TextUtils.isEmpty(apkInstallConfig) && TextUtils.isEmpty(removableApkInstallConfig))) {
            Log.e(TAG, "param is not valid!");
            return;
        }
        if (DEBUG) {
            Log.i(TAG, "Start Auto Install mccmnc:" + strMccMnc + " apkInstallConfig:" + apkInstallConfig + " removableApkInstallConfig:" + removableApkInstallConfig);
        }
        saveAutoInstallConfig(apkInstallConfig, AUTO_INSTALL_APK_CONFIG);
        saveAutoInstallConfig(removableApkInstallConfig, AUTO_INSTALL_DEL_APK_CONFIG);
        auotInstallForMccMnc(packageServiceEx);
        if (sAutoInstallPkgList.size() == 0) {
            Log.e(TAG, "size=0");
        } else if (this.mPms == null) {
            Log.e(TAG, "mPms null");
        } else {
            postInstallationOfHotInstall(sAutoInstallPkgList);
            updateLauncherLayout(strMccMnc);
            sIsAutoInstall = false;
            Log.i(TAG, "auto install complete!");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void realStartCotaInstall(IHwPackageManagerServiceEx packageServiceEx) {
        if (packageServiceEx == null) {
            Log.e(TAG, "realStartCotaInstall param is not valid!");
            return;
        }
        scanCotaApps(packageServiceEx);
        postInstallationOfHotInstall(this.mPms.mTempPkgList);
        this.mCotaApksInstallStatus = 1;
        if (CotaService.getICotaCallBack() != null) {
            try {
                Log.i(TAG, "isCotaAppsInstallFinish = " + getCotaStatus());
                CotaService.getICotaCallBack().onAppInstallFinish(getCotaStatus());
            } catch (RemoteException e) {
                Log.e(TAG, "onAppInstallFinish error");
            }
        }
        if (IS_SUPPORT_HW_COTA) {
            saveCotaPmsToDb(getCotaStatus());
        }
        this.mPms.mCotaFlag = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getCotaStatus() {
        return this.mCotaApksInstallStatus;
    }

    private void scanCotaApps(IHwPackageManagerServiceEx packageServiceEx) {
        this.mPms.mCotaFlag = true;
        deleteExistsIfNeeded();
        this.mCotaUpdateFlag = Settings.Global.getString(this.mContext.getContentResolver(), "cota_update_flag");
        if (IS_SUPPORT_HW_COTA) {
            ArrayList<ArrayList<File>> apkInstallList = getCotaApkInstallXmlPath();
            addTempCotaPartitionApkToHashMap(apkInstallList.get(0), apkInstallList.get(1), packageServiceEx);
        }
        if (IS_APK_INSTALL_FOREVER) {
            addTempCotaPartitionApkToHashMap(getSysdllInstallXmlPath(), new ArrayList<>(), packageServiceEx);
        }
        scanHotInstallDir(8720, packageServiceEx, sCotaInstallMap, sCotaDelInstallMap);
    }

    private void deleteExistsIfNeeded() {
        Set<String> allCotaApkPaths;
        ArrayList<File> apkDelInstallList;
        ArrayList<File> apkInstallList;
        ArrayList<ArrayList<File>> allApkInstallList;
        PackageSetting ps;
        ArrayList<ArrayList<File>> allApkInstallList2 = getCotaApkInstallXmlPath();
        int i = 0;
        ArrayList<File> apkInstallList2 = allApkInstallList2.get(0);
        boolean z = true;
        ArrayList<File> apkDelInstallList2 = allApkInstallList2.get(1);
        Set<String> allCotaApkPaths2 = getAllCotaApkPaths(apkInstallList2, apkDelInstallList2);
        File[] files = getCotaApkInstallXmlFile(allCotaApkPaths2);
        HashMap<String, ApplicationInfo> installedCotaPackageMap = getInstalledCotaPackages();
        try {
            int length = files.length;
            int i2 = 0;
            while (i2 < length) {
                File file = files[i2];
                Log.i(TAG, "deleteExistsIfNeed file: " + file);
                PackageParser pp = new PackageParser();
                pp.setCallback(new ParserCallback());
                PackageParser.Package pkg = pp.parsePackage(file, 16, z, i);
                if (pkg == null) {
                    allApkInstallList = allApkInstallList2;
                    apkInstallList = apkInstallList2;
                    apkDelInstallList = apkDelInstallList2;
                    allCotaApkPaths = allCotaApkPaths2;
                } else {
                    String pkgName = pkg.packageName;
                    PackageParser.Package oldPkg = (PackageParser.Package) this.mPms.getPackagesLock().get(pkgName);
                    if (oldPkg == null) {
                        allApkInstallList = allApkInstallList2;
                        apkInstallList = apkInstallList2;
                        apkDelInstallList = apkDelInstallList2;
                        allCotaApkPaths = allCotaApkPaths2;
                    } else {
                        String oldCodePath = oldPkg.codePath;
                        allApkInstallList = allApkInstallList2;
                        try {
                            String oldApkName = new File(oldCodePath).getName();
                            apkInstallList = apkInstallList2;
                            try {
                                ps = (PackageSetting) this.mPms.getSettings().mPackages.get(pkgName);
                                apkDelInstallList = apkDelInstallList2;
                            } catch (PackageParser.PackageParserException e) {
                                Log.e(TAG, "deleteExistsIfNeeded error for PackageParserException.");
                            } catch (IOException e2) {
                                Log.e(TAG, "deleteExistsIfNeeded error for IO.");
                            }
                            try {
                                StringBuilder sb = new StringBuilder();
                                allCotaApkPaths = allCotaApkPaths2;
                                try {
                                    sb.append("parsePackage pkg = ");
                                    sb.append(pkgName);
                                    sb.append(" ,oldCodePath = ");
                                    sb.append(oldCodePath);
                                    sb.append(" ,oldApkName = ");
                                    sb.append(oldApkName);
                                    sb.append(" ,ps = ");
                                    sb.append(ps);
                                    Log.i(TAG, sb.toString());
                                    if (this.mPms.getPackagesLock().containsKey(pkgName)) {
                                        if (ps != null) {
                                            removeApplicationFromCurCotaUpgrade(installedCotaPackageMap, pkgName);
                                            if (oldCodePath.startsWith("/data/app")) {
                                                this.mShouldNotUpdateByCotaDataApks.add(file.getCanonicalPath());
                                                Log.i(TAG, "deleteExistsIfNeed ignore " + file.getCanonicalPath());
                                            } else if (oldCodePath.startsWith(CUST_COTA_PATH)) {
                                                Log.i(TAG, "removePackageLI pkgName= " + pkgName + " oldApkName= " + oldApkName);
                                                removePackageLI(pkgName, ps.appId, oldApkName, oldPkg);
                                            } else {
                                                Log.i(TAG, "not belong to the above two situations, do nothing");
                                            }
                                        }
                                    }
                                } catch (PackageParser.PackageParserException e3) {
                                    Log.e(TAG, "deleteExistsIfNeeded error for PackageParserException.");
                                } catch (IOException e4) {
                                    Log.e(TAG, "deleteExistsIfNeeded error for IO.");
                                }
                            } catch (PackageParser.PackageParserException e5) {
                                Log.e(TAG, "deleteExistsIfNeeded error for PackageParserException.");
                            } catch (IOException e6) {
                                Log.e(TAG, "deleteExistsIfNeeded error for IO.");
                            }
                        } catch (PackageParser.PackageParserException e7) {
                            Log.e(TAG, "deleteExistsIfNeeded error for PackageParserException.");
                        } catch (IOException e8) {
                            Log.e(TAG, "deleteExistsIfNeeded error for IO.");
                        }
                    }
                }
                i2++;
                allApkInstallList2 = allApkInstallList;
                apkInstallList2 = apkInstallList;
                apkDelInstallList2 = apkDelInstallList;
                allCotaApkPaths2 = allCotaApkPaths;
                i = 0;
                z = true;
            }
            deleteCurCotaPackage(installedCotaPackageMap);
        } catch (PackageParser.PackageParserException e9) {
            Log.e(TAG, "deleteExistsIfNeeded error for PackageParserException.");
        } catch (IOException e10) {
            Log.e(TAG, "deleteExistsIfNeeded error for IO.");
        }
    }

    private HashMap<String, ApplicationInfo> getInstalledCotaPackages() {
        List<ApplicationInfo> list = this.mContext.getPackageManager().getInstalledApplications(8192);
        new ArrayList();
        HashMap<String, ApplicationInfo> hashMap = new HashMap<>();
        for (ApplicationInfo appInfo : list) {
            String codePath = appInfo.scanSourceDir;
            String packageName = appInfo.packageName;
            if (codePath.startsWith(CUST_COTA_PATH)) {
                Log.i(TAG, "Multiple cota update CotaInstalled packagename : " + packageName + " ,codePath : " + codePath);
                hashMap.put(packageName, appInfo);
            }
        }
        return hashMap;
    }

    private void deleteCurCotaPackage(HashMap<String, ApplicationInfo> map) {
        if (!(map == null || map.isEmpty())) {
            for (Map.Entry<String, ApplicationInfo> entry : map.entrySet()) {
                String deletePackageName = entry.getValue().packageName;
                String deleteAppName = new File(entry.getValue().scanSourceDir).getName();
                Log.i(TAG, "Multiple cota update need to delete Application PackageName : " + deletePackageName + " ,deleteApplication codePath : " + entry.getValue().scanSourceDir);
                removePackageLI(deletePackageName, ((PackageSetting) this.mPms.getSettings().mPackages.get(deletePackageName)).appId, deleteAppName, (PackageParser.Package) this.mPms.getPackagesLock().get(deletePackageName));
            }
        }
    }

    private void removeApplicationFromCurCotaUpgrade(HashMap<String, ApplicationInfo> map, String pkgName) {
        if (map != null && !map.isEmpty() && pkgName != null && map.containsKey(pkgName)) {
            map.remove(pkgName);
        }
    }

    private void removePackageLI(String pkgName, int appId, String appName, PackageParser.Package oldPkg) {
        if (pkgName != null && appName != null && oldPkg != null) {
            this.mPms.killApplicationInner(pkgName, appId, "killed by cota");
            this.mPms.removePackageLIInner(oldPkg, true);
            deletePackageCache(appName);
        }
    }

    private Set<String> getAllCotaApkPaths(ArrayList<File> installPaths, ArrayList<File> delInstallPaths) {
        Set<String> allCotaApkPaths = new HashSet<>();
        Iterator<File> it = installPaths.iterator();
        while (it.hasNext()) {
            File installPath = it.next();
            if (installPath != null && installPath.exists()) {
                allCotaApkPaths.addAll(getApkInstallPathList(installPath));
            }
        }
        Iterator<File> it2 = delInstallPaths.iterator();
        while (it2.hasNext()) {
            File delInstallPath = it2.next();
            if (delInstallPath != null && delInstallPath.exists()) {
                allCotaApkPaths.addAll(getApkInstallPathList(delInstallPath));
            }
        }
        return allCotaApkPaths;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0060, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0061, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0064, code lost:
        throw r5;
     */
    private Set<String> getApkInstallPathList(File scanApk) {
        Set<String> apkInstallPaths = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scanApk), UTF_8));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    $closeResource(null, reader);
                    break;
                }
                String[] strSplits = line.trim().split(",");
                if (strSplits.length != 0) {
                    String packagePath = replaceCotaPath(scanApk.getPath(), HwPackageManagerServiceUtils.getCustPackagePath(strSplits[0]));
                    Log.i(TAG, "getApkInstallPathList packagePath = " + packagePath);
                    apkInstallPaths.add(packagePath);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "HotInstall.getApkInstallPathList error for IO");
        }
        return apkInstallPaths;
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

    private void addTempCotaPartitionApkToHashMap(ArrayList<File> apkInstallList, ArrayList<File> apkDelInstallList, IHwPackageManagerServiceEx packageServiceEx) {
        HashMap<String, HashSet<String>> hashMap = sCotaInstallMap;
        if (hashMap == null) {
            sCotaInstallMap = new HashMap<>();
        } else {
            hashMap.clear();
        }
        if (apkInstallList != null) {
            HashSet<String> sysInstallSet = new HashSet<>();
            HashSet<String> privInstallSet = new HashSet<>();
            sCotaInstallMap.put(FLAG_APK_SYS, sysInstallSet);
            sCotaInstallMap.put(FLAG_APK_PRIV, privInstallSet);
            packageServiceEx.getAPKInstallListForHwPMS(apkInstallList, sCotaInstallMap);
        }
        HashMap<String, HashSet<String>> hashMap2 = sCotaDelInstallMap;
        if (hashMap2 == null) {
            sCotaDelInstallMap = new HashMap<>();
        } else {
            hashMap2.clear();
        }
        if (apkDelInstallList != null) {
            HashSet<String> sysDelInstallSet = new HashSet<>();
            HashSet<String> privDelInstallSet = new HashSet<>();
            HashSet<String> noSysDelInstallSet = new HashSet<>();
            sCotaDelInstallMap.put(FLAG_APK_SYS, sysDelInstallSet);
            sCotaDelInstallMap.put(FLAG_APK_PRIV, privDelInstallSet);
            sCotaDelInstallMap.put(FLAG_APK_NOSYS, noSysDelInstallSet);
            packageServiceEx.getAPKInstallListForHwPMS(apkDelInstallList, sCotaDelInstallMap);
            HwPackageManagerServiceUtils.setCotaDelInstallMap(sCotaDelInstallMap);
        }
    }

    private void postInstallationOfHotInstall(List<PackageParser.Package> tempPkgList) {
        PackageManagerService packageManagerService = this.mPms;
        packageManagerService.updateAllSharedLibrariesLocked((PackageParser.Package) null, Collections.unmodifiableMap(packageManagerService.mPackages));
        SystemConfig.getInstance().readCustPermissions();
        this.mPms.mPermissionManager.updateAllPermissions(StorageManager.UUID_PRIVATE_INTERNAL, false, this.mPms.mPackages.values(), this.mPms.mPermissionCallback);
        int pkgSize = tempPkgList.size();
        for (int i = 0; i < pkgSize; i++) {
            this.mPms.prepareAppDataAfterInstallLIFInner(tempPkgList.get(i));
        }
        this.mPms.mSettings.writeLPr();
        updateWidgetForHotInstall(tempPkgList);
        sendPreBootBroadcastToManagedProvisioning();
        long identity = Binder.clearCallingIdentity();
        try {
            int[] userIds = UserManagerService.getInstance().getUserIds();
            for (int userId : userIds) {
                Log.i(TAG, "auto install apps have installed ,grantCustDefaultPermissions userId = " + userId);
                this.mPms.mDefaultPermissionPolicy.grantCustDefaultPermissions(userId);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void scanHotInstallDir(int scanMode, IHwPackageManagerServiceEx packageServiceEx, HashMap<String, HashSet<String>> hotInstallMap, HashMap<String, HashSet<String>> hotDelInstallMap) {
        if (!hotInstallMap.isEmpty()) {
            packageServiceEx.installAPKforInstallListForHwPMS(hotInstallMap.get(FLAG_APK_SYS), 16, scanMode | 131072, 0, 0);
            packageServiceEx.installAPKforInstallListForHwPMS(hotInstallMap.get(FLAG_APK_PRIV), 16, scanMode | 131072 | 262144, 0, 0);
        }
        if (!hotDelInstallMap.isEmpty()) {
            packageServiceEx.installAPKforInstallListForHwPMS(hotDelInstallMap.get(FLAG_APK_SYS), 16, scanMode | 131072, 0, 33554432);
            packageServiceEx.installAPKforInstallListForHwPMS(hotDelInstallMap.get(FLAG_APK_PRIV), 16, scanMode | 131072 | 262144, 0, 33554432);
            packageServiceEx.installAPKforInstallListForHwPMS(hotDelInstallMap.get(FLAG_APK_NOSYS), 0, scanMode, 0, 33554432);
        }
    }

    private void saveCotaPmsToDb(int state) {
        String cotaPmsFlag;
        if (state == -2) {
            cotaPmsFlag = COTA_UPDATE_FLAG_INIT;
        } else if (state == 1) {
            cotaPmsFlag = COTA_UPDATE_FLAG_SUCCESS;
        } else {
            cotaPmsFlag = COTA_UPDATE_FLAG_FAIL;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Settings.Global.putString(contentResolver, COTA_PMS, this.mCotaUpdateFlag + cotaPmsFlag);
            Log.i(TAG, "startInstallCotaApks set COTA_PMS= " + this.mCotaUpdateFlag + cotaPmsFlag);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void updateLauncherLayout(String strMccMnc) {
        Intent intent = new Intent("com.huawei.android.launcher.STOP_LAUNCHER");
        intent.setPackage("com.huawei.android.launcher");
        intent.addFlags(268435456);
        intent.putExtra("mccmnc", strMccMnc);
        intent.putExtra("autoInstall", true);
        this.mContext.sendBroadcast(intent);
    }

    private void updateWidgetForHotInstall(List<PackageParser.Package> hotInstallPackageList) {
        Intent intent = new Intent(COTA_APP_UPDATE_APPWIDGET);
        Bundle extras = new Bundle();
        int size = hotInstallPackageList.size();
        String[] pkgList = new String[size];
        for (int j = 0; j < size; j++) {
            pkgList[j] = hotInstallPackageList.get(j).packageName;
        }
        extras.putStringArray(COTA_APP_UPDATE_APPWIDGET_EXTRA, pkgList);
        intent.addFlags(268435456);
        intent.putExtras(extras);
        intent.putExtra("android.intent.extra.user_handle", 0);
        this.mContext.sendBroadcast(intent, "com.huawei.permission.COTA_APPS_NO_REBOOT");
    }

    private void auotInstallForMccMnc(IHwPackageManagerServiceEx packageServiceEx) {
        HashMap<String, HashSet<String>> installMap = new HashMap<>();
        HashMap<String, HashSet<String>> delInstallMap = new HashMap<>();
        File installCfgFile = new File(AUTO_INSTALL_APK_CONFIG);
        if (installCfgFile.exists()) {
            HashSet<String> sysInstallSet = new HashSet<>();
            HashSet<String> privInstallSet = new HashSet<>();
            installMap.put(FLAG_APK_SYS, sysInstallSet);
            installMap.put(FLAG_APK_PRIV, privInstallSet);
            ArrayList<File> installFileList = new ArrayList<>();
            installFileList.add(installCfgFile);
            packageServiceEx.getAPKInstallListForHwPMS(installFileList, installMap);
            this.mCurrenPaths.addAll(sysInstallSet);
            this.mCurrenPaths.addAll(privInstallSet);
        }
        File delInstallCfgFile = new File(AUTO_INSTALL_DEL_APK_CONFIG);
        if (delInstallCfgFile.exists()) {
            HashSet<String> sysInstallSet2 = new HashSet<>();
            HashSet<String> privInstallSet2 = new HashSet<>();
            HashSet<String> noSysDelInstallSet = new HashSet<>();
            delInstallMap.put(FLAG_APK_SYS, sysInstallSet2);
            delInstallMap.put(FLAG_APK_PRIV, privInstallSet2);
            delInstallMap.put(FLAG_APK_NOSYS, noSysDelInstallSet);
            ArrayList<File> delInstallFileList = new ArrayList<>();
            delInstallFileList.add(delInstallCfgFile);
            packageServiceEx.getAPKInstallListForHwPMS(delInstallFileList, delInstallMap);
            HwPackageManagerServiceUtils.setAutoInstallMapForDelApps(delInstallMap);
            this.mCurrenPaths.addAll(sysInstallSet2);
            this.mCurrenPaths.addAll(privInstallSet2);
            this.mCurrenPaths.addAll(noSysDelInstallSet);
        }
        sIsAutoInstall = true;
        scanHotInstallDir(8720, packageServiceEx, installMap, delInstallMap);
    }

    private void sendPreBootBroadcastToManagedProvisioning() {
        Intent preBootBroadcastIntent = new Intent("android.intent.action.PRE_BOOT_COMPLETED");
        preBootBroadcastIntent.addFlags(268435456);
        preBootBroadcastIntent.setComponent(new ComponentName("com.android.managedprovisioning", "com.android.managedprovisioning.ota.PreBootListener"));
        this.mContext.sendBroadcast(preBootBroadcastIntent);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0038, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0039, code lost:
        $closeResource(r3, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003c, code lost:
        throw r4;
     */
    private void saveAutoInstallConfig(String config, String targetPath) {
        if (!TextUtils.isEmpty(config)) {
            File autoInstallPath = new File(AUTO_INSTALL_PATH);
            if (autoInstallPath.exists() || autoInstallPath.mkdir()) {
                try {
                    FileOutputStream outputStream = new FileOutputStream(targetPath);
                    outputStream.write(config.getBytes(UTF_8));
                    try {
                        $closeResource(null, outputStream);
                    } catch (FileNotFoundException | UnsupportedEncodingException e) {
                        Log.e(TAG, "save auto install config failed, file not found or not supported encoding");
                    }
                } catch (IOException e2) {
                    Log.e(TAG, "save auto install config failed, io exception");
                }
            } else {
                Log.e(TAG, "create directory failed");
            }
        }
    }

    /* access modifiers changed from: private */
    public class ParserCallback implements PackageParser.Callback {
        private ParserCallback() {
        }

        public boolean hasFeature(String feature) {
            return false;
        }

        public String[] getOverlayPaths(String targetPackageName, String targetPath) {
            return new String[0];
        }

        public String[] getOverlayApks(String targetPackageName) {
            return new String[0];
        }
    }
}
