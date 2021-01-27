package android.content.pm;

import com.android.server.LocalServices;

public class PackageManagerInternalEx {
    private PackageManagerInternal mPackageManagerInternal = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class));

    public PackageInfo getPackageInfo(String packageName, int flags, int filterCallingUid, int userId) {
        PackageManagerInternal packageManagerInternal = this.mPackageManagerInternal;
        if (packageManagerInternal != null) {
            return packageManagerInternal.getPackageInfo(packageName, flags, filterCallingUid, userId);
        }
        return null;
    }
}
