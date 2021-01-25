package com.android.server.intellicom.common;

public class SmartDualCardConsts {
    public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String ACTION_HW_DUAL_PS_STATE = "com.huawei.action.ACTION_HW_DUAL_PS_STATE";
    public static final String ACTION_USER_CHOOSE_SWITCH_BACK = "com.huawei.action.ACTION_HW_UNDO_DUAL_LINK";
    public static final String DUAL_PS_ALLOWED = "dualPsAllowed";
    public static final int EVENT_SETTING_AIRPLANE_MODE = 0;
    public static final int EVENT_SETTING_INTELLIGENCE_CARD = 2;
    public static final int EVENT_SETTING_MAX = 7;
    public static final int EVENT_SETTING_MOBILE_DATA = 1;
    public static final int EVENT_SETTING_SECURE_NETASSISTANT_MONTH_LIMIT = 6;
    public static final int EVENT_SETTING_SECURE_SWITCH_DUAL_CARD_SLOTS = 4;
    public static final int EVENT_SETTING_SECURE_VPN = 3;
    public static final int EVENT_SETTING_SMART_DUAL_CARD_FREE_APP = 5;
    public static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    public static final int INVALID = -1;
    public static final int INVALID_EVENT = -1;
    public static final int INVALID_SPECIAL = -100;
    public static final int MAX_PHONE_NUM = 3;
    public static final int MIN_PHONE_NUM = 1;
    public static final int MIN_SUB_ID = 0;
    public static final int SLOT_0 = 0;
    public static final int SLOT_1 = 1;
    public static final int SLOT_2 = 2;
    public static final int SLOT_INVALID = -1;
    public static final int SLOT_NUM = 3;
    public static final String SMART_DUAL_CARD_ENABLE_PROP = "persist.sys.smart_switch_enable";
    public static final String SYSTEM_STATE_ACTION_USER_SWITCHED = "android.intent.action.USER_SWITCHED";
    public static final String SYSTEM_STATE_NAME_ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    public static final String SYSTEM_STATE_NAME_ANY_DATA_CONNECTION_STATE_CHANGED = "android.intent.action.ANY_DATA_STATE";
    public static final String SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED = "android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED";
    public static final String SYSTEM_STATE_NAME_DSDS_MODE_STATE_CHANGED = "com.huawei.action.ACTION_HW_DSDS_MODE_STATE";
    public static final String SYSTEM_STATE_NAME_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    public static final String SYSTEM_STATE_NAME_SCREEN_ON = "android.intent.action.SCREEN_ON";
    public static final String SYSTEM_STATE_NAME_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public static final String SYSTEM_STATE_NAME_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
    public static final String SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED = "android.net.wifi.STATE_CHANGE";
    public static final int VSIM_SUB_ID = 999999;

    private SmartDualCardConsts() {
    }
}
