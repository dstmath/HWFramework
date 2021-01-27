package com.android.internal.telephony.dataconnection;

import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.Arrays;
import java.util.HashSet;

public class ApnSettingUtils {
    private static final boolean DBG = false;
    static final String LOG_TAG = "ApnSetting";

    private static boolean iccidMatches(String mvnoData, String iccId) {
        for (String mvnoIccid : mvnoData.split(",")) {
            if (iccId.startsWith(mvnoIccid)) {
                Log.d(LOG_TAG, "mvno icc id match found");
                return true;
            }
        }
        return false;
    }

    private static boolean imsiMatches(String imsiDB, String imsiSIM) {
        int len = imsiDB.length();
        if (len <= 0 || len > imsiSIM.length()) {
            return false;
        }
        for (int idx = 0; idx < len; idx++) {
            char c = imsiDB.charAt(idx);
            if (!(c == 'x' || c == 'X' || c == imsiSIM.charAt(idx))) {
                return false;
            }
        }
        return true;
    }

    public static boolean mvnoMatches(IccRecords r, int mvnoType, String mvnoMatchData) {
        String iccId;
        if (mvnoType == 0) {
            if (r.getServiceProviderName() != null && r.getServiceProviderName().equalsIgnoreCase(mvnoMatchData)) {
                return true;
            }
        } else if (mvnoType == 1) {
            String imsiSIM = r.getIMSI();
            if (imsiSIM != null && imsiMatches(mvnoMatchData, imsiSIM)) {
                return true;
            }
        } else if (mvnoType == 2) {
            String gid1 = r.getGid1();
            int mvno_match_data_length = mvnoMatchData.length();
            if (gid1 != null && gid1.length() >= mvno_match_data_length && gid1.substring(0, mvno_match_data_length).equalsIgnoreCase(mvnoMatchData)) {
                return true;
            }
        } else if (mvnoType == 3 && (iccId = r.getIccId()) != null && iccidMatches(mvnoMatchData, iccId)) {
            return true;
        }
        return false;
    }

    public static boolean isMeteredApnType(int apnType, Phone phone) {
        String carrierConfig;
        if (phone == null) {
            return true;
        }
        boolean isRoaming = phone.getServiceState().getDataRoaming();
        int subId = phone.getSubId();
        if (isRoaming) {
            carrierConfig = "carrier_metered_roaming_apn_types_strings";
        } else {
            carrierConfig = "carrier_metered_apn_types_strings";
        }
        CarrierConfigManager configManager = (CarrierConfigManager) phone.getContext().getSystemService("carrier_config");
        if (configManager == null) {
            Rlog.e(LOG_TAG, "Carrier config service is not available");
            return true;
        }
        PersistableBundle b = configManager.getConfigForSubId(subId);
        if (b == null) {
            Rlog.e(LOG_TAG, "Can't get the config. subId = " + subId);
            return true;
        }
        String[] meteredApnTypes = b.getStringArray(carrierConfig);
        if (meteredApnTypes == null) {
            Rlog.e(LOG_TAG, carrierConfig + " is not available. subId = " + subId);
            return true;
        }
        HashSet<String> meteredApnSet = new HashSet<>(Arrays.asList(meteredApnTypes));
        if (meteredApnSet.contains(ApnSetting.getApnTypeString(apnType))) {
            return true;
        }
        if (apnType != 8356095 || meteredApnSet.size() <= 0) {
            return false;
        }
        return true;
    }

    public static boolean isMetered(ApnSetting apn, Phone phone) {
        if (phone == null || apn == null) {
            return true;
        }
        for (Integer num : apn.getApnTypes()) {
            if (isMeteredApnType(num.intValue(), phone)) {
                return true;
            }
        }
        return false;
    }
}
