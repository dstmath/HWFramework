package com.android.server.pm;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.SplitNotificationUtils;
import android.view.WindowManagerGlobal;
import com.android.server.am.HwActivityManagerService;
import com.android.server.cota.CotaInstallImpl;
import com.android.server.cota.CotaService;
import com.android.server.notch.HwNotchScreenWhiteConfig;
import com.android.server.os.HwBootFail;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.cust.HwCustUtils;
import huawei.com.android.server.security.fileprotect.HwAppAuthManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsDetailModeID;

public class HwPackageManagerService extends PackageManagerService {
    public static final boolean APK_INSTALL_FOREVER = SystemProperties.getBoolean("ro.config.apkinstallforever", false);
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
    private static final boolean DEBUG = DEBUG_FLAG;
    private static final boolean DEBUG_DEXOPT_OPTIMIZE = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean DEBUG_DEXOPT_SHELL = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean DEBUG_FLAG = SystemProperties.get("ro.dbg.pms_log", "0").equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
    private static final String DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final String FLAG_APK_NOSYS = "nosys";
    private static final String FLAG_APK_PRIV = "priv";
    private static final String FLAG_APK_SYS = "sys";
    private static final String KEY_HWPMS_ERROR_REBOOT_COUNT = "persist.sys.hwpms_error_reboot_count";
    private static final int MAX_HWPMS_ERROR_REBOOT_COUNT = 2;
    private static final int MAX_PKG = 100;
    private static final String SIMPLE_COTA_APK_XML_PATH = "/data/cota/live_update/work/xml/APKInstallListEMUI5Release.txt";
    private static final String SIMPLE_COTA_DEL_APK_XML_PATH = "/data/cota/live_update/work/xml/DelAPKInstallListEMUI5Release.txt";
    public static final boolean SUPPORT_HW_COTA = SystemProperties.getBoolean("ro.config.hw_cota", false);
    public static final String SYSDLL_PATH = "xml/APKInstallListEMUI5Release_732999.txt";
    private static final String TAG = "HwPackageManagerService";
    private static final int THREAD_NUM = (Runtime.getRuntime().availableProcessors() + 1);
    public static final int TRANSACTION_CODE_GET_APP_TYPE = 1023;
    public static final int TRANSACTION_CODE_GET_HDB_KEY = 1011;
    public static final int TRANSACTION_CODE_GET_IM_AND_VIDEO_APP_LIST = 1022;
    public static final int TRANSACTION_CODE_IS_NOTIFICATION_SPLIT = 1021;
    public static final int TRANSACTION_PM_CHECK_GRANTED = 1005;
    public static final int TRANSACTION_SET_ENABLED_VISITOR_SETTING = 1001;
    private static HashMap<String, HashSet<String>> mCotaDelInstallMap = null;
    private static HashMap<String, HashSet<String>> mCotaInstallMap = null;
    private static HwCustPackageManagerService mCustPackageManagerService = ((HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[0]));
    private static List<String> mCustStoppedApps = new ArrayList();
    private static HwPackageManagerService mHwPackageManagerService = null;
    private HandlerThread mCommonHandlerThread = null;
    private ComponentChangeMonitor mComponentChangeMonitor = null;
    private int mCotaApksInstallStatus = -2;
    private CotaInstallImpl.CotaInstallCallBack mCotaInstallCallBack = new CotaInstallImpl.CotaInstallCallBack() {
        public void startInstall() {
            Log.i(HwPackageManagerService.TAG, "startInstallCotaApks()");
            HwPackageManagerService.this.startInstallCotaApks();
        }

        public int getStatus() {
            Log.i(HwPackageManagerService.TAG, "getStatus()");
            return HwPackageManagerService.this.getCotaStatus();
        }
    };
    private String mCotaUpdateFlag = "";
    private Object mCust = null;
    private HashSet<String> mGrantedInstalledPkg = new HashSet<>();
    private final Installer mInstaller;

