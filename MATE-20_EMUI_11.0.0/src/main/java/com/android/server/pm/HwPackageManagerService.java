package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.HwPackageParser;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.pm.VersionedPackage;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.SplitNotificationUtils;
import android.util.Xml;
import android.view.WindowManagerGlobal;
import com.android.server.LocalServices;
import com.android.server.am.HwActivityManagerService;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.os.HwBootFail;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.auth.HwCertificationManager;
import com.android.server.pm.auth.processor.IProcessor;
import com.huawei.cust.HwCustUtils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.server.HwBasicPlatformFactory;
import com.huawei.server.security.HwServiceSecurityPartsFactoryEx;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwPackageManagerService extends PackageManagerService {
    private static final String CLONE_APP_LIST = "hw_clone_app_list.xml";
    private static final String CLONE_USER_LOG_SUFFIX = "] for clone user ";
    private static final String DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final boolean IS_DEBUG = "on".equals(SystemProperties.get("ro.dbg.pms_log", "0"));
    private static final String KEY_HWPMS_ERROR_LOG = "persist.sys.hwpms_error_log";
    private static final String KEY_HWPMS_ERROR_REBOOT_COUNT = "persist.sys.hwpms_error_reboot_count";
    private static final String[] LIMITED_PACKAGE_NAMES = {"com.huawei.android.totemweather", "com.huawei.camera", "com.android.calendar", "com.huawei.calendar", "com.android.soundrecorder", "com.huawei.soundrecorder"};
    private static final String[] LIMITED_TARGET_PACKAGE_NAMES = {"com.google.android.wearable.app.cn", "com.google.android.wearable.app"};
    private static final int MAX_HWPMS_ERROR_REBOOT_COUNT = 3;
    private static final int MAX_PKG = 100;
    private static final Set<String> SUPPORT_CLONE_APPS = new HashSet();
    private static final String TAG = "HwPackageManagerService";
    public static final int TRANSACTION_CODE_GET_APP_TYPE = 1023;
    public static final int TRANSACTION_CODE_GET_HDB_KEY = 1011;
    public static final int TRANSACTION_CODE_GET_IM_AND_VIDEO_APP_LIST = 1022;
    public static final int TRANSACTION_CODE_IS_NOTIFICATION_SPLIT = 1021;
    public static final int TRANSACTION_CODE_PM_CHECK_GRANTED = 1005;
    public static final int TRANSACTION_CODE_SEND_LIMITED_PACKAGE_BROADCAST = 1006;
    public static final int TRANSACTION_CODE_SET_ENABLED_VISITOR_SETTING = 1001;
    private static HwCustPackageManagerService sCustPackageManagerService = ((HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[0]));
    private static HwPackageManagerService sHwPackageManagerService = null;
    private HandlerThread mCommonHandlerThread = null;
    private ComponentChangeMonitor mComponentChangeMonitor = null;
    private HashSet<String> mGrantedInstalledPkg = new HashSet<>();

    public HwPackageManagerService(Context context, Installer installer, boolean isFactoryTest, boolean isOnlyCore) {
        super(context, installer, isFactoryTest, isOnlyCore);
        getHwPMSEx().recordUninstalledDelapp((String) null, (String) null);
        getHwPMSEx().getOldDataBackup().clear();
        if (!SystemProperties.getBoolean("ro.config.hwcompmonitorthread.disable", false)) {
            this.mCommonHandlerThread = new HandlerThread(TAG);
            this.mCommonHandlerThread.start();
            this.mComponentChangeMonitor = new ComponentChangeMonitor(context, this.mCommonHandlerThread.getLooper());
        }
    }

    public static synchronized PackageManagerService getInstance(Context context, Installer installer, boolean isFactoryTest, boolean isOnlyCore) {
        synchronized (HwPackageManagerService.class) {
            File systemDir = new File(Environment.getDataDirectory(), "system");
            File packagesXml = new File(systemDir, "packages.xml");
            boolean isPackagesXmlExisted = packagesXml.exists();
            File packagesBackupXml = new File(systemDir, "packages-backup.xml");
            boolean isPackagesBackupXmlExisted = packagesBackupXml.exists();
            if (sHwPackageManagerService == null) {
                HwPackageManagerServiceEx.initCustStoppedApps();
                MultiWinWhiteListManager.getInstance().loadMultiWinWhiteList(context);
                initCloneAppsFromCust();
                try {
                    if (SystemProperties.getInt(KEY_HWPMS_ERROR_REBOOT_COUNT, 0) == 0 && (isPackagesXmlExisted || isPackagesBackupXmlExisted)) {
                        SystemProperties.set(KEY_HWPMS_ERROR_REBOOT_COUNT, "0");
                        SystemProperties.set(KEY_HWPMS_ERROR_LOG, "");
                    }
                    sHwPackageManagerService = new HwPackageManagerService(context, installer, isFactoryTest, isOnlyCore);
                    sHwPackageManagerService.deleteNonSupportedAppsForClone();
                    if (HwPackageParser.getIsNeedBootUpdate()) {
                        sHwPackageManagerService.getHwPMSEx().updateWhitelistByHot();
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Error while package manager initializing for Exception");
                    reportPmsInitError(e);
                }
            }
            if (sHwPackageManagerService == null) {
                if (!checkCanRebootForHwPmsError()) {
                    return sHwPackageManagerService;
                }
                if (isPackagesXmlExisted) {
                    boolean isDeleted = packagesXml.delete();
                    Settings.setPackageSettingsError();
                    Slog.e(TAG, "something may be missed in packages.xml, delete the file :" + isDeleted);
                }
                if (isPackagesBackupXmlExisted) {
                    boolean isSucess = packagesBackupXml.delete();
                    Settings.setPackageSettingsError();
                    Slog.e(TAG, "something may be missed in packages_backup.xml, delete the file :" + isSucess);
                }
                int times = SystemProperties.getInt(KEY_HWPMS_ERROR_REBOOT_COUNT, 0);
                StringBuilder sb = new StringBuilder();
                sb.append("repair");
                sb.append(times - 1);
                SystemProperties.set(KEY_HWPMS_ERROR_LOG, sb.toString());
                setBootFailError(times, isPackagesXmlExisted, isPackagesBackupXmlExisted);
            }
            return sHwPackageManagerService;
        }
    }

    private static void setBootFailError(int times, boolean isPackagesXmlExisted, boolean isPackagesBackupXmlExisted) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("packageManagerService load fail,and repair for ");
            sb.append(times - 1);
            sb.append(" times, try to reboot again. isPackagesXmlExisted = ");
            sb.append(isPackagesXmlExisted);
            sb.append(", isPackagesBackupXmlExisted = ");
            sb.append(isPackagesBackupXmlExisted);
            HwBootFail.bootFailError(83886088, 0, sb.toString(), new ArrayList<>(3));
        } catch (Exception e) {
            Slog.e(TAG, "getInstance, try to reboot error");
        }
    }

    private static void reportPmsInitError(Exception ex) {
        int times = SystemProperties.getInt(KEY_HWPMS_ERROR_REBOOT_COUNT, 0);
        StackTraceElement[] frames = ex.getStackTrace();
        String errorLineInfo = "can't get error line info";
        if (frames != null && frames.length > 0) {
            errorLineInfo = frames[0].toString();
        }
        String exceptionName = ex.getClass().toString();
        Slog.i(TAG, "Error while package manager initializing, occur to " + exceptionName + " the error line info:" + errorLineInfo + ", reboot " + times + " times.");
        HwPackageManagerServiceUtils.reportPmsInitException((String) null, times, exceptionName, errorLineInfo);
    }

    private static boolean checkCanRebootForHwPmsError() {
        boolean isReboot = false;
        int times = SystemProperties.getInt(KEY_HWPMS_ERROR_REBOOT_COUNT, 3);
        if (times > 0 && times < 3) {
            isReboot = true;
        }
        SystemProperties.set(KEY_HWPMS_ERROR_REBOOT_COUNT, (times + 1) + "");
        return isReboot;
    }

    private static void resetHwPmsErrorRebootCount() {
        SystemProperties.set(KEY_HWPMS_ERROR_REBOOT_COUNT, "0");
    }

    private void setEnabledVisitorSetting(int newState, int flags, String callingPackage, int userId) {
        Throwable th;
        Integer num;
        boolean z;
        PackageSetting pkgSetting;
        ArrayList<String> components;
        int i = newState;
        int i2 = 1;
        boolean isDisabledState = i == 2 || i == 3 || i == 4;
        if (i == 0 || i == 1 || isDisabledState) {
            String callingUid = callingPackage;
            if (callingUid == null) {
                callingUid = Integer.toString(Binder.getCallingUid());
            }
            HashMap<String, ArrayList<String>> componentsMap = new HashMap<>();
            HashMap<String, Integer> pkgMap = new HashMap<>();
            String pkgNameList = Settings.Secure.getString(this.mContext.getContentResolver(), "privacy_app_list");
            if (pkgNameList == null) {
                Slog.e(TAG, " pkgNameList = null ");
            } else if (TextUtils.isEmpty(pkgNameList)) {
                Slog.e(TAG, " pkgNameList is null");
            } else {
                String[] pkgNameArray = pkgNameList.contains(AwarenessInnerConstants.SEMI_COLON_KEY) ? pkgNameList.split(AwarenessInnerConstants.SEMI_COLON_KEY) : new String[]{pkgNameList};
                boolean isSendNow = false;
                String callingUid2 = callingUid;
                int i3 = 0;
                while (i3 < 100 && pkgNameArray != null && i3 < pkgNameArray.length) {
                    String packageName = pkgNameArray[i3];
                    String componentName = packageName;
                    synchronized (this.mPackages) {
                        try {
                            PackageSetting pkgSetting2 = (PackageSetting) this.mSettings.mPackages.get(packageName);
                            if (pkgSetting2 == null) {
                                try {
                                    pkgMap.put(packageName, i2);
                                } catch (Throwable th2) {
                                    th = th2;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th3) {
                                            th = th3;
                                        }
                                    }
                                    throw th;
                                }
                            } else {
                                try {
                                    if (pkgSetting2.getEnabled(userId) == i) {
                                        pkgMap.put(packageName, i2);
                                    } else {
                                        if (i == 0 || i == 1) {
                                            callingUid2 = null;
                                        }
                                        pkgSetting2.setEnabled(i, userId, callingUid2);
                                        z = false;
                                        pkgMap.put(packageName, 0);
                                        ArrayList<String> components2 = this.mPendingBroadcasts.get(userId, packageName);
                                        boolean isNewPackage = components2 == null;
                                        if (isNewPackage) {
                                            components = new ArrayList<>();
                                        } else {
                                            components = components2;
                                        }
                                        num = i2;
                                        try {
                                            if (!components.contains(componentName)) {
                                                try {
                                                    components.add(componentName);
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                }
                                            }
                                            componentsMap.put(packageName, components);
                                            if ((flags & 1) == 0) {
                                                isSendNow = true;
                                                this.mPendingBroadcasts.remove(userId, packageName);
                                                componentName = componentName;
                                                pkgSetting = pkgSetting2;
                                            } else {
                                                if (isNewPackage) {
                                                    this.mPendingBroadcasts.put(userId, packageName, components);
                                                }
                                                try {
                                                    if (!this.mHandler.hasMessages(1)) {
                                                        componentName = componentName;
                                                        pkgSetting = pkgSetting2;
                                                        try {
                                                            this.mHandler.sendEmptyMessageDelayed(1, 1000);
                                                        } catch (Throwable th5) {
                                                            th = th5;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else {
                                                        componentName = componentName;
                                                        pkgSetting = pkgSetting2;
                                                    }
                                                } catch (Throwable th6) {
                                                    th = th6;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            }
                                        } catch (Throwable th7) {
                                            th = th7;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    }
                                } catch (Throwable th8) {
                                    th = th8;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            }
                            num = i2;
                            pkgSetting = pkgSetting2;
                            z = false;
                            i3++;
                            i = newState;
                            isDisabledState = isDisabledState;
                            i2 = num;
                        } catch (Throwable th9) {
                            th = th9;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    i3++;
                    i = newState;
                    isDisabledState = isDisabledState;
                    i2 = num;
                }
                this.mSettings.writePackageRestrictionsLPr(userId);
                int i4 = 0;
                while (i4 < 100 && pkgNameArray != null && i4 < pkgNameArray.length) {
                    String packageName2 = pkgNameArray[i4];
                    if (pkgMap.get(packageName2).intValue() != 1) {
                        PackageSetting pkgSetting3 = (PackageSetting) this.mSettings.mPackages.get(packageName2);
                        if (pkgSetting3 != null && componentsMap.get(packageName2) != null && isSendNow) {
                            sendPackageChangedBroadcast(packageName2, (flags & 1) != 0, componentsMap.get(packageName2), UserHandle.getUid(userId, pkgSetting3.appId));
                        }
                    }
                    i4++;
                }
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 1001) {
            setVisitorSetting(data, reply);
            return true;
        } else if (code == 1011) {
            getHdbKey(data, reply);
            return true;
        } else if (code == 1005) {
            checkGranted(data, reply);
            return true;
        } else if (code != 1006) {
            switch (code) {
                case 1021:
                    isNotificationSplit(data, reply);
                    return true;
                case 1022:
                    getImAndVideoAppList(data, reply);
                    return true;
                case 1023:
                    data.enforceInterface(DESCRIPTOR);
                    Optional<int[]> appTypeOptional = getApplicationType(data);
                    if (!appTypeOptional.isPresent()) {
                        return false;
                    }
                    reply.writeNoException();
                    reply.writeIntArray(appTypeOptional.get());
                    return true;
                default:
                    return HwPackageManagerService.super.onTransact(code, data, reply, flags);
            }
        } else {
            data.enforceInterface(DESCRIPTOR);
            sendLimitedPackageBroadcast(data.readString(), data.readString(), data.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(data) : null, data.readString(), data.createIntArray());
            reply.writeNoException();
            return true;
        }
    }

    private void setVisitorSetting(Parcel data, Parcel reply) {
        Slog.w(TAG, "onTransact");
        data.enforceInterface(DESCRIPTOR);
        setEnabledVisitorSetting(data.readInt(), data.readInt(), null, data.readInt());
        reply.writeNoException();
    }

    private void checkGranted(Parcel data, Parcel reply) {
        data.enforceInterface(DESCRIPTOR);
        boolean checkInstallGranted = checkInstallGranted(data.readString());
        reply.writeNoException();
        reply.writeInt(checkInstallGranted ? 1 : 0);
    }

    private void getHdbKey(Parcel data, Parcel reply) {
        data.enforceInterface(DESCRIPTOR);
        reply.writeNoException();
        reply.writeString(HwAdbManager.getHdbKey());
    }

    private void isNotificationSplit(Parcel data, Parcel reply) {
        data.enforceInterface(DESCRIPTOR);
        boolean isNotificationAddSplitButton = isNotificationAddSplitButton(data.readString());
        reply.writeNoException();
        reply.writeInt(isNotificationAddSplitButton ? 1 : 0);
    }

    private void getImAndVideoAppList(Parcel data, Parcel reply) {
        data.enforceInterface(DESCRIPTOR);
        List<String> list = getSupportSplitScreenApps();
        reply.writeNoException();
        reply.writeStringList(list);
    }

    public void systemReady() {
        HwPackageManagerService.super.systemReady();
        getHwPMSEx().writePreinstalledApkListToFile();
        getHwPMSEx().createPublicityFile();
        HotInstall.getInstance().registInstallCallBack();
        if (!TextUtils.isEmpty(SystemProperties.get("ro.config.hw_notch_size", ""))) {
            for (PackageSetting ps : this.mSettings.mPackages.values()) {
                if (ps.getAppUseNotchMode() > 0) {
                    HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwNotchScreenWhiteConfig().updateAppUseNotchMode(ps.pkg.packageName, ps.getAppUseNotchMode());
                }
            }
        }
        resetHwPmsErrorRebootCount();
        HwServiceSecurityPartsFactoryEx.getInstance().getHwAppAuthManager().notifyPMSReady(this.mContext);
        HwForceDarkModeConfig.getInstance().registerAppTypeRecoReceiver();
    }

    /* access modifiers changed from: protected */
    public void checkHwCertification(PackageParser.Package pkg, boolean isUpdate) {
        if (HwCertificationManager.hasFeature()) {
            if (!HwCertificationManager.isSupportHwCertification(pkg)) {
                if (isContainHwCertification(pkg)) {
                    hwCertCleanUp(pkg);
                }
            } else if (isUpdate || !isContainHwCertification(pkg) || isUpgrade()) {
                Slog.i(IProcessor.TAG, "will checkCertificationInner,isUpdate = " + isUpdate + "isHotaUpGrade = " + isUpgrade());
                hwCertCleanUp(pkg);
                checkCertificationInner(pkg);
            }
        }
    }

    private void checkCertificationInner(PackageParser.Package pkg) {
        HwCertificationManager manager = HwCertificationManager.getInstance();
        if (manager != null && !manager.checkHwCertification(pkg)) {
            Slog.e(IProcessor.TAG, "checkHwCertification parse error");
        }
    }

    /* access modifiers changed from: protected */
    public boolean getHwCertificationPermission(boolean isAllowed, PackageParser.Package pkg, String perm) {
        if (!HwCertificationManager.hasFeature()) {
            return isAllowed;
        }
        if (!HwCertificationManager.isInitialized()) {
            HwCertificationManager.initialize(this.mContext);
        }
        HwCertificationManager manager = HwCertificationManager.getInstance();
        if (manager == null) {
            return isAllowed;
        }
        return manager.getHwCertificationPermission(isAllowed, pkg, perm);
    }

    private void hwCertCleanUp(PackageParser.Package pkg) {
        HwCertificationManager manager = HwCertificationManager.getInstance();
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
            HwCertificationManager manager = HwCertificationManager.getInstance();
            if (manager != null) {
                manager.cleanUp();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void installStage(PackageManagerService.ActiveInstallSession activeInstallSession) {
        if (activeInstallSession.mUser != null) {
            activeInstallSession.mUser = new UserHandle(redirectInstallForClone(activeInstallSession.mUser.getIdentifier()));
        }
        HwPackageManagerService.super.installStage(activeInstallSession);
    }

    /* access modifiers changed from: protected */
    public void initHwCertificationManager() {
        if (!HwCertificationManager.isInitialized()) {
            HwCertificationManager.initialize(this.mContext);
        }
        HwCertificationManager.getInstance();
    }

    /* access modifiers changed from: protected */
    public int getHwCertificateType(PackageParser.Package pkg) {
        if (!HwCertificationManager.isSupportHwCertification(pkg)) {
            return HwCertificationManager.getInstance().getHwCertificateTypeNotMdm();
        }
        return HwCertificationManager.getInstance().getHwCertificateType(pkg.packageName);
    }

    /* access modifiers changed from: protected */
    public boolean isContainHwCertification(PackageParser.Package pkg) {
        return HwCertificationManager.getInstance().isContainHwCertification(pkg.packageName);
    }

    /* access modifiers changed from: protected */
    public void addGrantedInstalledPkg(String pkgName, boolean isGrant) {
        if (isGrant) {
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

    private boolean checkLimitePackageBroadcast(String action, String pkg, String targetPkg) {
        String[] callingPkgNames = getPackagesForUid(Binder.getCallingUid());
        if (callingPkgNames == null || callingPkgNames.length <= 0) {
            Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "Wear-checkLimitePackageBroadcast: callingPkgNames is empty");
            return false;
        }
        String callingPkgName = callingPkgNames[0];
        Flog.d((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "Wear-checkLimitePackageBroadcast: callingPkgName = " + callingPkgName);
        if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action)) {
            boolean isTargetPkgExist = false;
            String[] strArr = LIMITED_TARGET_PACKAGE_NAMES;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (strArr[i].equals(targetPkg)) {
                    isTargetPkgExist = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!isTargetPkgExist) {
                Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "Wear-checkLimitePackageBroadcast: targetPkg is not permitted");
                return false;
            }
            boolean isPkgExist = false;
            String[] strArr2 = LIMITED_PACKAGE_NAMES;
            int length2 = strArr2.length;
            int i2 = 0;
            while (true) {
                if (i2 >= length2) {
                    break;
                } else if (strArr2[i2].equals(pkg)) {
                    isPkgExist = true;
                    break;
                } else {
                    i2++;
                }
            }
            if (!isPkgExist) {
                Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "Wear-checkLimitePackageBroadcast: pkg is not permitted");
                return false;
            } else if (!isSystemApp(getApplicationInfo(callingPkgName, 0, this.mContext.getUserId()))) {
                Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "Wear-checkLimitePackageBroadcast: is not System App.");
                return false;
            } else {
                Flog.d((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "Wear-checkLimitePackageBroadcast: success");
                return true;
            }
        } else {
            Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "Wear-checkLimitePackageBroadcast: action is not permitted");
            return false;
        }
    }

    private boolean isSystemApp(ApplicationInfo appInfo) {
        boolean isSystemApp = false;
        if (appInfo != null) {
            boolean z = true;
            if ((appInfo.flags & 1) == 0) {
                z = false;
            }
            isSystemApp = z;
        }
        Flog.d((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "Wear-checkLimitePackageBroadcast: isSystemApp=" + isSystemApp);
        return isSystemApp;
    }

    private void sendLimitedPackageBroadcast(final String action, final String pkg, final Bundle extras, final String targetPkg, final int[] userIds) {
        if (checkLimitePackageBroadcast(action, pkg, targetPkg)) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.pm.HwPackageManagerService.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    int[] resolvedUserIds;
                    try {
                        IActivityManager am = ActivityManagerNative.getDefault();
                        if (am != null) {
                            if (userIds == null) {
                                resolvedUserIds = am.getRunningUserIds();
                            } else {
                                resolvedUserIds = userIds;
                            }
                            int length = resolvedUserIds.length;
                            int i = 0;
                            while (i < length) {
                                int id = resolvedUserIds[i];
                                String str = action;
                                Uri uri = null;
                                if (pkg != null) {
                                    uri = Uri.fromParts("package", pkg, null);
                                }
                                Intent intent = new Intent(str, uri);
                                if (extras != null) {
                                    intent.putExtras(extras);
                                }
                                if (targetPkg != null) {
                                    intent.setPackage(targetPkg);
                                }
                                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                                if (uid > 0 && UserHandle.getUserId(uid) != id) {
                                    intent.putExtra("android.intent.extra.UID", UserHandle.getUid(id, UserHandle.getAppId(uid)));
                                }
                                intent.putExtra("android.intent.extra.user_handle", id);
                                am.broadcastIntent((IApplicationThread) null, intent, (String) null, (IIntentReceiver) null, 0, (String) null, (Bundle) null, (String[]) null, -1, (Bundle) null, false, false, id);
                                i++;
                                length = length;
                                resolvedUserIds = resolvedUserIds;
                            }
                        }
                    } catch (RemoteException e) {
                        Slog.e(HwPackageManagerService.TAG, "fail to send limited package broadcast");
                    }
                }
            });
            return;
        }
        throw new SecurityException("sendLimitedPackageBroadcast: checkLimitePackageBroadcast failed");
    }

    public int checkPermission(String permName, String pkgName, int userId) {
        boolean isSupportCloneApp = HwActivityManagerService.IS_SUPPORT_CLONE_APP && ("android.permission.INTERACT_ACROSS_USERS_FULL".equals(permName) || "android.permission.INTERACT_ACROSS_USERS".equals(permName));
        if (userId == 0 || !isSupportCloneApp || !sUserManager.isClonedProfile(userId)) {
            return HwPackageManagerService.super.checkPermission(permName, pkgName, userId);
        }
        return 0;
    }

    public int checkUidPermission(String permName, int uid) {
        boolean isSupportCloneApp = HwActivityManagerService.IS_SUPPORT_CLONE_APP && ("android.permission.INTERACT_ACROSS_USERS_FULL".equals(permName) || "android.permission.INTERACT_ACROSS_USERS".equals(permName));
        if (UserHandle.getUserId(uid) == 0 || !isSupportCloneApp || !sUserManager.isClonedProfile(UserHandle.getUserId(uid))) {
            return HwPackageManagerService.super.checkUidPermission(permName, uid);
        }
        return 0;
    }

    /* JADX INFO: Multiple debug info for r3v4 java.lang.String: [D('disabledComponent' java.lang.String[]), D('proc' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r3v5 android.content.Intent: [D('procIntent' android.content.Intent), D('proc' java.lang.String)] */
    /* access modifiers changed from: protected */
    public void deleteNonRequiredAppsForClone(int clonedProfileUserId, boolean isFirstCreate) {
        int i;
        Throwable th;
        Intent procIntent;
        String[] disabledComponent = this.mContext.getResources().getStringArray(33816597);
        int length = disabledComponent.length;
        boolean z = false;
        boolean isShouldUpdate = false;
        ComponentName component = null;
        int i2 = 0;
        while (true) {
            i = 1;
            if (i2 >= length) {
                break;
            }
            String[] componentArray = disabledComponent[i2].split("/");
            if (componentArray != null && componentArray.length == 2) {
                component = new ComponentName(componentArray[0], componentArray[1]);
                try {
                    if (getComponentEnabledSetting(component, clonedProfileUserId) != 2) {
                        setComponentEnabledSetting(component, 2, 1, clonedProfileUserId);
                        isShouldUpdate = true;
                    }
                } catch (IllegalArgumentException | SecurityException e) {
                    Slog.d(TAG, "deleteNonRequiredComponentsForClone exception:" + e.getMessage());
                }
            }
            i2++;
        }
        String[] requiredAppsList = this.mContext.getResources().getStringArray(33816586);
        Set<String> requiredAppsSet = new HashSet<>(Arrays.asList(requiredAppsList));
        UserInfo ui = sUserManager.getUserInfo(clonedProfileUserId);
        synchronized (this.mPackages) {
            try {
                for (Map.Entry<String, PackageSetting> entry : this.mSettings.mPackages.entrySet()) {
                    try {
                        if (!isFirstCreate) {
                            if (!isSupportCloneAppInCust(entry.getKey()) && !requiredAppsSet.contains(entry.getKey())) {
                                entry.getValue().setInstalled(z, clonedProfileUserId);
                                isShouldUpdate = true;
                                Slog.i(TAG, "Deleting non supported package [" + entry.getKey() + CLONE_USER_LOG_SUFFIX + clonedProfileUserId);
                            } else if (requiredAppsSet.contains(entry.getKey()) && entry.getValue().getInstalled(ui.profileGroupId)) {
                                if (!entry.getValue().getInstalled(clonedProfileUserId)) {
                                    entry.getValue().setInstalled(true, clonedProfileUserId);
                                    isShouldUpdate = true;
                                    Slog.i(TAG, "Adding required package [" + entry.getKey() + CLONE_USER_LOG_SUFFIX + clonedProfileUserId);
                                }
                                if (!entry.getValue().isSystem()) {
                                    try {
                                        setComponentEnabledSetting(new ComponentName(entry.getKey(), PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME), 2, 1, clonedProfileUserId);
                                    } catch (IllegalArgumentException | SecurityException e2) {
                                        Slog.d(TAG, "Disable details activity exception:" + e2.getMessage());
                                    }
                                    Slog.i(TAG, "Disable details activity [" + entry.getKey() + CLONE_USER_LOG_SUFFIX + clonedProfileUserId);
                                }
                            }
                            z = false;
                        } else if (!requiredAppsSet.contains(entry.getKey())) {
                            entry.getValue().setInstalled(z, clonedProfileUserId);
                            isShouldUpdate = true;
                            Slog.i(TAG, "Deleting non supported package [" + entry.getKey() + CLONE_USER_LOG_SUFFIX + clonedProfileUserId);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        throw th;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                while (true) {
                    break;
                }
                throw th;
            }
        }
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        launcherIntent.addCategory("android.intent.category.LAUNCHER");
        ParceledListSlice<ResolveInfo> launcherList = queryIntentActivities(launcherIntent, launcherIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786432, clonedProfileUserId);
        if (launcherList != null) {
            for (ResolveInfo resolveInfo : launcherList.getList()) {
                component = resolveInfo.activityInfo.getComponentName();
                if (requiredAppsSet.contains(resolveInfo.activityInfo.packageName)) {
                    setComponentEnabledSetting(component, 2, 1, clonedProfileUserId);
                    isShouldUpdate = true;
                    Slog.i(TAG, "Disable [" + component + CLONE_USER_LOG_SUFFIX + clonedProfileUserId);
                }
            }
        }
        Intent homeIntent = new Intent("android.intent.action.MAIN");
        homeIntent.addCategory("android.intent.category.HOME");
        ParceledListSlice<ResolveInfo> homeList = queryIntentActivities(homeIntent, homeIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786432, clonedProfileUserId);
        if (homeList != null) {
            for (ResolveInfo resolveInfo2 : homeList.getList()) {
                component = resolveInfo2.activityInfo.getComponentName();
                if (requiredAppsSet.contains(resolveInfo2.activityInfo.packageName)) {
                    setComponentEnabledSetting(component, 2, i, clonedProfileUserId);
                    isShouldUpdate = true;
                    Slog.i(TAG, "Disable [" + component + CLONE_USER_LOG_SUFFIX + clonedProfileUserId);
                }
                i = 1;
            }
        }
        String[] procList = this.mContext.getResources().getStringArray(33816598);
        Intent procIntent2 = null;
        int length2 = procList.length;
        int i3 = 0;
        while (i3 < length2) {
            Intent procIntent3 = new Intent(procList[i3]);
            ParceledListSlice<ResolveInfo> parceledList = queryIntentReceivers(procIntent3, procIntent3.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786432, clonedProfileUserId);
            if (parceledList != null) {
                for (ResolveInfo resolveInfo3 : parceledList.getList()) {
                    ComponentName component2 = resolveInfo3.activityInfo.getComponentName();
                    if (component2 == null || isSupportCloneAppInCust(resolveInfo3.activityInfo.packageName)) {
                        procIntent = procIntent3;
                    } else {
                        procIntent = procIntent3;
                        try {
                            setComponentEnabledSetting(component2, 2, 1, clonedProfileUserId);
                            Slog.i(TAG, "disableReceiversForClone package [" + component2 + "] for user " + clonedProfileUserId);
                        } catch (Exception e3) {
                            Slog.e(TAG, "disableReceiversForClone Exception");
                        }
                    }
                    procIntent3 = procIntent;
                }
            }
            i3++;
            disabledComponent = disabledComponent;
            requiredAppsList = requiredAppsList;
            procIntent2 = procIntent3;
        }
        if (isShouldUpdate) {
            scheduleWritePackageRestrictionsLocked(clonedProfileUserId);
        }
    }

    public void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId, int deleteFlags) {
        PackageSetting pkgSetting;
        PackageParser.Package deletePackage;
        int tmpDeleteFlags = deleteFlags;
        boolean isSupportCloneApp = HwActivityManagerService.IS_SUPPORT_CLONE_APP && versionedPackage != null && SUPPORT_CLONE_APPS.contains(versionedPackage.getPackageName());
        boolean isCloneProfileUser = userId != 0 && sUserManager.isClonedProfile(userId);
        if ((tmpDeleteFlags & 2) == 0 && isSupportCloneApp && isCloneProfileUser && (deletePackage = (PackageParser.Package) this.mPackages.get(versionedPackage.getPackageName())) != null && (1 & deletePackage.applicationInfo.flags) != 0) {
            tmpDeleteFlags |= 4;
        }
        if (versionedPackage != null) {
            HwPackageManagerService.super.deletePackageVersioned(versionedPackage, observer, userId, tmpDeleteFlags);
        }
        if ((tmpDeleteFlags & 2) == 0 && isSupportCloneApp) {
            long ident = Binder.clearCallingIdentity();
            try {
                for (UserInfo ui : sUserManager.getProfiles(userId, false)) {
                    if (ui.isClonedProfile() && ui.id != userId && ui.profileGroupId == userId && (pkgSetting = (PackageSetting) this.mSettings.mPackages.get(versionedPackage.getPackageName())) != null && pkgSetting.getInstalled(ui.id)) {
                        HwPackageManagerService.super.deletePackageVersioned(versionedPackage, observer, ui.id, tmpDeleteFlags);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void deleteClonedProfileIfNeed(int[] removedUsers) {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && removedUsers != null && removedUsers.length > 0) {
            for (int userId : removedUsers) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    UserInfo userInfo = sUserManager.getUserInfo(userId);
                    if (userInfo == null || !userInfo.isClonedProfile() || isAnyApkInstalledInClonedProfile(userId)) {
                        Binder.restoreCallingIdentity(callingId);
                    } else {
                        sUserManager.removeUser(userId);
                        Slog.i(TAG, "Remove cloned profile " + userId);
                        Intent clonedProfileIntent = new Intent("android.intent.action.USER_REMOVED");
                        clonedProfileIntent.setPackage("com.huawei.android.launcher");
                        clonedProfileIntent.addFlags(1342177280);
                        clonedProfileIntent.putExtra("android.intent.extra.USER", new UserHandle(userId));
                        clonedProfileIntent.putExtra("android.intent.extra.user_handle", userId);
                        this.mContext.sendBroadcastAsUser(clonedProfileIntent, new UserHandle(userInfo.profileGroupId), null);
                        return;
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
    }

    private boolean isAnyApkInstalledInClonedProfile(int clonedProfileUserId) {
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        launcherIntent.addCategory("android.intent.category.LAUNCHER");
        return queryIntentActivities(launcherIntent, launcherIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786432, clonedProfileUserId).getList().size() > 0;
    }

    private int redirectInstallForClone(int userId) {
        if (userId == 0 || !HwActivityManagerService.IS_SUPPORT_CLONE_APP) {
            return userId;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            UserInfo ui = sUserManager.getUserInfo(userId);
            if (ui != null && ui.isClonedProfile()) {
                return ui.profileGroupId;
            }
            Binder.restoreCallingIdentity(ident);
            return userId;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void sendPackageBroadcast(String action, String pkg, Bundle extras, int flags, String targetPkg, IIntentReceiver finishedReceiver, int[] userIds, int[] instantUserIds) {
        int i = 0;
        boolean isSupportCloneApp = HwActivityManagerService.IS_SUPPORT_CLONE_APP && getUserManagerInternal().hasClonedProfile();
        boolean isPackageChangedOrAdded = "android.intent.action.PACKAGE_ADDED".equals(action) || ("android.intent.action.PACKAGE_CHANGED".equals(action) && userIds != null);
        if (isSupportCloneApp && isPackageChangedOrAdded && !SUPPORT_CLONE_APPS.contains(pkg)) {
            long callingId = Binder.clearCallingIdentity();
            int cloneUserId = -1;
            if (userIds != null) {
                int length = userIds.length;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    int userId = userIds[i];
                    if (userId != 0 && sUserManager.isClonedProfile(userId)) {
                        cloneUserId = userId;
                        break;
                    }
                    i++;
                }
            } else {
                try {
                    cloneUserId = getUserManagerInternal().findClonedProfile().id;
                } catch (Exception e) {
                    Slog.e(TAG, "Set required Apps' component disabled failed");
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                    throw th;
                }
            }
            Intent launcherIntent = new Intent("android.intent.action.MAIN");
            launcherIntent.addCategory("android.intent.category.LAUNCHER");
            launcherIntent.setPackage(pkg);
            ParceledListSlice<ResolveInfo> parceledList = queryIntentActivities(launcherIntent, launcherIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786432, cloneUserId);
            if (parceledList != null) {
                for (ResolveInfo resolveInfo : parceledList.getList()) {
                    setComponentEnabledSetting(resolveInfo.activityInfo.getComponentName(), 2, 1, cloneUserId);
                }
            }
            Binder.restoreCallingIdentity(callingId);
        }
        HwPackageManagerService.super.sendPackageBroadcast(action, pkg, extras, flags, targetPkg, finishedReceiver, userIds, instantUserIds);
    }

    private static void initCloneAppsFromCust() {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP) {
            File configFile = HwPackageManagerUtils.getCustomizedFileName(CLONE_APP_LIST, 0);
            if (configFile == null || !configFile.exists()) {
                Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "hw_clone_app_list.xml does not exists.");
                return;
            }
            InputStream inputStream = null;
            try {
                InputStream inputStream2 = new FileInputStream(configFile);
                XmlPullParser xmlParser = Xml.newPullParser();
                xmlParser.setInput(inputStream2, null);
                while (true) {
                    int xmlEventType = xmlParser.next();
                    if (xmlEventType == 1) {
                        try {
                            inputStream2.close();
                            return;
                        } catch (IOException e) {
                            Slog.e(TAG, "initCloneAppsFromCust:- IOE while closing stream");
                            return;
                        }
                    } else if (xmlEventType == 2 && "package".equals(xmlParser.getName())) {
                        String packageName = xmlParser.getAttributeValue(null, "name");
                        if (!TextUtils.isEmpty(packageName)) {
                            SUPPORT_CLONE_APPS.add(packageName.intern());
                        }
                    }
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "initCloneAppsFromCust read error");
                if (0 != 0) {
                    inputStream.close();
                }
            } catch (XmlPullParserException e3) {
                Log.e(TAG, "initCloneAppsFromCust parse xml fail ");
                if (0 != 0) {
                    inputStream.close();
                }
            } catch (IOException e4) {
                Log.e(TAG, "initCloneAppsFromCust ");
                if (0 != 0) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Slog.e(TAG, "initCloneAppsFromCust:- IOE while closing stream");
                    }
                }
                throw th;
            }
        }
    }

    public static boolean isSupportCloneAppInCust(String packageName) {
        return SUPPORT_CLONE_APPS.contains(packageName);
    }

    private void deleteNonSupportedAppsForClone() {
        long callingId = Binder.clearCallingIdentity();
        try {
            Iterator<UserInfo> it = sUserManager.getUsers(false).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                UserInfo ui = it.next();
                if (ui.isClonedProfile()) {
                    deleteNonRequiredAppsForClone(ui.id, false);
                    break;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private int updateFlagsForClone(int flags, int userId) {
        if (!HwActivityManagerService.IS_SUPPORT_CLONE_APP || userId == 0 || !getUserManagerInternal().isClonedProfile(userId)) {
            return flags;
        }
        int callingUid = Binder.getCallingUid();
        if (userId != UserHandle.getUserId(callingUid) || !SUPPORT_CLONE_APPS.contains(getNameForUid(callingUid))) {
            return flags;
        }
        return flags | 4202496;
    }

    /* access modifiers changed from: protected */
    public List<ResolveInfo> queryIntentActivitiesInternal(Intent intent, String resolvedType, int flags, int filterCallingUid, int userId, boolean isResolveForStart, boolean isAllowDynamicSplits) {
        boolean shouldCheckUninstall;
        int tempFlags = flags;
        if (!HwActivityManagerService.IS_SUPPORT_CLONE_APP || userId == 0) {
            return HwPackageManagerService.super.queryIntentActivitiesInternal(intent, resolvedType, tempFlags, filterCallingUid, userId, isResolveForStart, isAllowDynamicSplits);
        }
        int callingUid = Binder.getCallingUid();
        UserInfo ui = getUserManagerInternal().getUserInfo(userId);
        if (ui == null || !ui.isClonedProfile() || userId != UserHandle.getUserId(callingUid)) {
            return HwPackageManagerService.super.queryIntentActivitiesInternal(intent, resolvedType, tempFlags, filterCallingUid, userId, isResolveForStart, isAllowDynamicSplits);
        }
        boolean shouldCheckUninstall2 = (tempFlags & 4202496) != 0 && UserHandle.getAppId(callingUid) == 1000;
        if (SUPPORT_CLONE_APPS.contains(getNameForUid(callingUid))) {
            if ((tempFlags & 4202496) == 0) {
                shouldCheckUninstall2 = true;
            }
            tempFlags = 4202496 | tempFlags;
            shouldCheckUninstall = shouldCheckUninstall2;
        } else {
            shouldCheckUninstall = shouldCheckUninstall2;
        }
        List<ResolveInfo> result = HwPackageManagerService.super.queryIntentActivitiesInternal(intent, resolvedType, tempFlags, filterCallingUid, userId, isResolveForStart, isAllowDynamicSplits);
        if (!shouldCheckUninstall) {
            return result;
        }
        Iterator<ResolveInfo> iterator = result.iterator();
        while (iterator.hasNext()) {
            ResolveInfo ri = iterator.next();
            if (!this.mSettings.isEnabledAndMatchLPr(ri.activityInfo, 786432, ui.profileGroupId) && !this.mSettings.isEnabledAndMatchLPr(ri.activityInfo, 786432, userId)) {
                iterator.remove();
            }
        }
        return result;
    }

    public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        int tempFlags = flags;
        if (!HwActivityManagerService.IS_SUPPORT_CLONE_APP || userId == 0) {
            return HwPackageManagerService.super.getActivityInfo(component, tempFlags, userId);
        }
        int callingUid = Binder.getCallingUid();
        UserInfo ui = getUserManagerInternal().getUserInfo(userId);
        if (ui == null || !ui.isClonedProfile() || userId != UserHandle.getUserId(callingUid)) {
            return HwPackageManagerService.super.getActivityInfo(component, tempFlags, userId);
        }
        boolean shouldCheckUninstall = (tempFlags & 4202496) != 0 && UserHandle.getAppId(callingUid) == 1000;
        if (SUPPORT_CLONE_APPS.contains(getNameForUid(callingUid))) {
            if ((tempFlags & 4202496) == 0) {
                shouldCheckUninstall = true;
            }
            tempFlags |= 4202496;
        }
        ActivityInfo ai = HwPackageManagerService.super.getActivityInfo(component, tempFlags, userId);
        if (!shouldCheckUninstall || ai == null || this.mSettings.isEnabledAndMatchLPr(ai, 786432, ui.profileGroupId) || this.mSettings.isEnabledAndMatchLPr(ai, 786432, userId)) {
            return ai;
        }
        return null;
    }

    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        return HwPackageManagerService.super.getPackageInfo(packageName, updateFlagsForClone(flags, userId), userId);
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        return HwPackageManagerService.super.getApplicationInfo(packageName, updateFlagsForClone(flags, userId), userId);
    }

    public boolean isPackageAvailable(String packageName, int userId) {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && userId != 0) {
            int callingUid = Binder.getCallingUid();
            if (userId == UserHandle.getUserId(callingUid)) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    UserInfo ui = sUserManager.getUserInfo(userId);
                    if (ui.isClonedProfile() && SUPPORT_CLONE_APPS.contains(getNameForUid(callingUid))) {
                        return HwPackageManagerService.super.isPackageAvailable(packageName, ui.profileGroupId);
                    }
                    Binder.restoreCallingIdentity(callingId);
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
        return HwPackageManagerService.super.isPackageAvailable(packageName, userId);
    }

    public ParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId) {
        return HwPackageManagerService.super.getInstalledPackages(updateFlagsForClone(flags, userId), userId);
    }

    public ParceledListSlice<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        return HwPackageManagerService.super.getInstalledApplications(updateFlagsForClone(flags, userId), userId);
    }

    public int installExistingPackageAsUser(String packageName, int userId, int installFlags, int installReason, List<String> whiteListedPermissions) {
        if (userId != 0 && SUPPORT_CLONE_APPS.contains(packageName) && getUserManagerInternal().isClonedProfile(userId)) {
            long callingId = Binder.clearCallingIdentity();
            try {
                setPackageStoppedState(packageName, true, userId);
                Slog.d(TAG, packageName + " is set stopped for user " + userId);
            } catch (IllegalArgumentException e) {
                Slog.w(TAG, "error in setPackageStoppedState for " + e.getMessage());
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
                throw th;
            }
            Binder.restoreCallingIdentity(callingId);
        }
        return HwPackageManagerService.super.installExistingPackageAsUser(packageName, userId, installFlags, installReason, whiteListedPermissions);
    }

    public ProviderInfo resolveContentProvider(String name, int flags, int userId) {
        return HwPackageManagerService.super.resolveContentProvider(name, flags, ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).handleUserForClone(name, userId));
    }

    public List<String> getSupportSplitScreenApps() {
        List<String> list = new ArrayList<>();
        list.addAll(SplitNotificationUtils.getInstance(this.mContext).getListPkgName(2));
        list.addAll(SplitNotificationUtils.getInstance(this.mContext).getListPkgName(1));
        return list;
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags, int userId) {
        if (componentName != null) {
            boolean isEnabledOrDefault = true;
            if (!(newState == 0 || newState == 1)) {
                isEnabledOrDefault = false;
            }
            if (!HwActivityManagerService.IS_SUPPORT_CLONE_APP || userId == 0 || !isEnabledOrDefault || SUPPORT_CLONE_APPS.contains(componentName.getPackageName())) {
                setComponentEnabledSettingSuper(componentName, newState, flags, userId);
            }
            long callingId = Binder.clearCallingIdentity();
            if (!sUserManager.isClonedProfile(userId)) {
                Binder.restoreCallingIdentity(callingId);
                setComponentEnabledSettingSuper(componentName, newState, flags, userId);
                return;
            }
            try {
                if (new HashSet<>(Arrays.asList(this.mContext.getResources().getStringArray(33816586))).contains(componentName.getPackageName())) {
                    Intent launcherIntent = new Intent("android.intent.action.MAIN");
                    launcherIntent.addCategory("android.intent.category.LAUNCHER");
                    launcherIntent.setPackage(componentName.getPackageName());
                    ParceledListSlice<ResolveInfo> parceledList = queryIntentActivities(launcherIntent, launcherIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 786944, userId);
                    if (parceledList != null) {
                        for (ResolveInfo resolveInfo : parceledList.getList()) {
                            if (componentName.equals(resolveInfo.getComponentInfo().getComponentName())) {
                                Slog.i(TAG, "skip enable [" + resolveInfo.activityInfo.getComponentName() + CLONE_USER_LOG_SUFFIX + userId);
                                Binder.restoreCallingIdentity(callingId);
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
                Binder.restoreCallingIdentity(callingId);
                setComponentEnabledSettingSuper(componentName, newState, flags, userId);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    private void setComponentEnabledSettingSuper(ComponentName componentName, int newState, int flags, int userId) {
        ComponentChangeMonitor componentChangeMonitor = this.mComponentChangeMonitor;
        if (componentChangeMonitor != null) {
            componentChangeMonitor.writeComponetChangeLogToFile(componentName, newState, userId);
        }
        HwPackageManagerService.super.setComponentEnabledSetting(componentName, newState, flags, userId);
    }

    private Optional<int[]> getApplicationType(Parcel in) {
        ArrayList<String> pkgName = in.createStringArrayList();
        if (pkgName == null) {
            Slog.i(TAG, "getApplicationType , pkgName is null");
            return Optional.empty();
        }
        int packageNameLength = pkgName.size();
        int[] appType = new int[packageNameLength];
        PackageManager package1 = this.mContext.getPackageManager();
        int[] comparedSignatures = in.createIntArray();
        Binder.clearCallingIdentity();
        for (int i = 0; i < packageNameLength; i++) {
            int i2 = 0;
            ApplicationInfo ai = getApplicationInfo(pkgName.get(i), HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE, 0);
            if (isSystemApp(ai)) {
                appType[i] = appType[i] | 1;
            }
            if (ai != null) {
                try {
                    if (ai.sourceDir != null) {
                        if (!ai.sourceDir.startsWith("/data/app/")) {
                            appType[i] = appType[i] | 2;
                        }
                        if (comparedSignatures == null) {
                            Slog.i(TAG, "getApplicationType , comparedSignatures is null , continue");
                        } else {
                            try {
                                PackageInfo pi = package1.getPackageInfo(pkgName.get(i), 64);
                                if (pi.packageName.equals(pkgName.get(i)) && pi.signatures != null) {
                                    if (pi.signatures.length == 1) {
                                        int sigHashCode = pi.signatures[0].hashCode();
                                        int length = comparedSignatures.length;
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
                                }
                            } catch (Exception e) {
                                Slog.i(TAG, "app:" + pkgName.get(i) + ", unmatch hwSignatures!");
                            }
                        }
                    }
                } catch (Exception e2) {
                    Slog.i(TAG, "app:" + pkgName.get(i) + ", not exists!");
                    appType[i] = appType[i] | 128;
                }
            }
            Slog.i(TAG, "app:" + pkgName.get(i) + ", getApplicationInfo or source return null");
            appType[i] = appType[i] | 128;
        }
        return Optional.ofNullable(appType);
    }

    public HwCustPackageManagerService getCustPackageManagerService() {
        return sCustPackageManagerService;
    }

    public void setCotaApksInstallStatus(int value) {
        HotInstall.getInstance().setCotaApksInstallStatus(value);
    }

    public HashMap<String, HashSet<String>> getCotaDelInstallMap() {
        return HotInstall.getInstance().getCotaDelInstallMap();
    }

    public HashMap<String, HashSet<String>> getCotaInstallMap() {
        return HotInstall.getInstance().getCotaInstallMap();
    }

    /* access modifiers changed from: protected */
    public boolean isNotificationAddSplitButton(String imsPkgName) {
        if (TextUtils.isEmpty(imsPkgName)) {
            return false;
        }
        List<String> oneSplitScreenImsListPkgNames = SplitNotificationUtils.getInstance(this.mContext).getListPkgName(2);
        if (oneSplitScreenImsListPkgNames.size() == 0 || !oneSplitScreenImsListPkgNames.contains(imsPkgName.toLowerCase(Locale.getDefault())) || !isSupportSplitScreen(imsPkgName)) {
            return false;
        }
        String dockableTopPkgName = getDockableTopPkgName();
        if (TextUtils.isEmpty(dockableTopPkgName)) {
            return false;
        }
        List<String> oneSplitScreenVideoListPkgNames = SplitNotificationUtils.getInstance(this.mContext).getListPkgName(1);
        if (oneSplitScreenVideoListPkgNames.size() == 0 || !oneSplitScreenVideoListPkgNames.contains(dockableTopPkgName.toLowerCase(Locale.getDefault())) || !isSupportSplitScreen(dockableTopPkgName)) {
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
        if (runningTaskInfo == null || !runningTaskInfo.supportsSplitScreenMultiWindow) {
            return "";
        }
        try {
            if (WindowManagerGlobal.getWindowManagerService().getDockedStackSide() != -1 || ActivityManager.getService().isInLockTaskMode()) {
                return "";
            }
            return runningTaskInfo.topActivity.getPackageName();
        } catch (RemoteException e) {
            Slog.e(TAG, "get dockside failed by RemoteException");
            return "";
        }
    }

    private boolean isSupportSplitScreen(String packageName) {
        Intent mainIntent;
        ComponentName mainComponentName;
        PackageManager packageManager = this.mContext.getPackageManager();
        int userId = ActivityManager.getCurrentUser();
        Optional<Intent> intentOptional = getLaunchIntentForPackageAsUser(packageName, packageManager, userId);
        if (!(!intentOptional.isPresent() || (mainIntent = intentOptional.get()) == null || (mainComponentName = mainIntent.getComponent()) == null)) {
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
        return false;
    }

    private boolean isResizeableMode(int mode) {
        return mode == 2 || mode == 4 || mode == 1;
    }

    private Optional<Intent> getLaunchIntentForPackageAsUser(String packageName, PackageManager pm, int userId) {
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
            return Optional.empty();
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(268435456);
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return Optional.ofNullable(intent);
    }
}
