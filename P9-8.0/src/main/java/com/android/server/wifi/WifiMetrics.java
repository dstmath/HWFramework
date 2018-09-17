package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifiStaIface.StaIfaceCapabilityMask;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.ReasonCode;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.StatusCode;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.util.SparseIntArray;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.NetworkDetail.HSRelease;
import com.android.server.wifi.nano.WifiMetricsProto.AlertReasonCount;
import com.android.server.wifi.nano.WifiMetricsProto.RssiPollCount;
import com.android.server.wifi.nano.WifiMetricsProto.SoftApReturnCodeCount;
import com.android.server.wifi.nano.WifiMetricsProto.StaEvent;
import com.android.server.wifi.nano.WifiMetricsProto.StaEvent.ConfigInfo;
import com.android.server.wifi.nano.WifiMetricsProto.WifiLog;
import com.android.server.wifi.nano.WifiMetricsProto.WifiLog.ScanReturnEntry;
import com.android.server.wifi.nano.WifiMetricsProto.WifiLog.WifiSystemStateEntry;
import com.android.server.wifi.nano.WifiMetricsProto.WifiScoreCount;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.StringUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class WifiMetrics {
    private static final /* synthetic */ int[] -android-net-wifi-SupplicantStateSwitchesValues = null;
    public static final String CLEAN_DUMP_ARG = "clean";
    private static final boolean DBG = false;
    private static final int MAX_CONNECTION_EVENTS = 256;
    public static final int MAX_RSSI_DELTA = 127;
    private static final int MAX_RSSI_POLL = 0;
    public static final int MAX_STA_EVENTS = 512;
    private static final int MAX_WIFI_SCORE = 60;
    public static final int MIN_RSSI_DELTA = -127;
    private static final int MIN_RSSI_POLL = -127;
    private static final int MIN_WIFI_SCORE = 0;
    public static final String PROTO_DUMP_ARG = "wifiMetricsProto";
    private static final int SCREEN_OFF = 0;
    private static final int SCREEN_ON = 1;
    private static final String TAG = "WifiMetrics";
    public static final long TIMEOUT_RSSI_DELTA_MILLIS = 3000;
    private Clock mClock;
    private final List<ConnectionEvent> mConnectionEventList = new ArrayList();
    private ConnectionEvent mCurrentConnectionEvent;
    private Handler mHandler;
    private int mLastPollFreq = -1;
    private int mLastPollLinkSpeed = -1;
    private int mLastPollRssi = -127;
    private final Object mLock = new Object();
    private long mRecordStartTimeSec;
    private final SparseIntArray mRssiDeltaCounts = new SparseIntArray();
    private final SparseIntArray mRssiPollCounts = new SparseIntArray();
    private int mScanResultRssi = 0;
    private long mScanResultRssiTimestampMillis = -1;
    private final SparseIntArray mScanReturnEntries = new SparseIntArray();
    private boolean mScreenOn;
    private final SparseIntArray mSoftApManagerReturnCodeCounts = new SparseIntArray();
    private LinkedList<StaEvent> mStaEventList = new LinkedList();
    private int mSupplicantStateChangeBitmask = 0;
    private final SparseIntArray mWifiAlertReasonCounts = new SparseIntArray();
    private final WifiLog mWifiLogProto = new WifiLog();
    private final SparseIntArray mWifiScoreCounts = new SparseIntArray();
    private int mWifiState;
    private final SparseIntArray mWifiSystemStateEntries = new SparseIntArray();

    class ConnectionEvent {
        public static final int FAILURE_ASSOCIATION_REJECTION = 2;
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
        private String mConfigBssid;
        private String mConfigSsid;
        com.android.server.wifi.nano.WifiMetricsProto.ConnectionEvent mConnectionEvent;
        private long mRealEndTime;
        private long mRealStartTime;
        RouterFingerPrint mRouterFingerPrint;
        private boolean mScreenOn;
        private int mWifiState;

        /* synthetic */ ConnectionEvent(WifiMetrics this$0, ConnectionEvent -this1) {
            this();
        }

        private ConnectionEvent() {
            this.mConnectionEvent = new com.android.server.wifi.nano.WifiMetricsProto.ConnectionEvent();
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
            StringBuilder sb = new StringBuilder();
            sb.append("startTime=");
            Calendar c = Calendar.getInstance();
            synchronized (WifiMetrics.this.mLock) {
                String str;
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
                sb.append(StringUtil.safeDisplayBssid(this.mConfigBssid));
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
                }
                sb.append("UNKNOWN");
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
        private com.android.server.wifi.nano.WifiMetricsProto.RouterFingerPrint mRouterFingerPrintProto = new com.android.server.wifi.nano.WifiMetricsProto.RouterFingerPrint();

        RouterFingerPrint() {
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            synchronized (WifiMetrics.this.mLock) {
                sb.append("mConnectionEvent.roamType=").append(this.mRouterFingerPrintProto.roamType);
                sb.append(", mChannelInfo=").append(this.mRouterFingerPrintProto.channelInfo);
                sb.append(", mDtim=").append(this.mRouterFingerPrintProto.dtim);
                sb.append(", mAuthentication=").append(this.mRouterFingerPrintProto.authentication);
                sb.append(", mHidden=").append(this.mRouterFingerPrintProto.hidden);
                sb.append(", mRouterTechnology=").append(this.mRouterFingerPrintProto.routerTechnology);
                sb.append(", mSupportsIpv6=").append(this.mRouterFingerPrintProto.supportsIpv6);
            }
            return sb.toString();
        }

        public void updateFromWifiConfiguration(WifiConfiguration config) {
            synchronized (WifiMetrics.this.mLock) {
                if (config != null) {
                    this.mRouterFingerPrintProto.hidden = config.hiddenSSID;
                    if (config.dtimInterval > 0) {
                        this.mRouterFingerPrintProto.dtim = config.dtimInterval;
                    }
                    WifiMetrics.this.mCurrentConnectionEvent.mConfigSsid = config.SSID;
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
                }
            }
        }
    }

    private static /* synthetic */ int[] -getandroid-net-wifi-SupplicantStateSwitchesValues() {
        if (-android-net-wifi-SupplicantStateSwitchesValues != null) {
            return -android-net-wifi-SupplicantStateSwitchesValues;
        }
        int[] iArr = new int[SupplicantState.values().length];
        try {
            iArr[SupplicantState.ASSOCIATED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SupplicantState.ASSOCIATING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SupplicantState.AUTHENTICATING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SupplicantState.COMPLETED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SupplicantState.DISCONNECTED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SupplicantState.DORMANT.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SupplicantState.FOUR_WAY_HANDSHAKE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[SupplicantState.GROUP_HANDSHAKE.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[SupplicantState.INACTIVE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[SupplicantState.INTERFACE_DISABLED.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[SupplicantState.INVALID.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[SupplicantState.SCANNING.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[SupplicantState.UNINITIALIZED.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        -android-net-wifi-SupplicantStateSwitchesValues = iArr;
        return iArr;
    }

    public WifiMetrics(Clock clock, Looper looper) {
        this.mClock = clock;
        this.mCurrentConnectionEvent = null;
        this.mScreenOn = true;
        this.mWifiState = 1;
        this.mRecordStartTimeSec = this.mClock.getElapsedSinceBootMillis() / 1000;
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                synchronized (WifiMetrics.this.mLock) {
                    WifiMetrics.this.processMessage(msg);
                }
            }
        };
    }

    public void startConnectionEvent(WifiConfiguration config, String targetBSSID, int roamType) {
        synchronized (this.mLock) {
            if (this.mCurrentConnectionEvent != null) {
                if (this.mCurrentConnectionEvent.mConfigSsid == null || this.mCurrentConnectionEvent.mConfigBssid == null || config == null || !this.mCurrentConnectionEvent.mConfigSsid.equals(config.SSID) || !(this.mCurrentConnectionEvent.mConfigBssid.equals("any") || this.mCurrentConnectionEvent.mConfigBssid.equals(targetBSSID))) {
                    endConnectionEvent(7, 1);
                } else {
                    this.mCurrentConnectionEvent.mConfigBssid = targetBSSID;
                    endConnectionEvent(8, 1);
                }
            }
            while (this.mConnectionEventList.size() >= 256) {
                this.mConnectionEventList.remove(0);
            }
            this.mCurrentConnectionEvent = new ConnectionEvent(this, null);
            this.mCurrentConnectionEvent.mConnectionEvent.startTimeMillis = this.mClock.getWallClockMillis();
            this.mCurrentConnectionEvent.mConfigBssid = targetBSSID;
            this.mCurrentConnectionEvent.mConnectionEvent.roamType = roamType;
            this.mCurrentConnectionEvent.mRouterFingerPrint.updateFromWifiConfiguration(config);
            this.mCurrentConnectionEvent.mConfigBssid = "any";
            this.mCurrentConnectionEvent.mRealStartTime = this.mClock.getElapsedSinceBootMillis();
            this.mCurrentConnectionEvent.mWifiState = this.mWifiState;
            this.mCurrentConnectionEvent.mScreenOn = this.mScreenOn;
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
                if (!(networkDetail == null || scanResult == null || this.mCurrentConnectionEvent.mConfigSsid == null || !this.mCurrentConnectionEvent.mConfigSsid.equals("\"" + networkDetail.getSSID() + "\""))) {
                    updateMetricsFromNetworkDetail(networkDetail);
                    updateMetricsFromScanResult(scanResult);
                }
            }
        }
    }

    public void endConnectionEvent(int level2FailureCode, int connectivityFailureCode) {
        int i = 1;
        synchronized (this.mLock) {
            if (this.mCurrentConnectionEvent != null) {
                boolean result = level2FailureCode == 1 ? connectivityFailureCode == 1 : false;
                com.android.server.wifi.nano.WifiMetricsProto.ConnectionEvent connectionEvent = this.mCurrentConnectionEvent.mConnectionEvent;
                if (!result) {
                    i = 0;
                }
                connectionEvent.connectionResult = i;
                this.mCurrentConnectionEvent.mRealEndTime = this.mClock.getElapsedSinceBootMillis();
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

    private void updateMetricsFromScanResult(ScanResult scanResult) {
        this.mCurrentConnectionEvent.mConnectionEvent.signalStrength = scanResult.level;
        this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 1;
        this.mCurrentConnectionEvent.mConfigBssid = scanResult.BSSID;
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

    void setIsLocationEnabled(boolean enabled) {
        synchronized (this.mLock) {
            this.mWifiLogProto.isLocationEnabled = enabled;
        }
    }

    void setIsScanningAlwaysEnabled(boolean enabled) {
        synchronized (this.mLock) {
            this.mWifiLogProto.isScanningAlwaysEnabled = enabled;
        }
    }

    public void incrementNonEmptyScanResultCount() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numNonEmptyScanResults++;
        }
    }

    public void incrementEmptyScanResultCount() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numEmptyScanResults++;
        }
    }

    public void incrementBackgroundScanCount() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numBackgroundScans++;
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
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numOneshotScans++;
        }
        incrementWifiSystemScanStateCount(this.mWifiState, this.mScreenOn);
    }

    public int getOneshotScanCount() {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiLogProto.numOneshotScans;
        }
        return i;
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
            int index = (state * 2) + (screenOn ? 1 : 0);
            this.mWifiSystemStateEntries.put(index, this.mWifiSystemStateEntries.get(index) + 1);
        }
    }

    public int getSystemStateCount(int state, boolean screenOn) {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiSystemStateEntries.get((state * 2) + (screenOn ? 1 : 0));
        }
        return i;
    }

    public void incrementNumLastResortWatchdogTriggers() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggers++;
        }
    }

    public void addCountToNumLastResortWatchdogBadAssociationNetworksTotal(int count) {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogBadAssociationNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogBadAuthenticationNetworksTotal(int count) {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogBadAuthenticationNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogBadDhcpNetworksTotal(int count) {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogBadDhcpNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogBadOtherNetworksTotal(int count) {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogBadOtherNetworksTotal += count;
        }
    }

    public void addCountToNumLastResortWatchdogAvailableNetworksTotal(int count) {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogAvailableNetworksTotal += count;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadAssociation() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadAssociation++;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadAuthentication() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadAuthentication++;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadDhcp() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadDhcp++;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadOther() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadOther++;
        }
    }

    public void incrementNumConnectivityWatchdogPnoGood() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogPnoGood++;
        }
    }

    public void incrementNumConnectivityWatchdogPnoBad() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogPnoBad++;
        }
    }

    public void incrementNumConnectivityWatchdogBackgroundGood() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogBackgroundGood++;
        }
    }

    public void incrementNumConnectivityWatchdogBackgroundBad() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogBackgroundBad++;
        }
    }

    public void handlePollResult(WifiInfo wifiInfo) {
        this.mLastPollRssi = wifiInfo.getRssi();
        this.mLastPollLinkSpeed = wifiInfo.getLinkSpeed();
        this.mLastPollFreq = wifiInfo.getFrequency();
        incrementRssiPollRssiCount(this.mLastPollRssi);
    }

    public void incrementRssiPollRssiCount(int rssi) {
        if (rssi >= -127 && rssi <= 0) {
            synchronized (this.mLock) {
                this.mRssiPollCounts.put(rssi, this.mRssiPollCounts.get(rssi) + 1);
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
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogSuccesses++;
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
                        if (networkDetail.getHSRelease() == HSRelease.R1) {
                            hotspot2r1Networks++;
                        } else if (networkDetail.getHSRelease() == HSRelease.R2) {
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
                WifiLog wifiLog = this.mWifiLogProto;
                wifiLog.numTotalScanResults += totalResults;
                wifiLog = this.mWifiLogProto;
                wifiLog.numOpenNetworkScanResults += openNetworks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numPersonalNetworkScanResults += personalNetworks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numEnterpriseNetworkScanResults += enterpriseNetworks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numHiddenNetworkScanResults += hiddenNetworks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numHotspot2R1NetworkScanResults += hotspot2r1Networks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numHotspot2R2NetworkScanResults += hotspot2r2Networks;
                wifiLog = this.mWifiLogProto;
                wifiLog.numScans++;
            }
        }
    }

    public void incrementWifiScoreCount(int score) {
        if (score >= 0 && score <= 60) {
            synchronized (this.mLock) {
                this.mWifiScoreCounts.put(score, this.mWifiScoreCounts.get(score) + 1);
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0029, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void incrementSoftApStartResult(boolean result, int failureCode) {
        synchronized (this.mLock) {
            if (result) {
                this.mSoftApManagerReturnCodeCounts.put(1, this.mSoftApManagerReturnCodeCounts.get(1) + 1);
            } else if (failureCode == 1) {
                this.mSoftApManagerReturnCodeCounts.put(3, this.mSoftApManagerReturnCodeCounts.get(3) + 1);
            } else {
                this.mSoftApManagerReturnCodeCounts.put(2, this.mSoftApManagerReturnCodeCounts.get(2) + 1);
            }
        }
    }

    public void incrementNumHalCrashes() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numHalCrashes++;
        }
    }

    public void incrementNumWificondCrashes() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numWificondCrashes++;
        }
    }

    public void incrementNumWifiOnFailureDueToHal() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numWifiOnFailureDueToHal++;
        }
    }

    public void incrementNumWifiOnFailureDueToWificond() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numWifiOnFailureDueToWificond++;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            int i;
            if (args != null) {
                if (args.length > 0 && PROTO_DUMP_ARG.equals(args[0])) {
                    consolidateProto(true);
                    for (ConnectionEvent event : this.mConnectionEventList) {
                        if (this.mCurrentConnectionEvent != event) {
                            event.mConnectionEvent.automaticBugReportTaken = true;
                        }
                    }
                    String metricsProtoDump = Base64.encodeToString(WifiLog.toByteArray(this.mWifiLogProto), 0);
                    if (args.length <= 1 || !CLEAN_DUMP_ARG.equals(args[1])) {
                        pw.println("WifiMetrics:");
                        pw.println(metricsProtoDump);
                        pw.println("EndWifiMetrics");
                    } else {
                        pw.print(metricsProtoDump);
                    }
                    clear();
                }
            }
            pw.println("WifiMetrics:");
            pw.println("mConnectionEvents:");
            for (ConnectionEvent event2 : this.mConnectionEventList) {
                String eventLine = event2.toString();
                if (event2 == this.mCurrentConnectionEvent) {
                    eventLine = eventLine + "CURRENTLY OPEN EVENT";
                }
                pw.println(eventLine);
            }
            pw.println("mWifiLogProto.numSavedNetworks=" + this.mWifiLogProto.numSavedNetworks);
            pw.println("mWifiLogProto.numOpenNetworks=" + this.mWifiLogProto.numOpenNetworks);
            pw.println("mWifiLogProto.numPersonalNetworks=" + this.mWifiLogProto.numPersonalNetworks);
            pw.println("mWifiLogProto.numEnterpriseNetworks=" + this.mWifiLogProto.numEnterpriseNetworks);
            pw.println("mWifiLogProto.numHiddenNetworks=" + this.mWifiLogProto.numHiddenNetworks);
            pw.println("mWifiLogProto.numPasspointNetworks=" + this.mWifiLogProto.numPasspointNetworks);
            pw.println("mWifiLogProto.isLocationEnabled=" + this.mWifiLogProto.isLocationEnabled);
            pw.println("mWifiLogProto.isScanningAlwaysEnabled=" + this.mWifiLogProto.isScanningAlwaysEnabled);
            pw.println("mWifiLogProto.numNetworksAddedByUser=" + this.mWifiLogProto.numNetworksAddedByUser);
            pw.println("mWifiLogProto.numNetworksAddedByApps=" + this.mWifiLogProto.numNetworksAddedByApps);
            pw.println("mWifiLogProto.numNonEmptyScanResults=" + this.mWifiLogProto.numNonEmptyScanResults);
            pw.println("mWifiLogProto.numEmptyScanResults=" + this.mWifiLogProto.numEmptyScanResults);
            pw.println("mWifiLogProto.numOneshotScans=" + this.mWifiLogProto.numOneshotScans);
            pw.println("mWifiLogProto.numBackgroundScans=" + this.mWifiLogProto.numBackgroundScans);
            pw.println("mScanReturnEntries:");
            pw.println("  SCAN_UNKNOWN: " + getScanReturnEntry(0));
            pw.println("  SCAN_SUCCESS: " + getScanReturnEntry(1));
            pw.println("  SCAN_FAILURE_INTERRUPTED: " + getScanReturnEntry(2));
            pw.println("  SCAN_FAILURE_INVALID_CONFIGURATION: " + getScanReturnEntry(3));
            pw.println("  FAILURE_WIFI_DISABLED: " + getScanReturnEntry(4));
            pw.println("mSystemStateEntries: <state><screenOn> : <scansInitiated>");
            pw.println("  WIFI_UNKNOWN       ON: " + getSystemStateCount(0, true));
            pw.println("  WIFI_DISABLED      ON: " + getSystemStateCount(1, true));
            pw.println("  WIFI_DISCONNECTED  ON: " + getSystemStateCount(2, true));
            pw.println("  WIFI_ASSOCIATED    ON: " + getSystemStateCount(3, true));
            pw.println("  WIFI_UNKNOWN      OFF: " + getSystemStateCount(0, false));
            pw.println("  WIFI_DISABLED     OFF: " + getSystemStateCount(1, false));
            pw.println("  WIFI_DISCONNECTED OFF: " + getSystemStateCount(2, false));
            pw.println("  WIFI_ASSOCIATED   OFF: " + getSystemStateCount(3, false));
            pw.println("mWifiLogProto.numConnectivityWatchdogPnoGood=" + this.mWifiLogProto.numConnectivityWatchdogPnoGood);
            pw.println("mWifiLogProto.numConnectivityWatchdogPnoBad=" + this.mWifiLogProto.numConnectivityWatchdogPnoBad);
            pw.println("mWifiLogProto.numConnectivityWatchdogBackgroundGood=" + this.mWifiLogProto.numConnectivityWatchdogBackgroundGood);
            pw.println("mWifiLogProto.numConnectivityWatchdogBackgroundBad=" + this.mWifiLogProto.numConnectivityWatchdogBackgroundBad);
            pw.println("mWifiLogProto.numLastResortWatchdogTriggers=" + this.mWifiLogProto.numLastResortWatchdogTriggers);
            pw.println("mWifiLogProto.numLastResortWatchdogBadAssociationNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadAssociationNetworksTotal);
            pw.println("mWifiLogProto.numLastResortWatchdogBadAuthenticationNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadAuthenticationNetworksTotal);
            pw.println("mWifiLogProto.numLastResortWatchdogBadDhcpNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadDhcpNetworksTotal);
            pw.println("mWifiLogProto.numLastResortWatchdogBadOtherNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogBadOtherNetworksTotal);
            pw.println("mWifiLogProto.numLastResortWatchdogAvailableNetworksTotal=" + this.mWifiLogProto.numLastResortWatchdogAvailableNetworksTotal);
            pw.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadAssociation=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadAssociation);
            pw.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadAuthentication=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadAuthentication);
            pw.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadDhcp=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadDhcp);
            pw.println("mWifiLogProto.numLastResortWatchdogTriggersWithBadOther=" + this.mWifiLogProto.numLastResortWatchdogTriggersWithBadOther);
            pw.println("mWifiLogProto.numLastResortWatchdogSuccesses=" + this.mWifiLogProto.numLastResortWatchdogSuccesses);
            pw.println("mWifiLogProto.recordDurationSec=" + ((this.mClock.getElapsedSinceBootMillis() / 1000) - this.mRecordStartTimeSec));
            pw.println("mWifiLogProto.rssiPollRssiCount: Printing counts for [-127, 0]");
            StringBuilder sb = new StringBuilder();
            for (i = -127; i <= 0; i++) {
                sb.append(this.mRssiPollCounts.get(i)).append(" ");
            }
            pw.println("  " + sb.toString());
            pw.println("mWifiLogProto.rssiPollDeltaCount: Printing counts for [-127, 127]");
            sb.setLength(0);
            for (i = -127; i <= 127; i++) {
                sb.append(this.mRssiDeltaCounts.get(i)).append(" ");
            }
            pw.println("  " + sb.toString());
            pw.print("mWifiLogProto.alertReasonCounts=");
            sb.setLength(0);
            for (i = 0; i <= 64; i++) {
                int count = this.mWifiAlertReasonCounts.get(i);
                if (count > 0) {
                    sb.append("(").append(i).append(",").append(count).append("),");
                }
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 1);
                pw.println(sb.toString());
            } else {
                pw.println("()");
            }
            pw.println("mWifiLogProto.numTotalScanResults=" + this.mWifiLogProto.numTotalScanResults);
            pw.println("mWifiLogProto.numOpenNetworkScanResults=" + this.mWifiLogProto.numOpenNetworkScanResults);
            pw.println("mWifiLogProto.numPersonalNetworkScanResults=" + this.mWifiLogProto.numPersonalNetworkScanResults);
            pw.println("mWifiLogProto.numEnterpriseNetworkScanResults=" + this.mWifiLogProto.numEnterpriseNetworkScanResults);
            pw.println("mWifiLogProto.numHiddenNetworkScanResults=" + this.mWifiLogProto.numHiddenNetworkScanResults);
            pw.println("mWifiLogProto.numHotspot2R1NetworkScanResults=" + this.mWifiLogProto.numHotspot2R1NetworkScanResults);
            pw.println("mWifiLogProto.numHotspot2R2NetworkScanResults=" + this.mWifiLogProto.numHotspot2R2NetworkScanResults);
            pw.println("mWifiLogProto.numScans=" + this.mWifiLogProto.numScans);
            pw.println("mWifiLogProto.WifiScoreCount: [0, 60]");
            for (i = 0; i <= 60; i++) {
                pw.print(this.mWifiScoreCounts.get(i) + " ");
            }
            pw.println();
            pw.println("mWifiLogProto.SoftApManagerReturnCodeCounts:");
            pw.println("  SUCCESS: " + this.mSoftApManagerReturnCodeCounts.get(1));
            pw.println("  FAILED_GENERAL_ERROR: " + this.mSoftApManagerReturnCodeCounts.get(2));
            pw.println("  FAILED_NO_CHANNEL: " + this.mSoftApManagerReturnCodeCounts.get(3));
            pw.print("\n");
            pw.println("mWifiLogProto.numHalCrashes=" + this.mWifiLogProto.numHalCrashes);
            pw.println("mWifiLogProto.numWificondCrashes=" + this.mWifiLogProto.numWificondCrashes);
            pw.println("mWifiLogProto.numWifiOnFailureDueToHal=" + this.mWifiLogProto.numWifiOnFailureDueToHal);
            pw.println("mWifiLogProto.numWifiOnFailureDueToWificond=" + this.mWifiLogProto.numWifiOnFailureDueToWificond);
            pw.println("StaEventList:");
            for (StaEvent event3 : this.mStaEventList) {
                pw.println(staEventToString(event3));
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
                WifiLog wifiLog;
                if (config.allowedKeyManagement.get(0)) {
                    wifiLog = this.mWifiLogProto;
                    wifiLog.numOpenNetworks++;
                } else if (config.isEnterprise()) {
                    wifiLog = this.mWifiLogProto;
                    wifiLog.numEnterpriseNetworks++;
                } else {
                    wifiLog = this.mWifiLogProto;
                    wifiLog.numPersonalNetworks++;
                }
                if (config.selfAdded) {
                    wifiLog = this.mWifiLogProto;
                    wifiLog.numNetworksAddedByUser++;
                } else {
                    wifiLog = this.mWifiLogProto;
                    wifiLog.numNetworksAddedByApps++;
                }
                if (config.hiddenSSID) {
                    wifiLog = this.mWifiLogProto;
                    wifiLog.numHiddenNetworks++;
                }
                if (config.isPasspoint()) {
                    wifiLog = this.mWifiLogProto;
                    wifiLog.numPasspointNetworks++;
                }
            }
        }
    }

    private void consolidateProto(boolean incremental) {
        List<com.android.server.wifi.nano.WifiMetricsProto.ConnectionEvent> events = new ArrayList();
        List<RssiPollCount> rssis = new ArrayList();
        List<RssiPollCount> rssiDeltas = new ArrayList();
        List<AlertReasonCount> alertReasons = new ArrayList();
        List<WifiScoreCount> scores = new ArrayList();
        synchronized (this.mLock) {
            int i;
            RssiPollCount keyVal;
            for (ConnectionEvent event : this.mConnectionEventList) {
                if (!(incremental && (this.mCurrentConnectionEvent == event || (event.mConnectionEvent.automaticBugReportTaken ^ 1) == 0))) {
                    events.add(event.mConnectionEvent);
                    if (incremental) {
                        event.mConnectionEvent.automaticBugReportTaken = true;
                    }
                }
            }
            if (events.size() > 0) {
                this.mWifiLogProto.connectionEvent = (com.android.server.wifi.nano.WifiMetricsProto.ConnectionEvent[]) events.toArray(this.mWifiLogProto.connectionEvent);
            }
            this.mWifiLogProto.scanReturnEntries = new ScanReturnEntry[this.mScanReturnEntries.size()];
            for (i = 0; i < this.mScanReturnEntries.size(); i++) {
                this.mWifiLogProto.scanReturnEntries[i] = new ScanReturnEntry();
                this.mWifiLogProto.scanReturnEntries[i].scanReturnCode = this.mScanReturnEntries.keyAt(i);
                this.mWifiLogProto.scanReturnEntries[i].scanResultsCount = this.mScanReturnEntries.valueAt(i);
            }
            this.mWifiLogProto.wifiSystemStateEntries = new WifiSystemStateEntry[this.mWifiSystemStateEntries.size()];
            for (i = 0; i < this.mWifiSystemStateEntries.size(); i++) {
                this.mWifiLogProto.wifiSystemStateEntries[i] = new WifiSystemStateEntry();
                this.mWifiLogProto.wifiSystemStateEntries[i].wifiState = this.mWifiSystemStateEntries.keyAt(i) / 2;
                this.mWifiLogProto.wifiSystemStateEntries[i].wifiStateCount = this.mWifiSystemStateEntries.valueAt(i);
                this.mWifiLogProto.wifiSystemStateEntries[i].isScreenOn = this.mWifiSystemStateEntries.keyAt(i) % 2 > 0;
            }
            this.mWifiLogProto.recordDurationSec = (int) ((this.mClock.getElapsedSinceBootMillis() / 1000) - this.mRecordStartTimeSec);
            for (i = 0; i < this.mRssiPollCounts.size(); i++) {
                keyVal = new RssiPollCount();
                keyVal.rssi = this.mRssiPollCounts.keyAt(i);
                keyVal.count = this.mRssiPollCounts.valueAt(i);
                rssis.add(keyVal);
            }
            this.mWifiLogProto.rssiPollRssiCount = (RssiPollCount[]) rssis.toArray(this.mWifiLogProto.rssiPollRssiCount);
            for (i = 0; i < this.mRssiDeltaCounts.size(); i++) {
                keyVal = new RssiPollCount();
                keyVal.rssi = this.mRssiDeltaCounts.keyAt(i);
                keyVal.count = this.mRssiDeltaCounts.valueAt(i);
                rssiDeltas.add(keyVal);
            }
            this.mWifiLogProto.rssiPollDeltaCount = (RssiPollCount[]) rssiDeltas.toArray(this.mWifiLogProto.rssiPollDeltaCount);
            for (i = 0; i < this.mWifiAlertReasonCounts.size(); i++) {
                AlertReasonCount keyVal2 = new AlertReasonCount();
                keyVal2.reason = this.mWifiAlertReasonCounts.keyAt(i);
                keyVal2.count = this.mWifiAlertReasonCounts.valueAt(i);
                alertReasons.add(keyVal2);
            }
            this.mWifiLogProto.alertReasonCount = (AlertReasonCount[]) alertReasons.toArray(this.mWifiLogProto.alertReasonCount);
            for (int score = 0; score < this.mWifiScoreCounts.size(); score++) {
                WifiScoreCount keyVal3 = new WifiScoreCount();
                keyVal3.score = this.mWifiScoreCounts.keyAt(score);
                keyVal3.count = this.mWifiScoreCounts.valueAt(score);
                scores.add(keyVal3);
            }
            this.mWifiLogProto.wifiScoreCount = (WifiScoreCount[]) scores.toArray(this.mWifiLogProto.wifiScoreCount);
            int codeCounts = this.mSoftApManagerReturnCodeCounts.size();
            this.mWifiLogProto.softApReturnCode = new SoftApReturnCodeCount[codeCounts];
            for (int sapCode = 0; sapCode < codeCounts; sapCode++) {
                this.mWifiLogProto.softApReturnCode[sapCode] = new SoftApReturnCodeCount();
                this.mWifiLogProto.softApReturnCode[sapCode].startResult = this.mSoftApManagerReturnCodeCounts.keyAt(sapCode);
                this.mWifiLogProto.softApReturnCode[sapCode].count = this.mSoftApManagerReturnCodeCounts.valueAt(sapCode);
            }
            this.mWifiLogProto.staEventList = (StaEvent[]) this.mStaEventList.toArray(this.mWifiLogProto.staEventList);
        }
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
            this.mRssiPollCounts.clear();
            this.mRssiDeltaCounts.clear();
            this.mWifiAlertReasonCounts.clear();
            this.mWifiScoreCounts.clear();
            this.mWifiLogProto.clear();
            this.mScanResultRssiTimestampMillis = -1;
            this.mSoftApManagerReturnCodeCounts.clear();
            this.mStaEventList.clear();
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
        }
    }

    private void processMessage(Message msg) {
        boolean z = true;
        boolean z2 = false;
        StaEvent event = new StaEvent();
        boolean logEvent = true;
        switch (msg.what) {
            case 131213:
                event.type = 10;
                break;
            case 131219:
                event.type = 6;
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                event.type = 3;
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                event.type = 4;
                event.reason = msg.arg2;
                if (msg.arg1 != 0) {
                    z2 = true;
                }
                event.localGen = z2;
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                logEvent = false;
                this.mSupplicantStateChangeBitmask |= supplicantStateToBit(msg.obj.state);
                break;
            case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                event.type = 2;
                switch (msg.arg2) {
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
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                event.type = 1;
                if (msg.arg1 <= 0) {
                    z = false;
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
                StaEvent event = new StaEvent();
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

    private void addStaEvent(StaEvent staEvent) {
        staEvent.startTimeMillis = this.mClock.getElapsedSinceBootMillis();
        staEvent.lastRssi = this.mLastPollRssi;
        staEvent.lastFreq = this.mLastPollFreq;
        staEvent.lastLinkSpeed = this.mLastPollLinkSpeed;
        staEvent.supplicantStateChangesBitmask = this.mSupplicantStateChangeBitmask;
        this.mSupplicantStateChangeBitmask = 0;
        this.mLastPollRssi = -127;
        this.mLastPollFreq = -1;
        this.mLastPollLinkSpeed = -1;
        this.mStaEventList.add(staEvent);
        if (this.mStaEventList.size() > 512) {
            this.mStaEventList.remove();
        }
    }

    private ConfigInfo createConfigInfo(WifiConfiguration config) {
        if (config == null) {
            return null;
        }
        ConfigInfo info = new ConfigInfo();
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

    public static int supplicantStateToBit(SupplicantState state) {
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
            case 1:
                return 64;
            case 2:
                return 32;
            case 3:
                return 16;
            case 4:
                return 512;
            case 5:
                return 1;
            case 6:
                return StaIfaceCapabilityMask.TDLS;
            case 7:
                return 128;
            case 8:
                return 256;
            case 9:
                return 4;
            case 10:
                return 2;
            case 11:
                return 4096;
            case 12:
                return 8;
            case 13:
                return StaIfaceCapabilityMask.TDLS_OFFCHANNEL;
            default:
                Log.wtf(TAG, "Got unknown supplicant state: " + state.ordinal());
                return 0;
        }
    }

    private static String supplicantStateChangesBitmaskToString(int mask) {
        StringBuilder sb = new StringBuilder();
        sb.append("SUPPLICANT_STATE_CHANGE_EVENTS: {");
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
        if ((mask & StaIfaceCapabilityMask.TDLS) > 0) {
            sb.append(" DORMANT");
        }
        if ((mask & StaIfaceCapabilityMask.TDLS_OFFCHANNEL) > 0) {
            sb.append(" UNINITIALIZED");
        }
        if ((mask & 4096) > 0) {
            sb.append(" INVALID");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String staEventToString(StaEvent event) {
        if (event == null) {
            return "<NULL>";
        }
        StringBuilder sb = new StringBuilder();
        Long time = Long.valueOf(event.startTimeMillis);
        sb.append(String.format("%9d ", new Object[]{Long.valueOf(time.longValue())})).append(" ");
        switch (event.type) {
            case 1:
                sb.append("ASSOCIATION_REJECTION_EVENT:").append(" timedOut=").append(event.associationTimedOut).append(" status=").append(event.status).append(":").append(StatusCode.toString(event.status));
                break;
            case 2:
                sb.append("AUTHENTICATION_FAILURE_EVENT: reason=").append(event.authFailureReason).append(":").append(authFailureReasonToString(event.authFailureReason));
                break;
            case 3:
                sb.append("NETWORK_CONNECTION_EVENT:");
                break;
            case 4:
                sb.append("NETWORK_DISCONNECTION_EVENT:").append(" local_gen=").append(event.localGen).append(" reason=").append(event.reason).append(":").append(ReasonCode.toString(event.reason >= 0 ? event.reason : event.reason * -1));
                break;
            case 6:
                sb.append("CMD_ASSOCIATED_BSSID:");
                break;
            case 7:
                sb.append("CMD_IP_CONFIGURATION_SUCCESSFUL:");
                break;
            case 8:
                sb.append("CMD_IP_CONFIGURATION_LOST:");
                break;
            case 9:
                sb.append("CMD_IP_REACHABILITY_LOST:");
                break;
            case 10:
                sb.append("CMD_TARGET_BSSID:");
                break;
            case 11:
                sb.append("CMD_START_CONNECT:");
                break;
            case 12:
                sb.append("CMD_START_ROAM:");
                break;
            case 13:
                sb.append("CONNECT_NETWORK:");
                break;
            case 14:
                sb.append("NETWORK_AGENT_VALID_NETWORK:");
                break;
            case 15:
                sb.append("FRAMEWORK_DISCONNECT:").append(" reason=").append(frameworkDisconnectReasonToString(event.frameworkDisconnectReason));
                break;
            default:
                sb.append("UNKNOWN ").append(event.type).append(":");
                break;
        }
        if (event.lastRssi != -127) {
            sb.append(" lastRssi=").append(event.lastRssi);
        }
        if (event.lastFreq != -1) {
            sb.append(" lastFreq=").append(event.lastFreq);
        }
        if (event.lastLinkSpeed != -1) {
            sb.append(" lastLinkSpeed=").append(event.lastLinkSpeed);
        }
        if (event.supplicantStateChangesBitmask != 0) {
            sb.append("\n             ").append(supplicantStateChangesBitmaskToString(event.supplicantStateChangesBitmask));
        }
        if (event.configInfo != null) {
            sb.append("\n             ").append(configInfoToString(event.configInfo));
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

    private static String configInfoToString(ConfigInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append("ConfigInfo:").append(" allowed_key_management=").append(info.allowedKeyManagement).append(" allowed_protocols=").append(info.allowedProtocols).append(" allowed_auth_algorithms=").append(info.allowedAuthAlgorithms).append(" allowed_pairwise_ciphers=").append(info.allowedPairwiseCiphers).append(" allowed_group_ciphers=").append(info.allowedGroupCiphers).append(" hidden_ssid=").append(info.hiddenSsid).append(" is_passpoint=").append(info.isPasspoint).append(" is_ephemeral=").append(info.isEphemeral).append(" has_ever_connected=").append(info.hasEverConnected).append(" scan_rssi=").append(info.scanRssi).append(" scan_freq=").append(info.scanFreq);
        return sb.toString();
    }

    private static int bitSetToInt(BitSet bits) {
        int value = 0;
        int i = 0;
        while (i < (bits.length() < 31 ? bits.length() : 31)) {
            value += bits.get(i) ? 1 << i : 0;
            i++;
        }
        return value;
    }
}
