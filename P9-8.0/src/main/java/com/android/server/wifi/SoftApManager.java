package com.android.server.wifi;

import android.content.Context;
import android.net.InterfaceConfiguration;
import android.net.wifi.IApInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.net.BaseNetworkObserver;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class SoftApManager extends AbsSoftApManager implements ActiveModeManager {
    static final int RESTART_AP_LATENCY = 1000;
    private static final String TAG = "SoftApManager";
    private WifiConfiguration mApConfig;
    private final IApInterface mApInterface;
    private Context mContext = null;
    private String mCountryCode;
    private final Listener mListener;
    private final INetworkManagementService mNwService;
    private final SoftApStateMachine mStateMachine;
    private final WifiApConfigStore mWifiApConfigStore;
    private final WifiMetrics mWifiMetrics;
    private final WifiNative mWifiNative;

    public interface Listener {
        void onStateChanged(int i, int i2);
    }

    private class SoftApStateMachine extends StateMachine {
        public static final int CMD_AP_INTERFACE_BINDER_DEATH = 2;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_START = 0;
        public static final int CMD_STOP = 1;
        private final StateMachineDeathRecipient mDeathRecipient = new StateMachineDeathRecipient(this, 2);
        private final State mIdleState = new IdleState(this, null);
        private NetworkObserver mNetworkObserver;
        private final State mStartedState = new StartedState(this, null);

        private class IdleState extends State {
            /* synthetic */ IdleState(SoftApStateMachine this$1, IdleState -this1) {
                this();
            }

            private IdleState() {
            }

            public void enter() {
                SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                unregisterObserver();
            }

            public boolean processMessage(Message message) {
                SoftApManager.this.logStateAndMessage(this, message);
                switch (message.what) {
                    case 0:
                        SoftApManager.this.updateApState(12, 0);
                        if (!SoftApStateMachine.this.mDeathRecipient.linkToDeath(SoftApManager.this.mApInterface.asBinder())) {
                            SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                            SoftApManager.this.updateApState(14, 0);
                            SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, 0);
                            break;
                        }
                        try {
                            SoftApStateMachine.this.mNetworkObserver = new NetworkObserver(SoftApManager.this.mApInterface.getInterfaceName());
                            SoftApManager.this.mNwService.registerObserver(SoftApStateMachine.this.mNetworkObserver);
                            int result = SoftApManager.this.startSoftAp((WifiConfiguration) message.obj);
                            if (result == 0) {
                                Log.d(SoftApManager.TAG, "SoftApStateMachine: IdleState: startSoftAp() returns TRUE. update ap state and transition to StartedState");
                                SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mStartedState);
                                break;
                            }
                            int failureReason = 0;
                            if (result == 1) {
                                failureReason = 1;
                            }
                            SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                            unregisterObserver();
                            Log.w(SoftApManager.TAG, "SoftApStateMachine: IdleState: startSoftAp() returns FALSE! update ap state and reason= " + failureReason);
                            SoftApManager.this.updateApState(14, failureReason);
                            SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, failureReason);
                            break;
                        } catch (RemoteException e) {
                            SoftApStateMachine.this.mDeathRecipient.unlinkToDeath();
                            unregisterObserver();
                            SoftApManager.this.updateApState(14, 0);
                            SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(false, 0);
                            break;
                        }
                }
                return true;
            }

            private void unregisterObserver() {
                if (SoftApStateMachine.this.mNetworkObserver != null) {
                    try {
                        SoftApManager.this.mNwService.unregisterObserver(SoftApStateMachine.this.mNetworkObserver);
                    } catch (RemoteException e) {
                        Log.e(SoftApManager.TAG, "RemoteException in unregisterObserver: " + e.getMessage());
                    }
                    SoftApStateMachine.this.mNetworkObserver = null;
                }
            }
        }

        private class NetworkObserver extends BaseNetworkObserver {
            private final String mIfaceName;

            NetworkObserver(String ifaceName) {
                this.mIfaceName = ifaceName;
            }

            public void interfaceLinkStateChanged(String iface, boolean up) {
                if (this.mIfaceName.equals(iface)) {
                    int i;
                    SoftApStateMachine softApStateMachine = SoftApStateMachine.this;
                    if (up) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    softApStateMachine.sendMessage(3, i, 0, this);
                }
            }
        }

        private class StartedState extends State {
            private boolean mIfaceIsUp;

            /* synthetic */ StartedState(SoftApStateMachine this$1, StartedState -this1) {
                this();
            }

            private StartedState() {
            }

            private void onUpChanged(boolean isUp) {
                if (isUp != this.mIfaceIsUp) {
                    this.mIfaceIsUp = isUp;
                    if (isUp) {
                        Log.d(SoftApManager.TAG, "SoftAp is ready for use");
                        SoftApManager.this.updateApState(13, 0);
                        SoftApManager.this.mWifiMetrics.incrementSoftApStartResult(true, 0);
                    } else {
                        Log.d(SoftApManager.TAG, "SoftAp interface down, close AP and restart");
                        SoftApManager.this.stop();
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                if (SoftApManager.this.mContext != null) {
                                    ((WifiManager) SoftApManager.this.mContext.getSystemService("wifi")).startSoftAp(SoftApManager.this.mApConfig);
                                }
                            }
                        }, 1000);
                    }
                }
            }

            public void enter() {
                this.mIfaceIsUp = false;
                InterfaceConfiguration config = null;
                try {
                    config = SoftApManager.this.mNwService.getInterfaceConfig(SoftApManager.this.mApInterface.getInterfaceName());
                } catch (RemoteException e) {
                }
                if (config != null) {
                    onUpChanged(config.isUp());
                }
            }

            public boolean processMessage(Message message) {
                SoftApManager.this.logStateAndMessage(this, message);
                switch (message.what) {
                    case 0:
                        break;
                    case 1:
                    case 2:
                        SoftApManager.this.updateApState(10, 0);
                        SoftApManager.this.stopSoftAp();
                        if (message.what == 2) {
                            SoftApManager.this.updateApState(14, 0);
                        } else {
                            SoftApManager.this.updateApState(11, 0);
                        }
                        SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mIdleState);
                        break;
                    case 3:
                        if (message.obj == SoftApStateMachine.this.mNetworkObserver) {
                            onUpChanged(message.arg1 == 1);
                            break;
                        }
                        break;
                    default:
                        return false;
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

    public SoftApManager(Looper looper, WifiNative wifiNative, String countryCode, Listener listener, IApInterface apInterface, INetworkManagementService nms, WifiApConfigStore wifiApConfigStore, WifiConfiguration config, WifiMetrics wifiMetrics) {
        this.mStateMachine = new SoftApStateMachine(looper);
        this.mWifiNative = wifiNative;
        this.mCountryCode = countryCode;
        this.mListener = listener;
        this.mApInterface = apInterface;
        this.mNwService = nms;
        this.mWifiApConfigStore = wifiApConfigStore;
        if (config == null) {
            this.mApConfig = this.mWifiApConfigStore.getApConfiguration();
        } else {
            this.mApConfig = config;
        }
        this.mWifiMetrics = wifiMetrics;
    }

    public void setCountryCode(String countryCode) {
        Log.d(TAG, "setCountryCode: " + countryCode);
        this.mCountryCode = countryCode;
    }

    private void logStateAndMessage(State state, Message message) {
        String str;
        switch (message.what) {
            case 0:
                str = "CMD_START";
                break;
            case 1:
                str = "CMD_STOP";
                break;
            case 2:
                str = "CMD_AP_INTERFACE_BINDER_DEATH";
                break;
            case 3:
                str = "CMD_INTERFACE_STATUS_CHANGED";
                break;
            default:
                str = "what:" + Integer.toString(message.what);
                break;
        }
        Log.d(TAG, state.getClass().getSimpleName() + ": handle message: " + str);
    }

    public void start() {
        if (this.mContext == null) {
            this.mContext = getContext();
        }
        this.mStateMachine.sendMessage(0, this.mApConfig);
    }

    public void stop() {
        this.mStateMachine.sendMessage(1);
    }

    private void updateApState(int state, int reason) {
        if (this.mListener != null) {
            this.mListener.onStateChanged(state, reason);
        }
    }

    private int startSoftAp(WifiConfiguration config) {
        if (config == null || config.SSID == null) {
            Log.e(TAG, "Unable to start soft AP without valid configuration");
            return 2;
        }
        WifiConfiguration localConfig = new WifiConfiguration(config);
        int result = updateApChannelConfig(this.mWifiNative, this.mCountryCode, this.mWifiApConfigStore.getAllowed2GChannel(), localConfig);
        if (result != 0) {
            Log.e(TAG, "Failed to update AP band and channel");
            return result;
        } else if (this.mCountryCode == null || this.mWifiNative.setCountryCodeHal(this.mCountryCode.toUpperCase(Locale.ROOT)) || config.apBand != 1) {
            int encryptionType = getIApInterfaceEncryptionType(localConfig);
            localConfig.apChannel = getApChannel(localConfig);
            Log.w(TAG, "startSoftAp apChannel from " + config.apChannel + " to " + localConfig.apChannel);
            try {
                byte[] bytes;
                IApInterface iApInterface = this.mApInterface;
                byte[] bytes2 = localConfig.SSID.getBytes(StandardCharsets.UTF_8);
                boolean isHideBroadcastSsid = isHideBroadcastSsid();
                int i = localConfig.apChannel;
                if (localConfig.preSharedKey != null) {
                    bytes = localConfig.preSharedKey.getBytes(StandardCharsets.UTF_8);
                } else {
                    bytes = new byte[0];
                }
                if (iApInterface.writeHostapdConfig(bytes2, isHideBroadcastSsid, i, encryptionType, bytes)) {
                    if (!this.mApInterface.startHostapd()) {
                        Log.e(TAG, "Failed to start hostapd.");
                        return 2;
                    }
                    Log.d(TAG, "Soft AP is started");
                    return 0;
                }
                Log.e(TAG, "Failed to write hostapd configuration");
                return 2;
            } catch (RemoteException e) {
                Log.e(TAG, "Exception in starting soft AP: " + e);
            }
        } else {
            Log.e(TAG, "Failed to set country code, required for setting up soft ap in 5GHz");
            return 2;
        }
    }

    private static int getIApInterfaceEncryptionType(WifiConfiguration localConfig) {
        switch (localConfig.getAuthType()) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 4:
                return 2;
            default:
                return 0;
        }
    }

    private void stopSoftAp() {
        try {
            this.mApInterface.stopHostapd();
            Log.d(TAG, "Soft AP is stopped");
        } catch (RemoteException e) {
            Log.e(TAG, "Exception in stopping soft AP: " + e);
        }
    }

    public void clearCallbacksAndMessages() {
    }
}
