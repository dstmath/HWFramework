package com.android.server.wifi.aware;

import android.hardware.wifi.V1_0.NanCapabilities;
import android.hardware.wifi.V1_0.NanClusterEventInd;
import android.hardware.wifi.V1_0.NanDataPathConfirmInd;
import android.hardware.wifi.V1_0.NanDataPathRequestInd;
import android.hardware.wifi.V1_0.NanFollowupReceivedInd;
import android.hardware.wifi.V1_0.NanMatchInd;
import android.hardware.wifi.V1_0.WifiNanStatus;
import android.hardware.wifi.V1_2.IWifiNanIfaceEventCallback;
import android.hardware.wifi.V1_2.NanDataPathScheduleUpdateInd;
import android.os.ShellCommand;
import android.util.Log;
import android.util.SparseIntArray;
import com.android.server.wifi.aware.WifiAwareShellCommand;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import libcore.util.HexEncoding;
import org.json.JSONException;
import org.json.JSONObject;

public class WifiAwareNativeCallback extends IWifiNanIfaceEventCallback.Stub implements WifiAwareShellCommand.DelegatedShellCommand {
    private static final int CB_EV_CLUSTER = 0;
    private static final int CB_EV_DATA_PATH_CONFIRM = 9;
    private static final int CB_EV_DATA_PATH_REQUEST = 8;
    private static final int CB_EV_DATA_PATH_SCHED_UPDATE = 11;
    private static final int CB_EV_DATA_PATH_TERMINATED = 10;
    private static final int CB_EV_DISABLED = 1;
    private static final int CB_EV_FOLLOWUP_RECEIVED = 6;
    private static final int CB_EV_MATCH = 4;
    private static final int CB_EV_MATCH_EXPIRED = 5;
    private static final int CB_EV_PUBLISH_TERMINATED = 2;
    private static final int CB_EV_SUBSCRIBE_TERMINATED = 3;
    private static final int CB_EV_TRANSMIT_FOLLOWUP = 7;
    private static final String TAG = "WifiAwareNativeCallback";
    private static final boolean VDBG = false;
    private SparseIntArray mCallbackCounter = new SparseIntArray();
    boolean mDbg = false;
    boolean mIsHal12OrLater = false;
    private final WifiAwareStateManager mWifiAwareStateManager;

    public WifiAwareNativeCallback(WifiAwareStateManager wifiAwareStateManager) {
        this.mWifiAwareStateManager = wifiAwareStateManager;
    }

    private void incrementCbCount(int callbackId) {
        this.mCallbackCounter.put(callbackId, this.mCallbackCounter.get(callbackId) + 1);
    }

    public int onCommand(ShellCommand parentShell) {
        PrintWriter pwe = parentShell.getErrPrintWriter();
        PrintWriter pwo = parentShell.getOutPrintWriter();
        String subCmd = parentShell.getNextArgRequired();
        if ((subCmd.hashCode() == -1587855368 && subCmd.equals("get_cb_count")) ? false : true) {
            pwe.println("Unknown 'wifiaware native_cb <cmd>'");
            return -1;
        }
        String option = parentShell.getNextOption();
        boolean reset = false;
        if (option != null) {
            if ("--reset".equals(option)) {
                reset = true;
            } else {
                pwe.println("Unknown option to 'get_cb_count'");
                return -1;
            }
        }
        JSONObject j = new JSONObject();
        int i = 0;
        while (i < this.mCallbackCounter.size()) {
            try {
                j.put(Integer.toString(this.mCallbackCounter.keyAt(i)), this.mCallbackCounter.valueAt(i));
                i++;
            } catch (JSONException e) {
                Log.e(TAG, "onCommand: get_cb_count e=" + e);
            }
        }
        pwo.println(j.toString());
        if (reset) {
            this.mCallbackCounter.clear();
        }
        return 0;
    }

    public void onReset() {
    }

    public void onHelp(String command, ShellCommand parentShell) {
        PrintWriter pw = parentShell.getOutPrintWriter();
        pw.println("  " + command);
        pw.println("    get_cb_count [--reset]: gets the number of callbacks (and optionally reset count)");
    }

