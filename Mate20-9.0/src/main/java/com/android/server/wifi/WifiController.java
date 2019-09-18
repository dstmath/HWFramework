package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.WorkSource;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.State;
import com.android.server.wifi.ClientModeManager;
import com.android.server.wifi.ScanOnlyModeManager;

class WifiController extends AbsWifiController {
    private static final int BASE = 155648;
    static final int CMD_AIRPLANE_TOGGLED = 155657;
    static final int CMD_AP_START_FAILURE = 155661;
    static final int CMD_AP_STOPPED = 155663;
    static final int CMD_DEFERRED_RECOVERY_RESTART_WIFI = 155670;
    static final int CMD_DEFERRED_TOGGLE = 155659;
    static final int CMD_EMERGENCY_CALL_STATE_CHANGED = 155662;
    static final int CMD_EMERGENCY_MODE_CHANGED = 155649;
    static final int CMD_RECOVERY_DISABLE_WIFI = 155667;
    static final int CMD_RECOVERY_RESTART_WIFI = 155665;
    private static final int CMD_RECOVERY_RESTART_WIFI_CONTINUE = 155666;
    static final int CMD_SCANNING_STOPPED = 155669;
    static final int CMD_SCAN_ALWAYS_MODE_CHANGED = 155655;
    static final int CMD_SET_AP = 155658;
    static final int CMD_STA_START_FAILURE = 155664;
    static final int CMD_STA_STOPPED = 155668;
    static final int CMD_USER_PRESENT = 155660;
    static final int CMD_WIFI_TOGGLED = 155656;
    /* access modifiers changed from: private */
    public static final boolean DBG = HWFLOW;
    private static final long DEFAULT_REENABLE_DELAY_MS = 500;
    private static final long DEFER_MARGIN_MS = 5;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "WifiController";
    private ClientModeManager.Listener mClientModeCallback;
    /* access modifiers changed from: private */
    public Context mContext;
    private DefaultState mDefaultState;
    /* access modifiers changed from: private */
    public DeviceActiveState mDeviceActiveState;
    /* access modifiers changed from: private */
    public EcmState mEcmState;
    /* access modifiers changed from: private */
    public FrameworkFacade mFacade;
    /* access modifiers changed from: private */
    public boolean mFirstUserSignOnSeen = false;
    private HwWifiCHRService mHwWifiCHRService;
    NetworkInfo mNetworkInfo;
    /* access modifiers changed from: private */
    public long mReEnableDelayMillis;
    private ScanOnlyModeManager.Listener mScanOnlyModeCallback;
    /* access modifiers changed from: private */
    public final WifiSettingsStore mSettingsStore;
    private WifiManager.SoftApCallback mSoftApCallback;
    /* access modifiers changed from: private */
    public StaDisabledState mStaDisabledState;
    /* access modifiers changed from: private */
    public StaDisabledWithScanState mStaDisabledWithScanState;
    private StaEnabledState mStaEnabledState;
    private final WorkSource mTmpWorkSource;
    /* access modifiers changed from: private */
    public final WifiStateMachine mWifiStateMachine;
    /* access modifiers changed from: private */
    public final Looper mWifiStateMachineLooper;
    /* access modifiers changed from: private */
    public final WifiStateMachinePrime mWifiStateMachinePrime;

    private class ClientModeCallback implements ClientModeManager.Listener {
        private ClientModeCallback() {
        }

