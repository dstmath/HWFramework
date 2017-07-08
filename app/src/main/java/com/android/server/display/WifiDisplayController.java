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
import android.util.Slog;
import android.view.Surface;
import com.android.internal.util.DumpUtils.Dump;
import com.android.server.am.ProcessList;
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
    private static final boolean HWFLOW = false;
    private static final int MAX_THROUGHPUT = 50;
    private static final int RTSP_TIMEOUT_SECONDS = 30;
    private static final int RTSP_TIMEOUT_SECONDS_CERT_MODE = 120;
    private static final String TAG = "WifiDisplayController";
    private WifiDisplay mAdvertisedDisplay;
    private int mAdvertisedDisplayFlags;
    private int mAdvertisedDisplayHeight;
    private Surface mAdvertisedDisplaySurface;
    private int mAdvertisedDisplayWidth;
    private final ArrayList<WifiP2pDevice> mAvailableWifiDisplayPeers;
    private WifiP2pDevice mCancelingDevice;
    private WifiP2pDevice mConnectedDevice;
    private WifiP2pGroup mConnectedDeviceGroupInfo;
    private WifiP2pDevice mConnectingDevice;
    private int mConnectionRetriesLeft;
    private final Runnable mConnectionTimeout;
    private final Context mContext;
    private WifiP2pDevice mDesiredDevice;
    private WifiP2pDevice mDisconnectingDevice;
    private final Runnable mDiscoverPeers;
    private boolean mDiscoverPeersInProgress;
    private final Handler mHandler;
    private final Listener mListener;
    private NetworkInfo mNetworkInfo;
    private RemoteDisplay mRemoteDisplay;
    private boolean mRemoteDisplayConnected;
    private String mRemoteDisplayInterface;
    private final Runnable mRtspTimeout;
    private boolean mScanRequested;
    private WifiP2pDevice mThisDevice;
    private boolean mWfdEnabled;
    private boolean mWfdEnabling;
    private boolean mWifiDisplayCertMode;
    private boolean mWifiDisplayOnSetting;
    private int mWifiDisplayWpsConfig;
    private final Channel mWifiP2pChannel;
    private boolean mWifiP2pEnabled;
    private final BroadcastReceiver mWifiP2pExReceiver;
    private final WifiP2pManager mWifiP2pManager;
    private final BroadcastReceiver mWifiP2pReceiver;

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

    /* renamed from: com.android.server.display.WifiDisplayController.14 */
    class AnonymousClass14 implements Runnable {
        final /* synthetic */ WifiDisplay[] val$displays;

        AnonymousClass14(WifiDisplay[] val$displays) {
            this.val$displays = val$displays;
        }

        public void run() {
            WifiDisplayController.this.mListener.onScanResults(this.val$displays);
        }
    }

    /* renamed from: com.android.server.display.WifiDisplayController.16 */
    class AnonymousClass16 implements ActionListener {
        final /* synthetic */ WifiP2pDevice val$oldDevice;

        AnonymousClass16(WifiP2pDevice val$oldDevice) {
            this.val$oldDevice = val$oldDevice;
        }

        public void onSuccess() {
            Slog.i(WifiDisplayController.TAG, "Disconnected from Wifi display: " + this.val$oldDevice.deviceName);
            next();
        }

        public void onFailure(int reason) {
            Slog.i(WifiDisplayController.TAG, "Failed to disconnect from Wifi display: " + this.val$oldDevice.deviceName + ", reason=" + reason);
            next();
        }

        private void next() {
            if (WifiDisplayController.this.mDisconnectingDevice == this.val$oldDevice) {
                WifiDisplayController.this.mDisconnectingDevice = null;
                WifiDisplayController.this.updateConnection();
            }
        }
    }

    /* renamed from: com.android.server.display.WifiDisplayController.17 */
    class AnonymousClass17 implements ActionListener {
        final /* synthetic */ WifiP2pDevice val$oldDevice;

        AnonymousClass17(WifiP2pDevice val$oldDevice) {
            this.val$oldDevice = val$oldDevice;
        }

        public void onSuccess() {
            Slog.i(WifiDisplayController.TAG, "Canceled connection to Wifi display: " + this.val$oldDevice.deviceName);
            next();
        }

        public void onFailure(int reason) {
            Slog.i(WifiDisplayController.TAG, "Failed to cancel connection to Wifi display: " + this.val$oldDevice.deviceName + ", reason=" + reason);
            next();
        }

        private void next() {
            if (WifiDisplayController.this.mCancelingDevice == this.val$oldDevice) {
                WifiDisplayController.this.mCancelingDevice = null;
                WifiDisplayController.this.updateConnection();
            }
        }
    }

    /* renamed from: com.android.server.display.WifiDisplayController.18 */
    class AnonymousClass18 implements ActionListener {
        final /* synthetic */ WifiP2pDevice val$newDevice;

        AnonymousClass18(WifiP2pDevice val$newDevice) {
            this.val$newDevice = val$newDevice;
        }

        public void onSuccess() {
            Slog.i(WifiDisplayController.TAG, "Initiated connection to Wifi display: " + this.val$newDevice.deviceName);
            WifiDisplayController.this.mHandler.postDelayed(WifiDisplayController.this.mConnectionTimeout, 30000);
        }

        public void onFailure(int reason) {
            if (WifiDisplayController.this.mConnectingDevice == this.val$newDevice) {
                Slog.i(WifiDisplayController.TAG, "Failed to initiate connection to Wifi display: " + this.val$newDevice.deviceName + ", reason=" + reason);
                WifiDisplayController.this.mConnectingDevice = null;
                WifiDisplayController.this.handleConnectionFailure(WifiDisplayController.HWFLOW);
            }
        }
    }

    /* renamed from: com.android.server.display.WifiDisplayController.19 */
    class AnonymousClass19 implements android.media.RemoteDisplay.Listener {
        final /* synthetic */ WifiP2pDevice val$oldDevice;

        AnonymousClass19(WifiP2pDevice val$oldDevice) {
            this.val$oldDevice = val$oldDevice;
        }

        public void onDisplayConnected(Surface surface, int width, int height, int flags, int session) {
            if (WifiDisplayController.this.mConnectedDevice == this.val$oldDevice && !WifiDisplayController.this.mRemoteDisplayConnected) {
                Slog.i(WifiDisplayController.TAG, "Opened RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                WifiDisplayController.this.mRemoteDisplayConnected = WifiDisplayController.DEBUG;
                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                if (WifiDisplayController.this.mWifiDisplayCertMode) {
                    WifiDisplayController.this.mListener.onDisplaySessionInfo(WifiDisplayController.this.getSessionInfo(WifiDisplayController.this.mConnectedDeviceGroupInfo, session));
                }
                WifiDisplayController.this.advertiseDisplay(WifiDisplayController.createWifiDisplay(WifiDisplayController.this.mConnectedDevice), surface, width, height, flags);
            }
        }

        public void onDisplayDisconnected() {
            if (WifiDisplayController.this.mConnectedDevice == this.val$oldDevice) {
                Slog.i(WifiDisplayController.TAG, "Closed RTSP connection with Wifi display: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                WifiDisplayController.this.disconnect();
            }
        }

        public void onDisplayError(int error) {
            if (WifiDisplayController.this.mConnectedDevice == this.val$oldDevice) {
                Slog.i(WifiDisplayController.TAG, "Lost RTSP connection with Wifi display due to error " + error + ": " + WifiDisplayController.this.mConnectedDevice.deviceName);
                WifiDisplayController.this.mHandler.removeCallbacks(WifiDisplayController.this.mRtspTimeout);
                WifiDisplayController.this.handleConnectionFailure(WifiDisplayController.HWFLOW);
            }
        }
    }

    /* renamed from: com.android.server.display.WifiDisplayController.21 */
    class AnonymousClass21 implements Runnable {
        final /* synthetic */ WifiP2pDevice val$oldDevice;

        AnonymousClass21(WifiP2pDevice val$oldDevice) {
            this.val$oldDevice = val$oldDevice;
        }

        public void run() {
            if (WifiDisplayController.this.mDesiredDevice == this.val$oldDevice && WifiDisplayController.this.mConnectionRetriesLeft > 0) {
                WifiDisplayController wifiDisplayController = WifiDisplayController.this;
                wifiDisplayController.mConnectionRetriesLeft = wifiDisplayController.mConnectionRetriesLeft - 1;
                Slog.i(WifiDisplayController.TAG, "Retrying Wifi display connection.  Retries left: " + WifiDisplayController.this.mConnectionRetriesLeft);
                WifiDisplayController.this.retryConnection();
            }
        }
    }

    /* renamed from: com.android.server.display.WifiDisplayController.22 */
    class AnonymousClass22 implements Runnable {
        final /* synthetic */ WifiDisplay val$display;
        final /* synthetic */ int val$flags;
        final /* synthetic */ int val$height;
        final /* synthetic */ WifiDisplay val$oldDisplay;
        final /* synthetic */ Surface val$oldSurface;
        final /* synthetic */ Surface val$surface;
        final /* synthetic */ int val$width;

        AnonymousClass22(Surface val$oldSurface, Surface val$surface, WifiDisplay val$oldDisplay, WifiDisplay val$display, int val$width, int val$height, int val$flags) {
            this.val$oldSurface = val$oldSurface;
            this.val$surface = val$surface;
            this.val$oldDisplay = val$oldDisplay;
            this.val$display = val$display;
            this.val$width = val$width;
            this.val$height = val$height;
            this.val$flags = val$flags;
        }

        public void run() {
            if (this.val$oldSurface != null && this.val$surface != this.val$oldSurface) {
                WifiDisplayController.this.mListener.onDisplayDisconnected();
            } else if (!(this.val$oldDisplay == null || this.val$oldDisplay.hasSameAddress(this.val$display))) {
                WifiDisplayController.this.mListener.onDisplayConnectionFailed();
            }
            if (this.val$display != null) {
                if (!this.val$display.hasSameAddress(this.val$oldDisplay)) {
                    WifiDisplayController.this.mListener.onDisplayConnecting(this.val$display);
                } else if (!this.val$display.equals(this.val$oldDisplay)) {
                    WifiDisplayController.this.mListener.onDisplayChanged(this.val$display);
                }
                if (this.val$surface != null && this.val$surface != this.val$oldSurface) {
                    WifiDisplayController.this.mListener.onDisplayConnected(this.val$display, this.val$surface, this.val$width, this.val$height, this.val$flags);
                }
            }
        }
    }

    /* renamed from: com.android.server.display.WifiDisplayController.6 */
    class AnonymousClass6 extends ContentObserver {
        AnonymousClass6(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange, Uri uri) {
            WifiDisplayController.this.updateSettings();
        }
    }

    /* renamed from: com.android.server.display.WifiDisplayController.9 */
    class AnonymousClass9 implements Runnable {
        final /* synthetic */ int val$featureState;

        AnonymousClass9(int val$featureState) {
            this.val$featureState = val$featureState;
        }

        public void run() {
            WifiDisplayController.this.mListener.onFeatureStateChanged(this.val$featureState);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.WifiDisplayController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.WifiDisplayController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.WifiDisplayController.<clinit>():void");
    }

    public WifiDisplayController(Context context, Handler handler, Listener listener) {
        this.mAvailableWifiDisplayPeers = new ArrayList();
        this.mWifiDisplayWpsConfig = 4;
        this.mDiscoverPeers = new Runnable() {
            public void run() {
                WifiDisplayController.this.tryDiscoverPeers();
            }
        };
        this.mConnectionTimeout = new Runnable() {
            public void run() {
                if (WifiDisplayController.this.mConnectingDevice != null && WifiDisplayController.this.mConnectingDevice == WifiDisplayController.this.mDesiredDevice) {
                    Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display connection after 30 seconds: " + WifiDisplayController.this.mConnectingDevice.deviceName);
                    WifiDisplayController.this.handleConnectionFailure(WifiDisplayController.DEBUG);
                }
            }
        };
        this.mRtspTimeout = new Runnable() {
            public void run() {
                if (WifiDisplayController.this.mConnectedDevice != null && WifiDisplayController.this.mRemoteDisplay != null && !WifiDisplayController.this.mRemoteDisplayConnected) {
                    Slog.i(WifiDisplayController.TAG, "Timed out waiting for Wifi display RTSP connection after 30 seconds: " + WifiDisplayController.this.mConnectedDevice.deviceName);
                    WifiDisplayController.this.handleConnectionFailure(WifiDisplayController.DEBUG);
                }
            }
        };
        this.mWifiP2pReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.net.wifi.p2p.STATE_CHANGED")) {
                    boolean enabled = intent.getIntExtra("wifi_p2p_state", 1) == 2 ? WifiDisplayController.DEBUG : WifiDisplayController.HWFLOW;
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
        this.mWifiP2pExReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.p2p.WIFI_P2P_FLAG_CHANGED_ACTION".equals(intent.getAction()) && WifiDisplayControllerHisiExt.hisiWifiEnabled()) {
                    boolean wifiP2pFlag = intent.getIntExtra("extra_p2p_flag", 0) == 1 ? WifiDisplayController.DEBUG : WifiDisplayController.HWFLOW;
                    Slog.d(WifiDisplayController.TAG, "WIFI_P2P_FLAG_CHANGED_ACTION,wifiP2pFlag:" + wifiP2pFlag);
                    WifiDisplayController.this.handleStateChanged(wifiP2pFlag);
                }
            }
        };
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
        ContentObserver settingsObserver = new AnonymousClass6(this.mHandler);
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(Global.getUriFor("wifi_display_on"), HWFLOW, settingsObserver);
        resolver.registerContentObserver(Global.getUriFor("wifi_display_certification_on"), HWFLOW, settingsObserver);
        resolver.registerContentObserver(Global.getUriFor("wifi_display_wps_config"), HWFLOW, settingsObserver);
        updateSettings();
    }

    private void updateSettings() {
        boolean z;
        boolean z2 = DEBUG;
        ContentResolver resolver = this.mContext.getContentResolver();
        if (Global.getInt(resolver, "wifi_display_on", 0) != 0) {
            z = DEBUG;
        } else {
            z = HWFLOW;
        }
        this.mWifiDisplayOnSetting = z;
        if (Global.getInt(resolver, "wifi_display_certification_on", 0) == 0) {
            z2 = HWFLOW;
        }
        this.mWifiDisplayCertMode = z2;
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
            this.mScanRequested = DEBUG;
            updateScanState();
        }
    }

    public void requestStopScan() {
        if (this.mScanRequested) {
            this.mScanRequested = HWFLOW;
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
                wfdInfo.setWfdEnabled(HWFLOW);
                this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, wfdInfo, new ActionListener() {
                    public void onSuccess() {
                        Slog.d(WifiDisplayController.TAG, "Successfully set WFD info.");
                    }

                    public void onFailure(int reason) {
                        Slog.d(WifiDisplayController.TAG, "Failed to set WFD info with reason " + reason + ".");
                    }
                });
            }
            this.mWfdEnabling = HWFLOW;
            this.mWfdEnabled = HWFLOW;
            reportFeatureState();
            updateScanState();
            disconnect();
        } else if (!this.mWfdEnabled && !this.mWfdEnabling) {
            this.mWfdEnabling = DEBUG;
            wfdInfo = new WifiP2pWfdInfo();
            wfdInfo.setWfdEnabled(DEBUG);
            wfdInfo.setDeviceType(0);
            wfdInfo.setSessionAvailable(DEBUG);
            wfdInfo.setControlPort(DEFAULT_CONTROL_PORT);
            wfdInfo.setMaxThroughput(MAX_THROUGHPUT);
            this.mWifiP2pManager.setWFDInfo(this.mWifiP2pChannel, wfdInfo, new ActionListener() {
                public void onSuccess() {
                    Slog.d(WifiDisplayController.TAG, "Successfully set WFD info.");
                    if (WifiDisplayController.this.mWfdEnabling) {
                        WifiDisplayController.this.mWfdEnabling = WifiDisplayController.HWFLOW;
                        WifiDisplayController.this.mWfdEnabled = WifiDisplayController.DEBUG;
                        WifiDisplayController.this.reportFeatureState();
                        WifiDisplayController.this.updateScanState();
                    }
                }

                public void onFailure(int reason) {
                    Slog.d(WifiDisplayController.TAG, "Failed to set WFD info with reason " + reason + ".");
                    WifiDisplayController.this.mWfdEnabling = WifiDisplayController.HWFLOW;
                }
            });
        }
    }

    private void reportFeatureState() {
        this.mHandler.post(new AnonymousClass9(computeFeatureState()));
    }

    private int computeFeatureState() {
        if (!this.mWifiP2pEnabled) {
            return 1;
        }
        int i;
        if (this.mWifiDisplayOnSetting) {
            i = CONNECT_MAX_RETRIES;
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
                this.mDiscoverPeersInProgress = DEBUG;
                handleScanStarted();
                tryDiscoverPeers();
            }
        } else if (this.mDiscoverPeersInProgress) {
            this.mHandler.removeCallbacks(this.mDiscoverPeers);
            if (this.mDesiredDevice == null || this.mDesiredDevice == this.mConnectedDevice) {
                Slog.i(TAG, "Stopping Wifi display scan.");
                this.mDiscoverPeersInProgress = HWFLOW;
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
        WifiDisplay[] displays = (WifiDisplay[]) WifiDisplay.CREATOR.newArray(count);
        for (int i = 0; i < count; i++) {
            WifiP2pDevice device = (WifiP2pDevice) this.mAvailableWifiDisplayPeers.get(i);
            displays[i] = createWifiDisplay(device);
            updateDesiredDevice(device);
        }
        this.mHandler.post(new AnonymousClass14(displays));
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
            Slog.d(TAG, "connect: nothing to do, already connected to " + describeWifiP2pDevice(device) + " and not part way through " + "connecting to a different device.");
        } else if (this.mWfdEnabled) {
            this.mDesiredDevice = device;
            this.mConnectionRetriesLeft = CONNECT_MAX_RETRIES;
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
            this.mRemoteDisplayConnected = HWFLOW;
            this.mHandler.removeCallbacks(this.mRtspTimeout);
            this.mWifiP2pManager.setMiracastMode(0);
            unadvertiseDisplay();
        }
        if (this.mDisconnectingDevice == null) {
            if (this.mConnectedDevice != null && this.mConnectedDevice != this.mDesiredDevice) {
                Slog.i(TAG, "Disconnecting from Wifi display: " + this.mConnectedDevice.deviceName);
                this.mDisconnectingDevice = this.mConnectedDevice;
                this.mConnectedDevice = null;
                this.mConnectedDeviceGroupInfo = null;
                unadvertiseDisplay();
                this.mWifiP2pManager.removeGroup(this.mWifiP2pChannel, new AnonymousClass16(this.mDisconnectingDevice));
            } else if (this.mCancelingDevice == null) {
                if (this.mConnectingDevice != null && this.mConnectingDevice != this.mDesiredDevice) {
                    Slog.i(TAG, "Canceling connection to Wifi display: " + this.mConnectingDevice.deviceName);
                    this.mCancelingDevice = this.mConnectingDevice;
                    this.mConnectingDevice = null;
                    unadvertiseDisplay();
                    this.mHandler.removeCallbacks(this.mConnectionTimeout);
                    this.mWifiP2pManager.cancelConnect(this.mWifiP2pChannel, new AnonymousClass17(this.mCancelingDevice));
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
                    this.mWifiP2pManager.connect(this.mWifiP2pChannel, config, new AnonymousClass18(this.mDesiredDevice));
                } else {
                    if (this.mConnectedDevice != null && this.mRemoteDisplay == null) {
                        Inet4Address addr = getInterfaceAddress(this.mConnectedDeviceGroupInfo);
                        if (addr == null) {
                            Slog.i(TAG, "Failed to get local interface address for communicating with Wifi display: " + this.mConnectedDevice.deviceName);
                            handleConnectionFailure(HWFLOW);
                            return;
                        }
                        this.mWifiP2pManager.setMiracastMode(1);
                        WifiP2pDevice oldDevice = this.mConnectedDevice;
                        String iface = addr.getHostAddress() + ":" + getPortNumber(this.mConnectedDevice);
                        this.mRemoteDisplayInterface = iface;
                        Slog.i(TAG, "Listening for RTSP connection on " + iface + " from Wifi display: " + this.mConnectedDevice.deviceName);
                        this.mRemoteDisplay = RemoteDisplay.listen(iface, new AnonymousClass19(oldDevice), this.mHandler, this.mContext.getOpPackageName());
                        this.mHandler.postDelayed(this.mRtspTimeout, (long) ((this.mWifiDisplayCertMode ? RTSP_TIMEOUT_SECONDS_CERT_MODE : RTSP_TIMEOUT_SECONDS) * ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE));
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
        boolean z = info.getOwner().deviceAddress.equals(this.mThisDevice.deviceAddress) ? HWFLOW : DEBUG;
        String str = info.getOwner().deviceAddress + " " + info.getNetworkName();
        String passphrase = info.getPassphrase();
        if (addr != null) {
            hostAddress = addr.getHostAddress();
        } else {
            hostAddress = "";
        }
        WifiDisplaySessionInfo sessionInfo = new WifiDisplaySessionInfo(z, session, str, passphrase, hostAddress);
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
                    if (WifiDisplayController.this.mConnectingDevice != null && !info.contains(WifiDisplayController.this.mConnectingDevice)) {
                        Slog.i(WifiDisplayController.TAG, "Aborting connection to Wifi display because the current P2P group does not contain the device we expected to find: " + WifiDisplayController.this.mConnectingDevice.deviceName + ", group info was: " + WifiDisplayController.describeWifiP2pGroup(info));
                        WifiDisplayController.this.handleConnectionFailure(WifiDisplayController.HWFLOW);
                    } else if (WifiDisplayController.this.mDesiredDevice == null || info.contains(WifiDisplayController.this.mDesiredDevice)) {
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
            WifiP2pDevice oldDevice = this.mDesiredDevice;
            Handler handler = this.mHandler;
            Runnable anonymousClass21 = new AnonymousClass21(oldDevice);
            if (!timeoutOccurred) {
                i = CONNECT_RETRY_DELAY_MILLIS;
            }
            handler.postDelayed(anonymousClass21, (long) i);
            return;
        }
        disconnect();
    }

    private void advertiseDisplay(WifiDisplay display, Surface surface, int width, int height, int flags) {
        if (Objects.equal(this.mAdvertisedDisplay, display) && this.mAdvertisedDisplaySurface == surface && this.mAdvertisedDisplayWidth == width && this.mAdvertisedDisplayHeight == height) {
            if (this.mAdvertisedDisplayFlags == flags) {
                return;
            }
        }
        WifiDisplay oldDisplay = this.mAdvertisedDisplay;
        Surface oldSurface = this.mAdvertisedDisplaySurface;
        this.mAdvertisedDisplay = display;
        this.mAdvertisedDisplaySurface = surface;
        this.mAdvertisedDisplayWidth = width;
        this.mAdvertisedDisplayHeight = height;
        this.mAdvertisedDisplayFlags = flags;
        this.mHandler.post(new AnonymousClass22(oldSurface, surface, oldDisplay, display, width, height, flags));
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
            return HWFLOW;
        }
        return isPrimarySinkDeviceType(device.wfdInfo.getDeviceType());
    }

    private static boolean isPrimarySinkDeviceType(int deviceType) {
        if (deviceType == 1 || deviceType == CONNECT_MAX_RETRIES) {
            return DEBUG;
        }
        return HWFLOW;
    }

    private static String describeWifiP2pDevice(WifiP2pDevice device) {
        return device != null ? device.toString().replace('\n', ',') : "null";
    }

    private static String describeWifiP2pGroup(WifiP2pGroup group) {
        return group != null ? group.toString().replace('\n', ',') : "null";
    }

    private static WifiDisplay createWifiDisplay(WifiP2pDevice device) {
        return new WifiDisplay(device.deviceAddress, device.deviceName, null, DEBUG, device.wfdInfo.isSessionAvailable(), HWFLOW);
    }
}
