package com.android.server.wifi.wifinearfind;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wm.HwWindowManagerServiceEx;
import com.android.server.wm.WindowManagerService;

public class HwWifiNearFindArbitration {
    private static final int AUDIO_SHIFT = 3;
    private static final int AVERAGE_NUMBER = 2;
    private static final int BIND_AIRLINK_TIMEOUT = 5000;
    private static final String BUSINESS_ID = "04";
    private static final double DEFAULT_BASE_NUMBER = 10.0d;
    private static final int DEFAULT_COUNT = 1;
    private static final double DEFAULT_DENOMINATOR = 20.0d;
    private static final double DEFAULT_DISTANCE = -1.0d;
    private static final int DEFAULT_IE_LENGTH = 2;
    private static final int DEFAULT_INFO_LENGTH = 2;
    private static final double DEFAULT_LOS_1 = 100.2d;
    private static final int DEFAULT_MAC_LENGTH = 6;
    private static final int DEFAULT_SCAN_CHANNEL = 2437;
    private static final int DEFAULT_SCAN_INTERVAL = 1500;
    private static final int DEFAULT_SSID_LENGTH = 4;
    private static final int DEFAULT_START_LENGTH = 1;
    private static final String EVENT_WIFI_NEAR_FIND_DEVICE = "wifiNearFindDevice";
    private static final int HICAR_SHIFT = 4;
    private static final String HILINK_DEVICE_PREFIX = "Hi- ";
    private static final int HOME_APP_SHIFT = 0;
    private static final int HWSHARE_SHIFT = 7;
    private static final int INITIAL_CONDITION = 35;
    private static final String KEY_BSSID = "ApMac";
    private static final String KEY_BUSINESS_ID = "BusinessId";
    private static final String KEY_DEVICE_INFO = "DeviceInfo";
    private static final String KEY_SOURCE = "Source";
    private static final String KEY_SSID = "SSID";
    private static final int KM_TO_CM_UNIT = 100000;
    private static final Object LOCK_OBJECT = new Object();
    private static final double MAX_DISTANCE_THRESHOLD = 50.0d;
    private static final double MED_DISTANCE_THRESHOLD = 35.0d;
    private static final double MIN_DISTANCE_THRESHOLD = 30.0d;
    private static final int P2P_CONNECT_SHIFT = 2;
    private static final long REPORT_STATISTICAL_CHR_INTERVAL = 43200000;
    private static final int RSSI_THRESHOLD = -60;
    private static final int SCAN_DURATION_LEVEL_0 = 5000;
    private static final int SCAN_DURATION_LEVEL_1 = 10000;
    private static final int SCAN_DURATION_LEVEL_2 = 15000;
    private static final int SCAN_DURATION_LEVEL_3 = 20000;
    private static final int SCAN_DURATION_LEVEL_4 = 25000;
    private static final int SCAN_DURATION_LEVEL_5 = 30000;
    private static final int SCREEN_STATE_SHIFT = 1;
    private static final int SCREEN_UNLOCK_SHIFT = 5;
    private static final int SHIFT_BIT_UTIL = 1;
    private static final String SOURCE_TYPE = "WIFI_RSSI";
    private static final String TAG = HwWifiNearFindArbitration.class.getSimpleName();
    private static final int TX_POWER_TAG = 35;
    private static final int WIFI_SCAN_CHECK_INTERVAL = 30000;
    private static final int WIFI_SWITCH_SHIFT = 6;
    private static volatile HwWifiNearFindArbitration sInstance;
    private volatile boolean isBindToAirLink = false;
    private volatile boolean isFirstLongDistance = false;
    private volatile int mArbitrationCond;
    private WifiScanner.ChannelSpec[] mChannelSpecList;
    private Context mContext;
    private double mFirstLongDistance = 0.0d;
    private Handler mHandler;
    private HwAirLinkUtils mHwAirLinkUtils;
    private HwWindowManagerServiceEx mHwWindowManagerServiceEx;
    private WifiScanner.ScanListener mScanListener;
    private WifiScanner.ScanSettings mScanSettings;
    private long mStartReportChrTime = 0;
    private long mStartScanTime = 0;
    private WifiScanner mWifiScanner;

