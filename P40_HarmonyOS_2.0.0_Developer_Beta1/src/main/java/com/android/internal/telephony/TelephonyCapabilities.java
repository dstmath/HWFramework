package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.telephony.Rlog;

public class TelephonyCapabilities {
    private static final String LOG_TAG = "TelephonyCapabilities";

    private TelephonyCapabilities() {
    }

    public static boolean supportsEcm(Phone phone) {
        Rlog.d("TelephonyCapabilities[SUB" + phone.getPhoneId() + "]", "supportsEcm: Phone type = " + phone.getPhoneType() + " Ims Phone = " + phone.getImsPhone());
        return phone.getPhoneType() == 2 || phone.getImsPhone() != null;
    }

    public static boolean supportsOtasp(Phone phone) {
        return phone.getPhoneType() == 2;
    }

    public static boolean supportsVoiceMessageCount(Phone phone) {
        return phone.getVoiceMessageCount() != -1;
    }

    public static boolean supportsNetworkSelection(Phone phone) {
        return phone.getPhoneType() == 1;
    }

    public static int getDeviceIdLabel(Phone phone) {
        if (phone.getPhoneType() == 1) {
            return 17040292;
        }
        if (phone.getPhoneType() == 2) {
            return 17040577;
        }
        Rlog.w("TelephonyCapabilities[SUB" + phone.getPhoneId() + "]", "getDeviceIdLabel: no known label for phone " + phone.getPhoneName());
        return 0;
    }

    public static boolean supportsConferenceCallManagement(Phone phone) {
        if (phone.getPhoneType() == 1 || phone.getPhoneType() == 3) {
            return true;
        }
        return false;
    }

    public static boolean supportsHoldAndUnhold(Phone phone) {
        if (phone.getPhoneType() == 1 || phone.getPhoneType() == 3 || phone.getPhoneType() == 5) {
            return true;
        }
        return false;
    }

    public static boolean supportsAnswerAndHold(Phone phone) {
        if (phone.getPhoneType() == 1 || phone.getPhoneType() == 3) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage
    public static boolean supportsAdn(int phoneType) {
        return phoneType == 1 || phoneType == 2;
    }

    public static boolean canDistinguishDialingAndConnected(int phoneType) {
        return phoneType == 1;
    }
}
