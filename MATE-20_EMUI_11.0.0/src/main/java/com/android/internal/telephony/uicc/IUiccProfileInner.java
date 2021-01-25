package com.android.internal.telephony.uicc;

import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;

public interface IUiccProfileInner {
    void broadcastIccStateChangedIntentHw(String str, String str2);

    int getCdmaSubscriptionAppIndex();

    CommandsInterfaceEx getCiHw();

    int getGsmUmtsSubscriptionAppIndex();

    boolean getIccCardStateHw();

    IccRecordsEx getIccRecordsHw();

    int getPhoneIdHw();

    UiccCard getUiccCardHw();
}