    private HwWifiNearFindArbitration(Context context) {
        this.mContext = context;
        this.mHwAirLinkUtils = HwAirLinkUtils.getInstance();
        initCondition();
        initWifiScanParams();
        initWifiNearFindHandler();
        initWindowManagerService();
        this.mStartReportChrTime = SystemClock.elapsedRealtime();
        Log.d(TAG, "init HwWifiNearFindArbitration success");
    }

    public static HwWifiNearFindArbitration getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK_OBJECT) {
                if (sInstance == null) {
                    sInstance = new HwWifiNearFindArbitration(context);
                }
            }
        }
        return sInstance;
    }

    private void initCondition() {
        this.mArbitrationCond = 35;
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "initCondition, mContext is null");
            return;
        }
        if (((WifiManager) context.getSystemService("wifi")).isWifiEnabled()) {
            this.mArbitrationCond &= -65;
        } else {
            this.mArbitrationCond |= 64;
        }
        if (HwWifiNearFindUtils.isInHomeLauncher(this.mContext)) {
            this.mArbitrationCond &= -2;
        } else {
            this.mArbitrationCond |= 1;
        }
    }

    private void initWifiScanParams() {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "initWifiScanParams, context is null");
            return;
        }
        this.mWifiScanner = (WifiScanner) context.getSystemService("wifiscanner");
        this.mChannelSpecList = new WifiScanner.ChannelSpec[]{new WifiScanner.ChannelSpec((int) DEFAULT_SCAN_CHANNEL)};
        this.mScanSettings = new WifiScanner.ScanSettings();
        this.mScanSettings.channels = this.mChannelSpecList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initWifiNearFindMonitor() {
        Handler handler;
        Context context = this.mContext;
        if (context == null || (handler = this.mHandler) == null) {
            Log.e(TAG, "initWifiNearFindMonitor, mContext or mHandler is null");
        } else {
            new HwWifiNearFindMonitor(context, handler);
        }
    }

    private void initWindowManagerService() {
        IBinder iBinder = ServiceManager.getService("window");
        if (iBinder == null) {
            Log.e(TAG, "initWindowManagerService, iBinder is null");
        } else if (iBinder instanceof WindowManagerService) {
            this.mHwWindowManagerServiceEx = new HwWindowManagerServiceEx((WindowManagerService) iBinder, this.mContext);
        }
    }

    private synchronized void checkWifiNearFindSwitch(int arbitrationCond) {
        String str = TAG;
        Log.d(str, "checkWifiNearFindSwitch, arbitrationCond = " + arbitrationCond);
        if (this.mContext == null) {
            Log.e(TAG, "checkWifiNearFindSwitch, mContext is null");
            return;
        }
        if (arbitrationCond == 0) {
            this.mStartScanTime = SystemClock.elapsedRealtime();
            this.mHandler.sendEmptyMessage(10);
            startCheckWifiScanInterval();
        } else {
            this.mHandler.sendEmptyMessage(11);
            handleScanTimeRecord();
            HwWifiNearFindChr.getInstance().clearFindScanCount();
        }
    }

    private void startWifiScan() {
        if (HwWifiNearFindUtils.isExistFloatingWindows(this.mHwWindowManagerServiceEx)) {
            Log.d(TAG, "startWifiScan, exist floating windows");
        } else if (this.mWifiScanner == null) {
            Log.e(TAG, "startWifiScan, mWifiScanner is null");
        } else {
            if (this.mScanListener == null) {
                this.mScanListener = new WifiScanner.ScanListener() {
                    /* class com.android.server.wifi.wifinearfind.HwWifiNearFindArbitration.AnonymousClass1 */

                    public void onPeriodChanged(int periodInMs) {
                    }

                    public void onResults(WifiScanner.ScanData[] results) {
                        HwWifiNearFindArbitration.this.processScanResults(results);
                    }

                    public void onFullResult(ScanResult fullScanResult) {
                    }

                    public void onSuccess() {
                    }

                    public void onFailure(int reason, String description) {
                    }
                };
            }
            this.mWifiScanner.startScan(this.mScanSettings, this.mScanListener);
            HwWifiNearFindChr.getInstance().updateFindScanCount();
            HwWifiNearFindChr.getInstance().updateTotalScanCount();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStopWifiScan() {
        removePeriodScanMessages();
        removeLongDistanceScan();
        removeCheckWifiScanMessages();
    }

    private void startCheckWifiScanInterval() {
        removeCheckWifiScanMessages();
        this.mHandler.sendEmptyMessageDelayed(17, 30000);
    }

    private void removePeriodScanMessages() {
        Handler handler = this.mHandler;
        if (handler == null) {
            Log.e(TAG, "removePeriodScanMessages, mHandler is null");
            return;
        }
        if (handler.hasMessages(10)) {
            this.mHandler.removeMessages(10);
        }
        if (this.mHandler.hasMessages(11)) {
            this.mHandler.removeMessages(11);
        }
        Log.d(TAG, "remove period wifi scan messages");
    }

    private void removeLongDistanceScan() {
        Handler handler = this.mHandler;
        if (handler == null) {
            Log.e(TAG, "removeLongDistanceScan: mHandle is null");
            return;
        }
        if (handler.hasMessages(16)) {
            this.mHandler.removeMessages(16);
        }
        Log.d(TAG, "remove long distance wifi scan messages");
    }

    private void removeCheckWifiScanMessages() {
        Handler handler = this.mHandler;
        if (handler == null) {
            Log.e(TAG, "removeCheckWifiScanMessages, mHandler is null");
            return;
        }
        if (handler.hasMessages(17)) {
            this.mHandler.removeMessages(17);
        }
        Log.d(TAG, "remove check wifi scan state messages");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processScanResults(WifiScanner.ScanData[] results) {
        if (results == null || results.length == 0) {
            Log.e(TAG, "processScanResults, results is null");
            return;
        }
        ScanResult tempResult = null;
        int tempRssi = RSSI_THRESHOLD;
        for (WifiScanner.ScanData data : results) {
            ScanResult[] tempScanResult = data.getResults();
            if (!(tempScanResult == null || tempScanResult.length == 0)) {
                int tempRssi2 = tempRssi;
                ScanResult tempResult2 = tempResult;
                for (ScanResult result : tempScanResult) {
                    if (result != null && !TextUtils.isEmpty(result.SSID) && result.is24GHz() && result.frequency == DEFAULT_SCAN_CHANNEL && result.SSID.startsWith(HILINK_DEVICE_PREFIX)) {
                        if (result.level > tempRssi2) {
                            tempRssi2 = result.level;
                            tempResult2 = result;
                        }
                        HwWifiNearFindChr.getInstance().updateScanDeviceMap(result.BSSID, 1);
                    }
                }
                tempResult = tempResult2;
                tempRssi = tempRssi2;
            }
        }
        processDeviceInfo(tempResult);
    }

    private void processDeviceInfo(ScanResult result) {
        if (result == null) {
            Log.e(TAG, "processDeviceInfo, result is null");
            return;
        }
        bindAirLinkService();
        int power = parseTxPower(result.informationElements);
        if (power == Integer.MIN_VALUE) {
            Log.e(TAG, "processDeviceInfo, illegal power value");
            return;
        }
        double distance = calculateDistance((double) result.level, (double) power);
        if (distance > DEFAULT_DISTANCE && distance <= MIN_DISTANCE_THRESHOLD) {
            sendMessageToAirLink(result);
        } else if (distance > MIN_DISTANCE_THRESHOLD && distance <= MAX_DISTANCE_THRESHOLD) {
            if (this.isFirstLongDistance) {
                this.mFirstLongDistance = distance;
                this.mHandler.sendEmptyMessage(16);
            } else if ((this.mFirstLongDistance + distance) / 2.0d <= MED_DISTANCE_THRESHOLD) {
                sendMessageToAirLink(result);
            }
        }
    }

    private int parseTxPower(ScanResult.InformationElement[] ies) {
        byte b = -2147483648;
        if (ies == null || ies.length == 0) {
            Log.e(TAG, "parseTxPower, ies is null");
            return Integer.MIN_VALUE;
        }
        int length = ies.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            ScanResult.InformationElement ie = ies[i];
            if (ie.id != 35) {
                i++;
            } else if (ie.bytes == null || ie.bytes.length < 2) {
                Log.e(TAG, "parseTxPower, ie is null");
                return Integer.MIN_VALUE;
            } else {
                b = ie.bytes[0];
            }
        }
        Log.d(TAG, "parseTxPower, receive device tx power is " + ((int) b));
        return b;
    }

    private double calculateDistance(double rssi, double power) {
        double distance = Math.pow(DEFAULT_BASE_NUMBER, (((Math.abs(rssi) + power) - ((double) HwWifiNearFindUtils.getWifiNearFindLin())) - DEFAULT_LOS_1) / DEFAULT_DENOMINATOR) * 100000.0d;
        String str = TAG;
        Log.d(str, "calculateDistance, device distance is[" + distance + "] cm, and rssi is " + rssi);
        return distance;
    }

    private void sendMessageToAirLink(ScanResult result) {
        if (this.mHwAirLinkUtils == null || result == null || !this.isBindToAirLink) {
            Log.e(TAG, "sendMessageToAirLink, mAirLinkUtils or result is null, or bind airlink failed");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(KEY_BUSINESS_ID, BUSINESS_ID);
        bundle.putString(KEY_SOURCE, SOURCE_TYPE);
        bundle.putString(KEY_DEVICE_INFO, convertDeviceInfo(result.SSID.trim()));
        bundle.putString(KEY_SSID, result.SSID.trim());
        bundle.putString(KEY_BSSID, convertMacAddress(result.BSSID.trim()));
        this.mHwAirLinkUtils.sendDeviceFoundOperation(EVENT_WIFI_NEAR_FIND_DEVICE, bundle);
        HwWifiNearFindChr.getInstance().updateReportSsid(result.SSID.trim());
        HwWifiNearFindChr.getInstance().updateReportDeviceMap(result.BSSID, 1);
    }

    private String convertDeviceInfo(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            Log.e(TAG, "convertDeviceInfo, ssid is null");
            return "";
        }
        String[] temps = ssid.split("-");
        if (temps == null || temps.length < 4) {
            Log.e(TAG, "convertDeviceInfo, temps is null");
            return "";
        }
        String str = temps[temps.length - 1];
        if (!TextUtils.isEmpty(str) && str.length() >= 2) {
            return str.substring(1);
        }
        Log.e(TAG, "convertDeviceInfo: str is null or length < 2");
        return "";
    }

    private String convertMacAddress(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.e(TAG, "convertMacAddress, bssid is null");
            return "";
        }
        String[] temps = bssid.split(":");
        if (temps == null || temps.length < 6) {
            Log.e(TAG, "convertMacAddress, temps is null");
            return "";
        }
        StringBuffer buffer = new StringBuffer(6);
        for (String str : temps) {
            buffer.append(str);
        }
        return buffer.toString();
    }

    private void bindAirLinkService() {
        if (!this.isBindToAirLink) {
            boolean bindResult = this.mHwAirLinkUtils.bindAirLinkService(this.mContext);
            if (bindResult) {
                Log.d(TAG, "processDeviceInfo, bind airlink success");
            } else {
                Log.d(TAG, "processDeviceInfo, bind airlink failed, and retry 5s");
                this.mHandler.sendEmptyMessageDelayed(12, 5000);
            }
            this.isBindToAirLink = bindResult;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAirLinkBindFailed() {
        HwAirLinkUtils hwAirLinkUtils = this.mHwAirLinkUtils;
        if (hwAirLinkUtils != null) {
            boolean result = hwAirLinkUtils.bindAirLinkService(this.mContext);
            String str = TAG;
            Log.d(str, "handleAirLinkBindFailed, rebind airlink result is " + result);
        }
    }

    private void unBindAirLinkService() {
        if (this.mHwAirLinkUtils != null && this.isBindToAirLink) {
            this.mHwAirLinkUtils.unBindAirLinkService(this.mContext);
            this.isBindToAirLink = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHomeActivityChanged(Message msg) {
        if (msg.arg1 > 0) {
            Log.d(TAG, "handleHomeActivityChanged, is in home launcher");
            this.mArbitrationCond &= -2;
        } else {
            Log.d(TAG, "handleHomeActivityChanged, not in home launcher");
            this.mArbitrationCond |= 1;
        }
        checkWifiNearFindSwitch(this.mArbitrationCond);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOn() {
        Log.d(TAG, "handleScreenOn, screen is on");
        this.mArbitrationCond &= -3;
        checkWifiNearFindSwitch(this.mArbitrationCond);
        handleScanInfoChrReport();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOff() {
        Log.d(TAG, "handleScreenOff, screen is off");
        this.mArbitrationCond = this.mArbitrationCond | 2 | 32;
        checkWifiNearFindSwitch(this.mArbitrationCond);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenUnLock() {
        Log.d(TAG, "handleScreenUnLock, screen is unlock");
        this.mArbitrationCond &= -33;
        checkWifiNearFindSwitch(this.mArbitrationCond);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pConnectedMsg(Message msg) {
        if (msg.arg1 > 0) {
            Log.d(TAG, "handleP2pConnectedMsg, p2p is enabled");
            this.mArbitrationCond |= 4;
        } else {
            Log.d(TAG, "handleP2pConnectedMsg, p2p is disabled");
            this.mArbitrationCond &= -5;
        }
        checkWifiNearFindSwitch(this.mArbitrationCond);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWechatAudioOff() {
        Log.d(TAG, "handleWechatAudioOff, wechat audio is off");
        this.mArbitrationCond &= -9;
        checkWifiNearFindSwitch(this.mArbitrationCond);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWechatAudioOn() {
        Log.d(TAG, "handleWechatAudioOn, wechat audio is on");
        this.mArbitrationCond |= 8;
        checkWifiNearFindSwitch(this.mArbitrationCond);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHicarStart() {
        Log.d(TAG, "handleHicarStart, hicar is start");
        this.mArbitrationCond |= 16;
        checkWifiNearFindSwitch(this.mArbitrationCond);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHicarStop() {
        Log.d(TAG, "handleHicarStop, hicar is stop");
        this.mArbitrationCond &= -17;
        checkWifiNearFindSwitch(this.mArbitrationCond);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiStateChanged(int state) {
        Log.d(TAG, "handleWifiStateChanged, wifi state = " + state);
        if (state == 1) {
            this.mArbitrationCond &= -65;
            checkWifiNearFindSwitch(this.mArbitrationCond);
        } else if (state == 0) {
            this.mArbitrationCond |= 64;
            checkWifiNearFindSwitch(this.mArbitrationCond);
            unBindAirLinkService();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHwShareStateChanged(int state) {
        String str = TAG;
        Log.d(str, "handleHwShareStateChange, hwshare state = " + state);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStartPeriodWifiScan() {
        this.isFirstLongDistance = true;
        removePeriodScanMessages();
        startWifiScan();
        this.mHandler.sendEmptyMessageDelayed(10, 1500);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLongDistanceScan() {
        this.isFirstLongDistance = false;
        removeLongDistanceScan();
        startWifiScan();
    }

    public int getArbitrationCond() {
        return this.mArbitrationCond;
    }

    public long getStartScanInterval() {
        return SystemClock.elapsedRealtime() - this.mStartScanTime;
    }

    private void handleScanTimeRecord() {
        long duration = SystemClock.elapsedRealtime() - this.mStartScanTime;
        if (duration > 0 && duration <= 5000) {
            HwWifiNearFindChr.getInstance().updateScanTimeMap(HwWifiNearFindChr.KEY_COUNT0, 1);
        } else if (duration > 5000 && duration <= 10000) {
            HwWifiNearFindChr.getInstance().updateScanTimeMap(HwWifiNearFindChr.KEY_COUNT1, 1);
        } else if (duration > 10000 && duration <= 15000) {
            HwWifiNearFindChr.getInstance().updateScanTimeMap(HwWifiNearFindChr.KEY_COUNT2, 1);
        } else if (duration > 15000 && duration <= 20000) {
            HwWifiNearFindChr.getInstance().updateScanTimeMap(HwWifiNearFindChr.KEY_COUNT3, 1);
        } else if (duration > 20000 && duration <= 25000) {
            HwWifiNearFindChr.getInstance().updateScanTimeMap(HwWifiNearFindChr.KEY_COUNT4, 1);
        } else if (duration <= 25000 || duration > 30000) {
            HwWifiNearFindChr.getInstance().updateScanTimeMap(HwWifiNearFindChr.KEY_COUNT6, 1);
        } else {
            HwWifiNearFindChr.getInstance().updateScanTimeMap(HwWifiNearFindChr.KEY_COUNT5, 1);
        }
    }

    private void handleScanInfoChrReport() {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - this.mStartReportChrTime >= REPORT_STATISTICAL_CHR_INTERVAL) {
            HwWifiNearFindChr.getInstance().reportScanInfoChr();
            this.mStartReportChrTime = currentTime;
        }
    }

    private void initWifiNearFindHandler() {
        HandlerThread handlerThread = new HandlerThread("WifiNearFind_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.wifinearfind.HwWifiNearFindArbitration.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        HwWifiNearFindArbitration.this.initWifiNearFindMonitor();
                        return;
                    case 2:
                        HwWifiNearFindArbitration.this.handleHomeActivityChanged(msg);
                        return;
                    case 3:
                        HwWifiNearFindArbitration.this.handleScreenOn();
                        return;
                    case 4:
                        HwWifiNearFindArbitration.this.handleScreenOff();
                        return;
                    case 5:
                        HwWifiNearFindArbitration.this.handleP2pConnectedMsg(msg);
                        return;
                    case 6:
                        HwWifiNearFindArbitration.this.handleWechatAudioOff();
                        return;
                    case 7:
                        HwWifiNearFindArbitration.this.handleWechatAudioOn();
                        return;
                    case 8:
                        HwWifiNearFindArbitration.this.handleHicarStart();
                        return;
                    case 9:
                        HwWifiNearFindArbitration.this.handleHicarStop();
                        return;
                    case 10:
                        HwWifiNearFindArbitration.this.handleStartPeriodWifiScan();
                        return;
                    case 11:
                    case 17:
                        HwWifiNearFindArbitration.this.handleStopWifiScan();
                        return;
                    case 12:
                        HwWifiNearFindArbitration.this.handleAirLinkBindFailed();
                        return;
                    case 13:
                        HwWifiNearFindArbitration.this.handleScreenUnLock();
                        return;
                    case 14:
                        HwWifiNearFindArbitration.this.handleWifiStateChanged(msg.arg1);
                        return;
                    case 15:
                        HwWifiNearFindArbitration.this.handleHwShareStateChanged(msg.arg1);
                        return;
                    case 16:
                        HwWifiNearFindArbitration.this.handleLongDistanceScan();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mHandler.sendEmptyMessage(1);
    }
}
