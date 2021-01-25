package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.State;
import com.android.server.wifi.ClientModeManager;
import com.android.server.wifi.ScanOnlyModeManager;
import com.android.server.wifi.WifiController;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.util.WifiPermissionsUtil;

/* access modifiers changed from: package-private */
public class WifiController extends AbsWifiController {
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
    static final int CMD_SET_WIFI_SAVED_STATE_DISABLE = 155671;
    static final int CMD_STA_START_FAILURE = 155664;
    static final int CMD_STA_STOPPED = 155668;
    static final int CMD_USER_PRESENT = 155660;
    static final int CMD_WIFI_TOGGLED = 155656;
    private static final boolean DBG = HWFLOW;
    private static final long DEFAULT_REENABLE_DELAY_MS = 500;
    private static final long DEFER_MARGIN_MS = 5;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MAX_RECOVERY_TIMEOUT_DELAY_MS = 4000;
    private static final String OPEN_HOTSPOT = "OpenHotspot";
    private static final String TAG = "WifiController";
    private final ActiveModeWarden mActiveModeWarden;
    private ClientModeManager.Listener mClientModeCallback = new ClientModeCallback();
    private final ClientModeImpl mClientModeImpl;
    private final Looper mClientModeImplLooper;
    private Context mContext;
    private DefaultState mDefaultState = new DefaultState();
    private EcmState mEcmState = new EcmState();
    private final FrameworkFacade mFacade;
    private boolean mFirstUserSignOnSeen = false;
    private HwWifiCHRService mHwWifiCHRService;
    private long mReEnableDelayMillis;
    private int mRecoveryDelayMillis;
    private ScanOnlyModeManager.Listener mScanOnlyModeCallback = new ScanOnlyCallback();
    private final WifiSettingsStore mSettingsStore;
    private WifiManager.SoftApCallback mSoftApCallback = new SoftApCallback();
    private StaDisabledState mStaDisabledState = new StaDisabledState();
    private StaDisabledWithScanState mStaDisabledWithScanState = new StaDisabledWithScanState();
    private StaEnabledState mStaEnabledState = new StaEnabledState();
    private final WifiPermissionsUtil mWifiPermissionsUtil;

    WifiController(Context context, ClientModeImpl clientModeImpl, Looper clientModeImplLooper, WifiSettingsStore wss, Looper wifiServiceLooper, FrameworkFacade f, ActiveModeWarden amw, WifiPermissionsUtil wifiPermissionsUtil) {
        super(TAG, wifiServiceLooper);
        this.mFacade = f;
        this.mContext = context;
        this.mClientModeImpl = clientModeImpl;
        this.mClientModeImplLooper = clientModeImplLooper;
        this.mActiveModeWarden = amw;
        this.mSettingsStore = wss;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        addState(this.mDefaultState);
        addState(this.mStaDisabledState, this.mDefaultState);
        addState(this.mStaEnabledState, this.mDefaultState);
        addState(this.mStaDisabledWithScanState, this.mDefaultState);
        addState(this.mEcmState, this.mDefaultState);
        setLogRecSize(100);
        setLogOnlyTransitions(false);
        this.mActiveModeWarden.registerScanOnlyCallback(this.mScanOnlyModeCallback);
        this.mActiveModeWarden.registerClientModeCallback(this.mClientModeCallback);
        readWifiReEnableDelay();
        readWifiRecoveryDelay();
    }

