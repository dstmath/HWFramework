package com.android.internal.telephony.emergency;

import android.telephony.emergency.EmergencyNumber;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.emergency.EmergencyNumberEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HwEmergencyNumberTrackerMgrImpl implements HwEmergencyNumberTrackerMgr {
    private static final String LOG_TAG = "HwEmergencyNumberTrackerMgrImpl";
    private static final int SLOT0 = 0;
    private static final String STRING_CHINA_LOCAL_REGION = "CN";
    private static final String STRING_HW_ECCLIST = "ril.hw_ecclist";
    private static final String STRING_PROP_LOCAL_REGION = SystemPropertiesEx.get("ro.product.locale.region", "");
    private static final String SYMBOL_PLUS = "+";
    private static HwEmergencyNumberTrackerMgrImpl mInstance = new HwEmergencyNumberTrackerMgrImpl();

    public static HwEmergencyNumberTrackerMgrImpl getDefault() {
        return mInstance;
    }

    public String changeEcclistToHwEcclist(int slotId) {
        if (slotId <= 0) {
            return STRING_HW_ECCLIST;
        }
        return STRING_HW_ECCLIST + slotId;
    }

    public String splitEmergencyNum(String number) {
        if (number == null || !number.contains(SYMBOL_PLUS)) {
            return number;
        }
        return number.substring(number.indexOf(SYMBOL_PLUS) + 1, number.length());
    }

    public EmergencyNumber getCustLabeledEmergencyNumberForEcclist(List<EmergencyNumber> emergencyNumberListFromDatabase, String number, String countryIso) {
        int category = 0;
        String tmpNumber = number;
        if (number != null && number.contains(SYMBOL_PLUS)) {
            try {
                category = Integer.parseInt(number.substring(0, number.indexOf(SYMBOL_PLUS)));
            } catch (NumberFormatException e) {
                log("HwEmergencyNumberTrackerMgrImpl NumberFormatException error");
            }
            tmpNumber = number.substring(number.indexOf(SYMBOL_PLUS) + 1, number.length());
        }
        for (EmergencyNumber num : emergencyNumberListFromDatabase) {
            if (num != null && num.getNumber().equals(tmpNumber)) {
                return EmergencyNumberEx.getEmergencyNumber(tmpNumber, countryIso.toLowerCase(Locale.ENGLISH), "", EmergencyNumberEx.getEmergencyServiceCategoryBitmask(num), new ArrayList(), 16, 0);
            }
        }
        return EmergencyNumberEx.getEmergencyNumber(tmpNumber, countryIso.toLowerCase(Locale.ENGLISH), "", category, new ArrayList(), 0, 0);
    }

    public boolean isCustEmergencyNum(String number, String emergencyNum) {
        boolean isChinaRegion = STRING_CHINA_LOCAL_REGION.equalsIgnoreCase(STRING_PROP_LOCAL_REGION);
        log("HwEmergencyNumberTrackerMgrImpl isChinaRegion = " + isChinaRegion + ",isEqualsEnum = " + number.equals(emergencyNum) + ",isStartWitchEnum = " + number.startsWith(emergencyNum));
        if (isChinaRegion && number.equals(emergencyNum)) {
            return true;
        }
        if (isChinaRegion || !number.startsWith(emergencyNum)) {
            return false;
        }
        return true;
    }

    private void log(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }
}
