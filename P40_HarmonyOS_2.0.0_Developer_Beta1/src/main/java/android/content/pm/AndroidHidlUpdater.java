package android.content.pm;

import android.content.pm.PackageParser;
import com.android.internal.annotations.VisibleForTesting;

@VisibleForTesting
public class AndroidHidlUpdater extends PackageSharedLibraryUpdater {
    @Override // android.content.pm.PackageSharedLibraryUpdater
    public void updatePackage(PackageParser.Package pkg) {
        ApplicationInfo info = pkg.applicationInfo;
        boolean isSystem = true;
        boolean isLegacy = info.targetSdkVersion <= 28;
        if (!info.isSystemApp() && !info.isUpdatedSystemApp()) {
            isSystem = false;
        }
        if (!isLegacy || !isSystem) {
            removeLibrary(pkg, "android.hidl.base-V1.0-java");
            removeLibrary(pkg, "android.hidl.manager-V1.0-java");
            return;
        }
        prefixRequiredLibrary(pkg, "android.hidl.base-V1.0-java");
        prefixRequiredLibrary(pkg, "android.hidl.manager-V1.0-java");
    }
}
