package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.ClientModeManager;
import com.android.server.wifi.ScanOnlyModeManager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiStateMachinePrime;
import com.android.server.wifi.scanner.ScanResultRecords;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Iterator;

public class WifiStateMachinePrime {
    static final int BASE = 131072;
    static final int CMD_AP_STOPPED = 131096;
    static final int CMD_CLIENT_MODE_FAILED = 131376;
    static final int CMD_CLIENT_MODE_STOPPED = 131375;
    static final int CMD_SCAN_ONLY_MODE_FAILED = 131276;
    static final int CMD_SCAN_ONLY_MODE_STOPPED = 131275;
    static final int CMD_START_AP = 131093;
    static final int CMD_START_AP_FAILURE = 131094;
    static final int CMD_START_CLIENT_MODE = 131372;
    static final int CMD_START_CLIENT_MODE_FAILURE = 131373;
    static final int CMD_START_SCAN_ONLY_MODE = 131272;
    static final int CMD_START_SCAN_ONLY_MODE_FAILURE = 131273;
    static final int CMD_STOP_AP = 131095;
    static final int CMD_STOP_CLIENT_MODE = 131374;
    static final int CMD_STOP_SCAN_ONLY_MODE = 131274;
    private static final String TAG = "WifiStateMachinePrime";
    /* access modifiers changed from: private */
    public final ArraySet<ActiveModeManager> mActiveModeManagers = new ArraySet<>();
    private final IBatteryStats mBatteryStats;
    /* access modifiers changed from: private */
    public ClientModeManager.Listener mClientModeCallback;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public DefaultModeManager mDefaultModeManager;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public final Looper mLooper;
    /* access modifiers changed from: private */
    public ModeStateMachine mModeStateMachine;
    /* access modifiers changed from: private */
    public ScanOnlyModeManager.Listener mScanOnlyCallback;
    /* access modifiers changed from: private */
    public final ScanRequestProxy mScanRequestProxy;
    private final SelfRecovery mSelfRecovery;
    /* access modifiers changed from: private */
    public WifiManager.SoftApCallback mSoftApCallback;
    /* access modifiers changed from: private */
    public WifiManager.SoftApCallback mSoftApListener;
    /* access modifiers changed from: private */
    public BaseWifiDiagnostics mWifiDiagnostics;
    /* access modifiers changed from: private */
    public final WifiInjector mWifiInjector;
    private final WifiNative mWifiNative;
    private WifiNative.StatusListener mWifiNativeStatusListener;

    private class ModeCallback {
        ActiveModeManager mActiveManager;

        private ModeCallback() {
        }

        /* access modifiers changed from: package-private */
        public void setActiveModeManager(ActiveModeManager manager) {
            this.mActiveManager = manager;
        }

        /* access modifiers changed from: package-private */
        public ActiveModeManager getActiveModeManager() {
            return this.mActiveManager;
        }
    }

    private class ModeStateMachine extends StateMachine {
        public static final int CMD_DISABLE_WIFI = 3;
        public static final int CMD_START_CLIENT_MODE = 0;
        public static final int CMD_START_SCAN_ONLY_MODE = 1;
        private final State mClientModeActiveState = new ClientModeActiveState();
        private final State mScanOnlyModeActiveState = new ScanOnlyModeActiveState();
        /* access modifiers changed from: private */
        public final State mWifiDisabledState = new WifiDisabledState();

        class ClientModeActiveState extends ModeActiveState {
            ClientListener mListener;

            private class ClientListener implements ClientModeManager.Listener {
                private ClientListener() {
                }

                public void onStateChanged(int state) {
                    if (this != ClientModeActiveState.this.mListener) {
                        Log.d(WifiStateMachinePrime.TAG, "Client mode state change from previous manager");
                        return;
                    }
                    Log.d(WifiStateMachinePrime.TAG, "State changed from client mode. state = " + state);
                    if (state == 4) {
                        WifiStateMachinePrime.this.mModeStateMachine.sendMessage(WifiStateMachinePrime.CMD_CLIENT_MODE_FAILED, this);
                    } else if (state == 1) {
                        WifiStateMachinePrime.this.mModeStateMachine.sendMessage(WifiStateMachinePrime.CMD_CLIENT_MODE_STOPPED, this);
                    } else if (state == 3) {
                        Log.d(WifiStateMachinePrime.TAG, "client mode active");
                    }
                }
            }

