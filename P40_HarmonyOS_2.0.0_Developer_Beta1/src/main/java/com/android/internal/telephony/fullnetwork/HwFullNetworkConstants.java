package com.android.internal.telephony.fullnetwork;

public class HwFullNetworkConstants {
    public static final int CARD_TYPE_DUAL_MODE = 3;
    public static final int CARD_TYPE_NO_SIM = 0;
    public static final int CARD_TYPE_SINGLE_CDMA = 2;
    public static final int CARD_TYPE_SINGLE_GSM = 1;
    public static final int DEFAULT_VALUE = 0;
    public static final String IF_NEED_SET_RADIO_CAP = "if_need_set_radio_cap";
    public static final int NO_NEED_SET_RADIO_CAP = 1;

    private HwFullNetworkConstants() {
    }

    public static String getMasterPassword() {
        return HwFullNetworkManager.getInstance().getMasterPassword();
    }
}
