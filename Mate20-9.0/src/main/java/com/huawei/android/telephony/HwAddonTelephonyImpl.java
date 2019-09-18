package com.huawei.android.telephony;

import android.util.Log;
import com.android.internal.telephony.HwAddonTelephonyFactory;

public class HwAddonTelephonyImpl implements HwAddonTelephonyFactory.HwAddonTelephonyInterface {
    private static final String TAG = "HwAddonTelephonyFactoryImpl";

    public int getDefault4GSlotId() {
        try {
            return TelephonyManagerEx.getDefault4GSlotId();
        } catch (Exception e) {
            Log.e(TAG, "getDefault4GSlotId exception is " + e);
            return 0;
        }
    }
}
