package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import java.util.List;

public abstract class PackageManagerInternal {

    public interface ExternalSourcesPolicy {
        public static final int USER_BLOCKED = 1;
        public static final int USER_DEFAULT = 2;
        public static final int USER_TRUSTED = 0;

        int getPackageTrustedToInstallApps(String str, int i);
    }

    public interface PackagesProvider {
        String[] getPackages(int i);
    }

    public interface SyncAdapterPackagesProvider {
        String[] getPackages(String str, int i);
    }

    public abstract void addIsolatedUid(int i, int i2);

    public abstract boolean canAccessInstantApps(int i, int i2);

    public abstract void checkPackageStartable(String str, int i);

    public abstract ActivityInfo getActivityInfo(ComponentName componentName, int i, int i2, int i3);

    public abstract ApplicationInfo getApplicationInfo(String str, int i, int i2, int i3);

    public abstract ComponentName getHomeActivitiesAsUser(List<ResolveInfo> list, int i);

    public abstract String getNameForUid(int i);

    public abstract List<PackageInfo> getOverlayPackages(int i);

    public abstract PackageInfo getPackageInfo(String str, int i, int i2, int i3);

    public abstract String getSetupWizardPackageName();

    public abstract List<String> getTargetPackageNames(int i);

    public abstract int getUidTargetSdkVersion(int i);

    public abstract float getUserMaxAspectRatio(String str);

    public abstract void grantDefaultPermissionsToDefaultDialerApp(String str, int i);

    public abstract void grantDefaultPermissionsToDefaultSimCallManager(String str, int i);

    public abstract void grantDefaultPermissionsToDefaultSmsApp(String str, int i);

    public abstract void grantEphemeralAccess(int i, Intent intent, int i2, int i3);

    public abstract void grantRuntimePermission(String str, String str2, int i, boolean z);

    public abstract boolean isInMWPortraitWhiteList(String str);

    public abstract boolean isInstantAppInstallerComponent(ComponentName componentName);

    public abstract boolean isPackageDataProtected(int i, String str);

    public abstract boolean isPackageEphemeral(int i, String str);

    public abstract boolean isPackagePersistent(String str);

    public abstract boolean isPermissionsReviewRequired(String str, int i);

    public abstract void pruneInstantApps();

    public abstract List<ResolveInfo> queryIntentActivities(Intent intent, int i, int i2, int i3);

    public abstract void removeIsolatedUid(int i);

    public abstract void requestInstantAppResolutionPhaseTwo(AuxiliaryResolveInfo auxiliaryResolveInfo, Intent intent, String str, String str2, Bundle bundle, int i);

    public abstract ResolveInfo resolveIntent(Intent intent, String str, int i, int i2);

    public abstract ResolveInfo resolveService(Intent intent, String str, int i, int i2, int i3);

    public abstract void revokeRuntimePermission(String str, String str2, int i, boolean z);

    public abstract void setDeviceAndProfileOwnerPackages(int i, String str, SparseArray<String> sparseArray);

    public abstract void setDialerAppPackagesProvider(PackagesProvider packagesProvider);

    public abstract boolean setEnabledOverlayPackages(int i, String str, List<String> list);

    public abstract void setExternalSourcesPolicy(ExternalSourcesPolicy externalSourcesPolicy);

    public abstract void setKeepUninstalledPackages(List<String> list);

    public abstract void setLocationPackagesProvider(PackagesProvider packagesProvider);

    public abstract void setSimCallManagerPackagesProvider(PackagesProvider packagesProvider);

    public abstract void setSmsAppPackagesProvider(PackagesProvider packagesProvider);

    public abstract void setSyncAdapterPackagesprovider(SyncAdapterPackagesProvider syncAdapterPackagesProvider);

    public abstract void setVoiceInteractionPackagesProvider(PackagesProvider packagesProvider);

    public abstract boolean wasPackageEverLaunched(String str, int i);
}
