package com.huawei.android.ims;

import com.android.ims.HwImsUtManager;
import com.android.ims.ImsUt;

public class HwImsUtManagerEx {
    public static String getUtIMPUFromNetwork(int phoneId, Object imsUt) {
        if (imsUt instanceof ImsUt) {
            return HwImsUtManager.getUtIMPUFromNetwork(phoneId, (ImsUt) imsUt);
        }
        return null;
    }
}
