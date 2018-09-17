package com.android.server.wifi.aware;

import android.hardware.wifi.V1_0.IWifiNanIfaceEventCallback.Stub;
import android.hardware.wifi.V1_0.NanCapabilities;
import android.hardware.wifi.V1_0.NanClusterEventInd;
import android.hardware.wifi.V1_0.NanDataPathConfirmInd;
import android.hardware.wifi.V1_0.NanDataPathRequestInd;
import android.hardware.wifi.V1_0.NanFollowupReceivedInd;
import android.hardware.wifi.V1_0.NanMatchInd;
import android.hardware.wifi.V1_0.WifiNanStatus;
import android.util.Log;
import java.util.ArrayList;

public class WifiAwareNativeCallback extends Stub {
    private static final boolean DBG = false;
    private static final String TAG = "WifiAwareNativeCallback";
    private static final boolean VDBG = false;
    private final WifiAwareStateManager mWifiAwareStateManager;

    public WifiAwareNativeCallback(WifiAwareStateManager wifiAwareStateManager) {
        this.mWifiAwareStateManager = wifiAwareStateManager;
    }

    public void notifyCapabilitiesResponse(short id, WifiNanStatus status, NanCapabilities capabilities) {
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
        if (status.status == 0) {
            this.mWifiAwareStateManager.onConfigSuccessResponse(id);
        } else {
            this.mWifiAwareStateManager.onConfigFailedResponse(id, status.status);
        }
    }

    public void notifyConfigResponse(short id, WifiNanStatus status) {
        if (status.status == 0) {
            this.mWifiAwareStateManager.onConfigSuccessResponse(id);
        } else {
            this.mWifiAwareStateManager.onConfigFailedResponse(id, status.status);
        }
    }

    public void notifyDisableResponse(short id, WifiNanStatus status) {
        if (status.status != 0) {
            Log.e(TAG, "notifyDisableResponse: failure - code=" + status.status + " (" + status.description + ")");
        }
    }

    public void notifyStartPublishResponse(short id, WifiNanStatus status, byte publishId) {
        if (status.status == 0) {
            this.mWifiAwareStateManager.onSessionConfigSuccessResponse(id, true, publishId);
        } else {
            this.mWifiAwareStateManager.onSessionConfigFailResponse(id, true, status.status);
        }
    }

    public void notifyStopPublishResponse(short id, WifiNanStatus status) {
        if (status.status != 0) {
            Log.e(TAG, "notifyStopPublishResponse: failure - code=" + status.status + " (" + status.description + ")");
        }
    }

    public void notifyStartSubscribeResponse(short id, WifiNanStatus status, byte subscribeId) {
        if (status.status == 0) {
            this.mWifiAwareStateManager.onSessionConfigSuccessResponse(id, false, subscribeId);
        } else {
            this.mWifiAwareStateManager.onSessionConfigFailResponse(id, false, status.status);
        }
    }

    public void notifyStopSubscribeResponse(short id, WifiNanStatus status) {
        if (status.status != 0) {
            Log.e(TAG, "notifyStopSubscribeResponse: failure - code=" + status.status + " (" + status.description + ")");
        }
    }

    public void notifyTransmitFollowupResponse(short id, WifiNanStatus status) {
        if (status.status == 0) {
            this.mWifiAwareStateManager.onMessageSendQueuedSuccessResponse(id);
        } else {
            this.mWifiAwareStateManager.onMessageSendQueuedFailResponse(id, status.status);
        }
    }

    public void notifyCreateDataInterfaceResponse(short id, WifiNanStatus status) {
        boolean z = false;
        WifiAwareStateManager wifiAwareStateManager = this.mWifiAwareStateManager;
        if (status.status == 0) {
            z = true;
        }
        wifiAwareStateManager.onCreateDataPathInterfaceResponse(id, z, status.status);
    }

