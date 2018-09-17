package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.wifi.WifiConfiguration;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.util.ApConfigUtil;
import com.google.protobuf.nano.Extension;
import java.util.ArrayList;
import java.util.Locale;

public class SoftApManager extends AbsSoftApManager {
    private static final String TAG = "SoftApManager";
    private final ArrayList<Integer> mAllowed2GChannels;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private String mCountryCode;
    private final String mInterfaceName;
    private final Listener mListener;
    private final INetworkManagementService mNmService;
    private final SoftApStateMachine mStateMachine;
    private String mTetherInterfaceName;
    private final WifiNative mWifiNative;

    public interface Listener {
        void onStateChanged(int i, int i2);
    }

    private class SoftApStateMachine extends StateMachine {
        public static final int CMD_START = 0;
        public static final int CMD_STOP = 1;
        public static final int CMD_TETHER_NOTIFICATION_TIMEOUT = 3;
        public static final int CMD_TETHER_STATE_CHANGE = 2;
        private static final int TETHER_NOTIFICATION_TIME_OUT_MSECS = 5000;
        private final State mIdleState;
        private final State mStartedState;
        private int mTetherToken;
        private final State mTetheredState;
        private final State mTetheringState;
        private final State mUntetheringState;

        private class IdleState extends State {
            private IdleState() {
            }

            public boolean processMessage(Message message) {
                SoftApManager.this.logStateAndMessage(this, message);
                switch (message.what) {
                    case SoftApStateMachine.CMD_START /*0*/:
                        SoftApManager.this.updateApState(12, SoftApStateMachine.CMD_START);
                        int result = SoftApManager.this.startSoftAp((WifiConfiguration) message.obj);
                        if (result != 0) {
                            int reason = SoftApStateMachine.CMD_START;
                            if (result == SoftApStateMachine.CMD_STOP) {
                                reason = SoftApStateMachine.CMD_STOP;
                            }
                            Log.w(SoftApManager.TAG, "SoftApStateMachine: IdleState: startSoftAp() returns FALSE! update ap state and reason= " + reason);
                            SoftApManager.this.updateApState(14, reason);
                            break;
                        }
                        Log.d(SoftApManager.TAG, "SoftApStateMachine: IdleState: startSoftAp() returns TRUE. update ap state and transition to StartedState");
                        SoftApManager.this.updateApState(13, SoftApStateMachine.CMD_START);
                        SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mStartedState);
                        break;
                    case SoftApStateMachine.CMD_TETHER_STATE_CHANGE /*2*/:
                        TetherStateChange stateChange = message.obj;
                        String str = SoftApManager.TAG;
                        Object[] objArr = new Object[SoftApStateMachine.CMD_TETHER_STATE_CHANGE];
                        objArr[SoftApStateMachine.CMD_START] = TextUtils.join(",", stateChange.available);
                        objArr[SoftApStateMachine.CMD_STOP] = TextUtils.join(",", stateChange.active);
                        Log.d(str, String.format("handle message: CMD_TETHER_STATE_CHANGE: avail=[%s] active=[%s]", objArr));
                        if (SoftApManager.this.isWifiTethered(stateChange.available)) {
                            Log.d(SoftApManager.TAG, "need to up " + stateChange.available + ", defer this message.");
                            SoftApStateMachine.this.deferMessage(Message.obtain(message));
                            break;
                        }
                        break;
                }
                return true;
            }
        }

        private class StartedState extends State {
            private StartedState() {
            }

