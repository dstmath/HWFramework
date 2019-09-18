package com.android.server.wifi.wifipro;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.wifi.HwPortalExceptionManager;
import com.android.server.wifi.HwSelfCureEngine;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import java.security.SecureRandom;

public class HwWifiproLiteStateMachine extends StateMachine {
    public static final String ACTION_NOTIFY_WIFI_INTERNET_STATUS = "com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_INTERNET_STATUS";
    private static final int CMD_HTTP_GET_RESULT_RCVD = 109;
    private static final int CMD_HTTP_UNREACHABLE_BY_FIRST_DETECT = 103;
    private static final int CMD_INTERNET_CAPABILITY_RCVD = 104;
    private static final int CMD_INTERNET_STATUS_DETECT_INTERVAL = 107;
    private static final int CMD_NETWORK_CONNECTED_RCVD = 101;
    private static final int CMD_NETWORK_DISCONNECTED_RCVD = 102;
    private static final int CMD_NOTIFY_NO_INTERNET_REASON = 110;
    private static final int CMD_NO_INTERNET_DETECT_INTERVAL = 106;
    private static final int CMD_NO_INTERNET_SHOW_TOAST = 112;
    private static final int CMD_PORTAL_DETECT_INTERVAL = 105;
    private static final int CMD_SCE_NOTIFY_HTTP_GET_RESULT = 108;
    private static final int CMD_TCP_PKTS_RESP_RCVD = 100;
    private static final int CMD_WIFI_DISABLED_RCVD = 111;
    public static final String EXTRA_INTERNET_STATUS = "internet_status";
    public static final String EXTRA_NETWORK_CONNECTED_STATUS = "network_connected_status_portal";
    private static final int NO_INTERNET_BY_PORTAL = -101;
    private static final int NO_INTERNET_BY_UNREACHABLE = -102;
    private static final String TAG = "HwWifiproLiteStateMachine";
    private static HwWifiproLiteStateMachine mLiteStateMachine = null;
    /* access modifiers changed from: private */
    public State mConnectedState = new ConnectedState();
    /* access modifiers changed from: private */
    public ConnectivityManager mConnectivityManager;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public String mCurrentSsid;
    /* access modifiers changed from: private */
    public WifiConfiguration mCurrentWifiConfig;
    private State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public State mDisconnectedState = new DisconnectedState();
    /* access modifiers changed from: private */
    public State mHasInternetMonitorState = new HasInternetMonitorState();
    private boolean mInitialized = false;
    /* access modifiers changed from: private */
    public IPQosMonitor mIpQosMonitor;
    /* access modifiers changed from: private */
    public State mNoInternetMonitorState = new NoInternetMonitorState();
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    private WiFiProEvaluateController mWiFiProEvaluateController;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    private WifiProConfigStore mWifiProConfigStore;
    /* access modifiers changed from: private */
    public WifiProUIDisplayManager mWifiProUIDisplayManager;
    /* access modifiers changed from: private */
    public AsyncChannel mWsmChannel;

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            WifiConfiguration unused = HwWifiproLiteStateMachine.this.mCurrentWifiConfig = WifiProCommonUtils.getCurrentWifiConfig(HwWifiproLiteStateMachine.this.mWifiManager);
            String unused2 = HwWifiproLiteStateMachine.this.mCurrentSsid = WifiProCommonUtils.getCurrentSsid(HwWifiproLiteStateMachine.this.mWifiManager);
            HwWifiproLiteStateMachine hwWifiproLiteStateMachine = HwWifiproLiteStateMachine.this;
            StringBuilder sb = new StringBuilder();
            sb.append("==> ##ConnectedState, network = ");
            sb.append(HwWifiproLiteStateMachine.this.mCurrentWifiConfig != null ? HwWifiproLiteStateMachine.this.mCurrentWifiConfig.configKey() : HwWifiproLiteStateMachine.this.mCurrentSsid);
            hwWifiproLiteStateMachine.LOGD(sb.toString());
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 111) {
                switch (i) {
                    case 102:
                        HwWifiproLiteStateMachine.this.LOGD("##CMD_NETWORK_DISCONNECTED_RCVD");
                        HwWifiproLiteStateMachine.this.transitionTo(HwWifiproLiteStateMachine.this.mDisconnectedState);
                        break;
                    case 103:
                        if (HwWifiproLiteStateMachine.this.getCurrentState() == HwWifiproLiteStateMachine.this.mConnectedState) {
                            HwWifiproLiteStateMachine.this.LOGD("##CMD_HTTP_UNREACHABLE_BY_FIRST_DETECT");
                            handleHttpUnreachableByFirstDetect();
                            break;
                        }
                        break;
                    case 104:
                        if (HwWifiproLiteStateMachine.this.getCurrentState() == HwWifiproLiteStateMachine.this.mConnectedState) {
                            HwWifiproLiteStateMachine hwWifiproLiteStateMachine = HwWifiproLiteStateMachine.this;
                            hwWifiproLiteStateMachine.LOGD("##CMD_INTERNET_CAPABILITY_RCVD, internetCapability = " + message.arg1);
                            handleInternetCapabilityRcvd(message.arg1);
                            break;
                        }
                        break;
                    default:
                        return false;
                }
            } else {
                HwWifiproLiteStateMachine.this.LOGD("##CMD_WIFI_DISABLED_RCVD");
                HwWifiproLiteStateMachine.this.transitionTo(HwWifiproLiteStateMachine.this.mDisconnectedState);
            }
            return true;
        }

        private void handleHttpUnreachableByFirstDetect() {
            HwWifiproLiteStateMachine.this.updateInternetCapabilityUI(true, false);
        }

        private void handleInternetCapabilityRcvd(int internetCapability) {
            if (internetCapability == -1) {
                HwWifiproLiteStateMachine.this.transitionTo(HwWifiproLiteStateMachine.this.mNoInternetMonitorState);
                HwWifiproLiteStateMachine.this.sendMessageDelayed(HwWifiproLiteStateMachine.this.obtainMessage(110, -102, 0), 100);
            } else if (internetCapability == 6) {
                HwWifiproLiteStateMachine.this.transitionTo(HwWifiproLiteStateMachine.this.mNoInternetMonitorState);
                HwWifiproLiteStateMachine.this.sendMessageDelayed(HwWifiproLiteStateMachine.this.obtainMessage(110, -101, 0), 100);
            } else if (internetCapability == 5) {
                HwWifiproLiteStateMachine.this.transitionTo(HwWifiproLiteStateMachine.this.mHasInternetMonitorState);
            } else {
                HwWifiproLiteStateMachine hwWifiproLiteStateMachine = HwWifiproLiteStateMachine.this;
                hwWifiproLiteStateMachine.LOGD("handleInternetCapabilityRcvd, unknown internetCapability = " + internetCapability);
            }
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            return true;
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            HwWifiproLiteStateMachine.this.LOGD("==> ##DisconnectedState");
            String unused = HwWifiproLiteStateMachine.this.mCurrentSsid = null;
            WifiConfiguration unused2 = HwWifiproLiteStateMachine.this.mCurrentWifiConfig = null;
        }

        public boolean processMessage(Message message) {
            if (message.what != 101) {
                return false;
            }
            HwWifiproLiteStateMachine.this.transitionTo(HwWifiproLiteStateMachine.this.mConnectedState);
            return true;
        }
    }

    class HasInternetMonitorState extends State {
        private static final int INTERNET_STATUS_DETECT_INTERVAL_MS = 8000;
        private int httpGetReqSessionId = -1;
        private int mInternetFailedCounter = 0;
        private boolean mInternetSelfCureAllowed = true;
        private int mLastDnsFailCounter = 0;
        private int mLastTcpRxCounter = 0;
        private int mLastTcpTxCounter = 0;
        private boolean mMobileHotspot = false;

        HasInternetMonitorState() {
        }

        public void enter() {
            this.mInternetFailedCounter = 0;
            this.mLastDnsFailCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
            this.mLastTcpTxCounter = 0;
            this.mLastTcpRxCounter = 0;
            this.httpGetReqSessionId = -1;
            this.mInternetSelfCureAllowed = true;
            this.mMobileHotspot = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(HwWifiproLiteStateMachine.this.mContext);
            HwWifiproLiteStateMachine hwWifiproLiteStateMachine = HwWifiproLiteStateMachine.this;
            hwWifiproLiteStateMachine.LOGD("==> ##HasInternetMonitorState, currentSsid = " + HwWifiproLiteStateMachine.this.mCurrentSsid + ", mobileHotspot = " + this.mMobileHotspot);
            HwSelfCureEngine.getInstance().notifyInternetAccessRecovery();
            HwWifiproLiteStateMachine.this.updateInternetCapabilityUI(false, false);
            HwWifiproLiteStateMachine.this.sendMessageDelayed(107, 8000);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 100) {
                if (parseNetworkInternetGood(message)) {
                    this.mInternetFailedCounter = 0;
                    this.mInternetSelfCureAllowed = true;
                } else {
                    this.mInternetFailedCounter++;
                    if (this.mInternetFailedCounter >= 2 && WifiProCommonUtils.isWifiConnected(HwWifiproLiteStateMachine.this.mWifiManager)) {
                        WifiInfo wifiInfo = HwWifiproLiteStateMachine.this.mWifiManager.getConnectionInfo();
                        int currentRssi = wifiInfo.getRssi();
                        HwWifiproLiteStateMachine.this.LOGD("internet access abnormal, currentRssi = " + currentRssi + ", internetFailedCounter = " + this.mInternetFailedCounter + ", mobileHotspot = " + this.mMobileHotspot);
                        if (allowSelfCureNetwork(currentRssi)) {
                            HwWifiproLiteStateMachine.this.LOGD("notify SCE(Self Cure Engine) to handle it, internetFailedCounter = " + this.mInternetFailedCounter);
                            HwSelfCureEngine.getInstance().notifyInternetFailureDetected(false);
                            this.mInternetSelfCureAllowed = false;
                        } else if (WifiProCommonUtils.getCurrenSignalLevel(wifiInfo) >= 2) {
                            this.httpGetReqSessionId = HwWifiproLiteStateMachine.this.asynDoHttpGetOneTime();
                        } else {
                            handleNoInternetAccessMidway();
                        }
                    }
                }
                HwWifiproLiteStateMachine.this.sendMessageDelayed(107, 8000);
            } else if (i != 112) {
                switch (i) {
                    case 107:
                        if (!HwSelfCureEngine.getInstance().isSelfCureOngoing() && WifiProCommonUtils.isWifiConnected(HwWifiproLiteStateMachine.this.mWifiManager)) {
                            HwWifiproLiteStateMachine.this.mIpQosMonitor.queryPackets(0);
                            break;
                        } else {
                            HwWifiproLiteStateMachine.this.LOGD("SelfCureOngoing or supp state isn't completed, internetFailedCounter = " + this.mInternetFailedCounter);
                            HwWifiproLiteStateMachine.this.sendMessageDelayed(107, 8000);
                            break;
                        }
                        break;
                    case 108:
                        HwWifiproLiteStateMachine.this.removeMessages(107);
                        boolean httpReachable = ((Boolean) message.obj).booleanValue();
                        HwWifiproLiteStateMachine.this.LOGD("#CMD_SCE_NOTIFY_HTTP_GET_RESULT, httpReachable = " + httpReachable);
                        if (!httpReachable) {
                            handleNoInternetAccessMidway();
                            break;
                        } else {
                            resetByInternetRechable();
                            HwWifiproLiteStateMachine.this.sendMessageDelayed(107, 8000);
                            break;
                        }
                    case 109:
                        HwWifiproLiteStateMachine.this.LOGD("#CMD_HTTP_GET_RESULT_RCVD, respCode = " + message.arg2);
                        if (this.httpGetReqSessionId == message.arg1) {
                            if (message.arg2 != 204 && !WifiProCommonUtils.isRedirectedRespCodeByGoogle(message.arg2)) {
                                handleNoInternetAccessMidway();
                                break;
                            } else {
                                resetByInternetRechable();
                                HwWifiproLiteStateMachine.this.sendMessageDelayed(107, 8000);
                                break;
                            }
                        } else {
                            HwWifiproLiteStateMachine.this.LOGD("#CMD_HTTP_GET_RESULT_RCVD, httpGetReqSessionId unmatched!");
                            break;
                        }
                        break;
                    default:
                        return false;
                }
            } else {
                WifiProUIDisplayManager access$1400 = HwWifiproLiteStateMachine.this.mWifiProUIDisplayManager;
                WifiProUIDisplayManager unused = HwWifiproLiteStateMachine.this.mWifiProUIDisplayManager;
                access$1400.showWifiProToast(1);
            }
            return true;
        }

        public void exit() {
            HwWifiproLiteStateMachine.this.LOGD("exit ##HasInternetMonitorState");
            HwWifiproLiteStateMachine.this.removeMessages(107);
        }

        private void resetByInternetRechable() {
            this.mLastTcpTxCounter = 0;
            this.mLastTcpRxCounter = 0;
            this.httpGetReqSessionId = -1;
            this.mInternetFailedCounter = 0;
            this.mInternetSelfCureAllowed = true;
        }

        private boolean allowSelfCureNetwork(int currentRssi) {
            if (this.mMobileHotspot || !this.mInternetSelfCureAllowed || currentRssi < -70 || HwSelfCureEngine.getInstance().isSelfCureOngoing()) {
                return false;
            }
            return true;
        }

        private void handleNoInternetAccessMidway() {
            HwWifiproLiteStateMachine.this.mWsmChannel.sendMessage(131875);
            HwWifiproLiteStateMachine.this.transitionTo(HwWifiproLiteStateMachine.this.mNoInternetMonitorState);
            HwWifiproLiteStateMachine.this.sendMessageDelayed(HwWifiproLiteStateMachine.this.obtainMessage(110, -102, 0), 100);
        }

        private boolean parseNetworkInternetGood(Message message) {
            boolean queryResp = message.arg1 == 0;
            int packetsLength = message.arg2;
            if (queryResp && packetsLength > 7) {
                int[] packets = (int[]) message.obj;
                int tcpTxPkts = packets[6];
                int tcpRxPkts = packets[7];
                if (this.mLastTcpTxCounter == 0 || this.mLastTcpRxCounter == 0) {
                    this.mLastTcpTxCounter = tcpTxPkts;
                    this.mLastTcpRxCounter = tcpRxPkts;
                    this.mLastDnsFailCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    return true;
                }
                int deltaTcpTxPkts = tcpTxPkts - this.mLastTcpTxCounter;
                int deltaTcpRxPkts = tcpRxPkts - this.mLastTcpRxCounter;
                this.mLastTcpTxCounter = tcpTxPkts;
                this.mLastTcpRxCounter = tcpRxPkts;
                if (deltaTcpRxPkts == 0) {
                    if (deltaTcpTxPkts >= 3) {
                        this.mLastDnsFailCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                        return false;
                    }
                    int currentDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    int deltaFailedDns = currentDnsFailedCounter - this.mLastDnsFailCounter;
                    this.mLastDnsFailCounter = currentDnsFailedCounter;
                    if (deltaFailedDns >= 2) {
                        return false;
                    }
                }
            }
            this.mLastDnsFailCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
            return true;
        }
    }

    private class NetworkCheckThread extends Thread {
        private Context mContext;
        private int mSessionId = -1;
        private HwWifiproLiteStateMachine mStateMachine;

        public NetworkCheckThread(Context context, HwWifiproLiteStateMachine stateMachine, int sessionId) {
            this.mContext = context;
            this.mStateMachine = stateMachine;
            this.mSessionId = sessionId;
        }

        public void run() {
            HwNetworkPropertyChecker checker = new HwNetworkPropertyChecker(this.mContext, null, null, true, null, false);
            int respCode = checker.isCaptivePortal(true);
            checker.release();
            this.mStateMachine.sendMessage(109, this.mSessionId, respCode);
        }
    }

    class NoInternetMonitorState extends State {
        private static final int NO_INTERNET_DETECT_INTERVAL_MS = 5000;
        private static final int PORTAL_DETECT_INTERVAL_MS = 15000;
        private int httpGetReqSessionId = -1;
        private int mTcpRxPacketsCounter = 0;
        private int noInternetReason = -1;

        NoInternetMonitorState() {
        }

        public void enter() {
            HwWifiproLiteStateMachine hwWifiproLiteStateMachine = HwWifiproLiteStateMachine.this;
            hwWifiproLiteStateMachine.LOGD("==> ##NoInternetMonitorState, currentSsid = " + HwWifiproLiteStateMachine.this.mCurrentSsid);
            this.httpGetReqSessionId = -1;
            this.noInternetReason = -1;
            this.mTcpRxPacketsCounter = 0;
        }

        public boolean processMessage(Message message) {
            boolean z = false;
            switch (message.what) {
                case 100:
                    int rx = parseTcpRxPacketsCounter(message);
                    if (rx > 0) {
                        if (this.mTcpRxPacketsCounter != 0 && rx - this.mTcpRxPacketsCounter >= 5) {
                            this.mTcpRxPacketsCounter = rx;
                            this.httpGetReqSessionId = HwWifiproLiteStateMachine.this.asynDoHttpGetOneTime();
                            break;
                        } else {
                            this.mTcpRxPacketsCounter = rx;
                        }
                    }
                    HwWifiproLiteStateMachine.this.sendMessageDelayed(106, 5000);
                    break;
                case 105:
                    if (HwWifiproLiteStateMachine.this.mPowerManager.isScreenOn()) {
                        this.httpGetReqSessionId = HwWifiproLiteStateMachine.this.asynDoHttpGetOneTime();
                        break;
                    } else {
                        HwWifiproLiteStateMachine.this.sendMessageDelayed(106, 5000);
                        break;
                    }
                case 106:
                    if (HwWifiproLiteStateMachine.this.mPowerManager.isScreenOn()) {
                        HwWifiproLiteStateMachine.this.mIpQosMonitor.queryPackets(0);
                        break;
                    } else {
                        HwWifiproLiteStateMachine.this.sendMessageDelayed(106, 5000);
                        break;
                    }
                case 109:
                    HwWifiproLiteStateMachine.this.LOGD("#CMD_HTTP_GET_RESULT_RCVD, respCode = " + message.arg2);
                    if (this.httpGetReqSessionId == message.arg1) {
                        if (message.arg2 != 204) {
                            if (this.noInternetReason != -102) {
                                if (this.noInternetReason == -101) {
                                    HwWifiproLiteStateMachine.this.sendMessageDelayed(105, 15000);
                                    break;
                                }
                            } else {
                                HwWifiproLiteStateMachine.this.sendMessageDelayed(106, 5000);
                                break;
                            }
                        } else {
                            if (this.noInternetReason == -101) {
                                notifyPortalHasInternetAccess();
                            }
                            HwWifiproLiteStateMachine.this.transitionTo(HwWifiproLiteStateMachine.this.mHasInternetMonitorState);
                            break;
                        }
                    } else {
                        HwWifiproLiteStateMachine.this.LOGD("#CMD_HTTP_GET_RESULT_RCVD, httpGetReqSessionId unmatched!");
                        break;
                    }
                    break;
                case 110:
                    this.noInternetReason = message.arg1;
                    HwWifiproLiteStateMachine hwWifiproLiteStateMachine = HwWifiproLiteStateMachine.this;
                    StringBuilder sb = new StringBuilder();
                    sb.append("#CMD_NOTIFY_NO_INTERNET_REASON, reason = ");
                    sb.append(this.noInternetReason == -101 ? "portal unlogin" : "http unreachable");
                    hwWifiproLiteStateMachine.LOGD(sb.toString());
                    if (this.noInternetReason == -102) {
                        HwSelfCureEngine.getInstance().notifyInternetFailureDetected(true);
                        HwWifiproLiteStateMachine.this.sendMessageDelayed(106, 5000);
                    } else if (this.noInternetReason == -101) {
                        HwWifiproLiteStateMachine.this.sendMessageDelayed(105, 15000);
                    }
                    HwWifiproLiteStateMachine hwWifiproLiteStateMachine2 = HwWifiproLiteStateMachine.this;
                    if (this.noInternetReason == -101) {
                        z = true;
                    }
                    hwWifiproLiteStateMachine2.updateInternetCapabilityUI(true, z);
                    break;
                case 112:
                    WifiProUIDisplayManager access$1400 = HwWifiproLiteStateMachine.this.mWifiProUIDisplayManager;
                    WifiProUIDisplayManager unused = HwWifiproLiteStateMachine.this.mWifiProUIDisplayManager;
                    access$1400.showWifiProToast(1);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwWifiproLiteStateMachine.this.LOGD("exit ##NoInternetMonitorState");
            HwWifiproLiteStateMachine.this.removeMessages(106);
            HwWifiproLiteStateMachine.this.removeMessages(105);
        }

        private int parseTcpRxPacketsCounter(Message message) {
            boolean queryResp = message.arg1 == 0;
            int packetsLength = message.arg2;
            if (!queryResp || packetsLength <= 7) {
                return 0;
            }
            return ((int[]) message.obj)[7];
        }

        private void notifyPortalHasInternetAccess() {
            if (isProvisioned(HwWifiproLiteStateMachine.this.mContext)) {
                Log.d(HwWifiproLiteStateMachine.TAG, "portal has internet access, force network re-evaluation");
                ConnectivityManager connMgr = ConnectivityManager.from(HwWifiproLiteStateMachine.this.mContext);
                Network[] info = connMgr.getAllNetworks();
                int length = info.length;
                int i = 0;
                while (i < length) {
                    Network nw = info[i];
                    NetworkCapabilities nc = connMgr.getNetworkCapabilities(nw);
                    if (!nc.hasTransport(1) || !nc.hasCapability(12)) {
                        i++;
                    } else {
                        connMgr.reportNetworkConnectivity(nw, false);
                        return;
                    }
                }
            }
        }

        private boolean isProvisioned(Context context) {
            return Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) == 1;
        }
    }

    public static synchronized HwWifiproLiteStateMachine getInstance(Context context, Messenger messenger) {
        HwWifiproLiteStateMachine hwWifiproLiteStateMachine;
        synchronized (HwWifiproLiteStateMachine.class) {
            if (mLiteStateMachine == null) {
                mLiteStateMachine = new HwWifiproLiteStateMachine(context, messenger);
            }
            hwWifiproLiteStateMachine = mLiteStateMachine;
        }
        return hwWifiproLiteStateMachine;
    }

    public static synchronized HwWifiproLiteStateMachine getInstance() {
        HwWifiproLiteStateMachine hwWifiproLiteStateMachine;
        synchronized (HwWifiproLiteStateMachine.class) {
            hwWifiproLiteStateMachine = mLiteStateMachine;
        }
        return hwWifiproLiteStateMachine;
    }

    private HwWifiproLiteStateMachine(Context context, Messenger messenger) {
        super(TAG);
        Looper looper = null;
        this.mWifiProConfigStore = null;
        this.mWiFiProEvaluateController = null;
        this.mWifiProUIDisplayManager = null;
        this.mContext = null;
        this.mWifiManager = null;
        this.mPowerManager = null;
        this.mWsmChannel = null;
        this.mCurrentSsid = null;
        this.mCurrentWifiConfig = null;
        this.mIpQosMonitor = null;
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mWsmChannel = new AsyncChannel();
        this.mWsmChannel.connectSync(this.mContext, getHandler(), messenger);
        this.mWifiProConfigStore = new WifiProConfigStore(this.mContext, this.mWsmChannel);
        this.mWiFiProEvaluateController = new WiFiProEvaluateController(context);
        this.mWifiProUIDisplayManager = WifiProUIDisplayManager.createInstance(context, null);
        this.mIpQosMonitor = new IPQosMonitor(getHandler());
        HwAutoConnectManager.getInstance(context, null).init(getHandler() != null ? getHandler().getLooper() : null);
        WifiProStatisticsManager.initStatisticsManager(this.mContext, getHandler() != null ? getHandler().getLooper() : looper);
        HwPortalExceptionManager.getInstance(context).init();
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        addState(this.mDefaultState);
        addState(this.mDisconnectedState, this.mDefaultState);
        addState(this.mConnectedState, this.mDefaultState);
        addState(this.mHasInternetMonitorState, this.mConnectedState);
        addState(this.mNoInternetMonitorState, this.mConnectedState);
        setInitialState(this.mDisconnectedState);
        start();
    }

    public synchronized void setup() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            registerReceivers();
        }
    }

    public void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("com.huawei.wifi.action.FIRST_CHECK_NO_INTERNET_NOTIFICATION");
        intentFilter.addAction("com.huawei.wifi.action.NETWOR_PROPERTY_NOTIFICATION");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                        HwWifiproLiteStateMachine.this.sendMessage(102);
                    } else if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        HwWifiproLiteStateMachine.this.sendMessage(101);
                    }
                } else if ("com.huawei.wifi.action.FIRST_CHECK_NO_INTERNET_NOTIFICATION".equals(intent.getAction())) {
                    HwWifiproLiteStateMachine.this.sendMessage(103);
                } else if ("com.huawei.wifi.action.NETWOR_PROPERTY_NOTIFICATION".equals(intent.getAction())) {
                    HwWifiproLiteStateMachine.this.sendMessage(104, intent.getIntExtra("wifi_network_property", -1), 0);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                    if (intent.getIntExtra("wifi_state", 4) == 1) {
                        HwWifiproLiteStateMachine.this.sendMessage(111);
                    }
                } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                    int networkType = intent.getIntExtra("networkType", 1);
                    NetworkInfo mobileInfo = HwWifiproLiteStateMachine.this.mConnectivityManager.getNetworkInfo(0);
                    if (networkType == 0 && mobileInfo != null && NetworkInfo.DetailedState.CONNECTED == mobileInfo.getDetailedState()) {
                        HwWifiproLiteStateMachine.this.sendMessage(112);
                    }
                }
            }
        }, intentFilter);
    }

    /* access modifiers changed from: private */
    public void updateInternetCapabilityUI(boolean noInternetAccess, boolean noInternetByPortal) {
        if (this.mCurrentWifiConfig == null) {
            LOGD("updateInternetCapabilityUI, but configuration is null.");
            return;
        }
        this.mWifiProUIDisplayManager.notificateNetAccessChange(noInternetAccess);
        if (!noInternetAccess) {
            this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, false, 0, false);
            this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 4, this.mCurrentSsid);
            this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 4);
        } else if (noInternetByPortal) {
            this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, noInternetAccess, 1, false);
            this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 3);
        } else {
            this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, noInternetAccess, 0, false);
            this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 2);
        }
        sendResutlToPowerSaveGenie(!noInternetAccess, noInternetByPortal);
    }

    private void sendResutlToPowerSaveGenie(boolean hasInternetAccess, boolean portalConnected) {
        Intent intent = new Intent("com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_INTERNET_STATUS");
        intent.setFlags(67108864);
        intent.putExtra("internet_status", hasInternetAccess);
        intent.putExtra("network_connected_status_portal", portalConnected);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    public int asynDoHttpGetOneTime() {
        int sessionId = new SecureRandom().nextInt(100000);
        new NetworkCheckThread(this.mContext, this, sessionId).start();
        return sessionId;
    }

    public void notifyHttpReachable(boolean isReachable) {
        sendMessage(108, Boolean.valueOf(isReachable));
    }

    /* access modifiers changed from: private */
    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
