package android.app.admin;

import android.common.FactoryLoader;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Slog;

public class HwDeviceAdminInfoFactory {
    private static final String DEVICE_ADMIN_INFO_FACTORY_IMPL_NAME = "huawei.android.app.admin.HwDeviceAdminInfoFactoryImpl";
    private static final String LOG_TAG = "HwDeviceAdminInfoFactory";
    private static HwDeviceAdminInfoFactory sFactory;

    public static HwDeviceAdminInfoFactory loadFactory() {
        HwDeviceAdminInfoFactory hwDeviceAdminInfoFactory = sFactory;
        if (hwDeviceAdminInfoFactory != null) {
            return hwDeviceAdminInfoFactory;
        }
        Object object = FactoryLoader.loadFactory(DEVICE_ADMIN_INFO_FACTORY_IMPL_NAME);
        if (object == null || !(object instanceof HwDeviceAdminInfoFactory)) {
            Slog.i(LOG_TAG, "Create default factory for mdm part is not exist.");
            sFactory = new HwDeviceAdminInfoFactory();
        } else {
            Slog.i(LOG_TAG, "Create actual factory for mdm part.");
            sFactory = (HwDeviceAdminInfoFactory) object;
        }
        return sFactory;
    }

    public IHwDeviceAdminInfo getHwDeviceAdminInfo(Context context, ActivityInfo activityInfo) {
        return HwDeviceAdminInfoDummy.getDefault();
    }
}
