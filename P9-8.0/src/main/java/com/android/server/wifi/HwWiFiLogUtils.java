package com.android.server.wifi;

import android.net.wifi.WifiLinkLayerStats;
import android.util.Log;
import com.huawei.ncdft.HwWifiDFTConnManager;
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
            int i;
            if (HwWifiDFTConnManager.getInstance().isCommercialUser()) {
                i = 2;
            } else {
                i = 1;
            }
            this.mVersionState = i;
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

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0055 A:{SYNTHETIC, Splitter: B:21:0x0055} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x005e A:{SYNTHETIC, Splitter: B:26:0x005e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void writeToFile(String fileName, String value) {
        IOException e;
        Throwable th;
        File file = new File(fileName);
        if (file.exists() && (file.canWrite() ^ 1) == 0) {
            FileOutputStream writer = null;
            try {
                FileOutputStream writer2 = new FileOutputStream(fileName, false);
                try {
                    writer2.write(value.getBytes("US-ASCII"));
                    if (writer2 != null) {
                        try {
                            writer2.close();
                        } catch (IOException e2) {
                        }
                    }
                    writer = writer2;
                } catch (IOException e3) {
                    e = e3;
                    writer = writer2;
                    try {
                        Log.d(TAG, e.toString());
                        if (writer != null) {
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    writer = writer2;
                    if (writer != null) {
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e = e5;
                Log.d(TAG, e.toString());
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e6) {
                    }
                }
                return;
            }
            return;
        }
        Log.d(TAG, fileName + " no premission");
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
