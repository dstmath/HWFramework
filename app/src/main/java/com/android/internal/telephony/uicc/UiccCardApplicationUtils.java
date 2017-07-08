package com.android.internal.telephony.uicc;

public class UiccCardApplicationUtils {
    public UiccCard getUiccCard(UiccCardApplication uiccCardApplication) {
        return uiccCardApplication.getUiccCardHw();
    }
}
