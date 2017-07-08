package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.HiSiWifiComm;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.State;
import com.android.server.wifi.WifiServiceImpl.LockList;
import com.google.protobuf.nano.Extension;
import java.io.FileDescriptor;
import java.io.PrintWriter;

class WifiController extends AbsWifiController {
    public static final String ACTION_DELAY_DEVICE_IDLE = "com.android.server.WifiManager.action.DELAY_DEVICE_IDLE";
    private static final String ACTION_DEVICE_IDLE = "com.android.server.WifiManager.action.DEVICE_IDLE";
    private static final int BASE = 155648;
    static final int CMD_AIRPLANE_TOGGLED = 155657;
    static final int CMD_AP_START_FAILURE = 155661;
    static final int CMD_AP_STOPPED = 155663;
    static final int CMD_BATTERY_CHANGED = 155652;
    static final int CMD_DEFERRED_TOGGLE = 155659;
    static final int CMD_DELAY_DEVICE_IDLE = 155698;
    static final int CMD_DEVICE_IDLE = 155653;
    static final int CMD_EMERGENCY_CALL_STATE_CHANGED = 155662;
    static final int CMD_EMERGENCY_MODE_CHANGED = 155649;
    static final int CMD_LOCKS_CHANGED = 155654;
    static final int CMD_SCAN_ALWAYS_MODE_CHANGED = 155655;
    static final int CMD_SCREEN_OFF = 155651;
    static final int CMD_SCREEN_ON = 155650;
    static final int CMD_SET_AP = 155658;
    static final int CMD_STA_START_FAILURE = 155664;
    static final int CMD_USER_PRESENT = 155660;
    static final int CMD_WIFI_TOGGLED = 155656;
    private static final boolean DBG = false;
    private static final long DEFAULT_IDLE_MS = 1800000;
    private static final long DEFAULT_REENABLE_DELAY_MS = 500;
    private static final long DEFER_MARGIN_MS = 5;
    private static final long DELAY_IDLE_MS = 60000;
    protected static final boolean HWFLOW = false;
    private static final int IDLE_REQUEST = 0;
    private static final String TAG = "WifiController";
    private AlarmManager mAlarmManager;
    private ApEnabledState mApEnabledState;
    private ApStaDisabledState mApStaDisabledState;
    private Context mContext;
    private DefaultState mDefaultState;
    private PendingIntent mDelayIdleIntent;
    private DeviceActiveState mDeviceActiveState;
    private boolean mDeviceIdle;
    private DeviceInactiveState mDeviceInactiveState;
    private EcmState mEcmState;
    private FrameworkFacade mFacade;
    private boolean mFirstUserSignOnSeen;
    private FullHighPerfLockHeldState mFullHighPerfLockHeldState;
    private FullLockHeldState mFullLockHeldState;
    private PendingIntent mIdleIntent;
    private long mIdleMillis;
    final LockList mLocks;
    NetworkInfo mNetworkInfo;
    private NoLockHeldState mNoLockHeldState;
    private int mPluggedType;
    private long mReEnableDelayMillis;
    private ScanOnlyLockHeldState mScanOnlyLockHeldState;
    private boolean mScreenOff;
    final WifiSettingsStore mSettingsStore;
    private int mSleepPolicy;
    private StaDisabledWithScanState mStaDisabledWithScanState;
    private StaEnabledState mStaEnabledState;
    private int mStayAwakeConditions;
    private final WorkSource mTmpWorkSource;
    final WifiStateMachine mWifiStateMachine;

    /* renamed from: com.android.server.wifi.WifiController.2 */
    class AnonymousClass2 extends ContentObserver {
        AnonymousClass2(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiController.this.readStayAwakeConditions();
        }
    }

    /* renamed from: com.android.server.wifi.WifiController.3 */
    class AnonymousClass3 extends ContentObserver {
        AnonymousClass3(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiController.this.readWifiIdleTime();
        }
    }

    /* renamed from: com.android.server.wifi.WifiController.4 */
    class AnonymousClass4 extends ContentObserver {
        AnonymousClass4(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiController.this.readWifiSleepPolicy();
        }
    }

