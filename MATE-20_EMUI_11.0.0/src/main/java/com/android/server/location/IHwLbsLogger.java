package com.android.server.location;

import android.os.Bundle;

public interface IHwLbsLogger {
    public static final int LOCATION_BACKGROUND = 105;
    public static final int LOCATION_ENGINE_ACTION = 102;
    public static final int LOCATION_FREEZEN_ACTION = 104;
    public static final int LOCATION_IAWARE_CONTROL = 108;
    public static final int LOCATION_NETWORK_STATUS = 107;
    public static final int LOCATION_POS_LOST = 103;
    public static final int LOCATION_POS_REPORT = 111;
    public static final int LOCATION_SCREEN_ONOFF = 106;
    public static final int LOCATION_SESSION_ACTION = 101;
    public static final int LOCATION_SV_STATUS = 112;
    public static final int LOCATION_SWITCH_CHANGE = 100;
    public static final int LOCATION_UID_MODIFY = 109;
    public static final int LOCATION_WIFI_CONNECTION = 110;

    void loggerEvent(int i, Bundle bundle);
}
