package com.android.internal.telephony;

public interface AbstractTelephonyProperties {
    public static final String EXTRAS_IS_CONFERENCE_URI = "isConferenceUri";
    public static final String EXTRA_IS_CALL_PULL = "android.telephony.extra.IS_CALL_PULL";
    public static final String PROPERTY_CDMA_OPERATOR_MCC = "ril.currMcc";
    public static final String PROPERTY_CDMA_OPERATOR_MCC_FROM_NW = "ril.radio.cdma.nw_mcc";
    public static final String PROPERTY_CDMA_TIME_LTMOFFSET = "ril.radio.cdma.ltmoffset";
    public static final String PROPERTY_CG_STANDBY_MODE = "persist.radio.cg_standby_mode";
    public static final String PROPERTY_COMMRIL_MODE = "persist.radio.commril_mode";
    public static final String PROPERTY_DUAL_CMCC_UNICOM_DEVICE = "ro.hwpp.dualcu";
    public static final String PROPERTY_FULL_NETWORK_SUPPORT = "ro.config.full_network_support";
    public static final String PROPERTY_ICC_ATR = "gsm.sim.hw_atr";
    public static final String PROPERTY_ICC_ATR1 = "gsm.sim.hw_atr1";
    public static final String PROPERTY_ICC_CDMA_OPERATOR_MCC = "ril.radio.cdma.icc_mcc";
    public static final String PROPERTY_OPERATOR_ALPHA_VSIM = "gsm.operator.alpha.vsim";
    public static final String PROPERTY_OPERATOR_ISO_COUNTRY_VSIM = "gsm.operator.iso-country.vsim";
    public static final String PROPERTY_OPERATOR_NUMERIC_VSIM = "gsm.operator.numeric.vsim";
    public static final String PROPERTY_RAT_ON = "persist.radio.rat_on";
    public static final String PROPERTY_STK_HIDE = "gsm.stk.hide";
    public static final String PROPERTY_TUNERIC_LOW_PERF = "ro.hwpp.is_tuneric_low_perf";
    public static final String PROPERTY_UIM_ID = "persist.radio.hwuimid";
    public static final String PROPERTY_UIM_PRL = "persist.radio.hwprlversion";
    public static final String PROPERTY_VSIM_MODEM_COUNT = "ro.radio.vsim_modem_count";
    public static final String PROPERTY_VSIM_STATE = "gsm.vsim.state";
    public static final String PROPERTY_VSIM_SUPPORTED = "ro.radio.vsim_support";
}
