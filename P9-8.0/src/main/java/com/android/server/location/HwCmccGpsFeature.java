package com.android.server.location;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.SettingsEx.Systemex;
import android.util.Log;
import com.huawei.utils.reflect.EasyInvokeFactory;

public class HwCmccGpsFeature implements IHwCmccGpsFeature {
    private static final int AGPS_ROAMING_ENABLED = 1;
    private static final int AGPS_ROAMING_UNENABLED = 0;
    private static final String AGPS_SERVICE_IP_DEFAULT = "supl.google.com";
    private static final int AGPS_SERVICE_PORT_DEFAULT = 7275;
    private static final int COLD_MODE = 2;
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    private static final int GPS_POSITION_MODE_MS_BASED = 1;
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    private static final int HOT_MODE = 0;
    private static final String KEY_AGPS_ROAMING_ENABLED = "assisted_gps_roaming_enabled";
    private static final String KEY_AGPS_SERVICE_ADDRESS = "assisted_gps_service_IP";
    private static final String KEY_AGPS_SERVICE_PORT = "assisted_gps_service_port";
    private static final String KEY_AGPS_SETTINGS = "assisted_gps_mode";
    private static final String TAG = "HwCmccGpsFeature";
    private static final String TIME_SYNCHRONIZATION = "time_synchronization";
    private static final int TIME_SYNCHRONIZTION_OFF = 0;
    private static final int TIME_SYNCHRONIZTION_ON = 1;
    private static final int WARM_MODE = 1;
    private boolean mAgpsSwitchOn = SystemProperties.getBoolean("ro.config.agps_server_setting", false);
    private Context mContext;
    private GnssLocationProvider mGnssLocationProvider;
    private boolean mIsRoaming = false;
    private int mNeedSyncTime;
    private boolean mSyncedTimeFlag = true;
    private GpsLocationProviderUtils utils;

    public HwCmccGpsFeature(Context context, GnssLocationProvider gnssLocationProvider) {
        this.mContext = context;
        this.mGnssLocationProvider = gnssLocationProvider;
        this.utils = (GpsLocationProviderUtils) EasyInvokeFactory.getInvokeUtils(GpsLocationProviderUtils.class);
    }

    public void setRoaming(boolean flag) {
        this.mIsRoaming = flag;
    }

    public boolean checkSuplInit() {
        if (!this.mAgpsSwitchOn) {
            return false;
        }
        if (Global.getInt(this.mContext.getContentResolver(), "assisted_gps_enabled", 1) == 0) {
            return true;
        }
        setAgpsServer();
        return false;
    }

    private void setAgpsServer() {
        String strSuplServerHost = Systemex.getString(this.mContext.getContentResolver(), KEY_AGPS_SERVICE_ADDRESS);
        int iSuplServerPort = Systemex.getInt(this.mContext.getContentResolver(), KEY_AGPS_SERVICE_PORT, AGPS_SERVICE_PORT_DEFAULT);
        if (strSuplServerHost == null) {
            strSuplServerHost = AGPS_SERVICE_IP_DEFAULT;
        }
        Log.d(TAG, "setAgpsServer SuplServerHost:" + strSuplServerHost + " SuplServerPort:" + iSuplServerPort);
        this.utils.setSuplHostPort(this.mGnssLocationProvider, strSuplServerHost, String.valueOf(iSuplServerPort));
    }

    private int setPostionMode() {
        int positionMode = Systemex.getInt(this.mContext.getContentResolver(), KEY_AGPS_SETTINGS, 1);
        if (!(positionMode == 0 || 1 == positionMode || 2 == positionMode)) {
            positionMode = 1;
        }
        if (1 != Systemex.getInt(this.mContext.getContentResolver(), KEY_AGPS_ROAMING_ENABLED, 0) && this.mIsRoaming) {
            positionMode = 0;
        }
        Log.d(TAG, "setPostionMode positionMode:" + positionMode);
        return positionMode;
    }

    public int setPostionModeAndAgpsServer(int oldPositionMode, boolean agpsEnabled) {
        if (!this.mAgpsSwitchOn || (agpsEnabled ^ 1) != 0) {
            return oldPositionMode;
        }
        int positionMode = setPostionMode();
        if (positionMode != 0) {
            setAgpsServer();
        }
        this.mSyncedTimeFlag = true;
        this.mNeedSyncTime = Systemex.getInt(this.mContext.getContentResolver(), TIME_SYNCHRONIZATION, 0);
        return positionMode;
    }

    public void syncTime(long timestamp) {
        if (1 == this.mNeedSyncTime && this.mSyncedTimeFlag && this.mAgpsSwitchOn) {
            Log.i(TAG, "syncing gps time");
            this.mSyncedTimeFlag = false;
            SystemClock.setCurrentTimeMillis(timestamp);
        }
    }

    public void setDelAidData() {
        int mode = Systemex.getInt(this.mContext.getContentResolver(), "gps_start_mode", 0);
        Bundle extras = new Bundle();
        switch (mode) {
            case 0:
                Log.d(TAG, "HOT_MODE");
                return;
            case 1:
                Log.d(TAG, "WARM_MODE");
                extras.putBoolean("ephemeris", true);
                this.mGnssLocationProvider.sendExtraCommand("delete_aiding_data", extras);
                return;
            case 2:
                Log.d(TAG, "COLD_MODE");
                extras.putBoolean("ephemeris", true);
                extras.putBoolean("position", true);
                extras.putBoolean("time", true);
                this.mGnssLocationProvider.sendExtraCommand("delete_aiding_data", extras);
                return;
            default:
                return;
        }
    }
}
