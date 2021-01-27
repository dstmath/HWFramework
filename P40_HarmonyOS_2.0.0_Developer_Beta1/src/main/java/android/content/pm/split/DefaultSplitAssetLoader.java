package android.content.pm.split;

import android.content.pm.PackageParser;
import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import android.os.Build;
import com.android.internal.util.ArrayUtils;
import java.io.IOException;
import libcore.io.IoUtils;

public class DefaultSplitAssetLoader implements SplitAssetLoader {
    private final String mBaseCodePath;
    private AssetManager mCachedAssetManager;
    private final int mFlags;
    private final String[] mSplitCodePaths;

    public DefaultSplitAssetLoader(PackageParser.PackageLite pkg, int flags) {
        this.mBaseCodePath = pkg.baseCodePath;
        this.mSplitCodePaths = pkg.splitCodePaths;
        this.mFlags = flags;
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

    @Override // android.content.pm.split.SplitAssetLoader
    public AssetManager getBaseAssetManager() throws PackageParser.PackageParserException {
        AssetManager assetManager = this.mCachedAssetManager;
        if (assetManager != null) {
            return assetManager;
        }
        String[] strArr = this.mSplitCodePaths;
        ApkAssets[] apkAssets = new ApkAssets[((strArr != null ? strArr.length : 0) + 1)];
        int splitIdx = 0 + 1;
        apkAssets[0] = loadApkAssets(this.mBaseCodePath, this.mFlags);
        if (!ArrayUtils.isEmpty(this.mSplitCodePaths)) {
            String[] strArr2 = this.mSplitCodePaths;
            int length = strArr2.length;
            int splitIdx2 = splitIdx;
            int splitIdx3 = 0;
            while (splitIdx3 < length) {
                apkAssets[splitIdx2] = loadApkAssets(strArr2[splitIdx3], this.mFlags);
                splitIdx3++;
                splitIdx2++;
            }
        }
        AssetManager assets = new AssetManager();
        assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Build.VERSION.RESOURCES_SDK_INT);
        assets.setApkAssets(apkAssets, false);
        this.mCachedAssetManager = assets;
        return this.mCachedAssetManager;
    }

    @Override // android.content.pm.split.SplitAssetLoader
    public AssetManager getSplitAssetManager(int splitIdx) throws PackageParser.PackageParserException {
        return getBaseAssetManager();
    }

    @Override // java.lang.AutoCloseable
    public void close() throws Exception {
        IoUtils.closeQuietly(this.mCachedAssetManager);
    }
}
