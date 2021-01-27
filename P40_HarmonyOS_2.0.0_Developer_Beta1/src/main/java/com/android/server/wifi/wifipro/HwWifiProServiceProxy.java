package com.android.server.wifi.wifipro;

import android.content.Context;
import android.net.DhcpResults;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.arp.HwMultiGw;
import android.net.dhcp.HwArpClient;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.server.HiLinkUtil;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.hidata.arbitration.HwArbitrationManager;
import com.android.server.policy.AbsPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wifi.ABS.HwAbsDetectorService;
import com.android.server.wifi.ABS.HwAbsUtils;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.HwArpUtils;
import com.android.server.wifi.HwPortalExceptionManager;
import com.android.server.wifi.HwQoE.HwQoEJNIAdapter;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.LAA.HwLaaController;
import com.android.server.wifi.LAA.HwLaaUtils;
import com.android.server.wifi.SavedNetworkEvaluator;
import com.android.server.wifi.ScanRequestProxy;
import com.android.server.wifi.WifiConnectivityManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiStateMachineUtils;
import com.android.server.wifi.cast.CastOptManager;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HwWifiProServiceProxy {
    private static final String ARP_SUMMERY_SPEND_TIME = "spendTime";
    private static final int DEFAULT_ARP_TIMEOUT_MS = 1000;
    private static final int DEFAULT_GATEWAY_NUMBER = 1;
    private static final int DEFAULT_RSSI_PACKET_COUNT_INFO = 0;
    private static final String EMPTY_STRING = "";
    private static final int GRATUITOUS_ARP_TIMEOUT_MS = 100;
    private static final String HW_DUAL_BAND_MANAGER = "HwDualBandManager";
    private static final int INTERFACE_CALL_HWPORTALEXCEPTIONMANAGER_INIT = 29;
    private static final int INTERFACE_CALL_HWPORTALEXCEPTIONMANAGER_NOTIFY_DISCONNETCTED = 42;
    private static final int INTERFACE_CALL_SAVEDNETWORKEVALUATOR_NOTIFY_PORTAL_CHANGED = 43;
    private static final int INTERFACE_DEAUTH_ROAMING_BSSID = 59;
    private static final int INTERFACE_DEL_STATIC_ARP = 58;
    private static final int INTERFACE_DO_GATWAY_ARP_TEST = 75;
    private static final int INTERFACE_DO_GRATUITOUSARP = 67;
    private static final int INTERFACE_DO_SLOW_ARP_TEST = 76;
    private static final int INTERFACE_GET_AP_VENDOR_INFO = 1;
    private static final int INTERFACE_GET_AVAILABLE_CHANNELS = 13;
    private static final int INTERFACE_GET_CAST_OPT_INFO = 87;
    private static final int INTERFACE_GET_DHCP_RESULTS = 46;
    private static final int INTERFACE_GET_GATEWAY_ARP_RESULT = 68;
    private static final int INTERFACE_GET_GW_ADDR = 70;
    private static final int INTERFACE_GET_HISTORY_SCAN_RESULTS = 86;
    private static final int INTERFACE_GET_NETWORK_FOR_TYPE_WIFI = 14;
    private static final int INTERFACE_GET_RSSI_PACKET_COUNT_INFO = 80;
    private static final int INTERFACE_GET_SCAN_RESULTS = 74;
    private static final int INTERFACE_GET_SCN_RESULTS_FROM_WSM = 12;
    private static final int INTERFACE_GET_WIFI6_WITHOUT_HTC_ARP_RESULT = 84;
    private static final int INTERFACE_GET_WIFI6_WITH_HTC_ARP_RESULT = 83;
    private static final int INTERFACE_GET_WIFISTATEMACHINE_MESSENGER = 78;
    private static final int INTERFACE_GET_WIFI_INFO = 52;
    private static final int INTERFACE_GET_WIFI_INVISABLE_INFO = 81;
    private static final int INTERFACE_GET_WIFI_OTA_INFO = 85;
    private static final int INTERFACE_GET_WIFI_PREFERENCE_FROM_HIDATA = 25;
    private static final int INTERFACE_HANDLE_CONNECTION_STATE_CHANGED = 77;
    private static final int INTERFACE_HANDLE_INVALID_IPADDR = 49;
    private static final int INTERFACE_HANDLE_NO_INTERNET_IP = 45;
    private static final int INTERFACE_INCR_ACCESS_WEB_RECORD = 32;
    private static final int INTERFACE_IS_BSSID_DISABLED = 30;
    private static final int INTERFACE_IS_FULL_SCREEN = 9;
    private static final int INTERFACE_IS_HILINK_UNCONFIG_ROUTER = 34;
    private static final int INTERFACE_IS_HWARBITRATIONMANAGER_NOT_NULL = 24;
    private static final int INTERFACE_IS_IN_GAME_ADN_NEED_DISC = 27;
    private static final int INTERFACE_IS_REACHABLEBY_ICMP = 17;
    private static final int INTERFACE_IS_SCAN_AND_MANUAL_CONNECT_MODE = 18;
    private static final int INTERFACE_IS_WIFIPRO_EVALUATING_AP = 44;
    private static final int INTERFACE_IS_WIFI_RESTRICTED = 11;
    private static final int INTERFACE_MULTI_GATEWAY = 71;
    private static final int INTERFACE_NOTIFY_HWWIFIPROSERVICE_ACCOMPLISHED = 0;
    private static final int INTERFACE_NOTIFY_PORTAL_AUTHEN_STATUS = 41;
    private static final int INTERFACE_NOTIFY_PORTAL_CONNECTED_INFO = 5;
    private static final int INTERFACE_NOTIFY_SELF_ENGINE_RESET_COMPLETE = 62;
    private static final int INTERFACE_NOTIFY_SELF_ENGINE_STATE_END = 72;
    private static final int INTERFACE_NOTIFY_SELF_ENGINE_STATE_START = 69;
    private static final int INTERFACE_PIGN_GATWAY = 66;
    private static final int INTERFACE_PORTAL_NOTIFY_CHANGED = 33;
    private static final int INTERFACE_QUERY_11VROAMING_NETWORK = 26;
    private static final int INTERFACE_QUERY_BQE_RTT_RESULT = 7;
    private static final int INTERFACE_READ_TCP_STAT_LINES = 79;
    private static final int INTERFACE_REQUEST_REASSOC_LINK = 50;
    private static final int INTERFACE_REQUEST_RENEW_DHCP = 48;
    private static final int INTERFACE_REQUEST_RESET_WIFI = 51;
    private static final int INTERFACE_REQUEST_UPDATE_DNS_SERVERS = 54;
    private static final int INTERFACE_REQUEST_USE_STATIC_IPCONFIG = 53;
    private static final int INTERFACE_REQUEST_WIFI_SOFT_SWITCH = 21;
    private static final int INTERFACE_RESET_IPCONFIG_STATUS = 47;
    private static final int INTERFACE_RESET_WLAN_RTT = 6;
    private static final int INTERFACE_SEND_MESSAGE_TO_WIFISTATEMACHINE = 28;
    private static final int INTERFACE_SEND_QOE_CMD = 8;
    private static final int INTERFACE_SET_LAA_ENABLED = 31;
    private static final int INTERFACE_SET_STATIC_ARP = 57;
    private static final int INTERFACE_SET_WIFI_BACKGROUND_REASON = 55;
    private static final int INTERFACE_START_CONNECT_TO_USER_SELECT_NETWORK = 22;
    private static final int INTERFACE_START_CUSTOMIZED_SCAN = 10;
    private static final int INTERFACE_START_PROXY_SCAN = 23;
    private static final int INTERFACE_START_ROAM_TO_NETWORK = 20;
    private static final int INTERFACE_START_SCAN = 73;
    private static final int INTERFACE_START_WIFI2WIFI_REQUEST = 19;
    private static final int INTERFACE_UPDATE_ACCESS_WEB_EXCEPTION = 60;
    private static final int INTERFACE_UPDATE_AP_VENDOR_INFO = 3;
    private static final int INTERFACE_UPDATE_ARP_SUMMERY = 63;
    private static final int INTERFACE_UPDATE_CONNECT_TYPE = 15;
    private static final int INTERFACE_UPDATE_EVALUATE_SCAN_RESULT = 82;
    private static final int INTERFACE_UPDATE_SOFTAP_CONFIG_FILE = 99;
    private static final int INTERFACE_UPDATE_VPN_STATE_CHANGED = 36;
    private static final int INTERFACE_UPDATE_WIFI_CONNECTION_MODE = 35;
    private static final int INTERFACE_UPDATE_WIFI_EXCEPTION = 4;
    private static final int INTERFACE_UPDATE_WIFI_SWITCH_TIME_STAMP = 16;
    private static final int INTERFACE_UPFATE_SC_CHR_COUNT = 61;
    private static final int INTERFACE_UPLOAD_DFT_EVENT = 2;
    private static final String KEYWORD_MESSAGE_ARG = "messageArg";
    private static final String RSSI_PACKET_COUNT_INFO = "rssiPacketCountInfo";
    private static final String SERVICE_NAME = "WIFIPRO_SERVICE";
    private static final String TAG = "HwWifiProServiceProxy";
    private static final String TCP_STAT_PATH = "proc/net/wifipro_tcp_stat";
    private static final String US_ASCII = "US-ASCII";
    private static final int WIFI6_ARP_TIMEOUT_MS = 500;
    private static final int WIFI6_HTC_ARP_TIMEOUT_MS = 300;
    private static HwWifiProServiceProxy mHwWifiProServiceProxy;
    private static WifiStateMachineUtils wifiStateMachineUtils = EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class);
    private HwArpClient mArpClient;
    private Context mContext;
    private final CustomizedScanListener mCustomizedScanListener = new CustomizedScanListener();
    private HwArpUtils mHwArpUtils;
    private HwMultiGw mHwMultiGw;
    private SavedNetworkEvaluator mSavedNetworkEvaluator;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    private ClientModeImpl mWifiStateMachine;
    private Messenger mWifiStateMachineMessenger;

    private HwWifiProServiceProxy(Context context) {
        this.mContext = context;
        this.mWifiStateMachine = WifiInjector.getInstance().getClientModeImpl();
        this.mSavedNetworkEvaluator = WifiInjector.getInstance().getSavedNetworkEvaluator();
        this.mHwArpUtils = new HwArpUtils(this.mContext);
        this.mArpClient = new HwArpClient(this.mContext);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
    }

    public static HwWifiProServiceProxy createHwWifiProServiceProxy(Context context) {
        if (mHwWifiProServiceProxy == null) {
            mHwWifiProServiceProxy = new HwWifiProServiceProxy(context);
        }
        return mHwWifiProServiceProxy;
    }

    public static HwWifiProServiceProxy getHwWifiProServiceProxy(Context context) {
        return createHwWifiProServiceProxy(context);
    }

    public Bundle ctrlHwWifiNetwork(String pkgName, int interfaceId, Bundle data) {
        Log.i(TAG, "ctrlHwWifiNetwork pkgName: " + pkgName + " interfaceId: " + interfaceId);
        if (!enforceCanAccessInterfacePermission(pkgName)) {
            Log.i(TAG, "The pkgName has no permission to invoke the interface");
            return null;
        } else if (interfaceId != INTERFACE_UPDATE_SOFTAP_CONFIG_FILE) {
            switch (interfaceId) {
                case 0:
                    notifyHwWifiProServiceAccomplished(data);
                    return null;
                case 1:
                    return getApVendorInfo();
                case 2:
                    uploadDftEvent(data);
                    return null;
                case 3:
                    updateApVendorInfo(data);
                    return null;
                case 4:
                    updateWifiException(data);
                    return null;
                case 5:
                    notifyPortalConnectedInfo(data);
                    return null;
                case 6:
                    resetWlanRtt(data);
                    return null;
                case 7:
                    return queryBqeRttResult(data);
                case 8:
                    return sendQoeCmd(data);
                case 9:
                    return isFullscreen();
                case 10:
                    startCustomizedScan(data);
                    return null;
                case 11:
                    return isWifiRestricted(data);
                case 12:
                    return getScanResultsFromWsm();
                case 13:
                    return getAvailableChannels(data);
                case 14:
                    return getNetworkForTypeWifi();
                case 15:
                    updateConnectType(data);
                    return null;
                case 16:
                    updateWifiSwitchTimeStamp(data);
                    return null;
                case 17:
                    return isNetworkReachableByIcmp(data);
                case 18:
                    return isScanAndManualConnectMode();
                case 19:
                    startWifi2WifiRequest();
                    return null;
                case 20:
                    startRoamToNetwork(data);
                    return null;
                case 21:
                    requestWifiSoftSwitch();
                    return null;
                case 22:
                    startConnectToUserSelectNetwork(data);
                    return null;
                case 23:
                    startProxyScan(data);
                    return null;
                case 24:
                    return isHwArbitrationManagerNotNull();
                case 25:
                    return getWifiPreferenceFromHiData();
                case 26:
                    query11vRoamingNetwork(data);
                    return null;
                case 27:
                    return isInGameAndNeedDisc();
                case 28:
                    sendMessageToWifiStateMachine(data);
                    return null;
                case 29:
                    callHwPortalExceptionManagerInit();
                    return null;
                case 30:
                    return isBssidDisabled(data);
                case 31:
                    setLaaEnabled(data);
                    return null;
                case 32:
                    incrAccessWebRecord(data);
                    return null;
                case 33:
                    portalNotifyChanged(data);
                    return null;
                case 34:
                    return isHiLinkUnconfigRouter(data);
                case 35:
                    updateWifiConnectionMode(data);
                    return null;
                case 36:
                    updateVnpStateChanged(data);
                    return null;
                default:
                    switch (interfaceId) {
                        case 41:
                            notifyPortalAuthenStatus(data);
                            return null;
                        case INTERFACE_CALL_HWPORTALEXCEPTIONMANAGER_NOTIFY_DISCONNETCTED /* 42 */:
                            callHwPortalExceptionManagerNotifyDisconnected();
                            return null;
                        case INTERFACE_CALL_SAVEDNETWORKEVALUATOR_NOTIFY_PORTAL_CHANGED /* 43 */:
                            callSavedNetworkEvaluatorPortalChanged(data);
                            return null;
                        case INTERFACE_IS_WIFIPRO_EVALUATING_AP /* 44 */:
                            return isWifiProEvaluatingAP(data);
                        case INTERFACE_HANDLE_NO_INTERNET_IP /* 45 */:
                            handleNoInternetIp();
                            return null;
                        case INTERFACE_GET_DHCP_RESULTS /* 46 */:
                            return syncGetDhcpResults();
                        case INTERFACE_RESET_IPCONFIG_STATUS /* 47 */:
                            resetIpConfigStatus();
                            return null;
                        case INTERFACE_REQUEST_RENEW_DHCP /* 48 */:
                            requestRenewDhcp();
                            return null;
                        case INTERFACE_HANDLE_INVALID_IPADDR /* 49 */:
                            handleInvalidIpAddr();
                            return null;
                        case 50:
                            if (data == null) {
                                return null;
                            }
                            requestReassocLink(data.getInt("useWithReassocType"));
                            return null;
                        case INTERFACE_REQUEST_RESET_WIFI /* 51 */:
                            requestResetWifi();
                            return null;
                        case INTERFACE_GET_WIFI_INFO /* 52 */:
                            return getWifiInfo();
                        case INTERFACE_REQUEST_USE_STATIC_IPCONFIG /* 53 */:
                            requestUseStaticIpConfig(data);
                            return null;
                        case INTERFACE_REQUEST_UPDATE_DNS_SERVERS /* 54 */:
                            requestUpdateDnsServers(data);
                            return null;
                        case INTERFACE_SET_WIFI_BACKGROUND_REASON /* 55 */:
                            setWifiBackgroundReason(data);
                            return null;
                        default:
                            switch (interfaceId) {
                                case INTERFACE_SET_STATIC_ARP /* 57 */:
                                    setStaticARP(data);
                                    return null;
                                case INTERFACE_DEL_STATIC_ARP /* 58 */:
                                    delStaticARP(data);
                                    return null;
                                case INTERFACE_DEAUTH_ROAMING_BSSID /* 59 */:
                                    deauthLastRoamingBssidHw();
                                    return null;
                                case INTERFACE_UPDATE_ACCESS_WEB_EXCEPTION /* 60 */:
                                    updateAccessWebException(data);
                                    return null;
                                case INTERFACE_UPFATE_SC_CHR_COUNT /* 61 */:
                                    updateScChrCount(data);
                                    return null;
                                case INTERFACE_NOTIFY_SELF_ENGINE_RESET_COMPLETE /* 62 */:
                                    notifySelEngineResetCompelete();
                                    return null;
                                case INTERFACE_UPDATE_ARP_SUMMERY /* 63 */:
                                    updateArpSummery(data);
                                    return null;
                                default:
                                    switch (interfaceId) {
                                        case INTERFACE_PIGN_GATWAY /* 66 */:
                                            pingGateway();
                                            return null;
                                        case INTERFACE_DO_GRATUITOUSARP /* 67 */:
                                            doGratuitousArp();
                                            return null;
                                        case INTERFACE_GET_GATEWAY_ARP_RESULT /* 68 */:
                                        case INTERFACE_GET_WIFI6_WITH_HTC_ARP_RESULT /* 83 */:
                                        case INTERFACE_GET_WIFI6_WITHOUT_HTC_ARP_RESULT /* 84 */:
                                            return getGateWayArpResult(interfaceId);
                                        case INTERFACE_NOTIFY_SELF_ENGINE_STATE_START /* 69 */:
                                            notifySelEngineStateStart();
                                            return null;
                                        case INTERFACE_GET_GW_ADDR /* 70 */:
                                            return getGwAddr();
                                        case INTERFACE_MULTI_GATEWAY /* 71 */:
                                            return multiGateway();
                                        case INTERFACE_NOTIFY_SELF_ENGINE_STATE_END /* 72 */:
                                            notifySelEngineStateEnd(data);
                                            return null;
                                        case INTERFACE_START_SCAN /* 73 */:
                                            startScan();
                                            return null;
                                        case INTERFACE_GET_SCAN_RESULTS /* 74 */:
                                            return getScanResults();
                                        case INTERFACE_DO_GATWAY_ARP_TEST /* 75 */:
                                            return doGatewayArpTest(data);
                                        case INTERFACE_DO_SLOW_ARP_TEST /* 76 */:
                                            return doSlowArpTest(data);
                                        case INTERFACE_HANDLE_CONNECTION_STATE_CHANGED /* 77 */:
                                            handleConnectionStateChanged();
                                            return null;
                                        case INTERFACE_GET_WIFISTATEMACHINE_MESSENGER /* 78 */:
                                            return getWifiStateMachineMessenger();
                                        case INTERFACE_READ_TCP_STAT_LINES /* 79 */:
                                            readTcpStatLines();
                                            return null;
                                        case INTERFACE_GET_RSSI_PACKET_COUNT_INFO /* 80 */:
                                            return getRssiPacketCountInfo();
                                        case INTERFACE_GET_WIFI_INVISABLE_INFO /* 81 */:
                                            return getWifiVisableInfo();
                                        case INTERFACE_UPDATE_EVALUATE_SCAN_RESULT /* 82 */:
                                            updateEvaluateScanResult();
                                            return null;
                                        case INTERFACE_GET_WIFI_OTA_INFO /* 85 */:
                                            return getWifiOtaInfo();
                                        case INTERFACE_GET_HISTORY_SCAN_RESULTS /* 86 */:
                                            return getHistoryScanResults();
                                        case INTERFACE_GET_CAST_OPT_INFO /* 87 */:
                                            return getCastOptInfo();
                                        default:
                                            return null;
                                    }
                            }
                    }
            }
        } else {
            updateSoftApConfigFile(data);
            return null;
        }
    }

    private boolean enforceCanAccessInterfacePermission(String pkgName) {
        if (SERVICE_NAME.equals(pkgName)) {
            return true;
        }
        return false;
    }

    private void notifyHwWifiProServiceAccomplished(Bundle data) {
        HwWifiProServiceManager.createHwWifiProServiceManager(this.mContext).bindToService(this.mContext);
        HwWifiProServiceManager.createHwWifiProServiceManager(this.mContext).initWifiproProperty(data);
    }

    private Bundle getApVendorInfo() {
        Bundle result = new Bundle();
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative != null) {
            String apvendorinfo = wifiNative.mHwWifiNativeEx.getApVendorInfo();
            Log.i(TAG, "routerModelRecognition: " + apvendorinfo);
            result.putString("apvendorinfo", apvendorinfo);
        } else {
            result.putString("apvendorinfo", null);
            Log.i(TAG, "routerModelRecognition: cannot recognize");
        }
        return result;
    }

    private void uploadDftEvent(Bundle data) {
        HwWifiCHRService chrInstance = HwWifiServiceFactory.getHwWifiCHRService();
        if (chrInstance != null && data != null) {
            chrInstance.uploadDFTEvent(data.getInt("eventId"), data.getBundle("eventData"));
        }
    }

    private void updateApVendorInfo(Bundle data) {
        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        if (mHwWifiCHRService != null && data != null) {
            mHwWifiCHRService.updateAPVendorInfo(data.getString("apvendorinfo"));
        }
    }

    private void updateWifiException(Bundle data) {
        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        if (mHwWifiCHRService != null && data != null) {
            mHwWifiCHRService.updateWifiException(data.getInt("eventId"), data.getString("eventData"));
        }
    }

    private void notifyPortalConnectedInfo(Bundle data) {
        if (data != null) {
            String configKey = data.getString("configKey");
            String redirectedUrl = data.getString("redirectedUrl");
            HwPortalExceptionManager.getInstance(this.mContext).notifyPortalConnectedInfo(configKey, data.getBoolean("firstDetected"), data.getInt("respCode"), redirectedUrl);
        }
    }

    private void resetWlanRtt(Bundle data) {
        if (data != null) {
            HwQoEJNIAdapter.getInstance().resetRtt(data.getInt("WIFIPRO_WLAN_BQE_RTT"));
        }
    }

    private Bundle queryBqeRttResult(Bundle data) {
        if (data == null) {
            return new Bundle();
        }
        int[] resultRtt = HwQoEJNIAdapter.getInstance().queryBQERttResult(data.getInt("WIFIPRO_WLAN_BQE_RTT"));
        Bundle result = new Bundle();
        result.putIntArray("resultRtt", resultRtt);
        return result;
    }

    private Bundle sendQoeCmd(Bundle data) {
        HwQoEJNIAdapter mHwQoEJNIAdapter = HwQoEJNIAdapter.getInstance();
        if (mHwQoEJNIAdapter == null || data == null) {
            return null;
        }
        int[] resultCmd = mHwQoEJNIAdapter.sendQoECmd(data.getInt("cmd"), data.getInt("arg"));
        Bundle result = new Bundle();
        result.putIntArray("resultCmd", resultCmd);
        return result;
    }

    private Bundle isFullscreen() {
        AbsPhoneWindowManager policy = null;
        if (LocalServices.getService(WindowManagerPolicy.class) instanceof AbsPhoneWindowManager) {
            policy = (AbsPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        }
        boolean isFullscreen = policy != null && policy.isTopIsFullscreen();
        Bundle result = new Bundle();
        result.putBoolean("isFullscreen", isFullscreen);
        return result;
    }

    private void startCustomizedScan(Bundle data) {
        if (data != null) {
            WifiScanner.ScanSettings requested = data.getParcelable("ScanSettings");
            WifiScanner mWifiScanner = WifiInjector.getInstance().getWifiScanner();
            Log.i(HW_DUAL_BAND_MANAGER, "ScanSettings = " + requested + ", WifiScanner = " + mWifiScanner + ", mCustomizedScanListener = " + this.mCustomizedScanListener);
            mWifiScanner.startScan(requested, this.mCustomizedScanListener, (WorkSource) null);
        }
    }

    /* access modifiers changed from: private */
    public class CustomizedScanListener implements WifiScanner.ScanListener {
        private CustomizedScanListener() {
        }

        public void onSuccess() {
            Log.d(HwWifiProServiceProxy.HW_DUAL_BAND_MANAGER, "CustomizedScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            Log.i(HwWifiProServiceProxy.HW_DUAL_BAND_MANAGER, "CustomizedScanListener onFailure");
        }

        public void onPeriodChanged(int periodInMs) {
            Log.d(HwWifiProServiceProxy.HW_DUAL_BAND_MANAGER, "CustomizedScanListener onPeriodChanged");
        }

        public void onResults(WifiScanner.ScanData[] results) {
            Log.i(HwWifiProServiceProxy.HW_DUAL_BAND_MANAGER, "CustomizedScanListener onResults");
            HwWifiProServiceManager.getHwWifiProServiceManager().sendMessageToHwDualBandStateMachine(7);
        }

        public void onFullResult(ScanResult fullScanResult) {
            Log.i(HwWifiProServiceProxy.HW_DUAL_BAND_MANAGER, "CustomizedScanListener onFullResult");
        }
    }

    private Bundle isWifiRestricted(Bundle data) {
        if (data == null) {
            return new Bundle();
        }
        data.getBoolean("isToast");
        boolean isWifiRestricted = HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted((WifiConfiguration) data.getParcelable("WifiConfiguration"), false);
        Bundle result = new Bundle();
        result.putBoolean("isWifiRestricted", isWifiRestricted);
        return result;
    }

    private Bundle getScanResultsFromWsm() {
        ArrayList<ScanResult> scanResults = new ArrayList<>();
        ScanRequestProxy scanProxy = WifiInjector.getInstance().getScanRequestProxy();
        if (scanProxy != null) {
            synchronized (scanProxy) {
                for (ScanResult result : scanProxy.getScanResults()) {
                    scanResults.add(new ScanResult(result));
                }
            }
        }
        Bundle result2 = new Bundle();
        result2.putParcelableArrayList("scanResults", scanResults);
        return result2;
    }

    private Bundle getAvailableChannels(Bundle data) {
        if (data == null) {
            return new Bundle();
        }
        List<Integer> availableChannelsList = WifiInjector.getInstance().getWifiScanner().getAvailableChannels(data.getInt("WIFI_BAND_5_GHZ"));
        ArrayList<Integer> availableChannels = new ArrayList<>();
        availableChannels.addAll(availableChannelsList);
        Bundle result = new Bundle();
        result.putIntegerArrayList("availableChannels", availableChannels);
        return result;
    }

    private Bundle getNetworkForTypeWifi() {
        Network mNetwork = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
        Bundle result = new Bundle();
        result.putParcelable("Network", mNetwork);
        return result;
    }

    private void updateConnectType(Bundle data) {
        if (data != null) {
            HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
            String mWifiProConnet = data.getString("WIFIPRO_CONNECT");
            if (mHwWifiCHRService != null) {
                mHwWifiCHRService.updateConnectType(mWifiProConnet);
            }
        }
    }

    private void updateWifiSwitchTimeStamp(Bundle data) {
        if (data != null) {
            this.mWifiStateMachine.sendMessage((int) HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS, (WifiConfiguration) data.getParcelable("WifiConfiguration"));
        }
    }

    private Bundle isScanAndManualConnectMode() {
        boolean isScanAndManualConnectMode = this.mWifiStateMachine.isScanAndManualConnectMode();
        Bundle result = new Bundle();
        result.putBoolean("isScanAndManualConnectMode", isScanAndManualConnectMode);
        return result;
    }

    private void startWifi2WifiRequest() {
        this.mWifiStateMachine.startWifi2WifiRequest();
    }

    private void startRoamToNetwork(Bundle data) {
        if (data != null) {
            this.mWifiStateMachine.startRoamToNetwork(data.getInt("networkId"), (ScanResult) data.getParcelable("ScanResult"));
        }
    }

    private void requestWifiSoftSwitch() {
        this.mWifiStateMachine.requestWifiSoftSwitch();
    }

    private void startConnectToUserSelectNetwork(Bundle data) {
        if (data != null) {
            this.mWifiStateMachine.startConnectToUserSelectNetwork(data.getInt("networkId"), data.getInt("CallingUid"), data.getString("bssid"));
        }
    }

    private void startProxyScan(Bundle data) {
        ScanRequestProxy scanRequest = WifiInjector.getInstance().getScanRequestProxy();
        if (scanRequest == null || data == null) {
            Log.e(TAG, "can't start wifi scan!");
            return;
        }
        int mCallingUid = data.getInt("CallingUid");
        String packageName = data.getString("packageName");
        ClientModeImpl clientModeImpl = this.mWifiStateMachine;
        if (clientModeImpl == null || !clientModeImpl.isNeedIgnoreScan()) {
            scanRequest.startScan(mCallingUid, packageName);
        } else {
            Log.i(TAG, "startProxyScan Ignore this scan because miracast is working");
        }
    }

    private Bundle isHwArbitrationManagerNotNull() {
        boolean isValid = HwArbitrationManager.getInstance() != null;
        Bundle result = new Bundle();
        result.putBoolean("isHwArbitrationManagerNotNull", isValid);
        return result;
    }

    private Bundle getWifiPreferenceFromHiData() {
        HashMap<Integer, String> preferList = new HashMap<>();
        if (HwArbitrationManager.getInstance() != null) {
            preferList = HwArbitrationManager.getInstance().getWifiPreferenceFromHiData();
        }
        Bundle result = new Bundle();
        result.putSerializable("preferList", preferList);
        Log.i(HW_DUAL_BAND_MANAGER, "preferList = " + preferList);
        return result;
    }

    private void query11vRoamingNetwork(Bundle data) {
        if (data != null) {
            this.mWifiNative.mHwWifiNativeEx.query11vRoamingNetwork(data.getInt("reason"));
        }
    }

    private Bundle isInGameAndNeedDisc() {
        boolean isInGameAndNeedDisc;
        HwQoEService hwQoeService = HwQoEService.getInstance();
        Bundle result = new Bundle();
        if (hwQoeService != null) {
            isInGameAndNeedDisc = hwQoeService.isInGameAndNeedDisc();
        } else {
            isInGameAndNeedDisc = false;
        }
        result.putBoolean("isInGameAndNeedDisc", isInGameAndNeedDisc);
        return result;
    }

    private void sendMessageToWifiStateMachine(Bundle data) {
        if (data != null) {
            int messageWhat = data.getInt("messageWhat");
            Message msg = Message.obtain();
            if (messageWhat != 131672) {
                switch (messageWhat) {
                    case 131873:
                        this.mWifiStateMachine.sendMessage(131873);
                        return;
                    case 131874:
                        msg.what = 131874;
                        msg.arg1 = data.getInt(KEYWORD_MESSAGE_ARG);
                        this.mWifiStateMachine.sendMessage(msg);
                        return;
                    case 131875:
                        this.mWifiStateMachine.sendMessage(131875);
                        return;
                    default:
                        return;
                }
            } else {
                msg.what = HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS;
                msg.obj = (WifiConfiguration) data.getParcelable("messageObj");
                this.mWifiStateMachine.sendMessage(msg);
            }
        }
    }

    private void callHwPortalExceptionManagerInit() {
        HwPortalExceptionManager.getInstance(this.mContext);
    }

    private Bundle isBssidDisabled(Bundle data) {
        if (data == null) {
            return new Bundle();
        }
        boolean isBssidDisabled = this.mWifiStateMachine.isBssidDisabled(data.getString("BSSID"));
        Bundle result = new Bundle();
        result.putBoolean("isBssidDisabled", isBssidDisabled);
        return result;
    }

    private void setLaaEnabled(Bundle data) {
        if (data != null) {
            boolean is24gConnected = data.getBoolean("is24gConnected");
            if (HwLaaUtils.isLaaPlusEnable() && HwLaaController.getInstrance() != null) {
                HwLaaController.getInstrance().setLaaEnabled(is24gConnected, 4);
            }
        }
    }

    private void incrAccessWebRecord(Bundle data) {
        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        if (mHwWifiCHRService != null && data != null) {
            mHwWifiCHRService.incrAccessWebRecord(data.getInt("reason"), data.getBoolean("succ"), data.getBoolean("isPortalAP"));
        }
    }

    private void portalNotifyChanged(Bundle data) {
        if (this.mSavedNetworkEvaluator != null && data != null) {
            this.mSavedNetworkEvaluator.portalNotifyChanged(data.getBoolean("popUp"), data.getString("configKey"), data.getBoolean("hasInternetAccess"));
        }
    }

    private Bundle isHiLinkUnconfigRouter(Bundle data) {
        if (data == null) {
            return new Bundle();
        }
        String mCurrentSsid = data.getString("CurrentSsid");
        String mCurrentBssid = data.getString("CurrentBssid");
        int resultValue = 0;
        if (this.mContext != null && !TextUtils.isEmpty(mCurrentSsid)) {
            resultValue = HiLinkUtil.getHiLinkSsidType(this.mContext, WifiInfo.removeDoubleQuotes(mCurrentSsid), mCurrentBssid);
        }
        Log.d(TAG, "isHiLinkUnconfigRouter, getHiLinkSsidType = " + resultValue);
        boolean isHiLinkUnconfigRouter = true;
        if (resultValue != 1) {
            isHiLinkUnconfigRouter = false;
        }
        Bundle result = new Bundle();
        result.putBoolean("isHiLinkUnconfigRouter", isHiLinkUnconfigRouter);
        return result;
    }

    private void updateWifiConnectionMode(Bundle data) {
        HwQoEService mHwQoEService = HwQoEService.getInstance();
        if (mHwQoEService != null && data != null) {
            mHwQoEService.updateWifiConnectionMode(data.getBoolean("isUserManualConnect"), data.getBoolean("isUserHandoverWiFi"));
        }
    }

    private void updateVnpStateChanged(Bundle data) {
        HwQoEService mHwQoEService = HwQoEService.getInstance();
        if (mHwQoEService != null && data != null) {
            mHwQoEService.updateVNPStateChanged(data.getBoolean("isVpnConnected"));
        }
    }

    private void notifyPortalAuthenStatus(Bundle data) {
        if (data != null) {
            HwPortalExceptionManager.getInstance(this.mContext).notifyPortalAuthenStatus(data.getBoolean("success"));
        }
    }

    private void callHwPortalExceptionManagerNotifyDisconnected() {
        HwPortalExceptionManager.getInstance(this.mContext).notifyNetworkDisconnected();
    }

    private void callSavedNetworkEvaluatorPortalChanged(Bundle data) {
        if (this.mSavedNetworkEvaluator != null && data != null) {
            this.mSavedNetworkEvaluator.portalNotifyChanged(data.getBoolean("popUp"), data.getString("configKey"), data.getBoolean("hasInternetAccess"));
        }
    }

    private Bundle isWifiProEvaluatingAP(Bundle data) {
        if (data == null) {
            return new Bundle();
        }
        boolean isWifiProEvaluatingAP = this.mWifiStateMachine.isWifiProEvaluatingAP();
        Bundle result = new Bundle();
        result.putBoolean("isWifiProEvaluatingAP", isWifiProEvaluatingAP);
        return result;
    }

    private void handleNoInternetIp() {
        this.mWifiStateMachine.handleNoInternetIp();
    }

    private Bundle syncGetDhcpResults() {
        DhcpResults dhcpResults = this.mWifiStateMachine.syncGetDhcpResults();
        Bundle result = new Bundle();
        result.putParcelable("dhcpResults", dhcpResults);
        return result;
    }

    private void resetIpConfigStatus() {
        this.mWifiStateMachine.resetIpConfigStatus();
    }

    private void requestRenewDhcp() {
        this.mWifiStateMachine.requestRenewDhcp();
    }

    private void handleInvalidIpAddr() {
        this.mWifiStateMachine.handleInvalidIpAddr();
    }

    private void requestReassocLink(int useWithRandMacAddress) {
        this.mWifiStateMachine.requestReassocLink(useWithRandMacAddress);
    }

    private void requestResetWifi() {
        this.mWifiStateMachine.requestResetWifi();
    }

    private Bundle getWifiInfo() {
        WifiInfo mWifiInfo = new WifiInfo(this.mWifiStateMachine.getWifiInfo());
        Bundle result = new Bundle();
        result.putParcelable("WifiInfo", mWifiInfo);
        return result;
    }

    private void requestUseStaticIpConfig(Bundle data) {
        if (data != null) {
            this.mWifiStateMachine.requestUseStaticIpConfig(data.getParcelable("staticIpConfig"));
        }
    }

    private void requestUpdateDnsServers(Bundle data) {
        if (data != null) {
            try {
                this.mWifiStateMachine.requestUpdateDnsServers(data.getStringArrayList("dnsServers"));
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(HW_DUAL_BAND_MANAGER, "requestUpdateDnsServers fail");
            }
        }
    }

    private void setWifiBackgroundReason(Bundle data) {
        if (data != null) {
            this.mWifiStateMachine.setWifiBackgroundReason(data.getInt("reason"));
        }
    }

    private void setStaticARP(Bundle data) {
        if (data != null) {
            this.mWifiNative.mHwWifiNativeEx.setStaticARP(data.getString("gateway"), data.getString("mac"));
        }
    }

    private void delStaticARP(Bundle data) {
        if (data != null) {
            this.mWifiNative.mHwWifiNativeEx.delStaticARP(data.getString("gateway"));
        }
    }

    private void deauthLastRoamingBssidHw() {
        this.mWifiNative.mHwWifiNativeEx.deauthLastRoamingBssidHw("2", "");
    }

    private void updateAccessWebException(Bundle data) {
        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        if (mHwWifiCHRService != null && data != null) {
            mHwWifiCHRService.updateAccessWebException(0, data.getString("errReason"));
        }
    }

    private void updateScChrCount(Bundle data) {
        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        if (mHwWifiCHRService != null && data != null) {
            mHwWifiCHRService.updateScCHRCount(data.getInt("count"));
        }
    }

    private void notifySelEngineResetCompelete() {
        HwAbsDetectorService service;
        if (HwAbsUtils.getAbsEnable() && (service = HwAbsDetectorService.getInstance()) != null) {
            service.notifySelEngineResetCompelete();
        }
    }

    private void notifySelEngineStateEnd(Bundle data) {
        if (data != null) {
            boolean isSuccess = data.getBoolean("success");
            HwQoEService mHwQoEService = HwQoEService.getInstance();
            if (mHwQoEService != null) {
                mHwQoEService.notifySelEngineStateEnd(isSuccess);
            }
        }
    }

    private void updateArpSummery(Bundle data) {
        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        if (mHwWifiCHRService != null && data != null) {
            boolean isSuccess = data.getBoolean("succ");
            if (data.getLong(ARP_SUMMERY_SPEND_TIME) < 2147483647L) {
                mHwWifiCHRService.updateArpSummery(isSuccess, (int) data.getLong(ARP_SUMMERY_SPEND_TIME), -1);
            }
        }
    }

    private void pingGateway() {
        this.mHwArpUtils.pingGateway();
    }

    private void doGratuitousArp() {
        this.mHwArpUtils.doGratuitousArp(100);
    }

    private Bundle getGateWayArpResult(int interfaceId) {
        int timeout;
        if (interfaceId == INTERFACE_GET_GATEWAY_ARP_RESULT) {
            timeout = 1000;
        } else if (interfaceId == INTERFACE_GET_WIFI6_WITH_HTC_ARP_RESULT) {
            timeout = WIFI6_HTC_ARP_TIMEOUT_MS;
        } else if (interfaceId == INTERFACE_GET_WIFI6_WITHOUT_HTC_ARP_RESULT) {
            timeout = WIFI6_ARP_TIMEOUT_MS;
        } else {
            Log.w(TAG, "invalid interfaceId = " + interfaceId);
            return new Bundle();
        }
        Pair<Boolean, Long> pair = this.mHwArpUtils.getGateWayArpResult(1, timeout);
        Bundle result = new Bundle();
        result.putBoolean("arpResult", ((Boolean) pair.first).booleanValue());
        result.putLong("time", ((Long) pair.second).longValue());
        Log.i(TAG, "pair = " + pair + ", Bundle = " + result);
        return result;
    }

    private void notifySelEngineStateStart() {
        HwQoEService mHwQoEService = HwQoEService.getInstance();
        if (mHwQoEService != null) {
            mHwQoEService.notifySelEngineStateStart();
        }
    }

    private Bundle getGwAddr() {
        HwMultiGw hwMultiGw = this.mHwMultiGw;
        if (hwMultiGw == null) {
            return new Bundle();
        }
        String mac = hwMultiGw.getNextGwMacAddr();
        String gateway = this.mHwMultiGw.getGwIpAddr();
        Bundle result = new Bundle();
        result.putString("mac", mac);
        result.putString("gateway", gateway);
        return result;
    }

    private Bundle multiGateway() {
        boolean z = true;
        this.mHwMultiGw = this.mHwArpUtils.getGateWayArpResponses(1, 1000);
        int gwNum = this.mHwMultiGw.getGwNum();
        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        if (mHwWifiCHRService != null) {
            mHwWifiCHRService.updateMultiGWCount((byte) gwNum);
        }
        String gateway = this.mHwMultiGw.getGwIpAddr();
        Bundle result = new Bundle();
        if (gateway == null) {
            result.putBoolean("isMultiGateway", false);
            return result;
        }
        ArrayList<String> unisFoundlists = new ArrayList<>();
        ArrayList<String> macLists = this.mHwMultiGw.getGwMacAddrList();
        for (HwArpUtils.ArpItem arpitem : this.mHwArpUtils.readArpFromFile()) {
            boolean isFound = true;
            int i = 0;
            int mListSize = macLists.size();
            while (true) {
                if (i >= mListSize) {
                    break;
                }
                boolean condition = (!arpitem.isValid() || !arpitem.sameIpaddress(gateway)) ? false : z;
                if (!condition || !arpitem.sameMacAddress(macLists.get(i))) {
                    if (condition && !arpitem.sameMacAddress(macLists.get(i))) {
                        isFound = false;
                    }
                    i++;
                    z = true;
                } else {
                    isFound = true;
                    if (arpitem.isStaticArp()) {
                    }
                }
            }
            if (!isFound) {
                unisFoundlists.add(arpitem.hwaddr);
            }
            z = true;
        }
        macLists.addAll(unisFoundlists);
        result.putBoolean("isMultiGateway", macLists.size() > 1);
        return result;
    }

    private void startScan() {
        ScanRequestProxy scanRequest = WifiInjector.getInstance().getScanRequestProxy();
        if (scanRequest != null && this.mContext != null) {
            scanRequest.startScan(Binder.getCallingUid(), this.mContext.getOpPackageName());
        }
    }

    private Bundle getScanResults() {
        ArrayList<ScanResult> results = new ArrayList<>();
        ScanRequestProxy scanRequest = WifiInjector.getInstance().getScanRequestProxy();
        if (scanRequest != null) {
            results.addAll(scanRequest.getScanResults());
        }
        Bundle result = new Bundle();
        result.putParcelableArrayList("results", results);
        return result;
    }

    private Bundle getHistoryScanResults() {
        ArrayList<ScanResult> results = new ArrayList<>();
        ScanRequestProxy scanRequest = WifiInjector.getInstance().getScanRequestProxy();
        if (scanRequest != null) {
            results.addAll(scanRequest.getHistoryScanResults());
        }
        Bundle result = new Bundle();
        result.putParcelableArrayList("results", results);
        return result;
    }

    private Bundle doGatewayArpTest(Bundle data) {
        if (data == null) {
            return new Bundle();
        }
        Inet4Address gateway = (Inet4Address) data.getSerializable("gateway");
        boolean isArpTest = this.mArpClient.doGatewayArpTest(gateway);
        Bundle result = new Bundle();
        result.putBoolean("arptest", isArpTest);
        if (gateway != null) {
            Log.i(HW_DUAL_BAND_MANAGER, "Inet4Address = " + StringUtilEx.safeDisplayIpAddress(gateway.toString()) + ", Bundle = " + result.toString());
        }
        return result;
    }

    private Bundle doSlowArpTest(Bundle data) {
        if (data == null) {
            return new Bundle();
        }
        Inet4Address testIpAddr = (Inet4Address) data.getSerializable("testIpAddr");
        boolean isSlowArpTest = this.mArpClient.doSlowArpTest(testIpAddr);
        Bundle result = new Bundle();
        result.putBoolean("slowArpTest", isSlowArpTest);
        if (testIpAddr != null) {
            Log.i(HW_DUAL_BAND_MANAGER, "Inet4Address = " + StringUtilEx.safeDisplayIpAddress(testIpAddr.toString()) + ", Bundle = " + result.toString());
        }
        return result;
    }

    private void handleConnectionStateChanged() {
        WifiConnectivityManager wcm = wifiStateMachineUtils.getWifiConnectivityManager(this.mWifiStateMachine);
        if (wcm != null) {
            wcm.handleConnectionStateChanged(2);
        }
    }

    private Bundle getWifiStateMachineMessenger() {
        this.mWifiStateMachineMessenger = this.mWifiStateMachine.getMessenger();
        Bundle result = new Bundle();
        result.putParcelable("WifiStateMachineMessenger", this.mWifiStateMachineMessenger);
        return result;
    }

    private Bundle isNetworkReachableByIcmp(Bundle data) {
        if (data == null) {
            return new Bundle();
        }
        boolean isRet = WifiProCommonUtils.isNetworkReachableByIcmp(data.getString("ipAddress"), data.getInt("timeout"));
        Bundle result = new Bundle();
        result.putBoolean("result", isRet);
        return result;
    }

    private void readTcpStatLines() {
        new Thread(new TcpStatisticsRun()).start();
    }

    /* access modifiers changed from: private */
    public class TcpStatisticsRun implements Runnable {
        private TcpStatisticsRun() {
        }

        @Override // java.lang.Runnable
        public void run() {
            Log.i(HwWifiProServiceProxy.TAG, "TcpStatisticsRun run");
            HwWifiProServiceManager.createHwWifiProServiceManager(HwWifiProServiceProxy.this.mContext).notifyTcpStatResult(HwWifiProServiceProxy.this.getFileResult(HwWifiProServiceProxy.TCP_STAT_PATH));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<String> getFileResult(String fileName) {
        ArrayList<String> result = new ArrayList<>();
        FileInputStream fileInput = null;
        BufferedReader bufRead = null;
        InputStreamReader inputStreamReader = null;
        try {
            FileInputStream fileInput2 = new FileInputStream(fileName);
            InputStreamReader inputStreamReader2 = new InputStreamReader(fileInput2, US_ASCII);
            BufferedReader bufRead2 = new BufferedReader(inputStreamReader2);
            while (true) {
                String line = bufRead2.readLine();
                if (line != null) {
                    String line2 = line.trim();
                    if (!"".equals(line2)) {
                        result.add(line2);
                    }
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        Log.e(TAG, "close buffer reader error!");
                    }
                }
            }
            bufRead2.close();
            try {
                inputStreamReader2.close();
            } catch (IOException e2) {
                Log.e(TAG, "close input stream reader error!");
            }
            try {
                fileInput2.close();
            } catch (IOException e3) {
                Log.e(TAG, "close file input stream error!");
            }
        } catch (IOException e4) {
            Log.e(TAG, "read file err!");
            if (0 != 0) {
                try {
                    bufRead.close();
                } catch (IOException e5) {
                    Log.e(TAG, "close buffer reader error!");
                }
            }
            if (0 != 0) {
                try {
                    inputStreamReader.close();
                } catch (IOException e6) {
                    Log.e(TAG, "close input stream reader error!");
                }
            }
            if (0 != 0) {
                fileInput.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bufRead.close();
                } catch (IOException e7) {
                    Log.e(TAG, "close buffer reader error!");
                }
            }
            if (0 != 0) {
                try {
                    inputStreamReader.close();
                } catch (IOException e8) {
                    Log.e(TAG, "close input stream reader error!");
                }
            }
            if (0 != 0) {
                try {
                    fileInput.close();
                } catch (IOException e9) {
                    Log.e(TAG, "close file input stream error!");
                }
            }
            throw th;
        }
        return result;
    }

    private Bundle getRssiPacketCountInfo() {
        RssiPacketCountInfo rssiPacketCountInfo = new RssiPacketCountInfo();
        WifiNative wifiNative = this.mWifiNative;
        WifiNative.TxPacketCounters counters = wifiNative.getTxPacketCounters(wifiNative.getClientInterfaceName());
        if (counters != null) {
            rssiPacketCountInfo.txgood = counters.txSucceeded;
            rssiPacketCountInfo.txbad = counters.txFailed;
        } else {
            rssiPacketCountInfo.txgood = 0;
            rssiPacketCountInfo.txbad = 0;
        }
        Bundle result = new Bundle();
        result.putParcelable(RSSI_PACKET_COUNT_INFO, rssiPacketCountInfo);
        return result;
    }

    private Bundle getWifiVisableInfo() {
        return HwWifiProServiceManager.createHwWifiProServiceManager(this.mContext).getWifiDisplayInfo(new NetworkInfo(this.mWifiStateMachine.getNetworkInfo()));
    }

    private void updateEvaluateScanResult() {
        ScanRequestProxy scanRequest = WifiInjector.getInstance().getScanRequestProxy();
        if (scanRequest != null) {
            scanRequest.updateEvaluateScanResult();
        }
    }

    private Bundle getWifiOtaInfo() {
        Bundle result = new Bundle();
        WifiNative wifiNative = this.mWifiNative;
        WifiNative.SignalPollResult pollResult = wifiNative.signalPoll(wifiNative.getClientInterfaceName());
        if (pollResult == null) {
            Log.e(TAG, "getWifiOtaInfo pollResult is null");
            return null;
        }
        result.putInt("currentNoise", pollResult.currentNoise);
        result.putInt("currentChload", pollResult.currentChload);
        result.putInt("currentUlDelay", pollResult.currentUlDelay);
        result.putInt("rxBitRate", pollResult.rxBitrate);
        result.putInt("txBitRate", pollResult.txBitrate);
        return result;
    }

    private void updateSoftApConfigFile(Bundle data) {
        WifiConfiguration config = this.mWifiManager.getWifiApConfiguration();
        if (config == null) {
            Log.e(TAG, "updateSoftApConfigFile failed with null configuration");
            return;
        }
        if (data.getBoolean("wpa3flag")) {
            config.allowedKeyManagement.clear();
            config.allowedKeyManagement.set(1);
        } else {
            config.allowedKeyManagement.clear();
            config.allowedKeyManagement.set(4);
        }
        WifiInjector.getInstance().getWifiApConfigStore().setApConfiguration(config);
    }

    private Bundle getCastOptInfo() {
        boolean isCastOptWorking = false;
        int p2pFreq = 0;
        CastOptManager castOptManager = CastOptManager.getInstance();
        if (castOptManager != null) {
            isCastOptWorking = castOptManager.isCastOptWorking();
            p2pFreq = castOptManager.getP2pFrequency();
        }
        Bundle result = new Bundle();
        result.putBoolean("isCastOptWorking", isCastOptWorking);
        result.putInt("p2pFreq", p2pFreq);
        return result;
    }
}