    public static synchronized PackageManagerService getInstance(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        synchronized (HwPackageManagerService.class) {
            File packagesXml = new File(new File(Environment.getDataDirectory(), "system"), "packages.xml");
            boolean isPackagesXmlExisted = packagesXml.exists();
            if (mHwPackageManagerService == null) {
                HwPackageManagerServiceEx.initCustStoppedApps();
                MultiWinWhiteListManager.getInstance().loadMultiWinWhiteList(context);
                HwPackageManagerServiceEx.initCloneAppsFromCust();
                try {
                    if (SystemProperties.getInt(KEY_HWPMS_ERROR_REBOOT_COUNT, 0) == 0 && isPackagesXmlExisted) {
                        SystemProperties.set(KEY_HWPMS_ERROR_REBOOT_COUNT, "0");
                    }
                    mHwPackageManagerService = new HwPackageManagerService(context, installer, factoryTest, onlyCore);
                    mHwPackageManagerService.getHwPMSEx().deleteNonSupportedAppsForClone();
                } catch (Exception e) {
                    Slog.e(TAG, "Error while package manager initializing! For:", e);
                }
            }
            if (mHwPackageManagerService == null) {
                if (!checkCanRebootForHwPmsError()) {
                    HwPackageManagerService hwPackageManagerService = mHwPackageManagerService;
                    return hwPackageManagerService;
                }
                if (isPackagesXmlExisted) {
                    boolean result = packagesXml.delete();
                    Settings.setPackageSettingsError();
                    Slog.e(TAG, "something may be missed in packages.xml, delete the file :" + result);
                }
                try {
                    HwBootFail.bootFailError(83886081, 0, "");
                } catch (Exception re) {
                    Slog.e(TAG, "try to reboot error, exception:" + re);
                }
            }
            HwPackageManagerService hwPackageManagerService2 = mHwPackageManagerService;
            return hwPackageManagerService2;
        }
    }

    private static boolean checkCanRebootForHwPmsError() {
        int times = SystemProperties.getInt(KEY_HWPMS_ERROR_REBOOT_COUNT, 2);
        if (times >= 2) {
            return false;
        }
        SystemProperties.set(KEY_HWPMS_ERROR_REBOOT_COUNT, (times + 1) + "");
        return true;
    }

    private static void resetHwPmsErrorRebootCount() {
        SystemProperties.set(KEY_HWPMS_ERROR_REBOOT_COUNT, "0");
    }

    public HwPackageManagerService(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        super(context, installer, factoryTest, onlyCore);
        this.mInstaller = installer;
        getHwPMSEx().recordUninstalledDelapp(null, null);
        getHwPMSEx().getOldDataBackup().clear();
        this.mCommonHandlerThread = new HandlerThread(TAG);
        this.mCommonHandlerThread.start();
        this.mComponentChangeMonitor = new ComponentChangeMonitor(context, this.mCommonHandlerThread.getLooper());
    }

