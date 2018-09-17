package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiSsid;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.net.DelayedDiskWrite.Writer;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WifiNetworkHistory {
    private static final int ACCESS_NOINTERNET = 2;
    private static final int ACCESS_NORMAL = 4;
    private static final int ACCESS_PORTAL = 3;
    private static final String AUTH_KEY = "AUTH";
    private static final String BSSID_KEY = "BSSID";
    private static final String BSSID_KEY_END = "/BSSID";
    private static final String BSSID_STATUS_KEY = "BSSID_STATUS";
    private static final String CHOICE_KEY = "CHOICE";
    private static final String CHOICE_TIME_KEY = "CHOICE_TIME";
    private static final String CLOUD_SECURITY_CHECK_KEY = "CLOUD_SECURITY_CHECK";
    private static final String CONFIG_BSSID_KEY = "CONFIG_BSSID";
    static final String CONFIG_KEY = "CONFIG";
    private static final String CONNECT_UID_KEY = "CONNECT_UID_KEY";
    private static final String CREATION_TIME_KEY = "CREATION_TIME";
    private static final String CREATOR_NAME_KEY = "CREATOR_NAME";
    static final String CREATOR_UID_KEY = "CREATOR_UID_KEY";
    private static final String DATE_KEY = "DATE";
    private static final boolean DBG = true;
    private static final String DEFAULT_GW_KEY = "DEFAULT_GW";
    private static final String DELETED_EPHEMERAL_KEY = "DELETED_EPHEMERAL";
    private static final String DID_SELF_ADD_KEY = "DID_SELF_ADD";
    private static final String EPHEMERAL_KEY = "EPHEMERAL";
    private static final String FAILURE_KEY = "FAILURE";
    private static final String FQDN_KEY = "FQDN";
    private static final String FREQ_KEY = "FREQ";
    private static final String FRIENDLY_NNAME_KEY = "PROVIDER_FRIENDLY_NAME";
    private static final String HAS_EVER_CONNECTED_KEY = "HAS_EVER_CONNECTED";
    public static final String INET_SELF_CURE_HISTORY_KEY = "INET_SELF_CURE_HISTORY";
    public static final String LAST_DHCP_RESULTS_KEY = "LAST_DHCP_RESULTS";
    private static final String LINK_KEY = "LINK";
    private static final String METERED_HINT_KEY = "METERED_HINT";
    private static final String MILLI_KEY = "MILLI";
    static final String NETWORK_HISTORY_CONFIG_FILE = null;
    private static final String NETWORK_ID_KEY = "ID";
    private static final String NETWORK_SELECTION_DISABLE_REASON_KEY = "NETWORK_SELECTION_DISABLE_REASON";
    private static final String NETWORK_SELECTION_STATUS_KEY = "NETWORK_SELECTION_STATUS";
    private static final String NL = "\n";
    private static final String NO_INTERNET_ACCESS_EXPECTED_KEY = "NO_INTERNET_ACCESS_EXPECTED";
    private static final String NO_INTERNET_ACCESS_REPORTS_KEY = "NO_INTERNET_ACCESS_REPORTS";
    private static final String NUM_ASSOCIATION_KEY = "NUM_ASSOCIATION";
    private static final String PEER_CONFIGURATION_KEY = "PEER_CONFIGURATION";
    private static final String PRIORITY_KEY = "PRIORITY";
    private static final String RSSI_KEY = "RSSI";
    private static final String SCORER_OVERRIDE_AND_SWITCH_KEY = "SCORER_OVERRIDE_AND_SWITCH";
    private static final String SCORER_OVERRIDE_KEY = "SCORER_OVERRIDE";
    private static final String SELF_ADDED_KEY = "SELF_ADDED";
    private static final String SEPARATOR = ":  ";
    static final String SHARED_KEY = "SHARED";
    private static final String SSID_KEY = "SSID";
    public static final String TAG = "WifiNetworkHistory";
    private static final String UPDATE_NAME_KEY = "UPDATE_NAME";
    private static final String UPDATE_TIME_KEY = "UPDATE_TIME";
    private static final String UPDATE_UID_KEY = "UPDATE_UID";
    private static final String USER_APPROVED_KEY = "USER_APPROVED";
    private static final String USE_EXTERNAL_SCORES_KEY = "USE_EXTERNAL_SCORES";
    private static final String VALIDATED_INTERNET_ACCESS_KEY = "VALIDATED_INTERNET_ACCESS";
    private static final boolean VDBG = true;
    private static final String WIFI_PRO_TEMP_CREATE = "WIFI_PRO_TEMP_CREATE";
    Context mContext;
    private final LocalLog mLocalLog;
    HashSet<String> mLostConfigsDbg;
    protected final DelayedDiskWrite mWriter;

    /* renamed from: com.android.server.wifi.WifiNetworkHistory.1 */
    class AnonymousClass1 implements Writer {
        final /* synthetic */ Set val$deletedEphemeralSSIDs;
        final /* synthetic */ List val$networks;
        final /* synthetic */ ConcurrentHashMap val$scanDetailCaches;

        AnonymousClass1(List val$networks, ConcurrentHashMap val$scanDetailCaches, Set val$deletedEphemeralSSIDs) {
            this.val$networks = val$networks;
            this.val$scanDetailCaches = val$scanDetailCaches;
            this.val$deletedEphemeralSSIDs = val$deletedEphemeralSSIDs;
        }

        public void onWriteCalled(DataOutputStream out) throws IOException {
            for (WifiConfiguration config : this.val$networks) {
                NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                if (WifiNetworkHistory.this.isValid(config)) {
                    if (config.SSID == null) {
                        WifiNetworkHistory.this.logv("writeKnownNetworkHistory trying to write config with null SSID");
                    } else {
                        out.writeUTF("CONFIG:  " + config.configKey() + WifiNetworkHistory.NL);
                        if (config.SSID != null) {
                            out.writeUTF("SSID:  " + config.SSID + WifiNetworkHistory.NL);
                        }
                        if (config.BSSID != null) {
                            out.writeUTF("CONFIG_BSSID:  " + config.BSSID + WifiNetworkHistory.NL);
                        } else {
                            out.writeUTF("CONFIG_BSSID:  null\n");
                        }
                        if (config.FQDN != null) {
                            out.writeUTF("FQDN:  " + config.FQDN + WifiNetworkHistory.NL);
                        }
                        if (config.providerFriendlyName != null) {
                            out.writeUTF("PROVIDER_FRIENDLY_NAME:  " + config.providerFriendlyName + WifiNetworkHistory.NL);
                        }
                        out.writeUTF("PRIORITY:  " + Integer.toString(config.priority) + WifiNetworkHistory.NL);
                        out.writeUTF("ID:  " + Integer.toString(config.networkId) + WifiNetworkHistory.NL);
                        out.writeUTF("SELF_ADDED:  " + Boolean.toString(config.selfAdded) + WifiNetworkHistory.NL);
                        out.writeUTF("DID_SELF_ADD:  " + Boolean.toString(config.didSelfAdd) + WifiNetworkHistory.NL);
                        out.writeUTF("NO_INTERNET_ACCESS_REPORTS:  " + Integer.toString(config.numNoInternetAccessReports) + WifiNetworkHistory.NL);
                        out.writeUTF("VALIDATED_INTERNET_ACCESS:  " + Boolean.toString(config.validatedInternetAccess) + WifiNetworkHistory.NL);
                        NetworkHistoryUtils.writeNetworkHistory(config, out, WifiNetworkHistory.SEPARATOR, WifiNetworkHistory.NL);
                        out.writeUTF("LAST_DHCP_RESULTS:  " + config.lastDhcpResults + WifiNetworkHistory.NL);
                        out.writeUTF("INET_SELF_CURE_HISTORY:  " + config.internetSelfCureHistory + WifiNetworkHistory.NL);
                        out.writeUTF("NO_INTERNET_ACCESS_EXPECTED:  " + Boolean.toString(config.noInternetAccessExpected) + WifiNetworkHistory.NL);
                        out.writeUTF("EPHEMERAL:  " + Boolean.toString(config.ephemeral) + WifiNetworkHistory.NL);
                        out.writeUTF("METERED_HINT:  " + Boolean.toString(config.meteredHint) + WifiNetworkHistory.NL);
                        out.writeUTF("USE_EXTERNAL_SCORES:  " + Boolean.toString(config.useExternalScores) + WifiNetworkHistory.NL);
                        if (config.creationTime != null) {
                            out.writeUTF("CREATION_TIME:  " + config.creationTime + WifiNetworkHistory.NL);
                        }
                        if (config.updateTime != null) {
                            out.writeUTF("UPDATE_TIME:  " + config.updateTime + WifiNetworkHistory.NL);
                        }
                        if (config.peerWifiConfiguration != null) {
                            out.writeUTF("PEER_CONFIGURATION:  " + config.peerWifiConfiguration + WifiNetworkHistory.NL);
                        }
                        out.writeUTF("SCORER_OVERRIDE:  " + Integer.toString(config.numScorerOverride) + WifiNetworkHistory.NL);
                        out.writeUTF("SCORER_OVERRIDE_AND_SWITCH:  " + Integer.toString(config.numScorerOverrideAndSwitchedNetwork) + WifiNetworkHistory.NL);
                        out.writeUTF("NUM_ASSOCIATION:  " + Integer.toString(config.numAssociation) + WifiNetworkHistory.NL);
                        out.writeUTF("CREATOR_UID_KEY:  " + Integer.toString(config.creatorUid) + WifiNetworkHistory.NL);
                        out.writeUTF("CONNECT_UID_KEY:  " + Integer.toString(config.lastConnectUid) + WifiNetworkHistory.NL);
                        out.writeUTF("UPDATE_UID:  " + Integer.toString(config.lastUpdateUid) + WifiNetworkHistory.NL);
                        out.writeUTF("CREATOR_NAME:  " + config.creatorName + WifiNetworkHistory.NL);
                        out.writeUTF("UPDATE_NAME:  " + config.lastUpdateName + WifiNetworkHistory.NL);
                        out.writeUTF("USER_APPROVED:  " + Integer.toString(config.userApproved) + WifiNetworkHistory.NL);
                        out.writeUTF("SHARED:  " + Boolean.toString(config.shared) + WifiNetworkHistory.NL);
                        DataOutputStream dataOutputStream = out;
                        dataOutputStream.writeUTF("AUTH:  " + WifiNetworkHistory.makeString(config.allowedKeyManagement, KeyMgmt.strings) + WifiNetworkHistory.NL);
                        out.writeUTF("CLOUD_SECURITY_CHECK:  " + Integer.toString(config.cloudSecurityCheck) + WifiNetworkHistory.NL);
                        out.writeUTF("NETWORK_SELECTION_STATUS:  " + status.getNetworkSelectionStatus() + WifiNetworkHistory.NL);
                        out.writeUTF("NETWORK_SELECTION_DISABLE_REASON:  " + status.getNetworkSelectionDisableReason() + WifiNetworkHistory.NL);
                        if (status.getConnectChoice() != null) {
                            out.writeUTF("CHOICE:  " + status.getConnectChoice() + WifiNetworkHistory.NL);
                            dataOutputStream = out;
                            dataOutputStream.writeUTF("CHOICE_TIME:  " + status.getConnectChoiceTimestamp() + WifiNetworkHistory.NL);
                        }
                        if (config.linkedConfigurations != null) {
                            WifiNetworkHistory.this.log("writeKnownNetworkHistory write linked " + config.linkedConfigurations.size());
                            for (String key : config.linkedConfigurations.keySet()) {
                                dataOutputStream = out;
                                dataOutputStream.writeUTF("LINK:  " + key + WifiNetworkHistory.NL);
                            }
                        }
                        String macAddress = config.defaultGwMacAddress;
                        if (macAddress != null) {
                            out.writeUTF("DEFAULT_GW:  " + macAddress + WifiNetworkHistory.NL);
                        }
                        if (WifiNetworkHistory.this.getScanDetailCache(config, this.val$scanDetailCaches) != null) {
                            for (ScanDetail scanDetail : WifiNetworkHistory.this.getScanDetailCache(config, this.val$scanDetailCaches).values()) {
                                ScanResult result = scanDetail.getScanResult();
                                out.writeUTF("BSSID:  " + result.BSSID + WifiNetworkHistory.NL);
                                out.writeUTF("FREQ:  " + Integer.toString(result.frequency) + WifiNetworkHistory.NL);
                                out.writeUTF("RSSI:  " + Integer.toString(result.level) + WifiNetworkHistory.NL);
                                out.writeUTF("/BSSID\n");
                            }
                        }
                        if (config.lastFailure != null) {
                            out.writeUTF("FAILURE:  " + config.lastFailure + WifiNetworkHistory.NL);
                        }
                        out.writeUTF("HAS_EVER_CONNECTED:  " + Boolean.toString(status.getHasEverConnected()) + WifiNetworkHistory.NL);
                        out.writeUTF(WifiNetworkHistory.NL);
                        out.writeUTF(WifiNetworkHistory.NL);
                        out.writeUTF(WifiNetworkHistory.NL);
                    }
                }
            }
            if (this.val$deletedEphemeralSSIDs != null && this.val$deletedEphemeralSSIDs.size() > 0) {
                for (String ssid : this.val$deletedEphemeralSSIDs) {
                    out.writeUTF(WifiNetworkHistory.DELETED_EPHEMERAL_KEY);
                    out.writeUTF(ssid);
                    out.writeUTF(WifiNetworkHistory.NL);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.WifiNetworkHistory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.WifiNetworkHistory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiNetworkHistory.<clinit>():void");
    }

    public WifiNetworkHistory(Context c, LocalLog localLog, DelayedDiskWrite writer) {
        this.mLostConfigsDbg = new HashSet();
        this.mContext = c;
        this.mWriter = writer;
        this.mLocalLog = localLog;
    }

    public void writeKnownNetworkHistory(List<WifiConfiguration> networks, ConcurrentHashMap<Integer, ScanDetailCache> scanDetailCaches, Set<String> deletedEphemeralSSIDs) {
        this.mWriter.write(NETWORK_HISTORY_CONFIG_FILE, new AnonymousClass1(networks, scanDetailCaches, deletedEphemeralSSIDs));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readNetworkHistory(Map<String, WifiConfiguration> configs, ConcurrentHashMap<Integer, ScanDetailCache> scanDetailCaches, Set<String> deletedEphemeralSSIDs) {
        Throwable e;
        IOException e2;
        Throwable th;
        Throwable th2;
        localLog("readNetworkHistory() path:" + NETWORK_HISTORY_CONFIG_FILE);
        Throwable th3 = null;
        DataInputStream dataInputStream = null;
        try {
            DataInputStream dataInputStream2 = new DataInputStream(new BufferedInputStream(new FileInputStream(NETWORK_HISTORY_CONFIG_FILE)));
            String bssid = null;
            String ssid = null;
            int freq = 0;
            long seen = 0;
            try {
                int rssi = WifiConfiguration.INVALID_RSSI;
                String caps = null;
                WifiConfiguration wifiConfiguration = null;
                while (true) {
                    String line = dataInputStream2.readUTF();
                    if (line == null) {
                        break;
                    }
                    int colon = line.indexOf(58);
                    if (colon >= 0) {
                        String key = line.substring(0, colon).trim();
                        String value = line.substring(colon + 1).trim();
                        if (key.equals(CONFIG_KEY)) {
                            wifiConfiguration = (WifiConfiguration) configs.get(value);
                            if (wifiConfiguration == null) {
                                localLog("readNetworkHistory didnt find netid for hash=" + Integer.toString(value.hashCode()) + " key: " + value);
                                this.mLostConfigsDbg.add(value);
                            } else if (wifiConfiguration.creatorName == null || wifiConfiguration.lastUpdateName == null) {
                                wifiConfiguration.creatorName = this.mContext.getPackageManager().getNameForUid(1000);
                                wifiConfiguration.lastUpdateName = wifiConfiguration.creatorName;
                                int i = wifiConfiguration.networkId;
                                String str = wifiConfiguration.creatorName;
                                Log.w(TAG, "Upgrading network " + i + " to " + str);
                            }
                        } else if (wifiConfiguration != null) {
                            NetworkSelectionStatus networkStatus = wifiConfiguration.getNetworkSelectionStatus();
                            if (!key.equals(SSID_KEY)) {
                                if (key.equals(CONFIG_BSSID_KEY)) {
                                    if (value.equals("null")) {
                                        value = null;
                                    }
                                    wifiConfiguration.BSSID = value;
                                } else {
                                    if (key.equals(FQDN_KEY)) {
                                        if (value.equals("null")) {
                                            value = null;
                                        }
                                        wifiConfiguration.FQDN = value;
                                    } else {
                                        if (key.equals(FRIENDLY_NNAME_KEY)) {
                                            if (value.equals("null")) {
                                                value = null;
                                            }
                                            wifiConfiguration.providerFriendlyName = value;
                                        } else {
                                            if (key.equals(CLOUD_SECURITY_CHECK_KEY)) {
                                                int i2;
                                                if (value.equals("null")) {
                                                    i2 = 0;
                                                } else {
                                                    i2 = Integer.parseInt(value);
                                                }
                                                wifiConfiguration.cloudSecurityCheck = i2;
                                            } else {
                                                if (key.equals(DEFAULT_GW_KEY)) {
                                                    wifiConfiguration.defaultGwMacAddress = value;
                                                } else {
                                                    if (key.equals(SELF_ADDED_KEY)) {
                                                        wifiConfiguration.selfAdded = Boolean.parseBoolean(value);
                                                    } else {
                                                        if (key.equals(DID_SELF_ADD_KEY)) {
                                                            wifiConfiguration.didSelfAdd = Boolean.parseBoolean(value);
                                                        } else {
                                                            if (key.equals(NO_INTERNET_ACCESS_REPORTS_KEY)) {
                                                                wifiConfiguration.numNoInternetAccessReports = Integer.parseInt(value);
                                                            } else {
                                                                if (key.equals(VALIDATED_INTERNET_ACCESS_KEY)) {
                                                                    wifiConfiguration.validatedInternetAccess = Boolean.parseBoolean(value);
                                                                } else {
                                                                    if (!key.equals("INTERNET_HISTORY")) {
                                                                        if (!key.equals("PORTAL_NETWORK")) {
                                                                            if (!key.equals(WIFI_PRO_TEMP_CREATE)) {
                                                                                if (!key.equals("LAST_HAS_INTERNET_TS")) {
                                                                                    if (key.equals(LAST_DHCP_RESULTS_KEY)) {
                                                                                        wifiConfiguration.lastDhcpResults = value;
                                                                                    } else {
                                                                                        if (key.equals(INET_SELF_CURE_HISTORY_KEY)) {
                                                                                            wifiConfiguration.internetSelfCureHistory = value;
                                                                                        } else {
                                                                                            if (key.equals(NO_INTERNET_ACCESS_EXPECTED_KEY)) {
                                                                                                wifiConfiguration.noInternetAccessExpected = Boolean.parseBoolean(value);
                                                                                            } else {
                                                                                                if (key.equals(CREATION_TIME_KEY)) {
                                                                                                    wifiConfiguration.creationTime = value;
                                                                                                } else {
                                                                                                    if (key.equals(UPDATE_TIME_KEY)) {
                                                                                                        wifiConfiguration.updateTime = value;
                                                                                                    } else {
                                                                                                        if (key.equals(EPHEMERAL_KEY)) {
                                                                                                            wifiConfiguration.ephemeral = Boolean.parseBoolean(value);
                                                                                                        } else {
                                                                                                            if (key.equals(METERED_HINT_KEY)) {
                                                                                                                wifiConfiguration.meteredHint = Boolean.parseBoolean(value);
                                                                                                            } else {
                                                                                                                if (key.equals(USE_EXTERNAL_SCORES_KEY)) {
                                                                                                                    wifiConfiguration.useExternalScores = Boolean.parseBoolean(value);
                                                                                                                } else {
                                                                                                                    if (key.equals(CREATOR_UID_KEY)) {
                                                                                                                        wifiConfiguration.creatorUid = Integer.parseInt(value);
                                                                                                                    } else {
                                                                                                                        if (key.equals(SCORER_OVERRIDE_KEY)) {
                                                                                                                            wifiConfiguration.numScorerOverride = Integer.parseInt(value);
                                                                                                                        } else {
                                                                                                                            if (key.equals(SCORER_OVERRIDE_AND_SWITCH_KEY)) {
                                                                                                                                wifiConfiguration.numScorerOverrideAndSwitchedNetwork = Integer.parseInt(value);
                                                                                                                            } else {
                                                                                                                                if (key.equals(NUM_ASSOCIATION_KEY)) {
                                                                                                                                    wifiConfiguration.numAssociation = Integer.parseInt(value);
                                                                                                                                } else {
                                                                                                                                    if (key.equals(CONNECT_UID_KEY)) {
                                                                                                                                        wifiConfiguration.lastConnectUid = Integer.parseInt(value);
                                                                                                                                    } else {
                                                                                                                                        if (key.equals(UPDATE_UID_KEY)) {
                                                                                                                                            wifiConfiguration.lastUpdateUid = Integer.parseInt(value);
                                                                                                                                        } else {
                                                                                                                                            if (key.equals(FAILURE_KEY)) {
                                                                                                                                                wifiConfiguration.lastFailure = value;
                                                                                                                                            } else {
                                                                                                                                                if (key.equals(PEER_CONFIGURATION_KEY)) {
                                                                                                                                                    wifiConfiguration.peerWifiConfiguration = value;
                                                                                                                                                } else {
                                                                                                                                                    if (key.equals(NETWORK_SELECTION_STATUS_KEY)) {
                                                                                                                                                        int networkStatusValue = Integer.parseInt(value);
                                                                                                                                                        if (networkStatusValue == 1) {
                                                                                                                                                            networkStatusValue = 0;
                                                                                                                                                        }
                                                                                                                                                        networkStatus.setNetworkSelectionStatus(networkStatusValue);
                                                                                                                                                    } else {
                                                                                                                                                        if (key.equals(NETWORK_SELECTION_DISABLE_REASON_KEY)) {
                                                                                                                                                            networkStatus.setNetworkSelectionDisableReason(Integer.parseInt(value));
                                                                                                                                                        } else {
                                                                                                                                                            if (key.equals(CHOICE_KEY)) {
                                                                                                                                                                networkStatus.setConnectChoice(value);
                                                                                                                                                            } else {
                                                                                                                                                                if (key.equals(CHOICE_TIME_KEY)) {
                                                                                                                                                                    networkStatus.setConnectChoiceTimestamp(Long.parseLong(value));
                                                                                                                                                                } else {
                                                                                                                                                                    if (!key.equals(LINK_KEY)) {
                                                                                                                                                                        if (key.equals(BSSID_KEY)) {
                                                                                                                                                                            ssid = null;
                                                                                                                                                                            bssid = null;
                                                                                                                                                                            freq = 0;
                                                                                                                                                                            seen = 0;
                                                                                                                                                                            rssi = WifiConfiguration.INVALID_RSSI;
                                                                                                                                                                            caps = "";
                                                                                                                                                                        } else {
                                                                                                                                                                            if (key.equals(RSSI_KEY)) {
                                                                                                                                                                                rssi = Integer.parseInt(value);
                                                                                                                                                                            } else {
                                                                                                                                                                                if (key.equals(FREQ_KEY)) {
                                                                                                                                                                                    freq = Integer.parseInt(value);
                                                                                                                                                                                } else {
                                                                                                                                                                                    if (!key.equals(DATE_KEY)) {
                                                                                                                                                                                        if (!key.equals(BSSID_KEY_END)) {
                                                                                                                                                                                            if (!key.equals(DELETED_EPHEMERAL_KEY)) {
                                                                                                                                                                                                if (key.equals(CREATOR_NAME_KEY)) {
                                                                                                                                                                                                    wifiConfiguration.creatorName = value;
                                                                                                                                                                                                } else {
                                                                                                                                                                                                    if (key.equals(UPDATE_NAME_KEY)) {
                                                                                                                                                                                                        wifiConfiguration.lastUpdateName = value;
                                                                                                                                                                                                    } else {
                                                                                                                                                                                                        if (key.equals(USER_APPROVED_KEY)) {
                                                                                                                                                                                                            wifiConfiguration.userApproved = Integer.parseInt(value);
                                                                                                                                                                                                        } else {
                                                                                                                                                                                                            if (key.equals(SHARED_KEY)) {
                                                                                                                                                                                                                wifiConfiguration.shared = Boolean.parseBoolean(value);
                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                if (key.equals(HAS_EVER_CONNECTED_KEY)) {
                                                                                                                                                                                                                    networkStatus.setHasEverConnected(Boolean.parseBoolean(value));
                                                                                                                                                                                                                }
                                                                                                                                                                                                            }
                                                                                                                                                                                                        }
                                                                                                                                                                                                    }
                                                                                                                                                                                                }
                                                                                                                                                                                            } else if (!TextUtils.isEmpty(value)) {
                                                                                                                                                                                                deletedEphemeralSSIDs.add(value);
                                                                                                                                                                                            }
                                                                                                                                                                                        } else if (!(null == null || ssid == null || getScanDetailCache(wifiConfiguration, scanDetailCaches) == null)) {
                                                                                                                                                                                            getScanDetailCache(wifiConfiguration, scanDetailCaches).put(new ScanDetail(WifiSsid.createFromAsciiEncoded(ssid), bssid, caps, rssi, freq, 0, seen));
                                                                                                                                                                                        }
                                                                                                                                                                                    }
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                    } else if (wifiConfiguration.linkedConfigurations == null) {
                                                                                                                                                                        wifiConfiguration.linkedConfigurations = new HashMap();
                                                                                                                                                                    } else {
                                                                                                                                                                        wifiConfiguration.linkedConfigurations.put(value, Integer.valueOf(-1));
                                                                                                                                                                    }
                                                                                                                                                                }
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    NetworkHistoryUtils.readNetworkHistory(wifiConfiguration, key, value);
                                                                    wifiConfiguration.wifiProNoInternetAccess = !wifiConfiguration.noInternetAccess ? wifiConfiguration.portalNetwork : VDBG;
                                                                    if (wifiConfiguration.noInternetAccess) {
                                                                        wifiConfiguration.internetAccessType = ACCESS_NOINTERNET;
                                                                    }
                                                                    if (wifiConfiguration.portalNetwork) {
                                                                        wifiConfiguration.internetAccessType = ACCESS_PORTAL;
                                                                    }
                                                                    if (!wifiConfiguration.wifiProNoInternetAccess) {
                                                                        wifiConfiguration.internetAccessType = ACCESS_NORMAL;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (!wifiConfiguration.isPasspoint()) {
                                ssid = value;
                                if (wifiConfiguration.SSID == null || wifiConfiguration.SSID.equals(value)) {
                                    wifiConfiguration.SSID = value;
                                } else {
                                    loge("Error parsing network history file, mismatched SSIDs");
                                    wifiConfiguration = null;
                                    ssid = null;
                                }
                            }
                        }
                    }
                }
                if (dataInputStream2 != null) {
                    try {
                        dataInputStream2.close();
                    } catch (Throwable th4) {
                        th3 = th4;
                    }
                }
                if (th3 != null) {
                    try {
                        throw th3;
                    } catch (NumberFormatException e3) {
                        e = e3;
                        dataInputStream = dataInputStream2;
                    } catch (EOFException e4) {
                        dataInputStream = dataInputStream2;
                        return;
                    } catch (IOException e5) {
                        e2 = e5;
                        dataInputStream = dataInputStream2;
                        Log.e(TAG, "readNetworkHistory: No config file, revert to default, " + e2, e2);
                        return;
                    }
                }
                dataInputStream = dataInputStream2;
            } catch (Throwable th5) {
                th = th5;
                th2 = null;
                dataInputStream = dataInputStream2;
            }
        } catch (Throwable th6) {
            th = th6;
            th2 = null;
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (Throwable th32) {
                    if (th2 == null) {
                        th2 = th32;
                    } else if (th2 != th32) {
                        th2.addSuppressed(th32);
                    }
                }
            }
            if (th2 != null) {
                try {
                    throw th2;
                } catch (NumberFormatException e6) {
                    e = e6;
                } catch (EOFException e7) {
                    return;
                } catch (IOException e8) {
                    e2 = e8;
                    Log.e(TAG, "readNetworkHistory: No config file, revert to default, " + e2, e2);
                    return;
                }
            }
            throw th;
        }
    }

    public boolean isValid(WifiConfiguration config) {
        if (config.allowedKeyManagement == null) {
            return false;
        }
        if (config.allowedKeyManagement.cardinality() > 1) {
            if (config.allowedKeyManagement.cardinality() == ACCESS_NOINTERNET && config.allowedKeyManagement.get(ACCESS_NOINTERNET)) {
                return (config.allowedKeyManagement.get(ACCESS_PORTAL) || config.allowedKeyManagement.get(1)) ? VDBG : false;
            } else {
                return false;
            }
        }
    }

    private static String makeString(BitSet set, String[] strings) {
        StringBuffer buf = new StringBuffer();
        int nextSetBit = -1;
        set = set.get(0, strings.length);
        while (true) {
            nextSetBit = set.nextSetBit(nextSetBit + 1);
            if (nextSetBit == -1) {
                break;
            }
            buf.append(strings[nextSetBit].replace('_', '-')).append(' ');
        }
        if (set.cardinality() > 0) {
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }

    protected void logv(String s) {
        Log.v(TAG, s);
    }

    protected void logd(String s) {
        Log.d(TAG, s);
    }

    protected void log(String s) {
        Log.d(TAG, s);
    }

    protected void loge(String s) {
        loge(s, false);
    }

    protected void loge(String s, boolean stack) {
        if (stack) {
            Log.e(TAG, s + " stack:" + Thread.currentThread().getStackTrace()[ACCESS_NOINTERNET].getMethodName() + " - " + Thread.currentThread().getStackTrace()[ACCESS_PORTAL].getMethodName() + " - " + Thread.currentThread().getStackTrace()[ACCESS_NORMAL].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        } else {
            Log.e(TAG, s);
        }
    }

    private void localLog(String s) {
        if (this.mLocalLog != null) {
            this.mLocalLog.log(s);
        }
    }

    private ScanDetailCache getScanDetailCache(WifiConfiguration config, ConcurrentHashMap<Integer, ScanDetailCache> scanDetailCaches) {
        if (config == null || scanDetailCaches == null) {
            return null;
        }
        ScanDetailCache cache = (ScanDetailCache) scanDetailCaches.get(Integer.valueOf(config.networkId));
        if (cache == null && config.networkId != -1) {
            cache = new ScanDetailCache(config);
            scanDetailCaches.put(Integer.valueOf(config.networkId), cache);
        }
        return cache;
    }
}