    public void notifyCapabilitiesResponse(short id, WifiNanStatus status, NanCapabilities capabilities) {
        if (this.mDbg) {
            Log.v(TAG, "notifyCapabilitiesResponse: id=" + id + ", status=" + statusString(status) + ", capabilities=" + capabilities);
        }
        if (status.status == 0) {
            Capabilities frameworkCapabilities = new Capabilities();
            frameworkCapabilities.maxConcurrentAwareClusters = capabilities.maxConcurrentClusters;
            frameworkCapabilities.maxPublishes = capabilities.maxPublishes;
            frameworkCapabilities.maxSubscribes = capabilities.maxSubscribes;
            frameworkCapabilities.maxServiceNameLen = capabilities.maxServiceNameLen;
            frameworkCapabilities.maxMatchFilterLen = capabilities.maxMatchFilterLen;
            frameworkCapabilities.maxTotalMatchFilterLen = capabilities.maxTotalMatchFilterLen;
            frameworkCapabilities.maxServiceSpecificInfoLen = capabilities.maxServiceSpecificInfoLen;
            frameworkCapabilities.maxExtendedServiceSpecificInfoLen = capabilities.maxExtendedServiceSpecificInfoLen;
            frameworkCapabilities.maxNdiInterfaces = capabilities.maxNdiInterfaces;
            frameworkCapabilities.maxNdpSessions = capabilities.maxNdpSessions;
            frameworkCapabilities.maxAppInfoLen = capabilities.maxAppInfoLen;
            frameworkCapabilities.maxQueuedTransmitMessages = capabilities.maxQueuedTransmitFollowupMsgs;
            frameworkCapabilities.maxSubscribeInterfaceAddresses = capabilities.maxSubscribeInterfaceAddresses;
            frameworkCapabilities.supportedCipherSuites = capabilities.supportedCipherSuites;
            this.mWifiAwareStateManager.onCapabilitiesUpdateResponse(id, frameworkCapabilities);
            return;
        }
        Log.e(TAG, "notifyCapabilitiesResponse: error code=" + status.status + " (" + status.description + ")");
    }

