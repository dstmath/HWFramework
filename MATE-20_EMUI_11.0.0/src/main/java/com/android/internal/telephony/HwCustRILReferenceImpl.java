package com.android.internal.telephony;

import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.preference.PreferenceManager;

public class HwCustRILReferenceImpl extends HwCustRILReference {
    private static final boolean CUST_APN_AUTH_ON = SystemProperties.getBoolean("ro.config.hw_allow_pdp_auth", false);
    private static final boolean IS_SET_NETWORK_AUTO = SystemProperties.getBoolean("hw_mc.telephony.set_network_auto", false);
    private static final int PHONE_NUM_0 = 0;
    private static final String SUB0_NETWORK_IS_CLICK = "sub0_network_is_click";
    private static final String SUB1_NETWORK_IS_CLICK = "sub1_network_is_click";
    private static final String TAG = "HwCustRILReferenceImpl";

    public boolean isSetNetwrokAutoAfterSwitch(int phoneId) {
        Phone phone;
        if (!IS_SET_NETWORK_AUTO || (phone = PhoneFactory.getPhone(phoneId)) == null) {
            return false;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(phone.getContext());
        if (phoneId == 0) {
            return !prefs.getBoolean(SUB0_NETWORK_IS_CLICK, false);
        }
        return !prefs.getBoolean(SUB1_NETWORK_IS_CLICK, false);
    }
}
