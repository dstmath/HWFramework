package com.android.server.wifi;

import android.net.wifi.WifiLinkLayerStats;
import android.util.Log;
import com.huawei.connectivitylog.LogManager;
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
    private static String WIFI_FWLOG_FILE;
    private static HwWiFiLogUtils hwWiFiLogUtils;
    private boolean mAllowModifyFwLog;
    private int mVersionState;
    private WifiNative mWifiNative;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwWiFiLogUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwWiFiLogUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwWiFiLogUtils.<clinit>():void");
    }

    private HwWiFiLogUtils() {
        this.mWifiNative = null;
        this.mVersionState = 0;
        this.mAllowModifyFwLog = false;
    }

    public static HwWiFiLogUtils getDefault() {
        return hwWiFiLogUtils;
    }

    public static void init(WifiNative wifiNative) {
        getDefault().setWifiNative(wifiNative);
    }

    public void startLinkLayerLog() {
        if (isVersionBeta()) {
            firmwareLog(true);
            getWifiLinkLayerStatsEx();
        }
    }

    public void stopLinkLayerLog() {
        if (isVersionBeta()) {
            getWifiLinkLayerStatsEx();
            sleep(200);
            firmwareLog(false);
        }
    }

    private boolean isVersionBeta() {
        if (this.mVersionState == 0) {
            int i;
            if (LogManager.getInstance().isCommercialUser()) {
                i = VERSION_STATE_COMM;
            } else {
                i = VERSION_STATE_BETA;
            }
            this.mVersionState = i;
        }
        if (this.mVersionState == VERSION_STATE_BETA) {
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
        IOException e;
        Throwable th;
        File file = new File(fileName);
        if (file.exists() && file.canWrite()) {
            FileOutputStream fileOutputStream = null;
            try {
                FileOutputStream writer = new FileOutputStream(fileName, false);
                try {
                    writer.write(value.getBytes("US-ASCII"));
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e2) {
                        }
                    }
                    fileOutputStream = writer;
                } catch (IOException e3) {
                    e = e3;
                    fileOutputStream = writer;
                    try {
                        Log.d(TAG, e.toString());
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e4) {
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e5) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = writer;
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e = e6;
                Log.d(TAG, e.toString());
                if (fileOutputStream != null) {
                    fileOutputStream.close();
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
        if (isAllowModify(enable)) {
            if (enable) {
                startFirmwareLogCap();
            } else {
                stopFirmwareLogCap();
            }
        }
    }

    private boolean isAllowModify(boolean enable) {
        if (DEF_CONSOLE_MS_STOP.equals(HwArpVerifier.readFileByChars(WIFI_FWLOG_FILE)) && enable) {
            this.mAllowModifyFwLog = true;
            return true;
        } else if (enable || !this.mAllowModifyFwLog) {
            return false;
        } else {
            this.mAllowModifyFwLog = false;
            return true;
        }
    }
}
