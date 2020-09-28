package com.huawei.dmsdpsdk2.hiplay;

import com.huawei.dmsdp.devicevirtualization.Capability;
import com.huawei.dmsdpsdk2.HwLog;

public class HiPlayUtils {
    private static final String TAG = "HiPlayUtils";

    public static boolean isHiPlayNeedDevice(int deviceType) {
        if (deviceType == 3 || deviceType == 6) {
            return true;
        }
        HwLog.i(TAG, "Not HiPlay need TV or VoideBox device");
        return false;
    }

    public static boolean isHiPlayNeedService(int serviceType) {
        if (serviceType == 4) {
            return true;
        }
        HwLog.i(TAG, "Not HiPlay need speaker service");
        return false;
    }

    public static boolean isHiPlayNeedCapability(Capability cat) {
        if (Capability.SPEAKER.equals(cat)) {
            return true;
        }
        HwLog.i(TAG, "Not HiPlay need Speaker Capability");
        return false;
    }
}