            public boolean processMessage(Message message) {
                SoftApManager.this.logStateAndMessage(this, message);
                switch (message.what) {
                    case SoftApStateMachine.CMD_START /*0*/:
                        break;
                    case SoftApStateMachine.CMD_STOP /*1*/:
                        SoftApManager.this.updateApState(10, SoftApStateMachine.CMD_START);
                        SoftApManager.this.stopSoftAp();
                        SoftApManager.this.updateApState(11, SoftApStateMachine.CMD_START);
                        SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mIdleState);
                        break;
                    case SoftApStateMachine.CMD_TETHER_STATE_CHANGE /*2*/:
                        if (!SoftApManager.this.startTethering(message.obj.available)) {
                            Log.w(SoftApManager.TAG, "SoftApStateMachine: StartedState: startTethering() returns FALSE!");
                            break;
                        }
                        Log.d(SoftApManager.TAG, "SoftApStateMachine: StartedState: startTethering() returns TRUE. transition to TetheringState");
                        SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mTetheringState);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        private class TetheredState extends State {
            private TetheredState() {
            }

            public boolean processMessage(Message message) {
                SoftApManager.this.logStateAndMessage(this, message);
                switch (message.what) {
                    case SoftApStateMachine.CMD_STOP /*1*/:
                        Log.d(SoftApManager.TAG, "Untethering before stopping AP");
                        SoftApManager.this.stopTethering();
                        SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mUntetheringState);
                        break;
                    case SoftApStateMachine.CMD_TETHER_STATE_CHANGE /*2*/:
                        if (!SoftApManager.this.isWifiTethered(message.obj.active)) {
                            Log.e(SoftApManager.TAG, "Tethering reports wifi as untethered!, shut down soft Ap");
                            SoftApStateMachine.this.sendMessage(SoftApStateMachine.CMD_STOP);
                            break;
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        private class TetheringState extends State {
            private TetheringState() {
            }

            public void enter() {
                SoftApStateMachine softApStateMachine = SoftApStateMachine.this;
                SoftApStateMachine softApStateMachine2 = SoftApStateMachine.this;
                SoftApStateMachine softApStateMachine3 = SoftApStateMachine.this;
                softApStateMachine.sendMessageDelayed(softApStateMachine2.obtainMessage(SoftApStateMachine.CMD_TETHER_NOTIFICATION_TIMEOUT, softApStateMachine3.mTetherToken = softApStateMachine3.mTetherToken + SoftApStateMachine.CMD_STOP), 5000);
            }

            public boolean processMessage(Message message) {
                SoftApManager.this.logStateAndMessage(this, message);
                switch (message.what) {
                    case SoftApStateMachine.CMD_STOP /*1*/:
                        SoftApStateMachine.this.deferMessage(message);
                        break;
                    case SoftApStateMachine.CMD_TETHER_STATE_CHANGE /*2*/:
                        if (!SoftApManager.this.isWifiTethered(message.obj.active)) {
                            Log.w(SoftApManager.TAG, "SoftApStateMachine: TetheringState: isWifiTethered() returns FALSE!");
                            break;
                        }
                        Log.d(SoftApManager.TAG, "SoftApStateMachine: TetheringState: isWifiTethered() returns TRUE. transition to TetheredState");
                        SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mTetheredState);
                        break;
                    case SoftApStateMachine.CMD_TETHER_NOTIFICATION_TIMEOUT /*3*/:
                        if (message.arg1 == SoftApStateMachine.this.mTetherToken) {
                            Log.e(SoftApManager.TAG, "Failed to get tether update, shutdown soft access point");
                            SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mStartedState);
                            SoftApStateMachine.this.sendMessageAtFrontOfQueue(SoftApStateMachine.CMD_STOP);
                            break;
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        private class UntetheringState extends State {
            private UntetheringState() {
            }

            public void enter() {
                SoftApStateMachine softApStateMachine = SoftApStateMachine.this;
                SoftApStateMachine softApStateMachine2 = SoftApStateMachine.this;
                SoftApStateMachine softApStateMachine3 = SoftApStateMachine.this;
                softApStateMachine.sendMessageDelayed(softApStateMachine2.obtainMessage(SoftApStateMachine.CMD_TETHER_NOTIFICATION_TIMEOUT, softApStateMachine3.mTetherToken = softApStateMachine3.mTetherToken + SoftApStateMachine.CMD_STOP), 5000);
            }

            public boolean processMessage(Message message) {
                SoftApManager.this.logStateAndMessage(this, message);
                switch (message.what) {
                    case SoftApStateMachine.CMD_TETHER_STATE_CHANGE /*2*/:
                        if (!SoftApManager.this.isWifiTethered(message.obj.active)) {
                            SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mStartedState);
                            SoftApStateMachine.this.sendMessageAtFrontOfQueue(SoftApStateMachine.CMD_STOP);
                            break;
                        }
                        break;
                    case SoftApStateMachine.CMD_TETHER_NOTIFICATION_TIMEOUT /*3*/:
                        if (message.arg1 == SoftApStateMachine.this.mTetherToken) {
                            Log.e(SoftApManager.TAG, "Failed to get tether update, force stop access point");
                            SoftApStateMachine.this.transitionTo(SoftApStateMachine.this.mStartedState);
                            SoftApStateMachine.this.sendMessageAtFrontOfQueue(SoftApStateMachine.CMD_STOP);
                            break;
                        }
                        break;
                    default:
                        SoftApStateMachine.this.deferMessage(message);
                        break;
                }
                return true;
            }
        }

        SoftApStateMachine(Looper looper) {
            super(SoftApManager.TAG, looper);
            this.mTetherToken = CMD_START;
            this.mIdleState = new IdleState();
            this.mStartedState = new StartedState();
            this.mTetheringState = new TetheringState();
            this.mTetheredState = new TetheredState();
            this.mUntetheringState = new UntetheringState();
            addState(this.mIdleState);
            addState(this.mStartedState, this.mIdleState);
            addState(this.mTetheringState, this.mStartedState);
            addState(this.mTetheredState, this.mStartedState);
            addState(this.mUntetheringState, this.mStartedState);
            setInitialState(this.mIdleState);
            start();
        }
    }

    private static class TetherStateChange {
        public ArrayList<String> active;
        public ArrayList<String> available;

        TetherStateChange(ArrayList<String> av, ArrayList<String> ac) {
            this.available = av;
            this.active = ac;
        }
    }

    public SoftApManager(Context context, Looper looper, WifiNative wifiNative, INetworkManagementService nmService, ConnectivityManager connectivityManager, String countryCode, ArrayList<Integer> allowed2GChannels, Listener listener) {
        this.mStateMachine = new SoftApStateMachine(looper);
        this.mContext = context;
        this.mNmService = nmService;
        this.mWifiNative = wifiNative;
        this.mConnectivityManager = connectivityManager;
        this.mCountryCode = countryCode;
        this.mAllowed2GChannels = allowed2GChannels;
        this.mListener = listener;
        this.mInterfaceName = this.mWifiNative.getInterfaceName();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d(SoftApManager.TAG, "onReceive: ConnectivityManager.ACTION_TETHER_STATE_CHANGED: broadcastId= " + intent.getLongExtra("broadcastId", 0));
                SoftApManager.this.mStateMachine.sendMessage(2, new TetherStateChange(intent.getStringArrayListExtra("availableArray"), intent.getStringArrayListExtra("activeArray")));
            }
        }, new IntentFilter("android.net.conn.TETHER_STATE_CHANGED"));
    }