            ClientModeActiveState() {
                super();
            }

            public void enter() {
                Log.d(WifiStateMachinePrime.TAG, "Entering ClientModeActiveState");
                this.mListener = new ClientListener();
                this.mManager = WifiStateMachinePrime.this.mWifiInjector.makeClientModeManager(this.mListener);
                this.mManager.start();
                WifiStateMachinePrime.this.mActiveModeManagers.add(this.mManager);
                WifiStateMachinePrime.this.updateBatteryStatsWifiState(true);
            }

            public void exit() {
                super.exit();
                this.mListener = null;
            }

            public boolean processMessage(Message message) {
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                int i = message.what;
                if (i != 0) {
                    switch (i) {
                        case WifiStateMachinePrime.CMD_CLIENT_MODE_STOPPED /*131375*/:
                            if (this.mListener == message.obj) {
                                Log.d(WifiStateMachinePrime.TAG, "ClientMode stopped, return to WifiDisabledState.");
                                WifiStateMachinePrime.this.mClientModeCallback.onStateChanged(1);
                                WifiStateMachinePrime.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mWifiDisabledState);
                                break;
                            } else {
                                Log.d(WifiStateMachinePrime.TAG, "Client mode state change from previous manager");
                                return true;
                            }
                        case WifiStateMachinePrime.CMD_CLIENT_MODE_FAILED /*131376*/:
                            if (this.mListener == message.obj) {
                                Log.d(WifiStateMachinePrime.TAG, "ClientMode failed, return to WifiDisabledState.");
                                WifiStateMachinePrime.this.mClientModeCallback.onStateChanged(4);
                                WifiStateMachinePrime.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mWifiDisabledState);
                                break;
                            } else {
                                Log.d(WifiStateMachinePrime.TAG, "Client mode state change from previous manager");
                                return true;
                            }
                        default:
                            return false;
                    }
                } else {
                    Log.d(WifiStateMachinePrime.TAG, "Received CMD_START_CLIENT_MODE when active - drop");
                }
                return false;
            }
        }

        class ModeActiveState extends State {
            ActiveModeManager mManager;

            ModeActiveState() {
            }

            public boolean processMessage(Message message) {
                return false;
            }

            public void exit() {
                if (this.mManager != null) {
                    this.mManager.stop();
                    WifiStateMachinePrime.this.mActiveModeManagers.remove(this.mManager);
                }
                WifiStateMachinePrime.this.updateBatteryStatsWifiState(false);
            }
        }

        class ScanOnlyModeActiveState extends ModeActiveState {
            ScanOnlyListener mListener;

            private class ScanOnlyListener implements ScanOnlyModeManager.Listener {
                private ScanOnlyListener() {
                }

                public void onStateChanged(int state) {
                    if (this != ScanOnlyModeActiveState.this.mListener) {
                        Log.d(WifiStateMachinePrime.TAG, "ScanOnly mode state change from previous manager");
                        return;
                    }
                    if (state == 4) {
                        Log.d(WifiStateMachinePrime.TAG, "ScanOnlyMode mode failed");
                        WifiStateMachinePrime.this.mModeStateMachine.sendMessage(WifiStateMachinePrime.CMD_SCAN_ONLY_MODE_FAILED, this);
                    } else if (state == 1) {
                        Log.d(WifiStateMachinePrime.TAG, "ScanOnlyMode stopped");
                        WifiStateMachinePrime.this.mModeStateMachine.sendMessage(WifiStateMachinePrime.CMD_SCAN_ONLY_MODE_STOPPED, this);
                    } else if (state == 3) {
                        Log.d(WifiStateMachinePrime.TAG, "scan mode active");
                    } else {
                        Log.d(WifiStateMachinePrime.TAG, "unexpected state update: " + state);
                    }
                }
            }

            ScanOnlyModeActiveState() {
                super();
            }

            public void enter() {
                Log.d(WifiStateMachinePrime.TAG, "Entering ScanOnlyModeActiveState");
                this.mListener = new ScanOnlyListener();
                this.mManager = WifiStateMachinePrime.this.mWifiInjector.makeScanOnlyModeManager(this.mListener);
                this.mManager.start();
                WifiStateMachinePrime.this.mActiveModeManagers.add(this.mManager);
                WifiStateMachinePrime.this.updateBatteryStatsWifiState(true);
                WifiStateMachinePrime.this.updateBatteryStatsScanModeActive();
            }

            public void exit() {
                super.exit();
                this.mListener = null;
            }

            public boolean processMessage(Message message) {
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                int i = message.what;
                if (i != 1) {
                    switch (i) {
                        case WifiStateMachinePrime.CMD_SCAN_ONLY_MODE_STOPPED /*131275*/:
                            if (this.mListener == message.obj) {
                                Log.d(WifiStateMachinePrime.TAG, "ScanOnlyMode stopped, return to WifiDisabledState.");
                                WifiStateMachinePrime.this.mScanOnlyCallback.onStateChanged(1);
                                WifiStateMachinePrime.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mWifiDisabledState);
                                break;
                            } else {
                                Log.d(WifiStateMachinePrime.TAG, "ScanOnly mode state change from previous manager");
                                return true;
                            }
                        case WifiStateMachinePrime.CMD_SCAN_ONLY_MODE_FAILED /*131276*/:
                            if (this.mListener == message.obj) {
                                Log.d(WifiStateMachinePrime.TAG, "ScanOnlyMode failed, return to WifiDisabledState.");
                                WifiStateMachinePrime.this.mScanOnlyCallback.onStateChanged(4);
                                WifiStateMachinePrime.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mWifiDisabledState);
                                break;
                            } else {
                                Log.d(WifiStateMachinePrime.TAG, "ScanOnly mode state change from previous manager");
                                return true;
                            }
                        default:
                            return false;
                    }
                } else {
                    Log.d(WifiStateMachinePrime.TAG, "Received CMD_START_SCAN_ONLY_MODE when active - drop");
                }
                return true;
            }
        }

        class WifiDisabledState extends ModeActiveState {
            WifiDisabledState() {
                super();
            }

            public void enter() {
                Log.d(WifiStateMachinePrime.TAG, "Entering WifiDisabledState");
                WifiStateMachinePrime.this.mDefaultModeManager.sendScanAvailableBroadcast(WifiStateMachinePrime.this.mContext, false);
                WifiStateMachinePrime.this.mScanRequestProxy.enableScanningForHiddenNetworks(false);
                WifiStateMachinePrime.this.mScanRequestProxy.clearScanResults();
                ScanResultRecords.getDefault().cleanup();
            }

            public boolean processMessage(Message message) {
                Log.d(WifiStateMachinePrime.TAG, "received a message in WifiDisabledState: " + message);
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                return false;
            }

            public void exit() {
            }
        }

        ModeStateMachine() {
            super(WifiStateMachinePrime.TAG, WifiStateMachinePrime.this.mLooper);
            addState(this.mClientModeActiveState);
            addState(this.mScanOnlyModeActiveState);
            addState(this.mWifiDisabledState);
            Log.d(WifiStateMachinePrime.TAG, "Starting Wifi in WifiDisabledState");
            setInitialState(this.mWifiDisabledState);
            start();
        }

        /* access modifiers changed from: private */
        public String getCurrentMode() {
            return getCurrentState().getName();
        }

        /* access modifiers changed from: private */
        public boolean checkForAndHandleModeChange(Message message) {
            int i = message.what;
            if (i != 3) {
                switch (i) {
                    case 0:
                        Log.d(WifiStateMachinePrime.TAG, "Switching from " + getCurrentMode() + " to ClientMode");
                        WifiStateMachinePrime.this.mModeStateMachine.transitionTo(this.mClientModeActiveState);
                        break;
                    case 1:
                        Log.d(WifiStateMachinePrime.TAG, "Switching from " + getCurrentMode() + " to ScanOnlyMode");
                        WifiStateMachinePrime.this.mModeStateMachine.transitionTo(this.mScanOnlyModeActiveState);
                        break;
                    default:
                        return false;
                }
            } else {
                Log.d(WifiStateMachinePrime.TAG, "Switching from " + getCurrentMode() + " to WifiDisabled");
                WifiStateMachinePrime.this.mModeStateMachine.transitionTo(this.mWifiDisabledState);
            }
            return true;
        }
    }

    private class SoftApCallbackImpl extends ModeCallback implements WifiManager.SoftApCallback {
        private SoftApCallbackImpl() {
            super();
        }

        public void onStateChanged(int state, int reason) {
            if (state == 11) {
                WifiStateMachinePrime.this.mActiveModeManagers.remove(getActiveModeManager());
                WifiStateMachinePrime.this.updateBatteryStatsWifiState(false);
                if (!(WifiStateMachinePrime.this.mWifiInjector == null || WifiStateMachinePrime.this.mWifiInjector.getWifiStateMachine() == null)) {
                    WifiStateMachinePrime.this.mWifiInjector.getWifiStateMachine().clearLastNetIdForAp();
                }
            } else if (state == 14) {
                WifiStateMachinePrime.this.mActiveModeManagers.remove(getActiveModeManager());
                WifiStateMachinePrime.this.updateBatteryStatsWifiState(false);
                if (!(WifiStateMachinePrime.this.mWifiInjector == null || WifiStateMachinePrime.this.mWifiInjector.getWifiStateMachine() == null)) {
                    WifiStateMachinePrime.this.mWifiInjector.getWifiStateMachine().clearLastNetIdForAp();
                }
            }
            if (WifiStateMachinePrime.this.mSoftApCallback != null) {
                WifiStateMachinePrime.this.mSoftApCallback.onStateChanged(state, reason);
            }
            if (WifiStateMachinePrime.this.mSoftApListener != null) {
                WifiStateMachinePrime.this.mSoftApListener.onStateChanged(state, reason);
            }
        }

        public void onNumClientsChanged(int numClients) {
            if (WifiStateMachinePrime.this.mSoftApCallback != null) {
                WifiStateMachinePrime.this.mSoftApCallback.onNumClientsChanged(numClients);
            } else {
                Log.d(WifiStateMachinePrime.TAG, "SoftApCallback is null. Dropping NumClientsChanged event.");
            }
            if (WifiStateMachinePrime.this.mSoftApListener != null) {
                WifiStateMachinePrime.this.mSoftApListener.onNumClientsChanged(numClients);
            } else {
                Log.d(WifiStateMachinePrime.TAG, "SoftApListener is null. Dropping NumClientsChanged event.");
            }
        }
    }

    private final class WifiNativeStatusListener implements WifiNative.StatusListener {
        private WifiNativeStatusListener() {
        }

        public void onStatusChanged(boolean isReady) {
            if (!isReady) {
                WifiStateMachinePrime.this.mHandler.post(new Runnable() {
                    public final void run() {
                        WifiStateMachinePrime.WifiNativeStatusListener.lambda$onStatusChanged$0(WifiStateMachinePrime.WifiNativeStatusListener.this);
                    }
                });
            }
        }

        public static /* synthetic */ void lambda$onStatusChanged$0(WifiNativeStatusListener wifiNativeStatusListener) {
            Log.e(WifiStateMachinePrime.TAG, "One of the native daemons died. Triggering recovery");
            WifiStateMachinePrime.this.mWifiDiagnostics.captureBugReportData(8);
            WifiStateMachinePrime.this.mWifiInjector.getSelfRecovery().trigger(1);
        }
    }

    public void registerSoftApListener(WifiManager.SoftApCallback listener) {
        this.mSoftApListener = listener;
    }

    public void registerSoftApCallback(WifiManager.SoftApCallback callback) {
        this.mSoftApCallback = callback;
    }

    public void registerScanOnlyCallback(ScanOnlyModeManager.Listener callback) {
        this.mScanOnlyCallback = callback;
    }

    public void registerClientModeCallback(ClientModeManager.Listener callback) {
        this.mClientModeCallback = callback;
    }

    WifiStateMachinePrime(WifiInjector wifiInjector, Context context, Looper looper, WifiNative wifiNative, DefaultModeManager defaultModeManager, IBatteryStats batteryStats) {
        this.mWifiInjector = wifiInjector;
        this.mContext = context;
        this.mLooper = looper;
        this.mHandler = new Handler(looper);
        this.mWifiNative = wifiNative;
        this.mDefaultModeManager = defaultModeManager;
        this.mBatteryStats = batteryStats;
        this.mSelfRecovery = this.mWifiInjector.getSelfRecovery();
        this.mWifiDiagnostics = this.mWifiInjector.getWifiDiagnostics();
        this.mScanRequestProxy = this.mWifiInjector.getScanRequestProxy();
        this.mModeStateMachine = new ModeStateMachine();
        this.mWifiNativeStatusListener = new WifiNativeStatusListener();
        this.mWifiNative.registerStatusListener(this.mWifiNativeStatusListener);
    }

    public void enterClientMode() {
        changeMode(0);
    }

    public void enterScanOnlyMode() {
        changeMode(1);
    }

    public void enterSoftAPMode(SoftApModeConfiguration wifiConfig) {
        this.mHandler.post(new Runnable(wifiConfig) {
            private final /* synthetic */ SoftApModeConfiguration f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WifiStateMachinePrime.this.startSoftAp(this.f$1);
            }
        });
    }

    public void stopSoftAPMode() {
        this.mHandler.post(new Runnable() {
            public final void run() {
                WifiStateMachinePrime.lambda$stopSoftAPMode$1(WifiStateMachinePrime.this);
            }
        });
    }

    public static /* synthetic */ void lambda$stopSoftAPMode$1(WifiStateMachinePrime wifiStateMachinePrime) {
        Iterator<ActiveModeManager> it = wifiStateMachinePrime.mActiveModeManagers.iterator();
        while (it.hasNext()) {
            ActiveModeManager manager = it.next();
            if (manager instanceof SoftApManager) {
                Log.d(TAG, "Stopping SoftApModeManager");
                manager.stop();
            }
        }
        wifiStateMachinePrime.updateBatteryStatsWifiState(false);
    }

    public void disableWifi() {
        changeMode(3);
    }

    public void shutdownWifi() {
        this.mHandler.post(new Runnable() {
            public final void run() {
                WifiStateMachinePrime.lambda$shutdownWifi$2(WifiStateMachinePrime.this);
            }
        });
    }

    public static /* synthetic */ void lambda$shutdownWifi$2(WifiStateMachinePrime wifiStateMachinePrime) {
        Iterator<ActiveModeManager> it = wifiStateMachinePrime.mActiveModeManagers.iterator();
        while (it.hasNext()) {
            it.next().stop();
        }
        wifiStateMachinePrime.updateBatteryStatsWifiState(false);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiStateMachinePrime");
        pw.println("Current wifi mode: " + getCurrentMode());
        pw.println("NumActiveModeManagers: " + this.mActiveModeManagers.size());
        Iterator<ActiveModeManager> it = this.mActiveModeManagers.iterator();
        while (it.hasNext()) {
            it.next().dump(fd, pw, args);
        }
    }

    /* access modifiers changed from: protected */
    public String getCurrentMode() {
        return this.mModeStateMachine.getCurrentMode();
    }

    private void changeMode(int newMode) {
        this.mModeStateMachine.sendMessage(newMode);
    }

    /* access modifiers changed from: private */
    public void startSoftAp(SoftApModeConfiguration softapConfig) {
        Log.d(TAG, "Starting SoftApModeManager");
        WifiConfiguration config = softapConfig.getWifiConfiguration();
        if (config != null && config.SSID != null) {
            Log.d(TAG, "Passing config to SoftApManager! " + config);
        }
        SoftApCallbackImpl callback = new SoftApCallbackImpl();
        ActiveModeManager manager = this.mWifiInjector.makeSoftApManager(callback, softapConfig);
        callback.setActiveModeManager(manager);
        manager.start();
        this.mActiveModeManagers.add(manager);
        updateBatteryStatsWifiState(true);
    }

    /* access modifiers changed from: private */
    public void updateBatteryStatsWifiState(boolean enabled) {
        if (enabled) {
            try {
                if (this.mActiveModeManagers.size() == 1) {
                    this.mBatteryStats.noteWifiOn();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to note battery stats in wifi");
            }
        } else if (this.mActiveModeManagers.size() == 0) {
            this.mBatteryStats.noteWifiOff();
        }
    }

    /* access modifiers changed from: private */
    public void updateBatteryStatsScanModeActive() {
        try {
            this.mBatteryStats.noteWifiState(1, null);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to note battery stats in wifi");
        }
    }
}
