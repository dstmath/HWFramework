package com.android.internal.telephony;

public class UiccSmsControllerUtils {
    public IccSmsInterfaceManager getIccSmsInterfaceManager(UiccSmsController uiccSmsController, int subId) {
        return uiccSmsController.getIccSmsInterfaceManagerHw(subId);
    }
}
