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
import android.net.wifi.wifipro.WifiProStatusUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.wifipro.PortalDataBaseManager;
import com.android.server.wifipro.WifiProCHRManager;
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
    public static final String ACTION_NETWORK_CONDITIONS_MEASURED = "huawei.conn.NETWORK_CONDITIONS_MEASURED";
    public static final String ACTION_NETWOR_PROPERTY_NOTIFICATION = "com.huawei.wifi.action.NETWOR_PROPERTY_NOTIFICATION";
    public static final String ACTION_REQUEST_TCP_RX_COUNTER = "com.huawei.wifi.action.ACTION_REQUEST_TCP_RX_COUNTER";
    public static final String ACTION_RESPONSE_TCP_RX_COUNTER = "com.huawei.wifi.action.ACTION_RESPONSE_TCP_RX_COUNTER";
    public static final String ACTION_UPDATE_CONFIG_HISTORY = "com.huawei.wifipro.ACTION_UPDATE_CONFIG_HISTORY";
    public static final String CHINA_MAINLAND_BACKUP_SERVER = "http://www.youku.com";
    public static final String CHINA_MAINLAND_MAIN_SERVER = "http://www.baidu.com";
    public static final String COUNTRY_CODE_CN = "460";
    public static final String EXTRA_FLAG_NETWOR_PROPERTY = "wifi_network_property";
    public static final String EXTRA_FLAG_NEW_WIFI_CONFIG = "new_wifi_config";
    public static final String EXTRA_FLAG_SPECIAL_PORTAL_STATUS = "special_portal_status";
    public static final String EXTRA_FLAG_TCP_RX_COUNTER = "wifipro_tcp_rx_counter";
    public static final String EXTRA_IS_INTERNET_READY = "extra_is_internet_ready";
    public static final int HW_DEFAULT_REEVALUATE_DELAY_MS = 2000;
    public static final int HW_MAX_RETRIES = 3;
    private static final int LAC_UNKNOWN = -1;
    public static final int MIN_RX_PKT_DELTA = 3;
    private static final int MSG_DNS_RESP_RCVD = 104;
    private static final int MSG_HTTP_RESP_RCVD = 103;
    private static final int MSG_HTTP_RESP_TIMEOUT = 101;
    private static final int MSG_NETWORK_DISCONNECTED = 102;
    public static final String NETWORK_CHECKER_RECV_PERMISSION = "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER";
    public static final int NETWORK_PROPERTY_INTERNET = 5;
    public static final int NETWORK_PROPERTY_NO_INTERNET = -1;
    public static final int NETWORK_PROPERTY_PENDING = -101;
    public static final int NETWORK_PROPERTY_PORTAL = 6;
    public static final String OPERATOR_COUNTRY_KEY = "WIFI_PRO_OPERATOR_COUNTRY_KEY";
    public static final String OVERSEA_BACKUP_SERVER = "http://www.bing.com";
    public static final String OVERSEA_BACKUP_YOUTUBE = "http://www.youtube.com";
    public static final String OVERSEA_MAIN_SERVER = "http://connectivitycheck.android.com/generate_204";
    public static final int POOR_RSSI_LEVEL = -75;
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String[] SPECIAL_REDIRECTED_PORTAL_LIST = null;
    public static final String TAG = "HwNetworkPropertyChecker";
    private static final int WAIT_TIMEOUT_MS = 6000;
    private boolean httpRespCodeRcvd;
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
    private Object mHttpRespWaitingLock;
    private int mHttpResponseCode;
    protected boolean mIgnoreRxCounter;
    private boolean mInOversea;
    private IntentFilter mIntentFilter;
    private String mMainServer;
    private int mMaxAttempts;
    private boolean mMobileHotspot;
    private Network mNetwork;
    private NetworkAgentInfo mNetworkAgentInfo;
    private AtomicBoolean mNetworkDisconnected;
    private int mNetworkTypeByWebView;
    private int mRawHttpRespCode;
    private String mRawRedirectedHostName;
    private long mRequestTimestamp;
    private long mResponseTimestamp;
    protected Object mRxCounterWaitLock;
    protected int mTcpRxCounter;
    protected int mTcpRxFirstCounter;
    protected int mTcpRxSecondCounter;
    private TelephonyManager mTelManager;
    private BroadcastReceiver mTempReceiverWebView;
    private boolean mUseOverseaBakcup;
    private Object mWebViewWaitingLock;
    protected WifiManager mWifiManager;
    private boolean rxCounterRespRcvd;
    private volatile boolean webviewRespRcvd;

    /* renamed from: com.android.server.HwNetworkPropertyChecker.2 */
    class AnonymousClass2 extends Handler {
        AnonymousClass2(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            Object -get3;
            switch (msg.what) {
                case HwNetworkPropertyChecker.MSG_HTTP_RESP_TIMEOUT /*101*/:
                    HwNetworkPropertyChecker.this.mHttpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
                    if (msg.arg1 == 1) {
                        HwNetworkPropertyChecker.this.mHttpResponseCode = WifiProCommonUtils.RESP_CODE_TIMEOUT;
                    }
                    -get3 = HwNetworkPropertyChecker.this.mHttpRespWaitingLock;
                    synchronized (-get3) {
                        break;
                    }
                    HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                    HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                    break;
                case HwNetworkPropertyChecker.MSG_NETWORK_DISCONNECTED /*102*/:
                    if (HwNetworkPropertyChecker.this.mHandler.hasMessages(HwNetworkPropertyChecker.MSG_HTTP_RESP_TIMEOUT)) {
                        HwNetworkPropertyChecker.this.LOGD("MSG_HTTP_RESP_TIMEOUT msg removed because of disconnected.");
                        HwNetworkPropertyChecker.this.mHandler.removeMessages(HwNetworkPropertyChecker.MSG_HTTP_RESP_TIMEOUT);
                    }
                    HwNetworkPropertyChecker.this.mHttpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
                    HwNetworkPropertyChecker.this.mRawHttpRespCode = WifiProCommonUtils.HTTP_UNREACHALBE;
                    HwNetworkPropertyChecker.this.mMobileHotspot = false;
                    HwNetworkPropertyChecker.this.mRawRedirectedHostName = null;
                    -get3 = HwNetworkPropertyChecker.this.mHttpRespWaitingLock;
                    synchronized (-get3) {
                        break;
                    }
                    HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                    HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                    break;
                case HwNetworkPropertyChecker.MSG_HTTP_RESP_RCVD /*103*/:
                    if (HwNetworkPropertyChecker.this.mHandler.hasMessages(HwNetworkPropertyChecker.MSG_HTTP_RESP_TIMEOUT)) {
                        HwNetworkPropertyChecker.this.LOGD("MSG_HTTP_RESP_TIMEOUT msg removed because of HTTP response received.");
                        HwNetworkPropertyChecker.this.mHandler.removeMessages(HwNetworkPropertyChecker.MSG_HTTP_RESP_TIMEOUT);
                    }
                    -get3 = HwNetworkPropertyChecker.this.mHttpRespWaitingLock;
                    synchronized (-get3) {
                        break;
                    }
                    HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                    HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                    break;
                case HwNetworkPropertyChecker.MSG_DNS_RESP_RCVD /*104*/:
                    if (HwNetworkPropertyChecker.this.mHandler.hasMessages(HwNetworkPropertyChecker.MSG_HTTP_RESP_TIMEOUT)) {
                        HwNetworkPropertyChecker.this.LOGD("MSG_HTTP_RESP_TIMEOUT msg removed because of DNS response received.");
                        HwNetworkPropertyChecker.this.mHandler.removeMessages(HwNetworkPropertyChecker.MSG_HTTP_RESP_TIMEOUT);
                    }
                    HwNetworkPropertyChecker.this.mDnsCheckResult = msg.arg1;
                    -get3 = HwNetworkPropertyChecker.this.mHttpRespWaitingLock;
                    synchronized (-get3) {
                        break;
                    }
                    HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                    HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                    break;
                default:
                    super.handleMessage(msg);
            }
            super.handleMessage(msg);
        }
    }

    private class NetworkCheckerThread extends Thread {
        public static final String SERVER_BAIDU = "baidu";
        public static final String SERVER_BING = "bing";
        public static final String SERVER_YOUKU = "youku";
        public static final String SERVER_YOUTUBE = "youtube";
        private final String[] INVALID_REDIRECTED_HOST_1;
        private final String[] INVALID_REDIRECTED_HOST_2;
        private String gateWayAddr;
        private boolean isMainServer;
        private String mChckingServer;
        private boolean reconfirm;
        private HttpURLConnection urlConnection;

        public NetworkCheckerThread(String server, boolean mainServer, boolean reconfirm, String gw) {
            this.INVALID_REDIRECTED_HOST_1 = new String[]{"yuzua", "huayaochou", "freewifi.360.cn", "free.wifi.360.cn"};
            this.INVALID_REDIRECTED_HOST_2 = new String[]{"miwifi"};
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
                    this.urlConnection.setConnectTimeout(HwNetworkPropertyChecker.SOCKET_TIMEOUT_MS);
                    this.urlConnection.setReadTimeout(HwNetworkPropertyChecker.SOCKET_TIMEOUT_MS);
                    this.urlConnection.setUseCaches(false);
                    if (this.isMainServer) {
                        HwNetworkPropertyChecker.this.mRequestTimestamp = SystemClock.elapsedRealtime();
                    }
                    this.urlConnection.getInputStream();
                    if (this.isMainServer) {
                        HwNetworkPropertyChecker.this.mResponseTimestamp = SystemClock.elapsedRealtime();
                    }
                    httpResponseCode = recomfirmResponseCode(this.urlConnection.getResponseCode(), this.mChckingServer);
                    if (this.mChckingServer.equals(HwNetworkPropertyChecker.OVERSEA_MAIN_SERVER) && httpResponseCode == WifiProCommonUtils.HTTP_REACHALBE_HOME && this.urlConnection.getContentLength() == 0) {
                        HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, reset for clients3.google.com, respCode = " + httpResponseCode);
                        httpResponseCode = WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
                    }
                    if (this.mChckingServer.equals(HwNetworkPropertyChecker.OVERSEA_BACKUP_SERVER) && !this.isMainServer) {
                        HwNetworkPropertyChecker.this.mUseOverseaBakcup = true;
                    }
                    if (this.urlConnection != null) {
                        this.urlConnection.disconnect();
                    }
                    if (!(HwNetworkPropertyChecker.this.mIgnoreRxCounter || httpResponseCode != WifiProCommonUtils.HTTP_UNREACHALBE || HwNetworkPropertyChecker.this.mNetworkDisconnected.get() || HwNetworkPropertyChecker.this.mNetwork == null || (!this.reconfirm && HwNetworkPropertyChecker.this.mCheckingCounter != HwNetworkPropertyChecker.this.mMaxAttempts))) {
                        try {
                            InetAddress baidu = HwNetworkPropertyChecker.this.mNetwork.getByName("www.baidu.com");
                            InetAddress youku = null;
                            if (!HwNetworkPropertyChecker.this.mNetworkDisconnected.get()) {
                                youku = HwNetworkPropertyChecker.this.mNetwork.getByName("www.youku.com");
                            }
                            if (!(baidu == null || youku == null)) {
                                HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, 1st svr = " + baidu.getHostAddress() + ", 2nd svr = " + youku.getHostAddress());
                                if (baidu.getHostAddress() == null || !baidu.getHostAddress().equals(youku.getHostAddress())) {
                                    HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, DNS is ok on wlan device, but HTTP Connection failed!");
                                } else {
                                    HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, 1st/2nd svr hosts have the same ip address, network is exceptional.");
                                    HwNetworkPropertyChecker.this.mIgnoreRxCounter = true;
                                }
                            }
                        } catch (IOException e1) {
                            HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, IOException e1, msg = " + e1.getMessage());
                            HwNetworkPropertyChecker.this.mIgnoreRxCounter = true;
                        }
                    }
                    HwNetworkPropertyChecker.this.mHttpResponseCode = httpResponseCode;
                    if (!HwNetworkPropertyChecker.this.mNetworkDisconnected.get()) {
                        HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, HwNetworkPropertyChecker.MSG_HTTP_RESP_RCVD, 0, 0));
                    }
                    return;
                }
                HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, openConnection doesn't return HttpURLConnection instance.");
                if (this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
            } catch (IOException e) {
                String msg = e.getMessage();
                HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, IOException, msg = " + msg);
                if (msg == null || !msg.contains("ECONNREFUSED") || HwNetworkPropertyChecker.this.mCurrentWifiConfig == null || !WifiProCommonUtils.matchedRequestByHistory(HwNetworkPropertyChecker.this.mCurrentWifiConfig.internetHistory, 100)) {
                    if (msg != null) {
                        if (msg.contains("ECONNRESET") && HwNetworkPropertyChecker.this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(HwNetworkPropertyChecker.this.mCurrentWifiConfig.internetHistory, 100)) {
                            httpResponseCode = WifiProCommonUtils.getReachableCode(this.mChckingServer.equals(HwNetworkPropertyChecker.OVERSEA_MAIN_SERVER));
                            WifiProCHRManager.getInstance().updateActiveCheckFail(HwNetworkPropertyChecker.this.mCurrentWifiConfig.SSID, this.mChckingServer, 2);
                            WifiProCHRManager.getInstance().updateWifiException(WifiProCHRManager.WIFI_WIFIPRO_EXCEPTION_EVENT, WifiProCHRManager.SUB_EVENT_ACTIVE_CHECK_FAIL);
                        }
                    }
                    if (msg != null) {
                        if (msg.contains("ENETUNREACH") || msg.contains("ENONET") || msg.contains("Unable to resolve host")) {
                            HwNetworkPropertyChecker.this.mIgnoreRxCounter = true;
                        }
                    }
                } else {
                    httpResponseCode = WifiProCommonUtils.getReachableCode(this.mChckingServer.equals(HwNetworkPropertyChecker.OVERSEA_MAIN_SERVER));
                    WifiProCHRManager.getInstance().updateActiveCheckFail(HwNetworkPropertyChecker.this.mCurrentWifiConfig.SSID, this.mChckingServer, 1);
                    WifiProCHRManager.getInstance().updateWifiException(WifiProCHRManager.WIFI_WIFIPRO_EXCEPTION_EVENT, WifiProCHRManager.SUB_EVENT_ACTIVE_CHECK_FAIL);
                }
                if (msg != null && (msg.contains("ECONNRESET") || msg.contains("Connection reset"))) {
                    httpResponseCode = WifiProCommonUtils.RESP_CODE_CONN_RESET;
                } else if (msg != null && (msg.contains("ECONNREFUSED") || msg.contains("unexpected end"))) {
                    if (WifiProCommonUtils.isOpenType(HwNetworkPropertyChecker.this.mCurrentWifiConfig)) {
                        httpResponseCode = WifiProCommonUtils.RESP_CODE_UNSTABLE;
                    } else if (!(HwNetworkPropertyChecker.this.mCurrentWifiConfig == null || WifiProCommonUtils.matchedRequestByHistory(HwNetworkPropertyChecker.this.mCurrentWifiConfig.internetHistory, 100))) {
                        httpResponseCode = WifiProCommonUtils.RESP_CODE_UNSTABLE;
                    }
                }
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
                        if (this.reconfirm && !host.equals(HwNetworkPropertyChecker.this.mRawRedirectedHostName)) {
                            HwNetworkPropertyChecker.this.mForcedNoRecomfirm = true;
                        }
                        return WifiProCommonUtils.HTTP_REACHALBE_HOME;
                    } else if (HwNetworkPropertyChecker.CHINA_MAINLAND_MAIN_SERVER.equals(server) && isInvalidRedirectedHost(host, this.INVALID_REDIRECTED_HOST_1)) {
                        HwNetworkPropertyChecker.this.LOGW("recomfirmResponseCode, redirect to the host that has internet access, , host = " + host + ", 302 --> 200.");
                        return WifiProCommonUtils.HTTP_REACHALBE_HOME;
                    } else if (HwNetworkPropertyChecker.CHINA_MAINLAND_MAIN_SERVER.equals(server) && isInvalidRedirectedHost(host, this.INVALID_REDIRECTED_HOST_2)) {
                        HwNetworkPropertyChecker.this.LOGW("recomfirmResponseCode, redirect the host has no internet, need webview to confirm.");
                        return WifiProCommonUtils.RESP_CODE_ABNORMAL_SERVER;
                    } else if (WifiProCommonUtils.invalidUrlLocation(newLocation)) {
                        HwNetworkPropertyChecker.this.LOGW("recomfirmResponseCode, redirect the invalid url, need webview to confirm.");
                        return WifiProCommonUtils.RESP_CODE_INVALID_URL;
                    } else if (this.gateWayAddr != null && this.gateWayAddr.length() > 0 && host.contains(this.gateWayAddr)) {
                        HwNetworkPropertyChecker.this.LOGW("recomfirmResponseCode, redirect to the gateway, maybe something wrong, need webview to confirm.");
                        return WifiProCommonUtils.RESP_CODE_GATEWAY;
                    } else if (!WifiProCommonUtils.isOpenType(HwNetworkPropertyChecker.this.mCurrentWifiConfig)) {
                        WifiProCHRManager.getInstance().updateApRedirectUrl(newLocation);
                        WifiProCHRManager.getInstance().updateSSID(HwNetworkPropertyChecker.this.mCurrentWifiConfig.SSID);
                        WifiProCHRManager.getInstance().updateWifiException(WifiProCHRManager.WIFI_WIFIPRO_EXCEPTION_EVENT, WifiProCHRManager.SUB_EVENT_NOT_OPEN_AP_REDIRECT);
                    }
                }
                if (!(HwNetworkPropertyChecker.this.mNetworkDisconnected.get() || TextUtils.isEmpty(HwNetworkPropertyChecker.this.mRawRedirectedHostName) || TextUtils.isEmpty(host) || HwNetworkPropertyChecker.this.mRawRedirectedHostName.equals(host) || !WifiProCommonUtils.isOpenType(HwNetworkPropertyChecker.this.mCurrentWifiConfig))) {
                    return WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED;
                }
            }
            return respCode;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.HwNetworkPropertyChecker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.HwNetworkPropertyChecker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.HwNetworkPropertyChecker.<clinit>():void");
    }

    public HwNetworkPropertyChecker(Context context, WifiManager wifiManager, TelephonyManager telManager, boolean enabled, NetworkAgentInfo agent, boolean needRxBroadcast) {
        this.mNetworkDisconnected = new AtomicBoolean(true);
        this.mHttpRespWaitingLock = new Object();
        this.mRxCounterWaitLock = new Object();
        this.webviewRespRcvd = false;
        this.mWebViewWaitingLock = new Object();
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
        this.mMaxAttempts = MIN_RX_PKT_DELTA;
        this.mTcpRxCounter = 0;
        this.mTcpRxFirstCounter = 0;
        this.mTcpRxSecondCounter = 0;
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
        }
        this.mContentResolver = this.mContext.getContentResolver();
        init(needRxBroadcast);
    }

    private void init(boolean needRxBroadcast) {
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        if (needRxBroadcast) {
            this.mIntentFilter.addAction(ACTION_RESPONSE_TCP_RX_COUNTER);
        }
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && DetailedState.DISCONNECTED == info.getDetailedState() && !HwNetworkPropertyChecker.this.mNetworkDisconnected.get()) {
                        HwNetworkPropertyChecker.this.LOGD("NETWORK_STATE_CHANGED_ACTION, network is connected --> disconnected.");
                        HwNetworkPropertyChecker.this.mNetworkDisconnected.set(true);
                        HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, HwNetworkPropertyChecker.MSG_NETWORK_DISCONNECTED, 0, 0));
                    }
                } else if (HwNetworkPropertyChecker.ACTION_RESPONSE_TCP_RX_COUNTER.equals(intent.getAction())) {
                    int rx = intent.getIntExtra(HwNetworkPropertyChecker.EXTRA_FLAG_TCP_RX_COUNTER, 0);
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
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, NETWORK_CHECKER_RECV_PERMISSION, null);
        this.mHandler = new AnonymousClass2(Looper.getMainLooper());
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
        this.mContext.registerReceiver(this.mTempReceiverWebView, intentFilter, NETWORK_CHECKER_RECV_PERMISSION, null);
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
                this.mWifiManager = (WifiManager) this.mContext.getSystemService(GnssConnectivityLogManager.SUBSYS_WIFI);
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
                operator = Secure.getString(this.mContentResolver, OPERATOR_COUNTRY_KEY);
                if (!(operator == null || operator.length() == 0 || operator.startsWith(COUNTRY_CODE_CN))) {
                    this.mMainServer = OVERSEA_MAIN_SERVER;
                    this.mBackupServer = OVERSEA_BACKUP_SERVER;
                    this.mInOversea = true;
                    this.mForcedRecheck = true;
                }
            } else {
                Secure.putString(this.mContentResolver, OPERATOR_COUNTRY_KEY, operator);
                if (!operator.startsWith(COUNTRY_CODE_CN)) {
                    this.mMainServer = OVERSEA_MAIN_SERVER;
                    this.mBackupServer = OVERSEA_BACKUP_SERVER;
                    this.mInOversea = true;
                }
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

    private void sendNetworkCheckingStatus(String action, String flag, int property) {
        Intent intent = new Intent(action);
        intent.setFlags(67108864);
        intent.putExtra(flag, property);
        if (property == NETWORK_PROPERTY_PORTAL && !TextUtils.isEmpty(this.mRawRedirectedHostName)) {
            intent.putExtra(WifiProCommonDefs.EXTRA_RAW_REDIRECTED_HOST, this.mRawRedirectedHostName);
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public boolean isNetworkChanged() {
        String currSsid = WifiProCommonUtils.getCurrentSsid(this.mWifiManager);
        if (!(currSsid == null || this.mCurrentWifiConfig == null || this.mNetworkDisconnected.get())) {
            LOGW("isNetworkChanged, current ssid = " + currSsid + ", checking ssid = " + this.mCurrentWifiConfig.SSID);
            if (this.mCurrentWifiConfig.SSID != null && this.mCurrentWifiConfig.SSID.equals(currSsid)) {
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
            if (!(this.mControllerNotified || isNetworkChanged())) {
                this.mControllerNotified = true;
                property = finalRespCode == WifiProCommonUtils.HTTP_REACHALBE_GOOLE ? NETWORK_PROPERTY_INTERNET : NETWORK_PROPERTY_PORTAL;
                sendNetworkCheckingStatus(ACTION_NETWOR_PROPERTY_NOTIFICATION, EXTRA_FLAG_NETWOR_PROPERTY, property);
                if (property == NETWORK_PROPERTY_INTERNET && this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, MSG_NETWORK_DISCONNECTED)) {
                    property = NETWORK_PROPERTY_PORTAL;
                }
                sendNetworkCheckingStatus(ACTION_NETWORK_CONDITIONS_MEASURED, EXTRA_IS_INTERNET_READY, property);
            }
        } else if (this.mCheckingCounter >= this.mMaxAttempts) {
            this.mCheckingCounter = 0;
            ret = true;
            if (!(this.mControllerNotified || isNetworkChanged())) {
                this.mControllerNotified = true;
                property = NETWORK_PROPERTY_NO_INTERNET;
                if (this.mCurrentWifiConfig != null) {
                    property = (!WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, 100) || isNetworkPoorRssi() || WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig)) ? NETWORK_PROPERTY_NO_INTERNET : NETWORK_PROPERTY_PENDING;
                }
                sendNetworkCheckingStatus(ACTION_NETWOR_PROPERTY_NOTIFICATION, EXTRA_FLAG_NETWOR_PROPERTY, property);
                sendNetworkCheckingStatus(ACTION_NETWORK_CONDITIONS_MEASURED, EXTRA_IS_INTERNET_READY, NETWORK_PROPERTY_NO_INTERNET);
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
                    handler.sendMessageDelayed(Message.obtain(handler2, MSG_HTTP_RESP_TIMEOUT, i, 0), (long) timeout);
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
        if (WifiProStatusUtils.isWifiProEnabledViaProperties()) {
            Intent intent = new Intent(ACTION_REQUEST_TCP_RX_COUNTER);
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
        if (reconfirm || !WifiProCommonUtils.unreachableRespCode(httpResponseCode) || this.mCheckingCounter != this.mMaxAttempts || this.mNetworkDisconnected.get()) {
            tcpRxUsable = false;
        } else {
            tcpRxUsable = !this.mIgnoreRxCounter;
        }
        if (tcpRxUsable) {
            this.mTcpRxSecondCounter = requestTcpRxPacketsCounter();
            LOGD("recomfirmByRxCounter, firstRx = " + this.mTcpRxFirstCounter + ", secondRx = " + this.mTcpRxSecondCounter);
            if (this.mTcpRxSecondCounter - this.mTcpRxFirstCounter >= MIN_RX_PKT_DELTA && this.mTcpRxFirstCounter >= 0) {
                httpResponseCode = WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
                if (this.mCurrentWifiConfig != null) {
                    if (WifiProCommonUtils.isOpenAndPortal(this.mCurrentWifiConfig)) {
                        httpResponseCode = WifiProCommonUtils.HTTP_REDIRECTED;
                    } else {
                        boolean portalMatched = PortalDataBaseManager.getInstance(this.mContext).syncQueryPortalNetwork(this.mCurrentWifiConfig.SSID);
                        httpResponseCode = portalMatched ? WifiProCommonUtils.HTTP_REDIRECTED : WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
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
        LOGD("=====ENTER: ===isCaptivePortal, reconfirm = " + reconfirm + ", counter = " + this.mCheckingCounter);
        int httpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
        initialize(reconfirm);
        this.mCheckingCounter++;
        this.mForcedNoRecomfirm = false;
        if (!this.mNetworkDisconnected.get()) {
            httpResponseCode = syncCheckNetworkProperty(this.mMainServer, true, reconfirm);
            updateStarndardPortalRecord(httpResponseCode, reconfirm);
            httpResponseCode = transformResponseCode(recomfirmByRxCounter(recomfirmResponseCode(httpResponseCode, reconfirm), reconfirm));
            updateWifiConfigHistory(httpResponseCode, reconfirm);
        }
        LOGD("=====LEAVE: ===isCaptivePortal, httpResponseCode = " + httpResponseCode);
        this.mUseOverseaBakcup = false;
        return httpResponseCode;
    }

    private void updateStarndardPortalRecord(int respCode, boolean reconfirm) {
        if (!reconfirm && !this.mInOversea && WifiProCommonUtils.isRedirectedRespCode(respCode) && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig)) {
            PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
            if (database != null && this.mCurrentWifiConfig != null) {
                LOGD("updateStarndardPortalRecord, ssid = " + this.mCurrentWifiConfig.SSID);
                database.updateStandardPortalTable(this.mCurrentWifiConfig.SSID, System.currentTimeMillis());
            }
        }
    }

    private boolean isStarndardPortalAuthenCompleted(int respCode) {
        PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
        if (!(this.mInOversea || database == null || this.mCurrentWifiConfig == null || !WifiProCommonUtils.httpReachableHome(respCode))) {
            long timestamp = database.syncQueryStarndardPortalNetwork(this.mCurrentWifiConfig.SSID);
            if (timestamp > 0 && System.currentTimeMillis() - timestamp <= 14400000) {
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
        if (!this.mInOversea || this.mUseOverseaBakcup) {
            if (!WifiProCommonUtils.httpReachableOrRedirected(respCode) || WifiProCommonUtils.isRedirectedRespCode(respCode)) {
                return httpResponseCode;
            }
            return WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
        } else if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(respCode)) {
            return WifiProCommonUtils.HTTP_REDIRECTED;
        } else {
            return httpResponseCode;
        }
    }

    public int syncRecheckBasedOnWebView(boolean oversea) {
        LOGD("syncRecheckBasedOnWebView:: begin, oversea = " + oversea);
        if (WifiProStatusUtils.isWifiProEnabledViaProperties()) {
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
        this.mDnsCheckResult = NETWORK_PROPERTY_NO_INTERNET;
        synchronized (this.mHttpRespWaitingLock) {
            try {
                new Thread(new Runnable() {
                    public void run() {
                        HwNetworkPropertyChecker.this.mNetwork = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
                        try {
                            if (HwNetworkPropertyChecker.this.mNetwork != null) {
                                HwNetworkPropertyChecker.this.mNetwork.getByName("www.bing.com");
                                HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, HwNetworkPropertyChecker.MSG_DNS_RESP_RCVD, 0, 0));
                            }
                        } catch (UnknownHostException e) {
                            HwNetworkPropertyChecker.this.LOGD("syncCheckDnsResponse, Unabled to resolve this host.");
                            HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, HwNetworkPropertyChecker.MSG_DNS_RESP_RCVD, HwNetworkPropertyChecker.NETWORK_PROPERTY_NO_INTERNET, 0));
                        }
                    }
                }).start();
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, MSG_HTTP_RESP_TIMEOUT, 0, 0), 6000);
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
        if (this.mCurrentWifiConfig == null || this.mNetworkDisconnected.get() || (reconfirm && respCode == WifiProCommonUtils.RESP_CODE_TIMEOUT)) {
            return respCode;
        }
        if (this.mForcedRecheck && WifiProCommonUtils.unreachableRespCode(respCode)) {
            newRespCode = syncCheckNetworkProperty(CHINA_MAINLAND_MAIN_SERVER, false, reconfirm);
            if (WifiProCommonUtils.httpReachableOrRedirected(newRespCode)) {
                repairOk = true;
                if (newRespCode == WifiProCommonUtils.HTTP_REACHALBE_HOME) {
                    newRespCode = WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
                }
                Secure.putString(this.mContentResolver, OPERATOR_COUNTRY_KEY, "46000");
            }
        }
        if (respCode == WifiProCommonUtils.RESP_CODE_CONN_RESET) {
            this.mUseOverseaBakcup = true;
            respCode = recheckForConnectionReset(respCode, reconfirm);
        }
        if (!this.mInOversea && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig) && WifiProCommonUtils.isInMonitorList(this.mCurrentWifiConfig.SSID, SPECIAL_REDIRECTED_PORTAL_LIST)) {
            respCode = WifiProCommonUtils.HTTP_REDIRECTED;
            if (syncRecheckBasedOnWebView(this.mInOversea) == MSG_HTTP_RESP_TIMEOUT && !this.mNetworkDisconnected.get()) {
                LOGW("recomfirmRespCode, SPECIAL_PORTAL, connected network type is hasInternet based on the WebView loading results.");
                repairOk = true;
                newRespCode = WifiProCommonUtils.HTTP_REACHALBE_HOME;
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
        } else if (!this.mInOversea && WifiProCommonUtils.httpReachableHome(newRespCode) && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig) && !this.mForcedNoRecomfirm && !this.mMobileHotspot && !isStarndardPortalAuthenCompleted(newRespCode)) {
            recheckRespCode = recheckForOpen200Type(newRespCode, reconfirm);
            if (recheckRespCode != newRespCode) {
                repairOk = true;
                newRespCode = recheckRespCode;
            }
        } else if (!reconfirm && this.mCheckingCounter == this.mMaxAttempts && WifiProCommonUtils.unreachableRespCode(respCode) && WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, 100)) {
            LOGD("for last time, network has internet ever, use the backup server to recheck it.");
            newRespCode = syncCheckNetworkProperty(this.mBackupServer, false, reconfirm);
            repairOk = WifiProCommonUtils.httpReachableHome(newRespCode);
        }
        if (!repairOk) {
            newRespCode = respCode;
        }
        return newRespCode;
    }

    private int recheckForRedirectedHostChanged() {
        boolean z = false;
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForRedirectedHostChanged, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type != MSG_HTTP_RESP_TIMEOUT) {
            return WifiProCommonUtils.HTTP_REDIRECTED;
        } else {
            LOGW("recheckForRedirectedHostChanged, connected network type is hasInternet based on the WebView loading results.");
            if (this.mInOversea && !this.mUseOverseaBakcup) {
                z = true;
            }
            return WifiProCommonUtils.getReachableCode(z);
        }
    }

    private int recheckForConnectionReset(int respCode, boolean reconfirm) {
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForConnectionReset, network is disconnected.");
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
        String server1 = this.mInOversea ? OVERSEA_BACKUP_SERVER : CHINA_MAINLAND_BACKUP_SERVER;
        String server2 = this.mInOversea ? OVERSEA_BACKUP_YOUTUBE : OVERSEA_BACKUP_SERVER;
        int tmpRespCode = syncCheckNetworkProperty(server1, false, reconfirm);
        LOGD("recheckForConnectionReset, use 2nd server, respCode = " + tmpRespCode);
        if (tmpRespCode == WifiProCommonUtils.RESP_CODE_CONN_RESET) {
            tmpRespCode = syncCheckNetworkProperty(server2, false, reconfirm);
            LOGD("recheckForConnectionReset, use 3rd server, respCode = " + tmpRespCode);
            if (tmpRespCode == WifiProCommonUtils.RESP_CODE_CONN_RESET || tmpRespCode == WifiProCommonUtils.HTTP_UNREACHALBE || tmpRespCode == WifiProCommonUtils.RESP_CODE_TIMEOUT) {
                return WifiProCommonUtils.HTTP_UNREACHALBE;
            }
            if (WifiProCommonUtils.isRedirectedRespCode(tmpRespCode)) {
                return WifiProCommonUtils.HTTP_REDIRECTED;
            }
            if (WifiProCommonUtils.httpReachableHome(tmpRespCode)) {
                return WifiProCommonUtils.HTTP_REACHALBE_HOME;
            }
        } else if (tmpRespCode == WifiProCommonUtils.HTTP_UNREACHALBE || tmpRespCode == WifiProCommonUtils.RESP_CODE_TIMEOUT) {
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else {
            if (WifiProCommonUtils.isRedirectedRespCode(tmpRespCode)) {
                return WifiProCommonUtils.HTTP_REDIRECTED;
            }
            if (WifiProCommonUtils.httpReachableHome(tmpRespCode)) {
                return WifiProCommonUtils.HTTP_REACHALBE_HOME;
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
        } else if (type == MSG_NETWORK_DISCONNECTED) {
            LOGW("recheckForAbnormalStatus, SPECIAL_STATUS, connected network type is portal based on the WebView loading results.");
            return WifiProCommonUtils.HTTP_REDIRECTED;
        } else if (type == MSG_HTTP_RESP_TIMEOUT) {
            LOGW("recheckForAbnormalStatus, SPECIAL_STATUS, connected network type is hasInternet based on the WebView loading results.");
            if (this.mInOversea && !this.mUseOverseaBakcup) {
                z = true;
            }
            return WifiProCommonUtils.getReachableCode(z);
        } else {
            if (type == 100) {
                int tmpRespCode = syncCheckNetworkProperty(OVERSEA_MAIN_SERVER, false, reconfirm);
                LOGW("recheckForAbnormalStatus, SPECIAL_STATUS, using google's server, tmpRespCode = " + tmpRespCode);
                if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(tmpRespCode)) {
                    return WifiProCommonUtils.HTTP_REDIRECTED;
                }
                if (!this.mNetworkDisconnected.get() && WifiProCommonUtils.httpUnreachableOrAbnormal(tmpRespCode) && PortalDataBaseManager.getInstance(this.mContext).syncQueryPortalNetwork(this.mCurrentWifiConfig.SSID)) {
                    LOGD("recheckForAbnormalStatus, SPECIAL_STATUS, syncQueryPortalNetwork, the query result is true.");
                    return WifiProCommonUtils.HTTP_REDIRECTED;
                }
            }
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
    }

    private int recheckForAbnormalRouterServer(int respCode, boolean reconfirm) {
        boolean z = false;
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForAbnormalRouterServer, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type == MSG_NETWORK_DISCONNECTED || type == 100) {
            LOGW("recheckForAbnormalRouterServer, connected network is 599 based on the WebView loading results.");
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type != MSG_HTTP_RESP_TIMEOUT) {
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else {
            LOGW("recheckForAbnormalRouterServer, connected network type is hasInternet based on the WebView");
            if (this.mInOversea && !this.mUseOverseaBakcup) {
                z = true;
            }
            return WifiProCommonUtils.getReachableCode(z);
        }
    }

    private int recheckForOpen200Type(int lastRespCode, boolean reconfirm) {
        int newRespCode = lastRespCode;
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForOpen200Type, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
        if (type == MSG_NETWORK_DISCONNECTED) {
            LOGW("recheckForOpen200Type, connected network type is portal based on the WebView loading results.");
            newRespCode = WifiProCommonUtils.HTTP_REDIRECTED;
        } else if (type == 100) {
            int tmpRespCode = syncCheckNetworkProperty(OVERSEA_MAIN_SERVER, false, reconfirm);
            LOGW("recheckForOpen200Type, using google's server, tmpRespCode = " + tmpRespCode + ", current ssid = " + this.mCurrentWifiConfig.SSID);
            if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(tmpRespCode)) {
                newRespCode = WifiProCommonUtils.HTTP_REDIRECTED;
            } else if (!this.mNetworkDisconnected.get() && WifiProCommonUtils.httpUnreachableOrAbnormal(tmpRespCode) && PortalDataBaseManager.getInstance(this.mContext).syncQueryPortalNetwork(this.mCurrentWifiConfig.SSID)) {
                LOGD("recheckForOpen200Type, syncQueryPortalNetwork, database is matched, it's known portal.");
                newRespCode = WifiProCommonUtils.HTTP_REDIRECTED;
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
        if (type == MSG_HTTP_RESP_TIMEOUT) {
            LOGD("recheckForHotspot302Type, hotspot has internet access when load webpage.");
            newRespCode = WifiProCommonUtils.HTTP_REACHALBE_HOME;
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
        } else if (type == MSG_HTTP_RESP_TIMEOUT) {
            LOGW("recheckForNonOpen302Type, 30x/GW from NON-OPEN WiFi, has internet based on the WebView.");
            return WifiProCommonUtils.HTTP_REACHALBE_HOME;
        } else if (type == MSG_NETWORK_DISCONNECTED && lastRespCode == WifiProCommonUtils.RESP_CODE_GATEWAY) {
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
            if (internetHistory == null || internetHistory.lastIndexOf("/") == NETWORK_PROPERTY_NO_INTERNET) {
                LOGW("updateWifiConfigHistory, inputed arg is invalid, internetHistory = " + internetHistory);
                return;
            }
            String status = internetHistory.substring(0, 1);
            Intent intent;
            if (status != null && status.equals(PPPOEStateMachine.PHASE_DEAD)) {
                boolean z2;
                int newStatus = respCode == WifiProCommonUtils.HTTP_REACHALBE_GOOLE ? 1 : 2;
                internetHistory = String.valueOf(newStatus) + "/" + internetHistory.substring(internetHistory.indexOf("/") + 1);
                this.mCurrentWifiConfig.noInternetAccess = false;
                WifiConfiguration wifiConfiguration = this.mCurrentWifiConfig;
                if (this.mCurrentWifiConfig.noInternetAccess) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                wifiConfiguration.validatedInternetAccess = z2;
                if (newStatus == 1) {
                    this.mCurrentWifiConfig.numNoInternetAccessReports = 0;
                    this.mCurrentWifiConfig.lastHasInternetTimestamp = System.currentTimeMillis();
                }
                WifiConfiguration wifiConfiguration2 = this.mCurrentWifiConfig;
                if (respCode != WifiProCommonUtils.HTTP_REACHALBE_GOOLE) {
                    z = true;
                }
                wifiConfiguration2.portalNetwork = z;
                this.mCurrentWifiConfig.internetHistory = internetHistory;
                if (newStatus == 1) {
                    sendNetworkCheckingStatus(ACTION_NETWORK_CONDITIONS_MEASURED, EXTRA_IS_INTERNET_READY, 1);
                }
                intent = new Intent(ACTION_UPDATE_CONFIG_HISTORY);
                intent.putExtra(EXTRA_FLAG_NEW_WIFI_CONFIG, new WifiConfiguration(this.mCurrentWifiConfig));
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, NETWORK_CHECKER_RECV_PERMISSION);
            } else if (status != null && status.equals(PPPOEStateMachine.PHASE_SERIALCONN) && respCode == WifiProCommonUtils.HTTP_REACHALBE_GOOLE) {
                this.mCurrentWifiConfig.lastHasInternetTimestamp = System.currentTimeMillis();
                this.mCurrentWifiConfig.portalCheckStatus = 0;
                intent = new Intent(ACTION_UPDATE_CONFIG_HISTORY);
                intent.putExtra(EXTRA_FLAG_NEW_WIFI_CONFIG, new WifiConfiguration(this.mCurrentWifiConfig));
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, NETWORK_CHECKER_RECV_PERMISSION);
            }
        }
    }

    public boolean isNetworkPoorRssi() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getRssi() >= POOR_RSSI_LEVEL) {
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
        this.httpRespCodeRcvd = false;
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
