package com.huawei.wifi2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArraySet;
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.huawei.wifi2.HwWifi2ClientModeManager;
import java.util.Iterator;

public class HwWifi2ActiveModeWarden {
    static final int BASE = 131072;
    static final int CMD_CLIENT_MODE_FAILED = 131074;
    static final int CMD_CLIENT_MODE_STOPPED = 131073;
    private static final String TAG = "HwWifi2ActiveModeWarden";
    private HwWifi2ClientModeManager.Listener mClientModeCallback;
    private final ArraySet<HwWifi2ClientModeManager> mClientModeManagers = new ArraySet<>();
    private final Context mContext;
    private final Handler mHandler;
    private final Looper mLooper;
    private ModeStateMachine mModeStateMachine = new ModeStateMachine();
    private final HwWifi2Injector mWifiInjector;
    private final HwWifi2Native mWifiNative;

    HwWifi2ActiveModeWarden(HwWifi2Injector wifiInjector, Context context, Looper looper, HwWifi2Native wifiNative) {
        this.mWifiInjector = wifiInjector;
        this.mContext = context;
        this.mLooper = looper;
        this.mHandler = new Handler(looper);
        this.mWifiNative = wifiNative;
    }

    public void enterClientMode() {
        changeMode(0);
    }

    public void disableWifi() {
        changeMode(1);
    }

    public void shutdownWifi() {
        this.mHandler.post(new Runnable() {
            /* class com.huawei.wifi2.$$Lambda$HwWifi2ActiveModeWarden$lt6ExgncTk9GKksVggt8UYFO1c */

            @Override // java.lang.Runnable
            public final void run() {
                HwWifi2ActiveModeWarden.this.lambda$shutdownWifi$0$HwWifi2ActiveModeWarden();
            }
        });
    }

    public /* synthetic */ void lambda$shutdownWifi$0$HwWifi2ActiveModeWarden() {
        Iterator<HwWifi2ClientModeManager> it = this.mClientModeManagers.iterator();
        while (it.hasNext()) {
            it.next().stop();
        }
    }

    public void registerClientModeCallback(HwWifi2ClientModeManager.Listener callback) {
        this.mClientModeCallback = callback;
    }

    private void changeMode(int newMode) {
        this.mModeStateMachine.sendMessage(newMode);
    }

    /* access modifiers changed from: private */
    public class ModeStateMachine extends StateMachine {
        public static final int CMD_DISABLE_WIFI = 1;
        public static final int CMD_START_CLIENT_MODE = 0;
        private final State mClientModeActiveState = new ClientModeActiveState();
        private final State mWifiDisabledState = new WifiDisabledState();

        ModeStateMachine() {
            super(HwWifi2ActiveModeWarden.TAG, HwWifi2ActiveModeWarden.this.mLooper);
            addState(this.mClientModeActiveState);
            addState(this.mWifiDisabledState);
            setInitialState(this.mWifiDisabledState);
            start();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String msgToString(int what) {
            if (what == 0) {
                return "CMD_START_CLIENT_MODE";
            }
            if (what != 1) {
                return "UNKNOWN_MSG";
            }
            return "CMD_DISABLE_WIFI";
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String wifiStateToString(int wifiState) {
            if (wifiState == 0) {
                return "WIFI_STATE_DISABLING";
            }
            if (wifiState == 1) {
                return "WIFI_STATE_DISABLED";
            }
            if (wifiState == 2) {
                return "WIFI_STATE_ENABLING";
            }
            if (wifiState == 3) {
                return "WIFI_STATE_ENABLED";
            }
            if (wifiState != 4) {
                return "UNKNOWN_MSG";
            }
            return "WIFI_STATE_UNKNOWN";
        }

        private String getCurrentMode() {
            return getCurrentState().getName();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean checkForAndHandleModeChange(Message message) {
            int i = message.what;
            if (i == 0) {
                HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "Switching from %{public}s to ClientMode", new Object[]{getCurrentMode()});
                HwWifi2ActiveModeWarden.this.mModeStateMachine.transitionTo(this.mClientModeActiveState);
            } else if (i != 1) {
                return false;
            } else {
                HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "Switching from %{public}s to WifiDisabled", new Object[]{getCurrentMode()});
                HwWifi2ActiveModeWarden.this.mModeStateMachine.transitionTo(this.mWifiDisabledState);
            }
            return true;
        }

        class ModeActiveState extends State {
            HwWifi2ClientModeManager mManager;

            ModeActiveState() {
            }

            public boolean processMessage(Message message) {
                return true;
            }

            public void exit() {
                HwWifi2ClientModeManager hwWifi2ClientModeManager = this.mManager;
                if (hwWifi2ClientModeManager != null) {
                    hwWifi2ClientModeManager.stop();
                    HwWifi2ActiveModeWarden.this.mClientModeManagers.remove(this.mManager);
                }
            }
        }

        class WifiDisabledState extends ModeActiveState {
            WifiDisabledState() {
                super();
            }

            public void enter() {
                HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "Entering WifiDisabledState", new Object[0]);
            }

            @Override // com.huawei.wifi2.HwWifi2ActiveModeWarden.ModeStateMachine.ModeActiveState
            public boolean processMessage(Message message) {
                HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "received a message in WifiDisabledState: %{public}s", new Object[]{ModeStateMachine.this.msgToString(message.what)});
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                return false;
            }
        }

