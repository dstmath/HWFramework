package com.android.server.location;

import android.os.SystemProperties;
import android.util.Log;

public class HwCustGpsLocationProviderImpl extends HwCustGpsLocationProvider {
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    private static final int GPS_POSITION_MODE_MS_BASED = 1;
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    static final String TAG = "HwCustGpsLocationProviderImpl";
    private boolean mAgpsRoaming = SystemProperties.getBoolean("ro.config.dis_agps_roaming", false);
    private boolean mIsRoamingFlag = false;
    private int mPositionMode = GPS_POSITION_MODE_STANDALONE;
    private boolean mSupportCommand = false;
    private boolean mSupportModeCommand = SystemProperties.getBoolean("ro.config.agps_mode_cmd", false);

    public HwCustGpsLocationProviderImpl(Object obj) {
        super(obj);
    }

    public void setRoaming(boolean flag) {
        this.mIsRoamingFlag = flag;
    }

    public int setPostionMode(int oldPositionMode) {
        if (this.mAgpsRoaming && this.mIsRoamingFlag) {
            return GPS_POSITION_MODE_STANDALONE;
        }
        if (this.mSupportCommand) {
            return this.mPositionMode;
        }
        return oldPositionMode;
    }

    public boolean sendPostionModeCommand(boolean oldResult, String command) {
        Log.v(TAG, "sendPostionModeCommand 0");
        if (!this.mSupportModeCommand) {
            return oldResult;
        }
        if ("ms_assisted_mode".equals(command)) {
            this.mPositionMode = GPS_POSITION_MODE_MS_ASSISTED;
            this.mSupportCommand = true;
            return true;
        } else if ("ms_based_mode".equals(command)) {
            this.mPositionMode = GPS_POSITION_MODE_MS_BASED;
            this.mSupportCommand = true;
            return true;
        } else if ("ms_standalone_mode".equals(command)) {
            this.mPositionMode = GPS_POSITION_MODE_STANDALONE;
            this.mSupportCommand = true;
            return true;
        } else {
            Log.e(TAG, "wrong command !");
            return oldResult;
        }
    }
}
