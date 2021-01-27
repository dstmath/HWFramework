package android.renderscript;

import android.annotation.UnsupportedAppUsage;
import java.io.File;

public class RenderScriptCacheDir {
    @UnsupportedAppUsage
    static File mCacheDir;

    @UnsupportedAppUsage
    public static void setupDiskCache(File cacheDir) {
        mCacheDir = cacheDir;
    }
}
