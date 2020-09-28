package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageParser;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.internal.util.function.TriFunction;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class PackageManagerInternal {
    public static final int ENABLE_ROLLBACK_FAILED = -1;
    public static final int ENABLE_ROLLBACK_SUCCEEDED = 1;
    public static final String EXTRA_ENABLE_ROLLBACK_INSTALLED_USERS = "android.content.pm.extra.ENABLE_ROLLBACK_INSTALLED_USERS";
    public static final String EXTRA_ENABLE_ROLLBACK_INSTALL_FLAGS = "android.content.pm.extra.ENABLE_ROLLBACK_INSTALL_FLAGS";
    public static final String EXTRA_ENABLE_ROLLBACK_TOKEN = "android.content.pm.extra.ENABLE_ROLLBACK_TOKEN";
    public static final String EXTRA_ENABLE_ROLLBACK_USER = "android.content.pm.extra.ENABLE_ROLLBACK_USER";
    public static final int PACKAGE_APP_PREDICTOR = 11;
    public static final int PACKAGE_BROWSER = 4;
    public static final int PACKAGE_CONFIGURATOR = 9;
    public static final int PACKAGE_DOCUMENTER = 8;
    public static final int PACKAGE_INCIDENT_REPORT_APPROVER = 10;
    public static final int PACKAGE_INSTALLER = 2;
    public static final int PACKAGE_PERMISSION_CONTROLLER = 6;
    public static final int PACKAGE_SETUP_WIZARD = 1;
    public static final int PACKAGE_SYSTEM = 0;
    public static final int PACKAGE_SYSTEM_TEXT_CLASSIFIER = 5;
    public static final int PACKAGE_VERIFIER = 3;
    public static final int PACKAGE_WELLBEING = 7;

    public interface CheckPermissionDelegate {
        int checkPermission(String str, String str2, int i, TriFunction<String, String, Integer, Integer> triFunction);

        int checkUidPermission(String str, int i, BiFunction<String, Integer, Integer> biFunction);
    }

    public interface DefaultBrowserProvider {
        String getDefaultBrowser(int i);

        boolean setDefaultBrowser(String str, int i);

        void setDefaultBrowserAsync(String str, int i);
    }

    public interface DefaultDialerProvider {
        String getDefaultDialer(int i);
    }

    public interface DefaultHomeProvider {
        String getDefaultHome(int i);

        void setDefaultHomeAsync(String str, int i, Consumer<Boolean> consumer);
    }

    public interface ExternalSourcesPolicy {
        public static final int USER_BLOCKED = 1;
        public static final int USER_DEFAULT = 2;
        public static final int USER_TRUSTED = 0;

        int getPackageTrustedToInstallApps(String str, int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface KnownPackage {
    }

    public interface PackagesProvider {
        String[] getPackages(int i);
    }

    public interface SyncAdapterPackagesProvider {
        String[] getPackages(String str, int i);
    }

    public abstract void addIsolatedUid(int i, int i2);

    public abstract boolean canAccessComponent(int i, ComponentName componentName, int i2);

    public abstract boolean canAccessInstantApps(int i, int i2);

    public abstract void checkPackageStartable(String str, int i);

    public abstract boolean compileLayouts(String str);

    public abstract boolean filterAppAccess(PackageParser.Package v, int i, int i2);

    public abstract void finishPackageInstall(int i, boolean z);

    public abstract void forEachInstalledPackage(Consumer<PackageParser.Package> consumer, int i);

    public abstract void forEachPackage(Consumer<PackageParser.Package> consumer);

    public abstract void freeStorage(String str, long j, int i) throws IOException;

    public abstract ActivityInfo getActivityInfo(ComponentName componentName, int i, int i2, int i3);

    public abstract int getApplicationEnabledState(String str, int i);

    public abstract ApplicationInfo getApplicationInfo(String str, int i, int i2, int i3);

    public abstract SparseArray<String> getAppsWithSharedUserIds();

    public abstract CheckPermissionDelegate getCheckPermissionDelegate();

    public abstract ComponentName getDefaultHomeActivity(int i);

    public abstract ArraySet<String> getDisabledComponents(String str, int i);

    public abstract PackageParser.Package getDisabledSystemPackage(String str);

    public abstract String getDisabledSystemPackageName(String str);

    public abstract int getDistractingPackageRestrictions(String str, int i);

    public abstract ArraySet<String> getEnabledComponents(String str, int i);

    public abstract ComponentName getHomeActivitiesAsUser(List<ResolveInfo> list, int i);

    public abstract boolean getHwCertPermission(boolean z, PackageParser.Package v, String str);

    public abstract List<ApplicationInfo> getInstalledApplications(int i, int i2, int i3);

    public abstract String getInstantAppPackageName(int i);

    public abstract String getKnownPackageName(int i, int i2);

    public abstract String getNameForUid(int i);

    public abstract List<PackageInfo> getOverlayPackages(int i);

    public abstract PackageParser.Package getPackage(String str);

    public abstract PackageInfo getPackageInfo(String str, int i, int i2, int i3);

    public abstract PackageList getPackageList(PackageListObserver packageListObserver);

    public abstract int getPackageTargetSdkVersion(String str);

    public abstract int getPackageUid(String str, int i, int i2);

    public abstract String[] getPackagesForSharedUserId(String str, int i);

    public abstract int getPermissionFlagsTEMP(String str, String str2, int i);

    public abstract String getSetupWizardPackageName();

    public abstract String getSharedUserIdForPackage(String str);

    public abstract SuspendDialogInfo getSuspendedDialogInfo(String str, int i);

    public abstract Bundle getSuspendedPackageLauncherExtras(String str, int i);

    public abstract String getSuspendingPackage(String str, int i);

    public abstract List<String> getTargetPackageNames(int i);

    public abstract int getUidTargetSdkVersion(int i);

    public abstract float getUserAspectRatio(String str, String str2);

    public abstract void grantDefaultPermissionsToDefaultUseOpenWifiApp(String str, int i);

    public abstract void grantEphemeralAccess(int i, Intent intent, int i2, int i3);

    public abstract void grantRuntimePermission(String str, String str2, int i, boolean z);

    public abstract boolean hasInstantApplicationMetadata(String str, int i);

    public abstract boolean hasSignatureCapability(int i, int i2, @PackageParser.SigningDetails.CertCapabilities int i3);

    public abstract void installPackageAsUser(String str, IPackageInstallObserver2 iPackageInstallObserver2, int i, String str2, int i2);

    public abstract boolean isApexPackage(String str);

    public abstract boolean isDataRestoreSafe(Signature signature, String str);

    public abstract boolean isDataRestoreSafe(byte[] bArr, String str);

    public abstract boolean isEnabledAndMatches(ComponentInfo componentInfo, int i, int i2);

    public abstract boolean isInMWPortraitWhiteList(String str);

    public abstract boolean isInstantApp(String str, int i);

    public abstract boolean isInstantAppInstallerComponent(ComponentName componentName);

    public abstract boolean isLegacySystemApp(PackageParser.Package v);

    public abstract boolean isOnlyCoreApps();

    public abstract boolean isPackageDataProtected(int i, String str);

    public abstract boolean isPackageEphemeral(int i, String str);

    public abstract boolean isPackagePersistent(String str);

    public abstract boolean isPackageStateProtected(String str, int i);

    public abstract boolean isPackageSuspended(String str, int i);

    public abstract boolean isPermissionsReviewRequired(String str, int i);

    public abstract boolean isPlatformSigned(String str);

    public abstract boolean isResolveActivityComponent(ComponentInfo componentInfo);

    public abstract boolean isSystemAppGrantByMdmAndNonPreload(String str);

    public abstract boolean isUpgrade();

    public abstract void migrateLegacyObbData();

    public abstract void notifyPackageUse(String str, int i);

    public abstract void pruneInstantApps();

    public abstract List<ResolveInfo> queryIntentActivities(Intent intent, int i, int i2, int i3);

    public abstract List<ResolveInfo> queryIntentActivities(Intent intent, String str, int i, int i2, int i3);

    public abstract List<ResolveInfo> queryIntentServices(Intent intent, int i, int i2, int i3);

    public abstract void removeIsolatedUid(int i);

    public abstract String removeLegacyDefaultBrowserPackageName(int i);

    public abstract void removePackageListObserver(PackageListObserver packageListObserver);

    public abstract void requestInstantAppResolutionPhaseTwo(AuxiliaryResolveInfo auxiliaryResolveInfo, Intent intent, String str, String str2, Bundle bundle, int i);

    public abstract ProviderInfo resolveContentProvider(String str, int i, int i2);

    public abstract ResolveInfo resolveIntent(Intent intent, String str, int i, int i2, boolean z, int i3);

    public abstract ResolveInfo resolveService(Intent intent, String str, int i, int i2, int i3);

    public abstract void revokeRuntimePermission(String str, String str2, int i, boolean z);

    public abstract void setCheckPermissionDelegate(CheckPermissionDelegate checkPermissionDelegate);

    public abstract void setDefaultBrowserProvider(DefaultBrowserProvider defaultBrowserProvider);

    public abstract void setDefaultDialerProvider(DefaultDialerProvider defaultDialerProvider);

    public abstract void setDefaultHomeProvider(DefaultHomeProvider defaultHomeProvider);

    public abstract void setDeviceAndProfileOwnerPackages(int i, String str, SparseArray<String> sparseArray);

    public abstract void setEnableRollbackCode(int i, int i2);

    public abstract boolean setEnabledOverlayPackages(int i, String str, List<String> list);

    public abstract void setExternalSourcesPolicy(ExternalSourcesPolicy externalSourcesPolicy);

    public abstract void setKeepUninstalledPackages(List<String> list);

    public abstract void setLocationExtraPackagesProvider(PackagesProvider packagesProvider);

    public abstract void setLocationPackagesProvider(PackagesProvider packagesProvider);

    public abstract void setRuntimePermissionsFingerPrint(String str, int i);

    public abstract void setSyncAdapterPackagesprovider(SyncAdapterPackagesProvider syncAdapterPackagesProvider);

    public abstract void setUseOpenWifiAppPackagesProvider(PackagesProvider packagesProvider);

    public abstract void setVoiceInteractionPackagesProvider(PackagesProvider packagesProvider);

    public abstract void uninstallApex(String str, long j, int i, IntentSender intentSender);

    public abstract void updatePermissionFlagsTEMP(String str, String str2, int i, int i2, int i3);

    public abstract boolean userNeedsBadging(int i);

    public abstract boolean wasPackageEverLaunched(String str, int i);

    public abstract boolean wereDefaultPermissionsGrantedSinceBoot(int i);

    public interface PackageListObserver {
        void onPackageAdded(String str, int i);

        void onPackageRemoved(String str, int i);

        default void onPackageChanged(String packageName, int uid) {
        }
    }

    public void onDefaultSmsAppChanged(String packageName, int userId) {
    }

    public void onDefaultSimCallManagerAppChanged(String packageName, int userId) {
    }

    public PackageList getPackageList() {
        return getPackageList(null);
    }
}
