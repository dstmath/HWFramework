package android.content.pm.split;

import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PackageParser.PackageParserException;
import android.content.res.AssetManager;
import android.os.Build.VERSION;
import com.android.internal.util.ArrayUtils;
import libcore.io.IoUtils;

public class DefaultSplitAssetLoader implements SplitAssetLoader {
    private final String mBaseCodePath;
    private AssetManager mCachedAssetManager;
    private final int mFlags;
    private final String[] mSplitCodePaths;

    public DefaultSplitAssetLoader(PackageLite pkg, int flags) {
        this.mBaseCodePath = pkg.baseCodePath;
        this.mSplitCodePaths = pkg.splitCodePaths;
        this.mFlags = flags;
    }

    private static void loadApkIntoAssetManager(AssetManager assets, String apkPath, int flags) throws PackageParserException {
        if ((flags & 4) != 0 && (PackageParser.isApkPath(apkPath) ^ 1) != 0) {
            throw new PackageParserException(-100, "Invalid package file: " + apkPath);
        } else if (assets.addAssetPath(apkPath) == 0) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Failed adding asset path: " + apkPath);
        }
    }

    public AssetManager getBaseAssetManager() throws PackageParserException {
        if (this.mCachedAssetManager != null) {
            return this.mCachedAssetManager;
        }
        AutoCloseable assetManager = new AssetManager();
        try {
            assetManager.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, VERSION.RESOURCES_SDK_INT);
            loadApkIntoAssetManager(assetManager, this.mBaseCodePath, this.mFlags);
            if (!ArrayUtils.isEmpty(this.mSplitCodePaths)) {
                for (String apkPath : this.mSplitCodePaths) {
                    loadApkIntoAssetManager(assetManager, apkPath, this.mFlags);
                }
            }
            this.mCachedAssetManager = assetManager;
            return this.mCachedAssetManager;
        } catch (Throwable th) {
            if (assetManager != null) {
                IoUtils.closeQuietly(assetManager);
            }
        }
    }

    public AssetManager getSplitAssetManager(int splitIdx) throws PackageParserException {
        return getBaseAssetManager();
    }

    public void close() throws Exception {
        if (this.mCachedAssetManager != null) {
            IoUtils.closeQuietly(this.mCachedAssetManager);
        }
    }
}
