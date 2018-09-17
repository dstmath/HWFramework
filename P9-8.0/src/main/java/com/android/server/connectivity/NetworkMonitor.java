package com.android.server.connectivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.NetworkEvent;
import android.net.metrics.ValidationProbeEvent;
import android.net.util.Stopwatch;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.LocalLog.ReadOnlyLocalLog;
import android.util.Log;
import com.android.internal.util.State;
import com.android.server.AbsNetworkMonitor;
import com.android.server.HwConnectivityManager;
import com.android.server.HwServiceFactory;
import com.android.server.am.ProcessList;
import com.android.server.display.DisplayTransformManager;
import com.android.server.location.FlpHardwareProvider;
import com.android.server.location.LocationFudger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NetworkMonitor extends AbsNetworkMonitor {
    public static final String ACTION_NETWORK_CONDITIONS_MEASURED = "android.net.conn.NETWORK_CONDITIONS_MEASURED";
    private static final boolean ADD_CRICKET_WIFI_MANAGER = SystemProperties.getBoolean("ro.config.cricket_wifi_manager", false);
    private static final String BAKUP_SERVER = "www.baidu.com";
    private static final String BAKUP_SERV_PAGE = "/";
    private static final int BASE = 532480;
    private static final int BLAME_FOR_EVALUATION_ATTEMPTS = 5;
    private static final int CAPTIVE_PORTAL_REEVALUATE_DELAY_MS = 600000;
    private static final int CMD_CAPTIVE_PORTAL_APP_FINISHED = 532489;
    private static final int CMD_CAPTIVE_PORTAL_RECHECK = 532492;
    public static final int CMD_FORCE_REEVALUATION = 532488;
    public static final int CMD_LAUNCH_CAPTIVE_PORTAL_APP = 532491;
    public static final int CMD_NETWORK_CONNECTED = 532481;
    public static final int CMD_NETWORK_DISCONNECTED = 532487;
    private static final int CMD_REEVALUATE = 532486;
    private static final String COUNTRY_CODE_CN = "460";
    private static final boolean DBG = true;
    private static final String DEFAULT_FALLBACK_URL = "http://www.google.com/gen_204";
    private static final String DEFAULT_HTTPS_URL = "https://www.google.com/generate_204";
    private static final String DEFAULT_HTTP_URL = "http://connectivitycheck.gstatic.com/generate_204";
    private static final String DEFAULT_OTHER_FALLBACK_URLS = "http://play.googleapis.com/generate_204";
    private static final String DEFAULT_SERV_PAGE = "/generate_204";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36";
    public static final int EVENT_NETWORK_TESTED = 532482;
    public static final int EVENT_PROVISIONING_NOTIFICATION = 532490;
    public static final String EXTRA_BSSID = "extra_bssid";
    public static final String EXTRA_CELL_ID = "extra_cellid";
    public static final String EXTRA_CONNECTIVITY_TYPE = "extra_connectivity_type";
    public static final String EXTRA_IS_CAPTIVE_PORTAL = "extra_is_captive_portal";
    public static final String EXTRA_NETWORK_TYPE = "extra_network_type";
    public static final String EXTRA_REQUEST_TIMESTAMP_MS = "extra_request_timestamp_ms";
    public static final String EXTRA_RESPONSE_RECEIVED = "extra_response_received";
    public static final String EXTRA_RESPONSE_TIMESTAMP_MS = "extra_response_timestamp_ms";
    public static final String EXTRA_SSID = "extra_ssid";
    private static final int IGNORE_REEVALUATE_ATTEMPTS = 5;
    private static final int INITIAL_REEVALUATE_DELAY_MS = 1000;
    private static final int INVALID_UID = -1;
    private static final boolean IS_CHINA_AREA = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    private static final int MAX_REEVALUATE_DELAY_MS = 600000;
    public static final int NETWORK_TEST_RESULT_INVALID = 1;
    public static final int NETWORK_TEST_RESULT_VALID = 0;
    private static final String PERMISSION_ACCESS_NETWORK_CONDITIONS = "android.permission.ACCESS_NETWORK_CONDITIONS";
    private static final int PROBE_TIMEOUT_MS = 3000;
    private static final String SERVER_BAIDU = "baidu";
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String TAG = NetworkMonitor.class.getSimpleName();
    private static final boolean VDBG = false;
    private boolean httpReachable;
    private final AlarmManager mAlarmManager;
    private final URL[] mCaptivePortalFallbackUrls;
    private final URL mCaptivePortalHttpUrl;
    private final URL mCaptivePortalHttpsUrl;
    private final State mCaptivePortalState;
    private final String mCaptivePortalUserAgent;
    private final Handler mConnectivityServiceHandler;
    private final Context mContext;
    private final NetworkRequest mDefaultRequest;
    private final State mDefaultState;
    private boolean mDontDisplaySigninNotification;
    private final State mEvaluatingState;
    private final Stopwatch mEvaluationTimer;
    protected boolean mIsCaptivePortalCheckEnabled;
    private CaptivePortalProbeResult mLastPortalProbeResult;
    private CustomIntentReceiver mLaunchCaptivePortalAppBroadcastReceiver;
    private final State mMaybeNotifyState;
    private final IpConnectivityLog mMetricsLog;
    private final int mNetId;
    private final NetworkAgentInfo mNetworkAgentInfo;
    private int mNextFallbackUrlIndex;
    private int mReevaluateToken;
    private final TelephonyManager mTelephonyManager;
    private int mUidResponsibleForReeval;
    private String mUrlHeadFieldLocation;
    private boolean mUseHttps;
    private boolean mUserDoesNotWant;
    private final State mValidatedState;
    private int mValidations;
    private final WifiManager mWifiManager;
    public boolean systemReady;
    private final LocalLog validationLogs;

    /* renamed from: com.android.server.connectivity.NetworkMonitor$1ProbeThread */
    final class AnonymousClass1ProbeThread extends Thread {
        private final boolean mIsHttps;
        private volatile CaptivePortalProbeResult mResult = CaptivePortalProbeResult.FAILED;
        final /* synthetic */ URL val$httpUrl;
        final /* synthetic */ URL val$httpsUrl;
        final /* synthetic */ CountDownLatch val$latch;
        final /* synthetic */ ProxyInfo val$proxy;

        public AnonymousClass1ProbeThread(boolean isHttps, ProxyInfo proxyInfo, URL url, URL url2, CountDownLatch countDownLatch) {
            this.val$proxy = proxyInfo;
            this.val$httpsUrl = url;
            this.val$httpUrl = url2;
            this.val$latch = countDownLatch;
            this.mIsHttps = isHttps;
        }

        public CaptivePortalProbeResult result() {
            return this.mResult;
        }

        public void run() {
            if (this.mIsHttps) {
                this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpsUrl, 2);
            } else {
                this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpUrl, 1);
            }
            if ((this.mIsHttps && this.mResult.isSuccessful()) || (!this.mIsHttps && this.mResult.isPortal())) {
                while (this.val$latch.getCount() > 0) {
                    this.val$latch.countDown();
                }
            }
            this.val$latch.countDown();
        }
    }

    public static final class CaptivePortalProbeResult {
        static final CaptivePortalProbeResult FAILED = new CaptivePortalProbeResult(599);
        final String detectUrl;
        int mHttpResponseCode;
        final String redirectUrl;

        public CaptivePortalProbeResult(int httpResponseCode, String redirectUrl, String detectUrl) {
            this.mHttpResponseCode = httpResponseCode;
            this.redirectUrl = redirectUrl;
            this.detectUrl = detectUrl;
        }

        public CaptivePortalProbeResult(int httpResponseCode) {
            this(httpResponseCode, null, null);
        }

        boolean isSuccessful() {
            return this.mHttpResponseCode == 204;
        }

        boolean isPortal() {
            return !isSuccessful() && this.mHttpResponseCode >= DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE && this.mHttpResponseCode <= 399;
        }
    }

    private class CaptivePortalState extends State {
        private static final String ACTION_LAUNCH_CAPTIVE_PORTAL_APP = "android.net.netmon.launchCaptivePortalApp";

        /* synthetic */ CaptivePortalState(NetworkMonitor this$0, CaptivePortalState -this1) {
            this();
        }

        private CaptivePortalState() {
        }

        public void enter() {
            NetworkMonitor.this.maybeLogEvaluationResult(NetworkMonitor.this.networkEventType(NetworkMonitor.this.validationStage(), EvaluationResult.CAPTIVE_PORTAL));
            if (!NetworkMonitor.this.mDontDisplaySigninNotification) {
                if (NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver == null) {
                    NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver = new CustomIntentReceiver(ACTION_LAUNCH_CAPTIVE_PORTAL_APP, new Random().nextInt(), NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP);
                }
                NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 1, NetworkMonitor.this.mNetworkAgentInfo.network.netId, NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver.getPendingIntent()));
                NetworkMonitor.this.sendMessageDelayed(NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK, 0, LocationFudger.FASTEST_INTERVAL_MS);
                NetworkMonitor networkMonitor = NetworkMonitor.this;
                networkMonitor.mValidations = networkMonitor.mValidations + 1;
            }
        }

        public void exit() {
            NetworkMonitor.this.removeMessages(NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK);
        }
    }

    private class CustomIntentReceiver extends BroadcastReceiver {
        private final String mAction;
        private final int mToken;
        private final int mWhat;

        CustomIntentReceiver(String action, int token, int what) {
            this.mToken = token;
            this.mWhat = what;
            this.mAction = action + "_" + NetworkMonitor.this.mNetworkAgentInfo.network.netId + "_" + token;
            NetworkMonitor.this.mContext.registerReceiver(this, new IntentFilter(this.mAction));
        }

        public PendingIntent getPendingIntent() {
            Intent intent = new Intent(this.mAction);
            intent.setPackage(NetworkMonitor.this.mContext.getPackageName());
            return PendingIntent.getBroadcast(NetworkMonitor.this.mContext, 0, intent, 0);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(this.mAction)) {
                NetworkMonitor.this.sendMessage(NetworkMonitor.this.obtainMessage(this.mWhat, this.mToken));
            }
        }
    }

    private class DefaultState extends State {
        /* synthetic */ DefaultState(NetworkMonitor this$0, DefaultState -this1) {
            this();
        }

        private DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    NetworkMonitor.this.logNetworkEvent(1);
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return true;
                case NetworkMonitor.CMD_NETWORK_DISCONNECTED /*532487*/:
                    NetworkMonitor.this.logNetworkEvent(7);
                    if (NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver != null) {
                        NetworkMonitor.this.mContext.unregisterReceiver(NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver);
                        NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver = null;
                    }
                    NetworkMonitor.this.releaseNetworkPropertyChecker();
                    NetworkMonitor.this.httpReachable = false;
                    NetworkMonitor.this.quit();
                    return true;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK /*532492*/:
                    NetworkMonitor.this.log("Forcing reevaluation for UID " + message.arg1);
                    NetworkMonitor.this.mUidResponsibleForReeval = message.arg1;
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return true;
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_APP_FINISHED /*532489*/:
                    NetworkMonitor.this.log("CaptivePortal App responded with " + message.arg1);
                    NetworkMonitor.this.mUseHttps = false;
                    switch (message.arg1) {
                        case 0:
                            NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_FORCE_REEVALUATION, 0, 0);
                            break;
                        case 1:
                            NetworkMonitor.this.mDontDisplaySigninNotification = true;
                            NetworkMonitor.this.mUserDoesNotWant = true;
                            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, null));
                            NetworkMonitor.this.mUidResponsibleForReeval = 0;
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                            break;
                        case 2:
                            NetworkMonitor.this.mDontDisplaySigninNotification = true;
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                            break;
                    }
                    return true;
                case AbsNetworkMonitor.CMD_NETWORK_ROAMING_CONNECTED /*532581*/:
                    NetworkMonitor.this.log("DefaultState receive CMD_NETWORK_ROAMING_CONNECTED");
                    NetworkMonitor.this.resetNetworkMonitor();
                    NetworkMonitor.this.httpReachable = false;
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return true;
                default:
                    return true;
            }
        }
    }

    private class EvaluatingState extends State {
        private int mAttempts;
        private int mReevaluateDelayMs;

        /* synthetic */ EvaluatingState(NetworkMonitor this$0, EvaluatingState -this1) {
            this();
        }

        private EvaluatingState() {
        }

        public void enter() {
            if (!NetworkMonitor.this.mEvaluationTimer.isStarted()) {
                NetworkMonitor.this.mEvaluationTimer.start();
            }
            NetworkMonitor networkMonitor = NetworkMonitor.this;
            NetworkMonitor networkMonitor2 = NetworkMonitor.this;
            networkMonitor.sendMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0);
            if (NetworkMonitor.this.mUidResponsibleForReeval != -1) {
                TrafficStats.setThreadStatsUid(NetworkMonitor.this.mUidResponsibleForReeval);
                NetworkMonitor.this.mUidResponsibleForReeval = -1;
            }
            this.mReevaluateDelayMs = 1000;
            this.mAttempts = 0;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_REEVALUATE /*532486*/:
                    if (message.arg1 != NetworkMonitor.this.mReevaluateToken || NetworkMonitor.this.mUserDoesNotWant) {
                        return true;
                    }
                    if (NetworkMonitor.this.mDefaultRequest.networkCapabilities.satisfiedByNetworkCapabilities(NetworkMonitor.this.mNetworkAgentInfo.networkCapabilities)) {
                        this.mAttempts++;
                        boolean needCaptivePortalForMobile = HwServiceFactory.getHwConnectivityManager().needCaptivePortalCheck(NetworkMonitor.this.mNetworkAgentInfo, NetworkMonitor.this.mContext);
                        NetworkMonitor.this.log("needCaptivePortalForMobile = " + needCaptivePortalForMobile);
                        if (NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() != 0 || (needCaptivePortalForMobile ^ 1) == 0) {
                            NetworkMonitor networkMonitor;
                            CaptivePortalProbeResult probeResult = new CaptivePortalProbeResult(599);
                            if (!NetworkMonitor.this.isWifiProEnabled() || !NetworkMonitor.this.mIsCaptivePortalCheckEnabled || (needCaptivePortalForMobile ^ 1) == 0) {
                                probeResult = NetworkMonitor.this.isCaptivePortal(NetworkMonitor.getCaptivePortalServerHttpUrl(NetworkMonitor.this.mContext), NetworkMonitor.DEFAULT_SERV_PAGE);
                                if (probeResult.mHttpResponseCode < DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE || probeResult.mHttpResponseCode > 399) {
                                    String operator = NetworkMonitor.this.mTelephonyManager.getNetworkOperator();
                                    NetworkMonitor.this.log("IS_CHINA_AREA =" + NetworkMonitor.IS_CHINA_AREA + ", operator =" + operator);
                                    if (!(operator == null || operator.length() == 0 || !operator.startsWith(NetworkMonitor.COUNTRY_CODE_CN)) || NetworkMonitor.IS_CHINA_AREA) {
                                        NetworkMonitor.this.log("NetworkMonitor isCaptivePortal transit to link baidu");
                                        probeResult = NetworkMonitor.this.isCaptivePortal(NetworkMonitor.BAKUP_SERVER, NetworkMonitor.BAKUP_SERV_PAGE);
                                        if (probeResult.mHttpResponseCode >= DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE && probeResult.mHttpResponseCode <= 399 && probeResult.mHttpResponseCode != 301 && probeResult.mHttpResponseCode != 302) {
                                            probeResult.mHttpResponseCode = 204;
                                        } else if (probeResult.mHttpResponseCode == 301 || probeResult.mHttpResponseCode == 302) {
                                            NetworkMonitor.this.log("mUrlHeadFieldLocation" + NetworkMonitor.this.mUrlHeadFieldLocation);
                                            String host = NetworkMonitor.this.parseHostByLocation(NetworkMonitor.this.mUrlHeadFieldLocation);
                                            if (host != null && host.contains(NetworkMonitor.SERVER_BAIDU)) {
                                                NetworkMonitor.this.log("host contains baidu ,change httpResponseCode to 204");
                                                probeResult.mHttpResponseCode = 204;
                                            }
                                        }
                                    }
                                }
                            } else if (NetworkMonitor.this.isCheckCompletedByWifiPro() && NetworkMonitor.this.httpReachable) {
                                return true;
                            } else {
                                probeResult.mHttpResponseCode = NetworkMonitor.this.getRespCodeByWifiPro();
                                if (probeResult.mHttpResponseCode != 599) {
                                    boolean z;
                                    networkMonitor = NetworkMonitor.this;
                                    if (probeResult.mHttpResponseCode != 204) {
                                        z = true;
                                    } else {
                                        z = false;
                                    }
                                    networkMonitor.sendNetworkConditionsBroadcast(true, z, NetworkMonitor.this.getReqTimestamp(), NetworkMonitor.this.getRespTimestamp());
                                }
                            }
                            HwConnectivityManager hwConnectivityManager = HwServiceFactory.getHwConnectivityManager();
                            Context -get5 = NetworkMonitor.this.mContext;
                            boolean z2 = (probeResult.mHttpResponseCode == 204 || probeResult.mHttpResponseCode == 599) ? false : true;
                            hwConnectivityManager.captivePortalCheckCompleted(-get5, z2);
                            if (probeResult.isSuccessful()) {
                                NetworkMonitor.this.httpReachable = true;
                                NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                            } else if (!probeResult.isPortal() || (NetworkMonitor.ADD_CRICKET_WIFI_MANAGER ^ 1) == 0) {
                                networkMonitor = NetworkMonitor.this;
                                NetworkMonitor networkMonitor2 = NetworkMonitor.this;
                                Message msg = networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0);
                                if (!NetworkMonitor.this.isWifiProEnabled() || (NetworkMonitor.this.isCheckCompletedByWifiPro() ^ 1) == 0 || (needCaptivePortalForMobile ^ 1) == 0) {
                                    NetworkMonitor.this.sendMessageDelayed(msg, (long) this.mReevaluateDelayMs);
                                    NetworkMonitor.this.logNetworkEvent(3);
                                    NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.redirectUrl));
                                    if (this.mAttempts >= 5) {
                                        TrafficStats.clearThreadStatsUid();
                                    }
                                    this.mReevaluateDelayMs *= 2;
                                    if (this.mReevaluateDelayMs > ProcessList.PSS_ALL_INTERVAL) {
                                        this.mReevaluateDelayMs = ProcessList.PSS_ALL_INTERVAL;
                                    }
                                } else {
                                    this.mReevaluateDelayMs *= 2;
                                    NetworkMonitor.this.sendMessageDelayed(msg, (long) NetworkMonitor.this.resetReevaluateDelayMs(this.mReevaluateDelayMs));
                                    return true;
                                }
                            } else {
                                NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.redirectUrl));
                                NetworkMonitor.this.mLastPortalProbeResult = probeResult;
                                if (!NetworkMonitor.this.isWifiProEnabled() || (needCaptivePortalForMobile ^ 1) == 0) {
                                    NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.redirectUrl));
                                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mCaptivePortalState);
                                } else {
                                    NetworkMonitor.this.reportPortalNetwork(NetworkMonitor.this.mConnectivityServiceHandler, NetworkMonitor.this.mNetId, probeResult.redirectUrl);
                                    NetworkMonitor.this.httpReachable = true;
                                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mCaptivePortalState);
                                    return true;
                                }
                            }
                            return true;
                        }
                        NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                        return true;
                    }
                    NetworkMonitor.this.validationLog("Network would not satisfy default request, not validating");
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                    return true;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                    return this.mAttempts < 5;
                default:
                    return false;
            }
        }

        public void exit() {
            TrafficStats.clearThreadStatsUid();
        }
    }

    enum EvaluationResult {
        VALIDATED(true),
        CAPTIVE_PORTAL(false);
        
        final boolean isValidated;

        private EvaluationResult(boolean isValidated) {
            this.isValidated = isValidated;
        }
    }

    private class MaybeNotifyState extends State {
        /* synthetic */ MaybeNotifyState(NetworkMonitor this$0, MaybeNotifyState -this1) {
            this();
        }

        private MaybeNotifyState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP /*532491*/:
                    try {
                        HwServiceFactory.getHwConnectivityManager().startBrowserOnClickNotification(NetworkMonitor.this.mContext, new URL(NetworkMonitor.getCaptivePortalServerHttpUrl(NetworkMonitor.this.mContext)).toString());
                    } catch (MalformedURLException e) {
                        NetworkMonitor.this.log("MalformedURLException " + e);
                    }
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 0, NetworkMonitor.this.mNetworkAgentInfo.network.netId, null));
        }
    }

    private class ValidatedState extends State {
        /* synthetic */ ValidatedState(NetworkMonitor this$0, ValidatedState -this1) {
            this();
        }

        private ValidatedState() {
        }

        public void enter() {
            NetworkMonitor.this.maybeLogEvaluationResult(NetworkMonitor.this.networkEventType(NetworkMonitor.this.validationStage(), EvaluationResult.VALIDATED));
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 0, NetworkMonitor.this.mNetworkAgentInfo.network.netId, null));
            NetworkMonitor networkMonitor = NetworkMonitor.this;
            networkMonitor.mValidations = networkMonitor.mValidations + 1;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                    return true;
                default:
                    return false;
            }
        }
    }

    enum ValidationStage {
        FIRST_VALIDATION(true),
        REVALIDATION(false);
        
        final boolean isFirstValidation;

        private ValidationStage(boolean isFirstValidation) {
            this.isFirstValidation = isFirstValidation;
        }
    }

    public NetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest) {
        this(context, handler, networkAgentInfo, defaultRequest, new IpConnectivityLog());
    }

    protected NetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest, IpConnectivityLog logger) {
        boolean z = true;
        super(TAG + networkAgentInfo.name());
        this.mReevaluateToken = 0;
        this.mUidResponsibleForReeval = -1;
        this.mValidations = 0;
        this.httpReachable = false;
        this.mUserDoesNotWant = false;
        this.mDontDisplaySigninNotification = false;
        this.systemReady = false;
        this.mDefaultState = new DefaultState(this, null);
        this.mValidatedState = new ValidatedState(this, null);
        this.mMaybeNotifyState = new MaybeNotifyState(this, null);
        this.mEvaluatingState = new EvaluatingState(this, null);
        this.mCaptivePortalState = new CaptivePortalState(this, null);
        this.mLaunchCaptivePortalAppBroadcastReceiver = null;
        this.validationLogs = new LocalLog(20);
        this.mUrlHeadFieldLocation = null;
        this.mEvaluationTimer = new Stopwatch();
        this.mLastPortalProbeResult = CaptivePortalProbeResult.FAILED;
        this.mNextFallbackUrlIndex = 0;
        this.mContext = context;
        this.mMetricsLog = logger;
        this.mConnectivityServiceHandler = handler;
        this.mNetworkAgentInfo = networkAgentInfo;
        this.mNetId = this.mNetworkAgentInfo.network.netId;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mDefaultRequest = defaultRequest;
        addState(this.mDefaultState);
        addState(this.mValidatedState, this.mDefaultState);
        addState(this.mMaybeNotifyState, this.mDefaultState);
        addState(this.mEvaluatingState, this.mMaybeNotifyState);
        addState(this.mCaptivePortalState, this.mMaybeNotifyState);
        setInitialState(this.mDefaultState);
        this.mIsCaptivePortalCheckEnabled = Global.getInt(this.mContext.getContentResolver(), "captive_portal_mode", 1) != 0;
        if (Global.getInt(this.mContext.getContentResolver(), "captive_portal_use_https", 1) != 1) {
            z = false;
        }
        this.mUseHttps = z;
        this.mCaptivePortalUserAgent = getCaptivePortalUserAgent(context);
        this.mCaptivePortalHttpsUrl = makeURL(getCaptivePortalServerHttpsUrl(context));
        this.mCaptivePortalHttpUrl = makeURL(getCaptivePortalServerHttpUrl(context));
        this.mCaptivePortalFallbackUrls = makeCaptivePortalFallbackUrls(context);
        start();
    }

    protected void log(String s) {
        Log.d(TAG + BAKUP_SERV_PAGE + this.mNetworkAgentInfo.name(), s);
    }

    private void validationLog(int probeType, Object url, String msg) {
        validationLog(String.format("%s %s %s", new Object[]{ValidationProbeEvent.getProbeName(probeType), url, msg}));
    }

    private void validationLog(String s) {
        log(s);
        this.validationLogs.log(s);
    }

    public ReadOnlyLocalLog getValidationLogs() {
        return this.validationLogs.readOnlyLocalLog();
    }

    private ValidationStage validationStage() {
        return this.mValidations == 0 ? ValidationStage.FIRST_VALIDATION : ValidationStage.REVALIDATION;
    }

    private static String getCaptivePortalServerHttpsUrl(Context context) {
        return getSetting(context, "captive_portal_https_url", DEFAULT_HTTPS_URL);
    }

    public static String getCaptivePortalServerHttpUrl(Context context) {
        return getSetting(context, "captive_portal_http_url", DEFAULT_HTTP_URL);
    }

    private URL[] makeCaptivePortalFallbackUrls(Context context) {
        String separator = ",";
        String joinedUrls = getSetting(context, "captive_portal_fallback_url", DEFAULT_FALLBACK_URL) + separator + getSetting(context, "captive_portal_other_fallback_urls", DEFAULT_OTHER_FALLBACK_URLS);
        List<URL> urls = new ArrayList();
        for (String s : joinedUrls.split(separator)) {
            URL u = makeURL(s);
            if (u != null) {
                urls.add(u);
            }
        }
        if (urls.isEmpty()) {
            Log.e(TAG, String.format("could not create any url from %s", new Object[]{joinedUrls}));
        }
        return (URL[]) urls.toArray(new URL[urls.size()]);
    }

    private static String getCaptivePortalUserAgent(Context context) {
        return getSetting(context, "captive_portal_user_agent", DEFAULT_USER_AGENT);
    }

    private static String getSetting(Context context, String symbol, String defaultValue) {
        String value = Global.getString(context.getContentResolver(), symbol);
        return value != null ? value : defaultValue;
    }

    private URL nextFallbackUrl() {
        if (this.mCaptivePortalFallbackUrls.length == 0) {
            return null;
        }
        int idx = Math.abs(this.mNextFallbackUrlIndex) % this.mCaptivePortalFallbackUrls.length;
        this.mNextFallbackUrlIndex += new Random().nextInt();
        return this.mCaptivePortalFallbackUrls[idx];
    }

    protected CaptivePortalProbeResult isCaptivePortal(String urlString) {
        if (this.mIsCaptivePortalCheckEnabled) {
            URL pacUrl = null;
            URL httpsUrl = this.mCaptivePortalHttpsUrl;
            URL httpUrl = this.mCaptivePortalHttpUrl;
            if (urlString != null && urlString.contains(BAKUP_SERVER)) {
                try {
                    URL httpUrl2 = new URL(urlString);
                    try {
                        httpsUrl = new URL(urlString);
                        httpUrl = httpUrl2;
                    } catch (MalformedURLException e) {
                        httpUrl = httpUrl2;
                        validationLog("Bad validation URL");
                        return CaptivePortalProbeResult.FAILED;
                    }
                } catch (MalformedURLException e2) {
                    validationLog("Bad validation URL");
                    return CaptivePortalProbeResult.FAILED;
                }
            }
            ProxyInfo proxyInfo = this.mNetworkAgentInfo.linkProperties.getHttpProxy();
            if (!(proxyInfo == null || (Uri.EMPTY.equals(proxyInfo.getPacFileUrl()) ^ 1) == 0)) {
                pacUrl = makeURL(proxyInfo.getPacFileUrl().toString());
                if (pacUrl == null) {
                    return CaptivePortalProbeResult.FAILED;
                }
            }
            if (pacUrl == null && (httpUrl == null || httpsUrl == null)) {
                return CaptivePortalProbeResult.FAILED;
            }
            CaptivePortalProbeResult result;
            long startTime = SystemClock.elapsedRealtime();
            if (pacUrl != null) {
                result = sendDnsAndHttpProbes(null, pacUrl, 3);
            } else if (this.mUseHttps) {
                result = sendParallelHttpProbes(proxyInfo, httpsUrl, httpUrl);
            } else {
                result = sendDnsAndHttpProbes(proxyInfo, httpUrl, 1);
            }
            sendNetworkConditionsBroadcast(true, result.isPortal(), startTime, SystemClock.elapsedRealtime());
            return result;
        }
        validationLog("Validation disabled.");
        return new CaptivePortalProbeResult(204);
    }

    protected CaptivePortalProbeResult isCaptivePortal() {
        return isCaptivePortal(getCaptivePortalServerHttpsUrl(this.mContext));
    }

    protected CaptivePortalProbeResult isCaptivePortal(String server_url, String page) {
        if (!(!server_url.startsWith("http://") ? server_url.startsWith("https://") : true)) {
            server_url = "http://" + server_url;
        }
        if (!server_url.endsWith(page)) {
            server_url = server_url + page;
        }
        return isCaptivePortal(server_url);
    }

    private CaptivePortalProbeResult sendDnsAndHttpProbes(ProxyInfo proxy, URL url, int probeType) {
        sendDnsProbe(proxy != null ? proxy.getHost() : url.getHost());
        return sendHttpProbe(url, probeType);
    }

    private void sendDnsProbe(String host) {
        if (!TextUtils.isEmpty(host)) {
            int result;
            String connectInfo;
            String name = ValidationProbeEvent.getProbeName(0);
            Stopwatch watch = new Stopwatch().start();
            try {
                InetAddress[] addresses = this.mNetworkAgentInfo.network.getAllByName(host);
                StringBuffer buffer = new StringBuffer();
                for (InetAddress address : addresses) {
                    buffer.append(',').append(address.getHostAddress());
                }
                result = 1;
                connectInfo = "OK " + buffer.substring(1);
            } catch (UnknownHostException e) {
                result = 0;
                connectInfo = "FAIL";
            }
            validationLog(0, host, String.format("%dms %s", new Object[]{Long.valueOf(watch.stop()), connectInfo}));
            logValidationProbe(latency, 0, result);
        }
    }

    protected CaptivePortalProbeResult sendHttpProbe(URL url, int probeType) {
        HttpURLConnection urlConnection = null;
        int httpResponseCode = 599;
        String redirectUrl = null;
        Stopwatch probeTimer = new Stopwatch().start();
        int oldTag = TrafficStats.getAndSetThreadStatsTag(-249);
        try {
            urlConnection = (HttpURLConnection) this.mNetworkAgentInfo.network.openConnection(url);
            urlConnection.setInstanceFollowRedirects(probeType == 3);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setUseCaches(false);
            if (this.mCaptivePortalUserAgent != null) {
                urlConnection.setRequestProperty("User-Agent", this.mCaptivePortalUserAgent);
            }
            String requestHeader = urlConnection.getRequestProperties().toString();
            long requestTimestamp = SystemClock.elapsedRealtime();
            httpResponseCode = urlConnection.getResponseCode();
            redirectUrl = urlConnection.getHeaderField("location");
            long responseTimestamp = SystemClock.elapsedRealtime();
            this.mUrlHeadFieldLocation = urlConnection.getHeaderField(FlpHardwareProvider.LOCATION);
            validationLog(probeType, url, "time=" + (responseTimestamp - requestTimestamp) + "ms" + " ret=" + httpResponseCode + " request=" + requestHeader + " headers=" + urlConnection.getHeaderFields());
            if (httpResponseCode == DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE) {
                if (probeType == 3) {
                    validationLog(probeType, url, "PAC fetch 200 response interpreted as 204 response.");
                    httpResponseCode = 204;
                } else if (urlConnection.getContentLengthLong() == 0) {
                    validationLog(probeType, url, "200 response with Content-length=0 interpreted as 204 response.");
                    httpResponseCode = 204;
                } else if (urlConnection.getContentLengthLong() == -1 && urlConnection.getInputStream().read() == -1) {
                    validationLog(probeType, url, "Empty 200 response interpreted as 204 response.");
                    httpResponseCode = 204;
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
        } catch (IOException e) {
            validationLog(probeType, url, "Probably not a portal: exception " + e);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
            throw th;
        }
        logValidationProbe(probeTimer.stop(), probeType, httpResponseCode);
        return new CaptivePortalProbeResult(httpResponseCode, redirectUrl, url.toString());
    }

    private CaptivePortalProbeResult sendParallelHttpProbes(ProxyInfo proxy, URL httpsUrl, URL httpUrl) {
        CountDownLatch latch = new CountDownLatch(2);
        AnonymousClass1ProbeThread httpsProbe = new AnonymousClass1ProbeThread(true, proxy, httpsUrl, httpUrl, latch);
        AnonymousClass1ProbeThread httpProbe = new AnonymousClass1ProbeThread(false, proxy, httpsUrl, httpUrl, latch);
        try {
            httpsProbe.start();
            httpProbe.start();
            latch.await(3000, TimeUnit.MILLISECONDS);
            CaptivePortalProbeResult httpsResult = httpsProbe.result();
            CaptivePortalProbeResult httpResult = httpProbe.result();
            if (httpResult.isPortal()) {
                return httpResult;
            }
            if (httpsResult.isPortal() || httpsResult.isSuccessful()) {
                return httpsResult;
            }
            URL fallbackUrl = nextFallbackUrl();
            if (fallbackUrl != null) {
                CaptivePortalProbeResult result = sendHttpProbe(fallbackUrl, 4);
                if (result.isPortal()) {
                    return result;
                }
            }
            try {
                httpProbe.join();
                if (httpProbe.result().isPortal()) {
                    return httpProbe.result();
                }
                httpsProbe.join();
                return httpsProbe.result();
            } catch (InterruptedException e) {
                validationLog("Error: http or https probe wait interrupted!");
                return CaptivePortalProbeResult.FAILED;
            }
        } catch (InterruptedException e2) {
            validationLog("Error: probes wait interrupted!");
            return CaptivePortalProbeResult.FAILED;
        }
    }

    private URL makeURL(String url) {
        if (url != null) {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                validationLog("Bad URL: " + url);
            }
        }
        return null;
    }

    private void sendNetworkConditionsBroadcast(boolean responseReceived, boolean isCaptivePortal, long requestTimestampMs, long responseTimestampMs) {
        if (Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) != 0 && this.systemReady) {
            Intent latencyBroadcast = new Intent(ACTION_NETWORK_CONDITIONS_MEASURED);
            switch (this.mNetworkAgentInfo.networkInfo.getType()) {
                case 0:
                    latencyBroadcast.putExtra(EXTRA_NETWORK_TYPE, this.mTelephonyManager.getNetworkType());
                    List<CellInfo> info = this.mTelephonyManager.getAllCellInfo();
                    if (info != null) {
                        int numRegisteredCellInfo = 0;
                        for (CellInfo cellInfo : info) {
                            if (cellInfo.isRegistered()) {
                                numRegisteredCellInfo++;
                                if (numRegisteredCellInfo <= 1) {
                                    if (cellInfo instanceof CellInfoCdma) {
                                        latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoCdma) cellInfo).getCellIdentity());
                                    } else if (cellInfo instanceof CellInfoGsm) {
                                        latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoGsm) cellInfo).getCellIdentity());
                                    } else if (cellInfo instanceof CellInfoLte) {
                                        latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoLte) cellInfo).getCellIdentity());
                                    } else if (cellInfo instanceof CellInfoWcdma) {
                                        latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoWcdma) cellInfo).getCellIdentity());
                                    } else {
                                        return;
                                    }
                                }
                                return;
                            }
                        }
                        break;
                    }
                    return;
                case 1:
                    WifiInfo currentWifiInfo = this.mWifiManager.getConnectionInfo();
                    if (currentWifiInfo != null) {
                        latencyBroadcast.putExtra(EXTRA_SSID, currentWifiInfo.getSSID());
                        latencyBroadcast.putExtra(EXTRA_BSSID, currentWifiInfo.getBSSID());
                        break;
                    }
                    return;
                default:
                    return;
            }
            latencyBroadcast.putExtra(EXTRA_CONNECTIVITY_TYPE, this.mNetworkAgentInfo.networkInfo.getType());
            latencyBroadcast.putExtra(EXTRA_RESPONSE_RECEIVED, responseReceived);
            latencyBroadcast.putExtra(EXTRA_REQUEST_TIMESTAMP_MS, requestTimestampMs);
            if (responseReceived) {
                latencyBroadcast.putExtra(EXTRA_IS_CAPTIVE_PORTAL, isCaptivePortal);
                latencyBroadcast.putExtra(EXTRA_RESPONSE_TIMESTAMP_MS, responseTimestampMs);
            }
            this.mContext.sendBroadcastAsUser(latencyBroadcast, UserHandle.CURRENT, PERMISSION_ACCESS_NETWORK_CONDITIONS);
        }
    }

    private void logNetworkEvent(int evtype) {
        this.mMetricsLog.log(new NetworkEvent(this.mNetId, evtype));
    }

    private int networkEventType(ValidationStage s, EvaluationResult r) {
        if (s.isFirstValidation) {
            if (r.isValidated) {
                return 8;
            }
            return 10;
        } else if (r.isValidated) {
            return 9;
        } else {
            return 11;
        }
    }

    private void maybeLogEvaluationResult(int evtype) {
        if (this.mEvaluationTimer.isRunning()) {
            this.mMetricsLog.log(new NetworkEvent(this.mNetId, evtype, this.mEvaluationTimer.stop()));
            this.mEvaluationTimer.reset();
        }
    }

    private void logValidationProbe(long durationMs, int probeType, int probeResult) {
        int[] transports = this.mNetworkAgentInfo.networkCapabilities.getTransportTypes();
        boolean isFirstValidation = validationStage().isFirstValidation;
        ValidationProbeEvent ev = new ValidationProbeEvent();
        ev.probeType = ValidationProbeEvent.makeProbeType(probeType, isFirstValidation);
        ev.returnCode = probeResult;
        ev.durationMs = durationMs;
        this.mMetricsLog.log(this.mNetId, transports, ev);
    }

    private String parseHostByLocation(String location) {
        if (location != null) {
            int start = 0;
            if (location.startsWith("http://")) {
                start = 7;
            } else if (location.startsWith("https://")) {
                start = 8;
            }
            int end = location.indexOf(BAKUP_SERV_PAGE, start);
            if (end == -1) {
                end = location.length();
            }
            if (start <= end && end <= location.length()) {
                return location.substring(start, end);
            }
        }
        return null;
    }
}
