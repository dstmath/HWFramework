package android.content.res;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

public class ResourcesImplEx {
    static final String TAG_DEBUG = "Resources_debug";
    private ResourcesImpl mResourcesImpl;

    public ResourcesImplEx(ResourcesImpl resourcesImpl) {
        this.mResourcesImpl = resourcesImpl;
    }

    public ResourcesImplEx(Resources resources) {
        this.mResourcesImpl = resources.getImpl();
    }

    public String getResourcePackageName(int resId) throws Resources.NotFoundException {
        return this.mResourcesImpl.getResourcePackageName(resId);
    }

    public AssetManager getAssets() {
        return this.mResourcesImpl.getAssets();
    }

    public DisplayMetrics getDisplayMetrics() {
        return this.mResourcesImpl.getDisplayMetrics();
    }

    public boolean isPreloading() {
        return this.mResourcesImpl.mPreloading;
    }

    public boolean isPreloaded() {
        ResourcesImpl resourcesImpl = this.mResourcesImpl;
        return ResourcesImpl.sPreloaded;
    }

    public Configuration getConfiguration() {
        return this.mResourcesImpl.getConfiguration();
    }

    public String getResourceName(int resid) throws Resources.NotFoundException {
        return this.mResourcesImpl.getResourceName(resid);
    }

    public void getValue(int id, TypedValue outValue, boolean isResolveRefs) throws Resources.NotFoundException {
        this.mResourcesImpl.getValue(id, outValue, isResolveRefs);
    }

    public void dumpApkAssets() {
        AssetManager assetManager = this.mResourcesImpl.getAssets();
        if (assetManager == null) {
            Log.i(TAG_DEBUG, "assetManager == null");
            return;
        }
        ApkAssets[] apkAssets = assetManager.getApkAssets();
        if (apkAssets == null) {
            Log.i(TAG_DEBUG, "apkAssets == null");
            return;
        }
        for (ApkAssets asset : apkAssets) {
            Log.i(TAG_DEBUG, "The apk asset path = " + asset);
        }
    }

    public AbsResourcesImpl getHwResourcesImpl() {
        return this.mResourcesImpl.getHwResourcesImpl();
    }
}