    public void notifyDeleteDataInterfaceResponse(short id, WifiNanStatus status) {
        boolean z = false;
        WifiAwareStateManager wifiAwareStateManager = this.mWifiAwareStateManager;
        if (status.status == 0) {
            z = true;
        }
        wifiAwareStateManager.onDeleteDataPathInterfaceResponse(id, z, status.status);
    }

    public void notifyInitiateDataPathResponse(short id, WifiNanStatus status, int ndpInstanceId) {
        if (status.status == 0) {
            this.mWifiAwareStateManager.onInitiateDataPathResponseSuccess(id, ndpInstanceId);
        } else {
            this.mWifiAwareStateManager.onInitiateDataPathResponseFail(id, status.status);
        }
    }

    public void notifyRespondToDataPathIndicationResponse(short id, WifiNanStatus status) {
        boolean z = false;
        WifiAwareStateManager wifiAwareStateManager = this.mWifiAwareStateManager;
        if (status.status == 0) {
            z = true;
        }
        wifiAwareStateManager.onRespondToDataPathSetupRequestResponse(id, z, status.status);
    }

    public void notifyTerminateDataPathResponse(short id, WifiNanStatus status) {
        boolean z = false;
        WifiAwareStateManager wifiAwareStateManager = this.mWifiAwareStateManager;
        if (status.status == 0) {
            z = true;
        }
        wifiAwareStateManager.onEndDataPathResponse(id, z, status.status);
    }

    public void eventClusterEvent(NanClusterEventInd event) {
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
        this.mWifiAwareStateManager.onAwareDownNotification(status.status);
    }

    public void eventPublishTerminated(byte sessionId, WifiNanStatus status) {
        this.mWifiAwareStateManager.onSessionTerminatedNotification(sessionId, status.status, true);
    }

    public void eventSubscribeTerminated(byte sessionId, WifiNanStatus status) {
        this.mWifiAwareStateManager.onSessionTerminatedNotification(sessionId, status.status, false);
    }

    public void eventMatch(NanMatchInd event) {
        this.mWifiAwareStateManager.onMatchNotification(event.discoverySessionId, event.peerId, event.addr, convertArrayListToNativeByteArray(event.serviceSpecificInfo), convertArrayListToNativeByteArray(event.matchFilter));
    }

    public void eventMatchExpired(byte discoverySessionId, int peerId) {
    }

    public void eventFollowupReceived(NanFollowupReceivedInd event) {
        this.mWifiAwareStateManager.onMessageReceivedNotification(event.discoverySessionId, event.peerId, event.addr, convertArrayListToNativeByteArray(event.serviceSpecificInfo));
    }

    public void eventTransmitFollowup(short id, WifiNanStatus status) {
        if (status.status == 0) {
            this.mWifiAwareStateManager.onMessageSendSuccessNotification(id);
        } else {
            this.mWifiAwareStateManager.onMessageSendFailNotification(id, status.status);
        }
    }

    public void eventDataPathRequest(NanDataPathRequestInd event) {
        this.mWifiAwareStateManager.onDataPathRequestNotification(event.discoverySessionId, event.peerDiscMacAddr, event.ndpInstanceId);
    }

    public void eventDataPathConfirm(NanDataPathConfirmInd event) {
        this.mWifiAwareStateManager.onDataPathConfirmNotification(event.ndpInstanceId, event.peerNdiMacAddr, event.dataPathSetupSuccess, event.status.status, convertArrayListToNativeByteArray(event.appInfo));
    }

    public void eventDataPathTerminated(int ndpInstanceId) {
        this.mWifiAwareStateManager.onDataPathEndNotification(ndpInstanceId);
    }

    private byte[] convertArrayListToNativeByteArray(ArrayList<Byte> from) {
        if (from == null) {
            return null;
        }
        byte[] to = new byte[from.size()];
        for (int i = 0; i < from.size(); i++) {
            to[i] = ((Byte) from.get(i)).byteValue();
        }
        return to;
    }

    private static String statusString(WifiNanStatus status) {
        if (status == null) {
            return "status=null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(status.status).append(" (").append(status.description).append(")");
        return sb.toString();
    }
}
