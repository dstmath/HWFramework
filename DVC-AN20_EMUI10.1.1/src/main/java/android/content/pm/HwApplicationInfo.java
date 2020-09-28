package android.content.pm;

import android.content.pm.PackageParser;
import android.content.pm.split.SplitAssetDependencyLoader;
import android.content.pm.split.SplitDependencyLoader;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.ArrayUtils;
import java.io.File;

public class HwApplicationInfo {
    private HwApplicationInfo() {
    }

    public static SparseArray<int[]> getSplitDependencies(ApplicationInfo applicationInfo) {
        if (applicationInfo == null) {
            return new SparseArray<>(0);
        }
        if (applicationInfo.splitDependencies != null && applicationInfo.splitDependencies.size() != 0) {
            return applicationInfo.splitDependencies;
        }
        if (applicationInfo.requestsIsolatedSplitLoading() || ArrayUtils.isEmpty(applicationInfo.splitNames)) {
            return new SparseArray<>(0);
        }
        try {
            return SplitAssetDependencyLoader.createDependenciesFromPackage(PackageParser.parsePackageLite(new File(applicationInfo.getCodePath()), 0));
        } catch (PackageParser.PackageParserException | SplitDependencyLoader.IllegalDependencyException e) {
            String simpleName = HwApplicationInfo.class.getSimpleName();
            Log.e(simpleName, "getSplitDependencies Exception for " + applicationInfo.packageName);
            return new SparseArray<>(0);
        }
    }
}
