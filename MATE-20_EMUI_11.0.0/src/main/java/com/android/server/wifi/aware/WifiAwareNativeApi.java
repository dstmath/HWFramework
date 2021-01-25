package com.android.server.wifi.aware;

import android.hardware.wifi.V1_0.NanBandSpecificConfig;
import android.hardware.wifi.V1_0.NanConfigRequest;
import android.hardware.wifi.V1_0.NanEnableRequest;
import android.hardware.wifi.V1_0.NanInitiateDataPathRequest;
import android.hardware.wifi.V1_0.NanPublishRequest;
import android.hardware.wifi.V1_0.NanRespondToDataPathIndicationRequest;
import android.hardware.wifi.V1_0.NanSubscribeRequest;
import android.hardware.wifi.V1_0.NanTransmitFollowupRequest;
import android.hardware.wifi.V1_0.WifiStatus;
import android.hardware.wifi.V1_2.IWifiNanIface;
import android.hardware.wifi.V1_2.NanConfigRequestSupplemental;
import android.net.wifi.aware.ConfigRequest;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.SubscribeConfig;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.os.ShellCommand;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.aware.WifiAwareShellCommand;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import libcore.util.HexEncoding;

public class WifiAwareNativeApi implements WifiAwareShellCommand.DelegatedShellCommand {
    static final String PARAM_DISCOVERY_BEACON_INTERVAL_MS = "disc_beacon_interval_ms";
    private static final int PARAM_DISCOVERY_BEACON_INTERVAL_MS_DEFAULT = 0;
    private static final int PARAM_DISCOVERY_BEACON_INTERVAL_MS_IDLE = 0;
    private static final int PARAM_DISCOVERY_BEACON_INTERVAL_MS_INACTIVE = 0;
    static final String PARAM_DW_24GHZ = "dw_24ghz";
    private static final int PARAM_DW_24GHZ_DEFAULT = 1;
    private static final int PARAM_DW_24GHZ_IDLE = 4;
    private static final int PARAM_DW_24GHZ_INACTIVE = 4;
    static final String PARAM_DW_5GHZ = "dw_5ghz";
    private static final int PARAM_DW_5GHZ_DEFAULT = 1;
    private static final int PARAM_DW_5GHZ_IDLE = 0;
    private static final int PARAM_DW_5GHZ_INACTIVE = 0;
    static final String PARAM_ENABLE_DW_EARLY_TERM = "enable_dw_early_term";
    private static final int PARAM_ENABLE_DW_EARLY_TERM_DEFAULT = 0;
    private static final int PARAM_ENABLE_DW_EARLY_TERM_IDLE = 0;
    private static final int PARAM_ENABLE_DW_EARLY_TERM_INACTIVE = 0;
    static final String PARAM_MAC_RANDOM_INTERVAL_SEC = "mac_random_interval_sec";
    private static final int PARAM_MAC_RANDOM_INTERVAL_SEC_DEFAULT = 1800;
    static final String PARAM_NUM_SS_IN_DISCOVERY = "num_ss_in_discovery";
    private static final int PARAM_NUM_SS_IN_DISCOVERY_DEFAULT = 0;
    private static final int PARAM_NUM_SS_IN_DISCOVERY_IDLE = 0;
    private static final int PARAM_NUM_SS_IN_DISCOVERY_INACTIVE = 0;
    static final String POWER_PARAM_DEFAULT_KEY = "default";
    static final String POWER_PARAM_IDLE_KEY = "idle";
    static final String POWER_PARAM_INACTIVE_KEY = "inactive";
    @VisibleForTesting
    static final String SERVICE_NAME_FOR_OOB_DATA_PATH = "Wi-Fi Aware Data Path";
    private static final String TAG = "WifiAwareNativeApi";
    private static final boolean VDBG = false;
    boolean mDbg = false;
    private final WifiAwareNativeManager mHal;
    private Map<String, Integer> mSettableParameters = new HashMap();
    private Map<String, Map<String, Integer>> mSettablePowerParameters = new HashMap();
    private SparseIntArray mTransactionIds;

    public WifiAwareNativeApi(WifiAwareNativeManager wifiAwareNativeManager) {
        this.mHal = wifiAwareNativeManager;
        onReset();
    }

    private void recordTransactionId(int transactionId) {
    }

