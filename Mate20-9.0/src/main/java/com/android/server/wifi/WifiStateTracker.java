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
            this.mBatteryStats.noteWifiState(state, null);
        } catch (RemoteException e) {
            Log.e(TAG, "Battery stats unreachable " + e.getMessage());
        } catch (RejectedExecutionException e2) {
            Log.e(TAG, "Battery stats executor is being shutdown " + e2.getMessage());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0011, code lost:
        r2.mWifiState = r3;
        informWifiStateBatteryStats(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001a, code lost:
        return;
     */
    public void updateState(int state) {
        int reportState;
        if (state != this.mWifiState) {
            switch (state) {
                case 0:
                    this.mWifiState = 0;
                    break;
                case 1:
                    reportState = 1;
                    break;
                case 2:
                    reportState = 3;
                    break;
                case 3:
                    reportState = 4;
                    break;
                case 4:
                    reportState = 7;
                    break;
            }
        }
    }
}
