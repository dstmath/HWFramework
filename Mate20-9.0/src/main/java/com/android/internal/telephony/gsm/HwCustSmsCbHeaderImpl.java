package com.android.internal.telephony.gsm;

import android.os.SystemProperties;

public class HwCustSmsCbHeaderImpl extends HwCustSmsCbHeader {
    public static final String CBS_CONFIG_SBM = "ro.config.hw_cbs_sbm";
    public static final int CUSTOMIZE_CHANNEL_DISASTER_LOWER_LIMIT = 44032;
    public static final int CUSTOMIZE_CHANNEL_DISASTER_UPPER_LIMIT = 45037;
    public static final int CUSTOMIZE_CHANNEL_FOR_EARTHQUACK = 43008;
    public static final int CUSTOMIZE_CHANNEL_FOR_TSUNAMI = 45038;
    private static final int DISASTER = 153;
    private static final int EARTHQUACK = 0;
    public static final int SBM_REV_CHANNLE = -2;
    private static final int TSUNAMI = 1;

    public boolean isShowCbsSettingForSBM() {
        return SystemProperties.getBoolean(CBS_CONFIG_SBM, false);
    }

    public boolean isEtwsMessageForSBM(int rev_channle) {
        if ((rev_channle >= 44032 && rev_channle <= 45037) || rev_channle == 43008 || rev_channle == 45038) {
            return true;
        }
        return false;
    }

    public int getEtwsTypeForSBM(int rev_channle) {
        if (rev_channle >= 44032 && rev_channle <= 45037) {
            return DISASTER;
        }
        if (rev_channle == 43008) {
            return 0;
        }
        if (rev_channle == 45038) {
            return 1;
        }
        return -2;
    }
}
