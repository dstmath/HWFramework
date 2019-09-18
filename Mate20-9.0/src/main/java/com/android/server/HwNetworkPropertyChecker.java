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
import android.net.NetworkUtils;
import android.net.dns.ResolvUtil;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.wifipro.PortalDataBaseManager;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class HwNetworkPropertyChecker {
    private static final int BUFFER_SIZE = 8192;
    private static final String BUNDLE_FLAG_MAIN_DETECT_MS = "mainDetectMs";
    private static final String BUNDLE_FLAG_USED_SERVER = "usedServer";
    public static final String CHINA_MAINLAND_BACKUP_SERVER = "http://www.baidu.com";
    public static final String CHINA_MAINLAND_MAIN_SERVER = "http://connectivitycheck.platform.hicloud.com/generate_204";
    private static final int CONNECTION_TYPE_CLOSE_10 = 10;
    private static final int CONNECTION_TYPE_CLOSE_11 = 11;
    private static final int CONNECTION_TYPE_CLOSE_20 = 20;
    private static final int CONNECTION_TYPE_CLOSE_21 = 21;
    private static final int CONNECTION_TYPE_CLOSE_30 = 30;
    private static final int CONNECTION_TYPE_CLOSE_31 = 31;
    private static final int CONNECTION_TYPE_KEEPALIVE_40 = 40;
    private static final int CONNECTION_TYPE_KEEPALIVE_41 = 41;
    private static final int CONNECTION_TYPE_NULL_50 = 50;
    private static final int CONNECTION_TYPE_NULL_51 = 51;
    private static final int CONNECTION_TYPE_OTHERS_60 = 60;
    private static final int CONNECTION_TYPE_OTHERS_61 = 61;
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.32 Safari/537.36";
    private static final boolean DISABLE_BQE_SERVICE = false;
    public static final String HICLOUD_HOST_NAME = "http://connectivitycheck.platform.hicloud.com";
    private static final String HTML_TITLE_BAIDU_CN = "°Ù¶ÈÒ»ÏÂ";
    private static final String HTML_TITLE_BAIDU_EN = "baidu";
    private static final String HTML_TITLE_HTTPS_EN = "https://";
    private static final String HTML_TITLE_HTTP_EN = "http://";
    public static final int HW_DEFAULT_REEVALUATE_DELAY_MS = 500;
    public static final int HW_MAX_RETRIES = 3;
    private static final String KEY_WORDS_REDIRECTION = "location.replace";
    private static final String KEY_WORDS_TITLE_END = "</title>";
    private static final String KEY_WORDS_TITLE_START = "<title>";
    private static final int LAC_UNKNOWN = -1;
    public static final int MIN_RX_PKT_DELTA = 3;
    private static final int MSG_DNS_RESP_RCVD = 104;
    private static final int MSG_HTML_DOWNLOADED_RCVD = 105;
    private static final int MSG_HTTP_RESP_RCVD = 103;
    private static final int MSG_HTTP_RESP_TIMEOUT = 101;
    private static final int MSG_NETWORK_DISCONNECTED = 102;
    public static final int NETWORK_PROPERTY_INTERNET = 5;
    public static final int NETWORK_PROPERTY_NO_INTERNET = -1;
    public static final int NETWORK_PROPERTY_PORTAL = 6;
    public static final String OPERATOR_COUNTRY_KEY = "WIFI_PRO_OPERATOR_COUNTRY_KEY";
    public static final String OVERSEA_BACKUP_SERVER = "http://connectivitycheck.gstatic.com/generate_204";
    public static final String OVERSEA_MAIN_SERVER = "http://connectivitycheck.platform.hicloud.com/generate_204";
    private static final String PRODUCT_LOCALE_CN = "CN";
    public static final String SERVER_HICLOUD = "hicloud";
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final int STRING_BUFFER_SIZE = 2097152;
    public static final String TAG = "HwNetworkPropertyChecker";
    private static final String URL_HTTPS_BAIDU = "https://www.baidu.com";
    private static final int WAIT_TIMEOUT_MS = 6000;
    /* access modifiers changed from: private */
    public boolean htmlDownloaded = false;
    /* access modifiers changed from: private */
    public boolean httpRespCodeRcvd = true;
    private String mBackupServer;
    private BroadcastReceiver mBroadcastReceiver;
    private CellLocation mCellLocation;
    /* access modifiers changed from: private */
    public boolean mCheckerInitialized;
    /* access modifiers changed from: private */
    public int mCheckingCounter;
    private ContentResolver mContentResolver;
    protected Context mContext;
    private boolean mControllerNotified;
    /* access modifiers changed from: private */
    public WifiConfiguration mCurrentWifiConfig;
    /* access modifiers changed from: private */
    public int mDnsCheckResult;
    private String mGatewayAddress;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public int mHtmlCheckResult = WifiProCommonUtils.HTTP_UNREACHALBE;
    /* access modifiers changed from: private */
    public Object mHtmlDownloadWaitingLock = new Object();
    /* access modifiers changed from: private */
    public String mHttpRespConnection = null;
    /* access modifiers changed from: private */
    public Object mHttpRespWaitingLock = new Object();
    /* access modifiers changed from: private */
    public int mHttpResponseCode;
    protected boolean mIgnoreRxCounter;
    /* access modifiers changed from: private */
    public boolean mInOversea;
    private IntentFilter mIntentFilter;
    /* access modifiers changed from: private */
    public boolean mLastDetectTimeout = false;
    private String mMainServer;
    /* access modifiers changed from: private */
    public int mMaxAttempts;
    /* access modifiers changed from: private */
    public boolean mMobileHotspot;
    /* access modifiers changed from: private */
    public Network mNetwork;
    private NetworkAgentInfo mNetworkAgentInfo;
    /* access modifiers changed from: private */
    public AtomicBoolean mNetworkDisconnected = new AtomicBoolean(true);
    /* access modifiers changed from: private */
    public int mNetworkTypeByWebView;
    private String mPortalDetectStatisticsInfo = null;
    /* access modifiers changed from: private */
    public String mPortalRedirectedUrl = null;
    /* access modifiers changed from: private */
    public int mRawHttpRespCode;
    /* access modifiers changed from: private */
    public String mRawRedirectedHostName;
    /* access modifiers changed from: private */
    public long mRequestTimestamp;
    /* access modifiers changed from: private */
    public long mResponseTimestamp;
    protected Object mRxCounterWaitLock = new Object();
    protected int mTcpRxCounter;
    protected int mTcpRxFirstCounter;
    protected int mTcpRxSecondCounter;
    private TelephonyManager mTelManager;
    private BroadcastReceiver mTempReceiverWebView;
    /* access modifiers changed from: private */
    public String mUsedServer = null;
    /* access modifiers changed from: private */
    public Object mWebViewWaitingLock = new Object();
    protected WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public boolean rxCounterRespRcvd;
    /* access modifiers changed from: private */
    public volatile boolean webviewRespRcvd = false;

    private class NetworkCheckerThread extends Thread {
        public static final String SERVER_BAIDU = "baidu";
        public static final String SERVER_BING = "bing";
        public static final String SERVER_YOUTUBE = "youtube";
        public static final String TYPE_HTTPS = "https";
        private final String[] INVALID_REDIRECTED_HOST_1 = {"yuzua", "huayaochou", "freewifi.360.cn", "free.wifi.360.cn", "wifi.shouji.360.cn"};
        private final String[] INVALID_REDIRECTED_HOST_2 = {"miwifi"};
        private String gateWayAddr;
        private boolean isMainServer;
        private String mChckingServer;
        private long mainDetectMs = -1;
        private int mainHttpCode = -1;
        private boolean reconfirm;
        private HttpURLConnection urlConnection;

        public NetworkCheckerThread(String server, boolean mainServer, boolean reconfirm2, int mainHttpCode2, long mainDetectMs2, String gw) {
            this.mChckingServer = server;
            this.isMainServer = mainServer;
            this.urlConnection = null;
            this.reconfirm = reconfirm2;
            this.gateWayAddr = gw;
            this.mainHttpCode = mainHttpCode2;
            this.mainDetectMs = mainDetectMs2;
        }

        public void run() {
            String urlServer;
            int httpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
            Network network = HwNetworkPropertyChecker.this.mNetwork;
            if (network == null) {
                HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, mNetwork == null");
                return;
            }
            InputStream inputStream = null;
            try {
                String urlServer2 = this.mChckingServer;
                if (urlServer2.contains(HwNetworkPropertyChecker.HICLOUD_HOST_NAME)) {
                    urlServer = urlServer2 + "_" + UUID.randomUUID().toString();
                } else {
                    urlServer = urlServer2;
                }
                URLConnection connection = network.openConnection(new URL(urlServer));
                if (!(connection instanceof HttpURLConnection)) {
                    HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, openConnection doesn't return HttpURLConnection instance.");
                    if (this.urlConnection != null) {
                        this.urlConnection.disconnect();
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            IOException iOException = e;
                            HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, IOException of close, msg receive ");
                        } catch (UncheckedIOException e2) {
                            UncheckedIOException uncheckedIOException = e2;
                            HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, UncheckedIOException of close, msg receive ");
                        }
                    }
                    return;
                }
                this.urlConnection = (HttpURLConnection) connection;
                this.urlConnection.setInstanceFollowRedirects(false);
                this.urlConnection.setConnectTimeout(10000);
                this.urlConnection.setReadTimeout(10000);
                this.urlConnection.setUseCaches(false);
                this.urlConnection.setRequestProperty("User-Agent", HwNetworkPropertyChecker.DEFAULT_USER_AGENT);
                long unused = HwNetworkPropertyChecker.this.mRequestTimestamp = SystemClock.elapsedRealtime();
                int httpResponseCode2 = this.urlConnection.getResponseCode();
                long unused2 = HwNetworkPropertyChecker.this.mResponseTimestamp = SystemClock.elapsedRealtime();
                if (httpResponseCode2 == 200) {
                    String unused3 = HwNetworkPropertyChecker.this.mHttpRespConnection = this.urlConnection.getHeaderField("Connection");
                }
                if ("http://connectivitycheck.platform.hicloud.com/generate_204".equals(this.mChckingServer) && httpResponseCode2 == 204) {
                    String requestId = this.urlConnection.getHeaderField("X-Hwcloud-ReqId");
                    if (requestId == null || requestId.length() != 32) {
                        HwNetworkPropertyChecker.this.LOGW("http return 204, but request id error and unreachable!");
                        httpResponseCode2 = WifiProCommonUtils.HTTP_UNREACHALBE;
                    }
                }
                httpResponseCode = recomfirmResponseCode(httpResponseCode2, this.mChckingServer);
                if (this.mChckingServer.equals(HwNetworkPropertyChecker.OVERSEA_BACKUP_SERVER) && httpResponseCode == 200 && this.urlConnection.getContentLength() == 0) {
                    HwNetworkPropertyChecker.this.LOGW("NetworkCheckerThread, reset for clients3.google.com, respCode = " + httpResponseCode);
                    httpResponseCode = 204;
                }
                if (httpResponseCode >= 400 && httpResponseCode <= 499) {
                    inputStream = this.urlConnection.getErrorStream();
                    if (inputStream != null) {
                        String urlContent = (String) new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining(System.lineSeparator()));
                        if ((urlContent.contains(HwNetworkPropertyChecker.HTML_TITLE_HTTP_EN) || urlContent.contains(HwNetworkPropertyChecker.HTML_TITLE_HTTPS_EN)) && urlContent.contains(HwNetworkPropertyChecker.KEY_WORDS_REDIRECTION)) {
                            HwNetworkPropertyChecker.this.LOGW("http return " + httpResponseCode + ", reset for the url in the content of response, respCode = " + 302);
                            httpResponseCode = 302;
                        }
                    }
                }
                if (this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        IOException iOException2 = e3;
                        HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, IOException of close, msg receive ");
                    } catch (UncheckedIOException e4) {
                        UncheckedIOException uncheckedIOException2 = e4;
                        HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, UncheckedIOException of close, msg receive ");
                    }
                }
                int unused4 = HwNetworkPropertyChecker.this.mHttpResponseCode = httpResponseCode;
                HwNetworkPropertyChecker.this.updatePortalDetectionStatistics(this.reconfirm, httpResponseCode, HwNetworkPropertyChecker.this.mResponseTimestamp - HwNetworkPropertyChecker.this.mRequestTimestamp, this.mChckingServer, this.mainHttpCode, this.mainDetectMs, HwNetworkPropertyChecker.this.mCurrentWifiConfig);
                if (!HwNetworkPropertyChecker.this.mNetworkDisconnected.get()) {
                    HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, 103, 0, 0));
                }
            } catch (IOException e5) {
                String msg = e5.getMessage();
                HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, IOException, mainServer = " + this.isMainServer + ", Oversea = " + HwNetworkPropertyChecker.this.mInOversea);
                if (msg != null && msg.contains("ECONNREFUSED") && !WifiProCommonUtils.isOpenType(HwNetworkPropertyChecker.this.mCurrentWifiConfig) && HwNetworkPropertyChecker.this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(HwNetworkPropertyChecker.this.mCurrentWifiConfig.internetHistory, 100)) {
                    httpResponseCode = 204;
                } else if (msg != null && msg.contains("ECONNRESET") && !WifiProCommonUtils.isOpenType(HwNetworkPropertyChecker.this.mCurrentWifiConfig) && HwNetworkPropertyChecker.this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(HwNetworkPropertyChecker.this.mCurrentWifiConfig.internetHistory, 100)) {
                    httpResponseCode = 204;
                } else if (msg != null && (msg.contains("ENETUNREACH") || msg.contains("ENONET") || msg.contains("Unable to resolve host"))) {
                    HwNetworkPropertyChecker.this.mIgnoreRxCounter = true;
                }
                if (!(httpResponseCode == 204 || msg == null)) {
                    if (!msg.contains("ECONNRESET")) {
                        if (!msg.contains("Connection reset")) {
                            if (msg.contains("ECONNREFUSED") || msg.contains("unexpected end")) {
                                httpResponseCode = 601;
                            }
                        }
                    }
                    httpResponseCode = WifiProCommonUtils.RESP_CODE_CONN_RESET;
                }
                if (this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (UncheckedIOException e6) {
                HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, UncheckedIOException ue");
                if (this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (SecurityException e7) {
                HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, SecurityException se");
                if (this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                Throwable th2 = th;
                if (this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                        IOException iOException3 = e8;
                        HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, IOException of close, msg receive ");
                    } catch (UncheckedIOException e9) {
                        UncheckedIOException uncheckedIOException3 = e9;
                        HwNetworkPropertyChecker.this.LOGD("NetworkCheckerThread, UncheckedIOException of close, msg receive ");
                    }
                }
                throw th2;
            }
        }

        private boolean isInvalidRedirectedHost(String host, String[] servers) {
            if (!(host == null || servers == null)) {
                for (String contains : servers) {
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
                HwNetworkPropertyChecker hwNetworkPropertyChecker = HwNetworkPropertyChecker.this;
                hwNetworkPropertyChecker.LOGW("recomfirmResponseCode, host = " + host + ", location = " + newLocation + ", gateway = " + this.gateWayAddr + ",respCode =" + respCode);
                if (!(host == null || newLocation == null || HwNetworkPropertyChecker.this.mCurrentWifiConfig == null)) {
                    if (!this.reconfirm) {
                        String unused = HwNetworkPropertyChecker.this.mRawRedirectedHostName = host;
                    }
                    String unused2 = HwNetworkPropertyChecker.this.mPortalRedirectedUrl = newLocation;
                    if (HwNetworkPropertyChecker.CHINA_MAINLAND_BACKUP_SERVER.equals(server) && host.contains("baidu")) {
                        if (host.startsWith(TYPE_HTTPS)) {
                            return 204;
                        }
                        return WifiProCommonUtils.HTTP_UNREACHALBE;
                    }
                }
                if (!HwNetworkPropertyChecker.this.mNetworkDisconnected.get() && !TextUtils.isEmpty(HwNetworkPropertyChecker.this.mRawRedirectedHostName) && !TextUtils.isEmpty(host) && !HwNetworkPropertyChecker.this.mRawRedirectedHostName.equals(host) && WifiProCommonUtils.isOpenType(HwNetworkPropertyChecker.this.mCurrentWifiConfig)) {
                    return WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED;
                }
            }
            return respCode;
        }
    }

    private static class OneAddressPerFamilyNetwork extends Network {
        public OneAddressPerFamilyNetwork(Network network) {
            super(network);
        }

        public InetAddress[] getAllByName(String host) throws UnknownHostException {
            List<InetAddress> addrs = Arrays.asList(ResolvUtil.blockingResolveAllLocally(this, host));
            LinkedHashMap<Class, InetAddress> addressByFamily = new LinkedHashMap<>();
            addressByFamily.put(addrs.get(0).getClass(), addrs.get(0));
            Collections.shuffle(addrs);
            for (InetAddress addr : addrs) {
                addressByFamily.put(addr.getClass(), addr);
            }
            return (InetAddress[]) addressByFamily.values().toArray(new InetAddress[addressByFamily.size()]);
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
        this.mMainServer = "http://connectivitycheck.platform.hicloud.com/generate_204";
        this.mBackupServer = CHINA_MAINLAND_BACKUP_SERVER;
        this.mInOversea = false;
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
            this.mWifiManager = (WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
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
                int i = 0;
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && NetworkInfo.DetailedState.DISCONNECTED == info.getDetailedState() && !HwNetworkPropertyChecker.this.mNetworkDisconnected.get()) {
                        HwNetworkPropertyChecker.this.LOGD("NETWORK_STATE_CHANGED_ACTION, network is connected --> disconnected.");
                        HwNetworkPropertyChecker.this.mNetworkDisconnected.set(true);
                        HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, 102, 0, 0));
                    }
                } else if (WifiProCommonDefs.ACTION_RESPONSE_TCP_RX_COUNTER.equals(intent.getAction())) {
                    int rx = intent.getIntExtra(WifiProCommonDefs.EXTRA_FLAG_TCP_RX_COUNTER, 0);
                    HwNetworkPropertyChecker hwNetworkPropertyChecker = HwNetworkPropertyChecker.this;
                    if (rx > 0) {
                        i = rx;
                    }
                    hwNetworkPropertyChecker.mTcpRxCounter = i;
                    synchronized (HwNetworkPropertyChecker.this.mRxCounterWaitLock) {
                        boolean unused = HwNetworkPropertyChecker.this.rxCounterRespRcvd = true;
                        HwNetworkPropertyChecker.this.mRxCounterWaitLock.notifyAll();
                    }
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, WifiProCommonDefs.NETWORK_CHECKER_RECV_PERMISSION, null);
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                Message message = msg;
                boolean reconfirmDetect = false;
                switch (message.what) {
                    case 101:
                        int unused = HwNetworkPropertyChecker.this.mHttpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
                        if (message.arg1 == 1) {
                            reconfirmDetect = true;
                        }
                        if (!reconfirmDetect && HwNetworkPropertyChecker.this.mCheckingCounter < HwNetworkPropertyChecker.this.mMaxAttempts) {
                            boolean unused2 = HwNetworkPropertyChecker.this.mLastDetectTimeout = true;
                        }
                        int mainHttpCode = HwNetworkPropertyChecker.this.mLastDetectTimeout ? 600 : message.arg2;
                        Bundle bundle = (Bundle) message.obj;
                        HwNetworkPropertyChecker.this.updatePortalDetectionStatistics(reconfirmDetect, 600, -1, bundle.getString(HwNetworkPropertyChecker.BUNDLE_FLAG_USED_SERVER), mainHttpCode, bundle.getLong(HwNetworkPropertyChecker.BUNDLE_FLAG_MAIN_DETECT_MS), HwNetworkPropertyChecker.this.mCurrentWifiConfig);
                        if (reconfirmDetect) {
                            int unused3 = HwNetworkPropertyChecker.this.mHttpResponseCode = 600;
                        }
                        synchronized (HwNetworkPropertyChecker.this.mHttpRespWaitingLock) {
                            boolean unused4 = HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                        }
                        break;
                    case 102:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(101)) {
                            HwNetworkPropertyChecker.this.LOGD("MSG_HTTP_RESP_TIMEOUT msg removed because of disconnected.");
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(101);
                        }
                        int unused5 = HwNetworkPropertyChecker.this.mHttpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
                        int unused6 = HwNetworkPropertyChecker.this.mRawHttpRespCode = WifiProCommonUtils.HTTP_UNREACHALBE;
                        boolean unused7 = HwNetworkPropertyChecker.this.mMobileHotspot = false;
                        boolean unused8 = HwNetworkPropertyChecker.this.mCheckerInitialized = false;
                        String unused9 = HwNetworkPropertyChecker.this.mRawRedirectedHostName = null;
                        String unused10 = HwNetworkPropertyChecker.this.mPortalRedirectedUrl = null;
                        String unused11 = HwNetworkPropertyChecker.this.mUsedServer = null;
                        synchronized (HwNetworkPropertyChecker.this.mHttpRespWaitingLock) {
                            boolean unused12 = HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                        }
                        break;
                    case 103:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(101)) {
                            HwNetworkPropertyChecker.this.LOGD("MSG_HTTP_RESP_TIMEOUT msg removed because of HTTP response received.");
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(101);
                        }
                        synchronized (HwNetworkPropertyChecker.this.mHttpRespWaitingLock) {
                            boolean unused13 = HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                        }
                        break;
                    case 104:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(101)) {
                            HwNetworkPropertyChecker.this.LOGD("MSG_HTTP_RESP_TIMEOUT msg removed because of DNS response received.");
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(101);
                        }
                        int unused14 = HwNetworkPropertyChecker.this.mDnsCheckResult = message.arg1;
                        synchronized (HwNetworkPropertyChecker.this.mHttpRespWaitingLock) {
                            boolean unused15 = HwNetworkPropertyChecker.this.httpRespCodeRcvd = true;
                            HwNetworkPropertyChecker.this.mHttpRespWaitingLock.notifyAll();
                        }
                        break;
                    case 105:
                        if (HwNetworkPropertyChecker.this.mHandler.hasMessages(105)) {
                            HwNetworkPropertyChecker.this.LOGD("MSG_HTML_DOWNLOADED_RCVD msg removed because of html downloaded.");
                            HwNetworkPropertyChecker.this.mHandler.removeMessages(105);
                        }
                        int unused16 = HwNetworkPropertyChecker.this.mHtmlCheckResult = message.arg1;
                        synchronized (HwNetworkPropertyChecker.this.mHtmlDownloadWaitingLock) {
                            boolean unused17 = HwNetworkPropertyChecker.this.htmlDownloaded = true;
                            HwNetworkPropertyChecker.this.mHtmlDownloadWaitingLock.notifyAll();
                        }
                        break;
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
                    int unused = HwNetworkPropertyChecker.this.mNetworkTypeByWebView = intent.getIntExtra(WifiProCommonDefs.EXTRA_FLAG_NETWORK_TYPE, 100);
                    synchronized (HwNetworkPropertyChecker.this.mWebViewWaitingLock) {
                        boolean unused2 = HwNetworkPropertyChecker.this.webviewRespRcvd = true;
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
                    WifiConfiguration config = configNetworks.get(i);
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
                this.mWifiManager = (WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
            }
            if (this.mTelManager == null) {
                this.mTelManager = (TelephonyManager) this.mContext.getSystemService("phone");
            }
            if (this.mNetwork == null) {
                if (this.mNetworkAgentInfo == null || this.mNetworkAgentInfo.network == null) {
                    Network activeNetwork = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
                    if (activeNetwork != null) {
                        this.mNetwork = new OneAddressPerFamilyNetwork(activeNetwork);
                    }
                } else {
                    this.mNetwork = new OneAddressPerFamilyNetwork(this.mNetworkAgentInfo.network);
                }
            }
            initNetworkInfo();
            initCurrWifiConfig();
            String operator = this.mTelManager.getNetworkOperator();
            if (operator == null || operator.length() == 0) {
                if (!PRODUCT_LOCALE_CN.equalsIgnoreCase(WifiProCommonUtils.getProductLocale())) {
                    LOGD("initialize, can't get network operator!");
                    this.mMainServer = "http://connectivitycheck.platform.hicloud.com/generate_204";
                    this.mBackupServer = OVERSEA_BACKUP_SERVER;
                    this.mInOversea = true;
                }
            } else if (!operator.startsWith(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                this.mMainServer = "http://connectivitycheck.platform.hicloud.com/generate_204";
                this.mBackupServer = OVERSEA_BACKUP_SERVER;
                this.mInOversea = true;
            }
            this.mMobileHotspot = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContext);
            this.mCheckerInitialized = true;
            if (this.mCurrentWifiConfig != null) {
                LOGD("initialize, AP's network history = " + this.mCurrentWifiConfig.internetHistory + ", operator = " + operator + ", mInOversea = " + this.mInOversea);
            }
        }
        if (reconfirm) {
            this.mNetwork = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
            initNetworkInfo();
            initCurrWifiConfig();
        }
        this.mIgnoreRxCounter = false;
    }

    private void sendCheckResultWhenConnected(int finalRespCode, String action, String flag, int property) {
        Intent intent = new Intent(action);
        intent.setFlags(67108864);
        intent.putExtra(flag, property);
        if (WifiProCommonDefs.EXTRA_FLAG_NETWORK_PROPERTY.equals(flag)) {
            if (property == 6) {
                if (!TextUtils.isEmpty(this.mRawRedirectedHostName)) {
                    intent.putExtra(WifiProCommonDefs.EXTRA_RAW_REDIRECTED_HOST, this.mRawRedirectedHostName);
                }
                boolean standardPortal = false;
                if (isPortalSpecifiedServer(this.mUsedServer) || !WifiProCommonUtils.httpReachableHome(this.mRawHttpRespCode)) {
                    standardPortal = true;
                }
                intent.putExtra(WifiProCommonDefs.EXTRA_STANDARD_PORTAL_NETWORK, standardPortal);
            }
            if (this.mPortalDetectStatisticsInfo != null) {
                intent.putExtra(WifiProCommonUtils.KEY_PORTAL_DETECT_STAT_INFO, this.mPortalDetectStatisticsInfo);
            }
            boolean firstDetected = this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, 103);
            intent.putExtra(WifiProCommonUtils.KEY_PORTAL_HTTP_RESP_CODE, finalRespCode);
            intent.putExtra(WifiProCommonUtils.KEY_PORTAL_FIRST_DETECT, firstDetected);
            intent.putExtra(WifiProCommonUtils.KEY_PORTAL_REDIRECTED_URL, this.mPortalRedirectedUrl);
            intent.putExtra(WifiProCommonUtils.KEY_PORTAL_CONFIG_KEY, this.mCurrentWifiConfig != null ? this.mCurrentWifiConfig.configKey() : "");
        }
        this.mContext.sendBroadcast(intent, WifiProCommonDefs.NETWORK_CHECKER_RECV_PERMISSION);
    }

    public boolean isNetworkChanged() {
        WifiConfiguration currConfig = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        if (!(currConfig == null || this.mCurrentWifiConfig == null || this.mNetworkDisconnected.get())) {
            LOGW("isNetworkChanged, current ssid = " + currConfig.configKey() + ", checking ssid = " + this.mCurrentWifiConfig.configKey());
            if (this.mCurrentWifiConfig.configKey() != null && this.mCurrentWifiConfig.configKey().equals(currConfig.configKey())) {
                return false;
            }
        }
        return true;
    }

    public boolean isInOversea() {
        return this.mInOversea;
    }

    public boolean isCheckingCompleted(int finalRespCode) {
        boolean ret = false;
        if (WifiProCommonUtils.httpReachableOrRedirected(finalRespCode)) {
            ret = true;
            if (!this.mControllerNotified && !isNetworkChanged()) {
                this.mControllerNotified = true;
                int property = finalRespCode == 204 ? 5 : 6;
                sendCheckResultWhenConnected(finalRespCode, WifiProCommonDefs.ACTION_NETWOR_PROPERTY_NOTIFICATION, WifiProCommonDefs.EXTRA_FLAG_NETWORK_PROPERTY, property);
                if (property == 5 && this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, 102)) {
                    property = 6;
                }
                sendCheckResultWhenConnected(finalRespCode, WifiProCommonDefs.ACTION_NETWORK_CONDITIONS_MEASURED, WifiProCommonDefs.EXTRA_IS_INTERNET_READY, property);
            }
            this.mLastDetectTimeout = false;
        } else if (this.mCheckingCounter >= this.mMaxAttempts) {
            ret = true;
            if (!this.mControllerNotified && !isNetworkChanged()) {
                this.mControllerNotified = true;
                sendCheckResultWhenConnected(finalRespCode, WifiProCommonDefs.ACTION_NETWOR_PROPERTY_NOTIFICATION, WifiProCommonDefs.EXTRA_FLAG_NETWORK_PROPERTY, -1);
                sendCheckResultWhenConnected(finalRespCode, WifiProCommonDefs.ACTION_NETWORK_CONDITIONS_MEASURED, WifiProCommonDefs.EXTRA_IS_INTERNET_READY, -1);
            }
            this.mLastDetectTimeout = false;
        } else if (this.mCheckingCounter == 1) {
            LOGD("sendbroadcast :WifiProCommonDefs.ACTION_FIRST_CHECK_NO_INTERNET_NOTIFICATION");
            Intent intent = new Intent(WifiProCommonDefs.ACTION_FIRST_CHECK_NO_INTERNET_NOTIFICATION);
            intent.setFlags(67108864);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
        return ret;
    }

    private int syncCheckNetworkProperty(String server, boolean mainServer, boolean reconfirm, int mainHttpCode, long mainDetectMs) {
        boolean z = reconfirm;
        this.mHttpResponseCode = WifiProCommonUtils.HTTP_UNREACHALBE;
        if (!this.mNetworkDisconnected.get()) {
            NetworkCheckerThread networkCheckerThread = new NetworkCheckerThread(server, mainServer, z, mainHttpCode, mainDetectMs, this.mGatewayAddress);
            int timeout = mainServer ? 10000 : WAIT_TIMEOUT_MS;
            if (!z && this.mCheckingCounter == 1) {
                timeout = WAIT_TIMEOUT_MS;
            }
            int timeout2 = timeout;
            synchronized (this.mHttpRespWaitingLock) {
                try {
                    this.httpRespCodeRcvd = false;
                    Bundle bundle = new Bundle();
                    try {
                        bundle.putLong(BUNDLE_FLAG_MAIN_DETECT_MS, mainDetectMs);
                        try {
                            bundle.putString(BUNDLE_FLAG_USED_SERVER, server);
                            try {
                                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 101, z ? 1 : 0, mainHttpCode, bundle), (long) timeout2);
                                networkCheckerThread.start();
                                while (!this.httpRespCodeRcvd) {
                                    this.mHttpRespWaitingLock.wait();
                                }
                                LOGD("syncCheckNetworkProperty, Thread exited or timeout, http resp code = " + this.mHttpResponseCode);
                            } catch (InterruptedException e) {
                            }
                        } catch (InterruptedException e2) {
                            int i = mainHttpCode;
                            this.mRawHttpRespCode = this.mHttpResponseCode;
                            return this.mHttpResponseCode;
                        } catch (Throwable th) {
                            th = th;
                            int i2 = mainHttpCode;
                            throw th;
                        }
                    } catch (InterruptedException e3) {
                        String str = server;
                        int i3 = mainHttpCode;
                        this.mRawHttpRespCode = this.mHttpResponseCode;
                        return this.mHttpResponseCode;
                    } catch (Throwable th2) {
                        th = th2;
                        String str2 = server;
                        int i22 = mainHttpCode;
                        throw th;
                    }
                    try {
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                } catch (InterruptedException e4) {
                    String str3 = server;
                    int i4 = mainHttpCode;
                    long j = mainDetectMs;
                } catch (Throwable th4) {
                    th = th4;
                    String str4 = server;
                    int i5 = mainHttpCode;
                    long j2 = mainDetectMs;
                    throw th;
                }
            }
        } else {
            String str5 = server;
            int i6 = mainHttpCode;
            long j3 = mainDetectMs;
        }
        this.mRawHttpRespCode = this.mHttpResponseCode;
        return this.mHttpResponseCode;
    }

    public int requestTcpRxPacketsCounter() {
        int rxCounter = 0;
        if (!WifiProCommonUtils.isWifiProPropertyEnabled(this.mContext)) {
            LOGD("requestTcpRxPacketsCounter: Product don't support wifi+, skip Tcp Rx Packets Counter, rx = 0");
            return 0;
        }
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

    public int recomfirmByRxCounter(int httpResponseCode, boolean reconfirm) {
        if (!reconfirm && WifiProCommonUtils.unreachableRespCode(httpResponseCode) && this.mCheckingCounter == this.mMaxAttempts && !this.mNetworkDisconnected.get() && !this.mIgnoreRxCounter && isNetworkPoorRssi()) {
            LOGD("recomfirmByRxCounter, firstRx = " + this.mTcpRxFirstCounter + ", secondRx = " + this.mTcpRxSecondCounter);
            if (this.mTcpRxSecondCounter - this.mTcpRxFirstCounter >= 3 && this.mTcpRxFirstCounter >= 0) {
                httpResponseCode = 204;
                if (this.mCurrentWifiConfig != null && WifiProCommonUtils.isOpenAndPortal(this.mCurrentWifiConfig)) {
                    httpResponseCode = 302;
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
        this.mCheckingCounter = this.mCheckingCounter + 1;
        if (!this.mNetworkDisconnected.get()) {
            this.mPortalDetectStatisticsInfo = null;
            this.mHttpRespConnection = null;
            httpResponseCode = transformResponseCode(recomfirmByRxCounter(recomfirmResponseCode(this.mUsedServer, getHttpRespCode(reconfirm, this.mCheckingCounter, portalNetwork, wifiBackground), reconfirm), reconfirm));
            updateWifiConfigHistory(httpResponseCode, reconfirm);
        }
        LOGD("=====LEAVE: ===isCaptivePortal, httpResponseCode = " + httpResponseCode + ", ovs= " + this.mInOversea + ", reconfirm = " + reconfirm);
        return httpResponseCode;
    }

    private int getHttpRespCode(boolean reconfirm, int checkingCounter, boolean portalNetwork, boolean wifiBackground) {
        boolean z = reconfirm;
        this.mUsedServer = this.mMainServer;
        if (z) {
            int i = checkingCounter;
        } else if (checkingCounter == 3 && !this.mControllerNotified) {
            LOGD("getHttpRespCode, http unreachable, use the backup server to check for 3rd time.");
            this.mUsedServer = this.mBackupServer;
        }
        int httpResponseCode = syncCheckNetworkProperty(this.mUsedServer, true, z, WifiProCommonUtils.HTTP_UNREACHALBE, -1);
        updateStarndardPortalRecord(httpResponseCode, z);
        if (!z || !WifiProCommonUtils.unreachableRespCode(httpResponseCode)) {
            return httpResponseCode;
        }
        if (this.mInOversea || wifiBackground || !portalNetwork || isStandardPortal()) {
            LOGD("getHttpRespCode, http unreachable, use the backup server to recheck again.");
            this.mUsedServer = this.mBackupServer;
            return syncCheckNetworkProperty(this.mUsedServer, false, z, httpResponseCode, -1);
        }
        LOGD("getHttpRespCode, http unreachable, use the backup server(g) to recheck again.");
        this.mUsedServer = OVERSEA_BACKUP_SERVER;
        return syncCheckNetworkProperty(this.mUsedServer, false, z, httpResponseCode, -1);
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
        if (!reconfirm && WifiProCommonUtils.isRedirectedRespCode(respCode) && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig)) {
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

    private boolean isStandardPortal() {
        PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
        if (!(database == null || this.mCurrentWifiConfig == null)) {
            StarndardPortalInfo portalInfo = new StarndardPortalInfo();
            portalInfo.currentSsid = this.mCurrentWifiConfig.SSID;
            database.syncQueryStarndardPortalNetwork(portalInfo);
            long timestamp = portalInfo.timestamp;
            if (timestamp > 0 && System.currentTimeMillis() - timestamp <= 14400000) {
                LOGD("isStandardPortal return true");
                return true;
            }
        }
        LOGD("isStandardPortal return false");
        return false;
    }

    private boolean isStarndardPortalAuthenCompleted(int respCode) {
        PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
        if (!(database == null || this.mCurrentWifiConfig == null || !WifiProCommonUtils.httpReachableHome(respCode))) {
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

    private int transformResponseCode(int respCode) {
        int httpResponseCode = respCode;
        if (respCode == 204 || WifiProCommonUtils.unreachableRespCode(respCode)) {
            return httpResponseCode;
        }
        if (isPortalSpecifiedServer(this.mUsedServer)) {
            if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(respCode)) {
                httpResponseCode = 302;
            }
        } else if (WifiProCommonUtils.httpReachableOrRedirected(respCode) && !WifiProCommonUtils.isRedirectedRespCode(respCode)) {
            httpResponseCode = 204;
        }
        return httpResponseCode;
    }

    public int syncRecheckBasedOnWebView(boolean oversea) {
        LOGD("syncRecheckBasedOnWebView:: begin, oversea = " + oversea);
        if (!WifiProCommonUtils.isWifiProPropertyEnabled(this.mContext)) {
            LOGD("syncRecheckBasedOnWebView:: Product don't support wifi+, skip webview check, network type = 100!");
            return 100;
        }
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

    public int syncCheckDnsResponse() {
        this.mDnsCheckResult = -1;
        synchronized (this.mHttpRespWaitingLock) {
            try {
                new Thread(new Runnable() {
                    public void run() {
                        Network unused = HwNetworkPropertyChecker.this.mNetwork = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
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
                this.httpRespCodeRcvd = false;
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

    public int syncCheckHtmlContent(final String checkUrl) {
        this.mHtmlCheckResult = WifiProCommonUtils.HTTP_UNREACHALBE;
        if (checkUrl == null || this.mNetworkDisconnected.get()) {
            LOGD("wifi disconnected, syncCheckHtmlContent return 599.");
            return this.mHtmlCheckResult;
        }
        synchronized (this.mHtmlDownloadWaitingLock) {
            this.htmlDownloaded = false;
            try {
                new Thread(new Runnable() {
                    public void run() {
                        String downloadHtml = HwNetworkPropertyChecker.this.downloadPortalWebHtml(checkUrl);
                        if (downloadHtml != null) {
                            String htmlTitle = null;
                            int start = downloadHtml.indexOf(HwNetworkPropertyChecker.KEY_WORDS_TITLE_START, 0);
                            if (start != -1) {
                                int end = downloadHtml.indexOf(HwNetworkPropertyChecker.KEY_WORDS_TITLE_END, start);
                                if (end != -1 && HwNetworkPropertyChecker.KEY_WORDS_TITLE_END.length() + end < downloadHtml.length()) {
                                    htmlTitle = downloadHtml.substring(HwNetworkPropertyChecker.KEY_WORDS_TITLE_START.length() + start, end);
                                }
                            }
                            if (htmlTitle != null && (htmlTitle.contains(HwNetworkPropertyChecker.HTML_TITLE_BAIDU_CN) || htmlTitle.toLowerCase(Locale.ENGLISH).contains("baidu"))) {
                                HwNetworkPropertyChecker.this.LOGD("parsePortalWebHtml(), http is reachable!!!");
                                HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, 105, 204, 0));
                            }
                        }
                        HwNetworkPropertyChecker.this.mHandler.sendMessage(Message.obtain(HwNetworkPropertyChecker.this.mHandler, 105, WifiProCommonUtils.HTTP_UNREACHALBE, 0));
                    }
                }).start();
                this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 105, WifiProCommonUtils.HTTP_UNREACHALBE, 0), 6000);
                while (!this.htmlDownloaded) {
                    this.mHtmlDownloadWaitingLock.wait();
                }
            } catch (InterruptedException e) {
                LOGD("syncCheckHtmlContent Interrupted ");
            }
        }
        return this.mHtmlCheckResult;
    }

    public String getCaptiveUsedServer() {
        if (TextUtils.isEmpty(this.mUsedServer) || !this.mUsedServer.contains(HICLOUD_HOST_NAME)) {
            return this.mUsedServer;
        }
        return this.mUsedServer + "_" + UUID.randomUUID().toString();
    }

    public String getPortalRedirectedUrl() {
        return this.mPortalRedirectedUrl;
    }

    private int recomfirmResponseCode(String usedServer, int respCode, boolean reconfirm) {
        if (this.mCurrentWifiConfig == null || this.mNetworkDisconnected.get()) {
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
        if (WifiProCommonUtils.unreachableRespCode(respCode)) {
            if (!reconfirm && this.mCheckingCounter == this.mMaxAttempts && !this.mControllerNotified && this.mCurrentWifiConfig.portalNetwork) {
                LOGD("HTTP GET failed, change 599 --> 302 based on the portal history record to launch browser.");
                respCode = 302;
            }
            return respCode;
        }
        if (respCode == 204 && ((WifiProCommonUtils.isOpenAndPortal(this.mCurrentWifiConfig) || WifiProCommonUtils.isOpenAndMaybePortal(this.mCurrentWifiConfig)) && usedServer != null && usedServer.contains(HICLOUD_HOST_NAME))) {
            int httpRespCode = syncCheckNetworkProperty(this.mBackupServer, false, reconfirm, respCode, this.mResponseTimestamp - this.mRequestTimestamp);
            if (WifiProCommonUtils.isRedirectedRespCode(httpRespCode) || (WifiProCommonUtils.httpReachableHome(httpRespCode) && OVERSEA_BACKUP_SERVER.equals(this.mBackupServer))) {
                this.mUsedServer = this.mBackupServer;
                LOGD("HTTP GET " + httpRespCode + ", 204 ==> 302 based on the backup server check.");
                return 302;
            }
        }
        if (!(WifiProCommonUtils.httpReachableHome(respCode) == 0 || usedServer == null || !usedServer.contains(HICLOUD_HOST_NAME))) {
            int secondRespCode = -1;
            if ("close".equalsIgnoreCase(this.mHttpRespConnection)) {
                secondRespCode = syncCheckNetworkProperty(usedServer, false, true, respCode, this.mResponseTimestamp - this.mRequestTimestamp);
                LOGD("recheck for the situation of respCode 200 + connection:close, the new respCode= " + secondRespCode);
                if (secondRespCode == 204) {
                    respCode = 204;
                }
            }
            replacePortalChrStatistics(secondRespCode);
        }
        if (!WifiProCommonUtils.httpReachableOrRedirected(respCode) || !isPortalSpecifiedServer(usedServer)) {
            return recheckForAbnormalRespCode(respCode, reconfirm);
        }
        return respCode;
    }

    private void replacePortalChrStatistics(int secondHttpCode) {
        int firstConnectEx;
        if (!TextUtils.isEmpty(this.mPortalDetectStatisticsInfo)) {
            String firstConnect = this.mPortalDetectStatisticsInfo.substring(0, 1);
            if (TextUtils.isDigitsOnly(firstConnect)) {
                int firstConnectEx2 = Integer.valueOf(firstConnect).intValue();
                if (secondHttpCode == -1) {
                    if ("keep-alive".equalsIgnoreCase(this.mHttpRespConnection)) {
                        firstConnectEx = firstConnectEx2 == 1 ? 41 : 40;
                    } else if (this.mHttpRespConnection == null) {
                        firstConnectEx = firstConnectEx2 == 1 ? 51 : 50;
                    } else {
                        LOGW("replacePortalChrStatistics, HttpRespConnection others = " + this.mHttpRespConnection);
                        firstConnectEx = firstConnectEx2 == 1 ? 61 : 60;
                    }
                } else if (secondHttpCode == 204) {
                    firstConnectEx = firstConnectEx2 == 1 ? 31 : 30;
                } else if (secondHttpCode == 200) {
                    firstConnectEx = firstConnectEx2 == 1 ? 11 : 10;
                } else {
                    firstConnectEx = firstConnectEx2 == 1 ? 21 : 20;
                }
                this.mPortalDetectStatisticsInfo = firstConnectEx + this.mPortalDetectStatisticsInfo.substring(1);
                LOGW("replacePortalChrStatistics, mPortalDetectStatisticsInfo = " + this.mPortalDetectStatisticsInfo);
            }
        }
    }

    private int recheckForAbnormalRespCode(int respCode, boolean reconfirm) {
        int respCode2;
        int newRespCode = respCode;
        boolean repairOk = false;
        if (respCode == 606) {
            respCode = recheckForConnectionReset(respCode, reconfirm);
            if (!WifiProCommonUtils.unreachableRespCode(respCode) && this.mInOversea) {
                return respCode;
            }
            if (WifiProCommonUtils.unreachableRespCode(respCode)) {
                int type = syncRecheckBasedOnWebView(this.mInOversea);
                if (type == 101 && !this.mNetworkDisconnected.get()) {
                    respCode2 = 204;
                } else if (type != 102 || this.mMobileHotspot || !WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig) || this.mNetworkDisconnected.get()) {
                    respCode2 = WifiProCommonUtils.HTTP_UNREACHALBE;
                } else {
                    respCode2 = 302;
                }
                LOGW("recomfirmRespCode, CONN_RESET,  the final respCode = " + respCode2);
                return respCode2;
            }
        }
        if (WifiProCommonUtils.httpReachableHome(newRespCode) != 0 && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig) && !this.mMobileHotspot && !isStarndardPortalAuthenCompleted(newRespCode)) {
            int recheckRespCode = recheckForOpen200Type(newRespCode, reconfirm);
            if (recheckRespCode != newRespCode) {
                repairOk = true;
                newRespCode = recheckRespCode;
            }
        } else if (WifiProCommonUtils.isRedirectedRespCode(respCode) && this.mMobileHotspot) {
            int recheckRespCode2 = recheckForHotspot302Type();
            if (recheckRespCode2 != respCode) {
                repairOk = true;
                newRespCode = recheckRespCode2;
            }
        } else if (!WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig) && (WifiProCommonUtils.isRedirectedRespCode(respCode) || respCode == 602)) {
            int recheckRespCode3 = recheckForNonOpen302Type(respCode);
            if (recheckRespCode3 != respCode) {
                repairOk = true;
                newRespCode = recheckRespCode3;
            }
        } else if (respCode == 601 || respCode == 603) {
            newRespCode = recheckForAbnormalStatus(respCode, reconfirm);
            if (newRespCode != 599) {
                repairOk = true;
            }
            respCode = WifiProCommonUtils.HTTP_UNREACHALBE;
            this.mIgnoreRxCounter = true;
        } else if (respCode == 604) {
            newRespCode = recheckForAbnormalRouterServer(respCode, reconfirm);
            if (newRespCode != 599) {
                repairOk = true;
            }
            respCode = WifiProCommonUtils.HTTP_UNREACHALBE;
            this.mIgnoreRxCounter = true;
        } else if (respCode == 605) {
            respCode = recheckForRedirectedHostChanged();
        }
        return repairOk ? newRespCode : respCode;
    }

    private boolean isPortalSpecifiedServer(String usedServer) {
        return "http://connectivitycheck.platform.hicloud.com/generate_204".equals(usedServer) || OVERSEA_BACKUP_SERVER.equals(usedServer);
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
            return 204;
        }
    }

    private int recheckForConnectionReset(int respCode, boolean reconfirm) {
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForConnectionReset, network is disconnected.");
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
        String backupServer = this.mInOversea ? OVERSEA_BACKUP_SERVER : CHINA_MAINLAND_BACKUP_SERVER;
        this.mUsedServer = backupServer;
        int tmpRespCode = syncCheckNetworkProperty(backupServer, false, reconfirm, respCode, this.mResponseTimestamp - this.mRequestTimestamp);
        LOGD("recheckForConnectionReset, use 2nd server, respCode = " + tmpRespCode);
        if (tmpRespCode == 599 || tmpRespCode == 600) {
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
        if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(tmpRespCode) && this.mInOversea) {
            return 302;
        }
        if (tmpRespCode == 204 && this.mInOversea) {
            return 204;
        }
        if (WifiProCommonUtils.isRedirectedRespCode(tmpRespCode)) {
            return 302;
        }
        if (WifiProCommonUtils.httpReachableHome(tmpRespCode)) {
            return 200;
        }
        return WifiProCommonUtils.HTTP_UNREACHALBE;
    }

    private int recheckForAbnormalStatus(int respCode, boolean reconfirm) {
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForAbnormalStatus, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type == 102 && WifiProCommonUtils.isOpenType(this.mCurrentWifiConfig)) {
            LOGW("recheckForAbnormalStatus, SPECIAL_STATUS, connected network type is portal based on the WebView loading results.");
            return 302;
        } else if (type == 101) {
            LOGW("recheckForAbnormalStatus, SPECIAL_STATUS, connected network type is hasInternet based on the WebView loading results.");
            return 204;
        } else if (type != 100 || this.mNetworkDisconnected.get() || !WifiProCommonUtils.isOpenAndPortal(this.mCurrentWifiConfig)) {
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else {
            return 302;
        }
    }

    private int recheckForAbnormalRouterServer(int respCode, boolean reconfirm) {
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForAbnormalRouterServer, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else if (type != 101) {
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        } else {
            LOGW("recheckForAbnormalRouterServer, connected network type is hasInternet based on the WebView");
            return 204;
        }
    }

    private int recheckForOpen200Type(int lastRespCode, boolean reconfirm) {
        int newRespCode = lastRespCode;
        int type = syncRecheckBasedOnWebView(this.mInOversea);
        if (this.mNetworkDisconnected.get()) {
            LOGW("recheckForOpen200Type, network disconnected when loading webview, type = " + type);
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
        if (type == 101) {
            newRespCode = 204;
        } else if (type == 102) {
            LOGW("recheckForOpen200Type, connected network type is portal based on the WebView loading results.");
            newRespCode = 302;
        } else if (type == 100) {
            int tmpRespCode = 200;
            if (!this.mInOversea) {
                tmpRespCode = syncCheckHtmlContent(URL_HTTPS_BAIDU);
            }
            if (tmpRespCode == 204) {
                newRespCode = tmpRespCode;
            } else if (!this.mNetworkDisconnected.get() && WifiProCommonUtils.httpUnreachableOrAbnormal(tmpRespCode) && WifiProCommonUtils.isOpenAndPortal(this.mCurrentWifiConfig)) {
                LOGD("recheckForOpen200Type, it's known portal.");
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
            newRespCode = 204;
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
            return 204;
        } else {
            LOGW("recheckForNonOpen302Type, 30x/GW from NON-OPEN WiFi, UNKNOWN/Redirect on the WebView --> tobe NO INTERNET");
            this.mIgnoreRxCounter = true;
            return WifiProCommonUtils.HTTP_UNREACHALBE;
        }
    }

    private void updateWifiConfigHistory(int respCode, boolean reconfirm) {
        if (reconfirm && WifiProCommonUtils.httpReachableOrRedirected(respCode) && this.mCurrentWifiConfig != null) {
            String internetHistory = this.mCurrentWifiConfig.internetHistory;
            if (internetHistory == null || internetHistory.lastIndexOf("/") == -1) {
                LOGW("updateWifiConfigHistory, inputed arg is invalid, internetHistory = " + internetHistory);
                return;
            }
            boolean z = false;
            String status = internetHistory.substring(0, 1);
            if (status != null && status.equals("0")) {
                int newStatus = respCode == 204 ? 1 : 2;
                String internetHistory2 = internetHistory.substring(internetHistory.indexOf("/") + 1);
                String internetHistory3 = String.valueOf(newStatus) + "/" + internetHistory2;
                this.mCurrentWifiConfig.noInternetAccess = false;
                this.mCurrentWifiConfig.validatedInternetAccess = !this.mCurrentWifiConfig.noInternetAccess;
                if (newStatus == 1) {
                    this.mCurrentWifiConfig.numNoInternetAccessReports = 0;
                    this.mCurrentWifiConfig.lastHasInternetTimestamp = System.currentTimeMillis();
                }
                WifiConfiguration wifiConfiguration = this.mCurrentWifiConfig;
                if (this.mCurrentWifiConfig.portalNetwork || respCode != 204) {
                    z = true;
                }
                wifiConfiguration.portalNetwork = z;
                this.mCurrentWifiConfig.internetHistory = internetHistory3;
                if (newStatus == 1) {
                    sendCheckResultWhenConnected(204, WifiProCommonDefs.ACTION_NETWORK_CONDITIONS_MEASURED, WifiProCommonDefs.EXTRA_IS_INTERNET_READY, 1);
                }
                Intent intent = new Intent(WifiProCommonDefs.ACTION_UPDATE_CONFIG_HISTORY);
                intent.putExtra(WifiProCommonDefs.EXTRA_FLAG_NEW_WIFI_CONFIG, new WifiConfiguration(this.mCurrentWifiConfig));
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WifiProCommonDefs.NETWORK_CHECKER_RECV_PERMISSION);
            } else if (status != null && status.equals("2") && respCode == 204) {
                this.mCurrentWifiConfig.lastHasInternetTimestamp = System.currentTimeMillis();
                this.mCurrentWifiConfig.portalCheckStatus = 0;
                Intent intent2 = new Intent(WifiProCommonDefs.ACTION_UPDATE_CONFIG_HISTORY);
                intent2.putExtra(WifiProCommonDefs.EXTRA_FLAG_NEW_WIFI_CONFIG, new WifiConfiguration(this.mCurrentWifiConfig));
                this.mContext.sendBroadcastAsUser(intent2, UserHandle.ALL, WifiProCommonDefs.NETWORK_CHECKER_RECV_PERMISSION);
            }
        }
    }

    public boolean isNetworkPoorRssi() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null || WifiProCommonUtils.getCurrenSignalLevel(wifiInfo) >= 3) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void updatePortalDetectionStatistics(boolean reconfirm, int currHttpCode, long currDetectMs, String server, int mainHttpCode, long mainHttpMs, WifiConfiguration config) {
        long currDetectMs2;
        long mainHttpMs2;
        String str = server;
        WifiConfiguration wifiConfiguration = config;
        if (reconfirm || this.mControllerNotified || wifiConfiguration == null || str == null) {
            long j = mainHttpMs;
        } else {
            boolean mainServer = str.contains(SERVER_HICLOUD);
            if (!mainServer || this.mCheckingCounter >= this.mMaxAttempts || !WifiProCommonUtils.unreachableRespCode(currHttpCode)) {
                boolean firstConnected = WifiProCommonUtils.matchedRequestByHistory(wifiConfiguration.internetHistory, 103);
                boolean currentPortalCode = WifiProCommonUtils.isRedirectedRespCodeByGoogle(currHttpCode);
                if (firstConnected || wifiConfiguration.portalNetwork || currentPortalCode) {
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
                    StringBuilder sb = new StringBuilder();
                    sb.append(firstConnected);
                    sb.append("|");
                    sb.append(this.mInOversea ? 1 : 0);
                    sb.append("|");
                    sb.append(mainCode);
                    sb.append("|");
                    sb.append(backupCode);
                    sb.append("|");
                    sb.append(mainDetectMs);
                    sb.append("|");
                    long backupDetectMs2 = backupDetectMs;
                    sb.append(backupDetectMs2);
                    this.mPortalDetectStatisticsInfo = sb.toString();
                    StringBuilder sb2 = new StringBuilder();
                    long j2 = backupDetectMs2;
                    sb2.append("updatePortalDetectionStatistics, portalDetectStatisticsInfo = ");
                    sb2.append(this.mPortalDetectStatisticsInfo);
                    LOGD(sb2.toString());
                }
            }
        }
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
        this.mMainServer = "http://connectivitycheck.platform.hicloud.com/generate_204";
        this.mBackupServer = CHINA_MAINLAND_BACKUP_SERVER;
        this.mInOversea = false;
        this.mControllerNotified = false;
        this.mIgnoreRxCounter = false;
        this.mLastDetectTimeout = false;
        synchronized (this.mRxCounterWaitLock) {
            this.rxCounterRespRcvd = false;
        }
    }

    /* JADX WARNING: type inference failed for: r5v5, types: [java.net.URLConnection] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005f, code lost:
        if (r1 == null) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0061, code lost:
        r1.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0079, code lost:
        if (r1 == null) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x008e, code lost:
        if (r1 == null) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0091, code lost:
        return r0;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    public String downloadPortalWebHtml(String dlUrl) {
        String downloadHtml = null;
        HttpURLConnection urlConnection = null;
        InputStream in = null;
        try {
            ? openConnection = new URL(dlUrl).openConnection();
            if (!(openConnection instanceof HttpURLConnection)) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        LOGD("NetworkCheckerThread, exception of close, msg receive ");
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return null;
            }
            urlConnection = openConnection;
            setConnectionProperty(urlConnection);
            InputStream in2 = urlConnection.getInputStream();
            int respCode = urlConnection.getResponseCode();
            LOGD("downloadPortalWebHtml, respCode = " + respCode);
            if (respCode == 200) {
                downloadHtml = readAllHtml(in2);
            }
            if (in2 != null) {
                try {
                    in2.close();
                } catch (IOException e2) {
                    LOGD("NetworkCheckerThread, exception of close, msg receive ");
                }
            }
        } catch (IOException e3) {
            LOGD("NetworkCheckerThread, IOException, msg receive ");
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e4) {
                    LOGD("NetworkCheckerThread, exception of close, msg receive ");
                }
            }
        } catch (Exception e5) {
            LOGD("NetworkCheckerThread, Exception, msg receive ");
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e6) {
                    LOGD("NetworkCheckerThread, exception of close, msg receive ");
                }
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e7) {
                    LOGD("NetworkCheckerThread, exception of close, msg receive ");
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            throw th;
        }
    }

    private void setConnectionProperty(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);
        urlConnection.setUseCaches(false);
    }

    private String readAllHtml(InputStream in) throws IOException {
        InputStreamReader isr = new InputStreamReader(in, "UTF-8");
        try {
            StringBuffer html = new StringBuffer(2097152);
            char[] buffer = new char[8192];
            int htmlLength = 0;
            while (true) {
                int read = isr.read(buffer, 0, 8192);
                int length = read;
                if (read != -1 && htmlLength + length < 2097152) {
                    htmlLength += length;
                    html.append(buffer, 0, length);
                }
            }
            return html.toString();
        } catch (OutOfMemoryError e) {
            LOGD("readAllHtml, OutOfMemoryError msg receive ");
            return null;
        }
    }

    public void release() {
        LOGW("release, checking counter = " + this.mCheckingCounter);
        if (this.mBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
        }
        this.mGatewayAddress = null;
        this.mNetworkDisconnected.set(true);
        this.mLastDetectTimeout = false;
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
