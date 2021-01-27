package android.test.mock;

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
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstantAppInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.KeySet;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.content.pm.VersionedPackage;
import android.content.pm.dex.ArtManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import java.util.List;
import java.util.Set;

@Deprecated
public class MockPackageManager extends PackageManager {
    @Override // android.content.pm.PackageManager
    public PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public PackageInfo getPackageInfo(VersionedPackage versionedPackage, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public PackageInfo getPackageInfoAsUser(String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public String[] currentToCanonicalPackageNames(String[] names) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public String[] canonicalToCurrentPackageNames(String[] names) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Intent getLaunchIntentForPackage(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
        throw new UnsupportedOperationException();
    }

    public Intent getCarLaunchIntentForPackage(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public int[] getPackageGids(String packageName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public int[] getPackageGids(String packageName, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public int getPackageUid(String packageName, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int getPackageUidAsUser(String packageName, int flags, int userHandle) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int getPackageUidAsUser(String packageName, int userHandle) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public PermissionInfo getPermissionInfo(String name, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public boolean arePermissionsIndividuallyControlled() {
        return false;
    }

    public boolean isWirelessConsentModeEnabled() {
        return false;
    }

    @Override // android.content.pm.PackageManager
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public ApplicationInfo getApplicationInfoAsUser(String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<PackageInfo> getInstalledPackages(int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        throw new UnsupportedOperationException();
    }

    public List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public int checkPermission(String permName, String pkgName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean canRequestPackageInstalls() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
        throw new UnsupportedOperationException();
    }

    public String getPermissionControllerPackageName() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean addPermission(PermissionInfo info) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean addPermissionAsync(PermissionInfo info) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
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

    @Override // android.content.pm.PackageManager
    public Set<String> getWhitelistedRestrictedPermissions(String packageName, int whitelistFlags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean addWhitelistedRestrictedPermission(String packageName, String permission, int whitelistFlags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean removeWhitelistedRestrictedPermission(String packageName, String permission, int whitelistFlags) {
        throw new UnsupportedOperationException();
    }

    public boolean shouldShowRequestPermissionRationale(String permission) {
        throw new UnsupportedOperationException();
    }

    public void addOnPermissionsChangeListener(PackageManager.OnPermissionsChangedListener listener) {
        throw new UnsupportedOperationException();
    }

    public void removeOnPermissionsChangeListener(PackageManager.OnPermissionsChangedListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public int checkSignatures(String pkg1, String pkg2) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public int checkSignatures(int uid1, int uid2) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public String[] getPackagesForUid(int uid) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public String getNameForUid(int uid) {
        throw new UnsupportedOperationException();
    }

    public String[] getNamesForUids(int[] uid) {
        throw new UnsupportedOperationException();
    }

    public int getUidForSharedUser(String sharedUserName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
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

    @Override // android.content.pm.PackageManager
    public byte[] getInstantAppCookie() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean isInstantApp() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean isInstantApp(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public int getInstantAppCookieMaxBytes() {
        throw new UnsupportedOperationException();
    }

    public int getInstantAppCookieMaxSize() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public void clearInstantAppCookie() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public void updateInstantAppCookie(byte[] cookie) {
        throw new UnsupportedOperationException();
    }

    public boolean setInstantAppCookie(byte[] cookie) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public ChangedPackages getChangedPackages(int sequenceNumber) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public ResolveInfo resolveService(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public ResolveInfo resolveServiceAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public ProviderInfo resolveContentProvider(String name, int flags) {
        throw new UnsupportedOperationException();
    }

    public ProviderInfo resolveContentProviderAsUser(String name, int flags, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityIcon(ComponentName activityName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityIcon(Intent intent) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getDefaultActivityIcon() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityBanner(ComponentName activityName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityBanner(Intent intent) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationBanner(ApplicationInfo info) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationBanner(String packageName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationIcon(ApplicationInfo info) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationIcon(String packageName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityLogo(ComponentName activityName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityLogo(Intent intent) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationLogo(ApplicationInfo info) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationLogo(String packageName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        throw new UnsupportedOperationException();
    }

    public Drawable getUserBadgeForDensity(UserHandle user, int density) {
        throw new UnsupportedOperationException();
    }

    public Drawable getUserBadgeForDensityNoBackground(UserHandle user, int density) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public CharSequence getApplicationLabel(ApplicationInfo info) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Resources getResourcesForActivity(ComponentName activityName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Resources getResourcesForApplication(ApplicationInfo app) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public Resources getResourcesForApplication(String appPackageName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Resources getResourcesForApplicationAsUser(String appPackageName, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        throw new UnsupportedOperationException();
    }

    public void setUpdateAvailable(String packageName, boolean updateAvailable) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public String getInstallerPackageName(String packageName) {
        throw new UnsupportedOperationException();
    }

    public int getMoveStatus(int moveId) {
        throw new UnsupportedOperationException();
    }

    public void registerMoveCallback(PackageManager.MoveCallback callback, Handler handler) {
        throw new UnsupportedOperationException();
    }

    public void unregisterMoveCallback(PackageManager.MoveCallback callback) {
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

    @Override // android.content.pm.PackageManager
    public void addPackageToPreferred(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public void removePackageFromPreferred(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public List<PackageInfo> getPreferredPackages(int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public int getComponentEnabledSetting(ComponentName componentName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public int getApplicationEnabledSetting(String packageName) {
        throw new UnsupportedOperationException();
    }

    public void flushPackageRestrictionsAsUser(int userId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        throw new UnsupportedOperationException();
    }

    public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public void clearPackagePreferredActivities(String packageName) {
        throw new UnsupportedOperationException();
    }

    public void getPackageSizeInfoAsUser(String packageName, int userHandle, IPackageStatsObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public int getPreferredActivities(List<IntentFilter> list, List<ComponentName> list2, String packageName) {
        throw new UnsupportedOperationException();
    }

    public ComponentName getHomeActivities(List<ResolveInfo> list) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public String[] getSystemSharedLibraryNames() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
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

    @Override // android.content.pm.PackageManager
    public FeatureInfo[] getSystemAvailableFeatures() {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean hasSystemFeature(String name) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean hasSystemFeature(String name, int version) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
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

    public String[] setPackagesSuspended(String[] packageNames, boolean hidden, PersistableBundle appExtras, PersistableBundle launcherExtras, String dialogMessage) {
        throw new UnsupportedOperationException();
    }

    public boolean isPackageSuspendedForUser(String packageName, int userId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public void setApplicationCategoryHint(String packageName, int categoryHint) {
        throw new UnsupportedOperationException();
    }

    public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, UserHandle user) {
        return false;
    }

    public boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
        return false;
    }

    public int installExistingPackage(String packageName) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int installExistingPackage(String packageName, int installReason) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    public int installExistingPackageAsUser(String packageName, int userId) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public void verifyPendingInstall(int id, int verificationCode) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
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

    @Override // android.content.pm.PackageManager
    public boolean isDeviceUpgrading() {
        throw new UnsupportedOperationException();
    }

    public void addCrossProfileIntentFilter(IntentFilter filter, int sourceUserId, int targetUserId, int flags) {
        throw new UnsupportedOperationException();
    }

    public void clearCrossProfileIntentFilters(int sourceUserId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
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

    public void registerDexModule(String dexModulePath, PackageManager.DexModuleRegisterCallback callback) {
        throw new UnsupportedOperationException();
    }

    public ArtManager getArtManager() {
        throw new UnsupportedOperationException();
    }

    public void setHarmfulAppWarning(String packageName, CharSequence warning) {
        throw new UnsupportedOperationException();
    }

    public CharSequence getHarmfulAppWarning(String packageName) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean hasSigningCertificate(String packageName, byte[] certificate, int type) {
        throw new UnsupportedOperationException();
    }

    @Override // android.content.pm.PackageManager
    public boolean hasSigningCertificate(int uid, byte[] certificate, int type) {
        throw new UnsupportedOperationException();
    }

    public String getSystemTextClassifierPackageName() {
        throw new UnsupportedOperationException();
    }
}
