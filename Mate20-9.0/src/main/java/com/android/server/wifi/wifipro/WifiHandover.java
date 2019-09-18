package com.android.server.wifi.wifipro;

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
import com.android.server.hidata.arbitration.HwArbitrationManager;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.ScanRequestProxy;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class WifiHandover {
    public static final String ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER = "com.huawei.wifi.action.REQUEST_DUAL_BAND_WIFI_HANDOVER";
    public static final String ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER = "com.huawei.wifi.action.RESPONSE_DUAL_BAND_WIFI_HANDOVER";
    public static final String ACTION_RESPONSE_WIFI_2_WIFI = "com.huawei.wifi.action.RESPONSE_WIFI_2_WIFI";
    private static final int CURRENT_STATE_IDLE = 0;
    private static final int CURRENT_STATE_WAITING_CONNECTION_COMPLETED = 2;
    private static final int CURRENT_STATE_WAITING_SCAN_RESULTS_FOR_CONNECT_WIFI = 1;
    private static final int CURRENT_STATE_WAITING_SCAN_RESULTS_FOR_WIFI_SWITCH = 4;
    private static final int CURRENT_STATE_WAITING_WIFI_2_WIFI_COMPLETED = 3;
    private static final int HANDLER_CMD_NOTIFY_GOOD_AP = 4;
    private static final int HANDLER_CMD_REQUEST_SCAN_TIME_OUT = 7;
    private static final int HANDLER_CMD_SCAN_RESULTS = 1;
    private static final int HANDLER_CMD_WIFI_2_WIFI = 3;
    private static final int HANDLER_CMD_WIFI_CONNECTED = 2;
    private static final int HANDLER_CMD_WIFI_DISCONNECTED = 6;
    private static final int HANDOVER_MIN_LEVEL_INTERVAL = 2;
    private static final int HANDOVER_STATE_WAITING_DUAL_BAND_WIFI_CONNECT = 5;
    public static final int HANDOVER_STATUS_CONNECT_AUTH_FAILED = -7;
    public static final int HANDOVER_STATUS_CONNECT_REJECT_FAILED = -6;
    public static final int HANDOVER_STATUS_DISALLOWED = -4;
    public static final int HANDOVER_STATUS_OK = 0;
    public static final long HANDOVER_WAIT_SCAN_TIME_OUT = 4000;
    public static final int INVALID_RSSI = -200;
    private static final int NETWORK_HANDLER_CMD_DUAL_BAND_WIFI_CONNECT = 5;
    public static final int NETWORK_HANDOVER_TYPE_CONNECT_SPECIFIC_WIFI = 2;
    public static final int NETWORK_HANDOVER_TYPE_DUAL_BAND_WIFI_CONNECT = 4;
    public static final int NETWORK_HANDOVER_TYPE_UNKNOWN = -1;
    public static final int NETWORK_HANDOVER_TYPE_WIFI_TO_WIFI = 1;
    private static final int SIGNAL_LEVEL_3 = 3;
    public static final String TAG = "WifiHandover";
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
    private BroadcastReceiver mBroadcastReceiver;
    private INetworksHandoverCallBack mCallBack;
    private Context mContext = null;
    private HwArbitrationManager mHwArbitrationManager = null;
    private HwWifiCHRService mHwWifiCHRService;
    private IntentFilter mIntentFilter;
    /* access modifiers changed from: private */
    public int mNetwoksHandoverState;
    /* access modifiers changed from: private */
    public int mNetwoksHandoverType;
    /* access modifiers changed from: private */
    public NetworkBlackListManager mNetworkBlackListManager = null;
    /* access modifiers changed from: private */
    public Handler mNetworkHandler;
    /* access modifiers changed from: private */
    public NetworkQosMonitor mNetworkQosMonitor;
    /* access modifiers changed from: private */
    public String mOldConfigKey = null;
    /* access modifiers changed from: private */
    public ArrayList<String> mSwitchBlacklist;
    /* access modifiers changed from: private */
    public String mTargetBssid = null;
    /* access modifiers changed from: private */
    public String mToConnectBssid;
    /* access modifiers changed from: private */
    public String mToConnectConfigKey;
    /* access modifiers changed from: private */
    public String mToConnectDualbandBssid = null;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    private WifiStateMachine mWifiStateMachine;

    private static class WifiSwitchCandidate {
        /* access modifiers changed from: private */
        public ScanResult bestScanResult;
        /* access modifiers changed from: private */
        public WifiConfiguration bestWifiConfig;

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

    public WifiHandover(Context context, INetworksHandoverCallBack callBack) {
        this.mCallBack = callBack;
        this.mContext = context;
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        this.mWifiStateMachine = WifiInjector.getInstance().getWifiStateMachine();
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
        this.mCallBack = null;
        if (this.mBroadcastReceiver != null && this.mContext != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
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
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (WifiHandover.this.mNetworkHandler.hasMessages(7)) {
                            WifiHandover.this.mNetworkHandler.removeMessages(7);
                        }
                        if (WifiHandover.this.mNetwoksHandoverState != 1) {
                            if (WifiHandover.this.mNetwoksHandoverState == 4) {
                                WifiHandover.this.trySwitchWifiNetwork(WifiHandover.this.selectQualifiedCandidate());
                                break;
                            }
                        } else {
                            boolean unused = WifiHandover.this.handleScanResultsForConnectWiFi();
                            break;
                        }
                        break;
                    case 2:
                        boolean connectStatus = false;
                        WifiInfo newWifiInfo = WifiHandover.this.mWifiManager.getConnectionInfo();
                        WifiHandover.LOGD("HANDLER_CMD_WIFI_CONNECTED, newWifiInfo = " + newWifiInfo);
                        if (!(newWifiInfo == null || newWifiInfo.getBSSID() == null || !newWifiInfo.getBSSID().equals(WifiHandover.this.mToConnectBssid))) {
                            connectStatus = true;
                        }
                        WifiHandover.this.sendNetworkHandoverResult(WifiHandover.this.mNetwoksHandoverType, connectStatus, WifiHandover.this.mToConnectBssid, 0);
                        int unused2 = WifiHandover.this.mNetwoksHandoverType = -1;
                        int unused3 = WifiHandover.this.mNetwoksHandoverState = 0;
                        String unused4 = WifiHandover.this.mToConnectBssid = null;
                        break;
                    case 3:
                        int code = msg.arg1;
                        Bundle bundle = (Bundle) msg.obj;
                        String ssid = bundle.getString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                        String configKey = bundle.getString(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE);
                        boolean handoverStatusOk = false;
                        if (code == 0 && configKey != null && !configKey.equals(WifiHandover.this.mOldConfigKey)) {
                            handoverStatusOk = true;
                            WifiHandover.this.mNetworkQosMonitor.resetMonitorStatus();
                        }
                        if ((configKey != null && code != 0) || handoverStatusOk) {
                            WifiHandover.LOGD("HANDLER_CMD_WIFI_2_WIFI , wifi 2 wifi cleanTempBlackList");
                            WifiHandover.this.mNetworkBlackListManager.cleanTempWifiBlackList();
                        } else if (WifiHandover.this.mNetworkBlackListManager.isFailedMultiTimes(WifiHandover.this.mTargetBssid)) {
                            WifiHandover.LOGD("HANDLER_CMD_WIFI_2_WIFI failed!, add to abnormal black list");
                            WifiHandover.this.mNetworkBlackListManager.addAbnormalWifiBlacklist(WifiHandover.this.mTargetBssid);
                            WifiHandover.this.mNetworkBlackListManager.cleanTempWifiBlackList();
                        }
                        WifiHandover.this.sendNetworkHandoverResult(WifiHandover.this.mNetwoksHandoverType, handoverStatusOk, ssid, 0);
                        int unused5 = WifiHandover.this.mNetwoksHandoverType = -1;
                        int unused6 = WifiHandover.this.mNetwoksHandoverState = 0;
                        String unused7 = WifiHandover.this.mOldConfigKey = null;
                        ArrayList unused8 = WifiHandover.this.mSwitchBlacklist = null;
                        String unused9 = WifiHandover.this.mTargetBssid = null;
                        break;
                    case 4:
                        int unused10 = WifiHandover.this.notifyGoodApFound();
                        break;
                    case 5:
                        int handoverStatus = msg.arg1;
                        Bundle result = (Bundle) msg.obj;
                        String dualBandSsid = result.getString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                        String dualBandBssid = result.getString(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID);
                        String dualBandConfigKey = result.getString(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE);
                        boolean dualbandhandoverOK = false;
                        if (handoverStatus == 0 && WifiHandover.this.mToConnectConfigKey != null && WifiHandover.this.mToConnectConfigKey.equals(dualBandConfigKey) && WifiHandover.this.mToConnectDualbandBssid != null && WifiHandover.this.mToConnectDualbandBssid.equals(dualBandBssid)) {
                            dualbandhandoverOK = true;
                            WifiHandover.this.mNetworkQosMonitor.resetMonitorStatus();
                        }
                        if (!dualbandhandoverOK && handoverStatus != -7) {
                            handoverStatus = -6;
                        }
                        WifiHandover.this.sendNetworkHandoverResult(WifiHandover.this.mNetwoksHandoverType, dualbandhandoverOK, dualBandSsid, handoverStatus);
                        int unused11 = WifiHandover.this.mNetwoksHandoverType = -1;
                        int unused12 = WifiHandover.this.mNetwoksHandoverState = 0;
                        String unused13 = WifiHandover.this.mToConnectConfigKey = null;
                        String unused14 = WifiHandover.this.mToConnectDualbandBssid = null;
                        break;
                    case 6:
                        WifiHandover.this.mNetworkBlackListManager.cleanAbnormalWifiBlacklist();
                        break;
                    case 7:
                        WifiHandover.LOGD("HANDLER_CMD_REQUEST_SCAN_TIME_OUT");
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 1));
                        break;
                }
                super.handleMessage(msg);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
                    if (WifiHandover.this.mNetwoksHandoverState == 1 || WifiHandover.this.mNetwoksHandoverState == 4) {
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 1));
                    } else if (WifiProCommonUtils.isWifiConnected(WifiHandover.this.mWifiManager) && WifiHandover.this.mNetwoksHandoverState == 0) {
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 4));
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (netInfo == null || netInfo.getState() != NetworkInfo.State.CONNECTED) {
                        if (netInfo != null && netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                            WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 6));
                        }
                    } else if (WifiHandover.this.mNetwoksHandoverState == 1 || WifiHandover.this.mNetwoksHandoverState == 2) {
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 2));
                    }
                } else if (WifiHandover.ACTION_RESPONSE_WIFI_2_WIFI.equals(intent.getAction())) {
                    if (WifiHandover.this.mNetwoksHandoverState == 3) {
                        int status = intent.getIntExtra(WifiHandover.WIFI_HANDOVER_COMPLETED_STATUS, -100);
                        String bssid = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID);
                        String ssid = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                        String configKey = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE);
                        Bundle bundle = new Bundle();
                        bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID, bssid);
                        bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID, ssid);
                        bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE, configKey);
                        WifiHandover.LOGW("ACTION_RESPONSE_WIFI_2_WIFI received, status = " + status + ", configKey = " + configKey + ", mOldConfigKey = " + WifiHandover.this.mOldConfigKey);
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 3, status, -1, bundle));
                    }
                } else if (WifiHandover.ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER.equals(intent.getAction()) && WifiHandover.this.mNetwoksHandoverState == 5) {
                    int status2 = intent.getIntExtra(WifiHandover.WIFI_HANDOVER_COMPLETED_STATUS, -100);
                    String bssid2 = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID);
                    String ssid2 = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                    String configKey2 = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE);
                    Bundle bundle2 = new Bundle();
                    bundle2.putString(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID, bssid2);
                    bundle2.putString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID, ssid2);
                    bundle2.putString(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE, configKey2);
                    WifiHandover.LOGD("ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER received, status = " + status2 + ", ssid = " + ssid2 + ", configKey = " + configKey2);
                    WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 5, status2, -1, bundle2));
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, WIFI_HANDOVER_RECV_PERMISSION, null);
    }

    /* access modifiers changed from: private */
    public void sendNetworkHandoverResult(int type, boolean status, String ssid, int errorReason) {
        if (this.mCallBack != null && type != -1) {
            LOGW("sendNetworkHandoverResult, type = " + type + ", status = " + status + " ,errorReason = " + errorReason);
            this.mCallBack.onWifiHandoverChange(type, status, ssid, errorReason);
        }
    }

    private void notifyWifiAvailableStatus(boolean status, int bestRssi, String targetSsid, int preferType, int freq) {
        if (this.mCallBack == null) {
            LOGW("notifyWifiAvailableStatus, mCallBack == null");
        } else {
            this.mCallBack.onCheckAvailableWifi(status, bestRssi, targetSsid, preferType, freq);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00bb  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01ea  */
    public int notifyGoodApFound() {
        int nextRssi;
        List<ScanResult> scanResults;
        boolean sameBssid;
        String currentSsid;
        WifiConfiguration current;
        int nextRssi2;
        int nextFreq = INVALID_RSSI;
        String scanResultEncrypt = null;
        boolean perferFound = false;
        String nextPreferSsid = null;
        if (this.mWifiStateMachine != null && this.mWifiStateMachine.isScanAndManualConnectMode()) {
            return INVALID_RSSI;
        }
        List<ScanResult> scanResults2 = this.mWifiManager.getScanResults();
        if (scanResults2 == null) {
        } else if (scanResults2.size() == 0) {
            List<ScanResult> list = scanResults2;
        } else {
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configNetworks == null) {
            } else if (configNetworks.size() == 0) {
                List<ScanResult> list2 = scanResults2;
            } else {
                WifiConfiguration current2 = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
                String currentSsid2 = WifiProCommonUtils.getCurrentSsid(this.mWifiManager);
                String currentBssid = WifiProCommonUtils.getCurrentBssid(this.mWifiManager);
                String currentEncrypt = current2 != null ? current2.configKey() : null;
                WifiConfiguration preferConfig = getUserPreferredNetwork(currentSsid2, configNetworks);
                int nextFreq2 = -1;
                int preferType = 0;
                int i = 0;
                int nextPreferRssi = -200;
                while (true) {
                    boolean z = true;
                    if (i >= scanResults2.size()) {
                        break;
                    }
                    ScanResult nextResult = scanResults2.get(i);
                    int preferType2 = preferType;
                    StringBuilder sb = new StringBuilder();
                    String nextSsid = scanResultEncrypt;
                    sb.append("\"");
                    sb.append(nextResult.SSID);
                    sb.append("\"");
                    String scanSsid = sb.toString();
                    String scanResultEncrypt2 = nextResult.capabilities;
                    if (currentBssid != null) {
                        scanResults = scanResults2;
                        if (currentBssid.equals(nextResult.BSSID)) {
                            sameBssid = true;
                            boolean sameConfigKey = currentSsid2 == null && currentSsid2.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt2, currentEncrypt);
                            if (sameBssid) {
                                if (sameConfigKey) {
                                    nextRssi2 = nextFreq;
                                    current = current2;
                                } else {
                                    boolean z2 = sameBssid;
                                    current = current2;
                                    if (this.mNetworkBlackListManager.isInAbnormalWifiBlacklist(nextResult.BSSID)) {
                                        nextRssi2 = nextFreq;
                                    } else {
                                        if (preferConfig != null) {
                                            LOGD("scan: " + scanSsid + ", prefer: " + preferConfig.SSID);
                                            if (preferConfig.SSID == null || !preferConfig.SSID.equals(scanSsid) || !WifiProCommonUtils.isSameEncryptType(scanResultEncrypt2, preferConfig.configKey())) {
                                                z = false;
                                            }
                                            boolean sameSsid = z;
                                            if (sameSsid) {
                                                StringBuilder sb2 = new StringBuilder();
                                                boolean z3 = sameSsid;
                                                sb2.append("found USER PREFERED network: ssid=");
                                                sb2.append(preferConfig.SSID);
                                                sb2.append(", cur rssi=");
                                                sb2.append(nextResult.level);
                                                sb2.append(", pre rssi=");
                                                sb2.append(nextPreferRssi);
                                                LOGD(sb2.toString());
                                                if (nextResult.level > nextPreferRssi) {
                                                    LOGD("update USER PREFERED network: rssi=" + nextPreferRssi);
                                                    nextPreferRssi = nextResult.level;
                                                    nextPreferSsid = nextResult.SSID;
                                                    perferFound = true;
                                                    preferType = 1;
                                                    currentSsid = currentSsid2;
                                                    scanResultEncrypt = nextSsid;
                                                    i++;
                                                    scanResults2 = scanResults;
                                                    current2 = current;
                                                    currentSsid2 = currentSsid;
                                                }
                                            }
                                        }
                                        currentSsid = currentSsid2;
                                        int signalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(nextResult.frequency, nextResult.level);
                                        if (signalLevel <= 2) {
                                            nextRssi2 = nextFreq;
                                        } else {
                                            if (nextResult.level > nextFreq) {
                                                int k = 0;
                                                while (k < configNetworks.size()) {
                                                    WifiConfiguration nextConfig = configNetworks.get(k);
                                                    int nextRssi3 = nextFreq;
                                                    int disableReason = nextConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                                                    int signalLevel2 = signalLevel;
                                                    if (nextConfig.noInternetAccess == 0 && !nextConfig.portalNetwork && disableReason <= 0 && !WifiProCommonUtils.isOpenAndPortal(nextConfig) && !WifiProCommonUtils.isOpenAndMaybePortal(nextConfig) && nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt2, nextConfig.configKey())) {
                                                        int nextRssi4 = nextResult.level;
                                                        int i2 = disableReason;
                                                        int nextId = nextConfig.networkId;
                                                        String nextSsid2 = nextResult.SSID;
                                                        nextFreq2 = nextResult.frequency;
                                                        nextFreq = nextRssi4;
                                                        preferType = preferType2;
                                                        int i3 = nextId;
                                                        scanResultEncrypt = nextSsid2;
                                                        break;
                                                    }
                                                    k++;
                                                    nextFreq = nextRssi3;
                                                    signalLevel = signalLevel2;
                                                }
                                            }
                                            nextRssi2 = nextFreq;
                                        }
                                    }
                                }
                                currentSsid = currentSsid2;
                            } else {
                                nextRssi2 = nextFreq;
                                current = current2;
                                currentSsid = currentSsid2;
                            }
                            preferType = preferType2;
                            scanResultEncrypt = nextSsid;
                            nextFreq = nextRssi2;
                            i++;
                            scanResults2 = scanResults;
                            current2 = current;
                            currentSsid2 = currentSsid;
                        }
                    } else {
                        scanResults = scanResults2;
                    }
                    sameBssid = false;
                    if (currentSsid2 == null) {
                    }
                    if (sameBssid) {
                    }
                    preferType = preferType2;
                    scanResultEncrypt = nextSsid;
                    nextFreq = nextRssi2;
                    i++;
                    scanResults2 = scanResults;
                    current2 = current;
                    currentSsid2 = currentSsid;
                }
                int nextRssi5 = nextFreq;
                int preferType3 = preferType;
                String nextSsid3 = scanResultEncrypt;
                List<ScanResult> list3 = scanResults2;
                WifiConfiguration wifiConfiguration = current2;
                String str = currentSsid2;
                if (perferFound) {
                    LOGD("final network: ssid=" + nextSsid);
                    nextSsid3 = nextPreferSsid;
                    nextRssi = nextPreferRssi;
                } else {
                    preferType3 = 0;
                    nextRssi = nextRssi5;
                }
                int i4 = nextPreferRssi;
                notifyWifiAvailableStatus(nextRssi != -200, nextRssi, nextSsid3, preferType3, nextFreq2);
                return nextRssi;
            }
            LOGW("notifyGoodApFound, WiFi configured networks are invalid, getConfiguredNetworks is null");
            return INVALID_RSSI;
        }
        LOGW("notifyGoodApFound, WiFi scan results are invalid, getScanResults is null ");
        return INVALID_RSSI;
    }

    /* access modifiers changed from: private */
    public boolean handleScanResultsForConnectWiFi() {
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            LOGW("handleScanResultsForConnectWiFi, WiFi scan results are invalid, getScanResults is null");
            return false;
        }
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            LOGW("handleScanResultsForConnectWiFi, WiFi configured networks are invalid, getConfiguredNetworks is null");
            return false;
        }
        boolean found = false;
        WifiConfiguration nextConfig = null;
        int nextRssi = -200;
        int nextId = -1;
        int i = 0;
        while (!found && i < scanResults.size()) {
            ScanResult nextResult = scanResults.get(i);
            String scanSsid = "\"" + nextResult.SSID + "\"";
            if (nextResult.BSSID.equals(this.mToConnectBssid)) {
                WifiConfiguration nextConfig2 = nextConfig;
                int k = 0;
                while (true) {
                    if (k >= configNetworks.size()) {
                        break;
                    }
                    nextConfig2 = configNetworks.get(k);
                    if (nextConfig2.SSID != null && nextConfig2.SSID.equals(scanSsid)) {
                        nextRssi = nextResult.level;
                        nextId = nextConfig2.networkId;
                        found = true;
                        break;
                    }
                    k++;
                }
                nextConfig = nextConfig2;
            }
            i++;
        }
        LOGD("handleScanResultsForConnectWiFi, nextId = " + nextId + ", nextRssi = " + nextRssi);
        if (nextId == -1) {
            return false;
        }
        if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(nextConfig, false)) {
            Log.w(TAG, "MDM deny connect!");
            return false;
        }
        this.mNetwoksHandoverState = 2;
        this.mWifiManager.connect(nextId, null);
        return true;
    }

    private boolean invalidConfigNetwork(WifiConfiguration config, int signalLevel, int currentLevel) {
        if (this.mNetwoksHandoverState == 4 && this.mSwitchBlacklist != null) {
            int l = 0;
            while (l < this.mSwitchBlacklist.size()) {
                if (!config.SSID.equals(this.mSwitchBlacklist.get(l)) || (signalLevel > 3 && signalLevel - currentLevel >= 2)) {
                    l++;
                } else {
                    LOGW("selectQualifiedCandidate, switch black list filter it, ssid = " + config.SSID);
                    return true;
                }
            }
        }
        int disableReason = config.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
        if (disableReason < 2 || disableReason > 9) {
            return WifiProCommonUtils.isOpenAndPortal(config) || WifiProCommonUtils.isOpenAndMaybePortal(config) || config.noInternetAccess || config.portalNetwork;
        }
        LOGW("selectQualifiedCandidate, wifi switch, ssid = " + config.SSID + ", disableReason = " + disableReason);
        return true;
    }

    private WifiSwitchCandidate getBetterSignalCandidate(WifiSwitchCandidate last, WifiSwitchCandidate current) {
        if (current == null || current.bestScanResult == null || (last != null && last.bestScanResult != null && last.bestScanResult.level >= current.bestScanResult.level)) {
            return last;
        }
        return current;
    }

    private WifiSwitchCandidate getBackupSwitchCandidate(WifiConfiguration currentConfig, ScanResult currentResult, WifiSwitchCandidate lastCandidate) {
        if (this.mNetwoksHandoverState != 4 || !currentConfig.noInternetAccess || !WifiProCommonUtils.allowWifiConfigRecovery(currentConfig.internetHistory)) {
            return null;
        }
        return getBetterSignalCandidate(lastCandidate, new WifiSwitchCandidate(currentConfig, currentResult));
    }

    private WifiSwitchCandidate getBestSwitchCandidate(WifiConfiguration currentConfig, ScanResult currentResult, WifiSwitchCandidate lastCandidate) {
        if (currentConfig == null || currentResult == null) {
            return lastCandidate;
        }
        if (lastCandidate == null || lastCandidate.bestWifiConfig == null || lastCandidate.bestScanResult == null) {
            return new WifiSwitchCandidate(currentConfig, currentResult);
        }
        if (this.mNetwoksHandoverState == 0 && SystemClock.elapsedRealtime() - (currentResult.timestamp / 1000) > 200) {
            return lastCandidate;
        }
        ScanResult lastResult = lastCandidate.bestScanResult;
        if (((ScanResult.is5GHz(lastResult.frequency) && ScanResult.is5GHz(currentResult.frequency)) || (ScanResult.is24GHz(lastResult.frequency) && ScanResult.is24GHz(currentResult.frequency))) && currentResult.level > lastResult.level) {
            return new WifiSwitchCandidate(currentConfig, currentResult);
        }
        if (ScanResult.is5GHz(lastResult.frequency) && ScanResult.is24GHz(currentResult.frequency) && lastResult.level < -72 && currentResult.level > lastResult.level) {
            return new WifiSwitchCandidate(currentConfig, currentResult);
        }
        if (!ScanResult.is24GHz(lastResult.frequency) || !ScanResult.is5GHz(currentResult.frequency) || (currentResult.level <= lastResult.level && currentResult.level < -72)) {
            return lastCandidate;
        }
        return new WifiSwitchCandidate(currentConfig, currentResult);
    }

    private WifiSwitchCandidate getAutoRoamCandidate(int currentSignalRssi, WifiSwitchCandidate bestCandidate) {
        if (!(this.mNetwoksHandoverState != 0 || bestCandidate == null || bestCandidate.bestScanResult == null)) {
            if (HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(bestCandidate.bestScanResult.frequency, bestCandidate.bestScanResult.level) - WifiProCommonUtils.getCurrenSignalLevel(this.mWifiManager.getConnectionInfo()) < 2 || bestCandidate.bestScanResult.level - currentSignalRssi < 10) {
                return null;
            }
        }
        return bestCandidate;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x021b A[EDGE_INSN: B:118:0x021b->B:95:0x021b ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01e2  */
    public WifiSwitchCandidate selectQualifiedCandidate() {
        WifiSwitchCandidate wifiSwitchCandidate;
        WifiSwitchCandidate wifiSwitchCandidate2;
        WifiInfo wifiInfo;
        boolean bssidSame;
        int nextPreferRssi;
        int k;
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        WifiConfiguration current = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        int currentRssi = WifiProCommonUtils.getCurrentRssi(this.mWifiManager);
        WifiInfo wifiInfo2 = this.mWifiManager.getConnectionInfo();
        int currentLevel = WifiProCommonUtils.getCurrenSignalLevel(wifiInfo2);
        if (scanResults == null) {
            WifiConfiguration wifiConfiguration = current;
            int i = currentRssi;
            WifiInfo wifiInfo3 = wifiInfo2;
            wifiSwitchCandidate = null;
        } else if (scanResults.size() == 0) {
            List<ScanResult> list = scanResults;
            WifiConfiguration wifiConfiguration2 = current;
            int i2 = currentRssi;
            WifiInfo wifiInfo4 = wifiInfo2;
            wifiSwitchCandidate = null;
        } else {
            if (current == null) {
                WifiConfiguration wifiConfiguration3 = current;
                int i3 = currentRssi;
                WifiInfo wifiInfo5 = wifiInfo2;
                wifiSwitchCandidate2 = null;
            } else if (current.SSID == null) {
                List<ScanResult> list2 = scanResults;
                WifiConfiguration wifiConfiguration4 = current;
                int i4 = currentRssi;
                WifiInfo wifiInfo6 = wifiInfo2;
                wifiSwitchCandidate2 = null;
            } else {
                List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
                if (configNetworks == null) {
                    WifiConfiguration wifiConfiguration5 = current;
                    int i5 = currentRssi;
                    WifiInfo wifiInfo7 = wifiInfo2;
                } else if (configNetworks.size() == 0) {
                    List<ScanResult> list3 = scanResults;
                    WifiConfiguration wifiConfiguration6 = current;
                    int i6 = currentRssi;
                    WifiInfo wifiInfo8 = wifiInfo2;
                } else {
                    String currentSsid = current.SSID;
                    String currentBssid = WifiProCommonUtils.getCurrentBssid(this.mWifiManager);
                    String currentEncrypt = current.configKey();
                    WifiConfiguration preferConfig = getUserPreferredNetwork(currentSsid, configNetworks);
                    if (preferConfig == null && this.mNetwoksHandoverState == 0 && currentLevel >= 4) {
                        return null;
                    }
                    WifiSwitchCandidate bestSwitchCandidate = null;
                    int nextPreferRssi2 = -200;
                    int i7 = 0;
                    boolean perferFound = false;
                    WifiSwitchCandidate backupSwitchCandidate = null;
                    WifiSwitchCandidate preferSwitchCandidate = null;
                    while (i7 < scanResults.size()) {
                        ScanResult nextResult = scanResults.get(i7);
                        List<ScanResult> scanResults2 = scanResults;
                        StringBuilder sb = new StringBuilder();
                        WifiConfiguration current2 = current;
                        sb.append("\"");
                        sb.append(nextResult.SSID);
                        sb.append("\"");
                        String scanSsid = sb.toString();
                        String scanResultEncrypt = nextResult.capabilities;
                        if (currentBssid != null) {
                            wifiInfo = wifiInfo2;
                            if (currentBssid.equals(nextResult.BSSID)) {
                                bssidSame = true;
                                String currentBssid2 = currentBssid;
                                int currentRssi2 = currentRssi;
                                boolean switchConfigKeySame = this.mNetwoksHandoverState != 4 && currentSsid.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, currentEncrypt);
                                boolean idleConfigKeyDiff = this.mNetwoksHandoverState != 0 && (!currentSsid.equals(scanSsid) || !WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, currentEncrypt));
                                String currentSsid2 = currentSsid;
                                String currentEncrypt2 = currentEncrypt;
                                boolean z = perferFound;
                                int signalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(nextResult.frequency, nextResult.level);
                                if (!bssidSame || switchConfigKeySame || idleConfigKeyDiff) {
                                    nextPreferRssi = nextPreferRssi2;
                                } else {
                                    if (signalLevel > 2) {
                                        if (this.mNetworkBlackListManager.isInAbnormalWifiBlacklist(nextResult.BSSID)) {
                                            LOGW("selectQualifiedCandidate, scanSsid" + scanSsid + " is in black list!");
                                        } else {
                                            if (preferConfig != null) {
                                                LOGD("scan: " + scanSsid + ", prefer: " + preferConfig.SSID);
                                                if ((preferConfig != null && preferConfig.SSID != null && preferConfig.SSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, preferConfig.configKey())) && !invalidConfigNetwork(preferConfig, signalLevel, currentLevel)) {
                                                    StringBuilder sb2 = new StringBuilder();
                                                    boolean z2 = idleConfigKeyDiff;
                                                    sb2.append("found USER PREFERED network: ssid=");
                                                    sb2.append(preferConfig.SSID);
                                                    sb2.append(", cur rssi=");
                                                    sb2.append(nextResult.level);
                                                    sb2.append(", pre rssi=");
                                                    nextPreferRssi = nextPreferRssi2;
                                                    sb2.append(nextPreferRssi);
                                                    LOGD(sb2.toString());
                                                    if (nextResult.level <= nextPreferRssi) {
                                                        k = 0;
                                                        while (true) {
                                                            if (k < configNetworks.size()) {
                                                                break;
                                                            }
                                                            WifiConfiguration nextConfig = configNetworks.get(k);
                                                            if ((nextConfig != null && nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, nextConfig.configKey())) && !invalidConfigNetwork(nextConfig, signalLevel, currentLevel)) {
                                                                backupSwitchCandidate = getBackupSwitchCandidate(nextConfig, nextResult, backupSwitchCandidate);
                                                                bestSwitchCandidate = getBestSwitchCandidate(nextConfig, nextResult, bestSwitchCandidate);
                                                                break;
                                                            }
                                                            k++;
                                                        }
                                                    } else {
                                                        StringBuilder sb3 = new StringBuilder();
                                                        boolean z3 = bssidSame;
                                                        sb3.append("update USER PREFERED network: rssi=");
                                                        sb3.append(nextPreferRssi);
                                                        LOGD(sb3.toString());
                                                        perferFound = true;
                                                        nextPreferRssi2 = nextResult.level;
                                                        preferSwitchCandidate = new WifiSwitchCandidate(preferConfig, nextResult);
                                                        i7++;
                                                        scanResults = scanResults2;
                                                        current = current2;
                                                        wifiInfo2 = wifiInfo;
                                                        currentBssid = currentBssid2;
                                                        currentRssi = currentRssi2;
                                                        currentSsid = currentSsid2;
                                                        currentEncrypt = currentEncrypt2;
                                                    }
                                                }
                                            }
                                            boolean z4 = bssidSame;
                                            nextPreferRssi = nextPreferRssi2;
                                            k = 0;
                                            while (true) {
                                                if (k < configNetworks.size()) {
                                                }
                                                k++;
                                            }
                                        }
                                    }
                                    nextPreferRssi = nextPreferRssi2;
                                }
                                nextPreferRssi2 = nextPreferRssi;
                                perferFound = z;
                                i7++;
                                scanResults = scanResults2;
                                current = current2;
                                wifiInfo2 = wifiInfo;
                                currentBssid = currentBssid2;
                                currentRssi = currentRssi2;
                                currentSsid = currentSsid2;
                                currentEncrypt = currentEncrypt2;
                            }
                        } else {
                            wifiInfo = wifiInfo2;
                        }
                        bssidSame = false;
                        String currentBssid22 = currentBssid;
                        int currentRssi22 = currentRssi;
                        if (this.mNetwoksHandoverState != 4) {
                        }
                        if (this.mNetwoksHandoverState != 0) {
                        }
                        String currentSsid22 = currentSsid;
                        String currentEncrypt22 = currentEncrypt;
                        boolean z5 = perferFound;
                        int signalLevel2 = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(nextResult.frequency, nextResult.level);
                        if (!bssidSame) {
                        }
                        nextPreferRssi = nextPreferRssi2;
                        nextPreferRssi2 = nextPreferRssi;
                        perferFound = z5;
                        i7++;
                        scanResults = scanResults2;
                        current = current2;
                        wifiInfo2 = wifiInfo;
                        currentBssid = currentBssid22;
                        currentRssi = currentRssi22;
                        currentSsid = currentSsid22;
                        currentEncrypt = currentEncrypt22;
                    }
                    WifiConfiguration wifiConfiguration7 = current;
                    int currentRssi3 = currentRssi;
                    WifiInfo wifiInfo9 = wifiInfo2;
                    String str = currentSsid;
                    String str2 = currentBssid;
                    String str3 = currentEncrypt;
                    boolean perferFound2 = perferFound;
                    int currentRssi4 = nextPreferRssi2;
                    if (bestSwitchCandidate == null && backupSwitchCandidate != null) {
                        bestSwitchCandidate = backupSwitchCandidate;
                    }
                    if (perferFound2 && preferSwitchCandidate != null) {
                        bestSwitchCandidate = preferSwitchCandidate;
                        LOGD("final network: ssid=" + bestSwitchCandidate.getWifiConfig().SSID);
                    }
                    return getAutoRoamCandidate(currentRssi3, bestSwitchCandidate);
                }
                LOGW("selectQualifiedCandidate, WiFi configured networks are invalid, getConfiguredNetworks is null");
                return null;
            }
            LOGW("selectQualifiedCandidate, current connected wifi configuration is null");
            return wifiSwitchCandidate2;
        }
        LOGW("selectQualifiedCandidate, WiFi scan results are invalid, getScanResults is null");
        return wifiSwitchCandidate;
    }

    private WifiConfiguration getUserPreferredNetwork(String currentSsid, List<WifiConfiguration> configNetworks) {
        this.mHwArbitrationManager = HwArbitrationManager.getInstance();
        if (this.mHwArbitrationManager == null) {
            return null;
        }
        HashMap<Integer, String> preferList = this.mHwArbitrationManager.getWifiPreferenceFromHiData();
        LOGD("getWifiPreferenceFromHiData: " + preferList.toString());
        if (!preferList.containsKey(0)) {
            return null;
        }
        int configNetworkSize = configNetworks.size();
        for (int k = 0; k < configNetworkSize; k++) {
            WifiConfiguration nextConfig = configNetworks.get(k);
            if (nextConfig.SSID != null && nextConfig.SSID.equals(preferList.get(0))) {
                LOGD("found user preferred network in configNW");
                int disableReason = nextConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                if (!nextConfig.noInternetAccess && !nextConfig.portalNetwork && disableReason <= 0 && !WifiProCommonUtils.isOpenAndPortal(nextConfig) && !WifiProCommonUtils.isOpenAndMaybePortal(nextConfig)) {
                    WifiConfiguration preferConfig = nextConfig;
                    LOGD("found avalible user preferred network: " + nextConfig.SSID);
                    return preferConfig;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void trySwitchWifiNetwork(WifiSwitchCandidate switchCandidate) {
        WifiConfiguration current = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        if (current == null || switchCandidate == null) {
            LOGW("trySwitchWifiNetwork# CurrentWifiConfig is null or switchCandidate is null!!");
            if (this.mNetwoksHandoverState == 4) {
                sendNetworkHandoverResult(this.mNetwoksHandoverType, false, null, 0);
            }
            this.mNetwoksHandoverType = -1;
            this.mNetwoksHandoverState = 0;
            this.mOldConfigKey = null;
            this.mSwitchBlacklist = null;
            return;
        }
        WifiConfiguration best = switchCandidate.getWifiConfig();
        if (this.mWifiStateMachine != null) {
            if (this.mNetwoksHandoverState == 4) {
                this.mNetwoksHandoverState = 3;
                this.mWifiStateMachine.startWifi2WifiRequest();
            }
            if (current.networkId == best.networkId || current.isLinked(best)) {
                LOGD("trySwitchWifiNetwork: Soft reconnect from " + current.SSID + " to " + best.SSID);
                updateWifiSwitchTimeStamp(System.currentTimeMillis());
                this.mWifiStateMachine.startRoamToNetwork(best.networkId, switchCandidate.getScanResult());
            } else if (this.mNetwoksHandoverState == 3) {
                LOGD("trySwitchWifiNetwork: Reconnect from " + current.SSID + " to " + best.SSID);
                String bssid = switchCandidate.getScanResult().BSSID;
                if (!TextUtils.isEmpty(bssid)) {
                    this.mTargetBssid = bssid;
                    if (this.mHwWifiCHRService != null) {
                        this.mHwWifiCHRService.updateConnectType("WIFIPRO_CONNECT");
                    }
                    updateWifiSwitchTimeStamp(System.currentTimeMillis());
                    this.mWifiStateMachine.requestWifiSoftSwitch();
                    this.mWifiStateMachine.startConnectToUserSelectNetwork(best.networkId, Binder.getCallingUid(), bssid);
                }
            }
        }
    }

    private void updateWifiSwitchTimeStamp(long ts) {
        WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        if (config != null && ts > 0) {
            config.lastTrySwitchWifiTimestamp = ts;
            this.mWifiStateMachine.sendMessage(HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS, config);
        }
    }

    public boolean handleWifiToWifi(ArrayList<String> invalidNetworks, int threshold, int qosLevel) {
        if (invalidNetworks == null) {
            LOGW("handleWifiToWifi, inputed arg is invalid, invalidNetworks is null");
            return false;
        } else if (this.mWifiStateMachine == null || !this.mWifiStateMachine.isScanAndManualConnectMode()) {
            this.mSwitchBlacklist = (ArrayList) invalidNetworks.clone();
            WifiConfiguration currConfig = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
            if (currConfig == null || currConfig.configKey() == null) {
                LOGW("handleWifiToWifi, getCurrentWifiConfig is null.");
                return false;
            }
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                LOGW("handleWifiToWifi, WiFi configured networks are invalid, getConfiguredNetworks is null");
                return false;
            }
            this.mOldConfigKey = currConfig.configKey();
            this.mNetwoksHandoverType = 1;
            this.mNetwoksHandoverState = 4;
            this.mNetworkHandler.sendMessageDelayed(Message.obtain(this.mNetworkHandler, 7), HANDOVER_WAIT_SCAN_TIME_OUT);
            requestScan();
            return true;
        } else {
            LOGW("Only allow Manual Connection, ignore auto connection.");
            return false;
        }
    }

    public boolean hasAvailableWifiNetwork(List<String> invalidNetworks, int threshold, String currBssid, String currSSid) {
        LOGD("hasAvailableWifiNetwork, invalidNetworks = " + invalidNetworks + ", threshold = " + threshold + ", currSSid = " + currSSid);
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            LOGW("hasAvailableWifiNetwork, WiFi configured networks are invalid, getConfiguredNetworks is null");
            return false;
        }
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        requestScan();
        return true;
    }

    private void requestScan() {
        if (this.mWifiStateMachine != null && this.mContext != null) {
            ScanRequestProxy scanRequest = WifiInjector.getInstance().getScanRequestProxy();
            if (scanRequest != null) {
                scanRequest.startScan(Binder.getCallingUid(), this.mContext.getOpPackageName());
            } else {
                LOGD("can't start wifi scan!");
            }
        }
    }

    public boolean connectWifiNetwork(String bssid) {
        if (bssid == null || bssid.length() == 0) {
            LOGW("connectWifiNetwork, inputed arg is invalid");
            return false;
        }
        WifiInfo currWifiInfo = this.mWifiManager.getConnectionInfo();
        if (currWifiInfo == null || !bssid.equals(currWifiInfo.getBSSID())) {
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                LOGW("connectWifiNetwork, WiFi configured networks are invalid, getConfiguredNetworks is null");
                return false;
            }
            this.mToConnectBssid = bssid;
            this.mNetwoksHandoverType = 2;
            this.mNetwoksHandoverState = 1;
            requestScan();
            return true;
        }
        LOGW("connectWifiNetwork, already connected, ignore it.");
        return true;
    }

    public int handleDualBandWifiConnect(String bssid, String ssid, int authType, int switchType) {
        Log.d(TAG, "DualBandWifiConnect, ssid = " + ssid + ", authType = " + authType + ", switchType = " + switchType);
        if (bssid == null || ssid == null || switchType < 1 || switchType > 3) {
            Log.d(TAG, "DualBandWifiConnect, inputed arg is invalid, ssid is null  , switchType = " + switchType);
            return 1;
        }
        WifiInfo currWifiInfo = this.mWifiManager.getConnectionInfo();
        if (currWifiInfo == null || !bssid.equals(currWifiInfo.getBSSID())) {
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
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
                    mManager.delectDualBandAPInfoBySsid(ssid, authType);
                }
                return 1;
            }
            this.mHwArbitrationManager = HwArbitrationManager.getInstance();
            if (this.mHwArbitrationManager != null) {
                HashMap<Integer, String> preferList = this.mHwArbitrationManager.getWifiPreferenceFromHiData();
                LOGD("getWifiPreferenceFromHiData: " + preferList.toString());
                if (preferList.containsKey(0) && !changeConfig.SSID.equals(preferList.get(0))) {
                    Log.d(TAG, "DualBandWifiConnect, found user preference but not target ssid, ignore it.");
                    return 11;
                }
            }
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
            return 0;
        }
        Log.d(TAG, "DualBandWifiConnect, already connected, ignore it.");
        return 10;
    }

    public int getNetwoksHandoverType() {
        return this.mNetwoksHandoverType;
    }

    private boolean isValidConfig(WifiConfiguration config) {
        int cc;
        boolean z = false;
        if (config == null) {
            return false;
        }
        Log.e(TAG, "config isValid cardinality=" + cc);
        if (cc <= 1) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public static void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    /* access modifiers changed from: private */
    public static void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