    public IWifiNanIface mockableCastTo_1_2(android.hardware.wifi.V1_0.IWifiNanIface iface) {
        return IWifiNanIface.castFrom((IHwInterface) iface);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.android.server.wifi.aware.WifiAwareShellCommand.DelegatedShellCommand
    public int onCommand(ShellCommand parentShell) {
        boolean z;
        PrintWriter pw = parentShell.getErrPrintWriter();
        String subCmd = parentShell.getNextArgRequired();
        switch (subCmd.hashCode()) {
            case -502265894:
                if (subCmd.equals("set-power")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -287648818:
                if (subCmd.equals("get-power")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 102230:
                if (subCmd.equals("get")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 113762:
                if (subCmd.equals("set")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            String name = parentShell.getNextArgRequired();
            if (!this.mSettableParameters.containsKey(name)) {
                pw.println("Unknown parameter name -- '" + name + "'");
                return -1;
            }
            String valueStr = parentShell.getNextArgRequired();
            try {
                this.mSettableParameters.put(name, Integer.valueOf(Integer.valueOf(valueStr).intValue()));
                return 0;
            } catch (NumberFormatException e) {
                pw.println("Can't convert value to integer -- '" + valueStr + "'");
                return -1;
            }
        } else if (z) {
            String mode = parentShell.getNextArgRequired();
            String name2 = parentShell.getNextArgRequired();
            String valueStr2 = parentShell.getNextArgRequired();
            if (!this.mSettablePowerParameters.containsKey(mode)) {
                pw.println("Unknown mode name -- '" + mode + "'");
                return -1;
            } else if (!this.mSettablePowerParameters.get(mode).containsKey(name2)) {
                pw.println("Unknown parameter name '" + name2 + "' in mode '" + mode + "'");
                return -1;
            } else {
                try {
                    this.mSettablePowerParameters.get(mode).put(name2, Integer.valueOf(Integer.valueOf(valueStr2).intValue()));
                    return 0;
                } catch (NumberFormatException e2) {
                    pw.println("Can't convert value to integer -- '" + valueStr2 + "'");
                    return -1;
                }
            }
        } else if (z) {
            String name3 = parentShell.getNextArgRequired();
            if (!this.mSettableParameters.containsKey(name3)) {
                pw.println("Unknown parameter name -- '" + name3 + "'");
                return -1;
            }
            parentShell.getOutPrintWriter().println(this.mSettableParameters.get(name3).intValue());
            return 0;
        } else if (!z) {
            pw.println("Unknown 'wifiaware native_api <cmd>'");
            return -1;
        } else {
            String mode2 = parentShell.getNextArgRequired();
            String name4 = parentShell.getNextArgRequired();
            if (!this.mSettablePowerParameters.containsKey(mode2)) {
                pw.println("Unknown mode -- '" + mode2 + "'");
                return -1;
            } else if (!this.mSettablePowerParameters.get(mode2).containsKey(name4)) {
                pw.println("Unknown parameter name -- '" + name4 + "' in mode '" + mode2 + "'");
                return -1;
            } else {
                parentShell.getOutPrintWriter().println(this.mSettablePowerParameters.get(mode2).get(name4).intValue());
                return 0;
            }
        }
    }

    @Override // com.android.server.wifi.aware.WifiAwareShellCommand.DelegatedShellCommand
    public void onReset() {
        Map<String, Integer> defaultMap = new HashMap<>();
        defaultMap.put(PARAM_DW_24GHZ, 1);
        defaultMap.put(PARAM_DW_5GHZ, 1);
        defaultMap.put(PARAM_DISCOVERY_BEACON_INTERVAL_MS, 0);
        defaultMap.put(PARAM_NUM_SS_IN_DISCOVERY, 0);
        defaultMap.put(PARAM_ENABLE_DW_EARLY_TERM, 0);
        Map<String, Integer> inactiveMap = new HashMap<>();
        inactiveMap.put(PARAM_DW_24GHZ, 4);
        inactiveMap.put(PARAM_DW_5GHZ, 0);
        inactiveMap.put(PARAM_DISCOVERY_BEACON_INTERVAL_MS, 0);
        inactiveMap.put(PARAM_NUM_SS_IN_DISCOVERY, 0);
        inactiveMap.put(PARAM_ENABLE_DW_EARLY_TERM, 0);
        Map<String, Integer> idleMap = new HashMap<>();
        idleMap.put(PARAM_DW_24GHZ, 4);
        idleMap.put(PARAM_DW_5GHZ, 0);
        idleMap.put(PARAM_DISCOVERY_BEACON_INTERVAL_MS, 0);
        idleMap.put(PARAM_NUM_SS_IN_DISCOVERY, 0);
        idleMap.put(PARAM_ENABLE_DW_EARLY_TERM, 0);
        this.mSettablePowerParameters.put("default", defaultMap);
        this.mSettablePowerParameters.put(POWER_PARAM_INACTIVE_KEY, inactiveMap);
        this.mSettablePowerParameters.put(POWER_PARAM_IDLE_KEY, idleMap);
        this.mSettableParameters.put(PARAM_MAC_RANDOM_INTERVAL_SEC, Integer.valueOf((int) PARAM_MAC_RANDOM_INTERVAL_SEC_DEFAULT));
    }

    @Override // com.android.server.wifi.aware.WifiAwareShellCommand.DelegatedShellCommand
    public void onHelp(String command, ShellCommand parentShell) {
        PrintWriter pw = parentShell.getOutPrintWriter();
        pw.println("  " + command);
        pw.println("    set <name> <value>: sets named parameter to value. Names: " + this.mSettableParameters.keySet());
        pw.println("    set-power <mode> <name> <value>: sets named power parameter to value. Modes: " + this.mSettablePowerParameters.keySet() + ", Names: " + this.mSettablePowerParameters.get("default").keySet());
        StringBuilder sb = new StringBuilder();
        sb.append("    get <name>: gets named parameter value. Names: ");
        sb.append(this.mSettableParameters.keySet());
        pw.println(sb.toString());
        pw.println("    get-power <mode> <name>: gets named parameter value. Modes: " + this.mSettablePowerParameters.keySet() + ", Names: " + this.mSettablePowerParameters.get("default").keySet());
    }

    public boolean getCapabilities(short transactionId) {
        if (this.mDbg) {
            Log.v(TAG, "getCapabilities: transactionId=" + ((int) transactionId));
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "getCapabilities: null interface");
            return false;
        }
        try {
            WifiStatus status = iface.getCapabilitiesRequest(transactionId);
            if (status.code == 0) {
                return true;
            }
            Log.e(TAG, "getCapabilities: error: " + statusString(status));
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "getCapabilities: exception: " + e);
            return false;
        }
    }

    public boolean enableAndConfigure(short transactionId, ConfigRequest configRequest, boolean notifyIdentityChange, boolean initialConfiguration, boolean isInteractive, boolean isIdle) {
        WifiStatus status;
        if (this.mDbg) {
            Log.v(TAG, "enableAndConfigure: transactionId=" + ((int) transactionId) + ", configRequest=" + configRequest + ", notifyIdentityChange=" + notifyIdentityChange + ", initialConfiguration=" + initialConfiguration + ", isInteractive=" + isInteractive + ", isIdle=" + isIdle);
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "enableAndConfigure: null interface");
            return false;
        }
        IWifiNanIface iface12 = mockableCastTo_1_2(iface);
        NanConfigRequestSupplemental configSupplemental12 = new NanConfigRequestSupplemental();
        if (iface12 != null) {
            configSupplemental12.discoveryBeaconIntervalMs = 0;
            configSupplemental12.numberOfSpatialStreamsInDiscovery = 0;
            configSupplemental12.enableDiscoveryWindowEarlyTermination = false;
            configSupplemental12.enableRanging = true;
        }
        if (initialConfiguration) {
            try {
                NanEnableRequest req = new NanEnableRequest();
                req.operateInBand[0] = true;
                req.operateInBand[1] = configRequest.mSupport5gBand;
                req.hopCountMax = 2;
                req.configParams.masterPref = (byte) configRequest.mMasterPreference;
                req.configParams.disableDiscoveryAddressChangeIndication = !notifyIdentityChange;
                req.configParams.disableStartedClusterIndication = !notifyIdentityChange;
                req.configParams.disableJoinedClusterIndication = !notifyIdentityChange;
                req.configParams.includePublishServiceIdsInBeacon = true;
                req.configParams.numberOfPublishServiceIdsInBeacon = 0;
                req.configParams.includeSubscribeServiceIdsInBeacon = true;
                req.configParams.numberOfSubscribeServiceIdsInBeacon = 0;
                req.configParams.rssiWindowSize = 8;
                req.configParams.macAddressRandomizationIntervalSec = this.mSettableParameters.get(PARAM_MAC_RANDOM_INTERVAL_SEC).intValue();
                NanBandSpecificConfig config24 = new NanBandSpecificConfig();
                config24.rssiClose = 60;
                config24.rssiMiddle = 70;
                config24.rssiCloseProximity = 60;
                config24.dwellTimeMs = -56;
                config24.scanPeriodSec = 20;
                if (configRequest.mDiscoveryWindowInterval[0] == -1) {
                    config24.validDiscoveryWindowIntervalVal = false;
                } else {
                    config24.validDiscoveryWindowIntervalVal = true;
                    config24.discoveryWindowIntervalVal = (byte) configRequest.mDiscoveryWindowInterval[0];
                }
                req.configParams.bandSpecificConfig[0] = config24;
                NanBandSpecificConfig config5 = new NanBandSpecificConfig();
                config5.rssiClose = 60;
                config5.rssiMiddle = 75;
                config5.rssiCloseProximity = 60;
                config5.dwellTimeMs = -56;
                config5.scanPeriodSec = 20;
                if (configRequest.mDiscoveryWindowInterval[1] == -1) {
                    config5.validDiscoveryWindowIntervalVal = false;
                } else {
                    config5.validDiscoveryWindowIntervalVal = true;
                    config5.discoveryWindowIntervalVal = (byte) configRequest.mDiscoveryWindowInterval[1];
                }
                req.configParams.bandSpecificConfig[1] = config5;
                req.debugConfigs.validClusterIdVals = true;
                req.debugConfigs.clusterIdTopRangeVal = (short) configRequest.mClusterHigh;
                req.debugConfigs.clusterIdBottomRangeVal = (short) configRequest.mClusterLow;
                req.debugConfigs.validIntfAddrVal = false;
                req.debugConfigs.validOuiVal = false;
                req.debugConfigs.ouiVal = 0;
                req.debugConfigs.validRandomFactorForceVal = false;
                req.debugConfigs.randomFactorForceVal = 0;
                req.debugConfigs.validHopCountForceVal = false;
                req.debugConfigs.hopCountForceVal = 0;
                req.debugConfigs.validDiscoveryChannelVal = false;
                req.debugConfigs.discoveryChannelMhzVal[0] = 0;
                req.debugConfigs.discoveryChannelMhzVal[1] = 0;
                req.debugConfigs.validUseBeaconsInBandVal = false;
                req.debugConfigs.useBeaconsInBandVal[0] = true;
                req.debugConfigs.useBeaconsInBandVal[1] = true;
                req.debugConfigs.validUseSdfInBandVal = false;
                req.debugConfigs.useSdfInBandVal[0] = true;
                req.debugConfigs.useSdfInBandVal[1] = true;
                updateConfigForPowerSettings(req.configParams, configSupplemental12, isInteractive, isIdle);
                if (iface12 != null) {
                    status = iface12.enableRequest_1_2(transactionId, req, configSupplemental12);
                } else {
                    status = iface.enableRequest(transactionId, req);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "enableAndConfigure: exception: " + e);
                return false;
            }
        } else {
            NanConfigRequest req2 = new NanConfigRequest();
            req2.masterPref = (byte) configRequest.mMasterPreference;
            req2.disableDiscoveryAddressChangeIndication = !notifyIdentityChange;
            req2.disableStartedClusterIndication = !notifyIdentityChange;
            req2.disableJoinedClusterIndication = !notifyIdentityChange;
            req2.includePublishServiceIdsInBeacon = true;
            req2.numberOfPublishServiceIdsInBeacon = 0;
            req2.includeSubscribeServiceIdsInBeacon = true;
            req2.numberOfSubscribeServiceIdsInBeacon = 0;
            req2.rssiWindowSize = 8;
            req2.macAddressRandomizationIntervalSec = this.mSettableParameters.get(PARAM_MAC_RANDOM_INTERVAL_SEC).intValue();
            NanBandSpecificConfig config242 = new NanBandSpecificConfig();
            config242.rssiClose = 60;
            config242.rssiMiddle = 70;
            config242.rssiCloseProximity = 60;
            config242.dwellTimeMs = -56;
            config242.scanPeriodSec = 20;
            if (configRequest.mDiscoveryWindowInterval[0] == -1) {
                config242.validDiscoveryWindowIntervalVal = false;
            } else {
                config242.validDiscoveryWindowIntervalVal = true;
                config242.discoveryWindowIntervalVal = (byte) configRequest.mDiscoveryWindowInterval[0];
            }
            req2.bandSpecificConfig[0] = config242;
            NanBandSpecificConfig config52 = new NanBandSpecificConfig();
            config52.rssiClose = 60;
            config52.rssiMiddle = 75;
            config52.rssiCloseProximity = 60;
            config52.dwellTimeMs = -56;
            config52.scanPeriodSec = 20;
            if (configRequest.mDiscoveryWindowInterval[1] == -1) {
                config52.validDiscoveryWindowIntervalVal = false;
            } else {
                config52.validDiscoveryWindowIntervalVal = true;
                config52.discoveryWindowIntervalVal = (byte) configRequest.mDiscoveryWindowInterval[1];
            }
            req2.bandSpecificConfig[1] = config52;
            updateConfigForPowerSettings(req2, configSupplemental12, isInteractive, isIdle);
            if (iface12 != null) {
                status = iface12.configRequest_1_2(transactionId, req2, configSupplemental12);
            } else {
                status = iface.configRequest(transactionId, req2);
            }
        }
        if (status.code == 0) {
            return true;
        }
        Log.e(TAG, "enableAndConfigure: error: " + statusString(status));
        return false;
    }

    public boolean disable(short transactionId) {
        if (this.mDbg) {
            Log.d(TAG, "disable");
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "disable: null interface");
            return false;
        }
        try {
            WifiStatus status = iface.disableRequest(transactionId);
            if (status.code == 0) {
                return true;
            }
            Log.e(TAG, "disable: error: " + statusString(status));
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "disable: exception: " + e);
            return false;
        }
    }

