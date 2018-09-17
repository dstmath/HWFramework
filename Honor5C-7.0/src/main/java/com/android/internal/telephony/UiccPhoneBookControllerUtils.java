package com.android.internal.telephony;

public class UiccPhoneBookControllerUtils {
    public IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager(UiccPhoneBookController uiccPhoneBookController, int subId) {
        return uiccPhoneBookController.getIccPhoneBookInterfaceManagerHw(subId);
    }

    public int getDefaultSubscription(UiccPhoneBookController uiccPhoneBookController) {
        return uiccPhoneBookController.getDefaultSubscriptionHw();
    }
}