        public void onStateChanged(int state) {
            if (state == 4) {
                WifiController.this.logd("ClientMode unexpected failure: state unknown");
                WifiController.this.sendMessage(WifiController.CMD_STA_START_FAILURE);
            } else if (state == 1) {
                WifiController.this.logd("ClientMode stopped");
                WifiController.this.sendMessage(WifiController.CMD_STA_STOPPED);
            } else if (state == 3) {
                WifiController.this.logd("client mode active");
            } else {
                WifiController wifiController = WifiController.this;
                wifiController.logd("unexpected state update: " + state);
            }
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController wifiController = WifiController.this;
                wifiController.log(getName() + " enter.\n");
            }
            WifiController.this.createQoEEngineService(WifiController.this.mContext, WifiController.this.mWifiStateMachine);
            WifiController.this.createWifiProStateMachine(WifiController.this.mContext, WifiController.this.mWifiStateMachine.getMessenger());
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController wifiController = WifiController.this;
                wifiController.logd(getName() + " what=" + msg.what);
            }
            if (WifiController.this.processDefaultState(msg)) {
                return true;
            }
            int i = msg.what;
            if (i != WifiController.CMD_EMERGENCY_MODE_CHANGED) {
                switch (i) {
                    case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    case WifiController.CMD_AP_START_FAILURE /*155661*/:
                    case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    case WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE /*155666*/:
                    case WifiController.CMD_STA_STOPPED /*155668*/:
                    case WifiController.CMD_SCANNING_STOPPED /*155669*/:
                    case WifiController.CMD_DEFERRED_RECOVERY_RESTART_WIFI /*155670*/:
                        break;
                    case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                        if (!WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                            WifiController.this.log("Airplane mode disabled, determine next state");
                            if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                                if (WifiController.this.checkScanOnlyModeAvailable()) {
                                    WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                                    break;
                                }
                            } else {
                                WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                                break;
                            }
                        } else {
                            WifiController.this.log("Airplane mode toggled, shutdown all modes");
                            WifiController.this.mWifiStateMachinePrime.shutdownWifi();
                            WifiController.this.transitionTo(WifiController.this.mStaDisabledState);
                            break;
                        }
                        break;
                    case WifiController.CMD_SET_AP /*155658*/:
                        if (!WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                            if (msg.arg1 != 1) {
                                WifiController.this.mWifiStateMachinePrime.stopSoftAPMode();
                                break;
                            } else {
                                Object obj = msg.obj;
                                WifiController.this.mWifiStateMachinePrime.enterSoftAPMode((SoftApModeConfiguration) msg.obj);
                                break;
                            }
                        } else {
                            WifiController.this.log("drop softap requests when in airplane mode");
                            break;
                        }
                    case WifiController.CMD_DEFERRED_TOGGLE /*155659*/:
                        WifiController.this.log("DEFERRED_TOGGLE ignored due to state change");
                        break;
                    case WifiController.CMD_USER_PRESENT /*155660*/:
                        boolean unused = WifiController.this.mFirstUserSignOnSeen = true;
                        break;
                    case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*155662*/:
                        break;
                    case WifiController.CMD_AP_STOPPED /*155663*/:
                        WifiController.this.log("SoftAp mode disabled, determine next state");
                        if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                            if (WifiController.this.checkScanOnlyModeAvailable()) {
                                WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                                break;
                            }
                        } else {
                            WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                            break;
                        }
                        break;
                    case WifiController.CMD_RECOVERY_RESTART_WIFI /*155665*/:
                        WifiController.this.deferMessage(WifiController.this.obtainMessage(WifiController.CMD_DEFERRED_RECOVERY_RESTART_WIFI));
                        WifiController.this.mWifiStateMachinePrime.shutdownWifi();
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledState);
                        break;
                    case WifiController.CMD_RECOVERY_DISABLE_WIFI /*155667*/:
                        WifiController.this.log("Recovery has been throttled, disable wifi");
                        WifiController.this.mWifiStateMachinePrime.shutdownWifi();
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledState);
                        break;
                    default:
                        throw new RuntimeException("WifiController.handleMessage " + msg.what);
                }
            }
            boolean configWiFiDisableInECBM = WifiController.this.mFacade.getConfigWiFiDisableInECBM(WifiController.this.mContext);
            WifiController wifiController2 = WifiController.this;
            wifiController2.log("WifiController msg " + msg + " getConfigWiFiDisableInECBM " + configWiFiDisableInECBM);
            if (msg.arg1 == 1 && configWiFiDisableInECBM) {
                WifiController.this.transitionTo(WifiController.this.mEcmState);
            }
            return true;
        }
    }

    class DeviceActiveState extends State {
        DeviceActiveState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController wifiController = WifiController.this;
                wifiController.log(getName() + " enter.\n");
            }
            WifiController.this.mWifiStateMachinePrime.enterClientMode();
            WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(false);
        }

        public boolean processMessage(Message msg) {
            String bugTitle;
            String bugDetail;
            if (WifiController.DBG) {
                WifiController.this.log(getName() + msg.toString() + "\n");
            }
            if (msg.what == WifiController.CMD_USER_PRESENT) {
                if (!WifiController.this.mFirstUserSignOnSeen) {
                    WifiController.this.mWifiStateMachine.reloadTlsNetworksAndReconnect();
                }
                boolean unused = WifiController.this.mFirstUserSignOnSeen = true;
                return true;
            } else if (msg.what != WifiController.CMD_RECOVERY_RESTART_WIFI) {
                return false;
            } else {
                if (msg.arg1 >= SelfRecovery.REASON_STRINGS.length || msg.arg1 < 0) {
                    bugDetail = "";
                    bugTitle = "Wi-Fi BugReport";
                } else {
                    bugDetail = SelfRecovery.REASON_STRINGS[msg.arg1];
                    bugTitle = "Wi-Fi BugReport: " + bugDetail;
                }
                if (msg.arg1 != 0) {
                    new Handler(WifiController.this.mWifiStateMachineLooper).post(new Runnable(bugTitle, bugDetail) {
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            WifiController.this.mWifiStateMachine.takeBugReport(this.f$1, this.f$2);
                        }
                    });
                }
                return false;
            }
        }
    }

    class EcmState extends State {
        private int mEcmEntryCount;

        EcmState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController wifiController = WifiController.this;
                wifiController.log(getName() + "\n");
            }
            WifiController.this.mWifiStateMachinePrime.shutdownWifi();
            WifiController.this.mWifiStateMachine.clearANQPCache();
            this.mEcmEntryCount = 1;
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + msg.toString() + "\n");
            }
            if (msg.what == WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED) {
                if (msg.arg1 == 1) {
                    this.mEcmEntryCount++;
                } else if (msg.arg1 == 0) {
                    decrementCountAndReturnToAppropriateState();
                }
                return true;
            } else if (msg.what == WifiController.CMD_EMERGENCY_MODE_CHANGED) {
                if (msg.arg1 == 1) {
                    this.mEcmEntryCount++;
                } else if (msg.arg1 == 0) {
                    decrementCountAndReturnToAppropriateState();
                }
                return true;
            } else if (msg.what == WifiController.CMD_RECOVERY_RESTART_WIFI || msg.what == WifiController.CMD_RECOVERY_DISABLE_WIFI || msg.what == WifiController.CMD_AP_STOPPED || msg.what == WifiController.CMD_SCANNING_STOPPED || msg.what == WifiController.CMD_STA_STOPPED || msg.what == WifiController.CMD_SET_AP) {
                return true;
            } else {
                return false;
            }
        }

        private void decrementCountAndReturnToAppropriateState() {
            boolean exitEcm = false;
            if (this.mEcmEntryCount == 0) {
                WifiController.this.loge("mEcmEntryCount is 0; exiting Ecm");
                exitEcm = true;
            } else {
                int i = this.mEcmEntryCount - 1;
                this.mEcmEntryCount = i;
                if (i == 0) {
                    exitEcm = true;
                }
            }
            if (!exitEcm) {
                return;
            }
            if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
            } else if (WifiController.this.checkScanOnlyModeAvailable()) {
                WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
            } else {
                WifiController.this.transitionTo(WifiController.this.mStaDisabledState);
            }
        }
    }

    private class ScanOnlyCallback implements ScanOnlyModeManager.Listener {
        private ScanOnlyCallback() {
        }

        public void onStateChanged(int state) {
            if (state == 4) {
                Log.d(WifiController.TAG, "ScanOnlyMode unexpected failure: state unknown");
            } else if (state == 1) {
                Log.d(WifiController.TAG, "ScanOnlyMode stopped");
                WifiController.this.sendMessage(WifiController.CMD_SCANNING_STOPPED);
            } else if (state == 3) {
                Log.d(WifiController.TAG, "scan mode active");
            } else {
                Log.d(WifiController.TAG, "unexpected state update: " + state);
            }
        }
    }

    private class SoftApCallback implements WifiManager.SoftApCallback {
        private SoftApCallback() {
        }

        public void onStateChanged(int state, int reason) {
            if (state == 14) {
                Log.e(WifiController.TAG, "SoftAP start failed");
                WifiController.this.sendMessage(WifiController.CMD_AP_START_FAILURE);
            } else if (state == 11) {
                WifiController.this.sendMessage(WifiController.CMD_AP_STOPPED);
            }
        }

        public void onNumClientsChanged(int numClients) {
        }
    }

    class StaDisabledState extends State {
        private static final int RECOVERY_DELAY_MILLIS = 2000;
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable = false;

        StaDisabledState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + " enter.\n");
            }
            WifiController.this.mWifiStateMachinePrime.disableWifi();
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = false;
            WifiController.this.mWifiStateMachine.clearANQPCache();
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.logd(getName() + " what=" + msg.what);
            }
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    if (WifiController.this.checkScanOnlyModeAvailable()) {
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (WifiController.this.checkScanOnlyModeAvailable() && WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                            WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                            break;
                        }
                    } else if (!doDeferEnable(msg)) {
                        WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                        break;
                    } else {
                        if (this.mHaveDeferredEnable) {
                            this.mDeferredEnableSerialNumber++;
                        }
                        this.mHaveDeferredEnable = !this.mHaveDeferredEnable;
                        break;
                    }
                case WifiController.CMD_SET_AP /*155658*/:
                    if (WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                        WifiController.this.log("drop softap requests when in airplane mode");
                        break;
                    } else {
                        if (msg.arg1 == 1) {
                            WifiController.this.mSettingsStore.setWifiSavedState(0);
                        }
                        return false;
                    }
                case WifiController.CMD_DEFERRED_TOGGLE /*155659*/:
                    if (msg.arg1 == this.mDeferredEnableSerialNumber) {
                        WifiController.this.log("DEFERRED_TOGGLE handled");
                        WifiController.this.sendMessage((Message) msg.obj);
                        break;
                    } else {
                        WifiController.this.log("DEFERRED_TOGGLE ignored due to serial mismatch");
                        break;
                    }
                case WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE /*155666*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (WifiController.this.checkScanOnlyModeAvailable()) {
                            WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                            break;
                        }
                    } else {
                        WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                        break;
                    }
                    break;
                case WifiController.CMD_DEFERRED_RECOVERY_RESTART_WIFI /*155670*/:
                    WifiController.this.log("CMD_DEFERRED_RECOVERY_RESTART_WIFI");
                    WifiController.this.sendMessageDelayed(WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE, 2000);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private boolean doDeferEnable(Message msg) {
            long delaySoFar = SystemClock.elapsedRealtime() - this.mDisabledTimestamp;
            if (delaySoFar >= WifiController.this.mReEnableDelayMillis) {
                return false;
            }
            WifiController wifiController = WifiController.this;
            wifiController.log("WifiController msg " + msg + " deferred for " + (WifiController.this.mReEnableDelayMillis - delaySoFar) + "ms");
            Message deferredMsg = WifiController.this.obtainMessage(WifiController.CMD_DEFERRED_TOGGLE);
            deferredMsg.obj = Message.obtain(msg);
            int i = this.mDeferredEnableSerialNumber + 1;
            this.mDeferredEnableSerialNumber = i;
            deferredMsg.arg1 = i;
            WifiController.this.sendMessageDelayed(deferredMsg, (WifiController.this.mReEnableDelayMillis - delaySoFar) + WifiController.DEFER_MARGIN_MS);
            return true;
        }
    }

    class StaDisabledWithScanState extends State {
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable = false;

        StaDisabledWithScanState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + " enter.\n");
            }
            WifiController.this.mWifiStateMachinePrime.enterScanOnlyMode();
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = false;
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.logd(getName() + " what=" + msg.what);
            }
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    if (!WifiController.this.checkScanOnlyModeAvailable()) {
                        WifiController.this.log("StaDisabledWithScanState: scan no longer available");
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!doDeferEnable(msg)) {
                            WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                            break;
                        } else {
                            if (this.mHaveDeferredEnable) {
                                this.mDeferredEnableSerialNumber++;
                            }
                            this.mHaveDeferredEnable = !this.mHaveDeferredEnable;
                            break;
                        }
                    }
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(0);
                    }
                    return false;
                case WifiController.CMD_DEFERRED_TOGGLE /*155659*/:
                    if (msg.arg1 == this.mDeferredEnableSerialNumber) {
                        WifiController.this.logd("DEFERRED_TOGGLE handled");
                        WifiController.this.sendMessage((Message) msg.obj);
                        break;
                    } else {
                        WifiController.this.log("DEFERRED_TOGGLE ignored due to serial mismatch");
                        break;
                    }
                case WifiController.CMD_AP_START_FAILURE /*155661*/:
                    break;
                case WifiController.CMD_AP_STOPPED /*155663*/:
                    if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                        break;
                    }
                    break;
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    WifiController.this.logd("CMD_STA_START_FAILURE  transition to mStaDisabledState.");
                    WifiController.this.transitionTo(WifiController.this.mStaDisabledState);
                    break;
                case WifiController.CMD_SCANNING_STOPPED /*155669*/:
                    WifiController.this.log("WifiController: SCANNING_STOPPED when in scan mode -> StaDisabled");
                    WifiController.this.transitionTo(WifiController.this.mStaDisabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private boolean doDeferEnable(Message msg) {
            long delaySoFar = SystemClock.elapsedRealtime() - this.mDisabledTimestamp;
            if (delaySoFar >= WifiController.this.mReEnableDelayMillis) {
                return false;
            }
            WifiController wifiController = WifiController.this;
            wifiController.log("WifiController msg " + msg + " deferred for " + (WifiController.this.mReEnableDelayMillis - delaySoFar) + "ms");
            Message deferredMsg = WifiController.this.obtainMessage(WifiController.CMD_DEFERRED_TOGGLE);
            deferredMsg.obj = Message.obtain(msg);
            int i = this.mDeferredEnableSerialNumber + 1;
            this.mDeferredEnableSerialNumber = i;
            deferredMsg.arg1 = i;
            WifiController.this.sendMessageDelayed(deferredMsg, (WifiController.this.mReEnableDelayMillis - delaySoFar) + WifiController.DEFER_MARGIN_MS);
            return true;
        }
    }

    class StaEnabledState extends State {
        StaEnabledState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController wifiController = WifiController.this;
                wifiController.log(getName() + " enter.\n");
            }
            WifiController.this.log("StaEnabledState.enter()");
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController wifiController = WifiController.this;
                wifiController.logd(getName() + " what=" + msg.what);
            }
            if (WifiController.this.processStaEnabled(msg)) {
                return true;
            }
            switch (msg.what) {
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!WifiController.this.checkScanOnlyModeAvailable()) {
                            WifiController.this.transitionTo(WifiController.this.mStaDisabledState);
                            break;
                        } else {
                            WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                            break;
                        }
                    }
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                        return false;
                    }
                    WifiController.this.log("airplane mode toggled - and airplane mode is off.  return handled");
                    return true;
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(1);
                        if (WifiController.this.mWifiStateMachine != null) {
                            WifiController.this.mWifiStateMachine.saveLastNetIdForAp();
                        }
                    }
                    return false;
                case WifiController.CMD_AP_START_FAILURE /*155661*/:
                case WifiController.CMD_AP_STOPPED /*155663*/:
                    break;
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    if (WifiController.this.checkScanOnlyModeAvailable()) {
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                        break;
                    } else {
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledState);
                        break;
                    }
                case WifiController.CMD_STA_STOPPED /*155668*/:
                    WifiController.this.transitionTo(WifiController.this.mStaDisabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    WifiController(Context context, WifiStateMachine wsm, Looper wifiStateMachineLooper, WifiSettingsStore wss, Looper wifiServiceLooper, FrameworkFacade f, WifiStateMachinePrime wsmp) {
        super(TAG, wifiServiceLooper);
        boolean isLocationModeActive = true;
        this.mNetworkInfo = new NetworkInfo(1, 0, "WIFI", "");
        this.mTmpWorkSource = new WorkSource();
        this.mDefaultState = new DefaultState();
        this.mStaEnabledState = new StaEnabledState();
        this.mStaDisabledState = new StaDisabledState();
        this.mStaDisabledWithScanState = new StaDisabledWithScanState();
        this.mDeviceActiveState = new DeviceActiveState();
        this.mEcmState = new EcmState();
        this.mScanOnlyModeCallback = new ScanOnlyCallback();
        this.mClientModeCallback = new ClientModeCallback();
        this.mSoftApCallback = new SoftApCallback();
        this.mFacade = f;
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mWifiStateMachineLooper = wifiStateMachineLooper;
        this.mWifiStateMachinePrime = wsmp;
        this.mSettingsStore = wss;
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        addState(this.mDefaultState);
        addState(this.mStaDisabledState, this.mDefaultState);
        addState(this.mStaEnabledState, this.mDefaultState);
        addState(this.mDeviceActiveState, this.mStaEnabledState);
        addState(this.mStaDisabledWithScanState, this.mDefaultState);
        addState(this.mEcmState, this.mDefaultState);
        boolean isAirplaneModeOn = this.mSettingsStore.isAirplaneModeOn();
        boolean isWifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        boolean isScanningAlwaysAvailable = this.mSettingsStore.isScanAlwaysAvailable();
        isLocationModeActive = this.mSettingsStore.getLocationModeSetting(this.mContext) != 0 ? false : isLocationModeActive;
        log("isAirplaneModeOn = " + isAirplaneModeOn + ", isWifiEnabled = " + isWifiEnabled + ", isScanningAvailable = " + isScanningAlwaysAvailable + ", isLocationModeActive = " + isLocationModeActive);
        if (checkScanOnlyModeAvailable()) {
            setInitialState(this.mStaDisabledWithScanState);
        } else {
            setInitialState(this.mStaDisabledState);
        }
        setLogRecSize(100);
        setLogOnlyTransitions(false);
        this.mWifiStateMachinePrime.registerScanOnlyCallback(this.mScanOnlyModeCallback);
        this.mWifiStateMachinePrime.registerClientModeCallback(this.mClientModeCallback);
        this.mWifiStateMachinePrime.registerSoftApListener(this.mSoftApCallback);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.location.MODE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiController.DBG) {
                    Slog.d(WifiController.TAG, "Received action: " + action);
                }
                if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null) {
                        WifiController.this.mNetworkInfo = networkInfo;
                    }
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    if (intent.getIntExtra("wifi_state", 4) == 4) {
                        int realState = WifiController.this.mWifiStateMachine.syncGetWifiState();
                        WifiController wifiController = WifiController.this;
                        wifiController.loge("WifiControllerWifi turn on failed, realState =" + realState);
                        if (realState == 4) {
                            WifiController.this.sendMessage(WifiController.CMD_STA_START_FAILURE);
                        }
                    }
                } else if (action.equals("android.location.MODE_CHANGED")) {
                    WifiController.this.sendMessage(WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED);
                }
            }
        }, new IntentFilter(filter));
        initializeAndRegisterForSettingsChange(wifiServiceLooper);
    }

    private void initializeAndRegisterForSettingsChange(Looper looper) {
        new Handler(looper);
        readWifiReEnableDelay();
    }

    /* access modifiers changed from: private */
    public boolean checkScanOnlyModeAvailable() {
        if (this.mSettingsStore.getLocationModeSetting(this.mContext) == 0) {
            return false;
        }
        return this.mSettingsStore.isScanAlwaysAvailable();
    }

    private void readWifiReEnableDelay() {
        this.mReEnableDelayMillis = this.mFacade.getLongSetting(this.mContext, "wifi_reenable_delay", DEFAULT_REENABLE_DELAY_MS);
    }

    private void updateBatteryWorkSource() {
        this.mTmpWorkSource.clear();
        this.mWifiStateMachine.updateBatteryWorkSource(this.mTmpWorkSource);
    }

    private State getNextWifiState() {
        if (this.mSettingsStore.getWifiSavedState() == 1) {
            return this.mDeviceActiveState;
        }
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_on", 0) == 1) {
            Log.e(TAG, "getWifiSavedState and Settings.Global.WIFI_ON are different!");
            this.mSettingsStore.setWifiSavedState(1);
            return this.mDeviceActiveState;
        } else if (checkScanOnlyModeAvailable()) {
            return this.mStaDisabledWithScanState;
        } else {
            return this.mStaDisabledState;
        }
    }
}
