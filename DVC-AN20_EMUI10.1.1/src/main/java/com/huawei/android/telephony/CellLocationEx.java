package com.huawei.android.telephony;

import android.os.Bundle;
import android.telephony.CellLocation;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class CellLocationEx {
    public static CellLocation newFromBundle(Bundle bundle, int slotId) {
        return CellLocation.newFromBundle(bundle, slotId);
    }

    public static boolean isEmpty(CellLocation cl) {
        return cl.isEmpty();
    }

    public static void fillInNotifierBundle(CellLocation cl, Bundle bundle) {
        cl.fillInNotifierBundle(bundle);
    }
}
