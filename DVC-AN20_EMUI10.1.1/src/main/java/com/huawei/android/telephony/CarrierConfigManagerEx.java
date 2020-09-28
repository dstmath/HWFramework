package com.huawei.android.telephony;

import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class CarrierConfigManagerEx {
    public static final String KEY_ALLOW_ENABLING_WAP_PUSH_SI_BOOL = "allowEnablingWapPushSI";
    public static final String KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL = "carrier_default_wfc_ims_enabled_bool";
    public static final String KEY_CARRIER_DEFAULT_WFC_IMS_MODE_INT = "carrier_default_wfc_ims_mode_int";
    public static final String KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_ENABLED_BOOL = "carrier_default_wfc_ims_roaming_enabled_bool";
    public static final String KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT = "carrier_default_wfc_ims_roaming_mode_int";
    public static final String KEY_CARRIER_VOLTE_SHOW_SWITCH_BOOL = "carrier_volte_show_switch_bool";
    public static final String KEY_ENABLE_WAP_PUSH_SI_BOOL = "enableWapPushSI";
    public static final String KEY_GROUP_CHAT_DEFAULT_TO_MMS_BOOL = "groupChatDefaultsToMMS";
    public static final String KEY_SMS_DELIVERY_REPORT_SETTING_BOOL = "smsDeliveryReportSettingOnByDefault";
    public static final String KEY_SMS_USES_SIMPLE_CHARACTERS_ONLY_BOOL = "smsUsesSimpleCharactersOnly";
    public static final String KEY_USE_CUSTOM_USER_AGENT_BOOL = "useCustomUserAgent";
    public static final String KEY_WFC_SPN_FORMAT_IDX_INT = "wfc_spn_format_idx_int";

    public static PersistableBundle getDefaultConfig() {
        return CarrierConfigManager.getDefaultConfig();
    }
}