    public void notifyEnableResponse(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "notifyEnableResponse: id=" + id + ", status=" + statusString(status));
        }
        if (status.status == 10) {
            Log.wtf(TAG, "notifyEnableResponse: id=" + id + ", already enabled!?");
        }
        if (status.status == 0 || status.status == 10) {
            this.mWifiAwareStateManager.onConfigSuccessResponse(id);
        } else {
            this.mWifiAwareStateManager.onConfigFailedResponse(id, status.status);
        }
    }

    public void notifyConfigResponse(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "notifyConfigResponse: id=" + id + ", status=" + statusString(status));
        }
        if (status.status == 0) {
            this.mWifiAwareStateManager.onConfigSuccessResponse(id);
        } else {
            this.mWifiAwareStateManager.onConfigFailedResponse(id, status.status);
        }
    }

    public void notifyDisableResponse(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "notifyDisableResponse: id=" + id + ", status=" + statusString(status));
        }
        if (status.status != 0) {
            Log.e(TAG, "notifyDisableResponse: failure - code=" + status.status + " (" + status.description + ")");
        }
        this.mWifiAwareStateManager.onDisableResponse(id, status.status);
    }

    public void notifyStartPublishResponse(short id, WifiNanStatus status, byte publishId) {
        if (this.mDbg) {
            Log.v(TAG, "notifyStartPublishResponse: id=" + id + ", status=" + statusString(status) + ", publishId=" + publishId);
        }
        if (status.status == 0) {
            this.mWifiAwareStateManager.onSessionConfigSuccessResponse(id, true, publishId);
        } else {
            this.mWifiAwareStateManager.onSessionConfigFailResponse(id, true, status.status);
        }
    }

    public void notifyStopPublishResponse(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "notifyStopPublishResponse: id=" + id + ", status=" + statusString(status));
        }
        if (status.status != 0) {
            Log.e(TAG, "notifyStopPublishResponse: failure - code=" + status.status + " (" + status.description + ")");
        }
    }

    public void notifyStartSubscribeResponse(short id, WifiNanStatus status, byte subscribeId) {
        if (this.mDbg) {
            Log.v(TAG, "notifyStartSubscribeResponse: id=" + id + ", status=" + statusString(status) + ", subscribeId=" + subscribeId);
        }
        if (status.status == 0) {
            this.mWifiAwareStateManager.onSessionConfigSuccessResponse(id, false, subscribeId);
        } else {
            this.mWifiAwareStateManager.onSessionConfigFailResponse(id, false, status.status);
        }
    }

    public void notifyStopSubscribeResponse(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "notifyStopSubscribeResponse: id=" + id + ", status=" + statusString(status));
        }
        if (status.status != 0) {
            Log.e(TAG, "notifyStopSubscribeResponse: failure - code=" + status.status + " (" + status.description + ")");
        }
    }

    public void notifyTransmitFollowupResponse(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "notifyTransmitFollowupResponse: id=" + id + ", status=" + statusString(status));
        }
        if (status.status == 0) {
            this.mWifiAwareStateManager.onMessageSendQueuedSuccessResponse(id);
        } else {
            this.mWifiAwareStateManager.onMessageSendQueuedFailResponse(id, status.status);
        }
    }

    public void notifyCreateDataInterfaceResponse(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "notifyCreateDataInterfaceResponse: id=" + id + ", status=" + statusString(status));
        }
        this.mWifiAwareStateManager.onCreateDataPathInterfaceResponse(id, status.status == 0, status.status);
    }

    public void notifyDeleteDataInterfaceResponse(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "notifyDeleteDataInterfaceResponse: id=" + id + ", status=" + statusString(status));
        }
        this.mWifiAwareStateManager.onDeleteDataPathInterfaceResponse(id, status.status == 0, status.status);
    }

    public void notifyInitiateDataPathResponse(short id, WifiNanStatus status, int ndpInstanceId) {
        if (this.mDbg) {
            Log.v(TAG, "notifyInitiateDataPathResponse: id=" + id + ", status=" + statusString(status) + ", ndpInstanceId=" + ndpInstanceId);
        }
        if (status.status == 0) {
            this.mWifiAwareStateManager.onInitiateDataPathResponseSuccess(id, ndpInstanceId);
        } else {
            this.mWifiAwareStateManager.onInitiateDataPathResponseFail(id, status.status);
        }
    }

    public void notifyRespondToDataPathIndicationResponse(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "notifyRespondToDataPathIndicationResponse: id=" + id + ", status=" + statusString(status));
        }
        this.mWifiAwareStateManager.onRespondToDataPathSetupRequestResponse(id, status.status == 0, status.status);
    }

    public void notifyTerminateDataPathResponse(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "notifyTerminateDataPathResponse: id=" + id + ", status=" + statusString(status));
        }
        this.mWifiAwareStateManager.onEndDataPathResponse(id, status.status == 0, status.status);
    }

    public void eventClusterEvent(NanClusterEventInd event) {
        if (this.mDbg) {
            Log.v(TAG, "eventClusterEvent: eventType=" + event.eventType + ", addr=" + String.valueOf(HexEncoding.encode(event.addr)));
        }
        incrementCbCount(0);
        if (event.eventType == 0) {
            this.mWifiAwareStateManager.onInterfaceAddressChangeNotification(event.addr);
        } else if (event.eventType == 1) {
            this.mWifiAwareStateManager.onClusterChangeNotification(0, event.addr);
        } else if (event.eventType == 2) {
            this.mWifiAwareStateManager.onClusterChangeNotification(1, event.addr);
        } else {
            Log.e(TAG, "eventClusterEvent: invalid eventType=" + event.eventType);
        }
    }

    public void eventDisabled(WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "eventDisabled: status=" + statusString(status));
        }
        incrementCbCount(1);
        this.mWifiAwareStateManager.onAwareDownNotification(status.status);
    }

    public void eventPublishTerminated(byte sessionId, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "eventPublishTerminated: sessionId=" + sessionId + ", status=" + statusString(status));
        }
        incrementCbCount(2);
        this.mWifiAwareStateManager.onSessionTerminatedNotification(sessionId, status.status, true);
    }

    public void eventSubscribeTerminated(byte sessionId, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "eventSubscribeTerminated: sessionId=" + sessionId + ", status=" + statusString(status));
        }
        incrementCbCount(3);
        this.mWifiAwareStateManager.onSessionTerminatedNotification(sessionId, status.status, false);
    }

    public void eventMatch(NanMatchInd event) {
        if (this.mDbg) {
            StringBuilder sb = new StringBuilder();
            sb.append("eventMatch: discoverySessionId=");
            sb.append(event.discoverySessionId);
            sb.append(", peerId=");
            sb.append(event.peerId);
            sb.append(", addr=");
            sb.append(String.valueOf(HexEncoding.encode(event.addr)));
            sb.append(", serviceSpecificInfo=");
            sb.append(Arrays.toString(convertArrayListToNativeByteArray(event.serviceSpecificInfo)));
            sb.append(", ssi.size()=");
            int i = 0;
            sb.append(event.serviceSpecificInfo == null ? 0 : event.serviceSpecificInfo.size());
            sb.append(", matchFilter=");
            sb.append(Arrays.toString(convertArrayListToNativeByteArray(event.matchFilter)));
            sb.append(", mf.size()=");
            if (event.matchFilter != null) {
                i = event.matchFilter.size();
            }
            sb.append(i);
            sb.append(", rangingIndicationType=");
            sb.append(event.rangingIndicationType);
            sb.append(", rangingMeasurementInCm=");
            sb.append(event.rangingMeasurementInCm);
            Log.v(TAG, sb.toString());
        }
        incrementCbCount(4);
        this.mWifiAwareStateManager.onMatchNotification(event.discoverySessionId, event.peerId, event.addr, convertArrayListToNativeByteArray(event.serviceSpecificInfo), convertArrayListToNativeByteArray(event.matchFilter), event.rangingIndicationType, event.rangingMeasurementInCm * 10);
    }

    public void eventMatchExpired(byte discoverySessionId, int peerId) {
        if (this.mDbg) {
            Log.v(TAG, "eventMatchExpired: discoverySessionId=" + discoverySessionId + ", peerId=" + peerId);
        }
        incrementCbCount(5);
    }

    public void eventFollowupReceived(NanFollowupReceivedInd event) {
        if (this.mDbg) {
            StringBuilder sb = new StringBuilder();
            sb.append("eventFollowupReceived: discoverySessionId=");
            sb.append(event.discoverySessionId);
            sb.append(", peerId=");
            sb.append(event.peerId);
            sb.append(", addr=");
            sb.append(String.valueOf(HexEncoding.encode(event.addr)));
            sb.append(", serviceSpecificInfo=");
            sb.append(Arrays.toString(convertArrayListToNativeByteArray(event.serviceSpecificInfo)));
            sb.append(", ssi.size()=");
            sb.append(event.serviceSpecificInfo == null ? 0 : event.serviceSpecificInfo.size());
            Log.v(TAG, sb.toString());
        }
        incrementCbCount(6);
        this.mWifiAwareStateManager.onMessageReceivedNotification(event.discoverySessionId, event.peerId, event.addr, convertArrayListToNativeByteArray(event.serviceSpecificInfo));
    }

    public void eventTransmitFollowup(short id, WifiNanStatus status) {
        if (this.mDbg) {
            Log.v(TAG, "eventTransmitFollowup: id=" + id + ", status=" + statusString(status));
        }
        incrementCbCount(7);
        if (status.status == 0) {
            this.mWifiAwareStateManager.onMessageSendSuccessNotification(id);
        } else {
            this.mWifiAwareStateManager.onMessageSendFailNotification(id, status.status);
        }
    }

    public void eventDataPathRequest(NanDataPathRequestInd event) {
        if (this.mDbg) {
            Log.v(TAG, "eventDataPathRequest: discoverySessionId=" + event.discoverySessionId + ", peerDiscMacAddr=" + String.valueOf(HexEncoding.encode(event.peerDiscMacAddr)) + ", ndpInstanceId=" + event.ndpInstanceId);
        }
        incrementCbCount(8);
        this.mWifiAwareStateManager.onDataPathRequestNotification(event.discoverySessionId, event.peerDiscMacAddr, event.ndpInstanceId);
    }

    public void eventDataPathConfirm(NanDataPathConfirmInd event) {
        if (this.mDbg) {
            Log.v(TAG, "onDataPathConfirm: ndpInstanceId=" + event.ndpInstanceId + ", peerNdiMacAddr=" + String.valueOf(HexEncoding.encode(event.peerNdiMacAddr)) + ", dataPathSetupSuccess=" + event.dataPathSetupSuccess + ", reason=" + event.status.status);
        }
        if (this.mIsHal12OrLater) {
            Log.wtf(TAG, "eventDataPathConfirm should not be called by a >=1.2 HAL!");
        }
        incrementCbCount(9);
        this.mWifiAwareStateManager.onDataPathConfirmNotification(event.ndpInstanceId, event.peerNdiMacAddr, event.dataPathSetupSuccess, event.status.status, convertArrayListToNativeByteArray(event.appInfo), null);
    }

    public void eventDataPathConfirm_1_2(android.hardware.wifi.V1_2.NanDataPathConfirmInd event) {
        if (this.mDbg) {
            Log.v(TAG, "eventDataPathConfirm_1_2: ndpInstanceId=" + event.V1_0.ndpInstanceId + ", peerNdiMacAddr=" + String.valueOf(HexEncoding.encode(event.V1_0.peerNdiMacAddr)) + ", dataPathSetupSuccess=" + event.V1_0.dataPathSetupSuccess + ", reason=" + event.V1_0.status.status);
        }
        if (!this.mIsHal12OrLater) {
            Log.wtf(TAG, "eventDataPathConfirm_1_2 should not be called by a <1.2 HAL!");
            return;
        }
        incrementCbCount(9);
        this.mWifiAwareStateManager.onDataPathConfirmNotification(event.V1_0.ndpInstanceId, event.V1_0.peerNdiMacAddr, event.V1_0.dataPathSetupSuccess, event.V1_0.status.status, convertArrayListToNativeByteArray(event.V1_0.appInfo), event.channelInfo);
    }

    public void eventDataPathScheduleUpdate(NanDataPathScheduleUpdateInd event) {
        if (this.mDbg) {
            Log.v(TAG, "eventDataPathScheduleUpdate");
        }
        if (!this.mIsHal12OrLater) {
            Log.wtf(TAG, "eventDataPathScheduleUpdate should not be called by a <1.2 HAL!");
            return;
        }
        incrementCbCount(11);
        this.mWifiAwareStateManager.onDataPathScheduleUpdateNotification(event.peerDiscoveryAddress, event.ndpInstanceIds, event.channelInfo);
    }

    public void eventDataPathTerminated(int ndpInstanceId) {
        if (this.mDbg) {
            Log.v(TAG, "eventDataPathTerminated: ndpInstanceId=" + ndpInstanceId);
        }
        incrementCbCount(10);
        this.mWifiAwareStateManager.onDataPathEndNotification(ndpInstanceId);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("WifiAwareNativeCallback:");
        pw.println("  mCallbackCounter: " + this.mCallbackCounter);
    }

    private byte[] convertArrayListToNativeByteArray(ArrayList<Byte> from) {
        if (from == null) {
            return null;
        }
        byte[] to = new byte[from.size()];
        for (int i = 0; i < from.size(); i++) {
            to[i] = from.get(i).byteValue();
        }
        return to;
    }

    private static String statusString(WifiNanStatus status) {
        if (status == null) {
            return "status=null";
        }
        return status.status + " (" + status.description + ")";
    }
}
