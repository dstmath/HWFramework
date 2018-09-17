package com.android.server;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.wifipro.PortalDataBaseManager;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwNetworkPropertyChecker {
    public static final String CHINA_MAINLAND_BACKUP_SERVER = "http://www.youku.com";
    public static final String CHINA_MAINLAND_MAIN_SERVER = "http://www.baidu.com";
    public static final int HW_DEFAULT_REEVALUATE_DELAY_MS = 2000;
    public static final int HW_MAX_RETRIES = 3;
    private static final int LAC_UNKNOWN = -1;
    public static final int MIN_RX_PKT_DELTA = 3;
    private static final int MSG_DNS_RESP_RCVD = 104;
    private static final int MSG_HTTP_RESP_RCVD = 103;
    private static final int MSG_HTTP_RESP_TIMEOUT = 101;
    private static final int MSG_NETWORK_DISCONNECTED = 102;
    public static final int NETWORK_PROPERTY_INTERNET = 5;
    public static final int NETWORK_PROPERTY_NO_INTERNET = -1;
    public static final int NETWORK_PROPERTY_PENDING = -101;
    public static final int NETWORK_PROPERTY_PORTAL = 6;
    public static final String OPERATOR_COUNTRY_KEY = "WIFI_PRO_OPERATOR_COUNTRY_KEY";
    public static final String OVERSEA_BACKUP_SERVER = "http://www.bing.com";
    public static final String OVERSEA_BACKUP_YOUTUBE = "http://www.youtube.com";
    public static final String OVERSEA_MAIN_SERVER = "http://connectivitycheck.android.com/generate_204";
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String[] SPECIAL_REDIRECTED_PORTAL_LIST = new String[]{"\" Xi'an Airport-Free\"", "\"CIQ-Airport\""};
    public static final String TAG = "HwNetworkPropertyChecker";
    private static final int WAIT_TIMEOUT_MS = 6000;
    private boolean httpRespCodeRcvd = true;
    private String mBackupServer;
    private BroadcastReceiver mBroadcastReceiver;
    private CellLocation mCellLocation;
    private boolean mCheckerInitialized;
    private int mCheckingCounter;
    private ContentResolver mContentResolver;
    protected Context mContext;
    private boolean mControllerNotified;
    private WifiConfiguration mCurrentWifiConfig;
    private int mDnsCheckResult;
    private boolean mForcedNoRecomfirm;
    private boolean mForcedRecheck;
    private String mGatewayAddress;
    private Handler mHandler;
    private Object mHttpRespWaitingLock = new Object();
    private int mHttpResponseCode;
    protected boolean mIgnoreRxCounter;
    private boolean mInOversea;
    private IntentFilter mIntentFilter;
    private String mMainServer;
    private int mMaxAttempts;
    private boolean mMobileHotspot;
    private Network mNetwork;
    private NetworkAgentInfo mNetworkAgentInfo;
    private AtomicBoolean mNetworkDisconnected = new AtomicBoolean(true);
    private int mNetworkTypeByWebView;
    private int mRawHttpRespCode;
    private String mRawRedirectedHostName;
    private long mRequestTimestamp;
    private long mResponseTimestamp;
    protected Object mRxCounterWaitLock = new Object();
    protected int mTcpRxCounter;
    protected int mTcpRxFirstCounter;
    protected int mTcpRxSecondCounter;
    private TelephonyManager mTelManager;
    private BroadcastReceiver mTempReceiverWebView;
    private boolean mUseOverseaBakcup;
    private String mUsedServer = null;
    private Object mWebViewWaitingLock = new Object();
    protected WifiManager mWifiManager;
    private boolean rxCounterRespRcvd;
    private volatile boolean webviewRespRcvd = false;

    private class NetworkCheckerThread extends Thread {
        public static final String SERVER_BAIDU = "baidu";
        public static final String SERVER_BING = "bing";
        public static final String SERVER_YOUKU = "youku";
        public static final String SERVER_YOUTUBE = "youtube";
        private final String[] INVALID_REDIRECTED_HOST_1 = new String[]{"yuzua", "huayaochou", "freewifi.360.cn", "free.wifi.360.cn"};
        private final String[] INVALID_REDIRECTED_HOST_2 = new String[]{"miwifi"};
        private String gateWayAddr;
        private boolean isMainServer;
        private String mChckingServer;
        private boolean reconfirm;
        private HttpURLConnection urlConnection;

        public NetworkCheckerThread(String server, boolean mainServer, boolean reconfirm, String gw) {
            this.mChckingServer = server;
            this.isMainServer = mainServer;
            this.urlConnection = null;
            this.reconfirm = reconfirm;
            this.gateWayAddr = gw;
        }

        public void run() {
            int httpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
            if (HwNetworkPropertyChecker.this.mNetwork == null) {
                HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, mNetwork == null");
                return;
            }
            try {
                URLConnection connection = HwNetworkPropertyChecker.this.mNetwork.openConnection(new URL(this.mChckingServer));
                if (connection instanceof HttpURLConnection) {
                    this.urlConnection = (HttpURLConnection) connection;
                    this.urlConnection.setInstanceFollowRedirects(false);
                    this.urlConnection.setConnectTimeout(10000);
                    this.urlConnection.setReadTimeout(10000);
                    this.urlConnection.setUseCaches(false);
                    if (this.isMainServer) {
                        HwNetworkPropertyChecker.this.mRequestTimestamp = SystemClock.elapsedRealtime();
                    }
                    this.urlConnection.getInputStream();
                    if (this.isMainServer) {
                        HwNetworkPropertyChecker.this.mResponseTimestamp = SystemClock.elapsedRealtime();
                    }
                    httpResponseCode = recomfirmResponseCode(this.urlConnection.getResponseCode(), this.mChckingServer);
                    if (this.mChckingServer.equals(HwNetworkPropertyChecker.OVERSEA_MAIN_SERVER) && httpResponseCode == 200 && this.urlConnection.getContentLength() == 0) {
                        HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, reset for clients3.google.com, respCode = " + httpResponseCode);
                        httpResponseCode = WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
                    }
                    if (this.mChckingServer.equals(HwNetworkPropertyChecker.OVERSEA_BACKUP_SERVER) && (this.isMainServer ^ 1) != 0) {
                        HwNetworkPropertyChecker.this.mUseOverseaBakcup = true;
                    }
                    if (this.urlConnection != null) {
                        this.urlConnection.disconnect();
                    }
                    if (!(HwNetworkPropertyChecker.this.mIgnoreRxCounter || httpResponseCode != WifiProCommonUtils.HTTP_UNREACHALBE || (HwNetworkPropertyChecker.this.mNetworkDisconnected.get() ^ 1) == 0 || HwNetworkPropertyChecker.this.mNetwork == null || (HwNetworkPropertyChecker.this.mInOversea ^ 1) == 0 || (!this.reconfirm && HwNetworkPropertyChecker.this.mCheckingCounter != HwNetworkPropertyChecker.this.mMaxAttempts))) {
                        try {
                            InetAddress baidu = HwNetworkPropertyChecker.this.mNetwork.getByName("www.baidu.com");
                            InetAddress youku = null;
                            if (!HwNetworkPropertyChecker.this.mNetworkDisconnected.get()) {
                                youku = HwNetworkPropertyChecker.this.mNetwork.getByName("www.youku.com");
                            }
                            if (!(baidu == null || youku == null)) {
                                if (baidu.getHostAddress() == null || !baidu.getHostAddress().equals(youku.getHostAddress())) {
                                    HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, DNS is ok on wlan device, but HTTP Connection failed!");
                                } else {
                                    HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, 1st/2nd svr hosts have the same ip address, network is exceptional.");
                                    HwNetworkPropertyChecker.this.mIgnoreRxCounter = true;
                                }
                            }
                        } catch (IOException e) {
                            HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, IOException e1, unable to parse dns.");
                            HwNetworkPropertyChecker.this.mIgnoreRxCounter = true;
                        } catch (SecurityException e2) {
                            HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, SecurityException se1");
                        }
                    }
                    HwNetworkPropertyChecker.this.mHttpResponseCode = httpResponseCode;
                    if (!HwNetworkPropertyChecker.this.mNetworkDisconnected.get()) {
                        HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, 103, 0, 0));
                    }
                    return;
                }
                HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, openConnection doesn't return HttpURLConnection instance.");
                if (this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
            } catch (IOException e3) {
                String msg = e3.getMessage();
                HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, IOException, mainServer = " + this.isMainServer + ", Oversea = " + HwNetworkPropertyChecker.this.mInOversea);
                if (msg == null || !msg.contains("ECONNREFUSED") || HwNetworkPropertyChecker.this.mCurrentWifiConfig == null || !WifiProCommonUtils.matchedRequestByHistory(HwNetworkPropertyChecker.this.mCurrentWifiConfig.internetHistory, 100)) {
                    if (msg != null) {
                        if (msg.contains("ECONNRESET") && HwNetworkPropertyChecker.this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(HwNetworkPropertyChecker.this.mCurrentWifiConfig.internetHistory, 100)) {
                            httpResponseCode = WifiProCommonUtils.getReachableCode(this.mChckingServer.equals(HwNetworkPropertyChecker.OVERSEA_MAIN_SERVER));
                        }
                    }
                    if (msg != null && (msg.contains("ENETUNREACH") || msg.contains("ENONET") || msg.contains("Unable to resolve host"))) {
                        HwNetworkPropertyChecker.this.mIgnoreRxCounter = true;
                    }
                } else {
                    httpResponseCode = WifiProCommonUtils.getReachableCode(this.mChckingServer.equals(HwNetworkPropertyChecker.OVERSEA_MAIN_SERVER));
                }
                if (msg != null && (msg.contains("ECONNRESET") || msg.contains("Connection reset"))) {
                    httpResponseCode = WifiProCommonUtils.RESP_CODE_CONN_RESET;
                } else if (msg != null) {
                    if (msg.contains("ECONNREFUSED") || msg.contains("unexpected end")) {
                        if (WifiProCommonUtils.isOpenType(HwNetworkPropertyChecker.this.mCurrentWifiConfig)) {
                            httpResponseCode = WifiProCommonUtils.RESP_CODE_UNSTABLE;
                        } else if (!(HwNetworkPropertyChecker.this.mCurrentWifiConfig == null || (WifiProCommonUtils.matchedRequestByHistory(HwNetworkPropertyChecker.this.mCurrentWifiConfig.internetHistory, 100) ^ 1) == 0)) {
                            httpResponseCode = WifiProCommonUtils.RESP_CODE_UNSTABLE;
                        }
                    }
                }
                if (this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
            } catch (SecurityException e4) {
                HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, SecurityException se");
                if (this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
            } catch (Throwable th) {
                if (this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
            }
        }

        private boolean isInvalidRedirectedHost(String host, String[] servers) {
            if (!(host == null || servers == null)) {
                for (CharSequence contains : servers) {
                    if (host.contains(contains)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private int recomfirmResponseCode(int respCode, String server) {
            if (WifiProCommonUtils.isRedirectedRespCode(respCode)) {
                String newLocation = this.urlConnection.getHeaderField("Location");
                String host = WifiProCommonUtils.parseHostByUrlLocation(newLocation);
                HwNetworkPropertyChecker.this.LOGW("recomfirmResponseCode, host = " + host + ", location = " + newLocation + ", gateway = " + this.gateWayAddr);
                if (!(host == null || newLocation == null || HwNetworkPropertyChecker.this.mCurrentWifiConfig == null)) {
                    if (!this.reconfirm) {
                        HwNetworkPropertyChecker.this.mRawRedirectedHostName = host;
                    }
                    if ((HwNetworkPropertyChecker.CHINA_MAINLAND_MAIN_SERVER.equals(server) && host.contains(SERVER_BAIDU)) || ((HwNetworkPropertyChecker.CHINA_MAINLAND_BACKUP_SERVER.equals(server) && host.contains(SERVER_YOUKU)) || ((HwNetworkPropertyChecker.OVERSEA_BACKUP_SERVER.equals(server) && host.contains(SERVER_BING)) || (HwNetworkPropertyChecker.OVERSEA_BACKUP_YOUTUBE.equals(server) && host.contains(SERVER_YOUTUBE))))) {
                        if (this.reconfirm && (host.equals(HwNetworkPropertyChecker.this.mRawRedirectedHostName) ^ 1) != 0) {
                            HwNetworkPropertyChecker.this.mForcedNoRecomfirm = true;
                        }
                        return 200;
                    } else if (!HwNetworkPropertyChecker.OVERSEA_MAIN_SERVER.equals(server) && isInvalidRedirectedHost(host, this.INVALID_REDIRECTED_HOST_1)) {
                        HwNetworkPropertyChecker.this.LOGW("recomfirmResponseCode, redirect to the host that has internet access, , host = " + host + ", 302 --> 200.");
                        return 200;
                    } else if (!HwNetworkPropertyChecker.OVERSEA_MAIN_SERVER.equals(server) && isInvalidRedirectedHost(host, this.INVALID_REDIRECTED_HOST_2)) {
                        HwNetworkPropertyChecker.this.LOGW("recomfirmResponseCode, redirect the host has no internet, need webview to confirm.");
                        return WifiProCommonUtils.RESP_CODE_ABNORMAL_SERVER;
                    } else if (WifiProCommonUtils.invalidUrlLocation(newLocation)) {
                        HwNetworkPropertyChecker.this.LOGW("recomfirmResponseCode, redirect the invalid url, need webview to confirm.");
                        return WifiProCommonUtils.RESP_CODE_INVALID_URL;
                    } else if (this.gateWayAddr != null && this.gateWayAddr.length() > 0 && host.contains(this.gateWayAddr)) {
                        HwNetworkPropertyChecker.this.LOGW("recomfirmResponseCode, redirect to the gateway, maybe something wrong, need webview to confirm.");
                        return WifiProCommonUtils.RESP_CODE_GATEWAY;
                    }
                }
                if (!(HwNetworkPropertyChecker.this.mNetworkDisconnected.get() || (TextUtils.isEmpty(HwNetworkPropertyChecker.this.mRawRedirectedHostName) ^ 1) == 0 || (TextUtils.isEmpty(host) ^ 1) == 0 || (HwNetworkPropertyChecker.this.mRawRedirectedHostName.equals(host) ^ 1) == 0 || !WifiProCommonUtils.isOpenType(HwNetworkPropertyChecker.this.mCurrentWifiConfig))) {
                    return WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED;
                }
            }
            return respCode;
        }
    }

    public static class StarndardPortalInfo {
        public String currentSsid = "";
        public int lac = -1;
        public long timestamp = 0;
    }

    public HwNetworkPropertyChecker(Context context, WifiManager wifiManager, TelephonyManager telManager, boolean enabled, NetworkAgentInfo agent, boolean needRxBroadcast) {
        this.mContext = context;
        this.mWifiManager = wifiManager;
        this.mTelManager = telManager;
        this.mNetworkAgentInfo = agent;
        this.mNetwork = null;
        this.mCurrentWifiConfig = null;
        this.mGatewayAddress = null;
        this.mMainServer = CHINA_MAINLAND_MAIN_SERVER;
        this.mBackupServer = CHINA_MAINLAND_BACKUP_SERVER;
        this.mInOversea = false;
        this.mUseOverseaBakcup = false;
        this.mForcedRecheck = false;
        this.mCheckerInitialized = false;
        this.mHttpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
        this.mIgnoreRxCounter = false;
        this.mControllerNotified = false;
        this.mRequestTimestamp = 0;
        this.mResponseTimestamp = 0;
        this.mCheckingCounter = 0;
        this.mMaxAttempts = 3;
        this.mTcpRxCounter = 0;
        this.mTcpRxFirstCounter = 0;
        this.mTcpRxSecondCounter = 0;
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        this.mContentResolver = this.mContext.getContentResolver();
        init(needRxBroadcast);
    }

    private void init(boolean needRxBroadcast) {
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        if (needRxBroadcast) {
            this.mIntentFilter.addAction(WifiProCommonDefs.ACTION_RESPONSE_TCP_RX_COUNTER);
        }
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && DetailedState.DISCONNECTED == info.getDetailedState() && (HwNetworkPropertyChecker.this.mNetworkDisconnected.get() ^ 1) != 0) {
                        HwNetworkPropertyChecker.this.LOGD("NETWORK_STATE_CHANGED_ACTION, network is connected --> disconnected.");
                        HwNetworkPropertyChecker.this.mNetworkDisconnected.set(true);
                        HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, 102, 0, 0));
                    }
                } else if (WifiProCommonDefs.ACTION_RESPONSE_TCP_RX_COUNTER.equals(intent.getAction())) {
                    int rx = intent.getIntExtra(WifiProCommonDefs.EXTRA_FLAG_TCP_RX_COUNTER, 0);
                    HwNetworkPropertyChecker hwNetworkPropertyChecker = HwNetworkPropertyChecker.this;
                    if (rx <= 0) {
                        rx = 0;
                    }
                    hwNetworkPropertyChecker.mTcpRxCounter = rx;
                    synchronized (HwNetworkPropertyChecker.this.mRxCounterWaitLock) {
                        HwNetworkPropertyChecker.this.rxCounterRespRcvd = true;
                        HwNetworkPropertyChecker.this.mRxCounterWaitLock.notifyAll();
                    }
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, WifiProCommonDefs.NETWORK_CHECKER_RECV_PERMISSION, null);
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                Object -get3;
                switch (msg.what) {
                    case 101:
                        HwNetworkPropertyChecker.this.mHttpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
                        if (msg.arg1 == 1) {
                            HwNetworkPropertyChecker.this.mHttpResponseCode = 600;
                        }
                        -get3 = HwNetworkPropertyChecker.this.mHttpRespWaitingLock;
                        synchronized (-get3) {
                            HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                            break;
                        }
                    case 102:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(101)) {
                            HwNetworkPropertyChecker.this.LOGD("MSG_HTTP_RESP_TIMEOUT msg removed because of disconnected.");
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(101);
                        }
                        HwNetworkPropertyChecker.this.mHttpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
                        HwNetworkPropertyChecker.this.mRawHttpRespCode = WifiProCommonUtils.HTTP_UNREACHALBE;
                        HwNetworkPropertyChecker.this.mMobileHotspot = false;
                        HwNetworkPropertyChecker.this.mCheckerInitialized = false;
                        HwNetworkPropertyChecker.this.mRawRedirectedHostName = null;
                        HwNetworkPropertyChecker.this.mUsedServer = null;
                        -get3 = HwNetworkPropertyChecker.this.mHttpRespWaitingLock;
                        synchronized (-get3) {
                            HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                            break;
                        }
                    case 103:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(101)) {
                            HwNetworkPropertyChecker.this.LOGD("MSG_HTTP_RESP_TIMEOUT msg removed because of HTTP response received.");
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(101);
                        }
                        -get3 = HwNetworkPropertyChecker.this.mHttpRespWaitingLock;
                        synchronized (-get3) {
                            HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                            break;
                        }
                    case 104:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(101)) {
                            HwNetworkPropertyChecker.this.LOGD("MSG_HTTP_RESP_TIMEOUT msg removed because of DNS response received.");
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(101);
                        }
                        HwNetworkPropertyChecker.this.mDnsCheckResult = msg.arg1;
                        -get3 = HwNetworkPropertyChecker.this.mHttpRespWaitingLock;
                        synchronized (-get3) {
                            HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                            break;
                        }
                    default:
                        super.handleMessage(msg);
                }
                super.handleMessage(msg);
            }
        };
    }

    private void registerTempReceiverWebView() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiProCommonDefs.ACTION_RESP_WEBVIEW_CHECK);
        this.mTempReceiverWebView = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WifiProCommonDefs.ACTION_RESP_WEBVIEW_CHECK.equals(intent.getAction())) {
                    HwNetworkPropertyChecker.this.mNetworkTypeByWebView = intent.getIntExtra(WifiProCommonDefs.EXTRA_FLAG_NETWORK_TYPE, 100);
                    synchronized (HwNetworkPropertyChecker.this.mWebViewWaitingLock) {
                        HwNetworkPropertyChecker.this.webviewRespRcvd = true;
                        HwNetworkPropertyChecker.this.mWebViewWaitingLock.notifyAll();
                    }
                }
            }
        };
        this.mContext.registerReceiver(this.mTempReceiverWebView, intentFilter, WifiProCommonDefs.NETWORK_CHECKER_RECV_PERMISSION, null);
    }

    private void unregisterTempReceiverWebView() {
        if (this.mTempReceiverWebView != null) {
            this.mContext.unregisterReceiver(this.mTempReceiverWebView);
            this.mTempReceiverWebView = null;
        }
    }

    private void initNetworkInfo() {
        if (this.mWifiManager != null) {
            DhcpInfo dhcpInfo = this.mWifiManager.getDhcpInfo();
            if (dhcpInfo != null && dhcpInfo.gateway != 0) {
                this.mGatewayAddress = NetworkUtils.intToInetAddress(dhcpInfo.gateway).getHostAddress();
            }
        }
    }

    private void initCurrWifiConfig() {
        if (this.mWifiManager != null) {
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configNetworks != null && wifiInfo != null) {
                for (int i = 0; i < configNetworks.size(); i++) {
                    WifiConfiguration config = (WifiConfiguration) configNetworks.get(i);
                    if (config.networkId == wifiInfo.getNetworkId()) {
                        this.mCurrentWifiConfig = config;
                        LOGD("initialize, current rssi = " + wifiInfo.getRssi() + ", network = " + config.configKey(true));
                        this.mNetworkDisconnected.set(false);
                        return;
                    }
                }
            }
        }
    }

    private void initialize(boolean reconfirm) {
        if (!this.mCheckerInitialized) {
            if (this.mWifiManager == null) {
                this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            }
            if (this.mTelManager == null) {
                this.mTelManager = (TelephonyManager) this.mContext.getSystemService("phone");
            }
            if (!(this.mNetworkAgentInfo == null || this.mNetworkAgentInfo.network == null)) {
                this.mNetwork = this.mNetworkAgentInfo.network;
            }
            if (this.mNetwork == null) {
                this.mNetwork = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
            }
            initNetworkInfo();
            initCurrWifiConfig();
            String operator = this.mTelManager.getNetworkOperator();
            this.mForcedRecheck = false;
            if (operator == null || operator.length() == 0) {
                this.mMainServer = reconfirm ? OVERSEA_BACKUP_SERVER : OVERSEA_MAIN_SERVER;
                this.mBackupServer = OVERSEA_BACKUP_SERVER;
                this.mInOversea = true;
                this.mForcedRecheck = true;
            } else if (!operator.startsWith(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                this.mMainServer = OVERSEA_MAIN_SERVER;
                this.mBackupServer = OVERSEA_BACKUP_SERVER;
                this.mInOversea = true;
            }
            this.mMobileHotspot = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContext);
            this.mCheckerInitialized = true;
            if (this.mCurrentWifiConfig != null) {
                LOGD("initialize, AP's network history = " + this.mCurrentWifiConfig.internetHistory);
            }
        }
        if (reconfirm) {
            this.mNetwork = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
            initNetworkInfo();
            initCurrWifiConfig();
        }
        this.mIgnoreRxCounter = false;
        if (!reconfirm && this.mCheckingCounter == 0) {
            this.mTcpRxFirstCounter = requestTcpRxPacketsCounter();
        }
    }

    private void sendCheckResultWhenConnected(String action, String flag, int property) {
        Intent intent = new Intent(action);
        intent.setFlags(67108864);
        intent.putExtra(flag, property);
        if (WifiProCommonDefs.EXTRA_FLAG_NETWORK_PROPERTY.equals(flag) && property == 6) {
            if (!TextUtils.isEmpty(this.mRawRedirectedHostName)) {
                intent.putExtra(WifiProCommonDefs.EXTRA_RAW_REDIRECTED_HOST, this.mRawRedirectedHostName);
            }
            boolean standardPortal = false;
            if (OVERSEA_MAIN_SERVER.equals(this.mUsedServer) || (WifiProCommonUtils.httpReachableHome(this.mRawHttpRespCode) ^ 1) != 0) {
                standardPortal = true;
            }
            intent.putExtra(WifiProCommonDefs.EXTRA_STANDARD_PORTAL_NETWORK, standardPortal);
        }
        this.mContext.sendBroadcast(intent, WifiProCommonDefs.NETWORK_CHECKER_RECV_PERMISSION);
    }

    public boolean isNetworkChanged() {
        WifiConfiguration currConfig = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        if (!(currConfig == null || this.mCurrentWifiConfig == null || (this.mNetworkDisconnected.get() ^ 1) == 0)) {
            LOGW("isNetworkChanged, current ssid = " + currConfig.configKey() + ", checking ssid = " + this.mCurrentWifiConfig.configKey());
            if (this.mCurrentWifiConfig.configKey() != null && this.mCurrentWifiConfig.configKey().equals(currConfig.configKey())) {
                return false;
            }
        }
        return true;
    }

    public boolean isCheckingCompleted(int finalRespCode) {
        boolean ret = false;
        int property;
        if (WifiProCommonUtils.httpReachableOrRedirected(finalRespCode)) {
            this.mCheckingCounter = 0;
            ret = true;
            if (!(this.mControllerNotified || (isNetworkChanged() ^ 1) == 0)) {
                this.mControllerNotified = true;
                property = finalRespCode == WifiProCommonUtils.HTTP_REACHALBE_GOOLE ? 5 : 6;
                sendCheckResultWhenConnected(WifiProCommonDefs.ACTION_NETWOR_PROPERTY_NOTIFICATION, WifiProCommonDefs.EXTRA_FLAG_NETWORK_PROPERTY, property);
                if (property == 5 && this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, 102)) {
                    property = 6;
                }
                sendCheckResultWhenConnected(WifiProCommonDefs.ACTION_NETWORK_CONDITIONS_MEASURED, WifiProCommonDefs.EXTRA_IS_INTERNET_READY, property);
            }
        } else if (this.mCheckingCounter >= this.mMaxAttempts) {
            this.mCheckingCounter = 0;
            ret = true;
            if (!(this.mControllerNotified || (isNetworkChanged() ^ 1) == 0)) {
                this.mControllerNotified = true;
                property = -1;
                if (this.mCurrentWifiConfig != null) {
                    property = (!WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, 100) || (isNetworkPoorRssi() ^ 1) == 0 || (WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig) ^ 1) == 0) ? -1 : NETWORK_PROPERTY_PENDING;
                }
                sendCheckResultWhenConnected(WifiProCommonDefs.ACTION_NETWOR_PROPERTY_NOTIFICATION, WifiProCommonDefs.EXTRA_FLAG_NETWORK_PROPERTY, property);
                sendCheckResultWhenConnected(WifiProCommonDefs.ACTION_NETWORK_CONDITIONS_MEASURED, WifiProCommonDefs.EXTRA_IS_INTERNET_READY, -1);
            }
        }
        return ret;
    }

    private int syncCheckNetworkProperty(String server, boolean mainServer, boolean reconfirm) {
        this.mHttpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
        if (!this.mNetworkDisconnected.get()) {
            NetworkCheckerThread networkCheckerThread = new NetworkCheckerThread(server, mainServer, reconfirm, this.mGatewayAddress);
            this.httpRespCodeRcvd = false;
            int timeout = mainServer ? 12000 : WAIT_TIMEOUT_MS;
            synchronized (this.mHttpRespWaitingLock) {
                try {
                    int i;
                    networkCheckerThread.start();
                    Handler handler = this.mHandler;
                    Handler handler2 = this.mHandler;
                    if (reconfirm) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    handler.sendMessageDelayed(Message.obtain(handler2, 101, i, 0), (long) timeout);
                    while (!this.httpRespCodeRcvd) {
                        this.mHttpRespWaitingLock.wait();
                    }
                    LOGD("syncCheckNetworkProperty, Thread exited or timeout, http resp code = " + this.mHttpResponseCode);
                } catch (InterruptedException e) {
                }
            }
        }
        this.mRawHttpRespCode = this.mHttpResponseCode;
        return this.mHttpResponseCode;
    }

    public int requestTcpRxPacketsCounter() {
        int rxCounter = 0;
        if (WifiProCommonUtils.isWifiProPropertyEnabled()) {
            Intent intent = new Intent(WifiProCommonDefs.ACTION_REQUEST_TCP_RX_COUNTER);
            intent.setFlags(67108864);
            synchronized (this.mRxCounterWaitLock) {
                try {
                    this.rxCounterRespRcvd = false;
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    while (!this.rxCounterRespRcvd) {
                        this.mRxCounterWaitLock.wait();
                    }
                    if (this.mTcpRxCounter > 0) {
                        rxCounter = this.mTcpRxCounter;
                    }
                } catch (InterruptedException e) {
                }
            }
            this.mTcpRxCounter = 0;
            return rxCounter;
        }
        LOGD("requestTcpRxPacketsCounter: Product don't support wifi+, skip Tcp Rx Packets Counter, rx = 0");
        return 0;
    }

    public int recomfirmByRxCounter(int httpResponseCode, boolean reconfirm) {
        boolean tcpRxUsable;
        if (reconfirm || !WifiProCommonUtils.unreachableRespCode(httpResponseCode) || this.mCheckingCounter != this.mMaxAttempts || (this.mNetworkDisconnected.get() ^ 1) == 0 || (this.mIgnoreRxCounter ^ 1) == 0) {
            tcpRxUsable = false;
        } else {
            tcpRxUsable = isNetworkPoorRssi();
        }
        if (tcpRxUsable) {
            this.mTcpRxSecondCounter = requestTcpRxPacketsCounter();
            LOGD("recomfirmByRxCounter, firstRx = " + this.mTcpRxFirstCounter + ", secondRx = " + this.mTcpRxSecondCounter);
            if (this.mTcpRxSecondCounter - this.mTcpRxFirstCounter >= 3 && this.mTcpRxFirstCounter >= 0) {
                httpResponseCode = WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
                if (this.mCurrentWifiConfig != null) {
                    if (WifiProCommonUtils.isOpenAndPortal(this.mCurrentWifiConfig)) {
                        httpResponseCode = 302;
                    } else {
                        boolean portalMatched = PortalDataBaseManager.getInstance(this.mContext).syncQueryPortalNetwork(this.mCurrentWifiConfig.SSID);
                        httpResponseCode = portalMatched ? 302 : WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
                        LOGD("recomfirmByRxCounter, query portal database, portalMatched = " + portalMatched + ", ssid = " + this.mCurrentWifiConfig.SSID);
                    }
                }
                LOGD("recomfirmByRxCounter, ResponseCode 599 --> " + httpResponseCode);
            }
            this.mTcpRxFirstCounter = 0;
            this.mTcpRxSecondCounter = 0;
        }
        return httpResponseCode;
    }

    public int isCaptivePortal(boolean reconfirm) {
        return isCaptivePortal(reconfirm, false, false);
    }

    public int isCaptivePortal(boolean reconfirm, boolean portalNetwork, boolean wifiBackground) {
        LOGD("=====ENTER: ===isCaptivePortal, reconfirm = " + reconfirm + ", counter = " + this.mCheckingCounter);
        int httpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
        initialize(reconfirm);
        this.mCheckingCounter++;
        this.mForcedNoRecomfirm = false;
        if (!this.mNetworkDisconnected.get()) {
            this.mUsedServer = this.mMainServer;
            if (!portalNetwork || (isStandarPortal() ^ 1) == 0) {
                httpResponseCode = syncCheckNetworkProperty(this.mMainServer, true, reconfirm);
                updateStarndardPortalRecord(httpResponseCode, reconfirm);
            } else {
                httpResponseCode = 200;
                if (wifiBackground || !isNetworkReachableByICMP()) {
                    LOGD("Non-Standart portal ICMP unreachable! check with webview and database.");
                } else {
                    updateWifiConfigHistory(WifiProCommonUtils.HTTP_REACHALBE_GOOLE, reconfirm);
                    LOGD("=====LEAVE: ===isCaptivePortal, Non-Standart Portal ping reachable!");
                    this.mUseOverseaBakcup = false;
                    return WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
                }
            }
            httpResponseCode = transformResponseCode(recomfirmByRxCounter(recomfirmResponseCode(httpResponseCode, reconfirm), reconfirm));
            updateWifiConfigHistory(httpResponseCode, reconfirm);
        }
        LOGD("=====LEAVE: ===isCaptivePortal, httpResponseCode = " + httpResponseCode);
        this.mUseOverseaBakcup = false;
        return httpResponseCode;
    }

    private boolean isNetworkReachableByICMP() {
        return WifiProCommonUtils.isNetworkReachableByICMP(!this.mInOversea ? "www.baidu.com" : "www.bing.com", 12000);
    }

    private int getCurrentLac() {
        if (this.mTelManager == null) {
            this.mTelManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        this.mCellLocation = this.mTelManager.getCellLocation();
        int simstatus = this.mTelManager.getSimState();
        if (this.mCellLocation == null || simstatus == 1) {
            return -1;
        }
        int lac = -1;
        if (this.mCellLocation instanceof GsmCellLocation) {
            lac = ((GsmCellLocation) this.mCellLocation).getLac();
        } else if (this.mCellLocation instanceof CdmaCellLocation) {
            lac = ((CdmaCellLocation) this.mCellLocation).getNetworkId();
        } else {
            LOGD("CellLocation PhoneType Unknown.");
        }
        return lac;
    }

    private void updateStarndardPortalRecord(int respCode, boolean reconfirm) {
        if (!reconfirm && (this.mInOversea ^ 1) != 0 && WifiProCommonUtils.isRedirectedRespCode(respCode) && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig)) {
            PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
            if (database != null && this.mCurrentWifiConfig != null) {
                StarndardPortalInfo portalInfo = new StarndardPortalInfo();
                portalInfo.currentSsid = this.mCurrentWifiConfig.SSID;
                portalInfo.lac = getCurrentLac();
                portalInfo.timestamp = System.currentTimeMillis();
                LOGD("updateStarndardPortalRecord, ssid = " + this.mCurrentWifiConfig.SSID);
                database.updateStandardPortalTable(portalInfo);
            }
        }
    }

    private boolean isStandarPortal() {
        PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
        if (!(this.mInOversea || database == null || this.mCurrentWifiConfig == null)) {
            StarndardPortalInfo portalInfo = new StarndardPortalInfo();
            portalInfo.currentSsid = this.mCurrentWifiConfig.SSID;
            database.syncQueryStarndardPortalNetwork(portalInfo);
            long timestamp = portalInfo.timestamp;
            if (timestamp > 0 && System.currentTimeMillis() - timestamp <= 14400000) {
                LOGD("isStandarPortal return true");
                return true;
            }
        }
        LOGD("isStandarPortal return false");
        return false;
    }

    private boolean isStarndardPortalAuthenCompleted(int respCode) {
        PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
        if (!(this.mInOversea || database == null || this.mCurrentWifiConfig == null || !WifiProCommonUtils.httpReachableHome(respCode))) {
            LOGD("isStarndardPortalAuthenCompleted, verify authen infomathion.");
            int curlac = getCurrentLac();
            StarndardPortalInfo portalInfo = new StarndardPortalInfo();
            portalInfo.currentSsid = this.mCurrentWifiConfig.SSID;
            database.syncQueryStarndardPortalNetwork(portalInfo);
            long timestamp = portalInfo.timestamp;
            int lastlac = portalInfo.lac;
            if ((timestamp > 0 && System.currentTimeMillis() - timestamp <= 14400000) || (lastlac != -1 && lastlac == curlac)) {
                LOGD("portal HTTP response --> 200, this' s starndard portal and authen completed.");
                return true;
            }
        }
        return false;
    }

    public int recheckWithBakcupServer(boolean reconfirm) {
        LOGD("=====ENTER: ===recheckWithBakcupServer, counter = " + this.mCheckingCounter);
        int httpResponseCode = transformResponseCode(syncCheckNetworkProperty(this.mBackupServer, false, reconfirm));
        LOGD("=====LEAVE: ===recheckWithBakcupServer, httpResponseCode = " + httpResponseCode);
        this.mUseOverseaBakcup = false;
        return httpResponseCode;
    }

    private int transformResponseCode(int respCode) {
        int httpResponseCode = respCode;
        if (OVERSEA_MAIN_SERVER.equals(this.mUsedServer)) {
            if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(respCode)) {
                return 302;
            }
            return httpResponseCode;
        } else if (!WifiProCommonUtils.httpReachableOrRedirected(respCode) || (WifiProCommonUtils.isRedirectedRespCode(respCode) ^ 1) == 0) {
            return httpResponseCode;
        } else {
            return WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
        }
    }

    public int syncRecheckBasedOnWebView(boolean oversea) {
        LOGD("syncRecheckBasedOnWebView:: begin, oversea = " + oversea);
        if (WifiProCommonUtils.isWifiProPropertyEnabled()) {
            registerTempReceiverWebView();
            Intent intent = new Intent(WifiProCommonDefs.ACTION_REQUEST_WEBVIEW_CHECK);
            intent.putExtra(WifiProCommonDefs.EXTRA_FLAG_OVERSEA, oversea);
            intent.setFlags(67108864);
            this.webviewRespRcvd = false;
            synchronized (this.mWebViewWaitingLock) {
                try {
                    this.mNetworkTypeByWebView = 100;
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    while (!this.webviewRespRcvd) {
                        this.mWebViewWaitingLock.wait();
                    }
                    LOGD("syncRecheckBasedOnWebView::end, network type = " + this.mNetworkTypeByWebView);
                    unregisterTempReceiverWebView();
                } catch (InterruptedException e) {
                }
            }
            return this.mNetworkTypeByWebView;
        }
        LOGD("syncRecheckBasedOnWebView:: Product don't support wifi+, skip webview check, network type = 100!");
        return 100;
    }

    public int syncCheckDnsResponse() {
        this.httpRespCodeRcvd = false;
        this.mDnsCheckResult = -1;
        synchronized (this.mHttpRespWaitingLock) {
            try {
                new Thread(new Runnable() {
                    public void run() {
                        HwNetworkPropertyChecker.this.mNetwork = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
                        try {
                            if (HwNetworkPropertyChecker.this.mNetwork != null) {
                                HwNetworkPropertyChecker.this.mNetwork.getByName("www.bing.com");
                                HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, 104, 0, 0));
                            }
                        } catch (UnknownHostException e) {
                            HwNetworkPropertyChecker.this.LOGD("syncCheckDnsResponse, Unabled to resolve this host.");
                            HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, 104, -1, 0));
                        }
                    }
                }).start();
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 101, 0, 0), 6000);
                while (!this.httpRespCodeRcvd) {
                    this.mHttpRespWaitingLock.wait();
                }
            } catch (InterruptedException e) {
                LOGD("syncCheckDnsResponse Interrupted ");
            }
        }
        return this.mDnsCheckResult;
    }

    private int recomfirmResponseCode(int respCode, boolean reconfirm) {
        int newRespCode = respCode;
        boolean repairOk = false;
        if (this.mCurrentWifiConfig == null || this.mNetworkDisconnected.get() || (reconfirm && respCode == 600)) {
            return respCode;
        }
        boolean useBackupSvr = false;
        if (this.mForcedRecheck && WifiProCommonUtils.unreachableRespCode(respCode)) {
            this.mUsedServer = this.mBackupServer;
            newRespCode = syncCheckNetworkProperty(this.mBackupServer, false, reconfirm);
            if (WifiProCommonUtils.httpReachableOrRedirected(newRespCode)) {
                repairOk = true;
                if (newRespCode == 200) {
                    newRespCode = WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
                }
                useBackupSvr = true;
            }
        }
        if (respCode == WifiProCommonUtils.RESP_CODE_CONN_RESET) {
            this.mUseOverseaBakcup = true;
            respCode = recheckForConnectionReset(respCode, reconfirm);
            if (WifiProCommonUtils.unreachableRespCode(respCode)) {
                int type = syncRecheckBasedOnWebView(this.mInOversea);
                if (type == 101 && (this.mNetworkDisconnected.get() ^ 1) != 0) {
                    LOGW("recomfirmRespCode, CONN_RESET, network hasInternet based on the WebView");
                    respCode = 200;
                } else if (type == 102 && (this.mNetworkDisconnected.get() ^ 1) != 0) {
                    LOGW("recomfirmRespCode, CONN_RESET,  network is portal based on the WebView");
                    respCode = 302;
                }
            }
        }
        int recheckRespCode;
        if ((!this.mInOversea || useBackupSvr) && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig) && WifiProCommonUtils.isInMonitorList(this.mCurrentWifiConfig.SSID, SPECIAL_REDIRECTED_PORTAL_LIST)) {
            respCode = 302;
            if (syncRecheckBasedOnWebView(this.mInOversea) == 101 && (this.mNetworkDisconnected.get() ^ 1) != 0) {
                LOGW("recomfirmRespCode, SPECIAL_PORTAL, connected network type is hasInternet based on the WebView loading results.");
                repairOk = true;
                newRespCode = 200;
            }
        } else if (respCode == WifiProCommonUtils.RESP_CODE_UNSTABLE || ((respCode == WifiProCommonUtils.RESP_CODE_GATEWAY && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig)) || respCode == WifiProCommonUtils.RESP_CODE_INVALID_URL)) {
            newRespCode = recheckForAbnormalStatus(respCode, reconfirm);
            if (newRespCode != WifiProCommonUtils.HTTP_UNREACHALBE) {
                repairOk = true;
            }
            respCode = WifiProCommonUtils.HTTP_UNREACHALBE;
            this.mIgnoreRxCounter = true;
        } else if (respCode == WifiProCommonUtils.RESP_CODE_ABNORMAL_SERVER) {
            newRespCode = recheckForAbnormalRouterServer(respCode, reconfirm);
            if (newRespCode != WifiProCommonUtils.HTTP_UNREACHALBE) {
                repairOk = true;
            }
            respCode = WifiProCommonUtils.HTTP_UNREACHALBE;
            this.mIgnoreRxCounter = true;
        } else if (respCode == WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED) {
            respCode = recheckForRedirectedHostChanged();
        } else if (WifiProCommonUtils.isRedirectedRespCode(respCode) && this.mMobileHotspot) {
            recheckRespCode = recheckForHotspot302Type();
            if (recheckRespCode != respCode) {
                repairOk = true;
                newRespCode = recheckRespCode;
            }
        } else if (!WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig) && (WifiProCommonUtils.isRedirectedRespCode(respCode) || respCode == WifiProCommonUtils.RESP_CODE_GATEWAY)) {
            recheckRespCode = recheckForNonOpen302Type(respCode);
            if (recheckRespCode != respCode) {
                repairOk = true;
                newRespCode = recheckRespCode;
            }
        } else if ((!this.mInOversea || useBackupSvr) && WifiProCommonUtils.httpReachableHome(newRespCode) && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig) && (this.mForcedNoRecomfirm ^ 1) != 0 && (this.mMobileHotspot ^ 1) != 0 && (isStarndardPortalAuthenCompleted(newRespCode) ^ 1) != 0) {
            recheckRespCode = recheckForOpen200Type(newRespCode, reconfirm);
            if (recheckRespCode != newRespCode) {
                repairOk = true;
                newRespCode = recheckRespCode;
            }
        } else if (!reconfirm && this.mCheckingCounter == this.mMaxAttempts && WifiProCommonUtils.unreachableRespCode(respCode) && WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, 100)) {
            LOGD("for last time, network has internet ever, use the backup server to recheck it.");
            this.mUsedServer = this.mBackupServer;
            newRespCode = syncCheckNetworkProperty(this.mBackupServer, false, reconfirm);
            repairOk = WifiProCommonUtils.httpReachableHome(newRespCode);
        }
        if (!repairOk) {
            newRespCode = respCode;
        }
        return newRespCode;
    }

    private int recheckForRedirectedHostChanged() {
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForRedirectedHostChanged, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type != 101) {
            return 302;
        } else {
            LOGW("recheckForRedirectedHostChanged, connected network type is hasInternet based on the WebView loading results.");
            return WifiProCommonUtils.getReachableCode(this.mInOversea ? this.mUseOverseaBakcup ^ 1 : false);
        }
    }

    private int recheckForConnectionReset(int respCode, boolean reconfirm) {
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForConnectionReset, network is disconnected.");
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
        String server1 = this.mInOversea ? OVERSEA_BACKUP_SERVER : CHINA_MAINLAND_BACKUP_SERVER;
        String server2 = this.mInOversea ? OVERSEA_BACKUP_YOUTUBE : OVERSEA_BACKUP_SERVER;
        this.mUsedServer = server1;
        int tmpRespCode = syncCheckNetworkProperty(server1, false, reconfirm);
        LOGD("recheckForConnectionReset, use 2nd server, respCode = " + tmpRespCode);
        if (tmpRespCode == WifiProCommonUtils.RESP_CODE_CONN_RESET) {
            this.mUsedServer = server2;
            tmpRespCode = syncCheckNetworkProperty(server2, false, reconfirm);
            LOGD("recheckForConnectionReset, use 3rd server, respCode = " + tmpRespCode);
            if (tmpRespCode == WifiProCommonUtils.RESP_CODE_CONN_RESET || tmpRespCode == WifiProCommonUtils.HTTP_UNREACHALBE || tmpRespCode == 600) {
                return WifiProCommonUtils.HTTP_UNREACHALBE;
            }
            if (WifiProCommonUtils.isRedirectedRespCode(tmpRespCode)) {
                return 302;
            }
            if (WifiProCommonUtils.httpReachableHome(tmpRespCode)) {
                return 200;
            }
        } else if (tmpRespCode == WifiProCommonUtils.HTTP_UNREACHALBE || tmpRespCode == 600) {
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else {
            if (WifiProCommonUtils.isRedirectedRespCode(tmpRespCode)) {
                return 302;
            }
            if (WifiProCommonUtils.httpReachableHome(tmpRespCode)) {
                return 200;
            }
        }
        return WifiProCommonUtils.HTTP_UNREACHALBE;
    }

    private int recheckForAbnormalStatus(int respCode, boolean reconfirm) {
        boolean z = false;
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForAbnormalStatus, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type == 102 && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig)) {
            LOGW("recheckForAbnormalStatus, SPECIAL_STATUS, connected network type is portal based on the WebView loading results.");
            return 302;
        } else if (type == 101) {
            LOGW("recheckForAbnormalStatus, SPECIAL_STATUS, connected network type is hasInternet based on the WebView loading results.");
            if (this.mInOversea) {
                z = this.mUseOverseaBakcup ^ 1;
            }
            return WifiProCommonUtils.getReachableCode(z);
        } else {
            if (type == 100) {
                int tmpRespCode = syncCheckNetworkProperty(OVERSEA_MAIN_SERVER, false, reconfirm);
                LOGW("recheckForAbnormalStatus, SPECIAL_STATUS, using google's server, tmpRespCode = " + tmpRespCode);
                if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(tmpRespCode)) {
                    return 302;
                }
                if (!this.mNetworkDisconnected.get() && WifiProCommonUtils.httpUnreachableOrAbnormal(tmpRespCode) && PortalDataBaseManager.getInstance(this.mContext).syncQueryPortalNetwork(this.mCurrentWifiConfig.SSID)) {
                    LOGD("recheckForAbnormalStatus, SPECIAL_STATUS, syncQueryPortalNetwork, the query result is true.");
                    return 302;
                }
            }
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
    }

    private int recheckForAbnormalRouterServer(int respCode, boolean reconfirm) {
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForAbnormalRouterServer, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type == 102 || type == 100) {
            LOGW("recheckForAbnormalRouterServer, connected network is 599 based on the WebView loading results.");
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type != 101) {
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else {
            LOGW("recheckForAbnormalRouterServer, connected network type is hasInternet based on the WebView");
            return WifiProCommonUtils.getReachableCode(this.mInOversea ? this.mUseOverseaBakcup ^ 1 : false);
        }
    }

    private int recheckForOpen200Type(int lastRespCode, boolean reconfirm) {
        int newRespCode = lastRespCode;
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForOpen200Type, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
        boolean isRedirectedToServer = false;
        if (!TextUtils.isEmpty(this.mRawRedirectedHostName) && (this.mRawRedirectedHostName.contains(NetworkCheckerThread.SERVER_BAIDU) || this.mRawRedirectedHostName.contains(NetworkCheckerThread.SERVER_YOUKU) || this.mRawRedirectedHostName.contains(NetworkCheckerThread.SERVER_BING) || this.mRawRedirectedHostName.contains(NetworkCheckerThread.SERVER_YOUTUBE))) {
            isRedirectedToServer = true;
        }
        if (type == 102) {
            LOGW("recheckForOpen200Type, connected network type is portal based on the WebView loading results.");
            newRespCode = 302;
        } else if (type == 100 && (isRedirectedToServer ^ 1) != 0) {
            int tmpRespCode = syncCheckNetworkProperty(OVERSEA_MAIN_SERVER, false, reconfirm);
            LOGW("recheckForOpen200Type, using google's server, tmpRespCode = " + tmpRespCode + ", current ssid = " + this.mCurrentWifiConfig.SSID);
            if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(tmpRespCode)) {
                newRespCode = 302;
            } else if (!this.mNetworkDisconnected.get() && WifiProCommonUtils.httpUnreachableOrAbnormal(tmpRespCode) && PortalDataBaseManager.getInstance(this.mContext).syncQueryPortalNetwork(this.mCurrentWifiConfig.SSID)) {
                LOGD("recheckForOpen200Type, syncQueryPortalNetwork, database is matched, it's known portal.");
                newRespCode = 302;
            }
        }
        return newRespCode;
    }

    private int recheckForHotspot302Type() {
        int newRespCode = WifiProCommonUtils.HTTP_UNREACHALBE;
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGD("recheckForHotspot302Type, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
        if (type == 101) {
            LOGD("recheckForHotspot302Type, hotspot has internet access when load webpage.");
            newRespCode = 200;
        } else if (type == 100) {
            LOGD("recheckForHotspot302Type, hotspot has no internet access when load webpage.");
            newRespCode = WifiProCommonUtils.HTTP_UNREACHALBE;
        }
        return newRespCode;
    }

    private int recheckForNonOpen302Type(int lastRespCode) {
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForNonOpen302Type, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type == 101) {
            LOGW("recheckForNonOpen302Type, 30x/GW from NON-OPEN WiFi, has internet based on the WebView.");
            return 200;
        } else if (type == 102 && lastRespCode == WifiProCommonUtils.RESP_CODE_GATEWAY) {
            LOGW("recheckForNonOpen302Type, GW from NON-OPEN WiFi, PORTAL on the WebView --> tobe NO INTERNET");
            this.mIgnoreRxCounter = true;
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type != 100) {
            return lastRespCode;
        } else {
            LOGW("recheckForNonOpen302Type, 30x/GW from NON-OPEN WiFi, UNKNOWN on the WebView --> tobe NO INTERNET");
            this.mIgnoreRxCounter = true;
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
    }

    private void updateWifiConfigHistory(int respCode, boolean reconfirm) {
        boolean z = false;
        if (reconfirm && WifiProCommonUtils.httpReachableOrRedirected(respCode) && this.mCurrentWifiConfig != null) {
            String internetHistory = this.mCurrentWifiConfig.internetHistory;
            if (internetHistory == null || internetHistory.lastIndexOf("/") == -1) {
                LOGW("updateWifiConfigHistory, inputed arg is invalid, internetHistory = " + internetHistory);
                return;
            }
            String status = internetHistory.substring(0, 1);
            Intent intent;
            if (status != null && status.equals("0")) {
                int newStatus = respCode == WifiProCommonUtils.HTTP_REACHALBE_GOOLE ? 1 : 2;
                internetHistory = String.valueOf(newStatus) + "/" + internetHistory.substring(internetHistory.indexOf("/") + 1);
                this.mCurrentWifiConfig.noInternetAccess = false;
                this.mCurrentWifiConfig.validatedInternetAccess = this.mCurrentWifiConfig.noInternetAccess ^ 1;
                if (newStatus == 1) {
                    this.mCurrentWifiConfig.numNoInternetAccessReports = 0;
                    this.mCurrentWifiConfig.lastHasInternetTimestamp = System.currentTimeMillis();
                }
                WifiConfiguration wifiConfiguration = this.mCurrentWifiConfig;
                if (respCode != WifiProCommonUtils.HTTP_REACHALBE_GOOLE) {
                    z = true;
                }
                wifiConfiguration.portalNetwork = z;
                this.mCurrentWifiConfig.internetHistory = internetHistory;
                if (newStatus == 1) {
                    sendCheckResultWhenConnected(WifiProCommonDefs.ACTION_NETWORK_CONDITIONS_MEASURED, WifiProCommonDefs.EXTRA_IS_INTERNET_READY, 1);
                }
                intent = new Intent(WifiProCommonDefs.ACTION_UPDATE_CONFIG_HISTORY);
                intent.putExtra(WifiProCommonDefs.EXTRA_FLAG_NEW_WIFI_CONFIG, new WifiConfiguration(this.mCurrentWifiConfig));
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WifiProCommonDefs.NETWORK_CHECKER_RECV_PERMISSION);
            } else if (status != null && status.equals(PPPOEStateMachine.PHASE_SERIALCONN) && respCode == WifiProCommonUtils.HTTP_REACHALBE_GOOLE) {
                this.mCurrentWifiConfig.lastHasInternetTimestamp = System.currentTimeMillis();
                this.mCurrentWifiConfig.portalCheckStatus = 0;
                intent = new Intent(WifiProCommonDefs.ACTION_UPDATE_CONFIG_HISTORY);
                intent.putExtra(WifiProCommonDefs.EXTRA_FLAG_NEW_WIFI_CONFIG, new WifiConfiguration(this.mCurrentWifiConfig));
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WifiProCommonDefs.NETWORK_CHECKER_RECV_PERMISSION);
            }
        }
    }

    public boolean isNetworkPoorRssi() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getRssi() >= -75) {
            return false;
        }
        return true;
    }

    public long getReqTimestamp() {
        return this.mRequestTimestamp;
    }

    public long getRespTimestamp() {
        return this.mResponseTimestamp;
    }

    public int getRawHttpRespCode() {
        return this.mRawHttpRespCode;
    }

    public boolean isMobileHotspot() {
        return this.mMobileHotspot;
    }

    public void setRawRedirectedHostName(String hostName) {
        LOGW("setRawRedirectedHostName, hostName = " + hostName);
        this.mRawRedirectedHostName = hostName;
    }

    public void resetCheckerStatus() {
        this.mCheckingCounter = 0;
        this.mRequestTimestamp = 0;
        this.mResponseTimestamp = 0;
        this.mCheckerInitialized = false;
        this.mMainServer = CHINA_MAINLAND_MAIN_SERVER;
        this.mBackupServer = CHINA_MAINLAND_BACKUP_SERVER;
        this.mInOversea = false;
        this.mForcedRecheck = false;
        this.mControllerNotified = false;
        this.mIgnoreRxCounter = false;
        synchronized (this.mRxCounterWaitLock) {
            this.rxCounterRespRcvd = false;
        }
    }

    public void release() {
        LOGW("release(), network's disconnected, checking counter = " + this.mCheckingCounter);
        if (this.mBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
        }
        this.mGatewayAddress = null;
        this.mNetworkDisconnected.set(true);
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
