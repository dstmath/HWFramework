package com.huawei.internal.telephony.smartnet;

import com.huawei.android.telephony.TelephonyManagerEx;

public class HwSmartNetConstants {
    public static final int BLACK_POINT_TYPE_NORMAL = 0;
    public static final int BLACK_POINT_TYPE_OFF_THE_RAT = 4;
    public static final int BLACK_POINT_TYPE_OUT_OF_SERVICE = 1;
    public static final int BLACK_POINT_TYPE_WEAK_SIGNAL_STRENGTH = 2;
    public static final int CAN_START_ESTIMATE_THRESHOLD = 1;
    public static final double ESTIMATE_BLACK_POINT_PROPBILITY_THRESHOLD = 0.8d;
    public static final int EVENT_CELL_INFO_CHANGED = 203;
    public static final int EVENT_CELL_SENSOR_TIMER_REQUEST = 204;
    public static final int EVENT_DEFAULT_STATE_BASE = 200;
    public static final int EVENT_INIT_STATE_BASE = 100;
    public static final int EVENT_LOCATION_CHANGE_STATE_BASE = 450;
    public static final int EVENT_REQUEST_CELL_INFO_DONE = 601;
    public static final int EVENT_REQUEST_CELL_SENSOR_DONE = 602;
    public static final int EVENT_SAMPLING_STATE_BASE = 300;
    public static final int EVENT_SERVICE_STATE_CHANGED = 201;
    public static final int EVENT_SIGNAL_STRENGTH_CHANGED = 202;
    public static final int EVENT_STATE_END_SAMPLING = 302;
    public static final int EVENT_STATE_END_STUDY = 402;
    public static final int EVENT_STATE_ENTER_HOME_OR_COMPANY = 452;
    public static final int EVENT_STATE_ENTER_HOME_OR_COMPANY_FOR_TEST = 453;
    public static final int EVENT_STATE_EXIT_HOME_OR_COMPANY = 451;
    public static final int EVENT_STATE_LISTENER_BASE = 600;
    public static final int EVENT_STATE_MACHINE_BASE = 0;
    public static final int EVENT_STATE_START_SAMPLING = 301;
    public static final int EVENT_STATE_START_STUDY = 401;
    public static final int EVENT_STUDY_STATE_BASE = 400;
    public static final boolean IS_DBG_ON = false;
    public static final int MAX_MATCH_INFO_POINTS_NUM = 5;
    public static final int ROUTE_ID_COMPANY_TO_HOME = 2;
    public static final int ROUTE_ID_HOME_TO_COMPANY = 1;
    public static final int ROUTE_ID_INVALID = -1;
    public static final int ROUTE_NUM = 2;
    public static final int SIM_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    public static final double WEAK_SIGNAL_STRENGTH_THRESHOLD = 2.0d;

    private HwSmartNetConstants() {
    }
}