    public void setCountryCode(String countryCode) {
        Log.d(TAG, "setCountryCode: " + countryCode);
        this.mCountryCode = countryCode;
    }

    private void logStateAndMessage(State state, Message message) {
        String str;
        switch (message.what) {
            case ApConfigUtil.SUCCESS /*0*/:
                str = "CMD_START";
                break;
            case Extension.TYPE_DOUBLE /*1*/:
                str = "CMD_STOP";
                break;
            case Extension.TYPE_FLOAT /*2*/:
                str = "CMD_TETHER_STATE_CHANGE";
                break;
            case Extension.TYPE_INT64 /*3*/:
                str = "CMD_TETHER_NOTIFICATION_TIMEOUT";
                break;
            default:
                str = "what:" + Integer.toString(message.what);
                break;
        }
        Log.d(TAG, state.getClass().getSimpleName() + ": handle message: " + str);
    }

    public void start(WifiConfiguration config) {
        this.mStateMachine.sendMessage(0, config);
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
        if (config == null) {
            Log.e(TAG, "Unable to start soft AP without configuration");
            return 2;
        }
        WifiConfiguration localConfig = new WifiConfiguration(config);
        int result = updateApChannelConfig(this.mWifiNative, this.mCountryCode, this.mAllowed2GChannels, localConfig);
        if (result != 0) {
            Log.e(TAG, "Failed to update AP band and channel");
            return result;
        } else if (this.mCountryCode == null || this.mWifiNative.setCountryCodeHal(this.mCountryCode.toUpperCase(Locale.ROOT)) || config.apBand != 1) {
            try {
                this.mNmService.startAccessPoint(localConfig, this.mInterfaceName);
                Log.d(TAG, "Soft AP is started");
                return 0;
            } catch (Exception e) {
                Log.e(TAG, "Exception in starting soft AP: " + e);
                return 2;
            }
        } else {
            Log.e(TAG, "Failed to set country code, required for setting up soft ap in 5GHz");
            return 2;
        }
    }

