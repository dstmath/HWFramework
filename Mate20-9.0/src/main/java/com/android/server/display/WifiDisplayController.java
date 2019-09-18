package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplaySessionInfo;
import android.media.RemoteDisplay;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import android.view.Surface;
import com.android.internal.util.DumpUtils;
import com.android.server.HwServiceFactory;
import com.android.server.display.hisi_wifi.WifiDisplayControllerHisiExt;
import com.android.server.job.controllers.JobStatus;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;

final class WifiDisplayController implements IWifiDisplayControllerInner, DumpUtils.Dump {
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;
    private static final int CONNECT_MAX_RETRIES = 3;
    private static final int CONNECT_RETRY_DELAY_MILLIS = 500;
    private static final boolean DEBUG = true;
    private static final int DEFAULT_CONTROL_PORT = 7236;
    private static final int DISCOVER_PEERS_INTERVAL_MILLIS = 10000;
    private static final String HUAWEI_WIFI_1101_P2P = "huawei.android.permission.WIFI_1101_P2P";
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MAX_THROUGHPUT = 50;
    private static final int RTSP_TIMEOUT_SECONDS = 30;
    private static final int RTSP_TIMEOUT_SECONDS_CERT_MODE = 120;
    private static final String TAG = "WifiDisplayController";
    /* access modifiers changed from: private */
    public WifiDisplay mAdvertisedDisplay;
    private int mAdvertisedDisplayFlags;
    private int mAdvertisedDisplayHeight;
    private Surface mAdvertisedDisplaySurface;
    private int mAdvertisedDisplayWidth;
    /* access modifiers changed from: private */
    public final ArrayList<WifiP2pDevice> mAvailableWifiDisplayPeers = new ArrayList<>();
    /* access modifiers changed from: private */
    public WifiP2pDevice mCancelingDevice;
    /* access modifiers changed from: private */
    public WifiP2pDevice mConnectedDevice;
    /* access modifiers changed from: private */
    public WifiP2pGroup mConnectedDeviceGroupInfo;
    /* access modifiers changed from: private */
    public WifiP2pDevice mConnectingDevice;
    /* access modifiers changed from: private */
    public int mConnectionRetriesLeft;
    /* access modifiers changed from: private */
    public final Runnable mConnectionTimeout = new Runnable() {
        public void run() {
            if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display connection after 30 seconds: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                WifiDisplayController.this.updateConnectionErrorCode(6);
                WifiDisplayController.this.handleConnectionFailure(true);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public WifiP2pDevice mDesiredDevice;
    /* access modifiers changed from: private */
    public WifiP2pDevice mDisconnectingDevice;
    private final Runnable mDiscoverPeers = new Runnable() {
        public void run() {
            WifiDisplayController.this.tryDiscoverPeers();
        }
    };
    /* access modifiers changed from: private */
    public boolean mDiscoverPeersInProgress;
    IHwWifiDisplayControllerEx mHWdcEx = null;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public final Listener mListener;
    private NetworkInfo mNetworkInfo;
    /* access modifiers changed from: private */
    public RemoteDisplay mRemoteDisplay;
    /* access modifiers changed from: private */
    public boolean mRemoteDisplayConnected;
    private String mRemoteDisplayInterface;
    /* access modifiers changed from: private */
    public final Runnable mRtspTimeout = new Runnable() {
        public void run() {
            if (WifiDisplayController.this.mConnectedDevice != null && WifiDisplayController.this.mRemoteDisplay != null && !WifiDisplayController.this.mRemoteDisplayConnected) {
                Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display RTSP connection after 30 seconds: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                WifiDisplayController.this.updateConnectionErrorCode(7);
                WifiDisplayController.this.handleConnectionFailure(true);
            }
        }
    };
    private boolean mScanRequested;
    /* access modifiers changed from: private */
    public WifiP2pDevice mThisDevice;
    /* access modifiers changed from: private */
    public int mUIBCCap = 0;
    IHwUibcReceiver mUibcInterface = null;
    /* access modifiers changed from: private */
    public boolean mUsingUIBC = false;
    /* access modifiers changed from: private */
    public boolean mWfdEnabled;
    /* access modifiers changed from: private */
    public boolean mWfdEnabling;
    /* access modifiers changed from: private */
    public boolean mWifiDisplayCertMode;
    private boolean mWifiDisplayOnSetting;
    private int mWifiDisplayWpsConfig = 4;
    private final WifiP2pManager.Channel mWifiP2pChannel;
    private boolean mWifiP2pEnabled;
    private final BroadcastReceiver mWifiP2pExReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.p2p.WIFI_P2P_FLAG_CHANGED_ACTION".equals(intent.getAction()) && WifiDisplayControllerHisiExt.hisiWifiEnabled()) {
                boolean wifiP2pFlag = false;
                if (intent.getIntExtra("extra_p2p_flag", 0) == 1) {
                    wifiP2pFlag = true;
                }
                Slog.d(WifiDisplayController.TAG, "WIFI_P2P_FLAG_CHANGED_ACTION,wifiP2pFlag:" + wifiP2pFlag);
                WifiDisplayController.this.handleStateChanged(wifiP2pFlag);
            }
        }
    };
    private final WifiP2pManager mWifiP2pManager;
    private final BroadcastReceiver mWifiP2pReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.wifi.p2p.STATE_CHANGED")) {
                boolean z = true;
                if (intent.getIntExtra("wifi_p2p_state", 1) != 2) {
                    z = false;
                }
                boolean enabled = z;
                Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_STATE_CHANGED_ACTION: enabled=" + enabled);
                WifiDisplayController.this.handleStateChanged(enabled);
            } else if (action.equals("android.net.wifi.p2p.PEERS_CHANGED")) {
                Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_PEERS_CHANGED_ACTION.");
                WifiDisplayController.this.handlePeersChanged();
            } else if (action.equals("android.net.wifi.p2p.CONNECTION_STATE_CHANGE")) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_CONNECTION_CHANGED_ACTION: networkInfo=" + networkInfo);
                if (networkInfo == null) {
                    Slog.e(WifiDisplayController.TAG, "networkInfo is null, return!");
                    return;
                }
                WifiDisplayController.this.handleConnectionChanged(networkInfo);
            } else if (action.equals("android.net.wifi.p2p.THIS_DEVICE_CHANGED")) {
                WifiP2pDevice unused = WifiDisplayController.this.mThisDevice = (WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice");
                Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: mThisDevice= " + WifiDisplayController.this.mThisDevice);
            }
        }
    };

    public interface Listener {
        void onDisplayCasting(WifiDisplay wifiDisplay);

        void onDisplayChanged(WifiDisplay wifiDisplay);

        void onDisplayConnected(WifiDisplay wifiDisplay, Surface surface, int i, int i2, int i3);

        void onDisplayConnecting(WifiDisplay wifiDisplay);

        void onDisplayConnectionFailed();

        void onDisplayDisconnected();

        void onDisplaySessionInfo(WifiDisplaySessionInfo wifiDisplaySessionInfo);

        void onFeatureStateChanged(int i);

        void onScanFinished();

        void onScanResults(WifiDisplay[] wifiDisplayArr);

        void onScanStarted();

        void onSetConnectionFailedReason(int i);

        void onSetUibcInfo(int i);
    }

    static /* synthetic */ int access$3120(WifiDisplayController x0, int x1) {
        int i = x0.mConnectionRetriesLeft - x1;
        x0.mConnectionRetriesLeft = i;
        return i;
    }

    public WifiDisplayController(Context context, Handler handler, Listener listener) {
        this.mContext = context;
        this.mHandler = handler;
        this.mListener = listener;
        this.mWifiP2pManager = (WifiP2pManager) context.getSystemService("wifip2p");
        this.mWifiP2pChannel = this.mWifiP2pManager.initialize(context, handler.getLooper(), null);
        this.mHWdcEx = HwServiceFactory.getHwWifiDisplayControllerEx(this, context, handler);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.p2p.STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
        context.registerReceiver(this.mWifiP2pReceiver, intentFilter, null, this.mHandler);
        context.registerReceiver(this.mWifiP2pExReceiver, new IntentFilter("android.net.wifi.p2p.WIFI_P2P_FLAG_CHANGED_ACTION"), HUAWEI_WIFI_1101_P2P, null);
        ContentObserver settingsObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange, Uri uri) {
                WifiDisplayController.this.updateSettings();
            }
        };
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Global.getUriFor("wifi_display_on"), false, settingsObserver);
        resolver.registerContentObserver(Settings.Global.getUriFor("wifi_display_certification_on"), false, settingsObserver);
        resolver.registerContentObserver(Settings.Global.getUriFor("wifi_display_wps_config"), false, settingsObserver);
        updateSettings();
    }

    /* access modifiers changed from: private */
    public void updateSettings() {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean z = false;
        this.mWifiDisplayOnSetting = Settings.Global.getInt(resolver, "wifi_display_on", 0) != 0;
        if (Settings.Global.getInt(resolver, "wifi_display_certification_on", 0) != 0) {
            z = true;
        }
        this.mWifiDisplayCertMode = z;
        this.mWifiDisplayWpsConfig = 4;
        if (this.mWifiDisplayCertMode) {
            this.mWifiDisplayWpsConfig = Settings.Global.getInt(resolver, "wifi_display_wps_config", 4);
        }
        updateWfdEnableState();
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println("mWifiDisplayOnSetting=" + this.mWifiDisplayOnSetting);
        pw.println("mWifiP2pEnabled=" + this.mWifiP2pEnabled);
        pw.println("mWfdEnabled=" + this.mWfdEnabled);
        pw.println("mWfdEnabling=" + this.mWfdEnabling);
        pw.println("mNetworkInfo=" + this.mNetworkInfo);
        pw.println("mScanRequested=" + this.mScanRequested);
        pw.println("mDiscoverPeersInProgress=" + this.mDiscoverPeersInProgress);
        pw.println("mDesiredDevice=" + describeWifiP2pDevice(this.mDesiredDevice));
        pw.println("mConnectingDisplay=" + describeWifiP2pDevice(this.mConnectingDevice));
        pw.println("mDisconnectingDisplay=" + describeWifiP2pDevice(this.mDisconnectingDevice));
        pw.println("mCancelingDisplay=" + describeWifiP2pDevice(this.mCancelingDevice));
        pw.println("mConnectedDevice=" + describeWifiP2pDevice(this.mConnectedDevice));
        pw.println("mConnectionRetriesLeft=" + this.mConnectionRetriesLeft);
        pw.println("mRemoteDisplay=" + this.mRemoteDisplay);
        pw.println("mRemoteDisplayInterface=" + this.mRemoteDisplayInterface);
        pw.println("mRemoteDisplayConnected=" + this.mRemoteDisplayConnected);
        pw.println("mAdvertisedDisplay=" + this.mAdvertisedDisplay);
        pw.println("mAdvertisedDisplaySurface=" + this.mAdvertisedDisplaySurface);
        pw.println("mAdvertisedDisplayWidth=" + this.mAdvertisedDisplayWidth);
        pw.println("mAdvertisedDisplayHeight=" + this.mAdvertisedDisplayHeight);
        pw.println("mAdvertisedDisplayFlags=" + this.mAdvertisedDisplayFlags);
        pw.println("mAvailableWifiDisplayPeers: size=" + this.mAvailableWifiDisplayPeers.size());
        Iterator<WifiP2pDevice> it = this.mAvailableWifiDisplayPeers.iterator();
        while (it.hasNext()) {
            pw.println("  " + describeWifiP2pDevice(it.next()));
        }
    }

    public void requestStartScan() {
        if (!this.mScanRequested) {
            this.mScanRequested = true;
            updateScanState();
        }
    }

    public void requestStopScan() {
        if (this.mScanRequested) {
            this.mScanRequested = false;
            updateScanState();
        }
    }

    public void requestConnect(String address) {
        Iterator<WifiP2pDevice> it = this.mAvailableWifiDisplayPeers.iterator();
        while (it.hasNext()) {
            WifiP2pDevice device = it.next();
            if (device.deviceAddress.equals(address)) {
                connect(device);
            }
        }
    }

    public void requestPause() {
        if (this.mRemoteDisplay != null) {
            this.mRemoteDisplay.pause();
        }
    }

    public void requestResume() {
        if (this.mRemoteDisplay != null) {
            this.mRemoteDisplay.resume();
        }
    }

    public void requestDisconnect() {
        disconnect();
    }

    private void updateWfdEnableState() {
        if (!this.mWifiDisplayOnSetting || !this.mWifiP2pEnabled) {
            if (this.mWfdEnabled || this.mWfdEnabling) {
                WifiP2pWfdInfo wfdInfo = new WifiP2pWfdInfo();
                wfdInfo.setWfdEnabled(false);
                this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, wfdInfo, new WifiP2pManager.ActionListener() {
                    public void onSuccess() {
                        Slog.d(WifiDisplayController.TAG, "Successfully set WFD info.");
                    }

                    public void onFailure(int reason) {
                        Slog.d(WifiDisplayController.TAG, "Failed to set WFD info with reason " + reason + ".");
                    }
                });
            }
            this.mWfdEnabling = false;
            this.mWfdEnabled = false;
            reportFeatureState();
            updateScanState();
            disconnect();
        } else if (!this.mWfdEnabled && !this.mWfdEnabling) {
            this.mWfdEnabling = true;
            WifiP2pWfdInfo wfdInfo2 = new WifiP2pWfdInfo();
            wfdInfo2.setWfdEnabled(true);
            wfdInfo2.setDeviceType(0);
            wfdInfo2.setSessionAvailable(true);
            wfdInfo2.setControlPort(DEFAULT_CONTROL_PORT);
            wfdInfo2.setMaxThroughput(50);
            this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, wfdInfo2, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    Slog.d(WifiDisplayController.TAG, "Successfully set WFD info.");
                    if (WifiDisplayController.this.mWfdEnabling) {
                        boolean unused = WifiDisplayController.this.mWfdEnabling = false;
                        boolean unused2 = WifiDisplayController.this.mWfdEnabled = true;
                        WifiDisplayController.this.reportFeatureState();
                        WifiDisplayController.this.updateScanState();
                    }
                }

                public void onFailure(int reason) {
                    Slog.d(WifiDisplayController.TAG, "Failed to set WFD info with reason " + reason + ".");
                    boolean unused = WifiDisplayController.this.mWfdEnabling = false;
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void reportFeatureState() {
        final int featureState = computeFeatureState();
        this.mHandler.post(new Runnable() {
            public void run() {
                WifiDisplayController.this.mListener.onFeatureStateChanged(featureState);
            }
        });
    }

    private int computeFeatureState() {
        int i;
        if (!this.mWifiP2pEnabled) {
            return 1;
        }
        if (this.mWifiDisplayOnSetting) {
            i = 3;
        } else {
            i = 2;
        }
        return i;
    }

    /* access modifiers changed from: private */
    public void updateScanState() {
        if (HWFLOW) {
            Slog.i(TAG, "updateScanState mScanRequested=" + this.mScanRequested + " updateScanState mWfdEnabled=" + this.mWfdEnabled);
        }
        if (HWFLOW) {
            Slog.i(TAG, "mDiscoverPeersInProgress=" + this.mDiscoverPeersInProgress + " mDesiredDevice=" + this.mDesiredDevice);
        }
        if (!this.mScanRequested || !this.mWfdEnabled || (this.mDesiredDevice != null && !IHwWifiDisplayControllerEx.ENABLED_PC)) {
            if (this.mDiscoverPeersInProgress) {
                this.mHandler.removeCallbacks(this.mDiscoverPeers);
                if (this.mDesiredDevice == null || this.mDesiredDevice == this.mConnectedDevice) {
                    Slog.i(TAG, "Stopping Wifi display scan.");
                    this.mDiscoverPeersInProgress = false;
                    stopPeerDiscovery();
                    handleScanFinished();
                }
            }
        } else if (!this.mDiscoverPeersInProgress) {
            Slog.i(TAG, "Starting Wifi display scan.");
            this.mDiscoverPeersInProgress = true;
            handleScanStarted();
            tryDiscoverPeers();
        }
    }

    /* access modifiers changed from: private */
    public void tryDiscoverPeers() {
        if (this.mHWdcEx == null || !this.mHWdcEx.tryDiscoverPeersEx()) {
            this.mWifiP2pManager.discoverPeers(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    Slog.d(WifiDisplayController.TAG, "Discover peers succeeded.  Requesting peers now.");
                    if (WifiDisplayController.this.mDiscoverPeersInProgress) {
                        WifiDisplayController.this.requestPeers();
                    }
                }

                public void onFailure(int reason) {
                    Slog.d(WifiDisplayController.TAG, "Discover peers failed with reason " + reason + ".");
                }
            });
            this.mHandler.postDelayed(this.mDiscoverPeers, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            return;
        }
        Slog.d(TAG, "used tryDiscoverPeersEx discover peers.");
    }

    private void stopPeerDiscovery() {
        this.mWifiP2pManager.stopPeerDiscovery(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Slog.d(WifiDisplayController.TAG, "Stop peer discovery succeeded.");
            }

            public void onFailure(int reason) {
                Slog.d(WifiDisplayController.TAG, "Stop peer discovery failed with reason " + reason + ".");
            }
        });
    }

    /* access modifiers changed from: private */
    public void requestPeers() {
        this.mWifiP2pManager.requestPeers(this.mWifiP2pChannel, new WifiP2pManager.PeerListListener() {
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                Slog.d(WifiDisplayController.TAG, "Received list of peers.");
                WifiDisplayController.this.mAvailableWifiDisplayPeers.clear();
                for (WifiP2pDevice device : peers.getDeviceList()) {
                    Slog.d(WifiDisplayController.TAG, "  " + WifiDisplayController.describeWifiP2pDevice(device));
                    if (WifiDisplayController.isWifiDisplay(device)) {
                        WifiDisplayController.this.mAvailableWifiDisplayPeers.add(device);
                    }
                }
                if (WifiDisplayController.this.mDiscoverPeersInProgress) {
                    WifiDisplayController.this.handleScanResults();
                }
            }
        });
    }

    private void handleScanStarted() {
        this.mHandler.post(new Runnable() {
            public void run() {
                WifiDisplayController.this.mListener.onScanStarted();
            }
        });
    }

    /* access modifiers changed from: private */
    public void handleScanResults() {
        int count = this.mAvailableWifiDisplayPeers.size();
        final WifiDisplay[] displays = (WifiDisplay[]) WifiDisplay.CREATOR.newArray(count);
        for (int i = 0; i < count; i++) {
            WifiP2pDevice device = this.mAvailableWifiDisplayPeers.get(i);
            displays[i] = createWifiDisplay(device);
            updateDesiredDevice(device);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                WifiDisplayController.this.mListener.onScanResults(displays);
            }
        });
    }

    private void handleScanFinished() {
        this.mHandler.post(new Runnable() {
            public void run() {
                WifiDisplayController.this.mListener.onScanFinished();
            }
        });
    }

    private void updateDesiredDevice(WifiP2pDevice device) {
        String address = device.deviceAddress;
        if (this.mDesiredDevice != null && this.mDesiredDevice.deviceAddress.equals(address)) {
            Slog.d(TAG, "updateDesiredDevice: new information " + describeWifiP2pDevice(device));
            this.mDesiredDevice.update(device);
            if (this.mAdvertisedDisplay != null && this.mAdvertisedDisplay.getDeviceAddress().equals(address)) {
                readvertiseDisplay(createWifiDisplay(this.mDesiredDevice));
            }
        }
    }

    private void connect(WifiP2pDevice device) {
        if (this.mDesiredDevice != null && !this.mDesiredDevice.deviceAddress.equals(device.deviceAddress)) {
            Slog.d(TAG, "connect: nothing to do, already connecting to " + describeWifiP2pDevice(device));
        } else if (this.mConnectedDevice != null && !this.mConnectedDevice.deviceAddress.equals(device.deviceAddress) && this.mDesiredDevice == null) {
            Slog.d(TAG, "connect: nothing to do, already connected to " + describeWifiP2pDevice(device) + " and not part way through connecting to a different device.");
        } else if (!this.mWfdEnabled) {
            Slog.i(TAG, "Ignoring request to connect to Wifi display because the  feature is currently disabled: " + device.deviceName);
        } else {
            this.mDesiredDevice = device;
            this.mConnectionRetriesLeft = 3;
            if (this.mHWdcEx != null) {
                this.mHWdcEx.resetDisplayParameters();
            }
            updateConnection();
        }
    }

    /* access modifiers changed from: private */
    public void disconnect() {
        this.mDesiredDevice = null;
        if (this.mUsingUIBC && this.mUibcInterface != null) {
            this.mUibcInterface.destroyReceiver();
            HwServiceFactory.clearHwUibcReceiver();
            this.mUibcInterface = null;
            this.mUIBCCap = 0;
        }
        updateConnection();
    }

    /* access modifiers changed from: private */
    public void retryConnection() {
        this.mDesiredDevice = new WifiP2pDevice(this.mDesiredDevice);
        updateConnection();
    }

    /* access modifiers changed from: private */
    public void updateConnection() {
        updateScanState();
        if (!(this.mRemoteDisplay == null || this.mConnectedDevice == this.mDesiredDevice)) {
            Slog.i(TAG, "Stopped listening for RTSP connection on " + this.mRemoteDisplayInterface + " from Wifi display: " + this.mConnectedDevice.deviceName);
            this.mRemoteDisplay.dispose();
            this.mRemoteDisplay = null;
            this.mRemoteDisplayInterface = null;
            this.mRemoteDisplayConnected = false;
            this.mHandler.removeCallbacks(this.mRtspTimeout);
            this.mWifiP2pManager.setMiracastMode(0);
            unadvertiseDisplay();
            unregisterPGStateEvent();
        }
        if (this.mDisconnectingDevice == null) {
            if (this.mConnectedDevice != null && this.mConnectedDevice != this.mDesiredDevice) {
                Slog.i(TAG, "Disconnecting from Wifi display: " + this.mConnectedDevice.deviceName);
                this.mDisconnectingDevice = this.mConnectedDevice;
                this.mConnectedDevice = null;
                this.mConnectedDeviceGroupInfo = null;
                unadvertiseDisplay();
                final WifiP2pDevice oldDevice = this.mDisconnectingDevice;
                this.mWifiP2pManager.removeGroup(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
                    public void onSuccess() {
                        Slog.i(WifiDisplayController.TAG, "Disconnected from Wifi display: " + oldDevice.deviceName);
                        next();
                    }

                    public void onFailure(int reason) {
                        Slog.i(WifiDisplayController.TAG, "Failed to disconnect from Wifi display: " + oldDevice.deviceName + ", reason=" + reason);
                        next();
                    }

                    private void next() {
                        if (WifiDisplayController.this.mDisconnectingDevice == oldDevice) {
                            WifiP2pDevice unused = WifiDisplayController.this.mDisconnectingDevice = null;
                            WifiDisplayController.this.updateConnection();
                        }
                    }
                });
            } else if (this.mCancelingDevice == null) {
                if (this.mConnectingDevice != null && this.mConnectingDevice != this.mDesiredDevice) {
                    Slog.i(TAG, "Canceling connection to Wifi display: " + this.mConnectingDevice.deviceName);
                    this.mCancelingDevice = this.mConnectingDevice;
                    this.mConnectingDevice = null;
                    unadvertiseDisplay();
                    this.mHandler.removeCallbacks(this.mConnectionTimeout);
                    final WifiP2pDevice oldDevice2 = this.mCancelingDevice;
                    this.mWifiP2pManager.cancelConnect(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Canceled connection to Wifi display: " + oldDevice2.deviceName);
                            next();
                            WifiDisplayController.this.unregisterPGStateEvent();
                        }

                        public void onFailure(int reason) {
                            Slog.i(WifiDisplayController.TAG, "Failed to cancel connection to Wifi display: " + oldDevice2.deviceName + ", reason=" + reason);
                            next();
                        }

                        private void next() {
                            if (WifiDisplayController.this.mCancelingDevice == oldDevice2) {
                                WifiP2pDevice unused = WifiDisplayController.this.mCancelingDevice = null;
                                WifiDisplayController.this.updateConnection();
                            }
                        }
                    });
                } else if (this.mDesiredDevice == null) {
                    if (this.mWifiDisplayCertMode) {
                        this.mListener.onDisplaySessionInfo(getSessionInfo(this.mConnectedDeviceGroupInfo, 0));
                    }
                    unadvertiseDisplay();
                } else if (this.mConnectedDevice == null && this.mConnectingDevice == null) {
                    Slog.i(TAG, "Connecting to Wifi display: " + this.mDesiredDevice.deviceName);
                    this.mConnectingDevice = this.mDesiredDevice;
                    WifiP2pConfig config = new WifiP2pConfig();
                    WpsInfo wps = new WpsInfo();
                    if (this.mWifiDisplayWpsConfig != 4) {
                        wps.setup = this.mWifiDisplayWpsConfig;
                    } else if (this.mConnectingDevice.wpsPbcSupported()) {
                        wps.setup = 0;
                    } else if (this.mConnectingDevice.wpsDisplaySupported()) {
                        wps.setup = 2;
                    } else {
                        wps.setup = 1;
                    }
                    config.wps = wps;
                    config.deviceAddress = this.mConnectingDevice.deviceAddress;
                    config.groupOwnerIntent = 14;
                    advertiseDisplay(createWifiDisplay(this.mConnectingDevice), null, 0, 0, 0);
                    final WifiP2pDevice newDevice = this.mDesiredDevice;
                    this.mWifiP2pManager.connect(this.mWifiP2pChannel, config, new WifiP2pManager.ActionListener() {
                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Initiated connection to Wifi display: " + newDevice.deviceName);
                            WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mConnectionTimeout, 30000);
                        }

                        public void onFailure(int reason) {
                            if (WifiDisplayController.this.mConnectingDevice == newDevice) {
                                Slog.i(WifiDisplayController.TAG, "Failed to initiate connection to Wifi display: " + newDevice.deviceName + ", reason=" + reason);
                                WifiP2pDevice unused = WifiDisplayController.this.mConnectingDevice = null;
                                WifiDisplayController.this.updateConnectionErrorCode(reason);
                                WifiDisplayController.this.handleConnectionFailure(false);
                            }
                        }
                    });
                } else {
                    if (this.mConnectedDevice != null && this.mRemoteDisplay == null) {
                        if (getInterfaceAddress(this.mConnectedDeviceGroupInfo) == null) {
                            Slog.i(TAG, "Failed to get local interface address for communicating with Wifi display: " + this.mConnectedDevice.deviceName);
                            updateConnectionErrorCode(3);
                            handleConnectionFailure(false);
                            return;
                        }
                        this.mWifiP2pManager.setMiracastMode(1);
                        final WifiP2pDevice oldDevice3 = this.mConnectedDevice;
                        int port = getPortNumber(this.mConnectedDevice);
                        String iface = addr.getHostAddress() + ":" + port;
                        this.mRemoteDisplayInterface = iface;
                        Slog.i(TAG, "Listening for RTSP connection on " + iface + " from Wifi display: " + this.mConnectedDevice.deviceName);
                        this.mRemoteDisplay = RemoteDisplay.listen(iface, new RemoteDisplay.Listener() {
                            public void onDisplayConnected(Surface surface, int width, int height, int flags, int session) {
                                if (WifiDisplayController.this.mConnectedDevice == oldDevice3 && !WifiDisplayController.this.mRemoteDisplayConnected) {
                                    Slog.i(WifiDisplayController.TAG, "Opened RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                    boolean unused = WifiDisplayController.this.mRemoteDisplayConnected = true;
                                    WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                    if (WifiDisplayController.this.mWifiDisplayCertMode) {
                                        WifiDisplayController.this.mListener.onDisplaySessionInfo(WifiDisplayController.this.getSessionInfo(WifiDisplayController.this.mConnectedDeviceGroupInfo, session));
                                    }
                                    WifiDisplayController.this.advertiseDisplay(WifiDisplayController.createWifiDisplay(WifiDisplayController.this.mConnectedDevice), surface, width, height, flags);
                                    if (WifiDisplayController.this.mHWdcEx != null) {
                                        WifiDisplayController.this.mHWdcEx.setVideoBitrate();
                                        WifiDisplayController.this.mHWdcEx.registerPGStateEvent();
                                    }
                                    if (WifiDisplayController.this.mUibcInterface != null) {
                                        WifiDisplayController.this.mUibcInterface.setRemoteScreenSize(width, height);
                                    }
                                }
                            }

                            public void onDisplayDisconnected() {
                                if (WifiDisplayController.this.mConnectedDevice == oldDevice3) {
                                    Slog.i(WifiDisplayController.TAG, "Closed RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                    WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                    WifiDisplayController.this.disconnect();
                                    WifiDisplayController.this.unregisterPGStateEvent();
                                }
                            }

                            public void onDisplayError(int error) {
                                if (WifiDisplayController.this.mConnectedDevice == oldDevice3) {
                                    if (50 == error) {
                                        if (WifiDisplayController.this.mHWdcEx != null) {
                                            WifiDisplayController.this.mHWdcEx.advertisDisplayCasting(WifiDisplayController.this.mAdvertisedDisplay);
                                        }
                                        return;
                                    }
                                    Slog.i(WifiDisplayController.TAG, "Lost RTSP connection with Wifi display due to error " + error + ": " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                    WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                    if (30 == error) {
                                        WifiDisplayController.this.updateConnectionErrorCode(8);
                                        int unused = WifiDisplayController.this.mConnectionRetriesLeft = 0;
                                    } else {
                                        WifiDisplayController.this.updateConnectionErrorCode(4);
                                    }
                                    WifiDisplayController.this.handleConnectionFailure(false);
                                    WifiDisplayController.this.unregisterPGStateEvent();
                                }
                            }

                            public int notifyUibcCreate(int capSupport) {
                                Log.i("UIBC", "Create and Start UIBC Receiver : cap " + capSupport);
                                WifiDisplayController.this.mUibcInterface = HwServiceFactory.getHwUibcReceiver();
                                if (WifiDisplayController.this.mUibcInterface == null) {
                                    return -1;
                                }
                                int port = WifiDisplayController.this.mUibcInterface.createReceiver(WifiDisplayController.this.mContext, WifiDisplayController.this.mHandler);
                                if (!(port == -1 || WifiDisplayController.this.mUibcInterface.getAcceptCheck() == null)) {
                                    WifiDisplayController.this.mUibcInterface.setRemoteMacAddress(WifiDisplayController.this.mConnectedDevice.deviceAddress);
                                    WifiDisplayController.this.mUibcInterface.startReceiver();
                                    WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mUibcInterface.getAcceptCheck(), 16000);
                                    boolean unused = WifiDisplayController.this.mUsingUIBC = true;
                                    int unused2 = WifiDisplayController.this.mUIBCCap = capSupport;
                                }
                                return port;
                            }
                        }, this.mHandler, this.mContext.getOpPackageName());
                        if (this.mHWdcEx != null) {
                            this.mHWdcEx.setDisplayParameters();
                        }
                        this.mHandler.postDelayed(this.mRtspTimeout, (long) ((this.mWifiDisplayCertMode ? RTSP_TIMEOUT_SECONDS_CERT_MODE : 30) * 1000));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public WifiDisplaySessionInfo getSessionInfo(WifiP2pGroup info, int session) {
        if (info == null) {
            return null;
        }
        Inet4Address addr = getInterfaceAddress(info);
        WifiDisplaySessionInfo sessionInfo = new WifiDisplaySessionInfo(!info.getOwner().deviceAddress.equals(this.mThisDevice.deviceAddress), session, info.getOwner().deviceAddress + " " + info.getNetworkName(), info.getPassphrase(), addr != null ? addr.getHostAddress() : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        Slog.d(TAG, sessionInfo.toString());
        return sessionInfo;
    }

    /* access modifiers changed from: private */
    public void handleStateChanged(boolean enabled) {
        this.mWifiP2pEnabled = enabled;
        updateWfdEnableState();
    }

    /* access modifiers changed from: private */
    public void handlePeersChanged() {
        requestPeers();
    }

    /* access modifiers changed from: private */
    public void handleConnectionChanged(NetworkInfo networkInfo) {
        this.mNetworkInfo = networkInfo;
        if (!this.mWfdEnabled || !networkInfo.isConnected()) {
            this.mConnectedDeviceGroupInfo = null;
            if (!(this.mConnectingDevice == null && this.mConnectedDevice == null)) {
                disconnect();
            }
            if (this.mWfdEnabled) {
                requestPeers();
            }
        } else if (this.mDesiredDevice != null || this.mWifiDisplayCertMode) {
            this.mWifiP2pManager.requestGroupInfo(this.mWifiP2pChannel, new WifiP2pManager.GroupInfoListener() {
                public void onGroupInfoAvailable(WifiP2pGroup info) {
                    Slog.d(WifiDisplayController.TAG, "Received group info: " + WifiDisplayController.describeWifiP2pGroup(info));
                    if (info == null) {
                        Slog.e(WifiDisplayController.TAG, "onGroupInfoAvailable info is null");
                    } else if (WifiDisplayController.this.mConnectingDevice != null && !info.contains(WifiDisplayController.this.mConnectingDevice)) {
                        Slog.i(WifiDisplayController.TAG, "Aborting connection to Wifi display because the current P2P group does not contain the device we expected to find: " + WifiDisplayController.this.mConnectingDevice.deviceName + ", group info was: " + WifiDisplayController.describeWifiP2pGroup(info));
                        WifiDisplayController.this.updateConnectionErrorCode(5);
                        WifiDisplayController.this.handleConnectionFailure(false);
                    } else if (WifiDisplayController.this.mDesiredDevice == null || info.contains(WifiDisplayController.this.mDesiredDevice)) {
                        if (WifiDisplayController.this.mWifiDisplayCertMode) {
                            boolean owner = info.getOwner().deviceAddress.equals(WifiDisplayController.this.mThisDevice.deviceAddress);
                            if (owner && info.getClientList().isEmpty()) {
                                WifiP2pDevice unused = WifiDisplayController.this.mConnectingDevice = WifiDisplayController.this.mDesiredDevice = null;
                                WifiP2pGroup unused2 = WifiDisplayController.this.mConnectedDeviceGroupInfo = info;
                                WifiDisplayController.this.updateConnection();
                            } else if (WifiDisplayController.this.mConnectingDevice == null && WifiDisplayController.this.mDesiredDevice == null) {
                                WifiP2pDevice unused3 = WifiDisplayController.this.mConnectingDevice = WifiDisplayController.this.mDesiredDevice = owner ? info.getClientList().iterator().next() : info.getOwner();
                            }
                        }
                        if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                            Slog.i(WifiDisplayController.TAG, "Connected to Wifi display: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                            if (WifiDisplayController.this.mHWdcEx != null) {
                                WifiDisplayController.this.mHWdcEx.setWorkFrequence(info.getFrequence());
                            }
                            WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mConnectionTimeout);
                            WifiP2pGroup unused4 = WifiDisplayController.this.mConnectedDeviceGroupInfo = info;
                            WifiP2pDevice unused5 = WifiDisplayController.this.mConnectedDevice = WifiDisplayController.this.mConnectingDevice;
                            WifiP2pDevice unused6 = WifiDisplayController.this.mConnectingDevice = null;
                            WifiDisplayController.this.updateConnection();
                        }
                    } else {
                        WifiDisplayController.this.disconnect();
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void handleConnectionFailure(boolean timeoutOccurred) {
        Slog.i(TAG, "Wifi display connection failed!");
        if (this.mDesiredDevice == null) {
            return;
        }
        if (this.mConnectionRetriesLeft > 0) {
            final WifiP2pDevice oldDevice = this.mDesiredDevice;
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (WifiDisplayController.this.mDesiredDevice == oldDevice && WifiDisplayController.this.mConnectionRetriesLeft > 0) {
                        WifiDisplayController.access$3120(WifiDisplayController.this, 1);
                        Slog.i(WifiDisplayController.TAG, "Retrying Wifi display connection.  Retries left: " + WifiDisplayController.this.mConnectionRetriesLeft);
                        WifiDisplayController.this.retryConnection();
                    }
                }
            }, timeoutOccurred ? 0 : 500);
            return;
        }
        disconnect();
    }

    /* access modifiers changed from: private */
    public void advertiseDisplay(WifiDisplay display, Surface surface, int width, int height, int flags) {
        WifiDisplay wifiDisplay = display;
        Surface surface2 = surface;
        int i = width;
        int i2 = height;
        int i3 = flags;
        if (!Objects.equals(this.mAdvertisedDisplay, wifiDisplay) || this.mAdvertisedDisplaySurface != surface2 || this.mAdvertisedDisplayWidth != i || this.mAdvertisedDisplayHeight != i2 || this.mAdvertisedDisplayFlags != i3) {
            WifiDisplay oldDisplay = this.mAdvertisedDisplay;
            Surface oldSurface = this.mAdvertisedDisplaySurface;
            this.mAdvertisedDisplay = wifiDisplay;
            this.mAdvertisedDisplaySurface = surface2;
            this.mAdvertisedDisplayWidth = i;
            this.mAdvertisedDisplayHeight = i2;
            this.mAdvertisedDisplayFlags = i3;
            Handler handler = this.mHandler;
            final Surface surface3 = oldSurface;
            final Surface surface4 = surface2;
            final WifiDisplay wifiDisplay2 = oldDisplay;
            final WifiDisplay wifiDisplay3 = wifiDisplay;
            AnonymousClass20 r9 = r0;
            final int i4 = i;
            Handler handler2 = handler;
            final int i5 = i2;
            Surface surface5 = oldSurface;
            final int i6 = i3;
            AnonymousClass20 r0 = new Runnable() {
                public void run() {
                    if (surface3 != null && surface4 != surface3) {
                        WifiDisplayController.this.mListener.onDisplayDisconnected();
                    } else if (wifiDisplay2 != null && !wifiDisplay2.hasSameAddress(wifiDisplay3)) {
                        if (WifiDisplayController.this.mHWdcEx != null) {
                            WifiDisplayController.this.mListener.onSetConnectionFailedReason(WifiDisplayController.this.mHWdcEx.getConnectionErrorCode());
                        }
                        WifiDisplayController.this.updateConnectionErrorCode(-1);
                        WifiDisplayController.this.mListener.onDisplayConnectionFailed();
                    }
                    if (wifiDisplay3 != null) {
                        if (!wifiDisplay3.hasSameAddress(wifiDisplay2)) {
                            WifiDisplayController.this.mListener.onDisplayConnecting(wifiDisplay3);
                        } else if (!wifiDisplay3.equals(wifiDisplay2)) {
                            WifiDisplayController.this.mListener.onDisplayChanged(wifiDisplay3);
                        }
                        if (surface4 != null && surface4 != surface3) {
                            WifiDisplayController.this.mListener.onSetUibcInfo(WifiDisplayController.this.mUIBCCap);
                            WifiDisplayController.this.mListener.onDisplayConnected(wifiDisplay3, surface4, i4, i5, i6);
                        }
                    }
                }
            };
            handler2.post(r9);
        }
    }

    private void unadvertiseDisplay() {
        advertiseDisplay(null, null, 0, 0, 0);
    }

    private void readvertiseDisplay(WifiDisplay display) {
        advertiseDisplay(display, this.mAdvertisedDisplaySurface, this.mAdvertisedDisplayWidth, this.mAdvertisedDisplayHeight, this.mAdvertisedDisplayFlags);
    }

    private static Inet4Address getInterfaceAddress(WifiP2pGroup info) {
        NetworkInterface iface;
        if (WifiDisplayControllerHisiExt.hisiWifiEnabled()) {
            try {
                iface = NetworkInterface.getByName(WifiDisplayControllerHisiExt.getHisiWifiInface());
                Slog.w(TAG, "get InterfaceAddress from network interface " + WifiDisplayControllerHisiExt.getHisiWifiInface());
            } catch (SocketException ex) {
                Slog.w(TAG, "Could not obtain address of network interface " + WifiDisplayControllerHisiExt.getHisiWifiInface(), ex);
                return null;
            }
        } else {
            try {
                iface = NetworkInterface.getByName(info.getInterface());
                Slog.w(TAG, "get InterfaceAddress from network interface " + info.getInterface());
            } catch (SocketException ex2) {
                Slog.w(TAG, "Could not obtain address of network interface " + info.getInterface(), ex2);
                return null;
            }
        }
        Enumeration<InetAddress> addrs = iface.getInetAddresses();
        while (addrs.hasMoreElements()) {
            InetAddress addr = addrs.nextElement();
            if (addr instanceof Inet4Address) {
                return (Inet4Address) addr;
            }
        }
        Slog.w(TAG, "Could not obtain address of network interface " + info.getInterface() + " because it had no IPv4 addresses.");
        return null;
    }

    private static int getPortNumber(WifiP2pDevice device) {
        if (!device.deviceName.startsWith("DIRECT-") || !device.deviceName.endsWith("Broadcom")) {
            return DEFAULT_CONTROL_PORT;
        }
        return 8554;
    }

    /* access modifiers changed from: private */
    public static boolean isWifiDisplay(WifiP2pDevice device) {
        return device.wfdInfo != null && device.wfdInfo.isWfdEnabled() && isPrimarySinkDeviceType(device.wfdInfo.getDeviceType());
    }

    private static boolean isPrimarySinkDeviceType(int deviceType) {
        return deviceType == 1 || deviceType == 3;
    }

    /* access modifiers changed from: private */
    public static String describeWifiP2pDevice(WifiP2pDevice device) {
        return device != null ? device.toString().replace(10, ',') : "null";
    }

    /* access modifiers changed from: private */
    public static String describeWifiP2pGroup(WifiP2pGroup group) {
        return group != null ? group.toString().replace(10, ',') : "null";
    }

    /* access modifiers changed from: private */
    public static WifiDisplay createWifiDisplay(WifiP2pDevice device) {
        WifiDisplay wifiDisplay = new WifiDisplay(device.deviceAddress, device.deviceName, null, true, device.wfdInfo.isSessionAvailable(), false);
        return wifiDisplay;
    }

    public RemoteDisplay getmRemoteDisplay() {
        return this.mRemoteDisplay;
    }

    public Listener getmListener() {
        return this.mListener;
    }

    public void requestStartScanInner() {
        requestStartScan();
    }

    public WifiP2pManager getWifiP2pManagerInner() {
        return this.mWifiP2pManager;
    }

    public WifiP2pManager.Channel getWifiP2pChannelInner() {
        return this.mWifiP2pChannel;
    }

    public boolean getmDiscoverPeersInProgress() {
        return this.mDiscoverPeersInProgress;
    }

    public void requestPeersEx() {
        requestPeers();
    }

    public void disconnectInner() {
        disconnect();
    }

    public void postDelayedDiscover() {
        this.mHandler.postDelayed(this.mDiscoverPeers, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    /* access modifiers changed from: private */
    public void unregisterPGStateEvent() {
        if (this.mHWdcEx != null) {
            this.mHWdcEx.unregisterPGStateEvent();
        }
    }

    /* access modifiers changed from: private */
    public void updateConnectionErrorCode(int errorCode) {
        if (this.mHWdcEx != null) {
            this.mHWdcEx.updateConnectionErrorCode(errorCode);
        }
    }

    public void requestStartScan(int channelID) {
        if (this.mHWdcEx != null) {
            this.mHWdcEx.requestStartScan(channelID);
        } else {
            requestStartScan();
        }
    }

    public void setConnectParameters(boolean isSupportHdcp, boolean isUibcError, String verificaitonCode) {
        if (this.mHWdcEx != null) {
            this.mHWdcEx.setConnectParameters(isSupportHdcp, isUibcError, verificaitonCode);
        }
    }

    public void checkVerificationResult(boolean isRight) {
        if (this.mHWdcEx != null) {
            this.mHWdcEx.checkVerificationResult(isRight);
        }
    }

    public void sendWifiDisplayAction(String action) {
        if (this.mHWdcEx != null) {
            this.mHWdcEx.sendWifiDisplayAction(action);
        }
    }
}
