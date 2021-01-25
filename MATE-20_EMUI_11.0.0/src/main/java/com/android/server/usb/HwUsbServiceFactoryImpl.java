package com.android.server.usb;

import android.content.Context;
import com.android.server.usb.HwUsbServiceFactory;
import com.huawei.server.HwBasicPlatformFactory;

public class HwUsbServiceFactoryImpl implements HwUsbServiceFactory.Factory {
    private static final String TAG = "HwUsbServiceFactoryImpl";

    public HwUsbServiceFactory.IHwUsbDeviceManager getHuaweiUsbDeviceManager() {
        return new HwUsbDeviceManagerImpl();
    }

    public static class HwUsbDeviceManagerImpl implements HwUsbServiceFactory.IHwUsbDeviceManager {
        public UsbDeviceManager getInstance(Context context, UsbAlsaManager alsaManager, UsbSettingsManager settingsManager) {
            UsbAlsaManagerEx alsaManagerEx = new UsbAlsaManagerEx();
            alsaManagerEx.setUsbAlsaManager(alsaManager);
            UsbSettingsManagerEx settingsManagerEx = new UsbSettingsManagerEx();
            settingsManagerEx.setUsbSettingsManager(settingsManager);
            return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwUsbDeviceManager(context, alsaManagerEx, settingsManagerEx).getUsbDeviceManager();
        }
    }
}
