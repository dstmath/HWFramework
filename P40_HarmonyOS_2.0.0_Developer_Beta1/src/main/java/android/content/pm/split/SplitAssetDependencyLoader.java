package android.content.pm.split;

import android.content.pm.PackageParser;
import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.SparseArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import libcore.io.IoUtils;

public class SplitAssetDependencyLoader extends SplitDependencyLoader<PackageParser.PackageParserException> implements SplitAssetLoader {
    private final AssetManager[] mCachedAssetManagers;
    private final ApkAssets[][] mCachedSplitApks;
    private final int mFlags;
    private final String[] mSplitPaths;

    public SplitAssetDependencyLoader(PackageParser.PackageLite pkg, SparseArray<int[]> dependencies, int flags) {
        super(dependencies);
        this.mSplitPaths = new String[(pkg.splitCodePaths.length + 1)];
        this.mSplitPaths[0] = pkg.baseCodePath;
        System.arraycopy(pkg.splitCodePaths, 0, this.mSplitPaths, 1, pkg.splitCodePaths.length);
        this.mFlags = flags;
        String[] strArr = this.mSplitPaths;
        this.mCachedSplitApks = new ApkAssets[strArr.length][];
        this.mCachedAssetManagers = new AssetManager[strArr.length];
    }

    /* access modifiers changed from: protected */
    @Override // android.content.pm.split.SplitDependencyLoader
    public boolean isSplitCached(int splitIdx) {
        return this.mCachedAssetManagers[splitIdx] != null;
    }

    private static ApkAssets loadApkAssets(String path, int flags) throws PackageParser.PackageParserException {
        if ((flags & 1) == 0 || PackageParser.isApkPath(path)) {
            try {
                return ApkAssets.loadFromPath(path);
            } catch (IOException e) {
                throw new PackageParser.PackageParserException(-2, "Failed to load APK at path " + path, e);
            }
        } else {
            throw new PackageParser.PackageParserException(-100, "Invalid package file: " + path);
        }
    }

    private static AssetManager createAssetManagerWithAssets(ApkAssets[] apkAssets) {
        AssetManager assets = new AssetManager();
        assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Build.VERSION.RESOURCES_SDK_INT);
        assets.setApkAssets(apkAssets, false);
        return assets;
    }

    /* access modifiers changed from: protected */
    @Override // android.content.pm.split.SplitDependencyLoader
    public void constructSplit(int splitIdx, int[] configSplitIndices, int parentSplitIdx) throws PackageParser.PackageParserException {
        ArrayList<ApkAssets> assets = new ArrayList<>();
        if (parentSplitIdx >= 0) {
            Collections.addAll(assets, this.mCachedSplitApks[parentSplitIdx]);
        }
        assets.add(loadApkAssets(this.mSplitPaths[splitIdx], this.mFlags));
        for (int configSplitIdx : configSplitIndices) {
            assets.add(loadApkAssets(this.mSplitPaths[configSplitIdx], this.mFlags));
        }
        this.mCachedSplitApks[splitIdx] = (ApkAssets[]) assets.toArray(new ApkAssets[assets.size()]);
        this.mCachedAssetManagers[splitIdx] = createAssetManagerWithAssets(this.mCachedSplitApks[splitIdx]);
    }

    @Override // android.content.pm.split.SplitAssetLoader
    public AssetManager getBaseAssetManager() throws PackageParser.PackageParserException {
        loadDependenciesForSplit(0);
        return this.mCachedAssetManagers[0];
    }

    @Override // android.content.pm.split.SplitAssetLoader
    public AssetManager getSplitAssetManager(int idx) throws PackageParser.PackageParserException {
        loadDependenciesForSplit(idx + 1);
        return this.mCachedAssetManagers[idx + 1];
    }

    @Override // java.lang.AutoCloseable
    public void close() throws Exception {
        for (AssetManager assets : this.mCachedAssetManagers) {
            IoUtils.closeQuietly(assets);
        }
    }
}
