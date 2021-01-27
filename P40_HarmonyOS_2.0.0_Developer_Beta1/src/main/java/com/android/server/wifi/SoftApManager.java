package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.wifi.WifiNative;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Locale;

public class SoftApManager extends AbsSoftApManager implements ActiveModeManager, IHwSoftApManagerInner {
    public static final String AP_LINKED_EVENT_KEY = "event_key";
    public static final String AP_LINKED_MAC_KEY = "mac_key";
    private static final int MIN_SOFT_AP_TIMEOUT_DELAY_MS = 600000;
    @VisibleForTesting
    public static final String SOFT_AP_SEND_MESSAGE_TIMEOUT_TAG = "SoftApManager Soft AP Send Message Timeout";
    public static final String STA_JOIN_EVENT = "STA_JOIN";
    public static final String STA_LEAVE_EVENT = "STA_LEAVE";
    private static final String TAG = "SoftApManager";
    protected WifiConfiguration mApConfig;
    private String mApInterfaceName;
    private final WifiManager.SoftApCallback mCallback;
    private final Context mContext;
    private String mCountryCode;
    private final FrameworkFacade mFrameworkFacade;
    public final IHwSoftApManagerEx mHwSoftApManagerEx;
    private boolean mIfaceIsDestroyed;
    private boolean mIfaceIsUp;
    private final int mMode;
    private int mNumAssociatedStations = 0;
    private int mReportedBandwidth = -1;
    private int mReportedFrequency = -1;
    private final SarManager mSarManager;
    private final WifiNative.SoftApListener mSoftApListener = new WifiNative.SoftApListener() {
        /* class com.android.server.wifi.SoftApManager.AnonymousClass1 */

        @Override // com.android.server.wifi.WifiNative.SoftApListener
        public void onFailure() {
            SoftApManager.this.mStateMachine.sendMessage(2);
        }

        @Override // com.android.server.wifi.WifiNative.SoftApListener
        public void onNumAssociatedStationsChanged(int numStations) {
            SoftApManager.this.mStateMachine.sendMessage(4, numStations);
        }

        @Override // com.android.server.wifi.WifiNative.SoftApListener
        public void onSoftApChannelSwitched(int frequency, int bandwidth) {
            SoftApManager.this.mStateMachine.sendMessage(9, frequency, bandwidth);
        }

        @Override // com.android.server.wifi.WifiNative.SoftApListener
        public void OnApLinkedStaJoin(String macAddress) {
            Bundle bundle = new Bundle();
            bundle.putString(SoftApManager.AP_LINKED_EVENT_KEY, SoftApManager.STA_JOIN_EVENT);
            bundle.putString(SoftApManager.AP_LINKED_MAC_KEY, macAddress);
            SoftApManager.this.notifyApLinkedStaListChange(bundle);
        }

        @Override // com.android.server.wifi.WifiNative.SoftApListener
        public void OnApLinkedStaLeave(String macAddress) {
            Bundle bundle = new Bundle();
            bundle.putString(SoftApManager.AP_LINKED_EVENT_KEY, SoftApManager.STA_LEAVE_EVENT);
            bundle.putString(SoftApManager.AP_LINKED_MAC_KEY, macAddress);
            SoftApManager.this.notifyApLinkedStaListChange(bundle);
        }
    };
    private long mStartTimestamp = -1;
    private final SoftApStateMachine mStateMachine;
    private boolean mTimeoutEnabled = false;
    private final WifiApConfigStore mWifiApConfigStore;
    private final WifiMetrics mWifiMetrics;
    private final WifiNative mWifiNative;

