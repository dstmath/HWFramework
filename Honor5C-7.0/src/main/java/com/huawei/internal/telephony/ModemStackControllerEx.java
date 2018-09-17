package com.huawei.internal.telephony;

import android.os.Handler;
import com.android.internal.telephony.HwModemStackController;

public class ModemStackControllerEx {
    public static void registerForStackReady(Handler h, int what, Object obj) {
        HwModemStackController.getInstance().registerForStackReady(h, what, obj);
    }

    public static boolean isStackReady() {
        return HwModemStackController.getInstance().isStackReady();
    }

    public static int getPrimarySub() {
        return HwModemStackController.getInstance().getPrimarySub();
    }
}
