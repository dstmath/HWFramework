package com.huawei.internal.telephony;

import com.huawei.annotation.HwSystemApi;

public class TelephonyIntentsEx {
    public static final String ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED";
    public static final String ACTION_DEFAULT_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_SUBSCRIPTION_CHANGED";
    @HwSystemApi
    public static final String ACTION_DUAL_SIM_IMSI_CHANGE = "android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE";
    public static final String ACTION_EMERGENCY_CALLBACK_MODE_CHANGED = "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED";
    @HwSystemApi
    public static final String ACTION_HW_CRR_CONN_IND = "com.huawei.action.ACTION_HW_CRR_CONN_IND";
    @HwSystemApi
    public static final String ACTION_HW_CSP_PLMN_CHANGE = "android.intent.action.ACTION_HW_CSP_PLMN_CHANGE";
    @HwSystemApi
    public static final String ACTION_HW_DSDS_MODE_STATE = "com.huawei.action.ACTION_HW_DSDS_MODE_STATE";
    @HwSystemApi
    public static final String ACTION_HW_DUAL_PS_STATE = "com.huawei.action.ACTION_HW_DUAL_PS_STATE";
    @HwSystemApi
    public static final String ACTION_HW_SWITCH_SLOT_DONE = "com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE";
    public static final String ACTION_MANAGED_ROAMING_IND = "codeaurora.intent.action.ACTION_MANAGED_ROAMING_IND";
    @HwSystemApi
    public static final String ACTION_MDM_DISABLE_SUB_RESULT = "android.intent.ACTION_MDM_DISABLE_SUB_RESULT";
    public static final String ACTION_MDM_POLICY_CHANGED = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    public static final String ACTION_NATIONAL_MODE_CHANGE = "android.intent.action.ACTION_NATIONAL_MODE_CHANGE";
    @HwSystemApi
    public static final String ACTION_NETWORK_SCAN_COMPLETE = "com.huawei.vsim.action.NETWORK_SCAN_COMPLETE";
    @HwSystemApi
    public static final String ACTION_RADIO_TECHNOLOGY_CHANGED = "android.intent.action.RADIO_TECHNOLOGY";
    public static final String ACTION_SERVICE_STATE_CHANGED = "android.intent.action.SERVICE_STATE";
    @HwSystemApi
    public static final String ACTION_SET_RADIO_CAPABILITY_DONE = "android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE";
    @HwSystemApi
    public static final String ACTION_SET_RADIO_CAPABILITY_FAILED = "android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED";
    public static final String ACTION_SET_SUBSCRIPTION_DONE = "com.huawei.intent.action.ACTION_SET_SUBSCRIPTION_DONE";
    public static final String ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS = "com.android.internal.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS";
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    @HwSystemApi
    public static final String ACTION_SIM_STATE_CHANGED_INTERNAL = "com.huawei.intent.action.ACTION_SIM_STATE_CHANGED";
    @HwSystemApi
    public static final String ACTION_SPEECH_CODEC_WB = "com.huawei.intent.action.SPEECH_CODEC_WB";
    public static final String ACTION_SUBINFO_CONTENT_CHANGE = "android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE";
    public static final String ACTION_SUBINFO_RECORD_UPDATED = "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED";
    public static final String ACTION_SUBSCRIPTION_SET_UICC_RESULT = "com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT";
    public static final String ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW = "android.intent.action.ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW";
    @HwSystemApi
    public static final String ACTION_VSIM_CARD_RELOAD = "com.huawei.vsim.action.VSIM_CARD_RELOAD";
    @HwSystemApi
    public static final String ACTION_VSIM_SERVICE_READY = "com.huawei.vsim.action.VSIM_SERVICE_READY";
    @HwSystemApi
    public static final String ACTION_VSIM_STATE_CHANGED = "com.huawei.vsim.action.VSIM_STATE_CHANGED";
    @HwSystemApi
    public static final String EXTRA_COLUMN_NAME = "columnName";
    public static final String EXTRA_DATA_SPN = "spnData";
    @HwSystemApi
    public static final String EXTRA_INT_CONTENT = "intContent";
    public static final String EXTRA_NEW_SUB_STATE = "newSubState";
    public static final String EXTRA_PLMN = "plmn";
    public static final String EXTRA_RESULT = "operationResult";
    public static final String EXTRA_SHOW_PLMN = "showPlmn";
    public static final String EXTRA_SHOW_SPN = "showSpn";
    @HwSystemApi
    public static final String EXTRA_SPEECH_CODEC_WB = "speechCodecWb";
    public static final String EXTRA_SPN = "spn";
    @HwSystemApi
    public static final String HUAWEI_SIM_REG_PLMNSELINFO_ACTION = "com.huawei.action.SIM_PLMN_SELINFO";
    @HwSystemApi
    public static final String NEED_NEGOTIATION_ACTION = "com.huawei.vsim.action.NEED_NEGOTIATION";
    @HwSystemApi
    public static final String SIM_REG_PLMNSELINFO_ACTION = "com.huawei.vsim.action.SIM_PLMN_SELINFO";
    @HwSystemApi
    public static final String SIM_REJINFO_ACTION = "com.huawei.vsim.action.SIM_REJINFO_ACTION";
    @HwSystemApi
    public static final String SIM_RESIDENT_PLMN_ACTION = "com.huawei.vsim.action.SIM_RESIDENT_PLMN";
    @HwSystemApi
    public static final String SIM_TRAFFIC_ACTION = "com.huawei.vsim.action.SIM_TRAFFIC";
    public static final String SPN_STRINGS_SUB1_UPDATED_ACTION = "android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED";
    public static final String SPN_STRINGS_SUB2_UPDATED_ACTION = "android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED";
    public static final String SPN_STRINGS_UPDATED_ACTION = "android.provider.Telephony.SPN_STRINGS_UPDATED";
    public static final String SPN_STRINGS_UPDATED_VSIM_ACTION = "com.huawei.vsim.action.SPN_STRINGS_UPDATED_VSIM";
    @HwSystemApi
    public static final String TIMER_TASK_EXPIRED_ACTION = "com.huawei.vsim.action.TIMERTASK_EXPIRED_ACTION";
    @HwSystemApi
    public static final String VSIM_DIAL_FAILED_ACTION = "com.huawei.vsim.action.DIAL_FAILED_ACTION";

    public static String getActionAnyDataConnection() {
        return "android.intent.action.ANY_DATA_STATE";
    }

    public static String getExtraShowPlmn() {
        return EXTRA_SHOW_PLMN;
    }

    public static String getExtraPlmn() {
        return EXTRA_PLMN;
    }

    public static String getExtraShowSpn() {
        return EXTRA_SHOW_SPN;
    }

    public static String getExtraSpn() {
        return EXTRA_SPN;
    }

    public static String getActionDefaultVoiceChanged() {
        return "android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED";
    }
}