    public void start() {
        boolean isAirplaneModeOn = this.mSettingsStore.isAirplaneModeOn();
        boolean isWifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        boolean isScanningAlwaysAvailable = this.mSettingsStore.isScanAlwaysAvailable();
        boolean isLocationModeActive = this.mWifiPermissionsUtil.isLocationModeEnabled();
        logi("isAirplaneModeOn = " + isAirplaneModeOn + ", isWifiEnabled = " + isWifiEnabled + ", isScanningAvailable = " + isScanningAlwaysAvailable + ", isLocationModeActive = " + isLocationModeActive);
        if (checkScanOnlyModeAvailable()) {
            setInitialState(this.mStaDisabledWithScanState);
        } else {
            setInitialState(this.mStaDisabledState);
        }
        this.mActiveModeWarden.registerSoftApListener(this.mSoftApCallback);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.location.MODE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiController.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    if (WifiController.DBG) {
                        Slog.i(WifiController.TAG, "Received action: " + action);
                    }
                    if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                        if (intent.getIntExtra("wifi_state", 4) == 4) {
                            int realState = WifiController.this.mClientModeImpl.syncGetWifiState();
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
            }
        }, new IntentFilter(filter));
        super.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkScanOnlyModeAvailable() {
        if (!this.mWifiPermissionsUtil.isLocationModeEnabled()) {
            return false;
        }
        return this.mSettingsStore.isScanAlwaysAvailable();
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

    private class ScanOnlyCallback implements ScanOnlyModeManager.Listener {
        private ScanOnlyCallback() {
        }

        @Override // com.android.server.wifi.ScanOnlyModeManager.Listener
        public void onStateChanged(int state) {
            if (state == 4) {
                Log.i(WifiController.TAG, "ScanOnlyMode unexpected failure: state unknown");
            } else if (state == 1) {
                Log.i(WifiController.TAG, "ScanOnlyMode stopped");
                WifiController.this.sendMessage(WifiController.CMD_SCANNING_STOPPED);
            } else if (state == 3) {
                Log.i(WifiController.TAG, "scan mode active");
            } else {
                Log.i(WifiController.TAG, "unexpected state update: " + state);
            }
        }
    }

    private class ClientModeCallback implements ClientModeManager.Listener {
        private ClientModeCallback() {
        }

        @Override // com.android.server.wifi.ClientModeManager.Listener
        public void onStateChanged(int state) {
            if (state == 4) {
                WifiController.this.logi("ClientMode unexpected failure: state unknown");
                WifiController.this.sendMessage(WifiController.CMD_STA_START_FAILURE);
            } else if (state == 1) {
                WifiController.this.logi("ClientMode stopped");
                WifiController.this.sendMessage(WifiController.CMD_STA_STOPPED);
            } else if (state == 3) {
                WifiController.this.logi("client mode active");
            } else {
                WifiController wifiController = WifiController.this;
                wifiController.logi("unexpected state update: " + state);
            }
        }
    }

    private void readWifiReEnableDelay() {
        this.mReEnableDelayMillis = this.mFacade.getLongSetting(this.mContext, "wifi_reenable_delay", DEFAULT_REENABLE_DELAY_MS);
    }

    private void readWifiRecoveryDelay() {
        this.mRecoveryDelayMillis = this.mContext.getResources().getInteger(17694938);
        if (this.mRecoveryDelayMillis > MAX_RECOVERY_TIMEOUT_DELAY_MS) {
            this.mRecoveryDelayMillis = MAX_RECOVERY_TIMEOUT_DELAY_MS;
            Log.w(TAG, "Overriding timeout delay with maximum limit value");
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController wifiController = WifiController.this;
                wifiController.logi(getName() + " enter.\n");
            }
            WifiController wifiController2 = WifiController.this;
            wifiController2.createWifiProStateMachine(wifiController2.mContext, WifiController.this.mClientModeImpl.getMessenger());
            WifiController wifiController3 = WifiController.this;
            wifiController3.createQoEEngineService(wifiController3.mContext, WifiController.this.mClientModeImpl);
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController wifiController = WifiController.this;
                wifiController.logi(getName() + " what=" + msg.what);
            }
            if (WifiController.this.processDefaultState(msg)) {
                return true;
            }
            if (WifiController.DBG) {
                WifiController wifiController2 = WifiController.this;
                wifiController2.logd(getName() + " what=" + msg.what);
            }
            int i = msg.what;
            if (i != WifiController.CMD_EMERGENCY_MODE_CHANGED) {
                switch (i) {
                    case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /* 155655 */:
                    case WifiController.CMD_WIFI_TOGGLED /* 155656 */:
                    case WifiController.CMD_AP_START_FAILURE /* 155661 */:
                    case WifiController.CMD_STA_START_FAILURE /* 155664 */:
                    case WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE /* 155666 */:
                    case WifiController.CMD_STA_STOPPED /* 155668 */:
                    case WifiController.CMD_SCANNING_STOPPED /* 155669 */:
                    case WifiController.CMD_DEFERRED_RECOVERY_RESTART_WIFI /* 155670 */:
                        break;
                    case WifiController.CMD_AIRPLANE_TOGGLED /* 155657 */:
                        if (!WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                            WifiController.this.logi("Airplane mode disabled, determine next state");
                            if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                                if (WifiController.this.checkScanOnlyModeAvailable()) {
                                    WifiController wifiController3 = WifiController.this;
                                    wifiController3.transitionTo(wifiController3.mStaDisabledWithScanState);
                                    break;
                                }
                            } else {
                                WifiController wifiController4 = WifiController.this;
                                wifiController4.transitionTo(wifiController4.mStaEnabledState);
                                break;
                            }
                        } else {
                            WifiController.this.logi("Airplane mode toggled, shutdown all modes");
                            WifiController.this.mActiveModeWarden.shutdownWifi();
                            WifiController wifiController5 = WifiController.this;
                            wifiController5.transitionTo(wifiController5.mStaDisabledState);
                            break;
                        }
                        break;
                    case WifiController.CMD_SET_AP /* 155658 */:
                        if (msg.arg1 != 1) {
                            WifiController.this.mActiveModeWarden.stopSoftAPMode(msg.arg2);
                            break;
                        } else {
                            if (!(WifiController.this.mClientModeImpl == null || (WifiController.this.mClientModeImpl.getWifiMode() & 8) == 0)) {
                                WifiController.this.mSettingsStore.handleWifiToggled(false);
                            }
                            SoftApModeConfiguration softApModeConfiguration = (SoftApModeConfiguration) msg.obj;
                            WifiController.this.mActiveModeWarden.enterSoftAPMode((SoftApModeConfiguration) msg.obj);
                            Bundle data = new Bundle();
                            data.putBoolean(WifiController.OPEN_HOTSPOT, true);
                            WifiController.this.mHwWifiCHRService.uploadDFTEvent(15, data);
                            break;
                        }
                    case WifiController.CMD_DEFERRED_TOGGLE /* 155659 */:
                        WifiController.this.log("DEFERRED_TOGGLE ignored due to state change");
                        break;
                    case WifiController.CMD_USER_PRESENT /* 155660 */:
                        WifiController.this.mFirstUserSignOnSeen = true;
                        break;
                    case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /* 155662 */:
                        break;
                    case WifiController.CMD_AP_STOPPED /* 155663 */:
                        WifiController.this.logi("SoftAp mode disabled, determine next state");
                        if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                            if (WifiController.this.checkScanOnlyModeAvailable()) {
                                WifiController wifiController6 = WifiController.this;
                                wifiController6.transitionTo(wifiController6.mStaDisabledWithScanState);
                                break;
                            }
                        } else {
                            WifiController wifiController7 = WifiController.this;
                            wifiController7.transitionTo(wifiController7.mStaEnabledState);
                            break;
                        }
                        break;
                    case WifiController.CMD_RECOVERY_RESTART_WIFI /* 155665 */:
                        WifiController wifiController8 = WifiController.this;
                        wifiController8.deferMessage(wifiController8.obtainMessage(WifiController.CMD_DEFERRED_RECOVERY_RESTART_WIFI));
                        WifiController.this.mActiveModeWarden.shutdownWifi();
                        WifiController wifiController9 = WifiController.this;
                        wifiController9.transitionTo(wifiController9.mStaDisabledState);
                        break;
                    case WifiController.CMD_RECOVERY_DISABLE_WIFI /* 155667 */:
                        WifiController.this.logi("Recovery has been throttled, disable wifi");
                        WifiController.this.mActiveModeWarden.shutdownWifi();
                        WifiController wifiController10 = WifiController.this;
                        wifiController10.transitionTo(wifiController10.mStaDisabledState);
                        break;
                    case WifiController.CMD_SET_WIFI_SAVED_STATE_DISABLE /* 155671 */:
                        WifiController.this.mSettingsStore.setWifiSavedState(0);
                        break;
                    default:
                        throw new RuntimeException("WifiController.handleMessage " + msg.what);
                }
                return true;
            }
            if (msg.arg1 == 1) {
                WifiController wifiController11 = WifiController.this;
                wifiController11.transitionTo(wifiController11.mEcmState);
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class StaDisabledState extends State {
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable = false;

        StaDisabledState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.logi(getName() + " enter.\n");
            }
            WifiController.this.mActiveModeWarden.disableWifi();
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = false;
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.logi(getName() + " what=" + msg.what);
            }
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /* 155655 */:
                    if (WifiController.this.checkScanOnlyModeAvailable()) {
                        WifiController wifiController = WifiController.this;
                        wifiController.transitionTo(wifiController.mStaDisabledWithScanState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /* 155656 */:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (WifiController.this.checkScanOnlyModeAvailable() && WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                            WifiController wifiController2 = WifiController.this;
                            wifiController2.transitionTo(wifiController2.mStaDisabledWithScanState);
                            break;
                        }
                    } else if (!doDeferEnable(msg)) {
                        WifiController wifiController3 = WifiController.this;
                        wifiController3.transitionTo(wifiController3.mStaEnabledState);
                        break;
                    } else {
                        if (this.mHaveDeferredEnable) {
                            this.mDeferredEnableSerialNumber++;
                        }
                        this.mHaveDeferredEnable = !this.mHaveDeferredEnable;
                        break;
                    }
                case WifiController.CMD_SET_AP /* 155658 */:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(0);
                    }
                    return false;
                case WifiController.CMD_DEFERRED_TOGGLE /* 155659 */:
                    if (msg.arg1 == this.mDeferredEnableSerialNumber) {
                        WifiController.this.log("DEFERRED_TOGGLE handled");
                        WifiController.this.sendMessage((Message) msg.obj);
                        break;
                    } else {
                        WifiController.this.logi("DEFERRED_TOGGLE ignored due to serial mismatch");
                        break;
                    }
                case WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE /* 155666 */:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (WifiController.this.checkScanOnlyModeAvailable()) {
                            WifiController wifiController4 = WifiController.this;
                            wifiController4.transitionTo(wifiController4.mStaDisabledWithScanState);
                            break;
                        }
                    } else {
                        WifiController wifiController5 = WifiController.this;
                        wifiController5.transitionTo(wifiController5.mStaEnabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_DEFERRED_RECOVERY_RESTART_WIFI /* 155670 */:
                    WifiController wifiController6 = WifiController.this;
                    wifiController6.sendMessageDelayed(WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE, (long) wifiController6.mRecoveryDelayMillis);
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
            WifiController wifiController2 = WifiController.this;
            wifiController2.sendMessageDelayed(deferredMsg, (wifiController2.mReEnableDelayMillis - delaySoFar) + WifiController.DEFER_MARGIN_MS);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class StaEnabledState extends State {
        StaEnabledState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController wifiController = WifiController.this;
                wifiController.log(getName() + " enter.\n");
            }
            WifiController.this.logi("StaEnabledState.enter()");
            if (WifiController.this.mClientModeImpl != null && (WifiController.this.mClientModeImpl.getWifiMode() & 8) == 0) {
                WifiController.this.mClientModeImpl.setWifiMode(WifiCommonUtils.PACKAGE_NAME_FRAMEWORK, 0);
            }
            WifiController.this.mActiveModeWarden.enterClientMode();
        }

        public boolean processMessage(Message msg) {
            String bugTitle;
            String bugDetail;
            if (WifiController.DBG) {
                WifiController.this.logi(getName() + " what=" + msg.what);
            }
            if (WifiController.this.processStaEnabled(msg)) {
                return true;
            }
            switch (msg.what) {
                case WifiController.CMD_WIFI_TOGGLED /* 155656 */:
                    if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!(WifiController.this.mClientModeImpl == null || (WifiController.this.mClientModeImpl.getWifiMode() & 8) == 0)) {
                            WifiController.this.mClientModeImpl.setWifiMode(WifiCommonUtils.PACKAGE_NAME_FRAMEWORK, WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                            WifiController.this.mClientModeImpl.sendWifiStateBroadcast(3, 2);
                            break;
                        }
                    } else if (WifiController.this.mClientModeImpl == null || (WifiController.this.mClientModeImpl.getWifiMode() & 8) == 0) {
                        if (!WifiController.this.checkScanOnlyModeAvailable()) {
                            WifiController wifiController = WifiController.this;
                            wifiController.transitionTo(wifiController.mStaDisabledState);
                            break;
                        } else {
                            WifiController wifiController2 = WifiController.this;
                            wifiController2.transitionTo(wifiController2.mStaDisabledWithScanState);
                            break;
                        }
                    } else {
                        WifiController.this.mClientModeImpl.sendWifiStateBroadcast(1, 3);
                        break;
                    }
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /* 155657 */:
                    if (WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                        return false;
                    }
                    WifiController.this.logi("airplane mode toggled - and airplane mode is off.  return handled");
                    return true;
                case WifiController.CMD_SET_AP /* 155658 */:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(1);
                        if (WifiController.this.mClientModeImpl != null) {
                            WifiController.this.mClientModeImpl.saveLastNetIdForAp();
                        }
                    }
                    return false;
                case WifiController.CMD_DEFERRED_TOGGLE /* 155659 */:
                case WifiController.CMD_USER_PRESENT /* 155660 */:
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /* 155662 */:
                case WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE /* 155666 */:
                case WifiController.CMD_RECOVERY_DISABLE_WIFI /* 155667 */:
                default:
                    return false;
                case WifiController.CMD_AP_START_FAILURE /* 155661 */:
                case WifiController.CMD_AP_STOPPED /* 155663 */:
                    break;
                case WifiController.CMD_STA_START_FAILURE /* 155664 */:
                    if (WifiController.this.checkScanOnlyModeAvailable()) {
                        WifiController wifiController3 = WifiController.this;
                        wifiController3.transitionTo(wifiController3.mStaDisabledWithScanState);
                        break;
                    } else {
                        WifiController wifiController4 = WifiController.this;
                        wifiController4.transitionTo(wifiController4.mStaDisabledState);
                        break;
                    }
                case WifiController.CMD_RECOVERY_RESTART_WIFI /* 155665 */:
                    if (msg.arg1 >= SelfRecovery.REASON_STRINGS.length || msg.arg1 < 0) {
                        bugDetail = "";
                        bugTitle = "Wi-Fi BugReport";
                    } else {
                        bugDetail = SelfRecovery.REASON_STRINGS[msg.arg1];
                        bugTitle = "Wi-Fi BugReport: " + bugDetail;
                    }
                    if (msg.arg1 != 0) {
                        new Handler(WifiController.this.mClientModeImplLooper).post(new Runnable(bugTitle, bugDetail) {
                            /* class com.android.server.wifi.$$Lambda$WifiController$StaEnabledState$8UmuHOrDhEe90LG8fQyHFz8Png */
                            private final /* synthetic */ String f$1;
                            private final /* synthetic */ String f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                WifiController.StaEnabledState.this.lambda$processMessage$0$WifiController$StaEnabledState(this.f$1, this.f$2);
                            }
                        });
                    }
                    return false;
                case WifiController.CMD_STA_STOPPED /* 155668 */:
                    WifiController wifiController5 = WifiController.this;
                    wifiController5.transitionTo(wifiController5.mStaDisabledState);
                    break;
            }
            return true;
        }

        public /* synthetic */ void lambda$processMessage$0$WifiController$StaEnabledState(String bugTitle, String bugDetail) {
            WifiController.this.mClientModeImpl.takeBugReport(bugTitle, bugDetail);
        }

        public void exit() {
            WifiController.this.mClientModeImpl.setWifiMode(WifiCommonUtils.PACKAGE_NAME_FRAMEWORK, 0);
        }
    }

    /* access modifiers changed from: package-private */
    public class StaDisabledWithScanState extends State {
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable = false;

        StaDisabledWithScanState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.logi(getName() + " enter.\n");
            }
            WifiController.this.mActiveModeWarden.enterScanOnlyMode();
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = false;
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.logi(getName() + " what=" + msg.what);
            }
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /* 155655 */:
                    if (!WifiController.this.checkScanOnlyModeAvailable()) {
                        WifiController.this.logi("StaDisabledWithScanState: scan no longer available");
                        WifiController wifiController = WifiController.this;
                        wifiController.transitionTo(wifiController.mStaDisabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /* 155656 */:
                    if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!doDeferEnable(msg)) {
                            WifiController wifiController2 = WifiController.this;
                            wifiController2.transitionTo(wifiController2.mStaEnabledState);
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
                case WifiController.CMD_AIRPLANE_TOGGLED /* 155657 */:
                case WifiController.CMD_USER_PRESENT /* 155660 */:
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /* 155662 */:
                case WifiController.CMD_RECOVERY_RESTART_WIFI /* 155665 */:
                case WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE /* 155666 */:
                case WifiController.CMD_RECOVERY_DISABLE_WIFI /* 155667 */:
                case WifiController.CMD_STA_STOPPED /* 155668 */:
                default:
                    return false;
                case WifiController.CMD_SET_AP /* 155658 */:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(0);
                    }
                    return false;
                case WifiController.CMD_DEFERRED_TOGGLE /* 155659 */:
                    if (msg.arg1 == this.mDeferredEnableSerialNumber) {
                        WifiController.this.logd("DEFERRED_TOGGLE handled");
                        WifiController.this.sendMessage((Message) msg.obj);
                        break;
                    } else {
                        WifiController.this.logi("DEFERRED_TOGGLE ignored due to serial mismatch");
                        break;
                    }
                case WifiController.CMD_AP_START_FAILURE /* 155661 */:
                    break;
                case WifiController.CMD_AP_STOPPED /* 155663 */:
                    if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        WifiController wifiController3 = WifiController.this;
                        wifiController3.transitionTo(wifiController3.mStaEnabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_STA_START_FAILURE /* 155664 */:
                    WifiController.this.logi("CMD_STA_START_FAILURE  transition to mStaDisabledState.");
                    WifiController wifiController4 = WifiController.this;
                    wifiController4.transitionTo(wifiController4.mStaDisabledState);
                    break;
                case WifiController.CMD_SCANNING_STOPPED /* 155669 */:
                    WifiController.this.logi("WifiController: SCANNING_STOPPED when in scan mode -> StaDisabled");
                    WifiController wifiController5 = WifiController.this;
                    wifiController5.transitionTo(wifiController5.mStaDisabledState);
                    break;
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
            WifiController wifiController2 = WifiController.this;
            wifiController2.sendMessageDelayed(deferredMsg, (wifiController2.mReEnableDelayMillis - delaySoFar) + WifiController.DEFER_MARGIN_MS);
            return true;
        }
    }

    private State getNextWifiState() {
        if (this.mSettingsStore.getWifiSavedState() == 1) {
            return this.mStaEnabledState;
        }
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_on", 0) == 1) {
            Log.e(TAG, "getWifiSavedState and Settings.Global.WIFI_ON are different!");
            this.mSettingsStore.setWifiSavedState(1);
            return this.mStaEnabledState;
        } else if (checkScanOnlyModeAvailable()) {
            return this.mStaDisabledWithScanState;
        } else {
            return this.mStaDisabledState;
        }
    }

    /* access modifiers changed from: package-private */
    public class EcmState extends State {
        private int mEcmEntryCount;

        EcmState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController wifiController = WifiController.this;
                wifiController.logi(getName() + "\n");
            }
            WifiController.this.mActiveModeWarden.stopSoftAPMode(-1);
            boolean configWiFiDisableInECBM = WifiController.this.mFacade.getConfigWiFiDisableInECBM(WifiController.this.mContext);
            WifiController wifiController2 = WifiController.this;
            wifiController2.logi("WifiController msg getConfigWiFiDisableInECBM " + configWiFiDisableInECBM);
            if (configWiFiDisableInECBM) {
                WifiController.this.mActiveModeWarden.shutdownWifi();
            }
            this.mEcmEntryCount = 1;
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.logi(getName() + msg.toString() + "\n");
            }
            switch (msg.what) {
                case WifiController.CMD_EMERGENCY_MODE_CHANGED /* 155649 */:
                    if (msg.arg1 == 1) {
                        this.mEcmEntryCount++;
                    } else if (msg.arg1 == 0) {
                        decrementCountAndReturnToAppropriateState();
                    }
                    return true;
                case WifiController.CMD_SET_AP /* 155658 */:
                    return true;
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /* 155662 */:
                    if (msg.arg1 == 1) {
                        this.mEcmEntryCount++;
                    } else if (msg.arg1 == 0) {
                        decrementCountAndReturnToAppropriateState();
                    }
                    return true;
                case WifiController.CMD_AP_STOPPED /* 155663 */:
                case WifiController.CMD_STA_STOPPED /* 155668 */:
                case WifiController.CMD_SCANNING_STOPPED /* 155669 */:
                    return true;
                case WifiController.CMD_RECOVERY_RESTART_WIFI /* 155665 */:
                case WifiController.CMD_RECOVERY_DISABLE_WIFI /* 155667 */:
                    return true;
                default:
                    return false;
            }
        }

        private void decrementCountAndReturnToAppropriateState() {
            boolean exitEcm = false;
            int i = this.mEcmEntryCount;
            if (i == 0) {
                WifiController.this.loge("mEcmEntryCount is 0; exiting Ecm");
                exitEcm = true;
            } else {
                int i2 = i - 1;
                this.mEcmEntryCount = i2;
                if (i2 == 0) {
                    exitEcm = true;
                }
            }
            if (!exitEcm) {
                return;
            }
            if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                WifiController wifiController = WifiController.this;
                wifiController.transitionTo(wifiController.mStaEnabledState);
            } else if (WifiController.this.checkScanOnlyModeAvailable()) {
                WifiController wifiController2 = WifiController.this;
                wifiController2.transitionTo(wifiController2.mStaDisabledWithScanState);
            } else {
                WifiController wifiController3 = WifiController.this;
                wifiController3.transitionTo(wifiController3.mStaDisabledState);
            }
        }
    }
}
