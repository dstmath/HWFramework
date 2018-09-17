package com.android.server.wifi.wifipro;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.HwWifiStatStore;
import com.android.server.wifi.HwWifiStatStoreImpl;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
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
    private static final int HANDLER_CMD_SCAN_RESULTS = 1;
    private static final int HANDLER_CMD_WIFI_2_WIFI = 3;
    private static final int HANDLER_CMD_WIFI_CONNECTED = 2;
    private static final int HANDLER_CMD_WIFI_DISCONNECTED = 6;
    private static final int HANDOVER_STATE_WAITING_DUAL_BAND_WIFI_CONNECT = 5;
    public static final int HANDOVER_STATUS_CONNECT_AUTH_FAILED = -7;
    public static final int HANDOVER_STATUS_CONNECT_REJECT_FAILED = -6;
    public static final int HANDOVER_STATUS_DISALLOWED = -4;
    public static final int HANDOVER_STATUS_OK = 0;
    public static final int INVALID_RSSI = -200;
    private static final int NETWORK_HANDLER_CMD_DUAL_BAND_WIFI_CONNECT = 5;
    public static final int NETWORK_HANDOVER_TYPE_CONNECT_SPECIFIC_WIFI = 2;
    public static final int NETWORK_HANDOVER_TYPE_DUAL_BAND_WIFI_CONNECT = 4;
    public static final int NETWORK_HANDOVER_TYPE_UNKNOWN = -1;
    public static final int NETWORK_HANDOVER_TYPE_WIFI_TO_WIFI = 1;
    public static final String TAG = "WifiHandover";
    public static final String WIFI_HANDOVER_COMPLETED_STATUS = "com.huawei.wifi.handover.status";
    public static final String WIFI_HANDOVER_NETWORK_BSSID = "com.huawei.wifi.handover.bssid";
    public static final String WIFI_HANDOVER_NETWORK_CONFIGKYE = "com.huawei.wifi.handover.configkey";
    public static final String WIFI_HANDOVER_NETWORK_SSID = "com.huawei.wifi.handover.ssid";
    public static final String WIFI_HANDOVER_NETWORK_SWITCHTYPE = "com.huawei.wifi.handover.switchtype";
    public static final String WIFI_HANDOVER_NETWORK_WIFICONFIG = "com.huawei.wifi.handover.wificonfig";
    public static final String WIFI_HANDOVER_RECV_PERMISSION = "com.huawei.wifipro.permission.RECV.WIFI_HANDOVER";
    private static Context mContext = null;
    private BroadcastReceiver mBroadcastReceiver;
    private INetworksHandoverCallBack mCallBack;
    private IntentFilter mIntentFilter;
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
    private WifiManager mWifiManager;
    private HwWifiStatStore mWifiStatStore;
    private WifiStateMachine mWifiStateMachine;

    private static class WifiSwitchCandidate {
        private ScanResult bestScanResult;
        private WifiConfiguration bestWifiConfig;

        public WifiSwitchCandidate(WifiConfiguration bestWifiConfig, ScanResult bestScanResult) {
            this.bestWifiConfig = bestWifiConfig;
            this.bestScanResult = bestScanResult;
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
        setWifiproContext(context);
        this.mWifiStatStore = HwWifiStatStoreImpl.getDefault();
        this.mWifiStateMachine = WifiInjector.getInstance().getWifiStateMachine();
        initialize();
        registerReceiverAndHandler();
    }

    public void registerCallBack(INetworksHandoverCallBack callBack, NetworkQosMonitor qosMonitor) {
        if (this.mCallBack == null) {
            this.mCallBack = callBack;
        }
        if (this.mBroadcastReceiver == null && mContext != null) {
            registerReceiverAndHandler();
        }
        this.mNetworkQosMonitor = qosMonitor;
    }

    public void unRegisterCallBack() {
        this.mCallBack = null;
        if (this.mBroadcastReceiver != null && mContext != null) {
            mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
            this.mIntentFilter = null;
            this.mNetworkHandler = null;
        }
    }

    private void initialize() {
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        this.mWifiManager = (WifiManager) mContext.getSystemService("wifi");
        this.mNetworkBlackListManager = NetworkBlackListManager.getNetworkBlackListManagerInstance(mContext);
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
                        WifiHandover.this.handleScanResultsForConnectWiFi();
                        break;
                    case 2:
                        boolean connectStatus = false;
                        WifiInfo newWifiInfo = WifiHandover.this.mWifiManager.getConnectionInfo();
                        WifiHandover.LOGD("HANDLER_CMD_WIFI_CONNECTED, newWifiInfo = " + newWifiInfo);
                        if (!(newWifiInfo == null || newWifiInfo.getBSSID() == null || !newWifiInfo.getBSSID().equals(WifiHandover.this.mToConnectBssid))) {
                            connectStatus = true;
                        }
                        WifiHandover.this.sendNetworkHandoverResult(WifiHandover.this.mNetwoksHandoverType, connectStatus, WifiHandover.this.mToConnectBssid, 0);
                        WifiHandover.this.mNetwoksHandoverType = -1;
                        WifiHandover.this.mNetwoksHandoverState = 0;
                        WifiHandover.this.mToConnectBssid = null;
                        break;
                    case 3:
                        int code = msg.arg1;
                        Bundle bundle = msg.obj;
                        String ssid = bundle.getString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                        String configKey = bundle.getString(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE);
                        boolean handoverStatusOk = false;
                        if (!(code != 0 || configKey == null || (configKey.equals(WifiHandover.this.mOldConfigKey) ^ 1) == 0)) {
                            handoverStatusOk = true;
                            WifiHandover.this.mNetworkQosMonitor.resetMonitorStatus();
                        }
                        if (code == 0 && (handoverStatusOk ^ 1) != 0) {
                            WifiHandover.LOGD("HANDLER_CMD_WIFI_2_WIFI failed!, add to abnormal black list ");
                            WifiHandover.this.mNetworkBlackListManager.addAbnormalWifiBlacklist(WifiHandover.this.mTargetBssid);
                        }
                        WifiHandover.this.sendNetworkHandoverResult(WifiHandover.this.mNetwoksHandoverType, handoverStatusOk, ssid, 0);
                        WifiHandover.this.mNetwoksHandoverType = -1;
                        WifiHandover.this.mNetwoksHandoverState = 0;
                        WifiHandover.this.mOldConfigKey = null;
                        WifiHandover.this.mSwitchBlacklist = null;
                        WifiHandover.this.mTargetBssid = null;
                        break;
                    case 4:
                        WifiHandover.this.notifyGoodApFound();
                        break;
                    case 5:
                        int handoverStatus = msg.arg1;
                        Bundle result = msg.obj;
                        String dualBandSsid = result.getString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                        String dualBandConfigKey = result.getString(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE);
                        boolean dualbandhandoverOK = false;
                        if (handoverStatus == 0 && WifiHandover.this.mToConnectConfigKey != null && WifiHandover.this.mToConnectConfigKey.equals(dualBandConfigKey)) {
                            dualbandhandoverOK = true;
                            WifiHandover.this.mNetworkQosMonitor.resetMonitorStatus();
                        }
                        if (!(dualbandhandoverOK || handoverStatus == -7)) {
                            handoverStatus = -6;
                        }
                        WifiHandover.this.sendNetworkHandoverResult(WifiHandover.this.mNetwoksHandoverType, dualbandhandoverOK, dualBandSsid, handoverStatus);
                        WifiHandover.this.mNetwoksHandoverType = -1;
                        WifiHandover.this.mNetwoksHandoverState = 0;
                        WifiHandover.this.mToConnectConfigKey = null;
                        break;
                    case 6:
                        WifiHandover.this.mNetworkBlackListManager.cleanAbnormalWifiBlacklist();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int status;
                String bssid;
                String ssid;
                String configKey;
                Bundle bundle;
                if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
                    if (WifiHandover.this.mNetwoksHandoverState == 1) {
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 1));
                    } else if (WifiHandover.this.mNetwoksHandoverState == 4) {
                        WifiHandover.this.trySwitchWifiNetwork(WifiHandover.this.selectQualifiedCandidate());
                    } else if (WifiProCommonUtils.isWifiConnected(WifiHandover.this.mWifiManager) && WifiHandover.this.mNetwoksHandoverState == 0) {
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 4));
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (WifiHandover.this.mNetwoksHandoverState == 2) {
                        if (netInfo != null && netInfo.getState() == State.CONNECTED) {
                            WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 2));
                        }
                    } else if (netInfo != null && netInfo.getState() == State.DISCONNECTED) {
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 6));
                    }
                } else if (WifiHandover.ACTION_RESPONSE_WIFI_2_WIFI.equals(intent.getAction())) {
                    if (WifiHandover.this.mNetwoksHandoverState == 3) {
                        status = intent.getIntExtra(WifiHandover.WIFI_HANDOVER_COMPLETED_STATUS, -100);
                        bssid = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID);
                        ssid = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                        configKey = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE);
                        bundle = new Bundle();
                        bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID, bssid);
                        bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID, ssid);
                        bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE, configKey);
                        WifiHandover.LOGW("ACTION_RESPONSE_WIFI_2_WIFI received, status = " + status + ", configKey = " + configKey + ", mOldConfigKey = " + WifiHandover.this.mOldConfigKey);
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 3, status, -1, bundle));
                    }
                } else if (WifiHandover.ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER.equals(intent.getAction()) && WifiHandover.this.mNetwoksHandoverState == 5) {
                    status = intent.getIntExtra(WifiHandover.WIFI_HANDOVER_COMPLETED_STATUS, -100);
                    bssid = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID);
                    ssid = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                    configKey = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE);
                    bundle = new Bundle();
                    bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID, bssid);
                    bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID, ssid);
                    bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE, configKey);
                    WifiHandover.LOGD("ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER received, status = " + status + ", ssid = " + ssid + ", configKey = " + configKey);
                    WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 5, status, -1, bundle));
                }
            }
        };
        mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, WIFI_HANDOVER_RECV_PERMISSION, null);
    }

    private void sendNetworkHandoverResult(int type, boolean status, String ssid, int errorReason) {
        if (this.mCallBack != null && type != -1) {
            LOGW("sendNetworkHandoverResult, type = " + type + ", status = " + status + " ,errorReason = " + errorReason);
            this.mCallBack.onWifiHandoverChange(type, status, ssid, errorReason);
        }
    }

    private void notifyWifiAvailableStatus(boolean status, int bestRssi, String targetSsid) {
        if (this.mCallBack == null) {
            LOGW("notifyWifiAvailableStatus, mCallBack == null");
        } else {
            this.mCallBack.onCheckAvailableWifi(status, bestRssi, targetSsid);
        }
    }

    private int notifyGoodApFound() {
        int nextRssi = INVALID_RSSI;
        String nextSsid = null;
        if (this.mWifiStateMachine != null && this.mWifiStateMachine.isScanAndManualConnectMode()) {
            return INVALID_RSSI;
        }
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            LOGW("notifyGoodApFound, WiFi scan results are invalid, getScanResults = " + scanResults);
            return INVALID_RSSI;
        }
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            LOGW("notifyGoodApFound, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
            return INVALID_RSSI;
        }
        WifiConfiguration current = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        String currentSsid = WifiProCommonUtils.getCurrentSsid(this.mWifiManager);
        String currentBssid = WifiProCommonUtils.getCurrentBssid(this.mWifiManager);
        String currentEncrypt = current != null ? current.configKey() : null;
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult nextResult = (ScanResult) scanResults.get(i);
            String scanSsid = "\"" + nextResult.SSID + "\"";
            String scanResultEncrypt = nextResult.capabilities;
            boolean sameBssid = currentBssid != null ? currentBssid.equals(nextResult.BSSID) : false;
            boolean sameConfigKey;
            if (currentSsid == null || !currentSsid.equals(scanSsid)) {
                sameConfigKey = false;
            } else {
                sameConfigKey = WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, currentEncrypt);
            }
            if (!sameBssid && !sameConfigKey && !this.mNetworkBlackListManager.isInAbnormalWifiBlacklist(nextResult.BSSID) && nextResult.level >= -75 && nextResult.level > nextRssi) {
                for (int k = 0; k < configNetworks.size(); k++) {
                    WifiConfiguration nextConfig = (WifiConfiguration) configNetworks.get(k);
                    int disableReason = nextConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                    if (((!nextConfig.noInternetAccess && !nextConfig.portalNetwork) || (WifiProCommonUtils.allowWifiConfigRecovery(nextConfig.internetHistory) ^ 1) == 0) && disableReason <= 0 && !WifiProCommonUtils.isOpenAndPortal(nextConfig) && !WifiProCommonUtils.isOpenAndMaybePortal(nextConfig) && nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, nextConfig.configKey())) {
                        nextRssi = nextResult.level;
                        int nextId = nextConfig.networkId;
                        nextSsid = nextResult.SSID;
                        break;
                    }
                }
            }
        }
        notifyWifiAvailableStatus(nextRssi != -200, nextRssi, nextSsid);
        return nextRssi;
    }

    private boolean handleScanResultsForConnectWiFi() {
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            LOGW("handleScanResultsForConnectWiFi, WiFi scan results are invalid, getScanResults = " + scanResults);
            return false;
        }
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            LOGW("handleScanResultsForConnectWiFi, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
            return false;
        }
        int nextId = -1;
        int nextRssi = INVALID_RSSI;
        boolean found = false;
        WifiConfiguration nextConfig = null;
        int i = 0;
        while (!found && i < scanResults.size()) {
            ScanResult nextResult = (ScanResult) scanResults.get(i);
            String scanSsid = "\"" + nextResult.SSID + "\"";
            if (nextResult.BSSID.equals(this.mToConnectBssid)) {
                for (int k = 0; k < configNetworks.size(); k++) {
                    nextConfig = (WifiConfiguration) configNetworks.get(k);
                    if (nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid)) {
                        nextRssi = nextResult.level;
                        nextId = nextConfig.networkId;
                        found = true;
                        break;
                    }
                }
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

    private boolean invalidConfigNetwork(WifiConfiguration config) {
        if (this.mNetwoksHandoverState == 4 && this.mSwitchBlacklist != null) {
            for (int l = 0; l < this.mSwitchBlacklist.size(); l++) {
                if (config.SSID.equals(this.mSwitchBlacklist.get(l))) {
                    LOGW("selectQualifiedCandidate, switch black list filter it, ssid = " + config.SSID);
                    return true;
                }
            }
        }
        int disableReason = config.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
        if (disableReason >= 2 && disableReason <= 8) {
            LOGW("selectQualifiedCandidate, wifi switch, ssid = " + config.SSID + ", disableReason = " + disableReason);
            return true;
        } else if (WifiProCommonUtils.isOpenAndPortal(config) || WifiProCommonUtils.isOpenAndMaybePortal(config)) {
            return true;
        } else {
            if ((config.noInternetAccess || config.portalNetwork) && (WifiProCommonUtils.allowWifiConfigRecovery(config.internetHistory) ^ 1) != 0) {
                return true;
            }
            return false;
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
        return null;
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
        if (ScanResult.is5GHz(lastResult.frequency) && ScanResult.is24GHz(currentResult.frequency) && lastResult.level < -75 && currentResult.level > lastResult.level) {
            return new WifiSwitchCandidate(currentConfig, currentResult);
        }
        if (ScanResult.is24GHz(lastResult.frequency) && ScanResult.is5GHz(currentResult.frequency) && (currentResult.level > lastResult.level || currentResult.level >= -75)) {
            return new WifiSwitchCandidate(currentConfig, currentResult);
        }
        return lastCandidate;
    }

    private WifiSwitchCandidate getAutoRoamCandidate(int currentSignalRssi, WifiSwitchCandidate bestCandidate) {
        if (!(this.mNetwoksHandoverState != 0 || bestCandidate == null || bestCandidate.bestScanResult == null)) {
            if (HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(bestCandidate.bestScanResult.level) - HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(currentSignalRssi) < 2 || bestCandidate.bestScanResult.level - currentSignalRssi < 10) {
                return null;
            }
        }
        return bestCandidate;
    }

    private WifiSwitchCandidate selectQualifiedCandidate() {
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        WifiConfiguration current = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        int currentRssi = WifiProCommonUtils.getCurrentRssi(this.mWifiManager);
        int currentLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(currentRssi);
        if (scanResults == null || scanResults.size() == 0) {
            LOGW("selectQualifiedCandidate, WiFi scan results are invalid, getScanResults = " + scanResults);
            return null;
        } else if (current == null || current.SSID == null) {
            LOGW("selectQualifiedCandidate, current connected wifi configuration is null");
            return null;
        } else if (this.mNetwoksHandoverState == 0 && currentLevel >= 4) {
            return null;
        } else {
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                LOGW("selectQualifiedCandidate, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
                return null;
            }
            String currentSsid = current.SSID;
            String currentBssid = WifiProCommonUtils.getCurrentBssid(this.mWifiManager);
            String currentEncrypt = current.configKey();
            WifiSwitchCandidate bestSwitchCandidate = null;
            WifiSwitchCandidate backupSwitchCandidate = null;
            for (int i = 0; i < scanResults.size(); i++) {
                ScanResult nextResult = (ScanResult) scanResults.get(i);
                String scanSsid = "\"" + nextResult.SSID + "\"";
                String scanResultEncrypt = nextResult.capabilities;
                boolean bssidSame = currentBssid != null ? currentBssid.equals(nextResult.BSSID) : false;
                boolean switchConfigKeySame;
                if (this.mNetwoksHandoverState == 4 && currentSsid.equals(scanSsid)) {
                    switchConfigKeySame = WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, currentEncrypt);
                } else {
                    switchConfigKeySame = false;
                }
                int idleConfigKeyDiff = this.mNetwoksHandoverState == 0 ? currentSsid.equals(scanSsid) ? WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, currentEncrypt) ^ 1 : 1 : 0;
                if (!bssidSame && !switchConfigKeySame && idleConfigKeyDiff == 0 && nextResult.level >= -75) {
                    if (!this.mNetworkBlackListManager.isInAbnormalWifiBlacklist(nextResult.BSSID)) {
                        for (int k = 0; k < configNetworks.size(); k++) {
                            boolean networkMatched;
                            WifiConfiguration nextConfig = (WifiConfiguration) configNetworks.get(k);
                            if (nextConfig == null || nextConfig.SSID == null || !nextConfig.SSID.equals(scanSsid)) {
                                networkMatched = false;
                            } else {
                                networkMatched = WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, nextConfig.configKey());
                            }
                            if (networkMatched && (invalidConfigNetwork(nextConfig) ^ 1) != 0) {
                                backupSwitchCandidate = getBackupSwitchCandidate(nextConfig, nextResult, backupSwitchCandidate);
                                bestSwitchCandidate = getBestSwitchCandidate(nextConfig, nextResult, bestSwitchCandidate);
                                break;
                            }
                        }
                    } else {
                        LOGW("selectQualifiedCandidate, scanSsid" + scanSsid + " is in black list!");
                    }
                }
            }
            if (bestSwitchCandidate == null && backupSwitchCandidate != null) {
                bestSwitchCandidate = backupSwitchCandidate;
            }
            return getAutoRoamCandidate(currentRssi, bestSwitchCandidate);
        }
    }

    private void trySwitchWifiNetwork(WifiSwitchCandidate switchCandidate) {
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
                    if (this.mWifiStatStore != null) {
                        this.mWifiStatStore.setWifiConnectType("WIFIPRO_CONNECT");
                    }
                    updateWifiSwitchTimeStamp(System.currentTimeMillis());
                    this.mWifiStateMachine.requestWifiSoftSwitch();
                    this.mWifiStateMachine.startConnectToUserSelectNetwork(best.networkId, bssid);
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
                LOGW("handleWifiToWifi, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
                return false;
            }
            this.mOldConfigKey = currConfig.configKey();
            this.mNetwoksHandoverType = 1;
            this.mNetwoksHandoverState = 4;
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
            LOGW("hasAvailableWifiNetwork, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
            return false;
        }
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        requestScan();
        return true;
    }

    private void requestScan() {
        if (this.mWifiStateMachine != null) {
            this.mWifiStateMachine.startScan(Binder.getCallingUid(), 0, null, null);
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
                LOGW("connectWifiNetwork, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
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

    public boolean handleDualBandWifiConnect(String bssid, String ssid, int authType, int switchType) {
        Log.d(TAG, "DualBandWifiConnect, ssid = " + ssid + ", authType = " + authType + ", switchType = " + switchType);
        if (bssid == null || ssid == null || switchType < 1 || switchType > 3) {
            Log.d(TAG, "DualBandWifiConnect, inputed arg is invalid, ssid = " + ssid + " , switchType = " + switchType);
            return false;
        }
        WifiInfo currWifiInfo = this.mWifiManager.getConnectionInfo();
        if (currWifiInfo == null || !bssid.equals(currWifiInfo.getBSSID())) {
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                Log.d(TAG, "DualBandWifiConnect, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
                return false;
            }
            Parcelable changeConfig = null;
            for (WifiConfiguration nextConfig : configNetworks) {
                LOGD("DualBandWifiConnect, nextConfig.SSID = " + nextConfig.SSID);
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
                return false;
            }
            Log.d(TAG, "DualBandWifiConnect, changeConfig.configKey = " + changeConfig.configKey() + ", AuthType = " + changeConfig.getAuthType());
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
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WIFI_HANDOVER_RECV_PERMISSION);
            return true;
        }
        Log.d(TAG, "DualBandWifiConnect, already connected, ignore it.");
        return true;
    }

    public int getNetwoksHandoverType() {
        return this.mNetwoksHandoverType;
    }

    private boolean isValidConfig(WifiConfiguration config) {
        boolean z = true;
        if (config == null) {
            return false;
        }
        int cc = config.allowedKeyManagement.cardinality();
        Log.e(TAG, "config isValid cardinality=" + cc);
        if (cc > 1) {
            z = false;
        }
        return z;
    }

    private static void setWifiproContext(Context context) {
        mContext = context;
    }

    public static boolean isWifiProEnabled() {
        return WifiProCommonUtils.isWifiProSwitchOn(mContext);
    }

    private static void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    private static void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
