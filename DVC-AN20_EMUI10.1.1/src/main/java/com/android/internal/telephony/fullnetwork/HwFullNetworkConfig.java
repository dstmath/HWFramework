package com.android.internal.telephony.fullnetwork;

import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.huawei.android.os.SystemPropertiesEx;

public class HwFullNetworkConfig {
    public static final boolean IS_AIS_4G_DSDX_ENABLE = (HuaweiTelephonyConfigs.isMTKPlatform() && "ais".equalsIgnoreCase(SystemPropertiesEx.get("ro.hwpp.dualsim_swap_solution", "")));
    public static final boolean IS_CHINA_TELECOM = (SystemPropertiesEx.get("ro.config.hw_opta", "0").equals(PROPERTY_HW_OPTA_TELECOM) && SystemPropertiesEx.get("ro.config.hw_optb", "0").equals(PROPERTY_HW_OPTB_CHINA));
    public static final boolean IS_CT_4GSWITCH_DISABLE = HwTelephonyManagerInner.TAG_MDM_CARRIER_CT.equalsIgnoreCase(SystemPropertiesEx.get("ro.hwpp.dualsim_swap_solution", ""));
    public static final boolean IS_FAST_SWITCH_SIMSLOT = SystemPropertiesEx.getBoolean("ro.config.fast_switch_simslot", false);
    public static final boolean IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT = (HuaweiTelephonyConfigs.isHisiPlatform() && "normal".equals(SystemPropertiesEx.get("ro.runmode", "normal")) && SystemPropertiesEx.getBoolean("ro.config.hw_switchdata_4G", false));
    public static final boolean IS_HISI_DSDX;
    public static final boolean IS_QCOM_DUAL_LTE_STACK = HwModemCapability.isCapabilitySupport(27);
    public static final boolean IS_QCRIL_CROSS_MAPPING = SystemPropertiesEx.getBoolean("ro.hwpp.qcril_cross_mapping", false);
    private static final String PROPERTY_HW_OPTA_TELECOM = "92";
    private static final String PROPERTY_HW_OPTB_CHINA = "156";

    static {
        boolean z = true;
        if (!HuaweiTelephonyConfigs.isHisiPlatform() || !"normal".equals(SystemPropertiesEx.get("ro.runmode", "normal")) || IS_CHINA_TELECOM) {
            z = false;
        }
        IS_HISI_DSDX = z;
    }

    private HwFullNetworkConfig() {
    }
}
