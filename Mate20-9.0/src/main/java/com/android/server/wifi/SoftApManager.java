package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
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
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.wifi.WifiNative;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

public class SoftApManager extends AbsSoftApManager implements ActiveModeManager {
    public static final String AP_LINKED_EVENT_KEY = "event_key";
    public static final String AP_LINKED_MAC_KEY = "mac_key";
    private static final int MIN_SOFT_AP_TIMEOUT_DELAY_MS = 600000;
    @VisibleForTesting
    public static final String SOFT_AP_SEND_MESSAGE_TIMEOUT_TAG = "SoftApManager Soft AP Send Message Timeout";
    public static final String STA_JOIN_EVENT = "STA_JOIN";
    public static final String STA_LEAVE_EVENT = "STA_LEAVE";
    private static final String TAG = "SoftApManager";
    protected WifiConfiguration mApConfig;
    /* access modifiers changed from: private */
    public String mApInterfaceName;
    /* access modifiers changed from: private */
    public final WifiManager.SoftApCallback mCallback;
    /* access modifiers changed from: private */
    public final Context mContext;
    private String mCountryCode;
    /* access modifiers changed from: private */
    public final FrameworkFacade mFrameworkFacade;
    /* access modifiers changed from: private */
    public boolean mIfaceIsUp;
    /* access modifiers changed from: private */
    public final int mMode;
    /* access modifiers changed from: private */
    public int mNumAssociatedStations = 0;
    /* access modifiers changed from: private */
    public int mReportedBandwidth = -1;
    /* access modifiers changed from: private */
    public int mReportedFrequency = -1;
    private final WifiNative.SoftApListener mSoftApListener = new WifiNative.SoftApListener() {
        public void onNumAssociatedStationsChanged(int numStations) {
            SoftApManager.this.mStateMachine.sendMessage(4, numStations);
        }

        public void onSoftApChannelSwitched(int frequency, int bandwidth) {
            SoftApManager.this.mStateMachine.sendMessage(9, frequency, bandwidth);
        }

        public void OnApLinkedStaJoin(String macAddress) {
            Bundle bundle = new Bundle();
            bundle.putString(SoftApManager.AP_LINKED_EVENT_KEY, SoftApManager.STA_JOIN_EVENT);
            bundle.putString(SoftApManager.AP_LINKED_MAC_KEY, macAddress);
            SoftApManager.this.notifyApLinkedStaListChange(bundle);
        }

        public void OnApLinkedStaLeave(String macAddress) {
            Bundle bundle = new Bundle();
            bundle.putString(SoftApManager.AP_LINKED_EVENT_KEY, SoftApManager.STA_LEAVE_EVENT);
            bundle.putString(SoftApManager.AP_LINKED_MAC_KEY, macAddress);
            SoftApManager.this.notifyApLinkedStaListChange(bundle);
        }
    };
    /* access modifiers changed from: private */
    public final SoftApStateMachine mStateMachine;
    /* access modifiers changed from: private */
    public boolean mTimeoutEnabled = false;
    private final WifiApConfigStore mWifiApConfigStore;
    /* access modifiers changed from: private */
    public final WifiMetrics mWifiMetrics;
    /* access modifiers changed from: private */
    public final WifiNative mWifiNative;

    private class SoftApStateMachine extends StateMachine {
        public static final int CMD_INTERFACE_DESTROYED = 7;
        public static final int CMD_INTERFACE_DOWN = 8;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_NO_ASSOCIATED_STATIONS_TIMEOUT = 5;
        public static final int CMD_NUM_ASSOCIATED_STATIONS_CHANGED = 4;
        public static final int CMD_SOFT_AP_CHANNEL_SWITCHED = 9;
        public static final int CMD_START = 0;
        public static final int CMD_TIMEOUT_TOGGLE_CHANGED = 6;
        /* access modifiers changed from: private */
        public final State mIdleState = new IdleState();
        /* access modifiers changed from: private */
        public final State mStartedState = new StartedState();
        /* access modifiers changed from: private */
        public final WifiNative.InterfaceCallback mWifiNativeInterfaceCallback = new WifiNative.InterfaceCallback() {
            public void onDestroyed(String ifaceName) {
                if (SoftApManager.this.mApInterfaceName != null && SoftApManager.this.mApInterfaceName.equals(ifaceName)) {
                    SoftApStateMachine.this.sendMessage(7);
                }
            }

            public void onUp(String ifaceName) {
                if (SoftApManager.this.mApInterfaceName != null && SoftApManager.this.mApInterfaceName.equals(ifaceName)) {
                    SoftApStateMachine.this.sendMessage(3, 1);
                }
            }

            public void onDown(String ifaceName) {
                if (SoftApManager.this.mApInterfaceName != null && SoftApManager.this.mApInterfaceName.equals(ifaceName)) {
                    SoftApStateMachine.this.sendMessage(3, 0);
                }
            }
        };

