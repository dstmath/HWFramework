package android.net.wifi;

import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.BackupUtils;
import android.util.BackupUtils.BadVersionException;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;

public class WifiConfiguration implements Parcelable {
    public static final int AP_BAND_2GHZ = 0;
    public static final int AP_BAND_5GHZ = 1;
    public static final int AP_NO_INTERNET_ACCESS = 0;
    public static final int AP_TYPE_INTERNET_ACCESS = 3;
    public static final int AP_TYPE_NO_INTERNET = 1;
    public static final int AP_TYPE_PORTAL = 2;
    public static final int AP_TYPE_UNKOWN = 0;
    private static final int BACKUP_VERSION = 2;
    public static final Creator<WifiConfiguration> CREATOR = null;
    private static boolean DBG = false;
    public static final int HOME_NETWORK_RSSI_BOOST = 5;
    private static final boolean HWFLOW = false;
    public static final int INTERNET_RECOVERED = 5;
    public static final int INTERNET_UNKNOWN = 3;
    public static final int INTERNET_UNRECOVERED = 4;
    public static final int INVALID_NETWORK_ID = -1;
    public static int INVALID_RSSI = 0;
    public static final int PORTAL_AP_UNAUTHORIZED = 1;
    public static final int PORTAL_HAS_INTERNET = 1;
    public static final int PORTAL_UNAUTHEN = 2;
    public static final int PORTAL_UNKNOWN = 0;
    public static final int QOS_LEVEL_GOOD = 3;
    public static final int QOS_LEVEL_NORMAL = 2;
    public static final int QOS_LEVEL_POOR = 1;
    public static final int QOS_LEVEL_UNKOWN = 0;
    public static int ROAMING_FAILURE_AUTH_FAILURE = 0;
    public static int ROAMING_FAILURE_IP_CONFIG = 0;
    private static final String TAG = "WifiConfiguration";
    public static final int UNKNOWN_UID = -1;
    public static final int USER_APPROVED = 1;
    public static final int USER_BANNED = 2;
    public static final int USER_PENDING = 3;
    public static final int USER_UNSPECIFIED = 0;
    public static final int WAPI_ASCII_PASSWORD = 0;
    public static final String WAPI_CERT_FLAG = "WAPI-CERT";
    public static final int WAPI_HEX_PASSWORD = 1;
    public static final String WAPI_PSK_FLAG = "WAPI-PSK";
    public static final String bssidVarName = "bssid";
    public static final String cloudSecurityCheckVarName = "cloudSecurityCheck";
    public static final String hiddenSSIDVarName = "scan_ssid";
    public static final String pmfVarName = "ieee80211w";
    public static final String priorityVarName = "priority";
    public static final String pskVarName = "psk";
    public static final String ssidVarName = "ssid";
    public static final String updateIdentiferVarName = "update_identifier";
    public static final String[] wepKeyVarNames = null;
    public static final String wepTxKeyIdxVarName = "wep_tx_keyidx";
    public String BSSID;
    public String FQDN;
    public String SSID;
    public BitSet allowedAuthAlgorithms;
    public BitSet allowedGroupCiphers;
    public BitSet allowedKeyManagement;
    public BitSet allowedPairwiseCiphers;
    public BitSet allowedProtocols;
    public int apBand;
    public int apChannel;
    public int callingPid;
    public int cloudSecurityCheck;
    public int consecutiveGoodRssiCounter;
    public String creationTime;
    public String creatorName;
    public int creatorUid;
    public String defaultGwMacAddress;
    public String dhcpServer;
    public boolean didSelfAdd;
    public int dtimInterval;
    public WifiEnterpriseConfig enterpriseConfig;
    public boolean ephemeral;
    public boolean hiddenSSID;
    public int internetAccessType;
    public String internetHistory;
    public long internetRecoveryCheckTimestamp;
    public int internetRecoveryStatus;
    public String internetSelfCureHistory;
    public boolean isHiLinkNetwork;
    public boolean isTempCreated;
    public int lastConnectUid;
    public long lastConnected;
    public long lastConnectionFailure;
    public String lastDhcpResults;
    public long lastDisconnected;
    public String lastFailure;
    public long lastHasInternetTimestamp;
    public long lastRoamingFailure;
    public int lastRoamingFailureReason;
    public String lastUpdateName;
    public int lastUpdateUid;
    public HashMap<String, Integer> linkedConfigurations;
    String mCachedConfigKey;
    private IpConfiguration mIpConfiguration;
    private final NetworkSelectionStatus mNetworkSelectionStatus;
    private String mPasspointManagementObjectTree;
    public boolean meteredHint;
    public int networkId;
    public int networkQosLevel;
    public int networkQosScore;
    public boolean noInternetAccess;
    public boolean noInternetAccessExpected;
    public int numAssociation;
    public int numNoInternetAccessReports;
    public int numScorerOverride;
    public int numScorerOverrideAndSwitchedNetwork;
    public int numTicksAtBadRSSI;
    public int numTicksAtLowRSSI;
    public int numTicksAtNotHighRSSI;
    public int numUserTriggeredJoinAttempts;
    public int numUserTriggeredWifiDisableBadRSSI;
    public int numUserTriggeredWifiDisableLowRSSI;
    public int numUserTriggeredWifiDisableNotHighRSSI;
    public String oriSsid;
    public String peerWifiConfiguration;
    public boolean poorRssiDectected;
    public int portalCheckStatus;
    public boolean portalNetwork;
    public String preSharedKey;
    public int priority;
    public String providerFriendlyName;
    public boolean requirePMF;
    public long[] roamingConsortiumIds;
    public long roamingFailureBlackListTimeMilli;
    public boolean selfAdded;
    public boolean shared;
    public int status;
    public String updateIdentifier;
    public String updateTime;
    public boolean useExternalScores;
    public int userApproved;
    public boolean validatedInternetAccess;
    public Visibility visibility;
    public String wapiAsCertBcm;
    public String wapiAsCertQualcomm;
    public int wapiCertIndexBcm;
    public String wapiPskQualcomm;
    public int wapiPskTypeBcm;
    public int wapiPskTypeQualcomm;
    public String wapiUserCertBcm;
    public String wapiUserCertQualcomm;
    public String[] wepKeys;
    public int wepTxKeyIndex;
    public boolean wifiProNoHandoverNetwork;
    public boolean wifiProNoInternetAccess;
    public int wifiProNoInternetReason;

    public static class AuthAlgorithm {
        public static final int LEAP = 2;
        public static final int OPEN = 0;
        public static final int SHARED = 1;
        public static final String[] strings = null;
        public static final String varName = "auth_alg";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiConfiguration.AuthAlgorithm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiConfiguration.AuthAlgorithm.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiConfiguration.AuthAlgorithm.<clinit>():void");
        }

