package com.android.server.wifi.wifipro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.wifi.HwPortalExceptionManager;
import com.android.server.wifi.HwSelfCureEngine;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiConnectivityMonitor;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifipro.WifiProCHRManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkQosMonitor {
    public static final String ACTION_NOTIFY_WIFI_INTERNET_STATUS = "com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_INTERNET_STATUS";
    public static final String EXTRA_INTERNET_STATUS = "internet_status";
    public static final String EXTRA_NETWORK_CONNECTED_STATUS = "network_connected_status_portal";
    public static final int MSG_BQE_DETECTION_RESULT = 10;
    public static final int MSG_NETWORK_DETECTION_RESULT = 2;
    public static final int MSG_NETWORK_QOS_RESULT = 1;
    public static final int MSG_NETWORK_TYPE_BY_WEBVIEW_REQ = 15;
    public static final int MSG_NETWORK_TYPE_BY_WEBVIEW_RESP = 16;
    public static final int MSG_NETWORK_TYPE_BY_WEBVIEW_RESP_TIMEOUT = 17;
    public static final int MSG_NOTIFY_MCC = 5;
    public static final int MSG_QUERY_PKT_TIMEOUT = 7;
    public static final int MSG_QUERY_RTT = 9;
    public static final int MSG_QUERY_TCPRX_TIMEOUT = 11;
    public static final int MSG_REQUEST_CHECK_NETWORK_PROPERTY = 6;
    public static final int MSG_RESET_RTT = 8;
    public static final int MSG_RETURN_AP_AVAILABLE_RSSI_TH = 12;
    public static final int MSG_RETURN_AP_CURRENT_RSSI = 14;
    public static final int MSG_RETURN_AP_HISTORY_SCORE_RS = 13;
    public static final int MSG_SEND_TCP_CHK_RESULT_QUERY = 4;
    public static final int MSG_WIFI_SECURITY_RESPONSE = 18;
    public static final int QUERY_TCPRX_TIMEOUT = 500;
    public static final String TAG = "WiFi_PRO";
    private static final int TCPF_CA_CWR = 4;
    private static final int TCPF_CA_DISORDER = 2;
    private static final int TCPF_CA_LOSS = 16;
    private static final int TCPF_CA_OPEN = 1;
    private static final int TCPF_CA_RECOVERY = 8;
    public static final int TYPE_HAS_INTERNET = 101;
    public static final int TYPE_NO_INTERNET = 102;
    public static final int TYPE_STANDARD_PORTAL = 103;
    public static final int TYPE_UNKNWON = 100;
    public static final int TYPE_UNSTANDARD_PORTAL = 104;
    public static final int WEBVIEW_RESP_TIMEOUT_MS = 12000;
    public static final int WIFIPRO_MOBILE_BQE_RTT = 2;
    public static final int WIFIPRO_NOTIFY_MOBILE_BQE_RTT = 3;
    public static final int WIFIPRO_NOTIFY_WLAN_BQE_RTT = 2;
    public static final int WIFIPRO_NOTIFY_WLAN_SAMPLE_RTT = 4;
    public static final int WIFIPRO_WLAN_BQE_RTT = 1;
    public static final int WIFIPRO_WLAN_SAMPLE_RTT = 3;
    public static final int WLAN_TCP_RX_ERROR = -1;
    private boolean isBetaVersion = false;
    /* access modifiers changed from: private */
    public WifiproBqeUtils mBqeClient;
    /* access modifiers changed from: private */
    public AtomicBoolean mBroadcastNotified = new AtomicBoolean(false);
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public INetworkQosCallBack mCallBack;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public HuaweiWifiWatchdogStateMachine mHuaweiWifiWatchdogStateMachine;
    /* access modifiers changed from: private */
    public HwNetworkPropertyChecker mHwNetworkPropertyChecker;
    private IntentFilter mIntentFilter;
    /* access modifiers changed from: private */
    public IPQosMonitor mIpQosMonitor = null;
    private boolean mIsBQEServiceStart;
    /* access modifiers changed from: private */
    public boolean mIsWaitingResponse;
    /* access modifiers changed from: private */
    public int mLastNotifyNetworkType = 100;
    /* access modifiers changed from: private */
    public MobileQosDetector mMobileQosDetector;
    /* access modifiers changed from: private */
    public Object mNetworkCheckLock = new Object();
    /* access modifiers changed from: private */
    public Handler mNetworkHandler;
    private Object mQueryTcpRxLock = new Object();
    /* access modifiers changed from: private */
    public Object mReceiverLock = new Object();
    /* access modifiers changed from: private */
    public Object mSyncNotifyLock = new Object();
    private int mTcpRxCounter;
    /* access modifiers changed from: private */
    public boolean mTcpRxRequestedViaBroadcast = false;
    private WifiProCHRManager mWiFiCHRMgr;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public WifiProUIDisplayManager mWifiProUIDisplayManager;

    private class NetworkCheckThread extends Thread {
        private boolean portalNetwork;
        private boolean wifiBackground;

        public NetworkCheckThread(boolean portal, boolean wifiBackground2) {
            this.portalNetwork = portal;
            this.wifiBackground = wifiBackground2;
        }

        public void run() {
            synchronized (NetworkQosMonitor.this.mNetworkCheckLock) {
                String startSsid = WifiProCommonUtils.getCurrentSsid(NetworkQosMonitor.this.mWifiManager);
                int respCode = NetworkQosMonitor.this.mHwNetworkPropertyChecker.isCaptivePortal(true, this.portalNetwork, this.wifiBackground);
                String endSsid = WifiProCommonUtils.getCurrentSsid(NetworkQosMonitor.this.mWifiManager);
                Log.d("WiFi_PRO", "startSsid = " + startSsid + ", endSsid = " + endSsid);
                if (startSsid != null && startSsid.equals(endSsid)) {
                    NetworkQosMonitor.this.notifyNetworkResult(respCode);
                }
            }
        }
    }

    public NetworkQosMonitor(Context context, INetworkQosCallBack callBack, Messenger dstMessenger, WifiProUIDisplayManager wifiProUIDisplayManager) {
        this.mContext = context;
        this.mCallBack = callBack;
        this.mWifiProUIDisplayManager = wifiProUIDisplayManager;
        initialize();
        this.mIpQosMonitor = new IPQosMonitor(this.mNetworkHandler);
        HwNetworkPropertyChecker hwNetworkPropertyChecker = new HwNetworkPropertyChecker(context, null, null, true, null, false);
        this.mHwNetworkPropertyChecker = hwNetworkPropertyChecker;
        this.mBqeClient = WifiproBqeUtils.getInstance(context);
        this.mHuaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.makeHuaweiWifiWatchdogStateMachine(this.mContext, dstMessenger, this.mNetworkHandler);
        this.mWiFiCHRMgr = WifiProCHRManager.getInstance();
        this.isBetaVersion = isBetaVer(this.mContext);
        registerBootCompleteReceiver(callBack);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    public void registerCallBack(INetworkQosCallBack callBack) {
        if (this.mCallBack == null) {
            this.mCallBack = callBack;
        }
        registerMonitorReceiver();
    }

    private void registerMonitorReceiver() {
        this.mBroadcastNotified.set(false);
        this.mTcpRxRequestedViaBroadcast = false;
        if (this.mContext != null && this.mBroadcastReceiver == null) {
            this.mIntentFilter = new IntentFilter();
            this.mIntentFilter.addAction("com.huawei.wifi.action.NETWOR_PROPERTY_NOTIFICATION");
            this.mIntentFilter.addAction("com.huawei.wifi.action.ACTION_REQUEST_TCP_RX_COUNTER");
            this.mIntentFilter.addAction("com.huawei.wifipro.action.ACTION_REQUEST_WEBVIEW_CHECK");
            this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.mIntentFilter.addAction("com.huawei.wifipro.ACTION_NOTIFY_WIFI_SECURITY_STATUS");
            this.mBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    boolean oversea;
                    synchronized (NetworkQosMonitor.this.mReceiverLock) {
                        Log.d("WiFi_PRO", "action received = " + intent.getAction() + ", mBroadcastNotified = " + NetworkQosMonitor.this.mBroadcastNotified.get() + ", mTcpRxRequestedViaBroadcast = " + NetworkQosMonitor.this.mTcpRxRequestedViaBroadcast);
                        int i = 1;
                        if ("com.huawei.wifi.action.NETWOR_PROPERTY_NOTIFICATION".equals(intent.getAction()) && !NetworkQosMonitor.this.mBroadcastNotified.get()) {
                            int property = intent.getIntExtra("wifi_network_property", WifiproUtils.NET_INET_QOS_LEVEL_UNKNOWN);
                            String portalDetectStatInfo = intent.getStringExtra("portal_detect_stat_info");
                            if (portalDetectStatInfo != null) {
                                HwWifiCHRService chrInstance = HwWifiServiceFactory.getHwWifiCHRService();
                                if (chrInstance != null) {
                                    Bundle data = new Bundle();
                                    data.putString("portalDetectionStatInfo", portalDetectStatInfo);
                                    chrInstance.uploadDFTEvent(909002060, data);
                                }
                            }
                            if (property == 6) {
                                NetworkQosMonitor.this.mHwNetworkPropertyChecker.setRawRedirectedHostName(intent.getStringExtra("raw_redirected_host"));
                                NetworkQosMonitor.this.syncNotifyPowerSaveGenie(false, intent.getBooleanExtra("standard_portal_network", false) ? 103 : 104, true);
                                if (HwAutoConnectManager.getInstance() != null) {
                                    HwAutoConnectManager.getInstance().notifyPortalNetworkConnected();
                                }
                                HwPortalExceptionManager.getInstance(NetworkQosMonitor.this.mContext).notifyPortalConnectedInfo(intent.getStringExtra("portal_config_key"), intent.getBooleanExtra("portal_first_detect", false), intent.getIntExtra("portal_http_resp_code", 599), intent.getStringExtra("portal_redirected_url"));
                            }
                            NetworkQosMonitor.this.mNetworkHandler.sendMessage(Message.obtain(NetworkQosMonitor.this.mNetworkHandler, 2, 1, property));
                            NetworkQosMonitor.this.mBroadcastNotified.set(true);
                            if (property != -1) {
                                if (property != -101) {
                                    if (property == 5) {
                                        NetworkQosMonitor.this.syncNotifyPowerSaveGenie(true, 101, true);
                                    }
                                }
                            }
                            HwSelfCureEngine.getInstance().notifyInternetFailureDetected(true);
                            HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.getInstance();
                            if (hwWifiConnectivityMonitor != null) {
                                hwWifiConnectivityMonitor.requestRoamingByNoInternet();
                            }
                            NetworkQosMonitor.this.syncNotifyPowerSaveGenie(false, 102, true);
                        } else if ("com.huawei.wifi.action.ACTION_REQUEST_TCP_RX_COUNTER".equals(intent.getAction()) && !NetworkQosMonitor.this.mTcpRxRequestedViaBroadcast) {
                            boolean unused = NetworkQosMonitor.this.mTcpRxRequestedViaBroadcast = true;
                            NetworkQosMonitor.this.mIpQosMonitor.queryPackets(0);
                        } else if ("com.huawei.wifipro.action.ACTION_REQUEST_WEBVIEW_CHECK".equals(intent.getAction())) {
                            Log.d("WiFi_PRO", "ACTION_REQUEST_WEBVIEW_CHECK:: begin, oversea = " + oversea);
                            Handler access$500 = NetworkQosMonitor.this.mNetworkHandler;
                            Handler access$5002 = NetworkQosMonitor.this.mNetworkHandler;
                            if (!oversea) {
                                i = 0;
                            }
                            access$500.sendMessage(Message.obtain(access$5002, 15, i, 0));
                            NetworkQosMonitor.this.mNetworkHandler.sendEmptyMessageDelayed(17, 12000);
                        } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                            if (info != null && NetworkInfo.DetailedState.DISCONNECTED == info.getDetailedState()) {
                                NetworkQosMonitor.this.mBroadcastNotified.set(false);
                                synchronized (NetworkQosMonitor.this.mSyncNotifyLock) {
                                    int unused2 = NetworkQosMonitor.this.mLastNotifyNetworkType = 100;
                                }
                            }
                        } else if ("com.huawei.wifipro.ACTION_NOTIFY_WIFI_SECURITY_STATUS".equals(intent.getAction())) {
                            NetworkQosMonitor.this.notifySecurityResult(intent.getExtras());
                        }
                    }
                }
            };
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
        }
    }

    public void unRegisterCallBack() {
        this.mCallBack = null;
        if (this.mBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
        }
    }

    private void registerBootCompleteReceiver(final INetworkQosCallBack callBack) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                    Log.d("WiFi_PRO", "**receive ACTION_BOOT_COMPLETED concurrently********");
                    if (NetworkQosMonitor.this.mMobileQosDetector == null) {
                        Log.d("WiFi_PRO", "**increate mNetworkQosMonitor ********");
                        MobileQosDetector unused = NetworkQosMonitor.this.mMobileQosDetector = new MobileQosDetector(NetworkQosMonitor.this.mContext, callBack, NetworkQosMonitor.this.mNetworkHandler, NetworkQosMonitor.this.mWifiProUIDisplayManager);
                    }
                }
            }
        }, intentFilter);
    }

    public WifiProUIDisplayManager getWifiProUIDisplayManager() {
        return this.mWifiProUIDisplayManager;
    }

    public void setIpQosEnabled(boolean enabled) {
        if (enabled) {
            Log.i("ipQos", "startMonitor");
            this.mIpQosMonitor.startMonitor();
            return;
        }
        Log.i("ipQos", "stopMonitor");
        this.mIpQosMonitor.stopMonitor();
    }

    /* access modifiers changed from: private */
    public void respTcpRxViaBroadcast(int[] rxResp, int len) {
        if (rxResp != null && len > 7) {
            Log.d("WiFi_PRO", "respTcpRxViaBroadcast, rx packets counter = " + rxResp[7]);
            this.mTcpRxRequestedViaBroadcast = false;
            Intent intent = new Intent("com.huawei.wifi.action.ACTION_RESPONSE_TCP_RX_COUNTER");
            intent.setFlags(67108864);
            intent.putExtra("wifipro_tcp_rx_counter", rxResp[7]);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER");
        }
    }

    private void initialize() {
        this.mNetworkHandler = new Handler() {
            public void handleMessage(Message msg) {
                WifiProEstimateApInfo estimateApInfo;
                WifiProEstimateApInfo estimateApInfo2;
                int rssi;
                int type = msg.arg1;
                int level = msg.arg2;
                int i = msg.what;
                if (i != 100) {
                    boolean isOversea = true;
                    switch (i) {
                        case 1:
                            NetworkQosMonitor.this.sendNetworkLevel(1, type, level);
                            break;
                        case 2:
                            NetworkQosMonitor.this.sendNetworkLevel(2, type, level);
                            break;
                        default:
                            switch (i) {
                                case 4:
                                    NetworkQosMonitor.this.mIpQosMonitor.queryPackets(0);
                                    break;
                                case 5:
                                    NetworkQosMonitor.this.mIpQosMonitor.notifyMCC(msg.arg1);
                                    break;
                                case 6:
                                    Log.d("WiFi_PRO", "MSG_REQUEST_CHECK_NETWORK_PROPERTY received, start to recheck network property...");
                                    if (msg.obj != null) {
                                        NetworkQosMonitor networkQosMonitor = NetworkQosMonitor.this;
                                        if (msg.arg1 != 1) {
                                            isOversea = false;
                                        }
                                        new NetworkCheckThread(isOversea, ((Boolean) msg.obj).booleanValue()).start();
                                        break;
                                    }
                                    break;
                                case 8:
                                    NetworkQosMonitor.this.mIpQosMonitor.resetRtt(msg.arg1);
                                    break;
                                case 9:
                                    NetworkQosMonitor.this.mIpQosMonitor.queryRtt(msg.arg1);
                                    break;
                                case 10:
                                    if (NetworkQosMonitor.this.mCallBack != null) {
                                        NetworkQosMonitor.this.mCallBack.onWifiBqeDetectionResult(level);
                                    }
                                    NetworkQosMonitor.this.mHuaweiWifiWatchdogStateMachine.setBqeLevel(level);
                                    break;
                                case 11:
                                    if (NetworkQosMonitor.this.mIsWaitingResponse) {
                                        Log.w("WiFi_PRO", "MSG_QUERY_TCPRX_TIMEOUT happened!");
                                        boolean unused = NetworkQosMonitor.this.mIsWaitingResponse = false;
                                        NetworkQosMonitor.this.responseTcpRxPacketsCounter(-1);
                                        break;
                                    }
                                    break;
                                case 12:
                                    if (msg.obj != null) {
                                        Log.d("WiFi_PRO", "get 5G RSSI TH = " + ((WifiProEstimateApInfo) msg.obj).getRetRssiTH());
                                        if (NetworkQosMonitor.this.mCallBack != null) {
                                            NetworkQosMonitor.this.mCallBack.onWifiBqeReturnRssiTH(estimateApInfo);
                                            break;
                                        }
                                    }
                                    break;
                                case 13:
                                    if (msg.obj != null) {
                                        Log.d("WiFi_PRO", "get AP history socre = " + ((WifiProEstimateApInfo) msg.obj).getRetHistoryScore());
                                        if (NetworkQosMonitor.this.mCallBack != null) {
                                            NetworkQosMonitor.this.mCallBack.onWifiBqeReturnHistoryScore(estimateApInfo2);
                                            break;
                                        }
                                    }
                                    break;
                                case 14:
                                    if (msg.obj != null) {
                                        Log.d("WiFi_PRO", "MSG_RETURN_AP_CURRY_RSSI rssi = " + rssi);
                                        if (NetworkQosMonitor.this.mCallBack != null) {
                                            NetworkQosMonitor.this.mCallBack.onWifiBqeReturnCurrentRssi(rssi);
                                            break;
                                        }
                                    }
                                    break;
                                case 15:
                                    Log.d("WiFi_PRO", "MSG_NETWORK_TYPE_BY_WEBVIEW_REQ:: begin");
                                    if (msg.arg1 != 1) {
                                        isOversea = false;
                                    }
                                    NetworkQosMonitor.this.startBqeService();
                                    NetworkQosMonitor.this.mBqeClient.recheckNetworkTypeByWebView(NetworkQosMonitor.this.mNetworkHandler, isOversea);
                                    break;
                                case 16:
                                    Log.d("WiFi_PRO", "MSG_NETWORK_TYPE_BY_WEBVIEW_RESP:: begin, result = " + msg.arg1);
                                    if (NetworkQosMonitor.this.mNetworkHandler.hasMessages(17)) {
                                        NetworkQosMonitor.this.mNetworkHandler.removeMessages(17);
                                    }
                                    NetworkQosMonitor.this.stopBqeService();
                                    Intent intentResponse = new Intent("com.huawei.wifipro.action.ACTION_RESP_WEBVIEW_CHECK");
                                    intentResponse.setFlags(67108864);
                                    intentResponse.putExtra("wifipro_flag_network_type", msg.arg1);
                                    NetworkQosMonitor.this.mContext.sendBroadcastAsUser(intentResponse, UserHandle.ALL, "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER");
                                    break;
                                case 17:
                                    Log.d("WiFi_PRO", "MSG_NETWORK_TYPE_BY_WEBVIEW_RESP_TIMEOUT");
                                    NetworkQosMonitor.this.stopBqeService();
                                    Intent intentTimeout = new Intent("com.huawei.wifipro.action.ACTION_RESP_WEBVIEW_CHECK");
                                    intentTimeout.setFlags(67108864);
                                    intentTimeout.putExtra("wifipro_flag_network_type", 100);
                                    NetworkQosMonitor.this.mContext.sendBroadcastAsUser(intentTimeout, UserHandle.ALL, "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER");
                                    break;
                                case 18:
                                    if (msg.obj != null) {
                                        NetworkQosMonitor.this.mCallBack.onNotifyWifiSecurityStatus((Bundle) msg.obj);
                                        break;
                                    }
                                    break;
                            }
                    }
                } else if (msg.obj != null) {
                    if (NetworkQosMonitor.this.mIsWaitingResponse && msg.arg1 == 0 && msg.arg2 > 7) {
                        NetworkQosMonitor.this.responseTcpRxPacketsCounter((int[]) msg.obj, msg.arg2);
                    }
                    if (NetworkQosMonitor.this.mTcpRxRequestedViaBroadcast && msg.arg1 == 0 && msg.arg2 > 7) {
                        NetworkQosMonitor.this.respTcpRxViaBroadcast((int[]) msg.obj, msg.arg2);
                    } else if (msg.arg1 == 2) {
                        NetworkQosMonitor.this.mBqeClient.setWlanRtt((int[]) msg.obj, msg.arg2);
                    } else if (msg.arg1 == 4) {
                        NetworkQosMonitor.this.mHuaweiWifiWatchdogStateMachine.setSampleRtt((int[]) msg.obj, msg.arg2);
                    } else {
                        int[] intArray = (int[]) msg.obj;
                        int report_type = msg.arg1;
                        int len = msg.arg2;
                        NetworkQosMonitor.this.mHuaweiWifiWatchdogStateMachine.tcpChkResultUpdate(intArray, len, report_type);
                        if (NetworkQosMonitor.this.mMobileQosDetector != null) {
                            NetworkQosMonitor.this.mMobileQosDetector.setIPQos(report_type, len, intArray);
                        } else {
                            Log.d("WiFi_PRO", "**error  mMobileQosDetector.setIPQos ********");
                        }
                    }
                }
                super.handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    public void sendNetworkLevel(int msg, int net_type, int level) {
        if (this.mCallBack != null) {
            switch (msg) {
                case 1:
                    this.mCallBack.onNetworkQosChange(net_type, level, true);
                    break;
                case 2:
                    Log.d("WiFi_PRO", "sendNetworkLevel, MSG_NETWORK_DETECTION_RESULT, net_type = " + net_type + ", level = " + level);
                    this.mCallBack.onNetworkDetectionResult(net_type, level);
                    break;
            }
        }
    }

    public void setMonitorMobileQos(boolean enabled) {
        Log.i("WiFi_PRO", "setMonitorMobileQos, enabled = " + enabled);
        if (this.mMobileQosDetector != null) {
            this.mMobileQosDetector.monitorNetworkQos(enabled);
        } else {
            Log.d("WiFi_PRO", "**error  setMonitorMobileQos ********");
        }
    }

    public void setMonitorWifiQos(int type, boolean enabled) {
        if (1 == type) {
            Log.i("WiFi_PRO", "setMonitorMobileQos,type = WIFI_CONNECT_WITH_DATA_LINK , enabled = " + enabled);
            if (enabled) {
                this.mHuaweiWifiWatchdogStateMachine.doWifiOTACheck(WifiproUtils.WIFIPRO_START_VERIFY_WITH_DATA_LINK);
            } else {
                this.mHuaweiWifiWatchdogStateMachine.doWifiOTACheck(WifiproUtils.WIFIPRO_STOP_VERIFY_WITH_DATA_LINK);
            }
        } else if (2 == type) {
            Log.i("WiFi_PRO", "setMonitorMobileQos,type = WIFI_VERIFY_NO_DATA_LINK , enabled = " + enabled);
            if (enabled) {
                this.mHuaweiWifiWatchdogStateMachine.doWifiOTACheck(WifiproUtils.WIFIPRO_START_VERIFY_WITH_NOT_DATA_LINK);
            } else {
                this.mHuaweiWifiWatchdogStateMachine.doWifiOTACheck(WifiproUtils.WIFIPRO_STOP_VERIFY_WITH_NOT_DATA_LINK);
            }
        }
    }

    public void queryNetworkQos(int network_type, boolean portal, boolean authen, boolean wifiBackground) {
        if (network_type == 0) {
            Log.d("WiFi_PRO", "Query Mobile Qos");
            if (this.mMobileQosDetector != null) {
                this.mMobileQosDetector.queryNetworkQos();
            } else {
                Log.d("WiFi_PRO", "**error  mMobileQosDetector.queryNetworkQos ********");
            }
        } else if (1 == network_type) {
            Log.d("WiFi_PRO", "start check wifi internet, portal = " + portal + ", authen = " + authen);
            this.mNetworkHandler.sendMessage(Message.obtain(this.mNetworkHandler, 6, (int) portal, (int) authen, Boolean.valueOf(wifiBackground)));
        }
    }

    public String getCurrentNetworkId(int network_type) {
        if (network_type != 0) {
            return null;
        }
        if (this.mMobileQosDetector != null) {
            return this.mMobileQosDetector.queryNetworkId();
        }
        Log.d("WiFi_PRO", "**error  mMobileQosDetector.queryNetworkId ********");
        return null;
    }

    public HwNetworkPropertyChecker getNetworkPropertyChecker() {
        return this.mHwNetworkPropertyChecker;
    }

    public void queryRxPackets() {
        this.mIpQosMonitor.queryPackets(0);
    }

    public void notifyNetworkResult(int respCode) {
        int property = -1;
        if (respCode == 204) {
            property = 5;
        } else if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(respCode)) {
            property = 6;
        } else if (respCode == 600) {
            property = 7;
        }
        syncNotifyPowerSaveGenie(property == 5, 100, false);
        Log.d("WiFi_PRO", "notifyNetworkResult, respCode = " + respCode + ", property = " + property);
        this.mNetworkHandler.sendMessage(Message.obtain(this.mNetworkHandler, 2, 1, property));
    }

    public void setWifiWatchDogEnabled(boolean enabled) {
        Log.d("WiFi_PRO", "setWifiWatchDogEnabled, enabled = " + enabled);
        this.mHuaweiWifiWatchdogStateMachine.enableCheck(enabled);
        if (enabled) {
            Log.i("ipQos", " ipQos init");
            this.mIpQosMonitor.init();
            return;
        }
        Log.i("ipQos", " ipQos release");
        this.mIpQosMonitor.release();
        stopALLMonitor();
    }

    public void stopALLMonitor() {
        Log.w("WiFi_PRO", "stopALLMonitor");
        setIpQosEnabled(false);
        setMonitorMobileQos(false);
        setMonitorWifiQos(1, false);
        setMonitorWifiQos(2, false);
    }

    public void resetMonitorStatus() {
        Log.w("WiFi_PRO", "resetMonitorStatus");
        this.mBroadcastNotified.set(false);
        if (this.mHwNetworkPropertyChecker != null) {
            this.mHwNetworkPropertyChecker.resetCheckerStatus();
        }
    }

    public synchronized boolean startBqeService() {
        if (!this.mIsBQEServiceStart) {
            this.mIsBQEServiceStart = true;
            this.mBqeClient.startBqeService();
            Log.d("WiFi_PRO", "startBqeService ");
            return true;
        }
        Log.d("WiFi_PRO", "BqeService is running,ignore restart ");
        return false;
    }

    public synchronized void stopBqeService() {
        Log.w("WiFi_PRO", "stopBqeService, mIsBQEServiceStart = " + this.mIsBQEServiceStart);
        if (this.mIsBQEServiceStart) {
            this.mIsBQEServiceStart = false;
            this.mBqeClient.stopBqeService();
        }
    }

    public void startWiFiBqeDetect(int interval) {
        this.mBqeClient.resetBqeState();
        this.mBqeClient.requestBqeOnce(interval, this.mNetworkHandler);
    }

    public boolean isBqeServicesStarted() {
        return this.mBqeClient.isBqeServicesStarted();
    }

    public void queryWifiSecurity(String ssid, String bssid) {
        Log.d("WiFi_PRO", "queryWifiSecurity, ssid = " + ssid);
        if (ssid != null && bssid != null) {
            Intent intent = new Intent("com.huawei.wifipro.ACTION_QUERY_WIFI_SECURITY");
            intent.setFlags(67108864);
            intent.putExtra("com.huawei.wifipro.FLAG_SSID", ssid);
            intent.putExtra("com.huawei.wifipro.FLAG_BSSID", bssid);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.wifipro.permission.WIFI_SECURITY_CHECK");
        }
    }

    public synchronized int getCurrentWiFiLevel() {
        int wifilevel;
        wifilevel = this.mHuaweiWifiWatchdogStateMachine.updateQosLevelByHistory();
        Log.d("WiFi_PRO", "CurrentWiFi QOE level: " + wifilevel);
        return wifilevel;
    }

    public static boolean isBetaVer(Context context) {
        return Version.isBeta(context);
    }

    public void setRoveOutToMobileState(int roState) {
        if (this.mMobileQosDetector != null) {
            this.mMobileQosDetector.setRoveOutToMobileState(roState);
        }
    }

    public boolean isHighDataFlowModel() {
        return this.mHuaweiWifiWatchdogStateMachine.isHighDataFlowModel();
    }

    public void get5GApRssiThreshold(WifiProEstimateApInfo apInfo) {
        if (apInfo == null) {
            Log.e("WiFi_PRO", "getHandover5GApRssiThreshold apInfo null error");
        } else {
            this.mHuaweiWifiWatchdogStateMachine.getHandover5GApRssiThreshold(apInfo);
        }
    }

    public void getApHistoryQualityScore(WifiProEstimateApInfo apInfo) {
        if (apInfo == null) {
            Log.e("WiFi_PRO", "getHandover5GApRssiThreshold apInfo null error");
        } else {
            this.mHuaweiWifiWatchdogStateMachine.getApHistoryQualityScore(apInfo);
        }
    }

    public int requestTcpRxPacketsCounter() {
        int rxCounter = -1;
        Log.i("WiFi_PRO", "requestTcpRxPacketsCounter, start to request tcp rx packets counter.");
        synchronized (this.mQueryTcpRxLock) {
            try {
                if (this.mIpQosMonitor != null) {
                    this.mIpQosMonitor.queryPackets(0);
                    this.mIsWaitingResponse = true;
                    this.mNetworkHandler.sendEmptyMessageDelayed(11, 500);
                    while (this.mIsWaitingResponse) {
                        Log.i("WiFi_PRO", "requestTcpRxCounter, start wait for notify");
                        this.mQueryTcpRxLock.wait();
                    }
                    if (this.mTcpRxCounter >= 0) {
                        rxCounter = this.mTcpRxCounter;
                    }
                    Log.i("WiFi_PRO", "tcp rx packets counter received rx = " + this.mTcpRxCounter);
                }
            } catch (InterruptedException e) {
            }
        }
        this.mTcpRxCounter = 0;
        return rxCounter;
    }

    /* access modifiers changed from: private */
    public void responseTcpRxPacketsCounter(int[] rxResp, int len) {
        if (rxResp != null && len > 7) {
            Log.d("WiFi_PRO", "responseTcpRxPacketsCounter, rx packets counter = " + rxResp[7]);
            this.mIsWaitingResponse = false;
            if (this.mNetworkHandler.hasMessages(11)) {
                this.mNetworkHandler.removeMessages(11);
            }
            responseTcpRxPacketsCounter(rxResp[7]);
        }
    }

    /* access modifiers changed from: private */
    public void responseTcpRxPacketsCounter(int counter) {
        synchronized (this.mQueryTcpRxLock) {
            this.mTcpRxCounter = counter;
            this.mIsWaitingResponse = false;
            Log.i("WiFi_PRO", "responseTcpRxPacketsCounter, mQueryTcpRxLock.notifyAll() called, current counter = " + this.mTcpRxCounter);
            this.mQueryTcpRxLock.notifyAll();
        }
    }

    private void sendResutl4PowerSaveGenie(boolean hasInternetAccess, boolean portalConnected) {
        Log.i("WiFi_PRO", "sendResutl4PowerSaveGenie, hasInternetAccess = " + hasInternetAccess + ", portalConnected = " + portalConnected);
        Intent intent = new Intent("com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_INTERNET_STATUS");
        intent.setFlags(67108864);
        intent.putExtra("internet_status", hasInternetAccess);
        intent.putExtra("network_connected_status_portal", portalConnected);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void syncNotifyPowerSaveGenie(boolean hasInternetAccess, int detailedType, boolean init) {
        synchronized (this.mSyncNotifyLock) {
            Log.i("WiFi_PRO", "syncNotifyPowerSaveGenie, hasInternetAccess = " + hasInternetAccess + ", init = " + init + ", lastNotifyNetworkType = " + this.mLastNotifyNetworkType + ", detailedType = " + detailedType);
            boolean z = false;
            if (init) {
                this.mLastNotifyNetworkType = detailedType;
                if (detailedType == 104) {
                    hasInternetAccess = true;
                }
                if (detailedType >= 103) {
                    z = true;
                }
                sendResutl4PowerSaveGenie(hasInternetAccess, z);
            } else if (hasInternetAccess && (this.mLastNotifyNetworkType == 102 || this.mLastNotifyNetworkType == 103)) {
                this.mLastNotifyNetworkType = 101;
                sendResutl4PowerSaveGenie(hasInternetAccess, false);
            } else if (!hasInternetAccess && this.mLastNotifyNetworkType == 101) {
                this.mLastNotifyNetworkType = 102;
                sendResutl4PowerSaveGenie(hasInternetAccess, false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifySecurityResult(Bundle bundle) {
        Message.obtain(this.mNetworkHandler, 18, bundle).sendToTarget();
    }
}
