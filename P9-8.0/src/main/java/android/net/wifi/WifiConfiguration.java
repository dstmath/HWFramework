package android.net.wifi;

import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.Uri;
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
    public static final Creator<WifiConfiguration> CREATOR = new Creator<WifiConfiguration>() {
        public WifiConfiguration createFromParcel(Parcel in) {
            boolean z;
            int i;
            boolean z2 = true;
            WifiConfiguration config = new WifiConfiguration();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.noInternetAccess = z;
            config.internetHistory = in.readString();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.portalNetwork = z;
            config.lastDhcpResults = in.readString();
            config.internetSelfCureHistory = in.readString();
            config.portalCheckStatus = in.readInt();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.poorRssiDectected = z;
            config.consecutiveGoodRssiCounter = in.readInt();
            config.lastHasInternetTimestamp = in.readLong();
            config.lastTrySwitchWifiTimestamp = in.readLong();
            config.internetRecoveryStatus = in.readInt();
            config.internetRecoveryCheckTimestamp = in.readLong();
            config.rssiStatusDisabled = in.readInt();
            config.lastConnFailedType = in.readInt();
            config.lastConnFailedTimestamp = in.readLong();
            config.rssiDiscNonLocally = in.readInt();
            config.timestampDiscNonLocally = in.readLong();
            config.wifiProNoHandoverNetwork = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.wifiProNoInternetAccess = z;
            config.wifiProNoInternetReason = in.readInt();
            config.internetAccessType = in.readInt();
            config.networkQosLevel = in.readInt();
            config.networkQosScore = in.readInt();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.isTempCreated = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.isHiLinkNetwork = z;
            config.networkId = in.readInt();
            config.status = in.readInt();
            config.mNetworkSelectionStatus.readFromParcel(in);
            config.SSID = in.readString();
            config.oriSsid = in.readString();
            config.cloudSecurityCheck = in.readInt();
            config.BSSID = in.readString();
            config.apBand = in.readInt();
            config.apChannel = in.readInt();
            config.FQDN = in.readString();
            config.providerFriendlyName = in.readString();
            config.isHomeProviderNetwork = in.readInt() != 0;
            int numRoamingConsortiumIds = in.readInt();
            config.roamingConsortiumIds = new long[numRoamingConsortiumIds];
            for (i = 0; i < numRoamingConsortiumIds; i++) {
                config.roamingConsortiumIds[i] = in.readLong();
            }
            config.preSharedKey = in.readString();
            for (i = 0; i < config.wepKeys.length; i++) {
                config.wepKeys[i] = in.readString();
            }
            config.wepTxKeyIndex = in.readInt();
            config.priority = in.readInt();
            config.hiddenSSID = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.requirePMF = z;
            config.updateIdentifier = in.readString();
            config.allowedKeyManagement = WifiConfiguration.readBitSet(in);
            config.allowedProtocols = WifiConfiguration.readBitSet(in);
            config.allowedAuthAlgorithms = WifiConfiguration.readBitSet(in);
            config.allowedPairwiseCiphers = WifiConfiguration.readBitSet(in);
            config.allowedGroupCiphers = WifiConfiguration.readBitSet(in);
            config.callingPid = in.readInt();
            config.callingPackage = in.readString();
            config.enterpriseConfig = (WifiEnterpriseConfig) in.readParcelable(null);
            config.mIpConfiguration = (IpConfiguration) in.readParcelable(null);
            config.dhcpServer = in.readString();
            config.defaultGwMacAddress = in.readString();
            config.selfAdded = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.didSelfAdd = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.validatedInternetAccess = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.isLegacyPasspointConfig = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.ephemeral = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.meteredHint = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.meteredOverride = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.useExternalScores = z;
            config.creatorUid = in.readInt();
            config.lastConnectUid = in.readInt();
            config.lastUpdateUid = in.readInt();
            config.creatorName = in.readString();
            config.lastUpdateName = in.readString();
            config.lastConnectionFailure = in.readLong();
            config.lastRoamingFailure = in.readLong();
            config.lastRoamingFailureReason = in.readInt();
            config.roamingFailureBlackListTimeMilli = in.readLong();
            config.numScorerOverride = in.readInt();
            config.numScorerOverrideAndSwitchedNetwork = in.readInt();
            config.numAssociation = in.readInt();
            config.userApproved = in.readInt();
            config.numNoInternetAccessReports = in.readInt();
            config.noInternetAccessExpected = in.readInt() != 0;
            if (in.readInt() == 0) {
                z2 = false;
            }
            config.shared = z2;
            config.mPasspointManagementObjectTree = in.readString();
            config.wapiAsCertQualcomm = in.readString();
            config.wapiUserCertQualcomm = in.readString();
            config.wapiPskQualcomm = in.readString();
            config.wapiPskTypeQualcomm = in.readInt();
            config.wapiPskTypeBcm = in.readInt();
            config.wapiAsCertBcm = in.readString();
            config.wapiUserCertBcm = in.readString();
            config.wapiCertIndexBcm = in.readInt();
            return config;
        }

        public WifiConfiguration[] newArray(int size) {
            return new WifiConfiguration[size];
        }
    };
    private static boolean DBG = HWFLOW;
    public static final int HILINK_CONNECTED_BACKGROUND = 6;
    public static final int HOME_NETWORK_RSSI_BOOST = 5;
    private static final boolean HWFLOW;
    public static final int INTERNET_RECOVERED = 5;
    public static final int INTERNET_UNKNOWN = 3;
    public static final int INTERNET_UNRECOVERED = 4;
    public static final int INVALID_NETWORK_ID = -1;
    public static int INVALID_RSSI = -127;
    public static final int LOCAL_ONLY_NETWORK_ID = -2;
    public static final int PORTAL_AP_UNAUTHORIZED = 1;
    public static final int PORTAL_HAS_INTERNET = 1;
    public static final int PORTAL_UNAUTHEN = 2;
    public static final int PORTAL_UNKNOWN = 0;
    public static final int QOS_LEVEL_GOOD = 3;
    public static final int QOS_LEVEL_NORMAL = 2;
    public static final int QOS_LEVEL_POOR = 1;
    public static final int QOS_LEVEL_UNKOWN = 0;
    public static int ROAMING_FAILURE_AUTH_FAILURE = 2;
    public static int ROAMING_FAILURE_IP_CONFIG = 1;
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
    public static final String[] wepKeyVarNames = new String[]{"wep_key0", "wep_key1", "wep_key2", "wep_key3"};
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
    public String callingPackage;
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
    public boolean isHomeProviderNetwork;
    public boolean isLegacyPasspointConfig;
    public boolean isTempCreated;
    public long lastConnFailedTimestamp;
    public int lastConnFailedType;
    public int lastConnectUid;
    public long lastConnected;
    public long lastConnectionFailure;
    public String lastDhcpResults;
    public long lastDisconnected;
    public String lastFailure;
    public long lastHasInternetTimestamp;
    public long lastRoamingFailure;
    public int lastRoamingFailureReason;
    public long lastTrySwitchWifiTimestamp;
    public String lastUpdateName;
    public int lastUpdateUid;
    public HashMap<String, Integer> linkedConfigurations;
    String mCachedConfigKey;
    private IpConfiguration mIpConfiguration;
    private NetworkSelectionStatus mNetworkSelectionStatus;
    private String mPasspointManagementObjectTree;
    public boolean meteredHint;
    public boolean meteredOverride;
    public int networkId;
    public int networkQosLevel;
    public int networkQosScore;
    public boolean noInternetAccess;
    public boolean noInternetAccessExpected;
    public int numAssociation;
    public int numNoInternetAccessReports;
    public int numScorerOverride;
    public int numScorerOverrideAndSwitchedNetwork;
    public String oriSsid;
    public String peerWifiConfiguration;
    public boolean poorRssiDectected;
    public int portalCheckStatus;
    public boolean portalNetwork;
    public String preSharedKey;
    @Deprecated
    public int priority;
    public String providerFriendlyName;
    public boolean requirePMF;
    public long[] roamingConsortiumIds;
    public long roamingFailureBlackListTimeMilli;
    public int rssiDiscNonLocally;
    public int rssiStatusDisabled;
    public boolean selfAdded;
    public boolean shared;
    public int status;
    public long timestampDiscNonLocally;
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
        public static final String[] strings = new String[]{"OPEN", "SHARED", "LEAP"};
        public static final String varName = "auth_alg";

        private AuthAlgorithm() {
        }
    }

    public static class BcmWapi {
        public static final String wapiAsCertVarName = "wapi_as_cert";
        public static final String wapiCertIndexVarName = "cert_index";
        public static final String wapiPskTypeVarName = "psk_key_type";
        public static final String wapiUserCertVarName = "wapi_user_cert";
    }

    public static class GroupCipher {
        public static final int CCMP = 3;
        public static final int GTK_NOT_USED = 4;
        public static final int TKIP = 2;
        public static final int WEP104 = 1;
        public static final int WEP40 = 0;
        public static final String[] strings = new String[]{"WEP40", "WEP104", "TKIP", "CCMP", "SMS4", "GTK_NOT_USED"};
        public static final String varName = "group";

        private GroupCipher() {
        }
    }

    public static class KeyMgmt {
        public static final int BCM_WAPI_CERT = 9;
        public static final int BCM_WAPI_PSK = 8;
        public static final int FT_EAP = 7;
        public static final int FT_PSK = 6;
        public static final int IEEE8021X = 3;
        public static final int NONE = 0;
        public static final int OSEN = 5;
        public static final int QUALCOMM_WAPI_CERT = 11;
        public static final int QUALCOMM_WAPI_PSK = 10;
        public static final int WPA2_PSK = 4;
        public static final int WPA_EAP = 2;
        public static final int WPA_PSK = 1;
        public static final String[] strings = new String[]{"NONE", "WPA_PSK", "WPA_EAP", "IEEE8021X", "WPA2_PSK", "OSEN", "FT_PSK", "FT_EAP", "WAPI_PSK", "WAPI_CERT", "QUALCOMM_WAPI_PSK", "QUALCOMM_WAPI_CERT"};
        public static final String varName = "key_mgmt";

        private KeyMgmt() {
        }
    }

    public static class NetworkSelectionStatus {
        private static final int CONNECT_CHOICE_EXISTS = 1;
        private static final int CONNECT_CHOICE_NOT_EXISTS = -1;
        public static final int DISABLED_ASSOCIATION_REJECTION = 2;
        public static final int DISABLED_AUTHENTICATION_FAILURE = 3;
        public static final int DISABLED_AUTHENTICATION_NO_CREDENTIALS = 8;
        public static final int DISABLED_BAD_LINK = 1;
        public static final int DISABLED_BY_SYSTEM = 13;
        public static final int DISABLED_BY_WIFI_MANAGER = 10;
        public static final int DISABLED_BY_WRONG_PASSWORD = 12;
        public static final int DISABLED_DHCP_FAILURE = 4;
        public static final int DISABLED_DISASSOC_REASON = 15;
        public static final int DISABLED_DNS_FAILURE = 5;
        public static final int DISABLED_DUE_TO_USER_SWITCH = 11;
        public static final int DISABLED_EAP_AKA_FAILURE = 14;
        public static final int DISABLED_NO_INTERNET = 9;
        public static final int DISABLED_TLS_VERSION_MISMATCH = 7;
        public static final int DISABLED_UNKNOWN_REASON = -1;
        public static final int DISABLED_WPS_START = 6;
        public static final long INVALID_NETWORK_SELECTION_DISABLE_TIMESTAMP = -1;
        public static final int NETWORK_SELECTION_DISABLED_MAX = 16;
        public static final int NETWORK_SELECTION_DISABLED_STARTING_INDEX = 1;
        public static final int NETWORK_SELECTION_ENABLE = 0;
        public static final int NETWORK_SELECTION_ENABLED = 0;
        public static final int NETWORK_SELECTION_PERMANENTLY_DISABLED = 2;
        public static final int NETWORK_SELECTION_STATUS_MAX = 3;
        public static final int NETWORK_SELECTION_TEMPORARY_DISABLED = 1;
        public static final String[] QUALITY_NETWORK_SELECTION_DISABLE_REASON = new String[]{"NETWORK_SELECTION_ENABLE", "NETWORK_SELECTION_DISABLED_BAD_LINK", "NETWORK_SELECTION_DISABLED_ASSOCIATION_REJECTION ", "NETWORK_SELECTION_DISABLED_AUTHENTICATION_FAILURE", "NETWORK_SELECTION_DISABLED_DHCP_FAILURE", "NETWORK_SELECTION_DISABLED_DNS_FAILURE", "NETWORK_SELECTION_DISABLED_WPS_START", "NETWORK_SELECTION_DISABLED_TLS_VERSION", "NETWORK_SELECTION_DISABLED_AUTHENTICATION_NO_CREDENTIALS", "NETWORK_SELECTION_DISABLED_NO_INTERNET", "NETWORK_SELECTION_DISABLED_BY_WIFI_MANAGER", "NETWORK_SELECTION_DISABLED_BY_USER_SWITCH", "NETWORK_SELECTION_DISABLED_BY_WRONG_PASSWORD", "NETWORK_SELECTION_DISABLED_BY_SYSTEM", "NETWORK_SELECTION_DISABLED_EAP_FAILURE", "NETWORK_SELECTION_DISABLED_DISASSOC_REASON"};
        public static final String[] QUALITY_NETWORK_SELECTION_STATUS = new String[]{"NETWORK_SELECTION_ENABLED", "NETWORK_SELECTION_TEMPORARY_DISABLED", "NETWORK_SELECTION_PERMANENTLY_DISABLED"};
        private ScanResult mCandidate;
        private int mCandidateScore;
        private String mConnectChoice;
        private long mConnectChoiceTimestamp = -1;
        private boolean mHasEverConnected = false;
        private int[] mNetworkSeclectionDisableCounter = new int[16];
        private String mNetworkSelectionBSSID;
        private String mNetworkSelectionDisableName;
        private int mNetworkSelectionDisableReason;
        private boolean mNotRecommended;
        private boolean mSeenInLastQualifiedNetworkSelection;
        private int mStatus;
        private long mTemporarilyDisabledTimestamp = -1;

        public void setNotRecommended(boolean notRecommended) {
            this.mNotRecommended = notRecommended;
        }

        public boolean isNotRecommended() {
            return this.mNotRecommended;
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

        public static String getNetworkDisableReasonString(int reason) {
            if (reason < 0 || reason >= 16) {
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
            return this.mStatus == 0;
        }

        public boolean isNetworkTemporaryDisabled() {
            return this.mStatus == 1;
        }

        public boolean isNetworkPermanentlyDisabled() {
            return this.mStatus == 2;
        }

        public void setNetworkSelectionStatus(int status) {
            if (status >= 0 && status < 3) {
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
            if (reason < 0 || reason >= 16) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            this.mNetworkSelectionDisableReason = reason;
        }

        public boolean isDisabledByReason(int reason) {
            return this.mNetworkSelectionDisableReason == reason;
        }

        public void setDisableTime(long timeStamp) {
            this.mTemporarilyDisabledTimestamp = timeStamp;
        }

        public long getDisableTime() {
            return this.mTemporarilyDisabledTimestamp;
        }

        public int getDisableReasonCounter(int reason) {
            if (reason >= 0 && reason < 16) {
                return this.mNetworkSeclectionDisableCounter[reason];
            }
            throw new IllegalArgumentException("Illegal reason value: " + reason);
        }

        public void setDisableReasonCounter(int reason, int value) {
            if (reason < 0 || reason >= 16) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            this.mNetworkSeclectionDisableCounter[reason] = value;
        }

        public void incrementDisableReasonCounter(int reason) {
            if (reason < 0 || reason >= 16) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            int[] iArr = this.mNetworkSeclectionDisableCounter;
            iArr[reason] = iArr[reason] + 1;
        }

        public void clearDisableReasonCounter(int reason) {
            if (reason < 0 || reason >= 16) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            this.mNetworkSeclectionDisableCounter[reason] = 0;
        }

        public void clearDisableReasonCounter() {
            Arrays.fill(this.mNetworkSeclectionDisableCounter, 0);
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
            for (int index = 0; index < 16; index++) {
                this.mNetworkSeclectionDisableCounter[index] = source.mNetworkSeclectionDisableCounter[index];
            }
            this.mTemporarilyDisabledTimestamp = source.mTemporarilyDisabledTimestamp;
            this.mNetworkSelectionBSSID = source.mNetworkSelectionBSSID;
            setSeenInLastQualifiedNetworkSelection(source.getSeenInLastQualifiedNetworkSelection());
            setCandidate(source.getCandidate());
            setCandidateScore(source.getCandidateScore());
            setConnectChoice(source.getConnectChoice());
            setConnectChoiceTimestamp(source.getConnectChoiceTimestamp());
            setHasEverConnected(source.getHasEverConnected());
            setNetworkSelectionDisableName(source.getNetworkSelectionDisableName());
            setNotRecommended(source.isNotRecommended());
        }

        public void writeToParcel(Parcel dest) {
            int i;
            int i2 = 1;
            dest.writeInt(getNetworkSelectionStatus());
            dest.writeInt(getNetworkSelectionDisableReason());
            for (int index = 0; index < 16; index++) {
                dest.writeInt(getDisableReasonCounter(index));
            }
            dest.writeLong(getDisableTime());
            dest.writeString(getNetworkSelectionBSSID());
            if (getConnectChoice() != null) {
                dest.writeInt(1);
                dest.writeString(getConnectChoice());
                dest.writeLong(getConnectChoiceTimestamp());
            } else {
                dest.writeInt(-1);
            }
            if (getHasEverConnected()) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeString(getNetworkSelectionDisableName());
            if (!isNotRecommended()) {
                i2 = 0;
            }
            dest.writeInt(i2);
        }

        public void readFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            setNetworkSelectionStatus(in.readInt());
            setNetworkSelectionDisableReason(in.readInt());
            for (int index = 0; index < 16; index++) {
                setDisableReasonCounter(index, in.readInt());
            }
            setDisableTime(in.readLong());
            setNetworkSelectionBSSID(in.readString());
            if (in.readInt() == 1) {
                setConnectChoice(in.readString());
                setConnectChoiceTimestamp(in.readLong());
            } else {
                setConnectChoice(null);
                setConnectChoiceTimestamp(-1);
            }
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            setHasEverConnected(z);
            setNetworkSelectionDisableName(in.readString());
            if (in.readInt() == 0) {
                z2 = false;
            }
            setNotRecommended(z2);
        }
    }

    public static class PairwiseCipher {
        public static final int CCMP = 2;
        public static final int NONE = 0;
        public static final int TKIP = 1;
        public static final String[] strings = new String[]{"NONE", "TKIP", "CCMP", "SMS4"};
        public static final String varName = "pairwise";

        private PairwiseCipher() {
        }
    }

    public static class Protocol {
        public static final int OSEN = 2;
        public static final int RSN = 1;
        public static final int WAPI = 3;
        public static final int WPA = 0;
        public static final String[] strings = new String[]{"WPA", "RSN", "OSEN", "WAPI"};
        public static final String varName = "proto";

        private Protocol() {
        }
    }

    public static class QualcommWapi {
        public static final String wapiAsCertVarName = "as_cert_file";
        public static final String wapiPskTypeVarName = "wapi_key_type";
        public static final String wapiPskVarName = "wapi_psk";
        public static final String wapiUserCertVarName = "user_cert_file";
    }

    public static class Status {
        public static final int CURRENT = 0;
        public static final int DISABLED = 1;
        public static final int ENABLED = 2;
        public static final String[] strings = new String[]{"current", "disabled", "enabled"};

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
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public void setVisibility(Visibility status) {
        this.visibility = status;
    }

    public boolean hasNoInternetAccess() {
        return this.numNoInternetAccessReports > 0 ? this.validatedInternetAccess ^ 1 : false;
    }

    public boolean isNoInternetAccessExpected() {
        return this.noInternetAccessExpected;
    }

    public boolean isEphemeral() {
        return this.ephemeral;
    }

    public NetworkSelectionStatus getNetworkSelectionStatus() {
        return this.mNetworkSelectionStatus;
    }

    public void setNetworkSelectionStatus(NetworkSelectionStatus status) {
        this.mNetworkSelectionStatus = status;
    }

    public WifiConfiguration() {
        this.apBand = 0;
        this.apChannel = 0;
        this.dtimInterval = 0;
        this.isLegacyPasspointConfig = false;
        this.userApproved = 0;
        this.roamingFailureBlackListTimeMilli = 1000;
        this.mNetworkSelectionStatus = new NetworkSelectionStatus();
        this.networkId = -1;
        this.SSID = null;
        this.BSSID = null;
        this.FQDN = null;
        this.roamingConsortiumIds = new long[0];
        this.priority = 0;
        this.hiddenSSID = false;
        this.allowedKeyManagement = new BitSet();
        this.allowedProtocols = new BitSet();
        this.allowedAuthAlgorithms = new BitSet();
        this.allowedPairwiseCiphers = new BitSet();
        this.allowedGroupCiphers = new BitSet();
        this.wepKeys = new String[4];
        for (int i = 0; i < this.wepKeys.length; i++) {
            this.wepKeys[i] = null;
        }
        this.callingPid = 0;
        this.callingPackage = null;
        this.enterpriseConfig = new WifiEnterpriseConfig();
        this.selfAdded = false;
        this.didSelfAdd = false;
        this.ephemeral = false;
        this.meteredHint = false;
        this.meteredOverride = false;
        this.useExternalScores = false;
        this.validatedInternetAccess = false;
        this.mIpConfiguration = new IpConfiguration();
        this.lastUpdateUid = -1;
        this.creatorUid = -1;
        this.shared = true;
        this.dtimInterval = 0;
        this.wapiAsCertBcm = null;
        this.wapiUserCertBcm = null;
        this.wapiCertIndexBcm = -1;
        this.wapiPskTypeBcm = -1;
        this.wapiPskTypeQualcomm = 0;
        this.noInternetAccess = false;
        this.internetHistory = "-1/-1/-1/-1/-1/-1/-1/-1/-1/-1";
        this.portalNetwork = false;
        this.lastDhcpResults = ProxyInfo.LOCAL_EXCL_LIST;
        this.internetSelfCureHistory = ProxyInfo.LOCAL_EXCL_LIST;
        this.portalCheckStatus = 0;
        this.poorRssiDectected = false;
        this.lastHasInternetTimestamp = 0;
        this.lastTrySwitchWifiTimestamp = 0;
        this.internetRecoveryStatus = 3;
        this.internetRecoveryCheckTimestamp = 0;
        this.rssiStatusDisabled = -200;
        this.lastConnFailedType = 0;
        this.lastConnFailedTimestamp = 0;
        this.consecutiveGoodRssiCounter = 0;
        this.rssiDiscNonLocally = 0;
        this.timestampDiscNonLocally = 0;
        this.wifiProNoInternetAccess = false;
        this.wifiProNoHandoverNetwork = false;
        this.wifiProNoInternetReason = 0;
        this.internetAccessType = 0;
        this.networkQosLevel = 0;
        this.networkQosScore = 0;
        this.isTempCreated = false;
        this.isHiLinkNetwork = false;
    }

    public boolean isPasspoint() {
        if (TextUtils.isEmpty(this.FQDN) || (TextUtils.isEmpty(this.providerFriendlyName) ^ 1) == 0 || this.enterpriseConfig == null || this.enterpriseConfig.getEapMethod() == -1) {
            return false;
        }
        return true;
    }

    public boolean isSsidValid() {
        if (this.SSID != null && this.SSID.startsWith("\"") && this.SSID.endsWith("\"")) {
            return true;
        }
        return false;
    }

    public void defendSsid() {
        if (!isSsidValid()) {
            this.SSID = "\"" + this.SSID + "\"";
        }
    }

    public boolean isLinked(WifiConfiguration config) {
        if (config == null || config.linkedConfigurations == null || this.linkedConfigurations == null || config.linkedConfigurations.get(configKey()) == null || this.linkedConfigurations.get(config.configKey()) == null) {
            return false;
        }
        return true;
    }

    public boolean isEnterprise() {
        if ((!this.allowedKeyManagement.get(2) && !this.allowedKeyManagement.get(3)) || this.enterpriseConfig == null || this.enterpriseConfig.getEapMethod() == -1) {
            return false;
        }
        return true;
    }

    public String toString() {
        long diff;
        StringBuilder sbuf = new StringBuilder();
        if (this.status == 0) {
            sbuf.append("* ");
        } else if (this.status == 1) {
            sbuf.append("- DSBLE ");
        }
        sbuf.append("ID: ").append(this.networkId).append(" SSID: ").append(this.SSID).append(" PROVIDER-NAME: ").append(this.providerFriendlyName).append(" BSSID: ").append(this.BSSID).append(" FQDN: ").append(this.FQDN).append(" PRIO: ").append(this.priority).append(" HIDDEN: ").append(this.hiddenSSID).append(10);
        sbuf.append(" NetworkSelectionStatus ").append(this.mNetworkSelectionStatus.getNetworkStatusString()).append("\n");
        if (this.mNetworkSelectionStatus.getNetworkSelectionDisableReason() > 0) {
            sbuf.append(" mNetworkSelectionDisableReason ").append(this.mNetworkSelectionStatus.getNetworkDisableReasonString()).append("\n");
            if (this.mNetworkSelectionStatus.getNetworkSelectionDisableName() != null) {
                sbuf.append(" networkSelectionDisableName ").append(this.mNetworkSelectionStatus.getNetworkSelectionDisableName()).append("\n");
            }
            for (int index = 0; index < 16; index++) {
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
        if (this.meteredOverride) {
            sbuf.append(" meteredOverride");
        }
        if (this.useExternalScores) {
            sbuf.append(" useExternalScores");
        }
        if (this.didSelfAdd || this.selfAdded || this.validatedInternetAccess || this.ephemeral || this.meteredHint || this.meteredOverride || this.useExternalScores) {
            sbuf.append("\n");
        }
        sbuf.append(" KeyMgmt:");
        for (int k = 0; k < this.allowedKeyManagement.size(); k++) {
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
        for (int p = 0; p < this.allowedProtocols.size(); p++) {
            if (this.allowedProtocols.get(p)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (p < Protocol.strings.length) {
                    sbuf.append(Protocol.strings[p]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append(10);
        sbuf.append(" AuthAlgorithms:");
        for (int a = 0; a < this.allowedAuthAlgorithms.size(); a++) {
            if (this.allowedAuthAlgorithms.get(a)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (a < AuthAlgorithm.strings.length) {
                    sbuf.append(AuthAlgorithm.strings[a]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append(10);
        sbuf.append(" PairwiseCiphers:");
        for (int pc = 0; pc < this.allowedPairwiseCiphers.size(); pc++) {
            if (this.allowedPairwiseCiphers.get(pc)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (pc < PairwiseCipher.strings.length) {
                    sbuf.append(PairwiseCipher.strings[pc]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append(10);
        sbuf.append(" GroupCiphers:");
        for (int gc = 0; gc < this.allowedGroupCiphers.size(); gc++) {
            if (this.allowedGroupCiphers.get(gc)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (gc < GroupCipher.strings.length) {
                    sbuf.append(GroupCipher.strings[gc]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append(10).append(" PSK: ");
        if (this.preSharedKey != null) {
            sbuf.append('*');
        }
        sbuf.append(10);
        if (this.wapiAsCertBcm != null) {
            sbuf.append(" wapiAsCertBcm: ").append(this.wapiAsCertBcm);
        }
        sbuf.append(10);
        if (this.wapiUserCertBcm != null) {
            sbuf.append(" WapiUserCertBcm: ").append(this.wapiUserCertBcm);
        }
        sbuf.append(10);
        if (this.wapiCertIndexBcm != -1) {
            sbuf.append(" WapiCertIndexBcm: ").append(this.wapiCertIndexBcm);
        }
        sbuf.append(10);
        if (this.wapiPskTypeBcm != -1) {
            sbuf.append(" WapiPskTypeBcm: ").append(this.wapiPskTypeBcm);
        }
        sbuf.append(10);
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
            sbuf.append(10);
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
            sbuf.append(10);
            diff = now_ms - this.lastConnected;
            if (diff <= 0) {
                sbuf.append("lastConnected since <incorrect>");
            } else {
                sbuf.append("lastConnected: ").append(Long.toString(diff / 1000)).append("sec ");
            }
        }
        if (this.lastConnectionFailure != 0) {
            sbuf.append(10);
            diff = now_ms - this.lastConnectionFailure;
            if (diff <= 0) {
                sbuf.append("lastConnectionFailure since <incorrect> ");
            } else {
                sbuf.append("lastConnectionFailure: ").append(Long.toString(diff / 1000));
                sbuf.append("sec ");
            }
        }
        if (this.lastRoamingFailure != 0) {
            sbuf.append(10);
            diff = now_ms - this.lastRoamingFailure;
            if (diff <= 0) {
                sbuf.append("lastRoamingFailure since <incorrect> ");
            } else {
                sbuf.append("lastRoamingFailure: ").append(Long.toString(diff / 1000));
                sbuf.append("sec ");
            }
        }
        sbuf.append("roamingFailureBlackListTimeMilli: ").append(Long.toString(this.roamingFailureBlackListTimeMilli));
        sbuf.append(10);
        if (this.linkedConfigurations != null) {
            for (String key : this.linkedConfigurations.keySet()) {
                sbuf.append(" linked: ").append(key);
                sbuf.append(10);
            }
        }
        return sbuf.toString();
    }

    public String getPrintableSsid() {
        if (this.SSID == null) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        int length = this.SSID.length();
        if (length > 2 && this.SSID.charAt(0) == '\"' && this.SSID.charAt(length - 1) == '\"') {
            return this.SSID.substring(1, length - 1);
        }
        if (length > 3 && this.SSID.charAt(0) == 'P' && this.SSID.charAt(1) == '\"' && this.SSID.charAt(length - 1) == '\"') {
            return WifiSsid.createFromAsciiEncoded(this.SSID.substring(2, length - 1)).toString();
        }
        return this.SSID;
    }

    public static String userApprovedAsString(int userApproved) {
        switch (userApproved) {
            case 0:
                return "USER_UNSPECIFIED";
            case 1:
                return "USER_APPROVED";
            case 2:
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
            if (this.allowedKeyManagement.get(2)) {
                keyMgmt = KeyMgmt.strings[2];
            }
            if (this.allowedKeyManagement.get(5)) {
                keyMgmt = KeyMgmt.strings[5];
            }
            if (this.allowedKeyManagement.get(3)) {
                keyMgmt = keyMgmt + KeyMgmt.strings[3];
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
        for (int i = 0; i < cardinality; i++) {
            set.set(src.readInt());
        }
        return set;
    }

    private static void writeBitSet(Parcel dest, BitSet set) {
        int nextSetBit = -1;
        dest.writeInt(set.cardinality());
        while (true) {
            nextSetBit = set.nextSetBit(nextSetBit + 1);
            if (nextSetBit != -1) {
                dest.writeInt(nextSetBit);
            } else {
                return;
            }
        }
    }

    public int getAuthType() {
        if (this.allowedKeyManagement.cardinality() > 1) {
            throw new IllegalStateException("More than one auth type set");
        } else if (this.allowedKeyManagement.get(1)) {
            return 1;
        } else {
            if (this.allowedKeyManagement.get(4)) {
                return 4;
            }
            if (this.allowedKeyManagement.get(2)) {
                return 2;
            }
            if (this.allowedKeyManagement.get(3)) {
                return 3;
            }
            if (this.allowedKeyManagement.get(8)) {
                return 8;
            }
            if (this.allowedKeyManagement.get(9)) {
                return 9;
            }
            if (this.allowedKeyManagement.get(10)) {
                return 10;
            }
            if (this.allowedKeyManagement.get(11)) {
                return 11;
            }
            if (this.allowedKeyManagement.get(6)) {
                return 6;
            }
            if (this.allowedKeyManagement.get(7)) {
                return 7;
            }
            return 0;
        }
    }

    public String configKey(boolean allowCached) {
        if (allowCached && this.mCachedConfigKey != null) {
            return this.mCachedConfigKey;
        }
        String key;
        if (this.providerFriendlyName != null) {
            if (this.FQDN != null) {
                key = this.FQDN + KeyMgmt.strings[2];
            } else {
                key = this.SSID + KeyMgmt.strings[2];
            }
            if (this.shared) {
                return key;
            }
            return key + "-" + Integer.toString(UserHandle.getUserId(this.creatorUid));
        }
        if (this.allowedKeyManagement.get(1)) {
            key = this.SSID + KeyMgmt.strings[1];
        } else if (this.allowedKeyManagement.get(2) || this.allowedKeyManagement.get(3)) {
            key = this.SSID + KeyMgmt.strings[2];
        } else if (this.wepKeys[0] != null) {
            key = this.SSID + "WEP";
        } else if (this.allowedKeyManagement.get(8)) {
            key = this.SSID + KeyMgmt.strings[8];
        } else if (this.allowedKeyManagement.get(9)) {
            key = this.SSID + KeyMgmt.strings[9];
        } else if (this.allowedKeyManagement.get(10)) {
            key = this.SSID + KeyMgmt.strings[10];
        } else if (this.allowedKeyManagement.get(11)) {
            key = this.SSID + KeyMgmt.strings[11];
        } else if (this.allowedKeyManagement.get(6)) {
            key = this.SSID + KeyMgmt.strings[6];
        } else if (this.allowedKeyManagement.get(7)) {
            key = this.SSID + KeyMgmt.strings[7];
        } else {
            key = this.SSID + KeyMgmt.strings[0];
        }
        if (!this.shared) {
            key = key + "-" + Integer.toString(UserHandle.getUserId(this.creatorUid));
        }
        this.mCachedConfigKey = key;
        return key;
    }

    public String configKey() {
        return configKey(false);
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
        if (this.mIpConfiguration == null || this.mIpConfiguration.proxySettings == ProxySettings.NONE) {
            return null;
        }
        return new ProxyInfo(this.mIpConfiguration.httpProxy);
    }

    public void setHttpProxy(ProxyInfo httpProxy) {
        if (this.mIpConfiguration != null) {
            if (httpProxy == null) {
                this.mIpConfiguration.setProxySettings(ProxySettings.NONE);
                this.mIpConfiguration.setHttpProxy(null);
                return;
            }
            ProxySettings proxySettingCopy;
            ProxyInfo httpProxyCopy;
            if (Uri.EMPTY.equals(httpProxy.getPacFileUrl())) {
                proxySettingCopy = ProxySettings.STATIC;
                httpProxyCopy = new ProxyInfo(httpProxy.getHost(), httpProxy.getPort(), httpProxy.getExclusionListAsString());
            } else {
                proxySettingCopy = ProxySettings.PAC;
                httpProxyCopy = new ProxyInfo(httpProxy.getPacFileUrl(), httpProxy.getPort());
            }
            if (httpProxyCopy.isValid()) {
                this.mIpConfiguration.setProxySettings(proxySettingCopy);
                this.mIpConfiguration.setHttpProxy(httpProxyCopy);
                return;
            }
            throw new IllegalArgumentException("Invalid ProxyInfo: " + httpProxyCopy.toString());
        }
    }

    public void setProxy(ProxySettings settings, ProxyInfo proxy) {
        if (this.mIpConfiguration != null) {
            this.mIpConfiguration.proxySettings = settings;
            this.mIpConfiguration.httpProxy = proxy;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void setPasspointManagementObjectTree(String passpointManagementObjectTree) {
        this.mPasspointManagementObjectTree = passpointManagementObjectTree;
    }

    public String getMoTree() {
        return this.mPasspointManagementObjectTree;
    }

    public WifiConfiguration(WifiConfiguration source) {
        this.apBand = 0;
        this.apChannel = 0;
        this.dtimInterval = 0;
        this.isLegacyPasspointConfig = false;
        this.userApproved = 0;
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
            this.lastTrySwitchWifiTimestamp = source.lastTrySwitchWifiTimestamp;
            this.internetRecoveryStatus = source.internetRecoveryStatus;
            this.internetRecoveryCheckTimestamp = source.internetRecoveryCheckTimestamp;
            this.rssiStatusDisabled = source.rssiStatusDisabled;
            this.lastConnFailedType = source.lastConnFailedType;
            this.lastConnFailedTimestamp = source.lastConnFailedTimestamp;
            this.rssiDiscNonLocally = source.rssiDiscNonLocally;
            this.timestampDiscNonLocally = source.timestampDiscNonLocally;
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
            this.isHomeProviderNetwork = source.isHomeProviderNetwork;
            this.preSharedKey = source.preSharedKey;
            this.mNetworkSelectionStatus.copy(source.getNetworkSelectionStatus());
            this.apBand = source.apBand;
            this.apChannel = source.apChannel;
            this.callingPid = source.callingPid;
            this.callingPackage = source.callingPackage;
            this.wepKeys = new String[4];
            for (int i = 0; i < this.wepKeys.length; i++) {
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
            this.isLegacyPasspointConfig = source.isLegacyPasspointConfig;
            this.ephemeral = source.ephemeral;
            this.meteredHint = source.meteredHint;
            this.meteredOverride = source.meteredOverride;
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
        int i2 = 1;
        if (this.noInternetAccess) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.internetHistory);
        if (this.portalNetwork) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.lastDhcpResults);
        dest.writeString(this.internetSelfCureHistory);
        dest.writeInt(this.portalCheckStatus);
        if (this.poorRssiDectected) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.consecutiveGoodRssiCounter);
        dest.writeLong(this.lastHasInternetTimestamp);
        dest.writeLong(this.lastTrySwitchWifiTimestamp);
        dest.writeInt(this.internetRecoveryStatus);
        dest.writeLong(this.internetRecoveryCheckTimestamp);
        dest.writeInt(this.rssiStatusDisabled);
        dest.writeInt(this.lastConnFailedType);
        dest.writeLong(this.lastConnFailedTimestamp);
        dest.writeInt(this.rssiDiscNonLocally);
        dest.writeLong(this.timestampDiscNonLocally);
        dest.writeInt(this.wifiProNoHandoverNetwork ? 1 : 0);
        if (this.wifiProNoInternetAccess) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.wifiProNoInternetReason);
        dest.writeInt(this.internetAccessType);
        dest.writeInt(this.networkQosLevel);
        dest.writeInt(this.networkQosScore);
        if (this.isTempCreated) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.isHiLinkNetwork) {
            i = 1;
        } else {
            i = 0;
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
        dest.writeInt(this.isHomeProviderNetwork ? 1 : 0);
        dest.writeInt(this.roamingConsortiumIds.length);
        for (long roamingConsortiumId : this.roamingConsortiumIds) {
            dest.writeLong(roamingConsortiumId);
        }
        dest.writeString(this.preSharedKey);
        for (String wepKey : this.wepKeys) {
            dest.writeString(wepKey);
        }
        dest.writeInt(this.wepTxKeyIndex);
        dest.writeInt(this.priority);
        dest.writeInt(this.hiddenSSID ? 1 : 0);
        if (this.requirePMF) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.updateIdentifier);
        writeBitSet(dest, this.allowedKeyManagement);
        writeBitSet(dest, this.allowedProtocols);
        writeBitSet(dest, this.allowedAuthAlgorithms);
        writeBitSet(dest, this.allowedPairwiseCiphers);
        writeBitSet(dest, this.allowedGroupCiphers);
        dest.writeInt(this.callingPid);
        dest.writeString(this.callingPackage);
        dest.writeParcelable(this.enterpriseConfig, flags);
        dest.writeParcelable(this.mIpConfiguration, flags);
        dest.writeString(this.dhcpServer);
        dest.writeString(this.defaultGwMacAddress);
        dest.writeInt(this.selfAdded ? 1 : 0);
        if (this.didSelfAdd) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.validatedInternetAccess) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.isLegacyPasspointConfig) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.ephemeral) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.meteredHint) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.meteredOverride) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.useExternalScores) {
            i = 1;
        } else {
            i = 0;
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
        dest.writeInt(this.userApproved);
        dest.writeInt(this.numNoInternetAccessReports);
        dest.writeInt(this.noInternetAccessExpected ? 1 : 0);
        if (!this.shared) {
            i2 = 0;
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
        out.writeInt(2);
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
        if (version < 1 || version > 2) {
            throw new BadVersionException("Unknown Backup Serialization Version");
        } else if (version == 1) {
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
