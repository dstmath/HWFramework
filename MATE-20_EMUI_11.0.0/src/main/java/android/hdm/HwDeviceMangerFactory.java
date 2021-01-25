package android.hdm;

import android.common.FactoryLoader;
import android.hdm.HwDeviceManager;
import android.util.Slog;

public class HwDeviceMangerFactory {
    private static final String DEVICE_MANAGER_FACTORY_IMPL_NAME = "huawei.android.app.admin.HwDeviceMangerFactoryimpl";
    private static final String LOG_TAG = "HwDeviceMangerFactory";
    private static HwDeviceMangerFactory sFactory;

    public static HwDeviceMangerFactory loadFactory() {
        HwDeviceMangerFactory hwDeviceMangerFactory = sFactory;
        if (hwDeviceMangerFactory != null) {
            return hwDeviceMangerFactory;
        }
        Object object = FactoryLoader.loadFactory(DEVICE_MANAGER_FACTORY_IMPL_NAME);
        if (object == null || !(object instanceof HwDeviceMangerFactory)) {
            Slog.i(LOG_TAG, "Create default factory for mdm part is not exist.");
            sFactory = new HwDeviceMangerFactory();
        } else {
            Slog.i(LOG_TAG, "Create actual factory for mdm part.");
            sFactory = (HwDeviceMangerFactory) object;
        }
        return sFactory;
    }

    public HwDeviceManager.IHwDeviceManager getHuaweiDevicePolicyManager() {
        return DefaultHwDeviceManagerImpl.getDefault();
    }
}
