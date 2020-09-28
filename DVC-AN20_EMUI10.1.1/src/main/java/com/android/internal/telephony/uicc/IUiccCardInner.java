package com.android.internal.telephony.uicc;

import android.os.Message;
import com.huawei.internal.telephony.uicc.UiccCardExt;

public interface IUiccCardInner {
    int getCdmaSubscriptionAppIndex();

    int getGsmUmtsSubscriptionAppIndex();

    UiccCardExt getUiccCard();

    void iccGetATR(Message message);

    boolean isCardUimLocked();
}
