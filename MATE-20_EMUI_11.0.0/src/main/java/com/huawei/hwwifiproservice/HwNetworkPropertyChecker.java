package com.huawei.hwwifiproservice;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.ScanResultRecords;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwNetworkPropertyChecker {
    private static final String BUNDLE_FLAG_MAIN_DETECT_MS = "mainDetectMs";
    public static final String BUNDLE_FLAG_REDIRECT_URL = "redirectUrl";
    public static final String BUNDLE_FLAG_USED_SERVER = "usedServer";
    public static final String BUNDLE_PROBE_RESPONSE_CODE = "probeResponseCode";
    public static final String BUNDLE_PROBE_RESPONSE_TIME = "probeResponseTime";
    private static final int HAS_INTERNET_CODE = 204;
    private static final int HTTP_SERVER_OPT_FAILED = 599;
    public static final int HW_MAX_RETRIES = 3;
    private static final int LAC_UNKNOWN = -1;
    private static final int MSG_DNS_RESP_RCVD = 104;
    private static final int MSG_HTML_DOWNLOADED_RCVD = 105;
    private static final int MSG_HTTP_RESP_RCVD = 103;
    private static final int MSG_HTTP_RESP_TIMEOUT = 101;
    private static final int MSG_NETWORK_DISCONNECTED = 102;
    public static final int NETWORK_PROPERTY_INTERNET = 5;
    public static final int NETWORK_PROPERTY_NO_INTERNET = -1;
    public static final int NETWORK_PROPERTY_PORTAL = 6;
    private static final int NO_INTERNET_CODE = 599;
    private static final int PORTAL_CODE = 302;
    private static final String PRODUCT_LOCALE_CN = "CN";
    public static final String SERVER_HICLOUD = "hicloud";
    public static final String TAG = "HwNetworkPropertyChecker";
    private BroadcastReceiver mBroadcastReceiver;
    private CellLocation mCellLocation;
    private boolean mCheckerInitialized;
    private int mCheckingCounter;
    protected Context mContext;
    private boolean mControllerNotified;
    private WifiConfiguration mCurrentWifiConfig;
    private long mDetectTime;
    private Handler mHandler;
    private final Object mHtmlDownloadWaitingLock = new Object();
    private final Object mHttpRespWaitingLock = new Object();
    private int mHttpResponseCode;
    private NetworkMonitor mHwNetworkMonitor;
    protected boolean mIgnoreRxCounter;
    private boolean mInOversea;
    private IntentFilter mIntentFilter;
    private boolean mLastDetectTimeout = false;
    private int mMaxAttempts;
    private boolean mMobileHotspot;
    private Network mNetwork;
    private AtomicBoolean mNetworkDisconnected = new AtomicBoolean(true);
    private Network mNetworkInfo;
    private String mPortalDetectStatisticsInfo = null;
    private String mPortalRedirectedUrl = "";
    private int mRawHttpRespCode;
    private String mRawRedirectedHostName;
    private final Object mRxCounterWaitLock = new Object();
    protected int mTcpRxCounter;
    private TelephonyManager mTelManager;
    private String mUsedServer = null;
    protected WifiManager mWifiManager;
    private boolean rxCounterRespRcvd;

    public static class StarndardPortalInfo {
        public String currentSsid = "";
        public int lac = -1;
        public long timestamp = 0;
    }

    public HwNetworkPropertyChecker(Context context, WifiManager wifiManager, TelephonyManager telManager, boolean enabled, Network agent, boolean needRxBroadcast) {
        this.mContext = context;
        this.mWifiManager = wifiManager;
        this.mTelManager = telManager;
        this.mNetworkInfo = agent;
        this.mNetwork = null;
        this.mCurrentWifiConfig = null;
        this.mInOversea = false;
        this.mCheckerInitialized = false;
        this.mHttpResponseCode = CaptivePortalProbeResult.FAILED_CODE;
        this.mIgnoreRxCounter = false;
        this.mControllerNotified = false;
        this.mCheckingCounter = 0;
        this.mMaxAttempts = 3;
        this.mTcpRxCounter = 0;
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        init(needRxBroadcast);
        this.mHwNetworkMonitor = new NetworkMonitor(context);
    }

    private void init(boolean needRxBroadcast) {
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        if (needRxBroadcast) {
            this.mIntentFilter.addAction("com.huawei.wifi.action.ACTION_RESPONSE_TCP_RX_COUNTER");
        }
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.huawei.hwwifiproservice.HwNetworkPropertyChecker.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int i = 0;
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && NetworkInfo.DetailedState.DISCONNECTED == info.getDetailedState() && !HwNetworkPropertyChecker.this.mNetworkDisconnected.get()) {
                        HwHiLog.d(HwNetworkPropertyChecker.TAG, false, "NETWORK_STATE_CHANGED_ACTION, network is connected --> disconnected.", new Object[0]);
                        HwNetworkPropertyChecker.this.mNetworkDisconnected.set(true);
                        HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, 102, 0, 0));
                    }
                } else if ("com.huawei.wifi.action.ACTION_RESPONSE_TCP_RX_COUNTER".equals(intent.getAction())) {
                    int rx = intent.getIntExtra("wifipro_tcp_rx_counter", 0);
                    HwNetworkPropertyChecker hwNetworkPropertyChecker = HwNetworkPropertyChecker.this;
                    if (rx > 0) {
                        i = rx;
                    }
                    hwNetworkPropertyChecker.mTcpRxCounter = i;
                    synchronized (HwNetworkPropertyChecker.this.mRxCounterWaitLock) {
                        HwNetworkPropertyChecker.this.rxCounterRespRcvd = true;
                        HwNetworkPropertyChecker.this.mRxCounterWaitLock.notifyAll();
                    }
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER", null);
        this.mHandler = new Handler(Looper.getMainLooper()) {
            /* class com.huawei.hwwifiproservice.HwNetworkPropertyChecker.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 101:
                        HwNetworkPropertyChecker.this.mHttpResponseCode = CaptivePortalProbeResult.FAILED_CODE;
                        boolean reconfirmDetect = msg.arg1 == 1;
                        if (!reconfirmDetect && HwNetworkPropertyChecker.this.mCheckingCounter < HwNetworkPropertyChecker.this.mMaxAttempts) {
                            HwNetworkPropertyChecker.this.mLastDetectTimeout = true;
                        }
                        int mainHttpCode = HwNetworkPropertyChecker.this.mLastDetectTimeout ? 600 : msg.arg2;
                        Bundle bundle = (Bundle) msg.obj;
                        if (bundle != null) {
                            long mainDetectMs = bundle.getLong(HwNetworkPropertyChecker.BUNDLE_FLAG_MAIN_DETECT_MS);
                            String usedServer = bundle.getString(HwNetworkPropertyChecker.BUNDLE_FLAG_USED_SERVER);
                            HwNetworkPropertyChecker hwNetworkPropertyChecker = HwNetworkPropertyChecker.this;
                            hwNetworkPropertyChecker.updatePortalDetectionStatistics(reconfirmDetect, 600, -1, usedServer, mainHttpCode, mainDetectMs, hwNetworkPropertyChecker.mCurrentWifiConfig);
                            if (reconfirmDetect) {
                                HwNetworkPropertyChecker.this.mHttpResponseCode = 600;
                            }
                            synchronized (HwNetworkPropertyChecker.this.mHttpRespWaitingLock) {
                                HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                            }
                            break;
                        } else {
                            HwHiLog.d(HwNetworkPropertyChecker.TAG, false, "MSG_HTTP_RESP_TIMEOUT get Bundle fail,Bundle is null", new Object[0]);
                            break;
                        }
                    case 102:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(101)) {
                            HwHiLog.d(HwNetworkPropertyChecker.TAG, false, "MSG_HTTP_RESP_TIMEOUT msg removed because of disconnected.", new Object[0]);
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(101);
                        }
                        HwNetworkPropertyChecker.this.mHttpResponseCode = CaptivePortalProbeResult.FAILED_CODE;
                        HwNetworkPropertyChecker.this.mRawHttpRespCode = CaptivePortalProbeResult.FAILED_CODE;
                        HwNetworkPropertyChecker.this.mMobileHotspot = false;
                        HwNetworkPropertyChecker.this.mCheckerInitialized = false;
                        HwNetworkPropertyChecker.this.mRawRedirectedHostName = null;
                        HwNetworkPropertyChecker.this.mPortalRedirectedUrl = "";
                        HwNetworkPropertyChecker.this.mUsedServer = null;
                        synchronized (HwNetworkPropertyChecker.this.mHttpRespWaitingLock) {
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                        }
                        break;
                    case HwNetworkPropertyChecker.MSG_HTTP_RESP_RCVD /* 103 */:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(101)) {
                            HwHiLog.d(HwNetworkPropertyChecker.TAG, false, "MSG_HTTP_RESP_TIMEOUT msg removed because of HTTP response received.", new Object[0]);
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(101);
                        }
                        synchronized (HwNetworkPropertyChecker.this.mHttpRespWaitingLock) {
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                        }
                        break;
                    case HwNetworkPropertyChecker.MSG_DNS_RESP_RCVD /* 104 */:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(101)) {
                            HwHiLog.d(HwNetworkPropertyChecker.TAG, false, "MSG_HTTP_RESP_TIMEOUT msg removed because of DNS response received.", new Object[0]);
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(101);
                        }
                        synchronized (HwNetworkPropertyChecker.this.mHttpRespWaitingLock) {
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                        }
                        break;
                    case HwNetworkPropertyChecker.MSG_HTML_DOWNLOADED_RCVD /* 105 */:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(HwNetworkPropertyChecker.MSG_HTML_DOWNLOADED_RCVD)) {
                            HwHiLog.d(HwNetworkPropertyChecker.TAG, false, "MSG_HTML_DOWNLOADED_RCVD msg removed because of html downloaded.", new Object[0]);
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(HwNetworkPropertyChecker.MSG_HTML_DOWNLOADED_RCVD);
                        }
                        synchronized (HwNetworkPropertyChecker.this.mHtmlDownloadWaitingLock) {
                            HwNetworkPropertyChecker.this.mHtmlDownloadWaitingLock.notifyAll();
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    public static class OneAddressPerFamilyNetwork extends Network {
        public OneAddressPerFamilyNetwork(Network network) {
            super(network);
        }
    }

    private void initCurrWifiConfig() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
            if (!(configNetworks == null || wifiInfo == null)) {
                for (int i = 0; i < configNetworks.size(); i++) {
                    WifiConfiguration config = configNetworks.get(i);
                    if (config.networkId == wifiInfo.getNetworkId()) {
                        this.mCurrentWifiConfig = config;
                        HwHiLog.d(TAG, false, "initialize, current rssi = %{public}d,network = %{public}s", new Object[]{Integer.valueOf(wifiInfo.getRssi()), StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                        this.mNetworkDisconnected.set(false);
                        return;
                    }
                }
            }
        }
    }

    private Network getNetworkForTypeWifi() {
        Bundle bundle = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 14, null);
        if (bundle != null) {
            return (Network) bundle.getParcelable(NetworkMonitor.KEY_NETWORK_NAME);
        }
        return null;
    }

    private void initialize(boolean reconfirm) {
        if (!this.mCheckerInitialized) {
            if (this.mWifiManager == null) {
                this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            }
            if (this.mTelManager == null) {
                this.mTelManager = (TelephonyManager) this.mContext.getSystemService("phone");
            }
            if (this.mNetwork == null) {
                Network network = this.mNetworkInfo;
                if (network != null) {
                    this.mNetwork = new OneAddressPerFamilyNetwork(network);
                } else {
                    Network activeNetwork = getNetworkForTypeWifi();
                    if (activeNetwork != null) {
                        this.mNetwork = new OneAddressPerFamilyNetwork(activeNetwork);
                    }
                }
            }
            initCurrWifiConfig();
            String operator = this.mTelManager.getNetworkOperator();
            if (operator == null || operator.length() == 0) {
                if (!PRODUCT_LOCALE_CN.equalsIgnoreCase(WifiProCommonUtils.getProductLocale())) {
                    HwHiLog.d(TAG, false, "initialize, can't get network operator!", new Object[0]);
                    this.mInOversea = true;
                }
            } else if (!operator.startsWith("460")) {
                this.mInOversea = true;
            }
            this.mMobileHotspot = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContext);
            this.mCheckerInitialized = true;
            WifiConfiguration wifiConfiguration = this.mCurrentWifiConfig;
            if (wifiConfiguration != null) {
                HwHiLog.d(TAG, false, "initialize, AP's network history = %{public}s, operator = %{public}s, mInOversea = %{public}s", new Object[]{wifiConfiguration.internetHistory, operator, String.valueOf(this.mInOversea)});
            }
        }
        if (reconfirm) {
            this.mNetwork = getNetworkForTypeWifi();
            initCurrWifiConfig();
        }
        this.mIgnoreRxCounter = false;
    }

    private void sendCheckResultWhenConnected(int finalRespCode, String action, String flag, int property) {
        Intent intent = new Intent(action);
        intent.setFlags(67108864);
        intent.putExtra(flag, property);
        if ("wifi_network_property".equals(flag)) {
            boolean firstDetected = true;
            if (property == 6) {
                if (!TextUtils.isEmpty(this.mRawRedirectedHostName)) {
                    intent.putExtra("raw_redirected_host", this.mRawRedirectedHostName);
                }
                intent.putExtra("standard_portal_network", true);
            }
            String str = this.mPortalDetectStatisticsInfo;
            if (str != null) {
                intent.putExtra("portal_detect_stat_info", str);
            }
            WifiConfiguration wifiConfiguration = this.mCurrentWifiConfig;
            if (wifiConfiguration == null || !WifiProCommonUtils.matchedRequestByHistory(wifiConfiguration.internetHistory, (int) MSG_HTTP_RESP_RCVD)) {
                firstDetected = false;
            }
            intent.putExtra("portal_http_resp_code", finalRespCode);
            intent.putExtra("portal_first_detect", firstDetected);
            intent.putExtra("portal_redirected_url", this.mPortalRedirectedUrl);
            WifiConfiguration wifiConfiguration2 = this.mCurrentWifiConfig;
            intent.putExtra("portal_config_key", wifiConfiguration2 != null ? wifiConfiguration2.configKey() : "");
        }
        this.mContext.sendBroadcast(intent, "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER");
    }

    public int isCaptivePortal(boolean reconfirm) {
        return isCaptivePortal(reconfirm, false, false);
    }

    public long getDetectTime() {
        return this.mDetectTime;
    }

    public int isCaptivePortal(boolean reconfirm, boolean portalNetwork, boolean wifiBackground) {
        HwHiLog.d(TAG, false, "=====ENTER: ===isCaptivePortal, reconfirm = %{public}s, counter = %{public}d", new Object[]{String.valueOf(reconfirm), Integer.valueOf(this.mCheckingCounter)});
        int httpResponseCode = CaptivePortalProbeResult.FAILED_CODE;
        initialize(reconfirm);
        this.mCheckingCounter++;
        if (!this.mNetworkDisconnected.get()) {
            Bundle result = this.mHwNetworkMonitor.getProbeResponse();
            httpResponseCode = result.getInt(BUNDLE_PROBE_RESPONSE_CODE, CaptivePortalProbeResult.FAILED_CODE);
            this.mUsedServer = result.getString(BUNDLE_FLAG_USED_SERVER, "");
            this.mDetectTime = result.getLong(BUNDLE_PROBE_RESPONSE_TIME, 0);
            this.mPortalRedirectedUrl = result.getString(BUNDLE_FLAG_REDIRECT_URL, "");
            if ((isHomeRouter() || isHilinkRouter()) && WifiProCommonUtils.isRedirectedRespCodeByGoogle(httpResponseCode)) {
                HwHiLog.d(TAG, false, "redirected by home router, do not update history", new Object[0]);
            } else {
                updateStarndardPortalRecord(httpResponseCode, reconfirm);
                updateWifiConfigHistory(httpResponseCode, reconfirm);
            }
        }
        HwHiLog.d(TAG, false, "=====LEAVE: ===isCaptivePortal, httpResponseCode = %{public}d, ovs= %{public}s, reconfirm = %{public}s", new Object[]{Integer.valueOf(httpResponseCode), String.valueOf(this.mInOversea), String.valueOf(reconfirm)});
        WifiProChrUploadManager uploadManager = WifiProChrUploadManager.getInstance(this.mContext);
        if (httpResponseCode == 204) {
            uploadManager.addChrSsidCntStat("activeDetecEvent", "hasInternet");
        } else if (httpResponseCode == 599) {
            uploadManager.addChrSsidCntStat("activeDetecEvent", "noInternet");
        } else if (httpResponseCode == 302) {
            uploadManager.addChrSsidCntStat("activeDetecEvent", "portal");
        } else {
            HwHiLog.w(TAG, false, "httpResponseCode is not in active detect event", new Object[0]);
        }
        return httpResponseCode;
    }

    private boolean isHomeRouter() {
        String[] redirectedUrls = this.mContext.getResources().getStringArray(33816599);
        if (redirectedUrls == null || redirectedUrls.length == 0) {
            return false;
        }
        for (String url : redirectedUrls) {
            String str = this.mPortalRedirectedUrl;
            if (str != null && str.contains(url)) {
                HwHiLog.d(TAG, false, "home router, because the redirect url matches the keyword", new Object[0]);
                return true;
            }
        }
        return false;
    }

    private boolean isHilinkRouter() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getBSSID() == null || ScanResultRecords.getDefault().getHiLinkAp(wifiInfo.getBSSID()) != 1) {
            return false;
        }
        return true;
    }

    private int getCurrentLac() {
        if (this.mTelManager == null) {
            this.mTelManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        this.mCellLocation = this.mTelManager.getCellLocation();
        int simstatus = this.mTelManager.getSimState();
        CellLocation cellLocation = this.mCellLocation;
        if (cellLocation == null || simstatus == 1) {
            return -1;
        }
        if (cellLocation instanceof GsmCellLocation) {
            return ((GsmCellLocation) cellLocation).getLac();
        }
        if (cellLocation instanceof CdmaCellLocation) {
            return ((CdmaCellLocation) cellLocation).getNetworkId();
        }
        HwHiLog.w(TAG, false, "CellLocation PhoneType Unknown.", new Object[0]);
        return -1;
    }

    private void updateStarndardPortalRecord(int respCode, boolean reconfirm) {
        HwWifiProPartManager wifiProPartManager;
        if (!reconfirm && WifiProCommonUtils.isRedirectedRespCode(respCode) && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig) && (wifiProPartManager = HwWifiProPartManager.getInstance()) != null && this.mCurrentWifiConfig != null) {
            StarndardPortalInfo portalInfo = new StarndardPortalInfo();
            portalInfo.currentSsid = this.mCurrentWifiConfig.SSID;
            portalInfo.lac = getCurrentLac();
            portalInfo.timestamp = System.currentTimeMillis();
            HwHiLog.d(TAG, false, "updateStarndardPortalRecord, ssid = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(this.mCurrentWifiConfig.SSID)});
            wifiProPartManager.updateStandardPortalTable(portalInfo);
        }
    }

    public String getCaptiveUsedServer() {
        if (TextUtils.isEmpty(this.mUsedServer)) {
            return this.mHwNetworkMonitor.getCaptivePortalServerHttpUrl();
        }
        return this.mUsedServer;
    }

    public String getPortalRedirectedUrl() {
        return this.mPortalRedirectedUrl;
    }

    private void updateWifiConfigHistory(int respCode, boolean reconfirm) {
        WifiConfiguration wifiConfiguration;
        if (reconfirm && WifiProCommonUtils.httpReachableOrRedirected(respCode) && (wifiConfiguration = this.mCurrentWifiConfig) != null) {
            String internetHistory = wifiConfiguration.internetHistory;
            boolean z = false;
            if (internetHistory == null || internetHistory.lastIndexOf("/") == -1) {
                HwHiLog.w(TAG, false, "updateWifiConfigHistory, inputed arg is invalid, internetHistory = %{public}s", new Object[]{internetHistory});
                return;
            }
            String status = internetHistory.substring(0, 1);
            if (status != null && status.equals("0")) {
                int newStatus = respCode == 204 ? 1 : 2;
                String internetHistory2 = String.valueOf(newStatus) + "/" + internetHistory.substring(internetHistory.indexOf("/") + 1);
                WifiConfiguration wifiConfiguration2 = this.mCurrentWifiConfig;
                wifiConfiguration2.noInternetAccess = false;
                wifiConfiguration2.validatedInternetAccess = !wifiConfiguration2.noInternetAccess;
                if (newStatus == 1) {
                    WifiConfiguration wifiConfiguration3 = this.mCurrentWifiConfig;
                    wifiConfiguration3.numNoInternetAccessReports = 0;
                    wifiConfiguration3.lastHasInternetTimestamp = System.currentTimeMillis();
                }
                WifiConfiguration wifiConfiguration4 = this.mCurrentWifiConfig;
                if (wifiConfiguration4.portalNetwork || respCode != 204) {
                    z = true;
                }
                wifiConfiguration4.portalNetwork = z;
                this.mCurrentWifiConfig.internetHistory = internetHistory2;
                if (newStatus == 1) {
                    sendCheckResultWhenConnected(204, "huawei.conn.NETWORK_CONDITIONS_MEASURED", "extra_is_internet_ready", 1);
                }
                Intent intent = new Intent("com.huawei.wifipro.ACTION_UPDATE_CONFIG_HISTORY");
                intent.putExtra("new_wifi_config", new WifiConfiguration(this.mCurrentWifiConfig));
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER");
            } else if (status != null && status.equals("2") && respCode == 204) {
                this.mCurrentWifiConfig.lastHasInternetTimestamp = System.currentTimeMillis();
                this.mCurrentWifiConfig.portalCheckStatus = 0;
                Intent intent2 = new Intent("com.huawei.wifipro.ACTION_UPDATE_CONFIG_HISTORY");
                intent2.putExtra("new_wifi_config", new WifiConfiguration(this.mCurrentWifiConfig));
                this.mContext.sendBroadcastAsUser(intent2, UserHandle.ALL, "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePortalDetectionStatistics(boolean reconfirm, int currHttpCode, long currDetectMs, String server, int mainHttpCode, long mainHttpMs, WifiConfiguration config) {
        long currDetectMs2;
        long mainHttpMs2;
        if (!reconfirm && !this.mControllerNotified && config != null && server != null) {
            boolean mainServer = server.contains(SERVER_HICLOUD);
            if (!mainServer || this.mCheckingCounter >= this.mMaxAttempts || !WifiProCommonUtils.unreachableRespCode(currHttpCode)) {
                boolean firstConnected = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, (int) MSG_HTTP_RESP_RCVD);
                boolean currentPortalCode = WifiProCommonUtils.isRedirectedRespCodeByGoogle(currHttpCode);
                if (firstConnected || config.portalNetwork || currentPortalCode) {
                    if (currDetectMs <= 0) {
                        currDetectMs2 = -1;
                    } else {
                        currDetectMs2 = currDetectMs;
                    }
                    if (mainHttpMs <= 0) {
                        mainHttpMs2 = -1;
                    } else {
                        mainHttpMs2 = mainHttpMs;
                    }
                    int mainCode = mainServer ? currHttpCode : mainHttpCode;
                    int backupCode = mainServer ? -1 : currHttpCode;
                    long mainDetectMs = mainServer ? currDetectMs2 : mainHttpMs2;
                    long backupDetectMs = mainServer ? -1 : currDetectMs2;
                    this.mPortalDetectStatisticsInfo = (firstConnected ? 1 : 0) + "|" + (this.mInOversea ? 1 : 0) + "|" + mainCode + "|" + backupCode + "|" + mainDetectMs + "|" + backupDetectMs;
                    HwHiLog.d(TAG, false, "updatePortalDetectionStatistics, portalDetectStatisticsInfo = %{public}s", new Object[]{this.mPortalDetectStatisticsInfo});
                }
            }
        }
    }

    public int getRawHttpRespCode() {
        return this.mRawHttpRespCode;
    }

    public void setRawRedirectedHostName(String hostName) {
        HwHiLog.i(TAG, false, "setRawRedirectedHostName", new Object[0]);
        this.mRawRedirectedHostName = hostName;
    }

    public void resetCheckerStatus() {
        this.mCheckingCounter = 0;
        this.mCheckerInitialized = false;
        this.mInOversea = false;
        this.mControllerNotified = false;
        this.mIgnoreRxCounter = false;
        this.mLastDetectTimeout = false;
        synchronized (this.mRxCounterWaitLock) {
            this.rxCounterRespRcvd = false;
        }
    }

    public void release() {
        HwHiLog.i(TAG, false, "release, checking counter = %{public}d", new Object[]{Integer.valueOf(this.mCheckingCounter)});
        BroadcastReceiver broadcastReceiver = this.mBroadcastReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mBroadcastReceiver = null;
        }
        this.mNetworkDisconnected.set(true);
        this.mLastDetectTimeout = false;
    }
}
