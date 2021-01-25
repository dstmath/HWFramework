package huawei.android.app.admin;

import android.hdm.HwDeviceManager;
import android.hdm.HwDeviceMangerFactory;

public class HwDeviceMangerFactoryimpl extends HwDeviceMangerFactory {
    public HwDeviceManager.IHwDeviceManager getHuaweiDevicePolicyManager() {
        return new HwDeviceManagerImpl();
    }
}
