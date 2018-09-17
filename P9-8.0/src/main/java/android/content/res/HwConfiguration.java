package android.content.res;

import android.common.HwFrameworkFactory;
import android.util.Log;

public final class HwConfiguration {
    private static final String TAG = HwConfiguration.class.getSimpleName();
    private static IHwConfiguration sInstance = null;

    public static IHwConfiguration initHwConfiguration() {
        return getImplObject();
    }

    private static IHwConfiguration getImplObject() {
        IHwConfiguration instance = HwFrameworkFactory.getHwConfiguration();
        if (instance != null) {
            sInstance = instance;
        } else {
            Log.w(TAG, "can't get impl object from vendor, use default implemention");
            sInstance = new HwConfigurationDummy();
        }
        return sInstance;
    }
}
