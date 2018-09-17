package android.content.pm.split;

import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PackageParser.PackageParserException;
import android.content.res.AssetManager;
import android.os.Build.VERSION;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import libcore.io.IoUtils;

public class SplitAssetDependencyLoader extends SplitDependencyLoader<PackageParserException> implements SplitAssetLoader {
    private AssetManager[] mCachedAssetManagers = new AssetManager[this.mSplitPaths.length];
    private String[][] mCachedPaths = new String[this.mSplitPaths.length][];
    private final int mFlags;
    private final String[] mSplitPaths;

    public SplitAssetDependencyLoader(PackageLite pkg, SparseArray<int[]> dependencies, int flags) {
        super(dependencies);
        this.mSplitPaths = new String[(pkg.splitCodePaths.length + 1)];
        this.mSplitPaths[0] = pkg.baseCodePath;
        System.arraycopy(pkg.splitCodePaths, 0, this.mSplitPaths, 1, pkg.splitCodePaths.length);
        this.mFlags = flags;
    }

    protected boolean isSplitCached(int splitIdx) {
        return this.mCachedAssetManagers[splitIdx] != null;
    }

    private static AssetManager createAssetManagerWithPaths(String[] assetPaths, int flags) throws PackageParserException {
        AssetManager assets = new AssetManager();
        try {
            assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, VERSION.RESOURCES_SDK_INT);
            int i = 0;
            int length = assetPaths.length;
            while (i < length) {
                String assetPath = assetPaths[i];
                if ((flags & 4) != 0 && (PackageParser.isApkPath(assetPath) ^ 1) != 0) {
                    throw new PackageParserException(-100, "Invalid package file: " + assetPath);
                } else if (assets.addAssetPath(assetPath) == 0) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Failed adding asset path: " + assetPath);
                } else {
                    i++;
                }
            }
            return assets;
        } catch (Throwable th) {
            IoUtils.closeQuietly(assets);
        }
    }

    protected void constructSplit(int splitIdx, int[] configSplitIndices, int parentSplitIdx) throws PackageParserException {
        ArrayList<String> assetPaths = new ArrayList();
        if (parentSplitIdx >= 0) {
            Collections.addAll(assetPaths, this.mCachedPaths[parentSplitIdx]);
        }
        assetPaths.add(this.mSplitPaths[splitIdx]);
        for (int configSplitIdx : configSplitIndices) {
            assetPaths.add(this.mSplitPaths[configSplitIdx]);
        }
        this.mCachedPaths[splitIdx] = (String[]) assetPaths.toArray(new String[assetPaths.size()]);
        this.mCachedAssetManagers[splitIdx] = createAssetManagerWithPaths(this.mCachedPaths[splitIdx], this.mFlags);
    }

    public AssetManager getBaseAssetManager() throws PackageParserException {
        loadDependenciesForSplit(0);
        return this.mCachedAssetManagers[0];
    }

    public AssetManager getSplitAssetManager(int idx) throws PackageParserException {
        loadDependenciesForSplit(idx + 1);
        return this.mCachedAssetManagers[idx + 1];
    }

    public void close() throws Exception {
        for (AssetManager assets : this.mCachedAssetManagers) {
            IoUtils.closeQuietly(assets);
        }
    }
}
