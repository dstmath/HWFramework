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

    public AssetManager getBaseAssetManager() throws PackageParser.PackageParserException {
        if (this.mCachedAssetManager != null) {
            return this.mCachedAssetManager;
        }
        ApkAssets[] apkAssets = new ApkAssets[((this.mSplitCodePaths != null ? this.mSplitCodePaths.length : 0) + 1)];
        int splitIdx = 0 + 1;
        apkAssets[0] = loadApkAssets(this.mBaseCodePath, this.mFlags);
        if (!ArrayUtils.isEmpty(this.mSplitCodePaths)) {
            String[] strArr = this.mSplitCodePaths;
            int length = strArr.length;
            int splitIdx2 = splitIdx;
            int splitIdx3 = 0;
            while (splitIdx3 < length) {
                apkAssets[splitIdx2] = loadApkAssets(strArr[splitIdx3], this.mFlags);
                splitIdx3++;
                splitIdx2++;
            }
            int i = splitIdx2;
        }
        AssetManager assets = new AssetManager();
        assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Build.VERSION.RESOURCES_SDK_INT);
        assets.setApkAssets(apkAssets, false);
        this.mCachedAssetManager = assets;
        return this.mCachedAssetManager;
    }

    public AssetManager getSplitAssetManager(int splitIdx) throws PackageParser.PackageParserException {
        return getBaseAssetManager();
    }

    public void close() throws Exception {
        IoUtils.closeQuietly(this.mCachedAssetManager);
    }
}
