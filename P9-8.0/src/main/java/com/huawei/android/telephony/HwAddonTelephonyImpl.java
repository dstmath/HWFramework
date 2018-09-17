package com.huawei.android.telephony;

import android.util.Log;
import com.android.internal.telephony.HwAddonTelephonyFactory.HwAddonTelephonyInterface;

public class HwAddonTelephonyImpl implements HwAddonTelephonyInterface {
    private static final String TAG = "HwAddonTelephonyFactoryImpl";

    public int getDefault4GSlotId() {
        int slotId = 0;
        try {
            return TelephonyManagerEx.getDefault4GSlotId();
        } catch (Exception e) {
            Log.e(TAG, "getDefault4GSlotId exception is " + e);
            return slotId;
        }
    }
}
