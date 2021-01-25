package com.huawei.internal.telephony;

import com.huawei.annotation.HwSystemApi;

public interface TelephonyPropertiesEx {
    public static final String CALLS_ON_IMS_ENABLED_PROPERTY = "ro.config.hw_volte_on";
    public static final String PROPERTY_CSVT_ENABLED = "persist.radio.csvt.enabled";
    public static final String PROPERTY_DEFAULT_SUBSCRIPTION = "persist.radio.default.sub";
    @HwSystemApi
    public static final String PROPERTY_DUAL_CMCC_UNICOM_DEVICE = "ro.hwpp.dualcu";
    @HwSystemApi
    public static final String PROPERTY_FULL_NETWORK_SUPPORT = "ro.config.full_network_support";
    public static final String PROPERTY_ICC_ATR = "gsm.sim.hw_atr";
    public static final String PROPERTY_ICC_ATR1 = "gsm.sim.hw_atr1";
    public static final String PROPERTY_ICC_OPERATOR_NUMERIC = "gsm.sim.operator.numeric";
    public static final String PROPERTY_INECM_MODE = "ril.cdma.inecmmode";
    public static final String PROPERTY_MULTI_SIM_CONFIG = "persist.radio.multisim.config";
    public static final String PROPERTY_MULTI_SIM_ENABLED = "persist.dsds.enabled";
    @HwSystemApi
    public static final String PROPERTY_OPERATOR_ALPHA_VSIM = "gsm.operator.alpha.vsim";
    @HwSystemApi
    public static final String PROPERTY_OPERATOR_ISO_COUNTRY = "gsm.operator.iso-country";
    @HwSystemApi
    public static final String PROPERTY_OPERATOR_ISO_COUNTRY_VSIM = "gsm.operator.iso-country.vsim";
    public static final String PROPERTY_OPERATOR_ISROAMING = "gsm.operator.isroaming";
    public static final String PROPERTY_OPERATOR_NUMERIC = "gsm.operator.numeric";
    @HwSystemApi
    public static final String PROPERTY_OPERATOR_NUMERIC_VSIM = "gsm.operator.numeric.vsim";
    @HwSystemApi
    public static final String PROPERTY_SIM_STATE = "gsm.sim.state";
    @HwSystemApi
    public static final String PROPERTY_TUNERIC_LOW_PERF = "ro.hwpp.is_tuneric_low_perf";
    @HwSystemApi
    public static final String PROPERTY_VSIM_MODEM_COUNT = "ro.radio.vsim_modem_count";
    @HwSystemApi
    public static final String PROPERTY_VSIM_STATE = "gsm.vsim.state";
    @HwSystemApi
    public static final String PROPERTY_VSIM_SUPPORTED = "ro.radio.vsim_support";
    public static final String PROPERTY_WAKE_LOCK_TIMEOUT = "ro.ril.wake_lock_timeout";
}
