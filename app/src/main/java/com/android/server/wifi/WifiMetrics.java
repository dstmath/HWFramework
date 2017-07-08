package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Base64;
import android.util.SparseIntArray;
import com.android.server.wifi.WifiMetricsProto.WifiLog;
import com.android.server.wifi.WifiMetricsProto.WifiLog.ScanReturnEntry;
import com.android.server.wifi.WifiMetricsProto.WifiLog.WifiSystemStateEntry;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.google.protobuf.nano.Extension;
import com.google.protobuf.nano.MessageNano;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WifiMetrics {
    private static final boolean DBG = false;
    private static final int MAX_CONNECTION_EVENTS = 256;
    public static final String PROTO_DUMP_ARG = "wifiMetricsProto";
    private static final int SCREEN_OFF = 0;
    private static final int SCREEN_ON = 1;
    private static final String TAG = "WifiMetrics";
    private Clock mClock;
    private final List<ConnectionEvent> mConnectionEventList;
    private ConnectionEvent mCurrentConnectionEvent;
    private final Object mLock;
    private long mRecordStartTimeSec;
    private SparseIntArray mScanReturnEntries;
    private boolean mScreenOn;
    private final WifiLog mWifiLogProto;
    private int mWifiState;
    private SparseIntArray mWifiSystemStateEntries;

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
        com.android.server.wifi.WifiMetricsProto.ConnectionEvent mConnectionEvent;
        private long mRealEndTime;
        private long mRealStartTime;
        RouterFingerPrint mRouterFingerPrint;
        private boolean mScreenOn;
        private int mWifiState;

        private ConnectionEvent() {
            this.mConnectionEvent = new com.android.server.wifi.WifiMetricsProto.ConnectionEvent();
            this.mRealEndTime = 0;
            this.mRealStartTime = 0;
            this.mRouterFingerPrint = new RouterFingerPrint();
            this.mConnectionEvent.routerFingerprint = this.mRouterFingerPrint.mRouterFingerPrintProto;
            this.mConfigSsid = "<NULL>";
            this.mConfigBssid = "<NULL>";
            this.mWifiState = WifiMetrics.SCREEN_OFF;
            this.mScreenOn = WifiMetrics.DBG;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    Object[] objArr = new Object[FAILURE_NETWORK_DISCONNECTION];
                    objArr[WifiMetrics.SCREEN_OFF] = c;
                    objArr[FAILURE_NONE] = c;
                    objArr[FAILURE_ASSOCIATION_REJECTION] = c;
                    objArr[FAILURE_AUTHENTICATION_FAILURE] = c;
                    objArr[FAILURE_SSID_TEMP_DISABLED] = c;
                    objArr[FAILURE_CONNECT_NETWORK_FAILED] = c;
                    str = String.format("%tm-%td %tH:%tM:%tS.%tL", objArr);
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
                    case FAILURE_NONE /*1*/:
                        sb.append("ROAM_NONE");
                        break;
                    case FAILURE_ASSOCIATION_REJECTION /*2*/:
                        sb.append("ROAM_DBDC");
                        break;
                    case FAILURE_AUTHENTICATION_FAILURE /*3*/:
                        sb.append("ROAM_ENTERPRISE");
                        break;
                    case FAILURE_SSID_TEMP_DISABLED /*4*/:
                        sb.append("ROAM_USER_SELECTED");
                        break;
                    case FAILURE_CONNECT_NETWORK_FAILED /*5*/:
                        sb.append("ROAM_UNRELATED");
                        break;
                    default:
                        sb.append("ROAM_UNKNOWN");
                        break;
                }
            }
            return sb.toString();
        }
    }

    class RouterFingerPrint {
        private com.android.server.wifi.WifiMetricsProto.RouterFingerPrint mRouterFingerPrintProto;

        RouterFingerPrint() {
            this.mRouterFingerPrintProto = new com.android.server.wifi.WifiMetricsProto.RouterFingerPrint();
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
                    if (config.allowedKeyManagement != null && config.allowedKeyManagement.get(WifiMetrics.SCREEN_OFF)) {
                        WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = WifiMetrics.SCREEN_ON;
                    } else if (config.isEnterprise()) {
                        WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 3;
                    } else {
                        WifiMetrics.this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 2;
                    }
                    ScanResult candidate = config.getNetworkSelectionStatus().getCandidate();
                    if (candidate != null) {
                        WifiMetrics.this.updateMetricsFromScanResult(candidate);
                    }
                }
            }
        }
    }

    public WifiMetrics(Clock clock) {
        this.mLock = new Object();
        this.mClock = clock;
        this.mWifiLogProto = new WifiLog();
        this.mConnectionEventList = new ArrayList();
        this.mScanReturnEntries = new SparseIntArray();
        this.mWifiSystemStateEntries = new SparseIntArray();
        this.mCurrentConnectionEvent = null;
        this.mScreenOn = true;
        this.mWifiState = SCREEN_ON;
        this.mRecordStartTimeSec = this.mClock.elapsedRealtime() / 1000;
    }

    public void startConnectionEvent(WifiConfiguration config, String targetBSSID, int roamType) {
        synchronized (this.mLock) {
            if (this.mCurrentConnectionEvent != null) {
                if (this.mCurrentConnectionEvent.mConfigSsid == null || this.mCurrentConnectionEvent.mConfigBssid == null || config == null || !this.mCurrentConnectionEvent.mConfigSsid.equals(config.SSID) || !(this.mCurrentConnectionEvent.mConfigBssid.equals(WifiLastResortWatchdog.BSSID_ANY) || this.mCurrentConnectionEvent.mConfigBssid.equals(targetBSSID))) {
                    endConnectionEvent(7, SCREEN_ON);
                } else {
                    this.mCurrentConnectionEvent.mConfigBssid = targetBSSID;
                    endConnectionEvent(8, SCREEN_ON);
                }
            }
            while (this.mConnectionEventList.size() >= MAX_CONNECTION_EVENTS) {
                this.mConnectionEventList.remove(SCREEN_OFF);
            }
            this.mCurrentConnectionEvent = new ConnectionEvent();
            this.mCurrentConnectionEvent.mConnectionEvent.startTimeMillis = this.mClock.currentTimeMillis();
            this.mCurrentConnectionEvent.mConfigBssid = targetBSSID;
            this.mCurrentConnectionEvent.mConnectionEvent.roamType = roamType;
            this.mCurrentConnectionEvent.mRouterFingerPrint.updateFromWifiConfiguration(config);
            this.mCurrentConnectionEvent.mConfigBssid = WifiLastResortWatchdog.BSSID_ANY;
            this.mCurrentConnectionEvent.mRealStartTime = this.mClock.elapsedRealtime();
            this.mCurrentConnectionEvent.mWifiState = this.mWifiState;
            this.mCurrentConnectionEvent.mScreenOn = this.mScreenOn;
            this.mConnectionEventList.add(this.mCurrentConnectionEvent);
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
        int i = SCREEN_ON;
        synchronized (this.mLock) {
            if (this.mCurrentConnectionEvent != null) {
                boolean result = level2FailureCode == SCREEN_ON ? connectivityFailureCode == SCREEN_ON ? true : DBG : DBG;
                com.android.server.wifi.WifiMetricsProto.ConnectionEvent connectionEvent = this.mCurrentConnectionEvent.mConnectionEvent;
                if (!result) {
                    i = SCREEN_OFF;
                }
                connectionEvent.connectionResult = i;
                this.mCurrentConnectionEvent.mRealEndTime = this.mClock.elapsedRealtime();
                this.mCurrentConnectionEvent.mConnectionEvent.durationTakenToConnectMillis = (int) (this.mCurrentConnectionEvent.mRealEndTime - this.mCurrentConnectionEvent.mRealStartTime);
                this.mCurrentConnectionEvent.mConnectionEvent.level2FailureCode = level2FailureCode;
                this.mCurrentConnectionEvent.mConnectionEvent.connectivityLevelFailureCode = connectivityFailureCode;
                this.mCurrentConnectionEvent = null;
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
            case SCREEN_OFF /*0*/:
                connectionWifiMode = SCREEN_OFF;
                break;
            case SCREEN_ON /*1*/:
                connectionWifiMode = SCREEN_ON;
                break;
            case Extension.TYPE_FLOAT /*2*/:
                connectionWifiMode = 2;
                break;
            case Extension.TYPE_INT64 /*3*/:
                connectionWifiMode = 3;
                break;
            case Extension.TYPE_UINT64 /*4*/:
                connectionWifiMode = 4;
                break;
            case Extension.TYPE_INT32 /*5*/:
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
        this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = SCREEN_ON;
        this.mCurrentConnectionEvent.mConfigBssid = scanResult.BSSID;
        if (scanResult.capabilities != null) {
            if (scanResult.capabilities.contains("WEP")) {
                this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 2;
            } else if (scanResult.capabilities.contains("PSK")) {
                this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 2;
            } else if (scanResult.capabilities.contains("EAP")) {
                this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.authentication = 3;
            }
        }
        this.mCurrentConnectionEvent.mRouterFingerPrint.mRouterFingerPrintProto.channelInfo = scanResult.frequency;
    }

    void setNumSavedNetworks(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numSavedNetworks = num;
        }
    }

    void setNumOpenNetworks(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numOpenNetworks = num;
        }
    }

    void setNumPersonalNetworks(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numPersonalNetworks = num;
        }
    }

    void setNumEnterpriseNetworks(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numEnterpriseNetworks = num;
        }
    }

    void setNumNetworksAddedByUser(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numNetworksAddedByUser = num;
        }
    }

    void setNumNetworksAddedByApps(int num) {
        synchronized (this.mLock) {
            this.mWifiLogProto.numNetworksAddedByApps = num;
        }
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
            wifiLog.numNonEmptyScanResults += SCREEN_ON;
        }
    }

    public void incrementEmptyScanResultCount() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numEmptyScanResults += SCREEN_ON;
        }
    }

    public void incrementBackgroundScanCount() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numBackgroundScans += SCREEN_ON;
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
            wifiLog.numOneshotScans += SCREEN_ON;
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
            case SCREEN_OFF /*0*/:
                return "SCAN_UNKNOWN";
            case SCREEN_ON /*1*/:
                return "SCAN_SUCCESS";
            case Extension.TYPE_FLOAT /*2*/:
                return "SCAN_FAILURE_INTERRUPTED";
            case Extension.TYPE_INT64 /*3*/:
                return "SCAN_FAILURE_INVALID_CONFIGURATION";
            case Extension.TYPE_UINT64 /*4*/:
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
            case SCREEN_OFF /*0*/:
                return "WIFI_UNKNOWN";
            case SCREEN_ON /*1*/:
                return "WIFI_DISABLED";
            case Extension.TYPE_FLOAT /*2*/:
                return "WIFI_DISCONNECTED";
            case Extension.TYPE_INT64 /*3*/:
                return "WIFI_ASSOCIATED";
            default:
                return "default";
        }
    }

    public void incrementWifiSystemScanStateCount(int state, boolean screenOn) {
        synchronized (this.mLock) {
            int index = (state * 2) + (screenOn ? SCREEN_ON : SCREEN_OFF);
            this.mWifiSystemStateEntries.put(index, this.mWifiSystemStateEntries.get(index) + SCREEN_ON);
        }
    }

    public int getSystemStateCount(int state, boolean screenOn) {
        int i;
        synchronized (this.mLock) {
            i = this.mWifiSystemStateEntries.get((state * 2) + (screenOn ? SCREEN_ON : SCREEN_OFF));
        }
        return i;
    }

    public void incrementNumLastResortWatchdogTriggers() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggers += SCREEN_ON;
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
            wifiLog.numLastResortWatchdogTriggersWithBadAssociation += SCREEN_ON;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadAuthentication() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadAuthentication += SCREEN_ON;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadDhcp() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadDhcp += SCREEN_ON;
        }
    }

    public void incrementNumLastResortWatchdogTriggersWithBadOther() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numLastResortWatchdogTriggersWithBadOther += SCREEN_ON;
        }
    }

    public void incrementNumConnectivityWatchdogPnoGood() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogPnoGood += SCREEN_ON;
        }
    }

    public void incrementNumConnectivityWatchdogPnoBad() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogPnoBad += SCREEN_ON;
        }
    }

    public void incrementNumConnectivityWatchdogBackgroundGood() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogBackgroundGood += SCREEN_ON;
        }
    }

    public void incrementNumConnectivityWatchdogBackgroundBad() {
        synchronized (this.mLock) {
            WifiLog wifiLog = this.mWifiLogProto;
            wifiLog.numConnectivityWatchdogBackgroundBad += SCREEN_ON;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mLock) {
            pw.println("WifiMetrics:");
            if (args == null || args.length <= 0 || !PROTO_DUMP_ARG.equals(args[SCREEN_OFF])) {
                pw.println("mConnectionEvents:");
                for (ConnectionEvent event : this.mConnectionEventList) {
                    String eventLine = event.toString();
                    if (event == this.mCurrentConnectionEvent) {
                        eventLine = eventLine + "CURRENTLY OPEN EVENT";
                    }
                    pw.println(eventLine);
                }
                pw.println("mWifiLogProto.numSavedNetworks=" + this.mWifiLogProto.numSavedNetworks);
                pw.println("mWifiLogProto.numOpenNetworks=" + this.mWifiLogProto.numOpenNetworks);
                pw.println("mWifiLogProto.numPersonalNetworks=" + this.mWifiLogProto.numPersonalNetworks);
                pw.println("mWifiLogProto.numEnterpriseNetworks=" + this.mWifiLogProto.numEnterpriseNetworks);
                pw.println("mWifiLogProto.isLocationEnabled=" + this.mWifiLogProto.isLocationEnabled);
                pw.println("mWifiLogProto.isScanningAlwaysEnabled=" + this.mWifiLogProto.isScanningAlwaysEnabled);
                pw.println("mWifiLogProto.numNetworksAddedByUser=" + this.mWifiLogProto.numNetworksAddedByUser);
                pw.println("mWifiLogProto.numNetworksAddedByApps=" + this.mWifiLogProto.numNetworksAddedByApps);
                pw.println("mWifiLogProto.numNonEmptyScanResults=" + this.mWifiLogProto.numNonEmptyScanResults);
                pw.println("mWifiLogProto.numEmptyScanResults=" + this.mWifiLogProto.numEmptyScanResults);
                pw.println("mWifiLogProto.numOneshotScans=" + this.mWifiLogProto.numOneshotScans);
                pw.println("mWifiLogProto.numBackgroundScans=" + this.mWifiLogProto.numBackgroundScans);
                pw.println("mScanReturnEntries:");
                pw.println("  SCAN_UNKNOWN: " + getScanReturnEntry(SCREEN_OFF));
                pw.println("  SCAN_SUCCESS: " + getScanReturnEntry(SCREEN_ON));
                pw.println("  SCAN_FAILURE_INTERRUPTED: " + getScanReturnEntry(2));
                pw.println("  SCAN_FAILURE_INVALID_CONFIGURATION: " + getScanReturnEntry(3));
                pw.println("  FAILURE_WIFI_DISABLED: " + getScanReturnEntry(4));
                pw.println("mSystemStateEntries: <state><screenOn> : <scansInitiated>");
                pw.println("  WIFI_UNKNOWN       ON: " + getSystemStateCount(SCREEN_OFF, true));
                pw.println("  WIFI_DISABLED      ON: " + getSystemStateCount(SCREEN_ON, true));
                pw.println("  WIFI_DISCONNECTED  ON: " + getSystemStateCount(2, true));
                pw.println("  WIFI_ASSOCIATED    ON: " + getSystemStateCount(3, true));
                pw.println("  WIFI_UNKNOWN      OFF: " + getSystemStateCount(SCREEN_OFF, DBG));
                pw.println("  WIFI_DISABLED     OFF: " + getSystemStateCount(SCREEN_ON, DBG));
                pw.println("  WIFI_DISCONNECTED OFF: " + getSystemStateCount(2, DBG));
                pw.println("  WIFI_ASSOCIATED   OFF: " + getSystemStateCount(3, DBG));
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
                pw.println("mWifiLogProto.recordDurationSec=" + ((this.mClock.elapsedRealtime() / 1000) - this.mRecordStartTimeSec));
            } else {
                consolidateProto(true);
                for (ConnectionEvent event2 : this.mConnectionEventList) {
                    if (this.mCurrentConnectionEvent != event2) {
                        event2.mConnectionEvent.automaticBugReportTaken = true;
                    }
                }
                pw.println(Base64.encodeToString(MessageNano.toByteArray(this.mWifiLogProto), SCREEN_OFF));
                pw.println("EndWifiMetrics");
                clear();
            }
        }
    }

    private void consolidateProto(boolean incremental) {
        List<com.android.server.wifi.WifiMetricsProto.ConnectionEvent> events = new ArrayList();
        synchronized (this.mLock) {
            int i;
            for (ConnectionEvent event : this.mConnectionEventList) {
                if (!(incremental && (this.mCurrentConnectionEvent == event || event.mConnectionEvent.automaticBugReportTaken))) {
                    events.add(event.mConnectionEvent);
                    if (incremental) {
                        event.mConnectionEvent.automaticBugReportTaken = true;
                    }
                }
            }
            if (events.size() > 0) {
                this.mWifiLogProto.connectionEvent = (com.android.server.wifi.WifiMetricsProto.ConnectionEvent[]) events.toArray(this.mWifiLogProto.connectionEvent);
            }
            this.mWifiLogProto.scanReturnEntries = new ScanReturnEntry[this.mScanReturnEntries.size()];
            for (i = SCREEN_OFF; i < this.mScanReturnEntries.size(); i += SCREEN_ON) {
                this.mWifiLogProto.scanReturnEntries[i] = new ScanReturnEntry();
                this.mWifiLogProto.scanReturnEntries[i].scanReturnCode = this.mScanReturnEntries.keyAt(i);
                this.mWifiLogProto.scanReturnEntries[i].scanResultsCount = this.mScanReturnEntries.valueAt(i);
            }
            this.mWifiLogProto.wifiSystemStateEntries = new WifiSystemStateEntry[this.mWifiSystemStateEntries.size()];
            for (i = SCREEN_OFF; i < this.mWifiSystemStateEntries.size(); i += SCREEN_ON) {
                boolean z;
                this.mWifiLogProto.wifiSystemStateEntries[i] = new WifiSystemStateEntry();
                this.mWifiLogProto.wifiSystemStateEntries[i].wifiState = this.mWifiSystemStateEntries.keyAt(i) / 2;
                this.mWifiLogProto.wifiSystemStateEntries[i].wifiStateCount = this.mWifiSystemStateEntries.valueAt(i);
                WifiSystemStateEntry wifiSystemStateEntry = this.mWifiLogProto.wifiSystemStateEntries[i];
                if (this.mWifiSystemStateEntries.keyAt(i) % 2 > 0) {
                    z = true;
                } else {
                    z = DBG;
                }
                wifiSystemStateEntry.isScreenOn = z;
            }
            this.mWifiLogProto.recordDurationSec = (int) ((this.mClock.elapsedRealtime() / 1000) - this.mRecordStartTimeSec);
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
            this.mRecordStartTimeSec = this.mClock.elapsedRealtime() / 1000;
            this.mWifiLogProto.clear();
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
}
