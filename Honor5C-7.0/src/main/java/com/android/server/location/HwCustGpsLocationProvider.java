package com.android.server.location;

import android.util.Log;

public class HwCustGpsLocationProvider {
    static final String TAG = "HwCustGpsLocationProvider";

    public HwCustGpsLocationProvider(Object obj) {
    }

    public int setPostionModebyCommand(int oldPositionMode) {
        Log.v(TAG, "setPostionMode 0");
        return oldPositionMode;
    }

    public void setRoaming(boolean flag) {
    }

    public int setPostionMode(int oldPositionMode) {
        Log.v(TAG, "setPostionMode 0");
        return oldPositionMode;
    }

    public boolean sendPostionModeCommand(boolean oldresult, String command) {
        Log.v(TAG, "sendPostionModeCommand 0");
        return oldresult;
    }

    public boolean isForceSetGpsInterval() {
        Log.v(TAG, "forceSetGpsInterval 0");
        return false;
    }
}
