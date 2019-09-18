package com.android.internal.telephony.uicc;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccCardConstants;

public class IccCardProxyUtils {
    public int getPhoneId(UiccProfile iccCardProxy) {
        return iccCardProxy.getPhoneIdHw();
    }

    public CommandsInterface getCi(UiccProfile iccCardProxy) {
        return iccCardProxy.getCiHw();
    }

    public static int getEventRadioOffOrUnavailable() {
        return UiccProfile.getEventRadioOffOrUnavailableHw();
    }

    public static int getEventAppReady() {
        return UiccProfile.getEventAppReadyHw();
    }

    public UiccCard getUiccCard(UiccProfile iccCardProxy) {
        return iccCardProxy.getUiccCardHw();
    }

    public IccRecords getIccRecords(UiccProfile iccCardProxy) {
        return iccCardProxy.getIccRecordsHw();
    }

    public void setRadioOn(UiccProfile iccCardProxy, boolean value) {
        iccCardProxy.setRadioOnHw(value);
    }

    public void registerUiccCardEvents(UiccProfile iccCardProxy) {
        iccCardProxy.registerUiccCardEventsHw();
    }

    public void unregisterUiccCardEvents(UiccProfile iccCardProxy) {
        iccCardProxy.unregisterUiccCardEventsHw();
    }

    public void broadcastIccStateChangedIntent(UiccProfile iccCardProxy, String value, String reason) {
        iccCardProxy.broadcastIccStateChangedIntentHw(value, reason);
    }

    public void setExternalState(UiccProfile iccCardProxy, IccCardConstants.State newState) {
        iccCardProxy.setExternalStateHw(newState);
    }

    public String getIccStateIntentString(UiccProfile iccCardProxy, IccCardConstants.State state) {
        return iccCardProxy.getIccStateIntentStringHw(state);
    }
}
