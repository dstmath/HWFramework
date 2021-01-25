package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;

public interface IsimRecords {
    @UnsupportedAppUsage
    String getIsimDomain();

    @UnsupportedAppUsage
    String getIsimImpi();

    @UnsupportedAppUsage
    String[] getIsimImpu();

    String getIsimIst();

    String[] getIsimPcscf();
}
