package com.huawei.hwwifiproservice;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiHandover {
    public static final String ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER = "com.huawei.wifi.action.REQUEST_DUAL_BAND_WIFI_HANDOVER";
    public static final String ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER = "com.huawei.wifi.action.RESPONSE_DUAL_BAND_WIFI_HANDOVER";
    public static final String ACTION_RESPONSE_WIFI_2_WIFI = "com.huawei.wifi.action.RESPONSE_WIFI_2_WIFI";
    private static final int CURRENT_STATE_IDLE = 0;
    private static final int CURRENT_STATE_WAITING_CONNECTION_COMPLETED = 2;
    private static final int CURRENT_STATE_WAITING_SCAN_RESULTS_FOR_CONNECT_WIFI = 1;
    private static final int CURRENT_STATE_WAITING_SCAN_RESULTS_FOR_WIFI_SWITCH = 4;
    private static final int CURRENT_STATE_WAITING_WIFI_2_WIFI_COMPLETED = 3;
    private static final String EVENT_SSID_SWITCH = "SsidSwitchEnter";
    private static final int HANDLER_CMD_NOTIFY_GOOD_AP = 4;
    private static final int HANDLER_CMD_REQUEST_SCAN_TIME_OUT = 7;
    private static final int HANDLER_CMD_SCAN_RESULTS = 1;
    private static final int HANDLER_CMD_WIFI_2_WIFI = 3;
    private static final int HANDLER_CMD_WIFI_CONNECTED = 2;
    private static final int HANDLER_CMD_WIFI_DISCONNECTED = 6;
    private static final int HANDOVER_MIN_LEVEL_INTERVAL = 2;
    public static final int HANDOVER_NO_CANDIDATE = 22;
    private static final int HANDOVER_STATE_WAITING_DUAL_BAND_WIFI_CONNECT = 5;
    public static final int HANDOVER_STATUS_CONNECT_AUTH_FAILED = -7;
    public static final int HANDOVER_STATUS_CONNECT_REJECT_FAILED = -6;
    public static final int HANDOVER_STATUS_DISALLOWED = -4;
    public static final int HANDOVER_STATUS_OK = 0;
    public static final long HANDOVER_WAIT_SCAN_TIME_OUT = 4000;
    private static final int HISI_CHIP_ROAM_24G_RSSI_THRESHOLD = -72;
    private static final int HISI_CHIP_ROAM_5G_RSSI_THRESHOLD = -70;
    public static final int INVALID_RSSI = -200;
    private static final int NETWORK_HANDLER_CMD_DUAL_BAND_WIFI_CONNECT = 5;
    public static final int NETWORK_HANDOVER_TYPE_CONNECT_SPECIFIC_WIFI = 2;
    public static final int NETWORK_HANDOVER_TYPE_DUAL_BAND_WIFI_CONNECT = 4;
    public static final int NETWORK_HANDOVER_TYPE_UNKNOWN = -1;
    public static final int NETWORK_HANDOVER_TYPE_WIFI_TO_WIFI = 1;
    private static final int SIGNAL_FOUR_LEVEL = 4;
    private static final int SIGNAL_THREE_LEVEL = 3;
    private static final int SIGNAL_TWO_LEVEL = 2;
    public static final String TAG = "WifiHandover";
    private static final int TARGET_AP_QUALITYSCORE = 40;
    private static final int TYPE_BATTERY_PREFERENCE = 2;
    private static final int TYPE_NON_PREFERENCE = 0;
    private static final int TYPE_USER_PREFERENCE = 1;
    public static final String WIFI_HANDOVER_COMPLETED_STATUS = "com.huawei.wifi.handover.status";
    public static final String WIFI_HANDOVER_NETWORK_BSSID = "com.huawei.wifi.handover.bssid";
    public static final String WIFI_HANDOVER_NETWORK_CONFIGKYE = "com.huawei.wifi.handover.configkey";
    public static final String WIFI_HANDOVER_NETWORK_SSID = "com.huawei.wifi.handover.ssid";
    public static final String WIFI_HANDOVER_NETWORK_SWITCHTYPE = "com.huawei.wifi.handover.switchtype";
    public static final String WIFI_HANDOVER_NETWORK_WIFICONFIG = "com.huawei.wifi.handover.wificonfig";
    public static final String WIFI_HANDOVER_RECV_PERMISSION = "com.huawei.wifipro.permission.RECV.WIFI_HANDOVER";
    public static final int WIFI_SWITCH_REASON_BACKGROUND_CHECK_AVAILABLE_WIFI = 5;
    public static final int WIFI_SWITCH_REASON_NO_INTERNET = 1;
    public static final int WIFI_SWITCH_REASON_POOR_RSSI = 2;
    public static final int WIFI_SWITCH_REASON_POOR_RSSI_INTERNET_SLOW = 4;
    public static final int WIFI_SWITCH_REASON_STRONG_RSSI_INTERNET_SLOW = 3;
    private static final int WIFI_TO_WIFI_THRESHOLD = 3;
    private BroadcastReceiver mBroadcastReceiver;
    private INetworksHandoverCallBack mCallBack;
    private Context mContext = null;
    private IntentFilter mIntentFilter;
    private boolean mIsWiFiInternet = false;
    private AtomicBoolean mIsWiFiSameSsidSwitch = new AtomicBoolean(false);
    private int mNetwoksHandoverState;
    private int mNetwoksHandoverType;
    private NetworkBlackListManager mNetworkBlackListManager = null;
    private Handler mNetworkHandler;
    private NetworkQosMonitor mNetworkQosMonitor;
    private String mOldConfigKey = null;
    private ArrayList<String> mSwitchBlacklist;
    private String mTargetBssid = null;
    private String mToConnectBssid;
    private String mToConnectConfigKey;
    private String mToConnectDualbandBssid = null;
    private WifiManager mWifiManager;
    private int mWifiSwitchReason = 0;
    private WifiProChrUploadManager uploadManager;

    public WifiHandover(Context context, INetworksHandoverCallBack callBack) {
        this.mCallBack = callBack;
        this.mContext = context;
        initialize();
        registerReceiverAndHandler();
    }

    public void registerCallBack(INetworksHandoverCallBack callBack, NetworkQosMonitor qosMonitor) {
        if (this.mCallBack == null) {
            this.mCallBack = callBack;
        }
        if (this.mBroadcastReceiver == null && this.mContext != null) {
            registerReceiverAndHandler();
        }
        this.mNetworkQosMonitor = qosMonitor;
    }

    public void unRegisterCallBack() {
        Context context;
        this.mCallBack = null;
        BroadcastReceiver broadcastReceiver = this.mBroadcastReceiver;
        if (broadcastReceiver != null && (context = this.mContext) != null) {
            context.unregisterReceiver(broadcastReceiver);
            this.mBroadcastReceiver = null;
            this.mIntentFilter = null;
            this.mNetworkHandler = null;
        }
    }

    private void initialize() {
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mNetworkBlackListManager = NetworkBlackListManager.getNetworkBlackListManagerInstance(this.mContext);
        this.uploadManager = WifiProChrUploadManager.getInstance(this.mContext);
    }

    private void registerReceiverAndHandler() {
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mIntentFilter.addAction(ACTION_RESPONSE_WIFI_2_WIFI);
        this.mIntentFilter.addAction(ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER);
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        this.mNetworkHandler = new Handler() {
            /* class com.huawei.hwwifiproservice.WifiHandover.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        WifiHandover.this.handleScanResult();
                        break;
                    case 2:
                        WifiHandover.this.handleWifiConnectMsg();
                        break;
                    case 3:
                        WifiHandover.this.handleWifi2Wifi(msg);
                        break;
                    case 4:
                        WifiHandover.this.notifyGoodApFound();
                        break;
                    case 5:
                        WifiHandover.this.handleDualBandConnect(msg);
                        break;
                    case 6:
                        WifiHandover.this.mNetworkBlackListManager.cleanAbnormalWifiBlacklist();
                        break;
                    case 7:
                        WifiHandover.logI("HANDLER_CMD_REQUEST_SCAN_TIME_OUT");
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 1));
                        break;
                }
                super.handleMessage(msg);
            }
        };
        handleBroadcastReceiver();
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, WIFI_HANDOVER_RECV_PERMISSION, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDualBandConnect(Message msg) {
        if (msg != null) {
            int handoverStatus = msg.arg1;
            if (msg.obj instanceof Bundle) {
                handleDualBandWifiConnectMsg((Bundle) msg.obj, handoverStatus);
            } else {
                logE("NETWORK_HANDLER_CMD_DUAL_BAND_WIFI_CONNECT:Class is not match");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifi2Wifi(Message msg) {
        if (msg != null) {
            int code = msg.arg1;
            if (msg.obj instanceof Bundle) {
                handleWifi2WifiMsg((Bundle) msg.obj, code);
            } else {
                logE("HANDLER_CMD_WIFI_2_WIFI:Class is not match");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanResult() {
        if (this.mNetworkHandler.hasMessages(7)) {
            this.mNetworkHandler.removeMessages(7);
        }
        int i = this.mNetwoksHandoverState;
        if (i == 1) {
            handleScanResultsForConnectWiFi();
        } else if (i == 4) {
            trySwitchWifiNetwork(selectQualifiedCandidate());
        }
    }

    private void handleBroadcastReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.huawei.hwwifiproservice.WifiHandover.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    WifiHandover.this.handleBroadcast(intent);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBroadcast(Intent intent) {
        if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
            int i = this.mNetwoksHandoverState;
            if (i == 1 || i == 4) {
                Handler handler = this.mNetworkHandler;
                handler.sendMessage(Message.obtain(handler, 1));
            } else if (WifiProCommonUtils.isWifiConnected(this.mWifiManager) && this.mNetwoksHandoverState == 0) {
                Handler handler2 = this.mNetworkHandler;
                handler2.sendMessage(Message.obtain(handler2, 4));
            }
        } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
            NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                int i2 = this.mNetwoksHandoverState;
                if (i2 == 1 || i2 == 2) {
                    Handler handler3 = this.mNetworkHandler;
                    handler3.sendMessage(Message.obtain(handler3, 2));
                }
            } else if (netInfo != null && netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                Handler handler4 = this.mNetworkHandler;
                handler4.sendMessage(Message.obtain(handler4, 6));
            }
        } else if (ACTION_RESPONSE_WIFI_2_WIFI.equals(intent.getAction())) {
            if (this.mNetwoksHandoverState == 3) {
                int status = intent.getIntExtra(WIFI_HANDOVER_COMPLETED_STATUS, -100);
                Bundle bundle = buildBundleFromIntent(intent);
                Handler handler5 = this.mNetworkHandler;
                handler5.sendMessage(Message.obtain(handler5, 3, status, -1, bundle));
            }
        } else if (ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER.equals(intent.getAction()) && this.mNetwoksHandoverState == 5) {
            int status2 = intent.getIntExtra(WIFI_HANDOVER_COMPLETED_STATUS, -100);
            Bundle bundle2 = buildBundleFromIntent(intent);
            Handler handler6 = this.mNetworkHandler;
            handler6.sendMessage(Message.obtain(handler6, 5, status2, -1, bundle2));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiConnectMsg() {
        boolean connectStatus = false;
        WifiInfo newWifiInfo = this.mWifiManager.getConnectionInfo();
        logI("HANDLER_CMD_WIFI_CONNECTED, newWifiInfo = " + newWifiInfo);
        if (!(newWifiInfo == null || newWifiInfo.getBSSID() == null || !newWifiInfo.getBSSID().equals(this.mToConnectBssid))) {
            connectStatus = true;
        }
        sendNetworkHandoverResult(this.mNetwoksHandoverType, connectStatus, this.mToConnectBssid, 0);
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        this.mToConnectBssid = null;
    }

    private void handleWifi2WifiMsg(Bundle bundle, int code) {
        if (bundle == null) {
            logW("handleWifi2WifiMsg , invalid parameter");
            return;
        }
        String bssid = bundle.getString(WIFI_HANDOVER_NETWORK_BSSID);
        String configKey = bundle.getString(WIFI_HANDOVER_NETWORK_CONFIGKYE);
        boolean handoverStatusOk = false;
        if (code == 0 && configKey != null) {
            handoverStatusOk = true;
        }
        if ((configKey != null && code != 0) || handoverStatusOk) {
            logI("HANDLER_CMD_WIFI_2_WIFI , wifi 2 wifi cleanTempBlackList");
            this.mNetworkBlackListManager.cleanTempWifiBlackList();
        } else if (this.mNetworkBlackListManager.isFailedMultiTimes(this.mTargetBssid)) {
            logI("HANDLER_CMD_WIFI_2_WIFI failed!, add to abnormal black list");
            this.mNetworkBlackListManager.addAbnormalWifiBlacklist(this.mTargetBssid);
            this.mNetworkBlackListManager.cleanTempWifiBlackList();
        }
        sendNetworkHandoverResult(this.mNetwoksHandoverType, handoverStatusOk, bssid, 0);
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        this.mOldConfigKey = null;
        this.mSwitchBlacklist = null;
        this.mTargetBssid = null;
    }

    private void handleDualBandWifiConnectMsg(Bundle result, int handoverStatus) {
        if (result == null) {
            logW("handleDualBandWifiConnectMsg , invalid parameter");
            return;
        }
        result.getString(WIFI_HANDOVER_NETWORK_SSID);
        String dualBandBssid = result.getString(WIFI_HANDOVER_NETWORK_BSSID);
        String dualBandConfigKey = result.getString(WIFI_HANDOVER_NETWORK_CONFIGKYE);
        boolean dualbandhandoverOK = false;
        String str = this.mToConnectConfigKey;
        boolean isSameBssid = true;
        boolean isSameConfigKey = str != null && str.equals(dualBandConfigKey);
        String str2 = this.mToConnectDualbandBssid;
        if (str2 == null || !str2.equals(dualBandBssid)) {
            isSameBssid = false;
        }
        int status = handoverStatus;
        if (status == 0 && isSameConfigKey && isSameBssid) {
            dualbandhandoverOK = true;
        }
        if (!dualbandhandoverOK && status != -7) {
            status = -6;
        }
        sendNetworkHandoverResult(this.mNetwoksHandoverType, dualbandhandoverOK, dualBandBssid, status);
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        this.mToConnectConfigKey = null;
        this.mToConnectDualbandBssid = null;
    }

    private Bundle buildBundleFromIntent(Intent intent) {
        Bundle bundle = null;
        if (intent != null) {
            int status = intent.getIntExtra(WIFI_HANDOVER_COMPLETED_STATUS, -100);
            String bssid = intent.getStringExtra(WIFI_HANDOVER_NETWORK_BSSID);
            String ssid = intent.getStringExtra(WIFI_HANDOVER_NETWORK_SSID);
            String configKey = intent.getStringExtra(WIFI_HANDOVER_NETWORK_CONFIGKYE);
            bundle = new Bundle();
            bundle.putString(WIFI_HANDOVER_NETWORK_BSSID, bssid);
            bundle.putString(WIFI_HANDOVER_NETWORK_SSID, ssid);
            bundle.putString(WIFI_HANDOVER_NETWORK_CONFIGKYE, configKey);
            if (ACTION_RESPONSE_WIFI_2_WIFI.equals(intent.getAction())) {
                logI("ACTION_RESPONSE_WIFI_2_WIFI received, status = " + status + ", ssid = " + StringUtilEx.safeDisplaySsid(ssid));
            } else if (ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER.equals(intent.getAction())) {
                logI("ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER received, status = " + status + ", ssid = " + StringUtilEx.safeDisplaySsid(ssid));
            } else {
                logW("unhandled action = " + intent.getAction());
            }
        }
        return bundle;
    }

    private void sendNetworkHandoverResult(int type, boolean status, String bssid, int errorReason) {
        if (this.mCallBack != null && type != -1) {
            logW("sendNetworkHandoverResult, type = " + type + ", status = " + status + ", bssid = " + StringUtilEx.safeDisplayBssid(bssid) + ", errorReason = " + errorReason);
            this.mCallBack.onWifiHandoverChange(type, status, bssid, errorReason);
        }
    }

    private void notifyWifiAvailableStatus(boolean status, int bestRssi, String targetBssid, int preferType, int freq) {
        INetworksHandoverCallBack iNetworksHandoverCallBack = this.mCallBack;
        if (iNetworksHandoverCallBack == null) {
            logW("notifyWifiAvailableStatus invalid parameter");
        } else {
            iNetworksHandoverCallBack.onCheckAvailableWifi(status, bestRssi, targetBssid, preferType, freq);
        }
    }

    private boolean isSatisfiedNotifyWifiAvailable(WifiInfo wifiInfo, List<ScanResult> scanResults, List<WifiConfiguration> configNetworks) {
        if (wifiInfo == null || wifiInfo.getRssi() == -127) {
            logW("wifiInfo RSSI is invalid");
            return false;
        } else if (scanResults == null || scanResults.isEmpty()) {
            logW("isSatisfiedNotifyWifiAvailable, WiFi scan results are invalid, getScanResults is null ");
            return false;
        } else if (configNetworks == null || configNetworks.isEmpty()) {
            logW("isSatisfiedNotifyWifiAvailable, WiFi configured networks are invalid, getConfiguredNetworks is null");
            return false;
        } else {
            Bundle result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 18, new Bundle());
            if (result == null || !result.getBoolean("isScanAndManualConnectMode")) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00bb, code lost:
        if ((com.android.server.wifipro.WifiProCommonUtils.getSignalLevel(r1.frequency, r1.level) - r14) < 2) goto L_0x01b5;
     */
    private int notifyGoodApFound() {
        int nextRssi;
        WifiInfo wifiInfo;
        int nextRssi2 = INVALID_RSSI;
        WifiInfo wifiInfo2 = this.mWifiManager.getConnectionInfo();
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (!isSatisfiedNotifyWifiAvailable(wifiInfo2, scanResults, configNetworks)) {
            return INVALID_RSSI;
        }
        WifiConfiguration current = WifiproUtils.getCurrentWifiConfig(this.mWifiManager);
        String currentSsid = WifiProCommonUtils.getCurrentSsid(this.mWifiManager);
        String currentBssid = WifiProCommonUtils.getCurrentBssid(this.mWifiManager);
        String currentEncrypt = current != null ? current.configKey() : null;
        int curRssiLevel = WifiProCommonUtils.getCurrenSignalLevel(wifiInfo2);
        WifiConfiguration preferConfig = getUserPreferredNetwork(currentSsid, configNetworks);
        String nextBssid = null;
        int preferType = 0;
        Iterator<ScanResult> it = scanResults.iterator();
        int nextFreq = -1;
        String nextPreferBssid = null;
        boolean perferFound = false;
        int nextPreferRssi = -200;
        while (true) {
            boolean sameSsid = false;
            if (!it.hasNext()) {
                break;
            }
            ScanResult nextResult = it.next();
            String scanSsid = "\"" + nextResult.SSID + "\"";
            String scanResultEncrypt = nextResult.capabilities;
            boolean sameBssid = currentBssid != null && currentBssid.equals(nextResult.BSSID);
            boolean sameConfigKey = currentSsid != null && currentSsid.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, currentEncrypt);
            if (!sameBssid) {
                if (!sameConfigKey) {
                    wifiInfo = wifiInfo2;
                } else if (!isCurrentWifiOpenOrEapType()) {
                    wifiInfo = wifiInfo2;
                    if (this.mWifiSwitchReason == 5) {
                    }
                } else {
                    wifiInfo = wifiInfo2;
                }
                if (this.mNetworkBlackListManager.isInAbnormalWifiBlacklist(nextResult.BSSID)) {
                    logW("find bssid: " + StringUtilEx.safeDisplayBssid(nextResult.BSSID) + " is in abnormal wifi black list");
                } else {
                    if (preferConfig != null) {
                        if (preferConfig.SSID != null && preferConfig.SSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, preferConfig.configKey())) {
                            sameSsid = true;
                        }
                        if (sameSsid) {
                            int nextResultRssiLevel = WifiProCommonUtils.getSignalLevel(nextResult.frequency, nextResult.level);
                            logI("found USER PREFERED network: ssid=" + StringUtilEx.safeDisplaySsid(preferConfig.SSID) + ", cur rssi=" + nextResult.level + ", pre rssi=" + nextPreferRssi + ", nextResultRssiLevel = " + nextResultRssiLevel + " curRssiLevel = " + curRssiLevel);
                            if (nextResultRssiLevel >= 2 && nextResultRssiLevel - curRssiLevel >= 2 && nextResult.level > nextPreferRssi) {
                                logI("update USER PREFERED network: rssi=" + nextPreferRssi);
                                nextPreferRssi = nextResult.level;
                                String nextPreferBssid2 = nextResult.BSSID;
                                perferFound = true;
                                nextFreq = nextResult.frequency;
                                preferType = 1;
                                nextBssid = nextBssid;
                                wifiInfo2 = wifiInfo;
                                nextPreferBssid = nextPreferBssid2;
                            }
                        }
                    }
                    if (isBetterScanResult(configNetworks, nextResult, nextRssi2)) {
                        nextRssi2 = nextResult.level;
                        nextBssid = nextResult.BSSID;
                        nextFreq = nextResult.frequency;
                    } else {
                        nextBssid = nextBssid;
                    }
                    preferType = preferType;
                    wifiInfo2 = wifiInfo;
                }
            } else {
                wifiInfo = wifiInfo2;
            }
            nextBssid = nextBssid;
            preferType = preferType;
            wifiInfo2 = wifiInfo;
        }
        String nextBssid2 = nextBssid;
        int preferType2 = preferType;
        if (perferFound) {
            logI("final network: bssid=" + StringUtilEx.safeDisplayBssid(nextPreferBssid));
            nextRssi = nextPreferRssi;
            nextBssid2 = nextPreferBssid;
        } else {
            nextRssi = nextRssi2;
            preferType2 = 0;
        }
        notifyWifiAvailableStatus(nextRssi != -200, nextRssi, nextBssid2, preferType2, nextFreq);
        return nextRssi;
    }

    private boolean isBetterScanResult(List<WifiConfiguration> configNetworks, ScanResult nextResult, int nextRssi) {
        if (configNetworks == null || configNetworks.size() == 0) {
            logW("WiFi configured networks are invalid, getConfiguredNetworks is null");
            return false;
        } else if (nextResult == null) {
            return false;
        } else {
            trackGoodApDetailslnfo(configNetworks, nextResult);
            if (HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(nextResult.frequency, nextResult.level) <= 2) {
                return false;
            }
            String scanSsid = "\"" + nextResult.SSID + "\"";
            String scanResultEncrypt = nextResult.capabilities;
            if (nextResult.level > nextRssi) {
                for (WifiConfiguration nextConfig : configNetworks) {
                    int disableReason = nextConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                    if (!(nextConfig.noInternetAccess || nextConfig.portalNetwork || disableReason > 0 || WifiProCommonUtils.isOpenAndPortal(nextConfig) || WifiProCommonUtils.isOpenAndMaybePortal(nextConfig) || nextConfig.SSID == null || !nextConfig.SSID.equals(scanSsid) || !WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, nextConfig.configKey()))) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private boolean handleScanResultsForConnectWiFi() {
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            logW("handleScanResultsForConnectWiFi, WiFi scan results are invalid, getScanResults is null");
            return false;
        }
        List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            logW("handleScanResultsForConnectWiFi, WiFi configured networks are invalid, getConfiguredNetworks is null");
            return false;
        }
        int nextId = -1;
        int nextRssi = INVALID_RSSI;
        boolean found = false;
        WifiConfiguration nextConfig = null;
        for (ScanResult nextResult : scanResults) {
            if (found) {
                break;
            }
            String scanSsid = "\"" + nextResult.SSID + "\"";
            if (nextResult.BSSID.equals(this.mToConnectBssid) && isSatisfiedSignalCondition(nextResult)) {
                Iterator<WifiConfiguration> it = configNetworks.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    nextConfig = it.next();
                    if (nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid)) {
                        nextRssi = nextResult.level;
                        nextId = nextConfig.networkId;
                        found = true;
                        break;
                    }
                }
            }
        }
        logI("handleScanResultsForConnectWiFi, nextId = " + nextId + ", nextRssi = " + nextRssi);
        if (nextId == -1 || isWifiRestricted(nextConfig)) {
            return false;
        }
        this.mNetwoksHandoverState = 2;
        this.mWifiManager.connect(nextId, null);
        return true;
    }

    private boolean isSatisfiedSignalCondition(ScanResult nextResult) {
        if (nextResult == null || HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(nextResult.frequency, nextResult.level) >= 2) {
            return true;
        }
        return false;
    }

    private boolean isWifiRestricted(WifiConfiguration nextConfig) {
        Bundle data = new Bundle();
        data.putParcelable("WifiConfiguration", nextConfig);
        data.putBoolean("isToast", false);
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 11, data);
        boolean isWifiRestricted = false;
        if (result != null) {
            isWifiRestricted = result.getBoolean("isWifiRestricted");
        }
        if (!isWifiRestricted) {
            return false;
        }
        Log.w(TAG, "MDM deny connect!");
        return true;
    }

    private boolean isTargetApInWifiBlacklist(ArrayList<String> switchBlacklist, int signalLevel, int currentLevel, String bssid) {
        if (switchBlacklist == null || switchBlacklist.isEmpty() || bssid == null) {
            return false;
        }
        int count = switchBlacklist.size();
        for (int i = 0; i < count; i++) {
            if (bssid.equals(switchBlacklist.get(i)) && (signalLevel <= 3 || signalLevel - currentLevel < 2)) {
                if (this.mIsWiFiInternet || signalLevel < 3) {
                    return true;
                } else {
                    logW("ignore blacklist limit when current ap has no internet and target ap rssi level >= 3");
                    return false;
                }
            }
        }
        return false;
    }

    private boolean invalidConfigNetwork(WifiConfiguration config, int signalLevel, int currentLevel, String bssid) {
        if (this.mNetwoksHandoverState != 4 || !isTargetApInWifiBlacklist(this.mSwitchBlacklist, signalLevel, currentLevel, bssid)) {
            int disableReason = config.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
            if (disableReason >= 2 && disableReason <= 9) {
                logW("selectQualifiedCandidate, wifi switch, ssid = " + StringUtilEx.safeDisplaySsid(config.SSID) + ", disableReason = " + disableReason);
                return true;
            } else if (WifiProCommonUtils.isOpenAndPortal(config) || WifiProCommonUtils.isOpenAndMaybePortal(config)) {
                HwHiLog.w(TAG, false, "ignore the open portal network known", new Object[0]);
                return true;
            } else if (WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 103)) {
                logW("ignore the network that has no internet history" + config.internetHistory);
                return true;
            } else if (!config.noInternetAccess && !config.portalNetwork) {
                return false;
            } else {
                HwHiLog.d(TAG, false, "ignore the network has no internet, ssid=%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID)});
                return true;
            }
        } else {
            logW("selectQualifiedCandidate, switch black list filter it, bssid = " + StringUtilEx.safeDisplayBssid(bssid));
            return true;
        }
    }

    private WifiSwitchCandidate getBetterSignalCandidate(WifiSwitchCandidate last, WifiSwitchCandidate current) {
        if (current == null || current.bestScanResult == null || (last != null && last.bestScanResult != null && last.bestScanResult.level >= current.bestScanResult.level)) {
            return last;
        }
        return current;
    }

    private WifiSwitchCandidate getBackupSwitchCandidate(WifiConfiguration currentConfig, ScanResult currentResult, WifiSwitchCandidate lastCandidate) {
        if (this.mNetwoksHandoverState == 4 && currentConfig.noInternetAccess && WifiProCommonUtils.allowWifiConfigRecovery(currentConfig.internetHistory)) {
            return getBetterSignalCandidate(lastCandidate, new WifiSwitchCandidate(currentConfig, currentResult));
        }
        HwHiLog.w(TAG, false, "BackupSwitchCandidate is null", new Object[0]);
        return null;
    }

    private WifiSwitchCandidate getBestSwitchCandidate(WifiConfiguration currentConfig, ScanResult currentResult, WifiSwitchCandidate lastCandidate) {
        if (currentConfig == null || currentResult == null) {
            HwHiLog.d(TAG, false, "currentConfig or currentResult = null", new Object[0]);
            return lastCandidate;
        } else if (lastCandidate == null || lastCandidate.bestWifiConfig == null || lastCandidate.bestScanResult == null) {
            HwHiLog.d(TAG, false, "lastCandidate or its bestWifiConfig or its bestScanResult  = null", new Object[0]);
            return new WifiSwitchCandidate(currentConfig, currentResult);
        } else if (this.mNetwoksHandoverState != 0 || SystemClock.elapsedRealtime() - (currentResult.timestamp / 1000) <= 200) {
            ScanResult lastResult = lastCandidate.bestScanResult;
            int currentScore = WifiProCommonUtils.calculateScore(lastResult);
            int newScore = WifiProCommonUtils.calculateScore(currentResult);
            dumpCandidateScore(newScore, currentScore, currentResult);
            if (newScore > currentScore || (newScore == currentScore && currentResult.level > lastResult.level)) {
                return new WifiSwitchCandidate(currentConfig, currentResult);
            }
            return lastCandidate;
        } else {
            HwHiLog.d(TAG, false, "skip the scan results if they are not found for this scan", new Object[0]);
            return lastCandidate;
        }
    }

    private void dumpCandidateScore(int newScore, int currentScore, ScanResult scanResult) {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null || scanResult == null) {
            HwHiLog.d(TAG, false, "mWifiManager is null", new Object[0]);
        } else if (wifiManager.getVerboseLoggingLevel() > 0) {
            HwHiLog.i(TAG, false, "BSSID = %{public}s, is5g = %{public}s, supportedWifiCategory = %{public}d, rssi = %{public}d, newScore = %{public}d, currentScore= %{public}d", new Object[]{StringUtilEx.safeDisplayBssid(scanResult.BSSID), String.valueOf(ScanResult.is5GHz(scanResult.frequency)), Integer.valueOf(scanResult.supportedWifiCategory), Integer.valueOf(scanResult.level), Integer.valueOf(newScore), Integer.valueOf(currentScore)});
        }
    }

    private WifiSwitchCandidate getAutoRoamCandidate(int currentSignalRssi, WifiSwitchCandidate bestCandidate) {
        if (!(this.mNetwoksHandoverState != 0 || bestCandidate == null || bestCandidate.bestScanResult == null)) {
            if (HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(bestCandidate.bestScanResult.frequency, bestCandidate.bestScanResult.level) - WifiProCommonUtils.getCurrenSignalLevel(this.mWifiManager.getConnectionInfo()) < 2 || bestCandidate.bestScanResult.level - currentSignalRssi < 10) {
                HwHiLog.w(TAG, false, "AutoRoamCandidate is null", new Object[0]);
                return null;
            }
        }
        return bestCandidate;
    }

    public void updateWiFiInternetAccess(boolean isWiFiInternet) {
        this.mIsWiFiInternet = isWiFiInternet;
    }

    public void clearWiFiSameSsidSwitchFlag() {
        this.mIsWiFiSameSsidSwitch.set(false);
    }

    public boolean isWiFiSameSsidSwitching() {
        return this.mIsWiFiSameSsidSwitch.get();
    }

    private boolean isNetworkSelectionAvailable(List<ScanResult> scanResults, WifiConfiguration currentConfiguration, List<WifiConfiguration> configNetworks) {
        if (scanResults == null || scanResults.size() == 0) {
            logW("isNetworkSelectionAvailable, get scan result invalid!");
            return false;
        } else if (currentConfiguration == null || currentConfiguration.SSID == null) {
            logW("isNetworkSelectionAvailable, current connected wifi configuration is null");
            return false;
        } else if (configNetworks != null && configNetworks.size() != 0) {
            return true;
        } else {
            logW("isNetworkSelectionAvailable, WiFi configured networks are invalid, getConfiguredNetworks is null");
            return false;
        }
    }

    private WifiSwitchCandidate getBetterWifiCandidate(WifiSwitchCandidate switchCandidate, int currentLevel) {
        int targetLevel = INVALID_RSSI;
        int targetRssi = INVALID_RSSI;
        String targetBssid = null;
        WifiSwitchCandidate betterWifiCandidate = switchCandidate;
        if (betterWifiCandidate == null || !this.mIsWiFiInternet) {
            logI("getBetterWifiCandidate invalid mIsWiFiInternet = " + this.mIsWiFiInternet);
            return betterWifiCandidate;
        }
        ScanResult targetScanResult = betterWifiCandidate.getScanResult();
        if (targetScanResult != null) {
            if (isNetworkMatched(betterWifiCandidate.getWifiConfig(), "\"" + targetScanResult.SSID + "\"", targetScanResult.capabilities)) {
                targetRssi = targetScanResult.level;
                targetBssid = targetScanResult.BSSID;
                targetLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(targetScanResult.frequency, targetScanResult.level);
                logI("targetRssi=" + targetRssi + " targetLevel=" + targetLevel + " targetBssid=" + StringUtilEx.safeDisplayBssid(targetBssid) + " scanSsid=" + StringUtilEx.safeDisplaySsid(targetScanResult.SSID));
            }
        }
        if ((currentLevel == 3 && targetLevel >= 3) || (currentLevel == 4 && targetLevel == 4)) {
            WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
            apInfo.setApBssid(targetBssid);
            if (betterWifiCandidate.getWifiConfig() == null) {
                return null;
            }
            apInfo.setEstimateApSsid(betterWifiCandidate.getWifiConfig().SSID);
            apInfo.setApRssi(targetRssi);
            this.mNetworkQosMonitor.getApHistoryQualityScoreForWifi2Wifi(apInfo);
            logI("WiFi2WiFi targetAp:HistoryScore " + apInfo.getRetHistoryScore());
            if (apInfo.getRetHistoryScore() < 40) {
                betterWifiCandidate = null;
            }
        }
        if (currentLevel != 4 || targetLevel > 3) {
            return betterWifiCandidate;
        }
        logI("currentLevel is level_4 and targetlevel <= level_3");
        return null;
    }

    private WifiSwitchCandidate selectQualifiedCandidate() {
        WifiSwitchCandidate bestSwitchCandidate;
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        WifiConfiguration current = WifiproUtils.getCurrentWifiConfig(this.mWifiManager);
        List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (!isNetworkSelectionAvailable(scanResults, current, configNetworks)) {
            return null;
        }
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        int currentLevel = WifiProCommonUtils.getCurrenSignalLevel(wifiInfo);
        String currentSsid = current.SSID;
        String scanSsid = WifiProCommonUtils.getCurrentBssid(this.mWifiManager);
        String currentEncrypt = current.configKey();
        WifiConfiguration preferConfig = getUserPreferredNetwork(currentSsid, configNetworks);
        String str = TAG;
        if (preferConfig == null && this.mNetwoksHandoverState == 0 && currentLevel >= 4) {
            HwHiLog.w(str, false, "CurrentLevel >= 4, QualifiedCandidate is null", new Object[0]);
            return null;
        }
        WifiSwitchCandidate backupSwitchCandidate = null;
        boolean perferFound = false;
        WifiSwitchCandidate preferSwitchCandidate = null;
        int nextPreferRssi = -200;
        WifiSwitchCandidate bestSwitchCandidate2 = null;
        for (ScanResult nextResult : scanResults) {
            String scanSsid2 = "\"" + nextResult.SSID + "\"";
            String scanBssid = nextResult.BSSID;
            String scanResultEncrypt = nextResult.capabilities;
            WifiSwitchCandidate backupSwitchCandidate2 = backupSwitchCandidate;
            if (isScanResultSkip(nextResult, currentSsid, scanSsid, currentEncrypt, currentLevel)) {
                nextPreferRssi = nextPreferRssi;
                bestSwitchCandidate2 = bestSwitchCandidate2;
                str = str;
                currentEncrypt = currentEncrypt;
                current = current;
                currentSsid = currentSsid;
                scanResults = scanResults;
                wifiInfo = wifiInfo;
                backupSwitchCandidate = backupSwitchCandidate2;
                scanSsid = scanSsid;
            } else {
                int signalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(nextResult.frequency, nextResult.level);
                if (preferConfig != null) {
                    logI("scan: " + StringUtilEx.safeDisplaySsid(nextResult.SSID) + ", prefer: " + StringUtilEx.safeDisplaySsid(preferConfig.SSID));
                    if (isNetworkMatched(preferConfig, scanSsid2, scanResultEncrypt) && !invalidConfigNetwork(preferConfig, signalLevel, currentLevel, scanBssid)) {
                        logI("found USER PREFERED network: ssid=" + StringUtilEx.safeDisplaySsid(preferConfig.SSID) + ", cur rssi=" + nextResult.level + ", pre rssi=" + nextPreferRssi);
                        if (nextResult.level > nextPreferRssi) {
                            logI("update USER PREFERED network: rssi=" + nextPreferRssi);
                            int nextPreferRssi2 = nextResult.level;
                            preferSwitchCandidate = new WifiSwitchCandidate(preferConfig, nextResult);
                            perferFound = true;
                            nextPreferRssi = nextPreferRssi2;
                            bestSwitchCandidate2 = bestSwitchCandidate2;
                            str = str;
                            currentEncrypt = currentEncrypt;
                            current = current;
                            currentSsid = currentSsid;
                            scanResults = scanResults;
                            wifiInfo = wifiInfo;
                            backupSwitchCandidate = backupSwitchCandidate2;
                            scanSsid = scanSsid;
                        }
                    }
                }
                Iterator<WifiConfiguration> it = configNetworks.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        backupSwitchCandidate = backupSwitchCandidate2;
                        bestSwitchCandidate2 = bestSwitchCandidate2;
                        break;
                    }
                    WifiConfiguration nextConfig = it.next();
                    if (isNetworkMatched(nextConfig, scanSsid2, scanResultEncrypt) && !invalidConfigNetwork(nextConfig, signalLevel, currentLevel, scanBssid)) {
                        backupSwitchCandidate = getBackupSwitchCandidate(nextConfig, nextResult, backupSwitchCandidate2);
                        bestSwitchCandidate2 = getBestSwitchCandidate(nextConfig, nextResult, bestSwitchCandidate2);
                        break;
                    }
                    backupSwitchCandidate2 = backupSwitchCandidate2;
                }
                nextPreferRssi = nextPreferRssi;
                str = str;
                currentEncrypt = currentEncrypt;
                current = current;
                currentSsid = currentSsid;
                scanResults = scanResults;
                wifiInfo = wifiInfo;
                scanSsid = scanSsid;
            }
        }
        if (bestSwitchCandidate2 != null || backupSwitchCandidate == null) {
            bestSwitchCandidate = bestSwitchCandidate2;
        } else {
            HwHiLog.d(str, false, "Wifi Swtich: try to use the backup one to connect if no other choice.", new Object[0]);
            bestSwitchCandidate = backupSwitchCandidate;
        }
        if (perferFound && preferSwitchCandidate != null) {
            bestSwitchCandidate = preferSwitchCandidate;
            logI("final network: ssid=" + StringUtilEx.safeDisplaySsid(bestSwitchCandidate.getWifiConfig().SSID));
        }
        return getBetterWifiCandidate(getAutoRoamCandidate(WifiProCommonUtils.getCurrentRssi(this.mWifiManager), bestSwitchCandidate), currentLevel);
    }

    private boolean isNetworkMatched(WifiConfiguration nextConfig, String scanSsid, String scanResultEncrypt) {
        if (nextConfig == null || nextConfig.SSID == null) {
            HwHiLog.w(TAG, false, "nextConfig or nextConfig.SSID is null", new Object[0]);
            return false;
        } else if (scanSsid == null || scanResultEncrypt == null) {
            HwHiLog.w(TAG, false, "scanSsid or scanResultEncrypt is null", new Object[0]);
            return false;
        } else {
            boolean isSameSsid = nextConfig.SSID.equals(scanSsid);
            boolean isSameEncryptType = WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, nextConfig.configKey());
            if (isSameSsid) {
                HwHiLog.w(TAG, false, "isSameEncryptType = %{public}s", new Object[]{Boolean.valueOf(isSameEncryptType)});
            }
            if (!isSameSsid || !isSameEncryptType) {
                return false;
            }
            HwHiLog.w(TAG, false, "find out the Confignetwork from the scanresults", new Object[0]);
            return true;
        }
    }

    private boolean isCurrentWifiOpenOrEapType() {
        WifiConfiguration currentConfig = WifiproUtils.getCurrentWifiConfig(this.mWifiManager);
        if (currentConfig == null) {
            return false;
        }
        if (currentConfig.isOpenNetwork() || currentConfig.isEnterprise()) {
            return true;
        }
        return false;
    }

    private boolean isScanResultSkip(ScanResult nextResult, String currentSsid, String currentBssid, String currentEncrypt, int currentLevel) {
        int signalLevel;
        boolean z;
        int i;
        if (nextResult == null) {
            return true;
        }
        if (currentSsid == null) {
            return true;
        }
        if (currentBssid == null) {
            return true;
        }
        if (currentEncrypt == null) {
            return true;
        }
        boolean isCastOptWorking = WifiproUtils.isCastOptWorking();
        int p2pFreq = WifiproUtils.getP2pFrequency();
        if ((isCastOptWorking && ScanResult.is5GHz(nextResult.frequency) && nextResult.frequency != p2pFreq) || (signalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(nextResult.frequency, nextResult.level)) <= 2) {
            return true;
        }
        String scanSsid = "\"" + nextResult.SSID + "\"";
        String scanResultEncrypt = nextResult.capabilities;
        boolean bssidSame = currentBssid.equals(nextResult.BSSID);
        boolean isSameSsidAndEncryptType = currentSsid.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, currentEncrypt);
        boolean switchConfigKeySame = this.mNetwoksHandoverState == 4 && isSameSsidAndEncryptType;
        boolean idleConfigKeyDiff = this.mNetwoksHandoverState == 0 && !isSameSsidAndEncryptType;
        if (bssidSame) {
            return true;
        }
        if (idleConfigKeyDiff) {
            return true;
        }
        if (!switchConfigKeySame) {
            z = true;
        } else if (isCurrentWifiOpenOrEapType() || (i = this.mWifiSwitchReason) == 1 || i == 3 || ((i == 4 || i == 5) && signalLevel - currentLevel < 2)) {
            logI("same ssid wifi handover is not allowed, detail reason mWifiSwitchReason = " + this.mWifiSwitchReason + ", isCurrentWifiOpenOrEapType = " + isCurrentWifiOpenOrEapType());
            return true;
        } else {
            z = true;
        }
        if (switchConfigKeySame && !isFwkRoamAllowed()) {
            return z;
        }
        if (!this.mNetworkBlackListManager.isInAbnormalWifiBlacklist(nextResult.BSSID)) {
            return false;
        }
        logW("selectQualifiedCandidate, scanBssid" + StringUtilEx.safeDisplayBssid(currentBssid) + " is in black list!");
        return true;
    }

    private boolean isFwkRoamAllowed() {
        int roamRssiThreshold;
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            logE("isFwkRoamAllowed wifiInfo is null");
            return false;
        }
        int currentRssi = wifiInfo.getRssi();
        if (wifiInfo.is5GHz()) {
            roamRssiThreshold = HISI_CHIP_ROAM_5G_RSSI_THRESHOLD;
        } else {
            roamRssiThreshold = HISI_CHIP_ROAM_24G_RSSI_THRESHOLD;
        }
        if (currentRssi > roamRssiThreshold) {
            return true;
        }
        logI("same ssid wifi handover is not allowed, detail reason mWifiSwitchReason = " + this.mWifiSwitchReason + ", roamRssiThreshold = " + roamRssiThreshold + ", currentRssi is " + currentRssi);
        return false;
    }

    private WifiConfiguration getUserPreferredNetwork(String currentSsid, List<WifiConfiguration> configNetworks) {
        Bundle result;
        HashMap<Integer, String> preferList;
        if ((!WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 24, new Bundle()).getBoolean("isHwArbitrationManagerNotNull")) || (result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 25, new Bundle())) == null || (preferList = (HashMap) result.getSerializable("preferList")) == null || !preferList.containsKey(0)) {
            return null;
        }
        for (WifiConfiguration nextConfig : configNetworks) {
            if (nextConfig.SSID != null && nextConfig.SSID.equals(preferList.get(0))) {
                logI("found user preferred network in configNW");
                int disableReason = nextConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                if (!nextConfig.noInternetAccess && !nextConfig.portalNetwork && disableReason <= 0 && !WifiProCommonUtils.isOpenAndPortal(nextConfig) && !WifiProCommonUtils.isOpenAndMaybePortal(nextConfig)) {
                    logI("found avalible user preferred network: " + StringUtilEx.safeDisplaySsid(nextConfig.SSID));
                    return nextConfig;
                }
            }
        }
        return null;
    }

    private void tryWiFiSwitchBetweenSameSsid(WifiSwitchCandidate switchCandidate) {
        if (switchCandidate == null) {
            logW("tryWiFiSwitchBetweenSameSsid: switchCandidate is null");
            return;
        }
        ScanResult scanResult = switchCandidate.getScanResult();
        WifiConfiguration wifiConfig = switchCandidate.getWifiConfig();
        if (scanResult == null || wifiConfig == null) {
            logW("tryWiFiSwitchBetweenSameSsid: scanResult or wifiConfig is null");
            return;
        }
        String bssid = scanResult.BSSID;
        logI("tryWiFiSwitchBetweenSameSsid: roam by FWK, bssid = " + StringUtilEx.safeDisplayBssid(bssid));
        if (!TextUtils.isEmpty(bssid)) {
            this.mTargetBssid = bssid;
            Bundle connectTypeBundle = new Bundle();
            connectTypeBundle.putString("WIFIPRO_CONNECT", "WIFIPRO_CONNECT");
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 15, connectTypeBundle);
            updateWifiSwitchTimeStamp(System.currentTimeMillis());
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 21, new Bundle());
            Bundle roamNetworkBundle = new Bundle();
            roamNetworkBundle.putInt("networkId", wifiConfig.networkId);
            roamNetworkBundle.putParcelable("ScanResult", scanResult);
            this.mIsWiFiSameSsidSwitch.set(true);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 20, roamNetworkBundle);
        }
    }

    private void trySwitchWifiNetwork(WifiSwitchCandidate switchCandidate) {
        WifiConfiguration current = WifiproUtils.getCurrentWifiConfig(this.mWifiManager);
        if (current == null || switchCandidate == null) {
            logW("trySwitchWifiNetwork# CurrentWifiConfig is null or switchCandidate is null!!");
            if (this.mNetwoksHandoverState == 4) {
                sendNetworkHandoverResult(this.mNetwoksHandoverType, false, null, 22);
            }
            this.mNetwoksHandoverType = -1;
            this.mNetwoksHandoverState = 0;
            this.mOldConfigKey = null;
            this.mSwitchBlacklist = null;
            return;
        }
        WifiConfiguration best = switchCandidate.getWifiConfig();
        if (this.mNetwoksHandoverState == 4) {
            this.mNetwoksHandoverState = 3;
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 19, new Bundle());
        }
        if (current.networkId == best.networkId) {
            tryWiFiSwitchBetweenSameSsid(switchCandidate);
        } else if (this.mNetwoksHandoverState == 3) {
            logI("trySwitchWifiNetwork: Reconnect from " + StringUtilEx.safeDisplaySsid(current.SSID) + " to " + StringUtilEx.safeDisplaySsid(best.SSID));
            String bssid = switchCandidate.getScanResult().BSSID;
            if (!TextUtils.isEmpty(bssid)) {
                this.mTargetBssid = bssid;
                Bundle connectTypeBundle = new Bundle();
                connectTypeBundle.putString("WIFIPRO_CONNECT", "WIFIPRO_CONNECT");
                WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 15, connectTypeBundle);
                updateWifiSwitchTimeStamp(System.currentTimeMillis());
                WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 21, new Bundle());
                Bundle connectNetworkBundle = new Bundle();
                connectNetworkBundle.putInt("networkId", best.networkId);
                connectNetworkBundle.putInt("CallingUid", Binder.getCallingUid());
                connectNetworkBundle.putString("bssid", bssid);
                WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 22, connectNetworkBundle);
            }
        }
    }

    private void updateWifiSwitchTimeStamp(long ts) {
        WifiConfiguration config = WifiproUtils.getCurrentWifiConfig(this.mWifiManager);
        if (config != null && ts > 0) {
            config.lastTrySwitchWifiTimestamp = ts;
            Bundle data = new Bundle();
            data.putParcelable("WifiConfiguration", config);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 16, data);
        }
    }

    public boolean handleWifiToWifi(ArrayList<String> invalidNetworks, int threshold, int qosLevel, int switchReason) {
        if (invalidNetworks == null) {
            logW("handleWifiToWifi, inputed arg is invalid, invalidNetworks is null");
            return false;
        }
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 18, new Bundle());
        boolean isScanAndManualConnectMode = false;
        if (result != null) {
            isScanAndManualConnectMode = result.getBoolean("isScanAndManualConnectMode");
        }
        if (isScanAndManualConnectMode) {
            logW("Only allow Manual Connection, ignore auto connection.");
            return false;
        }
        this.mSwitchBlacklist = (ArrayList) invalidNetworks.clone();
        WifiConfiguration currConfig = WifiproUtils.getCurrentWifiConfig(this.mWifiManager);
        if (currConfig == null || currConfig.configKey() == null) {
            logW("handleWifiToWifi, getCurrentWifiConfig is null.");
            return false;
        }
        List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            logW("handleWifiToWifi, WiFi configured networks are invalid, getConfiguredNetworks is null");
            return false;
        }
        this.mOldConfigKey = currConfig.configKey();
        this.mNetwoksHandoverType = 1;
        this.mNetwoksHandoverState = 4;
        Handler handler = this.mNetworkHandler;
        handler.sendMessageDelayed(Message.obtain(handler, 7), HANDOVER_WAIT_SCAN_TIME_OUT);
        requestScan();
        WifiProChrUploadManager.uploadDisconnectedEvent(EVENT_SSID_SWITCH);
        this.mWifiSwitchReason = switchReason;
        logI("handleWifiToWifi mWifiSwitchReason = " + this.mWifiSwitchReason);
        if (this.uploadManager != null) {
            Bundle ssidSwitch = new Bundle();
            ssidSwitch.putInt("index", 0);
            this.uploadManager.addChrBundleStat("wifiSwitchCntEvent", "wifiSwitchCnt", ssidSwitch);
        }
        return true;
    }

    public boolean hasAvailableWifiNetwork(List<String> invalidNetworks, int threshold, String currBssid, String currSSid) {
        logI("hasAvailableWifiNetwork, invalidNetworks = " + invalidNetworks + ", threshold = " + threshold + ", currSSid = " + StringUtilEx.safeDisplaySsid(currSSid));
        List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            logW("hasAvailableWifiNetwork, WiFi configured networks are invalid, getConfiguredNetworks is null");
            return false;
        }
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        requestScan();
        return true;
    }

    private void requestScan() {
        if (this.mContext != null) {
            Bundle data = new Bundle();
            data.putInt("CallingUid", Binder.getCallingUid());
            data.putString("packageName", this.mContext.getOpPackageName());
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 23, data);
        }
    }

    public boolean connectWifiNetwork(String bssid) {
        if (bssid == null || bssid.length() == 0) {
            logW("connectWifiNetwork, inputed arg is invalid");
            return false;
        }
        WifiInfo currWifiInfo = this.mWifiManager.getConnectionInfo();
        if (currWifiInfo == null || !bssid.equals(currWifiInfo.getBSSID())) {
            List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                logW("connectWifiNetwork, WiFi configured networks are invalid, getConfiguredNetworks is null");
                return false;
            }
            this.mToConnectBssid = bssid;
            this.mNetwoksHandoverType = 2;
            this.mNetwoksHandoverState = 1;
            requestScan();
            return true;
        }
        logW("connectWifiNetwork, already connected, ignore it.");
        return true;
    }

    public int handleDualBandWifiConnect(String bssid, String ssid, int authType, int switchType) {
        HashMap<Integer, String> preferList;
        Log.d(TAG, "DualBandWifiConnect, ssid = " + StringUtilEx.safeDisplaySsid(ssid) + ", authType = " + authType + ", switchType = " + switchType);
        if (bssid == null || ssid == null) {
            Log.d(TAG, "DualBandWifiConnect, inputed arg is invalid, ssid is null ");
            return 1;
        } else if (switchType < 1 || switchType > 3) {
            Log.d(TAG, "DualBandWifiConnect, inputed arg is invalid, switchType = " + switchType);
            return 1;
        } else if (isBssidConnected(this.mWifiManager.getConnectionInfo(), bssid)) {
            Log.d(TAG, "DualBandWifiConnect, already connected, ignore it.");
            return 10;
        } else {
            List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                Log.d(TAG, "DualBandWifiConnect, WiFi configured networks are invalid, getConfiguredNetworks is null");
                return 1;
            }
            WifiConfiguration changeConfig = null;
            Iterator<WifiConfiguration> it = configNetworks.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                WifiConfiguration nextConfig = it.next();
                if (isValidConfig(nextConfig) && ssid.equals(nextConfig.SSID) && authType == nextConfig.getAuthType()) {
                    changeConfig = nextConfig;
                    break;
                }
            }
            if (changeConfig == null) {
                Log.d(TAG, "DualBandWifiConnect, WifiConfiguration is null ");
                HwDualBandInformationManager mManager = HwDualBandInformationManager.getInstance();
                if (mManager != null) {
                    mManager.deleteDualbandApInfoBySsid(ssid, authType);
                }
                return 1;
            }
            if (!(!WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 24, new Bundle()).getBoolean("isHwArbitrationManagerNotNull"))) {
                Bundle result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 25, new Bundle());
                if (result == null || (preferList = (HashMap) result.getSerializable("preferList")) == null) {
                    return 1;
                }
                logI("getWifiPreferenceFromHiData: " + preferList.toString());
                if (preferList.containsKey(0) && !changeConfig.SSID.equals(preferList.get(0))) {
                    Log.d(TAG, "DualBandWifiConnect, found user preference but not target ssid, ignore it.");
                    return 11;
                }
            }
            sendDualBandHandOverBroadcast(bssid, switchType, changeConfig);
            return 0;
        }
    }

    private void sendDualBandHandOverBroadcast(String bssid, int switchType, WifiConfiguration changeConfig) {
        Log.d(TAG, "DualBandWifiConnect, changeConfig.configKey = " + changeConfig.configKey() + ", AuthType = " + changeConfig.getAuthType());
        changeConfig.BSSID = bssid;
        this.mToConnectDualbandBssid = bssid;
        this.mToConnectConfigKey = changeConfig.configKey();
        this.mNetwoksHandoverType = 4;
        this.mNetwoksHandoverState = 5;
        if (switchType == 1) {
            updateWifiSwitchTimeStamp(System.currentTimeMillis());
        }
        Intent intent = new Intent(ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(WIFI_HANDOVER_NETWORK_WIFICONFIG, changeConfig);
        intent.putExtras(mBundle);
        intent.putExtra(WIFI_HANDOVER_NETWORK_SWITCHTYPE, switchType);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WIFI_HANDOVER_RECV_PERMISSION);
    }

    public int getNetwoksHandoverType() {
        return this.mNetwoksHandoverType;
    }

    private boolean isValidConfig(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        int cc = config.allowedKeyManagement.cardinality();
        Log.e(TAG, "config isValid cardinality=" + cc);
        if (cc <= 1) {
            return true;
        }
        return false;
    }

    private boolean isBssidConnected(WifiInfo currWifiInfo, String bssid) {
        if (currWifiInfo == null || bssid == null) {
            return false;
        }
        String curBssid = currWifiInfo.getBSSID();
        if (curBssid != null) {
            return curBssid.equals(bssid);
        }
        Log.e(TAG, "curBssid is null, return.");
        return false;
    }

    /* access modifiers changed from: private */
    public static class WifiSwitchCandidate {
        private ScanResult bestScanResult;
        private WifiConfiguration bestWifiConfig;

        public WifiSwitchCandidate(WifiConfiguration bestWifiConfig2, ScanResult bestScanResult2) {
            this.bestWifiConfig = bestWifiConfig2;
            this.bestScanResult = bestScanResult2;
        }

        public WifiConfiguration getWifiConfig() {
            return this.bestWifiConfig;
        }

        public ScanResult getScanResult() {
            return this.bestScanResult;
        }
    }

    private void trackGoodApDetailslnfo(List<WifiConfiguration> configNetworks, ScanResult nextResult) {
        if (nextResult == null) {
            logW("trackGoodApDetailslnfo nextResult is null");
            return;
        }
        String scanSsid = "\"" + nextResult.SSID + "\"";
        String scanResultEncrypt = nextResult.capabilities;
        for (WifiConfiguration nextConfig : configNetworks) {
            if (nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, nextConfig.configKey())) {
                int signalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(nextResult.frequency, nextResult.level);
                int disableReason = nextConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                boolean isPortalNetwork = nextConfig.portalNetwork;
                boolean isOpenAndPortal = WifiProCommonUtils.isOpenAndPortal(nextConfig);
                boolean isOpenAndMaybePortal = WifiProCommonUtils.isOpenAndMaybePortal(nextConfig);
                StringBuilder sb = new StringBuilder();
                sb.append("trackGoodApDetailslnfo SSID = ");
                sb.append(StringUtilEx.safeDisplaySsid(nextResult.SSID));
                sb.append(" BSSID = ");
                sb.append(StringUtilEx.safeDisplayBssid(nextResult.BSSID));
                sb.append(" signalLevel = ");
                sb.append(signalLevel);
                sb.append(" disableReason = ");
                sb.append(disableReason);
                sb.append(" hasInternetAccess = ");
                sb.append(!nextConfig.noInternetAccess);
                sb.append(" isPortalNetwork = ");
                sb.append(isPortalNetwork);
                sb.append(" isOpenAndPortal = ");
                sb.append(isOpenAndPortal);
                sb.append(" isOpenAndMaybePortal = ");
                sb.append(isOpenAndMaybePortal);
                sb.append(" internetHistory = ");
                sb.append(nextConfig.internetHistory);
                sb.append(" rssi = ");
                sb.append(nextResult.level);
                logW(sb.toString());
            }
        }
    }

    private static void logD(String msg) {
        Log.d(TAG, msg);
    }

    /* access modifiers changed from: private */
    public static void logI(String msg) {
        Log.i(TAG, msg);
    }

    private static void logW(String msg) {
        Log.w(TAG, msg);
    }

    private static void logE(String msg) {
        Log.e(TAG, msg);
    }
}
