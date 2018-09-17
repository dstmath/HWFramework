package com.android.internal.telephony.uicc;

public interface IsimRecords {
    String getIsimChallengeResponse(String str);

    String getIsimDomain();

    String getIsimImpi();

    String[] getIsimImpu();

    String getIsimIst();

    String[] getIsimPcscf();
}
