package com.android.internal.telephony;

import com.huawei.internal.telephony.GsmCdmaConnectionEx;
import com.huawei.internal.telephony.PhoneExt;
import java.util.List;

public interface IGsmCdmaCallTrackerInner {
    List<GsmCdmaConnectionEx> getBackgroundCallConnections();

    List<GsmCdmaConnectionEx> getForegroundCallConnections();

    GsmCdmaConnectionEx getForegroundCallLatestConnection();

    IHwGsmCdmaCallTrackerEx getHwGsmCdmaCallTrackerEx();

    PhoneExt getPhoneHw();

    List<GsmCdmaConnectionEx> getRingingCallConnections();

    void voiceCallEndedRegistrantsNotifyHw();
}
