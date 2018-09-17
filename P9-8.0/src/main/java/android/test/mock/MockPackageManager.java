package android.test.mock;

import android.app.PackageInstallObserver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstantAppInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.KeySet;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.MoveCallback;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager.OnPermissionsChangedListener;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.content.pm.VersionedPackage;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import java.util.List;

@Deprecated
public class MockPackageManager extends PackageManager {
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public PackageInfo getPackageInfo(VersionedPackage versionedPackage, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public PackageInfo getPackageInfoAsUser(String packageName, int flags, int userId) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public String[] currentToCanonicalPackageNames(String[] names) {
        throw new UnsupportedOperationException();
    }

    public String[] canonicalToCurrentPackageNames(String[] names) {
        throw new UnsupportedOperationException();
    }

    public Intent getLaunchIntentForPackage(String packageName) {
        throw new UnsupportedOperationException();
    }

    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
        throw new UnsupportedOperationException();
    }

    public int[] getPackageGids(String packageName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int[] getPackageGids(String packageName, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int getPackageUid(String packageName, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int getPackageUidAsUser(String packageName, int flags, int userHandle) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int getPackageUidAsUser(String packageName, int userHandle) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public boolean isPermissionReviewModeEnabled() {
        return false;
    }

    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        throw new UnsupportedOperationException();
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public ApplicationInfo getApplicationInfoAsUser(String packageName, int flags, int userId) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public List<PackageInfo> getInstalledPackages(int flags) {
        throw new UnsupportedOperationException();
    }

    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        throw new UnsupportedOperationException();
    }

    public List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public int checkPermission(String permName, String pkgName) {
        throw new UnsupportedOperationException();
    }

    public boolean canRequestPackageInstalls() {
        throw new UnsupportedOperationException();
    }

    public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
        throw new UnsupportedOperationException();
    }

    public String getPermissionControllerPackageName() {
        throw new UnsupportedOperationException();
    }

    public boolean addPermission(PermissionInfo info) {
        throw new UnsupportedOperationException();
    }

    public boolean addPermissionAsync(PermissionInfo info) {
        throw new UnsupportedOperationException();
    }

    public void removePermission(String name) {
        throw new UnsupportedOperationException();
    }

    public void grantRuntimePermission(String packageName, String permissionName, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public void revokeRuntimePermission(String packageName, String permissionName, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public int getPermissionFlags(String permissionName, String packageName, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public boolean shouldShowRequestPermissionRationale(String permission) {
        throw new UnsupportedOperationException();
    }

    public void addOnPermissionsChangeListener(OnPermissionsChangedListener listener) {
        throw new UnsupportedOperationException();
    }

    public void removeOnPermissionsChangeListener(OnPermissionsChangedListener listener) {
        throw new UnsupportedOperationException();
    }

    public int checkSignatures(String pkg1, String pkg2) {
        throw new UnsupportedOperationException();
    }

    public int checkSignatures(int uid1, int uid2) {
        throw new UnsupportedOperationException();
    }

    public String[] getPackagesForUid(int uid) {
        throw new UnsupportedOperationException();
    }

    public String getNameForUid(int uid) {
        throw new UnsupportedOperationException();
    }

    public int getUidForSharedUser(String sharedUserName) {
        throw new UnsupportedOperationException();
    }

    public List<ApplicationInfo> getInstalledApplications(int flags) {
        throw new UnsupportedOperationException();
    }

    public List<ApplicationInfo> getInstalledApplicationsAsUser(int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public List<InstantAppInfo> getInstantApps() {
        throw new UnsupportedOperationException();
    }

    public Drawable getInstantAppIcon(String packageName) {
        throw new UnsupportedOperationException();
    }

    public byte[] getInstantAppCookie() {
        throw new UnsupportedOperationException();
    }

    public boolean isInstantApp() {
        throw new UnsupportedOperationException();
    }

    public boolean isInstantApp(String packageName) {
        throw new UnsupportedOperationException();
    }

    public int getInstantAppCookieMaxBytes() {
        throw new UnsupportedOperationException();
    }

    public int getInstantAppCookieMaxSize() {
        throw new UnsupportedOperationException();
    }

    public void clearInstantAppCookie() {
        throw new UnsupportedOperationException();
    }

    public void updateInstantAppCookie(byte[] cookie) {
        throw new UnsupportedOperationException();
    }

    public boolean setInstantAppCookie(byte[] cookie) {
        throw new UnsupportedOperationException();
    }

    public ChangedPackages getChangedPackages(int sequenceNumber) {
        throw new UnsupportedOperationException();
    }

    public ResolveInfo resolveActivity(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public ResolveInfo resolveService(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public ProviderInfo resolveContentProvider(String name, int flags) {
        throw new UnsupportedOperationException();
    }

    public ProviderInfo resolveContentProviderAsUser(String name, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        throw new UnsupportedOperationException();
    }

    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        throw new UnsupportedOperationException();
    }

    public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }

    public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Drawable getDefaultActivityIcon() {
        throw new UnsupportedOperationException();
    }

    public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Drawable getApplicationBanner(ApplicationInfo info) {
        throw new UnsupportedOperationException();
    }

    public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Drawable getApplicationIcon(ApplicationInfo info) {
        throw new UnsupportedOperationException();
    }

    public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Drawable getApplicationLogo(ApplicationInfo info) {
        throw new UnsupportedOperationException();
    }

    public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        throw new UnsupportedOperationException();
    }

    public Drawable getUserBadgeForDensity(UserHandle user, int density) {
        throw new UnsupportedOperationException();
    }

    public Drawable getUserBadgeForDensityNoBackground(UserHandle user, int density) {
        throw new UnsupportedOperationException();
    }

    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }

    public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }

    public CharSequence getApplicationLabel(ApplicationInfo info) {
        throw new UnsupportedOperationException();
    }

    public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Resources getResourcesForApplication(ApplicationInfo app) {
        throw new UnsupportedOperationException();
    }

    public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Resources getResourcesForApplicationAsUser(String appPackageName, int userId) {
        throw new UnsupportedOperationException();
    }

    public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
        throw new UnsupportedOperationException();
    }

    public void installPackage(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName) {
        throw new UnsupportedOperationException();
    }

    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        throw new UnsupportedOperationException();
    }

