package com.android.server.mtm.iaware.brjob;

import java.util.HashMap;
import java.util.Map;

public final class AwareJobSchedulerConstants {
    public static final String APP_STATUS_ACTION_FILTER_NAME = "AppStatus";
    public static final String APP_STATUS_RUN = "RUN";
    public static final String APP_STATUS_STOPPED = "STOPPED";
    public static final String BAR_STATUS_ACTION_FILTER_NAME = "BarStatus";
    public static final String BAR_STATUS_OFF = "OFF";
    public static final String BAR_STATUS_ON = "ON";
    public static final String BAR_STATUS_UNKNOWN = "UNKNOWN";
    public static final String BLUETOOTH_STATUS_ACTION_FILTER_NAME = "BluetoothStatus";
    public static final String BLUETOOTH_STATUS_CONNECTED = "BLUETOOTHCON";
    public static final String BLUETOOTH_STATUS_DISCONNECTED = "BLUETOOTHDSCON";
    public static final String BLUETOOTH_STATUS_UNKNOWN = "UNKNOWN";
    public static final int CACHE_TAG_CACHE = 1;
    public static final int CACHE_TAG_DEFAULT = 2;
    public static final int CACHE_TAG_DROP = 0;
    public static final String EXTRA_ACTION_FILTER_NAME = "Extra";
    public static final String FORCE_CACHE_ACTION_FILTER_NAME = "BrCache";
    public static final String KEYWORD_ACTION_FILTER_NAME = "KeyWord";
    public static final String LIMIT_NUM_PERDAY_ACTION_FILTER_NAME = "LimitNum#Day";
    public static final String LIMIT_NUM_PERHOUR_ACTION_FILTER_NAME = "LimitNum#Hour";
    public static final String MIN_TIME_PERHOUR_ACTION_FILTER_NAME = "MinTime#Hour";
    public static final String MIN_TIME_PERMINUTE_ACTION_FILTER_NAME = "MinTime#Minute";
    public static final String NETWORK_STATUS_ACTION_FILTER_NAME = "NetworkStatus";
    public static final String NETWORK_STATUS_ALL = "ALL";
    public static final String NETWORK_STATUS_CONNECTED = "MOBILEDATACON";
    public static final String NETWORK_STATUS_DISCONNECTED = "MOBILEDATADSCON";
    public static final String SERVICES_STATUS_ACTION_FILTER_NAME = "ServicesStatus";
    public static final String SERVICES_STATUS_CONNECTED = "CONNECTED";
    public static final String SERVICES_STATUS_DISCONNECTED = "DSCONNECTED";
    public static final String SERVICES_STATUS_ROAMING = "ROAMING";
    public static final String SERVICES_STATUS_UNKNOWN = "UNKNOWN";
    public static final String SIM_STATUS_ABSENT = "ABSENT";
    public static final String SIM_STATUS_ACTION_FILTER_NAME = "SIMStatus";
    public static final String SIM_STATUS_LOCKED = "LOCKED";
    public static final String SIM_STATUS_READY = "READY";
    public static final String SIM_STATUS_UNKNOWN = "UNKNOWN";
    public static final String WIDGET_STATUS_ACTION_FILTER_NAME = "WidgetStatus";
    public static final String WIFI_STATUS_ACTION_FILTER_NAME = "WifiStatus";
    public static final String WIFI_STATUS_CONNECTED = "WIFICON";
    public static final String WIFI_STATUS_DISCONNECTED = "WIFIDSCON";
    private static Map<String, Boolean> cacheConditionMap = new HashMap();
    private static String[] conditionArray = {LIMIT_NUM_PERDAY_ACTION_FILTER_NAME, LIMIT_NUM_PERHOUR_ACTION_FILTER_NAME, MIN_TIME_PERHOUR_ACTION_FILTER_NAME, MIN_TIME_PERMINUTE_ACTION_FILTER_NAME, NETWORK_STATUS_ACTION_FILTER_NAME, "WifiStatus", SERVICES_STATUS_ACTION_FILTER_NAME, SIM_STATUS_ACTION_FILTER_NAME, APP_STATUS_ACTION_FILTER_NAME, BAR_STATUS_ACTION_FILTER_NAME, WIDGET_STATUS_ACTION_FILTER_NAME, KEYWORD_ACTION_FILTER_NAME, BLUETOOTH_STATUS_ACTION_FILTER_NAME, "Extra"};

    static {
        cacheConditionMap.put(NETWORK_STATUS_ACTION_FILTER_NAME, true);
        cacheConditionMap.put("WifiStatus", true);
        cacheConditionMap.put(SERVICES_STATUS_ACTION_FILTER_NAME, true);
    }

    public static String[] getConditionArray() {
        return (String[]) conditionArray.clone();
    }

    public static Map<String, Boolean> getCacheConditionMap() {
        return new HashMap(cacheConditionMap);
    }
}