    private void setEnabledVisitorSetting(int newState, int flags, String callingPackage, int userId) {
        String callingPackage2;
        String componentName;
        String packageName;
        ArrayList<String> components;
        String callingPackage3;
        ArrayList<String> components2;
        ArrayList<String> components3;
        boolean newPackage;
        int i = newState;
        int i2 = userId;
        if (i == 0 || i == 1 || i == 2 || i == 3 || i == 4) {
            int packageUid = -1;
            if (callingPackage == null) {
                callingPackage2 = Integer.toString(Binder.getCallingUid());
            } else {
                callingPackage2 = callingPackage;
            }
            ArrayList<String> components4 = null;
            HashMap<String, ArrayList<String>> componentsMap = new HashMap<>();
            HashMap<String, Integer> pkgMap = new HashMap<>();
            String pkgNameList = Settings.Secure.getString(this.mContext.getContentResolver(), "privacy_app_list");
            if (pkgNameList == null) {
                Slog.e(TAG, " pkgNameList = null ");
            } else if (pkgNameList.equals("")) {
                Slog.e(TAG, " pkgNameList is null");
            } else {
                if (DEBUG) {
                    Slog.e(TAG, " pkgNameList =   " + pkgNameList);
                }
                String[] pkgNameArray = pkgNameList.contains(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER) ? pkgNameList.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER) : new String[]{pkgNameList};
                boolean sendNow = false;
                int i3 = 0;
                while (true) {
                    int i4 = i3;
                    if (i4 >= 100 || pkgNameArray == null || i4 >= pkgNameArray.length) {
                        String str = callingPackage2;
                        ArrayList<String> arrayList = components4;
                        this.mSettings.writePackageRestrictionsLPr(i2);
                        int i5 = 0;
                    } else {
                        String packageName2 = pkgNameArray[i4];
                        String componentName2 = packageName2;
                        int packageUid2 = packageUid;
                        synchronized (this.mPackages) {
                            try {
                                String callingPackage4 = callingPackage2;
                                PackageSetting pkgSetting = (PackageSetting) this.mSettings.mPackages.get(packageName2);
                                if (pkgSetting == null) {
                                    components2 = components4;
                                    try {
                                        pkgMap.put(packageName2, 1);
                                    } catch (Throwable th) {
                                        th = th;
                                        String str2 = packageName2;
                                        String str3 = componentName2;
                                        PackageSetting packageSetting = pkgSetting;
                                        String str4 = callingPackage4;
                                        ArrayList<String> arrayList2 = components2;
                                        while (true) {
                                            try {
                                                break;
                                            } catch (Throwable th2) {
                                                th = th2;
                                            }
                                        }
                                        throw th;
                                    }
                                } else {
                                    components2 = components4;
                                    try {
                                        if (pkgSetting.getEnabled(i2) == i) {
                                            pkgMap.put(packageName2, 1);
                                        } else {
                                            callingPackage3 = (i == 0 || i == 1) ? null : callingPackage4;
                                            try {
                                                pkgSetting.setEnabled(i, i2, callingPackage3);
                                                pkgMap.put(packageName2, 0);
                                                components3 = this.mPendingBroadcasts.get(i2, packageName2);
                                                newPackage = components3 == null;
                                                if (newPackage) {
                                                    try {
                                                        components3 = new ArrayList<>();
                                                    } catch (Throwable th3) {
                                                        th = th3;
                                                        String str5 = packageName2;
                                                        String str6 = componentName2;
                                                        PackageSetting packageSetting2 = pkgSetting;
                                                        String str7 = callingPackage3;
                                                        ArrayList<String> arrayList3 = components3;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                }
                                            } catch (Throwable th4) {
                                                th = th4;
                                                String str8 = packageName2;
                                                String str9 = componentName2;
                                                PackageSetting packageSetting3 = pkgSetting;
                                                String str10 = callingPackage3;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                            try {
                                                if (!components3.contains(componentName2)) {
                                                    components3.add(componentName2);
                                                }
                                                componentsMap.put(packageName2, components3);
                                                if ((flags & 1) == 0) {
                                                    sendNow = true;
                                                    this.mPendingBroadcasts.remove(i2, packageName2);
                                                    components = components3;
                                                    packageName = packageName2;
                                                    componentName = componentName2;
                                                } else {
                                                    if (newPackage) {
                                                        this.mPendingBroadcasts.put(i2, packageName2, components3);
                                                    }
                                                    components = components3;
                                                    try {
                                                        if (!this.mHandler.hasMessages(1)) {
                                                            packageName = packageName2;
                                                            componentName = componentName2;
                                                            try {
                                                                this.mHandler.sendEmptyMessageDelayed(1, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                                                            } catch (Throwable th5) {
                                                                th = th5;
                                                                PackageSetting packageSetting4 = pkgSetting;
                                                                String str11 = callingPackage3;
                                                                ArrayList<String> arrayList4 = components;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                throw th;
                                                            }
                                                        } else {
                                                            packageName = packageName2;
                                                            componentName = componentName2;
                                                        }
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                        String str12 = packageName2;
                                                        String str13 = componentName2;
                                                        PackageSetting packageSetting5 = pkgSetting;
                                                        String str14 = callingPackage3;
                                                        ArrayList<String> arrayList5 = components;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                }
                                            } catch (Throwable th7) {
                                                th = th7;
                                                String str15 = packageName2;
                                                String str16 = componentName2;
                                                PackageSetting packageSetting6 = pkgSetting;
                                                String str17 = callingPackage3;
                                                ArrayList<String> arrayList6 = components3;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        }
                                    } catch (Throwable th8) {
                                        th = th8;
                                        String str18 = packageName2;
                                        String str19 = componentName2;
                                        PackageSetting packageSetting7 = pkgSetting;
                                        String str20 = callingPackage4;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                                packageName = packageName2;
                                componentName = componentName2;
                                callingPackage3 = callingPackage4;
                                components = components2;
                                i3 = i4 + 1;
                                PackageSetting packageSetting8 = pkgSetting;
                                callingPackage2 = callingPackage3;
                                packageUid = packageUid2;
                                components4 = components;
                                String str21 = packageName;
                                String str22 = componentName;
                                i = newState;
                            } catch (Throwable th9) {
                                th = th9;
                                String str23 = packageName2;
                                String str24 = componentName2;
                                String str25 = callingPackage2;
                                ArrayList<String> arrayList7 = components4;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                    }
                }
                String str26 = callingPackage2;
                ArrayList<String> arrayList8 = components4;
                this.mSettings.writePackageRestrictionsLPr(i2);
                int i52 = 0;
                while (i52 < 100 && pkgNameArray != null && i52 < pkgNameArray.length) {
                    String packageName3 = pkgNameArray[i52];
                    if (pkgMap.get(packageName3).intValue() != 1) {
                        PackageSetting pkgSetting2 = (PackageSetting) this.mSettings.mPackages.get(packageName3);
                        if (pkgSetting2 != null && sendNow) {
                            int packageUid3 = UserHandle.getUid(i2, pkgSetting2.appId);
                            sendPackageChangedBroadcast(packageName3, (flags & 1) != 0, componentsMap.get(packageName3), packageUid3);
                            PackageSetting packageSetting9 = pkgSetting2;
                            int i6 = packageUid3;
                        } else {
                            PackageSetting packageSetting10 = pkgSetting2;
                        }
                    }
                    i52++;
                }
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 1001) {
            Slog.w(TAG, "onTransact");
            data.enforceInterface(DESCRIPTOR);
            setEnabledVisitorSetting(data.readInt(), data.readInt(), null, data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == 1005) {
            data.enforceInterface(DESCRIPTOR);
            boolean granted = checkInstallGranted(data.readString());
            reply.writeNoException();
            reply.writeInt(granted);
            return true;
        } else if (code != 1011) {
            switch (code) {
                case 1021:
                    data.enforceInterface(DESCRIPTOR);
                    boolean result = isNotificationAddSplitButton(data.readString());
                    reply.writeNoException();
                    reply.writeInt(result);
                    return true;
                case 1022:
                    data.enforceInterface(DESCRIPTOR);
                    List<String> list = getSupportSplitScreenApps();
                    reply.writeNoException();
                    reply.writeStringList(list);
                    return true;
                case 1023:
                    data.enforceInterface(DESCRIPTOR);
                    int[] appType = getApplicationType(data);
                    reply.writeNoException();
                    reply.writeIntArray(appType);
                    return true;
                default:
                    return HwPackageManagerService.super.onTransact(code, data, reply, flags);
            }
        } else {
            data.enforceInterface(DESCRIPTOR);
            reply.writeNoException();
            reply.writeString(HwAdbManager.getHdbKey());
            return true;
        }
    }

    private void installAPKforInstallListO(HashSet<String> installList, int flags, int scanMode, long currentTime, int hwFlags) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUM);
        if (this.mCotaFlag) {
            this.mCotaApksInstallStatus = -1;
        }
        Iterator<String> it = installList.iterator();
        while (it.hasNext()) {
            String installPath = it.next();
            StringBuilder sb = new StringBuilder();
            sb.append("package install path : ");
            sb.append(installPath);
            sb.append("scanMode:");
            int i = scanMode;
            sb.append(i);
            Flog.i(205, sb.toString());
            final File file = new File(installPath);
            if (this.mIsPackageScanMultiThread) {
                final int i2 = flags;
                final int i3 = i;
                final long j = currentTime;
                final int i4 = hwFlags;
                AnonymousClass1 task = new Runnable() {
                    public void run() {
                        try {
                            HwPackageManagerService.this.scanPackageLI(file, i2, i3, j, null, i4);
                        } catch (PackageManagerException e) {
                            Slog.e(HwPackageManagerService.TAG, "Failed to parse package: " + e.getMessage());
                        }
                    }
                };
                try {
                    executorService.submit(task);
                } catch (Exception e) {
                    Exception exc = e;
                    this.mIsPackageScanMultiThread = false;
                }
            }
            if (!this.mIsPackageScanMultiThread) {
                try {
                    scanPackageLI(file, flags, i, currentTime, null, hwFlags);
                } catch (PackageManagerException e2) {
                    PackageManagerException packageManagerException = e2;
                    Slog.e(TAG, "Failed to parse package: " + e2.getMessage());
                }
            }
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e3) {
            Slog.e(TAG, "Failed to awaitTermination: " + e3.getMessage());
        }
    }

    private void addTempCotaPartitionApkToHashMap(ArrayList<File> apkInstallList, ArrayList<File> apkDelInstallList) {
        if (mCotaInstallMap == null) {
            mCotaInstallMap = new HashMap<>();
        } else {
            mCotaInstallMap.clear();
        }
        if (apkInstallList != null) {
            HashSet<String> sysInstallSet = new HashSet<>();
            HashSet<String> privInstallSet = new HashSet<>();
            mCotaInstallMap.put(FLAG_APK_SYS, sysInstallSet);
            mCotaInstallMap.put(FLAG_APK_PRIV, privInstallSet);
            getHwPMSEx().getAPKInstallListForHwPMS(apkInstallList, mCotaInstallMap);
        }
        if (mCotaDelInstallMap == null) {
            mCotaDelInstallMap = new HashMap<>();
        } else {
            mCotaDelInstallMap.clear();
        }
        if (apkDelInstallList != null) {
            HashSet<String> sysDelInstallSet = new HashSet<>();
            HashSet<String> privDelInstallSet = new HashSet<>();
            HashSet<String> noSysDelInstallSet = new HashSet<>();
            mCotaDelInstallMap.put(FLAG_APK_SYS, sysDelInstallSet);
            mCotaDelInstallMap.put(FLAG_APK_PRIV, privDelInstallSet);
            mCotaDelInstallMap.put(FLAG_APK_NOSYS, noSysDelInstallSet);
            getHwPMSEx().getAPKInstallListForHwPMS(apkDelInstallList, mCotaDelInstallMap);
            HwPackageManagerServiceUtils.setCotaDelInstallMap(mCotaDelInstallMap);
        }
    }

    private void scanTempCotaPartitionDir(int scanMode) {
        if (!mCotaInstallMap.isEmpty()) {
            getHwPMSEx().installAPKforInstallListForHwPMS(mCotaInstallMap.get(FLAG_APK_SYS), 16, scanMode | 131072, 0, 0);
            getHwPMSEx().installAPKforInstallListForHwPMS(mCotaInstallMap.get(FLAG_APK_PRIV), 16, scanMode | 131072 | HighBitsDetailModeID.MODE_FOLIAGE, 0, 0);
        }
        if (!mCotaDelInstallMap.isEmpty()) {
            getHwPMSEx().installAPKforInstallListForHwPMS(mCotaDelInstallMap.get(FLAG_APK_SYS), 16, scanMode | 131072, 0, 33554432);
            getHwPMSEx().installAPKforInstallListForHwPMS(mCotaDelInstallMap.get(FLAG_APK_PRIV), 16, scanMode | 131072 | HighBitsDetailModeID.MODE_FOLIAGE, 0, 33554432);
            getHwPMSEx().installAPKforInstallListForHwPMS(mCotaDelInstallMap.get(FLAG_APK_NOSYS), 0, scanMode, 0, 33554432);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    public void startInstallCotaApks() {
        int[] iArr;
        this.mCotaFlag = true;
        getHwPMSEx().deleteExistsIfNeedForHwPMS();
        try {
            this.mCotaUpdateFlag = Settings.Global.getString(this.mContext.getContentResolver(), "cota_update_flag");
        } catch (Exception e) {
            Log.e(TAG, "startInstallCotaApks read cota_update_flag from global exception");
        }
        if (SUPPORT_HW_COTA) {
            ArrayList<ArrayList<File>> list = HwPackageManagerServiceEx.getCotaApkInstallXMLPath();
            addTempCotaPartitionApkToHashMap(list.get(0), list.get(1));
        }
        if (APK_INSTALL_FOREVER) {
            addTempCotaPartitionApkToHashMap(HwPackageManagerServiceEx.getSysdllInstallXMLPath(), new ArrayList());
        }
        long beginCotaScanTime = System.currentTimeMillis();
        scanTempCotaPartitionDir(8720);
        long endCotaScanTime = System.currentTimeMillis();
        Log.i(TAG, "scanTempCotaPartitionDir take time is " + (endCotaScanTime - beginCotaScanTime));
        updateAllSharedLibrariesLPw(null);
        this.mPermissionManager.updateAllPermissions(StorageManager.UUID_PRIVATE_INTERNAL, false, this.mTempPkgList, this.mPermissionCallback);
        int pksSize = this.mTempPkgList.size();
        for (int i = 0; i < pksSize; i++) {
            prepareAppDataAfterInstallLIF((PackageParser.Package) this.mTempPkgList.get(i));
        }
        this.mSettings.writeLPr();
        Intent cotaintent = new Intent(COTA_APP_UPDATE_APPWIDGET);
        Bundle extras = new Bundle();
        String[] pkgList = new String[pksSize];
        for (int j = 0; j < pksSize; j++) {
            pkgList[j] = ((PackageParser.Package) this.mTempPkgList.get(j)).packageName;
        }
        extras.putStringArray(COTA_APP_UPDATE_APPWIDGET_EXTRA, pkgList);
        cotaintent.addFlags(268435456);
        cotaintent.putExtras(extras);
        cotaintent.putExtra("android.intent.extra.user_handle", 0);
        this.mContext.sendBroadcast(cotaintent);
        deleteDisallowedPacakgeInPo(pkgList);
        long identity = Binder.clearCallingIdentity();
        try {
            int[] userIds = UserManagerService.getInstance().getUserIds();
            int length = userIds.length;
            int i2 = 0;
            while (i2 < length) {
                int userId = userIds[i2];
                if (this.mDefaultPermissionPolicy != null) {
                    StringBuilder sb = new StringBuilder();
                    iArr = userIds;
                    sb.append("Cota apps have installed ,grantCustDefaultPermissions userId = ");
                    int userId2 = userId;
                    sb.append(userId2);
                    Log.i(TAG, sb.toString());
                    this.mDefaultPermissionPolicy.grantCustDefaultPermissions(userId2);
                } else {
                    iArr = userIds;
                }
                i2++;
                userIds = iArr;
            }
            Binder.restoreCallingIdentity(identity);
            this.mCotaApksInstallStatus = 1;
            if (CotaService.getICotaCallBack() != null) {
                try {
                    Log.i(TAG, "isCotaAppsInstallFinish = " + getCotaStatus());
                    CotaService.getICotaCallBack().onAppInstallFinish(getCotaStatus());
                } catch (Exception e2) {
                    Log.w(TAG, "onAppInstallFinish error," + e2.getMessage());
                }
            }
            if (SUPPORT_HW_COTA) {
                saveCotaPmsToDB(getCotaStatus());
            }
            this.mCotaFlag = false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public int getCotaStatus() {
        return this.mCotaApksInstallStatus;
    }

    private void deleteDisallowedPacakgeInPo(String[] pkgList) {
        int i;
        int i2;
        Set<String> packagesToDelete;
        String[] strArr = pkgList;
        if (strArr == null || strArr.length == 0) {
            Log.e(TAG, "pkgList is null or empty");
            return;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        if (dpm == null) {
            Log.e(TAG, "fail to get DevicePolicyManager");
            return;
        }
        int i3 = 0;
        List<UserInfo> userInfos = UserManagerService.getInstance().getUsers(false);
        if (userInfos == null) {
            Log.e(TAG, "userInfos is null");
            return;
        }
        for (UserInfo user : userInfos) {
            if (user.isManagedProfile()) {
                ComponentName mdmComponentName = dpm.getProfileOwnerAsUser(user.id);
                long identity = Binder.clearCallingIdentity();
                try {
                    Set<String> packagesToDelete2 = dpm.getDisallowedSystemApps(mdmComponentName, user.id, "android.app.action.PROVISION_MANAGED_USER");
                    if (packagesToDelete2 != null) {
                        int length = strArr.length;
                        int i4 = i3;
                        while (i4 < length) {
                            String pkgName = strArr[i4];
                            if (!packagesToDelete2.contains(pkgName)) {
                                i = i4;
                                i2 = length;
                                packagesToDelete = packagesToDelete2;
                            } else {
                                Log.d(TAG, "disable package is " + pkgName);
                                i = i4;
                                String str = pkgName;
                                i2 = length;
                                packagesToDelete = packagesToDelete2;
                                deletePackageInner(pkgName, -1, user.id, 4);
                            }
                            i4 = i + 1;
                            packagesToDelete2 = packagesToDelete;
                            length = i2;
                        }
                        Binder.restoreCallingIdentity(identity);
                        i3 = 0;
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    private void saveCotaPmsToDB(int state) {
        String cotaPMSFlag;
        if (state == -2) {
            cotaPMSFlag = COTA_UPDATE_FLAG_INIT;
        } else if (state == 1) {
            cotaPMSFlag = COTA_UPDATE_FLAG_SUCCESS;
        } else {
            cotaPMSFlag = COTA_UPDATE_FLAG_FAIL;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Settings.Global.putString(contentResolver, COTA_PMS, this.mCotaUpdateFlag + cotaPMSFlag);
            Log.i(TAG, "startInstallCotaApks set COTA_PMS = " + this.mCotaUpdateFlag + cotaPMSFlag);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void systemReady() {
        HwPackageManagerService.super.systemReady();
        AntiMalPreInstallScanner.getInstance().systemReady();
        getHwPMSEx().writePreinstalledApkListToFile();
        getHwPMSEx().createPublicityFile();
        if (SUPPORT_HW_COTA || APK_INSTALL_FOREVER) {
            CotaInstallImpl.getInstance().registInstallCallBack(this.mCotaInstallCallBack);
        }
        if (!TextUtils.isEmpty(SystemProperties.get("ro.config.hw_notch_size", ""))) {
            for (PackageSetting ps : this.mSettings.mPackages.values()) {
                if (ps.getAppUseNotchMode() > 0) {
                    HwNotchScreenWhiteConfig.getInstance().updateAppUseNotchMode(ps.pkg.packageName, ps.getAppUseNotchMode());
                }
            }
        }
        resetHwPmsErrorRebootCount();
        HwAppAuthManager.getInstance().notifyPMSReady(this.mContext);
    }

    /* access modifiers changed from: protected */
    public void checkHwCertification(PackageParser.Package pkg, boolean isUpdate) {
        if (HwCertificationManager.hasFeature()) {
            if (!HwCertificationManager.isSupportHwCertification(pkg)) {
                if (isContainHwCertification(pkg)) {
                    hwCertCleanUp(pkg);
                }
                return;
            }
            if (isUpdate || !isContainHwCertification(pkg) || isUpgrade()) {
                Slog.i("HwCertificationManager", "will checkCertificationInner,isUpdate = " + isUpdate + "isHotaUpGrade = " + isUpgrade());
                hwCertCleanUp(pkg);
                checkCertificationInner(pkg);
            }
        }
    }

    private void checkCertificationInner(PackageParser.Package pkg) {
        HwCertificationManager manager = HwCertificationManager.getIntance();
        if (manager != null && !manager.checkHwCertification(pkg)) {
            Slog.e("HwCertificationManager", "checkHwCertification parse error");
        }
    }

    /* access modifiers changed from: protected */
    public boolean getHwCertificationPermission(boolean allowed, PackageParser.Package pkg, String perm) {
        if (!HwCertificationManager.hasFeature()) {
            return allowed;
        }
        if (!HwCertificationManager.isInitialized()) {
            HwCertificationManager.initialize(this.mContext);
        }
        HwCertificationManager manager = HwCertificationManager.getIntance();
        if (manager == null) {
            return allowed;
        }
        return manager.getHwCertificationPermission(allowed, pkg, perm);
    }

    private void hwCertCleanUp(PackageParser.Package pkg) {
        HwCertificationManager manager = HwCertificationManager.getIntance();
        if (manager != null) {
            manager.cleanUp(pkg);
        }
    }

    /* access modifiers changed from: protected */
    public void hwCertCleanUp() {
        if (HwCertificationManager.hasFeature()) {
            if (!HwCertificationManager.isInitialized()) {
                HwCertificationManager.initialize(this.mContext);
            }
            HwCertificationManager manager = HwCertificationManager.getIntance();
            if (manager != null) {
                manager.cleanUp();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initHwCertificationManager() {
        if (!HwCertificationManager.isInitialized()) {
            HwCertificationManager.initialize(this.mContext);
        }
        HwCertificationManager intance = HwCertificationManager.getIntance();
    }

    /* access modifiers changed from: protected */
    public int getHwCertificateType(PackageParser.Package pkg) {
        if (!HwCertificationManager.isSupportHwCertification(pkg)) {
            return HwCertificationManager.getIntance().getHwCertificateTypeNotMDM();
        }
        return HwCertificationManager.getIntance().getHwCertificateType(pkg.packageName);
    }

    /* access modifiers changed from: protected */
    public boolean isContainHwCertification(PackageParser.Package pkg) {
        return HwCertificationManager.getIntance().isContainHwCertification(pkg.packageName);
    }

    /* access modifiers changed from: protected */
    public void addGrantedInstalledPkg(String pkgName, boolean grant) {
        if (grant) {
            synchronized (this.mGrantedInstalledPkg) {
                Slog.i(TAG, "onReceive() package added:" + pkgName);
                this.mGrantedInstalledPkg.add(pkgName);
            }
        }
    }

    private boolean checkInstallGranted(String pkgName) {
        boolean contains;
        synchronized (this.mGrantedInstalledPkg) {
            contains = this.mGrantedInstalledPkg.contains(pkgName);
        }
        return contains;
    }

    public List<String> getSupportSplitScreenApps() {
        List<String> list = new ArrayList<>();
        list.addAll(SplitNotificationUtils.getInstance(this.mContext).getListPkgName(2));
        list.addAll(SplitNotificationUtils.getInstance(this.mContext).getListPkgName(1));
        return list;
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags, int userId) {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && userId != 0 && ((newState == 0 || newState == 1) && !HwPackageManagerServiceEx.getSupportCloneApps().contains(componentName.getPackageName()))) {
            long callingId = Binder.clearCallingIdentity();
            try {
                if (!sUserManager.isClonedProfile(userId) || !skipEnableComponentForClonedUser(componentName, userId)) {
                    Binder.restoreCallingIdentity(callingId);
                } else {
                    return;
                }
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
        if (this.mComponentChangeMonitor != null) {
            this.mComponentChangeMonitor.writeComponetChangeLogToFile(componentName, newState, userId);
        }
        HwPackageManagerService.super.setComponentEnabledSetting(componentName, newState, flags, userId);
    }

    private boolean skipEnableComponentForClonedUser(ComponentName componentName, int userId) {
        if (new HashSet<>(Arrays.asList(this.mContext.getResources().getStringArray(33816586))).contains(componentName.getPackageName())) {
            Intent launcherIntent = new Intent("android.intent.action.MAIN");
            launcherIntent.addCategory("android.intent.category.LAUNCHER");
            launcherIntent.setPackage(componentName.getPackageName());
            ParceledListSlice<ResolveInfo> parceledList = queryIntentActivities(launcherIntent, launcherIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786944, userId);
            if (parceledList != null) {
                for (ResolveInfo resolveInfo : parceledList.getList()) {
                    if (componentName.equals(resolveInfo.getComponentInfo().getComponentName())) {
                        Slog.i(TAG, "skip enable [" + resolveInfo.activityInfo.getComponentName() + "] for clone user " + userId);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int[] getApplicationType(Parcel in) {
        ArrayList<String> pkgName = in.createStringArrayList();
        if (pkgName == null) {
            Slog.i(TAG, "getApplicationType , pkgName is null");
            return null;
        }
        int pkgNameSize = pkgName.size();
        int[] appType = new int[pkgNameSize];
        PackageManager package1 = this.mContext.getPackageManager();
        int[] comparedSignatures = in.createIntArray();
        Binder.clearCallingIdentity();
        for (int i = 0; i < pkgNameSize; i++) {
            ApplicationInfo ai = getApplicationInfo(pkgName.get(i), 4194304, 0);
            if (HwPackageManagerUtils.isSystemApp(ai)) {
                appType[i] = appType[i] | 1;
            }
            try {
                if (!ai.sourceDir.startsWith("/data/app/")) {
                    appType[i] = appType[i] | 2;
                }
                if (comparedSignatures == null) {
                    Slog.i(TAG, "getApplicationType , comparedSignatures is null , continue");
                } else {
                    try {
                        PackageInfo pi = package1.getPackageInfo(pkgName.get(i), 64);
                        if (pi.packageName.equals(pkgName.get(i)) && pi.signatures != null && pi.signatures.length == 1) {
                            int sigHashCode = pi.signatures[0].hashCode();
                            int length = comparedSignatures.length;
                            int i2 = 0;
                            while (true) {
                                if (i2 >= length) {
                                    break;
                                } else if (sigHashCode == comparedSignatures[i2]) {
                                    appType[i] = appType[i] | 4;
                                    break;
                                } else {
                                    i2++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Slog.i(TAG, "app:" + pkgName.get(i) + ", unmatch hwSignatures!");
                    }
                }
            } catch (Exception e2) {
                Slog.i(TAG, "app:" + pkgName.get(i) + ", not exists!");
                appType[i] = appType[i] | 128;
            }
        }
        return appType;
    }

    public HwCustPackageManagerService getCustPackageManagerService() {
        return mCustPackageManagerService;
    }

    public void setCotaApksInstallStatus(int value) {
        this.mCotaApksInstallStatus = value;
    }

    public HashMap<String, HashSet<String>> getCotaDelInstallMap() {
        return mCotaDelInstallMap;
    }

    public HashMap<String, HashSet<String>> getCotaInstallMap() {
        return mCotaInstallMap;
    }

    /* access modifiers changed from: protected */
    public boolean isNotificationAddSplitButton(String imsPkgName) {
        if (TextUtils.isEmpty(imsPkgName)) {
            return false;
        }
        List<String> oneSplitScreenImsListPkgNames = SplitNotificationUtils.getInstance(this.mContext).getListPkgName(2);
        if (oneSplitScreenImsListPkgNames.size() == 0 || !oneSplitScreenImsListPkgNames.contains(imsPkgName.toLowerCase(Locale.ENGLISH)) || !isSupportSplitScreen(imsPkgName)) {
            return false;
        }
        String dockableTopPkgName = getDockableTopPkgName();
        if (TextUtils.isEmpty(dockableTopPkgName)) {
            return false;
        }
        List<String> oneSplitScreenVideoListPkgNames = SplitNotificationUtils.getInstance(this.mContext).getListPkgName(1);
        if (oneSplitScreenVideoListPkgNames.size() == 0 || !oneSplitScreenVideoListPkgNames.contains(dockableTopPkgName.toLowerCase(Locale.ENGLISH)) || !isSupportSplitScreen(dockableTopPkgName)) {
            return false;
        }
        return true;
    }

    private String getDockableTopPkgName() {
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
        List<ActivityManager.RunningTaskInfo> tasks = null;
        if (am != null) {
            tasks = am.getRunningTasks(1);
        }
        ActivityManager.RunningTaskInfo runningTaskInfo = null;
        if (tasks != null && !tasks.isEmpty()) {
            runningTaskInfo = tasks.get(0);
        }
        if (runningTaskInfo != null && runningTaskInfo.supportsSplitScreenMultiWindow) {
            try {
                if (WindowManagerGlobal.getWindowManagerService().getDockedStackSide() == -1 && !ActivityManager.getService().isInLockTaskMode()) {
                    return runningTaskInfo.topActivity.getPackageName();
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "get dockside failed by RemoteException");
            }
        }
        return "";
    }

    private boolean isSupportSplitScreen(String packageName) {
        PackageManager packageManager = this.mContext.getPackageManager();
        int userId = ActivityManager.getCurrentUser();
        Intent mainIntent = getLaunchIntentForPackageAsUser(packageName, packageManager, userId);
        if (mainIntent != null) {
            ComponentName mainComponentName = mainIntent.getComponent();
            if (mainComponentName != null) {
                try {
                    ActivityInfo activityInfo = getActivityInfo(mainComponentName, 0, userId);
                    if (activityInfo != null) {
                        return isResizeableMode(activityInfo.resizeMode);
                    }
                } catch (RuntimeException e) {
                    Slog.e(TAG, "get activityInfo failed by ComponentNameException");
                } catch (Exception e2) {
                    Slog.e(TAG, "get activityInfo failed by ComponentNameException");
                }
            }
        }
        return false;
    }

    private boolean isResizeableMode(int mode) {
        return mode == 2 || mode == 4 || mode == 1;
    }

    private Intent getLaunchIntentForPackageAsUser(String packageName, PackageManager pm, int userId) {
        Intent intentToResolve = new Intent("android.intent.action.MAIN");
        intentToResolve.addCategory("android.intent.category.INFO");
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = pm.queryIntentActivitiesAsUser(intentToResolve, 0, userId);
        if (ris == null || ris.size() <= 0) {
            intentToResolve.removeCategory("android.intent.category.INFO");
            intentToResolve.addCategory("android.intent.category.LAUNCHER");
            intentToResolve.setPackage(packageName);
            ris = pm.queryIntentActivitiesAsUser(intentToResolve, 0, userId);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(268435456);
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return intent;
    }
}
