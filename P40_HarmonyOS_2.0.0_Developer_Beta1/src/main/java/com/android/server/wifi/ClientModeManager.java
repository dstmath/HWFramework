package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ClientModeManager implements ActiveModeManager {
    private static final String TAG = "WifiClientModeManager";
    private String mClientInterfaceName;
    private final ClientModeImpl mClientModeImpl;
    private final Context mContext;
    private boolean mExpectedStop = false;
    private boolean mIfaceIsUp = false;
    private final Listener mListener;
    private final ClientModeStateMachine mStateMachine;
    private final WifiMetrics mWifiMetrics;
    private final WifiNative mWifiNative;

    public interface Listener {
        void onStateChanged(int i);
    }

    ClientModeManager(Context context, Looper looper, WifiNative wifiNative, Listener listener, WifiMetrics wifiMetrics, ClientModeImpl clientModeImpl) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mListener = listener;
        this.mWifiMetrics = wifiMetrics;
        this.mClientModeImpl = clientModeImpl;
        this.mStateMachine = new ClientModeStateMachine(looper);
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void start() {
        this.mStateMachine.sendMessage(0);
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void stop() {
        Log.i(TAG, " currentstate: " + getCurrentStateName());
        this.mExpectedStop = true;
        if (this.mClientInterfaceName != null) {
            if (this.mIfaceIsUp) {
                updateWifiState(0, 3);
            } else {
                updateWifiState(0, 2);
            }
        }
        this.mStateMachine.quitNow();
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public int getScanMode() {
        return 2;
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("--Dump of ClientModeManager--");
        pw.println("current StateMachine mode: " + getCurrentStateName());
        pw.println("mClientInterfaceName: " + this.mClientInterfaceName);
        pw.println("mIfaceIsUp: " + this.mIfaceIsUp);
    }

    private String getCurrentStateName() {
        IState currentState = this.mStateMachine.getCurrentState();
        if (currentState != null) {
            return currentState.getName();
        }
        return "StateMachine not active";
    }

    private void sendThreeStateBroadcast(int wifiState, int threeState) {
        int realThreeState = threeState;
        if (threeState == 0) {
            if (wifiState == 1) {
                realThreeState = 0;
            } else if (wifiState == 3) {
                realThreeState = 1;
            } else {
                return;
            }
        } else if (wifiState != 3) {
            return;
        }
        Intent intent = new Intent(WifiCommonUtils.THREE_STATE_ACTION);
        intent.addFlags(67108864);
        intent.putExtra(WifiCommonUtils.EXTRA_THREE_STATE, realThreeState);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WifiCommonUtils.HW_SYSTEM_PERMISSION);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateWifiState(int newState, int currentState) {
        if (!this.mExpectedStop) {
            this.mListener.onStateChanged(newState);
        } else {
            Log.i(TAG, "expected stop, not triggering callbacks: newState = " + newState);
        }
        if (newState == 4 || newState == 1) {
            this.mExpectedStop = true;
        }
        if (newState != 4) {
            this.mClientModeImpl.setWifiStateForHw(newState);
            this.mClientModeImpl.setWifiStateForApiCalls(newState);
            if (this.mClientModeImpl.checkSelfCureWifiResult(101)) {
                Log.i(TAG, "updateWifiState, ignore to send intent due to wifi self curing.");
            } else if ((this.mClientModeImpl.getWifiMode() & 8) == 0) {
                Intent intent = new Intent("android.net.wifi.WIFI_STATE_CHANGED");
                intent.addFlags(67108864);
                intent.putExtra("wifi_state", newState);
                intent.putExtra("previous_wifi_state", currentState);
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                sendThreeStateBroadcast(newState, 0);
            } else {
                sendThreeStateBroadcast(newState, 2);
            }
        }
    }

    /* access modifiers changed from: private */
    public class ClientModeStateMachine extends StateMachine {
        public static final int CMD_INTERFACE_DESTROYED = 4;
        public static final int CMD_INTERFACE_DOWN = 5;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_START = 0;
        private final State mIdleState = new IdleState();
        private final State mStartedState = new StartedState();
        private final WifiNative.InterfaceCallback mWifiNativeInterfaceCallback = new WifiNative.InterfaceCallback() {
            /* class com.android.server.wifi.ClientModeManager.ClientModeStateMachine.AnonymousClass1 */

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onDestroyed(String ifaceName) {
                if (ClientModeManager.this.mClientInterfaceName != null && ClientModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    Log.i(ClientModeManager.TAG, "STA iface " + ifaceName + " was destroyed, stopping client mode");
                    ClientModeManager.this.mClientModeImpl.handleIfaceDestroyed();
                    ClientModeStateMachine.this.sendMessage(4);
                }
            }

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onUp(String ifaceName) {
                if (ClientModeManager.this.mClientInterfaceName != null && ClientModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    ClientModeStateMachine.this.sendMessage(3, 1);
                }
            }

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onDown(String ifaceName) {
                if (ClientModeManager.this.mClientInterfaceName != null && ClientModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    ClientModeStateMachine.this.sendMessage(3, 0);
                }
            }
        };

        ClientModeStateMachine(Looper looper) {
            super(ClientModeManager.TAG, looper);
            addState(this.mIdleState);
            addState(this.mStartedState);
            setInitialState(this.mIdleState);
            start();
        }

        private class IdleState extends State {
            private IdleState() {
            }

            public void enter() {
                Log.i(ClientModeManager.TAG, "entering IdleState");
                ClientModeManager.this.mClientInterfaceName = null;
                ClientModeManager.this.mIfaceIsUp = false;
                ClientModeManager.this.mClientModeImpl.initialFeatureSet(0);
            }

            public boolean processMessage(Message message) {
                if (message.what != 0) {
                    Log.i(ClientModeManager.TAG, "received an invalid message: " + message);
                    return false;
                }
                ClientModeManager.this.updateWifiState(2, 1);
                ClientModeManager.this.mClientInterfaceName = ClientModeManager.this.mWifiNative.setupInterfaceForClientInConnectivityMode(ClientModeStateMachine.this.mWifiNativeInterfaceCallback);
                if (TextUtils.isEmpty(ClientModeManager.this.mClientInterfaceName)) {
                    Log.e(ClientModeManager.TAG, "Failed to create ClientInterface. Sit in Idle");
                    ClientModeManager.this.updateWifiState(4, 2);
                    ClientModeManager.this.updateWifiState(1, 4);
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
                if (isUp != ClientModeManager.this.mIfaceIsUp) {
                    ClientModeManager.this.mIfaceIsUp = isUp;
                    if (isUp) {
                        Log.i(ClientModeManager.TAG, "Wifi is ready to use for client mode");
                        ClientModeManager.this.mClientModeImpl.setOperationalMode(1, ClientModeManager.this.mClientInterfaceName);
                        ClientModeManager.this.updateWifiState(3, 2);
                    } else if (!ClientModeManager.this.mClientModeImpl.isConnectedMacRandomizationEnabled()) {
                        Log.i(ClientModeManager.TAG, "interface down!");
                        ClientModeManager.this.updateWifiState(4, 3);
                        ClientModeManager.this.mStateMachine.sendMessage(5);
                    }
                }
            }

            public void enter() {
                Log.i(ClientModeManager.TAG, "entering StartedState");
                ClientModeManager.this.mIfaceIsUp = false;
                onUpChanged(ClientModeManager.this.mWifiNative.isInterfaceUp(ClientModeManager.this.mClientInterfaceName));
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i != 0) {
                    boolean isUp = false;
                    if (i == 3) {
                        if (message.arg1 == 1) {
                            isUp = true;
                        }
                        ClientModeManager.this.mClientModeImpl.loadAndEnableAllNetworksByMode();
                        onUpChanged(isUp);
                    } else if (i == 4) {
                        Log.i(ClientModeManager.TAG, "interface destroyed - client mode stopping");
                        ClientModeManager.this.updateWifiState(0, 3);
                        ClientModeManager.this.mClientInterfaceName = null;
                        ClientModeStateMachine clientModeStateMachine = ClientModeStateMachine.this;
                        clientModeStateMachine.transitionTo(clientModeStateMachine.mIdleState);
                    } else if (i != 5) {
                        return false;
                    } else {
                        Log.e(ClientModeManager.TAG, "Detected an interface down, reporting failure to SelfRecovery");
                        ClientModeManager.this.mClientModeImpl.failureDetected(2);
                        ClientModeManager.this.updateWifiState(0, 4);
                        ClientModeStateMachine clientModeStateMachine2 = ClientModeStateMachine.this;
                        clientModeStateMachine2.transitionTo(clientModeStateMachine2.mIdleState);
                    }
                }
                return true;
            }

            public void exit() {
                ClientModeManager.this.mClientModeImpl.setOperationalMode(4, null);
                if (ClientModeManager.this.mClientInterfaceName != null) {
                    ClientModeManager.this.mWifiNative.teardownInterface(ClientModeManager.this.mClientInterfaceName);
                    ClientModeManager.this.mClientInterfaceName = null;
                    ClientModeManager.this.mIfaceIsUp = false;
                }
                ClientModeManager.this.updateWifiState(1, 0);
                ClientModeManager.this.mStateMachine.quitNow();
            }
        }
    }
}
