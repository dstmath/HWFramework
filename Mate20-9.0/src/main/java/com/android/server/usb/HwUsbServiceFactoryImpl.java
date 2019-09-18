package com.android.server.usb;

import android.content.Context;
import com.android.server.usb.HwUsbServiceFactory;

public class HwUsbServiceFactoryImpl implements HwUsbServiceFactory.Factory {
    private static final String TAG = "HwUsbServiceFactoryImpl";

    public static class HwUsbDeviceManagerImpl implements HwUsbServiceFactory.IHwUsbDeviceManager {
        public UsbDeviceManager getInstance(Context context, UsbAlsaManager alsaManager, UsbSettingsManager settingsManager) {
            return new HwUsbDeviceManager(context, alsaManager, settingsManager);
        }
    }

    public HwUsbServiceFactory.IHwUsbDeviceManager getHuaweiUsbDeviceManager() {
        return new HwUsbDeviceManagerImpl();
    }
}
