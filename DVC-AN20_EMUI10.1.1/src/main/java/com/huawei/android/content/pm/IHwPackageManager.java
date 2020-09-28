package com.huawei.android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBackupSessionCallback;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import com.huawei.android.content.pm.IExtServiceProvider;
import java.util.List;
import java.util.Map;

public interface IHwPackageManager extends IInterface {
    Bundle[] canGrantDPermissions(Bundle[] bundleArr) throws RemoteException;

    void clearPreferredActivityAsUser(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName, int i2) throws RemoteException;

    int executeBackupTask(int i, String str) throws RemoteException;

    int finishBackupSession(int i) throws RemoteException;

    int getAppUseNotchMode(String str) throws RemoteException;

    int getAppUseSideMode(String str) throws RemoteException;

    float getApplicationAspectRatio(String str, String str2) throws RemoteException;

    List<ApplicationInfo> getClusterApplications(int i, int i2, boolean z) throws RemoteException;

    int getDisplayChangeAppRestartConfig(int i, String str) throws RemoteException;

    int getForceDarkSetting(String str) throws RemoteException;

    List<String> getHwPublicityAppList() throws RemoteException;

    ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() throws RemoteException;

    Map getHwRenamedPackages(int i) throws RemoteException;

    List<HwHepPackageInfo> getInstalledHep(int i) throws RemoteException;

    boolean getMapleEnableFlag(String str) throws RemoteException;

    String getMspesOEMConfig() throws RemoteException;

    int getOpenFileResult(Intent intent) throws RemoteException;

    List<String> getPreinstalledApkList() throws RemoteException;

    int getPrivilegeAppType(String str) throws RemoteException;

    String getResourcePackageNameByIcon(String str, int i, int i2) throws RemoteException;

    List<String> getScanInstallList() throws RemoteException;

    List<String> getSystemWhiteList(String str) throws RemoteException;

    boolean getVersionMatchFlag(int i, int i2) throws RemoteException;

    boolean isAllAppsUseSideMode(List<String> list) throws RemoteException;

    boolean isMapleEnv() throws RemoteException;

    boolean isPerfOptEnable(String str, int i) throws RemoteException;

    boolean pmInstallHwTheme(String str, boolean z, int i) throws RemoteException;

    String readMspesFile(String str) throws RemoteException;

    void registerExtServiceProvider(IExtServiceProvider iExtServiceProvider, Intent intent) throws RemoteException;

    boolean restoreAllAppsUseSideMode() throws RemoteException;

    boolean scanInstallApk(String str) throws RemoteException;

    boolean setAllAppsUseSideMode(boolean z) throws RemoteException;

    void setAppCanUninstall(String str, boolean z) throws RemoteException;

    void setAppUseNotchMode(String str, int i) throws RemoteException;

    void setAppUseSideMode(String str, int i) throws RemoteException;

    boolean setApplicationAspectRatio(String str, String str2, float f) throws RemoteException;

    boolean setForceDarkSetting(List<String> list, int i) throws RemoteException;

    void setHdbKey(String str) throws RemoteException;

    void setMapleEnableFlag(String str, boolean z) throws RemoteException;

    void setOpenFileResult(Intent intent, int i) throws RemoteException;

    void setVersionMatchFlag(int i, int i2, boolean z) throws RemoteException;

    boolean shouldSkipTriggerFreeform(String str, int i) throws RemoteException;

    int startBackupSession(IBackupSessionCallback iBackupSessionCallback) throws RemoteException;

    int uninstallHep(String str, int i) throws RemoteException;

    void unregisterExtServiceProvider(IExtServiceProvider iExtServiceProvider) throws RemoteException;

    int updateMspesOEMConfig(String str) throws RemoteException;

    boolean writeMspesFile(String str, String str2) throws RemoteException;

