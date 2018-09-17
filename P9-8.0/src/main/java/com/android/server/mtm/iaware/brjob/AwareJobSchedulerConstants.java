package com.android.server.mtm.iaware.brjob;

import java.util.HashMap;
import java.util.Map;

public final class AwareJobSchedulerConstants {
    public static final String APPSTATUS_ACTION_FILTER_NAME = "AppStatus";
    public static final String APP_STATUS_RUN = "RUN";
    public static final String APP_STATUS_STOPPED = "STOPPED";
    public static final String BARSTATUS_ACTION_FILTER_NAME = "BarStatus";
    public static final String BAR_STATUS_OFF = "OFF";
    public static final String BAR_STATUS_ON = "ON";
    public static final String BAR_STATUS_UNKNOWN = "UNKNOWN";
    public static final String BLUETOOTHSTATUS_ACTION_FILTER_NAME = "BluetoothStatus";
    public static final String BLUETOOTH_STATUS_CONNECTED = "BLUETOOTHCON";
    public static final String BLUETOOTH_STATUS_DISCONNECTED = "BLUETOOTHDSCON";
    public static final String BLUETOOTH_STATUS_UNKNOWN = "UNKNOWN";
    private static Map<String, Boolean> CACHE_CONDITION_MAP = new HashMap();
    public static final int CACHE_TAG_CACHE = 1;
    public static final int CACHE_TAG_DEFAULT = 2;
    public static final int CACHE_TAG_DROP = 0;
    private static String[] CONDITION_ARRAY = new String[]{LIMITNUM_PERDAY_ACTION_FILTER_NAME, LIMITNUM_PERHOUR_ACTION_FILTER_NAME, MINTIME_ONHOUR_ACTION_FILTER_NAME, MINTIME_ONMINUTE_ACTION_FILTER_NAME, NETWORKSTATUS_ACTION_FILTER_NAME, WIFISTATUS_ACTION_FILTER_NAME, SERVICESSTATUS_ACTION_FILTER_NAME, SIMSTATUS_ACTION_FILTER_NAME, APPSTATUS_ACTION_FILTER_NAME, BARSTATUS_ACTION_FILTER_NAME, WIDGETSTATUS_ACTION_FILTER_NAME, KEYWORD_ACTION_FILTER_NAME, BLUETOOTHSTATUS_ACTION_FILTER_NAME, EXTRA_ACTION_FILTER_NAME};
    public static final String EXTRA_ACTION_FILTER_NAME = "Extra";
    public static final String FORCE_CACHE_ACTION_FILTER_NAME = "BrCache";
    public static final String KEYWORD_ACTION_FILTER_NAME = "KeyWord";
    public static final String LIMITNUM_PERDAY_ACTION_FILTER_NAME = "LimitNum#Day";
    public static final String LIMITNUM_PERHOUR_ACTION_FILTER_NAME = "LimitNum#Hour";
    public static final String MINTIME_ONHOUR_ACTION_FILTER_NAME = "MinTime#Hour";
    public static final String MINTIME_ONMINUTE_ACTION_FILTER_NAME = "MinTime#Minute";
    public static final String NETWORKSTATUS_ACTION_FILTER_NAME = "NetworkStatus";
    public static final String NETWORK_STATUS_ALL = "ALL";
    public static final String NETWORK_STATUS_CONNECTED = "MOBILEDATACON";
    public static final String NETWORK_STATUS_DISCONNECTED = "MOBILEDATADSCON";
    public static final String SERVICESSTATUS_ACTION_FILTER_NAME = "ServicesStatus";
    public static final String SERVICES_STATUS_CONNECTED = "CONNECTED";
    public static final String SERVICES_STATUS_DISCONNECTED = "DSCONNECTED";
    public static final String SERVICES_STATUS_ROAMING = "ROAMING";
    public static final String SERVICES_STATUS_UNKNOWN = "UNKNOWN";
    public static final String SIMSTATUS_ACTION_FILTER_NAME = "SIMStatus";
    public static final String SIM_STATUS_ABSENT = "ABSENT";
    public static final String SIM_STATUS_LOCKED = "LOCKED";
    public static final String SIM_STATUS_READY = "READY";
    public static final String SIM_STATUS_UNKNOWN = "UNKNOWN";
    public static final String WIDGETSTATUS_ACTION_FILTER_NAME = "WidgetStatus";
    public static final String WIFISTATUS_ACTION_FILTER_NAME = "WifiStatus";
    public static final String WIFI_STATUS_CONNECTED = "WIFICON";
    public static final String WIFI_STATUS_DISCONNECTED = "WIFIDSCON";

    static {
        CACHE_CONDITION_MAP.put(NETWORKSTATUS_ACTION_FILTER_NAME, Boolean.valueOf(true));
        CACHE_CONDITION_MAP.put(WIFISTATUS_ACTION_FILTER_NAME, Boolean.valueOf(true));
        CACHE_CONDITION_MAP.put(SERVICESSTATUS_ACTION_FILTER_NAME, Boolean.valueOf(true));
    }

    public static String[] getConditionArray() {
        return (String[]) CONDITION_ARRAY.clone();
    }

    public static Map<String, Boolean> getCacheConditionMap() {
        return new HashMap(CACHE_CONDITION_MAP);
    }
}