    public SoftApManager(Context context, Looper looper, FrameworkFacade framework, WifiNative wifiNative, String countryCode, WifiManager.SoftApCallback callback, WifiApConfigStore wifiApConfigStore, SoftApModeConfiguration apConfig, WifiMetrics wifiMetrics, SarManager sarManager) {
        this.mContext = context;
        this.mFrameworkFacade = framework;
        this.mWifiNative = wifiNative;
        this.mCountryCode = countryCode;
        this.mCallback = callback;
        this.mWifiApConfigStore = wifiApConfigStore;
        this.mMode = apConfig.getTargetMode();
        this.mHwSoftApManagerEx = HwWifiServiceFactory.getHwSoftApManagerEx(this, this.mContext);
        WifiConfiguration config = apConfig.getWifiConfiguration();
        if (config == null) {
            this.mApConfig = this.mWifiApConfigStore.getApConfiguration();
        } else {
            this.mApConfig = config;
        }
        this.mWifiMetrics = wifiMetrics;
        this.mSarManager = sarManager;
        this.mStateMachine = new SoftApStateMachine(looper);
    }

    public void setCountryCode(String countryCode) {
        this.mCountryCode = countryCode;
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void start() {
        this.mStateMachine.sendMessage(0, this.mApConfig);
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void stop() {
        Log.i(TAG, " currentstate: " + getCurrentStateName());
        if (this.mApInterfaceName != null) {
            if (this.mIfaceIsUp) {
                updateApState(10, 13, 0);
            } else {
                updateApState(10, 12, 0);
            }
        }
        this.mStateMachine.quitNow();
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public int getScanMode() {
        return 0;
    }

    public int getIpMode() {
        return this.mMode;
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("--Dump of SoftApManager--");
        pw.println("current StateMachine mode: " + getCurrentStateName());
        pw.println("mApInterfaceName: " + this.mApInterfaceName);
        pw.println("mIfaceIsUp: " + this.mIfaceIsUp);
        pw.println("mMode: " + this.mMode);
        pw.println("mCountryCode: " + this.mCountryCode);
        if (this.mApConfig != null) {
            pw.println("mApConfig.SSID: " + this.mApConfig.SSID);
            pw.println("mApConfig.apBand: " + this.mApConfig.apBand);
            pw.println("mApConfig.hiddenSSID: " + this.mApConfig.hiddenSSID);
        } else {
            pw.println("mApConfig: null");
        }
        pw.println("mNumAssociatedStations: " + this.mNumAssociatedStations);
        pw.println("mTimeoutEnabled: " + this.mTimeoutEnabled);
        pw.println("mReportedFrequency: " + this.mReportedFrequency);
        pw.println("mReportedBandwidth: " + this.mReportedBandwidth);
        pw.println("mStartTimestamp: " + this.mStartTimestamp);
    }

    private String getCurrentStateName() {
        IState currentState = this.mStateMachine.getCurrentState();
        if (currentState != null) {
            return currentState.getName();
        }
        return "StateMachine not active";
    }

    /* access modifiers changed from: protected */
    public void updateApState(int newState, int currentState, int reason) {
        this.mCallback.onStateChanged(newState, reason);
        Intent intent = new Intent("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", newState);
        intent.putExtra("previous_wifi_state", currentState);
        if (newState == 14) {
            intent.putExtra("wifi_ap_error_code", reason);
        }
        intent.putExtra("wifi_ap_interface_name", this.mApInterfaceName);
        intent.putExtra("wifi_ap_mode", this.mMode);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int startSoftAp(WifiConfiguration config) {
        if (config == null || config.SSID == null) {
            Log.e(TAG, "Unable to start soft AP without valid configuration");
            return 2;
        }
        if (TextUtils.isEmpty(this.mCountryCode)) {
            if (config.apBand == 1) {
                Log.e(TAG, "Invalid country code, required for setting up soft ap in 5GHz");
                return 2;
            }
        } else if (!this.mWifiNative.setCountryCodeHal(this.mApInterfaceName, this.mCountryCode.toUpperCase(Locale.ROOT)) && config.apBand == 1) {
            Log.e(TAG, "Failed to set country code, required for setting up soft ap in 5GHz");
            return 2;
        }
        WifiConfiguration localConfig = new WifiConfiguration(config);
        int result = updateApChannelConfig(this.mWifiNative, this.mCountryCode, this.mWifiApConfigStore.getAllowed2GChannel(), localConfig);
        if (result != 0) {
            Log.e(TAG, "Failed to update AP band and channel");
            return result;
        }
        if (localConfig.hiddenSSID) {
            Log.i(TAG, "SoftAP is a hidden network");
        }
        localConfig.apChannel = getApChannel(localConfig);
        Log.w(TAG, "startSoftAp apChannel from " + config.apChannel + " to " + localConfig.apChannel);
        if (!this.mWifiNative.startSoftAp(this.mApInterfaceName, localConfig, this.mSoftApListener)) {
            Log.e(TAG, "Soft AP start failed");
            return 2;
        }
        this.mStartTimestamp = SystemClock.elapsedRealtime();
        Log.i(TAG, "Soft AP is started");
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopSoftAp() {
        this.mWifiNative.teardownInterface(this.mApInterfaceName);
        Log.i(TAG, "Soft AP is stopped");
    }

    /* access modifiers changed from: private */
    public class SoftApStateMachine extends StateMachine {
        public static final int CMD_FAILURE = 2;
        public static final int CMD_INTERFACE_DESTROYED = 7;
        public static final int CMD_INTERFACE_DOWN = 8;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_NO_ASSOCIATED_STATIONS_TIMEOUT = 5;
        public static final int CMD_NUM_ASSOCIATED_STATIONS_CHANGED = 4;
        public static final int CMD_SOFT_AP_CHANNEL_SWITCHED = 9;
        public static final int CMD_START = 0;
        public static final int CMD_TIMEOUT_TOGGLE_CHANGED = 6;
        private final State mIdleState = new IdleState();
        private final State mStartedState = new StartedState();
        private final WifiNative.InterfaceCallback mWifiNativeInterfaceCallback = new WifiNative.InterfaceCallback() {
            /* class com.android.server.wifi.SoftApManager.SoftApStateMachine.AnonymousClass1 */

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onDestroyed(String ifaceName) {
                if (SoftApManager.this.mApInterfaceName != null && SoftApManager.this.mApInterfaceName.equals(ifaceName)) {
                    SoftApStateMachine.this.sendMessage(7);
                }
            }

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onUp(String ifaceName) {
                if (SoftApManager.this.mApInterfaceName != null && SoftApManager.this.mApInterfaceName.equals(ifaceName)) {
                    SoftApStateMachine.this.sendMessage(3, 1);
                }
            }

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onDown(String ifaceName) {
                if (SoftApManager.this.mApInterfaceName != null && SoftApManager.this.mApInterfaceName.equals(ifaceName)) {
                    SoftApStateMachine.this.sendMessage(3, 0);
                }
            }
        };

        SoftApStateMachine(Looper looper) {
            super(SoftApManager.TAG, looper);
            addState(this.mIdleState);
            addState(this.mStartedState);
            setInitialState(this.mIdleState);
            start();
        }

        private class IdleState extends State {
            private IdleState() {
            }

            public void enter() {
                SoftApManager.this.mApInterfaceName = null;
                SoftApManager.this.mIfaceIsUp = false;
                SoftApManager.this.mIfaceIsDestroyed = false;
            }

            public boolean processMessage(Message message) {
                SoftApManager.this.mHwSoftApManagerEx.logStateAndMessage(this, message);
                if (message.what == 0) {
                    if (!SoftApManager.this.mHwSoftApManagerEx.checkOpenHotsoptPolicy((WifiConfiguration) message.obj)) {
                        SoftApManager.this.updateApState(14, 12, 0);
                    } else {
                        SoftApManager.this.mApInterfaceName = SoftApManager.this.mWifiNative.setupInterfaceForSoftApMode(SoftApStateMachine.this.mWifiNativeInterfaceCallback);
                        if (TextUtils.isEmpty(SoftApManager.this.mApInterfaceName)) {
                            Log.e(SoftApManager.TAG, "setup failure when creating ap interface.");
                            SoftApManager.this.updateApState(14, 11, 0);
                            SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, 0);
                        } else {
                            SoftApManager.this.updateApState(12, 11, 0);
                            int result = SoftApManager.this.startSoftAp((WifiConfiguration) message.obj);
                            if (result != 0) {
                                int failureReason = 0;
                                if (result == 1) {
                                    failureReason = 1;
                                }
                                SoftApManager.this.updateApState(14, 12, failureReason);
                                SoftApManager.this.stopSoftAp();
                                Log.w(SoftApManager.TAG, "SoftApStateMachine: IdleState: startSoftAp() returns FALSE! update ap state and reason= " + failureReason);
                                SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, failureReason);
                            } else {
                                Log.i(SoftApManager.TAG, "IdleState: startSoftAp() returns TRUE. transition to StartedState");
                                SoftApStateMachine softApStateMachine = SoftApStateMachine.this;
                                softApStateMachine.transitionTo(softApStateMachine.mStartedState);
                            }
                        }
                    }
                }
                return true;
            }
        }

        /* access modifiers changed from: private */
        public class StartedState extends State {
            private boolean isIfaceDestroyed;
            private SoftApTimeoutEnabledSettingObserver mSettingObserver;
            private WakeupMessage mSoftApTimeoutMessage;
            private int mTimeoutDelay;

            private StartedState() {
                this.isIfaceDestroyed = false;
            }

            private class SoftApTimeoutEnabledSettingObserver extends ContentObserver {
                SoftApTimeoutEnabledSettingObserver(Handler handler) {
                    super(handler);
                }

                public void register() {
                    SoftApManager.this.mFrameworkFacade.registerContentObserver(SoftApManager.this.mContext, Settings.Global.getUriFor("soft_ap_timeout_enabled"), true, this);
                    SoftApManager.this.mTimeoutEnabled = getValue();
                }

                public void unregister() {
                    SoftApManager.this.mFrameworkFacade.unregisterContentObserver(SoftApManager.this.mContext, this);
                }

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    SoftApManager.this.mStateMachine.sendMessage(6, getValue() ? 1 : 0);
                }

                private boolean getValue() {
                    boolean enabled = true;
                    if (SoftApManager.this.mFrameworkFacade.getIntegerSetting(SoftApManager.this.mContext, "soft_ap_timeout_enabled", 1) != 1) {
                        enabled = false;
                    }
                    return enabled;
                }
            }

            private int getConfigSoftApTimeoutDelay() {
                int delay = SoftApManager.this.mContext.getResources().getInteger(17694944);
                if (delay < SoftApManager.MIN_SOFT_AP_TIMEOUT_DELAY_MS) {
                    delay = SoftApManager.MIN_SOFT_AP_TIMEOUT_DELAY_MS;
                    Log.w(SoftApManager.TAG, "Overriding timeout delay with minimum limit value");
                }
                Log.d(SoftApManager.TAG, "Timeout delay: " + delay);
                return delay;
            }

            private void scheduleTimeoutMessage() {
                if (SoftApManager.this.mTimeoutEnabled) {
                    this.mSoftApTimeoutMessage.schedule(SystemClock.elapsedRealtime() + ((long) this.mTimeoutDelay));
                    Log.d(SoftApManager.TAG, "Timeout message scheduled");
                }
            }

            private void cancelTimeoutMessage() {
                this.mSoftApTimeoutMessage.cancel();
                Log.d(SoftApManager.TAG, "Timeout message canceled");
            }

            private void setNumAssociatedStations(int numStations) {
                if (SoftApManager.this.mNumAssociatedStations != numStations) {
                    SoftApManager.this.mNumAssociatedStations = numStations;
                    Log.i(SoftApManager.TAG, "Number of associated stations changed: " + SoftApManager.this.mNumAssociatedStations);
                    if (SoftApManager.this.mCallback != null) {
                        SoftApManager.this.mCallback.onNumClientsChanged(SoftApManager.this.mNumAssociatedStations);
                    } else {
                        Log.e(SoftApManager.TAG, "SoftApCallback is null. Dropping NumClientsChanged event.");
                    }
                    SoftApManager.this.mWifiMetrics.addSoftApNumAssociatedStationsChangedEvent(SoftApManager.this.mNumAssociatedStations, SoftApManager.this.mMode);
                    if (SoftApManager.this.mNumAssociatedStations == 0) {
                        scheduleTimeoutMessage();
                    } else {
                        cancelTimeoutMessage();
                    }
                }
            }

            private void onUpChanged(boolean isUp) {
                if (isUp != SoftApManager.this.mIfaceIsUp) {
                    SoftApManager.this.mIfaceIsUp = isUp;
                    if (isUp) {
                        Log.i(SoftApManager.TAG, "SoftAp is ready for use");
                        SoftApManager.this.updateApState(13, 12, 0);
                        SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(true, 0);
                        if (SoftApManager.this.mCallback != null) {
                            SoftApManager.this.mCallback.onNumClientsChanged(SoftApManager.this.mNumAssociatedStations);
                        }
                    } else {
                        SoftApStateMachine.this.sendMessage(8);
                    }
                    SoftApManager.this.mWifiMetrics.addSoftApUpChangedEvent(isUp, SoftApManager.this.mMode);
                }
            }

            public void enter() {
                SoftApManager.this.mIfaceIsUp = false;
                SoftApManager.this.mIfaceIsDestroyed = false;
                onUpChanged(SoftApManager.this.mWifiNative.isInterfaceUp(SoftApManager.this.mApInterfaceName));
                this.mTimeoutDelay = getConfigSoftApTimeoutDelay();
                Handler handler = SoftApManager.this.mStateMachine.getHandler();
                this.mSoftApTimeoutMessage = new WakeupMessage(SoftApManager.this.mContext, handler, SoftApManager.SOFT_AP_SEND_MESSAGE_TIMEOUT_TAG, 5);
                this.mSettingObserver = new SoftApTimeoutEnabledSettingObserver(handler);
                SoftApTimeoutEnabledSettingObserver softApTimeoutEnabledSettingObserver = this.mSettingObserver;
                SoftApManager.this.mSarManager.setSapWifiState(13);
                Log.i(SoftApManager.TAG, "Resetting num stations on start");
                SoftApManager.this.mNumAssociatedStations = 0;
                scheduleTimeoutMessage();
            }

            public void exit() {
                if (!SoftApManager.this.mIfaceIsDestroyed) {
                    SoftApManager.this.stopSoftAp();
                }
                SoftApTimeoutEnabledSettingObserver softApTimeoutEnabledSettingObserver = this.mSettingObserver;
                Log.i(SoftApManager.TAG, "Resetting num stations on stop");
                setNumAssociatedStations(0);
                cancelTimeoutMessage();
                SoftApManager.this.mWifiMetrics.addSoftApUpChangedEvent(false, SoftApManager.this.mMode);
                SoftApManager.this.updateApState(11, 10, 0);
                SoftApManager.this.mSarManager.setSapWifiState(11);
                SoftApManager.this.mApInterfaceName = null;
                SoftApManager.this.mIfaceIsUp = false;
                SoftApManager.this.mIfaceIsDestroyed = false;
                SoftApManager.this.mStateMachine.quitNow();
                this.isIfaceDestroyed = false;
            }

            private void updateUserBandPreferenceViolationMetricsIfNeeded() {
                boolean bandPreferenceViolated = false;
                if (SoftApManager.this.mApConfig.apBand == 0 && ScanResult.is5GHz(SoftApManager.this.mReportedFrequency)) {
                    bandPreferenceViolated = true;
                } else if (SoftApManager.this.mApConfig.apBand == 1 && ScanResult.is24GHz(SoftApManager.this.mReportedFrequency)) {
                    bandPreferenceViolated = true;
                }
                if (bandPreferenceViolated) {
                    Log.e(SoftApManager.TAG, "Channel does not satisfy user band preference: " + SoftApManager.this.mReportedFrequency);
                    SoftApManager.this.mWifiMetrics.incrementNumSoftApUserBandPreferenceUnsatisfied();
                }
            }

            /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
            public boolean processMessage(Message message) {
                SoftApManager.this.mHwSoftApManagerEx.logStateAndMessage(this, message);
                int i = message.what;
                if (i != 0) {
                    boolean isEnabled = false;
                    switch (i) {
                        case 2:
                            Log.w(SoftApManager.TAG, "hostapd failure, stop and report failure");
                            Log.w(SoftApManager.TAG, "interface error, stop and report failure");
                            SoftApManager.this.updateApState(14, 13, 0);
                            SoftApManager.this.updateApState(10, 14, 0);
                            SoftApStateMachine softApStateMachine = SoftApStateMachine.this;
                            softApStateMachine.transitionTo(softApStateMachine.mIdleState);
                            break;
                        case 3:
                            if (message.arg1 == 1) {
                                isEnabled = true;
                            }
                            onUpChanged(isEnabled);
                            break;
                        case 4:
                            if (message.arg1 >= 0) {
                                Log.i(SoftApManager.TAG, "Setting num stations on CMD_NUM_ASSOCIATED_STATIONS_CHANGED");
                                setNumAssociatedStations(message.arg1);
                                break;
                            } else {
                                Log.e(SoftApManager.TAG, "Invalid number of associated stations: " + message.arg1);
                                break;
                            }
                        case 5:
                            if (SoftApManager.this.mTimeoutEnabled) {
                                if (SoftApManager.this.mNumAssociatedStations == 0) {
                                    Log.i(SoftApManager.TAG, "Timeout message received. Stopping soft AP.");
                                    SoftApManager.this.updateApState(10, 13, 0);
                                    SoftApStateMachine softApStateMachine2 = SoftApStateMachine.this;
                                    softApStateMachine2.transitionTo(softApStateMachine2.mIdleState);
                                    break;
                                } else {
                                    Log.wtf(SoftApManager.TAG, "Timeout message received but has clients. Dropping.");
                                    break;
                                }
                            } else {
                                Log.wtf(SoftApManager.TAG, "Timeout message received while timeout is disabled. Dropping.");
                                break;
                            }
                        case 6:
                            if (message.arg1 == 1) {
                                isEnabled = true;
                            }
                            if (SoftApManager.this.mTimeoutEnabled != isEnabled) {
                                SoftApManager.this.mTimeoutEnabled = isEnabled;
                                if (!SoftApManager.this.mTimeoutEnabled) {
                                    cancelTimeoutMessage();
                                }
                                if (SoftApManager.this.mTimeoutEnabled && SoftApManager.this.mNumAssociatedStations == 0) {
                                    scheduleTimeoutMessage();
                                    break;
                                }
                            }
                            break;
                        case 7:
                            Log.d(SoftApManager.TAG, "Interface was cleanly destroyed.");
                            SoftApManager.this.updateApState(10, 13, 0);
                            SoftApManager.this.mIfaceIsDestroyed = true;
                            SoftApStateMachine softApStateMachine3 = SoftApStateMachine.this;
                            softApStateMachine3.transitionTo(softApStateMachine3.mIdleState);
                            break;
                        case 8:
                            Log.w(SoftApManager.TAG, "interface error, stop and report failure");
                            SoftApManager.this.updateApState(14, 13, 0);
                            SoftApManager.this.updateApState(10, 14, 0);
                            SoftApStateMachine softApStateMachine4 = SoftApStateMachine.this;
                            softApStateMachine4.transitionTo(softApStateMachine4.mIdleState);
                            break;
                        case 9:
                            SoftApManager.this.mReportedFrequency = message.arg1;
                            SoftApManager.this.mReportedBandwidth = message.arg2;
                            Log.i(SoftApManager.TAG, "Channel switched. Frequency: " + SoftApManager.this.mReportedFrequency + " Bandwidth: " + SoftApManager.this.mReportedBandwidth);
                            SoftApManager.this.mWifiMetrics.addSoftApChannelSwitchedEvent(SoftApManager.this.mReportedFrequency, SoftApManager.this.mReportedBandwidth, SoftApManager.this.mMode);
                            updateUserBandPreferenceViolationMetricsIfNeeded();
                            break;
                        default:
                            return false;
                    }
                }
                return true;
            }
        }
    }

    @Override // com.android.server.wifi.IHwSoftApManagerInner
    public int getCmd1() {
        return 0;
    }

    @Override // com.android.server.wifi.IHwSoftApManagerInner
    public int getCmd2() {
        return 3;
    }
}
