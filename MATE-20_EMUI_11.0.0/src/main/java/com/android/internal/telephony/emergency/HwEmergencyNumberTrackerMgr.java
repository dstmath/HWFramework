package com.android.internal.telephony.emergency;

import android.telephony.emergency.EmergencyNumber;
import java.util.List;

public interface HwEmergencyNumberTrackerMgr {
    default String changeEcclistToHwEcclist(int slotId) {
        return null;
    }

    default String splitEmergencyNum(String number) {
        return number;
    }

    default EmergencyNumber getCustLabeledEmergencyNumberForEcclist(List<EmergencyNumber> list, String number, String countryIso) {
        return null;
    }

    default boolean isCustEmergencyNum(String number, String emergencyNum) {
        return false;
    }
}
