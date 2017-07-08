package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.Permission;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IMountService.Stub;
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

public class HwCustPackageManagerServiceImpl extends HwCustPackageManagerService {
    private static String BOOT_DELETE_CONFIG = null;
    protected static final boolean HWDBG;
    protected static final boolean HWFLOW;
    protected static final boolean HWLOGW_E = true;
    public static final boolean SECURITY_PACKAGE_ENABLE;
    private static final String TAG = "HwCustPackageManager";
    private static final String TAG_FLOW = "HwCustPackageManager_FLOW";
    private static final String TAG_INIT = "HwCustPackageManager_INIT";
    Context mContext;
    private ArrayList<DelPackage> mListedApps;
    private Object mLock;
    boolean mSdInstallEnable;

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
        boolean z = HWLOGW_E;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : SECURITY_PACKAGE_ENABLE : HWLOGW_E;
        HWDBG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : SECURITY_PACKAGE_ENABLE;
        }
        HWFLOW = z;
        SECURITY_PACKAGE_ENABLE = SystemProperties.getBoolean("ro.config.hw_security_pkg", SECURITY_PACKAGE_ENABLE);
        BOOT_DELETE_CONFIG = "/product/etc/boot_delete_apps.cfg";
    }

    public HwCustPackageManagerServiceImpl() {
        this.mSdInstallEnable = SystemProperties.getBoolean("ro.config.hw_sdInstall_enable", SECURITY_PACKAGE_ENABLE);
        this.mLock = new Object();
        this.mListedApps = new ArrayList();
        if (HWFLOW) {
            Log.d(TAG_FLOW, "HwCustPackageManagerServiceImpl");
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
        if (SystemProperties.getBoolean("ro.config.hw_DMHFA", SECURITY_PACKAGE_ENABLE)) {
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
        if (pkg == null || !pkg.hasComponentClassName(componentName)) {
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
        if (mccmnc == null || "".equals(mccmnc)) {
            return SECURITY_PACKAGE_ENABLE;
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
            return SECURITY_PACKAGE_ENABLE;
        }
        try {
            int DiskID = Integer.valueOf(CurrentDiskIDSplitstr[2]).intValue();
            int VolumeID = Integer.valueOf(CurrentVolumeIDSplitstr[2]).intValue();
            if (VolumeID == DiskID + 1 || VolumeID == DiskID) {
                return HWLOGW_E;
            }
            return SECURITY_PACKAGE_ENABLE;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return SECURITY_PACKAGE_ENABLE;
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
        return SECURITY_PACKAGE_ENABLE;
    }

    public boolean needDerivePkgAbi(Package pkg) {
        if (isSdInstallEnabled() && pkg.applicationInfo.isExternalAsec()) {
            return isSDCardMounted();
        }
        return SECURITY_PACKAGE_ENABLE;
    }

    public boolean canAppMoveToPublicSd(VolumeInfo volume) {
        boolean z = SECURITY_PACKAGE_ENABLE;
        if (!isSdInstallEnabled() || volume == null || volume.getDisk() == null) {
            return SECURITY_PACKAGE_ENABLE;
        }
        if (volume.getDisk().isSd() && volume.isMountedWritable()) {
            z = isFirstSdVolume(volume);
        }
        return z;
    }

    public boolean isHwCustHiddenInfoPackage(Package pkgInfo) {
        return SECURITY_PACKAGE_ENABLE ? isRestrictedPackage(pkgInfo) : SECURITY_PACKAGE_ENABLE;
    }

    private boolean isRestrictedPackage(Package pkgInfo) {
        if (pkgInfo != null && hasPermission(pkgInfo, "android.permission.SECURITY_PACKAGE") && Binder.getCallingUid() >= 10000 && Binder.getCallingUid() <= 19999) {
            return HWLOGW_E;
        }
        return SECURITY_PACKAGE_ENABLE;
    }

    private static boolean hasPermission(Package pkgInfo, String perm) {
        for (int i = pkgInfo.permissions.size() - 1; i >= 0; i--) {
            if (((Permission) pkgInfo.permissions.get(i)).info.name.equals(perm)) {
                return HWLOGW_E;
            }
        }
        return SECURITY_PACKAGE_ENABLE;
    }

    public boolean isSdVol(VolumeInfo vol) {
        boolean z = SECURITY_PACKAGE_ENABLE;
        if (!this.mSdInstallEnable || vol.getDisk() == null) {
            return SECURITY_PACKAGE_ENABLE;
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

    private void readDelAppsList() {
        IOException e;
        Throwable th;
        BufferedReader bufferedReader = null;
        try {
            File confFile = new File(BOOT_DELETE_CONFIG);
            File cfg = HwCfgFilePolicy.getCfgFile("boot_delete_apps.cfg", 0);
            if (cfg != null) {
                confFile = cfg;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(confFile), "UTF-8"));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] apps = line.trim().split(",");
                    this.mListedApps.add(new DelPackage(apps[0], Integer.parseInt(apps[1])));
                } catch (NoClassDefFoundError e2) {
                    bufferedReader = reader;
                } catch (FileNotFoundException e3) {
                    bufferedReader = reader;
                } catch (IOException e4) {
                    e = e4;
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = reader;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e5) {
                    e5.printStackTrace();
                }
            }
        } catch (NoClassDefFoundError e6) {
            try {
                Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e52) {
                        e52.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e522) {
                        e522.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            Log.i(TAG, "boot_delete_apps.cfg Not Found.");
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e5222) {
                    e5222.printStackTrace();
                }
            }
        } catch (IOException e8) {
            e = e8;
            Log.i(TAG, "boot_delete_apps.cfg IOException");
            e.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e52222) {
                    e52222.printStackTrace();
                }
            }
        }
    }

    public boolean isUnAppInstallAllowed(String originPath, Context context) {
        if (context == null || (!isUnInstallerCheck(context) && !isUnInstallerValid(originPath, context))) {
            return SECURITY_PACKAGE_ENABLE;
        }
        return HWLOGW_E;
    }

    private boolean isUnInstallerCheck(Context context) {
        return Secure.getInt(context.getContentResolver(), "hw_uninstall_status", 0) != 0 ? HWLOGW_E : SECURITY_PACKAGE_ENABLE;
    }

    private boolean isUnInstallerValid(String originPath, Context context) {
        String whiteInstallerPackages = Secure.getString(context.getContentResolver(), "hw_installer_whitelist");
        if (whiteInstallerPackages == null || "".equals(whiteInstallerPackages.trim()) || originPath == null) {
            return SECURITY_PACKAGE_ENABLE;
        }
        for (String pkg : whiteInstallerPackages.split(";")) {
            if (originPath.contains(pkg)) {
                return SECURITY_PACKAGE_ENABLE;
            }
        }
        return HWLOGW_E;
    }
}
