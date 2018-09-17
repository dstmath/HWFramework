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
import android.net.metrics.NetworkEvent;
import android.net.metrics.ValidationProbeEvent;
import android.net.util.Stopwatch;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
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
import com.android.internal.util.WakeupMessage;
import com.android.server.AbsNetworkMonitor;
import com.android.server.HwConnectivityManager;
import com.android.server.HwServiceFactory;
import com.android.server.location.FlpHardwareProvider;
import com.android.server.location.LocationFudger;
import com.android.server.wm.WindowState;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkMonitor extends AbsNetworkMonitor {
    public static final String ACTION_NETWORK_CONDITIONS_MEASURED = "android.net.conn.NETWORK_CONDITIONS_MEASURED";
    private static final String BAKUP_SERVER = "www.baidu.com";
    private static final String BAKUP_SERV_PAGE = "/";
    private static final int BASE = 532480;
    private static final int BLAME_FOR_EVALUATION_ATTEMPTS = 5;
    private static final int CAPTIVE_PORTAL_REEVALUATE_DELAY_MS = 600000;
    private static final int CMD_CAPTIVE_PORTAL_APP_FINISHED = 532489;
    private static final int CMD_CAPTIVE_PORTAL_RECHECK = 532492;
    public static final int CMD_FORCE_REEVALUATION = 532488;
    private static final int CMD_LAUNCH_CAPTIVE_PORTAL_APP = 532491;
    private static final int CMD_LINGER_EXPIRED = 532484;
    public static final int CMD_NETWORK_CONNECTED = 532481;
    public static final int CMD_NETWORK_DISCONNECTED = 532487;
    public static final int CMD_NETWORK_LINGER = 532483;
    private static final int CMD_REEVALUATE = 532486;
    private static final boolean DBG = true;
    private static int DEFAULT_LINGER_DELAY_MS = 0;
    private static final String DEFAULT_SERVER = "connectivitycheck.gstatic.com";
    private static final String DEFAULT_SERV_PAGE = "/generate_204";
    public static final int EVENT_NETWORK_LINGER_COMPLETE = 532485;
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
    private static final String LINGER_DELAY_PROPERTY = "persist.netmon.linger";
    private static final int MAX_REEVALUATE_DELAY_MS = 600000;
    public static final int NETWORK_TEST_RESULT_INVALID = 1;
    public static final int NETWORK_TEST_RESULT_VALID = 0;
    private static final String PERMISSION_ACCESS_NETWORK_CONDITIONS = "android.permission.ACCESS_NETWORK_CONDITIONS";
    private static final String SERVER_BAIDU = "baidu";
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String TAG = null;
    private final AlarmManager mAlarmManager;
    private final State mCaptivePortalState;
    private final Handler mConnectivityServiceHandler;
    private final Context mContext;
    private final NetworkRequest mDefaultRequest;
    private final State mDefaultState;
    private boolean mDontDisplaySigninNotification;
    private final State mEvaluatingState;
    private final Stopwatch mEvaluationTimer;
    private boolean mIsCaptivePortalCheckEnabled;
    private CustomIntentReceiver mLaunchCaptivePortalAppBroadcastReceiver;
    private final int mLingerDelayMs;
    private int mLingerToken;
    private final State mLingeringState;
    private final State mMaybeNotifyState;
    private final int mNetId;
    private final NetworkAgentInfo mNetworkAgentInfo;
    private int mReevaluateToken;
    private final TelephonyManager mTelephonyManager;
    private int mUidResponsibleForReeval;
    private String mUrlHeadFieldLocation;
    private boolean mUseHttps;
    private boolean mUserDoesNotWant;
    private final State mValidatedState;
    private final WifiManager mWifiManager;
    public boolean systemReady;
    private final LocalLog validationLogs;

    /* renamed from: com.android.server.connectivity.NetworkMonitor.1ProbeThread */
    final class AnonymousClass1ProbeThread extends Thread {
        private final boolean mIsHttps;
        private volatile CaptivePortalProbeResult mResult;
        final /* synthetic */ AtomicReference val$finalResult;
        final /* synthetic */ URL val$httpUrl;
        final /* synthetic */ URL val$httpsUrl;
        final /* synthetic */ CountDownLatch val$latch;

        public AnonymousClass1ProbeThread(boolean isHttps, URL val$httpsUrl, URL val$httpUrl, AtomicReference val$finalResult, CountDownLatch val$latch) {
            this.val$httpsUrl = val$httpsUrl;
            this.val$httpUrl = val$httpUrl;
            this.val$finalResult = val$finalResult;
            this.val$latch = val$latch;
            this.mIsHttps = isHttps;
        }

        public CaptivePortalProbeResult getResult() {
            return this.mResult;
        }

        public void run() {
            if (this.mIsHttps) {
                this.mResult = NetworkMonitor.this.sendHttpProbe(this.val$httpsUrl, 2);
            } else {
                this.mResult = NetworkMonitor.this.sendHttpProbe(this.val$httpUrl, NetworkMonitor.NETWORK_TEST_RESULT_INVALID);
            }
            if ((this.mIsHttps && this.mResult.isSuccessful()) || (!this.mIsHttps && this.mResult.isPortal())) {
                this.val$finalResult.compareAndSet(null, this.mResult);
                this.val$latch.countDown();
            }
            this.val$latch.countDown();
        }
    }

    public static final class CaptivePortalProbeResult {
        static final CaptivePortalProbeResult FAILED = null;
        int mHttpResponseCode;
        final String mRedirectUrl;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.connectivity.NetworkMonitor.CaptivePortalProbeResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.connectivity.NetworkMonitor.CaptivePortalProbeResult.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.NetworkMonitor.CaptivePortalProbeResult.<clinit>():void");
        }

        public CaptivePortalProbeResult(int httpResponseCode, String redirectUrl) {
            this.mHttpResponseCode = httpResponseCode;
            this.mRedirectUrl = redirectUrl;
        }

        boolean isSuccessful() {
            return this.mHttpResponseCode == 204 ? NetworkMonitor.DBG : false;
        }

        boolean isPortal() {
            return (isSuccessful() || this.mHttpResponseCode < 200 || this.mHttpResponseCode > 399) ? false : NetworkMonitor.DBG;
        }
    }

    private class CaptivePortalState extends State {
        private static final String ACTION_LAUNCH_CAPTIVE_PORTAL_APP = "android.net.netmon.launchCaptivePortalApp";
        final /* synthetic */ NetworkMonitor this$0;

        /* synthetic */ CaptivePortalState(NetworkMonitor this$0, CaptivePortalState captivePortalState) {
            this(this$0);
        }

        private CaptivePortalState(NetworkMonitor this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            if (this.this$0.mEvaluationTimer.isRunning()) {
                NetworkEvent.logCaptivePortalFound(this.this$0.mNetId, this.this$0.mEvaluationTimer.stop());
                this.this$0.mEvaluationTimer.reset();
            }
            if (!this.this$0.mDontDisplaySigninNotification) {
                if (this.this$0.mLaunchCaptivePortalAppBroadcastReceiver == null) {
                    this.this$0.mLaunchCaptivePortalAppBroadcastReceiver = new CustomIntentReceiver(this.this$0, ACTION_LAUNCH_CAPTIVE_PORTAL_APP, new Random().nextInt(), NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP);
                }
                this.this$0.mConnectivityServiceHandler.sendMessage(this.this$0.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, NetworkMonitor.NETWORK_TEST_RESULT_INVALID, this.this$0.mNetworkAgentInfo.network.netId, this.this$0.mLaunchCaptivePortalAppBroadcastReceiver.getPendingIntent()));
                this.this$0.sendMessageDelayed(NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK, NetworkMonitor.NETWORK_TEST_RESULT_VALID, LocationFudger.FASTEST_INTERVAL_MS);
            }
        }

        public void exit() {
            this.this$0.removeMessages(NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK);
        }
    }

    private class CustomIntentReceiver extends BroadcastReceiver {
        private final String mAction;
        private final int mToken;
        private final int mWhat;
        final /* synthetic */ NetworkMonitor this$0;

        CustomIntentReceiver(NetworkMonitor this$0, String action, int token, int what) {
            this.this$0 = this$0;
            this.mToken = token;
            this.mWhat = what;
            this.mAction = action + "_" + this$0.mNetworkAgentInfo.network.netId + "_" + token;
            this$0.mContext.registerReceiver(this, new IntentFilter(this.mAction));
        }

        public PendingIntent getPendingIntent() {
            Intent intent = new Intent(this.mAction);
            intent.setPackage(this.this$0.mContext.getPackageName());
            return PendingIntent.getBroadcast(this.this$0.mContext, NetworkMonitor.NETWORK_TEST_RESULT_VALID, intent, NetworkMonitor.NETWORK_TEST_RESULT_VALID);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(this.mAction)) {
                this.this$0.sendMessage(this.this$0.obtainMessage(this.mWhat, this.mToken));
            }
        }
    }

    private class DefaultState extends State {
        final /* synthetic */ NetworkMonitor this$0;

        /* synthetic */ DefaultState(NetworkMonitor this$0, DefaultState defaultState) {
            this(this$0);
        }

        private DefaultState(NetworkMonitor this$0) {
            this.this$0 = this$0;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    NetworkEvent.logEvent(this.this$0.mNetId, NetworkMonitor.NETWORK_TEST_RESULT_INVALID);
                    this.this$0.transitionTo(this.this$0.mEvaluatingState);
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_NETWORK_LINGER /*532483*/:
                    this.this$0.log("Lingering");
                    this.this$0.transitionTo(this.this$0.mLingeringState);
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_NETWORK_DISCONNECTED /*532487*/:
                    NetworkEvent.logEvent(this.this$0.mNetId, 7);
                    if (this.this$0.mLaunchCaptivePortalAppBroadcastReceiver != null) {
                        this.this$0.mContext.unregisterReceiver(this.this$0.mLaunchCaptivePortalAppBroadcastReceiver);
                        this.this$0.mLaunchCaptivePortalAppBroadcastReceiver = null;
                    }
                    this.this$0.releaseNetworkPropertyChecker();
                    this.this$0.quit();
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK /*532492*/:
                    this.this$0.log("Forcing reevaluation for UID " + message.arg1);
                    this.this$0.mUidResponsibleForReeval = message.arg1;
                    this.this$0.transitionTo(this.this$0.mEvaluatingState);
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_APP_FINISHED /*532489*/:
                    this.this$0.log("CaptivePortal App responded with " + message.arg1);
                    this.this$0.mUseHttps = false;
                    switch (message.arg1) {
                        case NetworkMonitor.NETWORK_TEST_RESULT_VALID /*0*/:
                            this.this$0.sendMessage(NetworkMonitor.CMD_FORCE_REEVALUATION, NetworkMonitor.NETWORK_TEST_RESULT_VALID, NetworkMonitor.NETWORK_TEST_RESULT_VALID);
                            break;
                        case NetworkMonitor.NETWORK_TEST_RESULT_INVALID /*1*/:
                            this.this$0.mDontDisplaySigninNotification = NetworkMonitor.DBG;
                            this.this$0.mUserDoesNotWant = NetworkMonitor.DBG;
                            this.this$0.mConnectivityServiceHandler.sendMessage(this.this$0.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, NetworkMonitor.NETWORK_TEST_RESULT_INVALID, this.this$0.mNetId, null));
                            this.this$0.mUidResponsibleForReeval = NetworkMonitor.NETWORK_TEST_RESULT_VALID;
                            this.this$0.transitionTo(this.this$0.mEvaluatingState);
                            break;
                        case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                            this.this$0.mDontDisplaySigninNotification = NetworkMonitor.DBG;
                            this.this$0.transitionTo(this.this$0.mValidatedState);
                            break;
                    }
                    return NetworkMonitor.DBG;
                case AbsNetworkMonitor.CMD_NETWORK_ROAMING_CONNECTED /*532581*/:
                    this.this$0.log("DefaultState receive CMD_NETWORK_ROAMING_CONNECTED");
                    this.this$0.resetNetworkMonitor();
                    this.this$0.transitionTo(this.this$0.mEvaluatingState);
                    return NetworkMonitor.DBG;
                default:
                    return NetworkMonitor.DBG;
            }
        }
    }

    private class EvaluatingState extends State {
        private int mAttempts;
        private int mReevaluateDelayMs;
        final /* synthetic */ NetworkMonitor this$0;

        /* synthetic */ EvaluatingState(NetworkMonitor this$0, EvaluatingState evaluatingState) {
            this(this$0);
        }

        private EvaluatingState(NetworkMonitor this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            if (!this.this$0.mEvaluationTimer.isStarted()) {
                this.this$0.mEvaluationTimer.start();
            }
            NetworkMonitor networkMonitor = this.this$0;
            NetworkMonitor networkMonitor2 = this.this$0;
            networkMonitor.sendMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + NetworkMonitor.NETWORK_TEST_RESULT_INVALID, NetworkMonitor.NETWORK_TEST_RESULT_VALID);
            if (this.this$0.mUidResponsibleForReeval != NetworkMonitor.INVALID_UID) {
                TrafficStats.setThreadStatsUid(this.this$0.mUidResponsibleForReeval);
                this.this$0.mUidResponsibleForReeval = NetworkMonitor.INVALID_UID;
            }
            this.mReevaluateDelayMs = NetworkMonitor.INITIAL_REEVALUATE_DELAY_MS;
            this.mAttempts = NetworkMonitor.NETWORK_TEST_RESULT_VALID;
        }

        public boolean processMessage(Message message) {
            boolean z = NetworkMonitor.DBG;
            switch (message.what) {
                case NetworkMonitor.CMD_REEVALUATE /*532486*/:
                    if (message.arg1 != this.this$0.mReevaluateToken || this.this$0.mUserDoesNotWant) {
                        return NetworkMonitor.DBG;
                    }
                    if (this.this$0.mDefaultRequest.networkCapabilities.satisfiedByNetworkCapabilities(this.this$0.mNetworkAgentInfo.networkCapabilities)) {
                        this.mAttempts += NetworkMonitor.NETWORK_TEST_RESULT_INVALID;
                        if (this.this$0.mNetworkAgentInfo.networkInfo.getType() == 0) {
                            this.this$0.transitionTo(this.this$0.mValidatedState);
                            return NetworkMonitor.DBG;
                        }
                        NetworkMonitor networkMonitor;
                        boolean z2;
                        CaptivePortalProbeResult probeResult = new CaptivePortalProbeResult(599, null);
                        if (!this.this$0.isWifiProEnabled()) {
                            probeResult = this.this$0.isCaptivePortal(NetworkMonitor.getCaptivePortalServerUrl(this.this$0.mContext), NetworkMonitor.DEFAULT_SERV_PAGE);
                            if (probeResult.mHttpResponseCode < 200 || probeResult.mHttpResponseCode > 399) {
                                this.this$0.log("NetworkMonitor isCaptivePortal transit to link baidu");
                                probeResult = this.this$0.isCaptivePortal(NetworkMonitor.BAKUP_SERVER, NetworkMonitor.BAKUP_SERV_PAGE);
                                if (probeResult.mHttpResponseCode >= 200 && probeResult.mHttpResponseCode <= 399 && probeResult.mHttpResponseCode != 301 && probeResult.mHttpResponseCode != 302) {
                                    probeResult.mHttpResponseCode = 204;
                                } else if (probeResult.mHttpResponseCode == 301 || probeResult.mHttpResponseCode == 302) {
                                    this.this$0.log("mUrlHeadFieldLocation" + this.this$0.mUrlHeadFieldLocation);
                                    String host = this.this$0.parseHostByLocation(this.this$0.mUrlHeadFieldLocation);
                                    if (host != null && host.contains(NetworkMonitor.SERVER_BAIDU)) {
                                        this.this$0.log("host contains baidu ,change httpResponseCode to 204");
                                        probeResult.mHttpResponseCode = 204;
                                    }
                                }
                            }
                        } else if (this.this$0.isCheckCompletedByWifiPro()) {
                            return NetworkMonitor.DBG;
                        } else {
                            probeResult.mHttpResponseCode = this.this$0.getRespCodeByWifiPro();
                            if (probeResult.mHttpResponseCode != 599) {
                                boolean z3;
                                networkMonitor = this.this$0;
                                if (probeResult.mHttpResponseCode != 204) {
                                    z3 = NetworkMonitor.DBG;
                                } else {
                                    z3 = false;
                                }
                                networkMonitor.sendNetworkConditionsBroadcast(NetworkMonitor.DBG, z3, this.this$0.getReqTimestamp(), this.this$0.getRespTimestamp());
                            }
                        }
                        HwConnectivityManager hwConnectivityManager = HwServiceFactory.getHwConnectivityManager();
                        Context -get2 = this.this$0.mContext;
                        if (probeResult.mHttpResponseCode == 204 || probeResult.mHttpResponseCode == 599) {
                            z2 = false;
                        } else {
                            z2 = NetworkMonitor.DBG;
                        }
                        hwConnectivityManager.captivePortalCheckCompleted(-get2, z2);
                        if (probeResult.isSuccessful()) {
                            this.this$0.transitionTo(this.this$0.mValidatedState);
                        } else if (probeResult.isPortal()) {
                            if (this.this$0.isWifiProEnabled()) {
                                this.this$0.reportPortalNetwork(this.this$0.mConnectivityServiceHandler, this.this$0.mNetId, probeResult.mRedirectUrl);
                            } else {
                                this.this$0.mConnectivityServiceHandler.sendMessage(this.this$0.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, NetworkMonitor.NETWORK_TEST_RESULT_INVALID, this.this$0.mNetId, probeResult.mRedirectUrl));
                            }
                            this.this$0.transitionTo(this.this$0.mCaptivePortalState);
                        } else {
                            networkMonitor = this.this$0;
                            NetworkMonitor networkMonitor2 = this.this$0;
                            Message msg = networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + NetworkMonitor.NETWORK_TEST_RESULT_INVALID, NetworkMonitor.NETWORK_TEST_RESULT_VALID);
                            if (!this.this$0.isWifiProEnabled() || this.this$0.isCheckCompletedByWifiPro()) {
                                this.this$0.sendMessageDelayed(msg, (long) this.mReevaluateDelayMs);
                                NetworkEvent.logEvent(this.this$0.mNetId, 3);
                                this.this$0.mConnectivityServiceHandler.sendMessage(this.this$0.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, NetworkMonitor.NETWORK_TEST_RESULT_INVALID, this.this$0.mNetId, probeResult.mRedirectUrl));
                                if (this.mAttempts >= NetworkMonitor.IGNORE_REEVALUATE_ATTEMPTS) {
                                    TrafficStats.clearThreadStatsUid();
                                }
                                this.mReevaluateDelayMs *= 2;
                                if (this.mReevaluateDelayMs > NetworkMonitor.MAX_REEVALUATE_DELAY_MS) {
                                    this.mReevaluateDelayMs = NetworkMonitor.MAX_REEVALUATE_DELAY_MS;
                                }
                            } else {
                                this.this$0.sendMessageDelayed(msg, (long) this.this$0.resetReevaluateDelayMs(this.mReevaluateDelayMs));
                                return NetworkMonitor.DBG;
                            }
                        }
                        return NetworkMonitor.DBG;
                    }
                    this.this$0.validationLog("Network would not satisfy default request, not validating");
                    this.this$0.transitionTo(this.this$0.mValidatedState);
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                    if (this.mAttempts >= NetworkMonitor.IGNORE_REEVALUATE_ATTEMPTS) {
                        z = false;
                    }
                    return z;
                default:
                    return false;
            }
        }

        public void exit() {
            TrafficStats.clearThreadStatsUid();
        }
    }

    private class LingeringState extends State {
        private static final String ACTION_LINGER_EXPIRED = "android.net.netmon.lingerExpired";
        private WakeupMessage mWakeupMessage;
        final /* synthetic */ NetworkMonitor this$0;

        /* synthetic */ LingeringState(NetworkMonitor this$0, LingeringState lingeringState) {
            this(this$0);
        }

        private LingeringState(NetworkMonitor this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.mEvaluationTimer.reset();
            this.mWakeupMessage = this.this$0.makeWakeupMessage(this.this$0.mContext, this.this$0.getHandler(), "android.net.netmon.lingerExpired." + this.this$0.mNetId, NetworkMonitor.CMD_LINGER_EXPIRED);
            this.mWakeupMessage.schedule(SystemClock.elapsedRealtime() + ((long) this.this$0.mLingerDelayMs));
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    this.this$0.log("Unlingered");
                    if (!this.this$0.mNetworkAgentInfo.lastValidated) {
                        return false;
                    }
                    this.this$0.transitionTo(this.this$0.mValidatedState);
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_LINGER_EXPIRED /*532484*/:
                    this.this$0.mConnectivityServiceHandler.sendMessage(this.this$0.obtainMessage(NetworkMonitor.EVENT_NETWORK_LINGER_COMPLETE, this.this$0.mNetworkAgentInfo));
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_APP_FINISHED /*532489*/:
                    return NetworkMonitor.DBG;
                default:
                    return false;
            }
        }

        public void exit() {
            this.mWakeupMessage.cancel();
        }
    }

    private class MaybeNotifyState extends State {
        final /* synthetic */ NetworkMonitor this$0;

        /* synthetic */ MaybeNotifyState(NetworkMonitor this$0, MaybeNotifyState maybeNotifyState) {
            this(this$0);
        }

        private MaybeNotifyState(NetworkMonitor this$0) {
            this.this$0 = this$0;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP /*532491*/:
                    try {
                        HwServiceFactory.getHwConnectivityManager().startBrowserOnClickNotification(this.this$0.mContext, new URL(NetworkMonitor.getCaptivePortalServerUrl(this.this$0.mContext)).toString());
                    } catch (MalformedURLException e) {
                        this.this$0.log("MalformedURLException " + e);
                    }
                    return NetworkMonitor.DBG;
                default:
                    return false;
            }
        }

        public void exit() {
            this.this$0.mConnectivityServiceHandler.sendMessage(this.this$0.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, NetworkMonitor.NETWORK_TEST_RESULT_VALID, this.this$0.mNetworkAgentInfo.network.netId, null));
        }
    }

    private class ValidatedState extends State {
        final /* synthetic */ NetworkMonitor this$0;

        /* synthetic */ ValidatedState(NetworkMonitor this$0, ValidatedState validatedState) {
            this(this$0);
        }

        private ValidatedState(NetworkMonitor this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            if (this.this$0.mEvaluationTimer.isRunning()) {
                NetworkEvent.logValidated(this.this$0.mNetId, this.this$0.mEvaluationTimer.stop());
                this.this$0.mEvaluationTimer.reset();
            }
            this.this$0.mConnectivityServiceHandler.sendMessage(this.this$0.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, NetworkMonitor.NETWORK_TEST_RESULT_VALID, this.this$0.mNetworkAgentInfo.network.netId, null));
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    this.this$0.transitionTo(this.this$0.mValidatedState);
                    return NetworkMonitor.DBG;
                default:
                    return false;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.connectivity.NetworkMonitor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.connectivity.NetworkMonitor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.NetworkMonitor.<clinit>():void");
    }

    public NetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest) {
        boolean z = DBG;
        super(TAG + networkAgentInfo.name());
        this.mLingerToken = NETWORK_TEST_RESULT_VALID;
        this.mReevaluateToken = NETWORK_TEST_RESULT_VALID;
        this.mUidResponsibleForReeval = INVALID_UID;
        this.mUserDoesNotWant = false;
        this.mDontDisplaySigninNotification = false;
        this.systemReady = false;
        this.mDefaultState = new DefaultState();
        this.mValidatedState = new ValidatedState();
        this.mMaybeNotifyState = new MaybeNotifyState();
        this.mEvaluatingState = new EvaluatingState();
        this.mCaptivePortalState = new CaptivePortalState();
        this.mLingeringState = new LingeringState();
        this.mLaunchCaptivePortalAppBroadcastReceiver = null;
        this.validationLogs = new LocalLog(20);
        this.mUrlHeadFieldLocation = null;
        this.mEvaluationTimer = new Stopwatch();
        this.mContext = context;
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
        addState(this.mLingeringState, this.mDefaultState);
        setInitialState(this.mDefaultState);
        this.mLingerDelayMs = SystemProperties.getInt(LINGER_DELAY_PROPERTY, DEFAULT_LINGER_DELAY_MS);
        this.mIsCaptivePortalCheckEnabled = Global.getInt(this.mContext.getContentResolver(), "captive_portal_detection_enabled", NETWORK_TEST_RESULT_INVALID) == NETWORK_TEST_RESULT_INVALID ? DBG : false;
        if (Global.getInt(this.mContext.getContentResolver(), "captive_portal_use_https", NETWORK_TEST_RESULT_INVALID) != NETWORK_TEST_RESULT_INVALID) {
            z = false;
        }
        this.mUseHttps = z;
        start();
    }

    protected void log(String s) {
        Log.d(TAG + BAKUP_SERV_PAGE + this.mNetworkAgentInfo.name(), s);
    }

    private void validationLog(String s) {
        log(s);
        this.validationLogs.log(s);
    }

    public ReadOnlyLocalLog getValidationLogs() {
        return this.validationLogs.readOnlyLocalLog();
    }

    private static String getCaptivePortalServerUrl(Context context, boolean isHttps) {
        String server = Global.getString(context.getContentResolver(), "captive_portal_server");
        if (server == null) {
            server = DEFAULT_SERVER;
        }
        return (isHttps ? "https" : "http") + "://" + server + DEFAULT_SERV_PAGE;
    }

    public static String getCaptivePortalServerUrl(Context context) {
        return getCaptivePortalServerUrl(context, false);
    }

    protected CaptivePortalProbeResult isCaptivePortal() {
        return isCaptivePortal(getCaptivePortalServerUrl(this.mContext));
    }

    protected CaptivePortalProbeResult isCaptivePortal(String server_url, String page) {
        if (!(!server_url.startsWith("http://") ? server_url.startsWith("https://") : DBG)) {
            server_url = "http://" + server_url;
        }
        if (!server_url.endsWith(page)) {
            server_url = server_url + page;
        }
        return isCaptivePortal(server_url);
    }

    protected CaptivePortalProbeResult isCaptivePortal(String urlString) {
        long dnsLatency;
        if (!this.mIsCaptivePortalCheckEnabled) {
            return new CaptivePortalProbeResult(204, null);
        }
        String hostToResolve;
        CaptivePortalProbeResult result;
        URL pacUrl = null;
        URL url = null;
        URL url2 = null;
        ProxyInfo proxyInfo = this.mNetworkAgentInfo.linkProperties.getHttpProxy();
        if (!(proxyInfo == null || Uri.EMPTY.equals(proxyInfo.getPacFileUrl()))) {
            try {
                URL url3 = new URL(proxyInfo.getPacFileUrl().toString());
            } catch (MalformedURLException e) {
                validationLog("Invalid PAC URL: " + proxyInfo.getPacFileUrl().toString());
                return CaptivePortalProbeResult.FAILED;
            }
        }
        if (pacUrl == null) {
            try {
                url3 = new URL(urlString);
                try {
                    url3 = new URL(urlString);
                    url = url3;
                } catch (MalformedURLException e2) {
                    url = url3;
                    validationLog("Bad validation URL: " + getCaptivePortalServerUrl(this.mContext, false));
                    return CaptivePortalProbeResult.FAILED;
                }
            } catch (MalformedURLException e3) {
                validationLog("Bad validation URL: " + getCaptivePortalServerUrl(this.mContext, false));
                return CaptivePortalProbeResult.FAILED;
            }
        }
        long startTime = SystemClock.elapsedRealtime();
        if (pacUrl != null) {
            hostToResolve = pacUrl.getHost();
        } else if (proxyInfo != null) {
            hostToResolve = proxyInfo.getHost();
        } else {
            hostToResolve = url.getHost();
        }
        if (!TextUtils.isEmpty(hostToResolve)) {
            String probeName = ValidationProbeEvent.getProbeName(NETWORK_TEST_RESULT_VALID);
            Stopwatch dnsTimer = new Stopwatch().start();
            try {
                InetAddress[] addresses = this.mNetworkAgentInfo.network.getAllByName(hostToResolve);
                dnsLatency = dnsTimer.stop();
                ValidationProbeEvent.logEvent(this.mNetId, dnsLatency, NETWORK_TEST_RESULT_VALID, NETWORK_TEST_RESULT_INVALID);
                StringBuffer connectInfo = new StringBuffer(", " + hostToResolve + "=");
                int length = addresses.length;
                for (int i = NETWORK_TEST_RESULT_VALID; i < length; i += NETWORK_TEST_RESULT_INVALID) {
                    InetAddress address = addresses[i];
                    connectInfo.append(address.getHostAddress());
                    if (address != addresses[addresses.length + INVALID_UID]) {
                        connectInfo.append(",");
                    }
                }
                validationLog(probeName + " OK " + dnsLatency + "ms" + connectInfo);
            } catch (UnknownHostException e4) {
                dnsLatency = dnsTimer.stop();
                ValidationProbeEvent.logEvent(this.mNetId, dnsLatency, NETWORK_TEST_RESULT_VALID, NETWORK_TEST_RESULT_VALID);
                validationLog(probeName + " FAIL " + dnsLatency + "ms, " + hostToResolve);
            }
        }
        if (pacUrl != null) {
            result = sendHttpProbe(pacUrl, 3);
        } else if (this.mUseHttps) {
            result = sendParallelHttpProbes(url2, url);
        } else {
            result = sendHttpProbe(url, NETWORK_TEST_RESULT_INVALID);
        }
        sendNetworkConditionsBroadcast(DBG, result.isPortal(), startTime, SystemClock.elapsedRealtime());
        return result;
    }

    protected CaptivePortalProbeResult sendHttpProbe(URL url, int probeType) {
        HttpURLConnection httpURLConnection = null;
        int httpResponseCode = 599;
        String redirectUrl = null;
        Stopwatch probeTimer = new Stopwatch().start();
        try {
            httpURLConnection = (HttpURLConnection) this.mNetworkAgentInfo.network.openConnection(url);
            httpURLConnection.setInstanceFollowRedirects(probeType == 3 ? DBG : false);
            httpURLConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
            httpURLConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
            httpURLConnection.setUseCaches(false);
            long requestTimestamp = SystemClock.elapsedRealtime();
            httpResponseCode = httpURLConnection.getResponseCode();
            redirectUrl = httpURLConnection.getHeaderField("location");
            long responseTimestamp = SystemClock.elapsedRealtime();
            this.mUrlHeadFieldLocation = httpURLConnection.getHeaderField(FlpHardwareProvider.LOCATION);
            validationLog(ValidationProbeEvent.getProbeName(probeType) + " " + url + " time=" + (responseTimestamp - requestTimestamp) + "ms" + " ret=" + httpResponseCode + " headers=" + httpURLConnection.getHeaderFields());
            if (httpResponseCode == 200 && httpURLConnection.getContentLength() == 0) {
                validationLog("Empty 200 response interpreted as 204 response.");
                httpResponseCode = 204;
            }
            if (httpResponseCode == 200 && probeType == 3) {
                validationLog("PAC fetch 200 response interpreted as 204 response.");
                httpResponseCode = 204;
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (IOException e) {
            validationLog("Probably not a portal: exception " + e);
            if (599 == 599) {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } else if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        ValidationProbeEvent.logEvent(this.mNetId, probeTimer.stop(), probeType, httpResponseCode);
        return new CaptivePortalProbeResult(httpResponseCode, redirectUrl);
    }

    private CaptivePortalProbeResult sendParallelHttpProbes(URL httpsUrl, URL httpUrl) {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<CaptivePortalProbeResult> finalResult = new AtomicReference();
        AnonymousClass1ProbeThread httpsProbe = new AnonymousClass1ProbeThread(DBG, httpsUrl, httpUrl, finalResult, latch);
        AnonymousClass1ProbeThread httpProbe = new AnonymousClass1ProbeThread(false, httpsUrl, httpUrl, finalResult, latch);
        httpsProbe.start();
        httpProbe.start();
        try {
            latch.await();
            finalResult.compareAndSet(null, httpsProbe.getResult());
            return (CaptivePortalProbeResult) finalResult.get();
        } catch (InterruptedException e) {
            validationLog("Error: probe wait interrupted!");
            return CaptivePortalProbeResult.FAILED;
        }
    }

    private void sendNetworkConditionsBroadcast(boolean responseReceived, boolean isCaptivePortal, long requestTimestampMs, long responseTimestampMs) {
        if (Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", NETWORK_TEST_RESULT_VALID) != 0 && this.systemReady) {
            Intent latencyBroadcast = new Intent(ACTION_NETWORK_CONDITIONS_MEASURED);
            switch (this.mNetworkAgentInfo.networkInfo.getType()) {
                case NETWORK_TEST_RESULT_VALID /*0*/:
                    latencyBroadcast.putExtra(EXTRA_NETWORK_TYPE, this.mTelephonyManager.getNetworkType());
                    List<CellInfo> info = this.mTelephonyManager.getAllCellInfo();
                    if (info != null) {
                        int numRegisteredCellInfo = NETWORK_TEST_RESULT_VALID;
                        for (CellInfo cellInfo : info) {
                            if (cellInfo.isRegistered()) {
                                numRegisteredCellInfo += NETWORK_TEST_RESULT_INVALID;
                                if (numRegisteredCellInfo > NETWORK_TEST_RESULT_INVALID) {
                                    log("more than one registered CellInfo.  Can't tell which is active.  Bailing.");
                                    return;
                                } else if (cellInfo instanceof CellInfoCdma) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoCdma) cellInfo).getCellIdentity());
                                } else if (cellInfo instanceof CellInfoGsm) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoGsm) cellInfo).getCellIdentity());
                                } else if (cellInfo instanceof CellInfoLte) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoLte) cellInfo).getCellIdentity());
                                } else if (cellInfo instanceof CellInfoWcdma) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoWcdma) cellInfo).getCellIdentity());
                                } else {
                                    logw("Registered cellinfo is unrecognized");
                                    return;
                                }
                            }
                        }
                        break;
                    }
                    return;
                case NETWORK_TEST_RESULT_INVALID /*1*/:
                    WifiInfo currentWifiInfo = this.mWifiManager.getConnectionInfo();
                    if (currentWifiInfo != null) {
                        latencyBroadcast.putExtra(EXTRA_SSID, currentWifiInfo.getSSID());
                        latencyBroadcast.putExtra(EXTRA_BSSID, currentWifiInfo.getBSSID());
                        break;
                    }
                    logw("network info is TYPE_WIFI but no ConnectionInfo found");
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

    public static void SetDefaultLingerTime(int time_ms) {
        if (Process.myUid() == INITIAL_REEVALUATE_DELAY_MS) {
            throw new SecurityException("SetDefaultLingerTime only for internal testing.");
        }
        DEFAULT_LINGER_DELAY_MS = time_ms;
    }

    protected WakeupMessage makeWakeupMessage(Context c, Handler h, String s, int i) {
        return new WakeupMessage(c, h, s, i);
    }

    private String parseHostByLocation(String location) {
        if (location != null) {
            int start = NETWORK_TEST_RESULT_VALID;
            if (location.startsWith("http://")) {
                start = 7;
            } else if (location.startsWith("https://")) {
                start = 8;
            }
            int end = location.indexOf(BAKUP_SERV_PAGE, start);
            if (end == INVALID_UID) {
                end = location.length();
            }
            if (start <= end && end <= location.length()) {
                return location.substring(start, end);
            }
        }
        return null;
    }
}
