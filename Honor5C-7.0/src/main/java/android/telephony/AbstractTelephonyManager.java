package android.telephony;

import android.common.HwFrameworkFactory;

public class AbstractTelephonyManager {
    public boolean setDualCardMode(int nMode) {
        return HwFrameworkFactory.getHwInnerTelephonyManager().setDualCardMode(nMode);
    }

    public int getDualCardMode() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().getDualCardMode();
    }

    public String getPesn() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().getPesn();
    }

    public static boolean isSms7BitEnabled() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().isSms7BitEnabled();
    }
}
