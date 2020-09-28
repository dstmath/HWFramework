package com.android.internal.telephony.uicc;

import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;

public class IccCardProxyUtils {
    public int getPhoneId(IUiccProfileInner iccCardProxy) {
        return iccCardProxy.getPhoneIdHw();
    }

    public CommandsInterfaceEx getCi(IUiccProfileInner iccCardProxy) {
        return iccCardProxy.getCiHw();
    }

    public static int getEventRadioOffOrUnavailable() {
        return UiccProfile.getEventRadioOffOrUnavailableHw();
    }

    public static int getEventAppReady() {
        return UiccProfile.getEventAppReadyHw();
    }

    public IccRecordsEx getIccRecords(IUiccProfileInner iccCardProxy) {
        return iccCardProxy.getIccRecordsHw();
    }

    public void broadcastIccStateChangedIntent(IUiccProfileInner iccCardProxy, String value, String reason) {
        iccCardProxy.broadcastIccStateChangedIntentHw(value, reason);
    }
}
