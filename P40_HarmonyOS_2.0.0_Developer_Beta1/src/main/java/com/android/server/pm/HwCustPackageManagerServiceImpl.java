package com.android.server.pm;

import android.app.AppGlobals;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwCustPackageManagerServiceImpl extends HwCustPackageManagerService {
    private static final String BOOT_DELETE_CONFIG = "/product/etc/boot_delete_apps.cfg";
    private static final String CUST_DEFAULT_LAUNCHER = SystemProperties.get("ro.config.def_launcher_pkg", "");
    private static final String DEVICE_OWNER_XML = "device_owner_2.xml";
    private static final String DEVICE_OWNER_XML_LEGACY = "device_owner.xml";
    private static final boolean FILT_REQ_PERM = SystemProperties.getBoolean("ro.config.hw_filt_req_perm", false);
    private static final boolean FILT_REQ_PERM_ALL = SystemProperties.getBoolean("ro.config.hw_filt_req_perm_all", false);
    private static final boolean HWDBG;
    private static final boolean HWFLOW;
    private static final boolean HWLOGW_E = true;
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final int SD_VOLUME_ID_LENGTH = 3;
    public static final boolean SECURITY_PACKAGE_ENABLE = SystemProperties.getBoolean("ro.config.hw_security_pkg", false);
    private static final String SIMPLE_LAUNCHER_PACKAGE_NAME = "com.huawei.android.simplelauncher";
    private static final int SYSTEMUI_DEFAULT_UID = -100;
    private static final String SYSTEMUI_PACKAGE_NAME = "com.android.systemui";
    private static final String TAG = "HwCustPackageManager";
    private static final String TAG_DEVICE_OWNER = "device-owner";
    private static final String TAG_FLOW = "HwCustPackageManager_FLOW";
    private static final String TAG_INIT = "HwCustPackageManager_INIT";
    Context mContext;
    private ArrayList<DelPackage> mListedApps = new ArrayList<>();
    private Object mLock = new Object();
    boolean mSdInstallEnable = SystemProperties.getBoolean("ro.config.hw_sdInstall_enable", false);
    private int mSystemUIUid = SYSTEMUI_DEFAULT_UID;

    static {
        boolean z = Log.HWLog;
        boolean z2 = HWLOGW_E;
        HWDBG = z || (Log.HWModuleLog && Log.isLoggable(TAG, SD_VOLUME_ID_LENGTH));
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z2 = false;
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

    public void handleCustInitailizations(Object settings) {
        if (SystemProperties.getBoolean("ro.config.hw_DMHFA", false)) {
            enableApplication(settings, "com.huawei.android.DMHFA", 1);
            enableComponent(settings, "com.huawei.sprint.setupwizard", "com.huawei.sprint.setupwizard.controller.ControllerActivity");
        }
    }

    public void enableApplication(Object settings, String packageName, int newState) {
        PackageSetting pkgSetting = null;
        if (settings instanceof Settings) {
            pkgSetting = (PackageSetting) ((Settings) settings).mPackages.get(packageName);
        }
        if (pkgSetting == null) {
            Log.w(TAG_FLOW, "enableApplication Unknown package: " + packageName);
            return;
        }
        int userId = UserHandle.getCallingUserId();
        if (pkgSetting.getEnabled(userId) != newState) {
            pkgSetting.setEnabled(newState, userId, (String) null);
        } else if (HWFLOW) {
            Log.d(TAG, "**** Nothing to do!");
        }
    }

    private void enableComponent(Object settings, String packageName, String componentName) {
        PackageSetting pkgSetting = null;
        if (settings instanceof Settings) {
            pkgSetting = (PackageSetting) ((Settings) settings).mPackages.get(packageName);
        }
        if (pkgSetting == null) {
            Log.w(TAG_FLOW, "enableComponent Unknown package: " + packageName);
            return;
        }
        PackageParser.Package pkg = pkgSetting.pkg;
        if (pkg == null || !pkg.hasComponentClassName(componentName)) {
            Log.w(TAG_FLOW, "Failed setComponentEnabledSetting: component class " + componentName + " does not exist");
            return;
        }
        pkgSetting.enableComponentLPw(componentName, UserHandle.getCallingUserId());
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
        boolean doModel = isDoModel();
        Log.d(TAG, "doModel is " + doModel);
        if (mccmnc == null || "".equals(mccmnc) || doModel) {
            return false;
        }
        return HWLOGW_E;
    }

    public String joinCustomizeFile(String fileName) {
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

    public String getCustomizeAPKListFile(String apkListFile, String installListFile, String delApkInstallListFile, String apkInstallListDir) {
        if (apkListFile == null) {
            return apkListFile;
        }
        if (!apkListFile.equals(installListFile) && !apkListFile.equals(delApkInstallListFile)) {
            return apkListFile;
        }
        String tmpApkInstallFile = joinCustomizeFile(installListFile);
        String tmpDelAPKInstallFile = joinCustomizeFile(delApkInstallListFile);
        if (!new File(apkInstallListDir, tmpApkInstallFile).exists() && !new File(apkInstallListDir, tmpDelAPKInstallFile).exists()) {
            return apkListFile;
        }
        return apkListFile.equals(installListFile) ? tmpApkInstallFile : tmpDelAPKInstallFile;
    }

    public String getCustomizeAPKInstallFile(String apkInstallFile, String delApkInstallFile) {
        String tmpAPKInstallFile = joinCustomizeFile(apkInstallFile);
        String tmpDelAPKInstallFile = joinCustomizeFile(delApkInstallFile);
        try {
            if (HwCfgFilePolicy.getCfgFileList(tmpAPKInstallFile, 0).size() > 0 || HwCfgFilePolicy.getCfgFileList(tmpDelAPKInstallFile, 0).size() > 0) {
                return tmpAPKInstallFile;
            }
            return apkInstallFile;
        } catch (NoClassDefFoundError e) {
            if (HWDBG) {
                Log.d(TAG_FLOW, "getCustomizeAPKInstallFile: NoClassDefFound");
            }
        }
    }

    public String getCustomizeDelAPKInstallFile(String apkInstallFile, String delApkInstallFile) {
        String tmpAPKInstallFile = joinCustomizeFile(apkInstallFile);
        String tmpDelAPKInstallFile = joinCustomizeFile(delApkInstallFile);
        try {
            if (HwCfgFilePolicy.getCfgFileList(tmpAPKInstallFile, 0).size() > 0 || HwCfgFilePolicy.getCfgFileList(tmpDelAPKInstallFile, 0).size() > 0) {
                return tmpDelAPKInstallFile;
            }
            return delApkInstallFile;
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
        String currentDiskID = vol.getDisk().getId();
        String currentVolumeID = vol.getId();
        String[] currentDiskIDSplitStr = currentDiskID.split(":");
        String[] currentVolumeIDSplitStr = currentVolumeID.split(":");
        if (currentDiskIDSplitStr.length != SD_VOLUME_ID_LENGTH || currentVolumeIDSplitStr.length != SD_VOLUME_ID_LENGTH) {
            return false;
        }
        try {
            int diskID = Integer.valueOf(currentDiskIDSplitStr[2]).intValue();
            int volumeID = Integer.valueOf(currentVolumeIDSplitStr[2]).intValue();
            if (volumeID == diskID + 1 || volumeID == diskID) {
                return HWLOGW_E;
            }
            return false;
        } catch (NumberFormatException e) {
            Log.w(TAG_FLOW, "Exception happend, when get first sd Volume info");
            return false;
        }
    }

    private boolean isSdCardMounted() {
        try {
            VolumeInfo[] vols = IStorageManager.Stub.asInterface(ServiceManager.getService("mount")).getVolumes(0);
            for (VolumeInfo vol : vols) {
                if (vol.getDisk() != null) {
                    if (vol.isMountedWritable() && vol.getDisk().isSd() && isFirstSdVolume(vol)) {
                        return HWLOGW_E;
                    }
                }
            }
        } catch (RemoteException e) {
            Log.w(TAG_FLOW, "Exception happend, when get sd card mounted info");
        }
        return false;
    }

    public boolean needDerivePkgAbi(PackageParser.Package pkg) {
        return false;
    }

    public boolean canAppMoveToPublicSd(VolumeInfo volume) {
        if (isSdInstallEnabled() && volume != null && volume.getDisk() != null && volume.getDisk().isSd() && volume.isMountedWritable() && isFirstSdVolume(volume)) {
            return HWLOGW_E;
        }
        return false;
    }

    public boolean isHwCustHiddenInfoPackage(PackageParser.Package pkgInfo) {
        if (!SECURITY_PACKAGE_ENABLE || !isRestrictedPackage(pkgInfo)) {
            return false;
        }
        return HWLOGW_E;
    }

    private boolean isRestrictedPackage(PackageParser.Package pkgInfo) {
        int uid;
        if (pkgInfo != null && hasPermission(pkgInfo, "android.permission.SECURITY_PACKAGE") && (uid = Binder.getCallingUid()) >= 10000 && uid <= 19999 && uid != this.mSystemUIUid) {
            return HWLOGW_E;
        }
        return false;
    }

    private int getUidByPackageName(String packageName) {
        try {
            return AppGlobals.getPackageManager().getPackageUid(packageName, 1048576, UserHandle.getCallingUserId());
        } catch (SecurityException e) {
            Log.w(TAG_FLOW, "Exception happend, package not found");
            return SYSTEMUI_DEFAULT_UID;
        } catch (Exception e2) {
            Log.w(TAG_FLOW, "Exception happend, when get package uid");
            return SYSTEMUI_DEFAULT_UID;
        }
    }

    private static boolean hasPermission(PackageParser.Package pkgInfo, String perm) {
        Iterator it = pkgInfo.permissions.iterator();
        while (it.hasNext()) {
            if (((PackageParser.Permission) it.next()).info.name.equals(perm)) {
                return HWLOGW_E;
            }
        }
        return false;
    }

    public boolean isSdVol(VolumeInfo vol) {
        if (this.mSdInstallEnable && vol.getDisk() != null && vol.getDisk().isSd() && isFirstSdVolume(vol)) {
            return HWLOGW_E;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class DelPackage {
        private int delFlag;
        private String delPackageName;

        private DelPackage() {
        }

        DelPackage(String name, int flag) {
            this.delPackageName = name;
            this.delFlag = flag;
        }
    }

    public int isListedApp(String packageName) {
        synchronized (this.mLock) {
            if (this.mListedApps.size() == 0) {
                readDelAppsList();
            }
        }
        Iterator<DelPackage> it = this.mListedApps.iterator();
        while (it.hasNext()) {
            DelPackage app = it.next();
            if (packageName.equals(app.delPackageName)) {
                return app.delFlag;
            }
        }
        return -1;
    }

    public boolean isHwFiltReqInstallPerm(String pkgName, String permission) {
        if (FILT_REQ_PERM_ALL && "android.permission.REQUEST_INSTALL_PACKAGES".equals(permission)) {
            return HWLOGW_E;
        }
        if (!FILT_REQ_PERM || !"com.huawei.hidisk".equals(pkgName) || !"android.permission.REQUEST_INSTALL_PACKAGES".equals(permission)) {
            return false;
        }
        return HWLOGW_E;
    }

    private void readDelAppsList() {
        BufferedReader reader = null;
        try {
            File confFile = new File(BOOT_DELETE_CONFIG);
            File cfg = HwCfgFilePolicy.getCfgFile("boot_delete_apps.cfg", 0);
            if (cfg != null) {
                confFile = cfg;
            }
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(confFile), "UTF-8"));
            while (true) {
                String line = reader2.readLine();
                if (line != null) {
                    String[] apps = line.trim().split(",");
                    this.mListedApps.add(new DelPackage(apps[0], Integer.parseInt(apps[1])));
                } else {
                    try {
                        reader2.close();
                        return;
                    } catch (IOException e) {
                        Log.i(TAG, "reader close Exception");
                        return;
                    } catch (Exception e2) {
                        Log.i(TAG, "reader close Exception");
                        return;
                    }
                }
            }
        } catch (NoClassDefFoundError e3) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            if (0 != 0) {
                reader.close();
            }
        } catch (FileNotFoundException e4) {
            Log.i(TAG, "boot_delete_apps.cfg Not Found.");
            if (0 != 0) {
                reader.close();
            }
        } catch (IOException e5) {
            Log.i(TAG, "boot_delete_apps.cfg IOException");
            if (0 != 0) {
                reader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e6) {
                    Log.i(TAG, "reader close Exception");
                } catch (Exception e7) {
                    Log.i(TAG, "reader close Exception");
                }
            }
            throw th;
        }
    }

    public boolean isUnAppInstallAllowed(String originPath, Context context) {
        if (context == null) {
            return false;
        }
        if (isUnInstallerCheck(context) || isUnInstallerValid(originPath, context)) {
            return HWLOGW_E;
        }
        return false;
    }

    private boolean isUnInstallerCheck(Context context) {
        if (Settings.Secure.getInt(context.getContentResolver(), "hw_uninstall_status", 0) != 0) {
            return HWLOGW_E;
        }
        return false;
    }

    private boolean isUnInstallerValid(String originPath, Context context) {
        String whiteInstallerPackages = Settings.Secure.getString(context.getContentResolver(), "hw_installer_whitelist");
        if (whiteInstallerPackages == null || "".equals(whiteInstallerPackages.trim()) || originPath == null) {
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
                ResolveInfo rInfo = rIter.next();
                if (rInfo.activityInfo != null) {
                    String pkgName = rInfo.activityInfo.packageName;
                    if (resolvedType != null && !resolvedType.contains("text/") && justText) {
                        if (("android.intent.action.SEND".equals(action) || "android.intent.action.SEND_MULTIPLE".equals(action)) && "com.android.mms".equals(pkgName)) {
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
        int clipDataLength = clipData.getItemCount();
        for (int i = 0; i < clipDataLength; i++) {
            ClipData.Item item = clipData.getItemAt(i);
            if (!(item == null || (item.getIntent() == null && item.getUri() == null))) {
                return false;
            }
        }
        return HWLOGW_E;
    }

    public String getCustDefaultLauncher(Context context, String pkg) {
        if (TextUtils.isEmpty(CUST_DEFAULT_LAUNCHER)) {
            return null;
        }
        try {
            context.getPackageManager().getPackageInfoAsUser(CUST_DEFAULT_LAUNCHER, 128, UserHandle.getCallingUserId());
            if (!IS_DOCOMO || !SIMPLE_LAUNCHER_PACKAGE_NAME.equals(pkg)) {
                return CUST_DEFAULT_LAUNCHER;
            }
            return pkg;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "there is no this cust launcher in system");
            return null;
        }
    }

    private boolean isDoModel() {
        if (!isMatchMarket()) {
            return false;
        }
        if (parseDoFile(DEVICE_OWNER_XML_LEGACY)) {
            return HWLOGW_E;
        }
        return parseDoFile(DEVICE_OWNER_XML);
    }

    private boolean isMatchMarket() {
        boolean isMarket = SystemProperties.getBoolean("ro.config.apk_not_match_in_do", false);
        Log.d(TAG, "isMarket is " + isMarket);
        return isMarket;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0065, code lost:
        if (r5 == null) goto L_0x00ac;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006b, code lost:
        r5 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006c, code lost:
        r6 = new java.lang.StringBuilder();
     */
    private boolean parseDoFile(String fileName) {
        StringBuilder sb;
        File legacy = new File(Environment.getDataSystemDirectory(), fileName);
        if (!legacy.exists()) {
            return false;
        }
        InputStream input = null;
        try {
            InputStream input2 = new AtomicFile(legacy).openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input2, StandardCharsets.UTF_8.name());
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    break;
                } else if (type == 2) {
                    if (TAG_DEVICE_OWNER.equals(parser.getName())) {
                        if (input2 != null) {
                            try {
                                input2.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Error in parseDoFile: " + e.getMessage());
                            }
                        }
                        return HWLOGW_E;
                    }
                }
            }
        } catch (IOException | XmlPullParserException e2) {
            Log.e(TAG, "Error parsing device-owner file: " + e2.getMessage());
            if (0 != 0) {
                try {
                    input.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    input.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Error in parseDoFile: " + e4.getMessage());
                }
            }
            throw th;
        }
        return false;
        sb.append("Error in parseDoFile: ");
        sb.append(e.getMessage());
        Log.e(TAG, sb.toString());
        return false;
    }
}
