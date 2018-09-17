package com.android.server.pm;

import android.app.AppGlobals;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.Permission;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IStorageManager.Stub;
import android.os.storage.VolumeInfo;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwCustPackageManagerServiceImpl extends HwCustPackageManagerService {
    private static String BOOT_DELETE_CONFIG = "/product/etc/boot_delete_apps.cfg";
    private static final String CUST_DEFAULT_LAUNCHER = SystemProperties.get("ro.config.def_launcher_pkg", "");
    private static final boolean FILT_REQ_PERM = SystemProperties.getBoolean("ro.config.hw_filt_req_perm", false);
    protected static final boolean HWDBG;
    protected static final boolean HWFLOW;
    protected static final boolean HWLOGW_E = true;
    public static final boolean SECURITY_PACKAGE_ENABLE = SystemProperties.getBoolean("ro.config.hw_security_pkg", false);
    private static final int SYSTEMUI_DEFAULT_UID = -100;
    private static final String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    private static final String TAG = "HwCustPackageManager";
    private static final String TAG_FLOW = "HwCustPackageManager_FLOW";
    private static final String TAG_INIT = "HwCustPackageManager_INIT";
    Context mContext;
    private ArrayList<DelPackage> mListedApps = new ArrayList();
    private Object mLock = new Object();
    boolean mSdInstallEnable = SystemProperties.getBoolean("ro.config.hw_sdInstall_enable", false);
    private int mSystemUIUid = SYSTEMUI_DEFAULT_UID;

    private class DelPackage {
        private int delFlag;
        private String delPackageName;

        private DelPackage() {
        }

        public DelPackage(String name, int flag) {
            this.delPackageName = name;
            this.delFlag = flag;
        }
    }

    static {
        boolean z;
        boolean z2 = HWLOGW_E;
        if (Log.HWLog) {
            z = HWLOGW_E;
        } else {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false;
        }
        HWDBG = z;
        if (!Log.HWINFO) {
            z2 = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        HWFLOW = z2;
    }

    public HwCustPackageManagerServiceImpl() {
        if (HWFLOW) {
            Log.d(TAG_FLOW, "HwCustPackageManagerServiceImpl");
        }
        if (SECURITY_PACKAGE_ENABLE) {
            this.mSystemUIUid = getUidByPackageName(SYSTEMUI_PACKAGE_NAME);
        }
    }

    public void scanCustPrivDir(int scanMode, AbsPackageManagerService service) {
        if (HWFLOW) {
            Log.d(TAG_FLOW, "scanCustPrivDir");
        }
        File custPrivAppDir = new File("/data/cust/", "priv-app");
        if (custPrivAppDir.exists()) {
            service.custScanPrivDir(custPrivAppDir, 193, scanMode, 0, 0);
        }
        File custDelPrivAppDir = new File("/data/cust/", "priv-delapp");
        if (custDelPrivAppDir.exists()) {
            service.custScanPrivDir(custDelPrivAppDir, 193, scanMode, 0, 33554432);
        }
    }

    public boolean isPrivAppInCust(File file) {
        if (HWFLOW) {
            Log.d(TAG_FLOW, "isPrivAppInCust,file=" + file);
        }
        String filePath = file.getAbsolutePath();
        if (filePath.startsWith("/data/cust/priv-app") || filePath.startsWith("/data/cust/priv-delapp") || filePath.startsWith("/data/cota/atl/priv-app")) {
            return HWLOGW_E;
        }
        return filePath.startsWith("/data/cota/atl/priv-delapp");
    }

    public void handleCustInitailizations(Object settings) {
        if (SystemProperties.getBoolean("ro.config.hw_DMHFA", false)) {
            enableApplication(settings, "com.huawei.android.DMHFA", 1);
            enableComponent(settings, "com.huawei.sprint.setupwizard", "com.huawei.sprint.setupwizard.controller.ControllerActivity");
        }
    }

    public void enableApplication(Object settings, String packageName, int newState) {
        PackageSetting packageSetting = null;
        if (settings instanceof Settings) {
            packageSetting = (PackageSetting) ((Settings) settings).mPackages.get(packageName);
        }
        if (packageSetting == null) {
            Log.w(TAG_FLOW, "enableApplication Unknown package: " + packageName);
            return;
        }
        int userId = UserHandle.getCallingUserId();
        if (packageSetting.getEnabled(userId) == newState) {
            if (HWFLOW) {
                Log.d(TAG, "**** Nothing to do!");
            }
            return;
        }
        packageSetting.setEnabled(newState, userId, null);
    }

    private void enableComponent(Object settings, String packageName, String componentName) {
        PackageSetting packageSetting = null;
        if (settings instanceof Settings) {
            packageSetting = (PackageSetting) ((Settings) settings).mPackages.get(packageName);
        }
        if (packageSetting == null) {
            Log.w(TAG_FLOW, "enableComponent Unknown package: " + packageName);
            return;
        }
        String classname = componentName;
        Package pkg = packageSetting.pkg;
        if (pkg == null || (pkg.hasComponentClassName(componentName) ^ 1) != 0) {
            Log.w(TAG_FLOW, "Failed setComponentEnabledSetting: component class " + componentName + " does not exist");
        } else {
            packageSetting.enableComponentLPw(componentName, UserHandle.getCallingUserId());
        }
    }

    public File customizeUninstallApk(File file) {
        String apkListFilePath = SystemProperties.get("ro.config.huawei.apklistpath", "");
        if (!TextUtils.isEmpty(apkListFilePath)) {
            File customFile = new File(apkListFilePath);
            if (customFile.exists()) {
                return customFile;
            }
        }
        return file;
    }

    public boolean isMccMncMatch() {
        String mccmnc = SystemProperties.get("persist.sys.mccmnc", "");
        if (mccmnc == null || ("".equals(mccmnc) ^ 1) == 0) {
            return false;
        }
        return HWLOGW_E;
    }

    public String joinCustomizeFile(String fileName) {
        String joinFileName = fileName;
        String mccmnc = SystemProperties.get("persist.sys.mccmnc", "");
        if (fileName == null) {
            return joinFileName;
        }
        String[] splitArray = fileName.split("\\.");
        if (splitArray.length == 2) {
            return splitArray[0] + "_" + mccmnc + "." + splitArray[1];
        }
        return joinFileName;
    }

    public String getCustomizeAPKListFile(String apkListFile, String mAPKInstallList_FILE, String mDelAPKInstallList_FILE, String mAPKInstallList_DIR) {
        if (apkListFile == null) {
            return apkListFile;
        }
        if (!apkListFile.equals(mAPKInstallList_FILE) && !apkListFile.equals(mDelAPKInstallList_FILE)) {
            return apkListFile;
        }
        String tmpAPKInstallFile = joinCustomizeFile(mAPKInstallList_FILE);
        String tmpDelAPKInstallFile = joinCustomizeFile(mDelAPKInstallList_FILE);
        if (new File(mAPKInstallList_DIR, tmpAPKInstallFile).exists() || new File(mAPKInstallList_DIR, tmpDelAPKInstallFile).exists()) {
            return apkListFile.equals(mAPKInstallList_FILE) ? tmpAPKInstallFile : tmpDelAPKInstallFile;
        } else {
            return apkListFile;
        }
    }

    public String getCustomizeAPKInstallFile(String APKInstallFile, String DelAPKInstallFile) {
        String tmpAPKInstallFile = joinCustomizeFile(APKInstallFile);
        String tmpDelAPKInstallFile = joinCustomizeFile(DelAPKInstallFile);
        try {
            if (HwCfgFilePolicy.getCfgFileList(tmpAPKInstallFile, 0).size() > 0 || HwCfgFilePolicy.getCfgFileList(tmpDelAPKInstallFile, 0).size() > 0) {
                return tmpAPKInstallFile;
            }
            return APKInstallFile;
        } catch (NoClassDefFoundError e) {
            if (HWDBG) {
                Log.d(TAG_FLOW, "getCustomizeAPKInstallFile: NoClassDefFound");
            }
        }
    }

    public String getCustomizeDelAPKInstallFile(String APKInstallFile, String DelAPKInstallFile) {
        String tmpAPKInstallFile = joinCustomizeFile(APKInstallFile);
        String tmpDelAPKInstallFile = joinCustomizeFile(DelAPKInstallFile);
        try {
            if (HwCfgFilePolicy.getCfgFileList(tmpAPKInstallFile, 0).size() > 0 || HwCfgFilePolicy.getCfgFileList(tmpDelAPKInstallFile, 0).size() > 0) {
                return tmpDelAPKInstallFile;
            }
            return DelAPKInstallFile;
        } catch (NoClassDefFoundError e) {
            if (HWDBG) {
                Log.d(TAG_FLOW, "getCustomizeDelAPKInstallFile: NoClassDefFound");
            }
        }
    }

    public boolean isSdInstallEnabled() {
        return this.mSdInstallEnable;
    }

    private boolean isFirstSdVolume(VolumeInfo vol) {
        String CurrentDiskID = vol.getDisk().getId();
        String CurrentVolumeID = vol.getId();
        String[] CurrentDiskIDSplitstr = CurrentDiskID.split(":");
        String[] CurrentVolumeIDSplitstr = CurrentVolumeID.split(":");
        if (CurrentDiskIDSplitstr.length != 3 || CurrentVolumeIDSplitstr.length != 3) {
            return false;
        }
        try {
            int DiskID = Integer.valueOf(CurrentDiskIDSplitstr[2]).intValue();
            int VolumeID = Integer.valueOf(CurrentVolumeIDSplitstr[2]).intValue();
            if (VolumeID == DiskID + 1 || VolumeID == DiskID) {
                return HWLOGW_E;
            }
            return false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isSDCardMounted() {
        try {
            for (VolumeInfo vol : Stub.asInterface(ServiceManager.getService("mount")).getVolumes(0)) {
                if (vol.getDisk() != null && vol.isMountedWritable() && vol.getDisk().isSd() && isFirstSdVolume(vol)) {
                    return HWLOGW_E;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean needDerivePkgAbi(Package pkg) {
        if (isSdInstallEnabled() && pkg.applicationInfo.isExternalAsec()) {
            return isSDCardMounted();
        }
        return false;
    }

    public boolean canAppMoveToPublicSd(VolumeInfo volume) {
        boolean z = false;
        if (!isSdInstallEnabled() || volume == null || volume.getDisk() == null) {
            return false;
        }
        if (volume.getDisk().isSd() && volume.isMountedWritable()) {
            z = isFirstSdVolume(volume);
        }
        return z;
    }

    public boolean isHwCustHiddenInfoPackage(Package pkgInfo) {
        return SECURITY_PACKAGE_ENABLE ? isRestrictedPackage(pkgInfo) : false;
    }

    private boolean isRestrictedPackage(Package pkgInfo) {
        if (pkgInfo != null && hasPermission(pkgInfo, "android.permission.SECURITY_PACKAGE")) {
            int uid = Binder.getCallingUid();
            if (uid >= 10000 && uid <= 19999 && uid != this.mSystemUIUid) {
                return HWLOGW_E;
            }
        }
        return false;
    }

    private int getUidByPackageName(String packageName) {
        int uid = SYSTEMUI_DEFAULT_UID;
        try {
            return AppGlobals.getPackageManager().getPackageUid(packageName, 1048576, UserHandle.getCallingUserId());
        } catch (Exception e) {
            Log.w(TAG_FLOW, "Exception happend, when get package uid");
            return uid;
        }
    }

    private static boolean hasPermission(Package pkgInfo, String perm) {
        for (int i = pkgInfo.permissions.size() - 1; i >= 0; i--) {
            if (((Permission) pkgInfo.permissions.get(i)).info.name.equals(perm)) {
                return HWLOGW_E;
            }
        }
        return false;
    }

    public boolean isSdVol(VolumeInfo vol) {
        boolean z = false;
        if (!this.mSdInstallEnable || vol.getDisk() == null) {
            return false;
        }
        if (vol.getDisk().isSd()) {
            z = isFirstSdVolume(vol);
        }
        return z;
    }

    public int isListedApp(String packageName) {
        synchronized (this.mLock) {
            if (this.mListedApps.size() == 0) {
                readDelAppsList();
            }
        }
        for (DelPackage app : this.mListedApps) {
            if (packageName.equals(app.delPackageName)) {
                return app.delFlag;
            }
        }
        return -1;
    }

    public boolean isHwFiltReqInstallPerm(String pkgName, String permission) {
        if (FILT_REQ_PERM && "com.huawei.hidisk".equals(pkgName) && "android.permission.REQUEST_INSTALL_PACKAGES".equals(permission)) {
            return HWLOGW_E;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x005d A:{SYNTHETIC, Splitter: B:16:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0091 A:{SYNTHETIC, Splitter: B:36:0x0091} */
    /* JADX WARNING: Removed duplicated region for block: B:60:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x007c A:{SYNTHETIC, Splitter: B:28:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a2 A:{SYNTHETIC, Splitter: B:44:0x00a2} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readDelAppsList() {
        Throwable th;
        IOException e;
        BufferedReader reader = null;
        try {
            File confFile = new File(BOOT_DELETE_CONFIG);
            File cfg = HwCfgFilePolicy.getCfgFile("boot_delete_apps.cfg", 0);
            if (cfg != null) {
                confFile = cfg;
            }
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(confFile), "UTF-8"));
            while (true) {
                try {
                    String line = reader2.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] apps = line.trim().split(",");
                    this.mListedApps.add(new DelPackage(apps[0], Integer.parseInt(apps[1])));
                } catch (NoClassDefFoundError e2) {
                    reader = reader2;
                    try {
                        Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
                        if (reader == null) {
                            try {
                                reader.close();
                                return;
                            } catch (Exception e3) {
                                e3.printStackTrace();
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    reader = reader2;
                    Log.i(TAG, "boot_delete_apps.cfg Not Found.");
                    if (reader == null) {
                        try {
                            reader.close();
                            return;
                        } catch (Exception e32) {
                            e32.printStackTrace();
                            return;
                        }
                    }
                    return;
                } catch (IOException e5) {
                    e = e5;
                    reader = reader2;
                    Log.i(TAG, "boot_delete_apps.cfg IOException");
                    e.printStackTrace();
                    if (reader == null) {
                        try {
                            reader.close();
                            return;
                        } catch (Exception e322) {
                            e322.printStackTrace();
                            return;
                        }
                    }
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e3222) {
                            e3222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (Exception e32222) {
                    e32222.printStackTrace();
                }
            }
        } catch (NoClassDefFoundError e6) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            if (reader == null) {
            }
        } catch (FileNotFoundException e7) {
            Log.i(TAG, "boot_delete_apps.cfg Not Found.");
            if (reader == null) {
            }
        } catch (IOException e8) {
            e = e8;
            Log.i(TAG, "boot_delete_apps.cfg IOException");
            e.printStackTrace();
            if (reader == null) {
            }
        }
    }

    public boolean isUnAppInstallAllowed(String originPath, Context context) {
        if (context == null || (!isUnInstallerCheck(context) && !isUnInstallerValid(originPath, context))) {
            return false;
        }
        return HWLOGW_E;
    }

    private boolean isUnInstallerCheck(Context context) {
        return Secure.getInt(context.getContentResolver(), "hw_uninstall_status", 0) != 0 ? HWLOGW_E : false;
    }

    private boolean isUnInstallerValid(String originPath, Context context) {
        String whiteInstallerPackages = Secure.getString(context.getContentResolver(), "hw_installer_whitelist");
        if (whiteInstallerPackages == null || ("".equals(whiteInstallerPackages.trim()) ^ 1) == 0 || originPath == null) {
            return false;
        }
        for (String pkg : whiteInstallerPackages.split(";")) {
            if (originPath.contains(pkg)) {
                return false;
            }
        }
        return HWLOGW_E;
    }

    private boolean isDocomo() {
        return SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    }

    public boolean isSkipMmsSendImageAction() {
        if (!isDocomo()) {
            return false;
        }
        Log.d(TAG, "Not support MMS , skip MMS SEND action !");
        return HWLOGW_E;
    }

    public List<ResolveInfo> filterResolveInfos(List<ResolveInfo> rInfos, Intent intent, String resolvedType) {
        if (isDocomo() && rInfos != null && intent != null && rInfos.size() > 0) {
            String action = intent.getAction();
            Iterator<ResolveInfo> rIter = rInfos.iterator();
            boolean justText = isJustText(intent);
            while (rIter.hasNext()) {
                ResolveInfo rInfo = (ResolveInfo) rIter.next();
                if (rInfo.activityInfo != null) {
                    String pkgName = rInfo.activityInfo.packageName;
                    if (resolvedType != null) {
                        int i;
                        if (resolvedType.contains("text/")) {
                            i = justText;
                        } else {
                            i = 0;
                        }
                        if ((i ^ 1) != 0 && (("android.intent.action.SEND".equals(action) || "android.intent.action.SEND_MULTIPLE".equals(action)) && "com.android.mms".equals(pkgName))) {
                            rIter.remove();
                            Log.d(TAG, "skip Mms for send image :" + action + ", pkg:" + pkgName);
                        }
                    }
                }
            }
        }
        return rInfos;
    }

    private boolean isJustText(Intent intent) {
        ClipData clipData = intent.getClipData();
        if (clipData == null) {
            return false;
        }
        for (int i = 0; i < clipData.getItemCount(); i++) {
            Item item = clipData.getItemAt(i);
            if (item != null && (item.getIntent() != null || item.getUri() != null)) {
                return false;
            }
        }
        return HWLOGW_E;
    }

    public String getCustDefaultLauncher(Context context) {
        if (TextUtils.isEmpty(CUST_DEFAULT_LAUNCHER)) {
            return null;
        }
        try {
            context.getPackageManager().getPackageInfoAsUser(CUST_DEFAULT_LAUNCHER, 128, UserHandle.getCallingUserId());
            return CUST_DEFAULT_LAUNCHER;
        } catch (NameNotFoundException e) {
            Log.d(TAG, "there is no this cust launcher in system");
            return null;
        }
    }
}