    public static class Default implements IHwPackageManager {
        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean isPerfOptEnable(String packageName, int optType) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int getAppUseNotchMode(String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public void setAppUseNotchMode(String packageName, int mode) throws RemoteException {
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public void setAppCanUninstall(String packageName, boolean canUninstall) throws RemoteException {
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean setApplicationAspectRatio(String packageName, String aspectName, float ar) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public float getApplicationAspectRatio(String packageName, String aspectName) throws RemoteException {
            return 0.0f;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public List<String> getPreinstalledApkList() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public List<String> getHwPublicityAppList() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int startBackupSession(IBackupSessionCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int executeBackupTask(int sessionId, String taskCmd) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int finishBackupSession(int sessionId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public String getResourcePackageNameByIcon(String pkgName, int icon, int userId) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean scanInstallApk(String apkFile) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public List<String> getScanInstallList() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public void setHdbKey(String key) throws RemoteException {
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean pmInstallHwTheme(String themePath, boolean setwallpaper, int userId) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean isMapleEnv() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public String readMspesFile(String fileName) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean writeMspesFile(String fileName, String content) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public String getMspesOEMConfig() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int updateMspesOEMConfig(String src) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public List<String> getSystemWhiteList(String type) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean shouldSkipTriggerFreeform(String pkgName, int userId) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int getAppUseSideMode(String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public void setAppUseSideMode(String packageName, int mode) throws RemoteException {
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int getPrivilegeAppType(String pkgName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean setAllAppsUseSideMode(boolean isUse) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean restoreAllAppsUseSideMode() throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean isAllAppsUseSideMode(List<String> list) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public void clearPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) throws RemoteException {
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public void registerExtServiceProvider(IExtServiceProvider extServiceProvider, Intent filter) throws RemoteException {
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public void unregisterExtServiceProvider(IExtServiceProvider extServiceProvider) throws RemoteException {
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean getMapleEnableFlag(String packageName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public void setMapleEnableFlag(String packageName, boolean flag) throws RemoteException {
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean setForceDarkSetting(List<String> list, int forceDarkMode) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int getForceDarkSetting(String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public Bundle[] canGrantDPermissions(Bundle[] bundles) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public Map getHwRenamedPackages(int flags) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public List<ApplicationInfo> getClusterApplications(int flags, int clusterMask, boolean isOnlyDisabled) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public List<HwHepPackageInfo> getInstalledHep(int flags) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int uninstallHep(String packageName, int flags) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public void setVersionMatchFlag(int deviceType, int version, boolean isMatchSuccess) throws RemoteException {
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public boolean getVersionMatchFlag(int deviceType, int version) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public void setOpenFileResult(Intent intent, int flag) throws RemoteException {
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int getOpenFileResult(Intent intent) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.content.pm.IHwPackageManager
        public int getDisplayChangeAppRestartConfig(int type, String pkgName) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwPackageManager {
        private static final String DESCRIPTOR = "com.huawei.android.content.pm.IHwPackageManager";
        static final int TRANSACTION_canGrantDPermissions = 38;
        static final int TRANSACTION_clearPreferredActivityAsUser = 31;
        static final int TRANSACTION_executeBackupTask = 11;
        static final int TRANSACTION_finishBackupSession = 12;
        static final int TRANSACTION_getAppUseNotchMode = 2;
        static final int TRANSACTION_getAppUseSideMode = 25;
        static final int TRANSACTION_getApplicationAspectRatio = 6;
        static final int TRANSACTION_getClusterApplications = 40;
        static final int TRANSACTION_getDisplayChangeAppRestartConfig = 47;
        static final int TRANSACTION_getForceDarkSetting = 37;
        static final int TRANSACTION_getHwPublicityAppList = 8;
        static final int TRANSACTION_getHwPublicityAppParcelFileDescriptor = 9;
        static final int TRANSACTION_getHwRenamedPackages = 39;
        static final int TRANSACTION_getInstalledHep = 41;
        static final int TRANSACTION_getMapleEnableFlag = 34;
        static final int TRANSACTION_getMspesOEMConfig = 21;
        static final int TRANSACTION_getOpenFileResult = 46;
        static final int TRANSACTION_getPreinstalledApkList = 7;
        static final int TRANSACTION_getPrivilegeAppType = 27;
        static final int TRANSACTION_getResourcePackageNameByIcon = 13;
        static final int TRANSACTION_getScanInstallList = 15;
        static final int TRANSACTION_getSystemWhiteList = 23;
        static final int TRANSACTION_getVersionMatchFlag = 44;
        static final int TRANSACTION_isAllAppsUseSideMode = 30;
        static final int TRANSACTION_isMapleEnv = 18;
        static final int TRANSACTION_isPerfOptEnable = 1;
        static final int TRANSACTION_pmInstallHwTheme = 17;
        static final int TRANSACTION_readMspesFile = 19;
        static final int TRANSACTION_registerExtServiceProvider = 32;
        static final int TRANSACTION_restoreAllAppsUseSideMode = 29;
        static final int TRANSACTION_scanInstallApk = 14;
        static final int TRANSACTION_setAllAppsUseSideMode = 28;
        static final int TRANSACTION_setAppCanUninstall = 4;
        static final int TRANSACTION_setAppUseNotchMode = 3;
        static final int TRANSACTION_setAppUseSideMode = 26;
        static final int TRANSACTION_setApplicationAspectRatio = 5;
        static final int TRANSACTION_setForceDarkSetting = 36;
        static final int TRANSACTION_setHdbKey = 16;
        static final int TRANSACTION_setMapleEnableFlag = 35;
        static final int TRANSACTION_setOpenFileResult = 45;
        static final int TRANSACTION_setVersionMatchFlag = 43;
        static final int TRANSACTION_shouldSkipTriggerFreeform = 24;
        static final int TRANSACTION_startBackupSession = 10;
        static final int TRANSACTION_uninstallHep = 42;
        static final int TRANSACTION_unregisterExtServiceProvider = 33;
        static final int TRANSACTION_updateMspesOEMConfig = 22;
        static final int TRANSACTION_writeMspesFile = 20;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwPackageManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwPackageManager)) {
                return new Proxy(obj);
            }
            return (IHwPackageManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "isPerfOptEnable";
                case 2:
                    return "getAppUseNotchMode";
                case 3:
                    return "setAppUseNotchMode";
                case 4:
                    return "setAppCanUninstall";
                case 5:
                    return "setApplicationAspectRatio";
                case 6:
                    return "getApplicationAspectRatio";
                case 7:
                    return "getPreinstalledApkList";
                case 8:
                    return "getHwPublicityAppList";
                case 9:
                    return "getHwPublicityAppParcelFileDescriptor";
                case 10:
                    return "startBackupSession";
                case 11:
                    return "executeBackupTask";
                case 12:
                    return "finishBackupSession";
                case 13:
                    return "getResourcePackageNameByIcon";
                case 14:
                    return "scanInstallApk";
                case 15:
                    return "getScanInstallList";
                case 16:
                    return "setHdbKey";
                case 17:
                    return "pmInstallHwTheme";
                case 18:
                    return "isMapleEnv";
                case 19:
                    return "readMspesFile";
                case 20:
                    return "writeMspesFile";
                case 21:
                    return "getMspesOEMConfig";
                case 22:
                    return "updateMspesOEMConfig";
                case 23:
                    return "getSystemWhiteList";
                case 24:
                    return "shouldSkipTriggerFreeform";
                case 25:
                    return "getAppUseSideMode";
                case 26:
                    return "setAppUseSideMode";
                case 27:
                    return "getPrivilegeAppType";
                case 28:
                    return "setAllAppsUseSideMode";
                case 29:
                    return "restoreAllAppsUseSideMode";
                case 30:
                    return "isAllAppsUseSideMode";
                case 31:
                    return "clearPreferredActivityAsUser";
                case 32:
                    return "registerExtServiceProvider";
                case 33:
                    return "unregisterExtServiceProvider";
                case 34:
                    return "getMapleEnableFlag";
                case 35:
                    return "setMapleEnableFlag";
                case 36:
                    return "setForceDarkSetting";
                case 37:
                    return "getForceDarkSetting";
                case 38:
                    return "canGrantDPermissions";
                case 39:
                    return "getHwRenamedPackages";
                case 40:
                    return "getClusterApplications";
                case 41:
                    return "getInstalledHep";
                case 42:
                    return "uninstallHep";
                case 43:
                    return "setVersionMatchFlag";
                case 44:
                    return "getVersionMatchFlag";
                case 45:
                    return "setOpenFileResult";
                case 46:
                    return "getOpenFileResult";
                case 47:
                    return "getDisplayChangeAppRestartConfig";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IntentFilter _arg0;
            ComponentName _arg3;
            Intent _arg1;
            Intent _arg02;
            Intent _arg03;
            if (code != 1598968902) {
                boolean _arg12 = false;
                boolean _arg2 = false;
                boolean _arg22 = false;
                boolean _arg13 = false;
                boolean _arg04 = false;
                boolean _arg14 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPerfOptEnable = isPerfOptEnable(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isPerfOptEnable ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getAppUseNotchMode(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setAppUseNotchMode(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setAppCanUninstall(_arg05, _arg12);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean applicationAspectRatio = setApplicationAspectRatio(data.readString(), data.readString(), data.readFloat());
                        reply.writeNoException();
                        reply.writeInt(applicationAspectRatio ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        float _result2 = getApplicationAspectRatio(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeFloat(_result2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result3 = getPreinstalledApkList();
                        reply.writeNoException();
                        reply.writeStringList(_result3);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result4 = getHwPublicityAppList();
                        reply.writeNoException();
                        reply.writeStringList(_result4);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result5 = getHwPublicityAppParcelFileDescriptor();
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = startBackupSession(IBackupSessionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = executeBackupTask(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = finishBackupSession(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _result9 = getResourcePackageNameByIcon(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result9);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean scanInstallApk = scanInstallApk(data.readString());
                        reply.writeNoException();
                        reply.writeInt(scanInstallApk ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result10 = getScanInstallList();
                        reply.writeNoException();
                        reply.writeStringList(_result10);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        setHdbKey(data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = true;
                        }
                        boolean pmInstallHwTheme = pmInstallHwTheme(_arg06, _arg14, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(pmInstallHwTheme ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isMapleEnv = isMapleEnv();
                        reply.writeNoException();
                        reply.writeInt(isMapleEnv ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _result11 = readMspesFile(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result11);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean writeMspesFile = writeMspesFile(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(writeMspesFile ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = getMspesOEMConfig();
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = updateMspesOEMConfig(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result14 = getSystemWhiteList(data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result14);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean shouldSkipTriggerFreeform = shouldSkipTriggerFreeform(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(shouldSkipTriggerFreeform ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = getAppUseSideMode(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        setAppUseSideMode(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = getPrivilegeAppType(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        boolean allAppsUseSideMode = setAllAppsUseSideMode(_arg04);
                        reply.writeNoException();
                        reply.writeInt(allAppsUseSideMode ? 1 : 0);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        boolean restoreAllAppsUseSideMode = restoreAllAppsUseSideMode();
                        reply.writeNoException();
                        reply.writeInt(restoreAllAppsUseSideMode ? 1 : 0);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAllAppsUseSideMode = isAllAppsUseSideMode(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(isAllAppsUseSideMode ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = IntentFilter.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        int _arg15 = data.readInt();
                        ComponentName[] _arg23 = (ComponentName[]) data.createTypedArray(ComponentName.CREATOR);
                        if (data.readInt() != 0) {
                            _arg3 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        clearPreferredActivityAsUser(_arg0, _arg15, _arg23, _arg3, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        IExtServiceProvider _arg07 = IExtServiceProvider.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        registerExtServiceProvider(_arg07, _arg1);
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterExtServiceProvider(IExtServiceProvider.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        boolean mapleEnableFlag = getMapleEnableFlag(data.readString());
                        reply.writeNoException();
                        reply.writeInt(mapleEnableFlag ? 1 : 0);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setMapleEnableFlag(_arg08, _arg13);
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean forceDarkSetting = setForceDarkSetting(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(forceDarkSetting ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = getForceDarkSetting(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle[] _result18 = canGrantDPermissions((Bundle[]) data.createTypedArray(Bundle.CREATOR));
                        reply.writeNoException();
                        reply.writeTypedArray(_result18, 1);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result19 = getHwRenamedPackages(data.readInt());
                        reply.writeNoException();
                        reply.writeMap(_result19);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        List<ApplicationInfo> _result20 = getClusterApplications(_arg09, _arg16, _arg22);
                        reply.writeNoException();
                        reply.writeTypedList(_result20);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        List<HwHepPackageInfo> _result21 = getInstalledHep(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result21);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = uninstallHep(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        int _arg17 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        setVersionMatchFlag(_arg010, _arg17, _arg2);
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        boolean versionMatchFlag = getVersionMatchFlag(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(versionMatchFlag ? 1 : 0);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        setOpenFileResult(_arg02, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        int _result23 = getOpenFileResult(_arg03);
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        int _result24 = getDisplayChangeAppRestartConfig(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwPackageManager {
            public static IHwPackageManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean isPerfOptEnable(String packageName, int optType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(optType);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPerfOptEnable(packageName, optType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int getAppUseNotchMode(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppUseNotchMode(packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public void setAppUseNotchMode(String packageName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppUseNotchMode(packageName, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public void setAppCanUninstall(String packageName, boolean canUninstall) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(canUninstall ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppCanUninstall(packageName, canUninstall);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean setApplicationAspectRatio(String packageName, String aspectName, float ar) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(aspectName);
                    _data.writeFloat(ar);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setApplicationAspectRatio(packageName, aspectName, ar);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public float getApplicationAspectRatio(String packageName, String aspectName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(aspectName);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApplicationAspectRatio(packageName, aspectName);
                    }
                    _reply.readException();
                    float _result = _reply.readFloat();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public List<String> getPreinstalledApkList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPreinstalledApkList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public List<String> getHwPublicityAppList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwPublicityAppList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwPublicityAppParcelFileDescriptor();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int startBackupSession(IBackupSessionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startBackupSession(callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int executeBackupTask(int sessionId, String taskCmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeString(taskCmd);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().executeBackupTask(sessionId, taskCmd);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int finishBackupSession(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().finishBackupSession(sessionId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public String getResourcePackageNameByIcon(String pkgName, int icon, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(icon);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getResourcePackageNameByIcon(pkgName, icon, userId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean scanInstallApk(String apkFile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkFile);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().scanInstallApk(apkFile);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public List<String> getScanInstallList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getScanInstallList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public void setHdbKey(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHdbKey(key);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean pmInstallHwTheme(String themePath, boolean setwallpaper, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(themePath);
                    boolean _result = true;
                    _data.writeInt(setwallpaper ? 1 : 0);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().pmInstallHwTheme(themePath, setwallpaper, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean isMapleEnv() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isMapleEnv();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public String readMspesFile(String fileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readMspesFile(fileName);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean writeMspesFile(String fileName, String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    _data.writeString(content);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().writeMspesFile(fileName, content);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public String getMspesOEMConfig() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMspesOEMConfig();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int updateMspesOEMConfig(String src) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(src);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateMspesOEMConfig(src);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public List<String> getSystemWhiteList(String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(type);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemWhiteList(type);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean shouldSkipTriggerFreeform(String pkgName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldSkipTriggerFreeform(pkgName, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int getAppUseSideMode(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppUseSideMode(packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public void setAppUseSideMode(String packageName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppUseSideMode(packageName, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int getPrivilegeAppType(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPrivilegeAppType(pkgName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean setAllAppsUseSideMode(boolean isUse) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(isUse ? 1 : 0);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAllAppsUseSideMode(isUse);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean restoreAllAppsUseSideMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().restoreAllAppsUseSideMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean isAllAppsUseSideMode(List<String> packages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packages);
                    boolean _result = false;
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAllAppsUseSideMode(packages);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public void clearPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(match);
                    _data.writeTypedArray(set, 0);
                    if (activity != null) {
                        _data.writeInt(1);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearPreferredActivityAsUser(filter, match, set, activity, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public void registerExtServiceProvider(IExtServiceProvider extServiceProvider, Intent filter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(extServiceProvider != null ? extServiceProvider.asBinder() : null);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerExtServiceProvider(extServiceProvider, filter);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public void unregisterExtServiceProvider(IExtServiceProvider extServiceProvider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(extServiceProvider != null ? extServiceProvider.asBinder() : null);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterExtServiceProvider(extServiceProvider);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean getMapleEnableFlag(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMapleEnableFlag(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public void setMapleEnableFlag(String packageName, boolean flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flag ? 1 : 0);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMapleEnableFlag(packageName, flag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean setForceDarkSetting(List<String> packageNames, int forceDarkMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    _data.writeInt(forceDarkMode);
                    boolean _result = false;
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setForceDarkSetting(packageNames, forceDarkMode);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int getForceDarkSetting(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getForceDarkSetting(packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public Bundle[] canGrantDPermissions(Bundle[] bundles) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(bundles, 0);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canGrantDPermissions(bundles);
                    }
                    _reply.readException();
                    Bundle[] _result = (Bundle[]) _reply.createTypedArray(Bundle.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public Map getHwRenamedPackages(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwRenamedPackages(flags);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public List<ApplicationInfo> getClusterApplications(int flags, int clusterMask, boolean isOnlyDisabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeInt(clusterMask);
                    _data.writeInt(isOnlyDisabled ? 1 : 0);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getClusterApplications(flags, clusterMask, isOnlyDisabled);
                    }
                    _reply.readException();
                    List<ApplicationInfo> _result = _reply.createTypedArrayList(ApplicationInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public List<HwHepPackageInfo> getInstalledHep(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstalledHep(flags);
                    }
                    _reply.readException();
                    List<HwHepPackageInfo> _result = _reply.createTypedArrayList(HwHepPackageInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int uninstallHep(String packageName, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().uninstallHep(packageName, flags);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public void setVersionMatchFlag(int deviceType, int version, boolean isMatchSuccess) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceType);
                    _data.writeInt(version);
                    _data.writeInt(isMatchSuccess ? 1 : 0);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVersionMatchFlag(deviceType, version, isMatchSuccess);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public boolean getVersionMatchFlag(int deviceType, int version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceType);
                    _data.writeInt(version);
                    boolean _result = false;
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVersionMatchFlag(deviceType, version);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public void setOpenFileResult(Intent intent, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flag);
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOpenFileResult(intent, flag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int getOpenFileResult(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOpenFileResult(intent);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.content.pm.IHwPackageManager
            public int getDisplayChangeAppRestartConfig(int type, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisplayChangeAppRestartConfig(type, pkgName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwPackageManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwPackageManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
