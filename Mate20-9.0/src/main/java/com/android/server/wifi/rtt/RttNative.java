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
    private Object mLock = new Object();
    private volatile RttCapabilities mRttCapabilities;
    private final RttServiceImpl mRttService;

    public RttNative(RttServiceImpl rttService, HalDeviceManager halDeviceManager) {
        this.mRttService = rttService;
        this.mHalDeviceManager = halDeviceManager;
    }

    public void start(Handler handler) {
        synchronized (this.mLock) {
            this.mHalDeviceManager.initialize();
            this.mHalDeviceManager.registerStatusListener(new HalDeviceManager.ManagerStatusListener() {
                public final void onStatusChanged() {
                    RttNative.this.updateController();
                }
            }, handler);
            updateController();
        }
    }

    public boolean isReady() {
        return this.mIWifiRttController != null;
    }

    /* access modifiers changed from: private */
    public void updateController() {
        if (this.mDbg) {
            Log.v(TAG, "updateController: mIWifiRttController=" + this.mIWifiRttController);
        }
        synchronized (this.mLock) {
            IWifiRttController localWifiRttController = this.mIWifiRttController;
            if (!this.mHalDeviceManager.isStarted()) {
                localWifiRttController = null;
            } else if (localWifiRttController == null) {
                localWifiRttController = this.mHalDeviceManager.createRttController();
                if (localWifiRttController == null) {
                    Log.e(TAG, "updateController: Failed creating RTT controller - but Wifi is started!");
                } else {
                    try {
                        localWifiRttController.registerEventCallback(this);
                    } catch (RemoteException e) {
                        Log.e(TAG, "updateController: exception registering callback: " + e);
                        localWifiRttController = null;
                    }
                }
            }
            this.mIWifiRttController = localWifiRttController;
            if (this.mIWifiRttController == null) {
                this.mRttService.disable();
            } else {
                this.mRttService.enableIfPossible();
                updateRttCapabilities();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateRttCapabilities() {
        if (this.mRttCapabilities == null) {
            synchronized (this.mLock) {
                try {
                    this.mIWifiRttController.getCapabilities(new IWifiRttController.getCapabilitiesCallback() {
                        public final void onValues(WifiStatus wifiStatus, RttCapabilities rttCapabilities) {
                            RttNative.lambda$updateRttCapabilities$1(RttNative.this, wifiStatus, rttCapabilities);
                        }
                    });
                } catch (RemoteException e) {
                    Log.e(TAG, "updateController: exception requesting capabilities: " + e);
                }
                if (this.mRttCapabilities != null && !this.mRttCapabilities.rttFtmSupported) {
                    Log.wtf(TAG, "Firmware indicates RTT is not supported - but device supports RTT - ignored!?");
                }
            }
        }
    }

    public static /* synthetic */ void lambda$updateRttCapabilities$1(RttNative rttNative, WifiStatus status, RttCapabilities capabilities) {
        if (status.code != 0) {
            Log.e(TAG, "updateController: error requesting capabilities -- code=" + status.code);
            return;
        }
        if (rttNative.mDbg) {
            Log.v(TAG, "updateController: RTT capabilities=" + capabilities);
        }
        rttNative.mRttCapabilities = capabilities;
    }

    public boolean rangeRequest(int cmdId, RangingRequest request, boolean isCalledFromPrivilegedContext) {
        if (this.mDbg) {
            Log.v(TAG, "rangeRequest: cmdId=" + cmdId + ", # of requests=" + request.mRttPeers.size());
        }
        updateRttCapabilities();
        synchronized (this.mLock) {
            if (!isReady()) {
                Log.e(TAG, "rangeRequest: RttController is null");
                return false;
            }
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
                            config.mustRequestLci = isCalledFromPrivilegedContext;
                            config.mustRequestLcr = isCalledFromPrivilegedContext;
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
        switch (responderType) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            default:
                throw new IllegalArgumentException("halRttPeerTypeFromResponderType: bad " + responderType);
        }
    }

    private static int halChannelWidthFromResponderChannelWidth(int responderChannelWidth) {
        switch (responderChannelWidth) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            default:
                throw new IllegalArgumentException("halChannelWidthFromResponderChannelWidth: bad " + responderChannelWidth);
        }
    }

    private static int halRttChannelBandwidthFromResponderChannelWidth(int responderChannelWidth) {
        switch (responderChannelWidth) {
            case 0:
                return 4;
            case 1:
                return 8;
            case 2:
                return 16;
            case 3:
                return 32;
            case 4:
                return 32;
            default:
                throw new IllegalArgumentException("halRttChannelBandwidthFromHalBandwidth: bad " + responderChannelWidth);
        }
    }

    private static int halRttPreambleFromResponderPreamble(int responderPreamble) {
        switch (responderPreamble) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 4;
            default:
                throw new IllegalArgumentException("halRttPreambleFromResponderPreamble: bad " + responderPreamble);
        }
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
