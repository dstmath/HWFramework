package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.util.Log;
import com.android.server.net.DelayedDiskWrite;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WifiNetworkHistory {
    private static final String AUTH_KEY = "AUTH";
    private static final String BSSID_KEY = "BSSID";
    private static final String BSSID_KEY_END = "/BSSID";
    private static final String BSSID_STATUS_KEY = "BSSID_STATUS";
    private static final String CHOICE_KEY = "CHOICE";
    private static final String CHOICE_TIME_KEY = "CHOICE_TIME";
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
    private static final String FQDN_KEY = "FQDN";
    private static final String FREQ_KEY = "FREQ";
    private static final String HAS_EVER_CONNECTED_KEY = "HAS_EVER_CONNECTED";
    private static final String LINK_KEY = "LINK";
    private static final String METERED_HINT_KEY = "METERED_HINT";
    private static final String METERED_OVERRIDE_KEY = "METERED_OVERRIDE";
    private static final String MILLI_KEY = "MILLI";
    static final String NETWORK_HISTORY_CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/networkHistory.txt");
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
    Context mContext;
    HashSet<String> mLostConfigsDbg = new HashSet<>();
    protected final DelayedDiskWrite mWriter;

    public WifiNetworkHistory(Context c, DelayedDiskWrite writer) {
        this.mContext = c;
        this.mWriter = writer;
    }

    public void writeKnownNetworkHistory(final List<WifiConfiguration> networks, final ConcurrentHashMap<Integer, ScanDetailCache> scanDetailCaches, final Set<String> deletedEphemeralSSIDs) {
        this.mWriter.write(NETWORK_HISTORY_CONFIG_FILE, new DelayedDiskWrite.Writer() {
            public void onWriteCalled(DataOutputStream out) throws IOException {
                String disableTime;
                for (WifiConfiguration config : networks) {
                    WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                    int numlink = 0;
                    if (config.linkedConfigurations != null) {
                        numlink = config.linkedConfigurations.size();
                    }
                    if (config.getNetworkSelectionStatus().isNetworkEnabled()) {
                        disableTime = "";
                    } else {
                        disableTime = "Disable time: " + DateFormat.getInstance().format(Long.valueOf(config.getNetworkSelectionStatus().getDisableTime()));
                    }
                    WifiNetworkHistory.this.logd("saving network history: " + config.configKey() + " gw: " + config.defaultGwMacAddress + " Network Selection-status: " + status.getNetworkStatusString() + disableTime + " ephemeral=" + config.ephemeral + " choice:" + status.getConnectChoice() + " link:" + numlink + " status:" + config.status + " nid:" + config.networkId + " hasEverConnected: " + status.getHasEverConnected());
                    if (WifiNetworkHistory.this.isValid(config)) {
                        if (config.SSID == null) {
                            WifiNetworkHistory.this.logv("writeKnownNetworkHistory trying to write config with null SSID");
                        } else {
                            WifiNetworkHistory.this.logv("writeKnownNetworkHistory write config " + config.configKey());
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
                            out.writeUTF("PRIORITY:  " + Integer.toString(config.priority) + WifiNetworkHistory.NL);
                            out.writeUTF("ID:  " + Integer.toString(config.networkId) + WifiNetworkHistory.NL);
                            out.writeUTF("SELF_ADDED:  " + Boolean.toString(config.selfAdded) + WifiNetworkHistory.NL);
                            out.writeUTF("DID_SELF_ADD:  " + Boolean.toString(config.didSelfAdd) + WifiNetworkHistory.NL);
                            out.writeUTF("NO_INTERNET_ACCESS_REPORTS:  " + Integer.toString(config.numNoInternetAccessReports) + WifiNetworkHistory.NL);
                            out.writeUTF("VALIDATED_INTERNET_ACCESS:  " + Boolean.toString(config.validatedInternetAccess) + WifiNetworkHistory.NL);
                            out.writeUTF("NO_INTERNET_ACCESS_EXPECTED:  " + Boolean.toString(config.noInternetAccessExpected) + WifiNetworkHistory.NL);
                            out.writeUTF("EPHEMERAL:  " + Boolean.toString(config.ephemeral) + WifiNetworkHistory.NL);
                            out.writeUTF("METERED_HINT:  " + Boolean.toString(config.meteredHint) + WifiNetworkHistory.NL);
                            out.writeUTF("METERED_OVERRIDE:  " + Integer.toString(config.meteredOverride) + WifiNetworkHistory.NL);
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
                            String allowedKeyManagementString = WifiNetworkHistory.makeString(config.allowedKeyManagement, WifiConfiguration.KeyMgmt.strings);
                            out.writeUTF("AUTH:  " + allowedKeyManagementString + WifiNetworkHistory.NL);
                            out.writeUTF("NETWORK_SELECTION_STATUS:  " + status.getNetworkSelectionStatus() + WifiNetworkHistory.NL);
                            out.writeUTF("NETWORK_SELECTION_DISABLE_REASON:  " + status.getNetworkSelectionDisableReason() + WifiNetworkHistory.NL);
                            if (status.getConnectChoice() != null) {
                                out.writeUTF("CHOICE:  " + status.getConnectChoice() + WifiNetworkHistory.NL);
                                out.writeUTF("CHOICE_TIME:  " + status.getConnectChoiceTimestamp() + WifiNetworkHistory.NL);
                            }
                            if (config.linkedConfigurations != null) {
                                WifiNetworkHistory.this.log("writeKnownNetworkHistory write linked " + config.linkedConfigurations.size());
                                Iterator it = config.linkedConfigurations.keySet().iterator();
                                while (it.hasNext()) {
                                    out.writeUTF("LINK:  " + ((String) it.next()) + WifiNetworkHistory.NL);
                                }
                            }
                            if (config.defaultGwMacAddress != null) {
                                out.writeUTF("DEFAULT_GW:  " + macAddress + WifiNetworkHistory.NL);
                            }
                            if (WifiNetworkHistory.this.getScanDetailCache(config, scanDetailCaches) != null) {
                                for (ScanDetail scanDetail : WifiNetworkHistory.this.getScanDetailCache(config, scanDetailCaches).values()) {
                                    ScanResult result = scanDetail.getScanResult();
                                    out.writeUTF("BSSID:  " + result.BSSID + WifiNetworkHistory.NL);
                                    out.writeUTF("FREQ:  " + Integer.toString(result.frequency) + WifiNetworkHistory.NL);
                                    out.writeUTF("RSSI:  " + Integer.toString(result.level) + WifiNetworkHistory.NL);
                                    out.writeUTF("/BSSID\n");
                                }
                            }
                            out.writeUTF("HAS_EVER_CONNECTED:  " + Boolean.toString(status.getHasEverConnected()) + WifiNetworkHistory.NL);
                            out.writeUTF(WifiNetworkHistory.NL);
                            out.writeUTF(WifiNetworkHistory.NL);
                            out.writeUTF(WifiNetworkHistory.NL);
                        }
                    }
                }
                if (deletedEphemeralSSIDs != null && deletedEphemeralSSIDs.size() > 0) {
                    for (String ssid : deletedEphemeralSSIDs) {
                        out.writeUTF(WifiNetworkHistory.DELETED_EPHEMERAL_KEY);
                        out.writeUTF(ssid);
                        out.writeUTF(WifiNetworkHistory.NL);
                    }
                }
            }
        });
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x02ab, code lost:
        r9 = 65535;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x02ac, code lost:
        switch(r9) {
            case 0: goto L_0x0564;
            case 1: goto L_0x054b;
            case 2: goto L_0x0532;
            case 3: goto L_0x0523;
            case 4: goto L_0x0510;
            case 5: goto L_0x04fd;
            case 6: goto L_0x04ea;
            case 7: goto L_0x04d7;
            case 8: goto L_0x04c4;
            case 9: goto L_0x04b5;
            case 10: goto L_0x04a6;
            case 11: goto L_0x0493;
            case 12: goto L_0x0480;
            case 13: goto L_0x046d;
            case 14: goto L_0x045a;
            case 15: goto L_0x0447;
            case 16: goto L_0x0434;
            case 17: goto L_0x0421;
            case 18: goto L_0x040e;
            case 19: goto L_0x03fb;
            case 20: goto L_0x03e8;
            case android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION :int: goto L_0x03d9;
            case 22: goto L_0x03c2;
            case 23: goto L_0x03ae;
            case 24: goto L_0x039e;
            case 25: goto L_0x038a;
            case android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.ReasonCode.TDLS_TEARDOWN_UNSPECIFIED :int: goto L_0x0366;
            case 27: goto L_0x0347;
            case 28: goto L_0x0333;
            case 29: goto L_0x0321;
            case 30: goto L_0x0314;
            case 31: goto L_0x02df;
            case 32: goto L_0x02cd;
            case 33: goto L_0x02ca;
            case 34: goto L_0x02c7;
            case 35: goto L_0x02c0;
            case android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.ReasonCode.STA_LEAVING :int: goto L_0x02b9;
            case 37: goto L_0x02b1;
            default: goto L_0x02af;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x02b1, code lost:
        r8.setHasEverConnected(java.lang.Boolean.parseBoolean(r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x02b9, code lost:
        r0.shared = java.lang.Boolean.parseBoolean(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x02c0, code lost:
        r0.userApproved = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x02c7, code lost:
        r0.lastUpdateName = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x02ca, code lost:
        r0.creatorName = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x02d1, code lost:
        if (android.text.TextUtils.isEmpty(r12) != false) goto L_0x02d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:?, code lost:
        r32.add(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x02d9, code lost:
        r9 = r32;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x02db, code lost:
        r25 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x02df, code lost:
        r9 = r32;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x02e1, code lost:
        if (r4 == null) goto L_0x0597;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x02e3, code lost:
        if (r5 == null) goto L_0x0597;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x02e9, code lost:
        if (getScanDetailCache(r0, r2) == null) goto L_0x0597;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x02eb, code lost:
        r25 = r5;
        r5 = r12;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r10 = new com.android.server.wifi.ScanDetail(android.net.wifi.WifiSsid.createFromAsciiEncoded(r5), r4, r6, r20, r7, 0, r21);
        getScanDetailCache(r0, r2).put(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x0314, code lost:
        r9 = r32;
        r25 = r5;
        r5 = r12;
        r26 = r13;
        r27 = r14;
        r28 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x0321, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r7 = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x0333, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r20 = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x0347, code lost:
        r9 = r32;
        r25 = r5;
        r5 = r12;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r4 = null;
        r7 = 0;
        r6 = "";
        r8 = 0;
        r5 = null;
        r21 = 0;
        r20 = android.net.wifi.WifiConfiguration.INVALID_RSSI;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:0x0366, code lost:
        r9 = r32;
        r25 = r5;
        r5 = r12;
        r26 = r13;
        r27 = r14;
        r28 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x0373, code lost:
        if (r0.linkedConfigurations != null) goto L_0x037e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x0375, code lost:
        r0.linkedConfigurations = new java.util.HashMap();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x037e, code lost:
        r0.linkedConfigurations.put(r5, -1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x038a, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r8.setConnectChoiceTimestamp(java.lang.Long.parseLong(r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:0x039e, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r8.setConnectChoice(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x03ae, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r8.setNetworkSelectionDisableReason(java.lang.Integer.parseInt(r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x03c2, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r11 = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x03d1, code lost:
        if (r11 != 1) goto L_0x03d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x03d3, code lost:
        r11 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x03d4, code lost:
        r8.setNetworkSelectionStatus(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:184:0x03d9, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.peerWifiConfiguration = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:185:0x03e8, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.lastUpdateUid = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:186:0x03fb, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.lastConnectUid = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x040e, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.numAssociation = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:0x0421, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.numScorerOverrideAndSwitchedNetwork = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x0434, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.numScorerOverride = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x0447, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.creatorUid = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x045a, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.useExternalScores = java.lang.Boolean.parseBoolean(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x046d, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.meteredOverride = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x0480, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.meteredHint = java.lang.Boolean.parseBoolean(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:194:0x0493, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.ephemeral = java.lang.Boolean.parseBoolean(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x04a6, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.updateTime = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x04b5, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.creationTime = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x04c4, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.noInternetAccessExpected = java.lang.Boolean.parseBoolean(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:0x04d7, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.validatedInternetAccess = java.lang.Boolean.parseBoolean(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x04ea, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.numNoInternetAccessReports = java.lang.Integer.parseInt(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:200:0x04fd, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.didSelfAdd = java.lang.Boolean.parseBoolean(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x0510, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.selfAdded = java.lang.Boolean.parseBoolean(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:202:0x0523, code lost:
        r9 = r32;
        r25 = r5;
        r26 = r13;
        r27 = r14;
        r28 = r15;
        r0.defaultGwMacAddress = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x0532, code lost:
        r9 = r32;
        r25 = r5;
        r5 = r12;
        r26 = r13;
        r27 = r14;
        r28 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x0543, code lost:
        if (r5.equals("null") == false) goto L_0x0547;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x0545, code lost:
        r10 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x0547, code lost:
        r10 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x0548, code lost:
        r0.FQDN = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x054b, code lost:
        r9 = r32;
        r25 = r5;
        r5 = r12;
        r26 = r13;
        r27 = r14;
        r28 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:209:0x055c, code lost:
        if (r5.equals("null") == false) goto L_0x0560;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:0x055e, code lost:
        r10 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x0560, code lost:
        r10 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:212:0x0561, code lost:
        r0.BSSID = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:213:0x0564, code lost:
        r9 = r32;
        r25 = r5;
        r5 = r12;
        r26 = r13;
        r27 = r14;
        r28 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:214:0x0573, code lost:
        if (r0.isPasspoint() == false) goto L_0x0576;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:0x0576, code lost:
        r10 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:217:0x0579, code lost:
        if (r0.SSID == null) goto L_0x058b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:219:0x0581, code lost:
        if (r0.SSID.equals(r10) != false) goto L_0x058b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:220:0x0583, code lost:
        loge("Error parsing network history file, mismatched SSIDs");
        r0 = null;
        r10 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x058b, code lost:
        r0.SSID = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:222:0x058e, code lost:
        r5 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:223:0x0591, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x0593, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:240:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:0x05b3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:242:0x05b5, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:243:0x05b7, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:245:0x05bb, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:248:?, code lost:
        r5.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:249:0x05c1, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:273:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:239:0x05af A[SYNTHETIC, Splitter:B:239:0x05af] */
    /* JADX WARNING: Removed duplicated region for block: B:249:0x05c1 A[Catch:{ EOFException -> 0x05b9, FileNotFoundException -> 0x05b7, NumberFormatException -> 0x05b5, IOException -> 0x05b3 }] */
    public void readNetworkHistory(Map<String, WifiConfiguration> configs, Map<Integer, ScanDetailCache> scanDetailCaches, Set<String> deletedEphemeralSSIDs) {
        Throwable th;
        Throwable th2;
        int status;
        char c;
        Map<Integer, ScanDetailCache> map = scanDetailCaches;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(NETWORK_HISTORY_CONFIG_FILE)));
            try {
                long seen = 0;
                int rssi = WifiConfiguration.INVALID_RSSI;
                int freq = 0;
                int status2 = 0;
                String caps = null;
                String value = null;
                String bssid = null;
                WifiConfiguration config = null;
                while (true) {
                    String line = in.readUTF();
                    if (line == null) {
                        in.close();
                        Set<String> set = deletedEphemeralSSIDs;
                        return;
                    }
                    int colon = line.indexOf(58);
                    if (colon >= 0) {
                        String key = line.substring(0, colon).trim();
                        String value2 = line.substring(colon + 1).trim();
                        if (key.equals(CONFIG_KEY)) {
                            config = configs.get(value2);
                            if (config == null) {
                                Log.e(TAG, "readNetworkHistory didnt find netid for hash=" + Integer.toString(value2.hashCode()) + " key: " + value2);
                                this.mLostConfigsDbg.add(value2);
                                status2 = status2;
                            } else {
                                status = status2;
                                if (config.creatorName == null || config.lastUpdateName == null) {
                                    config.creatorName = this.mContext.getPackageManager().getNameForUid(1000);
                                    config.lastUpdateName = config.creatorName;
                                    Log.w(TAG, "Upgrading network " + config.networkId + " to " + config.creatorName);
                                }
                                Set<String> set2 = deletedEphemeralSSIDs;
                            }
                        } else {
                            Map<String, WifiConfiguration> map2 = configs;
                            status = status2;
                            if (config != null) {
                                WifiConfiguration.NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
                                switch (key.hashCode()) {
                                    case -1946896213:
                                        if (key.equals(USER_APPROVED_KEY)) {
                                            c = '#';
                                            break;
                                        }
                                    case -1866906821:
                                        if (key.equals(CONNECT_UID_KEY)) {
                                            c = 19;
                                            break;
                                        }
                                    case -1865201711:
                                        if (key.equals(VALIDATED_INTERNET_ACCESS_KEY)) {
                                            c = 7;
                                            break;
                                        }
                                    case -1850236827:
                                        if (key.equals(SHARED_KEY)) {
                                            c = '$';
                                            break;
                                        }
                                    case -1842190690:
                                        if (key.equals(METERED_HINT_KEY)) {
                                            c = 12;
                                            break;
                                        }
                                    case -1646996988:
                                        if (key.equals(NO_INTERNET_ACCESS_REPORTS_KEY)) {
                                            c = 6;
                                            break;
                                        }
                                    case -1103645511:
                                        if (key.equals(PEER_CONFIGURATION_KEY)) {
                                            c = 21;
                                            break;
                                        }
                                    case -1025978637:
                                        if (key.equals(NO_INTERNET_ACCESS_EXPECTED_KEY)) {
                                            c = 8;
                                            break;
                                        }
                                    case -944985731:
                                        if (key.equals(EPHEMERAL_KEY)) {
                                            c = 11;
                                            break;
                                        }
                                    case -552300306:
                                        if (key.equals(DID_SELF_ADD_KEY)) {
                                            c = 5;
                                            break;
                                        }
                                    case -534910080:
                                        if (key.equals(CONFIG_BSSID_KEY)) {
                                            c = 1;
                                            break;
                                        }
                                    case -375674826:
                                        if (key.equals(NETWORK_SELECTION_STATUS_KEY)) {
                                            c = 22;
                                            break;
                                        }
                                    case 2090926:
                                        if (key.equals(DATE_KEY)) {
                                            c = 30;
                                            break;
                                        }
                                    case 2165397:
                                        if (key.equals("FQDN")) {
                                            c = 2;
                                            break;
                                        }
                                    case 2166392:
                                        if (key.equals(FREQ_KEY)) {
                                            c = 29;
                                            break;
                                        }
                                    case 2336762:
                                        if (key.equals(LINK_KEY)) {
                                            c = 26;
                                            break;
                                        }
                                    case 2525271:
                                        if (key.equals(RSSI_KEY)) {
                                            c = 28;
                                            break;
                                        }
                                    case 2554747:
                                        if (key.equals("SSID")) {
                                            c = 0;
                                            break;
                                        }
                                    case 63507133:
                                        if (key.equals("BSSID")) {
                                            c = 27;
                                            break;
                                        }
                                    case 89250059:
                                        if (key.equals(SCORER_OVERRIDE_KEY)) {
                                            c = 16;
                                            break;
                                        }
                                    case 190453690:
                                        if (key.equals(UPDATE_UID_KEY)) {
                                            c = 20;
                                            break;
                                        }
                                    case 417823927:
                                        if (key.equals(DELETED_EPHEMERAL_KEY)) {
                                            c = ' ';
                                            break;
                                        }
                                    case 501180973:
                                        if (key.equals(SELF_ADDED_KEY)) {
                                            c = 4;
                                            break;
                                        }
                                    case 740738782:
                                        if (key.equals(CREATOR_NAME_KEY)) {
                                            c = '!';
                                            break;
                                        }
                                    case 782347629:
                                        if (key.equals(HAS_EVER_CONNECTED_KEY)) {
                                            c = WifiLog.PLACEHOLDER;
                                            break;
                                        }
                                    case 783161389:
                                        if (key.equals(CREATION_TIME_KEY)) {
                                            c = 9;
                                            break;
                                        }
                                    case 1163774926:
                                        if (key.equals(DEFAULT_GW_KEY)) {
                                            c = 3;
                                            break;
                                        }
                                    case 1187625059:
                                        if (key.equals(METERED_OVERRIDE_KEY)) {
                                            c = 13;
                                            break;
                                        }
                                    case 1366197215:
                                        if (key.equals(NETWORK_SELECTION_DISABLE_REASON_KEY)) {
                                            c = 23;
                                            break;
                                        }
                                    case 1409077230:
                                        if (key.equals(BSSID_KEY_END)) {
                                            c = 31;
                                            break;
                                        }
                                    case 1477121648:
                                        if (key.equals(SCORER_OVERRIDE_AND_SWITCH_KEY)) {
                                            c = 17;
                                            break;
                                        }
                                    case 1608881217:
                                        if (key.equals(UPDATE_NAME_KEY)) {
                                            c = '\"';
                                            break;
                                        }
                                    case 1609067651:
                                        if (key.equals(UPDATE_TIME_KEY)) {
                                            c = 10;
                                            break;
                                        }
                                    case 1614319275:
                                        if (key.equals(CHOICE_TIME_KEY)) {
                                            c = 25;
                                            break;
                                        }
                                    case 1928336648:
                                        if (key.equals(NUM_ASSOCIATION_KEY)) {
                                            c = 18;
                                            break;
                                        }
                                    case 1946216573:
                                        if (key.equals(CREATOR_UID_KEY)) {
                                            c = 15;
                                            break;
                                        }
                                    case 1987072417:
                                        if (key.equals(CHOICE_KEY)) {
                                            c = 24;
                                            break;
                                        }
                                    case 2026657853:
                                        if (key.equals(USE_EXTERNAL_SCORES_KEY)) {
                                            c = 14;
                                            break;
                                        }
                                }
                            }
                            Set<String> set3 = deletedEphemeralSSIDs;
                            String ssid = value;
                            status2 = status;
                            value = ssid;
                        }
                        status2 = status;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                Set<String> set4 = deletedEphemeralSSIDs;
                th2 = th;
                th = null;
                if (th == null) {
                }
                throw th2;
            }
        } catch (EOFException e) {
            Set<String> set5 = deletedEphemeralSSIDs;
        } catch (FileNotFoundException e2) {
            e = e2;
            Set<String> set6 = deletedEphemeralSSIDs;
            Log.i(TAG, "readNetworkHistory: no config file, " + e);
        } catch (NumberFormatException e3) {
            e = e3;
            Set<String> set7 = deletedEphemeralSSIDs;
            Log.e(TAG, "readNetworkHistory: failed to parse, " + e, e);
        } catch (IOException e4) {
            e = e4;
            Set<String> set8 = deletedEphemeralSSIDs;
            Log.e(TAG, "readNetworkHistory: failed to read, " + e, e);
        }
    }

    public boolean isValid(WifiConfiguration config) {
        if (config.allowedKeyManagement == null) {
            return false;
        }
        if (config.allowedKeyManagement.cardinality() > 1) {
            if (config.allowedKeyManagement.cardinality() != 2 || !config.allowedKeyManagement.get(2)) {
                return false;
            }
            if (config.allowedKeyManagement.get(3) || config.allowedKeyManagement.get(1)) {
                return true;
            }
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static String makeString(BitSet set, String[] strings) {
        StringBuffer buf = new StringBuffer();
        int nextSetBit = -1;
        BitSet set2 = set.get(0, strings.length);
        while (true) {
            int nextSetBit2 = set2.nextSetBit(nextSetBit + 1);
            nextSetBit = nextSetBit2;
            if (nextSetBit2 == -1) {
                break;
            }
            buf.append(strings[nextSetBit].replace('_', '-'));
            buf.append(' ');
        }
        if (set2.cardinality() > 0) {
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }

    /* access modifiers changed from: protected */
    public void logv(String s) {
        Log.v(TAG, s);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Log.d(TAG, s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Log.d(TAG, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        loge(s, false);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, boolean stack) {
        if (stack) {
            Log.e(TAG, s + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
            return;
        }
        Log.e(TAG, s);
    }

    /* access modifiers changed from: private */
    public ScanDetailCache getScanDetailCache(WifiConfiguration config, Map<Integer, ScanDetailCache> scanDetailCaches) {
        if (config == null || scanDetailCaches == null) {
            return null;
        }
        ScanDetailCache cache = scanDetailCaches.get(Integer.valueOf(config.networkId));
        if (cache == null && config.networkId != -1) {
            cache = new ScanDetailCache(config, WifiConfigManager.SCAN_CACHE_ENTRIES_MAX_SIZE, 128);
            scanDetailCaches.put(Integer.valueOf(config.networkId), cache);
        }
        return cache;
    }
}
