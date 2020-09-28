package com.huawei.internal.telephony.emergency;

import android.telephony.emergency.EmergencyNumber;
import java.util.List;

public class EmergencyNumberEx {
    public static int getEmergencyServiceCategoryBitmask(EmergencyNumber emergencyNumber) {
        if (emergencyNumber != null) {
            return emergencyNumber.getEmergencyServiceCategoryBitmask();
        }
        return 0;
    }

    public static EmergencyNumber getEmergencyNumber(String number, String countryIso, String mnc, int emergencyServiceCategories, List<String> emergencyUrns, int emergencyNumberSources, int emergencyCallRouting) {
        return new EmergencyNumber(number, countryIso, mnc, emergencyServiceCategories, emergencyUrns, emergencyNumberSources, emergencyCallRouting);
    }
}
