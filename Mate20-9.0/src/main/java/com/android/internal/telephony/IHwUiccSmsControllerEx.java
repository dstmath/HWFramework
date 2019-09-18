package com.android.internal.telephony;

import android.content.Context;

public interface IHwUiccSmsControllerEx {
    String getMeidOrPesn(int i);

    String getSmscAddrForSubscriber(int i);

    boolean isUimSupportMeid(int i);

    boolean setCellBroadcastRangeListForSubscriber(int i, int[] iArr, int i2);

    void setEnabledSingleShiftTables(Context context, int[] iArr);

    boolean setMeidOrPesn(int i, String str, String str2);

    void setSmsCodingNationalCode(Context context, String str);

    boolean setSmscAddrForSubscriber(int i, String str);
}
