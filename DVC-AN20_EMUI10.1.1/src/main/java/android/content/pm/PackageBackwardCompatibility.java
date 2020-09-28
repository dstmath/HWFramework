package android.content.pm;

import android.content.pm.PackageParser;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@VisibleForTesting
public class PackageBackwardCompatibility extends PackageSharedLibraryUpdater {
    private static final PackageBackwardCompatibility INSTANCE;
    private static final String TAG = PackageBackwardCompatibility.class.getSimpleName();
    private final boolean mBootClassPathContainsATB;
    private final PackageSharedLibraryUpdater[] mPackageUpdaters;

    static {
        List<PackageSharedLibraryUpdater> packageUpdaters = new ArrayList<>();
        packageUpdaters.add(new OrgApacheHttpLegacyUpdater());
        packageUpdaters.add(new AndroidHidlUpdater());
        packageUpdaters.add(new AndroidTestRunnerSplitUpdater());
        INSTANCE = new PackageBackwardCompatibility(!addOptionalUpdater(packageUpdaters, "android.content.pm.AndroidTestBaseUpdater", $$Lambda$jpya2qgMDDEok2GAoKRDqPM5lIE.INSTANCE), (PackageSharedLibraryUpdater[]) packageUpdaters.toArray(new PackageSharedLibraryUpdater[0]));
    }

    private static boolean addOptionalUpdater(List<PackageSharedLibraryUpdater> packageUpdaters, String className, Supplier<PackageSharedLibraryUpdater> defaultUpdater) {
        Class<? extends PackageSharedLibraryUpdater> clazz;
        PackageSharedLibraryUpdater updater;
        try {
            clazz = PackageBackwardCompatibility.class.getClassLoader().loadClass(className).asSubclass(PackageSharedLibraryUpdater.class);
            String str = TAG;
            Log.i(str, "Loaded " + className);
        } catch (ClassNotFoundException e) {
            String str2 = TAG;
            Log.i(str2, "Could not find " + className + ", ignoring");
            clazz = null;
        }
        boolean usedOptional = false;
        if (clazz == null) {
            updater = defaultUpdater.get();
        } else {
            try {
                updater = (PackageSharedLibraryUpdater) clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                usedOptional = true;
            } catch (ReflectiveOperationException e2) {
                throw new IllegalStateException("Could not create instance of " + className, e2);
            }
        }
        packageUpdaters.add(updater);
        return usedOptional;
    }

    @VisibleForTesting
    public static PackageSharedLibraryUpdater getInstance() {
        return INSTANCE;
    }

    private PackageBackwardCompatibility(boolean bootClassPathContainsATB, PackageSharedLibraryUpdater[] packageUpdaters) {
        this.mBootClassPathContainsATB = bootClassPathContainsATB;
        this.mPackageUpdaters = packageUpdaters;
    }

    @VisibleForTesting
    public static void modifySharedLibraries(PackageParser.Package pkg) {
        INSTANCE.updatePackage(pkg);
    }

    @Override // android.content.pm.PackageSharedLibraryUpdater
    public void updatePackage(PackageParser.Package pkg) {
        for (PackageSharedLibraryUpdater packageUpdater : this.mPackageUpdaters) {
            packageUpdater.updatePackage(pkg);
        }
    }

    @VisibleForTesting
    public static boolean bootClassPathContainsATB() {
        return INSTANCE.mBootClassPathContainsATB;
    }

    @VisibleForTesting
    public static class AndroidTestRunnerSplitUpdater extends PackageSharedLibraryUpdater {
        @Override // android.content.pm.PackageSharedLibraryUpdater
        public void updatePackage(PackageParser.Package pkg) {
            prefixImplicitDependency(pkg, "android.test.runner", "android.test.mock");
        }
    }

    @VisibleForTesting
    public static class RemoveUnnecessaryOrgApacheHttpLegacyLibrary extends PackageSharedLibraryUpdater {
        @Override // android.content.pm.PackageSharedLibraryUpdater
        public void updatePackage(PackageParser.Package pkg) {
            removeLibrary(pkg, SharedLibraryNames.ORG_APACHE_HTTP_LEGACY);
        }
    }

    @VisibleForTesting
    public static class RemoveUnnecessaryAndroidTestBaseLibrary extends PackageSharedLibraryUpdater {
        @Override // android.content.pm.PackageSharedLibraryUpdater
        public void updatePackage(PackageParser.Package pkg) {
            removeLibrary(pkg, "android.test.base");
        }
    }
}