    class ApEnabledState extends State {
        private State mPendingState;

        ApEnabledState() {
            this.mPendingState = null;
        }

        private State getNextWifiState() {
            if (WifiController.this.mSettingsStore.getWifiSavedState() == 1) {
                return WifiController.this.mDeviceActiveState;
            }
            if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                return WifiController.this.mStaDisabledWithScanState;
            }
            return WifiController.this.mApStaDisabledState;
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + " enter.\n");
            }
            this.mPendingState = null;
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.logd(getName() + " what=" + msg.what);
            }
            switch (msg.what) {
                case WifiController.CMD_EMERGENCY_MODE_CHANGED /*155649*/:
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*155662*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mWifiStateMachine.setHostApRunning(null, WifiController.HWFLOW);
                        this.mPendingState = WifiController.this.mEcmState;
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        WifiController.this.mWifiStateMachine.setHostApRunning(null, WifiController.HWFLOW);
                        this.mPendingState = WifiController.this.mDeviceActiveState;
                        break;
                    }
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                        WifiController.this.mWifiStateMachine.setHostApRunning(null, WifiController.HWFLOW);
                        this.mPendingState = WifiController.this.mApStaDisabledState;
                        break;
                    }
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 != 0) {
                        if (msg.arg1 == 1) {
                            WifiController.this.loge(" ApEnabledState, Before starting tethering, turn off HostAp");
                            WifiController.this.mWifiStateMachine.setHostApRunning(null, WifiController.HWFLOW);
                            WifiController.this.deferMessage(msg);
                            this.mPendingState = WifiController.this.mApStaDisabledState;
                            break;
                        }
                    }
                    WifiController.this.mWifiStateMachine.setHostApRunning(null, WifiController.HWFLOW);
                    this.mPendingState = getNextWifiState();
                    break;
                    break;
                case WifiController.CMD_AP_START_FAILURE /*155661*/:
                    WifiController.this.transitionTo(getNextWifiState());
                    break;
                case WifiController.CMD_AP_STOPPED /*155663*/:
                    if (this.mPendingState == null) {
                        this.mPendingState = getNextWifiState();
                    }
                    if (this.mPendingState != WifiController.this.mDeviceActiveState || !WifiController.this.mDeviceIdle) {
                        WifiController.this.transitionTo(this.mPendingState);
                        break;
                    }
                    WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                    break;
                    break;
                default:
                    return WifiController.HWFLOW;
            }
            return true;
        }
    }

    class ApStaDisabledState extends State {
        private int mDeferredEnableSerialNumber;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable;

        ApStaDisabledState() {
            this.mDeferredEnableSerialNumber = WifiController.IDLE_REQUEST;
            this.mHaveDeferredEnable = WifiController.HWFLOW;
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + " enter.\n");
            }
            WifiController.this.mWifiStateMachine.setSupplicantRunning(WifiController.HWFLOW);
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = WifiController.HWFLOW;
            WifiController.this.mWifiStateMachine.clearANQPCache();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message msg) {
            boolean z = WifiController.HWFLOW;
            if (WifiController.DBG) {
                WifiController.this.logd(getName() + " what=" + msg.what);
            }
            switch (msg.what) {
                case WifiController.CMD_SCREEN_OFF /*155651*/:
                    if (!WifiController.this.shouldWifiStayAwake(WifiController.this.mPluggedType)) {
                        WifiController.this.mScreenOff = true;
                        WifiController.this.sendMessage(WifiController.CMD_DEVICE_IDLE);
                        break;
                    }
                    break;
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                            WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                            break;
                        }
                    } else if (!doDeferEnable(msg)) {
                        if (!WifiController.this.mDeviceIdle) {
                            WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                            break;
                        }
                        WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                        break;
                    } else {
                        if (this.mHaveDeferredEnable) {
                            this.mDeferredEnableSerialNumber++;
                        }
                        if (!this.mHaveDeferredEnable) {
                            z = true;
                        }
                        this.mHaveDeferredEnable = z;
                        break;
                    }
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        if (msg.arg2 == 0) {
                            WifiController.this.mSettingsStore.setWifiSavedState(WifiController.IDLE_REQUEST);
                        }
                        WifiController.this.mWifiStateMachine.setHostApRunning((WifiConfiguration) msg.obj, true);
                        WifiController.this.transitionTo(WifiController.this.mApEnabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_DEFERRED_TOGGLE /*155659*/:
                    if (msg.arg1 == this.mDeferredEnableSerialNumber) {
                        WifiController.this.log("DEFERRED_TOGGLE handled");
                        WifiController.this.sendMessage((Message) msg.obj);
                        break;
                    }
                    WifiController.this.log("DEFERRED_TOGGLE ignored due to serial mismatch");
                    break;
            }
        }

        private boolean doDeferEnable(Message msg) {
            long delaySoFar = SystemClock.elapsedRealtime() - this.mDisabledTimestamp;
            if (delaySoFar >= WifiController.this.mReEnableDelayMillis) {
                return WifiController.HWFLOW;
            }
            WifiController.this.log("WifiController msg " + msg + " deferred for " + (WifiController.this.mReEnableDelayMillis - delaySoFar) + "ms");
            Message deferredMsg = WifiController.this.obtainMessage(WifiController.CMD_DEFERRED_TOGGLE);
            deferredMsg.obj = Message.obtain(msg);
            int i = this.mDeferredEnableSerialNumber + 1;
            this.mDeferredEnableSerialNumber = i;
            deferredMsg.arg1 = i;
            WifiController.this.sendMessageDelayed(deferredMsg, (WifiController.this.mReEnableDelayMillis - delaySoFar) + WifiController.DEFER_MARGIN_MS);
            return true;
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + " enter.\n");
            }
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.logd(getName() + " what=" + msg.what);
            }
            if (WifiController.this.processDefaultState(msg)) {
                return true;
            }
            switch (msg.what) {
                case WifiController.CMD_EMERGENCY_MODE_CHANGED /*155649*/:
                case WifiController.CMD_LOCKS_CHANGED /*155654*/:
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                case WifiController.CMD_SET_AP /*155658*/:
                case WifiController.CMD_AP_START_FAILURE /*155661*/:
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*155662*/:
                case WifiController.CMD_AP_STOPPED /*155663*/:
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    break;
                case WifiController.CMD_SCREEN_ON /*155650*/:
                    WifiController.this.mAlarmManager.cancel(WifiController.this.mDelayIdleIntent);
                    WifiController.this.mAlarmManager.cancel(WifiController.this.mIdleIntent);
                    WifiController.this.mScreenOff = WifiController.HWFLOW;
                    WifiController.this.mDeviceIdle = WifiController.HWFLOW;
                    WifiController.this.updateBatteryWorkSource();
                    break;
                case WifiController.CMD_SCREEN_OFF /*155651*/:
                    WifiController.this.mScreenOff = true;
                    if (!WifiController.this.shouldWifiStayAwake(WifiController.this.mPluggedType)) {
                        if (WifiController.this.mNetworkInfo.getDetailedState() != DetailedState.CONNECTED) {
                            if (WifiController.DBG) {
                                Slog.d(WifiController.TAG, "set delay idle timer: 60000");
                            }
                            WifiController.this.mAlarmManager.setExact(WifiController.IDLE_REQUEST, System.currentTimeMillis() + WifiController.DELAY_IDLE_MS, WifiController.this.mDelayIdleIntent);
                            break;
                        }
                        if (WifiController.DBG) {
                            Slog.d(WifiController.TAG, "set idle timer: " + WifiController.this.mIdleMillis + " ms");
                        }
                        WifiController.this.mAlarmManager.setExact(WifiController.IDLE_REQUEST, System.currentTimeMillis() + WifiController.this.mIdleMillis, WifiController.this.mIdleIntent);
                        break;
                    }
                    break;
                case WifiController.CMD_BATTERY_CHANGED /*155652*/:
                    int pluggedType = msg.arg1;
                    if (WifiController.DBG) {
                        Slog.d(WifiController.TAG, "battery changed pluggedType: " + pluggedType);
                    }
                    if (WifiController.this.mScreenOff && WifiController.this.shouldWifiStayAwake(WifiController.this.mPluggedType) && !WifiController.this.shouldWifiStayAwake(pluggedType)) {
                        long triggerTime = System.currentTimeMillis() + WifiController.this.mIdleMillis;
                        if (WifiController.DBG) {
                            Slog.d(WifiController.TAG, "set idle timer for " + WifiController.this.mIdleMillis + "ms");
                        }
                        WifiController.this.mAlarmManager.setExact(WifiController.IDLE_REQUEST, triggerTime, WifiController.this.mIdleIntent);
                    }
                    WifiController.this.mPluggedType = pluggedType;
                    break;
                case WifiController.CMD_DEVICE_IDLE /*155653*/:
                    WifiController.this.mDeviceIdle = true;
                    WifiController.this.updateBatteryWorkSource();
                    break;
                case WifiController.CMD_DEFERRED_TOGGLE /*155659*/:
                    WifiController.this.log("DEFERRED_TOGGLE ignored due to state change");
                    break;
                case WifiController.CMD_USER_PRESENT /*155660*/:
                    WifiController.this.mFirstUserSignOnSeen = true;
                    break;
                case WifiController.CMD_DELAY_DEVICE_IDLE /*155698*/:
                    if (WifiController.this.mNetworkInfo.getDetailedState() != DetailedState.CONNECTED) {
                        WifiController.this.sendMessage(WifiController.CMD_DEVICE_IDLE);
                        break;
                    }
                    if (WifiController.DBG) {
                        Slog.d(WifiController.TAG, "set idle timer: " + WifiController.this.mIdleMillis + " ms");
                    }
                    WifiController.this.mAlarmManager.setExact(WifiController.IDLE_REQUEST, System.currentTimeMillis() + WifiController.this.mIdleMillis, WifiController.this.mIdleIntent);
                    break;
                default:
                    throw new RuntimeException("WifiController.handleMessage " + msg.what);
            }
            return true;
        }
    }

    class DeviceActiveState extends State {
        DeviceActiveState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + " enter.\n");
            }
            if (WifiController.this.setOperationalModeByMode()) {
                Log.d("HwWifiController", "DeviceActiveState enter setOperationalModeInDeviceActive");
            } else {
                WifiController.this.mWifiStateMachine.setOperationalMode(1);
            }
            WifiController.this.mWifiStateMachine.setDriverStart(true);
            WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(WifiController.HWFLOW);
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + msg.toString() + "\n");
            }
            if (msg.what == WifiController.CMD_DEVICE_IDLE) {
                if (WifiController.this.mScreenOff) {
                    WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                } else {
                    Slog.w(WifiController.TAG, "Error Screen on, recevie CMC_DEVICE_IDLE cmd");
                }
            } else if (msg.what == WifiController.CMD_USER_PRESENT) {
                if (!WifiController.this.mFirstUserSignOnSeen) {
                    WifiController.this.mWifiStateMachine.reloadTlsNetworksAndReconnect();
                }
                WifiController.this.mFirstUserSignOnSeen = true;
                return true;
            }
            return WifiController.HWFLOW;
        }
    }

    class DeviceInactiveState extends State {
        DeviceInactiveState() {
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + msg.toString() + "\n");
            }
            switch (msg.what) {
                case WifiController.CMD_SCREEN_ON /*155650*/:
                    WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                    return WifiController.HWFLOW;
                case WifiController.CMD_LOCKS_CHANGED /*155654*/:
                    WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                    WifiController.this.updateBatteryWorkSource();
                    return true;
                default:
                    return WifiController.HWFLOW;
            }
        }
    }

    class EcmState extends State {
        private int mEcmEntryCount;

        EcmState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + "\n");
            }
            WifiController.this.mWifiStateMachine.setSupplicantRunning(WifiController.HWFLOW);
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
            } else if (msg.what != WifiController.CMD_EMERGENCY_MODE_CHANGED) {
                return WifiController.HWFLOW;
            } else {
                if (msg.arg1 == 1) {
                    this.mEcmEntryCount++;
                } else if (msg.arg1 == 0) {
                    decrementCountAndReturnToAppropriateState();
                }
                return true;
            }
        }

        private void decrementCountAndReturnToAppropriateState() {
            boolean exitEcm = WifiController.HWFLOW;
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
                if (WifiController.this.mDeviceIdle) {
                    WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                } else {
                    WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                }
            } else if (WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
            } else {
                WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
            }
        }
    }

    class FullHighPerfLockHeldState extends State {
        FullHighPerfLockHeldState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + "\n");
            }
            if (WifiController.this.setOperationalModeByMode()) {
                Log.d("HwWifiController", "DeviceActiveState enter setOperationalModeInDeviceActive");
            } else {
                WifiController.this.mWifiStateMachine.setOperationalMode(1);
            }
            WifiController.this.mWifiStateMachine.setDriverStart(true);
            WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(true);
            WifiController.this.startWifiDataTrafficTrack();
        }

        public void exit() {
            WifiController.this.stopWifiDataTrafficTrack();
            super.exit();
        }
    }

    class FullLockHeldState extends State {
        FullLockHeldState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + "\n");
            }
            if (WifiController.this.setOperationalModeByMode()) {
                Log.d("HwWifiController", "DeviceActiveState enter setOperationalModeInDeviceActive");
            } else {
                WifiController.this.mWifiStateMachine.setOperationalMode(1);
            }
            WifiController.this.mWifiStateMachine.setDriverStart(true);
            WifiController.this.mWifiStateMachine.setHighPerfModeEnabled(WifiController.HWFLOW);
            WifiController.this.startWifiDataTrafficTrack();
        }

        public void exit() {
            WifiController.this.stopWifiDataTrafficTrack();
            super.exit();
        }
    }

    class NoLockHeldState extends State {
        NoLockHeldState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + "\n");
            }
            WifiController.this.mWifiStateMachine.setDriverStart(WifiController.HWFLOW);
        }
    }

    class ScanOnlyLockHeldState extends State {
        ScanOnlyLockHeldState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + "\n");
            }
            WifiController.this.mWifiStateMachine.setOperationalMode(2);
            WifiController.this.mWifiStateMachine.setDriverStart(true);
        }
    }

    class StaDisabledWithScanState extends State {
        private int mDeferredEnableSerialNumber;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable;

        StaDisabledWithScanState() {
            this.mDeferredEnableSerialNumber = WifiController.IDLE_REQUEST;
            this.mHaveDeferredEnable = WifiController.HWFLOW;
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log(getName() + " enter.\n");
            }
            WifiController.this.mWifiStateMachine.setSupplicantRunning(true);
            WifiController.this.mWifiStateMachine.setOperationalMode(3);
            WifiController.this.mWifiStateMachine.setDriverStart(true);
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = WifiController.HWFLOW;
            WifiController.this.mWifiStateMachine.clearANQPCache();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message msg) {
            boolean z = WifiController.HWFLOW;
            if (WifiController.DBG) {
                WifiController.this.logd(getName() + " what=" + msg.what);
            }
            switch (msg.what) {
                case WifiController.CMD_SCREEN_OFF /*155651*/:
                    if (!WifiController.this.shouldWifiStayAwake(WifiController.this.mPluggedType)) {
                        WifiController.this.mScreenOff = true;
                        WifiController.this.sendMessage(WifiController.CMD_DEVICE_IDLE);
                        break;
                    }
                    break;
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*155655*/:
                    if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!doDeferEnable(msg)) {
                            if (!WifiController.this.mDeviceIdle) {
                                WifiController.this.transitionTo(WifiController.this.mDeviceActiveState);
                                break;
                            }
                            WifiController.this.checkLocksAndTransitionWhenDeviceIdle();
                            break;
                        }
                        if (this.mHaveDeferredEnable) {
                            this.mDeferredEnableSerialNumber++;
                        }
                        if (!this.mHaveDeferredEnable) {
                            z = true;
                        }
                        this.mHaveDeferredEnable = z;
                        break;
                    }
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (WifiController.this.mSettingsStore.isAirplaneModeOn() && !WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(WifiController.IDLE_REQUEST);
                        WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_DEFERRED_TOGGLE /*155659*/:
                    if (msg.arg1 == this.mDeferredEnableSerialNumber) {
                        WifiController.this.logd("DEFERRED_TOGGLE handled");
                        WifiController.this.sendMessage((Message) msg.obj);
                        break;
                    }
                    WifiController.this.log("DEFERRED_TOGGLE ignored due to serial mismatch");
                    break;
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    WifiController.this.logd("CMD_STA_START_FAILURE  transition to mApStaDisabledState.");
                    WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                    break;
            }
        }

        private boolean doDeferEnable(Message msg) {
            long delaySoFar = SystemClock.elapsedRealtime() - this.mDisabledTimestamp;
            if (delaySoFar >= WifiController.this.mReEnableDelayMillis) {
                return WifiController.HWFLOW;
            }
            WifiController.this.log("WifiController msg " + msg + " deferred for " + (WifiController.this.mReEnableDelayMillis - delaySoFar) + "ms");
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
                WifiController.this.log(getName() + " enter.\n");
            }
            WifiController.this.mWifiStateMachine.setSupplicantRunning(true);
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                WifiController.this.logd(getName() + " what=" + msg.what);
            }
            if (WifiController.this.processStaEnabled(msg)) {
                return true;
            }
            switch (msg.what) {
                case WifiController.CMD_EMERGENCY_MODE_CHANGED /*155649*/:
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*155662*/:
                    boolean getConfigWiFiDisableInECBM = WifiController.this.mFacade.getConfigWiFiDisableInECBM(WifiController.this.mContext);
                    WifiController.this.log("WifiController msg " + msg + " getConfigWiFiDisableInECBM " + getConfigWiFiDisableInECBM);
                    if (msg.arg1 == 1 && getConfigWiFiDisableInECBM) {
                        WifiController.this.transitionTo(WifiController.this.mEcmState);
                        break;
                    }
                case WifiController.CMD_WIFI_TOGGLED /*155656*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                            WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                            break;
                        }
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                        break;
                    }
                    break;
                case WifiController.CMD_AIRPLANE_TOGGLED /*155657*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_SET_AP /*155658*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(1);
                        WifiController.this.deferMessage(WifiController.this.obtainMessage(msg.what, msg.arg1, 1, msg.obj));
                        WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_STA_START_FAILURE /*155664*/:
                    WifiController.this.mSettingsStore.handleWifiToggled(WifiController.HWFLOW);
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!WifiController.this.mSettingsStore.isScanAlwaysAvailable()) {
                            WifiController.this.transitionTo(WifiController.this.mApStaDisabledState);
                            break;
                        }
                        WifiController.this.transitionTo(WifiController.this.mStaDisabledWithScanState);
                        break;
                    }
                    break;
                default:
                    return WifiController.HWFLOW;
            }
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.WifiController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.WifiController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiController.<clinit>():void");
    }

    WifiController(Context context, WifiStateMachine wsm, WifiSettingsStore wss, LockList locks, Looper looper, FrameworkFacade f) {
        super(TAG, looper);
        this.mFirstUserSignOnSeen = HWFLOW;
        this.mNetworkInfo = new NetworkInfo(1, IDLE_REQUEST, "WIFI", "");
        this.mTmpWorkSource = new WorkSource();
        this.mDefaultState = new DefaultState();
        this.mStaEnabledState = new StaEnabledState();
        this.mApStaDisabledState = new ApStaDisabledState();
        this.mStaDisabledWithScanState = new StaDisabledWithScanState();
        this.mApEnabledState = new ApEnabledState();
        this.mDeviceActiveState = new DeviceActiveState();
        this.mDeviceInactiveState = new DeviceInactiveState();
        this.mScanOnlyLockHeldState = new ScanOnlyLockHeldState();
        this.mFullLockHeldState = new FullLockHeldState();
        this.mFullHighPerfLockHeldState = new FullHighPerfLockHeldState();
        this.mNoLockHeldState = new NoLockHeldState();
        this.mEcmState = new EcmState();
        this.mFacade = f;
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mSettingsStore = wss;
        this.mLocks = locks;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mIdleIntent = this.mFacade.getBroadcast(this.mContext, IDLE_REQUEST, new Intent(ACTION_DEVICE_IDLE, null), IDLE_REQUEST);
        this.mDelayIdleIntent = this.mFacade.getBroadcast(this.mContext, IDLE_REQUEST, new Intent(ACTION_DELAY_DEVICE_IDLE, null), IDLE_REQUEST);
        addState(this.mDefaultState);
        addState(this.mApStaDisabledState, this.mDefaultState);
        addState(this.mStaEnabledState, this.mDefaultState);
        addState(this.mDeviceActiveState, this.mStaEnabledState);
        addState(this.mDeviceInactiveState, this.mStaEnabledState);
        addState(this.mScanOnlyLockHeldState, this.mDeviceInactiveState);
        addState(this.mFullLockHeldState, this.mDeviceInactiveState);
        addState(this.mFullHighPerfLockHeldState, this.mDeviceInactiveState);
        addState(this.mNoLockHeldState, this.mDeviceInactiveState);
        addState(this.mStaDisabledWithScanState, this.mDefaultState);
        addState(this.mApEnabledState, this.mDefaultState);
        addState(this.mEcmState, this.mDefaultState);
        boolean isAirplaneModeOn = this.mSettingsStore.isAirplaneModeOn();
        boolean isWifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        boolean isScanningAlwaysAvailable = this.mSettingsStore.isScanAlwaysAvailable();
        log("isAirplaneModeOn = " + isAirplaneModeOn + ", isWifiEnabled = " + isWifiEnabled + ", isScanningAvailable = " + isScanningAlwaysAvailable);
        if (isScanningAlwaysAvailable) {
            setInitialState(this.mStaDisabledWithScanState);
        } else {
            setInitialState(this.mApStaDisabledState);
        }
        setLogRecSize(100);
        setLogOnlyTransitions(HWFLOW);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DEVICE_IDLE);
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction(ACTION_DELAY_DEVICE_IDLE);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiController.DBG) {
                    Slog.d(WifiController.TAG, "Received action: " + action);
                }
                if (action.equals(WifiController.ACTION_DEVICE_IDLE)) {
                    WifiController.this.sendMessage(WifiController.CMD_DEVICE_IDLE);
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null) {
                        WifiController.this.mNetworkInfo = networkInfo;
                    }
                } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                    int state = intent.getIntExtra("wifi_state", 14);
                    if (state == 14) {
                        WifiController.this.loge("WifiControllerSoftAP start failed");
                        WifiController.this.sendMessage(WifiController.CMD_AP_START_FAILURE);
                    } else if (state == 11) {
                        WifiController.this.sendMessage(WifiController.CMD_AP_STOPPED);
                    }
                } else if (action.equals(WifiController.ACTION_DELAY_DEVICE_IDLE)) {
                    WifiController.this.sendMessage(WifiController.CMD_DELAY_DEVICE_IDLE);
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED") && intent.getIntExtra("wifi_state", 4) == 4) {
                    WifiController.this.loge("WifiControllerWifi turn on failed");
                    WifiController.this.sendMessage(WifiController.CMD_STA_START_FAILURE);
                }
            }
        }, new IntentFilter(filter));
        initializeAndRegisterForSettingsChange(looper);
    }

    private void initializeAndRegisterForSettingsChange(Looper looper) {
        Handler handler = new Handler(looper);
        readStayAwakeConditions();
        registerForStayAwakeModeChange(handler);
        readWifiIdleTime();
        registerForWifiIdleTimeChange(handler);
        readWifiSleepPolicy();
        registerForWifiSleepPolicyChange(handler);
        readWifiReEnableDelay();
    }

    private void readStayAwakeConditions() {
        this.mStayAwakeConditions = this.mFacade.getIntegerSetting(this.mContext, "stay_on_while_plugged_in", IDLE_REQUEST);
    }

    private void readWifiIdleTime() {
        this.mIdleMillis = this.mFacade.getLongSetting(this.mContext, "wifi_idle_ms", DEFAULT_IDLE_MS);
    }

    private void readWifiSleepPolicy() {
        this.mSleepPolicy = this.mFacade.getIntegerSetting(this.mContext, "wifi_sleep_policy", 2);
    }

    private void readWifiReEnableDelay() {
        this.mReEnableDelayMillis = this.mFacade.getLongSetting(this.mContext, "wifi_reenable_delay", DEFAULT_REENABLE_DELAY_MS);
    }

    private void registerForStayAwakeModeChange(Handler handler) {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("stay_on_while_plugged_in"), HWFLOW, new AnonymousClass2(handler));
    }

    private void registerForWifiIdleTimeChange(Handler handler) {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_idle_ms"), HWFLOW, new AnonymousClass3(handler));
    }

    private void registerForWifiSleepPolicyChange(Handler handler) {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_sleep_policy"), HWFLOW, new AnonymousClass4(handler));
    }

    private boolean shouldWifiStayAwake(int pluggedType) {
        if (DBG) {
            Slog.d(TAG, "wifiSleepPolicy:" + this.mSleepPolicy);
        }
        if (this.mSleepPolicy == 2) {
            if (HiSiWifiComm.hisiWifiEnabled()) {
                HiSiWifiComm.writeSleepOption(2);
            }
            return true;
        } else if (this.mSleepPolicy != 1 || pluggedType == 0) {
            if (HiSiWifiComm.hisiWifiEnabled()) {
                HiSiWifiComm.writeSleepOption(IDLE_REQUEST);
            }
            if (isWifiRepeaterStarted()) {
                return true;
            }
            return shouldDeviceStayAwake(pluggedType);
        } else {
            if (HiSiWifiComm.hisiWifiEnabled()) {
                HiSiWifiComm.writeSleepOption(1);
            }
            return true;
        }
    }

    private boolean shouldDeviceStayAwake(int pluggedType) {
        return (this.mStayAwakeConditions & pluggedType) != 0 ? true : HWFLOW;
    }

    private void updateBatteryWorkSource() {
        this.mTmpWorkSource.clear();
        if (this.mDeviceIdle) {
            this.mLocks.updateWorkSource(this.mTmpWorkSource);
        }
        this.mWifiStateMachine.updateBatteryWorkSource(this.mTmpWorkSource);
    }

    private void checkLocksAndTransitionWhenDeviceIdle() {
        if (this.mLocks.hasLocks()) {
            switch (this.mLocks.getStrongestLockMode()) {
                case Extension.TYPE_DOUBLE /*1*/:
                    transitionTo(this.mFullLockHeldState);
                case Extension.TYPE_FLOAT /*2*/:
                    transitionTo(this.mScanOnlyLockHeldState);
                case Extension.TYPE_INT64 /*3*/:
                    transitionTo(this.mFullHighPerfLockHeldState);
                default:
                    loge("Illegal lock " + this.mLocks.getStrongestLockMode());
            }
        } else if (this.mSettingsStore.isScanAlwaysAvailable()) {
            transitionTo(this.mScanOnlyLockHeldState);
        } else {
            transitionTo(this.mNoLockHeldState);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        pw.println("mScreenOff " + this.mScreenOff);
        pw.println("mDeviceIdle " + this.mDeviceIdle);
        pw.println("mPluggedType " + this.mPluggedType);
        pw.println("mIdleMillis " + this.mIdleMillis);
        pw.println("mSleepPolicy " + this.mSleepPolicy);
    }

    public void setupHwSelfCureEngine(Context context, WifiStateMachine wsm) {
    }

    public void createABSService(Context context, WifiStateMachine wifiStateMachine) {
    }
}
