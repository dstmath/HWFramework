package com.android.internal.telephony.uicc;

import com.android.internal.telephony.uicc.IccCardStatus.CardState;

public class IccCardStatusUtils {
    public static boolean isCardPresent(CardState cardState) {
        if (cardState != null) {
            return cardState.isCardPresent();
        }
        return false;
    }
}
