package com.huawei.wifi2;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.arp.HwMultiGw;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.HwArpUtils;
import com.android.server.wifi.HwWifiConfigManagerEx;
import com.android.server.wifi.HwWifiNativeEx;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.wifi2.HwWifi2Manager;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.wifi2.HwWifi2ChrManager;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwWifi2ConnectivityManager {
    private static final int CANCEL_P2P_CONNECT_PROTECT_MS = 3000;
    private static final int CANCEL_P2P_CONNECT_PROTECT_MSG = 4;
    private static final int CANCEL_WIFI1_STATUS_SENSITIVE_MSG = 1;
    private static final int CONNECT_NETWORK_TIMEOUT_MS = 10000;
    private static final int CONNECT_NETWORK_TIMEOUT_MSG = 3;
    private static final String DEFAULT_WIFI2_IFACE_NAME = "wlan1";
    private static final int HIDDEN_SSID_MAX_NUM = 2;
    private static final int MAX_SCAN_RESTART_ALLOWED = 1;
    private static final int RESTART_SCAN_DELAY_MS = 1000;
    private static final String RESTART_SINGLE_SCAN_TIMER_TAG = "HwWifi2ConnectivityManager Restart Single Scan";
    private static final String TAG = "HwWifi2ConnectivityManager";
    private static final int TRY_SCAN_AGAIN = 2;
    private static final int WIFI1_ARP_DETECT_DURATION = 300;
    private static final int WIFI1_ARP_DETECT_FAIL_MSG = 0;
    private static final int WIFI1_ARP_DETECT_TIMES = 10;
    private static final int WIFI1_BSSID_BLACKLIST_EXPIRE_TIME_MS = 86400000;
    private static final int WIFI1_STATUS_SENSITIVE_TIME_MS = 3000;
    public static final int WIFI2_BSSID_BLACKLIST_EXPIRE_TIME_MS = 604800000;
    public static final int WIFI2_STATE_CONNECTED = 1;
    public static final int WIFI2_STATE_DISCONNECTED = 2;
    public static final int WIFI2_STATE_TRANSITIONING = 3;
    public static final int WIFI2_STATE_UNKNOWN = 0;
    private Handler mArpDetectHandler;
    private HwWifi2ClientModeImpl mClientModeImpl = null;
    private Context mContext;
    private Handler mCoreHandler;
    private boolean mHasPendingWifi2requested = false;
    private HwWifi2Service mHwWifi2Service = null;
    private boolean mIsP2pActivate = false;
    private boolean mIsP2pProtect = false;
    private boolean mIsWifi1StatusSensitive = false;
    private boolean mIsWifi1connected = false;
    private boolean mIsWifi2Up = false;
    private HwWifi2NetworkSelector mNetworkSelecter = null;
    private int mSingleScanRestartCount = 0;
    private WifiConfiguration mTargetWificonfiguration = null;
    private String mWifi1Bssid = null;
    private Map<String, Long> mWifi1BssidBlacklist = null;
    private String mWifi1GateWayIpCache = null;
    private Map<String, Long> mWifi2BssidBlacklist = null;
    private int mWifi2State = 0;
    private WifiManager mWifiManager;
    private WifiScanner mWifiScanner;

    public HwWifi2ConnectivityManager(Context context, Looper looper, HwWifi2NetworkSelector networkSelector, HwWifi2ClientModeImpl clientModeImpl, HwWifi2Service hwWifi2Service) {
        this.mContext = context;
        this.mNetworkSelecter = networkSelector;
        this.mClientModeImpl = clientModeImpl;
        this.mHwWifi2Service = hwWifi2Service;
        this.mWifi1BssidBlacklist = new HashMap();
        this.mWifi2BssidBlacklist = new HashMap();
        initCoreHandler(looper);
        HandlerThread handlerThread = new HandlerThread("wifi2ArpDetectThread");
        handlerThread.start();
        this.mArpDetectHandler = new Handler(handlerThread.getLooper());
    }

    public boolean requestWifi2Network(int reason) {
        if (!isWifi2Available()) {
            return false;
        }
        this.mHasPendingWifi2requested = true;
        HwHiLog.i(TAG, false, "requestWifi2Network", new Object[0]);
        if (this.mIsWifi2Up) {
            startNewScan();
        } else {
            setWifi1GateWayArp();
            this.mHwWifi2Service.setWifi2Enable(true, reason);
        }
        return true;
    }

    private boolean isWifi2Available() {
        if (!this.mIsWifi1connected || this.mWifi1Bssid == null) {
            HwHiLog.w(TAG, false, "wifi1 isn't connected, wifi2 is unavaiable", new Object[0]);
            HwWifi2ChrManager.handleWifi2ReqFailException(HwWifi2ChrManager.Wifi2ReqResFailReason.WIFI1_NOT_CONNECTED, true);
            return false;
        } else if (!isWifi1ActualConnected()) {
            return false;
        } else {
            if (this.mIsP2pActivate) {
                HwHiLog.i(TAG, false, "p2p is current activate, wifi2 is unavaiable", new Object[0]);
                HwWifi2ChrManager.handleWifi2ReqFailException(HwWifi2ChrManager.Wifi2ReqResFailReason.P2P_IS_ACTIVE, true);
                return false;
            } else if (this.mIsP2pProtect) {
                HwHiLog.i(TAG, false, "in p2p protect time, wifi2 is unavaiable", new Object[0]);
                HwWifi2ChrManager.handleWifi2ReqFailException(HwWifi2ChrManager.Wifi2ReqResFailReason.P2P_IS_PROTECTED, true);
                return false;
            } else if (this.mWifi2State == 1) {
                HwHiLog.i(TAG, false, "wifi2 is already connected, wifi2 is unavaiable for request", new Object[0]);
                HwWifi2ChrManager.handleWifi2ReqFailException(HwWifi2ChrManager.Wifi2ReqResFailReason.WIFI2_ALREADY_CONNECTED, true);
                return false;
            } else if (this.mHasPendingWifi2requested) {
                HwHiLog.i(TAG, false, "HasPendingWifi2requested, wifi2 is unavaiable for request", new Object[0]);
                HwWifi2ChrManager.handleWifi2ReqFailException(HwWifi2ChrManager.Wifi2ReqResFailReason.HAS_PENGDING_WIFI2_REQUESET, true);
                return false;
            } else if (isWifi1InBlackList()) {
                HwWifi2ChrManager.handleWifi2ReqFailException(HwWifi2ChrManager.Wifi2ReqResFailReason.WIFI1_IS_BLACKLIST, true);
                return false;
            } else if (isWifi1WapiNetwork()) {
                HwHiLog.i(TAG, false, "wifi is connected wapi network, wifi2 is unavaiable", new Object[0]);
                HwWifi2ChrManager.handleWifi2ReqFailException(HwWifi2ChrManager.Wifi2ReqResFailReason.WIFI1_WAPI_NETWORK, true);
                return false;
            } else if (!isWifi15gPublicHotspot()) {
                return true;
            } else {
                HwHiLog.i(TAG, false, "wifi is connected public 5g hotspot, wifi2 is unavaiable", new Object[0]);
                HwWifi2ChrManager.handleWifi2ReqFailException(HwWifi2ChrManager.Wifi2ReqResFailReason.WIFI1_5G_PUBLIC_HOTSPOT, true);
                return false;
            }
        }
    }

    private boolean isWifi1ActualConnected() {
        ClientModeImpl clientModeImpl = WifiInjector.getInstance().getClientModeImpl();
        if (clientModeImpl == null || clientModeImpl.getNetworkInfo() == null) {
            HwHiLog.w(TAG, false, "wifi1 status unknown", new Object[0]);
        } else if (clientModeImpl.getNetworkInfo().getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            HwHiLog.w(TAG, false, "wifi1 is actually not connected, wifi2 is unavaiable", new Object[0]);
        }
        HwWifi2ChrManager.handleWifi2ReqFailException(HwWifi2ChrManager.Wifi2ReqResFailReason.WIFI1_NOT_CONNECTED, true);
        return false;
    }

    private boolean isWifi1InBlackList() {
        Long bssidAddtoBlackListTime = this.mWifi1BssidBlacklist.get(this.mWifi1Bssid);
        if (bssidAddtoBlackListTime != null) {
            long currentTimeStamp = SystemClock.elapsedRealtime();
            if (currentTimeStamp - bssidAddtoBlackListTime.longValue() < 86400000) {
                HwHiLog.i(TAG, false, "wifi1 current bssid %{public}s is in blacklist, can not request Wifi2 Network. Current time is %{public}s, wifi1 bssid add to blacklist time is %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(this.mWifi1Bssid), String.valueOf(currentTimeStamp), String.valueOf(bssidAddtoBlackListTime)});
                return true;
            }
            this.mWifi1BssidBlacklist.remove(this.mWifi1Bssid);
            HwHiLog.i(TAG, false, "wifi1 bssid %{public}s expired from blacklist.. Current time is %{public}s, wifi1 bssid add to blacklist time is %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(this.mWifi1Bssid), String.valueOf(currentTimeStamp), String.valueOf(bssidAddtoBlackListTime)});
        }
        return false;
    }

    private boolean isWifi1WapiNetwork() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            HwHiLog.e(TAG, false, "isWifiWapiNetwork mWifiManager is null", new Object[0]);
            return false;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            HwHiLog.e(TAG, false, "isWifiWapiNetwork wifiInfo is null", new Object[0]);
            return false;
        }
        WifiConfigManager wifiConfigManager = WifiInjector.getInstance().getWifiConfigManager();
        if (wifiConfigManager != null) {
            return NetworkTypeUtil.isWapiWifiConfiguration(wifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId()));
        }
        return false;
    }

    private boolean isWifi15gPublicHotspot() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            HwHiLog.e(TAG, false, "isWifi15gPublicHotspot mWifiManager is null", new Object[0]);
            return false;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            HwHiLog.e(TAG, false, "isWifi15gPublicHotspot wifiInfo is null", new Object[0]);
            return false;
        } else if (!wifiInfo.is5GHz()) {
            return false;
        } else {
            WifiConfigManager wifiConfigManager = WifiInjector.getInstance().getWifiConfigManager();
            if (wifiConfigManager != null) {
                WifiConfiguration wifi1Config = wifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
                if (NetworkTypeUtil.isConfigForEapNetwork(wifi1Config) || NetworkTypeUtil.isConfigForEapSuitebNetwork(wifi1Config) || (wifi1Config != null && wifi1Config.portalNetwork)) {
                    return true;
                }
                return false;
            }
            HwHiLog.e(TAG, false, "isWifi15gPublicHotspot wifiConfigManager is null", new Object[0]);
            return false;
        }
    }

    public void releaseWifi2Network(int reason) {
        HwHiLog.i(TAG, false, "releaseWifi2Network for reason %{public}s", new Object[]{HwWifi2Manager.msgToString(reason)});
        clearWifi2Network();
        this.mHwWifi2Service.setWifi2Enable(false, reason);
    }

    public void clearWifi2Network() {
        HwHiLog.i(TAG, false, "clearWifi2Network", new Object[0]);
        clearDelayMsg();
        clearWifi1GateWayArp();
        this.mHasPendingWifi2requested = false;
        this.mSingleScanRestartCount = 0;
        this.mTargetWificonfiguration = null;
    }

    public void handleWifi2Up(boolean isWifi2Up) {
        this.mIsWifi2Up = isWifi2Up;
        if (isWifi2Up && this.mHasPendingWifi2requested && !this.mIsP2pActivate && this.mIsWifi1connected) {
            printSavedNetworks();
            startNewScan();
        }
        if (!isWifi2Up) {
            this.mWifi2State = 0;
        }
    }

    public boolean isWifi2Connected() {
        return this.mWifi2State == 1;
    }

    public WifiConfiguration getTargetWificonfiguration() {
        return this.mTargetWificonfiguration;
    }

    public void handleConnectionStateChanged(int state) {
        if (this.mWifi2State != 0 && state == 2) {
            releaseWifi2Network(1008);
        }
        if (state == 1) {
            if (this.mCoreHandler.hasMessages(3)) {
                this.mCoreHandler.removeMessages(3);
                HwHiLog.i(TAG, false, "Wifi2 is connected cancel CONNECT_NETWORK_TIMEOUT_MSG", new Object[0]);
            }
            clearWifi1GateWayArp();
            HwHiLog.i(TAG, false, "Wifi2 is connected, start wifi1 arp detect", new Object[0]);
            this.mArpDetectHandler.post(new Runnable() {
                /* class com.huawei.wifi2.$$Lambda$HwWifi2ConnectivityManager$aLqSfntR3xc8x0UnzvO4nzy78 */

                @Override // java.lang.Runnable
                public final void run() {
                    HwWifi2ConnectivityManager.this.lambda$handleConnectionStateChanged$0$HwWifi2ConnectivityManager();
                }
            });
        }
        this.mWifi2State = state;
        HwWifi2ChrManager.handleWifi2ConnectStateChange(state);
    }

    public void handleConnectFail() {
        String bssid;
        HwHiLog.e(TAG, false, "Wifi2 connect failed", new Object[0]);
        if (this.mCoreHandler.hasMessages(3)) {
            this.mCoreHandler.removeMessages(3);
        }
        WifiConfiguration wifiConfiguration = this.mTargetWificonfiguration;
        if (!(wifiConfiguration == null || (bssid = wifiConfiguration.getNetworkSelectionStatus().getNetworkSelectionBSSID()) == null)) {
            this.mWifi2BssidBlacklist.put(bssid, Long.valueOf(SystemClock.elapsedRealtime()));
            HwHiLog.i(TAG, false, "add %{public}s to wifi2 Bssid Blacklist", new Object[]{StringUtilEx.safeDisplayBssid(bssid)});
        }
        releaseWifi2Network(1005);
    }

    public void setSlaveWifiNetworkSelectionPara(int signalLevel, int callerUid, int needInternet) {
        boolean isNeedInternet = false;
        HwHiLog.i(TAG, false, "setSlaveWifiNetworkSelectionPara: %{public}d %{public}d %{public}d", new Object[]{Integer.valueOf(signalLevel), Integer.valueOf(callerUid), Integer.valueOf(needInternet)});
        if (signalLevel >= 0 || signalLevel <= 4) {
            if (needInternet != 0) {
                isNeedInternet = true;
            }
            this.mNetworkSelecter.setNetworkSelectorePara(signalLevel, isNeedInternet);
        }
    }

    public void handleBootCompleted() {
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mWifiScanner = (WifiScanner) this.mContext.getSystemService("wifiscanner");
    }

    public Map<String, Long> getWifi2BssidBlacklist() {
        return this.mWifi2BssidBlacklist;
    }

    private void initCoreHandler(Looper looper) {
        this.mCoreHandler = new Handler(looper) {
            /* class com.huawei.wifi2.HwWifi2ConnectivityManager.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 0) {
                    HwHiLog.i(HwWifi2ConnectivityManager.TAG, false, "WIFI1_ARP_DETECT_FAIL_MSG recived", new Object[0]);
                    HwWifi2ConnectivityManager.this.handleConflictWithWifi1(1009);
                } else if (i == 1) {
                    HwHiLog.i(HwWifi2ConnectivityManager.TAG, false, "CANCEL_WIFI1_STATUS_SENSITIVE_MSG recived", new Object[0]);
                    HwWifi2ConnectivityManager.this.mIsWifi1StatusSensitive = false;
                } else if (i != 2) {
                    if (i == 3) {
                        HwHiLog.i(HwWifi2ConnectivityManager.TAG, false, "wifi2 connect timeout!", new Object[0]);
                        HwWifi2ConnectivityManager.this.handleConnectFail();
                    } else if (i != 4) {
                        HwHiLog.e(HwWifi2ConnectivityManager.TAG, false, "unknow msg", new Object[0]);
                    } else {
                        HwHiLog.i(HwWifi2ConnectivityManager.TAG, false, "CANCEL_P2P_CONNECT_PROTECT_MSG", new Object[0]);
                        HwWifi2ConnectivityManager.this.mIsP2pProtect = false;
                    }
                } else if (HwWifi2ConnectivityManager.this.mIsP2pActivate || !HwWifi2ConnectivityManager.this.mIsWifi1connected || !HwWifi2ConnectivityManager.this.mHasPendingWifi2requested || !HwWifi2ConnectivityManager.this.mIsWifi2Up) {
                    HwWifi2ConnectivityManager.this.releaseWifi2Network(1015);
                } else {
                    HwWifi2ConnectivityManager.this.triggerScan();
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConflictWithWifi1(int reason) {
        HwHiLog.i(TAG, false, "handleConflictWithWifi1 enter reason is " + HwWifi2Manager.msgToString(reason), new Object[0]);
        if (this.mWifi1Bssid != null) {
            this.mWifi1BssidBlacklist.put(this.mWifi1Bssid, Long.valueOf(SystemClock.elapsedRealtime()));
            HwHiLog.i(TAG, false, "add %{public}s to Wifi1 Bssid Blacklist", new Object[]{StringUtilEx.safeDisplayBssid(this.mWifi1Bssid)});
        }
        releaseWifi2Network(reason);
    }

    private void startNewScan() {
        this.mSingleScanRestartCount = 0;
        this.mTargetWificonfiguration = null;
        if (this.mCoreHandler.hasMessages(2)) {
            this.mCoreHandler.removeMessages(2);
            HwHiLog.i(TAG, false, "startNewScan cancel old msg TRY_SCAN_AGAIN", new Object[0]);
        }
        triggerScan();
    }

    /* access modifiers changed from: private */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0044: APUT  (r4v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r3v1 java.lang.String) */
    /* access modifiers changed from: public */
    private void triggerScan() {
        WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
        settings.type = 2;
        settings.reportEvents = 3;
        settings.band = this.mWifiManager.getConnectionInfo().is24GHz() ? 6 : 1;
        settings.numBssidsPerScan = 0;
        List<WifiScanner.ScanSettings.HiddenNetwork> hiddenNetworkList = retrieveHiddenNetworkList();
        settings.hiddenNetworks = (WifiScanner.ScanSettings.HiddenNetwork[]) hiddenNetworkList.toArray(new WifiScanner.ScanSettings.HiddenNetwork[hiddenNetworkList.size()]);
        SingleScanListener singleScanListener = new SingleScanListener();
        Object[] objArr = new Object[1];
        objArr[0] = settings.band == 6 ? "WIFI_BAND_5_GHZ_WITH_DFS" : "WIFI_BAND_24_GHZ";
        HwHiLog.i(TAG, false, "start scan scan, band is %{public}s", objArr);
        this.mWifiScanner.startScan(settings, singleScanListener, (WorkSource) null);
    }

    private List<WifiScanner.ScanSettings.HiddenNetwork> retrieveHiddenNetworkList() {
        List<WifiScanner.ScanSettings.HiddenNetwork> hiddenList = new ArrayList<>();
        List<WifiConfiguration> networks = this.mWifiManager.getPrivilegedConfiguredNetworks();
        Iterator<WifiConfiguration> iter = networks.iterator();
        while (iter.hasNext()) {
            if (!iter.next().hiddenSSID) {
                iter.remove();
            }
        }
        Collections.sort(networks, HwWifiConfigManagerEx.sScanListTimeComparator);
        int hiddenSsidAddedNum = 0;
        for (WifiConfiguration config : networks) {
            if (hiddenSsidAddedNum >= 2) {
                HwHiLog.i(TAG, false, "hidden ssid %{public}s is filted", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID)});
            } else {
                hiddenList.add(new WifiScanner.ScanSettings.HiddenNetwork(config.SSID));
                hiddenSsidAddedNum++;
            }
        }
        HwHiLog.i(TAG, false, "retrieveHiddenNetworkList hiddenList size is %{public}d", new Object[]{Integer.valueOf(hiddenList.size())});
        return hiddenList;
    }

    private void printSavedNetworks() {
        List<WifiConfiguration> networks = this.mWifiManager.getPrivilegedConfiguredNetworks();
        StringBuilder networksLogBuffer = new StringBuilder(256);
        networksLogBuffer.append("there are " + networks.size() + " saved networks:");
        for (WifiConfiguration network : networks) {
            networksLogBuffer.append("[");
            networksLogBuffer.append(StringUtilEx.safeDisplaySsid(network.SSID));
            networksLogBuffer.append(",");
            networksLogBuffer.append(network.allowedKeyManagement);
            networksLogBuffer.append(",");
            networksLogBuffer.append(network.portalNetwork);
            networksLogBuffer.append(",");
            networksLogBuffer.append(network.noInternetAccess);
            networksLogBuffer.append("]");
        }
        HwHiLog.i(TAG, false, networksLogBuffer.toString(), new Object[0]);
    }

    public void setP2pActivate(boolean isActivate) {
        this.mIsP2pActivate = isActivate;
    }

    public void setWifi1connected(boolean isWifi1connected) {
        this.mIsWifi1connected = isWifi1connected;
        if (isWifi1connected) {
            if (this.mWifiManager == null) {
                this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            }
            this.mWifi1Bssid = this.mWifiManager.getConnectionInfo().getBSSID();
            if (this.mIsWifi2Up && ((this.mWifiManager.getConnectionInfo().is24GHz() && this.mClientModeImpl.getWifiInfo().is24GHz()) || (this.mWifiManager.getConnectionInfo().is5GHz() && this.mClientModeImpl.getWifiInfo().is5GHz()))) {
                releaseWifi2Network(1010);
                HwHiLog.i(TAG, false, "wifi1 roam to same band with wifi2, release wifi2", new Object[0]);
            }
        } else {
            if (this.mIsWifi1StatusSensitive) {
                handleConflictWithWifi1(1012);
            }
            this.mWifi1Bssid = null;
        }
        this.mIsWifi1StatusSensitive = false;
    }

    /* access modifiers changed from: protected */
    public void handleP2pConnectCommand(int command) {
        HwHiLog.i(TAG, false, "handleP2pConnectCommand cammand is %{public}d", new Object[]{Integer.valueOf(command)});
        this.mIsP2pProtect = true;
        Handler handler = this.mCoreHandler;
        handler.sendMessageDelayed(handler.obtainMessage(4), 3000);
        if (HwWifi2Injector.getInstance().getWifiController().getWifi2State() != 1) {
            releaseWifi2Network(1003);
        }
    }

    /* access modifiers changed from: private */
    public class SingleScanListener implements WifiScanner.ScanListener {
        private SingleScanListener() {
        }

        public void onSuccess() {
            HwHiLog.i(HwWifi2ConnectivityManager.TAG, false, "SingleScanListener onSuccess", new Object[0]);
        }

        public void onFailure(int reason, String description) {
            HwWifi2ConnectivityManager.this.tryScanAgain();
        }

        public void onPeriodChanged(int periodInMs) {
            HwHiLog.i(HwWifi2ConnectivityManager.TAG, false, "SingleScanListener onPeriodChanged", new Object[0]);
        }

        public void onResults(WifiScanner.ScanData[] results) {
            ScanResult[] scanResults = results[0].getResults();
            StringBuilder scanResultsLogBuffer = new StringBuilder(256);
            scanResultsLogBuffer.append("Received " + scanResults.length + " scan results:");
            int length = scanResults.length;
            for (int i = 0; i < length; i++) {
                ScanResult scanResult = scanResults[i];
                scanResultsLogBuffer.append("[");
                scanResultsLogBuffer.append(StringUtilEx.safeDisplaySsid(scanResult.SSID));
                scanResultsLogBuffer.append(",");
                scanResultsLogBuffer.append(StringUtilEx.safeDisplayBssid(scanResult.BSSID));
                scanResultsLogBuffer.append(",");
                scanResultsLogBuffer.append(scanResult.frequency);
                scanResultsLogBuffer.append(",");
                scanResultsLogBuffer.append(scanResult.level);
                scanResultsLogBuffer.append("]");
            }
            String scanResultsLog = scanResultsLogBuffer.toString();
            HwHiLog.i(HwWifi2ConnectivityManager.TAG, false, "%{public}s", new Object[]{scanResultsLog.substring(0, scanResultsLog.length() / 2)});
            HwHiLog.i(HwWifi2ConnectivityManager.TAG, false, "%{public}s", new Object[]{scanResultsLog.substring(scanResultsLog.length() / 2)});
            HwWifi2ConnectivityManager.this.handleScanResult(scanResults);
        }

        public void onFullResult(ScanResult fullScanResult) {
        }
    }

    private HwArpUtils.ArpItem getWifi1GateWayArpFromFile() {
        DhcpInfo dhcpInfo = this.mWifiManager.getDhcpInfo();
        if (dhcpInfo == null || dhcpInfo.gateway == 0) {
            HwHiLog.i(TAG, false, "getWifi1GateWayArpFromFile dhcpInfo invalid", new Object[0]);
            return null;
        }
        String wifi1GateWay = NetworkUtils.intToInetAddress(dhcpInfo.gateway).getHostAddress();
        for (HwArpUtils.ArpItem arpitem : new HwArpUtils(this.mContext).readArpFromFile()) {
            if (arpitem.sameIpaddress(wifi1GateWay)) {
                return arpitem;
            }
        }
        HwHiLog.i(TAG, false, "getWifi1GateWayArpFromFile null arpitem", new Object[0]);
        return null;
    }

    private void setWifi1GateWayArp() {
        String ifaceName = this.mClientModeImpl.getWifi2IfaceName();
        if (TextUtils.isEmpty(ifaceName)) {
            ifaceName = DEFAULT_WIFI2_IFACE_NAME;
        }
        HwWifiNativeEx hwWifiNativeEx = HwWifiNativeEx.getInstance();
        HwArpUtils.ArpItem arpItem = getWifi1GateWayArpFromFile();
        if (arpItem != null) {
            HwHiLog.i(TAG, false, "setWifi1GateWayArp from arp file", new Object[0]);
            hwWifiNativeEx.setStaticArp(ifaceName, arpItem.ipaddr, arpItem.hwaddr);
            this.mWifi1GateWayIpCache = arpItem.ipaddr;
            return;
        }
        HwMultiGw wifi1MultiGw = new HwArpUtils(this.mContext).getGateWayArpResponses(1, 100);
        if (wifi1MultiGw == null) {
            HwHiLog.i(TAG, false, "setWifi1GateWayArp wifi1MultiGw is null", new Object[0]);
        } else if (hwWifiNativeEx == null) {
            HwHiLog.i(TAG, false, "setWifi1GateWayArp hwWifiNativeEx is null", new Object[0]);
        } else {
            String mac = wifi1MultiGw.getNextGwMacAddr();
            this.mWifi1GateWayIpCache = wifi1MultiGw.getGwIpAddr();
            hwWifiNativeEx.setStaticArp(ifaceName, this.mWifi1GateWayIpCache, mac);
        }
    }

    private void clearWifi1GateWayArp() {
        String ifaceName = this.mClientModeImpl.getWifi2IfaceName();
        if (TextUtils.isEmpty(ifaceName)) {
            ifaceName = DEFAULT_WIFI2_IFACE_NAME;
        }
        if (!TextUtils.isEmpty(this.mWifi1GateWayIpCache)) {
            HwWifiNativeEx hwWifiNativeEx = HwWifiNativeEx.getInstance();
            if (hwWifiNativeEx == null) {
                HwHiLog.i(TAG, false, "clearWifi1GateWayArp hwWifiNativeEx is null", new Object[0]);
                return;
            }
            hwWifiNativeEx.delStaticArp(ifaceName, this.mWifi1GateWayIpCache);
            this.mWifi1GateWayIpCache = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryScanAgain() {
        HwHiLog.i(TAG, false, "tryScanAgain, mSingleScanRestartCount is %{public}d", new Object[]{Integer.valueOf(this.mSingleScanRestartCount)});
        int i = this.mSingleScanRestartCount;
        if (i < 1) {
            Handler handler = this.mCoreHandler;
            handler.sendMessageDelayed(handler.obtainMessage(2), (long) (this.mSingleScanRestartCount * RESTART_SCAN_DELAY_MS));
            this.mSingleScanRestartCount++;
            return;
        }
        HwHiLog.w(TAG, false, "tryScanAgain, tried %{public}d times, begin to close wifi2", new Object[]{Integer.valueOf(i)});
        this.mSingleScanRestartCount = 0;
        releaseWifi2Network(1007);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanResult(ScanResult[] scanResults) {
        WifiConfiguration targetWificonfiguration = this.mNetworkSelecter.selectNetwork(scanResults);
        this.mTargetWificonfiguration = targetWificonfiguration;
        if (targetWificonfiguration != null) {
            handleStaticIpConfig(targetWificonfiguration);
            this.mHasPendingWifi2requested = false;
            targetWificonfiguration.macRandomizationSetting = 0;
            this.mClientModeImpl.sendMessage(151553, -1, -1, targetWificonfiguration);
            this.mIsWifi1StatusSensitive = true;
            clearDelayMsg();
            HwHiLog.i(TAG, false, "targetWificonfiguration SSID is %{public}s,BSSID is %{public}s,keyMgmt is %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(targetWificonfiguration.getPrintableSsid()), StringUtilEx.safeDisplayBssid(targetWificonfiguration.getNetworkSelectionStatus().getNetworkSelectionBSSID()), targetWificonfiguration.allowedKeyManagement});
            Handler handler = this.mCoreHandler;
            handler.sendMessageDelayed(handler.obtainMessage(1), 3000);
            Handler handler2 = this.mCoreHandler;
            handler2.sendMessageDelayed(handler2.obtainMessage(3), 10000);
            return;
        }
        tryScanAgain();
    }

    private void handleStaticIpConfig(WifiConfiguration targetWificonfiguration) {
        WifiInfo wifiInfo;
        IpConfiguration ipConfiguration;
        LinkAddress linkAddress;
        if (targetWificonfiguration.getIpAssignment() != null && targetWificonfiguration.getIpAssignment() == IpConfiguration.IpAssignment.STATIC && (wifiInfo = this.mWifiManager.getConnectionInfo()) != null && wifiInfo.getSSID() != null) {
            if (wifiInfo.getSSID().equals(targetWificonfiguration.SSID)) {
                targetWificonfiguration.setIpAssignment(IpConfiguration.IpAssignment.DHCP);
                HwHiLog.i(TAG, false, "handleStaticIpConfig same SSID to wifi1 change static ip config to dhcp", new Object[0]);
            } else if (targetWificonfiguration.getNetworkSelectionStatus() != null && WifiProCommonUtils.isDualBandAp(wifiInfo.getBSSID(), targetWificonfiguration.getNetworkSelectionStatus().getNetworkSelectionBSSID()) && (ipConfiguration = targetWificonfiguration.getIpConfiguration()) != null && ipConfiguration.getStaticIpConfiguration() != null && (linkAddress = ipConfiguration.getStaticIpConfiguration().ipAddress) != null && linkAddress.getAddress() != null) {
                InetAddress inetAddress = linkAddress.getAddress();
                if ((inetAddress instanceof Inet4Address) && NetworkUtils.inetAddressToInt((Inet4Address) inetAddress) == wifiInfo.getIpAddress()) {
                    targetWificonfiguration.setIpAssignment(IpConfiguration.IpAssignment.DHCP);
                    HwHiLog.i(TAG, false, "handleStaticIpConfig same ip to wifi1 change static ip config to dhcp", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: checkWifi1ArpStatus */
    public void lambda$handleConnectionStateChanged$0$HwWifi2ConnectivityManager() {
        if (!new HwArpUtils(this.mContext).isGateWayReachable((int) WIFI1_ARP_DETECT_TIMES, (int) WIFI1_ARP_DETECT_DURATION)) {
            HwHiLog.i(TAG, false, "Wifi1 Arp detect fail during wifi2 connecting", new Object[0]);
            Handler handler = this.mCoreHandler;
            handler.sendMessage(handler.obtainMessage(0));
        }
    }

    private void clearDelayMsg() {
        if (this.mCoreHandler.hasMessages(3)) {
            this.mCoreHandler.removeMessages(3);
        }
        if (this.mCoreHandler.hasMessages(2)) {
            this.mCoreHandler.removeMessages(2);
        }
    }
}
