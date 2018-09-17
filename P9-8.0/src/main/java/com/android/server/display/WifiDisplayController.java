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
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.os.Handler;
import android.provider.Settings.Global;
import android.util.Log;
import android.util.Slog;
import android.view.Surface;
import com.android.internal.util.DumpUtils.Dump;
import com.android.server.display.hisi_wifi.WifiDisplayControllerHisiExt;
import com.android.server.job.controllers.JobStatus;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import libcore.util.Objects;

final class WifiDisplayController implements Dump {
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;
    private static final int CONNECT_MAX_RETRIES = 3;
    private static final int CONNECT_RETRY_DELAY_MILLIS = 500;
    private static final boolean DEBUG = true;
    private static final int DEFAULT_CONTROL_PORT = 7236;
    private static final int DISCOVER_PEERS_INTERVAL_MILLIS = 10000;
    private static final String HUAWEI_WIFI_1101_P2P = "huawei.android.permission.WIFI_1101_P2P";
    private static final boolean HWFLOW;
    private static final int MAX_THROUGHPUT = 50;
    private static final int RTSP_TIMEOUT_SECONDS = 30;
    private static final int RTSP_TIMEOUT_SECONDS_CERT_MODE = 120;
    private static final String TAG = "WifiDisplayController";
    private WifiDisplay mAdvertisedDisplay;
    private int mAdvertisedDisplayFlags;
    private int mAdvertisedDisplayHeight;
    private Surface mAdvertisedDisplaySurface;
    private int mAdvertisedDisplayWidth;
    private final ArrayList<WifiP2pDevice> mAvailableWifiDisplayPeers = new ArrayList();
    private WifiP2pDevice mCancelingDevice;
    private WifiP2pDevice mConnectedDevice;
    private WifiP2pGroup mConnectedDeviceGroupInfo;
    private WifiP2pDevice mConnectingDevice;
    private int mConnectionRetriesLeft;
    private final Runnable mConnectionTimeout = new Runnable() {
        public void run() {
            if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display connection after 30 seconds: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                WifiDisplayController.this.handleConnectionFailure(true);
            }
        }
    };
    private final Context mContext;
    private WifiP2pDevice mDesiredDevice;
    private WifiP2pDevice mDisconnectingDevice;
    private final Runnable mDiscoverPeers = new Runnable() {
        public void run() {
            WifiDisplayController.this.tryDiscoverPeers();
        }
    };
    private boolean mDiscoverPeersInProgress;
    private final Handler mHandler;
    private final Listener mListener;
    private NetworkInfo mNetworkInfo;
    private RemoteDisplay mRemoteDisplay;
    private boolean mRemoteDisplayConnected;
    private String mRemoteDisplayInterface;
    private final Runnable mRtspTimeout = new Runnable() {
        public void run() {
            if (WifiDisplayController.this.mConnectedDevice != null && WifiDisplayController.this.mRemoteDisplay != null && (WifiDisplayController.this.mRemoteDisplayConnected ^ 1) != 0) {
                Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display RTSP connection after 30 seconds: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                WifiDisplayController.this.handleConnectionFailure(true);
            }
        }
    };
    private boolean mScanRequested;
    private WifiP2pDevice mThisDevice;
    private boolean mWfdEnabled;
    private boolean mWfdEnabling;
    private boolean mWifiDisplayCertMode;
    private boolean mWifiDisplayOnSetting;
    private int mWifiDisplayWpsConfig = 4;
    private final Channel mWifiP2pChannel;
    private boolean mWifiP2pEnabled;
    private final BroadcastReceiver mWifiP2pExReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.p2p.WIFI_P2P_FLAG_CHANGED_ACTION".equals(intent.getAction()) && WifiDisplayControllerHisiExt.hisiWifiEnabled()) {
                boolean wifiP2pFlag = intent.getIntExtra("extra_p2p_flag", 0) == 1;
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
                boolean enabled = intent.getIntExtra("wifi_p2p_state", 1) == 2;
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
                WifiDisplayController.this.mThisDevice = (WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice");
                Slog.d(WifiDisplayController.TAG, "Received WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: mThisDevice= " + WifiDisplayController.this.mThisDevice);
            }
        }
    };

    public interface Listener {
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
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public WifiDisplayController(Context context, Handler handler, Listener listener) {
        this.mContext = context;
        this.mHandler = handler;
        this.mListener = listener;
        this.mWifiP2pManager = (WifiP2pManager) context.getSystemService("wifip2p");
        this.mWifiP2pChannel = this.mWifiP2pManager.initialize(context, handler.getLooper(), null);
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
        resolver.registerContentObserver(Global.getUriFor("wifi_display_on"), false, settingsObserver);
        resolver.registerContentObserver(Global.getUriFor("wifi_display_certification_on"), false, settingsObserver);
        resolver.registerContentObserver(Global.getUriFor("wifi_display_wps_config"), false, settingsObserver);
        updateSettings();
    }

    private void updateSettings() {
        boolean z = true;
        ContentResolver resolver = this.mContext.getContentResolver();
        this.mWifiDisplayOnSetting = Global.getInt(resolver, "wifi_display_on", 0) != 0;
        if (Global.getInt(resolver, "wifi_display_certification_on", 0) == 0) {
            z = false;
        }
        this.mWifiDisplayCertMode = z;
        this.mWifiDisplayWpsConfig = 4;
        if (this.mWifiDisplayCertMode) {
            this.mWifiDisplayWpsConfig = Global.getInt(resolver, "wifi_display_wps_config", 4);
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
        for (WifiP2pDevice device : this.mAvailableWifiDisplayPeers) {
            pw.println("  " + describeWifiP2pDevice(device));
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
        for (WifiP2pDevice device : this.mAvailableWifiDisplayPeers) {
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
        WifiP2pWfdInfo wfdInfo;
        if (!this.mWifiDisplayOnSetting || !this.mWifiP2pEnabled) {
            if (this.mWfdEnabled || this.mWfdEnabling) {
                wfdInfo = new WifiP2pWfdInfo();
                wfdInfo.setWfdEnabled(false);
                this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, wfdInfo, new ActionListener() {
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
        } else if (!this.mWfdEnabled && (this.mWfdEnabling ^ 1) != 0) {
            this.mWfdEnabling = true;
            wfdInfo = new WifiP2pWfdInfo();
            wfdInfo.setWfdEnabled(true);
            wfdInfo.setDeviceType(0);
            wfdInfo.setSessionAvailable(true);
            wfdInfo.setControlPort(DEFAULT_CONTROL_PORT);
            wfdInfo.setMaxThroughput(50);
            this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, wfdInfo, new ActionListener() {
                public void onSuccess() {
                    Slog.d(WifiDisplayController.TAG, "Successfully set WFD info.");
                    if (WifiDisplayController.this.mWfdEnabling) {
                        WifiDisplayController.this.mWfdEnabling = false;
                        WifiDisplayController.this.mWfdEnabled = true;
                        WifiDisplayController.this.reportFeatureState();
                        WifiDisplayController.this.updateScanState();
                    }
                }

                public void onFailure(int reason) {
                    Slog.d(WifiDisplayController.TAG, "Failed to set WFD info with reason " + reason + ".");
                    WifiDisplayController.this.mWfdEnabling = false;
                }
            });
        }
    }

    private void reportFeatureState() {
        final int featureState = computeFeatureState();
        this.mHandler.post(new Runnable() {
            public void run() {
                WifiDisplayController.this.mListener.onFeatureStateChanged(featureState);
            }
        });
    }

    private int computeFeatureState() {
        if (!this.mWifiP2pEnabled) {
            return 1;
        }
        int i;
        if (this.mWifiDisplayOnSetting) {
            i = 3;
        } else {
            i = 2;
        }
        return i;
    }

    private void updateScanState() {
        if (HWFLOW) {
            Slog.i(TAG, "updateScanState mScanRequested=" + this.mScanRequested + " updateScanState mWfdEnabled=" + this.mWfdEnabled);
        }
        if (HWFLOW) {
            Slog.i(TAG, "mDiscoverPeersInProgress=" + this.mDiscoverPeersInProgress + " mDesiredDevice=" + this.mDesiredDevice);
        }
        if (this.mScanRequested && this.mWfdEnabled && this.mDesiredDevice == null) {
            if (!this.mDiscoverPeersInProgress) {
                Slog.i(TAG, "Starting Wifi display scan.");
                this.mDiscoverPeersInProgress = true;
                handleScanStarted();
                tryDiscoverPeers();
            }
        } else if (this.mDiscoverPeersInProgress) {
            this.mHandler.removeCallbacks(this.mDiscoverPeers);
            if (this.mDesiredDevice == null || this.mDesiredDevice == this.mConnectedDevice) {
                Slog.i(TAG, "Stopping Wifi display scan.");
                this.mDiscoverPeersInProgress = false;
                stopPeerDiscovery();
                handleScanFinished();
            }
        }
    }

    private void tryDiscoverPeers() {
        this.mWifiP2pManager.discoverPeers(this.mWifiP2pChannel, new ActionListener() {
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
    }

    private void stopPeerDiscovery() {
        this.mWifiP2pManager.stopPeerDiscovery(this.mWifiP2pChannel, new ActionListener() {
            public void onSuccess() {
                Slog.d(WifiDisplayController.TAG, "Stop peer discovery succeeded.");
            }

            public void onFailure(int reason) {
                Slog.d(WifiDisplayController.TAG, "Stop peer discovery failed with reason " + reason + ".");
            }
        });
    }

    private void requestPeers() {
        this.mWifiP2pManager.requestPeers(this.mWifiP2pChannel, new PeerListListener() {
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

    private void handleScanResults() {
        int count = this.mAvailableWifiDisplayPeers.size();
        final WifiDisplay[] displays = (WifiDisplay[]) WifiDisplay.CREATOR.newArray(count);
        for (int i = 0; i < count; i++) {
            WifiP2pDevice device = (WifiP2pDevice) this.mAvailableWifiDisplayPeers.get(i);
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
        if (this.mDesiredDevice != null && (this.mDesiredDevice.deviceAddress.equals(device.deviceAddress) ^ 1) != 0) {
            Slog.d(TAG, "connect: nothing to do, already connecting to " + describeWifiP2pDevice(device));
        } else if (this.mConnectedDevice != null && (this.mConnectedDevice.deviceAddress.equals(device.deviceAddress) ^ 1) != 0 && this.mDesiredDevice == null) {
            Slog.d(TAG, "connect: nothing to do, already connected to " + describeWifiP2pDevice(device) + " and not part way through " + "connecting to a different device.");
        } else if (this.mWfdEnabled) {
            this.mDesiredDevice = device;
            this.mConnectionRetriesLeft = 3;
            updateConnection();
        } else {
            Slog.i(TAG, "Ignoring request to connect to Wifi display because the  feature is currently disabled: " + device.deviceName);
        }
    }

    private void disconnect() {
        this.mDesiredDevice = null;
        updateConnection();
    }

    private void retryConnection() {
        this.mDesiredDevice = new WifiP2pDevice(this.mDesiredDevice);
        updateConnection();
    }

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
        }
        if (this.mDisconnectingDevice == null) {
            final WifiP2pDevice oldDevice;
            if (this.mConnectedDevice != null && this.mConnectedDevice != this.mDesiredDevice) {
                Slog.i(TAG, "Disconnecting from Wifi display: " + this.mConnectedDevice.deviceName);
                this.mDisconnectingDevice = this.mConnectedDevice;
                this.mConnectedDevice = null;
                this.mConnectedDeviceGroupInfo = null;
                unadvertiseDisplay();
                oldDevice = this.mDisconnectingDevice;
                this.mWifiP2pManager.removeGroup(this.mWifiP2pChannel, new ActionListener() {
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
                            WifiDisplayController.this.mDisconnectingDevice = null;
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
                    oldDevice = this.mCancelingDevice;
                    this.mWifiP2pManager.cancelConnect(this.mWifiP2pChannel, new ActionListener() {
                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Canceled connection to Wifi display: " + oldDevice.deviceName);
                            next();
                        }

                        public void onFailure(int reason) {
                            Slog.i(WifiDisplayController.TAG, "Failed to cancel connection to Wifi display: " + oldDevice.deviceName + ", reason=" + reason);
                            next();
                        }

                        private void next() {
                            if (WifiDisplayController.this.mCancelingDevice == oldDevice) {
                                WifiDisplayController.this.mCancelingDevice = null;
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
                    config.groupOwnerIntent = 0;
                    advertiseDisplay(createWifiDisplay(this.mConnectingDevice), null, 0, 0, 0);
                    final WifiP2pDevice newDevice = this.mDesiredDevice;
                    this.mWifiP2pManager.connect(this.mWifiP2pChannel, config, new ActionListener() {
                        public void onSuccess() {
                            Slog.i(WifiDisplayController.TAG, "Initiated connection to Wifi display: " + newDevice.deviceName);
                            WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mConnectionTimeout, 30000);
                        }

                        public void onFailure(int reason) {
                            if (WifiDisplayController.this.mConnectingDevice == newDevice) {
                                Slog.i(WifiDisplayController.TAG, "Failed to initiate connection to Wifi display: " + newDevice.deviceName + ", reason=" + reason);
                                WifiDisplayController.this.mConnectingDevice = null;
                                WifiDisplayController.this.handleConnectionFailure(false);
                            }
                        }
                    });
                } else {
                    if (this.mConnectedDevice != null && this.mRemoteDisplay == null) {
                        Inet4Address addr = getInterfaceAddress(this.mConnectedDeviceGroupInfo);
                        if (addr == null) {
                            Slog.i(TAG, "Failed to get local interface address for communicating with Wifi display: " + this.mConnectedDevice.deviceName);
                            handleConnectionFailure(false);
                            return;
                        }
                        this.mWifiP2pManager.setMiracastMode(1);
                        oldDevice = this.mConnectedDevice;
                        String iface = addr.getHostAddress() + ":" + getPortNumber(this.mConnectedDevice);
                        this.mRemoteDisplayInterface = iface;
                        Slog.i(TAG, "Listening for RTSP connection on " + iface + " from Wifi display: " + this.mConnectedDevice.deviceName);
                        this.mRemoteDisplay = RemoteDisplay.listen(iface, new android.media.RemoteDisplay.Listener() {
                            public void onDisplayConnected(Surface surface, int width, int height, int flags, int session) {
                                if (WifiDisplayController.this.mConnectedDevice == oldDevice && (WifiDisplayController.this.mRemoteDisplayConnected ^ 1) != 0) {
                                    Slog.i(WifiDisplayController.TAG, "Opened RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                    WifiDisplayController.this.mRemoteDisplayConnected = true;
                                    WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                    if (WifiDisplayController.this.mWifiDisplayCertMode) {
                                        WifiDisplayController.this.mListener.onDisplaySessionInfo(WifiDisplayController.this.getSessionInfo(WifiDisplayController.this.mConnectedDeviceGroupInfo, session));
                                    }
                                    WifiDisplayController.this.advertiseDisplay(WifiDisplayController.createWifiDisplay(WifiDisplayController.this.mConnectedDevice), surface, width, height, flags);
                                }
                            }

                            public void onDisplayDisconnected() {
                                if (WifiDisplayController.this.mConnectedDevice == oldDevice) {
                                    Slog.i(WifiDisplayController.TAG, "Closed RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                    WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                    WifiDisplayController.this.disconnect();
                                }
                            }

                            public void onDisplayError(int error) {
                                if (WifiDisplayController.this.mConnectedDevice == oldDevice) {
                                    Slog.i(WifiDisplayController.TAG, "Lost RTSP connection with Wifi display due to error " + error + ": " + WifiDisplayController.this.mConnectedDevice.deviceName);
                                    WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                                    WifiDisplayController.this.handleConnectionFailure(false);
                                }
                            }
                        }, this.mHandler, this.mContext.getOpPackageName());
                        this.mHandler.postDelayed(this.mRtspTimeout, (long) ((this.mWifiDisplayCertMode ? RTSP_TIMEOUT_SECONDS_CERT_MODE : 30) * 1000));
                    }
                }
            }
        }
    }

    private WifiDisplaySessionInfo getSessionInfo(WifiP2pGroup info, int session) {
        if (info == null) {
            return null;
        }
        String hostAddress;
        Inet4Address addr = getInterfaceAddress(info);
        boolean equals = info.getOwner().deviceAddress.equals(this.mThisDevice.deviceAddress) ^ 1;
        String str = info.getOwner().deviceAddress + " " + info.getNetworkName();
        String passphrase = info.getPassphrase();
        if (addr != null) {
            hostAddress = addr.getHostAddress();
        } else {
            hostAddress = "";
        }
        WifiDisplaySessionInfo sessionInfo = new WifiDisplaySessionInfo(equals, session, str, passphrase, hostAddress);
        Slog.d(TAG, sessionInfo.toString());
        return sessionInfo;
    }

    private void handleStateChanged(boolean enabled) {
        this.mWifiP2pEnabled = enabled;
        updateWfdEnableState();
    }

    private void handlePeersChanged() {
        requestPeers();
    }

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
            this.mWifiP2pManager.requestGroupInfo(this.mWifiP2pChannel, new GroupInfoListener() {
                public void onGroupInfoAvailable(WifiP2pGroup info) {
                    Slog.d(WifiDisplayController.TAG, "Received group info: " + WifiDisplayController.describeWifiP2pGroup(info));
                    if (WifiDisplayController.this.mConnectingDevice != null && (info.contains(WifiDisplayController.this.mConnectingDevice) ^ 1) != 0) {
                        Slog.i(WifiDisplayController.TAG, "Aborting connection to Wifi display because the current P2P group does not contain the device we expected to find: " + WifiDisplayController.this.mConnectingDevice.deviceName + ", group info was: " + WifiDisplayController.describeWifiP2pGroup(info));
                        WifiDisplayController.this.handleConnectionFailure(false);
                    } else if (WifiDisplayController.this.mDesiredDevice == null || (info.contains(WifiDisplayController.this.mDesiredDevice) ^ 1) == 0) {
                        if (WifiDisplayController.this.mWifiDisplayCertMode) {
                            boolean owner = info.getOwner().deviceAddress.equals(WifiDisplayController.this.mThisDevice.deviceAddress);
                            if (owner && info.getClientList().isEmpty()) {
                                WifiDisplayController.this.mConnectingDevice = WifiDisplayController.this.mDesiredDevice = null;
                                WifiDisplayController.this.mConnectedDeviceGroupInfo = info;
                                WifiDisplayController.this.updateConnection();
                            } else if (WifiDisplayController.this.mConnectingDevice == null && WifiDisplayController.this.mDesiredDevice == null) {
                                WifiDisplayController.this.mConnectingDevice = WifiDisplayController.this.mDesiredDevice = owner ? (WifiP2pDevice) info.getClientList().iterator().next() : info.getOwner();
                            }
                        }
                        if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                            Slog.i(WifiDisplayController.TAG, "Connected to Wifi display: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                            WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mConnectionTimeout);
                            WifiDisplayController.this.mConnectedDeviceGroupInfo = info;
                            WifiDisplayController.this.mConnectedDevice = WifiDisplayController.this.mConnectingDevice;
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

    private void handleConnectionFailure(boolean timeoutOccurred) {
        int i = 0;
        Slog.i(TAG, "Wifi display connection failed!");
        if (this.mDesiredDevice == null) {
            return;
        }
        if (this.mConnectionRetriesLeft > 0) {
            final WifiP2pDevice oldDevice = this.mDesiredDevice;
            Handler handler = this.mHandler;
            Runnable anonymousClass21 = new Runnable() {
                public void run() {
                    if (WifiDisplayController.this.mDesiredDevice == oldDevice && WifiDisplayController.this.mConnectionRetriesLeft > 0) {
                        WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                        wifiDisplayController.mConnectionRetriesLeft = wifiDisplayController.mConnectionRetriesLeft - 1;
                        Slog.i(WifiDisplayController.TAG, "Retrying Wifi display connection.  Retries left: " + WifiDisplayController.this.mConnectionRetriesLeft);
                        WifiDisplayController.this.retryConnection();
                    }
                }
            };
            if (!timeoutOccurred) {
                i = 500;
            }
            handler.postDelayed(anonymousClass21, (long) i);
            return;
        }
        disconnect();
    }

    private void advertiseDisplay(WifiDisplay display, Surface surface, int width, int height, int flags) {
        if (!Objects.equal(this.mAdvertisedDisplay, display) || this.mAdvertisedDisplaySurface != surface || this.mAdvertisedDisplayWidth != width || this.mAdvertisedDisplayHeight != height || this.mAdvertisedDisplayFlags != flags) {
            final WifiDisplay oldDisplay = this.mAdvertisedDisplay;
            final Surface oldSurface = this.mAdvertisedDisplaySurface;
            this.mAdvertisedDisplay = display;
            this.mAdvertisedDisplaySurface = surface;
            this.mAdvertisedDisplayWidth = width;
            this.mAdvertisedDisplayHeight = height;
            this.mAdvertisedDisplayFlags = flags;
            final Surface surface2 = surface;
            final WifiDisplay wifiDisplay = display;
            final int i = width;
            final int i2 = height;
            final int i3 = flags;
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (oldSurface != null && surface2 != oldSurface) {
                        WifiDisplayController.this.mListener.onDisplayDisconnected();
                    } else if (!(oldDisplay == null || (oldDisplay.hasSameAddress(wifiDisplay) ^ 1) == 0)) {
                        WifiDisplayController.this.mListener.onDisplayConnectionFailed();
                    }
                    if (wifiDisplay != null) {
                        if (!wifiDisplay.hasSameAddress(oldDisplay)) {
                            WifiDisplayController.this.mListener.onDisplayConnecting(wifiDisplay);
                        } else if (!wifiDisplay.equals(oldDisplay)) {
                            WifiDisplayController.this.mListener.onDisplayChanged(wifiDisplay);
                        }
                        if (surface2 != null && surface2 != oldSurface) {
                            WifiDisplayController.this.mListener.onDisplayConnected(wifiDisplay, surface2, i, i2, i3);
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
        NetworkInterface iface;
        if (WifiDisplayControllerHisiExt.hisiWifiEnabled()) {
            try {
                iface = NetworkInterface.getByName(WifiDisplayControllerHisiExt.getHisiWifiInface());
                Slog.w(TAG, "get InterfaceAddress from network interface " + WifiDisplayControllerHisiExt.getHisiWifiInface());
            } catch (SocketException ex) {
                Slog.w(TAG, "Could not obtain address of network interface " + WifiDisplayControllerHisiExt.getHisiWifiInface(), ex);
                return null;
            }
        }
        try {
            iface = NetworkInterface.getByName(info.getInterface());
            Slog.w(TAG, "get InterfaceAddress from network interface " + info.getInterface());
        } catch (SocketException ex2) {
            Slog.w(TAG, "Could not obtain address of network interface " + info.getInterface(), ex2);
            return null;
        }
        Enumeration<InetAddress> addrs = iface.getInetAddresses();
        while (addrs.hasMoreElements()) {
            InetAddress addr = (InetAddress) addrs.nextElement();
            if (addr instanceof Inet4Address) {
                return (Inet4Address) addr;
            }
        }
        Slog.w(TAG, "Could not obtain address of network interface " + info.getInterface() + " because it had no IPv4 addresses.");
        return null;
    }

    private static int getPortNumber(WifiP2pDevice device) {
        if (device.deviceName.startsWith("DIRECT-") && device.deviceName.endsWith("Broadcom")) {
            return 8554;
        }
        return DEFAULT_CONTROL_PORT;
    }

    private static boolean isWifiDisplay(WifiP2pDevice device) {
        if (device.wfdInfo == null || !device.wfdInfo.isWfdEnabled()) {
            return false;
        }
        return isPrimarySinkDeviceType(device.wfdInfo.getDeviceType());
    }

    private static boolean isPrimarySinkDeviceType(int deviceType) {
        if (deviceType == 1 || deviceType == 3) {
            return true;
        }
        return false;
    }

    private static String describeWifiP2pDevice(WifiP2pDevice device) {
        return device != null ? device.toString().replace(10, ',') : "null";
    }

    private static String describeWifiP2pGroup(WifiP2pGroup group) {
        return group != null ? group.toString().replace(10, ',') : "null";
    }

    private static WifiDisplay createWifiDisplay(WifiP2pDevice device) {
        return new WifiDisplay(device.deviceAddress, device.deviceName, null, true, device.wfdInfo.isSessionAvailable(), false);
    }
}
