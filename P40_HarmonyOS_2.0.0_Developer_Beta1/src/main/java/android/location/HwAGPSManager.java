package android.location;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class HwAGPSManager {
    public static final int AGPS_ENABLE_VALUE = 1;
    public static final int AGPS_MODE_DEFAULT_VALUE = 1;
    public static final int AGPS_ROAMING_DEFAULT_VALUE = 0;
    public static final int AGPS_ROAMING_ENABLED = 1;
    public static final int AGPS_ROAMING_UNENABLED = 0;
    public static final String AGPS_SERVICE_ADDRESS_DEFAULT_VALUE = "supl.google.com";
    public static final String AGPS_SERVICE_PORT_DEFAULT_VALUE = "7275";
    public static final int AGPS_SETTINGS_DEFAULT_VALUE = 1;
    public static final int AGPS_UNENABLE_VALUE = 0;
    private static final boolean DBG = HWFLOW;
    public static final int GPSNI_REQUEST_DEFAULT_VALUE = 1;
    public static final int GPSNI_REQUEST_ENABLED = 1;
    public static final int GPSNI_REQUEST_UNENABLED = 0;
    public static final int GPSTIME_SYNC_DEFAULT_VALUE = 0;
    public static final int GPSTIME_SYNC_ENABLED = 1;
    public static final int GPSTIME_SYNC_UNENABLED = 0;
    private static final int GPS_COLD_START_AIDINGDATA = 13;
    public static final int GPS_COLD_START_MODE = 2;
    private static final int GPS_DELETE_ALL = 65535;
    private static final int GPS_DELETE_ALMANAC = 2;
    private static final int GPS_DELETE_CELLDB_INFO = 32768;
    private static final int GPS_DELETE_EPHEMERIS = 1;
    private static final int GPS_DELETE_HEALTH = 64;
    private static final int GPS_DELETE_IONO = 16;
    private static final int GPS_DELETE_POSITION = 4;
    private static final int GPS_DELETE_RTI = 1024;
    private static final int GPS_DELETE_SADATA = 512;
    private static final int GPS_DELETE_SVDIR = 128;
    private static final int GPS_DELETE_SVSTEER = 256;
    private static final int GPS_DELETE_TIME = 8;
    private static final int GPS_DELETE_UTC = 32;
    public static final int GPS_HOT_START_MODE = 0;
    public static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    public static final int GPS_POSITION_MODE_MS_BASED = 1;
    public static final int GPS_POSITION_MODE_STANDALONE = 0;
    public static final int GPS_START_MODE_DEFAULT_VALUE = 0;
    private static final int GPS_WARM_START_AIDINGDATA = 1;
    public static final int GPS_WARM_START_MODE = 1;
    protected static final boolean HWFLOW = Log.isLoggable(TAG, 4);
    private static final String KEY_AGPSMODE_SETTINGS = "assisted_gps_mode";
    private static final String KEY_AGPS_ROAMING_USED = "assisted_gps_roaming_enabled";
    private static final String KEY_AGPS_SERVICE_ADDRESS = "assisted_gps_service_IP";
    private static final String KEY_AGPS_SERVICE_PORT = "assisted_gps_service_port";
    private static final String KEY_AGPS_SWITCH_SETTINGS = "assisted_gps_enabled";
    private static final String KEY_GPS_START_MODE = "gps_start_mode";
    private static final String KEY_GPS_TIME_SYNC = "time_synchronization";
    private static final String KEY_NI_REQUEST = "gps_ni_request";
    private static final String KEY_QUICK_GPS = "quick_gps_switch";
    public static final int QUICKGPS_DEFAULT_VALUE = 1;
    public static final int QUICKGPS_ENABLED = 1;
    public static final int QUICKGPS_UNENABLED = 0;
    private static final String TAG = "HwAGPSManager";
    private Context mContext;

    public HwAGPSManager(Context context) {
        this.mContext = context;
    }

    public int getAGPSSwitchSettings() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), KEY_AGPS_SWITCH_SETTINGS, 1);
    }

    public void setAGPSSwitchSettings(int agpsSettings) {
        Settings.Global.putInt(this.mContext.getContentResolver(), KEY_AGPS_SWITCH_SETTINGS, agpsSettings);
    }

    public boolean isAgpsEnable() {
        return getAGPSSwitchSettings() != 0;
    }

    public int getAGPSModeSettings() {
        return getSecureDataBaseInt(KEY_AGPSMODE_SETTINGS, 1);
    }

    private int getSecureDataBaseInt(String key, int defaultValue) {
        return Settings.System.getInt(this.mContext.getContentResolver(), key, defaultValue);
    }

    public void setAGPSModeSettings(int agpsModeSettings) {
        setSecureDataBaseInt(KEY_AGPSMODE_SETTINGS, agpsModeSettings);
    }

    private void setSecureDataBaseInt(String key, int value) {
        Settings.System.putInt(this.mContext.getContentResolver(), key, value);
    }

    public int getAGPSRoamingEnable() {
        return getSecureDataBaseInt(KEY_AGPS_ROAMING_USED, 0);
    }

    public void setAGPSRoamingEnable(int agpsRoamingEnable) {
        setSecureDataBaseInt(KEY_AGPS_ROAMING_USED, agpsRoamingEnable);
    }

    public boolean isAgpsRoamingEnable() {
        return getAGPSRoamingEnable() == 1;
    }

    public String getAGPSServiceAddress() {
        return getSecureDataBaseString(KEY_AGPS_SERVICE_ADDRESS, AGPS_SERVICE_ADDRESS_DEFAULT_VALUE);
    }

    private String getSecureDataBaseString(String key, String defaultValue) {
        String dataValue = Settings.System.getString(this.mContext.getContentResolver(), key);
        return dataValue != null ? dataValue : defaultValue;
    }

    public void setAGPSServiceAddress(String agpsServiceAddress) {
        setSecureDataBaseString(KEY_AGPS_SERVICE_ADDRESS, agpsServiceAddress);
    }

    private void setSecureDataBaseString(String key, String value) {
        Settings.System.putString(this.mContext.getContentResolver(), key, value);
    }

    public String getAGPSServicePort() {
        return getSecureDataBaseString(KEY_AGPS_SERVICE_PORT, AGPS_SERVICE_PORT_DEFAULT_VALUE);
    }

    public void setAGPSServicePort(String agpsServicePort) {
        setSecureDataBaseString(KEY_AGPS_SERVICE_PORT, agpsServicePort);
    }

    public int getQuickGpsSettings() {
        return getDataBaseInt(KEY_QUICK_GPS, 1);
    }

    public void setQuickGpsSettings(int quickGpsSettings) {
        setDataBaseInt(KEY_QUICK_GPS, quickGpsSettings);
    }

    public boolean isQuickGpsEnable() {
        return getDataBaseInt(KEY_QUICK_GPS, 1) != 0;
    }

    public int getGpsTimeSyncSettings() {
        return getSecureDataBaseInt(KEY_GPS_TIME_SYNC, 0);
    }

    public void setGpsTimeSyncSettings(int gpsTimeSyncSettings) {
        setSecureDataBaseInt(KEY_GPS_TIME_SYNC, gpsTimeSyncSettings);
    }

    public boolean isGpsTimeSyncEnable() {
        return getGpsTimeSyncSettings() == 1;
    }

    public int getNiRequestSettings() {
        return getDataBaseInt(KEY_NI_REQUEST, 1);
    }

    public void setNiRequestSettings(int niRequestSettings) {
        setDataBaseInt(KEY_NI_REQUEST, niRequestSettings);
    }

    public boolean isNiRequestEnable() {
        return getDataBaseInt(KEY_NI_REQUEST, 1) != 0;
    }

    public int getGpsStartModeSettings() {
        return getSecureDataBaseInt(KEY_GPS_START_MODE, 0);
    }

    public void setGpsStartModeSettings(int gpsStartSettings) {
        setSecureDataBaseInt(KEY_GPS_START_MODE, gpsStartSettings);
    }

    public int getAidingDataByStartMode() {
        int flag = 0;
        switch (getGpsStartModeSettings()) {
            case 1:
                flag = 1;
                break;
            case 2:
                flag = GPS_COLD_START_AIDINGDATA;
                break;
        }
        if (DBG) {
            Log.d(TAG, " aidingdata = " + flag);
        }
        return flag;
    }

    private void setDataBaseInt(String key, int value) {
        Settings.Global.putInt(this.mContext.getContentResolver(), key, value);
    }

    private int getDataBaseInt(String key, int defaultValue) {
        return Settings.Global.getInt(this.mContext.getContentResolver(), key, defaultValue);
    }

    private void setDataBaseString(String key, String value) {
        Settings.Global.putString(this.mContext.getContentResolver(), key, value);
    }

    private String getDataBaseString(String key, String defaultValue) {
        String dataValue = Settings.Global.getString(this.mContext.getContentResolver(), key);
        return dataValue != null ? dataValue : defaultValue;
    }

    public void bootCompleteInit() {
    }

    public void setAgpsServer(Intent intent) {
    }
}
