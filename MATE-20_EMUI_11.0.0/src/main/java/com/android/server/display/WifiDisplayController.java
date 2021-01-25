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
import com.android.server.job.controllers.JobStatus;
import com.huawei.android.hardware.display.HwWifiDisplayParameters;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;

/* access modifiers changed from: package-private */
public final class WifiDisplayController implements IWifiDisplayControllerInner, DumpUtils.Dump {
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;
    private static final int CONNECT_MAX_RETRIES = 3;
    private static final int CONNECT_RETRY_DELAY_MILLIS = 500;
    private static final boolean DEBUG = false;
    private static final int DEFAULT_CONTROL_PORT = 7236;
    private static final int DISCOVER_PEERS_INTERVAL_MILLIS = 10000;
    private static final int MAX_THROUGHPUT = 50;
    private static final int RTSP_TIMEOUT_SECONDS = 30;
    private static final int RTSP_TIMEOUT_SECONDS_CERT_MODE = 120;
    private static final String TAG = "WifiDisplayController";
    private WifiDisplay mAdvertisedDisplay;
    private int mAdvertisedDisplayFlags;
    private int mAdvertisedDisplayHeight;
    private Surface mAdvertisedDisplaySurface;
    private int mAdvertisedDisplayWidth;
    private final ArrayList<WifiP2pDevice> mAvailableWifiDisplayPeers = new ArrayList<>();
    private WifiP2pDevice mCancelingDevice;
    private WifiP2pDevice mConnectedDevice;
    private WifiP2pGroup mConnectedDeviceGroupInfo;
    private WifiP2pDevice mConnectingDevice;
    private int mConnectionRetriesLeft;
    private final Runnable mConnectionTimeout = new Runnable() {
        /* class com.android.server.display.WifiDisplayController.AnonymousClass17 */

        @Override // java.lang.Runnable
        public void run() {
            if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display connection after 30 seconds: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                WifiDisplayController.this.updateConnectionErrorCode(6);
                WifiDisplayController.this.handleConnectionFailure(true);
            }
        }
    };
    private final Context mContext;
    private WifiP2pDevice mDesiredDevice;
    private WifiP2pDevice mDisconnectingDevice;
    private final Runnable mDiscoverPeers = new Runnable() {
        /* class com.android.server.display.WifiDisplayController.AnonymousClass16 */

        @Override // java.lang.Runnable
        public void run() {
            WifiDisplayController.this.tryDiscoverPeers();
        }
    };
    private boolean mDiscoverPeersInProgress;
    IHwWifiDisplayControllerEx mHWdcEx = null;
    private final Handler mHandler;
    private final Listener mListener;
    private NetworkInfo mNetworkInfo;
    private RemoteDisplay mRemoteDisplay;
    private boolean mRemoteDisplayConnected;
    private String mRemoteDisplayInterface;
    private final Runnable mRtspTimeout = new Runnable() {
        /* class com.android.server.display.WifiDisplayController.AnonymousClass18 */

        @Override // java.lang.Runnable
        public void run() {
            if (WifiDisplayController.this.mConnectedDevice != null && WifiDisplayController.this.mRemoteDisplay != null && !WifiDisplayController.this.mRemoteDisplayConnected) {
                Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display RTSP connection after 30 seconds: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                WifiDisplayController.this.updateConnectionErrorCode(7);
                WifiDisplayController.this.handleConnectionFailure(true);
            }
        }
    };
    private boolean mScanRequested;
    private WifiP2pDevice mThisDevice;
    private int mUIBCCap = 0;
    IHwUibcReceiver mUibcInterface = null;
    private boolean mUsingUIBC = false;
    private boolean mWfdEnabled;
    private boolean mWfdEnabling;
    private boolean mWifiDisplayCertMode;
    private boolean mWifiDisplayOnSetting;
    private int mWifiDisplayWpsConfig = 4;
    private final WifiP2pManager.Channel mWifiP2pChannel;
    private boolean mWifiP2pEnabled;
    private final WifiP2pManager mWifiP2pManager;
    private final BroadcastReceiver mWifiP2pReceiver = new BroadcastReceiver() {
        /* class com.android.server.display.WifiDisplayController.AnonymousClass21 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.wifi.p2p.STATE_CHANGED")) {
                boolean enabled = true;
                if (intent.getIntExtra("wifi_p2p_state", 1) != 2) {
                    enabled = false;
                }
                WifiDisplayController.this.handleStateChanged(enabled);
            } else if (action.equals("android.net.wifi.p2p.PEERS_CHANGED")) {
                WifiDisplayController.this.handlePeersChanged();
            } else if (action.equals("android.net.wifi.p2p.CONNECTION_STATE_CHANGE")) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (networkInfo == null) {
                    Slog.e(WifiDisplayController.TAG, "networkInfo is null, return!");
                } else {
                    WifiDisplayController.this.handleConnectionChanged(networkInfo);
                }
            } else if (action.equals("android.net.wifi.p2p.THIS_DEVICE_CHANGED")) {
                WifiDisplayController.this.mThisDevice = (WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice");
            }
        }
    };

    public interface Listener {
        void onDisplayCasting(WifiDisplay wifiDisplay);

        void onDisplayChanged(WifiDisplay wifiDisplay);

        void onDisplayConnected(WifiDisplay wifiDisplay, Surface surface, int i, int i2, int i3);

        void onDisplayConnecting(WifiDisplay wifiDisplay);

        void onDisplayConnectionFailed();

        void onDisplayDataInfo(String str);

        void onDisplayDisconnected();

        void onDisplaySessionInfo(WifiDisplaySessionInfo wifiDisplaySessionInfo);

        void onFeatureStateChanged(int i);

        void onScanFinished();

        void onScanResults(WifiDisplay[] wifiDisplayArr);

        void onScanStarted();

        void onSetConnectionFailedReason(int i);

        void onSetUibcInfo(int i);
    }

    static /* synthetic */ int access$3020(WifiDisplayController x0, int x1) {
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
        ContentObserver settingsObserver = new ContentObserver(this.mHandler) {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass1 */

            @Override // android.database.ContentObserver
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
    /* access modifiers changed from: public */
    private void updateSettings() {
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
                Slog.i(TAG, "requestConnect target device matched");
                connect(device);
            }
        }
        if (this.mAvailableWifiDisplayPeers != null) {
            Slog.i(TAG, "mAvailableWifiDisplayPeers size is " + this.mAvailableWifiDisplayPeers.size());
        }
    }

    public void requestPause() {
        RemoteDisplay remoteDisplay = this.mRemoteDisplay;
        if (remoteDisplay != null) {
            remoteDisplay.pause();
        }
    }

    public void requestResume() {
        RemoteDisplay remoteDisplay = this.mRemoteDisplay;
        if (remoteDisplay != null) {
            remoteDisplay.resume();
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
                    /* class com.android.server.display.WifiDisplayController.AnonymousClass3 */

                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onSuccess() {
                    }

                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onFailure(int reason) {
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
                /* class com.android.server.display.WifiDisplayController.AnonymousClass2 */

                @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                public void onSuccess() {
                    if (WifiDisplayController.this.mWfdEnabling) {
                        WifiDisplayController.this.mWfdEnabling = false;
                        WifiDisplayController.this.mWfdEnabled = true;
                        WifiDisplayController.this.reportFeatureState();
                        WifiDisplayController.this.updateScanState();
                    }
                }

                @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                public void onFailure(int reason) {
                    WifiDisplayController.this.mWfdEnabling = false;
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportFeatureState() {
        final int featureState = computeFeatureState();
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                WifiDisplayController.this.mListener.onFeatureStateChanged(featureState);
            }
        });
    }

    private int computeFeatureState() {
        if (!this.mWifiP2pEnabled) {
            return 1;
        }
        if (this.mWifiDisplayOnSetting) {
            return 3;
        }
        return 2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScanState() {
        if (!this.mScanRequested || !this.mWfdEnabled || (this.mDesiredDevice != null && !IHwWifiDisplayControllerEx.ENABLED_PC)) {
            if (this.mDiscoverPeersInProgress) {
                this.mHandler.removeCallbacks(this.mDiscoverPeers);
                WifiP2pDevice wifiP2pDevice = this.mDesiredDevice;
                if (wifiP2pDevice == null || wifiP2pDevice == this.mConnectedDevice) {
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
    /* access modifiers changed from: public */
    private void tryDiscoverPeers() {
        IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx = this.mHWdcEx;
        if (iHwWifiDisplayControllerEx == null || !iHwWifiDisplayControllerEx.tryDiscoverPeersEx()) {
            this.mWifiP2pManager.discoverPeers(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
                /* class com.android.server.display.WifiDisplayController.AnonymousClass5 */

                @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                public void onSuccess() {
                    if (WifiDisplayController.this.mDiscoverPeersInProgress) {
                        WifiDisplayController.this.requestPeers();
                    }
                }

                @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                public void onFailure(int reason) {
                }
            });
            this.mHandler.postDelayed(this.mDiscoverPeers, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            return;
        }
        Slog.d(TAG, "used tryDiscoverPeersEx discover peers.");
    }

    private void stopPeerDiscovery() {
        this.mWifiP2pManager.stopPeerDiscovery(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass6 */

            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onSuccess() {
            }

            @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
            public void onFailure(int reason) {
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestPeers() {
        this.mWifiP2pManager.requestPeers(this.mWifiP2pChannel, new WifiP2pManager.PeerListListener() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass7 */

            @Override // android.net.wifi.p2p.WifiP2pManager.PeerListListener
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                WifiDisplayController.this.mAvailableWifiDisplayPeers.clear();
                Slog.i(WifiDisplayController.TAG, "Available wifiDisplay peers cleared.");
                for (WifiP2pDevice device : peers.getDeviceList()) {
                    if (WifiDisplayController.isWifiDisplay(device)) {
                        WifiDisplayController.this.mAvailableWifiDisplayPeers.add(device);
                    }
                }
                Slog.i(WifiDisplayController.TAG, "peers size is " + WifiDisplayController.this.mAvailableWifiDisplayPeers.size());
                if (WifiDisplayController.this.mDiscoverPeersInProgress) {
                    WifiDisplayController.this.handleScanResults();
                }
            }
        });
    }

    private void handleScanStarted() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                WifiDisplayController.this.mListener.onScanStarted();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanResults() {
        int count = this.mAvailableWifiDisplayPeers.size();
        final WifiDisplay[] displays = (WifiDisplay[]) WifiDisplay.CREATOR.newArray(count);
        for (int i = 0; i < count; i++) {
            WifiP2pDevice device = this.mAvailableWifiDisplayPeers.get(i);
            displays[i] = createWifiDisplay(device);
            updateDesiredDevice(device);
        }
        Slog.i(TAG, "post scan result");
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass9 */

            @Override // java.lang.Runnable
            public void run() {
                WifiDisplayController.this.mListener.onScanResults(displays);
            }
        });
    }

    private void handleScanFinished() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.display.WifiDisplayController.AnonymousClass10 */

            @Override // java.lang.Runnable
            public void run() {
                WifiDisplayController.this.mListener.onScanFinished();
            }
        });
    }

    private void updateDesiredDevice(WifiP2pDevice device) {
        String address = device.deviceAddress;
        WifiP2pDevice wifiP2pDevice = this.mDesiredDevice;
        if (wifiP2pDevice != null && wifiP2pDevice.deviceAddress.equals(address)) {
            this.mDesiredDevice.update(device);
            WifiDisplay wifiDisplay = this.mAdvertisedDisplay;
            if (wifiDisplay != null && wifiDisplay.getDeviceAddress().equals(address)) {
                readvertiseDisplay(createWifiDisplay(this.mDesiredDevice));
            }
        }
    }

    private void connect(WifiP2pDevice device) {
        WifiP2pDevice wifiP2pDevice = this.mDesiredDevice;
        if (wifiP2pDevice == null || wifiP2pDevice.deviceAddress.equals(device.deviceAddress)) {
            WifiP2pDevice wifiP2pDevice2 = this.mConnectedDevice;
            if (wifiP2pDevice2 != null && !wifiP2pDevice2.deviceAddress.equals(device.deviceAddress) && this.mDesiredDevice == null) {
                Slog.i(TAG, "connect: ConnectedDevice not null");
            } else if (!this.mWfdEnabled) {
                Slog.i(TAG, "Ignoring request to connect to Wifi display because the  feature is currently disabled: " + device.deviceName);
            } else {
                this.mDesiredDevice = device;
                this.mConnectionRetriesLeft = 3;
                IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx = this.mHWdcEx;
                if (iHwWifiDisplayControllerEx != null) {
                    iHwWifiDisplayControllerEx.resetDisplayParameters();
                }
                updateConnection();
            }
        } else {
            Slog.i(TAG, "connect: DesiredDevice not null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnect() {
        IHwUibcReceiver iHwUibcReceiver;
        this.mDesiredDevice = null;
        if (this.mUsingUIBC && (iHwUibcReceiver = this.mUibcInterface) != null) {
            iHwUibcReceiver.destroyReceiver();
            HwServiceFactory.clearHwUibcReceiver();
            this.mUibcInterface = null;
            this.mUIBCCap = 0;
        }
        updateConnection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void retryConnection() {
        this.mDesiredDevice = new WifiP2pDevice(this.mDesiredDevice);
        updateConnection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConnection() {
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
            WifiP2pDevice wifiP2pDevice = this.mConnectedDevice;
            if (wifiP2pDevice != null && wifiP2pDevice != this.mDesiredDevice) {
                Slog.i(TAG, "Disconnecting from Wifi display: " + this.mConnectedDevice.deviceName);
                this.mDisconnectingDevice = this.mConnectedDevice;
                this.mConnectedDevice = null;
                this.mConnectedDeviceGroupInfo = null;
                unadvertiseDisplay();
                final WifiP2pDevice oldDevice = this.mDisconnectingDevice;
                WifiP2pManager.ActionListener listener = new WifiP2pManager.ActionListener() {
                    /* class com.android.server.display.WifiDisplayController.AnonymousClass11 */

                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onSuccess() {
                        Slog.i(WifiDisplayController.TAG, "Disconnected from Wifi display: " + oldDevice.deviceName);
                        next();
                    }

                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onFailure(int reason) {
                        Slog.i(WifiDisplayController.TAG, "Failed to disconnect from Wifi display: " + oldDevice.deviceName + ", reason=" + reason);
                        next();
                    }

                    private void next() {
                        if (WifiDisplayController.this.mDisconnectingDevice == oldDevice) {
                            WifiDisplayController.this.mDisconnectingDevice = null;
                            WifiDisplayController.this.updateConnection();
                        }
                    }
                };
                IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx = this.mHWdcEx;
                if (iHwWifiDisplayControllerEx != null) {
                    iHwWifiDisplayControllerEx.removeSharelinkGroup(this.mWifiP2pChannel, listener);
                } else {
                    this.mWifiP2pManager.removeGroup(this.mWifiP2pChannel, listener);
                }
            } else if (this.mCancelingDevice == null) {
                WifiP2pDevice wifiP2pDevice2 = this.mConnectingDevice;
                if (wifiP2pDevice2 != null && wifiP2pDevice2 != this.mDesiredDevice) {
                    Slog.i(TAG, "Canceling connection to Wifi display: " + this.mConnectingDevice.deviceName);
                    this.mCancelingDevice = this.mConnectingDevice;
                    this.mConnectingDevice = null;
                    unadvertiseDisplay();
                    this.mHandler.removeCallbacks(this.mConnectionTimeout);
                    final WifiP2pDevice oldDevice2 = this.mCancelingDevice;
                    this.mWifiP2pManager.cancelConnect(this.mWifiP2pChannel, new WifiP2pManager.ActionListener() {
                        /* class com.android.server.display.WifiDisplayController.AnonymousClass12 */

                        @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Canceled connection to Wifi display: " + oldDevice2.deviceName);
                            next();
                            WifiDisplayController.this.unregisterPGStateEvent();
                        }

                        @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                        public void onFailure(int reason) {
                            Slog.i(WifiDisplayController.TAG, "Failed to cancel connection to Wifi display: " + oldDevice2.deviceName + ", reason=" + reason);
                            next();
                        }

                        private void next() {
                            if (WifiDisplayController.this.mCancelingDevice == oldDevice2) {
                                WifiDisplayController.this.mCancelingDevice = null;
                                WifiDisplayController.this.updateConnection();
                            }
                        }
                    });
                } else if (this.mDesiredDevice == null) {
                    if (this.mWifiDisplayCertMode) {
                        this.mListener.onDisplaySessionInfo(getSessionInfo(this.mConnectedDeviceGroupInfo, 0));
                    }
                    Slog.i(TAG, "DesiredDevice is null");
                    unadvertiseDisplay();
                } else if (this.mConnectedDevice == null && this.mConnectingDevice == null) {
                    Slog.i(TAG, "Connecting to Wifi display: " + this.mDesiredDevice.deviceName);
                    IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx2 = this.mHWdcEx;
                    if (iHwWifiDisplayControllerEx2 != null) {
                        iHwWifiDisplayControllerEx2.saveCurrentTraceId();
                    }
                    this.mConnectingDevice = this.mDesiredDevice;
                    WifiP2pConfig config = new WifiP2pConfig();
                    WpsInfo wps = new WpsInfo();
                    int i = this.mWifiDisplayWpsConfig;
                    if (i != 4) {
                        wps.setup = i;
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
                        /* class com.android.server.display.WifiDisplayController.AnonymousClass13 */

                        @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Initiated connection to Wifi display: " + newDevice.deviceName);
                            WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mConnectionTimeout, 30000);
                        }

                        @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                        public void onFailure(int reason) {
                            if (WifiDisplayController.this.mConnectingDevice == newDevice) {
                                Slog.i(WifiDisplayController.TAG, "Failed to initiate connection to Wifi display: " + newDevice.deviceName + ", reason=" + reason);
                                WifiDisplayController.this.mConnectingDevice = null;
                                WifiDisplayController.this.updateConnectionErrorCode(reason);
                                WifiDisplayController.this.handleConnectionFailure(false);
                            }
                        }
                    });
                } else if (this.mConnectedDevice != null && this.mRemoteDisplay == null) {
                    Inet4Address addr = getInterfaceAddress(this.mConnectedDeviceGroupInfo);
                    if (addr == null) {
                        Slog.i(TAG, "Failed to get local interface address for communicating with Wifi display: " + this.mConnectedDevice.deviceName);
                        updateConnectionErrorCode(3);
                        handleConnectionFailure(false);
                        return;
                    }
                    this.mWifiP2pManager.setMiracastMode(1);
                    final WifiP2pDevice oldDevice3 = this.mConnectedDevice;
                    String iface = addr.getHostAddress() + ":" + getPortNumber(this.mConnectedDevice);
                    this.mRemoteDisplayInterface = iface;
                    Slog.i(TAG, "Listening for RTSP connection on " + iface + " from Wifi display: " + this.mConnectedDevice.deviceName);
                    this.mRemoteDisplay = RemoteDisplay.listen(iface, new RemoteDisplay.Listener() {
                        /* class com.android.server.display.WifiDisplayController.AnonymousClass14 */

                        public void onDisplayConnected(Surface surface, int width, int height, int flags, int session) {
                            if (WifiDisplayController.this.mConnectedDevice == oldDevice3 && !WifiDisplayController.this.mRemoteDisplayConnected) {
                                Slog.i(WifiDisplayController.TAG, "Opened RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                WifiDisplayController.this.mRemoteDisplayConnected = true;
                                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                if (WifiDisplayController.this.mWifiDisplayCertMode) {
                                    Listener listener = WifiDisplayController.this.mListener;
                                    WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                                    listener.onDisplaySessionInfo(wifiDisplayController.getSessionInfo(wifiDisplayController.mConnectedDeviceGroupInfo, session));
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
                            if (WifiDisplayController.this.mConnectedDevice != oldDevice3) {
                                return;
                            }
                            if (50 != error) {
                                Slog.i(WifiDisplayController.TAG, "Lost RTSP connection with Wifi display due to error " + error + ": " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                if (30 == error) {
                                    WifiDisplayController.this.updateConnectionErrorCode(8);
                                    WifiDisplayController.this.mConnectionRetriesLeft = 0;
                                } else {
                                    WifiDisplayController.this.updateConnectionErrorCode(4);
                                }
                                WifiDisplayController.this.handleConnectionFailure(false);
                                WifiDisplayController.this.unregisterPGStateEvent();
                            } else if (WifiDisplayController.this.mHWdcEx != null) {
                                WifiDisplayController.this.mHWdcEx.advertisDisplayCasting(WifiDisplayController.this.mAdvertisedDisplay);
                            }
                        }

                        public void onDisplayDataNotify(String displayData) {
                            if (WifiDisplayController.this.mHWdcEx != null) {
                                WifiDisplayController.this.mHWdcEx.displayDataNotify(displayData);
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
                                WifiDisplayController.this.mUsingUIBC = true;
                                WifiDisplayController.this.mUIBCCap = capSupport;
                            }
                            return port;
                        }
                    }, this.mHandler, this.mContext.getOpPackageName());
                    IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx3 = this.mHWdcEx;
                    if (iHwWifiDisplayControllerEx3 != null) {
                        iHwWifiDisplayControllerEx3.setDisplayParameters();
                    }
                    this.mHandler.postDelayed(this.mRtspTimeout, (long) ((this.mWifiDisplayCertMode ? RTSP_TIMEOUT_SECONDS_CERT_MODE : 30) * 1000));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WifiDisplaySessionInfo getSessionInfo(WifiP2pGroup info, int session) {
        if (info == null) {
            return null;
        }
        Inet4Address addr = getInterfaceAddress(info);
        return new WifiDisplaySessionInfo(!info.getOwner().deviceAddress.equals(this.mThisDevice.deviceAddress), session, info.getOwner().deviceAddress + " " + info.getNetworkName(), info.getPassphrase(), addr != null ? addr.getHostAddress() : "");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStateChanged(boolean enabled) {
        this.mWifiP2pEnabled = enabled;
        updateWfdEnableState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePeersChanged() {
        requestPeers();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConnectionChanged(NetworkInfo networkInfo) {
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
                /* class com.android.server.display.WifiDisplayController.AnonymousClass15 */

                @Override // android.net.wifi.p2p.WifiP2pManager.GroupInfoListener
                public void onGroupInfoAvailable(WifiP2pGroup info) {
                    if (WifiDisplayController.this.mConnectingDevice != null && !info.contains(WifiDisplayController.this.mConnectingDevice)) {
                        Slog.i(WifiDisplayController.TAG, "Aborting connection to Wifi display because the current P2P group does not contain the device we expected to find: " + WifiDisplayController.this.mConnectingDevice.deviceName + ", group info was: " + WifiDisplayController.describeWifiP2pGroup(info));
                        WifiDisplayController.this.updateConnectionErrorCode(5);
                        WifiDisplayController.this.handleConnectionFailure(false);
                    } else if (WifiDisplayController.this.mDesiredDevice == null || info.contains(WifiDisplayController.this.mDesiredDevice)) {
                        if (WifiDisplayController.this.mHWdcEx != null) {
                            WifiDisplayController.this.mHWdcEx.recoverTraceId();
                        }
                        if (WifiDisplayController.this.mWifiDisplayCertMode) {
                            boolean owner = info.getOwner().deviceAddress.equals(WifiDisplayController.this.mThisDevice.deviceAddress);
                            if (owner && info.getClientList().isEmpty()) {
                                WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                                wifiDisplayController.mConnectingDevice = wifiDisplayController.mDesiredDevice = null;
                                WifiDisplayController.this.mConnectedDeviceGroupInfo = info;
                                WifiDisplayController.this.updateConnection();
                            } else if (WifiDisplayController.this.mConnectingDevice == null && WifiDisplayController.this.mDesiredDevice == null) {
                                WifiDisplayController wifiDisplayController2 = WifiDisplayController.this;
                                wifiDisplayController2.mConnectingDevice = wifiDisplayController2.mDesiredDevice = owner ? info.getClientList().iterator().next() : info.getOwner();
                            }
                        }
                        if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                            Slog.i(WifiDisplayController.TAG, "Connected to Wifi display: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                            if (WifiDisplayController.this.mHWdcEx != null) {
                                WifiDisplayController.this.mHWdcEx.setWorkFrequency(info.getFrequency());
                            }
                            WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mConnectionTimeout);
                            WifiDisplayController.this.mConnectedDeviceGroupInfo = info;
                            WifiDisplayController wifiDisplayController3 = WifiDisplayController.this;
                            wifiDisplayController3.mConnectedDevice = wifiDisplayController3.mConnectingDevice;
                            WifiDisplayController.this.mConnectingDevice = null;
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
    /* access modifiers changed from: public */
    private void handleConnectionFailure(boolean timeoutOccurred) {
        Slog.i(TAG, "Wifi display connection failed!");
        if (this.mDesiredDevice == null) {
            return;
        }
        if (this.mConnectionRetriesLeft > 0) {
            final WifiP2pDevice oldDevice = this.mDesiredDevice;
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.display.WifiDisplayController.AnonymousClass19 */

                @Override // java.lang.Runnable
                public void run() {
                    if (WifiDisplayController.this.mDesiredDevice == oldDevice && WifiDisplayController.this.mConnectionRetriesLeft > 0) {
                        WifiDisplayController.access$3020(WifiDisplayController.this, 1);
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
    /* access modifiers changed from: public */
    private void advertiseDisplay(final WifiDisplay display, final Surface surface, final int width, final int height, final int flags) {
        if (!Objects.equals(this.mAdvertisedDisplay, display) || this.mAdvertisedDisplaySurface != surface || this.mAdvertisedDisplayWidth != width || this.mAdvertisedDisplayHeight != height || this.mAdvertisedDisplayFlags != flags) {
            final WifiDisplay oldDisplay = this.mAdvertisedDisplay;
            final Surface oldSurface = this.mAdvertisedDisplaySurface;
            this.mAdvertisedDisplay = display;
            this.mAdvertisedDisplaySurface = surface;
            this.mAdvertisedDisplayWidth = width;
            this.mAdvertisedDisplayHeight = height;
            this.mAdvertisedDisplayFlags = flags;
            this.mHandler.post(new Runnable() {
                /* class com.android.server.display.WifiDisplayController.AnonymousClass20 */

                @Override // java.lang.Runnable
                public void run() {
                    Surface surface = oldSurface;
                    if (surface == null || surface == surface) {
                        WifiDisplay wifiDisplay = oldDisplay;
                        if (wifiDisplay != null && !wifiDisplay.hasSameAddress(display)) {
                            if (WifiDisplayController.this.mHWdcEx != null) {
                                WifiDisplayController.this.mListener.onSetConnectionFailedReason(WifiDisplayController.this.mHWdcEx.getConnectionErrorCode());
                            }
                            WifiDisplayController.this.updateConnectionErrorCode(-1);
                            WifiDisplayController.this.mListener.onDisplayConnectionFailed();
                        }
                    } else {
                        WifiDisplayController.this.mListener.onDisplayDisconnected();
                    }
                    WifiDisplay wifiDisplay2 = display;
                    if (wifiDisplay2 != null) {
                        if (!wifiDisplay2.hasSameAddress(oldDisplay)) {
                            WifiDisplayController.this.mListener.onDisplayConnecting(display);
                        } else if (!display.equals(oldDisplay)) {
                            WifiDisplayController.this.mListener.onDisplayChanged(display);
                        }
                        Surface surface2 = surface;
                        if (surface2 != null && surface2 != oldSurface) {
                            WifiDisplayController.this.mListener.onSetUibcInfo(WifiDisplayController.this.mUIBCCap);
                            WifiDisplayController.this.mListener.onDisplayConnected(display, surface, width, height, flags);
                        }
                    }
                }
            });
        }
    }

    private void unadvertiseDisplay() {
        advertiseDisplay(null, null, 0, 0, 0);
    }

    private void readvertiseDisplay(WifiDisplay display) {
        advertiseDisplay(display, this.mAdvertisedDisplaySurface, this.mAdvertisedDisplayWidth, this.mAdvertisedDisplayHeight, this.mAdvertisedDisplayFlags);
    }

    private static Inet4Address getInterfaceAddress(WifiP2pGroup info) {
        try {
            Enumeration<InetAddress> addrs = NetworkInterface.getByName(info.getInterface()).getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = addrs.nextElement();
                if (addr instanceof Inet4Address) {
                    return (Inet4Address) addr;
                }
            }
            Slog.w(TAG, "Could not obtain address of network interface " + info.getInterface() + " because it had no IPv4 addresses.");
            return null;
        } catch (SocketException ex) {
            Slog.w(TAG, "Could not obtain address of network interface " + info.getInterface(), ex);
            return null;
        }
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

    private static String describeWifiP2pDevice(WifiP2pDevice device) {
        return device != null ? device.toString().replace('\n', ',') : "null";
    }

    /* access modifiers changed from: private */
    public static String describeWifiP2pGroup(WifiP2pGroup group) {
        return group != null ? group.toString().replace('\n', ',') : "null";
    }

    /* access modifiers changed from: private */
    public static WifiDisplay createWifiDisplay(WifiP2pDevice device) {
        return new WifiDisplay(device.deviceAddress, device.deviceName, (String) null, true, device.wfdInfo.isSessionAvailable(), false);
    }

    @Override // com.android.server.display.IWifiDisplayControllerInner
    public RemoteDisplay getmRemoteDisplay() {
        return this.mRemoteDisplay;
    }

    @Override // com.android.server.display.IWifiDisplayControllerInner
    public Listener getmListener() {
        return this.mListener;
    }

    @Override // com.android.server.display.IWifiDisplayControllerInner
    public void requestStartScanInner() {
        requestStartScan();
    }

    @Override // com.android.server.display.IWifiDisplayControllerInner
    public WifiP2pManager getWifiP2pManagerInner() {
        return this.mWifiP2pManager;
    }

    @Override // com.android.server.display.IWifiDisplayControllerInner
    public WifiP2pManager.Channel getWifiP2pChannelInner() {
        return this.mWifiP2pChannel;
    }

    @Override // com.android.server.display.IWifiDisplayControllerInner
    public boolean getmDiscoverPeersInProgress() {
        return this.mDiscoverPeersInProgress;
    }

    @Override // com.android.server.display.IWifiDisplayControllerInner
    public void requestPeersEx() {
        requestPeers();
    }

    @Override // com.android.server.display.IWifiDisplayControllerInner
    public void disconnectInner() {
        disconnect();
    }

    @Override // com.android.server.display.IWifiDisplayControllerInner
    public void postDelayedDiscover() {
        this.mHandler.postDelayed(this.mDiscoverPeers, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterPGStateEvent() {
        IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx = this.mHWdcEx;
        if (iHwWifiDisplayControllerEx != null) {
            iHwWifiDisplayControllerEx.unregisterPGStateEvent();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConnectionErrorCode(int errorCode) {
        IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx = this.mHWdcEx;
        if (iHwWifiDisplayControllerEx != null) {
            iHwWifiDisplayControllerEx.updateConnectionErrorCode(errorCode);
        }
    }

    public void requestStartScan(int channelID) {
        IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx = this.mHWdcEx;
        if (iHwWifiDisplayControllerEx != null) {
            iHwWifiDisplayControllerEx.requestStartScan(channelID);
        } else {
            requestStartScan();
        }
    }

    public void setConnectParameters(boolean isSupportHdcp, boolean isUibcError, HwWifiDisplayParameters parameters) {
        IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx = this.mHWdcEx;
        if (iHwWifiDisplayControllerEx != null) {
            iHwWifiDisplayControllerEx.setConnectParameters(isSupportHdcp, isUibcError, parameters);
        }
    }

    public void checkVerificationResult(boolean isRight) {
        IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx = this.mHWdcEx;
        if (iHwWifiDisplayControllerEx != null) {
            iHwWifiDisplayControllerEx.checkVerificationResult(isRight);
        }
    }

    public void sendWifiDisplayAction(String action) {
        IHwWifiDisplayControllerEx iHwWifiDisplayControllerEx = this.mHWdcEx;
        if (iHwWifiDisplayControllerEx != null) {
            iHwWifiDisplayControllerEx.sendWifiDisplayAction(action);
        }
    }
}
