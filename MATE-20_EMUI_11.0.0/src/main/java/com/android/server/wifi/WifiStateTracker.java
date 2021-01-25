package com.android.server.wifi;

import android.os.RemoteException;
import android.util.Log;
import com.android.internal.app.IBatteryStats;
import java.util.concurrent.RejectedExecutionException;

public class WifiStateTracker {
    public static final int CONNECTED = 3;
    public static final int DISCONNECTED = 2;
    public static final int INVALID = 0;
    public static final int SCAN_MODE = 1;
    public static final int SOFT_AP = 4;
    private static final String TAG = "WifiStateTracker";
    private IBatteryStats mBatteryStats;
    private int mWifiState = 0;

    public WifiStateTracker(IBatteryStats stats) {
        this.mBatteryStats = stats;
    }

    private void informWifiStateBatteryStats(int state) {
        try {
            this.mBatteryStats.noteWifiState(state, (String) null);
        } catch (RemoteException e) {
            Log.e(TAG, "Battery stats unreachable " + e.getMessage());
        } catch (RejectedExecutionException e2) {
            Log.e(TAG, "Battery stats executor is being shutdown " + e2.getMessage());
        }
    }

    public void updateState(int state) {
        int reportState;
        if (state == this.mWifiState) {
            return;
        }
        if (state != 0) {
            if (state == 1) {
                reportState = 1;
            } else if (state == 2) {
                reportState = 3;
            } else if (state == 3) {
                reportState = 4;
            } else if (state == 4) {
                reportState = 7;
            } else {
                return;
            }
            this.mWifiState = state;
            informWifiStateBatteryStats(reportState);
            return;
        }
        this.mWifiState = 0;
    }
}
