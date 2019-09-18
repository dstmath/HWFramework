package com.android.internal.telephony.uicc;

import com.android.internal.telephony.uicc.IccCardStatus;

public class IccCardStatusUtils {
    public static boolean isCardPresent(IccCardStatus.CardState cardState) {
        if (cardState != null) {
            return cardState.isCardPresent();
        }
        return false;
    }
}
