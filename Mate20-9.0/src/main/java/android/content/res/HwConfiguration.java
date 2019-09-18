package android.content.res;

import android.common.HwFrameworkFactory;
import android.util.Log;

public final class HwConfiguration {
    private static final String TAG = HwConfiguration.class.getSimpleName();

    public static IHwConfiguration initHwConfiguration() {
        return getImplObject();
    }

    private static IHwConfiguration getImplObject() {
        IHwConfiguration instance = HwFrameworkFactory.getHwConfiguration();
        if (instance != null) {
            return instance;
        }
        Log.w(TAG, "can't get impl object from vendor, use default implemention");
        return new HwConfigurationDummy();
    }
}