        private class IdleState extends State {
            private static final String POLICY_OPEN_HOTSPOT = "policy-open-hotspot";
            private static final String VALUE_DISABLE = "value_disable";

            private IdleState() {
            }

            public void enter() {
                String unused = SoftApManager.this.mApInterfaceName = null;
                boolean unused2 = SoftApManager.this.mIfaceIsUp = false;
            }

            public boolean processMessage(Message message) {
                SoftApManager.this.logStateAndMessage(this, message);
                if (message.what == 0) {
                    if (!checkOpenHotsoptPolicy((WifiConfiguration) message.obj)) {
                        SoftApManager.this.updateApState(14, 12, 0);
                    } else {
                        String unused = SoftApManager.this.mApInterfaceName = SoftApManager.this.mWifiNative.setupInterfaceForSoftApMode(SoftApStateMachine.this.mWifiNativeInterfaceCallback);
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
                                Log.d(SoftApManager.TAG, "SoftApStateMachine: IdleState: startSoftAp() returns TRUE. update ap state and transition to StartedState");
                                SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mStartedState);
                            }
                        }
                    }
                }
                return true;
            }

            private boolean checkOpenHotsoptPolicy(WifiConfiguration apConfig) {
                Bundle bundle = HwWifiServiceFactory.getHwDevicePolicyManager().getPolicy(null, POLICY_OPEN_HOTSPOT);
                if (bundle == null || ((apConfig != null && apConfig.preSharedKey != null) || !bundle.getBoolean(VALUE_DISABLE))) {
                    return true;
                }
                Log.w(SoftApManager.TAG, "SoftApState: MDM deny start unsecure soft ap!");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        Toast.makeText(SoftApManager.this.mContext, SoftApManager.this.mContext.getString(33685942), 0).show();
                    }
                });
                return false;
            }
        }

        private class StartedState extends State {
            private SoftApTimeoutEnabledSettingObserver mSettingObserver;
            private WakeupMessage mSoftApTimeoutMessage;
            private int mTimeoutDelay;

            private class SoftApTimeoutEnabledSettingObserver extends ContentObserver {
                SoftApTimeoutEnabledSettingObserver(Handler handler) {
                    super(handler);
                }

                public void register() {
                    SoftApManager.this.mFrameworkFacade.registerContentObserver(SoftApManager.this.mContext, Settings.Global.getUriFor("soft_ap_timeout_enabled"), true, this);
                    boolean unused = SoftApManager.this.mTimeoutEnabled = getValue();
                }

                public void unregister() {
                    SoftApManager.this.mFrameworkFacade.unregisterContentObserver(SoftApManager.this.mContext, this);
                }

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

            private StartedState() {
            }

            private int getConfigSoftApTimeoutDelay() {
                int delay = SoftApManager.this.mContext.getResources().getInteger(17694911);
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
                    int unused = SoftApManager.this.mNumAssociatedStations = numStations;
                    Log.d(SoftApManager.TAG, "Number of associated stations changed: " + SoftApManager.this.mNumAssociatedStations);
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
                    boolean unused = SoftApManager.this.mIfaceIsUp = isUp;
                    if (isUp) {
                        Log.d(SoftApManager.TAG, "SoftAp is ready for use");
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
                boolean unused = SoftApManager.this.mIfaceIsUp = false;
                onUpChanged(SoftApManager.this.mWifiNative.isInterfaceUp(SoftApManager.this.mApInterfaceName));
                this.mTimeoutDelay = getConfigSoftApTimeoutDelay();
                Handler handler = SoftApManager.this.mStateMachine.getHandler();
                this.mSoftApTimeoutMessage = new WakeupMessage(SoftApManager.this.mContext, handler, SoftApManager.SOFT_AP_SEND_MESSAGE_TIMEOUT_TAG, 5);
                this.mSettingObserver = new SoftApTimeoutEnabledSettingObserver(handler);
                SoftApTimeoutEnabledSettingObserver softApTimeoutEnabledSettingObserver = this.mSettingObserver;
                Log.d(SoftApManager.TAG, "Resetting num stations on start");
                int unused2 = SoftApManager.this.mNumAssociatedStations = 0;
                scheduleTimeoutMessage();
            }

            public void exit() {
                if (SoftApManager.this.mApInterfaceName != null) {
                    SoftApManager.this.stopSoftAp();
                }
                SoftApTimeoutEnabledSettingObserver softApTimeoutEnabledSettingObserver = this.mSettingObserver;
                Log.d(SoftApManager.TAG, "Resetting num stations on stop");
                int unused = SoftApManager.this.mNumAssociatedStations = 0;
                cancelTimeoutMessage();
                SoftApManager.this.mWifiMetrics.addSoftApUpChangedEvent(false, SoftApManager.this.mMode);
                SoftApManager.this.updateApState(11, 10, 0);
                String unused2 = SoftApManager.this.mApInterfaceName = null;
                boolean unused3 = SoftApManager.this.mIfaceIsUp = false;
                SoftApManager.this.mStateMachine.quitNow();
            }

            public boolean processMessage(Message message) {
                SoftApManager.this.logStateAndMessage(this, message);
                int i = message.what;
                if (i != 0) {
                    boolean isUp = false;
                    switch (i) {
                        case 3:
                            if (message.arg1 == 1) {
                                isUp = true;
                            }
                            onUpChanged(isUp);
                            break;
                        case 4:
                            if (message.arg1 >= 0) {
                                Log.d(SoftApManager.TAG, "Setting num stations on CMD_NUM_ASSOCIATED_STATIONS_CHANGED");
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
                                    SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mIdleState);
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
                                isUp = true;
                            }
                            boolean isEnabled = isUp;
                            if (SoftApManager.this.mTimeoutEnabled != isEnabled) {
                                boolean unused = SoftApManager.this.mTimeoutEnabled = isEnabled;
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
                            String unused2 = SoftApManager.this.mApInterfaceName = null;
                            SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mIdleState);
                            break;
                        case 8:
                            Log.w(SoftApManager.TAG, "interface error, stop and report failure");
                            SoftApManager.this.updateApState(14, 13, 0);
                            SoftApManager.this.updateApState(10, 14, 0);
                            SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mIdleState);
                            break;
                        case 9:
                            int unused3 = SoftApManager.this.mReportedFrequency = message.arg1;
                            int unused4 = SoftApManager.this.mReportedBandwidth = message.arg2;
                            Log.d(SoftApManager.TAG, "Channel switched. Frequency: " + SoftApManager.this.mReportedFrequency + " Bandwidth: " + SoftApManager.this.mReportedBandwidth);
                            SoftApManager.this.mWifiMetrics.addSoftApChannelSwitchedEvent(SoftApManager.this.mReportedFrequency, SoftApManager.this.mReportedBandwidth, SoftApManager.this.mMode);
                            int[] allowedChannels = new int[0];
                            if (SoftApManager.this.mApConfig.apBand == 0) {
                                allowedChannels = SoftApManager.this.mWifiNative.getChannelsForBand(1);
                            } else if (SoftApManager.this.mApConfig.apBand == 1) {
                                allowedChannels = SoftApManager.this.mWifiNative.getChannelsForBand(2);
                            } else if (SoftApManager.this.mApConfig.apBand == -1) {
                                allowedChannels = Stream.concat(Arrays.stream(SoftApManager.this.mWifiNative.getChannelsForBand(1)).boxed(), Arrays.stream(SoftApManager.this.mWifiNative.getChannelsForBand(2)).boxed()).mapToInt($$Lambda$SoftApManager$SoftApStateMachine$StartedState$gfCssnBJI7TKfXb_Jmv7raVYNkY.INSTANCE).toArray();
                            }
                            if (!ArrayUtils.contains(allowedChannels, SoftApManager.this.mReportedFrequency)) {
                                Log.e(SoftApManager.TAG, "Channel does not satisfy user band preference: " + SoftApManager.this.mReportedFrequency);
                                SoftApManager.this.mWifiMetrics.incrementNumSoftApUserBandPreferenceUnsatisfied();
                                break;
                            }
                            break;
                        default:
                            return false;
                    }
                }
                return true;
            }
        }

        SoftApStateMachine(Looper looper) {
            super(SoftApManager.TAG, looper);
            addState(this.mIdleState);
            addState(this.mStartedState);
            setInitialState(this.mIdleState);
            start();
        }
    }

    public SoftApManager(Context context, Looper looper, FrameworkFacade framework, WifiNative wifiNative, String countryCode, WifiManager.SoftApCallback callback, WifiApConfigStore wifiApConfigStore, SoftApModeConfiguration apConfig, WifiMetrics wifiMetrics) {
        this.mContext = context;
        this.mFrameworkFacade = framework;
        this.mWifiNative = wifiNative;
        this.mCountryCode = countryCode;
        this.mCallback = callback;
        this.mWifiApConfigStore = wifiApConfigStore;
        this.mMode = apConfig.getTargetMode();
        WifiConfiguration config = apConfig.getWifiConfiguration();
        if (config == null) {
            this.mApConfig = this.mWifiApConfigStore.getApConfiguration();
        } else {
            this.mApConfig = config;
        }
        this.mWifiMetrics = wifiMetrics;
        this.mStateMachine = new SoftApStateMachine(looper);
    }

    public void setCountryCode(String countryCode) {
        Log.d(TAG, "setCountryCode");
        this.mCountryCode = countryCode;
    }

    /* access modifiers changed from: private */
    public void logStateAndMessage(State state, Message message) {
        String str;
        int i = message.what;
        if (i == 0) {
            str = "CMD_START";
        } else if (i != 3) {
            str = "what:" + Integer.toString(message.what);
        } else {
            str = "CMD_INTERFACE_STATUS_CHANGED";
        }
        Log.d(TAG, state.getClass().getSimpleName() + ": handle message: " + str);
    }

    public void start() {
        this.mStateMachine.sendMessage(0, this.mApConfig);
    }

    public void stop() {
        Log.d(TAG, " currentstate: " + getCurrentStateName());
        if (this.mApInterfaceName != null) {
            if (this.mIfaceIsUp) {
                updateApState(10, 13, 0);
            } else {
                updateApState(10, 12, 0);
            }
        }
        this.mStateMachine.quitNow();
    }

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
    public int startSoftAp(WifiConfiguration config) {
        if (config == null || config.SSID == null) {
            Log.e(TAG, "Unable to start soft AP without valid configuration");
            return 2;
        }
        WifiConfiguration localConfig = new WifiConfiguration(config);
        int result = updateApChannelConfig(this.mWifiNative, this.mCountryCode, this.mWifiApConfigStore.getAllowed2GChannel(), localConfig);
        if (result != 0) {
            Log.e(TAG, "Failed to update AP band and channel");
            return result;
        } else if (this.mCountryCode == null || this.mWifiNative.setCountryCodeHal(this.mApInterfaceName, this.mCountryCode.toUpperCase(Locale.ROOT)) || config.apBand != 1) {
            if (localConfig.hiddenSSID) {
                Log.d(TAG, "SoftAP is a hidden network");
            }
            localConfig.apChannel = getApChannel(localConfig);
            Log.w(TAG, "startSoftAp apChannel from " + config.apChannel + " to " + localConfig.apChannel);
            if (!this.mWifiNative.startSoftAp(this.mApInterfaceName, localConfig, this.mSoftApListener)) {
                Log.e(TAG, "Soft AP start failed");
                return 2;
            }
            Log.d(TAG, "Soft AP is started");
            return 0;
        } else {
            Log.e(TAG, "Failed to set country code, required for setting up soft ap in 5GHz");
            return 2;
        }
    }

    /* access modifiers changed from: private */
    public void stopSoftAp() {
        this.mWifiNative.teardownInterface(this.mApInterfaceName);
        Log.d(TAG, "Soft AP is stopped");
    }
}
