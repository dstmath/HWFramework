package android.content.pm;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.IDexModuleRegisterCallback;
import android.content.pm.IOnPermissionsChangeListener;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.dex.IArtManager;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public interface IPackageManager extends IInterface {
    boolean activitySupportsIntent(ComponentName componentName, Intent intent, String str) throws RemoteException;

    void addCrossProfileIntentFilter(IntentFilter intentFilter, String str, int i, int i2, int i3) throws RemoteException;

    void addOnPermissionsChangeListener(IOnPermissionsChangeListener iOnPermissionsChangeListener) throws RemoteException;

    @UnsupportedAppUsage
    boolean addPermission(PermissionInfo permissionInfo) throws RemoteException;

    @UnsupportedAppUsage
    boolean addPermissionAsync(PermissionInfo permissionInfo) throws RemoteException;

    void addPersistentPreferredActivity(IntentFilter intentFilter, ComponentName componentName, int i) throws RemoteException;

    void addPreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName, int i2) throws RemoteException;

    boolean addWhitelistedRestrictedPermission(String str, String str2, int i, int i2) throws RemoteException;

    boolean canForwardTo(Intent intent, String str, int i, int i2) throws RemoteException;

    boolean canRequestPackageInstalls(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    String[] canonicalToCurrentPackageNames(String[] strArr) throws RemoteException;

    void checkPackageStartable(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    int checkPermission(String str, String str2, int i) throws RemoteException;

    @UnsupportedAppUsage
    int checkSignatures(String str, String str2) throws RemoteException;

    int checkUidPermission(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    int checkUidSignatures(int i, int i2) throws RemoteException;

    void clearApplicationProfileData(String str) throws RemoteException;

    void clearApplicationUserData(String str, IPackageDataObserver iPackageDataObserver, int i) throws RemoteException;

    void clearCrossProfileIntentFilters(int i, String str) throws RemoteException;

    void clearPackagePersistentPreferredActivities(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    void clearPackagePreferredActivities(String str) throws RemoteException;

    boolean compileLayouts(String str) throws RemoteException;

    @UnsupportedAppUsage
    String[] currentToCanonicalPackageNames(String[] strArr) throws RemoteException;

    @UnsupportedAppUsage
    void deleteApplicationCacheFiles(String str, IPackageDataObserver iPackageDataObserver) throws RemoteException;

    void deleteApplicationCacheFilesAsUser(String str, int i, IPackageDataObserver iPackageDataObserver) throws RemoteException;

    void deletePackageAsUser(String str, int i, IPackageDeleteObserver iPackageDeleteObserver, int i2, int i3) throws RemoteException;

    void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 iPackageDeleteObserver2, int i, int i2) throws RemoteException;

    void deletePreloadsFileCache() throws RemoteException;

    void dumpProfiles(String str) throws RemoteException;

    void enterSafeMode() throws RemoteException;

    void extendVerificationTimeout(int i, int i2, long j) throws RemoteException;

    ResolveInfo findPersistentPreferredActivity(Intent intent, int i) throws RemoteException;

    void finishPackageInstall(int i, boolean z) throws RemoteException;

    void flushPackageRestrictionsAsUser(int i) throws RemoteException;

    void forceDexOpt(String str) throws RemoteException;

    void freeStorage(String str, long j, int i, IntentSender intentSender) throws RemoteException;

    void freeStorageAndNotify(String str, long j, int i, IPackageDataObserver iPackageDataObserver) throws RemoteException;

    @UnsupportedAppUsage
    ActivityInfo getActivityInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    ParceledListSlice getAllIntentFilters(String str) throws RemoteException;

    List<String> getAllPackages() throws RemoteException;

    ParceledListSlice getAllPermissionGroups(int i) throws RemoteException;

    @UnsupportedAppUsage
    String[] getAppOpPermissionPackages(String str) throws RemoteException;

    String getAppPredictionServicePackageName() throws RemoteException;

    @UnsupportedAppUsage
    int getApplicationEnabledSetting(String str, int i) throws RemoteException;

    boolean getApplicationHiddenSettingAsUser(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    ApplicationInfo getApplicationInfo(String str, int i, int i2) throws RemoteException;

    IArtManager getArtManager() throws RemoteException;

    String getAttentionServicePackageName() throws RemoteException;

    @UnsupportedAppUsage
    boolean getBlockUninstallForUser(String str, int i) throws RemoteException;

    ChangedPackages getChangedPackages(int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    int getComponentEnabledSetting(ComponentName componentName, int i) throws RemoteException;

    ParceledListSlice getDeclaredSharedLibraries(String str, int i, int i2) throws RemoteException;

    byte[] getDefaultAppsBackup(int i) throws RemoteException;

    String getDefaultBrowserPackageName(int i) throws RemoteException;

    @UnsupportedAppUsage
    int getFlagsForUid(int i) throws RemoteException;

    CharSequence getHarmfulAppWarning(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    ComponentName getHomeActivities(List<ResolveInfo> list) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    String getIncidentReportApproverPackageName() throws RemoteException;

    @UnsupportedAppUsage
    int getInstallLocation() throws RemoteException;

    int getInstallReason(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    ParceledListSlice getInstalledApplications(int i, int i2) throws RemoteException;

    List<ModuleInfo> getInstalledModules(int i) throws RemoteException;

    @UnsupportedAppUsage
    ParceledListSlice getInstalledPackages(int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    String getInstallerPackageName(String str) throws RemoteException;

    String getInstantAppAndroidId(String str, int i) throws RemoteException;

    byte[] getInstantAppCookie(String str, int i) throws RemoteException;

    Bitmap getInstantAppIcon(String str, int i) throws RemoteException;

    ComponentName getInstantAppInstallerComponent() throws RemoteException;

    ComponentName getInstantAppResolverComponent() throws RemoteException;

    ComponentName getInstantAppResolverSettingsComponent() throws RemoteException;

    ParceledListSlice getInstantApps(int i) throws RemoteException;

    @UnsupportedAppUsage
    InstrumentationInfo getInstrumentationInfo(ComponentName componentName, int i) throws RemoteException;

    byte[] getIntentFilterVerificationBackup(int i) throws RemoteException;

    ParceledListSlice getIntentFilterVerifications(String str) throws RemoteException;

    int getIntentVerificationStatus(String str, int i) throws RemoteException;

    KeySet getKeySetByAlias(String str, String str2) throws RemoteException;

    @UnsupportedAppUsage
    ResolveInfo getLastChosenActivity(Intent intent, String str, int i) throws RemoteException;

    ModuleInfo getModuleInfo(String str, int i) throws RemoteException;

    int getMoveStatus(int i) throws RemoteException;

    @UnsupportedAppUsage
    String getNameForUid(int i) throws RemoteException;

    String[] getNamesForUids(int[] iArr) throws RemoteException;

    int[] getPackageGids(String str, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    PackageInfo getPackageInfo(String str, int i, int i2) throws RemoteException;

    PackageInfo getPackageInfoVersioned(VersionedPackage versionedPackage, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    IPackageInstaller getPackageInstaller() throws RemoteException;

    void getPackageSizeInfo(String str, int i, IPackageStatsObserver iPackageStatsObserver) throws RemoteException;

    @UnsupportedAppUsage
    int getPackageUid(String str, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    String[] getPackagesForUid(int i) throws RemoteException;

    ParceledListSlice getPackagesHoldingPermissions(String[] strArr, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    String getPermissionControllerPackageName() throws RemoteException;

    int getPermissionFlags(String str, String str2, int i) throws RemoteException;

    @UnsupportedAppUsage
    PermissionGroupInfo getPermissionGroupInfo(String str, int i) throws RemoteException;

    PermissionInfo getPermissionInfo(String str, String str2, int i) throws RemoteException;

    ParceledListSlice getPersistentApplications(int i) throws RemoteException;

    @UnsupportedAppUsage
    int getPreferredActivities(List<IntentFilter> list, List<ComponentName> list2, String str) throws RemoteException;

    byte[] getPreferredActivityBackup(int i) throws RemoteException;

    int getPrivateFlagsForUid(int i) throws RemoteException;

    @UnsupportedAppUsage
    ProviderInfo getProviderInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    ActivityInfo getReceiverInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    int getRuntimePermissionsVersion(int i) throws RemoteException;

    @UnsupportedAppUsage
    ServiceInfo getServiceInfo(ComponentName componentName, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    String getServicesSystemSharedLibraryPackageName() throws RemoteException;

    ParceledListSlice getSharedLibraries(String str, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    String getSharedSystemSharedLibraryPackageName() throws RemoteException;

    KeySet getSigningKeySet(String str) throws RemoteException;

    PersistableBundle getSuspendedPackageAppExtras(String str, int i) throws RemoteException;

    ParceledListSlice getSystemAvailableFeatures() throws RemoteException;

    String getSystemCaptionsServicePackageName() throws RemoteException;

    @UnsupportedAppUsage
    String[] getSystemSharedLibraryNames() throws RemoteException;

    String getSystemTextClassifierPackageName() throws RemoteException;

    @UnsupportedAppUsage
    int getUidForSharedUser(String str) throws RemoteException;

    String[] getUnsuspendablePackagesForUser(String[] strArr, int i) throws RemoteException;

    VerifierDeviceIdentity getVerifierDeviceIdentity() throws RemoteException;

    String getWellbeingPackageName() throws RemoteException;

    List<String> getWhitelistedRestrictedPermissions(String str, int i, int i2) throws RemoteException;

    void grantDefaultPermissionsToActiveLuiApp(String str, int i) throws RemoteException;

    void grantDefaultPermissionsToEnabledCarrierApps(String[] strArr, int i) throws RemoteException;

    void grantDefaultPermissionsToEnabledImsServices(String[] strArr, int i) throws RemoteException;

    void grantDefaultPermissionsToEnabledTelephonyDataServices(String[] strArr, int i) throws RemoteException;

    @UnsupportedAppUsage
    void grantRuntimePermission(String str, String str2, int i) throws RemoteException;

    boolean hasSigningCertificate(String str, byte[] bArr, int i) throws RemoteException;

    boolean hasSystemFeature(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean hasSystemUidErrors() throws RemoteException;

    boolean hasUidSigningCertificate(int i, byte[] bArr, int i2) throws RemoteException;

    int installExistingPackageAsUser(String str, int i, int i2, int i3, List<String> list) throws RemoteException;

    boolean isDeviceUpgrading() throws RemoteException;

    boolean isFirstBoot() throws RemoteException;

    boolean isInstantApp(String str, int i) throws RemoteException;

    boolean isOnlyCoreApps() throws RemoteException;

    @UnsupportedAppUsage
    boolean isPackageAvailable(String str, int i) throws RemoteException;

    boolean isPackageDeviceAdminOnAnyUser(String str) throws RemoteException;

    boolean isPackageSignedByKeySet(String str, KeySet keySet) throws RemoteException;

    boolean isPackageSignedByKeySetExactly(String str, KeySet keySet) throws RemoteException;

    boolean isPackageStateProtected(String str, int i) throws RemoteException;

    boolean isPackageSuspendedForUser(String str, int i) throws RemoteException;

    boolean isPermissionEnforced(String str) throws RemoteException;

    boolean isPermissionRevokedByPolicy(String str, String str2, int i) throws RemoteException;

    boolean isProtectedBroadcast(String str) throws RemoteException;

    @UnsupportedAppUsage
    boolean isSafeMode() throws RemoteException;

    @UnsupportedAppUsage
    boolean isStorageLow() throws RemoteException;

    @UnsupportedAppUsage
    boolean isUidPrivileged(int i) throws RemoteException;

    void logAppProcessStartIfNeeded(String str, int i, String str2, String str3, int i2) throws RemoteException;

    int movePackage(String str, String str2) throws RemoteException;

    int movePrimaryStorage(String str) throws RemoteException;

    void notifyDexLoad(String str, List<String> list, List<String> list2, String str2) throws RemoteException;

    void notifyPackageUse(String str, int i) throws RemoteException;

    void notifyPackagesReplacedReceived(String[] strArr) throws RemoteException;

    boolean performDexOptMode(String str, boolean z, String str2, boolean z2, boolean z3, String str3) throws RemoteException;

    boolean performDexOptSecondary(String str, String str2, boolean z) throws RemoteException;

    void performFstrimIfNeeded() throws RemoteException;

    ParceledListSlice queryContentProviders(String str, int i, int i2, String str2) throws RemoteException;

    @UnsupportedAppUsage
    ParceledListSlice queryInstrumentation(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    ParceledListSlice queryIntentActivities(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentActivityOptions(ComponentName componentName, Intent[] intentArr, String[] strArr, Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentContentProviders(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentReceivers(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryIntentServices(Intent intent, String str, int i, int i2) throws RemoteException;

    ParceledListSlice queryPermissionsByGroup(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    void querySyncProviders(List<String> list, List<ProviderInfo> list2) throws RemoteException;

    void reconcileSecondaryDexFiles(String str) throws RemoteException;

    void registerDexModule(String str, String str2, boolean z, IDexModuleRegisterCallback iDexModuleRegisterCallback) throws RemoteException;

    void registerMoveCallback(IPackageMoveObserver iPackageMoveObserver) throws RemoteException;

    void removeOnPermissionsChangeListener(IOnPermissionsChangeListener iOnPermissionsChangeListener) throws RemoteException;

    @UnsupportedAppUsage
    void removePermission(String str) throws RemoteException;

    boolean removeWhitelistedRestrictedPermission(String str, String str2, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void replacePreferredActivity(IntentFilter intentFilter, int i, ComponentName[] componentNameArr, ComponentName componentName, int i2) throws RemoteException;

    void resetApplicationPreferences(int i) throws RemoteException;

    void resetRuntimePermissions() throws RemoteException;

    ProviderInfo resolveContentProvider(String str, int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    ResolveInfo resolveIntent(Intent intent, String str, int i, int i2) throws RemoteException;

    ResolveInfo resolveService(Intent intent, String str, int i, int i2) throws RemoteException;

    void restoreDefaultApps(byte[] bArr, int i) throws RemoteException;

    void restoreIntentFilterVerification(byte[] bArr, int i) throws RemoteException;

    void restorePreferredActivities(byte[] bArr, int i) throws RemoteException;

    void revokeDefaultPermissionsFromDisabledTelephonyDataServices(String[] strArr, int i) throws RemoteException;

    void revokeDefaultPermissionsFromLuiApps(String[] strArr, int i) throws RemoteException;

    void revokeRuntimePermission(String str, String str2, int i) throws RemoteException;

    boolean runBackgroundDexoptJob(List<String> list) throws RemoteException;

    void sendDeviceCustomizationReadyBroadcast() throws RemoteException;

    void setApplicationCategoryHint(String str, int i, String str2) throws RemoteException;

    @UnsupportedAppUsage
    void setApplicationEnabledSetting(String str, int i, int i2, int i3, String str2) throws RemoteException;

    @UnsupportedAppUsage
    boolean setApplicationHiddenSettingAsUser(String str, boolean z, int i) throws RemoteException;

    boolean setBlockUninstallForUser(String str, boolean z, int i) throws RemoteException;

    @UnsupportedAppUsage
    void setComponentEnabledSetting(ComponentName componentName, int i, int i2, int i3) throws RemoteException;

    boolean setDefaultBrowserPackageName(String str, int i) throws RemoteException;

    String[] setDistractingPackageRestrictionsAsUser(String[] strArr, int i, int i2) throws RemoteException;

    void setHarmfulAppWarning(String str, CharSequence charSequence, int i) throws RemoteException;

    void setHomeActivity(ComponentName componentName, int i) throws RemoteException;

    boolean setInstallLocation(int i) throws RemoteException;

    @UnsupportedAppUsage
    void setInstallerPackageName(String str, String str2) throws RemoteException;

    boolean setInstantAppCookie(String str, byte[] bArr, int i) throws RemoteException;

    @UnsupportedAppUsage
    void setLastChosenActivity(Intent intent, String str, int i, IntentFilter intentFilter, int i2, ComponentName componentName) throws RemoteException;

    @UnsupportedAppUsage
    void setPackageStoppedState(String str, boolean z, int i) throws RemoteException;

    String[] setPackagesSuspendedAsUser(String[] strArr, boolean z, PersistableBundle persistableBundle, PersistableBundle persistableBundle2, SuspendDialogInfo suspendDialogInfo, String str, int i) throws RemoteException;

    void setPermissionEnforced(String str, boolean z) throws RemoteException;

    boolean setRequiredForSystemUser(String str, boolean z) throws RemoteException;

    void setRuntimePermissionsVersion(int i, int i2) throws RemoteException;

    void setSystemAppHiddenUntilInstalled(String str, boolean z) throws RemoteException;

    boolean setSystemAppInstallState(String str, boolean z, int i) throws RemoteException;

    void setUpdateAvailable(String str, boolean z) throws RemoteException;

    boolean shouldShowRequestPermissionRationale(String str, String str2, int i) throws RemoteException;

    void systemReady() throws RemoteException;

    void unregisterMoveCallback(IPackageMoveObserver iPackageMoveObserver) throws RemoteException;

    boolean updateIntentVerificationStatus(String str, int i, int i2) throws RemoteException;

    void updatePackagesIfNeeded() throws RemoteException;

    void updatePermissionFlags(String str, String str2, int i, int i2, boolean z, int i3) throws RemoteException;

    void updatePermissionFlagsForAllApps(int i, int i2, int i3) throws RemoteException;

    void verifyIntentFilter(int i, int i2, List<String> list) throws RemoteException;

    void verifyPendingInstall(int i, int i2) throws RemoteException;

    public static class Default implements IPackageManager {
        @Override // android.content.pm.IPackageManager
        public void checkPackageStartable(String packageName, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public boolean isPackageAvailable(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public PackageInfo getPackageInfo(String packageName, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public PackageInfo getPackageInfoVersioned(VersionedPackage versionedPackage, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public int getPackageUid(String packageName, int flags, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public int[] getPackageGids(String packageName, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String[] currentToCanonicalPackageNames(String[] names) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String[] canonicalToCurrentPackageNames(String[] names) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public PermissionInfo getPermissionInfo(String name, String packageName, int flags) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice queryPermissionsByGroup(String group, int flags) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getAllPermissionGroups(int flags) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ActivityInfo getActivityInfo(ComponentName className, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean activitySupportsIntent(ComponentName className, Intent intent, String resolvedType) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public ActivityInfo getReceiverInfo(ComponentName className, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ServiceInfo getServiceInfo(ComponentName className, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ProviderInfo getProviderInfo(ComponentName className, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public int checkPermission(String permName, String pkgName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public int checkUidPermission(String permName, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public boolean addPermission(PermissionInfo info) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void removePermission(String name) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void grantRuntimePermission(String packageName, String permissionName, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void revokeRuntimePermission(String packageName, String permissionName, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void resetRuntimePermissions() throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public int getPermissionFlags(String permissionName, String packageName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, boolean checkAdjustPolicyFlagPermission, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void updatePermissionFlagsForAllApps(int flagMask, int flagValues, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public List<String> getWhitelistedRestrictedPermissions(String packageName, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean addWhitelistedRestrictedPermission(String packageName, String permission, int whitelistFlags, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean removeWhitelistedRestrictedPermission(String packageName, String permission, int whitelistFlags, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean shouldShowRequestPermissionRationale(String permissionName, String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isProtectedBroadcast(String actionName) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public int checkSignatures(String pkg1, String pkg2) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public int checkUidSignatures(int uid1, int uid2) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public List<String> getAllPackages() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String[] getPackagesForUid(int uid) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String getNameForUid(int uid) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String[] getNamesForUids(int[] uids) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public int getUidForSharedUser(String sharedUserName) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public int getFlagsForUid(int uid) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public int getPrivateFlagsForUid(int uid) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isUidPrivileged(int uid) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public String[] getAppOpPermissionPackages(String permissionName) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ResolveInfo findPersistentPreferredActivity(Intent intent, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean canForwardTo(Intent intent, String resolvedType, int sourceUserId, int targetUserId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice queryIntentActivityOptions(ComponentName caller, Intent[] specifics, String[] specificTypes, Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice queryIntentServices(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getInstalledPackages(int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getPackagesHoldingPermissions(String[] permissions, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getInstalledApplications(int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getPersistentApplications(int flags) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ProviderInfo resolveContentProvider(String name, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public void querySyncProviders(List<String> list, List<ProviderInfo> list2) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice queryContentProviders(String processName, int uid, int flags, String metaDataKey) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice queryInstrumentation(String targetPackage, int flags) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public void finishPackageInstall(int token, boolean didLaunch) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void setInstallerPackageName(String targetPackage, String installerPackageName) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void setApplicationCategoryHint(String packageName, int categoryHint, String callerPackageName) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void deletePackageAsUser(String packageName, int versionCode, IPackageDeleteObserver observer, int userId, int flags) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId, int flags) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public String getInstallerPackageName(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public void resetApplicationPreferences(int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public ResolveInfo getLastChosenActivity(Intent intent, String resolvedType, int flags) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public void setLastChosenActivity(Intent intent, String resolvedType, int flags, IntentFilter filter, int match, ComponentName activity) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void clearPackagePreferredActivities(String packageName) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public int getPreferredActivities(List<IntentFilter> list, List<ComponentName> list2, String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public void addPersistentPreferredActivity(IntentFilter filter, ComponentName activity, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void clearPackagePersistentPreferredActivities(String packageName, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void addCrossProfileIntentFilter(IntentFilter intentFilter, String ownerPackage, int sourceUserId, int targetUserId, int flags) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void clearCrossProfileIntentFilters(int sourceUserId, String ownerPackage) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public String[] setDistractingPackageRestrictionsAsUser(String[] packageNames, int restrictionFlags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, PersistableBundle appExtras, PersistableBundle launcherExtras, SuspendDialogInfo dialogInfo, String callingPackage, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String[] getUnsuspendablePackagesForUser(String[] packageNames, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isPackageSuspendedForUser(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public PersistableBundle getSuspendedPackageAppExtras(String packageName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public byte[] getPreferredActivityBackup(int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public void restorePreferredActivities(byte[] backup, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public byte[] getDefaultAppsBackup(int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public void restoreDefaultApps(byte[] backup, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public byte[] getIntentFilterVerificationBackup(int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public void restoreIntentFilterVerification(byte[] backup, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public ComponentName getHomeActivities(List<ResolveInfo> list) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public void setHomeActivity(ComponentName className, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public int getComponentEnabledSetting(ComponentName componentName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public void setApplicationEnabledSetting(String packageName, int newState, int flags, int userId, String callingPackage) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public int getApplicationEnabledSetting(String packageName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public void logAppProcessStartIfNeeded(String processName, int uid, String seinfo, String apkFile, int pid) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void flushPackageRestrictionsAsUser(int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void setPackageStoppedState(String packageName, boolean stopped, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void freeStorageAndNotify(String volumeUuid, long freeStorageSize, int storageFlags, IPackageDataObserver observer) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void freeStorage(String volumeUuid, long freeStorageSize, int storageFlags, IntentSender pi) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void clearApplicationUserData(String packageName, IPackageDataObserver observer, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void clearApplicationProfileData(String packageName) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void getPackageSizeInfo(String packageName, int userHandle, IPackageStatsObserver observer) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public String[] getSystemSharedLibraryNames() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getSystemAvailableFeatures() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean hasSystemFeature(String name, int version) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void enterSafeMode() throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public boolean isSafeMode() throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void systemReady() throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public boolean hasSystemUidErrors() throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void performFstrimIfNeeded() throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void updatePackagesIfNeeded() throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void notifyPackageUse(String packageName, int reason) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void notifyDexLoad(String loadingPackageName, List<String> list, List<String> list2, String loaderIsa) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void registerDexModule(String packageName, String dexModulePath, boolean isSharedModule, IDexModuleRegisterCallback callback) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public boolean performDexOptMode(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force, boolean bootComplete, String splitName) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean performDexOptSecondary(String packageName, String targetCompilerFilter, boolean force) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean compileLayouts(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void dumpProfiles(String packageName) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void forceDexOpt(String packageName) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public boolean runBackgroundDexoptJob(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void reconcileSecondaryDexFiles(String packageName) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public int getMoveStatus(int moveId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public void registerMoveCallback(IPackageMoveObserver callback) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void unregisterMoveCallback(IPackageMoveObserver callback) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public int movePackage(String packageName, String volumeUuid) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public int movePrimaryStorage(String volumeUuid) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public boolean addPermissionAsync(PermissionInfo info) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean setInstallLocation(int loc) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public int getInstallLocation() throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public int installExistingPackageAsUser(String packageName, int userId, int installFlags, int installReason, List<String> list) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public void verifyPendingInstall(int id, int verificationCode) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void verifyIntentFilter(int id, int verificationCode, List<String> list) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public int getIntentVerificationStatus(String packageName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public boolean updateIntentVerificationStatus(String packageName, int status, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getIntentFilterVerifications(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getAllIntentFilters(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean setDefaultBrowserPackageName(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public String getDefaultBrowserPackageName(int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public VerifierDeviceIdentity getVerifierDeviceIdentity() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isFirstBoot() throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isOnlyCoreApps() throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isDeviceUpgrading() throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void setPermissionEnforced(String permission, boolean enforced) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public boolean isPermissionEnforced(String permission) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isStorageLow() throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean getApplicationHiddenSettingAsUser(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void setSystemAppHiddenUntilInstalled(String packageName, boolean hidden) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public boolean setSystemAppInstallState(String packageName, boolean installed, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public IPackageInstaller getPackageInstaller() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean getBlockUninstallForUser(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public KeySet getKeySetByAlias(String packageName, String alias) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public KeySet getSigningKeySet(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isPackageSignedByKeySet(String packageName, KeySet ks) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isPackageSignedByKeySetExactly(String packageName, KeySet ks) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void addOnPermissionsChangeListener(IOnPermissionsChangeListener listener) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void removeOnPermissionsChangeListener(IOnPermissionsChangeListener listener) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void grantDefaultPermissionsToEnabledCarrierApps(String[] packageNames, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void grantDefaultPermissionsToEnabledImsServices(String[] packageNames, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void grantDefaultPermissionsToEnabledTelephonyDataServices(String[] packageNames, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void revokeDefaultPermissionsFromDisabledTelephonyDataServices(String[] packageNames, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void grantDefaultPermissionsToActiveLuiApp(String packageName, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void revokeDefaultPermissionsFromLuiApps(String[] packageNames, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public boolean isPermissionRevokedByPolicy(String permission, String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public String getPermissionControllerPackageName() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getInstantApps(int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public byte[] getInstantAppCookie(String packageName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean setInstantAppCookie(String packageName, byte[] cookie, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public Bitmap getInstantAppIcon(String packageName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isInstantApp(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean setRequiredForSystemUser(String packageName, boolean systemUserApp) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void setUpdateAvailable(String packageName, boolean updateAvaialble) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public String getServicesSystemSharedLibraryPackageName() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String getSharedSystemSharedLibraryPackageName() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ChangedPackages getChangedPackages(int sequenceNumber, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isPackageDeviceAdminOnAnyUser(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public int getInstallReason(String packageName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getSharedLibraries(String packageName, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ParceledListSlice getDeclaredSharedLibraries(String packageName, int flags, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean canRequestPackageInstalls(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public void deletePreloadsFileCache() throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public ComponentName getInstantAppResolverComponent() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ComponentName getInstantAppResolverSettingsComponent() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ComponentName getInstantAppInstallerComponent() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String getInstantAppAndroidId(String packageName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public IArtManager getArtManager() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public void setHarmfulAppWarning(String packageName, CharSequence warning, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public CharSequence getHarmfulAppWarning(String packageName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean hasSigningCertificate(String packageName, byte[] signingCertificate, int flags) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public boolean hasUidSigningCertificate(int uid, byte[] signingCertificate, int flags) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public String getSystemTextClassifierPackageName() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String getAttentionServicePackageName() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String getWellbeingPackageName() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String getAppPredictionServicePackageName() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String getSystemCaptionsServicePackageName() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public String getIncidentReportApproverPackageName() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public boolean isPackageStateProtected(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IPackageManager
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public void sendDeviceCustomizationReadyBroadcast() throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public List<ModuleInfo> getInstalledModules(int flags) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public ModuleInfo getModuleInfo(String packageName, int flags) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManager
        public int getRuntimePermissionsVersion(int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManager
        public void setRuntimePermissionsVersion(int version, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IPackageManager
        public void notifyPackagesReplacedReceived(String[] packages) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPackageManager {
        private static final String DESCRIPTOR = "android.content.pm.IPackageManager";
        static final int TRANSACTION_activitySupportsIntent = 15;
        static final int TRANSACTION_addCrossProfileIntentFilter = 78;
        static final int TRANSACTION_addOnPermissionsChangeListener = 162;
        static final int TRANSACTION_addPermission = 21;
        static final int TRANSACTION_addPermissionAsync = 131;
        static final int TRANSACTION_addPersistentPreferredActivity = 76;
        static final int TRANSACTION_addPreferredActivity = 72;
        static final int TRANSACTION_addWhitelistedRestrictedPermission = 30;
        static final int TRANSACTION_canForwardTo = 47;
        static final int TRANSACTION_canRequestPackageInstalls = 186;
        static final int TRANSACTION_canonicalToCurrentPackageNames = 8;
        static final int TRANSACTION_checkPackageStartable = 1;
        static final int TRANSACTION_checkPermission = 19;
        static final int TRANSACTION_checkSignatures = 34;
        static final int TRANSACTION_checkUidPermission = 20;
        static final int TRANSACTION_checkUidSignatures = 35;
        static final int TRANSACTION_clearApplicationProfileData = 105;
        static final int TRANSACTION_clearApplicationUserData = 104;
        static final int TRANSACTION_clearCrossProfileIntentFilters = 79;
        static final int TRANSACTION_clearPackagePersistentPreferredActivities = 77;
        static final int TRANSACTION_clearPackagePreferredActivities = 74;
        static final int TRANSACTION_compileLayouts = 121;
        static final int TRANSACTION_currentToCanonicalPackageNames = 7;
        static final int TRANSACTION_deleteApplicationCacheFiles = 102;
        static final int TRANSACTION_deleteApplicationCacheFilesAsUser = 103;
        static final int TRANSACTION_deletePackageAsUser = 66;
        static final int TRANSACTION_deletePackageVersioned = 67;
        static final int TRANSACTION_deletePreloadsFileCache = 187;
        static final int TRANSACTION_dumpProfiles = 122;
        static final int TRANSACTION_enterSafeMode = 110;
        static final int TRANSACTION_extendVerificationTimeout = 136;
        static final int TRANSACTION_findPersistentPreferredActivity = 46;
        static final int TRANSACTION_finishPackageInstall = 63;
        static final int TRANSACTION_flushPackageRestrictionsAsUser = 98;
        static final int TRANSACTION_forceDexOpt = 123;
        static final int TRANSACTION_freeStorage = 101;
        static final int TRANSACTION_freeStorageAndNotify = 100;
        static final int TRANSACTION_getActivityInfo = 14;
        static final int TRANSACTION_getAllIntentFilters = 141;
        static final int TRANSACTION_getAllPackages = 36;
        static final int TRANSACTION_getAllPermissionGroups = 12;
        static final int TRANSACTION_getAppOpPermissionPackages = 44;
        static final int TRANSACTION_getAppPredictionServicePackageName = 200;
        static final int TRANSACTION_getApplicationEnabledSetting = 96;
        static final int TRANSACTION_getApplicationHiddenSettingAsUser = 152;
        static final int TRANSACTION_getApplicationInfo = 13;
        static final int TRANSACTION_getArtManager = 192;
        static final int TRANSACTION_getAttentionServicePackageName = 198;
        static final int TRANSACTION_getBlockUninstallForUser = 157;
        static final int TRANSACTION_getChangedPackages = 181;
        static final int TRANSACTION_getComponentEnabledSetting = 94;
        static final int TRANSACTION_getDeclaredSharedLibraries = 185;
        static final int TRANSACTION_getDefaultAppsBackup = 87;
        static final int TRANSACTION_getDefaultBrowserPackageName = 143;
        static final int TRANSACTION_getFlagsForUid = 41;
        static final int TRANSACTION_getHarmfulAppWarning = 194;
        static final int TRANSACTION_getHomeActivities = 91;
        static final int TRANSACTION_getHwInnerService = 204;
        static final int TRANSACTION_getIncidentReportApproverPackageName = 202;
        static final int TRANSACTION_getInstallLocation = 133;
        static final int TRANSACTION_getInstallReason = 183;
        static final int TRANSACTION_getInstalledApplications = 56;
        static final int TRANSACTION_getInstalledModules = 206;
        static final int TRANSACTION_getInstalledPackages = 54;
        static final int TRANSACTION_getInstallerPackageName = 68;
        static final int TRANSACTION_getInstantAppAndroidId = 191;
        static final int TRANSACTION_getInstantAppCookie = 173;
        static final int TRANSACTION_getInstantAppIcon = 175;
        static final int TRANSACTION_getInstantAppInstallerComponent = 190;
        static final int TRANSACTION_getInstantAppResolverComponent = 188;
        static final int TRANSACTION_getInstantAppResolverSettingsComponent = 189;
        static final int TRANSACTION_getInstantApps = 172;
        static final int TRANSACTION_getInstrumentationInfo = 61;
        static final int TRANSACTION_getIntentFilterVerificationBackup = 89;
        static final int TRANSACTION_getIntentFilterVerifications = 140;
        static final int TRANSACTION_getIntentVerificationStatus = 138;
        static final int TRANSACTION_getKeySetByAlias = 158;
        static final int TRANSACTION_getLastChosenActivity = 70;
        static final int TRANSACTION_getModuleInfo = 207;
        static final int TRANSACTION_getMoveStatus = 126;
        static final int TRANSACTION_getNameForUid = 38;
        static final int TRANSACTION_getNamesForUids = 39;
        static final int TRANSACTION_getPackageGids = 6;
        static final int TRANSACTION_getPackageInfo = 3;
        static final int TRANSACTION_getPackageInfoVersioned = 4;
        static final int TRANSACTION_getPackageInstaller = 155;
        static final int TRANSACTION_getPackageSizeInfo = 106;
        static final int TRANSACTION_getPackageUid = 5;
        static final int TRANSACTION_getPackagesForUid = 37;
        static final int TRANSACTION_getPackagesHoldingPermissions = 55;
        static final int TRANSACTION_getPermissionControllerPackageName = 171;
        static final int TRANSACTION_getPermissionFlags = 26;
        static final int TRANSACTION_getPermissionGroupInfo = 11;
        static final int TRANSACTION_getPermissionInfo = 9;
        static final int TRANSACTION_getPersistentApplications = 57;
        static final int TRANSACTION_getPreferredActivities = 75;
        static final int TRANSACTION_getPreferredActivityBackup = 85;
        static final int TRANSACTION_getPrivateFlagsForUid = 42;
        static final int TRANSACTION_getProviderInfo = 18;
        static final int TRANSACTION_getReceiverInfo = 16;
        static final int TRANSACTION_getRuntimePermissionsVersion = 208;
        static final int TRANSACTION_getServiceInfo = 17;
        static final int TRANSACTION_getServicesSystemSharedLibraryPackageName = 179;
        static final int TRANSACTION_getSharedLibraries = 184;
        static final int TRANSACTION_getSharedSystemSharedLibraryPackageName = 180;
        static final int TRANSACTION_getSigningKeySet = 159;
        static final int TRANSACTION_getSuspendedPackageAppExtras = 84;
        static final int TRANSACTION_getSystemAvailableFeatures = 108;
        static final int TRANSACTION_getSystemCaptionsServicePackageName = 201;
        static final int TRANSACTION_getSystemSharedLibraryNames = 107;
        static final int TRANSACTION_getSystemTextClassifierPackageName = 197;
        static final int TRANSACTION_getUidForSharedUser = 40;
        static final int TRANSACTION_getUnsuspendablePackagesForUser = 82;
        static final int TRANSACTION_getVerifierDeviceIdentity = 144;
        static final int TRANSACTION_getWellbeingPackageName = 199;
        static final int TRANSACTION_getWhitelistedRestrictedPermissions = 29;
        static final int TRANSACTION_grantDefaultPermissionsToActiveLuiApp = 168;
        static final int TRANSACTION_grantDefaultPermissionsToEnabledCarrierApps = 164;
        static final int TRANSACTION_grantDefaultPermissionsToEnabledImsServices = 165;
        static final int TRANSACTION_grantDefaultPermissionsToEnabledTelephonyDataServices = 166;
        static final int TRANSACTION_grantRuntimePermission = 23;
        static final int TRANSACTION_hasSigningCertificate = 195;
        static final int TRANSACTION_hasSystemFeature = 109;
        static final int TRANSACTION_hasSystemUidErrors = 113;
        static final int TRANSACTION_hasUidSigningCertificate = 196;
        static final int TRANSACTION_installExistingPackageAsUser = 134;
        static final int TRANSACTION_isDeviceUpgrading = 147;
        static final int TRANSACTION_isFirstBoot = 145;
        static final int TRANSACTION_isInstantApp = 176;
        static final int TRANSACTION_isOnlyCoreApps = 146;
        static final int TRANSACTION_isPackageAvailable = 2;
        static final int TRANSACTION_isPackageDeviceAdminOnAnyUser = 182;
        static final int TRANSACTION_isPackageSignedByKeySet = 160;
        static final int TRANSACTION_isPackageSignedByKeySetExactly = 161;
        static final int TRANSACTION_isPackageStateProtected = 203;
        static final int TRANSACTION_isPackageSuspendedForUser = 83;
        static final int TRANSACTION_isPermissionEnforced = 149;
        static final int TRANSACTION_isPermissionRevokedByPolicy = 170;
        static final int TRANSACTION_isProtectedBroadcast = 33;
        static final int TRANSACTION_isSafeMode = 111;
        static final int TRANSACTION_isStorageLow = 150;
        static final int TRANSACTION_isUidPrivileged = 43;
        static final int TRANSACTION_logAppProcessStartIfNeeded = 97;
        static final int TRANSACTION_movePackage = 129;
        static final int TRANSACTION_movePrimaryStorage = 130;
        static final int TRANSACTION_notifyDexLoad = 117;
        static final int TRANSACTION_notifyPackageUse = 116;
        static final int TRANSACTION_notifyPackagesReplacedReceived = 210;
        static final int TRANSACTION_performDexOptMode = 119;
        static final int TRANSACTION_performDexOptSecondary = 120;
        static final int TRANSACTION_performFstrimIfNeeded = 114;
        static final int TRANSACTION_queryContentProviders = 60;
        static final int TRANSACTION_queryInstrumentation = 62;
        static final int TRANSACTION_queryIntentActivities = 48;
        static final int TRANSACTION_queryIntentActivityOptions = 49;
        static final int TRANSACTION_queryIntentContentProviders = 53;
        static final int TRANSACTION_queryIntentReceivers = 50;
        static final int TRANSACTION_queryIntentServices = 52;
        static final int TRANSACTION_queryPermissionsByGroup = 10;
        static final int TRANSACTION_querySyncProviders = 59;
        static final int TRANSACTION_reconcileSecondaryDexFiles = 125;
        static final int TRANSACTION_registerDexModule = 118;
        static final int TRANSACTION_registerMoveCallback = 127;
        static final int TRANSACTION_removeOnPermissionsChangeListener = 163;
        static final int TRANSACTION_removePermission = 22;
        static final int TRANSACTION_removeWhitelistedRestrictedPermission = 31;
        static final int TRANSACTION_replacePreferredActivity = 73;
        static final int TRANSACTION_resetApplicationPreferences = 69;
        static final int TRANSACTION_resetRuntimePermissions = 25;
        static final int TRANSACTION_resolveContentProvider = 58;
        static final int TRANSACTION_resolveIntent = 45;
        static final int TRANSACTION_resolveService = 51;
        static final int TRANSACTION_restoreDefaultApps = 88;
        static final int TRANSACTION_restoreIntentFilterVerification = 90;
        static final int TRANSACTION_restorePreferredActivities = 86;
        static final int TRANSACTION_revokeDefaultPermissionsFromDisabledTelephonyDataServices = 167;
        static final int TRANSACTION_revokeDefaultPermissionsFromLuiApps = 169;
        static final int TRANSACTION_revokeRuntimePermission = 24;
        static final int TRANSACTION_runBackgroundDexoptJob = 124;
        static final int TRANSACTION_sendDeviceCustomizationReadyBroadcast = 205;
        static final int TRANSACTION_setApplicationCategoryHint = 65;
        static final int TRANSACTION_setApplicationEnabledSetting = 95;
        static final int TRANSACTION_setApplicationHiddenSettingAsUser = 151;
        static final int TRANSACTION_setBlockUninstallForUser = 156;
        static final int TRANSACTION_setComponentEnabledSetting = 93;
        static final int TRANSACTION_setDefaultBrowserPackageName = 142;
        static final int TRANSACTION_setDistractingPackageRestrictionsAsUser = 80;
        static final int TRANSACTION_setHarmfulAppWarning = 193;
        static final int TRANSACTION_setHomeActivity = 92;
        static final int TRANSACTION_setInstallLocation = 132;
        static final int TRANSACTION_setInstallerPackageName = 64;
        static final int TRANSACTION_setInstantAppCookie = 174;
        static final int TRANSACTION_setLastChosenActivity = 71;
        static final int TRANSACTION_setPackageStoppedState = 99;
        static final int TRANSACTION_setPackagesSuspendedAsUser = 81;
        static final int TRANSACTION_setPermissionEnforced = 148;
        static final int TRANSACTION_setRequiredForSystemUser = 177;
        static final int TRANSACTION_setRuntimePermissionsVersion = 209;
        static final int TRANSACTION_setSystemAppHiddenUntilInstalled = 153;
        static final int TRANSACTION_setSystemAppInstallState = 154;
        static final int TRANSACTION_setUpdateAvailable = 178;
        static final int TRANSACTION_shouldShowRequestPermissionRationale = 32;
        static final int TRANSACTION_systemReady = 112;
        static final int TRANSACTION_unregisterMoveCallback = 128;
        static final int TRANSACTION_updateIntentVerificationStatus = 139;
        static final int TRANSACTION_updatePackagesIfNeeded = 115;
        static final int TRANSACTION_updatePermissionFlags = 27;
        static final int TRANSACTION_updatePermissionFlagsForAllApps = 28;
        static final int TRANSACTION_verifyIntentFilter = 137;
        static final int TRANSACTION_verifyPendingInstall = 135;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPackageManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPackageManager)) {
                return new Proxy(obj);
            }
            return (IPackageManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "checkPackageStartable";
                case 2:
                    return "isPackageAvailable";
                case 3:
                    return "getPackageInfo";
                case 4:
                    return "getPackageInfoVersioned";
                case 5:
                    return "getPackageUid";
                case 6:
                    return "getPackageGids";
                case 7:
                    return "currentToCanonicalPackageNames";
                case 8:
                    return "canonicalToCurrentPackageNames";
                case 9:
                    return "getPermissionInfo";
                case 10:
                    return "queryPermissionsByGroup";
                case 11:
                    return "getPermissionGroupInfo";
                case 12:
                    return "getAllPermissionGroups";
                case 13:
                    return "getApplicationInfo";
                case 14:
                    return "getActivityInfo";
                case 15:
                    return "activitySupportsIntent";
                case 16:
                    return "getReceiverInfo";
                case 17:
                    return "getServiceInfo";
                case 18:
                    return "getProviderInfo";
                case 19:
                    return "checkPermission";
                case 20:
                    return "checkUidPermission";
                case 21:
                    return "addPermission";
                case 22:
                    return "removePermission";
                case 23:
                    return "grantRuntimePermission";
                case 24:
                    return "revokeRuntimePermission";
                case 25:
                    return "resetRuntimePermissions";
                case 26:
                    return "getPermissionFlags";
                case 27:
                    return "updatePermissionFlags";
                case 28:
                    return "updatePermissionFlagsForAllApps";
                case 29:
                    return "getWhitelistedRestrictedPermissions";
                case 30:
                    return "addWhitelistedRestrictedPermission";
                case 31:
                    return "removeWhitelistedRestrictedPermission";
                case 32:
                    return "shouldShowRequestPermissionRationale";
                case 33:
                    return "isProtectedBroadcast";
                case 34:
                    return "checkSignatures";
                case 35:
                    return "checkUidSignatures";
                case 36:
                    return "getAllPackages";
                case 37:
                    return "getPackagesForUid";
                case 38:
                    return "getNameForUid";
                case 39:
                    return "getNamesForUids";
                case 40:
                    return "getUidForSharedUser";
                case 41:
                    return "getFlagsForUid";
                case 42:
                    return "getPrivateFlagsForUid";
                case 43:
                    return "isUidPrivileged";
                case 44:
                    return "getAppOpPermissionPackages";
                case 45:
                    return "resolveIntent";
                case 46:
                    return "findPersistentPreferredActivity";
                case 47:
                    return "canForwardTo";
                case 48:
                    return "queryIntentActivities";
                case 49:
                    return "queryIntentActivityOptions";
                case 50:
                    return "queryIntentReceivers";
                case 51:
                    return "resolveService";
                case 52:
                    return "queryIntentServices";
                case 53:
                    return "queryIntentContentProviders";
                case 54:
                    return "getInstalledPackages";
                case 55:
                    return "getPackagesHoldingPermissions";
                case 56:
                    return "getInstalledApplications";
                case 57:
                    return "getPersistentApplications";
                case 58:
                    return "resolveContentProvider";
                case 59:
                    return "querySyncProviders";
                case 60:
                    return "queryContentProviders";
                case 61:
                    return "getInstrumentationInfo";
                case 62:
                    return "queryInstrumentation";
                case 63:
                    return "finishPackageInstall";
                case 64:
                    return "setInstallerPackageName";
                case 65:
                    return "setApplicationCategoryHint";
                case 66:
                    return "deletePackageAsUser";
                case 67:
                    return "deletePackageVersioned";
                case 68:
                    return "getInstallerPackageName";
                case 69:
                    return "resetApplicationPreferences";
                case 70:
                    return "getLastChosenActivity";
                case 71:
                    return "setLastChosenActivity";
                case 72:
                    return "addPreferredActivity";
                case 73:
                    return "replacePreferredActivity";
                case 74:
                    return "clearPackagePreferredActivities";
                case 75:
                    return "getPreferredActivities";
                case 76:
                    return "addPersistentPreferredActivity";
                case 77:
                    return "clearPackagePersistentPreferredActivities";
                case 78:
                    return "addCrossProfileIntentFilter";
                case 79:
                    return "clearCrossProfileIntentFilters";
                case 80:
                    return "setDistractingPackageRestrictionsAsUser";
                case 81:
                    return "setPackagesSuspendedAsUser";
                case 82:
                    return "getUnsuspendablePackagesForUser";
                case 83:
                    return "isPackageSuspendedForUser";
                case 84:
                    return "getSuspendedPackageAppExtras";
                case 85:
                    return "getPreferredActivityBackup";
                case 86:
                    return "restorePreferredActivities";
                case 87:
                    return "getDefaultAppsBackup";
                case 88:
                    return "restoreDefaultApps";
                case 89:
                    return "getIntentFilterVerificationBackup";
                case 90:
                    return "restoreIntentFilterVerification";
                case 91:
                    return "getHomeActivities";
                case 92:
                    return "setHomeActivity";
                case 93:
                    return "setComponentEnabledSetting";
                case 94:
                    return "getComponentEnabledSetting";
                case 95:
                    return "setApplicationEnabledSetting";
                case 96:
                    return "getApplicationEnabledSetting";
                case 97:
                    return "logAppProcessStartIfNeeded";
                case 98:
                    return "flushPackageRestrictionsAsUser";
                case 99:
                    return "setPackageStoppedState";
                case 100:
                    return "freeStorageAndNotify";
                case 101:
                    return "freeStorage";
                case 102:
                    return "deleteApplicationCacheFiles";
                case 103:
                    return "deleteApplicationCacheFilesAsUser";
                case 104:
                    return "clearApplicationUserData";
                case 105:
                    return "clearApplicationProfileData";
                case 106:
                    return "getPackageSizeInfo";
                case 107:
                    return "getSystemSharedLibraryNames";
                case 108:
                    return "getSystemAvailableFeatures";
                case 109:
                    return "hasSystemFeature";
                case 110:
                    return "enterSafeMode";
                case 111:
                    return "isSafeMode";
                case 112:
                    return "systemReady";
                case 113:
                    return "hasSystemUidErrors";
                case 114:
                    return "performFstrimIfNeeded";
                case 115:
                    return "updatePackagesIfNeeded";
                case 116:
                    return "notifyPackageUse";
                case 117:
                    return "notifyDexLoad";
                case 118:
                    return "registerDexModule";
                case 119:
                    return "performDexOptMode";
                case 120:
                    return "performDexOptSecondary";
                case 121:
                    return "compileLayouts";
                case 122:
                    return "dumpProfiles";
                case 123:
                    return "forceDexOpt";
                case 124:
                    return "runBackgroundDexoptJob";
                case 125:
                    return "reconcileSecondaryDexFiles";
                case 126:
                    return "getMoveStatus";
                case 127:
                    return "registerMoveCallback";
                case 128:
                    return "unregisterMoveCallback";
                case 129:
                    return "movePackage";
                case 130:
                    return "movePrimaryStorage";
                case 131:
                    return "addPermissionAsync";
                case 132:
                    return "setInstallLocation";
                case 133:
                    return "getInstallLocation";
                case 134:
                    return "installExistingPackageAsUser";
                case 135:
                    return "verifyPendingInstall";
                case 136:
                    return "extendVerificationTimeout";
                case 137:
                    return "verifyIntentFilter";
                case 138:
                    return "getIntentVerificationStatus";
                case 139:
                    return "updateIntentVerificationStatus";
                case 140:
                    return "getIntentFilterVerifications";
                case 141:
                    return "getAllIntentFilters";
                case 142:
                    return "setDefaultBrowserPackageName";
                case 143:
                    return "getDefaultBrowserPackageName";
                case 144:
                    return "getVerifierDeviceIdentity";
                case 145:
                    return "isFirstBoot";
                case 146:
                    return "isOnlyCoreApps";
                case 147:
                    return "isDeviceUpgrading";
                case 148:
                    return "setPermissionEnforced";
                case 149:
                    return "isPermissionEnforced";
                case 150:
                    return "isStorageLow";
                case 151:
                    return "setApplicationHiddenSettingAsUser";
                case 152:
                    return "getApplicationHiddenSettingAsUser";
                case 153:
                    return "setSystemAppHiddenUntilInstalled";
                case 154:
                    return "setSystemAppInstallState";
                case 155:
                    return "getPackageInstaller";
                case 156:
                    return "setBlockUninstallForUser";
                case 157:
                    return "getBlockUninstallForUser";
                case 158:
                    return "getKeySetByAlias";
                case 159:
                    return "getSigningKeySet";
                case 160:
                    return "isPackageSignedByKeySet";
                case 161:
                    return "isPackageSignedByKeySetExactly";
                case 162:
                    return "addOnPermissionsChangeListener";
                case 163:
                    return "removeOnPermissionsChangeListener";
                case 164:
                    return "grantDefaultPermissionsToEnabledCarrierApps";
                case 165:
                    return "grantDefaultPermissionsToEnabledImsServices";
                case 166:
                    return "grantDefaultPermissionsToEnabledTelephonyDataServices";
                case 167:
                    return "revokeDefaultPermissionsFromDisabledTelephonyDataServices";
                case 168:
                    return "grantDefaultPermissionsToActiveLuiApp";
                case 169:
                    return "revokeDefaultPermissionsFromLuiApps";
                case 170:
                    return "isPermissionRevokedByPolicy";
                case 171:
                    return "getPermissionControllerPackageName";
                case 172:
                    return "getInstantApps";
                case 173:
                    return "getInstantAppCookie";
                case 174:
                    return "setInstantAppCookie";
                case 175:
                    return "getInstantAppIcon";
                case 176:
                    return "isInstantApp";
                case 177:
                    return "setRequiredForSystemUser";
                case 178:
                    return "setUpdateAvailable";
                case 179:
                    return "getServicesSystemSharedLibraryPackageName";
                case 180:
                    return "getSharedSystemSharedLibraryPackageName";
                case 181:
                    return "getChangedPackages";
                case 182:
                    return "isPackageDeviceAdminOnAnyUser";
                case 183:
                    return "getInstallReason";
                case 184:
                    return "getSharedLibraries";
                case 185:
                    return "getDeclaredSharedLibraries";
                case 186:
                    return "canRequestPackageInstalls";
                case 187:
                    return "deletePreloadsFileCache";
                case 188:
                    return "getInstantAppResolverComponent";
                case 189:
                    return "getInstantAppResolverSettingsComponent";
                case 190:
                    return "getInstantAppInstallerComponent";
                case 191:
                    return "getInstantAppAndroidId";
                case 192:
                    return "getArtManager";
                case 193:
                    return "setHarmfulAppWarning";
                case 194:
                    return "getHarmfulAppWarning";
                case 195:
                    return "hasSigningCertificate";
                case 196:
                    return "hasUidSigningCertificate";
                case 197:
                    return "getSystemTextClassifierPackageName";
                case 198:
                    return "getAttentionServicePackageName";
                case 199:
                    return "getWellbeingPackageName";
                case 200:
                    return "getAppPredictionServicePackageName";
                case 201:
                    return "getSystemCaptionsServicePackageName";
                case 202:
                    return "getIncidentReportApproverPackageName";
                case 203:
                    return "isPackageStateProtected";
                case 204:
                    return "getHwInnerService";
                case 205:
                    return "sendDeviceCustomizationReadyBroadcast";
                case 206:
                    return "getInstalledModules";
                case 207:
                    return "getModuleInfo";
                case 208:
                    return "getRuntimePermissionsVersion";
                case 209:
                    return "setRuntimePermissionsVersion";
                case 210:
                    return "notifyPackagesReplacedReceived";
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
            VersionedPackage _arg0;
            ComponentName _arg02;
            ComponentName _arg03;
            Intent _arg1;
            ComponentName _arg04;
            ComponentName _arg05;
            ComponentName _arg06;
            PermissionInfo _arg07;
            Intent _arg08;
            Intent _arg09;
            Intent _arg010;
            Intent _arg011;
            ComponentName _arg012;
            Intent _arg3;
            Intent _arg013;
            Intent _arg014;
            Intent _arg015;
            Intent _arg016;
            ComponentName _arg017;
            VersionedPackage _arg018;
            Intent _arg019;
            Intent _arg020;
            IntentFilter _arg32;
            ComponentName _arg5;
            IntentFilter _arg021;
            ComponentName _arg33;
            IntentFilter _arg022;
            ComponentName _arg34;
            IntentFilter _arg023;
            ComponentName _arg12;
            IntentFilter _arg024;
            PersistableBundle _arg2;
            PersistableBundle _arg35;
            SuspendDialogInfo _arg4;
            ComponentName _arg025;
            ComponentName _arg026;
            ComponentName _arg027;
            IntentSender _arg36;
            PermissionInfo _arg028;
            KeySet _arg13;
            KeySet _arg14;
            CharSequence _arg15;
            if (code != 1598968902) {
                IBinder iBinder = null;
                boolean _arg16 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        checkPackageStartable(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPackageAvailable = isPackageAvailable(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isPackageAvailable ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        PackageInfo _result = getPackageInfo(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = VersionedPackage.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        PackageInfo _result2 = getPackageInfoVersioned(_arg0, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getPackageUid(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result4 = getPackageGids(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result5 = currentToCanonicalPackageNames(data.createStringArray());
                        reply.writeNoException();
                        reply.writeStringArray(_result5);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result6 = canonicalToCurrentPackageNames(data.createStringArray());
                        reply.writeNoException();
                        reply.writeStringArray(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        PermissionInfo _result7 = getPermissionInfo(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result8 = queryPermissionsByGroup(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        PermissionGroupInfo _result9 = getPermissionGroupInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result10 = getAllPermissionGroups(data.readInt());
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        ApplicationInfo _result11 = getApplicationInfo(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        ActivityInfo _result12 = getActivityInfo(_arg02, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        boolean activitySupportsIntent = activitySupportsIntent(_arg03, _arg1, data.readString());
                        reply.writeNoException();
                        reply.writeInt(activitySupportsIntent ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        ActivityInfo _result13 = getReceiverInfo(_arg04, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result13 != null) {
                            reply.writeInt(1);
                            _result13.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        ServiceInfo _result14 = getServiceInfo(_arg05, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result14 != null) {
                            reply.writeInt(1);
                            _result14.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        ProviderInfo _result15 = getProviderInfo(_arg06, data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result15 != null) {
                            reply.writeInt(1);
                            _result15.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = checkPermission(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = checkUidPermission(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = PermissionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        boolean addPermission = addPermission(_arg07);
                        reply.writeNoException();
                        reply.writeInt(addPermission ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        removePermission(data.readString());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        grantRuntimePermission(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        revokeRuntimePermission(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        resetRuntimePermissions();
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = getPermissionFlags(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        updatePermissionFlags(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        updatePermissionFlagsForAllApps(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result19 = getWhitelistedRestrictedPermissions(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result19);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addWhitelistedRestrictedPermission = addWhitelistedRestrictedPermission(data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(addWhitelistedRestrictedPermission ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeWhitelistedRestrictedPermission = removeWhitelistedRestrictedPermission(data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeWhitelistedRestrictedPermission ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(shouldShowRequestPermissionRationale ? 1 : 0);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isProtectedBroadcast = isProtectedBroadcast(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isProtectedBroadcast ? 1 : 0);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = checkSignatures(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = checkUidSignatures(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result22 = getAllPackages();
                        reply.writeNoException();
                        reply.writeStringList(_result22);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result23 = getPackagesForUid(data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result23);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        String _result24 = getNameForUid(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result24);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result25 = getNamesForUids(data.createIntArray());
                        reply.writeNoException();
                        reply.writeStringArray(_result25);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = getUidForSharedUser(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = getFlagsForUid(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = getPrivateFlagsForUid(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUidPrivileged = isUidPrivileged(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUidPrivileged ? 1 : 0);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result29 = getAppOpPermissionPackages(data.readString());
                        reply.writeNoException();
                        reply.writeStringArray(_result29);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        ResolveInfo _result30 = resolveIntent(_arg08, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result30 != null) {
                            reply.writeInt(1);
                            _result30.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        ResolveInfo _result31 = findPersistentPreferredActivity(_arg09, data.readInt());
                        reply.writeNoException();
                        if (_result31 != null) {
                            reply.writeInt(1);
                            _result31.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg010 = null;
                        }
                        boolean canForwardTo = canForwardTo(_arg010, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(canForwardTo ? 1 : 0);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg011 = null;
                        }
                        ParceledListSlice _result32 = queryIntentActivities(_arg011, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result32 != null) {
                            reply.writeInt(1);
                            _result32.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg012 = null;
                        }
                        Intent[] _arg17 = (Intent[]) data.createTypedArray(Intent.CREATOR);
                        String[] _arg22 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg3 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        ParceledListSlice _result33 = queryIntentActivityOptions(_arg012, _arg17, _arg22, _arg3, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result33 != null) {
                            reply.writeInt(1);
                            _result33.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg013 = null;
                        }
                        ParceledListSlice _result34 = queryIntentReceivers(_arg013, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result34 != null) {
                            reply.writeInt(1);
                            _result34.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg014 = null;
                        }
                        ResolveInfo _result35 = resolveService(_arg014, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result35 != null) {
                            reply.writeInt(1);
                            _result35.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg015 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg015 = null;
                        }
                        ParceledListSlice _result36 = queryIntentServices(_arg015, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result36 != null) {
                            reply.writeInt(1);
                            _result36.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg016 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg016 = null;
                        }
                        ParceledListSlice _result37 = queryIntentContentProviders(_arg016, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result37 != null) {
                            reply.writeInt(1);
                            _result37.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result38 = getInstalledPackages(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result38 != null) {
                            reply.writeInt(1);
                            _result38.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result39 = getPackagesHoldingPermissions(data.createStringArray(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result39 != null) {
                            reply.writeInt(1);
                            _result39.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result40 = getInstalledApplications(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result40 != null) {
                            reply.writeInt(1);
                            _result40.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result41 = getPersistentApplications(data.readInt());
                        reply.writeNoException();
                        if (_result41 != null) {
                            reply.writeInt(1);
                            _result41.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        ProviderInfo _result42 = resolveContentProvider(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result42 != null) {
                            reply.writeInt(1);
                            _result42.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _arg029 = data.createStringArrayList();
                        ArrayList createTypedArrayList = data.createTypedArrayList(ProviderInfo.CREATOR);
                        querySyncProviders(_arg029, createTypedArrayList);
                        reply.writeNoException();
                        reply.writeStringList(_arg029);
                        reply.writeTypedList(createTypedArrayList);
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result43 = queryContentProviders(data.readString(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result43 != null) {
                            reply.writeInt(1);
                            _result43.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg017 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg017 = null;
                        }
                        InstrumentationInfo _result44 = getInstrumentationInfo(_arg017, data.readInt());
                        reply.writeNoException();
                        if (_result44 != null) {
                            reply.writeInt(1);
                            _result44.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result45 = queryInstrumentation(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result45 != null) {
                            reply.writeInt(1);
                            _result45.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg030 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        finishPackageInstall(_arg030, _arg16);
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        setInstallerPackageName(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        setApplicationCategoryHint(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        deletePackageAsUser(data.readString(), data.readInt(), IPackageDeleteObserver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = VersionedPackage.CREATOR.createFromParcel(data);
                        } else {
                            _arg018 = null;
                        }
                        deletePackageVersioned(_arg018, IPackageDeleteObserver2.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        String _result46 = getInstallerPackageName(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result46);
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        resetApplicationPreferences(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg019 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg019 = null;
                        }
                        ResolveInfo _result47 = getLastChosenActivity(_arg019, data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result47 != null) {
                            reply.writeInt(1);
                            _result47.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg020 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg020 = null;
                        }
                        String _arg18 = data.readString();
                        int _arg23 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg32 = IntentFilter.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        int _arg42 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        setLastChosenActivity(_arg020, _arg18, _arg23, _arg32, _arg42, _arg5);
                        reply.writeNoException();
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg021 = IntentFilter.CREATOR.createFromParcel(data);
                        } else {
                            _arg021 = null;
                        }
                        int _arg19 = data.readInt();
                        ComponentName[] _arg24 = (ComponentName[]) data.createTypedArray(ComponentName.CREATOR);
                        if (data.readInt() != 0) {
                            _arg33 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg33 = null;
                        }
                        addPreferredActivity(_arg021, _arg19, _arg24, _arg33, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg022 = IntentFilter.CREATOR.createFromParcel(data);
                        } else {
                            _arg022 = null;
                        }
                        int _arg110 = data.readInt();
                        ComponentName[] _arg25 = (ComponentName[]) data.createTypedArray(ComponentName.CREATOR);
                        if (data.readInt() != 0) {
                            _arg34 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg34 = null;
                        }
                        replacePreferredActivity(_arg022, _arg110, _arg25, _arg34, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        clearPackagePreferredActivities(data.readString());
                        reply.writeNoException();
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        ArrayList arrayList = new ArrayList();
                        ArrayList arrayList2 = new ArrayList();
                        int _result48 = getPreferredActivities(arrayList, arrayList2, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result48);
                        reply.writeTypedList(arrayList);
                        reply.writeTypedList(arrayList2);
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg023 = IntentFilter.CREATOR.createFromParcel(data);
                        } else {
                            _arg023 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        addPersistentPreferredActivity(_arg023, _arg12, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        clearPackagePersistentPreferredActivities(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg024 = IntentFilter.CREATOR.createFromParcel(data);
                        } else {
                            _arg024 = null;
                        }
                        addCrossProfileIntentFilter(_arg024, data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        clearCrossProfileIntentFilters(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result49 = setDistractingPackageRestrictionsAsUser(data.createStringArray(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result49);
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _arg031 = data.createStringArray();
                        boolean _arg111 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg2 = PersistableBundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg35 = PersistableBundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg35 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg4 = SuspendDialogInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        String[] _result50 = setPackagesSuspendedAsUser(_arg031, _arg111, _arg2, _arg35, _arg4, data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result50);
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result51 = getUnsuspendablePackagesForUser(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result51);
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPackageSuspendedForUser = isPackageSuspendedForUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isPackageSuspendedForUser ? 1 : 0);
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        PersistableBundle _result52 = getSuspendedPackageAppExtras(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result52 != null) {
                            reply.writeInt(1);
                            _result52.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result53 = getPreferredActivityBackup(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result53);
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        restorePreferredActivities(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result54 = getDefaultAppsBackup(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result54);
                        return true;
                    case 88:
                        data.enforceInterface(DESCRIPTOR);
                        restoreDefaultApps(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 89:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result55 = getIntentFilterVerificationBackup(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result55);
                        return true;
                    case 90:
                        data.enforceInterface(DESCRIPTOR);
                        restoreIntentFilterVerification(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 91:
                        data.enforceInterface(DESCRIPTOR);
                        ArrayList arrayList3 = new ArrayList();
                        ComponentName _result56 = getHomeActivities(arrayList3);
                        reply.writeNoException();
                        if (_result56 != null) {
                            reply.writeInt(1);
                            _result56.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        reply.writeTypedList(arrayList3);
                        return true;
                    case 92:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg025 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg025 = null;
                        }
                        setHomeActivity(_arg025, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 93:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg026 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg026 = null;
                        }
                        setComponentEnabledSetting(_arg026, data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 94:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg027 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg027 = null;
                        }
                        int _result57 = getComponentEnabledSetting(_arg027, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result57);
                        return true;
                    case 95:
                        data.enforceInterface(DESCRIPTOR);
                        setApplicationEnabledSetting(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 96:
                        data.enforceInterface(DESCRIPTOR);
                        int _result58 = getApplicationEnabledSetting(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result58);
                        return true;
                    case 97:
                        data.enforceInterface(DESCRIPTOR);
                        logAppProcessStartIfNeeded(data.readString(), data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 98:
                        data.enforceInterface(DESCRIPTOR);
                        flushPackageRestrictionsAsUser(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 99:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg032 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        setPackageStoppedState(_arg032, _arg16, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 100:
                        data.enforceInterface(DESCRIPTOR);
                        freeStorageAndNotify(data.readString(), data.readLong(), data.readInt(), IPackageDataObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 101:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg033 = data.readString();
                        long _arg112 = data.readLong();
                        int _arg26 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg36 = IntentSender.CREATOR.createFromParcel(data);
                        } else {
                            _arg36 = null;
                        }
                        freeStorage(_arg033, _arg112, _arg26, _arg36);
                        reply.writeNoException();
                        return true;
                    case 102:
                        data.enforceInterface(DESCRIPTOR);
                        deleteApplicationCacheFiles(data.readString(), IPackageDataObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 103:
                        data.enforceInterface(DESCRIPTOR);
                        deleteApplicationCacheFilesAsUser(data.readString(), data.readInt(), IPackageDataObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 104:
                        data.enforceInterface(DESCRIPTOR);
                        clearApplicationUserData(data.readString(), IPackageDataObserver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 105:
                        data.enforceInterface(DESCRIPTOR);
                        clearApplicationProfileData(data.readString());
                        reply.writeNoException();
                        return true;
                    case 106:
                        data.enforceInterface(DESCRIPTOR);
                        getPackageSizeInfo(data.readString(), data.readInt(), IPackageStatsObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 107:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result59 = getSystemSharedLibraryNames();
                        reply.writeNoException();
                        reply.writeStringArray(_result59);
                        return true;
                    case 108:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result60 = getSystemAvailableFeatures();
                        reply.writeNoException();
                        if (_result60 != null) {
                            reply.writeInt(1);
                            _result60.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 109:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasSystemFeature = hasSystemFeature(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasSystemFeature ? 1 : 0);
                        return true;
                    case 110:
                        data.enforceInterface(DESCRIPTOR);
                        enterSafeMode();
                        reply.writeNoException();
                        return true;
                    case 111:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSafeMode = isSafeMode();
                        reply.writeNoException();
                        reply.writeInt(isSafeMode ? 1 : 0);
                        return true;
                    case 112:
                        data.enforceInterface(DESCRIPTOR);
                        systemReady();
                        reply.writeNoException();
                        return true;
                    case 113:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasSystemUidErrors = hasSystemUidErrors();
                        reply.writeNoException();
                        reply.writeInt(hasSystemUidErrors ? 1 : 0);
                        return true;
                    case 114:
                        data.enforceInterface(DESCRIPTOR);
                        performFstrimIfNeeded();
                        reply.writeNoException();
                        return true;
                    case 115:
                        data.enforceInterface(DESCRIPTOR);
                        updatePackagesIfNeeded();
                        reply.writeNoException();
                        return true;
                    case 116:
                        data.enforceInterface(DESCRIPTOR);
                        notifyPackageUse(data.readString(), data.readInt());
                        return true;
                    case 117:
                        data.enforceInterface(DESCRIPTOR);
                        notifyDexLoad(data.readString(), data.createStringArrayList(), data.createStringArrayList(), data.readString());
                        return true;
                    case 118:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg034 = data.readString();
                        String _arg113 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        registerDexModule(_arg034, _arg113, _arg16, IDexModuleRegisterCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 119:
                        data.enforceInterface(DESCRIPTOR);
                        boolean performDexOptMode = performDexOptMode(data.readString(), data.readInt() != 0, data.readString(), data.readInt() != 0, data.readInt() != 0, data.readString());
                        reply.writeNoException();
                        reply.writeInt(performDexOptMode ? 1 : 0);
                        return true;
                    case 120:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg035 = data.readString();
                        String _arg114 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        boolean performDexOptSecondary = performDexOptSecondary(_arg035, _arg114, _arg16);
                        reply.writeNoException();
                        reply.writeInt(performDexOptSecondary ? 1 : 0);
                        return true;
                    case 121:
                        data.enforceInterface(DESCRIPTOR);
                        boolean compileLayouts = compileLayouts(data.readString());
                        reply.writeNoException();
                        reply.writeInt(compileLayouts ? 1 : 0);
                        return true;
                    case 122:
                        data.enforceInterface(DESCRIPTOR);
                        dumpProfiles(data.readString());
                        reply.writeNoException();
                        return true;
                    case 123:
                        data.enforceInterface(DESCRIPTOR);
                        forceDexOpt(data.readString());
                        reply.writeNoException();
                        return true;
                    case 124:
                        data.enforceInterface(DESCRIPTOR);
                        boolean runBackgroundDexoptJob = runBackgroundDexoptJob(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(runBackgroundDexoptJob ? 1 : 0);
                        return true;
                    case 125:
                        data.enforceInterface(DESCRIPTOR);
                        reconcileSecondaryDexFiles(data.readString());
                        reply.writeNoException();
                        return true;
                    case 126:
                        data.enforceInterface(DESCRIPTOR);
                        int _result61 = getMoveStatus(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result61);
                        return true;
                    case 127:
                        data.enforceInterface(DESCRIPTOR);
                        registerMoveCallback(IPackageMoveObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 128:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterMoveCallback(IPackageMoveObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 129:
                        data.enforceInterface(DESCRIPTOR);
                        int _result62 = movePackage(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result62);
                        return true;
                    case 130:
                        data.enforceInterface(DESCRIPTOR);
                        int _result63 = movePrimaryStorage(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result63);
                        return true;
                    case 131:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg028 = PermissionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg028 = null;
                        }
                        boolean addPermissionAsync = addPermissionAsync(_arg028);
                        reply.writeNoException();
                        reply.writeInt(addPermissionAsync ? 1 : 0);
                        return true;
                    case 132:
                        data.enforceInterface(DESCRIPTOR);
                        boolean installLocation = setInstallLocation(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(installLocation ? 1 : 0);
                        return true;
                    case 133:
                        data.enforceInterface(DESCRIPTOR);
                        int _result64 = getInstallLocation();
                        reply.writeNoException();
                        reply.writeInt(_result64);
                        return true;
                    case 134:
                        data.enforceInterface(DESCRIPTOR);
                        int _result65 = installExistingPackageAsUser(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(_result65);
                        return true;
                    case 135:
                        data.enforceInterface(DESCRIPTOR);
                        verifyPendingInstall(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 136:
                        data.enforceInterface(DESCRIPTOR);
                        extendVerificationTimeout(data.readInt(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 137:
                        data.enforceInterface(DESCRIPTOR);
                        verifyIntentFilter(data.readInt(), data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 138:
                        data.enforceInterface(DESCRIPTOR);
                        int _result66 = getIntentVerificationStatus(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result66);
                        return true;
                    case 139:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateIntentVerificationStatus = updateIntentVerificationStatus(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(updateIntentVerificationStatus ? 1 : 0);
                        return true;
                    case 140:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result67 = getIntentFilterVerifications(data.readString());
                        reply.writeNoException();
                        if (_result67 != null) {
                            reply.writeInt(1);
                            _result67.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 141:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result68 = getAllIntentFilters(data.readString());
                        reply.writeNoException();
                        if (_result68 != null) {
                            reply.writeInt(1);
                            _result68.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 142:
                        data.enforceInterface(DESCRIPTOR);
                        boolean defaultBrowserPackageName = setDefaultBrowserPackageName(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(defaultBrowserPackageName ? 1 : 0);
                        return true;
                    case 143:
                        data.enforceInterface(DESCRIPTOR);
                        String _result69 = getDefaultBrowserPackageName(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result69);
                        return true;
                    case 144:
                        data.enforceInterface(DESCRIPTOR);
                        VerifierDeviceIdentity _result70 = getVerifierDeviceIdentity();
                        reply.writeNoException();
                        if (_result70 != null) {
                            reply.writeInt(1);
                            _result70.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 145:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFirstBoot = isFirstBoot();
                        reply.writeNoException();
                        reply.writeInt(isFirstBoot ? 1 : 0);
                        return true;
                    case 146:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isOnlyCoreApps = isOnlyCoreApps();
                        reply.writeNoException();
                        reply.writeInt(isOnlyCoreApps ? 1 : 0);
                        return true;
                    case 147:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDeviceUpgrading = isDeviceUpgrading();
                        reply.writeNoException();
                        reply.writeInt(isDeviceUpgrading ? 1 : 0);
                        return true;
                    case 148:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg036 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        setPermissionEnforced(_arg036, _arg16);
                        reply.writeNoException();
                        return true;
                    case 149:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPermissionEnforced = isPermissionEnforced(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isPermissionEnforced ? 1 : 0);
                        return true;
                    case 150:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isStorageLow = isStorageLow();
                        reply.writeNoException();
                        reply.writeInt(isStorageLow ? 1 : 0);
                        return true;
                    case 151:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg037 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        boolean applicationHiddenSettingAsUser = setApplicationHiddenSettingAsUser(_arg037, _arg16, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(applicationHiddenSettingAsUser ? 1 : 0);
                        return true;
                    case 152:
                        data.enforceInterface(DESCRIPTOR);
                        boolean applicationHiddenSettingAsUser2 = getApplicationHiddenSettingAsUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(applicationHiddenSettingAsUser2 ? 1 : 0);
                        return true;
                    case 153:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg038 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        setSystemAppHiddenUntilInstalled(_arg038, _arg16);
                        reply.writeNoException();
                        return true;
                    case 154:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg039 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        boolean systemAppInstallState = setSystemAppInstallState(_arg039, _arg16, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(systemAppInstallState ? 1 : 0);
                        return true;
                    case 155:
                        data.enforceInterface(DESCRIPTOR);
                        IPackageInstaller _result71 = getPackageInstaller();
                        reply.writeNoException();
                        if (_result71 != null) {
                            iBinder = _result71.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 156:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg040 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        boolean blockUninstallForUser = setBlockUninstallForUser(_arg040, _arg16, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(blockUninstallForUser ? 1 : 0);
                        return true;
                    case 157:
                        data.enforceInterface(DESCRIPTOR);
                        boolean blockUninstallForUser2 = getBlockUninstallForUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(blockUninstallForUser2 ? 1 : 0);
                        return true;
                    case 158:
                        data.enforceInterface(DESCRIPTOR);
                        KeySet _result72 = getKeySetByAlias(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result72 != null) {
                            reply.writeInt(1);
                            _result72.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 159:
                        data.enforceInterface(DESCRIPTOR);
                        KeySet _result73 = getSigningKeySet(data.readString());
                        reply.writeNoException();
                        if (_result73 != null) {
                            reply.writeInt(1);
                            _result73.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 160:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg041 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = KeySet.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean isPackageSignedByKeySet = isPackageSignedByKeySet(_arg041, _arg13);
                        reply.writeNoException();
                        reply.writeInt(isPackageSignedByKeySet ? 1 : 0);
                        return true;
                    case 161:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg042 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = KeySet.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        boolean isPackageSignedByKeySetExactly = isPackageSignedByKeySetExactly(_arg042, _arg14);
                        reply.writeNoException();
                        reply.writeInt(isPackageSignedByKeySetExactly ? 1 : 0);
                        return true;
                    case 162:
                        data.enforceInterface(DESCRIPTOR);
                        addOnPermissionsChangeListener(IOnPermissionsChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 163:
                        data.enforceInterface(DESCRIPTOR);
                        removeOnPermissionsChangeListener(IOnPermissionsChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 164:
                        data.enforceInterface(DESCRIPTOR);
                        grantDefaultPermissionsToEnabledCarrierApps(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 165:
                        data.enforceInterface(DESCRIPTOR);
                        grantDefaultPermissionsToEnabledImsServices(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 166:
                        data.enforceInterface(DESCRIPTOR);
                        grantDefaultPermissionsToEnabledTelephonyDataServices(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 167:
                        data.enforceInterface(DESCRIPTOR);
                        revokeDefaultPermissionsFromDisabledTelephonyDataServices(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 168:
                        data.enforceInterface(DESCRIPTOR);
                        grantDefaultPermissionsToActiveLuiApp(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 169:
                        data.enforceInterface(DESCRIPTOR);
                        revokeDefaultPermissionsFromLuiApps(data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 170:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPermissionRevokedByPolicy = isPermissionRevokedByPolicy(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isPermissionRevokedByPolicy ? 1 : 0);
                        return true;
                    case 171:
                        data.enforceInterface(DESCRIPTOR);
                        String _result74 = getPermissionControllerPackageName();
                        reply.writeNoException();
                        reply.writeString(_result74);
                        return true;
                    case 172:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result75 = getInstantApps(data.readInt());
                        reply.writeNoException();
                        if (_result75 != null) {
                            reply.writeInt(1);
                            _result75.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 173:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result76 = getInstantAppCookie(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result76);
                        return true;
                    case 174:
                        data.enforceInterface(DESCRIPTOR);
                        boolean instantAppCookie = setInstantAppCookie(data.readString(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(instantAppCookie ? 1 : 0);
                        return true;
                    case 175:
                        data.enforceInterface(DESCRIPTOR);
                        Bitmap _result77 = getInstantAppIcon(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result77 != null) {
                            reply.writeInt(1);
                            _result77.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 176:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInstantApp = isInstantApp(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isInstantApp ? 1 : 0);
                        return true;
                    case 177:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg043 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        boolean requiredForSystemUser = setRequiredForSystemUser(_arg043, _arg16);
                        reply.writeNoException();
                        reply.writeInt(requiredForSystemUser ? 1 : 0);
                        return true;
                    case 178:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg044 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        setUpdateAvailable(_arg044, _arg16);
                        reply.writeNoException();
                        return true;
                    case 179:
                        data.enforceInterface(DESCRIPTOR);
                        String _result78 = getServicesSystemSharedLibraryPackageName();
                        reply.writeNoException();
                        reply.writeString(_result78);
                        return true;
                    case 180:
                        data.enforceInterface(DESCRIPTOR);
                        String _result79 = getSharedSystemSharedLibraryPackageName();
                        reply.writeNoException();
                        reply.writeString(_result79);
                        return true;
                    case 181:
                        data.enforceInterface(DESCRIPTOR);
                        ChangedPackages _result80 = getChangedPackages(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result80 != null) {
                            reply.writeInt(1);
                            _result80.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 182:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPackageDeviceAdminOnAnyUser = isPackageDeviceAdminOnAnyUser(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isPackageDeviceAdminOnAnyUser ? 1 : 0);
                        return true;
                    case 183:
                        data.enforceInterface(DESCRIPTOR);
                        int _result81 = getInstallReason(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result81);
                        return true;
                    case 184:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result82 = getSharedLibraries(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result82 != null) {
                            reply.writeInt(1);
                            _result82.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 185:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result83 = getDeclaredSharedLibraries(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result83 != null) {
                            reply.writeInt(1);
                            _result83.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 186:
                        data.enforceInterface(DESCRIPTOR);
                        boolean canRequestPackageInstalls = canRequestPackageInstalls(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(canRequestPackageInstalls ? 1 : 0);
                        return true;
                    case 187:
                        data.enforceInterface(DESCRIPTOR);
                        deletePreloadsFileCache();
                        reply.writeNoException();
                        return true;
                    case 188:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result84 = getInstantAppResolverComponent();
                        reply.writeNoException();
                        if (_result84 != null) {
                            reply.writeInt(1);
                            _result84.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 189:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result85 = getInstantAppResolverSettingsComponent();
                        reply.writeNoException();
                        if (_result85 != null) {
                            reply.writeInt(1);
                            _result85.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 190:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result86 = getInstantAppInstallerComponent();
                        reply.writeNoException();
                        if (_result86 != null) {
                            reply.writeInt(1);
                            _result86.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 191:
                        data.enforceInterface(DESCRIPTOR);
                        String _result87 = getInstantAppAndroidId(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result87);
                        return true;
                    case 192:
                        data.enforceInterface(DESCRIPTOR);
                        IArtManager _result88 = getArtManager();
                        reply.writeNoException();
                        if (_result88 != null) {
                            iBinder = _result88.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 193:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg045 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        setHarmfulAppWarning(_arg045, _arg15, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 194:
                        data.enforceInterface(DESCRIPTOR);
                        CharSequence _result89 = getHarmfulAppWarning(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result89 != null) {
                            reply.writeInt(1);
                            TextUtils.writeToParcel(_result89, reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 195:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasSigningCertificate = hasSigningCertificate(data.readString(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasSigningCertificate ? 1 : 0);
                        return true;
                    case 196:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasUidSigningCertificate = hasUidSigningCertificate(data.readInt(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasUidSigningCertificate ? 1 : 0);
                        return true;
                    case 197:
                        data.enforceInterface(DESCRIPTOR);
                        String _result90 = getSystemTextClassifierPackageName();
                        reply.writeNoException();
                        reply.writeString(_result90);
                        return true;
                    case 198:
                        data.enforceInterface(DESCRIPTOR);
                        String _result91 = getAttentionServicePackageName();
                        reply.writeNoException();
                        reply.writeString(_result91);
                        return true;
                    case 199:
                        data.enforceInterface(DESCRIPTOR);
                        String _result92 = getWellbeingPackageName();
                        reply.writeNoException();
                        reply.writeString(_result92);
                        return true;
                    case 200:
                        data.enforceInterface(DESCRIPTOR);
                        String _result93 = getAppPredictionServicePackageName();
                        reply.writeNoException();
                        reply.writeString(_result93);
                        return true;
                    case 201:
                        data.enforceInterface(DESCRIPTOR);
                        String _result94 = getSystemCaptionsServicePackageName();
                        reply.writeNoException();
                        reply.writeString(_result94);
                        return true;
                    case 202:
                        data.enforceInterface(DESCRIPTOR);
                        String _result95 = getIncidentReportApproverPackageName();
                        reply.writeNoException();
                        reply.writeString(_result95);
                        return true;
                    case 203:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPackageStateProtected = isPackageStateProtected(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isPackageStateProtected ? 1 : 0);
                        return true;
                    case 204:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result96 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result96);
                        return true;
                    case 205:
                        data.enforceInterface(DESCRIPTOR);
                        sendDeviceCustomizationReadyBroadcast();
                        reply.writeNoException();
                        return true;
                    case 206:
                        data.enforceInterface(DESCRIPTOR);
                        List<ModuleInfo> _result97 = getInstalledModules(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result97);
                        return true;
                    case 207:
                        data.enforceInterface(DESCRIPTOR);
                        ModuleInfo _result98 = getModuleInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result98 != null) {
                            reply.writeInt(1);
                            _result98.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 208:
                        data.enforceInterface(DESCRIPTOR);
                        int _result99 = getRuntimePermissionsVersion(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result99);
                        return true;
                    case 209:
                        data.enforceInterface(DESCRIPTOR);
                        setRuntimePermissionsVersion(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 210:
                        data.enforceInterface(DESCRIPTOR);
                        notifyPackagesReplacedReceived(data.createStringArray());
                        reply.writeNoException();
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
        public static class Proxy implements IPackageManager {
            public static IPackageManager sDefaultImpl;
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

            @Override // android.content.pm.IPackageManager
            public void checkPackageStartable(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().checkPackageStartable(packageName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean isPackageAvailable(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPackageAvailable(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public PackageInfo getPackageInfo(String packageName, int flags, int userId) throws RemoteException {
                PackageInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageInfo(packageName, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PackageInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public PackageInfo getPackageInfoVersioned(VersionedPackage versionedPackage, int flags, int userId) throws RemoteException {
                PackageInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (versionedPackage != null) {
                        _data.writeInt(1);
                        versionedPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageInfoVersioned(versionedPackage, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PackageInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public int getPackageUid(String packageName, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageUid(packageName, flags, userId);
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

            @Override // android.content.pm.IPackageManager
            public int[] getPackageGids(String packageName, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageGids(packageName, flags, userId);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public String[] currentToCanonicalPackageNames(String[] names) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(names);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().currentToCanonicalPackageNames(names);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public String[] canonicalToCurrentPackageNames(String[] names) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(names);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canonicalToCurrentPackageNames(names);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public PermissionInfo getPermissionInfo(String name, String packageName, int flags) throws RemoteException {
                PermissionInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPermissionInfo(name, packageName, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PermissionInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice queryPermissionsByGroup(String group, int flags) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(group);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryPermissionsByGroup(group, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException {
                PermissionGroupInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPermissionGroupInfo(name, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PermissionGroupInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getAllPermissionGroups(int flags) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllPermissionGroups(flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException {
                ApplicationInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApplicationInfo(packageName, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ApplicationInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ActivityInfo getActivityInfo(ComponentName className, int flags, int userId) throws RemoteException {
                ActivityInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActivityInfo(className, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public boolean activitySupportsIntent(ComponentName className, Intent intent, String resolvedType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().activitySupportsIntent(className, intent, resolvedType);
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

            @Override // android.content.pm.IPackageManager
            public ActivityInfo getReceiverInfo(ComponentName className, int flags, int userId) throws RemoteException {
                ActivityInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getReceiverInfo(className, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ServiceInfo getServiceInfo(ComponentName className, int flags, int userId) throws RemoteException {
                ServiceInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServiceInfo(className, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ServiceInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ProviderInfo getProviderInfo(ComponentName className, int flags, int userId) throws RemoteException {
                ProviderInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProviderInfo(className, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ProviderInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public int checkPermission(String permName, String pkgName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permName);
                    _data.writeString(pkgName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkPermission(permName, pkgName, userId);
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

            @Override // android.content.pm.IPackageManager
            public int checkUidPermission(String permName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permName);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkUidPermission(permName, uid);
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

            @Override // android.content.pm.IPackageManager
            public boolean addPermission(PermissionInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addPermission(info);
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

            @Override // android.content.pm.IPackageManager
            public void removePermission(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removePermission(name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void grantRuntimePermission(String packageName, String permissionName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(permissionName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().grantRuntimePermission(packageName, permissionName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void revokeRuntimePermission(String packageName, String permissionName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(permissionName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().revokeRuntimePermission(packageName, permissionName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void resetRuntimePermissions() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetRuntimePermissions();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public int getPermissionFlags(String permissionName, String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPermissionFlags(permissionName, packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, boolean checkAdjustPolicyFlagPermission, int userId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(permissionName);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                        try {
                            _data.writeInt(flagMask);
                            try {
                                _data.writeInt(flagValues);
                                _data.writeInt(checkAdjustPolicyFlagPermission ? 1 : 0);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                            try {
                                _data.writeInt(userId);
                            } catch (Throwable th4) {
                                th = th4;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().updatePermissionFlags(permissionName, packageName, flagMask, flagValues, checkAdjustPolicyFlagPermission, userId);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.content.pm.IPackageManager
            public void updatePermissionFlagsForAllApps(int flagMask, int flagValues, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flagMask);
                    _data.writeInt(flagValues);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updatePermissionFlagsForAllApps(flagMask, flagValues, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public List<String> getWhitelistedRestrictedPermissions(String packageName, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWhitelistedRestrictedPermissions(packageName, flags, userId);
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

            @Override // android.content.pm.IPackageManager
            public boolean addWhitelistedRestrictedPermission(String packageName, String permission, int whitelistFlags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(permission);
                    _data.writeInt(whitelistFlags);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addWhitelistedRestrictedPermission(packageName, permission, whitelistFlags, userId);
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

            @Override // android.content.pm.IPackageManager
            public boolean removeWhitelistedRestrictedPermission(String packageName, String permission, int whitelistFlags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(permission);
                    _data.writeInt(whitelistFlags);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeWhitelistedRestrictedPermission(packageName, permission, whitelistFlags, userId);
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

            @Override // android.content.pm.IPackageManager
            public boolean shouldShowRequestPermissionRationale(String permissionName, String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldShowRequestPermissionRationale(permissionName, packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public boolean isProtectedBroadcast(String actionName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(actionName);
                    boolean _result = false;
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isProtectedBroadcast(actionName);
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

            @Override // android.content.pm.IPackageManager
            public int checkSignatures(String pkg1, String pkg2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg1);
                    _data.writeString(pkg2);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkSignatures(pkg1, pkg2);
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

            @Override // android.content.pm.IPackageManager
            public int checkUidSignatures(int uid1, int uid2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid1);
                    _data.writeInt(uid2);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkUidSignatures(uid1, uid2);
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

            @Override // android.content.pm.IPackageManager
            public List<String> getAllPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllPackages();
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

            @Override // android.content.pm.IPackageManager
            public String[] getPackagesForUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackagesForUid(uid);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public String getNameForUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNameForUid(uid);
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

            @Override // android.content.pm.IPackageManager
            public String[] getNamesForUids(int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uids);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNamesForUids(uids);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public int getUidForSharedUser(String sharedUserName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sharedUserName);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUidForSharedUser(sharedUserName);
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

            @Override // android.content.pm.IPackageManager
            public int getFlagsForUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFlagsForUid(uid);
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

            @Override // android.content.pm.IPackageManager
            public int getPrivateFlagsForUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPrivateFlagsForUid(uid);
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

            @Override // android.content.pm.IPackageManager
            public boolean isUidPrivileged(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUidPrivileged(uid);
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

            @Override // android.content.pm.IPackageManager
            public String[] getAppOpPermissionPackages(String permissionName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppOpPermissionPackages(permissionName);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ResolveInfo _result;
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
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resolveIntent(intent, resolvedType, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ResolveInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ResolveInfo findPersistentPreferredActivity(Intent intent, int userId) throws RemoteException {
                ResolveInfo _result;
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
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().findPersistentPreferredActivity(intent, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ResolveInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public boolean canForwardTo(Intent intent, String resolvedType, int sourceUserId, int targetUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    _data.writeInt(sourceUserId);
                    _data.writeInt(targetUserId);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canForwardTo(intent, resolvedType, sourceUserId, targetUserId);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
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
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryIntentActivities(intent, resolvedType, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice queryIntentActivityOptions(ComponentName caller, Intent[] specifics, String[] specificTypes, Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                Throwable th;
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (caller != null) {
                        _data.writeInt(1);
                        caller.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeTypedArray(specifics, 0);
                        try {
                            _data.writeStringArray(specificTypes);
                            if (intent != null) {
                                _data.writeInt(1);
                                intent.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                _data.writeString(resolvedType);
                                _data.writeInt(flags);
                                _data.writeInt(userId);
                                if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                    _reply.readException();
                                    if (_reply.readInt() != 0) {
                                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
                                    } else {
                                        _result = null;
                                    }
                                    _reply.recycle();
                                    _data.recycle();
                                    return _result;
                                }
                                ParceledListSlice queryIntentActivityOptions = Stub.getDefaultImpl().queryIntentActivityOptions(caller, specifics, specificTypes, intent, resolvedType, flags, userId);
                                _reply.recycle();
                                _data.recycle();
                                return queryIntentActivityOptions;
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
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
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryIntentReceivers(intent, resolvedType, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ResolveInfo _result;
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
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resolveService(intent, resolvedType, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ResolveInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice queryIntentServices(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
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
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(52, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryIntentServices(intent, resolvedType, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
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
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryIntentContentProviders(intent, resolvedType, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getInstalledPackages(int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstalledPackages(flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getPackagesHoldingPermissions(String[] permissions, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(permissions);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackagesHoldingPermissions(permissions, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getInstalledApplications(int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstalledApplications(flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getPersistentApplications(int flags) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPersistentApplications(flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ProviderInfo resolveContentProvider(String name, int flags, int userId) throws RemoteException {
                ProviderInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resolveContentProvider(name, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ProviderInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public void querySyncProviders(List<String> outNames, List<ProviderInfo> outInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(outNames);
                    _data.writeTypedList(outInfo);
                    if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.readStringList(outNames);
                        _reply.readTypedList(outInfo, ProviderInfo.CREATOR);
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().querySyncProviders(outNames, outInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice queryContentProviders(String processName, int uid, int flags, String metaDataKey) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    _data.writeInt(flags);
                    _data.writeString(metaDataKey);
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryContentProviders(processName, uid, flags, metaDataKey);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws RemoteException {
                InstrumentationInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(61, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstrumentationInfo(className, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = InstrumentationInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice queryInstrumentation(String targetPackage, int flags) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetPackage);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryInstrumentation(targetPackage, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public void finishPackageInstall(int token, boolean didLaunch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeInt(didLaunch ? 1 : 0);
                    if (this.mRemote.transact(63, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finishPackageInstall(token, didLaunch);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void setInstallerPackageName(String targetPackage, String installerPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetPackage);
                    _data.writeString(installerPackageName);
                    if (this.mRemote.transact(64, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInstallerPackageName(targetPackage, installerPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void setApplicationCategoryHint(String packageName, int categoryHint, String callerPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(categoryHint);
                    _data.writeString(callerPackageName);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setApplicationCategoryHint(packageName, categoryHint, callerPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void deletePackageAsUser(String packageName, int versionCode, IPackageDeleteObserver observer, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(versionCode);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(66, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deletePackageAsUser(packageName, versionCode, observer, userId, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void deletePackageVersioned(VersionedPackage versionedPackage, IPackageDeleteObserver2 observer, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (versionedPackage != null) {
                        _data.writeInt(1);
                        versionedPackage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(67, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deletePackageVersioned(versionedPackage, observer, userId, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public String getInstallerPackageName(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(68, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstallerPackageName(packageName);
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

            @Override // android.content.pm.IPackageManager
            public void resetApplicationPreferences(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(69, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetApplicationPreferences(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public ResolveInfo getLastChosenActivity(Intent intent, String resolvedType, int flags) throws RemoteException {
                ResolveInfo _result;
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
                    _data.writeString(resolvedType);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(70, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastChosenActivity(intent, resolvedType, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ResolveInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public void setLastChosenActivity(Intent intent, String resolvedType, int flags, IntentFilter filter, int match, ComponentName activity) throws RemoteException {
                Throwable th;
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
                    try {
                        _data.writeString(resolvedType);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flags);
                        if (filter != null) {
                            _data.writeInt(1);
                            filter.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(match);
                            if (activity != null) {
                                _data.writeInt(1);
                                activity.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(71, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().setLastChosenActivity(intent, resolvedType, flags, filter, match, activity);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.content.pm.IPackageManager
            public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) throws RemoteException {
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
                    if (this.mRemote.transact(72, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addPreferredActivity(filter, match, set, activity, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) throws RemoteException {
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
                    if (this.mRemote.transact(73, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().replacePreferredActivity(filter, match, set, activity, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void clearPackagePreferredActivities(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(74, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearPackagePreferredActivities(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPreferredActivities(outFilters, outActivities, packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(outFilters, IntentFilter.CREATOR);
                    _reply.readTypedList(outActivities, ComponentName.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void addPersistentPreferredActivity(IntentFilter filter, ComponentName activity, int userId) throws RemoteException {
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
                    if (activity != null) {
                        _data.writeInt(1);
                        activity.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (this.mRemote.transact(76, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addPersistentPreferredActivity(filter, activity, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void clearPackagePersistentPreferredActivities(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(77, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearPackagePersistentPreferredActivities(packageName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void addCrossProfileIntentFilter(IntentFilter intentFilter, String ownerPackage, int sourceUserId, int targetUserId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intentFilter != null) {
                        _data.writeInt(1);
                        intentFilter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(ownerPackage);
                    _data.writeInt(sourceUserId);
                    _data.writeInt(targetUserId);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(78, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addCrossProfileIntentFilter(intentFilter, ownerPackage, sourceUserId, targetUserId, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void clearCrossProfileIntentFilters(int sourceUserId, String ownerPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sourceUserId);
                    _data.writeString(ownerPackage);
                    if (this.mRemote.transact(79, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearCrossProfileIntentFilters(sourceUserId, ownerPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public String[] setDistractingPackageRestrictionsAsUser(String[] packageNames, int restrictionFlags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(restrictionFlags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(80, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDistractingPackageRestrictionsAsUser(packageNames, restrictionFlags, userId);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, PersistableBundle appExtras, PersistableBundle launcherExtras, SuspendDialogInfo dialogInfo, String callingPackage, int userId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStringArray(packageNames);
                        _data.writeInt(suspended ? 1 : 0);
                        if (appExtras != null) {
                            _data.writeInt(1);
                            appExtras.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (launcherExtras != null) {
                            _data.writeInt(1);
                            launcherExtras.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (dialogInfo != null) {
                            _data.writeInt(1);
                            dialogInfo.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPackage);
                        _data.writeInt(userId);
                        if (this.mRemote.transact(81, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            String[] _result = _reply.createStringArray();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        String[] packagesSuspendedAsUser = Stub.getDefaultImpl().setPackagesSuspendedAsUser(packageNames, suspended, appExtras, launcherExtras, dialogInfo, callingPackage, userId);
                        _reply.recycle();
                        _data.recycle();
                        return packagesSuspendedAsUser;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.content.pm.IPackageManager
            public String[] getUnsuspendablePackagesForUser(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(82, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUnsuspendablePackagesForUser(packageNames, userId);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean isPackageSuspendedForUser(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(83, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPackageSuspendedForUser(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public PersistableBundle getSuspendedPackageAppExtras(String packageName, int userId) throws RemoteException {
                PersistableBundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(84, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSuspendedPackageAppExtras(packageName, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PersistableBundle.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public byte[] getPreferredActivityBackup(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(85, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPreferredActivityBackup(userId);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void restorePreferredActivities(byte[] backup, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(backup);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(86, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restorePreferredActivities(backup, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public byte[] getDefaultAppsBackup(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(87, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultAppsBackup(userId);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void restoreDefaultApps(byte[] backup, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(backup);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(88, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreDefaultApps(backup, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public byte[] getIntentFilterVerificationBackup(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(89, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIntentFilterVerificationBackup(userId);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void restoreIntentFilterVerification(byte[] backup, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(backup);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(90, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreIntentFilterVerification(backup, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public ComponentName getHomeActivities(List<ResolveInfo> outHomeCandidates) throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(91, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHomeActivities(outHomeCandidates);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.readTypedList(outHomeCandidates, ResolveInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void setHomeActivity(ComponentName className, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (className != null) {
                        _data.writeInt(1);
                        className.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (this.mRemote.transact(92, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHomeActivity(className, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(newState);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(93, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setComponentEnabledSetting(componentName, newState, flags, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public int getComponentEnabledSetting(ComponentName componentName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(94, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getComponentEnabledSetting(componentName, userId);
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

            @Override // android.content.pm.IPackageManager
            public void setApplicationEnabledSetting(String packageName, int newState, int flags, int userId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(newState);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(95, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setApplicationEnabledSetting(packageName, newState, flags, userId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public int getApplicationEnabledSetting(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(96, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApplicationEnabledSetting(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public void logAppProcessStartIfNeeded(String processName, int uid, String seinfo, String apkFile, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    _data.writeString(seinfo);
                    _data.writeString(apkFile);
                    _data.writeInt(pid);
                    if (this.mRemote.transact(97, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().logAppProcessStartIfNeeded(processName, uid, seinfo, apkFile, pid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void flushPackageRestrictionsAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(98, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().flushPackageRestrictionsAsUser(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void setPackageStoppedState(String packageName, boolean stopped, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(stopped ? 1 : 0);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(99, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPackageStoppedState(packageName, stopped, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void freeStorageAndNotify(String volumeUuid, long freeStorageSize, int storageFlags, IPackageDataObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeLong(freeStorageSize);
                    _data.writeInt(storageFlags);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(100, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().freeStorageAndNotify(volumeUuid, freeStorageSize, storageFlags, observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void freeStorage(String volumeUuid, long freeStorageSize, int storageFlags, IntentSender pi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeLong(freeStorageSize);
                    _data.writeInt(storageFlags);
                    if (pi != null) {
                        _data.writeInt(1);
                        pi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(101, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().freeStorage(volumeUuid, freeStorageSize, storageFlags, pi);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(102, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteApplicationCacheFiles(packageName, observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(103, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteApplicationCacheFilesAsUser(packageName, userId, observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void clearApplicationUserData(String packageName, IPackageDataObserver observer, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(104, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearApplicationUserData(packageName, observer, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void clearApplicationProfileData(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(105, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearApplicationProfileData(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void getPackageSizeInfo(String packageName, int userHandle, IPackageStatsObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userHandle);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(106, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getPackageSizeInfo(packageName, userHandle, observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public String[] getSystemSharedLibraryNames() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(107, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemSharedLibraryNames();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getSystemAvailableFeatures() throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(108, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemAvailableFeatures();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public boolean hasSystemFeature(String name, int version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(version);
                    boolean _result = false;
                    if (!this.mRemote.transact(109, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasSystemFeature(name, version);
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

            @Override // android.content.pm.IPackageManager
            public void enterSafeMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(110, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enterSafeMode();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean isSafeMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(111, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSafeMode();
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

            @Override // android.content.pm.IPackageManager
            public void systemReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(112, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().systemReady();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean hasSystemUidErrors() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(113, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasSystemUidErrors();
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

            @Override // android.content.pm.IPackageManager
            public void performFstrimIfNeeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(114, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().performFstrimIfNeeded();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void updatePackagesIfNeeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(115, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updatePackagesIfNeeded();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void notifyPackageUse(String packageName, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(reason);
                    if (this.mRemote.transact(116, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyPackageUse(packageName, reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void notifyDexLoad(String loadingPackageName, List<String> classLoadersNames, List<String> classPaths, String loaderIsa) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(loadingPackageName);
                    _data.writeStringList(classLoadersNames);
                    _data.writeStringList(classPaths);
                    _data.writeString(loaderIsa);
                    if (this.mRemote.transact(117, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyDexLoad(loadingPackageName, classLoadersNames, classPaths, loaderIsa);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void registerDexModule(String packageName, String dexModulePath, boolean isSharedModule, IDexModuleRegisterCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(dexModulePath);
                    _data.writeInt(isSharedModule ? 1 : 0);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(118, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().registerDexModule(packageName, dexModulePath, isSharedModule, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean performDexOptMode(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force, boolean bootComplete, String splitName) throws RemoteException {
                Throwable th;
                boolean _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                        _result = true;
                        _data.writeInt(checkProfiles ? 1 : 0);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(targetCompilerFilter);
                        _data.writeInt(force ? 1 : 0);
                        _data.writeInt(bootComplete ? 1 : 0);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(splitName);
                        try {
                            if (this.mRemote.transact(119, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean performDexOptMode = Stub.getDefaultImpl().performDexOptMode(packageName, checkProfiles, targetCompilerFilter, force, bootComplete, splitName);
                            _reply.recycle();
                            _data.recycle();
                            return performDexOptMode;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean performDexOptSecondary(String packageName, String targetCompilerFilter, boolean force) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(targetCompilerFilter);
                    boolean _result = true;
                    _data.writeInt(force ? 1 : 0);
                    if (!this.mRemote.transact(120, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().performDexOptSecondary(packageName, targetCompilerFilter, force);
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

            @Override // android.content.pm.IPackageManager
            public boolean compileLayouts(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(121, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().compileLayouts(packageName);
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

            @Override // android.content.pm.IPackageManager
            public void dumpProfiles(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(122, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dumpProfiles(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void forceDexOpt(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(123, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forceDexOpt(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean runBackgroundDexoptJob(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    boolean _result = false;
                    if (!this.mRemote.transact(124, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().runBackgroundDexoptJob(packageNames);
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

            @Override // android.content.pm.IPackageManager
            public void reconcileSecondaryDexFiles(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(125, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reconcileSecondaryDexFiles(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public int getMoveStatus(int moveId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(moveId);
                    if (!this.mRemote.transact(126, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMoveStatus(moveId);
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

            @Override // android.content.pm.IPackageManager
            public void registerMoveCallback(IPackageMoveObserver callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(127, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerMoveCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void unregisterMoveCallback(IPackageMoveObserver callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(128, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterMoveCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public int movePackage(String packageName, String volumeUuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(volumeUuid);
                    if (!this.mRemote.transact(129, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().movePackage(packageName, volumeUuid);
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

            @Override // android.content.pm.IPackageManager
            public int movePrimaryStorage(String volumeUuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    if (!this.mRemote.transact(130, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().movePrimaryStorage(volumeUuid);
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

            @Override // android.content.pm.IPackageManager
            public boolean addPermissionAsync(PermissionInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(131, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addPermissionAsync(info);
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

            @Override // android.content.pm.IPackageManager
            public boolean setInstallLocation(int loc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(loc);
                    boolean _result = false;
                    if (!this.mRemote.transact(132, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setInstallLocation(loc);
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

            @Override // android.content.pm.IPackageManager
            public int getInstallLocation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(133, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstallLocation();
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

            @Override // android.content.pm.IPackageManager
            public int installExistingPackageAsUser(String packageName, int userId, int installFlags, int installReason, List<String> whiteListedPermissions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(installFlags);
                    _data.writeInt(installReason);
                    _data.writeStringList(whiteListedPermissions);
                    if (!this.mRemote.transact(134, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().installExistingPackageAsUser(packageName, userId, installFlags, installReason, whiteListedPermissions);
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

            @Override // android.content.pm.IPackageManager
            public void verifyPendingInstall(int id, int verificationCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(verificationCode);
                    if (this.mRemote.transact(135, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().verifyPendingInstall(id, verificationCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(verificationCodeAtTimeout);
                    _data.writeLong(millisecondsToDelay);
                    if (this.mRemote.transact(136, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(verificationCode);
                    _data.writeStringList(failedDomains);
                    if (this.mRemote.transact(137, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().verifyIntentFilter(id, verificationCode, failedDomains);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public int getIntentVerificationStatus(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(138, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIntentVerificationStatus(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public boolean updateIntentVerificationStatus(String packageName, int status, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(status);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(139, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateIntentVerificationStatus(packageName, status, userId);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getIntentFilterVerifications(String packageName) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(140, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIntentFilterVerifications(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getAllIntentFilters(String packageName) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(141, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllIntentFilters(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public boolean setDefaultBrowserPackageName(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(142, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDefaultBrowserPackageName(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public String getDefaultBrowserPackageName(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(143, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultBrowserPackageName(userId);
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

            @Override // android.content.pm.IPackageManager
            public VerifierDeviceIdentity getVerifierDeviceIdentity() throws RemoteException {
                VerifierDeviceIdentity _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(144, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVerifierDeviceIdentity();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VerifierDeviceIdentity.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public boolean isFirstBoot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(145, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFirstBoot();
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

            @Override // android.content.pm.IPackageManager
            public boolean isOnlyCoreApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(146, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isOnlyCoreApps();
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

            @Override // android.content.pm.IPackageManager
            public boolean isDeviceUpgrading() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(147, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDeviceUpgrading();
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

            @Override // android.content.pm.IPackageManager
            public void setPermissionEnforced(String permission, boolean enforced) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeInt(enforced ? 1 : 0);
                    if (this.mRemote.transact(148, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPermissionEnforced(permission, enforced);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean isPermissionEnforced(String permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    boolean _result = false;
                    if (!this.mRemote.transact(149, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPermissionEnforced(permission);
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

            @Override // android.content.pm.IPackageManager
            public boolean isStorageLow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(150, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isStorageLow();
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

            @Override // android.content.pm.IPackageManager
            public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(hidden ? 1 : 0);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(151, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setApplicationHiddenSettingAsUser(packageName, hidden, userId);
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

            @Override // android.content.pm.IPackageManager
            public boolean getApplicationHiddenSettingAsUser(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(152, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApplicationHiddenSettingAsUser(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public void setSystemAppHiddenUntilInstalled(String packageName, boolean hidden) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(hidden ? 1 : 0);
                    if (this.mRemote.transact(153, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSystemAppHiddenUntilInstalled(packageName, hidden);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean setSystemAppInstallState(String packageName, boolean installed, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(installed ? 1 : 0);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(154, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSystemAppInstallState(packageName, installed, userId);
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

            @Override // android.content.pm.IPackageManager
            public IPackageInstaller getPackageInstaller() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(155, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageInstaller();
                    }
                    _reply.readException();
                    IPackageInstaller _result = IPackageInstaller.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(blockUninstall ? 1 : 0);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(156, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setBlockUninstallForUser(packageName, blockUninstall, userId);
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

            @Override // android.content.pm.IPackageManager
            public boolean getBlockUninstallForUser(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(157, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBlockUninstallForUser(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public KeySet getKeySetByAlias(String packageName, String alias) throws RemoteException {
                KeySet _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(alias);
                    if (!this.mRemote.transact(158, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKeySetByAlias(packageName, alias);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = KeySet.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public KeySet getSigningKeySet(String packageName) throws RemoteException {
                KeySet _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(159, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSigningKeySet(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = KeySet.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public boolean isPackageSignedByKeySet(String packageName, KeySet ks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (ks != null) {
                        _data.writeInt(1);
                        ks.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(160, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPackageSignedByKeySet(packageName, ks);
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

            @Override // android.content.pm.IPackageManager
            public boolean isPackageSignedByKeySetExactly(String packageName, KeySet ks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (ks != null) {
                        _data.writeInt(1);
                        ks.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(161, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPackageSignedByKeySetExactly(packageName, ks);
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

            @Override // android.content.pm.IPackageManager
            public void addOnPermissionsChangeListener(IOnPermissionsChangeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(162, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addOnPermissionsChangeListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void removeOnPermissionsChangeListener(IOnPermissionsChangeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(163, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeOnPermissionsChangeListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void grantDefaultPermissionsToEnabledCarrierApps(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(164, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().grantDefaultPermissionsToEnabledCarrierApps(packageNames, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void grantDefaultPermissionsToEnabledImsServices(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(165, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().grantDefaultPermissionsToEnabledImsServices(packageNames, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void grantDefaultPermissionsToEnabledTelephonyDataServices(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(166, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().grantDefaultPermissionsToEnabledTelephonyDataServices(packageNames, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void revokeDefaultPermissionsFromDisabledTelephonyDataServices(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(167, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().revokeDefaultPermissionsFromDisabledTelephonyDataServices(packageNames, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void grantDefaultPermissionsToActiveLuiApp(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(168, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().grantDefaultPermissionsToActiveLuiApp(packageName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void revokeDefaultPermissionsFromLuiApps(String[] packageNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(169, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().revokeDefaultPermissionsFromLuiApps(packageNames, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean isPermissionRevokedByPolicy(String permission, String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(170, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPermissionRevokedByPolicy(permission, packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public String getPermissionControllerPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(171, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPermissionControllerPackageName();
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getInstantApps(int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(172, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstantApps(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public byte[] getInstantAppCookie(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(173, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstantAppCookie(packageName, userId);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public boolean setInstantAppCookie(String packageName, byte[] cookie, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeByteArray(cookie);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(174, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setInstantAppCookie(packageName, cookie, userId);
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

            @Override // android.content.pm.IPackageManager
            public Bitmap getInstantAppIcon(String packageName, int userId) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(175, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstantAppIcon(packageName, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public boolean isInstantApp(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(176, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInstantApp(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public boolean setRequiredForSystemUser(String packageName, boolean systemUserApp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    _data.writeInt(systemUserApp ? 1 : 0);
                    if (!this.mRemote.transact(177, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setRequiredForSystemUser(packageName, systemUserApp);
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

            @Override // android.content.pm.IPackageManager
            public void setUpdateAvailable(String packageName, boolean updateAvaialble) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(updateAvaialble ? 1 : 0);
                    if (this.mRemote.transact(178, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUpdateAvailable(packageName, updateAvaialble);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public String getServicesSystemSharedLibraryPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(179, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServicesSystemSharedLibraryPackageName();
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

            @Override // android.content.pm.IPackageManager
            public String getSharedSystemSharedLibraryPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(180, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSharedSystemSharedLibraryPackageName();
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

            @Override // android.content.pm.IPackageManager
            public ChangedPackages getChangedPackages(int sequenceNumber, int userId) throws RemoteException {
                ChangedPackages _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequenceNumber);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(181, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getChangedPackages(sequenceNumber, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ChangedPackages.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public boolean isPackageDeviceAdminOnAnyUser(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(182, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPackageDeviceAdminOnAnyUser(packageName);
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

            @Override // android.content.pm.IPackageManager
            public int getInstallReason(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(183, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstallReason(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getSharedLibraries(String packageName, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(184, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSharedLibraries(packageName, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ParceledListSlice getDeclaredSharedLibraries(String packageName, int flags, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(185, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeclaredSharedLibraries(packageName, flags, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public boolean canRequestPackageInstalls(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(186, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canRequestPackageInstalls(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public void deletePreloadsFileCache() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(187, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deletePreloadsFileCache();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public ComponentName getInstantAppResolverComponent() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(188, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstantAppResolverComponent();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ComponentName getInstantAppResolverSettingsComponent() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(189, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstantAppResolverSettingsComponent();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public ComponentName getInstantAppInstallerComponent() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(190, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstantAppInstallerComponent();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ComponentName.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public String getInstantAppAndroidId(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(191, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstantAppAndroidId(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public IArtManager getArtManager() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(192, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getArtManager();
                    }
                    _reply.readException();
                    IArtManager _result = IArtManager.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void setHarmfulAppWarning(String packageName, CharSequence warning, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (warning != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(warning, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (this.mRemote.transact(193, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHarmfulAppWarning(packageName, warning, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public CharSequence getHarmfulAppWarning(String packageName, int userId) throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(194, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHarmfulAppWarning(packageName, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public boolean hasSigningCertificate(String packageName, byte[] signingCertificate, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeByteArray(signingCertificate);
                    _data.writeInt(flags);
                    boolean _result = false;
                    if (!this.mRemote.transact(195, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasSigningCertificate(packageName, signingCertificate, flags);
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

            @Override // android.content.pm.IPackageManager
            public boolean hasUidSigningCertificate(int uid, byte[] signingCertificate, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeByteArray(signingCertificate);
                    _data.writeInt(flags);
                    boolean _result = false;
                    if (!this.mRemote.transact(196, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasUidSigningCertificate(uid, signingCertificate, flags);
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

            @Override // android.content.pm.IPackageManager
            public String getSystemTextClassifierPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(197, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemTextClassifierPackageName();
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

            @Override // android.content.pm.IPackageManager
            public String getAttentionServicePackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(198, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAttentionServicePackageName();
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

            @Override // android.content.pm.IPackageManager
            public String getWellbeingPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(199, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWellbeingPackageName();
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

            @Override // android.content.pm.IPackageManager
            public String getAppPredictionServicePackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(200, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppPredictionServicePackageName();
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

            @Override // android.content.pm.IPackageManager
            public String getSystemCaptionsServicePackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(201, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemCaptionsServicePackageName();
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

            @Override // android.content.pm.IPackageManager
            public String getIncidentReportApproverPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(202, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIncidentReportApproverPackageName();
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

            @Override // android.content.pm.IPackageManager
            public boolean isPackageStateProtected(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(203, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPackageStateProtected(packageName, userId);
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

            @Override // android.content.pm.IPackageManager
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(204, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwInnerService();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void sendDeviceCustomizationReadyBroadcast() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(205, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendDeviceCustomizationReadyBroadcast();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public List<ModuleInfo> getInstalledModules(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(206, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstalledModules(flags);
                    }
                    _reply.readException();
                    List<ModuleInfo> _result = _reply.createTypedArrayList(ModuleInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public ModuleInfo getModuleInfo(String packageName, int flags) throws RemoteException {
                ModuleInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(207, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getModuleInfo(packageName, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ModuleInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IPackageManager
            public int getRuntimePermissionsVersion(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(208, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRuntimePermissionsVersion(userId);
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

            @Override // android.content.pm.IPackageManager
            public void setRuntimePermissionsVersion(int version, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(version);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(209, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRuntimePermissionsVersion(version, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManager
            public void notifyPackagesReplacedReceived(String[] packages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packages);
                    if (this.mRemote.transact(210, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyPackagesReplacedReceived(packages);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPackageManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPackageManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
