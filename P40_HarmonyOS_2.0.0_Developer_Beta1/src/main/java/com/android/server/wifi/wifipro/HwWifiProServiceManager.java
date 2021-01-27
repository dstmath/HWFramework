package com.android.server.wifi.wifipro;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Messenger;
import android.provider.Settings;
import android.util.Log;
import com.android.server.wifipro.WifiProCommonUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HwWifiProServiceManager {
    public static final String ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND = "com.huawei.wifipro.action.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND";
    public static final String ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND = "com.huawei.wifipro.action.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND";
    public static final String ACTION_NOTIFY_PORTAL_OUT_OF_RANGE = "com.huawei.wifipro.action.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE";
    public static final String ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY = "com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY";
    public static final String ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER = "com.huawei.wifi.action.REQUEST_DUAL_BAND_WIFI_HANDOVER";
    public static final String ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER = "com.huawei.wifi.action.RESPONSE_DUAL_BAND_WIFI_HANDOVER";
    public static final String ACTION_RESPONSE_WIFI_2_WIFI = "com.huawei.wifi.action.RESPONSE_WIFI_2_WIFI";
    public static final boolean FAILURE_BOOLEAN_RESULT = false;
    public static final int FAILURE_INT_RESULT = -1;
    public static final String FAILURE_STRING_RESULT = null;
    public static final int HANDOVER_STATUS_CONNECT_AUTH_FAILED = -7;
    public static final int HANDOVER_STATUS_CONNECT_REJECT_FAILED = -6;
    public static final int HANDOVER_STATUS_OK = 0;
    public static final int MSG_BQE_DETECTION_RESULT = 10;
    public static final int MSG_WIFI_UPDATE_SCAN_RESULT = 7;
    public static final int NETWORK_HANDOVER_TYPE_DUAL_BAND_WIFI_CONNECT = 4;
    public static final int NETWORK_HANDOVER_TYPE_WIFI_TO_WIFI = 1;
    public static final int NETWORK_PROPERTY_INTERNET = 5;
    public static final int SCE_WIFI_CONNECT_STATE = 3;
    public static final int SCE_WIFI_CONNET_RETRY = 1;
    public static final int SCE_WIFI_OFF_STATE = 1;
    public static final int SCE_WIFI_ON_STATE = 2;
    public static final int SCE_WIFI_REASSOC_STATE = 4;
    public static final int SCE_WIFI_RECONNECT_STATE = 5;
    public static final int SCE_WIFI_STATUS_ABRORT = -3;
    public static final int SCE_WIFI_STATUS_ASSOC_REJECT = -5;
    public static final int SCE_WIFI_STATUS_FAIL = -1;
    public static final int SCE_WIFI_STATUS_LOST = -2;
    public static final int SCE_WIFI_STATUS_SUCC = 0;
    public static final int SCE_WIFI_STATUS_UNKOWN = 1;
    public static final int SELFCURE_WIFI_CONNECT_TIMEOUT = 15000;
    public static final int SELFCURE_WIFI_OFF_TIMEOUT = 2000;
    public static final int SELFCURE_WIFI_ON_TIMEOUT = 3000;
    public static final int SELFCURE_WIFI_REASSOC_TIMEOUT = 12000;
    public static final int SELFCURE_WIFI_RECONNECT_TIMEOUT = 15000;
    private static final String TAG = "HwWifiProServiceManager";
    public static final int WAVE_MAPPING_USER_PREFER_NW = 0;
    private static final String WIFIPRO_SERVICE_ACTION = "com.huawei.hwwifiproservice.WIFIPRO_SERVICE";
    private static final String WIFIPRO_SERVICE_NAME = "com.huawei.hwwifiproservice.HwWifiProService";
    private static final String WIFIPRO_SERVICE_PKG = "com.huawei.hwwifiproservice";
    public static final int WIFIPRO_SOFT_CONNECT_FAILED = -4;
    public static final int WIFI_BACKGROUND_AP_SCORE = 1;
    public static final int WIFI_BACKGROUND_IDLE = 0;
    public static final int WIFI_BACKGROUND_INTERNET_RECOVERY_CHECKING = 3;
    public static final int WIFI_BACKGROUND_PORTAL_CHECKING = 2;
    public static final String WIFI_HANDOVER_COMPLETED_STATUS = "com.huawei.wifi.handover.status";
    public static final String WIFI_HANDOVER_NETWORK_BSSID = "com.huawei.wifi.handover.bssid";
    public static final String WIFI_HANDOVER_NETWORK_CONFIGKYE = "com.huawei.wifi.handover.configkey";
    public static final String WIFI_HANDOVER_NETWORK_SSID = "com.huawei.wifi.handover.ssid";
    public static final String WIFI_HANDOVER_NETWORK_SWITCHTYPE = "com.huawei.wifi.handover.switchtype";
    public static final String WIFI_HANDOVER_NETWORK_WIFICONFIG = "com.huawei.wifi.handover.wificonfig";
    public static final String WIFI_HANDOVER_RECV_PERMISSION = "com.huawei.wifipro.permission.RECV.WIFI_HANDOVER";
    private static HwWifiProServiceManager mHwWifiProServiceManager;
    private IHwSelfCureService iHwSelfCureService = null;
    private IHwWifiProService iHwWifiProService = null;
    private boolean isHwAutoConnectManagerStarted = false;
    private boolean isHwDualBandManagerStarted = false;
    private boolean isHwSelfCureServiceStarted = false;
    private boolean isHwWifiProServiceInitCompleted = false;
    private boolean isWifiProStateMachineStarted = false;
    private Context mContext = null;
    private Object mHwSelfCureService;
    private Object mHwWifiProService;
    private Messenger mMessenger;
    private Intent mServceIntent;

    public HwWifiProServiceManager(Context context) {
        this.mContext = context.getApplicationContext();
        if (this.mContext == null) {
            this.mContext = context;
        }
    }

    public static HwWifiProServiceManager createHwWifiProServiceManager(Context context) {
        if (mHwWifiProServiceManager == null) {
            mHwWifiProServiceManager = new HwWifiProServiceManager(context);
        }
        return mHwWifiProServiceManager;
    }

    public static HwWifiProServiceManager getHwWifiProServiceManager() {
        return mHwWifiProServiceManager;
    }

    public void createHwWifiProService(Context context) {
        Log.i(TAG, "createHwWifiProService");
        if (isHwWifiProServiceExist()) {
            Settings.Global.putString(this.mContext.getContentResolver(), "hw_wifipro_enable", "true");
            initHwWifiProService();
        } else if (isInitHwSelfCureService()) {
            if (WifiProCommonUtils.isWifiProPropertyEnabled(context)) {
                Log.w(TAG, "hwWifiPro-Service not exist, do not display wifipro switch.");
                Settings.Global.putString(this.mContext.getContentResolver(), "hw_wifipro_enable", "lite");
            }
            completeHwSelfcureServiceImpl();
        } else {
            Log.w(TAG, "hwWifiPro-Service and hwSelfCure not exist, do not display wifipro switch.");
            Settings.Global.putString(this.mContext.getContentResolver(), "hw_wifipro_enable", "false");
        }
    }

    private boolean isHwWifiProServiceExist() {
        if (this.mHwWifiProService != null) {
            return true;
        }
        try {
            this.mHwWifiProService = Class.forName(WIFIPRO_SERVICE_NAME).getConstructor(Context.class).newInstance(this.mContext);
            if (this.mHwWifiProService != null) {
                Log.i(TAG, "successes to get HwWifiProService newInstance.");
                return true;
            }
            Log.e(TAG, "fail to get HwWifiProService, HwWifiProService is not exist.");
            return false;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            Log.e(TAG, "isHwWifiProServiceExist class or method not found.");
            return false;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e2) {
            Log.e(TAG, "isHwWifiProServiceExist newInstance expression is illegal.");
            return false;
        }
    }

    private void initHwWifiProService() {
        invokeMethodViaReflection(this.mHwWifiProService, "initHwWifiProService");
    }

    private Object invokeMethodViaReflection(Object object, String methodName) {
        if (object == null || methodName == null) {
            return null;
        }
        Method method = null;
        try {
            method = object.getClass().getMethod(methodName, new Class[0]);
        } catch (NoSuchMethodException | SecurityException e) {
            Log.e(TAG, "invokeMethodViaReflection NoSuchMethod = " + methodName);
        }
        if (method != null) {
            try {
                return method.invoke(object, new Object[0]);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
                Log.e(TAG, "invokeMethodViaReflection happen error.");
                return null;
            }
        } else {
            Log.e(TAG, "invokeMethodViaReflection invoke, Method is null.");
            return null;
        }
    }

    private boolean isInitHwSelfCureService() {
        if (this.mHwSelfCureService != null) {
            return true;
        }
        try {
            this.mHwSelfCureService = Class.forName("com.huawei.hwwifiproservice.HwSelfCureService").getConstructor(Context.class).newInstance(this.mContext);
            if (this.mHwSelfCureService != null) {
                Log.v(TAG, "successes to get HwSelfCureService object.");
                return true;
            }
            Log.e(TAG, "fail to init HwSelfCureService.");
            return false;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            Log.e(TAG, "isInitHwSelfCureService class or method not found.");
            return false;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e2) {
            Log.e(TAG, "isInitHwSelfCureService newInstance expression is illegal.");
            return false;
        }
    }

    private void completeHwSelfcureServiceImpl() {
        Object hwSelfCureServiceImpl = invokeMethodViaReflection(this.mHwSelfCureService, "getHwSelfCureServiceManager");
        if (hwSelfCureServiceImpl == null) {
            Log.e(TAG, "completeHwSelfcureServiceImpl failed.");
        } else if (hwSelfCureServiceImpl instanceof IHwSelfCureService) {
            this.iHwSelfCureService = (IHwSelfCureService) hwSelfCureServiceImpl;
            this.isHwSelfCureServiceStarted = true;
        }
    }

    private void completeHwWifiProServiceImpl() {
        Object wifiProServiceImpl = invokeMethodViaReflection(this.mHwWifiProService, "getHwWifiProServiceManager");
        if (wifiProServiceImpl == null) {
            Log.e(TAG, "completeHwWifiProServiceImpl failed.");
            return;
        }
        if (wifiProServiceImpl instanceof IHwWifiProService) {
            this.iHwWifiProService = (IHwWifiProService) wifiProServiceImpl;
        }
        if (wifiProServiceImpl instanceof IHwSelfCureService) {
            this.iHwSelfCureService = (IHwSelfCureService) wifiProServiceImpl;
        }
    }

    public void initWifiproProperty(Bundle bundle) {
        if (bundle == null) {
            this.isWifiProStateMachineStarted = false;
            this.isHwAutoConnectManagerStarted = false;
            this.isHwDualBandManagerStarted = false;
            this.isHwSelfCureServiceStarted = false;
            return;
        }
        this.isWifiProStateMachineStarted = bundle.getBoolean("isWifiProStateMachineNotNull", false);
        this.isHwAutoConnectManagerStarted = bundle.getBoolean("isHwAutoConnectManagerNotNull", false);
        this.isHwDualBandManagerStarted = bundle.getBoolean("isHwDualBandManagerNotNull", false);
        this.isHwSelfCureServiceStarted = bundle.getBoolean("isHwSelfCureEngineNotNull", false);
    }

    private boolean bindToServiceInternal(Context context) {
        if (this.mContext == null) {
            Log.e(TAG, "mContext == null");
            return false;
        }
        completeHwWifiProServiceImpl();
        return true;
    }

    public boolean bindToService(Context context) {
        this.isHwWifiProServiceInitCompleted = true;
        return bindToServiceInternal(context);
    }

    public long getRttDuration(int uid, int type) {
        if (!isWifiProServiceReady()) {
            return -1;
        }
        return this.iHwWifiProService.getRttDuration(uid, type);
    }

    public long getRttSegs(int uid, int type) {
        if (!isWifiProServiceReady()) {
            return -1;
        }
        return this.iHwWifiProService.getRttSegs(uid, type);
    }

    public boolean isWifiProStateMachineStarted() {
        if (!isWifiProServiceReady()) {
            return false;
        }
        return this.isWifiProStateMachineStarted;
    }

    public void userHandoverWifi() {
        if (isWifiProServiceReady() && this.isWifiProStateMachineStarted) {
            this.iHwWifiProService.userHandoverWifi();
        }
    }

    public void setWifiApEvaluateEnabled(boolean enablen) {
        if (isWifiProServiceReady() && this.isWifiProStateMachineStarted) {
            this.iHwWifiProService.setWifiApEvaluateEnabled(enablen);
        }
    }

    public int getNetwoksHandoverType() {
        if (!isWifiProServiceReady() || !this.isWifiProStateMachineStarted) {
            return -1;
        }
        return this.iHwWifiProService.getNetwoksHandoverType();
    }

    public void notifyNetworkUserConnect(boolean isUserConnect) {
        if (isWifiProServiceReady() && this.isWifiProStateMachineStarted) {
            this.iHwWifiProService.notifyNetworkUserConnect(isUserConnect);
        }
    }

    public void notifyApkChangeWifiStatus(boolean enable, String packageName) {
        if (isWifiProServiceReady() && this.isWifiProStateMachineStarted) {
            this.iHwWifiProService.notifyApkChangeWifiStatus(enable, packageName);
        }
    }

    public void notifyWifiDisconnected(Intent intent) {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyWifiDisconnected(intent);
        }
    }

    public boolean isWifiEvaluating() {
        if (!isWifiProServiceReady() || !this.isWifiProStateMachineStarted) {
            return false;
        }
        return this.iHwWifiProService.isWifiEvaluating();
    }

    public void notifyUseFullChannels() {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyUseFullChannels();
        }
    }

    public List<Integer> getScanfrequencys() {
        if (!isWifiProServiceReady()) {
            return null;
        }
        List<Integer> result = null;
        List<String> frequencyStrs = this.iHwWifiProService.getScanfrequencys();
        if (frequencyStrs != null && frequencyStrs.size() > 0) {
            result = new ArrayList<>(frequencyStrs.size());
            try {
                for (String strItem : frequencyStrs) {
                    result.add(Integer.valueOf(Integer.parseInt(strItem)));
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Exception happend in getScanfrequencys()");
            }
        }
        return result;
    }

    public void handleWiFiDisconnected() {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.handleWiFiDisconnected();
        }
    }

    public void notifyWifiConnectedBackground() {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyWifiConnectedBackground();
        }
    }

    public void handleWiFiConnected(WifiConfiguration currentWifiConfig, boolean cellIdChanged) {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.handleWiFiConnected(currentWifiConfig, cellIdChanged);
        }
    }

    public void notifyNetworkRoamingCompleted(String newBssid) {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyNetworkRoamingCompleted(newBssid);
        }
    }

    public void disconnectePoorWifi() {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.disconnectePoorWifi();
        }
    }

    public void notifyForegroundAppChanged(String packageName) {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyForegroundAppChanged(packageName);
        }
    }

    public void notifyWifiMonitorDisconnected() {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyWifiMonitorDisconnected();
        }
    }

    public void notifyWifiRoamingStarted() {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyWifiRoamingStarted();
        }
    }

    public void notifyWifiConnectivityRoamingCompleted() {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyWifiConnectivityRoamingCompleted();
        }
    }

    public boolean isHwAutoConnectManagerStarted() {
        if (!isWifiProServiceReady()) {
            return false;
        }
        return this.isHwAutoConnectManagerStarted;
    }

    public boolean isPortalNotifyOn() {
        if (!isWifiProServiceReady()) {
            return false;
        }
        return this.iHwWifiProService.isPortalNotifyOn();
    }

    public boolean isAutoJoinAllowedSetTargetBssid(WifiConfiguration candidate, String scanResultBssid) {
        if (!isWifiProServiceReady()) {
            return false;
        }
        return this.iHwWifiProService.isAutoJoinAllowedSetTargetBssid(candidate, scanResultBssid);
    }

    public void releaseBlackListBssid(WifiConfiguration config, boolean enable) {
        if (isWifiProServiceReady() && this.isHwAutoConnectManagerStarted) {
            this.iHwWifiProService.releaseBlackListBssid(config, enable);
        }
    }

    public void notifyAutoConnectManagerDisconnected() {
        if (!isWifiProServiceReady() || !this.isHwAutoConnectManagerStarted) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "HW_WIFI_PORTAL_FLAG", 0);
        } else {
            this.iHwWifiProService.notifyAutoConnectManagerDisconnected();
        }
    }

    public void notifyWifiConnFailedInfo(WifiConfiguration selectedConfig, String bssid, int rssi, int reason) {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyWifiConnFailedInfo(selectedConfig, bssid, rssi, reason);
        }
    }

    public void notifyEnableSameNetworkId(int netId) {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyEnableSameNetworkId(netId);
        }
    }

    public boolean allowAutoJoinDisabledNetworkAgain(WifiConfiguration config) {
        if (!isWifiProServiceReady()) {
            return false;
        }
        return this.iHwWifiProService.allowAutoJoinDisabledNetworkAgain(config);
    }

    public String getCurrentPackageNameFromWifiPro() {
        String result = FAILURE_STRING_RESULT;
        if (!isWifiProServiceReady()) {
            return result;
        }
        return this.iHwWifiProService.getCurrentPackageNameFromWifiPro();
    }

    public boolean isBssidMatchedBlacklist(String bssid) {
        if (!isWifiProServiceReady() || !this.isHwAutoConnectManagerStarted) {
            return false;
        }
        return this.iHwWifiProService.isBssidMatchedBlacklist(bssid);
    }

    public boolean allowCheckPortalNetwork(String configKey, String bssid) {
        if (!isWifiProServiceReady() || !this.isHwAutoConnectManagerStarted) {
            return false;
        }
        return this.iHwWifiProService.allowCheckPortalNetwork(configKey, bssid);
    }

    public void updatePopUpNetworkRssi(String configKey, int maxRssi) {
        if (isWifiProServiceReady() && this.isHwAutoConnectManagerStarted) {
            this.iHwWifiProService.updatePopUpNetworkRssi(configKey, maxRssi);
        }
    }

    public void setWiFiProScanResultList(List<ScanResult> list) {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.setWiFiProScanResultList(list);
        }
    }

    public List<ScanResult> updateScanResultByWifiPro(List<ScanResult> scanResults) {
        if (!isHwSelfCureServiceStarted()) {
            return null;
        }
        return this.iHwSelfCureService.updateScanResultByWifiPro(scanResults);
    }

    public ScanResult updateScanDetailByWifiPro(ScanResult scanResult) {
        if (!isHwSelfCureServiceStarted()) {
            return null;
        }
        ScanResult scResult = this.iHwSelfCureService.updateScanDetailByWifiPro(scanResult);
        if (scResult != null) {
            scanResult.internetAccessType = scResult.internetAccessType;
            scanResult.networkQosLevel = scResult.networkQosLevel;
            scanResult.networkSecurity = scResult.networkSecurity;
            scanResult.networkQosScore = scResult.networkQosScore;
        }
        return scResult;
    }

    public boolean isHwDualBandManagerStarted() {
        if (!isWifiProServiceReady()) {
            return false;
        }
        return this.isHwDualBandManagerStarted;
    }

    public boolean isDualbandScanning() {
        if (!isWifiProServiceReady() || !this.isHwDualBandManagerStarted) {
            return false;
        }
        return this.iHwWifiProService.isDualbandScanning();
    }

    public boolean isHwSelfCureEngineStarted() {
        return isHwSelfCureServiceStarted();
    }

    public boolean isHwSelfCureServiceStarted() {
        if (this.iHwSelfCureService != null && this.isHwSelfCureServiceStarted) {
            return true;
        }
        Log.e(TAG, "iHwSelfCureService is null obj or HwSelfCureService isn't started.");
        return false;
    }

    public boolean isDhcpFailedBssid(String bssid) {
        if (!isHwSelfCureServiceStarted()) {
            return false;
        }
        return this.iHwSelfCureService.isDhcpFailedBssid(bssid);
    }

    public boolean isDhcpFailedConfigKey(String configKey) {
        if (!isHwSelfCureServiceStarted()) {
            return false;
        }
        return this.iHwSelfCureService.isDhcpFailedConfigKey(configKey);
    }

    public void notifyDhcpResultsInternetOk(String dhcpResults) {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.notifyDhcpResultsInternetOk(dhcpResults);
        }
    }

    public void notifySelfCureWifiConnectedBackground() {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.notifySelfCureWifiConnectedBackground();
        }
    }

    public void notifySelfCureWifiDisconnected() {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.notifySelfCureWifiDisconnected();
        }
    }

    public void notifySelfCureWifiScanResultsAvailable(boolean bAvailable) {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.notifySelfCureWifiScanResultsAvailable(bAvailable);
        }
    }

    public void notifySelfCureWifiRoamingCompleted(String newBssid) {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.notifySelfCureWifiRoamingCompleted(newBssid);
        }
    }

    public void notifySelfCureIpConfigCompleted() {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.notifySelfCureIpConfigCompleted();
        }
    }

    public boolean notifySelfCureIpConfigLostAndHandle(WifiConfiguration config) {
        if (!isHwSelfCureServiceStarted()) {
            return false;
        }
        return this.iHwSelfCureService.notifySelfCureIpConfigLostAndHandle(config);
    }

    public void requestChangeWifiStatus(boolean enable) {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.requestChangeWifiStatus(enable);
        }
    }

    public void notifySefCureCompleted(int status) {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.notifySefCureCompleted(status);
        }
    }

    public void sendMessageToHwDualBandStateMachine(int message) {
        if (isWifiProServiceReady() && this.isHwDualBandManagerStarted) {
            this.iHwWifiProService.sendMessageToHwDualBandStateMachine(message);
        }
    }

    public void notifyFirstConnectProbeResult(int respCode) {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.notifyFirstConnectProbeResult(respCode);
        }
    }

    public void notifyTcpStatResult(List<String> list) {
        if (isWifiProServiceReady() && this.isWifiProStateMachineStarted) {
            this.iHwWifiProService.notifyTcpStatResult(list);
        }
    }

    public Bundle getWifiDisplayInfo(NetworkInfo networkInfo) {
        if (!isWifiProServiceReady() || !this.isWifiProStateMachineStarted) {
            return new Bundle();
        }
        return this.iHwWifiProService.getWifiDisplayInfo(networkInfo);
    }

    public void notifyChrEvent(int eventId, String apType, String ssid, int freq) {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.notifyChrEvent(eventId, apType, ssid, freq);
        }
    }

    public int getNetworkProbeRuslt(boolean isReconfirm) {
        if (!isHwSelfCureServiceStarted()) {
            return -1;
        }
        return this.iHwSelfCureService.getNetworkProbeRuslt(isReconfirm);
    }

    public void notifyScanGenieEvent(int eventId) {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.notifyScanGenieEvent(eventId);
        }
    }

    public void updateDualBandSwitchEvent() {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.updateDualBandSwitchEvent();
        }
    }

    public void uploadDisconnectedEvent(String eventType) {
        if (isHwSelfCureServiceStarted()) {
            this.iHwSelfCureService.uploadDisconnectedEvent(eventType);
        }
    }

    public void setWifiSwitchForbiddenFromApp(boolean enable) {
        if (isWifiProServiceReady()) {
            this.iHwWifiProService.setWifiSwitchForbiddenFromApp(enable);
        }
    }

    public boolean isWifiProServiceReady() {
        if (this.iHwWifiProService != null && this.isHwWifiProServiceInitCompleted) {
            return true;
        }
        Log.e(TAG, "iHwWifiProService is null obj or Service initialization isn't completed ");
        return false;
    }
}