        private AuthAlgorithm() {
        }
    }

    public static class BcmWapi {
        public static final String wapiAsCertVarName = "wapi_as_cert";
        public static final String wapiCertIndexVarName = "cert_index";
        public static final String wapiPskTypeVarName = "psk_key_type";
        public static final String wapiUserCertVarName = "wapi_user_cert";

        public BcmWapi() {
        }
    }

    public static class GroupCipher {
        public static final int CCMP = 3;
        public static final int GTK_NOT_USED = 4;
        public static final int TKIP = 2;
        public static final int WEP104 = 1;
        public static final int WEP40 = 0;
        public static final String[] strings = null;
        public static final String varName = "group";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiConfiguration.GroupCipher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiConfiguration.GroupCipher.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiConfiguration.GroupCipher.<clinit>():void");
        }

        private GroupCipher() {
        }
    }

    public static class KeyMgmt {
        public static final int BCM_WAPI_CERT = 7;
        public static final int BCM_WAPI_PSK = 6;
        public static final int IEEE8021X = 3;
        public static final int NONE = 0;
        public static final int OSEN = 5;
        public static final int QUALCOMM_WAPI_CERT = 9;
        public static final int QUALCOMM_WAPI_PSK = 8;
        public static final int WPA2_PSK = 4;
        public static final int WPA_EAP = 2;
        public static final int WPA_PSK = 1;
        public static final String[] strings = null;
        public static final String varName = "key_mgmt";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiConfiguration.KeyMgmt.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiConfiguration.KeyMgmt.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiConfiguration.KeyMgmt.<clinit>():void");
        }

        private KeyMgmt() {
        }
    }

    public static class NetworkSelectionStatus {
        private static final int CONNECT_CHOICE_EXISTS = 1;
        private static final int CONNECT_CHOICE_NOT_EXISTS = -1;
        public static final int DISABLED_ASSOCIATION_REJECTION = 2;
        public static final int DISABLED_AUTHENTICATION_FAILURE = 3;
        public static final int DISABLED_AUTHENTICATION_NO_CREDENTIALS = 7;
        public static final int DISABLED_BAD_LINK = 1;
        public static final int DISABLED_BY_SYSTEM = 10;
        public static final int DISABLED_BY_WIFI_MANAGER = 9;
        public static final int DISABLED_DHCP_FAILURE = 4;
        public static final int DISABLED_DNS_FAILURE = 5;
        public static final int DISABLED_NO_INTERNET = 8;
        public static final int DISABLED_TLS_VERSION_MISMATCH = 6;
        public static final int DISABLED_UNKNOWN_REASON = -1;
        public static final long INVALID_NETWORK_SELECTION_DISABLE_TIMESTAMP = -1;
        public static final int NETWORK_SELECTION_DISABLED_MAX = 11;
        public static final int NETWORK_SELECTION_ENABLE = 0;
        public static final int NETWORK_SELECTION_ENABLED = 0;
        public static final int NETWORK_SELECTION_PERMANENTLY_DISABLED = 2;
        public static final int NETWORK_SELECTION_STATUS_MAX = 3;
        public static final int NETWORK_SELECTION_TEMPORARY_DISABLED = 1;
        private static final String[] QUALITY_NETWORK_SELECTION_DISABLE_REASON = null;
        private static final String[] QUALITY_NETWORK_SELECTION_STATUS = null;
        private ScanResult mCandidate;
        private int mCandidateScore;
        private String mConnectChoice;
        private long mConnectChoiceTimestamp;
        private boolean mHasEverConnected;
        private int[] mNetworkSeclectionDisableCounter;
        private String mNetworkSelectionBSSID;
        private String mNetworkSelectionDisableName;
        private int mNetworkSelectionDisableReason;
        private boolean mSeenInLastQualifiedNetworkSelection;
        private int mStatus;
        private long mTemporarilyDisabledTimestamp;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiConfiguration.NetworkSelectionStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiConfiguration.NetworkSelectionStatus.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiConfiguration.NetworkSelectionStatus.<clinit>():void");
        }

        /* synthetic */ NetworkSelectionStatus(NetworkSelectionStatus networkSelectionStatus) {
            this();
        }

        public void setSeenInLastQualifiedNetworkSelection(boolean seen) {
            this.mSeenInLastQualifiedNetworkSelection = seen;
        }

        public boolean getSeenInLastQualifiedNetworkSelection() {
            return this.mSeenInLastQualifiedNetworkSelection;
        }

        public void setCandidate(ScanResult scanCandidate) {
            this.mCandidate = scanCandidate;
        }

        public ScanResult getCandidate() {
            return this.mCandidate;
        }

        public void setCandidateScore(int score) {
            this.mCandidateScore = score;
        }

        public int getCandidateScore() {
            return this.mCandidateScore;
        }

        public String getConnectChoice() {
            return this.mConnectChoice;
        }

        public void setConnectChoice(String newConnectChoice) {
            this.mConnectChoice = newConnectChoice;
        }

        public long getConnectChoiceTimestamp() {
            return this.mConnectChoiceTimestamp;
        }

        public void setConnectChoiceTimestamp(long timeStamp) {
            this.mConnectChoiceTimestamp = timeStamp;
        }

        public String getNetworkStatusString() {
            return QUALITY_NETWORK_SELECTION_STATUS[this.mStatus];
        }

        public void setHasEverConnected(boolean value) {
            this.mHasEverConnected = value;
        }

        public boolean getHasEverConnected() {
            return this.mHasEverConnected;
        }

        private NetworkSelectionStatus() {
            this.mTemporarilyDisabledTimestamp = INVALID_NETWORK_SELECTION_DISABLE_TIMESTAMP;
            this.mNetworkSeclectionDisableCounter = new int[NETWORK_SELECTION_DISABLED_MAX];
            this.mConnectChoiceTimestamp = INVALID_NETWORK_SELECTION_DISABLE_TIMESTAMP;
            this.mHasEverConnected = WifiConfiguration.HWFLOW;
        }

        public static String getNetworkDisableReasonString(int reason) {
            if (reason < 0 || reason >= NETWORK_SELECTION_DISABLED_MAX) {
                return null;
            }
            return QUALITY_NETWORK_SELECTION_DISABLE_REASON[reason];
        }

        public String getNetworkDisableReasonString() {
            return QUALITY_NETWORK_SELECTION_DISABLE_REASON[this.mNetworkSelectionDisableReason];
        }

        public int getNetworkSelectionStatus() {
            return this.mStatus;
        }

        public boolean isNetworkEnabled() {
            return this.mStatus == 0 ? true : WifiConfiguration.HWFLOW;
        }

        public boolean isNetworkTemporaryDisabled() {
            return this.mStatus == NETWORK_SELECTION_TEMPORARY_DISABLED ? true : WifiConfiguration.HWFLOW;
        }

        public boolean isNetworkPermanentlyDisabled() {
            return this.mStatus == NETWORK_SELECTION_PERMANENTLY_DISABLED ? true : WifiConfiguration.HWFLOW;
        }

        public void setNetworkSelectionStatus(int status) {
            if (status >= 0 && status < NETWORK_SELECTION_STATUS_MAX) {
                this.mStatus = status;
            }
            if (status == 0) {
                this.mNetworkSelectionDisableName = null;
            }
        }

        public int getNetworkSelectionDisableReason() {
            return this.mNetworkSelectionDisableReason;
        }

        public void setNetworkSelectionDisableReason(int reason) {
            if (reason < 0 || reason >= NETWORK_SELECTION_DISABLED_MAX) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            this.mNetworkSelectionDisableReason = reason;
        }

        public boolean isDisabledByReason(int reason) {
            return this.mNetworkSelectionDisableReason == reason ? true : WifiConfiguration.HWFLOW;
        }

        public void setDisableTime(long timeStamp) {
            this.mTemporarilyDisabledTimestamp = timeStamp;
        }

        public long getDisableTime() {
            return this.mTemporarilyDisabledTimestamp;
        }

        public int getDisableReasonCounter(int reason) {
            if (reason >= 0 && reason < NETWORK_SELECTION_DISABLED_MAX) {
                return this.mNetworkSeclectionDisableCounter[reason];
            }
            throw new IllegalArgumentException("Illegal reason value: " + reason);
        }

        public void setDisableReasonCounter(int reason, int value) {
            if (reason < 0 || reason >= NETWORK_SELECTION_DISABLED_MAX) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            this.mNetworkSeclectionDisableCounter[reason] = value;
        }

        public void incrementDisableReasonCounter(int reason) {
            if (reason < 0 || reason >= NETWORK_SELECTION_DISABLED_MAX) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            int[] iArr = this.mNetworkSeclectionDisableCounter;
            iArr[reason] = iArr[reason] + NETWORK_SELECTION_TEMPORARY_DISABLED;
        }

        public void clearDisableReasonCounter(int reason) {
            if (reason < 0 || reason >= NETWORK_SELECTION_DISABLED_MAX) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            this.mNetworkSeclectionDisableCounter[reason] = NETWORK_SELECTION_ENABLED;
        }

        public void clearDisableReasonCounter() {
            Arrays.fill(this.mNetworkSeclectionDisableCounter, NETWORK_SELECTION_ENABLED);
        }

        public String getNetworkSelectionBSSID() {
            return this.mNetworkSelectionBSSID;
        }

        public void setNetworkSelectionBSSID(String bssid) {
            this.mNetworkSelectionBSSID = bssid;
        }

        public void setNetworkSelectionDisableName(String name) {
            this.mNetworkSelectionDisableName = name;
        }

        public String getNetworkSelectionDisableName() {
            return this.mNetworkSelectionDisableName;
        }

        public void copy(NetworkSelectionStatus source) {
            this.mStatus = source.mStatus;
            this.mNetworkSelectionDisableReason = source.mNetworkSelectionDisableReason;
            for (int index = NETWORK_SELECTION_ENABLED; index < NETWORK_SELECTION_DISABLED_MAX; index += NETWORK_SELECTION_TEMPORARY_DISABLED) {
                this.mNetworkSeclectionDisableCounter[index] = source.mNetworkSeclectionDisableCounter[index];
            }
            this.mTemporarilyDisabledTimestamp = source.mTemporarilyDisabledTimestamp;
            this.mNetworkSelectionBSSID = source.mNetworkSelectionBSSID;
            setConnectChoice(source.getConnectChoice());
            setConnectChoiceTimestamp(source.getConnectChoiceTimestamp());
            setHasEverConnected(source.getHasEverConnected());
            setNetworkSelectionDisableName(source.getNetworkSelectionDisableName());
        }

        public void writeToParcel(Parcel dest) {
            int i = NETWORK_SELECTION_TEMPORARY_DISABLED;
            dest.writeInt(getNetworkSelectionStatus());
            dest.writeInt(getNetworkSelectionDisableReason());
            for (int index = NETWORK_SELECTION_ENABLED; index < NETWORK_SELECTION_DISABLED_MAX; index += NETWORK_SELECTION_TEMPORARY_DISABLED) {
                dest.writeInt(getDisableReasonCounter(index));
            }
            dest.writeLong(getDisableTime());
            dest.writeString(getNetworkSelectionBSSID());
            if (getConnectChoice() != null) {
                dest.writeInt(NETWORK_SELECTION_TEMPORARY_DISABLED);
                dest.writeString(getConnectChoice());
                dest.writeLong(getConnectChoiceTimestamp());
            } else {
                dest.writeInt(DISABLED_UNKNOWN_REASON);
            }
            if (!getHasEverConnected()) {
                i = NETWORK_SELECTION_ENABLED;
            }
            dest.writeInt(i);
            dest.writeString(getNetworkSelectionDisableName());
        }

        public void readFromParcel(Parcel in) {
            boolean z = true;
            setNetworkSelectionStatus(in.readInt());
            setNetworkSelectionDisableReason(in.readInt());
            for (int index = NETWORK_SELECTION_ENABLED; index < NETWORK_SELECTION_DISABLED_MAX; index += NETWORK_SELECTION_TEMPORARY_DISABLED) {
                setDisableReasonCounter(index, in.readInt());
            }
            setDisableTime(in.readLong());
            setNetworkSelectionBSSID(in.readString());
            if (in.readInt() == NETWORK_SELECTION_TEMPORARY_DISABLED) {
                setConnectChoice(in.readString());
                setConnectChoiceTimestamp(in.readLong());
            } else {
                setConnectChoice(null);
                setConnectChoiceTimestamp(INVALID_NETWORK_SELECTION_DISABLE_TIMESTAMP);
            }
            if (in.readInt() == 0) {
                z = WifiConfiguration.HWFLOW;
            }
            setHasEverConnected(z);
            setNetworkSelectionDisableName(in.readString());
        }
    }

    public static class PairwiseCipher {
        public static final int CCMP = 2;
        public static final int NONE = 0;
        public static final int TKIP = 1;
        public static final String[] strings = null;
        public static final String varName = "pairwise";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiConfiguration.PairwiseCipher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiConfiguration.PairwiseCipher.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiConfiguration.PairwiseCipher.<clinit>():void");
        }

        private PairwiseCipher() {
        }
    }

    public static class Protocol {
        public static final int OSEN = 2;
        public static final int RSN = 1;
        public static final int WAPI = 3;
        public static final int WPA = 0;
        public static final String[] strings = null;
        public static final String varName = "proto";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiConfiguration.Protocol.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiConfiguration.Protocol.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiConfiguration.Protocol.<clinit>():void");
        }

        private Protocol() {
        }
    }

    public static class QualcommWapi {
        public static final String wapiAsCertVarName = "as_cert_file";
        public static final String wapiPskTypeVarName = "wapi_key_type";
        public static final String wapiPskVarName = "wapi_psk";
        public static final String wapiUserCertVarName = "user_cert_file";

        public QualcommWapi() {
        }
    }

    public static class Status {
        public static final int CURRENT = 0;
        public static final int DISABLED = 1;
        public static final int ENABLED = 2;
        public static final String[] strings = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiConfiguration.Status.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiConfiguration.Status.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiConfiguration.Status.<clinit>():void");
        }

        private Status() {
        }
    }

    public static final class Visibility {
        public String BSSID24;
        public String BSSID5;
        public long age24;
        public long age5;
        public int bandPreferenceBoost;
        public int currentNetworkBoost;
        public int lastChoiceBoost;
        public String lastChoiceConfig;
        public int num24;
        public int num5;
        public int rssi24;
        public int rssi5;
        public int score;

        public Visibility() {
            this.rssi5 = WifiConfiguration.INVALID_RSSI;
            this.rssi24 = WifiConfiguration.INVALID_RSSI;
        }

        public Visibility(Visibility source) {
            this.rssi5 = source.rssi5;
            this.rssi24 = source.rssi24;
            this.age24 = source.age24;
            this.age5 = source.age5;
            this.num24 = source.num24;
            this.num5 = source.num5;
            this.BSSID5 = source.BSSID5;
            this.BSSID24 = source.BSSID24;
        }

        public String toString() {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("[");
            if (this.rssi24 > WifiConfiguration.INVALID_RSSI) {
                sbuf.append(Integer.toString(this.rssi24));
                sbuf.append(",");
                sbuf.append(Integer.toString(this.num24));
                if (this.BSSID24 != null) {
                    sbuf.append(",").append(this.BSSID24);
                }
            }
            sbuf.append("; ");
            if (this.rssi5 > WifiConfiguration.INVALID_RSSI) {
                sbuf.append(Integer.toString(this.rssi5));
                sbuf.append(",");
                sbuf.append(Integer.toString(this.num5));
                if (this.BSSID5 != null) {
                    sbuf.append(",").append(this.BSSID5);
                }
            }
            if (this.score != 0) {
                sbuf.append("; ").append(this.score);
                sbuf.append(", ").append(this.currentNetworkBoost);
                sbuf.append(", ").append(this.bandPreferenceBoost);
                if (this.lastChoiceConfig != null) {
                    sbuf.append(", ").append(this.lastChoiceBoost);
                    sbuf.append(", ").append(this.lastChoiceConfig);
                }
            }
            sbuf.append("]");
            return sbuf.toString();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiConfiguration.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiConfiguration.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiConfiguration.<clinit>():void");
    }

    public void setVisibility(Visibility status) {
        this.visibility = status;
    }

    public boolean hasNoInternetAccess() {
        return (this.numNoInternetAccessReports <= 0 || this.validatedInternetAccess) ? HWFLOW : true;
    }

    public NetworkSelectionStatus getNetworkSelectionStatus() {
        return this.mNetworkSelectionStatus;
    }

    public WifiConfiguration() {
        this.apBand = WAPI_ASCII_PASSWORD;
        this.apChannel = WAPI_ASCII_PASSWORD;
        this.dtimInterval = WAPI_ASCII_PASSWORD;
        this.userApproved = WAPI_ASCII_PASSWORD;
        this.roamingFailureBlackListTimeMilli = 1000;
        this.mNetworkSelectionStatus = new NetworkSelectionStatus();
        this.networkId = UNKNOWN_UID;
        this.SSID = null;
        this.BSSID = null;
        this.FQDN = null;
        this.roamingConsortiumIds = new long[WAPI_ASCII_PASSWORD];
        this.priority = WAPI_ASCII_PASSWORD;
        this.hiddenSSID = HWFLOW;
        this.allowedKeyManagement = new BitSet();
        this.allowedProtocols = new BitSet();
        this.allowedAuthAlgorithms = new BitSet();
        this.allowedPairwiseCiphers = new BitSet();
        this.allowedGroupCiphers = new BitSet();
        this.wepKeys = new String[INTERNET_UNRECOVERED];
        for (int i = WAPI_ASCII_PASSWORD; i < this.wepKeys.length; i += WAPI_HEX_PASSWORD) {
            this.wepKeys[i] = null;
        }
        this.callingPid = WAPI_ASCII_PASSWORD;
        this.enterpriseConfig = new WifiEnterpriseConfig();
        this.selfAdded = HWFLOW;
        this.didSelfAdd = HWFLOW;
        this.ephemeral = HWFLOW;
        this.meteredHint = HWFLOW;
        this.useExternalScores = HWFLOW;
        this.validatedInternetAccess = HWFLOW;
        this.mIpConfiguration = new IpConfiguration();
        this.lastUpdateUid = UNKNOWN_UID;
        this.creatorUid = UNKNOWN_UID;
        this.shared = true;
        this.dtimInterval = WAPI_ASCII_PASSWORD;
        this.wapiAsCertBcm = null;
        this.wapiUserCertBcm = null;
        this.wapiCertIndexBcm = UNKNOWN_UID;
        this.wapiPskTypeBcm = UNKNOWN_UID;
        this.wapiPskTypeQualcomm = WAPI_ASCII_PASSWORD;
        this.noInternetAccess = HWFLOW;
        this.internetHistory = NetworkHistoryUtils.INTERNET_HISTORY_INIT;
        this.portalNetwork = HWFLOW;
        this.lastDhcpResults = ProxyInfo.LOCAL_EXCL_LIST;
        this.internetSelfCureHistory = ProxyInfo.LOCAL_EXCL_LIST;
        this.portalCheckStatus = WAPI_ASCII_PASSWORD;
        this.poorRssiDectected = HWFLOW;
        this.consecutiveGoodRssiCounter = WAPI_ASCII_PASSWORD;
        this.lastHasInternetTimestamp = 0;
        this.internetRecoveryStatus = USER_PENDING;
        this.internetRecoveryCheckTimestamp = 0;
        this.wifiProNoInternetAccess = HWFLOW;
        this.wifiProNoHandoverNetwork = HWFLOW;
        this.wifiProNoInternetReason = WAPI_ASCII_PASSWORD;
        this.internetAccessType = WAPI_ASCII_PASSWORD;
        this.networkQosLevel = WAPI_ASCII_PASSWORD;
        this.networkQosScore = WAPI_ASCII_PASSWORD;
        this.isTempCreated = HWFLOW;
        this.isHiLinkNetwork = HWFLOW;
    }

    public boolean isPasspoint() {
        if (TextUtils.isEmpty(this.FQDN) || TextUtils.isEmpty(this.providerFriendlyName) || this.enterpriseConfig == null || this.enterpriseConfig.getEapMethod() == UNKNOWN_UID) {
            return HWFLOW;
        }
        return true;
    }

    public boolean isSsidValid() {
        if (this.SSID != null && this.SSID.startsWith("\"") && this.SSID.endsWith("\"")) {
            return true;
        }
        return HWFLOW;
    }

    public void defendSsid() {
        if (!isSsidValid()) {
            this.SSID = "\"" + this.SSID + "\"";
        }
    }

    public boolean isLinked(WifiConfiguration config) {
        if (config == null || config.linkedConfigurations == null || this.linkedConfigurations == null || config.linkedConfigurations.get(configKey()) == null || this.linkedConfigurations.get(config.configKey()) == null) {
            return HWFLOW;
        }
        return true;
    }

    public boolean isEnterprise() {
        if (this.allowedKeyManagement.get(USER_BANNED)) {
            return true;
        }
        return this.allowedKeyManagement.get(USER_PENDING);
    }

    public String toString() {
        long diff;
        StringBuilder sbuf = new StringBuilder();
        if (this.status == 0) {
            sbuf.append("* ");
        } else if (this.status == WAPI_HEX_PASSWORD) {
            sbuf.append("- DSBLE ");
        }
        int i = this.networkId;
        String str = this.SSID;
        str = this.providerFriendlyName;
        str = this.BSSID;
        str = this.FQDN;
        i = this.priority;
        sbuf.append("ID: ").append(r0).append(" SSID: ").append(r0).append(" PROVIDER-NAME: ").append(r0).append(" BSSID: ").append(r0).append(" FQDN: ").append(r0).append(" PRIO: ").append(r0).append(" HIDDEN: ").append(this.hiddenSSID).append('\n');
        sbuf.append(" NetworkSelectionStatus ").append(this.mNetworkSelectionStatus.getNetworkStatusString()).append("\n");
        if (this.mNetworkSelectionStatus.getNetworkSelectionDisableReason() > 0) {
            sbuf.append(" mNetworkSelectionDisableReason ").append(this.mNetworkSelectionStatus.getNetworkDisableReasonString()).append("\n");
            if (this.mNetworkSelectionStatus.getNetworkSelectionDisableName() != null) {
                sbuf.append(" networkSelectionDisableName ").append(this.mNetworkSelectionStatus.getNetworkSelectionDisableName()).append("\n");
            }
            for (int index = WAPI_ASCII_PASSWORD; index < 11; index += WAPI_HEX_PASSWORD) {
                if (this.mNetworkSelectionStatus.getDisableReasonCounter(index) != 0) {
                    sbuf.append(NetworkSelectionStatus.getNetworkDisableReasonString(index)).append(" counter:").append(this.mNetworkSelectionStatus.getDisableReasonCounter(index)).append("\n");
                }
            }
        }
        if (this.mNetworkSelectionStatus.getConnectChoice() != null) {
            sbuf.append(" connect choice: ").append(this.mNetworkSelectionStatus.getConnectChoice());
            sbuf.append(" connect choice set time: ").append(this.mNetworkSelectionStatus.getConnectChoiceTimestamp());
        }
        sbuf.append(" hasEverConnected: ").append(this.mNetworkSelectionStatus.getHasEverConnected()).append("\n");
        if (this.numAssociation > 0) {
            sbuf.append(" numAssociation ").append(this.numAssociation).append("\n");
        }
        if (this.numNoInternetAccessReports > 0) {
            sbuf.append(" numNoInternetAccessReports ");
            sbuf.append(this.numNoInternetAccessReports).append("\n");
        }
        if (this.updateTime != null) {
            sbuf.append("update ").append(this.updateTime).append("\n");
        }
        if (this.creationTime != null) {
            sbuf.append("creation").append(this.creationTime).append("\n");
        }
        if (this.didSelfAdd) {
            sbuf.append(" didSelfAdd");
        }
        if (this.selfAdded) {
            sbuf.append(" selfAdded");
        }
        if (this.validatedInternetAccess) {
            sbuf.append(" validatedInternetAccess");
        }
        if (this.ephemeral) {
            sbuf.append(" ephemeral");
        }
        if (this.meteredHint) {
            sbuf.append(" meteredHint");
        }
        if (this.useExternalScores) {
            sbuf.append(" useExternalScores");
        }
        if (this.didSelfAdd || this.selfAdded || this.validatedInternetAccess || this.ephemeral || this.meteredHint || this.useExternalScores) {
            sbuf.append("\n");
        }
        sbuf.append(" KeyMgmt:");
        for (int k = WAPI_ASCII_PASSWORD; k < this.allowedKeyManagement.size(); k += WAPI_HEX_PASSWORD) {
            if (this.allowedKeyManagement.get(k)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (k < KeyMgmt.strings.length) {
                    sbuf.append(KeyMgmt.strings[k]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append(" Protocols:");
        for (int p = WAPI_ASCII_PASSWORD; p < this.allowedProtocols.size(); p += WAPI_HEX_PASSWORD) {
            if (this.allowedProtocols.get(p)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (p < Protocol.strings.length) {
                    sbuf.append(Protocol.strings[p]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" AuthAlgorithms:");
        for (int a = WAPI_ASCII_PASSWORD; a < this.allowedAuthAlgorithms.size(); a += WAPI_HEX_PASSWORD) {
            if (this.allowedAuthAlgorithms.get(a)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (a < AuthAlgorithm.strings.length) {
                    sbuf.append(AuthAlgorithm.strings[a]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" PairwiseCiphers:");
        for (int pc = WAPI_ASCII_PASSWORD; pc < this.allowedPairwiseCiphers.size(); pc += WAPI_HEX_PASSWORD) {
            if (this.allowedPairwiseCiphers.get(pc)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (pc < PairwiseCipher.strings.length) {
                    sbuf.append(PairwiseCipher.strings[pc]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" GroupCiphers:");
        for (int gc = WAPI_ASCII_PASSWORD; gc < this.allowedGroupCiphers.size(); gc += WAPI_HEX_PASSWORD) {
            if (this.allowedGroupCiphers.get(gc)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (gc < GroupCipher.strings.length) {
                    sbuf.append(GroupCipher.strings[gc]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n').append(" PSK: ");
        if (this.preSharedKey != null) {
            sbuf.append('*');
        }
        sbuf.append('\n');
        if (this.wapiAsCertBcm != null) {
            sbuf.append(" wapiAsCertBcm: ").append(this.wapiAsCertBcm);
        }
        sbuf.append('\n');
        if (this.wapiUserCertBcm != null) {
            sbuf.append(" WapiUserCertBcm: ").append(this.wapiUserCertBcm);
        }
        sbuf.append('\n');
        if (this.wapiCertIndexBcm != UNKNOWN_UID) {
            sbuf.append(" WapiCertIndexBcm: ").append(this.wapiCertIndexBcm);
        }
        sbuf.append('\n');
        if (this.wapiPskTypeBcm != UNKNOWN_UID) {
            sbuf.append(" WapiPskTypeBcm: ").append(this.wapiPskTypeBcm);
        }
        sbuf.append('\n');
        if (this.wapiPskQualcomm != null) {
            sbuf.append(" wapiPskQualcomm: ").append('*');
        }
        sbuf.append("\nEnterprise config:\n");
        sbuf.append(this.enterpriseConfig);
        sbuf.append("IP config:\n");
        if (this.mIpConfiguration != null) {
            sbuf.append(this.mIpConfiguration.toString());
        }
        if (this.mNetworkSelectionStatus.getNetworkSelectionBSSID() != null) {
            sbuf.append(" networkSelectionBSSID=").append(this.mNetworkSelectionStatus.getNetworkSelectionBSSID());
        }
        long now_ms = System.currentTimeMillis();
        if (this.mNetworkSelectionStatus.getDisableTime() != -1) {
            sbuf.append('\n');
            diff = now_ms - this.mNetworkSelectionStatus.getDisableTime();
            if (diff <= 0) {
                sbuf.append(" blackListed since <incorrect>");
            } else {
                sbuf.append(" blackListed: ").append(Long.toString(diff / 1000)).append("sec ");
            }
        }
        if (this.creatorUid != 0) {
            sbuf.append(" cuid=").append(this.creatorUid);
        }
        if (this.creatorName != null) {
            sbuf.append(" cname=").append(this.creatorName);
        }
        if (this.lastUpdateUid != 0) {
            sbuf.append(" luid=").append(this.lastUpdateUid);
        }
        if (this.lastUpdateName != null) {
            sbuf.append(" lname=").append(this.lastUpdateName);
        }
        sbuf.append(" lcuid=").append(this.lastConnectUid);
        sbuf.append(" userApproved=").append(userApprovedAsString(this.userApproved));
        sbuf.append(" noInternetAccessExpected=").append(this.noInternetAccessExpected);
        sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (this.lastConnected != 0) {
            sbuf.append('\n');
            diff = now_ms - this.lastConnected;
            if (diff <= 0) {
                sbuf.append("lastConnected since <incorrect>");
            } else {
                sbuf.append("lastConnected: ").append(Long.toString(diff / 1000)).append("sec ");
            }
        }
        if (this.lastConnectionFailure != 0) {
            sbuf.append('\n');
            diff = now_ms - this.lastConnectionFailure;
            if (diff <= 0) {
                sbuf.append("lastConnectionFailure since <incorrect> ");
            } else {
                sbuf.append("lastConnectionFailure: ").append(Long.toString(diff / 1000));
                sbuf.append("sec ");
            }
        }
        if (this.lastRoamingFailure != 0) {
            sbuf.append('\n');
            diff = now_ms - this.lastRoamingFailure;
            if (diff <= 0) {
                sbuf.append("lastRoamingFailure since <incorrect> ");
            } else {
                sbuf.append("lastRoamingFailure: ").append(Long.toString(diff / 1000));
                sbuf.append("sec ");
            }
        }
        sbuf.append("roamingFailureBlackListTimeMilli: ").append(Long.toString(this.roamingFailureBlackListTimeMilli));
        sbuf.append('\n');
        if (this.linkedConfigurations != null) {
            for (String key : this.linkedConfigurations.keySet()) {
                sbuf.append(" linked: ").append(key);
                sbuf.append('\n');
            }
        }
        sbuf.append("triggeredLow: ").append(this.numUserTriggeredWifiDisableLowRSSI);
        sbuf.append(" triggeredBad: ").append(this.numUserTriggeredWifiDisableBadRSSI);
        sbuf.append(" triggeredNotHigh: ").append(this.numUserTriggeredWifiDisableNotHighRSSI);
        sbuf.append('\n');
        sbuf.append("ticksLow: ").append(this.numTicksAtLowRSSI);
        sbuf.append(" ticksBad: ").append(this.numTicksAtBadRSSI);
        sbuf.append(" ticksNotHigh: ").append(this.numTicksAtNotHighRSSI);
        sbuf.append('\n');
        sbuf.append("triggeredJoin: ").append(this.numUserTriggeredJoinAttempts);
        sbuf.append('\n');
        return sbuf.toString();
    }

    public String getPrintableSsid() {
        if (this.SSID == null) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        int length = this.SSID.length();
        if (length > USER_BANNED && this.SSID.charAt(WAPI_ASCII_PASSWORD) == '\"' && this.SSID.charAt(length + UNKNOWN_UID) == '\"') {
            return this.SSID.substring(WAPI_HEX_PASSWORD, length + UNKNOWN_UID);
        }
        if (length > USER_PENDING && this.SSID.charAt(WAPI_ASCII_PASSWORD) == 'P' && this.SSID.charAt(WAPI_HEX_PASSWORD) == '\"' && this.SSID.charAt(length + UNKNOWN_UID) == '\"') {
            return WifiSsid.createFromAsciiEncoded(this.SSID.substring(USER_BANNED, length + UNKNOWN_UID)).toString();
        }
        return this.SSID;
    }

    public static String userApprovedAsString(int userApproved) {
        switch (userApproved) {
            case WAPI_ASCII_PASSWORD /*0*/:
                return "USER_UNSPECIFIED";
            case WAPI_HEX_PASSWORD /*1*/:
                return "USER_APPROVED";
            case USER_BANNED /*2*/:
                return "USER_BANNED";
            default:
                return "INVALID";
        }
    }

    public String getKeyIdForCredentials(WifiConfiguration current) {
        WifiEnterpriseConfig wifiEnterpriseConfig = null;
        Object keyMgmt = null;
        try {
            if (TextUtils.isEmpty(this.SSID)) {
                this.SSID = current.SSID;
            }
            if (this.allowedKeyManagement.cardinality() == 0) {
                this.allowedKeyManagement = current.allowedKeyManagement;
            }
            if (this.allowedKeyManagement.get(USER_BANNED)) {
                keyMgmt = KeyMgmt.strings[USER_BANNED];
            }
            if (this.allowedKeyManagement.get(INTERNET_RECOVERED)) {
                keyMgmt = KeyMgmt.strings[INTERNET_RECOVERED];
            }
            if (this.allowedKeyManagement.get(USER_PENDING)) {
                keyMgmt = keyMgmt + KeyMgmt.strings[USER_PENDING];
            }
            if (TextUtils.isEmpty(keyMgmt)) {
                throw new IllegalStateException("Not an EAP network");
            }
            StringBuilder append = new StringBuilder().append(trimStringForKeyId(this.SSID)).append("_").append(keyMgmt).append("_");
            WifiEnterpriseConfig wifiEnterpriseConfig2 = this.enterpriseConfig;
            if (current != null) {
                wifiEnterpriseConfig = current.enterpriseConfig;
            }
            return append.append(trimStringForKeyId(wifiEnterpriseConfig2.getKeyId(wifiEnterpriseConfig))).toString();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Invalid config details");
        }
    }

    private String trimStringForKeyId(String string) {
        return string.replace("\"", ProxyInfo.LOCAL_EXCL_LIST).replace(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, ProxyInfo.LOCAL_EXCL_LIST);
    }

    private static BitSet readBitSet(Parcel src) {
        int cardinality = src.readInt();
        BitSet set = new BitSet();
        for (int i = WAPI_ASCII_PASSWORD; i < cardinality; i += WAPI_HEX_PASSWORD) {
            set.set(src.readInt());
        }
        return set;
    }

    private static void writeBitSet(Parcel dest, BitSet set) {
        int nextSetBit = UNKNOWN_UID;
        dest.writeInt(set.cardinality());
        while (true) {
            nextSetBit = set.nextSetBit(nextSetBit + WAPI_HEX_PASSWORD);
            if (nextSetBit != UNKNOWN_UID) {
                dest.writeInt(nextSetBit);
            } else {
                return;
            }
        }
    }

    public int getAuthType() {
        if (this.allowedKeyManagement.cardinality() > WAPI_HEX_PASSWORD) {
            throw new IllegalStateException("More than one auth type set");
        } else if (this.allowedKeyManagement.get(WAPI_HEX_PASSWORD)) {
            return WAPI_HEX_PASSWORD;
        } else {
            if (this.allowedKeyManagement.get(INTERNET_UNRECOVERED)) {
                return INTERNET_UNRECOVERED;
            }
            if (this.allowedKeyManagement.get(USER_BANNED)) {
                return USER_BANNED;
            }
            if (this.allowedKeyManagement.get(USER_PENDING)) {
                return USER_PENDING;
            }
            if (this.allowedKeyManagement.get(6)) {
                return 6;
            }
            if (this.allowedKeyManagement.get(7)) {
                return 7;
            }
            if (this.allowedKeyManagement.get(8)) {
                return 8;
            }
            if (this.allowedKeyManagement.get(9)) {
                return 9;
            }
            return WAPI_ASCII_PASSWORD;
        }
    }

    public String configKey(boolean allowCached) {
        if (allowCached && this.mCachedConfigKey != null) {
            return this.mCachedConfigKey;
        }
        String key;
        if (this.providerFriendlyName != null) {
            if (this.FQDN != null) {
                key = this.FQDN + KeyMgmt.strings[USER_BANNED];
            } else {
                key = this.SSID + KeyMgmt.strings[USER_BANNED];
            }
            if (this.shared) {
                return key;
            }
            return key + "-" + Integer.toString(UserHandle.getUserId(this.creatorUid));
        }
        if (this.allowedKeyManagement.get(WAPI_HEX_PASSWORD)) {
            key = this.SSID + KeyMgmt.strings[WAPI_HEX_PASSWORD];
        } else if (this.allowedKeyManagement.get(USER_BANNED) || this.allowedKeyManagement.get(USER_PENDING)) {
            key = this.SSID + KeyMgmt.strings[USER_BANNED];
        } else if (this.wepKeys[WAPI_ASCII_PASSWORD] != null) {
            key = this.SSID + "WEP";
        } else if (this.allowedKeyManagement.get(6)) {
            key = this.SSID + KeyMgmt.strings[6];
        } else if (this.allowedKeyManagement.get(7)) {
            key = this.SSID + KeyMgmt.strings[7];
        } else {
            key = this.SSID + KeyMgmt.strings[WAPI_ASCII_PASSWORD];
        }
        if (!this.shared) {
            key = key + "-" + Integer.toString(UserHandle.getUserId(this.creatorUid));
        }
        this.mCachedConfigKey = key;
        return key;
    }

    public String configKey() {
        return configKey(HWFLOW);
    }

    public IpConfiguration getIpConfiguration() {
        return this.mIpConfiguration;
    }

    public void setIpConfiguration(IpConfiguration ipConfiguration) {
        this.mIpConfiguration = ipConfiguration;
    }

    public StaticIpConfiguration getStaticIpConfiguration() {
        if (this.mIpConfiguration != null) {
            return this.mIpConfiguration.getStaticIpConfiguration();
        }
        return null;
    }

    public void setStaticIpConfiguration(StaticIpConfiguration staticIpConfiguration) {
        if (this.mIpConfiguration != null) {
            this.mIpConfiguration.setStaticIpConfiguration(staticIpConfiguration);
        }
    }

    public IpAssignment getIpAssignment() {
        if (this.mIpConfiguration == null) {
            return null;
        }
        if (DBG) {
            Log.d(TAG, "Get IpAssignment (nid: " + this.networkId + ", SSID: " + this.SSID + ") IpAssignment: " + this.mIpConfiguration.ipAssignment.toString());
        }
        return this.mIpConfiguration.ipAssignment;
    }

    public void setIpAssignment(IpAssignment ipAssignment) {
        if (this.mIpConfiguration != null) {
            this.mIpConfiguration.ipAssignment = ipAssignment;
        }
    }

    public ProxySettings getProxySettings() {
        if (this.mIpConfiguration != null) {
            return this.mIpConfiguration.proxySettings;
        }
        return null;
    }

    public void setProxySettings(ProxySettings proxySettings) {
        if (this.mIpConfiguration != null) {
            this.mIpConfiguration.proxySettings = proxySettings;
        }
    }

    public ProxyInfo getHttpProxy() {
        if (this.mIpConfiguration != null) {
            return this.mIpConfiguration.httpProxy;
        }
        return null;
    }

    public void setHttpProxy(ProxyInfo httpProxy) {
        if (this.mIpConfiguration != null) {
            this.mIpConfiguration.httpProxy = httpProxy;
        }
    }

    public void setProxy(ProxySettings settings, ProxyInfo proxy) {
        if (this.mIpConfiguration != null) {
            this.mIpConfiguration.proxySettings = settings;
            this.mIpConfiguration.httpProxy = proxy;
        }
    }

    public int describeContents() {
        return WAPI_ASCII_PASSWORD;
    }

    public void setPasspointManagementObjectTree(String passpointManagementObjectTree) {
        this.mPasspointManagementObjectTree = passpointManagementObjectTree;
    }

    public String getMoTree() {
        return this.mPasspointManagementObjectTree;
    }

    public WifiConfiguration(WifiConfiguration source) {
        this.apBand = WAPI_ASCII_PASSWORD;
        this.apChannel = WAPI_ASCII_PASSWORD;
        this.dtimInterval = WAPI_ASCII_PASSWORD;
        this.userApproved = WAPI_ASCII_PASSWORD;
        this.roamingFailureBlackListTimeMilli = 1000;
        this.mNetworkSelectionStatus = new NetworkSelectionStatus();
        if (source != null) {
            this.noInternetAccess = source.noInternetAccess;
            this.internetHistory = source.internetHistory;
            this.portalNetwork = source.portalNetwork;
            this.lastDhcpResults = source.lastDhcpResults;
            this.internetSelfCureHistory = source.internetSelfCureHistory;
            this.portalCheckStatus = source.portalCheckStatus;
            this.poorRssiDectected = source.poorRssiDectected;
            this.consecutiveGoodRssiCounter = source.consecutiveGoodRssiCounter;
            this.lastHasInternetTimestamp = source.lastHasInternetTimestamp;
            this.internetRecoveryStatus = source.internetRecoveryStatus;
            this.internetRecoveryCheckTimestamp = source.internetRecoveryCheckTimestamp;
            this.wifiProNoHandoverNetwork = source.wifiProNoHandoverNetwork;
            this.wifiProNoInternetAccess = source.wifiProNoInternetAccess;
            this.wifiProNoInternetReason = source.wifiProNoInternetReason;
            this.internetAccessType = source.internetAccessType;
            this.networkQosLevel = source.networkQosLevel;
            this.networkQosScore = source.networkQosScore;
            this.isTempCreated = source.isTempCreated;
            this.isHiLinkNetwork = source.isHiLinkNetwork;
            this.networkId = source.networkId;
            this.status = source.status;
            this.SSID = source.SSID;
            this.oriSsid = source.oriSsid;
            this.cloudSecurityCheck = source.cloudSecurityCheck;
            this.BSSID = source.BSSID;
            this.FQDN = source.FQDN;
            this.roamingConsortiumIds = (long[]) source.roamingConsortiumIds.clone();
            this.providerFriendlyName = source.providerFriendlyName;
            this.preSharedKey = source.preSharedKey;
            this.mNetworkSelectionStatus.copy(source.getNetworkSelectionStatus());
            this.apBand = source.apBand;
            this.apChannel = source.apChannel;
            this.callingPid = source.callingPid;
            this.wepKeys = new String[INTERNET_UNRECOVERED];
            for (int i = WAPI_ASCII_PASSWORD; i < this.wepKeys.length; i += WAPI_HEX_PASSWORD) {
                this.wepKeys[i] = source.wepKeys[i];
            }
            this.wepTxKeyIndex = source.wepTxKeyIndex;
            this.priority = source.priority;
            this.hiddenSSID = source.hiddenSSID;
            this.allowedKeyManagement = (BitSet) source.allowedKeyManagement.clone();
            this.allowedProtocols = (BitSet) source.allowedProtocols.clone();
            this.allowedAuthAlgorithms = (BitSet) source.allowedAuthAlgorithms.clone();
            this.allowedPairwiseCiphers = (BitSet) source.allowedPairwiseCiphers.clone();
            this.allowedGroupCiphers = (BitSet) source.allowedGroupCiphers.clone();
            this.enterpriseConfig = new WifiEnterpriseConfig(source.enterpriseConfig);
            this.defaultGwMacAddress = source.defaultGwMacAddress;
            this.mIpConfiguration = new IpConfiguration(source.mIpConfiguration);
            if (source.linkedConfigurations != null && source.linkedConfigurations.size() > 0) {
                this.linkedConfigurations = new HashMap();
                this.linkedConfigurations.putAll(source.linkedConfigurations);
            }
            this.mCachedConfigKey = null;
            this.selfAdded = source.selfAdded;
            this.validatedInternetAccess = source.validatedInternetAccess;
            this.ephemeral = source.ephemeral;
            this.meteredHint = source.meteredHint;
            this.useExternalScores = source.useExternalScores;
            if (source.visibility != null) {
                this.visibility = new Visibility(source.visibility);
            }
            this.lastFailure = source.lastFailure;
            this.didSelfAdd = source.didSelfAdd;
            this.lastConnectUid = source.lastConnectUid;
            this.lastUpdateUid = source.lastUpdateUid;
            this.creatorUid = source.creatorUid;
            this.creatorName = source.creatorName;
            this.lastUpdateName = source.lastUpdateName;
            this.peerWifiConfiguration = source.peerWifiConfiguration;
            this.lastConnected = source.lastConnected;
            this.lastDisconnected = source.lastDisconnected;
            this.lastConnectionFailure = source.lastConnectionFailure;
            this.lastRoamingFailure = source.lastRoamingFailure;
            this.lastRoamingFailureReason = source.lastRoamingFailureReason;
            this.roamingFailureBlackListTimeMilli = source.roamingFailureBlackListTimeMilli;
            this.numScorerOverride = source.numScorerOverride;
            this.numScorerOverrideAndSwitchedNetwork = source.numScorerOverrideAndSwitchedNetwork;
            this.numAssociation = source.numAssociation;
            this.numUserTriggeredWifiDisableLowRSSI = source.numUserTriggeredWifiDisableLowRSSI;
            this.numUserTriggeredWifiDisableBadRSSI = source.numUserTriggeredWifiDisableBadRSSI;
            this.numUserTriggeredWifiDisableNotHighRSSI = source.numUserTriggeredWifiDisableNotHighRSSI;
            this.numTicksAtLowRSSI = source.numTicksAtLowRSSI;
            this.numTicksAtBadRSSI = source.numTicksAtBadRSSI;
            this.numTicksAtNotHighRSSI = source.numTicksAtNotHighRSSI;
            this.numUserTriggeredJoinAttempts = source.numUserTriggeredJoinAttempts;
            this.userApproved = source.userApproved;
            this.numNoInternetAccessReports = source.numNoInternetAccessReports;
            this.noInternetAccessExpected = source.noInternetAccessExpected;
            this.creationTime = source.creationTime;
            this.updateTime = source.updateTime;
            this.shared = source.shared;
            this.wapiAsCertBcm = source.wapiAsCertBcm;
            this.wapiUserCertBcm = source.wapiUserCertBcm;
            this.wapiCertIndexBcm = source.wapiCertIndexBcm;
            this.wapiPskTypeBcm = source.wapiPskTypeBcm;
            this.wapiPskTypeQualcomm = source.wapiPskTypeQualcomm;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = WAPI_HEX_PASSWORD;
        if (this.noInternetAccess) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        dest.writeString(this.internetHistory);
        if (this.portalNetwork) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        dest.writeString(this.lastDhcpResults);
        dest.writeString(this.internetSelfCureHistory);
        dest.writeInt(this.portalCheckStatus);
        if (this.poorRssiDectected) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        dest.writeInt(this.consecutiveGoodRssiCounter);
        dest.writeLong(this.lastHasInternetTimestamp);
        dest.writeInt(this.internetRecoveryStatus);
        dest.writeLong(this.internetRecoveryCheckTimestamp);
        if (this.wifiProNoHandoverNetwork) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        if (this.wifiProNoInternetAccess) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        dest.writeInt(this.wifiProNoInternetReason);
        dest.writeInt(this.internetAccessType);
        dest.writeInt(this.networkQosLevel);
        dest.writeInt(this.networkQosScore);
        if (this.isTempCreated) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        if (this.isHiLinkNetwork) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        dest.writeInt(this.networkId);
        dest.writeInt(this.status);
        this.mNetworkSelectionStatus.writeToParcel(dest);
        dest.writeString(this.SSID);
        dest.writeString(this.oriSsid);
        dest.writeInt(this.cloudSecurityCheck);
        dest.writeString(this.BSSID);
        dest.writeInt(this.apBand);
        dest.writeInt(this.apChannel);
        dest.writeString(this.FQDN);
        dest.writeString(this.providerFriendlyName);
        dest.writeInt(this.roamingConsortiumIds.length);
        long[] jArr = this.roamingConsortiumIds;
        int length = jArr.length;
        for (i = WAPI_ASCII_PASSWORD; i < length; i += WAPI_HEX_PASSWORD) {
            dest.writeLong(jArr[i]);
        }
        dest.writeString(this.preSharedKey);
        String[] strArr = this.wepKeys;
        length = strArr.length;
        for (i = WAPI_ASCII_PASSWORD; i < length; i += WAPI_HEX_PASSWORD) {
            dest.writeString(strArr[i]);
        }
        dest.writeInt(this.wepTxKeyIndex);
        dest.writeInt(this.priority);
        dest.writeInt(this.hiddenSSID ? WAPI_HEX_PASSWORD : WAPI_ASCII_PASSWORD);
        if (this.requirePMF) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        dest.writeString(this.updateIdentifier);
        writeBitSet(dest, this.allowedKeyManagement);
        writeBitSet(dest, this.allowedProtocols);
        writeBitSet(dest, this.allowedAuthAlgorithms);
        writeBitSet(dest, this.allowedPairwiseCiphers);
        writeBitSet(dest, this.allowedGroupCiphers);
        dest.writeInt(this.callingPid);
        dest.writeParcelable(this.enterpriseConfig, flags);
        dest.writeParcelable(this.mIpConfiguration, flags);
        dest.writeString(this.dhcpServer);
        dest.writeString(this.defaultGwMacAddress);
        dest.writeInt(this.selfAdded ? WAPI_HEX_PASSWORD : WAPI_ASCII_PASSWORD);
        if (this.didSelfAdd) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        if (this.validatedInternetAccess) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        if (this.ephemeral) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        if (this.meteredHint) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        if (this.useExternalScores) {
            i = WAPI_HEX_PASSWORD;
        } else {
            i = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i);
        dest.writeInt(this.creatorUid);
        dest.writeInt(this.lastConnectUid);
        dest.writeInt(this.lastUpdateUid);
        dest.writeString(this.creatorName);
        dest.writeString(this.lastUpdateName);
        dest.writeLong(this.lastConnectionFailure);
        dest.writeLong(this.lastRoamingFailure);
        dest.writeInt(this.lastRoamingFailureReason);
        dest.writeLong(this.roamingFailureBlackListTimeMilli);
        dest.writeInt(this.numScorerOverride);
        dest.writeInt(this.numScorerOverrideAndSwitchedNetwork);
        dest.writeInt(this.numAssociation);
        dest.writeInt(this.numUserTriggeredWifiDisableLowRSSI);
        dest.writeInt(this.numUserTriggeredWifiDisableBadRSSI);
        dest.writeInt(this.numUserTriggeredWifiDisableNotHighRSSI);
        dest.writeInt(this.numTicksAtLowRSSI);
        dest.writeInt(this.numTicksAtBadRSSI);
        dest.writeInt(this.numTicksAtNotHighRSSI);
        dest.writeInt(this.numUserTriggeredJoinAttempts);
        dest.writeInt(this.userApproved);
        dest.writeInt(this.numNoInternetAccessReports);
        dest.writeInt(this.noInternetAccessExpected ? WAPI_HEX_PASSWORD : WAPI_ASCII_PASSWORD);
        if (!this.shared) {
            i2 = WAPI_ASCII_PASSWORD;
        }
        dest.writeInt(i2);
        dest.writeString(this.mPasspointManagementObjectTree);
        dest.writeString(this.wapiAsCertQualcomm);
        dest.writeString(this.wapiUserCertQualcomm);
        dest.writeString(this.wapiPskQualcomm);
        dest.writeInt(this.wapiPskTypeQualcomm);
        dest.writeInt(this.wapiPskTypeBcm);
        dest.writeString(this.wapiAsCertBcm);
        dest.writeString(this.wapiUserCertBcm);
        dest.writeInt(this.wapiCertIndexBcm);
    }

    public byte[] getBytesForBackup() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeInt(USER_BANNED);
        BackupUtils.writeString(out, this.SSID);
        out.writeInt(this.apBand);
        out.writeInt(this.apChannel);
        BackupUtils.writeString(out, this.preSharedKey);
        out.writeInt(getAuthType());
        return baos.toByteArray();
    }

    public static WifiConfiguration getWifiConfigFromBackup(DataInputStream in) throws IOException, BadVersionException {
        WifiConfiguration config = new WifiConfiguration();
        int version = in.readInt();
        if (version < WAPI_HEX_PASSWORD || version > USER_BANNED) {
            throw new BadVersionException("Unknown Backup Serialization Version");
        } else if (version == WAPI_HEX_PASSWORD) {
            return null;
        } else {
            config.SSID = BackupUtils.readString(in);
            config.apBand = in.readInt();
            config.apChannel = in.readInt();
            config.preSharedKey = BackupUtils.readString(in);
            config.allowedKeyManagement.set(in.readInt());
            return config;
        }
    }
}
