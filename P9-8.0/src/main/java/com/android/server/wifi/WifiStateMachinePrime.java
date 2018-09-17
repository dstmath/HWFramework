package com.android.server.wifi;

import android.net.wifi.IApInterface;
import android.net.wifi.IWificond;
import android.net.wifi.WifiConfiguration;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.SoftApManager.Listener;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WifiStateMachinePrime {
    static final int BASE = 131072;
    static final int CMD_AP_STOPPED = 131096;
    static final int CMD_START_AP = 131093;
    static final int CMD_START_AP_FAILURE = 131094;
    static final int CMD_STOP_AP = 131095;
    private static final String TAG = "WifiStateMachinePrime";
    private Queue<WifiConfiguration> mApConfigQueue = new ConcurrentLinkedQueue();
    private final Looper mLooper;
    private ModeStateMachine mModeStateMachine;
    private final INetworkManagementService mNMService;
    private final WifiInjector mWifiInjector;
    private IWificond mWificond;

    private class ModeStateMachine extends StateMachine {
        public static final int CMD_DISABLE_WIFI = 3;
        public static final int CMD_START_CLIENT_MODE = 0;
        public static final int CMD_START_SCAN_ONLY_MODE = 1;
        public static final int CMD_START_SOFT_AP_MODE = 2;
        private final State mClientModeActiveState = new ClientModeActiveState();
        private final State mClientModeState = new ClientModeState();
        private final State mScanOnlyModeActiveState = new ScanOnlyModeActiveState();
        private final State mScanOnlyModeState = new ScanOnlyModeState();
        private final State mSoftAPModeActiveState = new SoftAPModeActiveState();
        private final State mSoftAPModeState = new SoftAPModeState();
        private final State mWifiDisabledState = new WifiDisabledState();

        class ModeActiveState extends State {
            ActiveModeManager mActiveModeManager;

            ModeActiveState() {
            }

            public boolean processMessage(Message message) {
                return false;
            }

            public void exit() {
                this.mActiveModeManager.stop();
            }
        }

        class ClientModeActiveState extends ModeActiveState {
            ClientModeActiveState() {
                super();
            }

            public void enter() {
                this.mActiveModeManager = new ClientModeManager();
            }
        }

        class ClientModeState extends State {
            ClientModeState() {
            }

            public void enter() {
                WifiStateMachinePrime.this.mWificond = WifiStateMachinePrime.this.mWifiInjector.makeWificond();
            }

            public boolean processMessage(Message message) {
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                return false;
            }

            public void exit() {
                ModeStateMachine.this.tearDownInterfaces();
            }
        }

        class ScanOnlyModeActiveState extends ModeActiveState {
            ScanOnlyModeActiveState() {
                super();
            }

            public void enter() {
                this.mActiveModeManager = new ScanOnlyModeManager();
            }
        }

        class ScanOnlyModeState extends State {
            ScanOnlyModeState() {
            }

            public void enter() {
            }

            public boolean processMessage(Message message) {
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                return false;
            }

            public void exit() {
            }
        }

        class SoftAPModeActiveState extends ModeActiveState {

            private class SoftApListener implements Listener {
                /* synthetic */ SoftApListener(SoftAPModeActiveState this$2, SoftApListener -this1) {
                    this();
                }

                private SoftApListener() {
                }

                public void onStateChanged(int state, int reason) {
                    if (state == 11) {
                        WifiStateMachinePrime.this.mModeStateMachine.sendMessage(WifiStateMachinePrime.CMD_AP_STOPPED);
                    } else if (state == 14) {
                        WifiStateMachinePrime.this.mModeStateMachine.sendMessage(WifiStateMachinePrime.CMD_START_AP_FAILURE);
                    }
                }
            }

            SoftAPModeActiveState() {
                super();
            }

            public void enter() {
                Log.d(WifiStateMachinePrime.TAG, "Entering SoftApModeActiveState");
                WifiConfiguration config = (WifiConfiguration) WifiStateMachinePrime.this.mApConfigQueue.poll();
                if (config == null || config.SSID == null) {
                    config = null;
                } else {
                    Log.d(WifiStateMachinePrime.TAG, "Passing config to SoftApManager! " + config);
                }
                this.mActiveModeManager = WifiStateMachinePrime.this.mWifiInjector.makeSoftApManager(WifiStateMachinePrime.this.mNMService, new SoftApListener(this, null), ((SoftAPModeState) ModeStateMachine.this.mSoftAPModeState).getInterface(), config);
                this.mActiveModeManager.start();
            }

            public boolean processMessage(Message message) {
                switch (message.what) {
                    case WifiStateMachinePrime.CMD_START_AP /*131093*/:
                        Log.d(WifiStateMachinePrime.TAG, "Received CMD_START_AP when active - invalid message - drop");
                        break;
                    case WifiStateMachinePrime.CMD_START_AP_FAILURE /*131094*/:
                        Log.d(WifiStateMachinePrime.TAG, "Failed to start SoftApMode.  Return to SoftApMode (inactive).");
                        WifiStateMachinePrime.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mSoftAPModeState);
                        break;
                    case WifiStateMachinePrime.CMD_STOP_AP /*131095*/:
                        this.mActiveModeManager.stop();
                        break;
                    case WifiStateMachinePrime.CMD_AP_STOPPED /*131096*/:
                        Log.d(WifiStateMachinePrime.TAG, "SoftApModeActiveState stopped.  Return to SoftApMode (inactive).");
                        WifiStateMachinePrime.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mSoftAPModeState);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        class SoftAPModeState extends State {
            IApInterface mApInterface = null;

            SoftAPModeState() {
            }

            public void enter() {
                if (WifiStateMachinePrime.this.mModeStateMachine.getCurrentMessage().what != 2) {
                    Log.d(WifiStateMachinePrime.TAG, "Entering SoftAPMode (idle)");
                    return;
                }
                this.mApInterface = null;
                WifiStateMachinePrime.this.mWificond = WifiStateMachinePrime.this.mWifiInjector.makeWificond();
                if (WifiStateMachinePrime.this.mWificond == null) {
                    Log.e(WifiStateMachinePrime.TAG, "Failed to get reference to wificond");
                    writeApConfigDueToStartFailure();
                    WifiStateMachinePrime.this.mModeStateMachine.sendMessage(WifiStateMachinePrime.CMD_START_AP_FAILURE);
                    return;
                }
                try {
                    this.mApInterface = WifiStateMachinePrime.this.mWificond.createApInterface();
                } catch (RemoteException e1) {
                    Log.e(WifiStateMachinePrime.TAG, "RemoteException in createApInterface: " + e1.getMessage());
                }
                if (this.mApInterface == null) {
                    Log.e(WifiStateMachinePrime.TAG, "Could not get IApInterface instance from wificond");
                    writeApConfigDueToStartFailure();
                    WifiStateMachinePrime.this.mModeStateMachine.sendMessage(WifiStateMachinePrime.CMD_START_AP_FAILURE);
                    return;
                }
                WifiStateMachinePrime.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mSoftAPModeActiveState);
            }

            public boolean processMessage(Message message) {
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                switch (message.what) {
                    case WifiStateMachinePrime.CMD_START_AP /*131093*/:
                        Log.d(WifiStateMachinePrime.TAG, "Received CMD_START_AP (now invalid message) - dropping");
                        break;
                    case WifiStateMachinePrime.CMD_START_AP_FAILURE /*131094*/:
                        Log.e(WifiStateMachinePrime.TAG, "Failed to start SoftApMode.  Wait for next mode command.");
                        break;
                    case WifiStateMachinePrime.CMD_STOP_AP /*131095*/:
                        break;
                    case WifiStateMachinePrime.CMD_AP_STOPPED /*131096*/:
                        Log.d(WifiStateMachinePrime.TAG, "SoftApModeActiveState stopped.  Wait for next mode command.");
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public void exit() {
                ModeStateMachine.this.tearDownInterfaces();
            }

            protected IApInterface getInterface() {
                return this.mApInterface;
            }

            private void writeApConfigDueToStartFailure() {
                WifiConfiguration config = (WifiConfiguration) WifiStateMachinePrime.this.mApConfigQueue.poll();
                if (config != null && config.SSID != null) {
                    WifiStateMachinePrime.this.mWifiInjector.getWifiApConfigStore().setApConfiguration(config);
                }
            }
        }

        class WifiDisabledState extends State {
            WifiDisabledState() {
            }

            public void enter() {
                Log.d(WifiStateMachinePrime.TAG, "Entering WifiDisabledState");
            }

            public boolean processMessage(Message message) {
                Log.d(WifiStateMachinePrime.TAG, "received a message in WifiDisabledState: " + message);
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                return false;
            }
        }

        ModeStateMachine() {
            super(WifiStateMachinePrime.TAG, WifiStateMachinePrime.this.mLooper);
            addState(this.mClientModeState);
            addState(this.mClientModeActiveState, this.mClientModeState);
            addState(this.mScanOnlyModeState);
            addState(this.mScanOnlyModeActiveState, this.mScanOnlyModeState);
            addState(this.mSoftAPModeState);
            addState(this.mSoftAPModeActiveState, this.mSoftAPModeState);
            addState(this.mWifiDisabledState);
            Log.d(WifiStateMachinePrime.TAG, "Starting Wifi in WifiDisabledState");
            setInitialState(this.mWifiDisabledState);
            start();
        }

        private String getCurrentMode() {
            return getCurrentState().getName();
        }

        private boolean checkForAndHandleModeChange(Message message) {
            switch (message.what) {
                case 0:
                    Log.d(WifiStateMachinePrime.TAG, "Switching from " + getCurrentMode() + " to ClientMode");
                    WifiStateMachinePrime.this.mModeStateMachine.transitionTo(this.mClientModeState);
                    break;
                case 1:
                    Log.d(WifiStateMachinePrime.TAG, "Switching from " + getCurrentMode() + " to ScanOnlyMode");
                    WifiStateMachinePrime.this.mModeStateMachine.transitionTo(this.mScanOnlyModeState);
                    break;
                case 2:
                    Log.d(WifiStateMachinePrime.TAG, "Switching from " + getCurrentMode() + " to SoftApMode");
                    WifiStateMachinePrime.this.mModeStateMachine.transitionTo(this.mSoftAPModeState);
                    break;
                case 3:
                    Log.d(WifiStateMachinePrime.TAG, "Switching from " + getCurrentMode() + " to WifiDisabled");
                    WifiStateMachinePrime.this.mModeStateMachine.transitionTo(this.mWifiDisabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void tearDownInterfaces() {
            if (WifiStateMachinePrime.this.mWificond != null) {
                try {
                    WifiStateMachinePrime.this.mWificond.tearDownInterfaces();
                } catch (RemoteException e) {
                    Log.e(WifiStateMachinePrime.TAG, "Failed to tear down interfaces via wificond");
                }
                WifiStateMachinePrime.this.mWificond = null;
            }
        }
    }

    WifiStateMachinePrime(WifiInjector wifiInjector, Looper looper, INetworkManagementService nmService) {
        this.mWifiInjector = wifiInjector;
        this.mLooper = looper;
        this.mNMService = nmService;
        try {
            this.mWificond = this.mWifiInjector.makeWificond();
            if (this.mWificond != null) {
                this.mWificond.tearDownInterfaces();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "wificond died during framework startup");
        }
    }

    public void enterClientMode() {
        changeMode(0);
    }

    public void enterScanOnlyMode() {
        changeMode(1);
    }

    public void enterSoftAPMode(WifiConfiguration wifiConfig) {
        if (wifiConfig == null) {
            wifiConfig = new WifiConfiguration();
        }
        this.mApConfigQueue.offer(wifiConfig);
        changeMode(2);
    }

    public void disableWifi() {
        changeMode(3);
    }

    protected String getCurrentMode() {
        if (this.mModeStateMachine != null) {
            return this.mModeStateMachine.getCurrentMode();
        }
        return "WifiDisabledState";
    }

    private void changeMode(int newMode) {
        if (this.mModeStateMachine == null) {
            if (newMode == 3) {
                Log.e(TAG, "Received call to disable wifi when it is already disabled.");
                return;
            }
            this.mModeStateMachine = new ModeStateMachine();
        }
        this.mModeStateMachine.sendMessage(newMode);
    }
}
