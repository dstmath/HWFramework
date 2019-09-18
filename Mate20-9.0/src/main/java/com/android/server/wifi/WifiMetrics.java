package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.aware.WifiAwareMetrics;
import com.android.server.wifi.hotspot2.ANQPNetworkKey;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.PasspointMatch;
import com.android.server.wifi.hotspot2.PasspointProvider;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.nano.WifiMetricsProto;
import com.android.server.wifi.rtt.RttMetrics;
import com.android.server.wifi.util.ScanResultUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WifiMetrics {
    public static final String CLEAN_DUMP_ARG = "clean";
    private static final int CONNECT_TO_NETWORK_NOTIFICATION_ACTION_KEY_MULTIPLIER = 1000;
    private static final boolean DBG = false;
    @VisibleForTesting
    static final int LOW_WIFI_SCORE = 50;
    public static final int MAX_CONNECTABLE_BSSID_NETWORK_BUCKET = 50;
    public static final int MAX_CONNECTABLE_SSID_NETWORK_BUCKET = 20;
    private static final int MAX_CONNECTION_EVENTS = 256;
    private static final int MAX_NUM_SOFT_AP_EVENTS = 256;
    public static final int MAX_PASSPOINT_APS_PER_UNIQUE_ESS_BUCKET = 50;
    public static final int MAX_RSSI_DELTA = 127;
    private static final int MAX_RSSI_POLL = 0;
    public static final int MAX_STA_EVENTS = 768;
    public static final int MAX_TOTAL_80211MC_APS_BUCKET = 20;
    public static final int MAX_TOTAL_PASSPOINT_APS_BUCKET = 50;
    public static final int MAX_TOTAL_PASSPOINT_UNIQUE_ESS_BUCKET = 20;
    public static final int MAX_TOTAL_SCAN_RESULTS_BUCKET = 250;
    public static final int MAX_TOTAL_SCAN_RESULT_SSIDS_BUCKET = 100;
    private static final int MAX_WIFI_SCORE = 60;
    public static final int MIN_RSSI_DELTA = -127;
    private static final int MIN_RSSI_POLL = -127;
    private static final int MIN_WIFI_SCORE = 0;
    public static final String PROTO_DUMP_ARG = "wifiMetricsProto";
    private static final int SCREEN_OFF = 0;
    private static final int SCREEN_ON = 1;
    private static final String TAG = "WifiMetrics";
    public static final long TIMEOUT_RSSI_DELTA_MILLIS = 3000;
    private final SparseIntArray mAvailableOpenBssidsInScanHistogram = new SparseIntArray();
    private final SparseIntArray mAvailableOpenOrSavedBssidsInScanHistogram = new SparseIntArray();
    private final SparseIntArray mAvailableOpenOrSavedSsidsInScanHistogram = new SparseIntArray();
    private final SparseIntArray mAvailableOpenSsidsInScanHistogram = new SparseIntArray();
    private final SparseIntArray mAvailableSavedBssidsInScanHistogram = new SparseIntArray();
    private final SparseIntArray mAvailableSavedPasspointProviderBssidsInScanHistogram = new SparseIntArray();
    private final SparseIntArray mAvailableSavedPasspointProviderProfilesInScanHistogram = new SparseIntArray();
    private final SparseIntArray mAvailableSavedSsidsInScanHistogram = new SparseIntArray();
    private Clock mClock;
    private final SparseIntArray mConnectToNetworkNotificationActionCount = new SparseIntArray();
    private final SparseIntArray mConnectToNetworkNotificationCount = new SparseIntArray();
    private final List<ConnectionEvent> mConnectionEventList = new ArrayList();
    /* access modifiers changed from: private */
    public ConnectionEvent mCurrentConnectionEvent;
    private Handler mHandler;
    private boolean mIsMacRandomizationOn = false;
    private boolean mIsWifiNetworksAvailableNotificationOn = false;
    private int mLastPollFreq = -1;
    private int mLastPollLinkSpeed = -1;
    private int mLastPollRssi = -127;
    private int mLastScore = -1;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private int mNumOpenNetworkConnectMessageFailedToSend = 0;
    private int mNumOpenNetworkRecommendationUpdates = 0;
    private final SparseIntArray mObserved80211mcApInScanHistogram = new SparseIntArray();
    private final SparseIntArray mObservedHotspotR1ApInScanHistogram = new SparseIntArray();
    private final SparseIntArray mObservedHotspotR1ApsPerEssInScanHistogram = new SparseIntArray();
    private final SparseIntArray mObservedHotspotR1EssInScanHistogram = new SparseIntArray();
    private final SparseIntArray mObservedHotspotR2ApInScanHistogram = new SparseIntArray();
    private final SparseIntArray mObservedHotspotR2ApsPerEssInScanHistogram = new SparseIntArray();
    private final SparseIntArray mObservedHotspotR2EssInScanHistogram = new SparseIntArray();
    private int mOpenNetworkRecommenderBlacklistSize = 0;
    private PasspointManager mPasspointManager;
    private final WifiMetricsProto.PnoScanMetrics mPnoScanMetrics = new WifiMetricsProto.PnoScanMetrics();
    private long mRecordStartTimeSec;
    private final SparseIntArray mRssiDeltaCounts = new SparseIntArray();
    private final Map<Integer, SparseIntArray> mRssiPollCountsMap = new HashMap();
    private RttMetrics mRttMetrics;
    private int mScanResultRssi = 0;
    private long mScanResultRssiTimestampMillis = -1;
    private final SparseIntArray mScanReturnEntries = new SparseIntArray();
    private ScoringParams mScoringParams;
    private boolean mScreenOn;
    private final List<WifiMetricsProto.SoftApConnectedClientsEvent> mSoftApEventListLocalOnly = new ArrayList();
    private final List<WifiMetricsProto.SoftApConnectedClientsEvent> mSoftApEventListTethered = new ArrayList();
    private final SparseIntArray mSoftApManagerReturnCodeCounts = new SparseIntArray();
    private LinkedList<StaEventWithTime> mStaEventList = new LinkedList<>();
    private int mSupplicantStateChangeBitmask = 0;
    private final SparseIntArray mTotalBssidsInScanHistogram = new SparseIntArray();
    private final SparseIntArray mTotalSsidsInScanHistogram = new SparseIntArray();
    private final SparseIntArray mWifiAlertReasonCounts = new SparseIntArray();
    private WifiAwareMetrics mWifiAwareMetrics;
    private WifiConfigManager mWifiConfigManager;
    private final WifiMetricsProto.WifiLog mWifiLogProto = new WifiMetricsProto.WifiLog();
    private WifiNetworkSelector mWifiNetworkSelector;
    private WifiPowerMetrics mWifiPowerMetrics = new WifiPowerMetrics();
    private final SparseIntArray mWifiScoreCounts = new SparseIntArray();
    private int mWifiState;
    private final SparseIntArray mWifiSystemStateEntries = new SparseIntArray();
    private final WifiWakeMetrics mWifiWakeMetrics = new WifiWakeMetrics();
    private boolean mWifiWins = false;
    private final WifiMetricsProto.WpsMetrics mWpsMetrics = new WifiMetricsProto.WpsMetrics();

    /* renamed from: com.android.server.wifi.WifiMetrics$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$android$net$wifi$SupplicantState = new int[SupplicantState.values().length];

        static {
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.DISCONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.INTERFACE_DISABLED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.INACTIVE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.SCANNING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.AUTHENTICATING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.ASSOCIATING.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.ASSOCIATED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.FOUR_WAY_HANDSHAKE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.GROUP_HANDSHAKE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.COMPLETED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.DORMANT.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.UNINITIALIZED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.INVALID.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
        }
    }

    class ConnectionEvent {
        public static final int FAILURE_ASSOCIATION_REJECTION = 2;
        public static final int FAILURE_ASSOCIATION_TIMED_OUT = 11;
        public static final int FAILURE_AUTHENTICATION_FAILURE = 3;
        public static final int FAILURE_CONNECT_NETWORK_FAILED = 5;
        public static final int FAILURE_DHCP = 10;
        public static final int FAILURE_NETWORK_DISCONNECTION = 6;
        public static final int FAILURE_NEW_CONNECTION_ATTEMPT = 7;
        public static final int FAILURE_NONE = 1;
        public static final int FAILURE_REDUNDANT_CONNECTION_ATTEMPT = 8;
        public static final int FAILURE_ROAM_TIMEOUT = 9;
        public static final int FAILURE_SSID_TEMP_DISABLED = 4;
        public static final int FAILURE_UNKNOWN = 0;
        /* access modifiers changed from: private */
        public String mConfigBssid;
        /* access modifiers changed from: private */
        public String mConfigSsid;
        WifiMetricsProto.ConnectionEvent mConnectionEvent;
        /* access modifiers changed from: private */
        public long mRealEndTime;
        /* access modifiers changed from: private */
        public long mRealStartTime;
        RouterFingerPrint mRouterFingerPrint;
        /* access modifiers changed from: private */
        public boolean mScreenOn;
        /* access modifiers changed from: private */
        public int mWifiState;

        private ConnectionEvent() {
            this.mConnectionEvent = new WifiMetricsProto.ConnectionEvent();
            this.mRealEndTime = 0;
            this.mRealStartTime = 0;
            this.mRouterFingerPrint = new RouterFingerPrint();
            this.mConnectionEvent.routerFingerprint = this.mRouterFingerPrint.mRouterFingerPrintProto;
            this.mConfigSsid = "<NULL>";
            this.mConfigBssid = "<NULL>";
            this.mWifiState = 0;
            this.mScreenOn = false;
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("startTime=");
            Calendar c = Calendar.getInstance();
            synchronized (WifiMetrics.this.mLock) {
                c.setTimeInMillis(this.mConnectionEvent.startTimeMillis);
                if (this.mConnectionEvent.startTimeMillis == 0) {
                    str = "            <null>";
                } else {
                    str = String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c});
                }
                sb.append(str);
                sb.append(", SSID=");
                sb.append(this.mConfigSsid);
                sb.append(", BSSID=");
                sb.append(this.mConfigBssid);
                sb.append(", durationMillis=");
                sb.append(this.mConnectionEvent.durationTakenToConnectMillis);
                sb.append(", roamType=");
                switch (this.mConnectionEvent.roamType) {
                    case 1:
                        sb.append("ROAM_NONE");
                        break;
                    case 2:
                        sb.append("ROAM_DBDC");
                        break;
                    case 3:
                        sb.append("ROAM_ENTERPRISE");
                        break;
                    case 4:
                        sb.append("ROAM_USER_SELECTED");
                        break;
                    case 5:
                        sb.append("ROAM_UNRELATED");
                        break;
                    default:
                        sb.append("ROAM_UNKNOWN");
                        break;
                }
                sb.append(", connectionResult=");
                sb.append(this.mConnectionEvent.connectionResult);
                sb.append(", level2FailureCode=");
                switch (this.mConnectionEvent.level2FailureCode) {
                    case 1:
                        sb.append("NONE");
                        break;
                    case 2:
                        sb.append("ASSOCIATION_REJECTION");
                        break;
                    case 3:
                        sb.append("AUTHENTICATION_FAILURE");
                        break;
                    case 4:
                        sb.append("SSID_TEMP_DISABLED");
                        break;
                    case 5:
                        sb.append("CONNECT_NETWORK_FAILED");
                        break;
                    case 6:
                        sb.append("NETWORK_DISCONNECTION");
                        break;
                    case 7:
                        sb.append("NEW_CONNECTION_ATTEMPT");
                        break;
                    case 8:
                        sb.append("REDUNDANT_CONNECTION_ATTEMPT");
                        break;
                    case 9:
                        sb.append("ROAM_TIMEOUT");
                        break;
                    case 10:
                        sb.append("DHCP");
                        break;
                    case 11:
                        sb.append("ASSOCIATION_TIMED_OUT");
                        break;
                    default:
                        sb.append("UNKNOWN");
                        break;
                }
                sb.append(", connectivityLevelFailureCode=");
                switch (this.mConnectionEvent.connectivityLevelFailureCode) {
                    case 1:
                        sb.append("NONE");
                        break;
                    case 2:
                        sb.append("DHCP");
                        break;
                    case 3:
                        sb.append("NO_INTERNET");
                        break;
                    case 4:
                        sb.append("UNWANTED");
                        break;
                    default:
                        sb.append("UNKNOWN");
                        break;
                }
                sb.append(", signalStrength=");
                sb.append(this.mConnectionEvent.signalStrength);
                sb.append(", wifiState=");
                switch (this.mWifiState) {
                    case 1:
                        sb.append("WIFI_DISABLED");
                        break;
                    case 2:
                        sb.append("WIFI_DISCONNECTED");
                        break;
                    case 3:
                        sb.append("WIFI_ASSOCIATED");
                        break;
                    default:
                        sb.append("WIFI_UNKNOWN");
                        break;
                }
                sb.append(", screenOn=");
                sb.append(this.mScreenOn);
                sb.append(". mRouterFingerprint: ");
                sb.append(this.mRouterFingerPrint.toString());
            }
            return sb.toString();
        }
    }

    class RouterFingerPrint {
        /* access modifiers changed from: private */
        public WifiMetricsProto.RouterFingerPrint mRouterFingerPrintProto = new WifiMetricsProto.RouterFingerPrint();

        RouterFingerPrint() {
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            synchronized (WifiMetrics.this.mLock) {
                sb.append("mConnectionEvent.roamType=" + this.mRouterFingerPrintProto.roamType);
                sb.append(", mChannelInfo=" + this.mRouterFingerPrintProto.channelInfo);
                sb.append(", mDtim=" + this.mRouterFingerPrintProto.dtim);
                sb.append(", mAuthentication=" + this.mRouterFingerPrintProto.authentication);
                sb.append(", mHidden=" + this.mRouterFingerPrintProto.hidden);
                sb.append(", mRouterTechnology=" + this.mRouterFingerPrintProto.routerTechnology);
                sb.append(", mSupportsIpv6=" + this.mRouterFingerPrintProto.supportsIpv6);
            }
            return sb.toString();
        }

        public void updateFromWifiConfiguration(WifiConfiguration config) {
            synchronized (WifiMetrics.this.mLock) {
                if (config != null) {
                    try {
                        this.mRouterFingerPrintProto.hidden = config.hiddenSSID;
                        if (config.dtimInterval > 0) {
                            this.mRouterFingerPrintProto.dtim = config.dtimInterval;
                        }
                        String unused = WifiMetrics.this.mCurrentConnectionEvent.mConfigSsid = config.SSID;
                        if (config.allowedKeyManagement != null && config.allowedKeyManagement.get(0)) {
                            WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 1;
                        } else if (config.isEnterprise()) {
                            WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 3;
                        } else {
                            WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 2;
                        }
                        WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.passpoint = config.isPasspoint();
                        ScanResult candidate = config.getNetworkSelectionStatus().getCandidate();
                        if (candidate != null) {
                            WifiMetrics.this.updateMetricsFromScanResult(candidate);
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
        }
    }

    private static class StaEventWithTime {
        public WifiMetricsProto.StaEvent staEvent;
        public long wallClockMillis;

        StaEventWithTime(WifiMetricsProto.StaEvent event, long wallClockMillis2) {
            this.staEvent = event;
            this.wallClockMillis = wallClockMillis2;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(this.wallClockMillis);
            if (this.wallClockMillis != 0) {
                sb.append(String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c}));
            } else {
                sb.append("                  ");
            }
            sb.append(" ");
            sb.append(WifiMetrics.staEventToString(this.staEvent));
            return sb.toString();
        }
    }

    public WifiMetrics(Clock clock, Looper looper, WifiAwareMetrics awareMetrics, RttMetrics rttMetrics) {
        this.mClock = clock;
        this.mCurrentConnectionEvent = null;
        this.mScreenOn = true;
        this.mWifiState = 1;
        this.mRecordStartTimeSec = this.mClock.getElapsedSinceBootMillis() / 1000;
        this.mWifiAwareMetrics = awareMetrics;
        this.mRttMetrics = rttMetrics;
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                synchronized (WifiMetrics.this.mLock) {
                    WifiMetrics.this.processMessage(msg);
                }
            }
        };
    }

    public void setScoringParams(ScoringParams scoringParams) {
        this.mScoringParams = scoringParams;
    }

    public void setWifiConfigManager(WifiConfigManager wifiConfigManager) {
        this.mWifiConfigManager = wifiConfigManager;
    }

    public void setWifiNetworkSelector(WifiNetworkSelector wifiNetworkSelector) {
        this.mWifiNetworkSelector = wifiNetworkSelector;
    }

    public void setPasspointManager(PasspointManager passpointManager) {
        this.mPasspointManager = passpointManager;
    }

    public void incrementPnoScanStartAttempCount() {
        synchronized (this.mLock) {
            this.mPnoScanMetrics.numPnoScanAttempts++;
        }
    }

    public void incrementPnoScanFailedCount() {
        synchronized (this.mLock) {
            this.mPnoScanMetrics.numPnoScanFailed++;
        }
    }

    public void incrementPnoScanStartedOverOffloadCount() {
        synchronized (this.mLock) {
            this.mPnoScanMetrics.numPnoScanStartedOverOffload++;
        }
    }

    public void incrementPnoScanFailedOverOffloadCount() {
        synchronized (this.mLock) {
            this.mPnoScanMetrics.numPnoScanFailedOverOffload++;
        }
    }

    public void incrementPnoFoundNetworkEventCount() {
        synchronized (this.mLock) {
            this.mPnoScanMetrics.numPnoFoundNetworkEvents++;
        }
    }

    public void incrementWpsAttemptCount() {
        synchronized (this.mLock) {
            this.mWpsMetrics.numWpsAttempts++;
        }
    }

    public void incrementWpsSuccessCount() {
        synchronized (this.mLock) {
            this.mWpsMetrics.numWpsSuccess++;
        }
    }

    public void incrementWpsStartFailureCount() {
        synchronized (this.mLock) {
            this.mWpsMetrics.numWpsStartFailure++;
        }
    }

    public void incrementWpsOverlapFailureCount() {
        synchronized (this.mLock) {
            this.mWpsMetrics.numWpsOverlapFailure++;
        }
    }

    public void incrementWpsTimeoutFailureCount() {
        synchronized (this.mLock) {
            this.mWpsMetrics.numWpsTimeoutFailure++;
        }
    }

    public void incrementWpsOtherConnectionFailureCount() {
        synchronized (this.mLock) {
            this.mWpsMetrics.numWpsOtherConnectionFailure++;
        }
    }

    public void incrementWpsSupplicantFailureCount() {
        synchronized (this.mLock) {
            this.mWpsMetrics.numWpsSupplicantFailure++;
        }
    }

    public void incrementWpsCancellationCount() {
        synchronized (this.mLock) {
            this.mWpsMetrics.numWpsCancellation++;
        }
    }

    public void startConnectionEvent(WifiConfiguration config, String targetBSSID, int roamType) {
        synchronized (this.mLock) {
            if (this.mCurrentConnectionEvent != null) {
                if (this.mCurrentConnectionEvent.mConfigSsid == null || this.mCurrentConnectionEvent.mConfigBssid == null || config == null || !this.mCurrentConnectionEvent.mConfigSsid.equals(config.SSID) || (!this.mCurrentConnectionEvent.mConfigBssid.equals("any") && !this.mCurrentConnectionEvent.mConfigBssid.equals(targetBSSID))) {
                    endConnectionEvent(7, 1);
                } else {
                    String unused = this.mCurrentConnectionEvent.mConfigBssid = targetBSSID;
                    endConnectionEvent(8, 1);
                }
            }
            while (this.mConnectionEventList.size() >= 256) {
                this.mConnectionEventList.remove(0);
            }
            this.mCurrentConnectionEvent = new ConnectionEvent();
            this.mCurrentConnectionEvent.mConnectionEvent.startTimeMillis = this.mClock.getWallClockMillis();
            String unused2 = this.mCurrentConnectionEvent.mConfigBssid = targetBSSID;
            this.mCurrentConnectionEvent.mConnectionEvent.roamType = roamType;
            this.mCurrentConnectionEvent.mRouterFingerPrint.updateFromWifiConfiguration(config);
            String unused3 = this.mCurrentConnectionEvent.mConfigBssid = "any";
            long unused4 = this.mCurrentConnectionEvent.mRealStartTime = this.mClock.getElapsedSinceBootMillis();
            int unused5 = this.mCurrentConnectionEvent.mWifiState = this.mWifiState;
            boolean unused6 = this.mCurrentConnectionEvent.mScreenOn = this.mScreenOn;
            this.mConnectionEventList.add(this.mCurrentConnectionEvent);
            this.mScanResultRssiTimestampMillis = -1;
            if (config != null) {
                ScanResult candidate = config.getNetworkSelectionStatus().getCandidate();
                if (candidate != null) {
                    this.mScanResultRssi = candidate.level;
                    this.mScanResultRssiTimestampMillis = this.mClock.getElapsedSinceBootMillis();
                }
            }
        }
    }

    public void setConnectionEventRoamType(int roamType) {
        synchronized (this.mLock) {
            if (this.mCurrentConnectionEvent != null) {
                this.mCurrentConnectionEvent.mConnectionEvent.roamType = roamType;
            }
        }
    }

    public void setConnectionScanDetail(ScanDetail scanDetail) {
        synchronized (this.mLock) {
            if (!(this.mCurrentConnectionEvent == null || scanDetail == null)) {
                NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                ScanResult scanResult = scanDetail.getScanResult();
                if (!(networkDetail == null || scanResult == null || this.mCurrentConnectionEvent.mConfigSsid == null)) {
                    String access$200 = this.mCurrentConnectionEvent.mConfigSsid;
                    if (access$200.equals("\"" + networkDetail.getSSID() + "\"")) {
                        updateMetricsFromNetworkDetail(networkDetail);
                        updateMetricsFromScanResult(scanResult);
                    }
                }
            }
        }
    }

    public void endConnectionEvent(int level2FailureCode, int connectivityFailureCode) {
        synchronized (this.mLock) {
            if (this.mCurrentConnectionEvent != null) {
                int i = 0;
                boolean result = level2FailureCode == 1 && connectivityFailureCode == 1;
                WifiMetricsProto.ConnectionEvent connectionEvent = this.mCurrentConnectionEvent.mConnectionEvent;
                if (result) {
                    i = 1;
                }
                connectionEvent.connectionResult = i;
                long unused = this.mCurrentConnectionEvent.mRealEndTime = this.mClock.getElapsedSinceBootMillis();
                this.mCurrentConnectionEvent.mConnectionEvent.durationTakenToConnectMillis = (int) (this.mCurrentConnectionEvent.mRealEndTime - this.mCurrentConnectionEvent.mRealStartTime);
                this.mCurrentConnectionEvent.mConnectionEvent.level2FailureCode = level2FailureCode;
                this.mCurrentConnectionEvent.mConnectionEvent.connectivityLevelFailureCode = connectivityFailureCode;
                this.mCurrentConnectionEvent = null;
                if (!result) {
                    this.mScanResultRssiTimestampMillis = -1;
                }
            }
        }
    }

    private void updateMetricsFromNetworkDetail(NetworkDetail networkDetail) {
        int connectionWifiMode;
        int dtimInterval = networkDetail.getDtimInterval();
        if (dtimInterval > 0) {
            this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.dtim = dtimInterval;
        }
        switch (networkDetail.getWifiMode()) {
            case 0:
                connectionWifiMode = 0;
                break;
            case 1:
                connectionWifiMode = 1;
                break;
            case 2:
                connectionWifiMode = 2;
                break;
            case 3:
                connectionWifiMode = 3;
                break;
            case 4:
                connectionWifiMode = 4;
                break;
            case 5:
                connectionWifiMode = 5;
                break;
            default:
                connectionWifiMode = 6;
                break;
        }
        this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.routerTechnology = connectionWifiMode;
    }

    /* access modifiers changed from: private */
    public void updateMetricsFromScanResult(ScanResult scanResult) {
        this.mCurrentConnectionEvent.mConnectionEvent.signalStrength = scanResult.level;
        this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 1;
        String unused = this.mCurrentConnectionEvent.mConfigBssid = scanResult.BSSID;
        if (scanResult.capabilities != null) {
            if (ScanResultUtil.isScanResultForWepNetwork(scanResult)) {
                this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 2;
            } else if (ScanResultUtil.isScanResultForPskNetwork(scanResult)) {
                this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 2;
            } else if (ScanResultUtil.isScanResultForEapNetwork(scanResult)) {
                this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 3;
            }
        }
        this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.channelInfo = scanResult.frequency;
    }

    /* access modifiers changed from: package-private */
    public void setIsLocationEnabled(boolean enabled) {
        synchronized (this.mLock) {
            this.mWifiLogProto.isLocationEnabled = enabled;
        }
    }

    /* access modifiers changed from: package-private */
    public void setIsScanningAlwaysEnabled(boolean enabled) {
        synchronized (this.mLock) {
            this.mWifiLogProto.isScanningAlwaysEnabled = enabled;
        }
    }

    public void incrementNonEmptyScanResultCount() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numNonEmptyScanResults++;
        }
    }

    public void incrementEmptyScanResultCount() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numEmptyScanResults++;
        }
    }

    public void incrementBackgroundScanCount() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numBackgroundScans++;
        }
    }

    public int getBackgroundScanCount() {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiLogProto.numBackgroundScans;
        }
        return i;
    }

    public void incrementOneshotScanCount() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numOneshotScans++;
        }
        incrementWifiSystemScanStateCount(this.mWifiState, this.mScreenOn);
    }

    public void incrementConnectivityOneshotScanCount() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numConnectivityOneshotScans++;
        }
    }

    public int getOneshotScanCount() {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiLogProto.numOneshotScans;
        }
        return i;
    }

    public int getConnectivityOneshotScanCount() {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiLogProto.numConnectivityOneshotScans;
        }
        return i;
    }

    public void incrementExternalAppOneshotScanRequestsCount() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numExternalAppOneshotScanRequests++;
        }
    }

    public void incrementExternalForegroundAppOneshotScanRequestsThrottledCount() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numExternalForegroundAppOneshotScanRequestsThrottled++;
        }
    }

    public void incrementExternalBackgroundAppOneshotScanRequestsThrottledCount() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numExternalBackgroundAppOneshotScanRequestsThrottled++;
        }
    }

    private String returnCodeToString(int scanReturnCode) {
        switch (scanReturnCode) {
            case 0:
                return "SCAN_UNKNOWN";
            case 1:
                return "SCAN_SUCCESS";
            case 2:
                return "SCAN_FAILURE_INTERRUPTED";
            case 3:
                return "SCAN_FAILURE_INVALID_CONFIGURATION";
            case 4:
                return "FAILURE_WIFI_DISABLED";
            default:
                return "<UNKNOWN>";
        }
    }

    public void incrementScanReturnEntry(int scanReturnCode, int countToAdd) {
        synchronized (this.mLock) {
            this.mScanReturnEntries.put(scanReturnCode, this.mScanReturnEntries.get(scanReturnCode) + countToAdd);
        }
    }

    public int getScanReturnEntry(int scanReturnCode) {
        int i;
        synchronized (this.mLock) {
            i = this.mScanReturnEntries.get(scanReturnCode);
        }
        return i;
    }

    private String wifiSystemStateToString(int state) {
        switch (state) {
            case 0:
                return "WIFI_UNKNOWN";
            case 1:
                return "WIFI_DISABLED";
            case 2:
                return "WIFI_DISCONNECTED";
            case 3:
                return "WIFI_ASSOCIATED";
            default:
                return HalDeviceManager.HAL_INSTANCE_NAME;
        }
    }

    public void incrementWifiSystemScanStateCount(int state, boolean screenOn) {
        synchronized (this.mLock) {
            int index = (state * 2) + (screenOn);
            this.mWifiSystemStateEntries.put(index, this.mWifiSystemStateEntries.get(index) + 1);
        }
    }

    public int getSystemStateCount(int state, boolean screenOn) {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiSystemStateEntries.get((state * 2) + (screenOn));
        }
        return i;
    }

    public void incrementNumLastResortWatchdogTriggers() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogTriggers++;
        }
    }

    public void addCountToNumLastResortWatchdogBadAssociationNetworksTotal(int count) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogBadAssociationNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogBadAuthenticationNetworksTotal(int count) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogBadAuthenticationNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogBadDhcpNetworksTotal(int count) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogBadDhcpNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogBadOtherNetworksTotal(int count) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogBadOtherNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogAvailableNetworksTotal(int count) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogAvailableNetworksTotal += count;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadAssociation() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogTriggersWithBadAssociation++;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadAuthentication() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogTriggersWithBadAuthentication++;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadDhcp() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogTriggersWithBadDhcp++;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadOther() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogTriggersWithBadOther++;
        }
    }

    public void incrementNumConnectivityWatchdogPnoGood() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numConnectivityWatchdogPnoGood++;
        }
    }

    public void incrementNumConnectivityWatchdogPnoBad() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numConnectivityWatchdogPnoBad++;
        }
    }

    public void incrementNumConnectivityWatchdogBackgroundGood() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numConnectivityWatchdogBackgroundGood++;
        }
    }

    public void incrementNumConnectivityWatchdogBackgroundBad() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numConnectivityWatchdogBackgroundBad++;
        }
    }

    public void handlePollResult(WifiInfo wifiInfo) {
        this.mLastPollRssi = wifiInfo.getRssi();
        this.mLastPollLinkSpeed = wifiInfo.getLinkSpeed();
        this.mLastPollFreq = wifiInfo.getFrequency();
        incrementRssiPollRssiCount(this.mLastPollFreq, this.mLastPollRssi);
    }

    @VisibleForTesting
    public void incrementRssiPollRssiCount(int frequency, int rssi) {
        if (rssi >= -127 && rssi <= 0) {
            synchronized (this.mLock) {
                if (!this.mRssiPollCountsMap.containsKey(Integer.valueOf(frequency))) {
                    this.mRssiPollCountsMap.put(Integer.valueOf(frequency), new SparseIntArray());
                }
                SparseIntArray sparseIntArray = this.mRssiPollCountsMap.get(Integer.valueOf(frequency));
                sparseIntArray.put(rssi, sparseIntArray.get(rssi) + 1);
                maybeIncrementRssiDeltaCount(rssi - this.mScanResultRssi);
            }
        }
    }

    private void maybeIncrementRssiDeltaCount(int rssi) {
        if (this.mScanResultRssiTimestampMillis >= 0) {
            if (this.mClock.getElapsedSinceBootMillis() - this.mScanResultRssiTimestampMillis <= TIMEOUT_RSSI_DELTA_MILLIS && rssi >= -127 && rssi <= 127) {
                this.mRssiDeltaCounts.put(rssi, this.mRssiDeltaCounts.get(rssi) + 1);
            }
            this.mScanResultRssiTimestampMillis = -1;
        }
    }

    public void incrementNumLastResortWatchdogSuccesses() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numLastResortWatchdogSuccesses++;
        }
    }

    public void incrementWatchdogTotalConnectionFailureCountAfterTrigger() {
        synchronized (this.mLock) {
            this.mWifiLogProto.watchdogTotalConnectionFailureCountAfterTrigger++;
        }
    }

    public void setWatchdogSuccessTimeDurationMs(long ms) {
        synchronized (this.mLock) {
            this.mWifiLogProto.watchdogTriggerToConnectionSuccessDurationMs = ms;
        }
    }

    public void incrementAlertReasonCount(int reason) {
        if (reason > 64 || reason < 0) {
            reason = 0;
        }
        synchronized (this.mLock) {
            this.mWifiAlertReasonCounts.put(reason, this.mWifiAlertReasonCounts.get(reason) + 1);
        }
    }

    public void countScanResults(List<ScanDetail> scanDetails) {
        if (scanDetails != null) {
            int totalResults = 0;
            int openNetworks = 0;
            int personalNetworks = 0;
            int enterpriseNetworks = 0;
            int hiddenNetworks = 0;
            int hotspot2r1Networks = 0;
            int hotspot2r2Networks = 0;
            for (ScanDetail scanDetail : scanDetails) {
                NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                ScanResult scanResult = scanDetail.getScanResult();
                totalResults++;
                if (networkDetail != null) {
                    if (networkDetail.isHiddenBeaconFrame()) {
                        hiddenNetworks++;
                    }
                    if (networkDetail.getHSRelease() != null) {
                        if (networkDetail.getHSRelease() == NetworkDetail.HSRelease.R1) {
                            hotspot2r1Networks++;
                        } else if (networkDetail.getHSRelease() == NetworkDetail.HSRelease.R2) {
                            hotspot2r2Networks++;
                        }
                    }
                }
                if (!(scanResult == null || scanResult.capabilities == null)) {
                    if (ScanResultUtil.isScanResultForEapNetwork(scanResult)) {
                        enterpriseNetworks++;
                    } else if (ScanResultUtil.isScanResultForPskNetwork(scanResult) || ScanResultUtil.isScanResultForWepNetwork(scanResult)) {
                        personalNetworks++;
                    } else {
                        openNetworks++;
                    }
                }
            }
            synchronized (this.mLock) {
                this.mWifiLogProto.numTotalScanResults += totalResults;
                this.mWifiLogProto.numOpenNetworkScanResults += openNetworks;
                this.mWifiLogProto.numPersonalNetworkScanResults += personalNetworks;
                this.mWifiLogProto.numEnterpriseNetworkScanResults += enterpriseNetworks;
                this.mWifiLogProto.numHiddenNetworkScanResults += hiddenNetworks;
                this.mWifiLogProto.numHotspot2R1NetworkScanResults += hotspot2r1Networks;
                this.mWifiLogProto.numHotspot2R2NetworkScanResults += hotspot2r2Networks;
                this.mWifiLogProto.numScans++;
            }
        }
    }

    public void incrementWifiScoreCount(int score) {
        if (score >= 0 && score <= 60) {
            synchronized (this.mLock) {
                this.mWifiScoreCounts.put(score, this.mWifiScoreCounts.get(score) + 1);
                boolean wifiWins = this.mWifiWins;
                if (this.mWifiWins && score < 50) {
                    wifiWins = false;
                } else if (!this.mWifiWins && score > 50) {
                    wifiWins = true;
                }
                this.mLastScore = score;
                if (wifiWins != this.mWifiWins) {
                    this.mWifiWins = wifiWins;
                    WifiMetricsProto.StaEvent event = new WifiMetricsProto.StaEvent();
                    event.type = 16;
                    addStaEvent(event);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
        return;
     */
    public void incrementSoftApStartResult(boolean result, int failureCode) {
        synchronized (this.mLock) {
            if (result) {
                try {
                    this.mSoftApManagerReturnCodeCounts.put(1, this.mSoftApManagerReturnCodeCounts.get(1) + 1);
                } catch (Throwable th) {
                    throw th;
                }
            } else if (failureCode == 1) {
                this.mSoftApManagerReturnCodeCounts.put(3, this.mSoftApManagerReturnCodeCounts.get(3) + 1);
            } else {
                this.mSoftApManagerReturnCodeCounts.put(2, this.mSoftApManagerReturnCodeCounts.get(2) + 1);
            }
        }
    }

    public void addSoftApUpChangedEvent(boolean isUp, int mode) {
        WifiMetricsProto.SoftApConnectedClientsEvent event = new WifiMetricsProto.SoftApConnectedClientsEvent();
        event.eventType = isUp ? 0 : 1;
        event.numConnectedClients = 0;
        addSoftApConnectedClientsEvent(event, mode);
    }

    public void addSoftApNumAssociatedStationsChangedEvent(int numStations, int mode) {
        WifiMetricsProto.SoftApConnectedClientsEvent event = new WifiMetricsProto.SoftApConnectedClientsEvent();
        event.eventType = 2;
        event.numConnectedClients = numStations;
        addSoftApConnectedClientsEvent(event, mode);
    }

    private void addSoftApConnectedClientsEvent(WifiMetricsProto.SoftApConnectedClientsEvent event, int mode) {
        List<WifiMetricsProto.SoftApConnectedClientsEvent> softApEventList;
        synchronized (this.mLock) {
            switch (mode) {
                case 1:
                    softApEventList = this.mSoftApEventListTethered;
                    break;
                case 2:
                    softApEventList = this.mSoftApEventListLocalOnly;
                    break;
                default:
                    return;
            }
            if (softApEventList.size() <= 256) {
                event.timeStampMillis = this.mClock.getElapsedSinceBootMillis();
                softApEventList.add(event);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002c, code lost:
        return;
     */
    public void addSoftApChannelSwitchedEvent(int frequency, int bandwidth, int mode) {
        List<WifiMetricsProto.SoftApConnectedClientsEvent> softApEventList;
        synchronized (this.mLock) {
            switch (mode) {
                case 1:
                    softApEventList = this.mSoftApEventListTethered;
                    break;
                case 2:
                    softApEventList = this.mSoftApEventListLocalOnly;
                    break;
                default:
                    return;
            }
            int index = softApEventList.size() - 1;
            while (true) {
                if (index >= 0) {
                    WifiMetricsProto.SoftApConnectedClientsEvent event = softApEventList.get(index);
                    if (event == null || event.eventType != 0) {
                        index--;
                    } else {
                        event.channelFrequency = frequency;
                        event.channelBandwidth = bandwidth;
                    }
                }
            }
        }
    }

    public void incrementNumHalCrashes() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numHalCrashes++;
        }
    }

    public void incrementNumWificondCrashes() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numWificondCrashes++;
        }
    }

    public void incrementNumSupplicantCrashes() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSupplicantCrashes++;
        }
    }

    public void incrementNumHostapdCrashes() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numHostapdCrashes++;
        }
    }

    public void incrementNumSetupClientInterfaceFailureDueToHal() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSetupClientInterfaceFailureDueToHal++;
        }
    }

    public void incrementNumSetupClientInterfaceFailureDueToWificond() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSetupClientInterfaceFailureDueToWificond++;
        }
    }

    public void incrementNumSetupClientInterfaceFailureDueToSupplicant() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSetupClientInterfaceFailureDueToSupplicant++;
        }
    }

    public void incrementNumSetupSoftApInterfaceFailureDueToHal() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSetupSoftApInterfaceFailureDueToHal++;
        }
    }

    public void incrementNumSetupSoftApInterfaceFailureDueToWificond() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSetupSoftApInterfaceFailureDueToWificond++;
        }
    }

    public void incrementNumSetupSoftApInterfaceFailureDueToHostapd() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSetupSoftApInterfaceFailureDueToHostapd++;
        }
    }

    public void incrementNumClientInterfaceDown() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numClientInterfaceDown++;
        }
    }

    public void incrementNumSoftApInterfaceDown() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSoftApInterfaceDown++;
        }
    }

    public void incrementNumPasspointProviderInstallation() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numPasspointProviderInstallation++;
        }
    }

    public void incrementNumPasspointProviderInstallSuccess() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numPasspointProviderInstallSuccess++;
        }
    }

    public void incrementNumPasspointProviderUninstallation() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numPasspointProviderUninstallation++;
        }
    }

    public void incrementNumPasspointProviderUninstallSuccess() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numPasspointProviderUninstallSuccess++;
        }
    }

    public void incrementNumRadioModeChangeToMcc() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numRadioModeChangeToMcc++;
        }
    }

    public void incrementNumRadioModeChangeToScc() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numRadioModeChangeToScc++;
        }
    }

    public void incrementNumRadioModeChangeToSbs() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numRadioModeChangeToSbs++;
        }
    }

    public void incrementNumRadioModeChangeToDbs() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numRadioModeChangeToDbs++;
        }
    }

    public void incrementNumSoftApUserBandPreferenceUnsatisfied() {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSoftApUserBandPreferenceUnsatisfied++;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:102:0x02c3, code lost:
        return;
     */
    public void incrementAvailableNetworksHistograms(List<ScanDetail> scanDetails, boolean isFullBand) {
        int openBssids;
        Set<ScanResultMatchInfo> savedSsids;
        int passpointR1Aps;
        int savedBssids;
        int openOrSavedBssids;
        PasspointProvider passpointProvider;
        Set<ScanResultMatchInfo> savedSsids2;
        Set<PasspointProvider> savedPasspointProviderProfiles;
        PasspointProvider passpointProvider2;
        boolean validBssid;
        synchronized (this.mLock) {
            if (!(this.mWifiConfigManager == null || this.mWifiNetworkSelector == null)) {
                if (this.mPasspointManager != null) {
                    if (!isFullBand) {
                        this.mWifiLogProto.partialAllSingleScanListenerResults++;
                        return;
                    }
                    Set<ScanResultMatchInfo> ssids = new HashSet<>();
                    Set<ScanResultMatchInfo> openSsids = new HashSet<>();
                    Set<ScanResultMatchInfo> savedSsids3 = new HashSet<>();
                    Set<PasspointProvider> savedPasspointProviderProfiles2 = new HashSet<>();
                    int passpointR2Aps = 0;
                    Map<ANQPNetworkKey, Integer> passpointR1UniqueEss = new HashMap<>();
                    Map<ANQPNetworkKey, Integer> passpointR2UniqueEss = new HashMap<>();
                    Iterator<ScanDetail> it = scanDetails.iterator();
                    int savedPasspointProviderBssids = 0;
                    int savedPasspointProviderBssids2 = 0;
                    int openOrSavedBssids2 = 0;
                    int savedBssids2 = 0;
                    int bssids = 0;
                    int passpointR1Aps2 = 0;
                    int supporting80211mcAps = 0;
                    while (it.hasNext() != 0) {
                        Iterator<ScanDetail> it2 = it;
                        ScanDetail scanDetail = it.next();
                        NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                        ScanResult scanResult = scanDetail.getScanResult();
                        Set<PasspointProvider> savedPasspointProviderProfiles3 = savedPasspointProviderProfiles2;
                        NetworkDetail networkDetail2 = networkDetail;
                        if (networkDetail2.is80211McResponderSupport()) {
                            supporting80211mcAps++;
                        }
                        int supporting80211mcAps2 = supporting80211mcAps;
                        ScanResult scanResult2 = scanResult;
                        ScanResultMatchInfo matchInfo = ScanResultMatchInfo.fromScanResult(scanResult2);
                        boolean z = false;
                        if (networkDetail2.isInterworking()) {
                            openOrSavedBssids = savedPasspointProviderBssids2;
                            Pair<PasspointProvider, PasspointMatch> providerMatch = this.mPasspointManager.matchProvider(scanResult2);
                            if (providerMatch != null) {
                                savedBssids = openOrSavedBssids2;
                                passpointProvider2 = (PasspointProvider) providerMatch.first;
                            } else {
                                savedBssids = openOrSavedBssids2;
                                passpointProvider2 = null;
                            }
                            PasspointProvider passpointProvider3 = passpointProvider2;
                            Pair<PasspointProvider, PasspointMatch> pair = providerMatch;
                            if (networkDetail2.getHSRelease() == NetworkDetail.HSRelease.R1) {
                                passpointR1Aps2++;
                            } else if (networkDetail2.getHSRelease() == NetworkDetail.HSRelease.R2) {
                                passpointR2Aps++;
                            }
                            int passpointR1Aps3 = passpointR1Aps2;
                            long bssid = 0;
                            try {
                                bssid = Utils.parseMac(scanResult2.BSSID);
                                validBssid = true;
                                passpointR1Aps = passpointR1Aps3;
                            } catch (IllegalArgumentException e) {
                                IllegalArgumentException illegalArgumentException = e;
                                passpointR1Aps = passpointR1Aps3;
                                Log.e(TAG, "Invalid BSSID provided in the scan result: " + scanResult2.BSSID);
                                validBssid = false;
                            }
                            if (validBssid) {
                                savedSsids = savedSsids3;
                                openBssids = savedBssids2;
                                ANQPNetworkKey uniqueEss = ANQPNetworkKey.buildKey(scanResult2.SSID, bssid, scanResult2.hessid, networkDetail2.getAnqpDomainID());
                                if (networkDetail2.getHSRelease() == NetworkDetail.HSRelease.R1) {
                                    Integer countObj = passpointR1UniqueEss.get(uniqueEss);
                                    passpointR1UniqueEss.put(uniqueEss, Integer.valueOf((countObj == null ? 0 : countObj.intValue()) + 1));
                                } else if (networkDetail2.getHSRelease() == NetworkDetail.HSRelease.R2) {
                                    Integer countObj2 = passpointR2UniqueEss.get(uniqueEss);
                                    passpointR2UniqueEss.put(uniqueEss, Integer.valueOf((countObj2 == null ? 0 : countObj2.intValue()) + 1));
                                }
                            } else {
                                savedSsids = savedSsids3;
                                openBssids = savedBssids2;
                            }
                            passpointProvider = passpointProvider3;
                        } else {
                            savedSsids = savedSsids3;
                            openBssids = savedBssids2;
                            savedBssids = openOrSavedBssids2;
                            openOrSavedBssids = savedPasspointProviderBssids2;
                            passpointR1Aps = passpointR1Aps2;
                            passpointProvider = null;
                        }
                        if (this.mWifiNetworkSelector.isSignalTooWeak(scanResult2)) {
                            it = it2;
                            savedPasspointProviderProfiles2 = savedPasspointProviderProfiles3;
                            supporting80211mcAps = supporting80211mcAps2;
                            savedPasspointProviderBssids2 = openOrSavedBssids;
                            openOrSavedBssids2 = savedBssids;
                            passpointR1Aps2 = passpointR1Aps;
                            savedSsids3 = savedSsids;
                            savedBssids2 = openBssids;
                        } else {
                            ScanResultMatchInfo matchInfo2 = matchInfo;
                            ssids.add(matchInfo2);
                            bssids++;
                            boolean isOpen = matchInfo2.networkType == 0;
                            WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetworkForScanDetail(scanDetail);
                            boolean isSaved = config != null && !config.isEphemeral() && !config.isPasspoint();
                            if (passpointProvider != null) {
                                z = true;
                            }
                            boolean isSavedPasspoint = z;
                            if (isOpen) {
                                openSsids.add(matchInfo2);
                                openBssids++;
                            }
                            if (isSaved) {
                                ScanDetail scanDetail2 = scanDetail;
                                savedSsids2 = savedSsids;
                                savedSsids2.add(matchInfo2);
                                savedBssids++;
                            } else {
                                savedSsids2 = savedSsids;
                            }
                            if (isOpen || isSaved) {
                                openOrSavedBssids++;
                            }
                            if (isSavedPasspoint) {
                                ScanResultMatchInfo scanResultMatchInfo = matchInfo2;
                                savedPasspointProviderProfiles = savedPasspointProviderProfiles3;
                                savedPasspointProviderProfiles.add(passpointProvider);
                                savedPasspointProviderBssids++;
                            } else {
                                savedPasspointProviderProfiles = savedPasspointProviderProfiles3;
                            }
                            savedPasspointProviderProfiles2 = savedPasspointProviderProfiles;
                            supporting80211mcAps = supporting80211mcAps2;
                            savedPasspointProviderBssids2 = openOrSavedBssids;
                            openOrSavedBssids2 = savedBssids;
                            passpointR1Aps2 = passpointR1Aps;
                            savedBssids2 = openBssids;
                            savedSsids3 = savedSsids2;
                            it = it2;
                        }
                    }
                    Set<ScanResultMatchInfo> set = savedSsids3;
                    int openBssids2 = savedBssids2;
                    this.mWifiLogProto.fullBandAllSingleScanListenerResults++;
                    incrementTotalScanSsids(this.mTotalSsidsInScanHistogram, ssids.size());
                    incrementTotalScanResults(this.mTotalBssidsInScanHistogram, bssids);
                    incrementSsid(this.mAvailableOpenSsidsInScanHistogram, openSsids.size());
                    incrementBssid(this.mAvailableOpenBssidsInScanHistogram, openBssids2);
                    incrementSsid(this.mAvailableSavedSsidsInScanHistogram, set.size());
                    incrementBssid(this.mAvailableSavedBssidsInScanHistogram, openOrSavedBssids2);
                    openSsids.addAll(set);
                    incrementSsid(this.mAvailableOpenOrSavedSsidsInScanHistogram, openSsids.size());
                    incrementBssid(this.mAvailableOpenOrSavedBssidsInScanHistogram, savedPasspointProviderBssids2);
                    Set<ScanResultMatchInfo> set2 = set;
                    incrementSsid(this.mAvailableSavedPasspointProviderProfilesInScanHistogram, savedPasspointProviderProfiles2.size());
                    incrementBssid(this.mAvailableSavedPasspointProviderBssidsInScanHistogram, savedPasspointProviderBssids);
                    incrementTotalPasspointAps(this.mObservedHotspotR1ApInScanHistogram, passpointR1Aps2);
                    incrementTotalPasspointAps(this.mObservedHotspotR2ApInScanHistogram, passpointR2Aps);
                    int i = passpointR1Aps2;
                    incrementTotalUniquePasspointEss(this.mObservedHotspotR1EssInScanHistogram, passpointR1UniqueEss.size());
                    incrementTotalUniquePasspointEss(this.mObservedHotspotR2EssInScanHistogram, passpointR2UniqueEss.size());
                    Iterator<Integer> it3 = passpointR1UniqueEss.values().iterator();
                    while (it3.hasNext()) {
                        incrementPasspointPerUniqueEss(this.mObservedHotspotR1ApsPerEssInScanHistogram, it3.next().intValue());
                        it3 = it3;
                        ssids = ssids;
                    }
                    for (Iterator<Integer> it4 = passpointR2UniqueEss.values().iterator(); it4.hasNext(); it4 = it4) {
                        incrementPasspointPerUniqueEss(this.mObservedHotspotR2ApsPerEssInScanHistogram, it4.next().intValue());
                    }
                    increment80211mcAps(this.mObserved80211mcApInScanHistogram, supporting80211mcAps);
                }
            }
        }
    }

    public void incrementConnectToNetworkNotification(String notifierTag, int notificationType) {
        synchronized (this.mLock) {
            this.mConnectToNetworkNotificationCount.put(notificationType, this.mConnectToNetworkNotificationCount.get(notificationType) + 1);
        }
    }

    public void incrementConnectToNetworkNotificationAction(String notifierTag, int notificationType, int actionType) {
        synchronized (this.mLock) {
            int key = (notificationType * CONNECT_TO_NETWORK_NOTIFICATION_ACTION_KEY_MULTIPLIER) + actionType;
            this.mConnectToNetworkNotificationActionCount.put(key, this.mConnectToNetworkNotificationActionCount.get(key) + 1);
        }
    }

    public void setNetworkRecommenderBlacklistSize(String notifierTag, int size) {
        synchronized (this.mLock) {
            this.mOpenNetworkRecommenderBlacklistSize = size;
        }
    }

    public void setIsWifiNetworksAvailableNotificationEnabled(String notifierTag, boolean enabled) {
        synchronized (this.mLock) {
            this.mIsWifiNetworksAvailableNotificationOn = enabled;
        }
    }

    public void incrementNumNetworkRecommendationUpdates(String notifierTag) {
        synchronized (this.mLock) {
            this.mNumOpenNetworkRecommendationUpdates++;
        }
    }

    public void incrementNumNetworkConnectMessageFailedToSend(String notifierTag) {
        synchronized (this.mLock) {
            this.mNumOpenNetworkConnectMessageFailedToSend++;
        }
    }

    public void setIsMacRandomizationOn(boolean enabled) {
        synchronized (this.mLock) {
            this.mIsMacRandomizationOn = enabled;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        FileDescriptor fileDescriptor = fd;
        PrintWriter printWriter = pw;
        String[] strArr = args;
        synchronized (this.mLock) {
            consolidateScoringParams();
            if (strArr == null || strArr.length <= 0 || !PROTO_DUMP_ARG.equals(strArr[0])) {
                printWriter.println("WifiMetrics:");
                printWriter.println("mConnectionEvents:");
                for (ConnectionEvent event : this.mConnectionEventList) {
                    String eventLine = event.toString();
                    if (event == this.mCurrentConnectionEvent) {
                        eventLine = eventLine + "CURRENTLY OPEN EVENT";
                    }
                    printWriter.println(eventLine);
                }
                printWriter.println("mWifiLogProto.numSavedNetworks=" + this.mWifiLogProto.numSavedNetworks);
                printWriter.println("mWifiLogProto.numOpenNetworks=" + this.mWifiLogProto.numOpenNetworks);
                printWriter.println("mWifiLogProto.numPersonalNetworks=" + this.mWifiLogProto.numPersonalNetworks);
                printWriter.println("mWifiLogProto.numEnterpriseNetworks=" + this.mWifiLogProto.numEnterpriseNetworks);
                printWriter.println("mWifiLogProto.numHiddenNetworks=" + this.mWifiLogProto.numHiddenNetworks);
                printWriter.println("mWifiLogProto.numPasspointNetworks=" + this.mWifiLogProto.numPasspointNetworks);
                printWriter.println("mWifiLogProto.isLocationEnabled=" + this.mWifiLogProto.isLocationEnabled);
                printWriter.println("mWifiLogProto.isScanningAlwaysEnabled=" + this.mWifiLogProto.isScanningAlwaysEnabled);
                printWriter.println("mWifiLogProto.numNetworksAddedByUser=" + this.mWifiLogProto.numNetworksAddedByUser);
                printWriter.println("mWifiLogProto.numNetworksAddedByApps=" + this.mWifiLogProto.numNetworksAddedByApps);
                printWriter.println("mWifiLogProto.numNonEmptyScanResults=" + this.mWifiLogProto.numNonEmptyScanResults);
                printWriter.println("mWifiLogProto.numEmptyScanResults=" + this.mWifiLogProto.numEmptyScanResults);
                printWriter.println("mWifiLogProto.numConnecitvityOneshotScans=" + this.mWifiLogProto.numConnectivityOneshotScans);
                printWriter.println("mWifiLogProto.numOneshotScans=" + this.mWifiLogProto.numOneshotScans);
                printWriter.println("mWifiLogProto.numBackgroundScans=" + this.mWifiLogProto.numBackgroundScans);
                printWriter.println("mWifiLogProto.numExternalAppOneshotScanRequests=" + this.mWifiLogProto.numExternalAppOneshotScanRequests);
                printWriter.println("mWifiLogProto.numExternalForegroundAppOneshotScanRequestsThrottled=" + this.mWifiLogProto.numExternalForegroundAppOneshotScanRequestsThrottled);
                printWriter.println("mWifiLogProto.numExternalBackgroundAppOneshotScanRequestsThrottled=" + this.mWifiLogProto.numExternalBackgroundAppOneshotScanRequestsThrottled);
                printWriter.println("mScanReturnEntries:");
                printWriter.println("  SCAN_UNKNOWN: " + getScanReturnEntry(0));
                printWriter.println("  SCAN_SUCCESS: " + getScanReturnEntry(1));
                printWriter.println("  SCAN_FAILURE_INTERRUPTED: " + getScanReturnEntry(2));
                printWriter.println("  SCAN_FAILURE_INVALID_CONFIGURATION: " + getScanReturnEntry(3));
                printWriter.println("  FAILURE_WIFI_DISABLED: " + getScanReturnEntry(4));
                printWriter.println("mSystemStateEntries: <state><screenOn> : <scansInitiated>");
                printWriter.println("  WIFI_UNKNOWN       ON: " + getSystemStateCount(0, true));
                printWriter.println("  WIFI_DISABLED      ON: " + getSystemStateCount(1, true));
                printWriter.println("  WIFI_DISCONNECTED  ON: " + getSystemStateCount(2, true));
                printWriter.println("  WIFI_ASSOCIATED    ON: " + getSystemStateCount(3, true));
                printWriter.println("  WIFI_UNKNOWN      OFF: " + getSystemStateCount(0, false));
                printWriter.println("  WIFI_DISABLED     OFF: " + getSystemStateCount(1, false));
                printWriter.println("  WIFI_DISCONNECTED OFF: " + getSystemStateCount(2, false));
                printWriter.println("  WIFI_ASSOCIATED   OFF: " + getSystemStateCount(3, false));
                printWriter.println("mWifiLogProto.numConnectivityWatchdogPnoGood=" + this.mWifiLogProto.numConnectivityWatchdogPnoGood);
                printWriter.println("mWifiLogProto.numConnectivityWatchdogPnoBad=" + this.mWifiLogProto.numConnectivityWatchdogPnoBad);
                printWriter.println("mWifiLogProto.numConnectivityWatchdogBackgroundGood=" + this.mWifiLogProto.numConnectivityWatchdogBackgroundGood);
                printWriter.println("mWifiLogProto.numConnectivityWatchdogBackgroundBad=" + this.mWifiLogProto.numConnectivityWatchdogBackgroundBad);
                printWriter.println("mWifiLogProto.numLastResortWatchdogTriggers=" + this.mWifiLogProto.numLastResortWatchdogTriggers);
                printWriter.println("mWifiLogProto.numLastResortWatchdogBadAssociationNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadAssociationNetworksTotal);
                printWriter.println("mWifiLogProto.numLastResortWatchdogBadAuthenticationNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadAuthenticationNetworksTotal);
                printWriter.println("mWifiLogProto.numLastResortWatchdogBadDhcpNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadDhcpNetworksTotal);
                printWriter.println("mWifiLogProto.numLastResortWatchdogBadOtherNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadOtherNetworksTotal);
                printWriter.println("mWifiLogProto.numLastResortWatchdogAvailableNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogAvailableNetworksTotal);
                printWriter.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadAssociation=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadAssociation);
                printWriter.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadAuthentication=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadAuthentication);
                printWriter.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadDhcp=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadDhcp);
                printWriter.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadOther=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadOther);
                printWriter.println("mWifiLogProto.numLastResortWatchdogSuccesses=" + this.mWifiLogProto.numLastResortWatchdogSuccesses);
                printWriter.println("mWifiLogProto.recordDurationSec=" + ((this.mClock.getElapsedSinceBootMillis() / 1000) - this.mRecordStartTimeSec));
                try {
                    JSONObject rssiMap = new JSONObject();
                    for (Map.Entry<Integer, SparseIntArray> entry : this.mRssiPollCountsMap.entrySet()) {
                        int frequency = entry.getKey().intValue();
                        SparseIntArray histogram = entry.getValue();
                        JSONArray histogramElements = new JSONArray();
                        int i = -127;
                        while (true) {
                            int i2 = i;
                            if (i2 > 0) {
                                break;
                            }
                            int count = histogram.get(i2);
                            if (count != 0) {
                                JSONObject histogramElement = new JSONObject();
                                histogramElement.put(Integer.toString(i2), count);
                                histogramElements.put(histogramElement);
                            }
                            i = i2 + 1;
                        }
                        rssiMap.put(Integer.toString(frequency), histogramElements);
                    }
                    printWriter.println("mWifiLogProto.rssiPollCount: " + rssiMap.toString());
                } catch (JSONException e) {
                    printWriter.println("JSONException occurred: " + e.getMessage());
                }
                printWriter.println("mWifiLogProto.rssiPollDeltaCount: Printing counts for [-127, 127]");
                StringBuilder sb = new StringBuilder();
                int i3 = -127;
                while (true) {
                    int i4 = i3;
                    if (i4 > 127) {
                        break;
                    }
                    sb.append(this.mRssiDeltaCounts.get(i4) + " ");
                    i3 = i4 + 1;
                }
                printWriter.println("  " + sb.toString());
                printWriter.print("mWifiLogProto.alertReasonCounts=");
                sb.setLength(0);
                for (int i5 = 0; i5 <= 64; i5++) {
                    if (this.mWifiAlertReasonCounts.get(i5) > 0) {
                        sb.append("(" + i5 + "," + count + "),");
                    }
                }
                if (sb.length() > 1) {
                    sb.setLength(sb.length() - 1);
                    printWriter.println(sb.toString());
                } else {
                    printWriter.println("()");
                }
                printWriter.println("mWifiLogProto.numTotalScanResults=" + this.mWifiLogProto.numTotalScanResults);
                printWriter.println("mWifiLogProto.numOpenNetworkScanResults=" + this.mWifiLogProto.numOpenNetworkScanResults);
                printWriter.println("mWifiLogProto.numPersonalNetworkScanResults=" + this.mWifiLogProto.numPersonalNetworkScanResults);
                printWriter.println("mWifiLogProto.numEnterpriseNetworkScanResults=" + this.mWifiLogProto.numEnterpriseNetworkScanResults);
                printWriter.println("mWifiLogProto.numHiddenNetworkScanResults=" + this.mWifiLogProto.numHiddenNetworkScanResults);
                printWriter.println("mWifiLogProto.numHotspot2R1NetworkScanResults=" + this.mWifiLogProto.numHotspot2R1NetworkScanResults);
                printWriter.println("mWifiLogProto.numHotspot2R2NetworkScanResults=" + this.mWifiLogProto.numHotspot2R2NetworkScanResults);
                printWriter.println("mWifiLogProto.numScans=" + this.mWifiLogProto.numScans);
                printWriter.println("mWifiLogProto.WifiScoreCount: [0, 60]");
                for (int i6 = 0; i6 <= 60; i6++) {
                    printWriter.print(this.mWifiScoreCounts.get(i6) + " ");
                }
                pw.println();
                printWriter.println("mWifiLogProto.SoftApManagerReturnCodeCounts:");
                printWriter.println("  SUCCESS: " + this.mSoftApManagerReturnCodeCounts.get(1));
                printWriter.println("  FAILED_GENERAL_ERROR: " + this.mSoftApManagerReturnCodeCounts.get(2));
                printWriter.println("  FAILED_NO_CHANNEL: " + this.mSoftApManagerReturnCodeCounts.get(3));
                printWriter.print("\n");
                printWriter.println("mWifiLogProto.numHalCrashes=" + this.mWifiLogProto.numHalCrashes);
                printWriter.println("mWifiLogProto.numWificondCrashes=" + this.mWifiLogProto.numWificondCrashes);
                printWriter.println("mWifiLogProto.numSupplicantCrashes=" + this.mWifiLogProto.numSupplicantCrashes);
                printWriter.println("mWifiLogProto.numHostapdCrashes=" + this.mWifiLogProto.numHostapdCrashes);
                printWriter.println("mWifiLogProto.numSetupClientInterfaceFailureDueToHal=" + this.mWifiLogProto.numSetupClientInterfaceFailureDueToHal);
                printWriter.println("mWifiLogProto.numSetupClientInterfaceFailureDueToWificond=" + this.mWifiLogProto.numSetupClientInterfaceFailureDueToWificond);
                printWriter.println("mWifiLogProto.numSetupClientInterfaceFailureDueToSupplicant=" + this.mWifiLogProto.numSetupClientInterfaceFailureDueToSupplicant);
                printWriter.println("mWifiLogProto.numSetupSoftApInterfaceFailureDueToHal=" + this.mWifiLogProto.numSetupSoftApInterfaceFailureDueToHal);
                printWriter.println("mWifiLogProto.numSetupSoftApInterfaceFailureDueToWificond=" + this.mWifiLogProto.numSetupSoftApInterfaceFailureDueToWificond);
                printWriter.println("mWifiLogProto.numSetupSoftApInterfaceFailureDueToHostapd=" + this.mWifiLogProto.numSetupSoftApInterfaceFailureDueToHostapd);
                printWriter.println("StaEventList:");
                Iterator it = this.mStaEventList.iterator();
                while (it.hasNext()) {
                    printWriter.println((StaEventWithTime) it.next());
                }
                printWriter.println("mWifiLogProto.numPasspointProviders=" + this.mWifiLogProto.numPasspointProviders);
                printWriter.println("mWifiLogProto.numPasspointProviderInstallation=" + this.mWifiLogProto.numPasspointProviderInstallation);
                printWriter.println("mWifiLogProto.numPasspointProviderInstallSuccess=" + this.mWifiLogProto.numPasspointProviderInstallSuccess);
                printWriter.println("mWifiLogProto.numPasspointProviderUninstallation=" + this.mWifiLogProto.numPasspointProviderUninstallation);
                printWriter.println("mWifiLogProto.numPasspointProviderUninstallSuccess=" + this.mWifiLogProto.numPasspointProviderUninstallSuccess);
                printWriter.println("mWifiLogProto.numPasspointProvidersSuccessfullyConnected=" + this.mWifiLogProto.numPasspointProvidersSuccessfullyConnected);
                printWriter.println("mWifiLogProto.numRadioModeChangeToMcc=" + this.mWifiLogProto.numRadioModeChangeToMcc);
                printWriter.println("mWifiLogProto.numRadioModeChangeToScc=" + this.mWifiLogProto.numRadioModeChangeToScc);
                printWriter.println("mWifiLogProto.numRadioModeChangeToSbs=" + this.mWifiLogProto.numRadioModeChangeToSbs);
                printWriter.println("mWifiLogProto.numRadioModeChangeToDbs=" + this.mWifiLogProto.numRadioModeChangeToDbs);
                printWriter.println("mWifiLogProto.numSoftApUserBandPreferenceUnsatisfied=" + this.mWifiLogProto.numSoftApUserBandPreferenceUnsatisfied);
                printWriter.println("mTotalSsidsInScanHistogram:" + this.mTotalSsidsInScanHistogram.toString());
                printWriter.println("mTotalBssidsInScanHistogram:" + this.mTotalBssidsInScanHistogram.toString());
                printWriter.println("mAvailableOpenSsidsInScanHistogram:" + this.mAvailableOpenSsidsInScanHistogram.toString());
                printWriter.println("mAvailableOpenBssidsInScanHistogram:" + this.mAvailableOpenBssidsInScanHistogram.toString());
                printWriter.println("mAvailableSavedSsidsInScanHistogram:" + this.mAvailableSavedSsidsInScanHistogram.toString());
                printWriter.println("mAvailableSavedBssidsInScanHistogram:" + this.mAvailableSavedBssidsInScanHistogram.toString());
                printWriter.println("mAvailableOpenOrSavedSsidsInScanHistogram:" + this.mAvailableOpenOrSavedSsidsInScanHistogram.toString());
                printWriter.println("mAvailableOpenOrSavedBssidsInScanHistogram:" + this.mAvailableOpenOrSavedBssidsInScanHistogram.toString());
                printWriter.println("mAvailableSavedPasspointProviderProfilesInScanHistogram:" + this.mAvailableSavedPasspointProviderProfilesInScanHistogram.toString());
                printWriter.println("mAvailableSavedPasspointProviderBssidsInScanHistogram:" + this.mAvailableSavedPasspointProviderBssidsInScanHistogram.toString());
                printWriter.println("mWifiLogProto.partialAllSingleScanListenerResults=" + this.mWifiLogProto.partialAllSingleScanListenerResults);
                printWriter.println("mWifiLogProto.fullBandAllSingleScanListenerResults=" + this.mWifiLogProto.fullBandAllSingleScanListenerResults);
                printWriter.println("mWifiAwareMetrics:");
                this.mWifiAwareMetrics.dump(fileDescriptor, printWriter, strArr);
                printWriter.println("mRttMetrics:");
                this.mRttMetrics.dump(fileDescriptor, printWriter, strArr);
                printWriter.println("mPnoScanMetrics.numPnoScanAttempts=" + this.mPnoScanMetrics.numPnoScanAttempts);
                printWriter.println("mPnoScanMetrics.numPnoScanFailed=" + this.mPnoScanMetrics.numPnoScanFailed);
                printWriter.println("mPnoScanMetrics.numPnoScanStartedOverOffload=" + this.mPnoScanMetrics.numPnoScanStartedOverOffload);
                printWriter.println("mPnoScanMetrics.numPnoScanFailedOverOffload=" + this.mPnoScanMetrics.numPnoScanFailedOverOffload);
                printWriter.println("mPnoScanMetrics.numPnoFoundNetworkEvents=" + this.mPnoScanMetrics.numPnoFoundNetworkEvents);
                printWriter.println("mWifiLogProto.connectToNetworkNotificationCount=" + this.mConnectToNetworkNotificationCount.toString());
                printWriter.println("mWifiLogProto.connectToNetworkNotificationActionCount=" + this.mConnectToNetworkNotificationActionCount.toString());
                printWriter.println("mWifiLogProto.openNetworkRecommenderBlacklistSize=" + this.mOpenNetworkRecommenderBlacklistSize);
                printWriter.println("mWifiLogProto.isWifiNetworksAvailableNotificationOn=" + this.mIsWifiNetworksAvailableNotificationOn);
                printWriter.println("mWifiLogProto.numOpenNetworkRecommendationUpdates=" + this.mNumOpenNetworkRecommendationUpdates);
                printWriter.println("mWifiLogProto.numOpenNetworkConnectMessageFailedToSend=" + this.mNumOpenNetworkConnectMessageFailedToSend);
                printWriter.println("mWifiLogProto.observedHotspotR1ApInScanHistogram=" + this.mObservedHotspotR1ApInScanHistogram);
                printWriter.println("mWifiLogProto.observedHotspotR2ApInScanHistogram=" + this.mObservedHotspotR2ApInScanHistogram);
                printWriter.println("mWifiLogProto.observedHotspotR1EssInScanHistogram=" + this.mObservedHotspotR1EssInScanHistogram);
                printWriter.println("mWifiLogProto.observedHotspotR2EssInScanHistogram=" + this.mObservedHotspotR2EssInScanHistogram);
                printWriter.println("mWifiLogProto.observedHotspotR1ApsPerEssInScanHistogram=" + this.mObservedHotspotR1ApsPerEssInScanHistogram);
                printWriter.println("mWifiLogProto.observedHotspotR2ApsPerEssInScanHistogram=" + this.mObservedHotspotR2ApsPerEssInScanHistogram);
                printWriter.println("mWifiLogProto.observed80211mcSupportingApsInScanHistogram" + this.mObserved80211mcApInScanHistogram);
                printWriter.println("mSoftApTetheredEvents:");
                for (WifiMetricsProto.SoftApConnectedClientsEvent event2 : this.mSoftApEventListTethered) {
                    StringBuilder eventLine2 = new StringBuilder();
                    eventLine2.append("event_type=" + event2.eventType);
                    eventLine2.append(",time_stamp_millis=" + event2.timeStampMillis);
                    eventLine2.append(",num_connected_clients=" + event2.numConnectedClients);
                    eventLine2.append(",channel_frequency=" + event2.channelFrequency);
                    eventLine2.append(",channel_bandwidth=" + event2.channelBandwidth);
                    printWriter.println(eventLine2.toString());
                }
                printWriter.println("mSoftApLocalOnlyEvents:");
                for (WifiMetricsProto.SoftApConnectedClientsEvent event3 : this.mSoftApEventListLocalOnly) {
                    StringBuilder eventLine3 = new StringBuilder();
                    eventLine3.append("event_type=" + event3.eventType);
                    eventLine3.append(",time_stamp_millis=" + event3.timeStampMillis);
                    eventLine3.append(",num_connected_clients=" + event3.numConnectedClients);
                    eventLine3.append(",channel_frequency=" + event3.channelFrequency);
                    eventLine3.append(",channel_bandwidth=" + event3.channelBandwidth);
                    printWriter.println(eventLine3.toString());
                }
                printWriter.println("mWpsMetrics.numWpsAttempts=" + this.mWpsMetrics.numWpsAttempts);
                printWriter.println("mWpsMetrics.numWpsSuccess=" + this.mWpsMetrics.numWpsSuccess);
                printWriter.println("mWpsMetrics.numWpsStartFailure=" + this.mWpsMetrics.numWpsStartFailure);
                printWriter.println("mWpsMetrics.numWpsOverlapFailure=" + this.mWpsMetrics.numWpsOverlapFailure);
                printWriter.println("mWpsMetrics.numWpsTimeoutFailure=" + this.mWpsMetrics.numWpsTimeoutFailure);
                printWriter.println("mWpsMetrics.numWpsOtherConnectionFailure=" + this.mWpsMetrics.numWpsOtherConnectionFailure);
                printWriter.println("mWpsMetrics.numWpsSupplicantFailure=" + this.mWpsMetrics.numWpsSupplicantFailure);
                printWriter.println("mWpsMetrics.numWpsCancellation=" + this.mWpsMetrics.numWpsCancellation);
                this.mWifiPowerMetrics.dump(printWriter);
                this.mWifiWakeMetrics.dump(printWriter);
                printWriter.println("mWifiLogProto.isMacRandomizationOn=" + this.mIsMacRandomizationOn);
                printWriter.println("mWifiLogProto.scoreExperimentId=" + this.mWifiLogProto.scoreExperimentId);
            } else {
                consolidateProto(true);
                for (ConnectionEvent event4 : this.mConnectionEventList) {
                    if (this.mCurrentConnectionEvent != event4) {
                        event4.mConnectionEvent.automaticBugReportTaken = true;
                    }
                }
                String metricsProtoDump = Base64.encodeToString(WifiMetricsProto.WifiLog.toByteArray(this.mWifiLogProto), 0);
                if (strArr.length <= 1 || !CLEAN_DUMP_ARG.equals(strArr[1])) {
                    printWriter.println("WifiMetrics:");
                    printWriter.println(metricsProtoDump);
                    printWriter.println("EndWifiMetrics");
                } else {
                    printWriter.print(metricsProtoDump);
                }
                clear();
            }
        }
    }

    public void updateSavedNetworks(List<WifiConfiguration> networks) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSavedNetworks = networks.size();
            this.mWifiLogProto.numOpenNetworks = 0;
            this.mWifiLogProto.numPersonalNetworks = 0;
            this.mWifiLogProto.numEnterpriseNetworks = 0;
            this.mWifiLogProto.numNetworksAddedByUser = 0;
            this.mWifiLogProto.numNetworksAddedByApps = 0;
            this.mWifiLogProto.numHiddenNetworks = 0;
            this.mWifiLogProto.numPasspointNetworks = 0;
            for (WifiConfiguration config : networks) {
                if (config.allowedKeyManagement.get(0)) {
                    this.mWifiLogProto.numOpenNetworks++;
                } else if (config.isEnterprise()) {
                    this.mWifiLogProto.numEnterpriseNetworks++;
                } else {
                    this.mWifiLogProto.numPersonalNetworks++;
                }
                if (config.selfAdded) {
                    this.mWifiLogProto.numNetworksAddedByUser++;
                } else {
                    this.mWifiLogProto.numNetworksAddedByApps++;
                }
                if (config.hiddenSSID) {
                    this.mWifiLogProto.numHiddenNetworks++;
                }
                if (config.isPasspoint()) {
                    this.mWifiLogProto.numPasspointNetworks++;
                }
            }
        }
    }

    public void updateSavedPasspointProfiles(int numSavedProfiles, int numConnectedProfiles) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numPasspointProviders = numSavedProfiles;
            this.mWifiLogProto.numPasspointProvidersSuccessfullyConnected = numConnectedProfiles;
        }
    }

    private void consolidateProto(boolean incremental) {
        List<WifiMetricsProto.ConnectionEvent> events = new ArrayList<>();
        List<WifiMetricsProto.RssiPollCount> rssis = new ArrayList<>();
        List<WifiMetricsProto.RssiPollCount> rssiDeltas = new ArrayList<>();
        List<WifiMetricsProto.AlertReasonCount> alertReasons = new ArrayList<>();
        List<WifiMetricsProto.WifiScoreCount> scores = new ArrayList<>();
        synchronized (this.mLock) {
            for (ConnectionEvent event : this.mConnectionEventList) {
                if (!incremental || (this.mCurrentConnectionEvent != event && !event.mConnectionEvent.automaticBugReportTaken)) {
                    events.add(event.mConnectionEvent);
                    if (incremental) {
                        event.mConnectionEvent.automaticBugReportTaken = true;
                    }
                }
            }
            if (events.size() > 0) {
                this.mWifiLogProto.connectionEvent = (WifiMetricsProto.ConnectionEvent[]) events.toArray(this.mWifiLogProto.connectionEvent);
            }
            this.mWifiLogProto.scanReturnEntries = new WifiMetricsProto.WifiLog.ScanReturnEntry[this.mScanReturnEntries.size()];
            for (int i = 0; i < this.mScanReturnEntries.size(); i++) {
                this.mWifiLogProto.scanReturnEntries[i] = new WifiMetricsProto.WifiLog.ScanReturnEntry();
                this.mWifiLogProto.scanReturnEntries[i].scanReturnCode = this.mScanReturnEntries.keyAt(i);
                this.mWifiLogProto.scanReturnEntries[i].scanResultsCount = this.mScanReturnEntries.valueAt(i);
            }
            this.mWifiLogProto.wifiSystemStateEntries = new WifiMetricsProto.WifiLog.WifiSystemStateEntry[this.mWifiSystemStateEntries.size()];
            for (int i2 = 0; i2 < this.mWifiSystemStateEntries.size(); i2++) {
                this.mWifiLogProto.wifiSystemStateEntries[i2] = new WifiMetricsProto.WifiLog.WifiSystemStateEntry();
                this.mWifiLogProto.wifiSystemStateEntries[i2].wifiState = this.mWifiSystemStateEntries.keyAt(i2) / 2;
                this.mWifiLogProto.wifiSystemStateEntries[i2].wifiStateCount = this.mWifiSystemStateEntries.valueAt(i2);
                this.mWifiLogProto.wifiSystemStateEntries[i2].isScreenOn = this.mWifiSystemStateEntries.keyAt(i2) % 2 > 0;
            }
            this.mWifiLogProto.recordDurationSec = (int) ((this.mClock.getElapsedSinceBootMillis() / 1000) - this.mRecordStartTimeSec);
            for (Map.Entry<Integer, SparseIntArray> entry : this.mRssiPollCountsMap.entrySet()) {
                int frequency = entry.getKey().intValue();
                SparseIntArray histogram = entry.getValue();
                for (int i3 = 0; i3 < histogram.size(); i3++) {
                    WifiMetricsProto.RssiPollCount keyVal = new WifiMetricsProto.RssiPollCount();
                    keyVal.rssi = histogram.keyAt(i3);
                    keyVal.count = histogram.valueAt(i3);
                    keyVal.frequency = frequency;
                    rssis.add(keyVal);
                }
            }
            this.mWifiLogProto.rssiPollRssiCount = (WifiMetricsProto.RssiPollCount[]) rssis.toArray(this.mWifiLogProto.rssiPollRssiCount);
            for (int i4 = 0; i4 < this.mRssiDeltaCounts.size(); i4++) {
                WifiMetricsProto.RssiPollCount keyVal2 = new WifiMetricsProto.RssiPollCount();
                keyVal2.rssi = this.mRssiDeltaCounts.keyAt(i4);
                keyVal2.count = this.mRssiDeltaCounts.valueAt(i4);
                rssiDeltas.add(keyVal2);
            }
            this.mWifiLogProto.rssiPollDeltaCount = (WifiMetricsProto.RssiPollCount[]) rssiDeltas.toArray(this.mWifiLogProto.rssiPollDeltaCount);
            for (int i5 = 0; i5 < this.mWifiAlertReasonCounts.size(); i5++) {
                WifiMetricsProto.AlertReasonCount keyVal3 = new WifiMetricsProto.AlertReasonCount();
                keyVal3.reason = this.mWifiAlertReasonCounts.keyAt(i5);
                keyVal3.count = this.mWifiAlertReasonCounts.valueAt(i5);
                alertReasons.add(keyVal3);
            }
            this.mWifiLogProto.alertReasonCount = (WifiMetricsProto.AlertReasonCount[]) alertReasons.toArray(this.mWifiLogProto.alertReasonCount);
            for (int score = 0; score < this.mWifiScoreCounts.size(); score++) {
                WifiMetricsProto.WifiScoreCount keyVal4 = new WifiMetricsProto.WifiScoreCount();
                keyVal4.score = this.mWifiScoreCounts.keyAt(score);
                keyVal4.count = this.mWifiScoreCounts.valueAt(score);
                scores.add(keyVal4);
            }
            this.mWifiLogProto.wifiScoreCount = (WifiMetricsProto.WifiScoreCount[]) scores.toArray(this.mWifiLogProto.wifiScoreCount);
            int codeCounts = this.mSoftApManagerReturnCodeCounts.size();
            this.mWifiLogProto.softApReturnCode = new WifiMetricsProto.SoftApReturnCodeCount[codeCounts];
            for (int sapCode = 0; sapCode < codeCounts; sapCode++) {
                this.mWifiLogProto.softApReturnCode[sapCode] = new WifiMetricsProto.SoftApReturnCodeCount();
                this.mWifiLogProto.softApReturnCode[sapCode].startResult = this.mSoftApManagerReturnCodeCounts.keyAt(sapCode);
                this.mWifiLogProto.softApReturnCode[sapCode].count = this.mSoftApManagerReturnCodeCounts.valueAt(sapCode);
            }
            this.mWifiLogProto.staEventList = new WifiMetricsProto.StaEvent[this.mStaEventList.size()];
            for (int i6 = 0; i6 < this.mStaEventList.size(); i6++) {
                this.mWifiLogProto.staEventList[i6] = this.mStaEventList.get(i6).staEvent;
            }
            this.mWifiLogProto.totalSsidsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mTotalSsidsInScanHistogram);
            this.mWifiLogProto.totalBssidsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mTotalBssidsInScanHistogram);
            this.mWifiLogProto.availableOpenSsidsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mAvailableOpenSsidsInScanHistogram);
            this.mWifiLogProto.availableOpenBssidsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mAvailableOpenBssidsInScanHistogram);
            this.mWifiLogProto.availableSavedSsidsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mAvailableSavedSsidsInScanHistogram);
            this.mWifiLogProto.availableSavedBssidsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mAvailableSavedBssidsInScanHistogram);
            this.mWifiLogProto.availableOpenOrSavedSsidsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mAvailableOpenOrSavedSsidsInScanHistogram);
            this.mWifiLogProto.availableOpenOrSavedBssidsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mAvailableOpenOrSavedBssidsInScanHistogram);
            this.mWifiLogProto.availableSavedPasspointProviderProfilesInScanHistogram = makeNumConnectableNetworksBucketArray(this.mAvailableSavedPasspointProviderProfilesInScanHistogram);
            this.mWifiLogProto.availableSavedPasspointProviderBssidsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mAvailableSavedPasspointProviderBssidsInScanHistogram);
            this.mWifiLogProto.wifiAwareLog = this.mWifiAwareMetrics.consolidateProto();
            this.mWifiLogProto.wifiRttLog = this.mRttMetrics.consolidateProto();
            this.mWifiLogProto.pnoScanMetrics = this.mPnoScanMetrics;
            WifiMetricsProto.ConnectToNetworkNotificationAndActionCount[] notificationCountArray = new WifiMetricsProto.ConnectToNetworkNotificationAndActionCount[this.mConnectToNetworkNotificationCount.size()];
            for (int i7 = 0; i7 < this.mConnectToNetworkNotificationCount.size(); i7++) {
                WifiMetricsProto.ConnectToNetworkNotificationAndActionCount keyVal5 = new WifiMetricsProto.ConnectToNetworkNotificationAndActionCount();
                keyVal5.notification = this.mConnectToNetworkNotificationCount.keyAt(i7);
                keyVal5.recommender = 1;
                keyVal5.count = this.mConnectToNetworkNotificationCount.valueAt(i7);
                notificationCountArray[i7] = keyVal5;
            }
            this.mWifiLogProto.connectToNetworkNotificationCount = notificationCountArray;
            WifiMetricsProto.ConnectToNetworkNotificationAndActionCount[] notificationActionCountArray = new WifiMetricsProto.ConnectToNetworkNotificationAndActionCount[this.mConnectToNetworkNotificationActionCount.size()];
            int i8 = 0;
            while (true) {
                int i9 = i8;
                if (i9 >= this.mConnectToNetworkNotificationActionCount.size()) {
                    break;
                }
                WifiMetricsProto.ConnectToNetworkNotificationAndActionCount keyVal6 = new WifiMetricsProto.ConnectToNetworkNotificationAndActionCount();
                int key = this.mConnectToNetworkNotificationActionCount.keyAt(i9);
                keyVal6.notification = key / CONNECT_TO_NETWORK_NOTIFICATION_ACTION_KEY_MULTIPLIER;
                keyVal6.action = key % CONNECT_TO_NETWORK_NOTIFICATION_ACTION_KEY_MULTIPLIER;
                keyVal6.recommender = 1;
                keyVal6.count = this.mConnectToNetworkNotificationActionCount.valueAt(i9);
                notificationActionCountArray[i9] = keyVal6;
                i8 = i9 + 1;
            }
            this.mWifiLogProto.connectToNetworkNotificationActionCount = notificationActionCountArray;
            this.mWifiLogProto.openNetworkRecommenderBlacklistSize = this.mOpenNetworkRecommenderBlacklistSize;
            this.mWifiLogProto.isWifiNetworksAvailableNotificationOn = this.mIsWifiNetworksAvailableNotificationOn;
            this.mWifiLogProto.numOpenNetworkRecommendationUpdates = this.mNumOpenNetworkRecommendationUpdates;
            this.mWifiLogProto.numOpenNetworkConnectMessageFailedToSend = this.mNumOpenNetworkConnectMessageFailedToSend;
            this.mWifiLogProto.observedHotspotR1ApsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mObservedHotspotR1ApInScanHistogram);
            this.mWifiLogProto.observedHotspotR2ApsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mObservedHotspotR2ApInScanHistogram);
            this.mWifiLogProto.observedHotspotR1EssInScanHistogram = makeNumConnectableNetworksBucketArray(this.mObservedHotspotR1EssInScanHistogram);
            this.mWifiLogProto.observedHotspotR2EssInScanHistogram = makeNumConnectableNetworksBucketArray(this.mObservedHotspotR2EssInScanHistogram);
            this.mWifiLogProto.observedHotspotR1ApsPerEssInScanHistogram = makeNumConnectableNetworksBucketArray(this.mObservedHotspotR1ApsPerEssInScanHistogram);
            this.mWifiLogProto.observedHotspotR2ApsPerEssInScanHistogram = makeNumConnectableNetworksBucketArray(this.mObservedHotspotR2ApsPerEssInScanHistogram);
            this.mWifiLogProto.observed80211McSupportingApsInScanHistogram = makeNumConnectableNetworksBucketArray(this.mObserved80211mcApInScanHistogram);
            if (this.mSoftApEventListTethered.size() > 0) {
                this.mWifiLogProto.softApConnectedClientsEventsTethered = (WifiMetricsProto.SoftApConnectedClientsEvent[]) this.mSoftApEventListTethered.toArray(this.mWifiLogProto.softApConnectedClientsEventsTethered);
            }
            if (this.mSoftApEventListLocalOnly.size() > 0) {
                this.mWifiLogProto.softApConnectedClientsEventsLocalOnly = (WifiMetricsProto.SoftApConnectedClientsEvent[]) this.mSoftApEventListLocalOnly.toArray(this.mWifiLogProto.softApConnectedClientsEventsLocalOnly);
            }
            this.mWifiLogProto.wpsMetrics = this.mWpsMetrics;
            this.mWifiLogProto.wifiPowerStats = this.mWifiPowerMetrics.buildProto();
            this.mWifiLogProto.wifiWakeStats = this.mWifiWakeMetrics.buildProto();
            this.mWifiLogProto.isMacRandomizationOn = this.mIsMacRandomizationOn;
        }
    }

    private void consolidateScoringParams() {
        synchronized (this.mLock) {
            if (this.mScoringParams != null) {
                int experimentIdentifier = this.mScoringParams.getExperimentIdentifier();
                if (experimentIdentifier == 0) {
                    this.mWifiLogProto.scoreExperimentId = "";
                } else {
                    WifiMetricsProto.WifiLog wifiLog = this.mWifiLogProto;
                    wifiLog.scoreExperimentId = "x" + experimentIdentifier;
                }
            }
        }
    }

    private WifiMetricsProto.NumConnectableNetworksBucket[] makeNumConnectableNetworksBucketArray(SparseIntArray sia) {
        WifiMetricsProto.NumConnectableNetworksBucket[] array = new WifiMetricsProto.NumConnectableNetworksBucket[sia.size()];
        for (int i = 0; i < sia.size(); i++) {
            WifiMetricsProto.NumConnectableNetworksBucket keyVal = new WifiMetricsProto.NumConnectableNetworksBucket();
            keyVal.numConnectableNetworks = sia.keyAt(i);
            keyVal.count = sia.valueAt(i);
            array[i] = keyVal;
        }
        return array;
    }

    private void clear() {
        synchronized (this.mLock) {
            this.mConnectionEventList.clear();
            if (this.mCurrentConnectionEvent != null) {
                this.mConnectionEventList.add(this.mCurrentConnectionEvent);
            }
            this.mScanReturnEntries.clear();
            this.mWifiSystemStateEntries.clear();
            this.mRecordStartTimeSec = this.mClock.getElapsedSinceBootMillis() / 1000;
            this.mRssiPollCountsMap.clear();
            this.mRssiDeltaCounts.clear();
            this.mWifiAlertReasonCounts.clear();
            this.mWifiScoreCounts.clear();
            this.mWifiLogProto.clear();
            this.mScanResultRssiTimestampMillis = -1;
            this.mSoftApManagerReturnCodeCounts.clear();
            this.mStaEventList.clear();
            this.mWifiAwareMetrics.clear();
            this.mRttMetrics.clear();
            this.mTotalSsidsInScanHistogram.clear();
            this.mTotalBssidsInScanHistogram.clear();
            this.mAvailableOpenSsidsInScanHistogram.clear();
            this.mAvailableOpenBssidsInScanHistogram.clear();
            this.mAvailableSavedSsidsInScanHistogram.clear();
            this.mAvailableSavedBssidsInScanHistogram.clear();
            this.mAvailableOpenOrSavedSsidsInScanHistogram.clear();
            this.mAvailableOpenOrSavedBssidsInScanHistogram.clear();
            this.mAvailableSavedPasspointProviderProfilesInScanHistogram.clear();
            this.mAvailableSavedPasspointProviderBssidsInScanHistogram.clear();
            this.mPnoScanMetrics.clear();
            this.mConnectToNetworkNotificationCount.clear();
            this.mConnectToNetworkNotificationActionCount.clear();
            this.mNumOpenNetworkRecommendationUpdates = 0;
            this.mNumOpenNetworkConnectMessageFailedToSend = 0;
            this.mObservedHotspotR1ApInScanHistogram.clear();
            this.mObservedHotspotR2ApInScanHistogram.clear();
            this.mObservedHotspotR1EssInScanHistogram.clear();
            this.mObservedHotspotR2EssInScanHistogram.clear();
            this.mObservedHotspotR1ApsPerEssInScanHistogram.clear();
            this.mObservedHotspotR2ApsPerEssInScanHistogram.clear();
            this.mSoftApEventListTethered.clear();
            this.mSoftApEventListLocalOnly.clear();
            this.mWpsMetrics.clear();
            this.mWifiWakeMetrics.clear();
            this.mObserved80211mcApInScanHistogram.clear();
        }
    }

    public void setScreenState(boolean screenOn) {
        synchronized (this.mLock) {
            this.mScreenOn = screenOn;
        }
    }

    public void setWifiState(int wifiState) {
        synchronized (this.mLock) {
            this.mWifiState = wifiState;
            this.mWifiWins = wifiState == 3;
        }
    }

    /* access modifiers changed from: private */
    public void processMessage(Message msg) {
        WifiMetricsProto.StaEvent event = new WifiMetricsProto.StaEvent();
        boolean logEvent = true;
        boolean z = false;
        switch (msg.what) {
            case 131213:
                event.type = 10;
                break;
            case 131219:
                event.type = 6;
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT:
                event.type = 3;
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                event.type = 4;
                event.reason = msg.arg2;
                if (msg.arg1 != 0) {
                    z = true;
                }
                event.localGen = z;
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                logEvent = false;
                this.mSupplicantStateChangeBitmask |= supplicantStateToBit(((StateChangeResult) msg.obj).state);
                break;
            case WifiMonitor.AUTHENTICATION_FAILURE_EVENT:
                event.type = 2;
                switch (msg.arg1) {
                    case 0:
                        event.authFailureReason = 1;
                        break;
                    case 1:
                        event.authFailureReason = 2;
                        break;
                    case 2:
                        event.authFailureReason = 3;
                        break;
                    case 3:
                        event.authFailureReason = 4;
                        break;
                }
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                event.type = 1;
                if (msg.arg1 > 0) {
                    z = true;
                }
                event.associationTimedOut = z;
                event.status = msg.arg2;
                break;
            default:
                return;
        }
        if (logEvent) {
            addStaEvent(event);
        }
    }

    public void logStaEvent(int type) {
        logStaEvent(type, 0, null);
    }

    public void logStaEvent(int type, WifiConfiguration config) {
        logStaEvent(type, 0, config);
    }

    public void logStaEvent(int type, int frameworkDisconnectReason) {
        logStaEvent(type, frameworkDisconnectReason, null);
    }

    public void logStaEvent(int type, int frameworkDisconnectReason, WifiConfiguration config) {
        switch (type) {
            case 7:
            case 8:
            case 9:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
                WifiMetricsProto.StaEvent event = new WifiMetricsProto.StaEvent();
                event.type = type;
                if (frameworkDisconnectReason != 0) {
                    event.frameworkDisconnectReason = frameworkDisconnectReason;
                }
                event.configInfo = createConfigInfo(config);
                addStaEvent(event);
                return;
            default:
                Log.e(TAG, "Unknown StaEvent:" + type);
                return;
        }
    }

    private void addStaEvent(WifiMetricsProto.StaEvent staEvent) {
        staEvent.startTimeMillis = this.mClock.getElapsedSinceBootMillis();
        staEvent.lastRssi = this.mLastPollRssi;
        staEvent.lastFreq = this.mLastPollFreq;
        staEvent.lastLinkSpeed = this.mLastPollLinkSpeed;
        staEvent.supplicantStateChangesBitmask = this.mSupplicantStateChangeBitmask;
        staEvent.lastScore = this.mLastScore;
        this.mSupplicantStateChangeBitmask = 0;
        this.mLastPollRssi = -127;
        this.mLastPollFreq = -1;
        this.mLastPollLinkSpeed = -1;
        this.mLastScore = -1;
        this.mStaEventList.add(new StaEventWithTime(staEvent, this.mClock.getWallClockMillis()));
        if (this.mStaEventList.size() > 768) {
            this.mStaEventList.remove();
        }
    }

    private WifiMetricsProto.StaEvent.ConfigInfo createConfigInfo(WifiConfiguration config) {
        if (config == null) {
            return null;
        }
        WifiMetricsProto.StaEvent.ConfigInfo info = new WifiMetricsProto.StaEvent.ConfigInfo();
        info.allowedKeyManagement = bitSetToInt(config.allowedKeyManagement);
        info.allowedProtocols = bitSetToInt(config.allowedProtocols);
        info.allowedAuthAlgorithms = bitSetToInt(config.allowedAuthAlgorithms);
        info.allowedPairwiseCiphers = bitSetToInt(config.allowedPairwiseCiphers);
        info.allowedGroupCiphers = bitSetToInt(config.allowedGroupCiphers);
        info.hiddenSsid = config.hiddenSSID;
        info.isPasspoint = config.isPasspoint();
        info.isEphemeral = config.isEphemeral();
        info.hasEverConnected = config.getNetworkSelectionStatus().getHasEverConnected();
        ScanResult candidate = config.getNetworkSelectionStatus().getCandidate();
        if (candidate != null) {
            info.scanRssi = candidate.level;
            info.scanFreq = candidate.frequency;
        }
        return info;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public WifiAwareMetrics getWifiAwareMetrics() {
        return this.mWifiAwareMetrics;
    }

    public WifiWakeMetrics getWakeupMetrics() {
        return this.mWifiWakeMetrics;
    }

    public RttMetrics getRttMetrics() {
        return this.mRttMetrics;
    }

    public static int supplicantStateToBit(SupplicantState state) {
        switch (AnonymousClass2.$SwitchMap$android$net$wifi$SupplicantState[state.ordinal()]) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 4;
            case 4:
                return 8;
            case 5:
                return 16;
            case 6:
                return 32;
            case 7:
                return 64;
            case 8:
                return 128;
            case 9:
                return 256;
            case 10:
                return 512;
            case 11:
                return 1024;
            case 12:
                return 2048;
            case 13:
                return 4096;
            default:
                Log.wtf(TAG, "Got unknown supplicant state: " + state.ordinal());
                return 0;
        }
    }

    private static String supplicantStateChangesBitmaskToString(int mask) {
        StringBuilder sb = new StringBuilder();
        sb.append("supplicantStateChangeEvents: {");
        if ((mask & 1) > 0) {
            sb.append(" DISCONNECTED");
        }
        if ((mask & 2) > 0) {
            sb.append(" INTERFACE_DISABLED");
        }
        if ((mask & 4) > 0) {
            sb.append(" INACTIVE");
        }
        if ((mask & 8) > 0) {
            sb.append(" SCANNING");
        }
        if ((mask & 16) > 0) {
            sb.append(" AUTHENTICATING");
        }
        if ((mask & 32) > 0) {
            sb.append(" ASSOCIATING");
        }
        if ((mask & 64) > 0) {
            sb.append(" ASSOCIATED");
        }
        if ((mask & 128) > 0) {
            sb.append(" FOUR_WAY_HANDSHAKE");
        }
        if ((mask & 256) > 0) {
            sb.append(" GROUP_HANDSHAKE");
        }
        if ((mask & 512) > 0) {
            sb.append(" COMPLETED");
        }
        if ((mask & 1024) > 0) {
            sb.append(" DORMANT");
        }
        if ((mask & 2048) > 0) {
            sb.append(" UNINITIALIZED");
        }
        if ((mask & 4096) > 0) {
            sb.append(" INVALID");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String staEventToString(WifiMetricsProto.StaEvent event) {
        if (event == null) {
            return "<NULL>";
        }
        StringBuilder sb = new StringBuilder();
        switch (event.type) {
            case 1:
                sb.append("ASSOCIATION_REJECTION_EVENT");
                sb.append(" timedOut=");
                sb.append(event.associationTimedOut);
                sb.append(" status=");
                sb.append(event.status);
                sb.append(":");
                sb.append(ISupplicantStaIfaceCallback.StatusCode.toString(event.status));
                break;
            case 2:
                sb.append("AUTHENTICATION_FAILURE_EVENT reason=");
                sb.append(event.authFailureReason);
                sb.append(":");
                sb.append(authFailureReasonToString(event.authFailureReason));
                break;
            case 3:
                sb.append("NETWORK_CONNECTION_EVENT");
                break;
            case 4:
                sb.append("NETWORK_DISCONNECTION_EVENT");
                sb.append(" local_gen=");
                sb.append(event.localGen);
                sb.append(" reason=");
                sb.append(event.reason);
                sb.append(":");
                sb.append(ISupplicantStaIfaceCallback.ReasonCode.toString(event.reason >= 0 ? event.reason : event.reason * -1));
                break;
            case 6:
                sb.append("CMD_ASSOCIATED_BSSID");
                break;
            case 7:
                sb.append("CMD_IP_CONFIGURATION_SUCCESSFUL");
                break;
            case 8:
                sb.append("CMD_IP_CONFIGURATION_LOST");
                break;
            case 9:
                sb.append("CMD_IP_REACHABILITY_LOST");
                break;
            case 10:
                sb.append("CMD_TARGET_BSSID");
                break;
            case 11:
                sb.append("CMD_START_CONNECT");
                break;
            case 12:
                sb.append("CMD_START_ROAM");
                break;
            case 13:
                sb.append("CONNECT_NETWORK");
                break;
            case 14:
                sb.append("NETWORK_AGENT_VALID_NETWORK");
                break;
            case 15:
                sb.append("FRAMEWORK_DISCONNECT");
                sb.append(" reason=");
                sb.append(frameworkDisconnectReasonToString(event.frameworkDisconnectReason));
                break;
            case 16:
                sb.append("SCORE_BREACH");
                break;
            case 17:
                sb.append("MAC_CHANGE");
                break;
            default:
                sb.append("UNKNOWN " + event.type + ":");
                break;
        }
        if (event.lastRssi != -127) {
            sb.append(" lastRssi=");
            sb.append(event.lastRssi);
        }
        if (event.lastFreq != -1) {
            sb.append(" lastFreq=");
            sb.append(event.lastFreq);
        }
        if (event.lastLinkSpeed != -1) {
            sb.append(" lastLinkSpeed=");
            sb.append(event.lastLinkSpeed);
        }
        if (event.lastScore != -1) {
            sb.append(" lastScore=");
            sb.append(event.lastScore);
        }
        if (event.supplicantStateChangesBitmask != 0) {
            sb.append(", ");
            sb.append(supplicantStateChangesBitmaskToString(event.supplicantStateChangesBitmask));
        }
        if (event.configInfo != null) {
            sb.append(", ");
            sb.append(configInfoToString(event.configInfo));
        }
        return sb.toString();
    }

    private static String authFailureReasonToString(int authFailureReason) {
        switch (authFailureReason) {
            case 1:
                return "ERROR_AUTH_FAILURE_NONE";
            case 2:
                return "ERROR_AUTH_FAILURE_TIMEOUT";
            case 3:
                return "ERROR_AUTH_FAILURE_WRONG_PSWD";
            case 4:
                return "ERROR_AUTH_FAILURE_EAP_FAILURE";
            default:
                return "";
        }
    }

    private static String frameworkDisconnectReasonToString(int frameworkDisconnectReason) {
        switch (frameworkDisconnectReason) {
            case 1:
                return "DISCONNECT_API";
            case 2:
                return "DISCONNECT_GENERIC";
            case 3:
                return "DISCONNECT_UNWANTED";
            case 4:
                return "DISCONNECT_ROAM_WATCHDOG_TIMER";
            case 5:
                return "DISCONNECT_P2P_DISCONNECT_WIFI_REQUEST";
            case 6:
                return "DISCONNECT_RESET_SIM_NETWORKS";
            default:
                return "DISCONNECT_UNKNOWN=" + frameworkDisconnectReason;
        }
    }

    private static String configInfoToString(WifiMetricsProto.StaEvent.ConfigInfo info) {
        return "ConfigInfo:" + " allowed_key_management=" + info.allowedKeyManagement + " allowed_protocols=" + info.allowedProtocols + " allowed_auth_algorithms=" + info.allowedAuthAlgorithms + " allowed_pairwise_ciphers=" + info.allowedPairwiseCiphers + " allowed_group_ciphers=" + info.allowedGroupCiphers + " hidden_ssid=" + info.hiddenSsid + " is_passpoint=" + info.isPasspoint + " is_ephemeral=" + info.isEphemeral + " has_ever_connected=" + info.hasEverConnected + " scan_rssi=" + info.scanRssi + " scan_freq=" + info.scanFreq;
    }

    private static int bitSetToInt(BitSet bits) {
        int i = 31;
        if (bits.length() < 31) {
            i = bits.length();
        }
        int nBits = i;
        int value = 0;
        for (int i2 = 0; i2 < nBits; i2++) {
            value += bits.get(i2) ? 1 << i2 : 0;
        }
        return value;
    }

    private void incrementSsid(SparseIntArray sia, int element) {
        increment(sia, Math.min(element, 20));
    }

    private void incrementBssid(SparseIntArray sia, int element) {
        increment(sia, Math.min(element, 50));
    }

    private void incrementTotalScanResults(SparseIntArray sia, int element) {
        increment(sia, Math.min(element, MAX_TOTAL_SCAN_RESULTS_BUCKET));
    }

    private void incrementTotalScanSsids(SparseIntArray sia, int element) {
        increment(sia, Math.min(element, 100));
    }

    private void incrementTotalPasspointAps(SparseIntArray sia, int element) {
        increment(sia, Math.min(element, 50));
    }

    private void incrementTotalUniquePasspointEss(SparseIntArray sia, int element) {
        increment(sia, Math.min(element, 20));
    }

    private void incrementPasspointPerUniqueEss(SparseIntArray sia, int element) {
        increment(sia, Math.min(element, 50));
    }

    private void increment80211mcAps(SparseIntArray sia, int element) {
        increment(sia, Math.min(element, 20));
    }

    private void increment(SparseIntArray sia, int element) {
        sia.put(element, sia.get(element) + 1);
    }
}