    public boolean publish(short transactionId, byte publishId, PublishConfig publishConfig) {
        if (this.mDbg) {
            Log.d(TAG, "publish: transactionId=" + ((int) transactionId) + ", publishId=" + ((int) publishId) + ", config=" + publishConfig);
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "publish: null interface");
            return false;
        }
        NanPublishRequest req = new NanPublishRequest();
        req.baseConfigs.sessionId = publishId;
        req.baseConfigs.ttlSec = (short) publishConfig.mTtlSec;
        req.baseConfigs.discoveryWindowPeriod = 1;
        req.baseConfigs.discoveryCount = 0;
        convertNativeByteArrayToArrayList(publishConfig.mServiceName, req.baseConfigs.serviceName);
        req.baseConfigs.discoveryMatchIndicator = 2;
        convertNativeByteArrayToArrayList(publishConfig.mServiceSpecificInfo, req.baseConfigs.serviceSpecificInfo);
        convertNativeByteArrayToArrayList(publishConfig.mMatchFilter, publishConfig.mPublishType == 0 ? req.baseConfigs.txMatchFilter : req.baseConfigs.rxMatchFilter);
        req.baseConfigs.useRssiThreshold = false;
        req.baseConfigs.disableDiscoveryTerminationIndication = !publishConfig.mEnableTerminateNotification;
        req.baseConfigs.disableMatchExpirationIndication = true;
        req.baseConfigs.disableFollowupReceivedIndication = false;
        req.autoAcceptDataPathRequests = false;
        req.baseConfigs.rangingRequired = publishConfig.mEnableRanging;
        req.baseConfigs.securityConfig.securityType = 0;
        req.publishType = publishConfig.mPublishType;
        req.txType = 0;
        try {
            WifiStatus status = iface.startPublishRequest(transactionId, req);
            if (status.code == 0) {
                return true;
            }
            Log.e(TAG, "publish: error: " + statusString(status));
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "publish: exception: " + e);
            return false;
        }
    }

    public boolean subscribe(short transactionId, byte subscribeId, SubscribeConfig subscribeConfig) {
        if (this.mDbg) {
            Log.d(TAG, "subscribe: transactionId=" + ((int) transactionId) + ", subscribeId=" + ((int) subscribeId) + ", config=" + subscribeConfig);
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "subscribe: null interface");
            return false;
        }
        NanSubscribeRequest req = new NanSubscribeRequest();
        req.baseConfigs.sessionId = subscribeId;
        req.baseConfigs.ttlSec = (short) subscribeConfig.mTtlSec;
        req.baseConfigs.discoveryWindowPeriod = 1;
        req.baseConfigs.discoveryCount = 0;
        convertNativeByteArrayToArrayList(subscribeConfig.mServiceName, req.baseConfigs.serviceName);
        req.baseConfigs.discoveryMatchIndicator = 0;
        convertNativeByteArrayToArrayList(subscribeConfig.mServiceSpecificInfo, req.baseConfigs.serviceSpecificInfo);
        convertNativeByteArrayToArrayList(subscribeConfig.mMatchFilter, subscribeConfig.mSubscribeType == 1 ? req.baseConfigs.txMatchFilter : req.baseConfigs.rxMatchFilter);
        req.baseConfigs.useRssiThreshold = false;
        req.baseConfigs.disableDiscoveryTerminationIndication = !subscribeConfig.mEnableTerminateNotification;
        req.baseConfigs.disableMatchExpirationIndication = true;
        req.baseConfigs.disableFollowupReceivedIndication = false;
        req.baseConfigs.rangingRequired = subscribeConfig.mMinDistanceMmSet || subscribeConfig.mMaxDistanceMmSet;
        req.baseConfigs.configRangingIndications = 0;
        if (subscribeConfig.mMinDistanceMmSet) {
            req.baseConfigs.distanceEgressCm = (short) Math.min(subscribeConfig.mMinDistanceMm / 10, 32767);
            req.baseConfigs.configRangingIndications |= 4;
        }
        if (subscribeConfig.mMaxDistanceMmSet) {
            req.baseConfigs.distanceIngressCm = (short) Math.min(subscribeConfig.mMaxDistanceMm / 10, 32767);
            req.baseConfigs.configRangingIndications |= 2;
        }
        req.baseConfigs.securityConfig.securityType = 0;
        req.subscribeType = subscribeConfig.mSubscribeType;
        try {
            WifiStatus status = iface.startSubscribeRequest(transactionId, req);
            if (status.code == 0) {
                return true;
            }
            Log.e(TAG, "subscribe: error: " + statusString(status));
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "subscribe: exception: " + e);
            return false;
        }
    }

    public boolean sendMessage(short transactionId, byte pubSubId, int requestorInstanceId, byte[] dest, byte[] message, int messageId) {
        Object obj;
        int i;
        if (this.mDbg) {
            StringBuilder sb = new StringBuilder();
            sb.append("sendMessage: transactionId=");
            sb.append((int) transactionId);
            sb.append(", pubSubId=");
            sb.append((int) pubSubId);
            sb.append(", requestorInstanceId=");
            sb.append(requestorInstanceId);
            sb.append(", dest=");
            sb.append(String.valueOf(HexEncoding.encode(dest)));
            sb.append(", messageId=");
            sb.append(messageId);
            sb.append(", message=");
            if (message == null) {
                obj = "<null>";
            } else {
                obj = HexEncoding.encode(message);
            }
            sb.append(obj);
            sb.append(", message.length=");
            if (message == null) {
                i = 0;
            } else {
                i = message.length;
            }
            sb.append(i);
            Log.d(TAG, sb.toString());
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "sendMessage: null interface");
            return false;
        }
        NanTransmitFollowupRequest req = new NanTransmitFollowupRequest();
        req.discoverySessionId = pubSubId;
        req.peerId = requestorInstanceId;
        copyArray(dest, req.addr);
        req.isHighPriority = false;
        req.shouldUseDiscoveryWindow = true;
        convertNativeByteArrayToArrayList(message, req.serviceSpecificInfo);
        req.disableFollowupResultIndication = false;
        try {
            WifiStatus status = iface.transmitFollowupRequest(transactionId, req);
            if (status.code == 0) {
                return true;
            }
            Log.e(TAG, "sendMessage: error: " + statusString(status));
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "sendMessage: exception: " + e);
            return false;
        }
    }

    public boolean stopPublish(short transactionId, byte pubSubId) {
        if (this.mDbg) {
            Log.d(TAG, "stopPublish: transactionId=" + ((int) transactionId) + ", pubSubId=" + ((int) pubSubId));
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "stopPublish: null interface");
            return false;
        }
        try {
            WifiStatus status = iface.stopPublishRequest(transactionId, pubSubId);
            if (status.code == 0) {
                return true;
            }
            Log.e(TAG, "stopPublish: error: " + statusString(status));
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "stopPublish: exception: " + e);
            return false;
        }
    }

    public boolean stopSubscribe(short transactionId, byte pubSubId) {
        if (this.mDbg) {
            Log.d(TAG, "stopSubscribe: transactionId=" + ((int) transactionId) + ", pubSubId=" + ((int) pubSubId));
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "stopSubscribe: null interface");
            return false;
        }
        try {
            WifiStatus status = iface.stopSubscribeRequest(transactionId, pubSubId);
            if (status.code == 0) {
                return true;
            }
            Log.e(TAG, "stopSubscribe: error: " + statusString(status));
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "stopSubscribe: exception: " + e);
            return false;
        }
    }

    public boolean createAwareNetworkInterface(short transactionId, String interfaceName) {
        if (this.mDbg) {
            Log.v(TAG, "createAwareNetworkInterface: transactionId=" + ((int) transactionId) + ", interfaceName=" + interfaceName);
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "createAwareNetworkInterface: null interface");
            return false;
        }
        try {
            WifiStatus status = iface.createDataInterfaceRequest(transactionId, interfaceName);
            if (status.code == 0) {
                return true;
            }
            Log.e(TAG, "createAwareNetworkInterface: error: " + statusString(status));
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "createAwareNetworkInterface: exception: " + e);
            return false;
        }
    }

    public boolean deleteAwareNetworkInterface(short transactionId, String interfaceName) {
        if (this.mDbg) {
            Log.v(TAG, "deleteAwareNetworkInterface: transactionId=" + ((int) transactionId) + ", interfaceName=" + interfaceName);
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "deleteAwareNetworkInterface: null interface");
            return false;
        }
        try {
            WifiStatus status = iface.deleteDataInterfaceRequest(transactionId, interfaceName);
            if (status.code == 0) {
                return true;
            }
            Log.e(TAG, "deleteAwareNetworkInterface: error: " + statusString(status));
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "deleteAwareNetworkInterface: exception: " + e);
            return false;
        }
    }

    public boolean initiateDataPath(short transactionId, int peerId, int channelRequestType, int channel, byte[] peer, String interfaceName, byte[] pmk, String passphrase, boolean isOutOfBand, byte[] appInfo, Capabilities capabilities) {
        if (this.mDbg) {
            StringBuilder sb = new StringBuilder();
            sb.append("initiateDataPath: transactionId=");
            sb.append((int) transactionId);
            sb.append(", peerId=");
            sb.append(peerId);
            sb.append(", channelRequestType=");
            sb.append(channelRequestType);
            sb.append(", channel=");
            sb.append(channel);
            sb.append(", peer=");
            sb.append(String.valueOf(HexEncoding.encode(peer)));
            sb.append(", interfaceName=");
            sb.append(interfaceName);
            sb.append(", pmk=");
            String str = "<*>";
            sb.append(pmk == null ? "<null>" : str);
            sb.append(", passphrase=");
            if (TextUtils.isEmpty(passphrase)) {
                str = "<empty>";
            }
            sb.append(str);
            sb.append(", isOutOfBand=");
            sb.append(isOutOfBand);
            sb.append(", appInfo.length=");
            sb.append(appInfo == null ? 0 : appInfo.length);
            sb.append(", capabilities=");
            sb.append(capabilities);
            Log.v(TAG, sb.toString());
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "initiateDataPath: null interface");
            return false;
        } else if (capabilities == null) {
            Log.e(TAG, "initiateDataPath: null capabilities");
            return false;
        } else {
            NanInitiateDataPathRequest req = new NanInitiateDataPathRequest();
            req.peerId = peerId;
            copyArray(peer, req.peerDiscMacAddr);
            req.channelRequestType = channelRequestType;
            req.channel = channel;
            req.ifaceName = interfaceName;
            req.securityConfig.securityType = 0;
            if (!(pmk == null || pmk.length == 0)) {
                req.securityConfig.cipherType = getStrongestCipherSuiteType(capabilities.supportedCipherSuites);
                req.securityConfig.securityType = 1;
                copyArray(pmk, req.securityConfig.pmk);
            }
            if (!(passphrase == null || passphrase.length() == 0)) {
                req.securityConfig.cipherType = getStrongestCipherSuiteType(capabilities.supportedCipherSuites);
                req.securityConfig.securityType = 2;
                convertNativeByteArrayToArrayList(passphrase.getBytes(), req.securityConfig.passphrase);
            }
            if (req.securityConfig.securityType != 0 && isOutOfBand) {
                convertNativeByteArrayToArrayList(SERVICE_NAME_FOR_OOB_DATA_PATH.getBytes(StandardCharsets.UTF_8), req.serviceNameOutOfBand);
            }
            convertNativeByteArrayToArrayList(appInfo, req.appInfo);
            try {
                WifiStatus status = iface.initiateDataPathRequest(transactionId, req);
                if (status.code == 0) {
                    return true;
                }
                Log.e(TAG, "initiateDataPath: error: " + statusString(status));
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "initiateDataPath: exception: " + e);
                return false;
            }
        }
    }

    public boolean respondToDataPathRequest(short transactionId, boolean accept, int ndpId, String interfaceName, byte[] pmk, String passphrase, byte[] appInfo, boolean isOutOfBand, Capabilities capabilities) {
        if (this.mDbg) {
            StringBuilder sb = new StringBuilder();
            sb.append("respondToDataPathRequest: transactionId=");
            sb.append((int) transactionId);
            sb.append(", accept=");
            sb.append(accept);
            sb.append(", int ndpId=");
            sb.append(ndpId);
            sb.append(", interfaceName=");
            sb.append(interfaceName);
            sb.append(", appInfo.length=");
            sb.append(appInfo == null ? 0 : appInfo.length);
            Log.v(TAG, sb.toString());
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "respondToDataPathRequest: null interface");
            return false;
        } else if (capabilities == null) {
            Log.e(TAG, "initiateDataPath: null capabilities");
            return false;
        } else {
            NanRespondToDataPathIndicationRequest req = new NanRespondToDataPathIndicationRequest();
            req.acceptRequest = accept;
            req.ndpInstanceId = ndpId;
            req.ifaceName = interfaceName;
            req.securityConfig.securityType = 0;
            if (!(pmk == null || pmk.length == 0)) {
                req.securityConfig.cipherType = getStrongestCipherSuiteType(capabilities.supportedCipherSuites);
                req.securityConfig.securityType = 1;
                copyArray(pmk, req.securityConfig.pmk);
            }
            if (!(passphrase == null || passphrase.length() == 0)) {
                req.securityConfig.cipherType = getStrongestCipherSuiteType(capabilities.supportedCipherSuites);
                req.securityConfig.securityType = 2;
                convertNativeByteArrayToArrayList(passphrase.getBytes(), req.securityConfig.passphrase);
            }
            if (req.securityConfig.securityType != 0 && isOutOfBand) {
                convertNativeByteArrayToArrayList(SERVICE_NAME_FOR_OOB_DATA_PATH.getBytes(StandardCharsets.UTF_8), req.serviceNameOutOfBand);
            }
            convertNativeByteArrayToArrayList(appInfo, req.appInfo);
            try {
                WifiStatus status = iface.respondToDataPathIndicationRequest(transactionId, req);
                if (status.code == 0) {
                    return true;
                }
                Log.e(TAG, "respondToDataPathRequest: error: " + statusString(status));
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "respondToDataPathRequest: exception: " + e);
                return false;
            }
        }
    }

    public boolean endDataPath(short transactionId, int ndpId) {
        if (this.mDbg) {
            Log.v(TAG, "endDataPath: transactionId=" + ((int) transactionId) + ", ndpId=" + ndpId);
        }
        recordTransactionId(transactionId);
        android.hardware.wifi.V1_0.IWifiNanIface iface = this.mHal.getWifiNanIface();
        if (iface == null) {
            Log.e(TAG, "endDataPath: null interface");
            return false;
        }
        try {
            WifiStatus status = iface.terminateDataPathRequest(transactionId, ndpId);
            if (status.code == 0) {
                return true;
            }
            Log.e(TAG, "endDataPath: error: " + statusString(status));
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "endDataPath: exception: " + e);
            return false;
        }
    }

    private void updateConfigForPowerSettings(NanConfigRequest req, NanConfigRequestSupplemental configSupplemental12, boolean isInteractive, boolean isIdle) {
        String key = "default";
        if (isIdle) {
            key = POWER_PARAM_IDLE_KEY;
        } else if (!isInteractive) {
            key = POWER_PARAM_INACTIVE_KEY;
        }
        boolean z = true;
        updateSingleConfigForPowerSettings(req.bandSpecificConfig[1], this.mSettablePowerParameters.get(key).get(PARAM_DW_5GHZ).intValue());
        updateSingleConfigForPowerSettings(req.bandSpecificConfig[0], this.mSettablePowerParameters.get(key).get(PARAM_DW_24GHZ).intValue());
        configSupplemental12.discoveryBeaconIntervalMs = this.mSettablePowerParameters.get(key).get(PARAM_DISCOVERY_BEACON_INTERVAL_MS).intValue();
        configSupplemental12.numberOfSpatialStreamsInDiscovery = this.mSettablePowerParameters.get(key).get(PARAM_NUM_SS_IN_DISCOVERY).intValue();
        if (this.mSettablePowerParameters.get(key).get(PARAM_ENABLE_DW_EARLY_TERM).intValue() == 0) {
            z = false;
        }
        configSupplemental12.enableDiscoveryWindowEarlyTermination = z;
    }

    private void updateSingleConfigForPowerSettings(NanBandSpecificConfig cfg, int override) {
        if (override != -1) {
            cfg.validDiscoveryWindowIntervalVal = true;
            cfg.discoveryWindowIntervalVal = (byte) override;
        }
    }

    private int getStrongestCipherSuiteType(int supportedCipherSuites) {
        if ((supportedCipherSuites & 2) != 0) {
            return 2;
        }
        if ((supportedCipherSuites & 1) != 0) {
            return 1;
        }
        return 0;
    }

    private ArrayList<Byte> convertNativeByteArrayToArrayList(byte[] from, ArrayList<Byte> to) {
        if (from == null) {
            from = new byte[0];
        }
        if (to == null) {
            to = new ArrayList<>(from.length);
        } else {
            to.ensureCapacity(from.length);
        }
        for (byte b : from) {
            to.add(Byte.valueOf(b));
        }
        return to;
    }

    private void copyArray(byte[] from, byte[] to) {
        if (from == null || to == null || from.length != to.length) {
            Log.e(TAG, "copyArray error: from=" + from + ", to=" + to);
            return;
        }
        for (int i = 0; i < from.length; i++) {
            to[i] = from[i];
        }
    }

    private static String statusString(WifiStatus status) {
        if (status == null) {
            return "status=null";
        }
        return status.code + " (" + status.description + ")";
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("WifiAwareNativeApi:");
        pw.println("  mSettableParameters: " + this.mSettableParameters);
        this.mHal.dump(fd, pw, args);
    }
}
