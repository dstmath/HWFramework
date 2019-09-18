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
    private final boolean mBootClassPathContainsOAHL;
    private final PackageSharedLibraryUpdater[] mPackageUpdaters;

    @VisibleForTesting
    public static class AndroidTestRunnerSplitUpdater extends PackageSharedLibraryUpdater {
        public void updatePackage(PackageParser.Package pkg) {
            prefixImplicitDependency(pkg, "android.test.runner", "android.test.mock");
        }
    }

    @VisibleForTesting
    public static class RemoveUnnecessaryAndroidTestBaseLibrary extends PackageSharedLibraryUpdater {
        public void updatePackage(PackageParser.Package pkg) {
            removeLibrary(pkg, "android.test.base");
        }
    }

    @VisibleForTesting
    public static class RemoveUnnecessaryOrgApacheHttpLegacyLibrary extends PackageSharedLibraryUpdater {
        public void updatePackage(PackageParser.Package pkg) {
            removeLibrary(pkg, "org.apache.http.legacy");
        }
    }

    static {
        List<PackageSharedLibraryUpdater> packageUpdaters = new ArrayList<>();
        packageUpdaters.add(new AndroidTestRunnerSplitUpdater());
        INSTANCE = new PackageBackwardCompatibility(!addOptionalUpdater(packageUpdaters, "android.content.pm.OrgApacheHttpLegacyUpdater", $$Lambda$FMztmpMwSp3D3ge8Zxr31di8ZBg.INSTANCE), !addOptionalUpdater(packageUpdaters, "android.content.pm.AndroidTestBaseUpdater", $$Lambda$jpya2qgMDDEok2GAoKRDqPM5lIE.INSTANCE), (PackageSharedLibraryUpdater[]) packageUpdaters.toArray(new PackageSharedLibraryUpdater[0]));
    }

    private static boolean addOptionalUpdater(List<PackageSharedLibraryUpdater> packageUpdaters, String className, Supplier<PackageSharedLibraryUpdater> defaultUpdater) {
        Class<? extends U> cls;
        PackageSharedLibraryUpdater updater;
        try {
            cls = PackageBackwardCompatibility.class.getClassLoader().loadClass(className).asSubclass(PackageSharedLibraryUpdater.class);
            String str = TAG;
            Log.i(str, "Loaded " + className);
        } catch (ClassNotFoundException e) {
            String str2 = TAG;
            Log.i(str2, "Could not find " + className + ", ignoring");
            cls = null;
        }
        boolean usedOptional = false;
        if (cls == null) {
            updater = defaultUpdater.get();
        } else {
            try {
                updater = (PackageSharedLibraryUpdater) cls.getConstructor(new Class[0]).newInstance(new Object[0]);
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

    public PackageBackwardCompatibility(boolean bootClassPathContainsOAHL, boolean bootClassPathContainsATB, PackageSharedLibraryUpdater[] packageUpdaters) {
        this.mBootClassPathContainsOAHL = bootClassPathContainsOAHL;
        this.mBootClassPathContainsATB = bootClassPathContainsATB;
        this.mPackageUpdaters = packageUpdaters;
    }

    @VisibleForTesting
    public static void modifySharedLibraries(PackageParser.Package pkg) {
        INSTANCE.updatePackage(pkg);
    }

    public void updatePackage(PackageParser.Package pkg) {
        for (PackageSharedLibraryUpdater packageUpdater : this.mPackageUpdaters) {
            packageUpdater.updatePackage(pkg);
        }
    }

    @VisibleForTesting
    public static boolean bootClassPathContainsOAHL() {
        return INSTANCE.mBootClassPathContainsOAHL;
    }

    @VisibleForTesting
    public static boolean bootClassPathContainsATB() {
        return INSTANCE.mBootClassPathContainsATB;
    }
}
