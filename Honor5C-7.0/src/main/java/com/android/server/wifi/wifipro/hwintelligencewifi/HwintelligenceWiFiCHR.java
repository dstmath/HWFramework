package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.util.Log;
import com.android.server.wifi.HwWifiCHRConstImpl;
import com.android.server.wifi.wifipro.WifiProStatisticsManager;
import com.android.server.wifipro.WifiProCHRManager;
import java.util.Timer;
import java.util.TimerTask;

public class HwintelligenceWiFiCHR {
    public static final int CLOSE_REASON_CLOSE_BY_USER = 2;
    public static final int CLOSE_REASON_CONNECT_TO_NEW_AP = 1;
    public static final int CLOSE_REASON_ENTER_NEW_CELL = 3;
    public static final int OPEN_REASON_ASSOCIATION_REJECT = 4;
    public static final int OPEN_REASON_DHCP_FAILURE = 2;
    public static final int OPEN_REASON_DNS_FAILURE = 5;
    public static final int OPEN_REASON_PASSWORD_FAILURE = 1;
    public static final int OPEN_REASON_SERVER_FULL = 3;
    public static final int OPEN_REASON_UNKOWN = 0;
    private static HwintelligenceWiFiCHR mHwintelligenceWiFiCHR;
    private Timer mConnectTimer;
    private HwIntelligenceStateMachine mMachine;
    private WifiProCHRManager mWifiCHRStateManager;
    private WifiProStatisticsManager mWifiProStatisticsManager;

    public class MyTimerTask extends TimerTask {
        public void run() {
            String bssid = HwintelligenceWiFiCHR.this.mMachine.getConnectFailedBssid();
            String ssid = HwintelligenceWiFiCHR.this.mMachine.getConnectFailedSsid();
            int reason = HwintelligenceWiFiCHR.this.mMachine.getConnectFailedReason();
            Log.d(MessageUtil.TAG, "uploadAutoOpenConnectFailed  bssid = " + bssid + " ssid = " + ssid + " reason = " + reason);
            HwintelligenceWiFiCHR.this.uploadAutoOpenConnectFailed(bssid, ssid, reason);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.hwintelligencewifi.HwintelligenceWiFiCHR.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.hwintelligencewifi.HwintelligenceWiFiCHR.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.hwintelligencewifi.HwintelligenceWiFiCHR.<clinit>():void");
    }

    private HwintelligenceWiFiCHR(HwIntelligenceStateMachine machine) {
        this.mConnectTimer = null;
        this.mMachine = machine;
        this.mWifiCHRStateManager = WifiProCHRManager.getInstance();
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
    }

    public static HwintelligenceWiFiCHR getInstance(HwIntelligenceStateMachine machine) {
        if (mHwintelligenceWiFiCHR == null) {
            mHwintelligenceWiFiCHR = new HwintelligenceWiFiCHR(machine);
        }
        return mHwintelligenceWiFiCHR;
    }

    public void increaseAutoOpenCount() {
        this.mWifiProStatisticsManager.increaseAutoOpenCount();
    }

    public void increaseAutoCloseCount() {
        this.mWifiProStatisticsManager.increaseAutoCloseCount();
    }

    public void uploadPortalApInWhite(String bssid, String ssid) {
        Log.e(MessageUtil.TAG, "uploadPortalApInWhite bssid = " + bssid + " ssid =  " + ssid);
        this.mWifiCHRStateManager.updateBSSID(bssid);
        this.mWifiCHRStateManager.updateSSID(ssid);
        this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "PORTALAP_IN_WHITE");
    }

    public void uploadWhiteNum(short num) {
        Log.d(MessageUtil.TAG, "uploadWhiteNum num = " + num);
        this.mWifiCHRStateManager.updateAutoOpenWhiteNum(num);
        this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "WHITE_MORETHAN_500");
    }

    public void uploadAutoCloseFailed(int reason) {
        this.mWifiCHRStateManager.updateAutoCloseRootCause((short) reason);
        this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "AUTO_CLOSE_TERMINATION");
    }

    public void uploadAutoOpenConnectFailed(String bssid, String ssid, int reason) {
        this.mWifiCHRStateManager.updateBSSID(bssid);
        this.mWifiCHRStateManager.updateSSID(ssid);
        this.mWifiCHRStateManager.updateAutoOpenRootCause((short) reason);
        this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "CANT_CONNECT_FOR_LONG");
    }

    public void startConnectTimer() {
        stopConnectTimer();
        Log.d(MessageUtil.TAG, "startConnectTimer");
        this.mConnectTimer = new Timer();
        this.mConnectTimer.schedule(new MyTimerTask(), 60000);
    }

    public void stopConnectTimer() {
        Log.d(MessageUtil.TAG, "stopConnectTimer ");
        if (this.mConnectTimer != null) {
            this.mConnectTimer.cancel();
            this.mConnectTimer = null;
        }
    }
}