        class ClientModeActiveState extends ModeActiveState {
            ClientListener mListener;

            ClientModeActiveState() {
                super();
            }

            private class ClientListener implements HwWifi2ClientModeManager.Listener {
                private ClientListener() {
                }

                @Override // com.huawei.wifi2.HwWifi2ClientModeManager.Listener
                public void onStateChanged(int state) {
                    if (this != ClientModeActiveState.this.mListener) {
                        HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "Client mode state change from previous manager", new Object[0]);
                        return;
                    }
                    HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "State changed from client mode. state =  %{public}s", new Object[]{ModeStateMachine.this.wifiStateToString(state)});
                    HwWifi2ActiveModeWarden.this.mClientModeCallback.onStateChanged(state);
                    if (state == 4) {
                        HwWifi2ActiveModeWarden.this.mModeStateMachine.sendMessage(HwWifi2ActiveModeWarden.CMD_CLIENT_MODE_FAILED, this);
                    } else if (state == 1) {
                        HwWifi2ActiveModeWarden.this.mModeStateMachine.sendMessage(HwWifi2ActiveModeWarden.CMD_CLIENT_MODE_STOPPED, this);
                    } else {
                        HwHiLog.d(HwWifi2ActiveModeWarden.TAG, false, "client mode active", new Object[0]);
                    }
                }
            }

            public void enter() {
                HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "Entering ClientModeActiveState", new Object[0]);
                this.mListener = new ClientListener();
                this.mManager = HwWifi2ActiveModeWarden.this.mWifiInjector.makeClientModeManager(this.mListener);
                this.mManager.start();
                HwWifi2ActiveModeWarden.this.mClientModeManagers.add(this.mManager);
            }

            @Override // com.huawei.wifi2.HwWifi2ActiveModeWarden.ModeStateMachine.ModeActiveState
            public boolean processMessage(Message message) {
                if (ModeStateMachine.this.checkForAndHandleModeChange(message)) {
                    return true;
                }
                int i = message.what;
                if (i != 0) {
                    switch (i) {
                        case HwWifi2ActiveModeWarden.CMD_CLIENT_MODE_STOPPED /* 131073 */:
                            if (this.mListener == message.obj) {
                                HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "ClientMode stopped, return to WifiDisabledState.", new Object[0]);
                                HwWifi2ActiveModeWarden.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mWifiDisabledState);
                                break;
                            } else {
                                HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "Client mode state change from previous manager", new Object[0]);
                                return true;
                            }
                        case HwWifi2ActiveModeWarden.CMD_CLIENT_MODE_FAILED /* 131074 */:
                            if (this.mListener == message.obj) {
                                HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "ClientMode failed, return to WifiDisabledState.", new Object[0]);
                                HwWifi2ActiveModeWarden.this.mModeStateMachine.transitionTo(ModeStateMachine.this.mWifiDisabledState);
                                break;
                            } else {
                                HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "Client mode state change from previous manager", new Object[0]);
                                return true;
                            }
                        default:
                            return false;
                    }
                } else {
                    HwHiLog.i(HwWifi2ActiveModeWarden.TAG, false, "Received CMD_START_CLIENT_MODE when active - drop", new Object[0]);
                }
                return false;
            }

            @Override // com.huawei.wifi2.HwWifi2ActiveModeWarden.ModeStateMachine.ModeActiveState
            public void exit() {
                super.exit();
                this.mListener = null;
            }
        }
    }
}
