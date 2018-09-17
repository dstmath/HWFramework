package com.android.server.emcom;

import android.os.SystemProperties;

public class ParaManagerConstants {
    public static final int BASIC_COMM_UPGRADE_SITUATION_PHONEBUSY = 1;
    public static final int BASIC_COMM_UPGRADE_SITUATION_RADIO = 2;
    public static final int BASIC_COMM_UPGRADE_SITUATION_SCREENON = 4;
    public static final int CALL_STATE_UNKNOW = -1;
    public static final String EMCOM_PARA_READY_ACTION = "huawei.intent.action.ACTION_EMCOM_PARA_READY";
    public static final String EXTRA_CFG_REL_DIR = "cfgDir";
    public static final String EXTRA_COTAEMCOM_PARA_BITMAP = "EXTRA_COTAEMCOM_PARA_BITMAP";
    public static final String EXTRA_EMCOM_PARA_READY_REC = "EXTRA_EMCOM_PARA_READY_REC";
    public static final String EXTRA_UPDATE_MODE = "updateMode";
    public static final int MESSAGE_BASE_MONITOR_RESPONSE = 4000;
    public static final int MESSAGE_BASE_MONITOR_SITUATION = 0;
    public static final int PARATYPE_BASIC_COMM = 1;
    public static final int PARATYPE_CARRIER_CONFIG = 512;
    public static final int PARATYPE_CELLULAR_CLOUD = 2;
    public static final boolean PARA_DEBUG = SystemProperties.getBoolean("ro.config.hwParaManagerDebug", false);
    public static final int PARA_PATHTYPE_COTA = 1;
    public static final int PARA_UPGRADE_FILE_NOTEXIST = 0;
    public static final int PARA_UPGRADE_FILE_READY = 1;
    public static final int PARA_UPGRADE_NOTIFY_TO_MODULE = 3;
    public static final int PARA_UPGRADE_RESPONSE_FILE_ERROR = 6;
    public static final int PARA_UPGRADE_RESPONSE_UPGRADE_ALREADY = 4;
    public static final int PARA_UPGRADE_RESPONSE_UPGRADE_FAILURE = 9;
    public static final int PARA_UPGRADE_RESPONSE_UPGRADE_PENDING = 7;
    public static final int PARA_UPGRADE_RESPONSE_UPGRADE_SUCCESS = 8;
    public static final int PARA_UPGRADE_RESPONSE_VERSION_MISMATCH = 5;
    public static final int PARA_UPGRADE_SITUATION_UNSUITABLE = 2;
    public static final String RECEIVE_EMCOM_PARA_UPGRADE_PERMISSION = "huawei.permission.RECEIVE_EMCOM_PARA_UPGRADE";
    public static final int RESPONSE_BASE_BASIC_COMM = 0;
    public static final int RESPONSE_BASE_EMCOM_CTR = 800;
    public static final int RESPONSE_BASE_NONCELL_BT = 400;
    public static final int SCREEN_OFF = 2;
    public static final int SCREEN_ON = 1;
    public static final int SCREEN_STATE_UNKNOW = -1;
    public static final int SIM_STATE_READY = 5;
    public static final int TYPEMASK_PARATYPE_BASIC_COMM = 0;
    public static final int TYPEMASK_PARATYPE_CARRIER_CONFIG = 9;
    public static final int TYPEMASK_PARATYPE_CELLULAR_CLOUD = 1;
    public static final int TYPEMASK_PARATYPE_EMCOM_CTR = 8;
    public static final int TYPEMASK_PARATYPE_MAX = 10;
    public static final int TYPEMASK_PARATYPE_NONCELL_BT = 4;
    public static final int TYPEMASK_UPGRADE_SITUATION_PHONEBUSY = 0;
    public static final int TYPEMASK_UPGRADE_SITUATION_RADIO = 1;
    public static final int TYPEMASK_UPGRADE_SITUATION_SCREENON = 2;
}
