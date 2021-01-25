package com.android.server.wifi.HwUtil;

import android.telephony.HwTelephonyManager;
import com.android.server.wifi.hwUtil.IHwTelphonyUtilsEx;

public class HwTelphonyUtils implements IHwTelphonyUtilsEx {
    private static HwTelphonyUtils mHwTelphonyUtils = null;

    public static synchronized HwTelphonyUtils getDefault() {
        HwTelphonyUtils hwTelphonyUtils;
        synchronized (HwTelphonyUtils.class) {
            if (mHwTelphonyUtils == null) {
                mHwTelphonyUtils = new HwTelphonyUtils();
            }
            hwTelphonyUtils = mHwTelphonyUtils;
        }
        return hwTelphonyUtils;
    }

    public boolean notifyDeviceState(String device, String state, String extras) {
        return HwTelephonyManager.getDefault().notifyDeviceState(device, state, extras);
    }

    public int getDefault4GSlotId() {
        return HwTelephonyManager.getDefault().getDefault4GSlotId();
    }

    public String getCdmaGsmImsi() {
        return HwTelephonyManager.getDefault().getCdmaGsmImsi();
    }

    public boolean isCDMASimCard(int slotId) {
        return HwTelephonyManager.getDefault().isCDMASimCard(slotId);
    }
}
