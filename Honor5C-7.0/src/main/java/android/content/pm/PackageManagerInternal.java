package android.content.pm;

import android.content.ComponentName;
import android.util.SparseArray;
import java.util.List;

public abstract class PackageManagerInternal {

    public interface PackagesProvider {
        String[] getPackages(int i);
    }

    public interface SyncAdapterPackagesProvider {
        String[] getPackages(String str, int i);
    }

    public abstract boolean canPackageBeWiped(int i, String str);

    public abstract ApplicationInfo getApplicationInfo(String str, int i);

    public abstract ComponentName getHomeActivitiesAsUser(List<ResolveInfo> list, int i);

    public abstract void grantDefaultPermissionsToDefaultDialerApp(String str, int i);

    public abstract void grantDefaultPermissionsToDefaultSimCallManager(String str, int i);

    public abstract void grantDefaultPermissionsToDefaultSmsApp(String str, int i);

    public abstract boolean isInMWPortraitWhiteList(String str);

    public abstract boolean isPermissionsReviewRequired(String str, int i);

    public abstract void setDeviceAndProfileOwnerPackages(int i, String str, SparseArray<String> sparseArray);

    public abstract void setDialerAppPackagesProvider(PackagesProvider packagesProvider);

    public abstract void setKeepUninstalledPackages(List<String> list);

    public abstract void setLocationPackagesProvider(PackagesProvider packagesProvider);

    public abstract void setSimCallManagerPackagesProvider(PackagesProvider packagesProvider);

    public abstract void setSmsAppPackagesProvider(PackagesProvider packagesProvider);

    public abstract void setSyncAdapterPackagesprovider(SyncAdapterPackagesProvider syncAdapterPackagesProvider);

    public abstract void setVoiceInteractionPackagesProvider(PackagesProvider packagesProvider);
}
