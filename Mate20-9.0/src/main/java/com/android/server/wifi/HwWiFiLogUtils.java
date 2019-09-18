package com.android.server.wifi;

import android.util.Log;
import com.huawei.ncdft.HwNcDftConnManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HwWiFiLogUtils {
    private static final String DEF_CONSOLE_MS_START = "200";
    private static final String DEF_CONSOLE_MS_STOP = "0";
    private static final String TAG = "HwWiFiLogUtils";
    private static final int VERSION_STATE_BETA = 1;
    private static final int VERSION_STATE_COMM = 2;
    private static final int VERSION_STATE_INIT = 0;
    private static String WIFI_FWLOG_FILE = "/sys/bcm-dhd/dhd_watchdog_time";
    private static HwWiFiLogUtils hwWiFiLogUtils = new HwWiFiLogUtils();
    private boolean mAllowModifyFwLog = false;
    private int mVersionState = 0;
    private WifiNative mWifiNative = null;

    private HwWiFiLogUtils() {
    }

    public static HwWiFiLogUtils getDefault() {
        return hwWiFiLogUtils;
    }

    public static void init(WifiNative wifiNative) {
        getDefault().setWifiNative(wifiNative);
    }

    public void startLinkLayerLog() {
        if (isVersionBeta()) {
            getWifiLinkLayerStatsEx();
        }
    }

    public void stopLinkLayerLog() {
        if (isVersionBeta()) {
            getWifiLinkLayerStatsEx();
            sleep(200);
        }
    }

    private boolean isVersionBeta() {
        if (this.mVersionState == 0) {
            this.mVersionState = HwNcDftConnManager.isCommercialUser() ? 2 : 1;
        }
        if (this.mVersionState == 1) {
            return true;
        }
        return false;
    }

    private void setWifiNative(WifiNative wifiNative) {
        this.mWifiNative = wifiNative;
    }

    private void getWifiLinkLayerStatsEx() {
        if (this.mWifiNative != null) {
            WifiLinkLayerStats stats = this.mWifiNative.getWifiLinkLayerStats("wlan0");
            if (stats != null) {
                Log.d(TAG, stats.toString());
            }
        }
    }

    private static void startFirmwareLogCap() {
        writeToFile(WIFI_FWLOG_FILE, DEF_CONSOLE_MS_START);
    }

    private static void stopFirmwareLogCap() {
        writeToFile(WIFI_FWLOG_FILE, DEF_CONSOLE_MS_STOP);
    }

    private static void writeToFile(String fileName, String value) {
        File file = new File(fileName);
        if (!file.exists() || !file.canWrite()) {
            Log.d(TAG, fileName + " no premission");
            return;
        }
        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(fileName, false);
            writer.write(value.getBytes("US-ASCII"));
            try {
                writer.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            Log.d(TAG, e2.toString());
            if (writer != null) {
                writer.close();
            }
        } catch (Throwable th) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public void firmwareLog(boolean enable) {
        if (enable) {
            startFirmwareLogCap();
        } else {
            stopFirmwareLogCap();
        }
    }
}
