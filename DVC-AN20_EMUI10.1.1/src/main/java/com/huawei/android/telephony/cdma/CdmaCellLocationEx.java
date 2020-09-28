package com.huawei.android.telephony.cdma;

import android.telephony.cdma.CdmaCellLocation;

public class CdmaCellLocationEx {
    public static final int INVALID_ID = -1;

    public static int getLac(CdmaCellLocation cdmaCellLocation) {
        if (cdmaCellLocation != null) {
            return cdmaCellLocation.getLac();
        }
        return -1;
    }

    public static int getCid(CdmaCellLocation cdmaCellLocation) {
        if (cdmaCellLocation != null) {
            return cdmaCellLocation.getCid();
        }
        return -1;
    }
}
