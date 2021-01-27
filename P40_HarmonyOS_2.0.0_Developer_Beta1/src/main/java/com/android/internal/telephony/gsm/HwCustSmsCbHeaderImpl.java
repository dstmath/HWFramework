package com.android.internal.telephony.gsm;

import com.huawei.android.os.SystemPropertiesEx;

public class HwCustSmsCbHeaderImpl extends HwCustSmsCbHeader {
    private static final String CBS_CONFIG_SBM = "ro.config.hw_cbs_sbm";
    private static final int CUSTOMIZE_CHANNEL_DISASTER_LOWER_LIMIT = 44032;
    private static final int CUSTOMIZE_CHANNEL_DISASTER_UPPER_LIMIT = 45037;
    private static final int CUSTOMIZE_CHANNEL_FOR_EARTHQUACK = 43008;
    private static final int CUSTOMIZE_CHANNEL_FOR_RAKUTEN = 43016;
    private static final int CUSTOMIZE_CHANNEL_FOR_TSUNAMI = 45038;
    private static final int DISASTER = 153;
    private static final int EARTHQUACK = 0;
    private static final boolean IS_SUPORT_CBS_RAKUTEN = SystemPropertiesEx.getBoolean("hw_mc.cbs.suport_cust_etws", (boolean) IS_SUPORT_CBS_RAKUTEN);
    private static final int SBM_REV_CHANNLE = -2;
    private static final int TSUNAMI = 1;

    public boolean isShowCbsSettingForSBM() {
        return SystemPropertiesEx.getBoolean(CBS_CONFIG_SBM, (boolean) IS_SUPORT_CBS_RAKUTEN);
    }

    public boolean isCustEtwsMessage(int channel) {
        if (IS_SUPORT_CBS_RAKUTEN && channel == CUSTOMIZE_CHANNEL_FOR_RAKUTEN) {
            return true;
        }
        if (!isShowCbsSettingForSBM() || !isEtwsMessageForSBM(channel)) {
            return IS_SUPORT_CBS_RAKUTEN;
        }
        return true;
    }

    public boolean isEtwsMessageForSBM(int channle) {
        if ((channle >= CUSTOMIZE_CHANNEL_DISASTER_LOWER_LIMIT && channle <= CUSTOMIZE_CHANNEL_DISASTER_UPPER_LIMIT) || channle == CUSTOMIZE_CHANNEL_FOR_EARTHQUACK || channle == CUSTOMIZE_CHANNEL_FOR_TSUNAMI) {
            return true;
        }
        return IS_SUPORT_CBS_RAKUTEN;
    }

    public int getEtwsTypeForSBM(int channle) {
        if (channle >= CUSTOMIZE_CHANNEL_DISASTER_LOWER_LIMIT && channle <= CUSTOMIZE_CHANNEL_DISASTER_UPPER_LIMIT) {
            return DISASTER;
        }
        if (channle == CUSTOMIZE_CHANNEL_FOR_EARTHQUACK) {
            return EARTHQUACK;
        }
        if (channle == CUSTOMIZE_CHANNEL_FOR_TSUNAMI) {
            return TSUNAMI;
        }
        return SBM_REV_CHANNLE;
    }
}
