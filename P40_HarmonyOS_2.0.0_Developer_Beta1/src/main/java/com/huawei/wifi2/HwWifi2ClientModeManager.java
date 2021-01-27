package com.huawei.wifi2;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.huawei.wifi2.HwWifi2Native;

public class HwWifi2ClientModeManager {
    private static final String TAG = "HwWifi2ClientModeManager";
    private String mClientInterfaceName;
    private final HwWifi2ClientModeImpl mClientModeImpl;
    private final Context mContext;
    private boolean mIsExpectedStop = false;
    private boolean mIsIfaceUp = false;
    private final Listener mListener;
    private final ClientModeStateMachine mStateMachine;
    private final HwWifi2Native mWifiNative;

    public interface Listener {
        void onStateChanged(int i);
    }

    HwWifi2ClientModeManager(Context context, Looper looper, HwWifi2Native wifiNative, Listener listener, HwWifi2ClientModeImpl clientModeImpl) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mListener = listener;
        this.mClientModeImpl = clientModeImpl;
        this.mStateMachine = new ClientModeStateMachine(looper);
    }

    public void start() {
        this.mStateMachine.sendMessage(0);
    }

    public void stop() {
        HwHiLog.i(TAG, false, " currentstate: %{public}s", new Object[]{getCurrentStateName()});
        this.mIsExpectedStop = true;
        if (this.mClientInterfaceName != null) {
            if (this.mIsIfaceUp) {
                updateWifiState(0, 3);
            } else {
                updateWifiState(0, 2);
            }
        }
        this.mStateMachine.quitNow();
    }

    private String getCurrentStateName() {
        IState currentState = this.mStateMachine.getCurrentState();
        if (currentState != null) {
            return currentState.getName();
        }
        return "StateMachine not active";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateWifiState(int newState, int currentState) {
        if (!this.mIsExpectedStop) {
            this.mListener.onStateChanged(newState);
        } else {
            HwHiLog.i(TAG, false, "expected stop, not triggering callbacks: newState = %{public}d", new Object[]{Integer.valueOf(newState)});
        }
        if (newState == 4 || newState == 1) {
            this.mIsExpectedStop = true;
        }
        if (newState != 4) {
            Intent intent = new Intent("huawei.net.slave_wifi.WIFI_STATE_CHANGED");
            intent.addFlags(67108864);
            intent.putExtra("wifi_state", newState);
            intent.putExtra("previous_wifi_state", currentState);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: private */
    public class ClientModeStateMachine extends StateMachine {
        public static final int CMD_INTERFACE_DESTROYED = 2;
        public static final int CMD_INTERFACE_DOWN = 3;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 1;
        public static final int CMD_START = 0;
        private final State mIdleState = new IdleState();
        private final State mStartedState = new StartedState();
        private final HwWifi2Native.InterfaceCallback mWifiNativeInterfaceCallback = new HwWifi2Native.InterfaceCallback() {
            /* class com.huawei.wifi2.HwWifi2ClientModeManager.ClientModeStateMachine.AnonymousClass1 */

            @Override // com.huawei.wifi2.HwWifi2Native.InterfaceCallback
            public void onDestroyed(String ifaceName) {
                if (HwWifi2ClientModeManager.this.mClientInterfaceName != null && HwWifi2ClientModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "STA iface %{public}s was destroyed, stopping client mode", new Object[]{ifaceName});
                    HwWifi2ClientModeManager.this.mClientModeImpl.handleIfaceDestroyed();
                    ClientModeStateMachine.this.sendMessage(2);
                }
            }

            @Override // com.huawei.wifi2.HwWifi2Native.InterfaceCallback
            public void onUp(String ifaceName) {
                if (HwWifi2ClientModeManager.this.mClientInterfaceName != null && HwWifi2ClientModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    ClientModeStateMachine.this.sendMessage(1, 1);
                }
            }

            @Override // com.huawei.wifi2.HwWifi2Native.InterfaceCallback
            public void onDown(String ifaceName) {
                if (HwWifi2ClientModeManager.this.mClientInterfaceName != null && HwWifi2ClientModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    ClientModeStateMachine.this.sendMessage(1, 0);
                }
            }
        };

        ClientModeStateMachine(Looper looper) {
            super(HwWifi2ClientModeManager.TAG, looper);
            addState(this.mIdleState);
            addState(this.mStartedState);
            setInitialState(this.mIdleState);
            start();
        }

        private class IdleState extends State {
            private IdleState() {
            }

            public void enter() {
                HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "entering IdleState", new Object[0]);
                HwWifi2ClientModeManager.this.mClientInterfaceName = null;
                HwWifi2ClientModeManager.this.mIsIfaceUp = false;
            }

            public boolean processMessage(Message message) {
                HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "received a message in IdleState: %{public}d", new Object[]{Integer.valueOf(message.what)});
                if (message.what != 0) {
                    HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "received invalid message: %{public}s", new Object[]{message});
                    return false;
                }
                HwWifi2ClientModeManager.this.updateWifiState(2, 1);
                HwWifi2ClientModeManager.this.mClientInterfaceName = HwWifi2ClientModeManager.this.mWifiNative.setupInterfaceForClientInConnectivityMode(ClientModeStateMachine.this.mWifiNativeInterfaceCallback);
                if (TextUtils.isEmpty(HwWifi2ClientModeManager.this.mClientInterfaceName)) {
                    HwHiLog.e(HwWifi2ClientModeManager.TAG, false, "Failed to create ClientInterface. Sit in Idle", new Object[0]);
                    HwWifi2ClientModeManager.this.updateWifiState(4, 2);
                    HwWifi2ClientModeManager.this.updateWifiState(1, 4);
                } else {
                    ClientModeStateMachine clientModeStateMachine = ClientModeStateMachine.this;
                    clientModeStateMachine.transitionTo(clientModeStateMachine.mStartedState);
                }
                return true;
            }
        }

        private class StartedState extends State {
            private StartedState() {
            }

            private void onUpChanged(boolean isUp) {
                if (isUp != HwWifi2ClientModeManager.this.mIsIfaceUp) {
                    HwWifi2ClientModeManager.this.mIsIfaceUp = isUp;
                    if (isUp) {
                        HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "Wifi is ready to use for client mode", new Object[0]);
                        HwWifi2ClientModeManager.this.updateWifiState(3, 2);
                        HwWifi2ClientModeManager.this.mClientModeImpl.setOperationalMode(1, HwWifi2ClientModeManager.this.mClientInterfaceName);
                        return;
                    }
                    HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "interface down!", new Object[0]);
                    HwWifi2ClientModeManager.this.updateWifiState(4, 3);
                    HwWifi2ClientModeManager.this.mStateMachine.sendMessage(3);
                }
            }

            public void enter() {
                HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "entering StartedState", new Object[0]);
                HwWifi2ClientModeManager.this.mIsIfaceUp = false;
                onUpChanged(HwWifi2ClientModeManager.this.mWifiNative.isInterfaceUp(HwWifi2ClientModeManager.this.mClientInterfaceName));
            }

            public boolean processMessage(Message message) {
                boolean isUp = true;
                HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "received message in StartedState: %{public}s", new Object[]{message});
                int i = message.what;
                if (i != 0) {
                    if (i == 1) {
                        if (message.arg1 != 1) {
                            isUp = false;
                        }
                        onUpChanged(isUp);
                    } else if (i == 2) {
                        HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "interface destroyed - client mode stopping", new Object[0]);
                        HwWifi2ClientModeManager.this.updateWifiState(0, 3);
                        HwWifi2ClientModeManager.this.mClientInterfaceName = null;
                        ClientModeStateMachine clientModeStateMachine = ClientModeStateMachine.this;
                        clientModeStateMachine.transitionTo(clientModeStateMachine.mIdleState);
                    } else if (i != 3) {
                        HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "message will handle in parent state", new Object[0]);
                        return false;
                    } else {
                        HwHiLog.i(HwWifi2ClientModeManager.TAG, false, "Detected an interface down, reporting failure to SelfRecovery", new Object[0]);
                        HwWifi2ClientModeManager.this.updateWifiState(0, 4);
                        ClientModeStateMachine clientModeStateMachine2 = ClientModeStateMachine.this;
                        clientModeStateMachine2.transitionTo(clientModeStateMachine2.mIdleState);
                    }
                }
                return true;
            }

            public void exit() {
                HwWifi2ClientModeManager.this.mClientModeImpl.setOperationalMode(4, null);
                if (HwWifi2ClientModeManager.this.mClientInterfaceName != null) {
                    HwWifi2ClientModeManager.this.mWifiNative.teardownInterface(HwWifi2ClientModeManager.this.mClientInterfaceName);
                    HwWifi2ClientModeManager.this.mClientInterfaceName = null;
                    HwWifi2ClientModeManager.this.mIsIfaceUp = false;
                }
                HwWifi2ClientModeManager.this.updateWifiState(1, 0);
                HwWifi2ClientModeManager.this.mStateMachine.quitNow();
            }
        }
    }
}
