package com.android.server.connectivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.CaptivePortal;
import android.net.ICaptivePortal;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.captiveportal.CaptivePortalProbeResult;
import android.net.captiveportal.CaptivePortalProbeSpec;
import android.net.dns.ResolvUtil;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.NetworkEvent;
import android.net.metrics.ValidationProbeEvent;
import android.net.util.Stopwatch;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.State;
import com.android.server.AbsNetworkMonitor;
import com.android.server.HwServiceFactory;
import com.android.server.connectivity.DnsManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NetworkMonitor extends AbsNetworkMonitor {
    /* access modifiers changed from: private */
    public static final boolean ADD_CRICKET_WIFI_MANAGER = SystemProperties.getBoolean("ro.config.cricket_wifi_manager", false);
    private static final String BAKUP_SERVER = "www.baidu.com";
    private static final String BAKUP_SERV_PAGE = "/";
    private static final int BASE = 532480;
    private static final int BLAME_FOR_EVALUATION_ATTEMPTS = 5;
    private static final int CAPTIVE_PORTAL_REEVALUATE_DELAY_MS = 600000;
    private static final int CMD_CAPTIVE_PORTAL_APP_FINISHED = 532489;
    private static final int CMD_CAPTIVE_PORTAL_RECHECK = 532492;
    private static final int CMD_EVALUATE_PRIVATE_DNS = 532495;
    private static final int CMD_FORCE_REEVALUATION = 532488;
    public static final int CMD_LAUNCH_CAPTIVE_PORTAL_APP = 532491;
    public static final int CMD_NETWORK_CONNECTED = 532481;
    public static final int CMD_NETWORK_DISCONNECTED = 532487;
    private static final int CMD_PRIVATE_DNS_SETTINGS_CHANGED = 532493;
    private static final int CMD_REEVALUATE = 532486;
    private static final String COUNTRY_CODE_CN = "460";
    private static final boolean DBG = true;
    private static final String DEFAULT_FALLBACK_URL = "http://www.google.com/gen_204";
    private static final String DEFAULT_HTTPS_URL = "https://www.google.com/generate_204";
    private static final String DEFAULT_HTTP_AND_HTTPS_FOR_BLUETOOTH = "http://connectivitycheck.platform.hicloud.com/generate_204";
    private static final String DEFAULT_HTTP_URL = "http://connectivitycheck.gstatic.com/generate_204";
    private static final String DEFAULT_OTHER_FALLBACK_URLS = "http://play.googleapis.com/generate_204";
    private static final String DEFAULT_SERV_PAGE = "/generate_204";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.32 Safari/537.36";
    private static final String DOCOMO_HW_OPTA = "341";
    private static final String DOCOMO_HW_OPTB = "392";
    public static final int EVENT_NETWORK_TESTED = 532482;
    public static final int EVENT_PRIVATE_DNS_CONFIG_RESOLVED = 532494;
    public static final int EVENT_PROVISIONING_NOTIFICATION = 532490;
    private static final int IGNORE_REEVALUATE_ATTEMPTS = 5;
    private static final int INITIAL_REEVALUATE_DELAY_MS = 1000;
    private static final int INVALID_UID = -1;
    /* access modifiers changed from: private */
    public static final boolean IS_CHINA_AREA = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    /* access modifiers changed from: private */
    public static final boolean IS_DOCOMO;
    private static final int MAX_REEVALUATE_DELAY_MS = 600000;
    public static final int NETWORK_TEST_RESULT_INVALID = 1;
    public static final int NETWORK_TEST_RESULT_VALID = 0;
    private static final int NO_UID = 0;
    private static final int NUM_VALIDATION_LOG_LINES = 20;
    private static final int PROBE_TIMEOUT_MS = 3000;
    private static final String SERVER_BAIDU = "baidu";
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String TAG = NetworkMonitor.class.getSimpleName();
    private static final boolean VDBG = false;
    /* access modifiers changed from: private */
    public boolean httpReachable;
    private final CaptivePortalProbeSpec[] mCaptivePortalFallbackSpecs;
    private final URL[] mCaptivePortalFallbackUrls;
    private final URL mCaptivePortalHttpUrl;
    private final URL mCaptivePortalHttpsUrl;
    /* access modifiers changed from: private */
    public final State mCaptivePortalState;
    /* access modifiers changed from: private */
    public final String mCaptivePortalUserAgent;
    /* access modifiers changed from: private */
    public final Handler mConnectivityServiceHandler;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final NetworkRequest mDefaultRequest;
    private final State mDefaultState;
    /* access modifiers changed from: private */
    public boolean mDontDisplaySigninNotification;
    /* access modifiers changed from: private */
    public final State mEvaluatingPrivateDnsState;
    /* access modifiers changed from: private */
    public final State mEvaluatingState;
    /* access modifiers changed from: private */
    public final Stopwatch mEvaluationTimer;
    @VisibleForTesting
    protected boolean mIsCaptivePortalCheckEnabled;
    /* access modifiers changed from: private */
    public CaptivePortalProbeResult mLastPortalProbeResult;
    /* access modifiers changed from: private */
    public CustomIntentReceiver mLaunchCaptivePortalAppBroadcastReceiver;
    private final State mMaybeNotifyState;
    private final IpConnectivityLog mMetricsLog;
    /* access modifiers changed from: private */
    public final int mNetId;
    /* access modifiers changed from: private */
    public final Network mNetwork;
    /* access modifiers changed from: private */
    public final NetworkAgentInfo mNetworkAgentInfo;
    private int mNextFallbackUrlIndex;
    /* access modifiers changed from: private */
    public String mPrivateDnsProviderHostname;
    /* access modifiers changed from: private */
    public int mReevaluateToken;
    private final NetworkMonitorSettings mSettings;
    /* access modifiers changed from: private */
    public final TelephonyManager mTelephonyManager;
    /* access modifiers changed from: private */
    public int mUidResponsibleForReeval;
    /* access modifiers changed from: private */
    public String mUrlHeadFieldLocation;
    /* access modifiers changed from: private */
    public boolean mUseHttps;
    /* access modifiers changed from: private */
    public boolean mUserDoesNotWant;
    /* access modifiers changed from: private */
    public final State mValidatedState;
    /* access modifiers changed from: private */
    public int mValidations;
    private final WifiManager mWifiManager;
    public boolean systemReady;
    private final LocalLog validationLogs;

    private class CaptivePortalState extends State {
        private static final String ACTION_LAUNCH_CAPTIVE_PORTAL_APP = "android.net.netmon.launchCaptivePortalApp";

        private CaptivePortalState() {
        }

        public void enter() {
            NetworkMonitor.this.setWifiConfigWithPortalConnect();
            NetworkMonitor.this.maybeLogEvaluationResult(NetworkMonitor.this.networkEventType(NetworkMonitor.this.validationStage(), EvaluationResult.CAPTIVE_PORTAL));
            if (!NetworkMonitor.this.mDontDisplaySigninNotification) {
                if (NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver == null) {
                    CustomIntentReceiver unused = NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver = new CustomIntentReceiver(ACTION_LAUNCH_CAPTIVE_PORTAL_APP, new Random().nextInt(), NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP);
                }
                if (!NetworkMonitor.this.mNetworkAgentInfo.everCaptivePortalDetected) {
                    NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 1, NetworkMonitor.this.mNetId, NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver.getPendingIntent()));
                }
                NetworkMonitor.this.sendMessageDelayed(NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK, 0, 600000);
                int unused2 = NetworkMonitor.this.mValidations = NetworkMonitor.this.mValidations + 1;
            }
        }

        public boolean processMessage(Message msg) {
            if (msg.what != NetworkMonitor.CMD_FORCE_REEVALUATION) {
                NetworkMonitor networkMonitor = NetworkMonitor.this;
                networkMonitor.log(NetworkMonitor.this.mNetId + ":CaptivePortalState not handle " + msg.what);
                return false;
            }
            String callingApp = NetworkMonitor.this.mContext.getPackageManager().getNameForUid(msg.arg1);
            if (callingApp == null || !callingApp.equals("com.android.chrome")) {
                return false;
            }
            NetworkMonitor.this.log("webview is started by connection request from chrome");
            NetworkMonitor.this.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP, 1));
            return true;
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
            this.mAction = action + "_" + r3.mNetId + "_" + token;
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

    @VisibleForTesting
    public static class DefaultNetworkMonitorSettings implements NetworkMonitorSettings {
        public int getSetting(Context context, String symbol, int defaultValue) {
            return Settings.Global.getInt(context.getContentResolver(), symbol, defaultValue);
        }

        public String getSetting(Context context, String symbol, String defaultValue) {
            String value = Settings.Global.getString(context.getContentResolver(), symbol);
            return value != null ? value : defaultValue;
        }
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    NetworkMonitor.this.log("DefaultState handle CMD_NETWORK_CONNECTED");
                    NetworkMonitor.this.logNetworkEvent(1);
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return true;
                case NetworkMonitor.CMD_NETWORK_DISCONNECTED /*532487*/:
                    NetworkMonitor.this.log("DefaultState handle CMD_NETWORK_DISCONNECTED");
                    NetworkMonitor.this.logNetworkEvent(7);
                    if (NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver != null) {
                        NetworkMonitor.this.mContext.unregisterReceiver(NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver);
                        CustomIntentReceiver unused = NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver = null;
                    }
                    NetworkMonitor.this.releaseNetworkPropertyChecker();
                    boolean unused2 = NetworkMonitor.this.httpReachable = false;
                    NetworkMonitor.this.quit();
                    return true;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK /*532492*/:
                    if (message.what == NetworkMonitor.CMD_FORCE_REEVALUATION) {
                        boolean unused3 = NetworkMonitor.this.httpReachable = false;
                    }
                    NetworkMonitor networkMonitor = NetworkMonitor.this;
                    networkMonitor.log("Forcing reevaluation for UID " + message.arg1);
                    int unused4 = NetworkMonitor.this.mUidResponsibleForReeval = message.arg1;
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return true;
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_APP_FINISHED /*532489*/:
                    NetworkMonitor networkMonitor2 = NetworkMonitor.this;
                    networkMonitor2.log("CaptivePortal App responded with " + message.arg1);
                    boolean unused5 = NetworkMonitor.this.mUseHttps = false;
                    switch (message.arg1) {
                        case 0:
                            NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_FORCE_REEVALUATION, 0, 0);
                            break;
                        case 1:
                            boolean unused6 = NetworkMonitor.this.mDontDisplaySigninNotification = true;
                            boolean unused7 = NetworkMonitor.this.mUserDoesNotWant = true;
                            NetworkMonitor.this.notifyNetworkTestResultInvalid(null);
                            int unused8 = NetworkMonitor.this.mUidResponsibleForReeval = 0;
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                            break;
                        case 2:
                            boolean unused9 = NetworkMonitor.this.mDontDisplaySigninNotification = true;
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingPrivateDnsState);
                            break;
                    }
                    return true;
                case NetworkMonitor.CMD_PRIVATE_DNS_SETTINGS_CHANGED /*532493*/:
                    NetworkMonitor.this.log("DefaultState handle CMD_PRIVATE_DNS_SETTINGS_CHANGED");
                    DnsManager.PrivateDnsConfig cfg = (DnsManager.PrivateDnsConfig) message.obj;
                    if (NetworkMonitor.this.isValidationRequired() && cfg != null && cfg.inStrictMode()) {
                        String unused10 = NetworkMonitor.this.mPrivateDnsProviderHostname = cfg.hostname;
                        NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_EVALUATE_PRIVATE_DNS);
                        break;
                    } else {
                        String unused11 = NetworkMonitor.this.mPrivateDnsProviderHostname = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                        break;
                    }
                case NetworkMonitor.CMD_EVALUATE_PRIVATE_DNS /*532495*/:
                    NetworkMonitor.this.log("DefaultState defer CMD_EVALUATE_PRIVATE_DNS");
                    NetworkMonitor.this.deferMessage(message);
                    return true;
                case AbsNetworkMonitor.CMD_NETWORK_ROAMING_CONNECTED:
                    NetworkMonitor.this.log("DefaultState receive CMD_NETWORK_ROAMING_CONNECTED");
                    NetworkMonitor.this.resetNetworkMonitor();
                    boolean unused12 = NetworkMonitor.this.httpReachable = false;
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return true;
                case AbsNetworkMonitor.CMD_INVALIDLINK_NETWORK_DETECTION:
                    NetworkMonitor.this.log("DefaultState receive CMD_INVALIDLINK_NETWORK_DETECTION");
                    NetworkMonitor.this.mNetworkAgentInfo.lastCaptivePortalDetected = false;
                    boolean unused13 = NetworkMonitor.this.httpReachable = false;
                    if (NetworkMonitor.IS_DOCOMO) {
                        NetworkMonitor.this.notifyNetworkTestResultInvalid(null);
                    }
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return true;
            }
            return true;
        }
    }

    private class EvaluatingPrivateDnsState extends State {
        private DnsManager.PrivateDnsConfig mPrivateDnsConfig;
        private int mPrivateDnsReevalDelayMs;

        private EvaluatingPrivateDnsState() {
        }

        public void enter() {
            NetworkMonitor.this.log("EvaluatingPrivateDnsState enter");
            this.mPrivateDnsReevalDelayMs = 1000;
            this.mPrivateDnsConfig = null;
            NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_EVALUATE_PRIVATE_DNS);
        }

        public boolean processMessage(Message msg) {
            if (msg.what != NetworkMonitor.CMD_EVALUATE_PRIVATE_DNS) {
                NetworkMonitor networkMonitor = NetworkMonitor.this;
                networkMonitor.log("EvaluatingPrivateDnsState not handle " + msg.what);
                return false;
            }
            NetworkMonitor.this.log("EvaluatingPrivateDnsState handle CMD_EVALUATE_PRIVATE_DNS");
            if (inStrictMode()) {
                if (!isStrictModeHostnameResolved()) {
                    resolveStrictModeHostname();
                    if (isStrictModeHostnameResolved()) {
                        notifyPrivateDnsConfigResolved();
                    } else {
                        handlePrivateDnsEvaluationFailure();
                        return true;
                    }
                }
                if (!sendPrivateDnsProbe()) {
                    handlePrivateDnsEvaluationFailure();
                    return true;
                }
            }
            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
            return true;
        }

        private boolean inStrictMode() {
            return !TextUtils.isEmpty(NetworkMonitor.this.mPrivateDnsProviderHostname);
        }

        private boolean isStrictModeHostnameResolved() {
            return this.mPrivateDnsConfig != null && this.mPrivateDnsConfig.hostname.equals(NetworkMonitor.this.mPrivateDnsProviderHostname) && this.mPrivateDnsConfig.ips.length > 0;
        }

        private void resolveStrictModeHostname() {
            try {
                this.mPrivateDnsConfig = new DnsManager.PrivateDnsConfig(NetworkMonitor.this.mPrivateDnsProviderHostname, ResolvUtil.blockingResolveAllLocally(NetworkMonitor.this.mNetwork, NetworkMonitor.this.mPrivateDnsProviderHostname, 0));
            } catch (UnknownHostException e) {
                this.mPrivateDnsConfig = null;
            }
        }

        private void notifyPrivateDnsConfigResolved() {
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PRIVATE_DNS_CONFIG_RESOLVED, 0, NetworkMonitor.this.mNetId, this.mPrivateDnsConfig));
        }

        private void handlePrivateDnsEvaluationFailure() {
            NetworkMonitor.this.notifyNetworkTestResultInvalid(null);
            NetworkMonitor.this.sendMessageDelayed(NetworkMonitor.CMD_EVALUATE_PRIVATE_DNS, (long) this.mPrivateDnsReevalDelayMs);
            this.mPrivateDnsReevalDelayMs *= 2;
            if (this.mPrivateDnsReevalDelayMs > 600000) {
                this.mPrivateDnsReevalDelayMs = 600000;
            }
        }

        private boolean sendPrivateDnsProbe() {
            StringBuilder sb = new StringBuilder();
            boolean z = false;
            sb.append(UUID.randomUUID().toString().substring(0, 8));
            sb.append("-dnsotls-ds.metric.gstatic.com");
            try {
                InetAddress[] ips = NetworkMonitor.this.mNetworkAgentInfo.network().getAllByName(sb.toString());
                if (ips != null && ips.length > 0) {
                    z = true;
                }
                return z;
            } catch (UnknownHostException e) {
                return false;
            }
        }
    }

    private class EvaluatingState extends State {
        private int mAttempts;
        private int mReevaluateDelayMs;

        private EvaluatingState() {
        }

        public void enter() {
            NetworkMonitor.this.log("EvaluatingState enter");
            if (!NetworkMonitor.this.mEvaluationTimer.isStarted()) {
                NetworkMonitor.this.mEvaluationTimer.start();
            }
            NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_REEVALUATE, NetworkMonitor.access$3204(NetworkMonitor.this), 0);
            if (NetworkMonitor.this.mUidResponsibleForReeval != -1) {
                TrafficStats.setThreadStatsUid(NetworkMonitor.this.mUidResponsibleForReeval);
                int unused = NetworkMonitor.this.mUidResponsibleForReeval = -1;
            }
            this.mReevaluateDelayMs = 1000;
            this.mAttempts = 0;
        }

        public boolean processMessage(Message message) {
            Message message2 = message;
            int i = message2.what;
            boolean z = false;
            if (i == NetworkMonitor.CMD_REEVALUATE) {
                NetworkMonitor.this.log("EvaluatingState handle CMD_REEVALUATE");
                if (message2.arg1 != NetworkMonitor.this.mReevaluateToken || NetworkMonitor.this.mUserDoesNotWant) {
                    return true;
                }
                if (!NetworkMonitor.this.isValidationRequired()) {
                    NetworkMonitor.this.validationLog("Network would not satisfy default request, not validating");
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                    return true;
                }
                this.mAttempts++;
                boolean needCaptivePortalForMobile = HwServiceFactory.getHwConnectivityManager().needCaptivePortalCheck(NetworkMonitor.this.mNetworkAgentInfo, NetworkMonitor.this.mContext);
                NetworkMonitor.this.log("needCaptivePortalForMobile = " + needCaptivePortalForMobile);
                if (NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() != 0 || needCaptivePortalForMobile) {
                    CaptivePortalProbeResult probeResult = new CaptivePortalProbeResult(599);
                    if (!NetworkMonitor.this.isWifiProEnabled() || !NetworkMonitor.this.mIsCaptivePortalCheckEnabled || needCaptivePortalForMobile) {
                        probeResult = NetworkMonitor.this.isCaptivePortal(NetworkMonitor.getCaptivePortalServerHttpUrl(NetworkMonitor.this.mContext), NetworkMonitor.DEFAULT_SERV_PAGE);
                        if (probeResult.mHttpResponseCode < 200 || probeResult.mHttpResponseCode > 399) {
                            String operator = NetworkMonitor.this.mTelephonyManager.getNetworkOperator();
                            NetworkMonitor.this.log("IS_CHINA_AREA =" + NetworkMonitor.IS_CHINA_AREA + ", operator =" + operator);
                            if (!(operator == null || operator.length() == 0 || !operator.startsWith(NetworkMonitor.COUNTRY_CODE_CN)) || NetworkMonitor.IS_CHINA_AREA) {
                                NetworkMonitor.this.log("NetworkMonitor isCaptivePortal transit to link baidu");
                                probeResult = NetworkMonitor.this.isCaptivePortal(NetworkMonitor.BAKUP_SERVER, "/");
                                if (probeResult.mHttpResponseCode >= 200 && probeResult.mHttpResponseCode <= 399 && probeResult.mHttpResponseCode != 301 && probeResult.mHttpResponseCode != 302) {
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
                    } else if (!NetworkMonitor.this.isCheckCompletedByWifiPro() || !NetworkMonitor.this.httpReachable) {
                        probeResult.mHttpResponseCode = NetworkMonitor.this.getRespCodeByWifiPro();
                        if (NetworkMonitor.this.mNetworkAgentInfo.lastCaptivePortalDetected && probeResult.mHttpResponseCode == 599) {
                            probeResult.mHttpResponseCode = 302;
                        }
                        if (probeResult.mHttpResponseCode != 599) {
                            NetworkMonitor.this.sendNetworkConditionsBroadcast(true, probeResult.mHttpResponseCode != 204, NetworkMonitor.this.getReqTimestamp(), NetworkMonitor.this.getRespTimestamp());
                        }
                    } else {
                        NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                        return true;
                    }
                    HwServiceFactory.getHwConnectivityManager().captivePortalCheckCompleted(NetworkMonitor.this.mContext, (probeResult.mHttpResponseCode == 204 || probeResult.mHttpResponseCode == 599) ? false : true);
                    if (probeResult.isSuccessful()) {
                        boolean unused = NetworkMonitor.this.httpReachable = true;
                        NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingPrivateDnsState);
                    } else if (!probeResult.isPortal() || NetworkMonitor.ADD_CRICKET_WIFI_MANAGER) {
                        Message msg = NetworkMonitor.this.obtainMessage(NetworkMonitor.CMD_REEVALUATE, NetworkMonitor.access$3204(NetworkMonitor.this), 0);
                        if (!NetworkMonitor.this.isWifiProEnabled() || NetworkMonitor.this.isCheckCompletedByWifiPro() || needCaptivePortalForMobile) {
                            NetworkMonitor.this.sendMessageDelayed(msg, (long) this.mReevaluateDelayMs);
                            NetworkMonitor.this.logNetworkEvent(3);
                            NetworkMonitor.this.notifyNetworkTestResultInvalid(probeResult.redirectUrl);
                            if (this.mAttempts >= 5) {
                                TrafficStats.clearThreadStatsUid();
                            }
                            this.mReevaluateDelayMs *= 2;
                            if (this.mReevaluateDelayMs > 600000) {
                                this.mReevaluateDelayMs = 600000;
                            }
                        } else {
                            this.mReevaluateDelayMs *= 2;
                            NetworkMonitor.this.sendMessageDelayed(msg, (long) NetworkMonitor.this.resetReevaluateDelayMs(this.mReevaluateDelayMs));
                            return true;
                        }
                    } else {
                        CaptivePortalProbeResult unused2 = NetworkMonitor.this.mLastPortalProbeResult = probeResult;
                        if (!TextUtils.isEmpty(NetworkMonitor.this.getCaptiveUsedServer())) {
                            Settings.Global.putString(NetworkMonitor.this.mContext.getContentResolver(), "captive_portal_server", NetworkMonitor.this.getCaptiveUsedServer());
                        } else if (!TextUtils.isEmpty(probeResult.detectUrl)) {
                            NetworkMonitor.this.log("used server is null, use detect url");
                            Settings.Global.putString(NetworkMonitor.this.mContext.getContentResolver(), "captive_portal_server", probeResult.detectUrl);
                        } else {
                            NetworkMonitor.this.log("portal server is null, clear it");
                            Settings.Global.putString(NetworkMonitor.this.mContext.getContentResolver(), "captive_portal_server", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        }
                        if (!NetworkMonitor.this.isWifiProEnabled() || needCaptivePortalForMobile) {
                            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.redirectUrl));
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mCaptivePortalState);
                        } else {
                            NetworkMonitor.this.reportPortalNetwork(NetworkMonitor.this.mConnectivityServiceHandler, NetworkMonitor.this.mNetId, probeResult.redirectUrl);
                            boolean unused3 = NetworkMonitor.this.httpReachable = true;
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mCaptivePortalState);
                            return true;
                        }
                    }
                    return true;
                }
                NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                return true;
            } else if (i == NetworkMonitor.CMD_FORCE_REEVALUATION) {
                NetworkMonitor.this.log("EvaluatingState handle CMD_FORCE_REEVALUATION");
                if (this.mAttempts < 5) {
                    z = true;
                }
                return z;
            } else if (i != NetworkMonitor.CMD_EVALUATE_PRIVATE_DNS) {
                NetworkMonitor.this.log(NetworkMonitor.this.mNetId + ":EvaluatingState not handle " + message2.what);
                return false;
            } else {
                NetworkMonitor.this.log("EvaluatingState defer CMD_EVALUATE_PRIVATE_DNS");
                NetworkMonitor.this.deferMessage(message2);
                return true;
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

        private EvaluationResult(boolean isValidated2) {
            this.isValidated = isValidated2;
        }
    }

    private class MaybeNotifyState extends State {
        private MaybeNotifyState() {
        }

        /* JADX WARNING: type inference failed for: r5v2, types: [com.android.server.connectivity.NetworkMonitor$MaybeNotifyState$1, android.os.IBinder] */
        public boolean processMessage(Message message) {
            if (message.what != 532491) {
                return false;
            }
            boolean isLaunchedByChrome = 1 == message.arg1;
            if (SystemProperties.getBoolean("runtime.hwwifi.portal_webview_support", false) || isLaunchedByChrome) {
                Intent intent = new Intent("android.net.conn.CAPTIVE_PORTAL");
                intent.putExtra("android.net.extra.NETWORK", new Network(NetworkMonitor.this.mNetwork));
                intent.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new ICaptivePortal.Stub() {
                    public void appResponse(int response) {
                        if (response == 2) {
                            NetworkMonitor.this.mContext.enforceCallingPermission("android.permission.CONNECTIVITY_INTERNAL", "CaptivePortal");
                        }
                        NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_CAPTIVE_PORTAL_APP_FINISHED, response);
                    }
                }));
                CaptivePortalProbeResult probeRes = NetworkMonitor.this.mLastPortalProbeResult;
                String usedUrl = Settings.Global.getString(NetworkMonitor.this.mContext.getContentResolver(), "captive_portal_server");
                if (TextUtils.isEmpty(usedUrl) || !usedUrl.startsWith("http")) {
                    intent.putExtra("android.net.extra.CAPTIVE_PORTAL_URL", probeRes.detectUrl);
                } else {
                    NetworkMonitor networkMonitor = NetworkMonitor.this;
                    networkMonitor.log("use the portal url from the settings" + usedUrl);
                    intent.putExtra("android.net.extra.CAPTIVE_PORTAL_URL", usedUrl);
                }
                if (probeRes.probeSpec != null) {
                    intent.putExtra("android.net.extra.CAPTIVE_PORTAL_PROBE_SPEC", probeRes.probeSpec.getEncodedSpec());
                }
                intent.putExtra("android.net.extra.CAPTIVE_PORTAL_USER_AGENT", NetworkMonitor.this.mCaptivePortalUserAgent);
                intent.setFlags(272629760);
                NetworkMonitor.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
            } else {
                try {
                    HwServiceFactory.getHwConnectivityManager().startBrowserOnClickNotification(NetworkMonitor.this.mContext, new URL(NetworkMonitor.getCaptivePortalServerHttpUrl(NetworkMonitor.this.mContext)).toString());
                } catch (MalformedURLException e) {
                    NetworkMonitor networkMonitor2 = NetworkMonitor.this;
                    networkMonitor2.log("MalformedURLException " + e);
                }
            }
            return true;
        }

        public void exit() {
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 0, NetworkMonitor.this.mNetId, null));
        }
    }

    @VisibleForTesting
    public interface NetworkMonitorSettings {
        public static final NetworkMonitorSettings DEFAULT = new DefaultNetworkMonitorSettings();

        int getSetting(Context context, String str, int i);

        String getSetting(Context context, String str, String str2);
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

    private class ValidatedState extends State {
        private ValidatedState() {
        }

        public void enter() {
            NetworkMonitor.this.log("ValidatedState enter");
            NetworkMonitor.this.maybeLogEvaluationResult(NetworkMonitor.this.networkEventType(NetworkMonitor.this.validationStage(), EvaluationResult.VALIDATED));
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 0, NetworkMonitor.this.mNetId, null));
            int unused = NetworkMonitor.this.mValidations = NetworkMonitor.this.mValidations + 1;
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 532481) {
                NetworkMonitor.this.log("ValidatedState handle CMD_NETWORK_CONNECTED");
                NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
            } else if (i != NetworkMonitor.CMD_EVALUATE_PRIVATE_DNS) {
                return false;
            } else {
                NetworkMonitor.this.log("ValidatedState handle CMD_EVALUATE_PRIVATE_DNS");
                NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingPrivateDnsState);
            }
            return true;
        }
    }

    enum ValidationStage {
        FIRST_VALIDATION(true),
        REVALIDATION(false);
        
        final boolean isFirstValidation;

        private ValidationStage(boolean isFirstValidation2) {
            this.isFirstValidation = isFirstValidation2;
        }
    }

    static /* synthetic */ int access$3204(NetworkMonitor x0) {
        int i = x0.mReevaluateToken + 1;
        x0.mReevaluateToken = i;
        return i;
    }

    static {
        boolean z = false;
        if (SystemProperties.get("ro.config.hw_opta", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS).equals(DOCOMO_HW_OPTA) && SystemProperties.get("ro.config.hw_optb", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS).equals(DOCOMO_HW_OPTB)) {
            z = true;
        }
        IS_DOCOMO = z;
    }

    public static boolean isValidationRequired(NetworkCapabilities dfltNetCap, NetworkCapabilities nc) {
        return dfltNetCap.satisfiedByNetworkCapabilities(nc);
    }

    public NetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest) {
        this(context, handler, networkAgentInfo, defaultRequest, new IpConnectivityLog(), NetworkMonitorSettings.DEFAULT);
    }

    @VisibleForTesting
    protected NetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest, IpConnectivityLog logger, NetworkMonitorSettings settings) {
        super(TAG + networkAgentInfo.name());
        this.mReevaluateToken = 0;
        this.mUidResponsibleForReeval = -1;
        this.mPrivateDnsProviderHostname = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        this.mValidations = 0;
        this.httpReachable = false;
        this.mUserDoesNotWant = false;
        this.mDontDisplaySigninNotification = false;
        this.systemReady = false;
        this.mDefaultState = new DefaultState();
        this.mValidatedState = new ValidatedState();
        this.mMaybeNotifyState = new MaybeNotifyState();
        this.mEvaluatingState = new EvaluatingState();
        this.mCaptivePortalState = new CaptivePortalState();
        this.mEvaluatingPrivateDnsState = new EvaluatingPrivateDnsState();
        this.mLaunchCaptivePortalAppBroadcastReceiver = null;
        this.validationLogs = new LocalLog(20);
        this.mUrlHeadFieldLocation = null;
        this.mEvaluationTimer = new Stopwatch();
        this.mLastPortalProbeResult = CaptivePortalProbeResult.FAILED;
        this.mNextFallbackUrlIndex = 0;
        setDbg(false);
        this.mContext = context;
        this.mMetricsLog = logger;
        this.mConnectivityServiceHandler = handler;
        this.mSettings = settings;
        this.mNetworkAgentInfo = networkAgentInfo;
        this.mNetwork = new OneAddressPerFamilyNetwork(networkAgentInfo.network());
        this.mNetId = this.mNetwork.netId;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mDefaultRequest = defaultRequest;
        addState(this.mDefaultState);
        addState(this.mMaybeNotifyState, this.mDefaultState);
        addState(this.mEvaluatingState, this.mMaybeNotifyState);
        addState(this.mCaptivePortalState, this.mMaybeNotifyState);
        addState(this.mEvaluatingPrivateDnsState, this.mDefaultState);
        addState(this.mValidatedState, this.mDefaultState);
        setInitialState(this.mDefaultState);
        this.mIsCaptivePortalCheckEnabled = getIsCaptivePortalCheckEnabled();
        this.mUseHttps = getUseHttpsValidation();
        this.mCaptivePortalUserAgent = getCaptivePortalUserAgent();
        if (this.mNetworkAgentInfo == null || this.mNetworkAgentInfo.networkInfo == null || this.mNetworkAgentInfo.networkInfo.getType() != 7) {
            this.mCaptivePortalHttpsUrl = makeURL(getCaptivePortalServerHttpsUrl());
            this.mCaptivePortalHttpUrl = makeURL(getCaptivePortalServerHttpUrl(settings, context));
        } else {
            this.mCaptivePortalHttpsUrl = makeURL(DEFAULT_HTTP_AND_HTTPS_FOR_BLUETOOTH);
            this.mCaptivePortalHttpUrl = makeURL(DEFAULT_HTTP_AND_HTTPS_FOR_BLUETOOTH);
        }
        this.mCaptivePortalFallbackUrls = makeCaptivePortalFallbackUrls();
        this.mCaptivePortalFallbackSpecs = makeCaptivePortalFallbackProbeSpecs();
        start();
    }

    public void forceReevaluation(int responsibleUid) {
        sendMessage(CMD_FORCE_REEVALUATION, responsibleUid, 0);
    }

    public void notifyPrivateDnsSettingsChanged(DnsManager.PrivateDnsConfig newCfg) {
        removeMessages(CMD_PRIVATE_DNS_SETTINGS_CHANGED);
        sendMessage(CMD_PRIVATE_DNS_SETTINGS_CHANGED, newCfg);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Log.d(TAG + "/" + this.mNetworkAgentInfo.name(), s);
    }

    private void validationLog(int probeType, Object url, String msg) {
        validationLog(String.format("%s %s %s", new Object[]{ValidationProbeEvent.getProbeName(probeType), url, msg}));
    }

    /* access modifiers changed from: private */
    public void validationLog(String s) {
        log(s);
        this.validationLogs.log(s);
    }

    public LocalLog.ReadOnlyLocalLog getValidationLogs() {
        return this.validationLogs.readOnlyLocalLog();
    }

    /* access modifiers changed from: private */
    public ValidationStage validationStage() {
        return this.mValidations == 0 ? ValidationStage.FIRST_VALIDATION : ValidationStage.REVALIDATION;
    }

    /* access modifiers changed from: private */
    public boolean isValidationRequired() {
        return isValidationRequired(this.mDefaultRequest.networkCapabilities, this.mNetworkAgentInfo.networkCapabilities);
    }

    /* access modifiers changed from: private */
    public void notifyNetworkTestResultInvalid(Object obj) {
        this.mConnectivityServiceHandler.sendMessage(obtainMessage(EVENT_NETWORK_TESTED, 1, this.mNetId, obj));
    }

    public boolean getIsCaptivePortalCheckEnabled() {
        return this.mSettings.getSetting(this.mContext, "captive_portal_mode", 1) != 0;
    }

    public boolean getUseHttpsValidation() {
        return this.mSettings.getSetting(this.mContext, "captive_portal_use_https", 1) == 1;
    }

    public boolean getWifiScansAlwaysAvailableDisabled() {
        return this.mSettings.getSetting(this.mContext, "wifi_scan_always_enabled", 0) == 0;
    }

    private String getCaptivePortalServerHttpsUrl() {
        return this.mSettings.getSetting(this.mContext, "captive_portal_https_url", DEFAULT_HTTPS_URL);
    }

    public static String getCaptivePortalServerHttpUrl(Context context) {
        return getCaptivePortalServerHttpUrl(NetworkMonitorSettings.DEFAULT, context);
    }

    public static String getCaptivePortalServerHttpUrl(NetworkMonitorSettings settings, Context context) {
        return settings.getSetting(context, "captive_portal_http_url", DEFAULT_HTTP_URL);
    }

    private URL[] makeCaptivePortalFallbackUrls() {
        try {
            String firstUrl = this.mSettings.getSetting(this.mContext, "captive_portal_fallback_url", DEFAULT_FALLBACK_URL);
            String joinedUrls = firstUrl + "," + this.mSettings.getSetting(this.mContext, "captive_portal_other_fallback_urls", DEFAULT_OTHER_FALLBACK_URLS);
            List<URL> urls = new ArrayList<>();
            for (String s : joinedUrls.split(",")) {
                URL u = makeURL(s);
                if (u != null) {
                    urls.add(u);
                }
            }
            if (urls.isEmpty()) {
                Log.e(TAG, String.format("could not create any url from %s", new Object[]{joinedUrls}));
            }
            return (URL[]) urls.toArray(new URL[urls.size()]);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing configured fallback URLs", e);
            return new URL[0];
        }
    }

    private CaptivePortalProbeSpec[] makeCaptivePortalFallbackProbeSpecs() {
        try {
            String settingsValue = this.mSettings.getSetting(this.mContext, "captive_portal_fallback_probe_specs", (String) null);
            if (TextUtils.isEmpty(settingsValue)) {
                return null;
            }
            return CaptivePortalProbeSpec.parseCaptivePortalProbeSpecs(settingsValue);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing configured fallback probe specs", e);
            return null;
        }
    }

    private String getCaptivePortalUserAgent() {
        return this.mSettings.getSetting(this.mContext, "captive_portal_user_agent", DEFAULT_USER_AGENT);
    }

    private URL nextFallbackUrl() {
        if (this.mCaptivePortalFallbackUrls.length == 0) {
            return null;
        }
        int idx = Math.abs(this.mNextFallbackUrlIndex) % this.mCaptivePortalFallbackUrls.length;
        this.mNextFallbackUrlIndex += new Random().nextInt();
        return this.mCaptivePortalFallbackUrls[idx];
    }

    private CaptivePortalProbeSpec nextFallbackSpec() {
        if (ArrayUtils.isEmpty(this.mCaptivePortalFallbackSpecs)) {
            return null;
        }
        return this.mCaptivePortalFallbackSpecs[Math.abs(new Random().nextInt()) % this.mCaptivePortalFallbackSpecs.length];
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public CaptivePortalProbeResult isCaptivePortal(String urlString) {
        CaptivePortalProbeResult result;
        String str = urlString;
        if (!this.mIsCaptivePortalCheckEnabled) {
            validationLog("Validation disabled.");
            return CaptivePortalProbeResult.SUCCESS;
        }
        URL pacUrl = null;
        URL httpsUrl = this.mCaptivePortalHttpsUrl;
        URL httpUrl = this.mCaptivePortalHttpUrl;
        if (str != null && str.contains(BAKUP_SERVER)) {
            try {
                httpUrl = new URL(str);
                httpsUrl = new URL(str);
            } catch (MalformedURLException e) {
                validationLog("Bad validation URL");
                return CaptivePortalProbeResult.FAILED;
            }
        }
        URL httpsUrl2 = httpsUrl;
        URL httpUrl2 = httpUrl;
        ProxyInfo proxyInfo = this.mNetworkAgentInfo.linkProperties.getHttpProxy();
        if (proxyInfo != null && !Uri.EMPTY.equals(proxyInfo.getPacFileUrl())) {
            pacUrl = makeURL(proxyInfo.getPacFileUrl().toString());
            if (pacUrl == null) {
                return CaptivePortalProbeResult.FAILED;
            }
        }
        URL pacUrl2 = pacUrl;
        if (pacUrl2 == null && (httpUrl2 == null || httpsUrl2 == null)) {
            return CaptivePortalProbeResult.FAILED;
        }
        long startTime = SystemClock.elapsedRealtime();
        if (pacUrl2 != null) {
            result = sendDnsAndHttpProbes(null, pacUrl2, 3);
        } else if (this.mUseHttps) {
            result = sendParallelHttpProbes(proxyInfo, httpsUrl2, httpUrl2);
        } else {
            result = sendDnsAndHttpProbes(proxyInfo, httpUrl2, 1);
        }
        CaptivePortalProbeResult result2 = result;
        sendNetworkConditionsBroadcast(true, result2.isPortal(), startTime, SystemClock.elapsedRealtime());
        return result2;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public CaptivePortalProbeResult isCaptivePortal() {
        return isCaptivePortal(getCaptivePortalServerHttpsUrl());
    }

    /* access modifiers changed from: protected */
    public CaptivePortalProbeResult isCaptivePortal(String server_url, String page) {
        if (!server_url.startsWith("http://") && !server_url.startsWith("https://")) {
            server_url = "http://" + server_url;
        }
        if (!server_url.endsWith(page)) {
            server_url = server_url + page;
        }
        return isCaptivePortal(server_url);
    }

    /* access modifiers changed from: private */
    public CaptivePortalProbeResult sendDnsAndHttpProbes(ProxyInfo proxy, URL url, int probeType) {
        sendDnsProbe(proxy != null ? proxy.getHost() : url.getHost());
        return sendHttpProbe(url, probeType, null);
    }

    private void sendDnsProbe(String host) {
        int result;
        String connectInfo;
        if (!TextUtils.isEmpty(host)) {
            String probeName = ValidationProbeEvent.getProbeName(0);
            Stopwatch watch = new Stopwatch().start();
            try {
                InetAddress[] addresses = this.mNetwork.getAllByName(host);
                StringBuffer buffer = new StringBuffer();
                for (InetAddress address : addresses) {
                    buffer.append(',');
                    buffer.append(address.getHostAddress());
                }
                result = 1;
                connectInfo = "OK " + buffer.substring(1);
            } catch (UnknownHostException e) {
                result = 0;
                connectInfo = "FAIL";
            }
            long latency = watch.stop();
            validationLog(0, host, String.format("%dms %s", new Object[]{Long.valueOf(latency), connectInfo}));
            logValidationProbe(latency, 0, result);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0126  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0138  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0142  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x014e  */
    @VisibleForTesting
    public CaptivePortalProbeResult sendHttpProbe(URL url, int probeType, CaptivePortalProbeSpec probeSpec) {
        Stopwatch probeTimer;
        URL url2 = url;
        int i = probeType;
        CaptivePortalProbeSpec captivePortalProbeSpec = probeSpec;
        HttpURLConnection urlConnection = null;
        int httpResponseCode = 599;
        String redirectUrl = null;
        Stopwatch probeTimer2 = new Stopwatch().start();
        int oldTag = TrafficStats.getAndSetThreadStatsTag(-190);
        try {
            urlConnection = (HttpURLConnection) this.mNetwork.openConnection(url2);
            urlConnection.setInstanceFollowRedirects(i == 3);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setUseCaches(false);
            if (this.mCaptivePortalUserAgent != null) {
                try {
                    urlConnection.setRequestProperty("User-Agent", this.mCaptivePortalUserAgent);
                } catch (IOException e) {
                    e = e;
                    probeTimer = probeTimer2;
                } catch (Throwable th) {
                    th = th;
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    TrafficStats.setThreadStatsTag(oldTag);
                    throw th;
                }
            }
            String requestHeader = urlConnection.getRequestProperties().toString();
            long requestTimestamp = SystemClock.elapsedRealtime();
            httpResponseCode = urlConnection.getResponseCode();
            redirectUrl = urlConnection.getHeaderField("location");
            try {
                long responseTimestamp = SystemClock.elapsedRealtime();
                this.mUrlHeadFieldLocation = urlConnection.getHeaderField("Location");
                StringBuilder sb = new StringBuilder();
                sb.append("time=");
                String redirectUrl2 = redirectUrl;
                probeTimer = probeTimer2;
                try {
                    sb.append(responseTimestamp - requestTimestamp);
                    sb.append("ms ret=");
                    sb.append(httpResponseCode);
                    sb.append(" request=");
                    sb.append(requestHeader);
                    sb.append(" headers=");
                    sb.append(urlConnection.getHeaderFields());
                    validationLog(i, url2, sb.toString());
                    if (httpResponseCode == 200) {
                        if (i == 3) {
                            validationLog(i, url2, "PAC fetch 200 response interpreted as 204 response.");
                            httpResponseCode = 204;
                        } else if (urlConnection.getContentLengthLong() == 0) {
                            validationLog(i, url2, "200 response with Content-length=0 interpreted as 204 response.");
                            httpResponseCode = 204;
                        } else if (urlConnection.getContentLengthLong() == -1 && urlConnection.getInputStream().read() == -1) {
                            validationLog(i, url2, "Empty 200 response interpreted as 204 response.");
                            httpResponseCode = 204;
                        }
                    }
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    TrafficStats.setThreadStatsTag(oldTag);
                    redirectUrl = redirectUrl2;
                } catch (IOException e2) {
                    e = e2;
                    redirectUrl = redirectUrl2;
                    try {
                        validationLog(i, url2, "Probe failed with exception " + e);
                        if (urlConnection != null) {
                        }
                        TrafficStats.setThreadStatsTag(oldTag);
                        logValidationProbe(probeTimer.stop(), i, httpResponseCode);
                        if (captivePortalProbeSpec != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        Stopwatch stopwatch = probeTimer;
                        String str = redirectUrl;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Stopwatch stopwatch2 = probeTimer;
                    if (urlConnection != null) {
                    }
                    TrafficStats.setThreadStatsTag(oldTag);
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                String str2 = redirectUrl;
                probeTimer = probeTimer2;
                validationLog(i, url2, "Probe failed with exception " + e);
                if (urlConnection != null) {
                }
                TrafficStats.setThreadStatsTag(oldTag);
                logValidationProbe(probeTimer.stop(), i, httpResponseCode);
                if (captivePortalProbeSpec != null) {
                }
            } catch (Throwable th4) {
                th = th4;
                String str3 = redirectUrl;
                if (urlConnection != null) {
                }
                TrafficStats.setThreadStatsTag(oldTag);
                throw th;
            }
        } catch (IOException e4) {
            e = e4;
            probeTimer = probeTimer2;
            validationLog(i, url2, "Probe failed with exception " + e);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
            logValidationProbe(probeTimer.stop(), i, httpResponseCode);
            if (captivePortalProbeSpec != null) {
            }
        } catch (Throwable th5) {
            th = th5;
            if (urlConnection != null) {
            }
            TrafficStats.setThreadStatsTag(oldTag);
            throw th;
        }
        logValidationProbe(probeTimer.stop(), i, httpResponseCode);
        if (captivePortalProbeSpec != null) {
            return new CaptivePortalProbeResult(httpResponseCode, redirectUrl, url.toString());
        }
        return captivePortalProbeSpec.getResult(httpResponseCode, redirectUrl);
    }

    private CaptivePortalProbeResult sendParallelHttpProbes(ProxyInfo proxy, URL httpsUrl, URL httpUrl) {
        CountDownLatch latch = new CountDownLatch(2);
        ProxyInfo proxyInfo = proxy;
        URL url = httpsUrl;
        URL url2 = httpUrl;
        CountDownLatch countDownLatch = latch;
        AnonymousClass1ProbeThread httpProbe = new Thread(true, proxyInfo, url, url2, countDownLatch) {
            private final boolean mIsHttps;
            private volatile CaptivePortalProbeResult mResult = CaptivePortalProbeResult.FAILED;
            final /* synthetic */ URL val$httpUrl;
            final /* synthetic */ URL val$httpsUrl;
            final /* synthetic */ CountDownLatch val$latch;
            final /* synthetic */ ProxyInfo val$proxy;

            {
                this.val$proxy = r3;
                this.val$httpsUrl = r4;
                this.val$httpUrl = r5;
                this.val$latch = r6;
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
        };
        AnonymousClass1ProbeThread r2 = new Thread(false, proxyInfo, url, url2, countDownLatch) {
            private final boolean mIsHttps;
            private volatile CaptivePortalProbeResult mResult = CaptivePortalProbeResult.FAILED;
            final /* synthetic */ URL val$httpUrl;
            final /* synthetic */ URL val$httpsUrl;
            final /* synthetic */ CountDownLatch val$latch;
            final /* synthetic */ ProxyInfo val$proxy;

            {
                this.val$proxy = r3;
                this.val$httpsUrl = r4;
                this.val$httpUrl = r5;
                this.val$latch = r6;
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
        };
        AnonymousClass1ProbeThread httpProbe2 = r2;
        try {
            httpProbe.start();
            httpProbe2.start();
            latch.await(3000, TimeUnit.MILLISECONDS);
            CaptivePortalProbeResult httpsResult = httpProbe.result();
            CaptivePortalProbeResult httpResult = httpProbe2.result();
            if (httpResult.isPortal()) {
                return httpResult;
            }
            if (httpsResult.isPortal() || httpsResult.isSuccessful()) {
                return httpsResult;
            }
            CaptivePortalProbeSpec probeSpec = nextFallbackSpec();
            URL fallbackUrl = probeSpec != null ? probeSpec.getUrl() : nextFallbackUrl();
            if (fallbackUrl != null) {
                CaptivePortalProbeResult result = sendHttpProbe(fallbackUrl, 4, probeSpec);
                if (result.isPortal()) {
                    return result;
                }
            }
            try {
                httpProbe2.join();
                if (httpProbe2.result().isPortal()) {
                    return httpProbe2.result();
                }
                httpProbe.join();
                return httpProbe.result();
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

    /* access modifiers changed from: private */
    public void sendNetworkConditionsBroadcast(boolean responseReceived, boolean isCaptivePortal, long requestTimestampMs, long responseTimestampMs) {
        if (!getWifiScansAlwaysAvailableDisabled() && this.systemReady) {
            Intent latencyBroadcast = new Intent(ConnectivityConstants.ACTION_NETWORK_CONDITIONS_MEASURED);
            switch (this.mNetworkAgentInfo.networkInfo.getType()) {
                case 0:
                    latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_NETWORK_TYPE, this.mTelephonyManager.getNetworkType());
                    List<CellInfo> info = this.mTelephonyManager.getAllCellInfo();
                    if (info != null) {
                        int numRegisteredCellInfo = 0;
                        for (CellInfo cellInfo : info) {
                            if (cellInfo.isRegistered()) {
                                numRegisteredCellInfo++;
                                if (numRegisteredCellInfo <= 1) {
                                    if (cellInfo instanceof CellInfoCdma) {
                                        latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_CELL_ID, ((CellInfoCdma) cellInfo).getCellIdentity());
                                    } else if (cellInfo instanceof CellInfoGsm) {
                                        latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_CELL_ID, ((CellInfoGsm) cellInfo).getCellIdentity());
                                    } else if (cellInfo instanceof CellInfoLte) {
                                        latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_CELL_ID, ((CellInfoLte) cellInfo).getCellIdentity());
                                    } else if (cellInfo instanceof CellInfoWcdma) {
                                        latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_CELL_ID, ((CellInfoWcdma) cellInfo).getCellIdentity());
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            }
                        }
                        break;
                    } else {
                        return;
                    }
                case 1:
                    WifiInfo currentWifiInfo = this.mWifiManager.getConnectionInfo();
                    if (currentWifiInfo != null) {
                        latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_SSID, currentWifiInfo.getSSID());
                        latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_BSSID, currentWifiInfo.getBSSID());
                        break;
                    } else {
                        return;
                    }
                default:
                    return;
            }
            latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_CONNECTIVITY_TYPE, this.mNetworkAgentInfo.networkInfo.getType());
            latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_RESPONSE_RECEIVED, responseReceived);
            latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_REQUEST_TIMESTAMP_MS, requestTimestampMs);
            if (responseReceived) {
                latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_IS_CAPTIVE_PORTAL, isCaptivePortal);
                latencyBroadcast.putExtra(ConnectivityConstants.EXTRA_RESPONSE_TIMESTAMP_MS, responseTimestampMs);
            }
            this.mContext.sendBroadcastAsUser(latencyBroadcast, UserHandle.CURRENT, ConnectivityConstants.PERMISSION_ACCESS_NETWORK_CONDITIONS);
        }
    }

    /* access modifiers changed from: private */
    public void logNetworkEvent(int evtype) {
        this.mMetricsLog.log(this.mNetId, this.mNetworkAgentInfo.networkCapabilities.getTransportTypes(), new NetworkEvent(evtype));
    }

    /* access modifiers changed from: private */
    public int networkEventType(ValidationStage s, EvaluationResult r) {
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

    /* access modifiers changed from: private */
    public void maybeLogEvaluationResult(int evtype) {
        if (this.mEvaluationTimer.isRunning()) {
            this.mMetricsLog.log(this.mNetId, this.mNetworkAgentInfo.networkCapabilities.getTransportTypes(), new NetworkEvent(evtype, this.mEvaluationTimer.stop()));
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

    /* access modifiers changed from: private */
    public String parseHostByLocation(String location) {
        if (location != null) {
            int start = 0;
            if (location.startsWith("http://")) {
                start = 7;
            } else if (location.startsWith("https://")) {
                start = 8;
            }
            int end = location.indexOf("/", start);
            if (end == -1) {
                end = location.length();
            }
            if (start <= end && end <= location.length()) {
                return location.substring(start, end);
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void setWifiConfigWithPortalConnect() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            int networkId = wifiInfo.getNetworkId();
            if (-1 != networkId) {
                List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
                if (configs != null) {
                    for (WifiConfiguration config : configs) {
                        if (config != null && networkId == config.networkId) {
                            log("update wifi config(netId:" + config.networkId + ",ssid:" + config.SSID + "), set isPortalConnect-->true");
                            config.isPortalConnect = true;
                            this.mWifiManager.updateNetwork(config);
                            return;
                        }
                    }
                }
            }
        }
    }

    public String getCaptiveUsedServer() {
        return null;
    }
}
