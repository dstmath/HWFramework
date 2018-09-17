package com.android.internal.telephony;

public class AbstractTelephonyIntents {
    public static final String ACTION_DUAL_SIM_IMSI_CHANGE = "android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE";
    public static final String ACTION_HW_CRR_CONN_IND = "com.huawei.action.ACTION_HW_CRR_CONN_IND";
    public static final String ACTION_HW_CSP_PLMN_CHANGE = "android.intent.action.ACTION_HW_CSP_PLMN_CHANGE";
    public static final String ACTION_HW_SWITCH_SLOT_DONE = "com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE";
    public static final String ACTION_MDM_DISABLE_SUB_RESULT = "android.intent.ACTION_MDM_DISABLE_SUB_RESULT";
    public static final String ACTION_MDM_POLICY_CHANGED = "com.huawei.devicepolicy.action.POLICY_CHANGED";
    public static final String ACTION_NETWORK_SCAN_COMPLETE = "com.huawei.vsim.action.NETWORK_SCAN_COMPLETE";
    public static final String ACTION_SUBSCRIPTION_SET_UICC_RESULT = "com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT";
    public static final String ACTION_VSIM_CARD_RELOAD = "com.huawei.vsim.action.VSIM_CARD_RELOAD";
    public static final String ACTION_VSIM_SERVICE_READY = "com.huawei.vsim.action.VSIM_SERVICE_READY";
    public static final String ACTION_VSIM_STATE_CHANGED = "com.huawei.vsim.action.VSIM_STATE_CHANGED";
    public static final String EXTRA_COLUMN_NAME = "columnName";
    public static final String EXTRA_INT_CONTENT = "intContent";
    public static final String EXTRA_NEW_SUB_STATE = "newSubState";
    public static final String EXTRA_RESULT = "operationResult";
    public static final String HUAWEI_SIM_REG_PLMNSELINFO_ACTION = "com.huawei.action.SIM_PLMN_SELINFO";
    public static final String NEED_NEGOTIATION_ACTION = "com.huawei.vsim.action.NEED_NEGOTIATION";
    public static final String SIM_REG_PLMNSELINFO_ACTION = "com.huawei.vsim.action.SIM_PLMN_SELINFO";
    public static final String SIM_REJINFO_ACTION = "com.huawei.vsim.action.SIM_REJINFO_ACTION";
    public static final String SIM_RESIDENT_PLMN_ACTION = "com.huawei.vsim.action.SIM_RESIDENT_PLMN";
    public static final String SIM_TRAFFIC_ACTION = "com.huawei.vsim.action.SIM_TRAFFIC";
    public static final String SPN_STRINGS_UPDATED_VSIM_ACTION = "com.huawei.vsim.action.SPN_STRINGS_UPDATED_VSIM";
    public static final String TIMER_TASK_EXPIRED_ACTION = "com.huawei.vsim.action.TIMERTASK_EXPIRED_ACTION";
    public static final String VSIM_DIAL_FAILED_ACTION = "com.huawei.vsim.action.DIAL_FAILED_ACTION";
}
