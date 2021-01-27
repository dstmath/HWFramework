package android.vrsystem;

import android.common.FactoryLoader;

public class HwVrFactory {
    private static final String TAG = "HwVrFactory";
    public static final String VR_FACTORY_IMPL_NAME = "android.vrsystem.HwVrFactoryImpl";
    private static DefaultHwVrFactory sFactory;

    private static class Instance {
        private static HwVrFactory sInstance = new HwVrFactory();

        private Instance() {
        }
    }

    public static HwVrFactory getInstance() {
        return Instance.sInstance;
    }

    public static DefaultHwVrFactory loadFactory() {
        DefaultHwVrFactory defaultHwVrFactory = sFactory;
        if (defaultHwVrFactory != null) {
            return defaultHwVrFactory;
        }
        Object object = FactoryLoader.loadFactory(VR_FACTORY_IMPL_NAME);
        if (object != null && (object instanceof DefaultHwVrFactory)) {
            sFactory = (DefaultHwVrFactory) object;
        }
        if (sFactory == null) {
            sFactory = DefaultHwVrFactory.getInstance();
        }
        return sFactory;
    }
}
