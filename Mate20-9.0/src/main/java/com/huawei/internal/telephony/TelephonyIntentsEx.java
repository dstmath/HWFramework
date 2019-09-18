package com.huawei.internal.telephony;

public class TelephonyIntentsEx {
    public static final String ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED";
    public static final String ACTION_DEFAULT_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_SUBSCRIPTION_CHANGED";
    public static final String ACTION_EMERGENCY_CALLBACK_MODE_CHANGED = "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED";
    public static final String ACTION_MANAGED_ROAMING_IND = "codeaurora.intent.action.ACTION_MANAGED_ROAMING_IND";
    public static final String ACTION_MDM_POLICY_CHANGED = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    public static final String ACTION_NATIONAL_MODE_CHANGE = "android.intent.action.ACTION_NATIONAL_MODE_CHANGE";
    public static final String ACTION_SERVICE_STATE_CHANGED = "android.intent.action.SERVICE_STATE";
    public static final String ACTION_SET_SUBSCRIPTION_DONE = "com.huawei.intent.action.ACTION_SET_SUBSCRIPTION_DONE";
    public static final String ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS = "com.android.internal.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS";
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public static final String ACTION_SUBINFO_CONTENT_CHANGE = "android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE";
    public static final String ACTION_SUBINFO_RECORD_UPDATED = "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED";
    public static final String ACTION_SUBSCRIPTION_SET_UICC_RESULT = "com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT";
    public static final String ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW = "android.intent.action.ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW";
    public static final String EXTRA_DATA_SPN = "spnData";
    public static final String EXTRA_NEW_SUB_STATE = "newSubState";
    public static final String EXTRA_PLMN = "plmn";
    public static final String EXTRA_RESULT = "operationResult";
    public static final String EXTRA_SHOW_PLMN = "showPlmn";
    public static final String EXTRA_SHOW_SPN = "showSpn";
    public static final String EXTRA_SPN = "spn";
    public static final String SPN_STRINGS_SUB1_UPDATED_ACTION = "android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED";
    public static final String SPN_STRINGS_SUB2_UPDATED_ACTION = "android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED";
    public static final String SPN_STRINGS_UPDATED_ACTION = "android.provider.Telephony.SPN_STRINGS_UPDATED";
    public static final String SPN_STRINGS_UPDATED_VSIM_ACTION = "com.huawei.vsim.action.SPN_STRINGS_UPDATED_VSIM";

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
