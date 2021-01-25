package com.huawei.android.rio;

import android.content.Context;
import com.huawei.android.app.HwRioClientInfoEx;
import com.huawei.featurelayer.HwFeatureLoader;
import com.huawei.featurelayer.featureframework.IFeature;
import com.huawei.featurelayer.featureframework.IFeatureFramework;

public class DefaultHwRioTools implements IHwRioTools {
    private static IHwRioTools sInstance;

    private static IHwRioTools create() {
        IFeature feature;
        IFeatureFramework iFeatureFwk = HwFeatureLoader.SystemFeature.getFeatureFramework();
        if (iFeatureFwk == null || (feature = iFeatureFwk.loadFeature("com.huawei.systemfeature.rio", "com.huawei.systemfeature.rio.HwRioTools")) == null || !(feature instanceof IHwRioTools)) {
            return new DefaultHwRioTools();
        }
        return (IHwRioTools) feature;
    }

    public static synchronized IHwRioTools getInstance() {
        IHwRioTools iHwRioTools;
        synchronized (DefaultHwRioTools.class) {
            if (sInstance == null) {
                sInstance = create();
            }
            iHwRioTools = sInstance;
        }
        return iHwRioTools;
    }

    private DefaultHwRioTools() {
    }

    @Override // com.huawei.android.rio.IHwRioTools
    public void initContext(Context context) {
    }

    @Override // com.huawei.android.rio.IHwRioTools
    public String loadConfig(HwRioClientInfoEx rioClientInfoEx, int ruleResType) {
        return "";
    }

    @Override // com.huawei.android.rio.IHwRioTools
    public void setHotUpdateFilePath(String filePath) {
    }
}
