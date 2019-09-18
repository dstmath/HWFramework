package com.android.internal.telephony;

import android.os.Message;

public interface IHwGsmCdmaPhoneEx {
    void autoExitEmergencyCallbackMode();

    boolean dialInternalForCdmaLte(String str);

    void restoreSavedRadioTech();

    void setCallForwardingOption(int i, int i2, String str, int i3, int i4, Message message);
}
