package com.android.server.wifi.rtt;

import android.hardware.wifi.V1_0.IWifiRttController;
import android.hardware.wifi.V1_0.IWifiRttControllerEventCallback;
import android.hardware.wifi.V1_0.RttCapabilities;
import android.hardware.wifi.V1_0.RttConfig;
import android.hardware.wifi.V1_0.RttResult;
import android.hardware.wifi.V1_0.WifiStatus;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.ResponderConfig;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wifi.HalDeviceManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ListIterator;

public class RttNative extends IWifiRttControllerEventCallback.Stub {
    private static final String TAG = "RttNative";
    private static final boolean VDBG = false;
    boolean mDbg = false;
    private final HalDeviceManager mHalDeviceManager;
    private volatile IWifiRttController mIWifiRttController;
    private final Object mLock = new Object();
    private volatile RttCapabilities mRttCapabilities;
    private final HalDeviceManager.InterfaceRttControllerLifecycleCallback mRttLifecycleCb = new HalDeviceManager.InterfaceRttControllerLifecycleCallback() {
        /* class com.android.server.wifi.rtt.RttNative.AnonymousClass1 */

        @Override // com.android.server.wifi.HalDeviceManager.InterfaceRttControllerLifecycleCallback
        public void onNewRttController(IWifiRttController controller) {
            if (RttNative.this.mDbg) {
                Log.d(RttNative.TAG, "onNewRttController: controller=" + controller);
            }
            synchronized (RttNative.this.mLock) {
                try {
                    controller.registerEventCallback(RttNative.this);
                    RttNative.this.mIWifiRttController = controller;
                    RttNative.this.mRttService.enableIfPossible();
                    RttNative.this.updateRttCapabilities();
                } catch (RemoteException e) {
                    Log.e(RttNative.TAG, "onNewRttController: exception registering callback: " + e);
                    if (RttNative.this.mIWifiRttController != null) {
                        RttNative.this.mIWifiRttController = null;
                        RttNative.this.mRttService.disable();
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        @Override // com.android.server.wifi.HalDeviceManager.InterfaceRttControllerLifecycleCallback
        public void onRttControllerDestroyed() {
            if (RttNative.this.mDbg) {
                Log.d(RttNative.TAG, "onRttControllerDestroyed");
            }
            synchronized (RttNative.this.mLock) {
                RttNative.this.mIWifiRttController = null;
                RttNative.this.mRttCapabilities = null;
                RttNative.this.mRttService.disable();
            }
        }
    };
    private final RttServiceImpl mRttService;

    public RttNative(RttServiceImpl rttService, HalDeviceManager halDeviceManager) {
        this.mRttService = rttService;
        this.mHalDeviceManager = halDeviceManager;
    }

    public void start(Handler handler) {
        synchronized (this.mLock) {
            this.mHalDeviceManager.initialize();
            this.mHalDeviceManager.registerStatusListener(new HalDeviceManager.ManagerStatusListener(handler) {
                /* class com.android.server.wifi.rtt.$$Lambda$RttNative$rJS9dcQwV7YDCbGMGmH46AVDmws */
                private final /* synthetic */ Handler f$1;

                {
                    this.f$1 = r2;
                }

                @Override // com.android.server.wifi.HalDeviceManager.ManagerStatusListener
                public final void onStatusChanged() {
                    RttNative.this.lambda$start$0$RttNative(this.f$1);
                }
            }, handler);
            if (this.mHalDeviceManager.isStarted()) {
                this.mHalDeviceManager.registerRttControllerLifecycleCallback(this.mRttLifecycleCb, handler);
            }
        }
    }

    public /* synthetic */ void lambda$start$0$RttNative(Handler handler) {
        if (this.mHalDeviceManager.isStarted()) {
            this.mHalDeviceManager.registerRttControllerLifecycleCallback(this.mRttLifecycleCb, handler);
        }
    }

    public boolean isReady() {
        return this.mIWifiRttController != null;
    }

    public RttCapabilities getRttCapabilities() {
        return this.mRttCapabilities;
    }

    /* access modifiers changed from: package-private */
    public void updateRttCapabilities() {
        if (this.mIWifiRttController == null) {
            Log.e(TAG, "updateRttCapabilities: but a RTT controll is NULL!?");
        } else if (this.mRttCapabilities == null) {
            if (this.mDbg) {
                Log.v(TAG, "updateRttCapabilities");
            }
            synchronized (this.mLock) {
                try {
                    this.mIWifiRttController.getCapabilities(new IWifiRttController.getCapabilitiesCallback() {
                        /* class com.android.server.wifi.rtt.$$Lambda$RttNative$nRSOFcP2WhqxmfStf2OeZAekTCY */

                        @Override // android.hardware.wifi.V1_0.IWifiRttController.getCapabilitiesCallback
                        public final void onValues(WifiStatus wifiStatus, RttCapabilities rttCapabilities) {
                            RttNative.this.lambda$updateRttCapabilities$1$RttNative(wifiStatus, rttCapabilities);
                        }
                    });
                } catch (RemoteException e) {
                    Log.e(TAG, "updateRttCapabilities: exception requesting capabilities: " + e);
                }
                if (this.mRttCapabilities != null && !this.mRttCapabilities.rttFtmSupported) {
                    Log.wtf(TAG, "Firmware indicates RTT is not supported - but device supports RTT - ignored!?");
                }
            }
        }
    }

    public /* synthetic */ void lambda$updateRttCapabilities$1$RttNative(WifiStatus status, RttCapabilities capabilities) {
        if (status.code != 0) {
            Log.e(TAG, "updateRttCapabilities: error requesting capabilities -- code=" + status.code);
            return;
        }
        if (this.mDbg) {
            Log.v(TAG, "updateRttCapabilities: RTT capabilities=" + capabilities);
        }
        this.mRttCapabilities = capabilities;
    }

    public boolean rangeRequest(int cmdId, RangingRequest request, boolean isCalledFromPrivilegedContext) {
        if (this.mDbg) {
            Log.v(TAG, "rangeRequest: cmdId=" + cmdId + ", # of requests=" + request.mRttPeers.size());
        }
        synchronized (this.mLock) {
            if (!isReady()) {
                Log.e(TAG, "rangeRequest: RttController is null");
                return false;
            }
            updateRttCapabilities();
            ArrayList<RttConfig> rttConfig = convertRangingRequestToRttConfigs(request, isCalledFromPrivilegedContext, this.mRttCapabilities);
            if (rttConfig == null) {
                Log.e(TAG, "rangeRequest: invalid request parameters");
                return false;
            } else if (rttConfig.size() == 0) {
                Log.e(TAG, "rangeRequest: all requests invalidated");
                this.mRttService.onRangingResults(cmdId, new ArrayList());
                return true;
            } else {
                try {
                    WifiStatus status = this.mIWifiRttController.rangeRequest(cmdId, rttConfig);
                    if (status.code == 0) {
                        return true;
                    }
                    Log.e(TAG, "rangeRequest: cannot issue range request -- code=" + status.code);
                    return false;
                } catch (RemoteException e) {
                    Log.e(TAG, "rangeRequest: exception issuing range request: " + e);
                    return false;
                }
            }
        }
    }

    public boolean rangeCancel(int cmdId, ArrayList<byte[]> macAddresses) {
        if (this.mDbg) {
            Log.v(TAG, "rangeCancel: cmdId=" + cmdId);
        }
        synchronized (this.mLock) {
            if (!isReady()) {
                Log.e(TAG, "rangeCancel: RttController is null");
                return false;
            }
            try {
                WifiStatus status = this.mIWifiRttController.rangeCancel(cmdId, macAddresses);
                if (status.code == 0) {
                    return true;
                }
                Log.e(TAG, "rangeCancel: cannot issue range cancel -- code=" + status.code);
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "rangeCancel: exception issuing range cancel: " + e);
                return false;
            }
        }
    }

    @Override // android.hardware.wifi.V1_0.IWifiRttControllerEventCallback
    public void onResults(int cmdId, ArrayList<RttResult> halResults) {
        if (this.mDbg) {
            Log.v(TAG, "onResults: cmdId=" + cmdId + ", # of results=" + halResults.size());
        }
        if (halResults == null) {
            halResults = new ArrayList<>();
        }
        ListIterator<RttResult> lit = halResults.listIterator();
        while (lit.hasNext()) {
            if (lit.next() == null) {
                lit.remove();
            }
        }
        this.mRttService.onRangingResults(cmdId, halResults);
    }

    private static ArrayList<RttConfig> convertRangingRequestToRttConfigs(RangingRequest request, boolean isCalledFromPrivilegedContext, RttCapabilities cap) {
        ArrayList<RttConfig> rttConfigs = new ArrayList<>(request.mRttPeers.size());
        for (ResponderConfig responder : request.mRttPeers) {
            if (isCalledFromPrivilegedContext || responder.supports80211mc) {
                RttConfig config = new RttConfig();
                System.arraycopy(responder.macAddress.toByteArray(), 0, config.addr, 0, config.addr.length);
                try {
                    boolean z = true;
                    config.type = responder.supports80211mc ? 2 : 1;
                    if (config.type != 1 || cap == null || cap.rttOneSidedSupported) {
                        config.peer = halRttPeerTypeFromResponderType(responder.responderType);
                        config.channel.width = halChannelWidthFromResponderChannelWidth(responder.channelWidth);
                        config.channel.centerFreq = responder.frequency;
                        config.channel.centerFreq0 = responder.centerFreq0;
                        config.channel.centerFreq1 = responder.centerFreq1;
                        config.bw = halRttChannelBandwidthFromResponderChannelWidth(responder.channelWidth);
                        config.preamble = halRttPreambleFromResponderPreamble(responder.preamble);
                        if (config.peer == 5) {
                            config.mustRequestLci = false;
                            config.mustRequestLcr = false;
                            config.burstPeriod = 0;
                            config.numBurst = 0;
                            config.numFramesPerBurst = 5;
                            config.numRetriesPerRttFrame = 0;
                            config.numRetriesPerFtmr = 3;
                            config.burstDuration = 9;
                        } else {
                            config.mustRequestLci = true;
                            config.mustRequestLcr = true;
                            config.burstPeriod = 0;
                            config.numBurst = 0;
                            config.numFramesPerBurst = 8;
                            config.numRetriesPerRttFrame = config.type == 2 ? 0 : 3;
                            config.numRetriesPerFtmr = 3;
                            config.burstDuration = 9;
                            if (cap != null) {
                                config.mustRequestLci = config.mustRequestLci && cap.lciSupported;
                                if (!config.mustRequestLcr || !cap.lcrSupported) {
                                    z = false;
                                }
                                config.mustRequestLcr = z;
                                config.bw = halRttChannelBandwidthCapabilityLimiter(config.bw, cap);
                                config.preamble = halRttPreambleCapabilityLimiter(config.preamble, cap);
                            }
                        }
                        rttConfigs.add(config);
                    } else {
                        Log.w(TAG, "Device does not support one-sided RTT");
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid configuration: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Invalid responder: does not support 802.11mc");
            }
        }
        return rttConfigs;
    }

    private static int halRttPeerTypeFromResponderType(int responderType) {
        if (responderType == 0) {
            return 1;
        }
        if (responderType == 1) {
            return 2;
        }
        if (responderType == 2) {
            return 3;
        }
        if (responderType == 3) {
            return 4;
        }
        if (responderType == 4) {
            return 5;
        }
        throw new IllegalArgumentException("halRttPeerTypeFromResponderType: bad " + responderType);
    }

    private static int halChannelWidthFromResponderChannelWidth(int responderChannelWidth) {
        if (responderChannelWidth == 0) {
            return 0;
        }
        if (responderChannelWidth == 1) {
            return 1;
        }
        if (responderChannelWidth == 2) {
            return 2;
        }
        if (responderChannelWidth == 3) {
            return 3;
        }
        if (responderChannelWidth == 4) {
            return 4;
        }
        throw new IllegalArgumentException("halChannelWidthFromResponderChannelWidth: bad " + responderChannelWidth);
    }

    private static int halRttChannelBandwidthFromResponderChannelWidth(int responderChannelWidth) {
        if (responderChannelWidth == 0) {
            return 4;
        }
        if (responderChannelWidth == 1) {
            return 8;
        }
        if (responderChannelWidth == 2) {
            return 16;
        }
        if (responderChannelWidth == 3 || responderChannelWidth == 4) {
            return 32;
        }
        throw new IllegalArgumentException("halRttChannelBandwidthFromHalBandwidth: bad " + responderChannelWidth);
    }

    private static int halRttPreambleFromResponderPreamble(int responderPreamble) {
        if (responderPreamble == 0) {
            return 1;
        }
        if (responderPreamble == 1) {
            return 2;
        }
        if (responderPreamble == 2) {
            return 4;
        }
        throw new IllegalArgumentException("halRttPreambleFromResponderPreamble: bad " + responderPreamble);
    }

    private static int halRttChannelBandwidthCapabilityLimiter(int halRttChannelBandwidth, RttCapabilities cap) {
        while (halRttChannelBandwidth != 0 && (cap.bwSupport & halRttChannelBandwidth) == 0) {
            halRttChannelBandwidth >>= 1;
        }
        if (halRttChannelBandwidth != 0) {
            return halRttChannelBandwidth;
        }
        throw new IllegalArgumentException("RTT BW=" + halRttChannelBandwidth + ", not supported by device capabilities=" + cap + " - and no supported alternative");
    }

    private static int halRttPreambleCapabilityLimiter(int halRttPreamble, RttCapabilities cap) {
        while (halRttPreamble != 0 && (cap.preambleSupport & halRttPreamble) == 0) {
            halRttPreamble >>= 1;
        }
        if (halRttPreamble != 0) {
            return halRttPreamble;
        }
        throw new IllegalArgumentException("RTT Preamble=" + halRttPreamble + ", not supported by device capabilities=" + cap + " - and no supported alternative");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("RttNative:");
        pw.println("  mHalDeviceManager: " + this.mHalDeviceManager);
        pw.println("  mIWifiRttController: " + this.mIWifiRttController);
        pw.println("  mRttCapabilities: " + this.mRttCapabilities);
    }
}
