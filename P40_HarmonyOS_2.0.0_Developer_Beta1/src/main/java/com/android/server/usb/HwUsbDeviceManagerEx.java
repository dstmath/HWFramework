package com.android.server.usb;

import android.content.Context;
import android.util.Slog;
import com.huawei.android.os.SystemPropertiesEx;

public class HwUsbDeviceManagerEx implements IHwUsbDeviceManagerEx {
    private static final String HW_HICAR_MODE = "ro.config.hw_hicar_mode";
    private static final String HW_HICAR_MODE_NONSUPPORT = "false";
    private static final String HW_HICAR_MODE_SUPPORT = "true";
    private static final String TAG = HwUsbDeviceManagerEx.class.getSimpleName();
    private Context mContext;
    private IHwUsbDeviceManagerInner mHwUsbDeviceManagerInner;
    private HwUsbHiCarManager mHwUsbHiCarManager = null;
    private HwUsbNearbyManager mHwUsbNearbyManager = null;

    public HwUsbDeviceManagerEx(IHwUsbDeviceManagerInner ums, Context context) {
        this.mHwUsbDeviceManagerInner = ums;
        this.mContext = context;
        if (isSupportHiCarMode()) {
            this.mHwUsbHiCarManager = new HwUsbHiCarManager(this.mContext);
        }
        this.mHwUsbNearbyManager = new HwUsbNearbyManager(this.mContext);
    }

    private boolean isSupportHiCarMode() {
        boolean isSupportHiCarMode = "true".equals(SystemPropertiesEx.get(HW_HICAR_MODE, "false"));
        String str = TAG;
        Slog.i(str, "Get HiCarModeProperty, isSupportHiCarMode: " + isSupportHiCarMode);
        return isSupportHiCarMode;
    }

    public void notifyHiCarInfo(byte[] hiCarInfoBytes, boolean isConnected) {
        HwUsbHiCarManager hwUsbHiCarManager = this.mHwUsbHiCarManager;
        if (hwUsbHiCarManager != null) {
            hwUsbHiCarManager.notifyHiCarInfo(hiCarInfoBytes, isConnected);
        } else if (isConnected) {
            String str = TAG;
            Slog.e(str, "mHwUsbHiCarManager is null, isSupportHiCarMode: " + isSupportHiCarMode());
        }
    }

    public void notifyNearbyInfo(byte[] nearbyInfoBytes, boolean isConnected) {
        HwUsbNearbyManager hwUsbNearbyManager = this.mHwUsbNearbyManager;
        if (hwUsbNearbyManager != null) {
            hwUsbNearbyManager.notifyNearbyInfo(nearbyInfoBytes, isConnected);
        }
    }
}
