package com.huawei.android.gps;

import android.content.Context;
import com.huawei.android.util.NoExtAPIException;

public class AGPSManagerCustEx {
    public static final int AGPS_ENABLE_VALUE = 1;
    public static final int AGPS_MODE_DEFAULT_VALUE = 1;
    public static final int AGPS_ROAMING_DEFAULT_VALUE = 1;
    public static final int AGPS_ROAMING_ENABLED = 1;
    public static final int AGPS_ROAMING_UNENABLED = 0;
    public static final String AGPS_SERVICE_ADDRESS_DEFAULT_VALUE = "supl.google.com";
    public static final String AGPS_SERVICE_PORT_DEFAULT_VALUE = "7275";
    public static final int AGPS_SETTINGS_DEFAULT_VALUE = 1;
    public static final int AGPS_UNENABLE_VALUE = 0;
    public static final int GPSNI_REQUEST_DEFAULT_VALUE = 1;
    public static final int GPSNI_REQUEST_ENABLED = 1;
    public static final int GPSNI_REQUEST_UNENABLED = 0;
    public static final int GPSTIME_SYNC_DEFAULT_VALUE = 0;
    public static final int GPSTIME_SYNC_ENABLED = 1;
    public static final int GPSTIME_SYNC_UNENABLED = 0;
    public static final int GPS_COLD_START_MODE = 2;
    public static final int GPS_HOT_START_MODE = 0;
    public static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    public static final int GPS_POSITION_MODE_MS_BASED = 1;
    public static final int GPS_POSITION_MODE_STANDALONE = 0;
    public static final int GPS_START_MODE_DEFAULT_VALUE = 0;
    public static final int GPS_WARM_START_MODE = 1;
    public static final int QUICKGPS_DEFAULT_VALUE = 1;
    public static final int QUICKGPS_ENABLED = 1;
    public static final int QUICKGPS_UNENABLED = 0;

    public AGPSManagerCustEx(Context context) {
    }

    public int getAGPSSwitchSettings() {
        throw new NoExtAPIException("method not supported.");
    }

    public void setAGPSSwitchSettings(int AGPSSettings) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isAgpsEnable() {
        throw new NoExtAPIException("method not supported.");
    }

    public int getAGPSModeSettings() {
        throw new NoExtAPIException("method not supported.");
    }

    public void setAGPSModeSettings(int AGPSModeSettings) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getAGPSRoamingEnable() {
        throw new NoExtAPIException("method not supported.");
    }

    public void setAGPSRoamingEnable(int AGPSRoamingEnable) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isAgpsRoamingEnable() {
        throw new NoExtAPIException("method not supported.");
    }

    public String getAGPSServiceAddress() {
        throw new NoExtAPIException("method not supported.");
    }

    public void setAGPSServiceAddress(String AGPSServiceAddress) {
        throw new NoExtAPIException("method not supported.");
    }

    public String getAGPSServicePort() {
        throw new NoExtAPIException("method not supported.");
    }

    public void setAGPSServicePort(String AGPSServicePort) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getQuickGpsSettings() {
        throw new NoExtAPIException("method not supported.");
    }

    public void setQuickGpsSettings(int QuickGpsSettings) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isQuickGpsEnable() {
        throw new NoExtAPIException("method not supported.");
    }

    public int getGpsTimeSyncSettings() {
        throw new NoExtAPIException("method not supported.");
    }

    public void setGpsTimeSyncSettings(int GpsTimeSyncSettings) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isGpsTimeSyncEnable() {
        throw new NoExtAPIException("method not supported.");
    }

    public int getNiRequestSettings() {
        throw new NoExtAPIException("method not supported.");
    }

    public void setNiRequestSettings(int NiRequestSettings) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isNiRequestEnable() {
        throw new NoExtAPIException("method not supported.");
    }

    public int getGpsStartModeSettings() {
        throw new NoExtAPIException("method not supported.");
    }

    public void setGpsStartModeSettings(int GpsStartSettings) {
        throw new NoExtAPIException("method not supported.");
    }
}
