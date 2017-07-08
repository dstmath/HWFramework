package com.android.internal.telephony.uicc;

import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccCardConstants.State;

public class IccCardProxyUtils {
    public int getPhoneId(IccCardProxy iccCardProxy) {
        return iccCardProxy.getPhoneIdHw();
    }

    public CommandsInterface getCi(IccCardProxy iccCardProxy) {
        return iccCardProxy.getCiHw();
    }

    public static int getEventRadioOffOrUnavailable() {
        return IccCardProxy.getEventRadioOffOrUnavailableHw();
    }

    public static int getEventAppReady() {
        return IccCardProxy.getEventAppReadyHw();
    }

    public UiccCard getUiccCard(IccCardProxy iccCardProxy) {
        return iccCardProxy.getUiccCardHw();
    }

    public IccRecords getIccRecords(IccCardProxy iccCardProxy) {
        return iccCardProxy.getIccRecordsHw();
    }

    public void setRadioOn(IccCardProxy iccCardProxy, boolean value) {
        iccCardProxy.setRadioOnHw(value);
    }

    public void registerUiccCardEvents(IccCardProxy iccCardProxy) {
        iccCardProxy.registerUiccCardEventsHw();
    }

    public void unregisterUiccCardEvents(IccCardProxy iccCardProxy) {
        iccCardProxy.unregisterUiccCardEventsHw();
    }

    public void broadcastIccStateChangedIntent(IccCardProxy iccCardProxy, String value, String reason) {
        iccCardProxy.broadcastIccStateChangedIntentHw(value, reason);
    }

    public void setExternalState(IccCardProxy iccCardProxy, State newState) {
        iccCardProxy.setExternalStateHw(newState);
    }

    public String getIccStateIntentString(IccCardProxy iccCardProxy, State state) {
        return iccCardProxy.getIccStateIntentStringHw(state);
    }
}
