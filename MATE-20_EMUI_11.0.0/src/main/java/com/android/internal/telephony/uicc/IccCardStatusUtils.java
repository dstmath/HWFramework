package com.android.internal.telephony.uicc;

import com.huawei.internal.telephony.uicc.IccCardStatusExt;

public class IccCardStatusUtils {
    private IccCardStatusUtils() {
    }

    public static boolean isCardPresentHw(IccCardStatusExt.CardStateEx cardState) {
        if (cardState != null) {
            return cardState.isCardPresent();
        }
        return false;
    }
}
