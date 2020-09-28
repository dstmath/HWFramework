package com.android.internal.telephony.gsm;

import com.huawei.android.os.SystemPropertiesEx;

public class HwCustSmsCbHeaderImpl extends HwCustSmsCbHeader {
    public static final String CBS_CONFIG_SBM = "ro.config.hw_cbs_sbm";
    public static final int CUSTOMIZE_CHANNEL_DISASTER_LOWER_LIMIT = 44032;
    public static final int CUSTOMIZE_CHANNEL_DISASTER_UPPER_LIMIT = 45037;
    public static final int CUSTOMIZE_CHANNEL_FOR_EARTHQUACK = 43008;
    private static final int CUSTOMIZE_CHANNEL_FOR_RAKUTEN = 43016;
    public static final int CUSTOMIZE_CHANNEL_FOR_TSUNAMI = 45038;
    private static final int DISASTER = 153;
    private static final int EARTHQUACK = 0;
    public static final int SBM_REV_CHANNLE = -2;
    private static final boolean SUPORT_CBS_RAKUTEN = SystemPropertiesEx.getBoolean("hw_mc.cbs.suport_cust_etws", (boolean) SUPORT_CBS_RAKUTEN);
    private static final int TSUNAMI = 1;

    public boolean isShowCbsSettingForSBM() {
        return SystemPropertiesEx.getBoolean(CBS_CONFIG_SBM, (boolean) SUPORT_CBS_RAKUTEN);
    }

    public boolean isCustEtwsMessage(int channel) {
        if (SUPORT_CBS_RAKUTEN && channel == CUSTOMIZE_CHANNEL_FOR_RAKUTEN) {
            return true;
        }
        if (!isShowCbsSettingForSBM() || !isEtwsMessageForSBM(channel)) {
            return SUPORT_CBS_RAKUTEN;
        }
        return true;
    }

    public boolean isEtwsMessageForSBM(int rev_channle) {
        if ((rev_channle >= 44032 && rev_channle <= 45037) || rev_channle == 43008 || rev_channle == 45038) {
            return true;
        }
        return SUPORT_CBS_RAKUTEN;
    }

    public int getEtwsTypeForSBM(int rev_channle) {
        if (rev_channle >= 44032 && rev_channle <= 45037) {
            return DISASTER;
        }
        if (rev_channle == 43008) {
            return EARTHQUACK;
        }
        if (rev_channle == 45038) {
            return TSUNAMI;
        }
        return -2;
    }
}
