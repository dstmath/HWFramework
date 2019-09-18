package com.android.internal.telephony.imsphone;

public interface IHwImsPhoneCallTrackerInner {
    int getEventPendingMo();

    HwCustImsPhoneCallTracker getHwCustImsPhoneCallTracker();

    ImsPhone getImsPhone();

    ImsPhoneCallTracker getImsPhoneCallTracker();

    ImsPhoneConnection getImsPhoneConnection();

    void logHw(String str);

    void removeConnectionHw(ImsPhoneConnection imsPhoneConnection);

    void removeMessagesHw(int i);

    void updatePhoneStateHw();
}