    public void setUpdateAvailable(String packageName, boolean updateAvailable) {
        throw new UnsupportedOperationException();
    }

    public String getInstallerPackageName(String packageName) {
        throw new UnsupportedOperationException();
    }

    public int getMoveStatus(int moveId) {
        throw new UnsupportedOperationException();
    }

    public void registerMoveCallback(MoveCallback callback, Handler handler) {
        throw new UnsupportedOperationException();
    }

    public void unregisterMoveCallback(MoveCallback callback) {
        throw new UnsupportedOperationException();
    }

    public int movePackage(String packageName, VolumeInfo vol) {
        throw new UnsupportedOperationException();
    }

    public VolumeInfo getPackageCurrentVolume(ApplicationInfo app) {
        throw new UnsupportedOperationException();
    }

    public List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo app) {
        throw new UnsupportedOperationException();
    }

    public int movePrimaryStorage(VolumeInfo vol) {
        throw new UnsupportedOperationException();
    }

    public VolumeInfo getPrimaryStorageCurrentVolume() {
        throw new UnsupportedOperationException();
    }

    public List<VolumeInfo> getPrimaryStorageCandidateVolumes() {
        throw new UnsupportedOperationException();
    }

    public void clearApplicationUserData(String packageName, IPackageDataObserver observer) {
        throw new UnsupportedOperationException();
    }

    public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {
        throw new UnsupportedOperationException();
    }

    public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) {
        throw new UnsupportedOperationException();
    }

    public void freeStorageAndNotify(String volumeUuid, long idealStorageSize, IPackageDataObserver observer) {
        throw new UnsupportedOperationException();
    }

    public void freeStorage(String volumeUuid, long idealStorageSize, IntentSender pi) {
        throw new UnsupportedOperationException();
    }

    public void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
        throw new UnsupportedOperationException();
    }

    public void deletePackageAsUser(String packageName, IPackageDeleteObserver observer, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public void addPackageToPreferred(String packageName) {
        throw new UnsupportedOperationException();
    }

    public void removePackageFromPreferred(String packageName) {
        throw new UnsupportedOperationException();
    }

    public List<PackageInfo> getPreferredPackages(int flags) {
        throw new UnsupportedOperationException();
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        throw new UnsupportedOperationException();
    }

    public int getComponentEnabledSetting(ComponentName componentName) {
        throw new UnsupportedOperationException();
    }

    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        throw new UnsupportedOperationException();
    }

    public int getApplicationEnabledSetting(String packageName) {
        throw new UnsupportedOperationException();
    }

    public void flushPackageRestrictionsAsUser(int userId) {
        throw new UnsupportedOperationException();
    }

    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        throw new UnsupportedOperationException();
    }

    public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        throw new UnsupportedOperationException();
    }

    public void clearPackagePreferredActivities(String packageName) {
        throw new UnsupportedOperationException();
    }

    public void getPackageSizeInfoAsUser(String packageName, int userHandle, IPackageStatsObserver observer) {
        throw new UnsupportedOperationException();
    }

    public int getPreferredActivities(List<IntentFilter> list, List<ComponentName> list2, String packageName) {
        throw new UnsupportedOperationException();
    }

    public ComponentName getHomeActivities(List<ResolveInfo> list) {
        throw new UnsupportedOperationException();
    }

    public String[] getSystemSharedLibraryNames() {
        throw new UnsupportedOperationException();
    }

    public List<SharedLibraryInfo> getSharedLibraries(int flags) {
        throw new UnsupportedOperationException();
    }

    public List<SharedLibraryInfo> getSharedLibrariesAsUser(int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public String getServicesSystemSharedLibraryPackageName() {
        throw new UnsupportedOperationException();
    }

    public String getSharedSystemSharedLibraryPackageName() {
        throw new UnsupportedOperationException();
    }

    public FeatureInfo[] getSystemAvailableFeatures() {
        throw new UnsupportedOperationException();
    }

    public boolean hasSystemFeature(String name) {
        throw new UnsupportedOperationException();
    }

    public boolean hasSystemFeature(String name, int version) {
        throw new UnsupportedOperationException();
    }

    public boolean isSafeMode() {
        throw new UnsupportedOperationException();
    }

    public KeySet getKeySetByAlias(String packageName, String alias) {
        throw new UnsupportedOperationException();
    }

    public KeySet getSigningKeySet(String packageName) {
        throw new UnsupportedOperationException();
    }

    public boolean isSignedBy(String packageName, KeySet ks) {
        throw new UnsupportedOperationException();
    }

    public boolean isSignedByExactly(String packageName, KeySet ks) {
        throw new UnsupportedOperationException();
    }

    public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean hidden, int userId) {
        throw new UnsupportedOperationException();
    }

    public boolean isPackageSuspendedForUser(String packageName, int userId) {
        throw new UnsupportedOperationException();
    }

    public void setApplicationCategoryHint(String packageName, int categoryHint) {
        throw new UnsupportedOperationException();
    }

    public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, UserHandle user) {
        return false;
    }

    public boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
        return false;
    }

    public int installExistingPackage(String packageName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int installExistingPackage(String packageName, int installReason) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int installExistingPackageAsUser(String packageName, int userId) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public void verifyPendingInstall(int id, int verificationCode) {
        throw new UnsupportedOperationException();
    }

    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
        throw new UnsupportedOperationException();
    }

    public void verifyIntentFilter(int id, int verificationCode, List<String> list) {
        throw new UnsupportedOperationException();
    }

    public int getIntentVerificationStatusAsUser(String packageName, int userId) {
        throw new UnsupportedOperationException();
    }

    public boolean updateIntentVerificationStatusAsUser(String packageName, int status, int userId) {
        throw new UnsupportedOperationException();
    }

    public List<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName) {
        throw new UnsupportedOperationException();
    }

    public List<IntentFilter> getAllIntentFilters(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public String getDefaultBrowserPackageName(int userId) {
        throw new UnsupportedOperationException();
    }

    public String getDefaultBrowserPackageNameAsUser(int userId) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public boolean setDefaultBrowserPackageName(String packageName, int userId) {
        throw new UnsupportedOperationException();
    }

    public boolean setDefaultBrowserPackageNameAsUser(String packageName, int userId) {
        throw new UnsupportedOperationException();
    }

    public VerifierDeviceIdentity getVerifierDeviceIdentity() {
        throw new UnsupportedOperationException();
    }

    public boolean isUpgrade() {
        throw new UnsupportedOperationException();
    }

    public void installPackage(Uri packageURI, PackageInstallObserver observer, int flags, String installerPackageName) {
        throw new UnsupportedOperationException();
    }

    public void addCrossProfileIntentFilter(IntentFilter filter, int sourceUserId, int targetUserId, int flags) {
        throw new UnsupportedOperationException();
    }

    public void clearCrossProfileIntentFilters(int sourceUserId) {
        throw new UnsupportedOperationException();
    }

    public PackageInstaller getPackageInstaller() {
        throw new UnsupportedOperationException();
    }

    public boolean isPackageAvailable(String packageName) {
        throw new UnsupportedOperationException();
    }

    public Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }

    public Drawable loadUnbadgedItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }

    public int getInstallReason(String packageName, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    public ComponentName getInstantAppResolverSettingsComponent() {
        throw new UnsupportedOperationException();
    }

    public ComponentName getInstantAppInstallerComponent() {
        throw new UnsupportedOperationException();
    }

    public String getInstantAppAndroidId(String packageName, UserHandle user) {
        throw new UnsupportedOperationException();
    }
}