    private void stopSoftAp() {
        try {
            this.mNmService.stopAccessPoint(this.mInterfaceName);
            Log.d(TAG, "Soft AP is stopped");
        } catch (Exception e) {
            Log.e(TAG, "Exception in stopping soft AP: " + e);
        }
    }

    private boolean startTethering(ArrayList<String> available) {
        String[] wifiRegexs = this.mConnectivityManager.getTetherableWifiRegexs();
        if (wifiRegexs.length == 0) {
            Log.e(TAG, "startTethering: wifiRegexs == NULL !");
        } else if (available.size() == 0) {
            Log.e(TAG, "startTethering: available == NULL !");
        }
        for (String intf : available) {
            for (String regex : wifiRegexs) {
                if (intf.matches(regex)) {
                    try {
                        InterfaceConfiguration ifcg = this.mNmService.getInterfaceConfig(intf);
                        if (ifcg != null) {
                            ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress("192.168.43.1"), 24));
                            ifcg.setInterfaceUp();
                            Log.d(TAG, "startTethering: getInterfaceConfig() succeed. now setInterfaceConfig().");
                            this.mNmService.setInterfaceConfig(intf, ifcg);
                        } else {
                            Log.e(TAG, "startTethering: getInterfaceConfig() returns NULL !");
                        }
                        Log.d(TAG, "startTethering: call ConnectivityManager.tether()");
                        if (this.mConnectivityManager.tether(intf) != 0) {
                            Log.e(TAG, "Error tethering on " + intf);
                            return false;
                        }
                        this.mTetherInterfaceName = intf;
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error configuring interface " + intf + ", :" + e);
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private void stopTethering() {
        Log.d(TAG, "stopTethering.");
        try {
            InterfaceConfiguration ifcg = this.mNmService.getInterfaceConfig(this.mTetherInterfaceName);
            if (ifcg != null) {
                ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress("0.0.0.0"), 0));
                this.mNmService.setInterfaceConfig(this.mTetherInterfaceName, ifcg);
            } else {
                Log.e(TAG, "stopTethering: getInterfaceConfig() returns NULL !");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resetting interface " + this.mTetherInterfaceName + ", :" + e);
        }
        if (this.mConnectivityManager.untether(this.mTetherInterfaceName) != 0) {
            Log.e(TAG, "Untether initiate failed!");
        }
    }

    private boolean isWifiTethered(ArrayList<String> active) {
        try {
            String[] wifiRegexs = this.mConnectivityManager.getTetherableWifiRegexs();
            for (String intf : active) {
                for (String regex : wifiRegexs) {
                    if (intf.matches(regex)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }
}
